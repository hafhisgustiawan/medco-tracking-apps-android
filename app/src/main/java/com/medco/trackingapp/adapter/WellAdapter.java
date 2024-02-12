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
import com.medco.trackingapp.activity.WellActivity;
import com.medco.trackingapp.databinding.ListProgressHorizontalBinding;
import com.medco.trackingapp.databinding.ListProgressVerticalBinding;
import com.medco.trackingapp.databinding.ListWellHorizontalBinding;
import com.medco.trackingapp.databinding.ListWellVerticalBinding;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.model.WellItem;

import java.util.Objects;

public class WellAdapter extends FirestorePagingAdapter<WellItem, WellAdapter.ViewHolder> {

	public static final String TAG = WellAdapter.class.getSimpleName();
	private static final int ITEM = 0;
	private static final int LOADING = 1;
	private final String mType;
	public Animation animation;
	public Context mContext;
	private boolean isLoadingAdded = false;
	private StateChangeListener stateListener;

	public WellAdapter(@NonNull FirestorePagingOptions<WellItem> options,
										 Context context, String type) {
		super(options);
		this.mContext = context;
		this.mType = type;
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
	}

	public void setOnStateChangeListener(StateChangeListener stateListener) {
		this.stateListener = stateListener;
	}

	@Override
	protected void onBindViewHolder(@NonNull WellAdapter.ViewHolder holder, int position,
																	@NonNull WellItem model) {
		if (getItemViewType(position) == LOADING) return;
		holder.itemView.setAnimation(animation);

		DocumentSnapshot snapshot = getItem(position);
		if (snapshot == null) return;
		if (Objects.equals(mType, "horizontal")) {
			holder.bindingHorizontal.setCurrentWellRef(snapshot.getReference());
			holder.bindingHorizontal.setWellItem(model);
		} else {
			holder.bindingVertical.setCurrentWellRef(snapshot.getReference());
			holder.bindingVertical.setWellItem(model);
		}

		holder.itemView.setOnClickListener(view -> {
			Intent intent = new Intent(mContext, WellActivity.class);
			intent.putExtra("path", snapshot.getReference().getPath());
			mContext.startActivity(intent);
		});
	}

	@Override
	public int getItemViewType(int position) {
		return (position == getItemCount() - 1 && isLoadingAdded) ? LOADING : ITEM;
	}

	@NonNull
	@Override
	public WellAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if (viewType == ITEM) {
			if (Objects.equals(mType, "horizontal")) {
				return new ViewHolder(null, ListWellHorizontalBinding.inflate(inflater,
					parent, false), null, null);

			} else {
				return new ViewHolder(ListWellVerticalBinding.inflate(inflater, parent,
					false), null, null, null);
			}
		} else {
			if (Objects.equals(mType, "horizontal")) {
				return new ViewHolder(null, null, null,
					ListProgressHorizontalBinding.inflate(inflater, parent, false));
			} else {
				return new ViewHolder(null, null, ListProgressVerticalBinding
					.inflate(inflater, parent, false), null);
			}
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

	public interface StateChangeListener {
		void stateChange(Exception e);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ListWellVerticalBinding bindingVertical;
		public ListWellHorizontalBinding bindingHorizontal;
		public ListProgressVerticalBinding progressVertical;
		public ListProgressHorizontalBinding progressHorizontal;

		public ViewHolder(ListWellVerticalBinding bindingVertical,
											ListWellHorizontalBinding bindingHorizontal,
											ListProgressVerticalBinding progressVertical,
											ListProgressHorizontalBinding progressHorizontal) {
			super(bindingVertical != null ? bindingVertical.getRoot() : bindingHorizontal != null ?
				bindingHorizontal.getRoot() : progressVertical != null ? progressVertical.getRoot() :
				progressHorizontal.getRoot());
			this.bindingVertical = bindingVertical;
			this.bindingHorizontal = bindingHorizontal;
			this.progressVertical = progressVertical;
			this.progressHorizontal = progressHorizontal;
		}
	}
}
