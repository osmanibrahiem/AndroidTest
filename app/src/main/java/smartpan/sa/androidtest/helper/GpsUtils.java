package smartpan.sa.androidtest.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

import java.lang.ref.WeakReference;

public class GpsUtils {

    public static final int GPS_REQUEST = 66;
    public static final int LOCATION_REQUEST_INTERVAL_TIME = 30 * 1000; // Update location every 30 seconds.
    public static final float LOCATION_REQUEST_SMALLEST_DISPLACEMENT = 1.0f; // Update location every 1 meter.

    private static final String TAG = "GpsUtils";
    private VIEW_TYPE TYPE;
    private WeakReference<Activity> activityView;
    private WeakReference<Fragment> fragmentView;
    private WeakReference<Context> nonView;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private GpsListener listener;

    public GpsUtils(Activity view) {
        this.activityView = new WeakReference<Activity>(view);
        TYPE = VIEW_TYPE.ACTIVITY;
        init();
    }

    public GpsUtils(Fragment view) {
        this.fragmentView = new WeakReference<Fragment>(view);
        TYPE = VIEW_TYPE.FRAGMENT;
        init();
    }

    public GpsUtils(Context context) {
        this.nonView = new WeakReference<Context>(context);
        TYPE = VIEW_TYPE.NON_VIEW;
        init();
    }

    private void init() {
        if (isContextAttached() && getContext() != null) {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            mSettingsClient = LocationServices.getSettingsClient(getContext());
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(LOCATION_REQUEST_INTERVAL_TIME);
            locationRequest.setFastestInterval(LOCATION_REQUEST_INTERVAL_TIME);
            locationRequest.setSmallestDisplacement(LOCATION_REQUEST_SMALLEST_DISPLACEMENT);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);
            mLocationSettingsRequest = builder.build();
        }
    }

    public GpsUtils setListener(GpsListener listener) {
        this.listener = listener;
        return this;
    }

    public boolean isGPSProviderEnabled() {
        if (isContextAttached() && locationManager != null)
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return false;
    }

    public LocationRequest getLocationRequest() {
        if (isContextAttached() && locationRequest != null)
            return locationRequest;
        return null;
    }

    // method for turn on GPS
    public void turnGPSOn() {
        if (isContextAttached() && getActivity() != null) {
            if (isGPSProviderEnabled()) {
                if (listener != null) {
                    listener.onGetGPSStatus(true);
                }
            } else {
                mSettingsClient
                        .checkLocationSettings(mLocationSettingsRequest)
                        .addOnSuccessListener(getActivity(), locationSettingsResponse -> {
                            //  GPS is already enable, callback GPS status through listener
                            if (listener != null) {
                                listener.onGetGPSStatus(true);
                            }
                        })
                        .addOnFailureListener(getActivity(), e -> {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the
                                        // result in onActivityResult().
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(getActivity(), GPS_REQUEST);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.i(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    String errorMessage = "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings.";
                                    Log.e(TAG, errorMessage);
                            }
                        });
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                if (listener != null) {
                    listener.onGetGPSStatus(isGPSProviderEnabled());
                    if (!isGPSProviderEnabled()) {
                        listener.onUserCancelEnabledGPS();
                    }
                }
            }
        }
    }

    private boolean isContextAttached() {
        switch (TYPE) {
            case ACTIVITY:
                return getActivityView() != null;
            case FRAGMENT:
                return getFragmentView() != null;
            case NON_VIEW:
                return getNonView() != null;
        }
        return false;
    }

    private Activity getActivityView() {
        if (activityView != null)
            return activityView.get();
        return null;
    }

    private Fragment getFragmentView() {
        if (fragmentView != null)
            return fragmentView.get();
        return null;
    }

    private Context getNonView() {
        if (nonView != null)
            return nonView.get();
        return null;
    }

    private Activity getActivity() {
        switch (TYPE) {
            case ACTIVITY:
                return getActivityView();
            case FRAGMENT:
                return getFragmentView().getActivity();
        }
        return null;
    }

    private Context getContext() {
        switch (TYPE) {
            case ACTIVITY:
                return getActivityView();
            case FRAGMENT:
                return getFragmentView().getActivity();
            case NON_VIEW:
                return getNonView();
        }
        return null;
    }

    public void onDestroy() {
        listener = null;
        activityView = null;
        fragmentView = null;
        nonView = null;
    }

    private enum VIEW_TYPE {
        ACTIVITY, FRAGMENT, NON_VIEW
    }

    public interface GpsListener {

        void onGetGPSStatus(boolean isGPSEnable);

        void onUserCancelEnabledGPS();
    }
}
