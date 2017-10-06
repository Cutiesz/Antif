package com.korsolution.antif;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;

public class ProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapView;

    private ImageView imgEdit;
    //private ImageView imgAddCar;
    private TextView txtUserProfileName;
    private TextView txtUserFullName;
    private ImageView imgUserProfilePhoto;
    private TextView txtEmail;
    private TextView txtTel;
    private TextView txtIdCard;
    private TextView txtLincenseEXP;
    private TextView txtAddress;
    private TextView txtDistrict;
    private TextView txtAmphur;
    private TextView txtProvince;
    private TextView txtPostcode;

    private AccountDBClass AccountDB;

    private String email;

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        getSupportActionBar().hide();

        AccountDB = new AccountDBClass(this);

        USER_ID = getIntent().getStringExtra("USER_ID");

        appLog = new AppLogClass(this);

        // set Map
        mMapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mMapView);
        mMapView.getMapAsync(this);

        setupWidgets();

        loadProfileData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    private void loadProfileData() {

        String[][] arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            String USER_ID = arrData[0][1].toString();
            String PERMISSION_ID = arrData[0][2].toString();
            String EMAIL = arrData[0][3].toString();
            String PASSWORD = arrData[0][4].toString();
            String DISPLAY_NAME = arrData[0][5].toString();
            String FIRST_NAME = arrData[0][6].toString();
            String LAST_NAME = arrData[0][7].toString();
            String TEL = arrData[0][8].toString();
            String LOGIN_TYPE = arrData[0][9].toString();
            String ID_CARD = arrData[0][10].toString();
            String LICENSE_EXP = arrData[0][11].toString();
            String USER_PICTURE = arrData[0][12].toString();
            String ADDRESS = arrData[0][13].toString();
            String District_ID = arrData[0][14].toString();
            String District_Name = arrData[0][15].toString();
            String Amphur_ID = arrData[0][16].toString();
            String Amphur_Name = arrData[0][17].toString();
            String Province_ID = arrData[0][18].toString();
            String Province_Name = arrData[0][19].toString();
            String POSTCODE = arrData[0][20].toString();
            String LATITUDE_ADDRESS = arrData[0][21].toString();
            String LONGITUDE_ADDRESS = arrData[0][22].toString();

            email = EMAIL;

            if (!DISPLAY_NAME.equals(null)) {
                txtUserProfileName.setText(DISPLAY_NAME);
            }

            if (!FIRST_NAME.equals(null) || !LAST_NAME.equals(null)) {
                txtUserFullName.setText(FIRST_NAME + "  " + LAST_NAME);
            }

            txtEmail.setText("E-mail : " + EMAIL);
            txtTel.setText("Telephone : " + TEL);
            txtIdCard.setText("ID Card : " + ID_CARD);
            txtLincenseEXP.setText("License EXP : " + LICENSE_EXP);

            txtAddress.setText("Address : " + ADDRESS);
            txtDistrict.setText("District : " + District_Name);
            txtAmphur.setText("Amphur : " + Amphur_Name);
            txtProvince.setText("Province : " + Province_Name);
            txtPostcode.setText("Postcode : " + POSTCODE);

            // set Profile Image
            /*Glide.with(this)
                    .load(USER_PICTURE)
                    .error(R.drawable.blank_person_photo)
                    .into(imgUserProfilePhoto);*/

            if (LOGIN_TYPE.equals("FB") && USER_PICTURE.equals("")) {

                String strImgProfile = Prefs.getString("uriImgProfile","");
                Uri uriImgProfile = Uri.parse(strImgProfile);

                Glide.with(this)
                        .load(uriImgProfile)
                        .error(R.drawable.blank_person_photo)
                        .into(imgUserProfilePhoto);
            } else {
                Glide.with(this)
                        .load(USER_PICTURE)
                        .error(R.drawable.blank_person_photo)
                        .into(imgUserProfilePhoto);
            }

            if (LOGIN_TYPE.equals("FB") && FIRST_NAME.equals("null") && LAST_NAME.equals("null")) {
                String firstName = Prefs.getString("firstName","");
                String lastName = Prefs.getString("lastName","");

                txtUserFullName.setText(firstName + "  " + lastName);
            }

            if (LOGIN_TYPE.equals("FB") && DISPLAY_NAME.equals("null")) {
                String middleName = Prefs.getString("middleName","");

                txtUserProfileName.setText(middleName);
            }

            appLog.setLog("ProfileActivity", "Load Profile Data", USER_ID);
        }
    }

    private void setupWidgets() {
        imgEdit = (ImageView) findViewById(R.id.imgEdit);
        //imgAddCar = (ImageView) findViewById(R.id.imgAddCar);
        txtUserProfileName = (TextView) findViewById(R.id.txtUserProfileName);
        txtUserFullName = (TextView) findViewById(R.id.txtUserFullName);
        imgUserProfilePhoto = (ImageView) findViewById(R.id.imgUserProfilePhoto);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtTel = (TextView) findViewById(R.id.txtTel);
        txtIdCard = (TextView) findViewById(R.id.txtIdCard);
        txtLincenseEXP = (TextView) findViewById(R.id.txtLincenseEXP);
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        txtDistrict = (TextView) findViewById(R.id.txtDistrict);
        txtAmphur = (TextView) findViewById(R.id.txtAmphur);
        txtProvince = (TextView) findViewById(R.id.txtProvince);
        txtPostcode = (TextView) findViewById(R.id.txtPostcode);

        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("ProfileActivity", "กดปุ่ม Edit Profile", USER_ID);

                Intent intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
            }
        });

        /*imgAddCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
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

        String[][] arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            String DISPLAY_NAME = arrData[0][5].toString();
            String LATITUDE_ADDRESS = arrData[0][21].toString();
            String LONGITUDE_ADDRESS = arrData[0][22].toString();

            if (!LATITUDE_ADDRESS.equals("null") && !LONGITUDE_ADDRESS.equals("null")) {
                Double lat = Double.parseDouble(LATITUDE_ADDRESS);
                Double lng = Double.parseDouble(LONGITUDE_ADDRESS);

                placeMarker(lat, lng, DISPLAY_NAME, "");
            } else {
                LatLng currentlocation = new LatLng(13.7246005, 100.6331108);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 5));
            }

        } else {
            LatLng currentlocation = new LatLng(13.7246005, 100.6331108);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 5));
        }
    }

    private void placeMarker(double lat, double lng, String title, String snippet) {
        // TODO Auto-generated method stub
        LatLng latLng = new LatLng(lat, lng);
        //mMapView.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(title)/*.snippet(snippet)*/).showInfoWindow();  //snippet is sub title
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));	//animateCamera is method move and zoom monitor to marker
    }
}
