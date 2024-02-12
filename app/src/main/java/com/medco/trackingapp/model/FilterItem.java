package com.medco.trackingapp.model;

import androidx.core.util.Pair;

public class FilterItem {
	private Pair<Long, Long> pair;
	private String label;
	private String wellDocId;
	private String wellName;

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

	public String getWellDocId() {
		return wellDocId;
	}

	public void setWellDocId(String wellDocId) {
		this.wellDocId = wellDocId;
	}

	public String getWellName() {
		return wellName;
	}

	public void setWellName(String wellName) {
		this.wellName = wellName;
	}
}
