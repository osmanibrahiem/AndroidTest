package smartpan.sa.androidtest.ui.activities.main;

import java.util.List;

import smartpan.sa.androidtest.model.Location;

interface MainView {

    void startLocationUpdateService();

    void stopLocationUpdateService();

    void showNeedGPSAlertDialog();

    void showNeedPermissionAlertDialog();

    void onLineChanged(List<Location> locations);
}
