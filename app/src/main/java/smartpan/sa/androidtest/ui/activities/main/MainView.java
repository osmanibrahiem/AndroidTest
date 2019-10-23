package smartpan.sa.androidtest.ui.activities.main;

import android.location.Location;

interface MainView {

    void startLocationUpdateService();

    void stopLocationUpdateService();

    void showNeedGPSAlertDialog();

    void showNeedPermissionAlertDialog();

    void onLocationChanged(Location location);
}
