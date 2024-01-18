package com.medco.trackingapp.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.medco.trackingapp.model.FilterItem;

public class FilterViewModel extends ViewModel {
	private final MutableLiveData<FilterItem> filterState = new MutableLiveData
		<>(new FilterItem());

	public LiveData<FilterItem> getFilterState() {
		return filterState;
	}

	public void setFilterState(FilterItem filterItem) {
		filterState.setValue(filterItem);
	}
}
