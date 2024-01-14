package com.medco.trackingapp.adapter.binding;

import androidx.databinding.BindingAdapter;

import com.google.android.material.textfield.TextInputLayout;
import com.medco.trackingapp.model.viewmodel.ManageUserItem;

import java.util.Objects;

public class ManageUserBindAdapter {
	@BindingAdapter("validateTilManageUserName")
	public static void validateTilManageUserName(TextInputLayout til, ManageUserItem item) {
		til.setError(null);
		if (item == null || item.getName() == null) return;
		if (Objects.equals(item.getName(), "") || item.getName().length() > 5) return;

		til.setError("Minimal 5 karakter");
	}

	@BindingAdapter("validateTilManageUserEmail")
	public static void validateTilManageUserEmail(TextInputLayout til, ManageUserItem item) {
		til.setError(null);
		if (item == null || item.getName() == null) return;

		til.setError("Email tidak valid");
	}
}
