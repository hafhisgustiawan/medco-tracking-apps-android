package com.medco.trackingapp.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medco.trackingapp.R;
import com.medco.trackingapp.model.Well;
import com.medco.trackingapp.utils.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class GetWellWorker extends Worker {
	public static final String TAG = GetWellWorker.class.getSimpleName();
	public CollectionReference wellColl;
	public AppDatabase db;

	public GetWellWorker(@NonNull Context context,
											 @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		wellColl = firebaseFirestore.collection(context.getString(R.string.collection_well));
		db = Room.databaseBuilder(context, AppDatabase.class, "well").allowMainThreadQueries()
			.build();
	}

	@NonNull
	@Override
	public Result doWork() {
		Log.d(TAG, "doWork: ");

		wellColl.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;

			if (task.getResult().isEmpty()) return;

			List<Well> wells = new ArrayList<>();
			task.getResult().getDocuments().forEach(doc -> {
				Well well = new Well(doc.getString("name"), doc.getId());
				wells.add(well);
			});
			db.wellDAO().deleteAllWells();
			db.wellDAO().insertAllWell(wells);
		});

		return Result.success();

		/*Result[] results = {Result.retry()};
		CountDownLatch countDownLatch = new CountDownLatch(1);

		wellColl.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) {
				results[0] = Result.failure();
				return;
			}

			if (task.getResult().isEmpty()) {
				results[0] = Result.failure();
				return;
			}

			Data.Builder outData = new Data.Builder();
			List<String> wellNames = new ArrayList<>();
			List<String> docsId = new ArrayList<>();

			task.getResult().getDocuments().forEach(documentSnapshot -> {
				wellNames.add(documentSnapshot.getString("name"));
				docsId.add(documentSnapshot.getId());
			});

			outData.putStringArray("names", wellNames.toArray(new String[0]));
			outData.putStringArray("docs", docsId.toArray(new String[0]));
			results[0] = Result.success(outData.build());
		});

		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return results[0];*/
	}
}
