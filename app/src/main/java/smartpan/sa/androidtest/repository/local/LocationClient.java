package smartpan.sa.androidtest.repository.local;

import android.content.Context;

import androidx.room.Room;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import smartpan.sa.androidtest.model.Location;

public class LocationClient {

    private static LocationClient mInstance;

    private LocationDatabase database;

    private LocationClient(Context mContext) {
        database = Room.databaseBuilder(mContext, LocationDatabase.class, "MyLocations").build();
    }

    public static synchronized LocationClient getInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new LocationClient(mContext);
        }
        return mInstance;
    }

    public LocationDatabase getDatabase() {
        return database;
    }

    public void getLocations(final DatabaseCallback databaseCallback) {
        Disposable mDisposable = database.getLocationTable()
                .getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(databaseCallback::onLocationsLoaded);
    }

    public void addLocation(final DatabaseCallback databaseCallback, final Location location) {
        Completable.fromAction(() -> database.getLocationTable().insert(location))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        databaseCallback.onLocationAdded();
                    }

                    @Override
                    public void onError(Throwable e) {
                        databaseCallback.onDataNotAvailable();
                    }
                });
    }
}
