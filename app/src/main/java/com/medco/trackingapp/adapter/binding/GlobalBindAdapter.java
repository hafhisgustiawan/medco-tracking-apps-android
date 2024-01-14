package com.medco.trackingapp.adapter.binding;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.medco.trackingapp.model.UserItem;

import java.util.Objects;

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

	@BindingAdapter("imgBtnVisibleForAdmin")
	public static void imgBtnVisibleForAdmin(ImageButton imgBtn, DocumentReference userRef) {
		imgBtn.setVisibility(View.GONE);
		if (userRef == null) return;
		userRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			UserItem userItem = task.getResult().toObject(UserItem.class);
			if (userItem == null) return;
			if (!Objects.equals(userItem.getRole(), "admin")) return;
			imgBtn.setVisibility(View.VISIBLE);
		});
	}
}
