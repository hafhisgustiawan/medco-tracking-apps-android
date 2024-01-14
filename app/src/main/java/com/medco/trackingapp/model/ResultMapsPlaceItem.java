package com.medco.trackingapp.model;

import java.util.ArrayList;

public class ResultMapsPlaceItem {
	private String status;
	private ArrayList<PlaceItem> predictions;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ArrayList<PlaceItem> getPredictions() {
		return predictions;
	}

	public void setPredictions(ArrayList<PlaceItem> predictions) {
		this.predictions = predictions;
	}
}
