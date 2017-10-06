package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MyCarActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private MapView mapView;
    private TextView txtVehicleName;
    private TextView txtVehicleLocation;
    private TextView txtDateTime;
    private Button btnSelectCar;

    private AccountDBClass AccountDB;
    private VehicleDBClass VehicleDB;

    protected ArrayList<JSONObject> feedDataList;

    private String vehicleName;
    private String LATITUDE;
    private String LONGITUDE;

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_car);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        USER_ID = getIntent().getStringExtra("USER_ID");

        AccountDB = new AccountDBClass(this);
        VehicleDB = new VehicleDBClass(this);

        appLog = new AppLogClass(this);

        // set Map
        mapView = (MapView) findViewById(R.id.mMapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        setupWidgets();

        loadMyCar();
    }

    private void setupWidgets() {
        txtVehicleName = (TextView) findViewById(R.id.txtVehicleName);
        txtVehicleLocation = (TextView) findViewById(R.id.txtVehicleLocation);
        txtDateTime = (TextView) findViewById(R.id.txtDateTime);

        btnSelectCar = (Button) findViewById(R.id.btnSelectCar);
        btnSelectCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAlertCarList(v);
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
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        LatLng currentlocation = new LatLng(13.7246005, 100.6331108);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 5));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_refresh, menu);

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

            appLog.setLog("MyCarActivity", "กดปุ่ม menu Refresh", USER_ID);

            loadMyCar();

            return true;
        }

        if (id == android.R.id.home) {

            Intent homeIntent = new Intent(this, NaviDrawerActivity.class);
            //Intent homeIntent = new Intent(this, MainAntifActivity.class);
            startActivity(homeIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadMyCar() {

        // check Account
        String[][] arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            String _USER_ID = arrData[0][1].toString();

            if (isOnline()) {
                new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_VEHICLE", "LYd162fYt", _USER_ID, "");
            } else {
                Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
            }
        }

    }

    public void dialogAlertCarList(View view) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(MyCarActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View Viewlayout = inflater.inflate(R.layout.dialog_car_list, (ViewGroup) view.findViewById(R.id.layout_dialog));

        RadioGroup rg = (RadioGroup) Viewlayout.findViewById(R.id.radiogroup);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.FILL_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(12, 6, 12, 6);


        String title;

        // select car
        String[] arrData = VehicleDB.SelectVehicleName();
        if (arrData != null) {
            title = "SELECT YOUR CAR";

            RadioButton rb;

            for (int i = 0; i < arrData.length; i++) {
                String _VEHICLE_NAME = arrData[i].toString();

                rb = new RadioButton(this);
                rb.setPadding(5, 5, 5, 5);
                rb.setTextColor(Color.parseColor("#000000"));
                rb.setText(_VEHICLE_NAME);
                rb.setId(i);
                rg.addView(rb, layoutParams);
            }

            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup rg, int checkedId) {
                    for(int i=0; i<rg.getChildCount(); i++) {
                        RadioButton btn = (RadioButton) rg.getChildAt(i);
                        if(btn.getId() == checkedId) {
                            String text = btn.getText().toString();

                            // do something with text
                            //Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
                            vehicleName = text;

                            return;
                        }
                    }
                }
            });
        } else {

            title = "ไม่มีรถ";
        }

        CheckDialog.setTitle(title);
        CheckDialog.setIcon(R.drawable.ic_action_edit);
        CheckDialog.setView(Viewlayout);

        // Button OK
        CheckDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                appLog.setLog("MyCarActivity", "กดปุ่ม เลือกรถ " + vehicleName, USER_ID);

                selectCar(vehicleName);
            }
        })
                // Button Cancel
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });
        CheckDialog.create();
        CheckDialog.show();
    }

    private void selectCar(String _vehicleName) {

        String[][] arrDataVehicle = VehicleDB.SelectAllByVehicleName(_vehicleName);
        if (arrDataVehicle != null) {
            String _IMEI = arrDataVehicle[0][1].toString();
            String _VEHICLE_DISPLAY = arrDataVehicle[0][2].toString();
            String _VEHICLE_NAME = arrDataVehicle[0][3].toString();
            String _HISTORY_DATETIME = arrDataVehicle[0][4].toString();
            String _LATITUDE = arrDataVehicle[0][5].toString();
            String _LONGITUDE = arrDataVehicle[0][6].toString();
            String _PLACE = arrDataVehicle[0][7].toString();
            String _ANGLE = arrDataVehicle[0][8].toString();
            String _SPEED = arrDataVehicle[0][9].toString();
            String _STATUS = arrDataVehicle[0][10].toString();
            String _IS_CUT_ENGINE = arrDataVehicle[0][11].toString();
            String _SIM = arrDataVehicle[0][12].toString();
            String _TEL_EMERGING_1 = arrDataVehicle[0][13].toString();
            String _TEL_EMERGING_2 = arrDataVehicle[0][14].toString();
            String _TEL_EMERGING_3 = arrDataVehicle[0][15].toString();
            String _IS_UNPLUG_GPS = arrDataVehicle[0][16].toString();
            String _GSM_SIGNAL = arrDataVehicle[0][17].toString();
            String _NUM_SAT = arrDataVehicle[0][18].toString();

            // Cut String Date Time
            String[] separated = _HISTORY_DATETIME.split("-");
            String[] day = separated[2].split("T");
            String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];

            txtVehicleName.setText(_VEHICLE_NAME);
            txtVehicleLocation.setText(/*"สถานที่ : " + */_PLACE);
            txtDateTime.setText("วันที่ : " + dateTime);

            placeMarkerAndAnimate(Double.parseDouble(_LATITUDE), Double.parseDouble(_LONGITUDE), _VEHICLE_NAME, _PLACE);

            LATITUDE = _LATITUDE;
            LONGITUDE = _LONGITUDE;
        }

    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(MyCarActivity.this);
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

                VehicleDB.Delete();

                feedDataList = CuteFeedJsonUtil.feed(s);
                if (feedDataList != null) {

                    // for Zoom Center All Lat Lng
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

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
                            String strIS_AUTHEN = String.valueOf(feedDataList.get(i).getString("IS_AUTHEN"));
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
                            String strVEHICLE_TYPE_NAME = String.valueOf(feedDataList.get(i).getString("VEHICLE_TYPE_NAME"));
                            String strVEHICLE_BRAND_NAME = String.valueOf(feedDataList.get(i).getString("VEHICLE_BRAND_NAME"));
                            String strMODEL = String.valueOf(feedDataList.get(i).getString("MODEL"));
                            String strYEAR = String.valueOf(feedDataList.get(i).getString("YEAR"));
                            String strVEHICLE_COLOR = String.valueOf(feedDataList.get(i).getString("VEHICLE_COLOR"));


                            VehicleDB.Insert(strIMEI, strVEHICLE_DISPLAY, strVEHICLE_NAME,
                                    strHISTORY_DATETIME, strLATITUDE, strLONGITUDE, strPLACE,
                                    strANGLE, strSPEED, strSTATUS, strIS_CUT_ENGINE, strIS_IQNITION, strIS_AUTHEN,
                                    strSIM, strTEL_EMERGING_1, strTEL_EMERGING_2, strTEL_EMERGING_3,
                                    strIS_UNPLUG_GPS, strGSM_SIGNAL, strNUM_SAT, strCAR_IMAGE_FRONT,
                                    strCAR_IMAGE_BACK, strCAR_IMAGE_LEFT, strCAR_IMAGE_RIGHT, strSTATUS_UPDATE,
                                    strVEHICLE_TYPE_NAME, strVEHICLE_BRAND_NAME, strMODEL, strYEAR, strVEHICLE_COLOR);

                            /*
                            txtVehicleName.setText(strVEHICLE_DISPLAY + "    " + strVEHICLE_NAME);
                            txtVehiclePlace.setText("สถานที่ : " + strPLACE);
                            txtDateTime.setText("วันที่ : " + strHISTORY_DATETIME);
                            */

                            placeMarker(Double.parseDouble(strLATITUDE), Double.parseDouble(strLONGITUDE), strVEHICLE_NAME, strPLACE);

                            LatLng latLng = new LatLng(Double.parseDouble(strLATITUDE), Double.parseDouble(strLONGITUDE));
                            builder.include(latLng);

                        } catch (Exception e) {

                        }
                    }

                    // Zoom Center All Lat Lng
                    LatLngBounds bounds = builder.build();
                    int padding = 0 + 100; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    //mMap.moveCamera(cu);
                    mMap.animateCamera(cu);


                    // Select first car
                    String[][] arrData = VehicleDB.SelectAll();
                    if (arrData != null) {
                        String _IMEI = arrData[0][1].toString();
                        String _VEHICLE_DISPLAY = arrData[0][2].toString();
                        String _VEHICLE_NAME = arrData[0][3].toString();
                        String _HISTORY_DATETIME = arrData[0][4].toString();
                        String _LATITUDE = arrData[0][5].toString();
                        String _LONGITUDE = arrData[0][6].toString();
                        String _PLACE = arrData[0][7].toString();
                        String _ANGLE = arrData[0][8].toString();
                        String _SPEED = arrData[0][9].toString();
                        String _STATUS = arrData[0][10].toString();
                        String _IS_CUT_ENGINE = arrData[0][11].toString();
                        String _IS_IQNITION = arrData[0][12].toString();
                        String _IS_AUTHEN = arrData[0][13].toString();
                        String _SIM = arrData[0][14].toString();
                        String _TEL_EMERGING_1 = arrData[0][15].toString();
                        String _TEL_EMERGING_2 = arrData[0][16].toString();
                        String _TEL_EMERGING_3 = arrData[0][17].toString();
                        String _IS_UNPLUG_GPS = arrData[0][18].toString();
                        String _GSM_SIGNAL = arrData[0][19].toString();
                        String _NUM_SAT = arrData[0][20].toString();
                        String _CAR_IMAGE_FRONT = arrData[0][21].toString();
                        String _CAR_IMAGE_BACK = arrData[0][22].toString();
                        String _CAR_IMAGE_LEFT = arrData[0][23].toString();
                        String _CAR_IMAGE_RIGHT = arrData[0][24].toString();
                        String _STATUS_UPDATE = arrData[0][25].toString();
                        String _VEHICLE_TYPE_NAME = arrData[0][26].toString();
                        String _VEHICLE_BRAND_NAME = arrData[0][27].toString();
                        String _MODEL = arrData[0][28].toString();
                        String _YEAR = arrData[0][29].toString();
                        String _VEHICLE_COLOR = arrData[0][30].toString();

                        // Cut String Date Time
                        String[] separated = _HISTORY_DATETIME.split("-");
                        String[] day = separated[2].split("T");
                        String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];

                        vehicleName = _VEHICLE_NAME;

                        txtVehicleName.setText(_VEHICLE_NAME);
                        txtVehicleLocation.setText("สถานที่ : " + _PLACE);
                        txtDateTime.setText("วันที่ : " + dateTime);

                        LATITUDE = _LATITUDE;
                        LONGITUDE = _LONGITUDE;
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                //Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }

            nDialog.dismiss();
        }
    }

    private void placeMarker(double lat, double lng, String title, String snippet) {
        // TODO Auto-generated method stub
        LatLng latLng = new LatLng(lat, lng);
        //mMapView.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(snippet)).showInfoWindow();  //snippet is sub title
        //mMapView.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));	//animateCamera is method move and zoom monitor to marker
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
