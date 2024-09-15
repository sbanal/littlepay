package com.github.sbanal.littlepay;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TripEventServiceTest {

    @Test
    void processEvents_whenCompletedTrip_shouldWriteCompletedTripEvent() throws IOException {
        TripCostService mockTripCostService = Mockito.mock(TripCostService.class);
        TripEventReader mockTripEventReader = Mockito.mock(TripEventReader.class);
        TripCompletionEventWriter mockTripCompletionEventWriter = Mockito.mock(TripCompletionEventWriter.class);
        when(mockTripCostService.getTripCost("stop1", "stop2")).thenReturn(555.00f);
        Instant tapOnDateTime = Instant.now();
        Instant tapOffDateTime = tapOnDateTime.plusSeconds(123);
        when(mockTripEventReader.readEvents()).thenReturn(
                Arrays.asList(
                        new TripEvent(1L, tapOnDateTime, TapType.ON, "stop1", "company1", "bus1", "123123123"),
                        new TripEvent(2L, tapOffDateTime, TapType.OFF, "stop2", "company1", "bus1", "123123123")
                )
        );

        TripEventService tripEventService = new TripEventService(mockTripCostService);
        tripEventService.processEvents(mockTripEventReader, mockTripCompletionEventWriter);

        ArgumentCaptor<TripCompletionEvent> tripCompletionEventArgumentCaptor =
                ArgumentCaptor.forClass(TripCompletionEvent.class);
        verify(mockTripCompletionEventWriter).write(tripCompletionEventArgumentCaptor.capture());
        TripCompletionEvent tripCompletionEvent = tripCompletionEventArgumentCaptor.getValue();
        assertNotNull(tripCompletionEvent);
        assertEquals(tapOnDateTime, tripCompletionEvent.started());
        assertEquals(tapOffDateTime, tripCompletionEvent.finished());
        assertEquals(123, tripCompletionEvent.durationSecs());
        assertEquals("stop1", tripCompletionEvent.fromStopId());
        assertEquals("stop2", tripCompletionEvent.toStopId());
        assertEquals(555.00f, tripCompletionEvent.chargeAmount());
        assertEquals("company1", tripCompletionEvent.companyId());
        assertEquals("bus1", tripCompletionEvent.busId());
        assertEquals("123123123", tripCompletionEvent.pan());
        assertEquals(TripCompletionStatus.COMPLETED, tripCompletionEvent.status());
    }

    @Test
    void processEvents_whenIncompleteTrip_shouldWriteIncompleteTripEvent() throws IOException {
        TripCostService mockTripCostService = Mockito.mock(TripCostService.class);
        TripEventReader mockTripEventReader = Mockito.mock(TripEventReader.class);
        TripCompletionEventWriter mockTripCompletionEventWriter = Mockito.mock(TripCompletionEventWriter.class);
        when(mockTripCostService.getTripCost("stop1", null)).thenReturn(555.00f);
        Instant tapOnDateTime = Instant.now();
        when(mockTripEventReader.readEvents()).thenReturn(
                List.of(
                        new TripEvent(1L, tapOnDateTime, TapType.ON, "stop1", "company1", "bus1", "123123123")
                )
        );

        TripEventService tripEventService = new TripEventService(mockTripCostService);
        tripEventService.processEvents(mockTripEventReader, mockTripCompletionEventWriter);

        ArgumentCaptor<TripCompletionEvent> tripCompletionEventArgumentCaptor =
                ArgumentCaptor.forClass(TripCompletionEvent.class);
        verify(mockTripCompletionEventWriter).write(tripCompletionEventArgumentCaptor.capture());
        TripCompletionEvent tripCompletionEvent = tripCompletionEventArgumentCaptor.getValue();
        assertNotNull(tripCompletionEvent);
        assertEquals(tapOnDateTime, tripCompletionEvent.started());
        assertNull(tripCompletionEvent.finished());
        assertNull(tripCompletionEvent.durationSecs());
        assertEquals("stop1", tripCompletionEvent.fromStopId());
        assertNull(tripCompletionEvent.toStopId());
        assertEquals(555.00f, tripCompletionEvent.chargeAmount());
        assertEquals("company1", tripCompletionEvent.companyId());
        assertEquals("bus1", tripCompletionEvent.busId());
        assertEquals("123123123", tripCompletionEvent.pan());
        assertEquals(TripCompletionStatus.INCOMPLETE, tripCompletionEvent.status());
    }

    @Test
    void processEvents_whenTapOnSuccessivelyAndCompletedTrip_shouldWriteIncompleteAndCompleteTripEvent() throws IOException {
        TripCostService mockTripCostService = Mockito.mock(TripCostService.class);
        TripEventReader mockTripEventReader = Mockito.mock(TripEventReader.class);
        TripCompletionEventWriter mockTripCompletionEventWriter = Mockito.mock(TripCompletionEventWriter.class);
        when(mockTripCostService.getTripCost("stop1", null)).thenReturn(555.00f);
        when(mockTripCostService.getTripCost("stop1", "stop2")).thenReturn(123.00f);
        Instant tapOnDateTime = Instant.now();
        Instant tapOnDateTime2 = Instant.now().plusSeconds(3600);
        Instant tapOffDateTime2 = tapOnDateTime2.plusSeconds(600);
        when(mockTripEventReader.readEvents()).thenReturn(
                Arrays.asList(
                        new TripEvent(1L, tapOnDateTime, TapType.ON, "stop1", "company1", "bus1", "123123123"),
                        new TripEvent(2L, tapOnDateTime2, TapType.ON, "stop1", "company1", "bus2", "123123123"),
                        new TripEvent(3L, tapOffDateTime2, TapType.OFF, "stop2", "company1", "bus2", "123123123")
                )
        );

        TripEventService tripEventService = new TripEventService(mockTripCostService);
        tripEventService.processEvents(mockTripEventReader, mockTripCompletionEventWriter);

        ArgumentCaptor<TripCompletionEvent> tripCompletionEventArgumentCaptor =
                ArgumentCaptor.forClass(TripCompletionEvent.class);
        verify(mockTripCompletionEventWriter, times(2)).write(tripCompletionEventArgumentCaptor.capture());
        List<TripCompletionEvent> tripCompletionEvents = tripCompletionEventArgumentCaptor.getAllValues();
        assertNotNull(tripCompletionEvents);

        TripCompletionEvent firstCompletionEvent = tripCompletionEvents.get(0);
        assertEquals(tapOnDateTime, firstCompletionEvent.started());
        assertNull(firstCompletionEvent.finished());
        assertNull(firstCompletionEvent.durationSecs());
        assertEquals("stop1", firstCompletionEvent.fromStopId());
        assertNull(firstCompletionEvent.toStopId());
        assertEquals(555.00f, firstCompletionEvent.chargeAmount());
        assertEquals("company1", firstCompletionEvent.companyId());
        assertEquals("bus1", firstCompletionEvent.busId());
        assertEquals("123123123", firstCompletionEvent.pan());
        assertEquals(TripCompletionStatus.INCOMPLETE, firstCompletionEvent.status());

        TripCompletionEvent secondCompletionEvent = tripCompletionEvents.get(1);
        assertNotNull(secondCompletionEvent);
        assertEquals(tapOnDateTime2, secondCompletionEvent.started());
        assertEquals(tapOffDateTime2, secondCompletionEvent.finished());
        assertEquals(600, secondCompletionEvent.durationSecs());
        assertEquals("stop1", secondCompletionEvent.fromStopId());
        assertEquals("stop2", secondCompletionEvent.toStopId());
        assertEquals(123, secondCompletionEvent.chargeAmount());
        assertEquals("company1", secondCompletionEvent.companyId());
        assertEquals("bus2", secondCompletionEvent.busId());
        assertEquals("123123123", secondCompletionEvent.pan());
        assertEquals(TripCompletionStatus.COMPLETED, secondCompletionEvent.status());
    }

    @Test
    void processEvents_whenCancelledTrip_shouldWriteCancelledTripEvent() throws IOException {
        TripCostService mockTripCostService = Mockito.mock(TripCostService.class);
        TripEventReader mockTripEventReader = Mockito.mock(TripEventReader.class);
        TripCompletionEventWriter mockTripCompletionEventWriter = Mockito.mock(TripCompletionEventWriter.class);
        when(mockTripCostService.getTripCost("stop1", "stop1")).thenReturn(555.00f);
        Instant tapOnDateTime = Instant.now();
        Instant tapOffDateTime = tapOnDateTime.plusSeconds(123);
        when(mockTripEventReader.readEvents()).thenReturn(
                Arrays.asList(
                        new TripEvent(1L, tapOnDateTime, TapType.ON, "stop1", "company1", "bus1", "123123123"),
                        new TripEvent(2L, tapOffDateTime, TapType.OFF, "stop1", "company1", "bus1", "123123123")
                )
        );

        TripEventService tripEventService = new TripEventService(mockTripCostService);
        tripEventService.processEvents(mockTripEventReader, mockTripCompletionEventWriter);

        ArgumentCaptor<TripCompletionEvent> tripCompletionEventArgumentCaptor =
                ArgumentCaptor.forClass(TripCompletionEvent.class);
        verify(mockTripCompletionEventWriter).write(tripCompletionEventArgumentCaptor.capture());
        TripCompletionEvent tripCompletionEvent = tripCompletionEventArgumentCaptor.getValue();
        assertNotNull(tripCompletionEvent);
        assertEquals(tapOnDateTime, tripCompletionEvent.started());
        assertEquals(tapOffDateTime, tripCompletionEvent.finished());
        assertEquals(123, tripCompletionEvent.durationSecs());
        assertEquals("stop1", tripCompletionEvent.fromStopId());
        assertEquals("stop1", tripCompletionEvent.toStopId());
        assertEquals(555.00f, tripCompletionEvent.chargeAmount());
        assertEquals("company1", tripCompletionEvent.companyId());
        assertEquals("bus1", tripCompletionEvent.busId());
        assertEquals("123123123", tripCompletionEvent.pan());
        assertEquals(TripCompletionStatus.CANCELLED, tripCompletionEvent.status());
    }

    @Test
    void processEvents_whenTapOffOnlyTrip_shouldNotWriteAnyTripEvent() throws IOException {
        TripCostService mockTripCostService = Mockito.mock(TripCostService.class);
        TripEventReader mockTripEventReader = Mockito.mock(TripEventReader.class);
        TripCompletionEventWriter mockTripCompletionEventWriter = Mockito.mock(TripCompletionEventWriter.class);
        when(mockTripCostService.getTripCost("stop1", "stop1")).thenReturn(555.00f);
        Instant tapOnDateTime = Instant.now();
        when(mockTripEventReader.readEvents()).thenReturn(
                List.of(
                        new TripEvent(1L, tapOnDateTime, TapType.OFF, "stop1", "company1", "bus1", "123123123")
                )
        );

        TripEventService tripEventService = new TripEventService(mockTripCostService);
        tripEventService.processEvents(mockTripEventReader, mockTripCompletionEventWriter);

        verify(mockTripCompletionEventWriter, never()).write(any());
    }

}