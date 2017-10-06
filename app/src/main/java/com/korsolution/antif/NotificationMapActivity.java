package com.korsolution.antif;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NotificationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;

    GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private String NOTI_NAME;
    private String LATITUDE;
    private String LONGITUDE;
    private String PLACE;
    private String UPDATE_DATE;
    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_map);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        NOTI_NAME = getIntent().getStringExtra("NOTI_NAME");
        LATITUDE = getIntent().getStringExtra("LATITUDE");
        LONGITUDE = getIntent().getStringExtra("LONGITUDE");
        PLACE = getIntent().getStringExtra("PLACE");
        UPDATE_DATE = getIntent().getStringExtra("UPDATE_DATE");
        USER_ID = getIntent().getStringExtra("USER_ID");

        appLog = new AppLogClass(this);

        // set Map
        mapView = (MapView) findViewById(R.id.mMapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        ImageButton btnMapDirection = (ImageButton) findViewById(R.id.btnMapDirection);
        btnMapDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapNavigation(LATITUDE, LONGITUDE);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.setMyLocationEnabled(true);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        LatLng currentlocation = new LatLng(13.7246005, 100.6331108);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 5));


        // Cut String Date Time
        String[] separated = UPDATE_DATE.split("-");
        String[] day = separated[2].split("T");
        String[] time = day[1].split("\\.");
        //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
        String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];

        placeMarkerAndAnimate(Double.parseDouble(LATITUDE), Double.parseDouble(LONGITUDE), NOTI_NAME, PLACE);
        //placeMarkerAndAnimate(Double.parseDouble(LATITUDE), Double.parseDouble(LONGITUDE), NOTI_NAME, dateTime);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        if (id == android.R.id.home) {

            Intent homeIntent = new Intent(this, NotificationListActivity.class);
            homeIntent.putExtra("USER_ID", USER_ID);
            startActivity(homeIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void placeMarkerAndAnimate(double lat, double lng, String title, String snippet) {
        // TODO Auto-generated method stub
        LatLng latLng = new LatLng(lat, lng);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
                .showInfoWindow();  //snippet is sub title
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));    //animateCamera is method move and zoom monitor to marker
    }

    private void MapNavigation(String lat, String lng) {

        LatLng cusLocation = null;
        if (lat.length() > 0 && lng.length() > 0) {
            cusLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        }

        // Check if GPS Enabled
        gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLocation()) {

            mylat = gpsTracker.getLatitude();
            mylng = gpsTracker.getLongitude();

            if (cusLocation != null) {

                Intent intentNavigation = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + mylat + "," + mylng + "&daddr=" + cusLocation.latitude + "," + cusLocation.longitude));
                startActivity(intentNavigation);

            } else {
                Toast.makeText(getApplicationContext(), "ไม่สามารถนำทางได้เนื่องจากไม่มีตำแหน่งรถ!", Toast.LENGTH_LONG).show();
            }

        } else {
            gpsTracker.showSettingsAlert();
        }
    }
}
