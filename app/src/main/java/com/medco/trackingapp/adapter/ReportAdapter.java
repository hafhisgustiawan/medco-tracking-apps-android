package com.medco.trackingapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.DocumentSnapshot;
import com.medco.trackingapp.R;
import com.medco.trackingapp.activity.ReportActivity;
import com.medco.trackingapp.databinding.ListProgressVerticalBinding;
import com.medco.trackingapp.databinding.ListReportBinding;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.model.ReportItem;

public class ReportAdapter extends FirestorePagingAdapter<ReportItem, ReportAdapter
	.ViewHolder> {

	public static final String TAG = "ReportAdapter";
	private static final int ITEM = 0;
	private static final int LOADING = 1;
	public Animation animation;
	public Context mContext;
	private boolean isLoadingAdded = false;
	private StateChangeListener stateListener;

	public void setOnStateChangeListener(StateChangeListener stateListener) {
		this.stateListener = stateListener;
	}

	public interface StateChangeListener {
		void stateChange(Exception e);
	}

	public ReportAdapter(@NonNull FirestorePagingOptions<ReportItem> options,
											 Context context) {
		super(options);
		this.mContext = context;
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
	}

	@Override
	protected void onBindViewHolder(@NonNull ReportAdapter.ViewHolder holder, int position,
																	@NonNull ReportItem model) {
		if (getItemViewType(position) == LOADING) return;

		holder.itemView.setAnimation(animation);
		holder.binding.setReportItem(model);

		DocumentSnapshot snapshot = getItem(position);
		if (snapshot == null) return;

		holder.itemView.setOnClickListener(view -> {
			Intent intent = new Intent(mContext, ReportActivity.class);
			intent.putExtra("path", snapshot.getReference().getPath());
			mContext.startActivity(intent);
		});
	}

	@NonNull
	@Override
	public ReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if (viewType == ITEM) {
			return new ViewHolder(ListReportBinding
				.inflate(inflater, parent, false), null);
		} else {
			return new ViewHolder(null, ListProgressVerticalBinding
				.inflate(inflater, parent, false));
		}
	}

	@Override
	public int getItemViewType(int position) {
		return (position == getItemCount() - 1 && isLoadingAdded) ? LOADING : ITEM;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ListReportBinding binding;
		public ListProgressVerticalBinding bindingProgress;

		public ViewHolder(ListReportBinding binding,
											ListProgressVerticalBinding bindingProgress) {
			super(binding != null ? binding.getRoot() : bindingProgress.getRoot());
			this.binding = binding;
			this.bindingProgress = bindingProgress;
		}
	}

	public void addLoadingFooter() {
		isLoadingAdded = true;
	}

	public void removeLoadingFooter() {
		isLoadingAdded = false;
	}

	@SuppressLint("NotifyDataSetChanged")
	@Override
	protected void onLoadingStateChanged(@NonNull LoadingState state) {
		super.onLoadingStateChanged(state);
		switch (state) {
			case LOADING_INITIAL:
			case LOADING_MORE:
				addLoadingFooter();
				break;
			case LOADED:
				removeLoadingFooter();
				break;
			case FINISHED:
				removeLoadingFooter();
				if (stateListener == null) break;
				stateListener.stateChange(null);
				break;
			case ERROR:
				removeLoadingFooter();
				if (stateListener == null) break;
				stateListener.stateChange(new CustomException("Terjadi kesalahan saat memuat!",
					new Throwable()));
				break;

		}
		notifyDataSetChanged();
	}
}
