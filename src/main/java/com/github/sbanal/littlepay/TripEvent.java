package com.github.sbanal.littlepay;

import java.time.Instant;

record TripEvent(Long id, Instant dateTimeUtc, TapType tapType, String stopId, String companyId, String busId, String pan) {
}
