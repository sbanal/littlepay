package com.github.sbanal.littlepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class TripCostServiceTest {

    @ParameterizedTest
    @NullAndEmptySource
    void addTripCost_whenEmptyRouteStartStopId_shouldThrowException(String routeStartStopId) {
        TripCostService service = new TripCostService();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.addTripCost(routeStartStopId, "stop2", 1.0f));
        assertEquals("Invalid route start stop id", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void addTripCost_whenEmptyRouteEndStopId_shouldThrowException(String routeEndStopId) {
        TripCostService service = new TripCostService();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.addTripCost("stop1", routeEndStopId, 1.0f));
        assertEquals("Invalid route end stop id", ex.getMessage());
    }

    @Test
    void addTripCost_withSameStopIds_shouldThrowException() {
        TripCostService service = new TripCostService();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.addTripCost("stop1", "stop1", 1.0f));
        assertEquals("Invalid route start and route end stop id, values cannot be the same", ex.getMessage());
    }

    @Test
    void addTripCost_withNoneEmptyStopIds_shouldReturnSuccess() {
        TripCostService service = new TripCostService();
        assertDoesNotThrow(() -> service.addTripCost("stop1", "stop2", 1.0f));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getTripCost_whenEmptyRouteStartStopId_shouldThrowException(String routeStartStopId) {
        TripCostService service = createMockService();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getTripCost(routeStartStopId, "stop2"));
        assertEquals("Invalid route start stop Id '" + routeStartStopId + "'", ex.getMessage());
    }

    @Test
    void getTripCost_whenInvalidRouteEndStopId_shouldThrowException() {
        TripCostService service = createMockService();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getTripCost("stop1", "unknown_stop"));
        assertEquals("Invalid route end stop Id 'unknown_stop'", ex.getMessage());
    }

    @Test
    void getTripCost_whenInvalidRouteStartAndEndStopIdCombination_shouldThrowException() {
        TripCostService service = createMockService();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getTripCost("stop1", "stop4"));
        assertEquals("Invalid route start and route end combination", ex.getMessage());
    }

    @Test
    void getTripCost_withCompletedTrip_shouldReturnTripCost() {
        TripCostService service = createMockService();

        assertEquals(3.25f, service.getTripCost("stop1", "stop2"));
        assertEquals(3.25f, service.getTripCost("stop2", "stop1"));
        assertEquals(5.50f, service.getTripCost("stop2", "stop3"));
        assertEquals(5.50f, service.getTripCost("stop3", "stop2"));
        assertEquals(7.30f, service.getTripCost("stop1", "stop3"));
        assertEquals(7.30f, service.getTripCost("stop3", "stop1"));
        assertEquals(1.50f, service.getTripCost("stop2", "stop4"));
        assertEquals(1.50f, service.getTripCost("stop4", "stop2"));
    }

    @Test
    void getTripCost_withCancelledTrip_shouldReturnTripCost() {
        TripCostService service = createMockService();

        assertEquals(0.0f, service.getTripCost("stop1", "stop1"));
        assertEquals(0.0f, service.getTripCost("stop2", "stop2"));
        assertEquals(0.0f, service.getTripCost("stop3", "stop3"));
        assertEquals(0.0f, service.getTripCost("stop4", "stop4"));
    }

    @Test
    void getTripCost_withIncompleteTrip_shouldReturnTripCost() {
        TripCostService service = createMockService();
        service.calculateIncompleteTripCost();

        assertEquals(7.30f, service.getTripCost("stop1", null));
        assertEquals(5.50f, service.getTripCost("stop2", null));
        assertEquals(7.30f, service.getTripCost("stop3", null));
        assertEquals(1.50f, service.getTripCost("stop4", null));
    }

    private TripCostService createMockService() {
        TripCostService service = new TripCostService();
        service.addTripCost("stop1", "stop2", 3.25f);
        service.addTripCost("stop2", "stop3", 5.50f);
        service.addTripCost("stop1", "stop3", 7.30f);
        service.addTripCost("stop2", "stop4", 1.50f);
        return service;
    }

}