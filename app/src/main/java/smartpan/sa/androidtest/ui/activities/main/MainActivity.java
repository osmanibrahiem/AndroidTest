package smartpan.sa.androidtest.ui.activities.main;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import smartpan.sa.androidtest.R;
import smartpan.sa.androidtest.model.Location;
import smartpan.sa.androidtest.ui.services.LocationService;

public class MainActivity extends AppCompatActivity implements MainView, OnMapReadyCallback {

    private MainPresenter presenter;
    private Intent serviceIntent;

    private TextView locationText;
    private GoogleMap mMap;

    private Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationText = findViewById(R.id.txt_location);

        presenter = new MainPresenter(this, getLifecycle(), this);
        serviceIntent = new Intent(this, LocationService.class);

        presenter.checkGPSProvider();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
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
                .setPositiveButton(getString(R.string.btn_ok), (dialogInterface, i) -> presenter.checkGPSProvider())
                .setNegativeButton(getString(R.string.btn_no), (dialogInterface, i) -> MainActivity.this.finish())
                .show();
    }

    @Override
    public void showNeedPermissionAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_permission_required))
                .setMessage(getString(R.string.message_need_location_permission))
                .setPositiveButton(getString(R.string.btn_ok), (dialogInterface, i) -> presenter.checkLocationPermission())
                .setNegativeButton(getString(R.string.btn_no), (dialogInterface, i) -> MainActivity.this.finish())
                .show();
    }

    @Override
    public void onLineChanged(List<Location> locations) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        List<LatLng> points = new ArrayList<>();
        for (Location location : locations) {
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            points.add(point);
            builder.include(point);
        }
        if (line != null) {
            line.setPoints(points);

            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            mMap.setOnMapLoadedCallback(() -> mMap.animateCamera(cu));
        }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style_map));
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }

        mMap = googleMap;

        PatternItem DASH = new Dash(50);
        PatternItem GAP = new Gap(10);
        PatternItem DOT = new Dot();
        line = googleMap.addPolyline(new PolylineOptions()
                .width(16)
                .color(ContextCompat.getColor(this, R.color.colorLine))
                .geodesic(true)
                .pattern(Arrays.asList(DASH, GAP)));

        presenter.updateLine();
    }

}
