package com.medco.trackingapp.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.medco.trackingapp.model.PrivateDataItem;

public class PrivateDataViewModel extends ViewModel {
	private final MutableLiveData<PrivateDataItem> privateDataState = new
		MutableLiveData<>(new PrivateDataItem());

	public LiveData<PrivateDataItem> getPrivateData() {
		return privateDataState;
	}

	public void setPrivateData(PrivateDataItem privateData) {
		privateDataState.setValue(privateData);
	}
}
