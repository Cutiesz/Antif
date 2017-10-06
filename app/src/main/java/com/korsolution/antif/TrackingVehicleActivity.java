package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TrackingVehicleActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;

    Bitmap.Config conf = Bitmap.Config.ARGB_8888;
    Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
    Canvas canvas1 = new Canvas(bmp);
    private Bitmap bmImg;

    private ImageView imgVehicle;
    private TextView txtName;
    private TextView txtPlace;
    private TextView txtDateTime;

    private String FEED_HEADER_ID;
    private String FEED_TYPE_NAME;
    private String FEED_NAME;
    private String VEHICLE_DISPLAY;
    private String VEHICLE_TYPE_NAME;
    private String VEHICLE_BRAND_NAME;
    private String MODEL;
    private String VEHICLE_COLOR;
    private String YEAR;
    private String CAR_IMAGE_FRONT;
    private String CAR_IMAGE_BACK;
    private String CAR_IMAGE_LEFT;
    private String CAR_IMAGE_RIGHT;
    private String STATUS;
    private String CREATE_DATE;
    private String CREATE_BY;
    private String DISPLAY_NAME;
    private String USER_ID;
    private String SHARE_LOCATION;
    private String LATITUDE;
    private String LONGITUDE;
    private String PLACE;
    private String COUNT_COMMENT;
    private String USER_PICTURE;
    private String IMEI;
    private String TEL_NUM;

    protected ArrayList<JSONObject> feedDataList;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_vehicle);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        // check permission Location
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // set Map
        mapView = (MapView) findViewById(R.id.mMapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        FEED_HEADER_ID = getIntent().getStringExtra("FEED_HEADER_ID");
        FEED_TYPE_NAME = getIntent().getStringExtra("FEED_TYPE_NAME");
        FEED_NAME = getIntent().getStringExtra("FEED_NAME");
        VEHICLE_DISPLAY = getIntent().getStringExtra("VEHICLE_DISPLAY");
        VEHICLE_TYPE_NAME = getIntent().getStringExtra("VEHICLE_TYPE_NAME");
        VEHICLE_BRAND_NAME = getIntent().getStringExtra("VEHICLE_BRAND_NAME");
        MODEL = getIntent().getStringExtra("MODEL");
        VEHICLE_COLOR = getIntent().getStringExtra("VEHICLE_COLOR");
        YEAR = getIntent().getStringExtra("YEAR");
        CAR_IMAGE_FRONT = getIntent().getStringExtra("CAR_IMAGE_FRONT");
        CAR_IMAGE_BACK = getIntent().getStringExtra("CAR_IMAGE_BACK");
        CAR_IMAGE_LEFT = getIntent().getStringExtra("CAR_IMAGE_LEFT");
        CAR_IMAGE_RIGHT = getIntent().getStringExtra("CAR_IMAGE_RIGHT");
        STATUS = getIntent().getStringExtra("STATUS");
        CREATE_DATE = getIntent().getStringExtra("CREATE_DATE");
        CREATE_BY = getIntent().getStringExtra("CREATE_BY");
        DISPLAY_NAME = getIntent().getStringExtra("DISPLAY_NAME");
        USER_ID = getIntent().getStringExtra("USER_ID");
        SHARE_LOCATION = getIntent().getStringExtra("SHARE_LOCATION");
        LATITUDE = getIntent().getStringExtra("LATITUDE");
        LONGITUDE = getIntent().getStringExtra("LONGITUDE");
        PLACE = getIntent().getStringExtra("PLACE");
        COUNT_COMMENT = getIntent().getStringExtra("COUNT_COMMENT");
        USER_PICTURE = getIntent().getStringExtra("USER_PICTURE");
        IMEI = getIntent().getStringExtra("IMEI");
        TEL_NUM = getIntent().getStringExtra("TEL_NUM");

        appLog = new AppLogClass(this);

        setupWidgets();

        loadData();
    }

    private void placeMarkerAndAnimate(double lat, double lng, String title, String snippet) {
        // TODO Auto-generated method stub
        LatLng latLng = new LatLng(lat, lng);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_map)))
                .showInfoWindow();  //snippet is sub title
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));    //animateCamera is method move and zoom monitor to marker
    }

    private void loadData() {
/*
        // Cut String Date Time
        String[] separated = CREATE_DATE.split("-");
        String[] day = separated[2].split("T");
        String[] time = day[1].split("\\.");
        //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + "\n" + day[1];
        String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];

        Glide.with(this)
                .load(CAR_IMAGE_FRONT)
                .error(R.drawable.blank_img)
                .into(imgVehicle);

        txtName.setText(VEHICLE_DISPLAY);
        txtPlace.setText("");
        txtDateTime.setText(dateTime);
*/

        if (isOnline()) {
            new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_VEHICLE", "LYd162fYt", "", IMEI);
        } else {
            Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_refresh, menu);

        MenuItem itemRefresh = menu.findItem(R.id.action_refresh);

        return true;
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

        if (id == R.id.action_refresh) {

            loadData();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupWidgets() {

        imgVehicle = (ImageView) findViewById(R.id.imgVehicle);
        txtName = (TextView) findViewById(R.id.txtName);
        txtPlace = (TextView) findViewById(R.id.txtPlace);
        txtDateTime = (TextView) findViewById(R.id.txtDateTime);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // check permission location
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //User has previously accepted this permission
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            //Not in api-23, no need to prompt
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        LatLng currentlocation = new LatLng(13.7246005, 100.6331108);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 5));

        //MapsInitializer.initialize(this);
        //addCustomMarker();
/*
        Double _lat = Double.parseDouble(LATITUDE);
        Double _lng = Double.parseDouble(LONGITUDE);
        placeMarkerAndAnimate(_lat, _lng, VEHICLE_DISPLAY, PLACE);
*/
    }

    private void addCustomMarker() {
        Log.d("AAAAA", "addCustomMarker()");
        if (mMap == null) {
            return;
        }

        LatLng currentlocation = new LatLng(13.7246005, 100.6331108);

        // adding a marker on map with image from  drawable
        mMap.addMarker(new MarkerOptions()
                .position(currentlocation)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.blank_person_oval))));
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }


    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(TrackingVehicleActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                // 1. connect server with okHttp
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();


                // 2. assign post data
                RequestBody postData = new FormBody.Builder()
                        //.add("username", "admin")
                        //.add("password", "password")
                        .add("CODE_API", params[1])
                        .add("USER_ID", params[2])
                        .add("IMEI", params[3])
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(postData)
                        .build();

                // 3. transport request to server
                okhttp3.Response response = client.newCall(request).execute();
                String result = response.body().string();

                return result;

            } catch (Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null) {
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

                s = s.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
                s = s.replace("<string xmlns=\"http://kontin.co.th\">", "");
                s = s.replace("</string>", "");

                feedDataList = CuteFeedJsonUtil.feed(s);
                if (feedDataList != null) {
                    for (int i = 0; i <= feedDataList.size(); i++) {
                        try {

                            String strIMEI = String.valueOf(feedDataList.get(i).getString("IMEI"));
                            String strVEHICLE_DISPLAY = String.valueOf(feedDataList.get(i).getString("VEHICLE_DISPLAY"));
                            String strVEHICLE_NAME = String.valueOf(feedDataList.get(i).getString("VEHICLE_NAME"));
                            String strHISTORY_DATETIME = String.valueOf(feedDataList.get(i).getString("HISTORY_DATETIME"));
                            String strLATITUDE = String.valueOf(feedDataList.get(i).getString("LATITUDE"));
                            String strLONGITUDE = String.valueOf(feedDataList.get(i).getString("LONGITUDE"));
                            String strPLACE = String.valueOf(feedDataList.get(i).getString("PLACE"));
                            String strANGLE = String.valueOf(feedDataList.get(i).getString("ANGLE"));
                            String strSPEED = String.valueOf(feedDataList.get(i).getString("SPEED"));
                            String strSTATUS = String.valueOf(feedDataList.get(i).getString("STATUS"));
                            String strIS_CUT_ENGINE = String.valueOf(feedDataList.get(i).getString("IS_CUT_ENGINE"));
                            String strIS_IQNITION = String.valueOf(feedDataList.get(i).getString("IS_IQNITION"));
                            String strSIM = String.valueOf(feedDataList.get(i).getString("SIM"));
                            String strTEL_EMERGING_1 = String.valueOf(feedDataList.get(i).getString("TEL_EMERGING_1"));
                            String strTEL_EMERGING_2 = String.valueOf(feedDataList.get(i).getString("TEL_EMERGING_2"));
                            String strTEL_EMERGING_3 = String.valueOf(feedDataList.get(i).getString("TEL_EMERGING_3"));
                            String strIS_UNPLUG_GPS = String.valueOf(feedDataList.get(i).getString("IS_UNPLUG_GPS"));
                            String strGSM_SIGNAL = String.valueOf(feedDataList.get(i).getString("GSM_SIGNAL"));
                            String strNUM_SAT = String.valueOf(feedDataList.get(i).getString("NUM_SAT"));
                            String strCAR_IMAGE_FRONT = String.valueOf(feedDataList.get(i).getString("CAR_IMAGE_FRONT"));
                            String strCAR_IMAGE_BACK = String.valueOf(feedDataList.get(i).getString("CAR_IMAGE_BACK"));
                            String strCAR_IMAGE_LEFT = String.valueOf(feedDataList.get(i).getString("CAR_IMAGE_LEFT"));
                            String strCAR_IMAGE_RIGHT = String.valueOf(feedDataList.get(i).getString("CAR_IMAGE_RIGHT"));
                            String strSTATUS_UPDATE = String.valueOf(feedDataList.get(i).getString("STATUS_UPDATE"));

                            // Cut String Date Time
                            String[] separated = strHISTORY_DATETIME.split("-");
                            String[] day = separated[2].split("T");
                            //String[] time = day[1].split("\\.");
                            String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
                            //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];

                            Glide.with(TrackingVehicleActivity.this)
                                    .load(strCAR_IMAGE_FRONT)
                                    .error(R.drawable.blank_img)
                                    .into(imgVehicle);

                            txtName.setText(strVEHICLE_NAME);
                            txtPlace.setText(strPLACE);
                            txtDateTime.setText(dateTime);

                            Double _lat = Double.parseDouble(strLATITUDE);
                            Double _lng = Double.parseDouble(strLONGITUDE);
                            placeMarkerAndAnimate(_lat, _lng, strVEHICLE_NAME, PLACE);

                        } catch (Exception e) {

                        }
                    }

                    appLog.setLog("TrackingVehicleActivity", "Load Vehicle Success", USER_ID);

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }

            nDialog.dismiss();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // check permission location
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //  TODO: Prompt with explanation!

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    if (ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }
}
