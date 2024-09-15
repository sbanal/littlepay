package com.github.sbanal.littlepay;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class TripService {
    private static final float CANCELLED_TRIP_COST = 0.0f;

    private Set<String> routeStops = new HashSet<>();
    private Map<TripRoute, Float> tripCostTable = new HashMap<>();
    private Map<String, Set<String>> routeEdges = new HashMap<>();

    public void addTripCost(String routeStartStopId, String routeEndStopId, Float tripCost) {
        if (StringUtils.isEmpty(routeStartStopId)) {
            throw new IllegalArgumentException("Invalid route start stop id");
        }
        if (StringUtils.isEmpty(routeEndStopId)) {
            throw new IllegalArgumentException("Invalid route end stop id");
        }
        if (StringUtils.equals(routeStartStopId, routeEndStopId)) {
            throw new IllegalArgumentException("Invalid route start and route end stop id, values cannot be the same");
        }
        this.routeStops.add(routeStartStopId);
        this.routeStops.add(routeEndStopId);
        this.tripCostTable.put(new TripRoute(routeStartStopId, routeEndStopId), tripCost);
        this.tripCostTable.put(new TripRoute(routeEndStopId, routeStartStopId), tripCost);
        addEdge(routeStartStopId, routeEndStopId);
        addEdge(routeEndStopId, routeStartStopId);
    }

    private void addEdge(String routeStartStopId, String routeEndStopId) {
        Set<String> routeEdges = this.routeEdges.get(routeStartStopId);
        if (routeEdges == null) {
            this.routeEdges.put(routeStartStopId, new HashSet<>(Arrays.asList(routeEndStopId)));
        } else {
            routeEdges.add(routeEndStopId);
        }
    }

    public void calculateIncompleteTripCost() {
        for (String routeStartStopId : this.routeStops) {
            Float maxCost = Float.MIN_VALUE;
            for (String routeEndStopId : this.routeEdges.get(routeStartStopId)) {
                maxCost = Math.max(this.tripCostTable.get(new TripRoute(routeStartStopId, routeEndStopId)), maxCost);
            }
            this.tripCostTable.put(new TripRoute(routeStartStopId, null), maxCost);
        }
    }

    public Float getTripCost(String routeStartStopId, String routeEndStopId) {
        if (!this.routeStops.contains(routeStartStopId)) {
            throw new IllegalArgumentException("Invalid route start stop Id '" + routeStartStopId + "'");
        }
        if (routeEndStopId != null && !this.routeStops.contains(routeEndStopId)) {
            throw new IllegalArgumentException("Invalid route end stop Id '" + routeEndStopId + "'");
        }

        Float tripCost;
        if (this.routeStops.contains(routeEndStopId)) {
            if (StringUtils.equals(routeStartStopId, routeEndStopId)) {
                tripCost = CANCELLED_TRIP_COST;
            } else {
                tripCost = this.tripCostTable.get(new TripRoute(routeStartStopId, routeEndStopId));
            }
        } else {
            tripCost = this.tripCostTable.get(new TripRoute(routeStartStopId, null));
        }
        if (tripCost == null) {
            throw new IllegalArgumentException("Invalid route start and route end combination");
        }
        return tripCost;
    }

}
