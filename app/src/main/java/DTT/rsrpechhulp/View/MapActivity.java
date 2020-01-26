package DTT.rsrpechhulp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private FusedLocationProviderClient client;//we need the client, the location request and
    private LocationRequest locationRequest;//the location callback for updates of the location
    private LocationCallback locationCallback;//in real time
    private Location lastLocation;
    private Dialog locationDialog;
    private Dialog internetDialog;

    private static final int REQUEST_CALL = 1;
    private static final int LOCATION_CAMERA_ZOOM = 16;
    static final int REQUEST_LOCATION = 2;
    static final int REQUEST_INTERNET = 3;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
    private static final int GRANTED = PackageManager.PERMISSION_GRANTED;

    private static final int ONE_SEC = 1000;//for the location request refresh rate

    private static final double OCEAN_LATITUDE = 48.8;//to move the map at random coordinates
    private static final double OCEAN_LONGITUDE = -25.1;//in case location can't be retrieved
    //I saw the map shows blue before retrieving the location in the original app and I thought
    //this would be a good way of simulating it

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        presenter = (Presenter) getIntent().getSerializableExtra("presenter");
        presenter.loadUI(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Button backBtn = (Button) findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(checkSelfPermission(FINE_LOCATION) != GRANTED) {//checking for permissions
            requestPermissions(new String[] {FINE_LOCATION}, REQUEST_LOCATION);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        BroadcastReceiver networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!internetEnabled()) {
                    if(!isDialogActive(internetDialog)){//same logic as in OnResume
                        showWarningDialog(REQUEST_INTERNET);
                    }
                } else if(isDialogActive(internetDialog)) {
                    internetDialog.dismiss();
                }
            }
        };
        this.registerReceiver(networkReceiver, new IntentFilter
                (ConnectivityManager.CONNECTIVITY_ACTION));

        BroadcastReceiver locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(!gpsEnabled()) {
                    if(!isDialogActive(locationDialog)) {//same logic as in OnResume
                        showWarningDialog(REQUEST_LOCATION);
                    }
                } else if(isDialogActive(locationDialog)) {
                    locationDialog.dismiss();
                }
            }
        };
        this.registerReceiver(locationReceiver, new IntentFilter(LocationManager
                .PROVIDERS_CHANGED_ACTION));

        locationRequest = new LocationRequest();
        locationRequest.setInterval(7 * ONE_SEC);//we make an update every 7 seconds
        locationRequest.setFastestInterval(5 * ONE_SEC);//an increase in these numbers would
        //lead to faster battery consumption
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client = LocationServices.getFusedLocationProviderClient(this);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if(location != null){
                    map.clear();//delete all the previous markers
                    LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
                    drawMarker(gps);
                    if(lastLocation == null){ //we only move the camera if we did not have a
                        // previous valid location
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, LOCATION_CAMERA_ZOOM));
                    }
                }
                lastLocation = location;
            }
        };
    }

    //this method call is initiated by a call to the getMapAsync
    //it initialises the map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
        client.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng gps;
                map.clear();
                lastLocation = location;
                if(location != null){
                    gps = new LatLng(location.getLatitude(), location.getLongitude());
                    drawMarker(gps);
                } else {
                    gps = new LatLng(OCEAN_LATITUDE, OCEAN_LONGITUDE);//if we do not have
                    //a location provided we set the camera in these default coordinates
                }
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, LOCATION_CAMERA_ZOOM));
            }
        });
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
            case REQUEST_LOCATION://if these permissions are not granted we exit the activity
            case REQUEST_INTERNET://as in the original version of the app
                if(grantResults.length > 0 && grantResults[0] != GRANTED){
                    finish();
                }
                break;
        }
    }

    //we need all these following methods for displaying the map
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
        //we have to check if the dialogs are not already shown, as the call to onResume
        //along with the onReceive from the BroadcastReceivers may display the dialog twice
        if(!gpsEnabled() && !isDialogActive(locationDialog)) {
            showWarningDialog(REQUEST_LOCATION);
        }
        if (!internetEnabled() && !isDialogActive(internetDialog)) {
            showWarningDialog(REQUEST_INTERNET);
        }
        if(map == null){ // we have to check if the map did not lose its state
            //for ex, if we exit the activity and then open it again
            mapView.getMapAsync(this);
        }
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

    //a dialog is null at the beginning of the activity, so we also have to check this
    private boolean isDialogActive(Dialog dialog) {
        return dialog != null && dialog.isShowing();
    }

    //this metohod shows the warning (internet or gps inactive) dialog
    private void showWarningDialog(int requestCode) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.gps_and_network_dialog);
        Window window = dialog.getWindow();
        //again, we get the dimensions from the user, through the presenter, respecting the
        //MVP structure
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
            locationDialog = dialog;//here we assign the dialog to one of the specific fields
            //they will need to be checked if they are active by other methods
        } else {
            title = getResources().getString(R.string.no_internet_title);
            message = getResources().getString(R.string.no_internet_message);
            settingsPath = Settings.ACTION_WIFI_SETTINGS;
            internetDialog = dialog;
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
                startActivity(new Intent(settingsPath));//we go to the specific activity in the
                //settings (location or wi-fi)
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    //this method is only called if the device is a phone, through the ring button
    //it shows the dialog after pressing "Bel RSR nu"
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
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    //this method initiates the call to the RSR services
    private void makeCall() {
        String phoneNumber = getResources().getString(R.string.phone_number);
        if (checkSelfPermission(CALL_PHONE) == GRANTED) {
            String dial = "tel:" + phoneNumber;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        } else {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        }
    }

    //here we draw the marker on the map on our location
    private void drawMarker(LatLng gps) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    gps.latitude, gps.longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker);
            Marker marker = map.addMarker(new MarkerOptions().position(gps).
                    snippet(address).icon(icon));
            marker.showInfoWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //checks if we have internet connection
    private boolean internetEnabled() {
        return connectivityManager.getActiveNetwork() != null;
    }

    //checks if gps is active
    private boolean gpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //initiates the continuous updates of the location in real time
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

    //we only need the ring button if the device is a phone, this being the only UI difference
    //for this activity
    public void loadPhone() {
        ringButton = (Button) findViewById(R.id.ring_button);
        ringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringButton.setVisibility(View.GONE);
                showCallDialog();
            }
        });
    }

    public void loadTablet() {
        //we don't have anything different to do, but we have to write the method because we
        //implement the UI interface
    }
}
