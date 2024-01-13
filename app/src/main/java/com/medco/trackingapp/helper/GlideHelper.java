package com.medco.trackingapp.helper;

import android.app.Activity;
import android.content.Context;

public class GlideHelper {

	public GlideHelper() {
	}

	public boolean isValidCtx(Context context) {
		if (context == null) {
			return false;
		}
		if (context instanceof Activity) {
			final Activity activity = (Activity) context;
			return !activity.isDestroyed() && !activity.isFinishing();
		}
		return true;
	}

}
