package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainAntifActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapView;

    private RelativeLayout mapLayout;
    private RelativeLayout parkingLayout;
    private LinearLayout authenLayout;
    private LinearLayout notiLayout;
    private TextView txtNumberNoti;

    private AccountDBClass AccountDB;
    private VehicleDBClass VehicleDB;
    private NotificationDBClass NotificationDB;

    protected ArrayList<JSONObject> feedDataList;
    protected ArrayList<JSONObject> feedNotiDataList;

    private String vehicleName;
    private String LATITUDE;
    private String LONGITUDE;

    static ImageButton btnAnnouncement;
    static TextView textOne;
    static int mNotifCount = 10;

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private ImageView imgParking;
    private TextView txtParkingDetail;
    private ListView lvNoti;
    private TextView txtFeedNews;
    private ListView mListView;
    private FeedCommentDBClass FeedCommentDB;
    private FeedNewsListViewAdapter mAdapter;
    protected ArrayList<JSONObject> feedNewsDataList;
    protected ArrayList<JSONObject> feedNumberOfCommentDataList;

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_antif);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        AccountDB = new AccountDBClass(this);
        VehicleDB = new VehicleDBClass(this);
        FeedCommentDB = new FeedCommentDBClass(this);
        NotificationDB = new NotificationDBClass(this);

        USER_ID = getIntent().getStringExtra("USER_ID");

        appLog = new AppLogClass(this);

        // set Map
        mMapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mMapView);
        mMapView.getMapAsync(this);

        setupWidgets();

        if (isOnline()) {
            loadMyCar();
            loadImgParking();
            feedData();
            feedNotification();
        }

        //Intent intent = new Intent(getApplicationContext(), NaviDrawerActivity.class);
        //startActivity(intent);

        appLog.setLog("MainAntifActivity", "เข้า Main Page App", USER_ID);
        /*AppLogClass appLog = new AppLogClass(this);
        appLog.setLog("Header", "Comment", USER_ID);
        String[][] arrData = appLog.getLog();
        if (arrData != null) {
            for (int i = 0; i < arrData.length; i++) {
                String ID = arrData[i][0].toString();
                String HEADER = arrData[i][1].toString();
                String LATITUDE = arrData[i][2].toString();
                String LONGITUDE = arrData[i][3].toString();
                String COMMENT = arrData[i][4].toString();
                String CREATE_BY = arrData[i][5].toString();
                String CREATE_DATE = arrData[i][6].toString();

                String _log = HEADER + LATITUDE + LONGITUDE + COMMENT + CREATE_BY + CREATE_DATE;
                Toast.makeText(getApplicationContext(), _log, Toast.LENGTH_LONG).show();

                appLog.delLog(ID);
            }
        }*/

        appLog.uploadLog();
    }

    private void setupWidgets() {
        mapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
        LinearLayout layoutMap = (LinearLayout) findViewById(R.id.layoutMap);
        parkingLayout = (RelativeLayout) findViewById(R.id.parkingLayout);
        authenLayout = (LinearLayout) findViewById(R.id.authenLayout);
        notiLayout = (LinearLayout) findViewById(R.id.notiLayout);
        txtNumberNoti = (TextView) findViewById(R.id.txtNumberNoti);
        imgParking = (ImageView) findViewById(R.id.imgParking);
        txtParkingDetail = (TextView) findViewById(R.id.txtParkingDetail);
        lvNoti = (ListView) findViewById(R.id.lvNoti);
        txtFeedNews = (TextView) findViewById(R.id.txtFeedNews);
        mListView = (ListView) findViewById(R.id.listview);

        layoutMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("MainAntifActivity", "กดปุ่ม Map", USER_ID);

                Intent intent = new Intent(getApplicationContext(), MyCarActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);
            }
        });

        parkingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(), ParkingPhotoActivity.class);
                //intent.putExtra("USER_ID", USER_ID);
                //intent.putExtra("vehicleName", vehicleName);
                //startActivity(intent);

                appLog.setLog("MainAntifActivity", "กดปุ่ม Parking", USER_ID);

                dialogAlertCarList("parking");
            }
        });

        authenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(), VehicleDetailActivity.class);
                //intent.putExtra("USER_ID", USER_ID);
                //intent.putExtra("vehicleName", vehicleName);
                //startActivity(intent);
                //overridePendingTransition(R.anim.slide_right_to_left, R.anim.no_change);

                appLog.setLog("MainAntifActivity", "กดปุ่ม Authen", USER_ID);


                dialogAlertCarList("authen");
            }
        });

        notiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("MainAntifActivity", "กดปุ่ม Notification", USER_ID);

                Intent intent = new Intent(getApplicationContext(), NotificationListActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);
            }
        });

        txtFeedNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("MainAntifActivity", "กดปุ่ม Feed News", USER_ID);


                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng currentlocation = new LatLng(13.7246005, 100.6331108);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 5));

        // Add a marker in Sydney, Australia, and move the camera.
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);

        MenuItem itemNoti = menu.findItem(R.id.action_noti);
        itemNoti.setVisible(false);

        View count = menu.findItem(R.id.action_noti).getActionView();
        btnAnnouncement = (ImageButton) count.findViewById(R.id.btnAnnouncement);
        textOne = (TextView) count.findViewById(R.id.textOne);
        textOne.setText(String.valueOf(mNotifCount));
        //textOne.setVisibility(View.GONE);
        btnAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NotificationListActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);
            }
        });

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

            appLog.setLog("MainAntifActivity", "กดปุ่ม menu Refresh", USER_ID);

            loadMyCar();
            loadImgParking();
            feedData();
            feedNotification();

            return true;
        }

        if (id == R.id.action_notification) {

            appLog.setLog("MainAntifActivity", "กดปุ่ม menu Notification", USER_ID);

            Intent intent = new Intent(getApplicationContext(), NotificationListActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);

            return true;
        }

        if (id == R.id.action_feed_news) {

            appLog.setLog("MainAntifActivity", "กดปุ่ม menu Feed News", USER_ID);

            Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);

            return true;
        }

        if (id == R.id.action_addcar) {

            appLog.setLog("MainAntifActivity", "กดปุ่ม menu Add Car", USER_ID);

            Intent intent = new Intent(getApplicationContext(), AddCarActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);

            return true;
        }

        if (id == R.id.action_profile) {

            appLog.setLog("MainAntifActivity", "กดปุ่ม menu Profile", USER_ID);

            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);

            return true;
        }

        if (id == R.id.action_logout) {

            appLog.setLog("MainAntifActivity", "กดปุ่ม menu Log out", USER_ID);

            dialogAlertLogOut();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void dialogAlertCarList(final String _goPage) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(MainAntifActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View Viewlayout = inflater.inflate(R.layout.dialog_car_list, (ViewGroup) findViewById(R.id.layout_dialog));

        RadioGroup rg = (RadioGroup) Viewlayout.findViewById(R.id.radiogroup);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.FILL_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(12, 6, 12, 6);

        String title;

        // select car
        String[] arrData = VehicleDB.SelectVehicleName();
        if (arrData != null) {
            title = "Choose your car.";

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

            title = "No car.";
        }

        CheckDialog.setTitle(title);
        CheckDialog.setIcon(R.drawable.ic_action_edit);
        CheckDialog.setView(Viewlayout);

        // Button OK
        CheckDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (_goPage.equals("parking")) {

                    appLog.setLog("MainAntifActivity", "เลือกรถ " + vehicleName + " Parking", USER_ID);

                    Intent intent = new Intent(getApplicationContext(), ParkingPhotoActivity.class);
                    intent.putExtra("USER_ID", USER_ID);
                    intent.putExtra("vehicleName", vehicleName);
                    startActivity(intent);
                } else if (_goPage.equals("authen")) {

                    appLog.setLog("MainAntifActivity", "เลือกรถ " + vehicleName + " Authen", USER_ID);

                    Intent intent = new Intent(getApplicationContext(), VehicleDetailActivity.class);
                    intent.putExtra("USER_ID", USER_ID);
                    intent.putExtra("vehicleName", vehicleName);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_to_left, R.anim.no_change);
                }

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

    public void dialogAlertLogOut(){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(MainAntifActivity.this);

        mDialog.setTitle("Do you want to log out?");
        mDialog.setIcon(R.drawable.ic_action_close);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("MainAntifActivity", "Log out", USER_ID);

                AccountDB.DeleteAccount();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //Toast.makeText(getBaseContext(), "Fail", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        mDialog.show();
    }

    private void feedNotification() {

        new FeedAsynTaskNoti().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_NOTI", "LYd162fYt", /*"2"*/USER_ID);
    }

    private void feedData() {
        new FeedNewsAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_FEED", "LYd162fYt", "");
    }

    private void loadImgParking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        txtParkingDetail.setText(Prefs.getString("ParkingDetails",""));

        File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            Glide.with(this)
                    .load(files[0])
                    .error(R.drawable.blank_img)
                    .into(imgParking);
        }
    }
    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
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

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(MainAntifActivity.this);
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

                        //txtVehicleName.setText(_VEHICLE_NAME);
                        //txtVehicleLocation.setText("สถานที่ : " + _PLACE);
                        //txtDateTime.setText("วันที่ : " + dateTime);

                        LATITUDE = _LATITUDE;
                        LONGITUDE = _LONGITUDE;

                        appLog.setLog("MainAntifActivity", "Load Vehicle Success", USER_ID);
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
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

    public class FeedNewsListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return feedNewsDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return feedNewsDataList.size();
        }

        @Override
        public long getItemId(int position) {
            return feedNewsDataList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.listview_item_feed_news, null);
                holder = new ViewHolder();

                holder.imgVehicle = (ImageView) convertView.findViewById(R.id.imgVehicle);
                holder.txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
                holder.txtDateTime = (TextView) convertView.findViewById(R.id.txtDateTime);
                holder.txtDetails = (TextView) convertView.findViewById(R.id.txtDetails);
                holder.imgComment = (ImageView) convertView.findViewById(R.id.imgComment);
                holder.txtNumberOfComment = (TextView) convertView.findViewById(R.id.txtNumberOfComment);

                if (feedNewsDataList != null) {
                    try {

                        final String FEED_HEADER_ID = String.valueOf(feedNewsDataList.get(position).getString("FEED_HEADER_ID"));
                        final String FEED_TYPE_NAME = String.valueOf(feedNewsDataList.get(position).getString("FEED_TYPE_NAME"));
                        final String FEED_NAME = String.valueOf(feedNewsDataList.get(position).getString("FEED_NAME"));
                        final String VEHICLE_DISPLAY = String.valueOf(feedNewsDataList.get(position).getString("VEHICLE_DISPLAY"));
                        final String VEHICLE_TYPE_NAME = String.valueOf(feedNewsDataList.get(position).getString("VEHICLE_TYPE_NAME"));
                        final String VEHICLE_BRAND_NAME = String.valueOf(feedNewsDataList.get(position).getString("VEHICLE_BRAND_NAME"));
                        final String MODEL = String.valueOf(feedNewsDataList.get(position).getString("MODEL"));
                        final String VEHICLE_COLOR_NAME = String.valueOf(feedNewsDataList.get(position).getString("VEHICLE_COLOR_NAME"));
                        final String YEAR = String.valueOf(feedNewsDataList.get(position).getString("YEAR"));
                        final String CAR_IMAGE_FRONT = String.valueOf(feedNewsDataList.get(position).getString("CAR_IMAGE_FRONT"));
                        final String CAR_IMAGE_BACK = String.valueOf(feedNewsDataList.get(position).getString("CAR_IMAGE_BACK"));
                        final String CAR_IMAGE_LEFT = String.valueOf(feedNewsDataList.get(position).getString("CAR_IMAGE_LEFT"));
                        final String CAR_IMAGE_RIGHT = String.valueOf(feedNewsDataList.get(position).getString("CAR_IMAGE_RIGHT"));
                        final String STATUS = String.valueOf(feedNewsDataList.get(position).getString("STATUS"));
                        final String CREATE_DATE = String.valueOf(feedNewsDataList.get(position).getString("CREATE_DATE"));
                        final String CREATE_BY = String.valueOf(feedNewsDataList.get(position).getString("CREATE_BY"));
                        final String DISPLAY_NAME = String.valueOf(feedNewsDataList.get(position).getString("DISPLAY_NAME"));
                        final String USER_ID = String.valueOf(feedNewsDataList.get(position).getString("USER_ID"));

                        // Cut String Date Time
                        String[] separated = CREATE_DATE.split("-");
                        String[] day = separated[2].split("T");
                        String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];

                        holder.txtTitle.setText(DISPLAY_NAME + " : " + FEED_TYPE_NAME);
                        holder.txtDateTime.setText(dateTime);
                        holder.txtDetails.setText(FEED_NAME);

                        if (!CAR_IMAGE_FRONT.equals("")) {
                            // set Image
                            Glide.with(MainAntifActivity.this)
                                    .load(CAR_IMAGE_FRONT)
                                    .into(holder.imgVehicle);
                        } else {
                            // set Image
                            Glide.with(MainAntifActivity.this)
                                    .load(R.drawable.blank_img)
                                    .into(holder.imgVehicle);
                        }


                        String[][] arrData = FeedCommentDB.SelectAll(FEED_HEADER_ID);
                        if (arrData != null) {
                            holder.txtNumberOfComment.setText(String.valueOf(arrData.length));
                        } else {
                            holder.txtNumberOfComment.setVisibility(View.GONE);
                        }

                        holder.imgComment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), FeedDetailsActivity.class);
                                intent.putExtra("FEED_HEADER_ID", FEED_HEADER_ID);
                                intent.putExtra("FEED_TYPE_NAME", FEED_TYPE_NAME);
                                intent.putExtra("FEED_NAME", FEED_NAME);
                                intent.putExtra("VEHICLE_DISPLAY", VEHICLE_DISPLAY);
                                intent.putExtra("VEHICLE_TYPE_NAME", VEHICLE_TYPE_NAME);
                                intent.putExtra("VEHICLE_BRAND_NAME", VEHICLE_BRAND_NAME);
                                intent.putExtra("MODEL", MODEL);
                                intent.putExtra("VEHICLE_COLOR_NAME", VEHICLE_COLOR_NAME);
                                intent.putExtra("YEAR", YEAR);
                                intent.putExtra("CAR_IMAGE_FRONT", CAR_IMAGE_FRONT);
                                intent.putExtra("CAR_IMAGE_BACK", CAR_IMAGE_BACK);
                                intent.putExtra("CAR_IMAGE_LEFT", CAR_IMAGE_LEFT);
                                intent.putExtra("CAR_IMAGE_RIGHT", CAR_IMAGE_RIGHT);
                                intent.putExtra("STATUS", STATUS);
                                intent.putExtra("CREATE_DATE", CREATE_DATE);
                                intent.putExtra("CREATE_BY", CREATE_BY);
                                intent.putExtra("DISPLAY_NAME", DISPLAY_NAME);
                                intent.putExtra("USER_ID", USER_ID);
                                startActivity(intent);
                            }
                        });

                    } catch (Exception e) {

                    }
                }

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            return convertView;
        }

        public class ViewHolder {
            ImageView imgVehicle;
            TextView txtTitle;
            TextView txtDateTime;
            TextView txtDetails;
            ImageView imgComment;
            TextView txtNumberOfComment;
        }
    }

    public class FeedNewsAsynTask extends AsyncTask<String, Void, String> {

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

                feedNewsDataList = CuteFeedJsonUtil.feed(s);
                if (feedNewsDataList != null) {

                    for (int i = 0; i <= feedNewsDataList.size(); i++) {
                        try {

                            String FEED_HEADER_ID = String.valueOf(feedNewsDataList.get(i).getString("FEED_HEADER_ID"));
                            String FEED_TYPE_NAME = String.valueOf(feedNewsDataList.get(i).getString("FEED_TYPE_NAME"));
                            String FEED_NAME = String.valueOf(feedNewsDataList.get(i).getString("FEED_NAME"));
                            String VEHICLE_DISPLAY = String.valueOf(feedNewsDataList.get(i).getString("VEHICLE_DISPLAY"));
                            String VEHICLE_TYPE_NAME = String.valueOf(feedNewsDataList.get(i).getString("VEHICLE_TYPE_NAME"));
                            String VEHICLE_BRAND_NAME = String.valueOf(feedNewsDataList.get(i).getString("VEHICLE_BRAND_NAME"));
                            String MODEL = String.valueOf(feedNewsDataList.get(i).getString("MODEL"));
                            String VEHICLE_COLOR_NAME = String.valueOf(feedNewsDataList.get(i).getString("VEHICLE_COLOR_NAME"));
                            String YEAR = String.valueOf(feedNewsDataList.get(i).getString("YEAR"));
                            String CAR_IMAGE_FRONT = String.valueOf(feedNewsDataList.get(i).getString("CAR_IMAGE_FRONT"));
                            String CAR_IMAGE_BACK = String.valueOf(feedNewsDataList.get(i).getString("CAR_IMAGE_BACK"));
                            String CAR_IMAGE_LEFT = String.valueOf(feedNewsDataList.get(i).getString("CAR_IMAGE_LEFT"));
                            String CAR_IMAGE_RIGHT = String.valueOf(feedNewsDataList.get(i).getString("CAR_IMAGE_RIGHT"));
                            String STATUS = String.valueOf(feedNewsDataList.get(i).getString("STATUS"));
                            String CREATE_DATE = String.valueOf(feedNewsDataList.get(i).getString("CREATE_DATE"));
                            String CREATE_BY = String.valueOf(feedNewsDataList.get(i).getString("CREATE_BY"));
                            String DISPLAY_NAME = String.valueOf(feedNewsDataList.get(i).getString("DISPLAY_NAME"));
                            String USER_ID = String.valueOf(feedNewsDataList.get(i).getString("USER_ID"));

                            new FeedAsynTaskComment().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_FEED_COMMENT", "LYd162fYt", FEED_HEADER_ID);

                            mAdapter = new FeedNewsListViewAdapter();
                            mListView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class FeedAsynTaskComment extends AsyncTask<String, Void, String> {

        String feedHeaderID;

        @Override
        protected String doInBackground(String... params) {

            try{

                feedHeaderID = params[2];

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
                        .add("FEED_HEADER_ID", params[2])
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

                FeedCommentDB.Delete(feedHeaderID);

                feedNumberOfCommentDataList = CuteFeedJsonUtil.feed(s);
                if (feedNumberOfCommentDataList != null) {
                    for (int i = 0; i <= feedNumberOfCommentDataList.size(); i++) {
                        try {
                            String FEED_COMMENT_ID = String.valueOf(feedNumberOfCommentDataList.get(i).getString("FEED_COMMENT_ID"));
                            String COMMENT = String.valueOf(feedNumberOfCommentDataList.get(i).getString("COMMENT"));
                            String DISPLAY_NAME = String.valueOf(feedNumberOfCommentDataList.get(i).getString("DISPLAY_NAME"));
                            String USER_PICTURE = String.valueOf(feedNumberOfCommentDataList.get(i).getString("USER_PICTURE"));
                            String CREATE_DATE = String.valueOf(feedNumberOfCommentDataList.get(i).getString("CREATE_DATE"));

                            FeedCommentDB.Insert(feedHeaderID, FEED_COMMENT_ID, COMMENT, DISPLAY_NAME, USER_PICTURE, CREATE_DATE);

                        } catch (Exception e) {

                        }
                    }



                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class FeedAsynTaskNoti extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(MainAntifActivity.this);
            nDialog.setMessage("Uploading..");
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

                NotificationDB.Delete();

                feedNotiDataList = CuteFeedJsonUtil.feed(s);
                if (feedNotiDataList != null) {

                    for (int i = 0; i <= feedNotiDataList.size(); i++) {
                        try {

                            String NOTI_ID = String.valueOf(feedNotiDataList.get(i).getString("NOTI_ID"));
                            String NOTI_NAME = String.valueOf(feedNotiDataList.get(i).getString("NOTI_NAME"));
                            String READED = String.valueOf(feedNotiDataList.get(i).getString("READED"));
                            String LATITUDE = String.valueOf(feedNotiDataList.get(i).getString("LATITUDE"));
                            String LONGITUDE = String.valueOf(feedNotiDataList.get(i).getString("LONGITUDE"));
                            String PLACE = String.valueOf(feedNotiDataList.get(i).getString("PLACE"));
                            String UPDATE_DATE = String.valueOf(feedNotiDataList.get(i).getString("UPDATE_DATE"));

                            NotificationDB.Insert(NOTI_ID, NOTI_NAME, READED, LATITUDE, LONGITUDE, PLACE, UPDATE_DATE);

                        } catch (Exception e) {

                        }
                    }

                    String[][] arrData = NotificationDB.SelectAll();
                    if (arrData != null) {
                        lvNoti.setAdapter(new ImageAdapter(MainAntifActivity.this, arrData));
                    } else {
                        //txtNoData.setVisibility(View.VISIBLE);
                    }

                    String[][] arrDataUnread = NotificationDB.SelectDataUnRead();
                    if (arrDataUnread != null) {
                        txtNumberNoti.setVisibility(View.VISIBLE);
                        txtNumberNoti.setText(String.valueOf(arrDataUnread.length));
                    }


                } else {
                    //Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                    //txtNoData.setVisibility(View.VISIBLE);
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
                //txtNoData.setVisibility(View.VISIBLE);
            }

            nDialog.dismiss();
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private String[][] arrList;

        public ImageAdapter(Context c, String[][] _list)
        {
            // TODO Auto-generated method stub
            context = c;
            arrList = _list;
        }

        public int getCount() {
            // TODO Auto-generated method stub
            return arrList.length;
        }

        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listview_item_noti, null);
            }

            final LinearLayout layout1 = (LinearLayout) convertView.findViewById(R.id.layout1);
            TextView txtNoti = (TextView) convertView.findViewById(R.id.txtNoti);
            TextView txtDateTimeNoti = (TextView) convertView.findViewById(R.id.txtDateTimeNoti);

            final String ID = arrList[position][0].toString();
            final String NOTI_ID = arrList[position][1].toString();
            String NOTI_NAME = arrList[position][2].toString();
            final String READED = arrList[position][3].toString();
            String LATITUDE = arrList[position][4].toString();
            String LONGITUDE = arrList[position][5].toString();
            String PLACE = arrList[position][6].toString();
            String UPDATE_DATE = arrList[position][7].toString();

            /*if (READED.equals("1")) {
                layout1.setBackgroundColor(Color.LTGRAY);
            } else {
                layout1.setBackgroundColor(Color.WHITE);
            }*/

            // Cut String Date Time
            String[] separated = UPDATE_DATE.split("-");
            String[] day = separated[2].split("T");
            String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];

            txtNoti.setText(NOTI_NAME);
            txtDateTimeNoti.setText(dateTime);

            layout1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });

            return convertView;
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            System.out.println("KEYCODE_BACK");

            appLog.setLog("MainAntifActivity", "ออกจาก Antif", USER_ID);

            moveTaskToBack(true);
        }
        return false;
    }
}
