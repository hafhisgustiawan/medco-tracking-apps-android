package com.medco.trackingapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.ListProgressVerticalBinding;
import com.medco.trackingapp.databinding.ListUserBinding;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.model.UserItem;

public class UserAdapter extends FirestorePagingAdapter<UserItem,
	UserAdapter.ViewHolder> {

	public static final String TAG = "Notification Adapter";
	private static final int ITEM = 0;
	private static final int LOADING = 1;
	public Animation animation;
	public Context mContext;
	private boolean isLoadingAdded = false;
	private OnItemClickListener listener;
	private StateChangeListener stateListener;

	public UserAdapter(@NonNull FirestorePagingOptions<UserItem> options,
										 Context context) {
		super(options);
		this.mContext = context;
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	public void setOnStateChangeListener(StateChangeListener stateListener) {
		this.stateListener = stateListener;
	}

	@Override
	protected void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position,
																	@NonNull UserItem model) {
		if (getItemViewType(position) == LOADING) return;

		holder.itemView.setAnimation(animation);
		holder.binding.setUserItem(model);
	}

	@NonNull
	@Override
	public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if (viewType == ITEM) {
			return new ViewHolder(ListUserBinding
				.inflate(inflater, parent, false), null);
		} else {
			return new ViewHolder(null, ListProgressVerticalBinding
				.inflate(inflater, parent, false));
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

	@Override
	public int getItemViewType(int position) {
		return (position == getItemCount() - 1 && isLoadingAdded) ? LOADING : ITEM;
	}

	public interface OnItemClickListener {
		void onItemClick(int visibility);
	}

	public interface StateChangeListener {
		void stateChange(Exception e);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ListUserBinding binding;
		public ListProgressVerticalBinding bindingProgress;

		public ViewHolder(ListUserBinding binding,
											ListProgressVerticalBinding bindingProgress) {
			super(binding != null ? binding.getRoot() : bindingProgress.getRoot());
			this.binding = binding;
			this.bindingProgress = bindingProgress;
		}
	}
}
