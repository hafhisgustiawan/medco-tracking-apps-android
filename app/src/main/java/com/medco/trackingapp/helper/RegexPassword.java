package com.medco.trackingapp.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPassword {
	public boolean matchPass(String password) {
		// Pola regex ini memerlukan minimal 8 karakter dan setidaknya satu huruf kecil, satu huruf besar, satu digit, dan satu karakter khusus (@, #, $, %, ^, &, +, =, atau !). Anda dapat menyesuaikan pola regex sesuai dengan kebutuhan keamanan dan kebijakan password yang Anda inginkan.
		String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

		// Buat objek pattern
		Pattern pattern = Pattern.compile(regex);

		// Buat objek matcher
		Matcher matcher = pattern.matcher(password);

		// Return true jika password sesuai dengan pola
		return matcher.matches();
	}
}
