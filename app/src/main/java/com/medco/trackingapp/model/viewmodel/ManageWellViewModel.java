package com.medco.trackingapp.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.medco.trackingapp.model.WellItem;

public class ManageWellViewModel extends ViewModel {
	private final MutableLiveData<WellItem> manageWellState = new MutableLiveData<>
		(new WellItem());

	public LiveData<WellItem> getManageWellState() {
		return manageWellState;
	}

	public void setManageWellState(WellItem wellItem) {
		manageWellState.setValue(wellItem);
	}
}
