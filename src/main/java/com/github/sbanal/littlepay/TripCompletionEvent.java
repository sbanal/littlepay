package com.github.sbanal.littlepay;

import java.time.Instant;

record TripCompletionEvent(Instant started, Instant finished, Long durationSecs, String fromStopId, String toStopId,
                           float chargeAmount, String companyId, String busId, String pan, TripCompletionStatus status) {

}
