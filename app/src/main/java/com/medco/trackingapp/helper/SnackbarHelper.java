package com.medco.trackingapp.helper;

import android.graphics.Color;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {
	public View mView;
	public View mAnchor;

	public SnackbarHelper(View view, View anchor) {
		this.mView = view;
		this.mAnchor = anchor;
	}

	public void show(String message, int length) {
		Snackbar snackbar;
		if (length == Snackbar.LENGTH_INDEFINITE) {
			snackbar = Snackbar.make(mView, message, length)
				.setAction("Ok", view -> {
				})
				.setActionTextColor(Color.RED);
		} else {
			snackbar = Snackbar.make(mView, message, length);
		}
		if (mAnchor != null) snackbar.setAnchorView(mAnchor);
		snackbar.show();
	}
}
