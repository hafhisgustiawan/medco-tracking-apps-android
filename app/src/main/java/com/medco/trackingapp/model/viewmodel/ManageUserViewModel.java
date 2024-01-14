package com.medco.trackingapp.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ManageUserViewModel extends ViewModel {
	private final MutableLiveData<ManageUserItem> manageUserState = new MutableLiveData<>
		(new ManageUserItem());

	public LiveData<ManageUserItem> getManageUserState() {
		return manageUserState;
	}

	public void setManageUserState(ManageUserItem manageUserItem) {
		manageUserState.setValue(manageUserItem);
	}
}
