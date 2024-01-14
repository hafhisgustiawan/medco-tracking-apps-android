package com.medco.trackingapp.model.direction;

import java.util.List;

public class GeocodedWaypointsItem {
	private List<String> types;
	private String geocoderStatus;
	private String placeId;

	public List<String> getTypes() {
		return types;
	}

	public String getGeocoderStatus() {
		return geocoderStatus;
	}

	public String getPlaceId() {
		return placeId;
	}
}