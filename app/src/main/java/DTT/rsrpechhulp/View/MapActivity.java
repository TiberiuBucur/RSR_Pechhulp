package DTT.rsrpechhulp.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import DTT.rsrpechhulp.Presenter.Presenter;
import DTT.rsrpechhulp.R;

public class MapActivity extends AppCompatActivity implements UI {

    private GoogleMap map;
    private MapView mapView;
    private LocationManager locationManager;
    private Presenter presenter;
    private Button ringButton;

    private static final int LOCATION_CAMERA_ZOOM = 16;

    private static final String[] PERMS={
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final int REQUEST = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        presenter = (Presenter) getIntent().getSerializableExtra("presenter") ;

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

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if(!hasPermission(PERMS[0]) || !hasPermission(PERMS[1])){
                    requestPermissions(PERMS, REQUEST);
                }
                map = googleMap;
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
                Location location;
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED && isGPSEnabled) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    drawMarker(location);
                }
            }
        });
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

    private void drawMarker(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker);
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            Marker marker = map.addMarker(new MarkerOptions().position(gps).
                    snippet(address).icon(icon));
            marker.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, LOCATION_CAMERA_ZOOM));
        } catch (IOException e) {
            e.printStackTrace();
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
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
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

    private boolean hasPermission(String perm){
        return PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm);
    }

    public void loadPhone() {

    }

    public void loadTablet() {

    }

}
