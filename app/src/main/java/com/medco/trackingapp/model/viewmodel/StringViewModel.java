package com.medco.trackingapp.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StringViewModel extends ViewModel {
	private final MutableLiveData<String> stringState = new MutableLiveData<>
		("");

	public LiveData<String> getStringState() {
		return stringState;
	}

	public void setStringState(String value) {
		stringState.setValue(value);
	}
}
