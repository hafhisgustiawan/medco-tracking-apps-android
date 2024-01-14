package com.medco.trackingapp.model.direction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RoutesItem {
	//	private String summary;
//	private String copyrights;
	@SerializedName("legs")
	private List<LegsItem> legs;
	//	private List<Object> warnings;
//	private Bounds bounds;
	@SerializedName("overview_polyline")
	private OverviewPolyline overviewPolyline;
//	private List<Object> waypointOrder;

	/*public String getSummary(){
		return summary;
	}

	public String getCopyrights(){
		return copyrights;
	}*/

	public List<LegsItem> getLegs() {
		return legs;
	}

	/*public List<Object> getWarnings(){
		return warnings;
	}

	public Bounds getBounds(){
		return bounds;
	}*/

	public OverviewPolyline getOverviewPolyline() {
		return overviewPolyline;
	}

	/*public List<Object> getWaypointOrder(){
		return waypointOrder;
	}*/
}