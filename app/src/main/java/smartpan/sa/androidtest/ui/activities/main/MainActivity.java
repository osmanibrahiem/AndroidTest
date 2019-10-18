package smartpan.sa.androidtest.ui.activities.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

import smartpan.sa.androidtest.R;
import smartpan.sa.androidtest.model.DBPresenter.DBModel;
import smartpan.sa.androidtest.model.DBPresenter.DBView;
import smartpan.sa.androidtest.ui.services.LocationService;

public class MainActivity extends AppCompatActivity implements DBView {

    private static final int LOCATION_REQUEST_CODE = 10;
    DBModel presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        presenter = new DBModel(this);


        checkLocationPermission();

    }

    private void checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this
                    , new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                            , Manifest.permission.ACCESS_COARSE_LOCATION}
                    , LOCATION_REQUEST_CODE
            );

        } else {

            // set up service for update

            startLocationUpdateService();

        }

    }

    private void startLocationUpdateService() {
        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();
        ActivityCompat.startForegroundService(this, new Intent(this, LocationService.class));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                ) {
                    // show dialog to explain permission purpose
                } else {
                    startLocationUpdateService();
                }
                break;
        }
    }


    @Override
    public void onSumResult(int result) {
        Toast.makeText(this, String.valueOf(result), Toast.LENGTH_LONG).show();
    }

    public void onAddClick(View view) {
        presenter.add(new Random().nextInt(50), new Random().nextInt(50));
    }
}
