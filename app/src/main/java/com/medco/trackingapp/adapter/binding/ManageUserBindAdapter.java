package com.medco.trackingapp.adapter.binding;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.medco.trackingapp.helper.ValidationEmail;
import com.medco.trackingapp.helper.ValidationPhone;
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
		if (item == null || item.getEmail() == null) return;
		if (new ValidationEmail().isValidEmail(item.getEmail())) return;
		til.setError("Email tidak valid");
	}

	@BindingAdapter("validateTilManageUserPhone")
	public static void validateTilManageUserPhone(TextInputLayout til, ManageUserItem item) {
		til.setError(null);
		if (item == null || item.getPhone() == null) return;
		if (new ValidationPhone().isValidPhone(item.getPhone())) return;
		til.setError("Telepon tidak valid, gunakan awalan +62 atau 08");
	}

	@BindingAdapter("validateManageUserBtn")
	public static void validateManageUserBtn(MaterialButton btn, ManageUserItem item) {
		btn.setEnabled(false);

		// 0) NULL CHECK
		if (item == null) return;

		if (item.getName() == null || item.getEmail() == null || item.getPhone() == null) {
			return;
		}

		if (Objects.equals(item.getName(), "") || Objects.equals(item.getEmail(), "")
			|| Objects.equals(item.getPhone(), "")) {
			return;
		}

		// 1) NAME CHECK
		if (item.getName().length() < 5) return;

		// 2) EMAIL CHECK
		if (!(new ValidationEmail().isValidEmail(item.getEmail()))) return;

		// 3) PHONE CHECK
		if (!(new ValidationPhone().isValidPhone(item.getPhone()))) return;

		// 4) ALL BYPASS AND ENABLE BUTTON
		btn.setEnabled(true);
	}
}
