package com.medco.trackingapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.ListWellSuggestBinding;
import com.medco.trackingapp.model.Well;

import java.util.List;

public class WellSuggestAdapter extends RecyclerView.Adapter<WellSuggestAdapter
	.ViewHolder> {

	public static final String TAG = WellSuggestAdapter.class.getSimpleName();
	public List<Well> mData;
	public Context mContext;
	public Animation animation;

	//callback
	private OnItemClickListener listener;

	public interface OnItemClickListener {
		void onItemClick(int position, Well well);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	public WellSuggestAdapter(Context context, List<Well> data) {
		super();
		this.mData = data;
		this.mContext = context;
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
	}

	@NonNull
	@Override
	public WellSuggestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		return new ViewHolder(ListWellSuggestBinding.inflate(inflater, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull WellSuggestAdapter.ViewHolder holder, int position) {
		holder.itemView.setAnimation(animation);
		holder.binding.setWell(mData.get(position));
		holder.itemView.setOnClickListener(view -> listener.onItemClick(position,
			mData.get(position)));
	}

	@Override
	public int getItemCount() {
		return mData.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ListWellSuggestBinding binding;

		public ViewHolder(ListWellSuggestBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
