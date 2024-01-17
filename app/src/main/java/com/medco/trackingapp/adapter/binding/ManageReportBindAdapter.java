package com.medco.trackingapp.adapter.binding;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.medco.trackingapp.model.ReportItem;

import java.util.Objects;

public class ManageReportBindAdapter {

	public static boolean validateReportManageReport(ReportItem item) {
		if (item == null) return true;
		if (item.getReport() == null || Objects.equals(item.getReport(), "")) return true;
		return item.getReport().length() > 10;
	}

	@BindingAdapter("validateTilReportManageReport")
	public static void validateTilReportManageReport(TextInputLayout til,
																									 ReportItem item) {
		til.setError(null);
		if (validateReportManageReport(item)) return;
		til.setError("Minimal 10 karakter");
	}

	public static boolean validateConditionManageReport(ReportItem item) {
		if (item == null) return true;
		if (item.getCondition() == null || Objects.equals(item.getCondition(), "")) return true;
		return item.getCondition().length() > 10;
	}

	@BindingAdapter("validateTilConditionManageReport")
	public static void validateTilConditionManageReport(TextInputLayout til,
																											ReportItem item) {
		til.setError(null);
		if (validateConditionManageReport(item)) return;
		til.setError("Minimal 10 karakter");
	}

	public static boolean validateNoteManageReport(ReportItem item) {
		if (item == null) return true;
		if (item.getNote() == null || Objects.equals(item.getNote(), "")) return true;
		return item.getNote().length() > 10;
	}

	@BindingAdapter("validateTilNoteManageReport")
	public static void validateTilNoteManageReport(TextInputLayout til,
																								 ReportItem item) {
		til.setError(null);
		if (validateNoteManageReport(item)) return;
		til.setError("Minimal 10 karakter");
	}

	@BindingAdapter("validateBtnManageReport")
	public static void validateBtnManageReport(MaterialButton btn, ReportItem item) {
		btn.setEnabled(false);

		// 0) NULL CHECK
		if (item == null) return;

		if (item.getUserRef() == null || item.getWellRef() == null || item.getReport() == null ||
			item.getCondition() == null || item.getNote() == null) {
			return;
		}
		if (Objects.equals(item.getReport(), "") || Objects.equals(item.getCondition(), "") ||
			Objects.equals(item.getNote(), "")) {
			return;
		}
	}
}
