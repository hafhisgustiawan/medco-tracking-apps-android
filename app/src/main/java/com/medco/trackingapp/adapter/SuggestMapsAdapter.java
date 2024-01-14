package com.medco.trackingapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.MapsSuggestListBinding;
import com.medco.trackingapp.model.PlaceItem;

import java.util.ArrayList;

public class SuggestMapsAdapter extends RecyclerView.Adapter<SuggestMapsAdapter
	.ViewHolder> {

	public static final String TAG = "Suggest Maps Adapter";
	public ArrayList<PlaceItem> mData;
	public Context mContext;
	public Animation animation;

	//callback
	private OnItemClickListener listener;

	public SuggestMapsAdapter(Context context, ArrayList<PlaceItem> data) {
		this.mContext = context;
		this.mData = data;
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		return new ViewHolder(MapsSuggestListBinding.inflate(inflater, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.itemView.setAnimation(animation);
		if (mData.get(position) == null) return;
		holder.binding.setItem(mData.get(position));
		holder.itemView.setOnClickListener(view -> listener.onItemClick(position, mData.get(position)
			.getDescription()));
	}

	@Override
	public int getItemCount() {
		return mData.size();
	}

	public interface OnItemClickListener {
		void onItemClick(int position, String keyWord);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public MapsSuggestListBinding binding;

		public ViewHolder(MapsSuggestListBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
