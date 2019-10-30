package smartpan.sa.androidtest.ui.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

import smartpan.sa.androidtest.R;
import smartpan.sa.androidtest.helper.GpsUtils;
import smartpan.sa.androidtest.helper.PermissionHelper;
import smartpan.sa.androidtest.ui.activities.main.MainActivity;

public class LocationService extends Service {

    private static final int NOTIFICATION_ID = 12;
    private static final String CHANNEL_DEFAULT_IMPORTANCE = "CHANNEL_DEFAULT_IMPORTANCE";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private GpsUtils gpsProvider;
    private PermissionHelper permissionHelper;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.e(LocationService.class.getSimpleName(), "service still running");
            if (handler != null)
                handler.postDelayed(this, 1000);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler.postDelayed(runnable, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initNotification();

        gpsProvider = new GpsUtils(this);
        permissionHelper = new PermissionHelper(this);
        String[] location_permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        initFusedLocationClient();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                EventBus.getDefault().post(locationResult.getLastLocation());
            }
        };

        if (gpsProvider.isGPSProviderEnabled() && permissionHelper.checkPermission(location_permissions)) {
        startLocationUpdates();
        }

        return START_STICKY;
    }

    private synchronized void initFusedLocationClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(gpsProvider.getLocationRequest(),
                mLocationCallback, Looper.getMainLooper());
    }

    private void initNotification() {


        createNotificationChannel();


//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                .setContentTitle("Location Service")
                .setContentText("This app needs to update your current location with server")
                .setSmallIcon(R.drawable.ic_notification)
//                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_DEFAULT_IMPORTANCE,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        handler.removeCallbacks(runnable);
        handler = null;
        super.onDestroy();
    }
}
