package com.github.sbanal.littlepay;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TripEventService {

    private static final Logger LOGGER = Logger.getLogger(TripEventService.class.getName());
    private final TripCostService tripCostService;

    public TripEventService(TripCostService tripCostService) {
        this.tripCostService = tripCostService;
    }

    public void processEvents(TripEventReader tripEventsReader,
                              TripCompletionEventWriter tripCompletionEventsWriter) throws IOException {
        Map<String, TripEvent> customerTapOnTripEvent = new HashMap<>();
        List<TripCompletionEvent> tripCompletionEvents = new ArrayList<>();

        try (TripEventReader reader = tripEventsReader; TripCompletionEventWriter writer = tripCompletionEventsWriter) {

            List<TripEvent> tripEventList = reader.readEvents();
            tripEventList.forEach(tripEvent -> {
                TripEvent tapOnTripEvent = customerTapOnTripEvent.get(tripEvent.pan());
                if (tripEvent.tapType() == TapType.ON) {
                    if (tapOnTripEvent == null) {
                        // customer started a new trip
                        customerTapOnTripEvent.put(tripEvent.pan(), tripEvent);
                    } else {
                        // customer started a new trip but did not tap off previously
                        TripCompletionEvent completionEvent = createIncompleteTripEvent(tapOnTripEvent);
                        tripCompletionEvents.add(completionEvent);
                        customerTapOnTripEvent.put(tripEvent.pan(), tripEvent);
                    }
                } else {
                    if (tapOnTripEvent == null) {
                        // anomaly, customer tap-off without a tap-on event
                        LOGGER.warning("skipping, customer tap-off event without a tap-on event " + tripEvent);
                    } else {
                        // customer completed a trip by tap-off
                        TripCompletionEvent completionEvent = createCompleteTripEvent(tapOnTripEvent, tripEvent);
                        tripCompletionEvents.add(completionEvent);
                        customerTapOnTripEvent.remove(tripEvent.pan());
                    }
                }
            });

            // customer did not tap-off
            for (TripEvent startTripEvent : customerTapOnTripEvent.values()) {
                TripCompletionEvent completionEvent = createIncompleteTripEvent(startTripEvent);
                tripCompletionEvents.add(completionEvent);
                customerTapOnTripEvent.remove(startTripEvent.pan());
            }

            for (TripCompletionEvent completionEvent : tripCompletionEvents) {
                writer.write(completionEvent);
            }
        }
    }

    private TripCompletionEvent createIncompleteTripEvent(TripEvent startTripEvent) {
        TripCompletionEvent completionEvent = new TripCompletionEvent(
                startTripEvent.dateTimeUtc(),
                null,
                null,
                startTripEvent.stopId(),
                null,
                tripCostService.getTripCost(startTripEvent.stopId(), null),
                startTripEvent.companyId(),
                startTripEvent.busId(),
                startTripEvent.pan(),
                TripCompletionStatus.INCOMPLETE
        );
        return completionEvent;
    }

    private TripCompletionEvent createCompleteTripEvent(TripEvent startTripEvent, TripEvent endTripEvent) {
        long tripDurationInSeconds = Duration.between(startTripEvent.dateTimeUtc(), endTripEvent.dateTimeUtc())
                .getSeconds();
        TripCompletionStatus completionStatus = StringUtils.equals(startTripEvent.stopId(), endTripEvent.stopId()) ?
                TripCompletionStatus.CANCELLED : TripCompletionStatus.COMPLETED;
        TripCompletionEvent completionEvent = new TripCompletionEvent(
                startTripEvent.dateTimeUtc(),
                endTripEvent.dateTimeUtc(),
                tripDurationInSeconds,
                startTripEvent.stopId(),
                endTripEvent.stopId(),
                tripCostService.getTripCost(startTripEvent.stopId(), endTripEvent.stopId()),
                startTripEvent.companyId(),
                startTripEvent.busId(),
                startTripEvent.pan(),
                completionStatus
        );
        return completionEvent;
    }

}
