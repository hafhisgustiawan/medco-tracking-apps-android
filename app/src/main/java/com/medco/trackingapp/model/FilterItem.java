package com.medco.trackingapp.model;

import androidx.core.util.Pair;

public class FilterItem {
	private Pair<Long, Long> pair;
	private String label;

	public Pair<Long, Long> getPair() {
		return pair;
	}

	public void setPair(Pair<Long, Long> pair) {
		this.pair = pair;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
