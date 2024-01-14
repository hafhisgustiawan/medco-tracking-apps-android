package com.medco.trackingapp.adapter.binding;

import android.text.InputType;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.medco.trackingapp.helper.RegexPassword;
import com.medco.trackingapp.helper.ValidationEmail;
import com.medco.trackingapp.helper.ValidationPhone;
import com.medco.trackingapp.model.PrivateDataItem;

import java.util.Objects;

public class UpdatePrivateDataBindAdapter {
	@BindingAdapter("validateUpdatePrivateDataNewData")
	public static void validateUpdatePrivateDataNewData(TextInputLayout til,
																											PrivateDataItem item) {
		til.setError(null);
		if (item == null || item.getNewData() == null || item.getType() == null) return;

		if (Objects.equals(item.getNewData(), "")) return;

		if (Objects.equals(item.getType(), "name") && item.getNewData()
			.length() < 5) {
			til.setError("Minimal 5 karakter");
		}

		ValidationEmail validationEmail = new ValidationEmail();
		if (Objects.equals(item.getType(), "email") && !validationEmail.isValidEmail
			(item.getNewData())) {
			til.setError("Email tidak valid");
		}

		ValidationPhone validationPhone = new ValidationPhone();
		if (Objects.equals(item.getType(), "phone") && !validationPhone.isValidPhone
			(item.getNewData())) {
			til.setError("Telepon tidak valid, gunakan awalan +62 atau 08");
		}
	}

	@BindingAdapter("validateUpdatePrivateDataPwd")
	public static void validateUpdatePrivateDataPwd(TextInputLayout til,
																									PrivateDataItem item) {
		til.setError(null);
		if (item == null || item.getPassword() == null) return;

		RegexPassword regexPassword = new RegexPassword();

		if (Objects.equals(item.getPassword(), "") || regexPassword.matchPass(item
			.getPassword())) {
			return;
		}
		til.setError("Setidaknya gunakan satu huruf kecil, satu huruf besar, " +
			"satu digit, dan satu karakter khusus (@, #, $, %, ^, &, +, =, atau !)");
	}

	@BindingAdapter("validateUpdatePrivateDataBtn")
	public static void validateUpdatePrivateDataBtn(MaterialButton btn,
																									PrivateDataItem item) {
		btn.setEnabled(false);

		// 0) NULL CHECK
		if (item == null) return;

		if (item.getNewData() == null || item.getType() == null || item
			.getPassword() == null) {
			return;
		}

		if (Objects.equals(item.getNewData(), "") || Objects.equals(item
			.getType(), "") || Objects.equals(item.getPassword(), "")) {
			return;
		}

		// 1) PASSWORD
		RegexPassword regexPassword = new RegexPassword();
		if (!regexPassword.matchPass(item.getPassword())) return;

		// 2) NEW NAME
		if (Objects.equals(item.getType(), "name") && item.getNewData()
			.length() < 5) {
			return;
		}

		// 3) NEW EMAIL
		ValidationEmail validationEmail = new ValidationEmail();
		if (Objects.equals(item.getType(), "email") && !validationEmail.isValidEmail
			(item.getNewData())) {
			return;
		}

		// 3) NEW EMAIL
		ValidationPhone validationPhone = new ValidationPhone();
		if (Objects.equals(item.getType(), "phone") && !validationPhone.isValidPhone
			(item.getNewData())) {
			return;
		}

		// 5) ALL BYPASS AND ENABLE BUTTON
		btn.setEnabled(true);
	}

	@BindingAdapter("validateEtNewDataType")
	public static void validateEtNewDataType(TextInputEditText et, PrivateDataItem item) {
		if (item == null || item.getType() == null) return;
		if (Objects.equals(item.getType(), "name")) {
			et.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
			return;
		}
		if (Objects.equals(item.getType(), "email")) {
			et.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			return;
		}

		et.setInputType(InputType.TYPE_CLASS_PHONE);
	}
}
