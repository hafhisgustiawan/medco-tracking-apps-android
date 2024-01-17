package com.medco.trackingapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class ReportItem {
	private DocumentReference userRef;
	private DocumentReference wellRef;
	private String report;
	private String condition;
	private String note;
	private List<String> images;
	private Timestamp createdAt;

	public DocumentReference getWellRef() {
		return wellRef;
	}

	public void setWellRef(DocumentReference wellRef) {
		this.wellRef = wellRef;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public DocumentReference getUserRef() {
		return userRef;
	}

	public void setUserRef(DocumentReference userRef) {
		this.userRef = userRef;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}
}
