package com.medco.trackingapp.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.medco.trackingapp.model.ReportItem;

public class ManageReportViewModel extends ViewModel {
	private final MutableLiveData<ReportItem> manageReportState = new MutableLiveData
		<>(new ReportItem());

	public LiveData<ReportItem> getManageReportState() {
		return manageReportState;
	}

	public void setManageReportState(ReportItem reportItem) {
		manageReportState.setValue(reportItem);
	}
}
