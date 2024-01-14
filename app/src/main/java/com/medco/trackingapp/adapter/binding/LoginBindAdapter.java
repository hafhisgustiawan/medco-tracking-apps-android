package com.medco.trackingapp.adapter.binding;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.medco.trackingapp.helper.RegexPassword;
import com.medco.trackingapp.helper.ValidationEmail;
import com.medco.trackingapp.model.LoginItem;

import java.util.Objects;

public class LoginBindAdapter {
	@BindingAdapter("validateTilEmailLogin")
	public static void validateTilEmailLogin(TextInputLayout til, LoginItem loginItem) {
		til.setError(null);
		if (loginItem == null || loginItem.getEmail() == null) return;

		if (Objects.equals(loginItem.getEmail(), "")) return;

		if (new ValidationEmail().isValidEmail(loginItem.getEmail())) return;

		til.setError("Email tidak valid");
	}

	@BindingAdapter("validateTilPasswordLogin")
	public static void validateTilPasswordLogin(TextInputLayout til, LoginItem loginItem) {
		til.setError(null);
		if (loginItem == null || loginItem.getPassword() == null) return;

		if (Objects.equals(loginItem.getPassword(), "")) return;

		if (new RegexPassword().matchPass(loginItem.getPassword())) return;

		til.setError("Setidaknya gunakan satu huruf kecil, satu huruf besar, " +
			"satu digit, dan satu karakter khusus (@, #, $, %, ^, &, +, =, atau !)");
	}

	@BindingAdapter("validateBtnLogin")
	public static void validateBtnLogin(MaterialButton btn, LoginItem loginItem) {
		btn.setEnabled(false);

		// 0) NULL CHECK
		if (loginItem == null) return;

		if (loginItem.getEmail() == null || loginItem.getPassword() == null) {
			return;
		}

		if (Objects.equals(loginItem.getEmail(), "") || Objects.equals(loginItem
			.getPassword(), "")) {
			return;
		}

		// 1) PASSWORD
		RegexPassword regexPassword = new RegexPassword();
		if (!regexPassword.matchPass(loginItem.getPassword())) return;

		// 2) NEW EMAIL
		if (!(new ValidationEmail().isValidEmail(loginItem.getEmail()))) return;

		// 3) ALL BYPASS AND ENABLE BUTTON
		btn.setEnabled(true);
	}
}
