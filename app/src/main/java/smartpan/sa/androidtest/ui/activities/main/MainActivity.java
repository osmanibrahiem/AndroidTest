package smartpan.sa.androidtest.ui.activities.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import smartpan.sa.androidtest.R;
import smartpan.sa.androidtest.ui.services.LocationService;

public class MainActivity extends AppCompatActivity implements MainView {

    private MainPresenter presenter;
    private Intent serviceIntent;

    private TextView locationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationText = findViewById(R.id.txt_location);

        presenter = new MainPresenter(this, getLifecycle(), this);
        serviceIntent = new Intent(this, LocationService.class);

        presenter.checkGPSProvider();
    }

    @Override
    public void startLocationUpdateService() {
        ActivityCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    public void stopLocationUpdateService() {
        stopService(serviceIntent);
    }

    @Override
    public void showNeedGPSAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_gps_dialog))
                .setMessage(getString(R.string.message_need_gps))
                .setPositiveButton(getString(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                presenter.checkGPSProvider();
                            }
                        })
                .setNegativeButton(getString(R.string.btn_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.this.finish();
                            }
                        })
                .show();
    }

    @Override
    public void showNeedPermissionAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_permission_required))
                .setMessage(getString(R.string.message_need_location_permission))
                .setPositiveButton(getString(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                presenter.checkLocationPermission();
                            }
                        })
                .setNegativeButton(getString(R.string.btn_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.this.finish();
                            }
                        })
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        locationText.setText("Your location is :\nLatitude: " + location.getLatitude() + " \nLongitude: " + location.getLongitude());
        Log.i("LocationUpdate", location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }
}
