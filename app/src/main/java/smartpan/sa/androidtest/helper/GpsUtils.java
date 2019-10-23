package smartpan.sa.androidtest.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.ref.WeakReference;

public class GpsUtils {

    public static final int GPS_REQUEST = 66;
    private static final String TAG = "GpsUtils";
    private VIEW_TYPE TYPE;
    private WeakReference<Activity> activityView;
    private WeakReference<Fragment> fragmentView;

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

    private void init() {
        if (isViewAttached() && getActivity() != null) {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            mSettingsClient = LocationServices.getSettingsClient(getActivity());
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10 * 1000);
            locationRequest.setFastestInterval(2 * 1000);
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
        if (isViewAttached() && locationManager != null)
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return false;
    }

    // method for turn on GPS
    public void turnGPSOn() {
        if (isViewAttached() && getActivity() != null) {
            if (isGPSProviderEnabled()) {
                if (listener != null) {
                    listener.onGetGPSStatus(true);
                }
            } else {
                mSettingsClient
                        .checkLocationSettings(mLocationSettingsRequest)
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                            @Override
                            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                //  GPS is already enable, callback GPS status through listener
                                if (listener != null) {
                                    listener.onGetGPSStatus(true);
                                }
                            }
                        })
                        .addOnFailureListener(getActivity(), new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
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

    private boolean isViewAttached() {
        switch (TYPE) {
            case ACTIVITY:
                return getActivityView() != null;
            case FRAGMENT:
                return getFragmentView() != null;
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

    private Activity getActivity() {
        switch (TYPE) {
            case ACTIVITY:
                return getActivityView();
            case FRAGMENT:
                return getFragmentView().getActivity();
        }
        return null;
    }

    public void onDestroy() {
        listener = null;
        activityView = null;
        fragmentView = null;
    }

    private enum VIEW_TYPE {
        ACTIVITY, FRAGMENT
    }

    public interface GpsListener {
        void onGetGPSStatus(boolean isGPSEnable);

        void onUserCancelEnabledGPS();
    }
}
