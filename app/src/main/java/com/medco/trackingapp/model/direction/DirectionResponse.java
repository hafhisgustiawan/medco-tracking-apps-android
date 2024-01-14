package com.medco.trackingapp.model.direction;

import java.util.List;

public class DirectionResponse {
	private List<RoutesItem> routes;
	private List<GeocodedWaypointsItem> geocodedWaypoints;
	private String status;

	public List<RoutesItem> getRoutes() {
		return routes;
	}

	public List<GeocodedWaypointsItem> getGeocodedWaypoints() {
		return geocodedWaypoints;
	}

	public String getStatus() {
		return status;
	}
}