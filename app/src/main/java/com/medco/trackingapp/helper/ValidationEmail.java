package com.medco.trackingapp.helper;

public class ValidationEmail {
	public boolean isValidEmail(String email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email)
			.matches();
	}
}
