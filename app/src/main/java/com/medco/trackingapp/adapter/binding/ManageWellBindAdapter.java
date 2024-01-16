package com.medco.trackingapp.adapter.binding;

import android.annotation.SuppressLint;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.medco.trackingapp.model.WellItem;

import java.util.Objects;

public class ManageWellBindAdapter {
	@SuppressLint("SetTextI18n")
	@BindingAdapter("initCategoriesWell")
	public static void initCategoriesWell(TextInputEditText et, String cat) {
		if (cat == null) return;

		if (cat.equals("as")) {
			et.setText("AS (Alur Siwah)");
			return;
		}

		if (cat.equals("jr")) {
			et.setText("JR (Julok Rayeuk)");
			return;
		}

		et.setText("Other Well (Sumur Tua)");
	}

	public static boolean validateNameManageWell(WellItem item) {
		if (item == null) return true;
		if (item.getName() == null || Objects.equals(item.getName(), "")) return true;
		return item.getName().length() > 5;
	}

	@BindingAdapter("validateTilNameManageWell")
	public static void validateTilNameManageWell(TextInputLayout til, WellItem item) {
		til.setError(null);
		if (validateNameManageWell(item)) return;
		til.setError("Minimal 5 karakter");
	}

	public static boolean validateCategoriesManageWell(WellItem item) {
		if (item == null) return true;
		if (item.getCategory() == null || Objects.equals(item.getCategory(), "")) return true;
		return Objects.equals(item.getCategory(), "as") || Objects.equals(item.getCategory(),
			"jr") || Objects.equals(item.getCategory(), "otherwell");
	}

	@BindingAdapter("validateTilCategoriesManageWell")
	public static void validateTilCategoriesManageWell(TextInputLayout til, WellItem item) {
		til.setError(null);
		if (validateCategoriesManageWell(item)) return;
		til.setError("Kategori tidak sesuai");
	}

	public static boolean validateDescriptionManageWell(WellItem item) {
		if (item == null) return true;
		if (item.getDescription() == null || Objects.equals(item.getDescription(), "")) return true;
		return item.getDescription().length() > 20;
	}

	@BindingAdapter("validateTilDescriptionManageWell")
	public static void validateTilDescriptionManageWell(TextInputLayout til, WellItem item) {
		til.setError(null);
		if (validateDescriptionManageWell(item)) return;
		til.setError("Minimal 20 karakter");
	}

	public static boolean validateAddressManageWell(WellItem item) {
		if (item == null) return true;
		if (item.getAddress() == null || Objects.equals(item.getAddress(), "")) return true;
		return item.getAddress().length() > 10;
	}

	@BindingAdapter("validateTilAddressManageWell")
	public static void validateTilAddressManageWell(TextInputLayout til, WellItem item) {
		til.setError(null);
		if (validateAddressManageWell(item)) return;
		til.setError("Minimal 10 karakter");
	}

	public static boolean validateLatitude(Double num) {
		if (num == null) return false;
		return num >= -90 && num <= 90;
	}

	public static boolean validateLatitudeManageWell(WellItem item) {
		if (item == null) return true;
		if (item.getLocation() == null) return true;
		if (item.getLocation().getLatitude() == 0) return true;
		return validateLatitude(item.getLocation().getLatitude());
	}

	@BindingAdapter("validateTilLatitudeManageWell")
	public static void validateTilLatitudeManageWell(TextInputLayout til, WellItem item) {
		til.setError(null);
		if (validateLatitudeManageWell(item)) return;
		til.setError("Latitude tidak valid");
	}

	public static boolean validateLongitude(Double num) {
		if (num == null) return false;
		return num >= -180 && num <= 180;
	}

	public static boolean validateLongitudeManageWell(WellItem item) {
		if (item == null) return true;
		if (item.getLocation() == null) return true;
		if (item.getLocation().getLongitude() == 0) return true;
		return validateLongitude(item.getLocation().getLongitude());
	}

	@BindingAdapter("validateTilLongitudeManageWell")
	public static void validateTilLongitudeManageWell(TextInputLayout til, WellItem item) {
		til.setError(null);
		if (validateLongitudeManageWell(item)) return;
		til.setError("Longitude tidak valid");
	}

	@BindingAdapter("validateBtnManageWell")
	public static void validateBtnManageWell(MaterialButton btn, WellItem item) {
		btn.setEnabled(false);

		// 0) NULL CHECK
		if (item == null) return;

		if (item.getName() == null || item.getCategory() == null || item.getDescription() == null
			|| item.getLocation() == null || item.getAddress() == null) {
			return;
		}
		if (Objects.equals(item.getName(), "") || Objects.equals(item.getCategory(), "") ||
			Objects.equals(item.getDescription(), "") || Objects.equals(item.getAddress(), "")) {
			return;
		}

		if (item.getLocation().getLongitude() == 0 || item.getLocation().getLatitude() == 0) return;

		if (!validateNameManageWell(item)) return;
		if (!validateCategoriesManageWell(item)) return;
		if (!validateDescriptionManageWell(item)) return;
		if (!validateAddressManageWell(item)) return;
		if (!validateLatitudeManageWell(item)) return;
		if (!validateLongitudeManageWell(item)) return;

		btn.setEnabled(true);
	}
}
