package DTT.rsrpechhulp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import DTT.rsrpechhulp.Presenter.Presenter;
import DTT.rsrpechhulp.R;

public class MapActivity extends AppCompatActivity implements UI, OnMapReadyCallback {

    private GoogleMap map;
    private MapView mapView;
    private LocationManager locationManager;
    private ConnectivityManager connectivityManager;
    private Presenter presenter;
    private Button ringButton;
    private FusedLocationProviderClient client;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int REQUEST_CALL = 1;
    private static final int LOCATION_CAMERA_ZOOM = 16;
    private static final int REQUEST_LOCATION = 2;
    private static final int REQUEST_INTERNET = 3;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
    private static final int GRANTED = PackageManager.PERMISSION_GRANTED;

    private static final int ONE_SEC = 1000;//for the location request refresh rate

    private static final double OCEAN_LATITUDE = 48.8;//to move the map at random coordinates
    private static final double OCEAN_LONGITUDE = -25.1;//in case location can't be retrieved

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        presenter = (Presenter) getIntent().getSerializableExtra("presenter");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ringButton = (Button) findViewById(R.id.ring_button);
        ringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringButton.setVisibility(View.GONE);
                showCallDialog();
            }
        });

        Button backBtn = (Button) findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(checkSelfPermission(FINE_LOCATION) != GRANTED) {
            requestPermissions(new String[] {FINE_LOCATION}, REQUEST_LOCATION);
        }

        locationRequest = new LocationRequest();
        locationRequest.setInterval(7 * ONE_SEC);
        locationRequest.setFastestInterval(5 * ONE_SEC);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                drawMarker(location);
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!internetEnabled()) {
            showWarningDialog(REQUEST_INTERNET);
        } else {
            if (!gpsEnabled()) {
                showWarningDialog(REQUEST_LOCATION);
            }
        }
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                drawMarker(locationResult.getLastLocation());
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CALL:
                if (grantResults.length > 0 && grantResults[0] == GRANTED) {
                    makeCall();
                }
                break;
            case REQUEST_LOCATION:
            case REQUEST_INTERNET:
                if(grantResults.length > 0 && grantResults[0] != GRANTED){
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    protected void onStart() {
        mapView.onStart();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    private void showWarningDialog(int requestCode) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.gps_and_network_dialog);
        Window window = dialog.getWindow();
        window.setLayout(presenter.getDialogWidth(), WindowManager.LayoutParams.WRAP_CONTENT);

        TextView titleTv = (TextView) dialog.findViewById(R.id.warning_title_tv);
        TextView messageTv = (TextView) dialog.findViewById(R.id.warning_message_tv);
        String title;
        String message;
        final String settingsPath;

        if (requestCode == REQUEST_LOCATION) {
            title = getResources().getString(R.string.no_gps_title);
            message = getResources().getString(R.string.no_gps_message);
            settingsPath = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        } else {
            title = getResources().getString(R.string.no_internet_title);
            message = getResources().getString(R.string.no_internet_message);
            settingsPath = Settings.ACTION_WIFI_SETTINGS;
        }
        titleTv.setText(title);
        messageTv.setText(message);

        Button closeBtn = (Button) dialog.findViewById(R.id.close_warning_button);
        Button activateBtn = (Button) dialog.findViewById(R.id.activate_button);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        activateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(settingsPath));
            }
        });
        dialog.show();
    }


    private void showCallDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.call_dialog);
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        window.setLayout(presenter.getCallDialogWidth(), Toolbar.LayoutParams.WRAP_CONTENT);

        Button callBtn = (Button) dialog.findViewById(R.id.call_button);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCall();
            }
        });

        Button closeBtn = (Button) dialog.findViewById(R.id.close_dialog_button);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringButton.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void makeCall() {
        String phoneNumber = getResources().getString(R.string.phone_number);
        if (checkSelfPermission(CALL_PHONE) == GRANTED) {
            String dial = "tel:" + phoneNumber;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        } else {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        }
    }

    private void drawMarker(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        LatLng gps;
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker);
            gps = new LatLng(location.getLatitude(), location.getLongitude());
            Marker marker = map.addMarker(new MarkerOptions().position(gps).
                    snippet(address).icon(icon));
            marker.showInfoWindow();
        } catch (Exception e) {
            gps = new LatLng(OCEAN_LATITUDE, OCEAN_LONGITUDE);
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, LOCATION_CAMERA_ZOOM));
    }

    private boolean internetEnabled() {
        return connectivityManager.getActiveNetwork() != null;
    }

    private boolean gpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void startLocationUpdates() {
        if(checkSelfPermission(FINE_LOCATION) == GRANTED){
            client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } else {
            requestPermissions(new String[] {FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    private void stopLocationUpdates() {
        client.removeLocationUpdates(locationCallback);
    }

    public void loadPhone() {

    }

    public void loadTablet() {

    }
}
