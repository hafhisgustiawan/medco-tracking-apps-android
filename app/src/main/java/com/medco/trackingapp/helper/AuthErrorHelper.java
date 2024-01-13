package com.medco.trackingapp.helper;

public class AuthErrorHelper {

	public AuthErrorHelper() {
	}

	//jika selain 0 maka tampilkan error di edit text, jika 0 maka toast aja
	public static int isAuthErrorCritical(String errorCode) {
		switch (errorCode) {
			case "ERROR_INVALID_EMAIL":
				return 1;

			case "ERROR_EMAIL_ALREADY_IN_USE":
				return 1;

			case "ERROR_WRONG_PASSWORD":
				return 2;

			case "ERROR_WEAK_PASSWORD":
				return 2;

			default:
				return 0;
		}
	}

	public static String getAuthErrorDesc(String errorCode) {
		switch (errorCode) {
			case "ERROR_INVALID_CUSTOM_TOKEN":
				return "Format token salah. Silakan periksa dokumentasi.";

			case "ERROR_CUSTOM_TOKEN_MISMATCH":
				return "Token khusus sesuai dengan audiens yang berbeda.";

			case "ERROR_INVALID_CREDENTIAL":
				return "Kredensial autentikasi yang diberikan salah format atau telah kedaluwarsa.";

			case "ERROR_INVALID_EMAIL":
				return "Format email salah.";

			case "ERROR_WRONG_PASSWORD":
				return "Kata kunci salah";

			case "ERROR_USER_MISMATCH":
				return "Kredensial yang diberikan tidak sesuai dengan pengguna yang masuk sebelumnya.";

			case "ERROR_REQUIRES_RECENT_LOGIN":
				return "Operasi ini sensitif dan memerlukan otentikasi terbaru. Masuk lagi sebelum mencoba lagi permintaan ini.";

			case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
				return "Akun sudah ada dengan alamat email yang sama tetapi kredensial masuk yang berbeda. Masuk menggunakan penyedia yang terkait dengan alamat email ini.";

			case "ERROR_EMAIL_ALREADY_IN_USE":
				return "Alamat email sudah digunakan oleh akun lain.";

			case "ERROR_CREDENTIAL_ALREADY_IN_USE":
				return "Kredensial ini sudah dikaitkan dengan akun pengguna yang berbeda.";

			case "ERROR_USER_DISABLED":
				return "Akun pengguna telah dinonaktifkan oleh administrator.";

			case "ERROR_USER_TOKEN_EXPIRED":
				return "Kredensial pengguna tidak lagi valid. Pengguna harus masuk lagi.";

			case "ERROR_USER_NOT_FOUND":
				return "Tidak ada catatan pengguna yang sesuai dengan pengenal ini. Pengguna mungkin telah dihapus.";

			case "ERROR_INVALID_USER_TOKEN":
				return "Kredensial pengguna tidak lagi valid, pengguna harus masuk lagi.";

			case "ERROR_OPERATION_NOT_ALLOWED":
				return "Operasi ini tidak diperbolehkan. Anda harus mengaktifkan layanan ini di konsol.";

			case "ERROR_WEAK_PASSWORD":
				return "Kata sandi tidak valid, setidaknya harus 6 karakter";

			default:
				return "Periksa koneksi anda";
		}
	}

}
