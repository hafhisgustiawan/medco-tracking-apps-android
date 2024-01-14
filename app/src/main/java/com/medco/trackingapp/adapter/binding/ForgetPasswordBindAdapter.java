package com.medco.trackingapp.adapter.binding;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.medco.trackingapp.helper.ValidationEmail;

public class ForgetPasswordBindAdapter {
	@BindingAdapter("validateTilEmailForgetPassword")
	public static void validateTilEmailForgetPassword(TextInputLayout til, String email) {
		til.setError(null);
		if (email == null || email.equals("")) return;
		if (new ValidationEmail().isValidEmail(email)) return;
		til.setError("Email tidak valid");
	}

	@BindingAdapter("validateForgetPasswordBtn")
	public static void validateForgetPasswordBtn(MaterialButton btn, String email) {
		btn.setEnabled(false);

		if (email == null || email.equals("")) return;

		if (!(new ValidationEmail().isValidEmail(email))) return;

		btn.setEnabled(true);
	}
}
