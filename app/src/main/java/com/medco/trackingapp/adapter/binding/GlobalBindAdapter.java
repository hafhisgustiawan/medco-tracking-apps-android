package com.medco.trackingapp.adapter.binding;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;

public class GlobalBindAdapter {
	private static Drawable getProgressBarDrawable(final Context context) {
		TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.progressBarStyleSmall, value,
			false);
		int progressBarStyle = value.data;
		int[] attributes = new int[]{android.R.attr.indeterminateDrawable};
		TypedArray typedArray = context.obtainStyledAttributes(progressBarStyle, attributes);
		Drawable drawable = typedArray.getDrawable(0);
		typedArray.recycle();

		return drawable;
	}

	@BindingAdapter(value = {"android:progressVisible"})
	public static void setButtonLoading(MaterialButton button, boolean loading) {
		if (loading) {
			Drawable drawable = button.getIcon();
			if (!(drawable instanceof Animatable)) {
				drawable = getProgressBarDrawable(button.getContext());

				button.setIcon(drawable);
			}
			((Animatable) drawable).start();
		} else {
			button.setIcon(null);
		}
	}
}
