package com.medco.trackingapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.ListImageBinding;
import com.medco.trackingapp.model.ImageItem;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

	public static final String TAG = "Image Adapter";

	//constructor variable
	public Context mContext;
	public List<ImageItem> mData;
	public Animation animation;

	//callback
	private OnItemClickListener listener;

	public ImageAdapter(Context context) {
		this.mContext = context;
		mData = new ArrayList<>();
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		return new ViewHolder(ListImageBinding.inflate(inflater, parent, false));
	}

	@SuppressLint("NonConstantResourceId")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.itemView.setAnimation(animation);

		holder.binding.setImageItem(mData.get(position));

		holder.binding.img.setOnClickListener(view -> listener.onItemClick(position));

		holder.binding.btnDelete.setOnClickListener(view -> {
			PopupMenu popupMenu = new PopupMenu(mContext, holder.binding.btnDelete);
			popupMenu.inflate(R.menu.delete_selection_menu);
			popupMenu.setOnMenuItemClickListener(item -> {
				if (item.getItemId() == R.id.action_delete) {
					deleteSelectedItem(position);
				}
				return false;
			});
			popupMenu.show();
		});
	}

	@Override
	public int getItemCount() {
		return mData.size();
	}

	public List<ImageItem> getData() {
		return mData;
	}

	public ImageItem getSelectedData(int position) {
		//disini size nya gak mungkin 0
		if (mData == null || mData.get(position) == null) return new ImageItem();
		return mData.get(position);
	}

	public void addData(ImageItem item) {
		if (mData == null) return;
		mData.add(item);
		notifyItemInserted(mData.size() - 1);
		new Handler().postDelayed(this::notifyDataSetChanged, 500);
	}

	public void updateData(ImageItem item, int position) {
		if (mData == null) return;
		mData.set(position, item);
		notifyItemChanged(position);
		new Handler().postDelayed(this::notifyDataSetChanged, 500);
	}

	public void deleteSelectedItem(int position) {
		if (mData == null) return;
		if (mData.size() == 0) return;
		mData.remove(position);
		notifyItemRemoved(position);
		new Handler().postDelayed(this::notifyDataSetChanged, 500);
	}

	public interface OnItemClickListener {
		void onItemClick(int position);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ListImageBinding binding;

		public ViewHolder(ListImageBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
