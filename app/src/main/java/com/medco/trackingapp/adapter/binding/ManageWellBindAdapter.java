package com.medco.trackingapp.adapter.binding;

import android.annotation.SuppressLint;

import androidx.databinding.BindingAdapter;

import com.google.android.material.textfield.TextInputEditText;

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
}
