package com.medco.trackingapp.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.medco.trackingapp.model.LoginItem;

public class LoginViewModel extends ViewModel {
	private final MutableLiveData<LoginItem> loginState = new MutableLiveData<>
		(new LoginItem());

	public LiveData<LoginItem> getLoginState() {
		return loginState;
	}

	public void setLoginState(LoginItem loginItem) {
		loginState.setValue(loginItem);
	}
}
