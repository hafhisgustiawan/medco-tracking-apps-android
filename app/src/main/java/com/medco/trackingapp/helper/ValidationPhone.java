package com.medco.trackingapp.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationPhone {
	public boolean isValidPhone(String phoneNumber) {
		//		Pola regex ini mengizinkan nomor telepon dalam format internasional yang diawali dengan "+62" atau nomor lokal yang diawali dengan "0". Panjang nomor telepon dapat antara 8 hingga 15 digit. Anda dapat menyesuaikan pola regex sesuai dengan format nomor telepon yang ingin Anda validasi.
		// Buat pola regex untuk nomor telepon Indonesia
		String regex = "^(\\+62|0)(\\d{8,15})$";

		// Buat objek pattern
		Pattern pattern = Pattern.compile(regex);

		// Buat objek matcher
		Matcher matcher = pattern.matcher(phoneNumber);

		// Return true jika nomor telepon sesuai dengan pola
		return matcher.matches();
	}
}
