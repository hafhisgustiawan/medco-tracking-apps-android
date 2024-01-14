package com.medco.trackingapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class ReportItem {
	private DocumentReference wellRef;
	private String report;
	private String condition;
	private String photo;
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

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
}
