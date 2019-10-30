package smartpan.sa.androidtest.ui.activities.main;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import smartpan.sa.androidtest.helper.GpsUtils;
import smartpan.sa.androidtest.helper.PermissionHelper;
import smartpan.sa.androidtest.repository.local.DatabaseCallback;
import smartpan.sa.androidtest.repository.local.LocationClient;

class MainPresenter implements
        LifecycleObserver,
        PermissionHelper.PermissionsListener,
        GpsUtils.GpsListener,
        DatabaseCallback {

    private static final int LOCATION_REQUEST_CODE = 10;
    private Activity context;
    private MainView view;
    private PermissionHelper permissionHelper;
    private GpsUtils gpsProvider;

    private BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null &&
                    intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                if (gpsProvider.isGPSProviderEnabled()) {
                    checkLocationPermission();
                } else {
                    gpsProvider.turnGPSOn();
                }
            }
        }
    };

    MainPresenter(Activity context, Lifecycle lifecycle, MainView view) {
        this.context = context;
        this.view = view;
        lifecycle.addObserver(this);

        gpsProvider = new GpsUtils(context).setListener(this);
        permissionHelper = new PermissionHelper(context).setListener(this);
    }

    void checkGPSProvider() {
        if (gpsProvider.isGPSProviderEnabled()) {
            checkLocationPermission();
        } else {
            gpsProvider.turnGPSOn();
        }
    }

    public void onGetGPSStatus(boolean isGPSEnable) {
        if (isGPSEnable) {
            checkLocationPermission();
        }
    }

    @Override
    public void onUserCancelEnabledGPS() {
        view.showNeedGPSAlertDialog();
    }

    void checkLocationPermission() {
        String[] needed_permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!permissionHelper.checkPermission(needed_permissions))
            permissionHelper.requestPermission(needed_permissions, LOCATION_REQUEST_CODE);
        else view.startLocationUpdateService();
    }

    @Override
    public void onPermissionGranted(int request_code) {
        view.startLocationUpdateService();
    }

    @Override
    public void onPermissionRejectedManyTimes(@NonNull List<String> rejectedPerms, int request_code) {
        view.showNeedPermissionAlertDialog();
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        gpsProvider.onActivityResult(requestCode, resultCode, data);
    }

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        context.registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        EventBus.getDefault().register(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        context.unregisterReceiver(mGpsSwitchStateReceiver);
        EventBus.getDefault().unregister(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        gpsProvider.onDestroy();
        permissionHelper.onDestroy();
        view.stopLocationUpdateService();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationChanged(Location location) {
        saveLocation(location);
    }

    private void saveLocation(Location location) {
        smartpan.sa.androidtest.model.Location newLocation = new smartpan.sa.androidtest.model.Location();
        newLocation.setLatitude(location.getLatitude());
        newLocation.setLongitude(location.getLongitude());
        LocationClient.getInstance(context.getApplicationContext())
                .addLocation(this, newLocation);
    }

    void updateLine() {
        LocationClient.getInstance(context.getApplicationContext())
                .getLocations(this);
    }

    @Override
    public void onLocationsLoaded(List<smartpan.sa.androidtest.model.Location> locations) {
        view.onLineChanged(locations);
    }

    @Override
    public void onLocationAdded() {
        updateLine();
    }

    @Override
    public void onDataNotAvailable() {

    }
}
