package com.medco.trackingapp.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.ListProgressVerticalBinding;
import com.medco.trackingapp.databinding.ListUserBinding;
import com.medco.trackingapp.fragment.ManageUserFragment;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.model.UserItem;

import java.util.List;
import java.util.Objects;

public class UserAdapter extends FirestorePagingAdapter<UserItem,
	UserAdapter.ViewHolder> {

	public static final String TAG = "Notification Adapter";
	private static final int ITEM = 0;
	private static final int LOADING = 1;
	public Animation animation;
	public Context mContext;
	private boolean isLoadingAdded = false;
	public DocumentReference mUserRef;
	public FragmentManager mFragmentManager;
	private OnItemClickListener listener;
	private StateChangeListener stateListener;
	public FirebaseAuth firebaseAuth;

	public UserAdapter(@NonNull FirestorePagingOptions<UserItem> options,
										 Context context, DocumentReference userRef,
										 FragmentManager fragmentManager) {
		super(options);
		this.mContext = context;
		this.mUserRef = userRef;
		this.mFragmentManager = fragmentManager;
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
		firebaseAuth = FirebaseAuth.getInstance();
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
		holder.binding.setCurrentUserRef(mUserRef);

		DocumentSnapshot snapshot = getItem(position);
		if (snapshot == null) return;

		if (Objects.equals(model.getRole(), "admin")) return;

		holder.binding.btnMoreOption.setOnClickListener(view -> {
			if (listener == null || position == RecyclerView.NO_POSITION
				|| stateListener == null) {
				return;
			}
			PopupMenu popupMenu = new PopupMenu(mContext, holder.binding.btnMoreOption);
			if (model.getRole() == null) {
				popupMenu.inflate(R.menu.active_user_menu);
			} else {
				popupMenu.inflate(R.menu.non_active_user_menu);
			}
			popupMenu.setOnMenuItemClickListener(item -> {
				if (item.getItemId() == R.id.action_block) {
					new AlertDialog.Builder(mContext)
						.setMessage("Anda yakin ingin mengubah status pengguna ini? Tindakan ini bisa " +
							"mengakibatkan pengguna ter-logout secara otomatis!")
						.setNegativeButton("Tidak", null)
						.setPositiveButton("Ya", (dialogInterface, i) -> {
							listener.onItemClick(View.VISIBLE);
							String status = model.getRole() != null ? null : "user";
							snapshot.getReference().update("role", status)
								.addOnCompleteListener(task -> {
									listener.onItemClick(View.GONE);
									if (!task.isSuccessful()) {
										stateListener.stateChange(task.getException());
										return;
									}
									Toast.makeText(mContext, "Berhasil update status pengguna!",
										Toast.LENGTH_SHORT).show();
									refresh();
								});
						}).create().show();
				} else if (item.getItemId() == R.id.action_delete) {
					//todo : INI BELUM ADA MENU ITEM NYA
					new AlertDialog.Builder(mContext)
						.setMessage("Tindakan ini hanya akan berhasil jika pengguna belum pernah " +
							"masuk ke dalam aplikasi!")
						.setNegativeButton("Tutup", null)
						.setPositiveButton("Lanjutkan", (dialogInterface, i) -> {

							listener.onItemClick(View.VISIBLE);

							firebaseAuth.fetchSignInMethodsForEmail(model.getEmail())
								.addOnCompleteListener(t -> {
									listener.onItemClick(View.GONE);
									if (!t.isSuccessful()) {
										stateListener.stateChange(t.getException());
										return;
									}

									List<String> methods = t.getResult().getSignInMethods();
									if (methods == null || methods.isEmpty()) {
										Toast.makeText(mContext, model.getEmail() + " Bisa hapus", Toast
											.LENGTH_SHORT).show();
										/*snapshot.getReference().delete()
											.addOnCompleteListener(task -> {
												listener.onItemClick(View.GONE);
												if (!task.isSuccessful()) {
													stateListener.stateChange(task.getException());
													return;
												}
												Toast.makeText(mContext, "Berhasil hapus data pengguna pengguna!",
													Toast.LENGTH_SHORT).show();
												refresh();
											});*/
									} else {
										Toast.makeText(mContext, "Gabisa hapus", Toast.LENGTH_SHORT).show();
										/*stateListener.stateChange(new CustomException("Pengguna ini tidak dapat " +
											"dihapus dari sistem dikarenakan bisa menyebabkan crash!",
											new Throwable()));*/
									}
								});
						}).create().show();
				} else {
					if (mFragmentManager.isDestroyed()) return false;
					ManageUserFragment fragment = new ManageUserFragment(snapshot);
					fragment.setCancelable(false);
					fragment.ListenerApiClose(selector -> {
						fragment.dismiss();
						if (selector == 1) refresh();
					});
					fragment.show(mFragmentManager, TAG);
				}
				return false;
			});
			popupMenu.show();
		});
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
