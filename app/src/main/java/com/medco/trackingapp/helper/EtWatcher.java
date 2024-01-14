package com.medco.trackingapp.helper;

import android.text.Editable;
import android.text.TextWatcher;

public class EtWatcher implements TextWatcher {

	private final Callbackable<String> func;

	public EtWatcher(Callbackable<String> func) {
		this.func = func;
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		func.call(charSequence != null ? charSequence.toString() : "");
	}

	@Override
	public void afterTextChanged(Editable editable) {

	}
}
