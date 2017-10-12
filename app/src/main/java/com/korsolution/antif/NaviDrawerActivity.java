package com.korsolution.antif;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NaviDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private View navHeader;
    private ImageView img_profile;
    private TextView txtName;
    private TextView txtEmail;

    static ImageButton btnAnnouncement;
    static TextView textOne;
    static int mNotifCount = 10;

    private GoogleMap mMap;
    private SupportMapFragment mMapView;

    private RelativeLayout mapLayout;
    private LinearLayout layoutMap;
    private ImageView imgOnline;
    private TextView txtOnline;
    private RelativeLayout parkingLayout;
    private RelativeLayout authenLayout;
    private LinearLayout shareLayout;
    private LinearLayout notiLayout;
    private TextView txtNumberNoti;
    private ImageView imgStatusAuthen;

    private AccountDBClass AccountDB;
    private VehicleDBClass VehicleDB;
    private NotificationDBClass NotificationDB;

    protected ArrayList<JSONObject> feedDataList;
    protected ArrayList<JSONObject> feedNotiDataList;
    protected ArrayList<JSONObject> feedShareDataList;
    protected ArrayList<JSONObject> feedDataListAuthen;

    private String vehicleName;
    private String IMEI;
    private String LATITUDE;
    private String LONGITUDE;

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private ImageView imgParking;
    private TextView txtParkingDetail;
    private TextView txtFeedNews;
    private TextView txtFeedReadMore;
    private ListView mListView;
    private FeedCommentDBClass FeedCommentDB;
    private FeedNewsListViewAdapter mAdapter;
    protected ArrayList<JSONObject> feedNewsDataList;
    protected ArrayList<JSONObject> feedNumberOfCommentDataList;

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private String USER_ID;

    private AppLogClass appLog;

    private Dialog dialog;
    private String option;

    // Broadcast Receiver
    private BroadcastReceiver mReceivedNewMsgBroadcastReceiver;
    private boolean isReceiverRegistered;

    //private Boolean isAuthen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navHeader = navigationView.getHeaderView(0);

        img_profile = (ImageView) navHeader.findViewById(R.id.img_profile);
        txtName = (TextView) navHeader.findViewById(R.id.txtName);
        txtEmail = (TextView) navHeader.findViewById(R.id.txtEmail);

        /*Glide.with(this)
                .load(R.drawable.image_car)
                .bitmapTransform(new CropCircleTransformation(this))
                .into(img_profile);*/

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
            feedDataNoti();

            loadNumberOnline();
        }

        // load image & name in Navi Drawer
        String[][] arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            String EMAIL = arrData[0][3].toString();
            String FIRST_NAME = arrData[0][6].toString();
            String LAST_NAME = arrData[0][7].toString();
            String LOGIN_TYPE = arrData[0][9].toString();
            String USER_PICTURE = arrData[0][12].toString();

            txtName.setText(FIRST_NAME + "  " + LAST_NAME);
            txtEmail.setText(EMAIL);

            /*Glide.with(this)
                    .load(USER_PICTURE)
                    .bitmapTransform(new CropCircleTransformation(this))
                    .error(R.drawable.blank_person_oval)
                    .into(img_profile);*/

            if (LOGIN_TYPE.equals("FB") && USER_PICTURE.equals("")) {

                String strImgProfile = Prefs.getString("uriImgProfile","");
                Uri uriImgProfile = Uri.parse(strImgProfile);

                Glide.with(this)
                        .load(uriImgProfile)
                        .bitmapTransform(new CropCircleTransformation(this))
                        .error(R.drawable.blank_person_oval)
                        .into(img_profile);
            } else {
                Glide.with(this)
                        .load(USER_PICTURE)
                        .bitmapTransform(new CropCircleTransformation(this))
                        .error(R.drawable.blank_person_oval)
                        .into(img_profile);
            }

            if (LOGIN_TYPE.equals("FB") && FIRST_NAME.equals("null") && LAST_NAME.equals("null")) {
                String firstName = Prefs.getString("firstName","");
                String lastName = Prefs.getString("lastName","");

                txtName.setText(firstName + "  " + lastName);
            }
        }

        appLog.setLog("NaviDrawerActivity", "เข้า Main Page App", USER_ID);
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

        if (isOnline()) {
            // upload log
            appLog.uploadLog();
        }

        /*for (int i=0; i<10; i++) {
            int randomPIN = (int)(Math.random()*9000)+1000;
            Toast.makeText(getApplicationContext(), String.valueOf(randomPIN), Toast.LENGTH_LONG).show();
        }*/
        int randomPIN = (int)(Math.random()*9000)+1000;
        //dialogAlertRandomNumber(String.valueOf(randomPIN));

        /*Intent mServiceIntent = new Intent(this, PopupMessageService.class);
        mServiceIntent.putExtra("message", "iCeeV5-4 สตาร์ทเครื่องยนต์");
        startService(mServiceIntent);*/

        // GCM Receiver
        Intent inboundIntent = getIntent();
        if (inboundIntent != null ){
            String _msg = inboundIntent.getStringExtra("MSG");
            if (_msg != null) {
                txtFeedNews.setText("Intent : " + _msg);
                //Toast.makeText(getApplicationContext(), "Intent : " + _msg, Toast.LENGTH_LONG).show();
                receiveNoti(_msg);
            }
        }

        mReceivedNewMsgBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String _msg = intent.getStringExtra("MSG");
                txtFeedNews.setText("Broadcast : " + _msg);
                //Toast.makeText(getApplicationContext(), "Broadcast : " + _msg, Toast.LENGTH_LONG).show();
                receiveNoti(_msg);
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        // get inside parentheses value in a string.
        String example = "United Arab Emirates Dirham (AED)";
        //example = "ทะเบียน (iCeeV4-1) พร้อมทำงาน";
        example = "ทะเบียน (iCeeV4-1) System Ready";        //Engine Start, Engine Stop, System Ready, Door Open
        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(example);
        while(m.find()) {
            System.out.println(m.group(1));
            //Toast.makeText(getApplicationContext(), m.group(1), Toast.LENGTH_LONG).show();
        }

        //Intent intent = new Intent(this, MainPageLatestActivity.class);
        //startActivity(intent);

        String modelName = getDeviceName();
        //Toast.makeText(getApplicationContext(), modelName, Toast.LENGTH_LONG).show();

        Log.i("token", FirebaseInstanceId.getInstance().getToken());
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String tmp = "";
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                tmp += key + ": " + value + "\n\n";
            }
            //txtFeedNews.setText(tmp);
            //Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_LONG).show();

            String message = bundle.getString("MSG");
            //txtFeedNews.setText(message);
            /*if (picture_url.length() > 1) {
                Intent intent = new Intent(this, SecondActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                intent.putExtras(bundle);

                startActivity(intent);
            }*/

            //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        // Motorcycle
        // iCeeMotorcycleV2R2-1
        /*
        antif มอเตอร์ไซค์จะส่งข้อมูลทุก 30 วินาที และจะอัพเดทข้อมูลทันทีที่มีการเปลี่ยนแปลง flag หรือมีการโทร
        flag คือ field [DigIn_Flag] ของตาราง tracking มีข้อมูลดังนี้

        DigIn_Flag |= IsUse << 5;  - มีการขับรถหรือจอดติดไฟแดง
        DigIn_Flag |= IsSlam << 4;  - จอดอยู่แล้วมีคนมากระแทก
        DigIn_Flag |= IsClash << 3;  - อุบัติเหตุ
        DigIn_Flag |= IsShake << 2; - จอดรถแล้วมีการโยกหรือขยับเล็กน้อย
        DigIn_Flag |= IsPowerLoss << 1; - ไฟจากแบตเตอรี่รถหมดหรือต่ำกว่าที่กำหนด กำลังใช้แบตสำรองในตัว
        DigIn_Flag |= IsMove << 0; - มีการเคลื่อนที่รถ
        */
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceivedNewMsgBroadcastReceiver,
                    new IntentFilter(FireBasePreferences.RECEIVED_NEW_MESSAGE));

            isReceiverRegistered = true;
        }
    }

    private void receiveNoti(String message) {

        String VehicleName = "";
        if (message != null) {
            if (message.contains("(")) {

                Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(message);
                while(m.find()) {
                    System.out.println(m.group(1));

                    VehicleName = m.group(1);
                }
            }
        }

        //if (message.contains("พร้อมทำงาน")) {
        if (message.contains("Ready")) {

            dialogAlertNotification(message, VehicleName);

        //} else if (message.contains("เปิดประตู")) {
        } else if (message.contains("Open")) {

            dialogAlertNotification(message, VehicleName);

        //} else if (message.contains("สตาร์ทเครื่องยนต์")) {
        } else if (message.contains("Start")) {

            dialogAlertNotification(message, VehicleName);

        //} else if (message.contains("ดับเครื่องยนต์")) {
        } else if (message.contains("Stop")) {

            Intent intent = new Intent(this, ParkingPhotoNewActivity.class);
            intent.putExtra("takePhoto", "1");
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);

        }

    }

    public void dialogAlertNotification(final String _message, final String VehicleName){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(NaviDrawerActivity.this);

        String title = "";
        String message = "";

        if (_message.contains("Ready")) {

            title = VehicleName + " System Ready";
            message = _message;

        } else if (_message.contains("Open")) {

            title = VehicleName + " Door Open";
            message = _message;

        } else if (_message.contains("Start")) {

            title = VehicleName + " Engine Start";
            message = "Do you want to Authen?";

        } else if (_message.contains("Stop")) {

            title = VehicleName + " Engine Stop";
            message = _message;

        }

        mDialog.setTitle(title);
        mDialog.setMessage(message);
        mDialog.setIcon(android.R.drawable.stat_sys_warning);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (_message.contains("Ready")) {

                    //

                } else if (_message.contains("Open")) {

                    //

                } else if (_message.contains("Start")) {

                    String[][] arrData = VehicleDB.SelectAll();
                    if (arrData != null) {
                        if (arrData.length > 1) {
                            //dialogAlertCarList("authen");

                            option = "authen";
                            dialogVehicleSelect();
                        } else if (arrData.length == 1) {

                            String _IMEI = arrData[0][1].toString();
                            IMEI = _IMEI;

                            dialogAlertAuthen();
                        }
                    }

                } else if (_message.contains("Stop")) {

                    Intent intent = new Intent(getApplicationContext(), ParkingPhotoNewActivity.class);
                    intent.putExtra("takePhoto", "1");
                    intent.putExtra("USER_ID", USER_ID);
                    startActivity(intent);

                }

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navi_drawer, menu);

        MenuItem itemNoti = menu.findItem(R.id.action_noti);
        itemNoti.setVisible(false);

        View count = menu.findItem(R.id.action_noti).getActionView();
        btnAnnouncement = (ImageButton) count.findViewById(R.id.btnAnnouncement);
        textOne = (TextView) count.findViewById(R.id.textOne);
        textOne.setText(String.valueOf(mNotifCount));
        //textOne.setVisibility(View.GONE);

        String[][] arrDataUnread = NotificationDB.SelectDataUnRead();
        if (arrDataUnread != null) {
            textOne.setVisibility(View.VISIBLE);
            textOne.setText(String.valueOf(arrDataUnread.length));
        }

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
            appLog.setLog("NaviDrawerActivity", "กดปุ่ม menu action bar Refresh", USER_ID);

            loadMyCar();
            loadImgParking();
            feedData();
            feedDataNoti();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        if (id == R.id.nav_refresh) {
            // Handle the camera action
            appLog.setLog("NaviDrawerActivity", "กดปุ่ม menu Refresh", USER_ID);

            loadMyCar();
            loadImgParking();
            feedData();
            feedDataNoti();

        } else if (id == R.id.nav_profile) {

            appLog.setLog("NaviDrawerActivity", "กดปุ่ม menu Profile", USER_ID);

            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);

        } else if (id == R.id.nav_add_car) {

            appLog.setLog("NaviDrawerActivity", "กดปุ่ม menu Add Car", USER_ID);

            Intent intent = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
            //Intent intent = new Intent(getApplicationContext(), AddCarActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);

        } else if (id == R.id.nav_setting) {

            appLog.setLog("NaviDrawerActivity", "กดปุ่ม menu Setting", USER_ID);

            //dialogAlertCarList("authen");

            String[][] arrData = VehicleDB.SelectAll();
            if (arrData != null) {
                if (arrData.length > 1) {
                    //dialogAlertCarList("setting");

                    option = "setting";
                    dialogVehicleSelect();

                } else if (arrData.length == 1) {
                    Intent intent = new Intent(getApplicationContext(), VehicleSettingActivity.class);
                    //Intent intent = new Intent(getApplicationContext(), VehicleDetailActivity.class);
                    intent.putExtra("USER_ID", USER_ID);
                    intent.putExtra("vehicleName", vehicleName);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_to_left, R.anim.no_change);
                }
            } else {
                dialogAlertNoCar();
            }

        } else if (id == R.id.nav_notification) {

            appLog.setLog("NaviDrawerActivity", "กดปุ่ม menu Notification", USER_ID);

            Intent intent = new Intent(getApplicationContext(), NotificationListActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);

        } else if (id == R.id.nav_feed_news) {

            appLog.setLog("NaviDrawerActivity", "กดปุ่ม menu Feed News", USER_ID);

            Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {

            appLog.setLog("NaviDrawerActivity", "กดปุ่ม menu Log out", USER_ID);

            dialogAlertLogOut();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    private void setupWidgets() {
        mapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
        layoutMap = (LinearLayout) findViewById(R.id.layoutMap);
        imgOnline = (ImageView) findViewById(R.id.imgOnline);
        txtOnline = (TextView) findViewById(R.id.txtOnline);
        parkingLayout = (RelativeLayout) findViewById(R.id.parkingLayout);
        authenLayout = (RelativeLayout) findViewById(R.id.authenLayout);
        notiLayout = (LinearLayout) findViewById(R.id.notiLayout);
        shareLayout  = (LinearLayout) findViewById(R.id.shareLayout);
        txtNumberNoti = (TextView) findViewById(R.id.txtNumberNoti);
        imgParking = (ImageView) findViewById(R.id.imgParking);
        txtParkingDetail = (TextView) findViewById(R.id.txtParkingDetail);
        txtFeedNews = (TextView) findViewById(R.id.txtFeedNews);
        txtFeedReadMore = (TextView) findViewById(R.id.txtFeedReadMore);
        mListView = (ListView) findViewById(R.id.listview);

        imgStatusAuthen = (ImageView) findViewById(R.id.imgStatusAuthen);

        layoutMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("NaviDrawerActivity", "กดปุ่ม Map", USER_ID);

                //Intent intent = new Intent(getApplicationContext(), MyCarActivity.class);
                Intent intent = new Intent(getApplicationContext(), MainPageActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            parkingLayout.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        parkingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("NaviDrawerActivity", "กดปุ่ม Parking", USER_ID);

                //Intent intent = new Intent(getApplicationContext(), ParkingPhotoActivity.class);
                Intent intent = new Intent(getApplicationContext(), ParkingPhotoNewActivity.class);
                intent.putExtra("takePhoto", "0");
                intent.putExtra("USER_ID", USER_ID);
                //intent.putExtra("vehicleName", vehicleName);
                startActivity(intent);

                //dialogAlertCarList("parking");

                /*String[][] arrData = VehicleDB.SelectAll();
                if (arrData != null) {
                    if (arrData.length > 1) {
                        dialogAlertCarList("parking");
                    } else if (arrData.length == 1) {

                        //Intent intent = new Intent(getApplicationContext(), ParkingPhotoActivity.class);
                        Intent intent = new Intent(getApplicationContext(), ParkingPhotoNewActivity.class);
                        intent.putExtra("USER_ID", USER_ID);
                        intent.putExtra("vehicleName", vehicleName);
                        startActivity(intent);
                    }
                }*/
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

                appLog.setLog("NaviDrawerActivity", "กดปุ่ม Authen", USER_ID);
                
                //dialogAlertCarList("authen");

                String[][] arrData = VehicleDB.SelectAll();
                if (arrData != null) {
                    if (arrData.length > 1) {
                        //dialogAlertCarList("authen");

                        option = "authen";
                        dialogVehicleSelect();
                    } else if (arrData.length == 1) {

                        String _IMEI = arrData[0][1].toString();
                        IMEI = _IMEI;

                        dialogAlertAuthen();
                    }
                }
            }
        });

        notiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("NaviDrawerActivity", "กดปุ่ม Notification", USER_ID);

                Intent intent = new Intent(getApplicationContext(), NotificationListActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);
            }
        });

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("NaviDrawerActivity", "กดปุ่ม Quick Share", USER_ID);

                int randomPIN = (int)(Math.random()*9000)+1000;
                dialogAlertRandomNumber(String.valueOf(randomPIN), "share");
            }
        });

        txtFeedReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("NaviDrawerActivity", "กดปุ่ม Feed News", USER_ID);

                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onPause() {
        ((MainApplication)getApplication()).mIsActivityRunning = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceivedNewMsgBroadcastReceiver);
        isReceiverRegistered = false;

        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();

        ((MainApplication)getApplication()).mIsActivityRunning = true;
        registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    private void feedDataNoti() {
        new NotiFeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_NOTI", "LYd162fYt", /*"2"*/USER_ID);
    }

    private void feedData() {
        new FeedNewsAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_FEED", "LYd162fYt", "", "");
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
            if (files != null) {
                if (files.length > 0) {
                    Glide.with(this)
                            .load(files[0])
                            .error(R.drawable.image_car)
                            .into(imgParking);
                }
            }
        }
    }
    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private void loadNumberOnline() {
        String[][] arrData = VehicleDB.SelectAll();
        if (arrData != null) {

            //txtOnline.setText("Online " + String.valueOf(arrData.length));

            int numberOnline = 0;

            for (int i = 0; i < arrData.length; i++) {
                String STATUS_UPDATE = arrData[i][25].toString();

                switch (STATUS_UPDATE) {
                    case "TRUE":
                        numberOnline += 1;
                        break;
                    case "FALSE":
                        numberOnline += 0;
                        break;
                    default:
                        numberOnline += 0;
                        break;
                }
            }

            if (numberOnline > 0) {
                Glide.with(this)
                        .load(R.drawable.green_circle)
                        .into(imgOnline);
                txtOnline.setText("Online " + String.valueOf(numberOnline));
            } else {
                Glide.with(this)
                        .load(R.drawable.red_circle)
                        .into(imgOnline);
                txtOnline.setText("Offline");
            }
        }
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

    private void dialogVehicleSelect() {
        dialog = new Dialog(this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("Choose your car.");
        dialog.setContentView(R.layout.dialog_select_vehicle);
        dialog.setCancelable(true);

        ListView mListView = (ListView) dialog.findViewById(R.id.listview);
        if (feedDataList != null) {
            VehicleSelectListViewAdapter mAdapterVehicle = new VehicleSelectListViewAdapter();
            mListView.setAdapter(mAdapterVehicle);
            mAdapterVehicle.notifyDataSetChanged();
        }

        Button btnCancle = (Button) dialog.findViewById(R.id.btnCancle);
        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void dialogAlertCarList(final String _goPage) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(NaviDrawerActivity.this);
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

                    appLog.setLog("NaviDrawerActivity", "เลือกรถ " + vehicleName + " Parking", USER_ID);

                    //Intent intent = new Intent(getApplicationContext(), ParkingPhotoActivity.class);
                    Intent intent = new Intent(getApplicationContext(), ParkingPhotoNewActivity.class);
                    intent.putExtra("USER_ID", USER_ID);
                    //intent.putExtra("vehicleName", vehicleName);
                    startActivity(intent);
                } else if (_goPage.equals("authen")) {

                    appLog.setLog("NaviDrawerActivity", "เลือกรถ " + vehicleName + " Authen", USER_ID);

                    String[][] arrData = VehicleDB.SelectAllByVehicleName(vehicleName);
                    if (arrData != null) {
                        String _IMEI = arrData[0][1].toString();

                        IMEI = _IMEI;

                        dialogAlertAuthen();
                    }

                } else if (_goPage.equals("setting")) {

                    appLog.setLog("NaviDrawerActivity", "เลือกรถ " + vehicleName + " Setting", USER_ID);

                    Intent intent = new Intent(getApplicationContext(), VehicleSettingActivity.class);
                    //Intent intent = new Intent(getApplicationContext(), VehicleDetailActivity.class);
                    intent.putExtra("USER_ID", USER_ID);
                    intent.putExtra("vehicleName", vehicleName);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_to_left, R.anim.no_change);
                }  else if (_goPage.equals("share")) {

                    appLog.setLog("NaviDrawerActivity", "เลือกรถ " + vehicleName + " Quick Share", USER_ID);

                    //dialogAlertShareFeed("1", USER_ID);
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

    public void dialogAlertNoCar() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(NaviDrawerActivity.this);

        completeDialog.setTitle("No car!!");
        completeDialog.setMessage("Do you want to add car?");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                Intent intent = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
                //Intent intent = new Intent(getApplicationContext(), AddCarActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertAuthen() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(NaviDrawerActivity.this);

        completeDialog.setTitle("คุณต้องการ authen ใช่หรือไม่?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    appLog.setLog("NaviDrawerActivity", "Authen " + IMEI, USER_ID);

                    new FeedAsynTaskAuthen().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_AUTHEN", "LYd162fYt", IMEI);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertAuthenSuccess() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(NaviDrawerActivity.this);

        completeDialog.setTitle("Authen Success.");
        //completeDialog.setMessage("คุณต้องการ " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME + " ใช่หรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_accept);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();

                loadMyCar();
                loadImgParking();
                feedData();
                feedDataNoti();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertVehicleNotUpdate() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(NaviDrawerActivity.this);

        completeDialog.setTitle("รถของคุณไม่อัพเดท!!");
        completeDialog.setMessage("กรุณาลองใหม่อีกครั้ง");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertAuthenFail() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(NaviDrawerActivity.this);

        completeDialog.setTitle("Authen Fail!");
        completeDialog.setMessage("คุณต้องการลอง authen ใหม่อีกครั้งหรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_cancel);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    new FeedAsynTaskAuthen().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_AUTHEN", "LYd162fYt", IMEI);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertLogOut(){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(NaviDrawerActivity.this);

        mDialog.setTitle("Do you want to log out?");
        mDialog.setIcon(R.drawable.ic_action_close);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("NaviDrawerActivity", "Log out", USER_ID);

                AccountDB.DeleteAccount();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("log_out", "1");
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

    public void dialogAlertRandomNumber(final String _randomNumber, final String _goPage) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(NaviDrawerActivity.this);
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View Viewlayout = inflater.inflate(R.layout.dialog_random_number, (ViewGroup) findViewById(R.id.layout_dialog));

        final TextView txtRandomNumber = (TextView) Viewlayout.findViewById(R.id.txtRandomNumber);
        final EditText edtRandomNumber = (EditText) Viewlayout.findViewById(R.id.edtRandomNumber);

        txtRandomNumber.setText(_randomNumber);

        CheckDialog.setTitle("Please enter the number.");
        CheckDialog.setIcon(R.drawable.ic_action_edit);
        CheckDialog.setView(Viewlayout);

        // Button OK
        CheckDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String randomnumber = edtRandomNumber.getText().toString();

                appLog.setLog("NaviDrawerActivity", "Random number (" + _randomNumber + "/" + randomnumber + ")", USER_ID);
                
                if (randomnumber.equals(_randomNumber)) {
                    /*if (_goPage.equals("authen")) {

                        String[][] arrData = VehicleDB.SelectAll();
                        if (arrData != null) {
                            if (arrData.length > 1) {
                                //dialogAlertCarList("authen");

                                option = "authen";
                                dialogVehicleSelect();
                            } else if (arrData.length == 1) {
                                Intent intent = new Intent(getApplicationContext(), VehicleDetailActivity.class);
                                intent.putExtra("USER_ID", USER_ID);
                                intent.putExtra("vehicleName", vehicleName);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_right_to_left, R.anim.no_change);
                            }
                        }

                    } else */if (_goPage.equals("share")) {

                        String[][] arrData = VehicleDB.SelectAll();
                        if (arrData != null) {
                            if (arrData.length > 1) {
                                //dialogAlertCarList("share");

                                option = "share";
                                dialogVehicleSelect();
                            } else if (arrData.length == 1) {
                                //dialogAlertShareFeed("1", USER_ID);

                                Intent intent = new Intent(getApplicationContext(), FeedAddActivity.class);
                                intent.putExtra("vehicleName", vehicleName);
                                intent.putExtra("FEED_HEADER_ID", "");
                                intent.putExtra("IMEI", IMEI);
                                intent.putExtra("USER_ID", USER_ID);
                                startActivity(intent);
                            }
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter the number to match the above number!!", Toast.LENGTH_LONG).show();
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

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(NaviDrawerActivity.this);
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
                //isAuthen = true;

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


                            /*if (!strIS_AUTHEN.equals(true)) {
                                isAuthen = false;
                            } else {
                                isAuthen = true;
                            }*/


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

                            //placeMarker(Double.parseDouble(strLATITUDE), Double.parseDouble(strLONGITUDE), strVEHICLE_NAME, strPLACE);

                            if (feedDataList.size() > 1) {
                                LatLng latLng = new LatLng(Double.parseDouble(strLATITUDE), Double.parseDouble(strLONGITUDE));
                                mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_green))
                                        .position(latLng)
                                        .title(strVEHICLE_NAME)
                                        .snippet(strPLACE));
                            } else {
                                placeMarker(Double.parseDouble(strLATITUDE), Double.parseDouble(strLONGITUDE), strVEHICLE_NAME, strPLACE);
                            }

                            LatLng latLng = new LatLng(Double.parseDouble(strLATITUDE), Double.parseDouble(strLONGITUDE));
                            builder.include(latLng);

                        } catch (Exception e) {

                        }
                    }

                    if (feedDataList.size() > 1) {
                        // Zoom Center All Lat Lng
                        LatLngBounds bounds = builder.build();
                        int padding = 0 + 100; // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        //mMap.moveCamera(cu);
                        mMap.animateCamera(cu);
                    }

/*
                    // Zoom Center All Lat Lng
                    LatLngBounds bounds = builder.build();
                    int padding = 0 + 50; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    //mMap.moveCamera(cu);
                    mMap.animateCamera(cu);
*/
                    //LatLng currentlocation = new LatLng(13.7246005, 100.6331108);
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 3));


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

                        //txtVehicleName.setText(_VEHICLE_NAME);
                        //txtVehicleLocation.setText("สถานที่ : " + _PLACE);
                        //txtDateTime.setText("วันที่ : " + dateTime);

                        vehicleName = _VEHICLE_NAME;

                        IMEI = _IMEI;
                        LATITUDE = _LATITUDE;
                        LONGITUDE = _LONGITUDE;

                        appLog.setLog("NaviDrawerActivity", "Load Vehicle Success", USER_ID);

                        if (_IS_AUTHEN.equals("true")) {
                            // set Image
                            Glide.with(NaviDrawerActivity.this)
                                    .load(R.drawable.green_circle)
                                    .into(imgStatusAuthen);
                        } else {
                            // set Image
                            Glide.with(NaviDrawerActivity.this)
                                    .load(R.drawable.red_circle)
                                    .into(imgStatusAuthen);
                        }

                        loadNumberOnline();
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
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_map1))).showInfoWindow();  //snippet is sub title
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
                holder.txtVehicleDisplay = (TextView) convertView.findViewById(R.id.txtVehicleDisplay);
                holder.txtDateTime = (TextView) convertView.findViewById(R.id.txtDateTime);
                holder.txtDetails = (TextView) convertView.findViewById(R.id.txtDetails);
                holder.imgComment = (ImageView) convertView.findViewById(R.id.imgComment);
                holder.txtNumberOfComment = (TextView) convertView.findViewById(R.id.txtNumberOfComment);
                holder.btnEditFeed = (ImageView) convertView.findViewById(R.id.btnEditFeed);

                convertView.setTag(R.id.imgVehicle,  holder.imgVehicle);
                convertView.setTag(R.id.txtTitle,  holder.txtTitle);
                convertView.setTag(R.id.txtVehicleDisplay,  holder.txtVehicleDisplay);
                convertView.setTag(R.id.txtDateTime,  holder.txtDateTime);
                convertView.setTag(R.id.txtDetails,  holder.txtDetails);
                convertView.setTag(R.id.imgComment,  holder.imgComment);
                convertView.setTag(R.id.txtNumberOfComment,  holder.txtNumberOfComment);
                convertView.setTag(R.id.btnEditFeed,  holder.btnEditFeed);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();

                holder.imgVehicle = (ImageView) convertView.getTag(R.id.imgVehicle);
                holder.txtTitle = (TextView) convertView.getTag(R.id.txtTitle);
                holder.txtVehicleDisplay = (TextView) convertView.getTag(R.id.txtVehicleDisplay);
                holder.txtDateTime = (TextView) convertView.getTag(R.id.txtDateTime);
                holder.txtDetails = (TextView) convertView.getTag(R.id.txtDetails);
                holder.imgComment = (ImageView) convertView.getTag(R.id.imgComment);
                holder.txtNumberOfComment = (TextView) convertView.getTag(R.id.txtNumberOfComment);
                holder.btnEditFeed = (ImageView) convertView.getTag(R.id.btnEditFeed);
            }

            // set Data
            if (feedNewsDataList != null) {
                try {

                    final String FEED_HEADER_ID = String.valueOf(feedNewsDataList.get(position).getString("FEED_HEADER_ID"));
                    final String FEED_TYPE_NAME = String.valueOf(feedNewsDataList.get(position).getString("FEED_TYPE_NAME"));
                    final String FEED_NAME = String.valueOf(feedNewsDataList.get(position).getString("FEED_NAME"));
                    final String FEED_DETAIL = String.valueOf(feedNewsDataList.get(position).getString("FEED_DETAIL"));
                    final String VEHICLE_DISPLAY = String.valueOf(feedNewsDataList.get(position).getString("VEHICLE_DISPLAY"));
                    final String VEHICLE_TYPE_NAME = String.valueOf(feedNewsDataList.get(position).getString("VEHICLE_TYPE_NAME"));
                    final String VEHICLE_BRAND_NAME = String.valueOf(feedNewsDataList.get(position).getString("VEHICLE_BRAND_NAME"));
                    final String MODEL = String.valueOf(feedNewsDataList.get(position).getString("MODEL"));
                    final String VEHICLE_COLOR = String.valueOf(feedNewsDataList.get(position).getString("VEHICLE_COLOR"));
                    final String YEAR = String.valueOf(feedNewsDataList.get(position).getString("YEAR"));
                    final String CAR_IMAGE_FRONT = String.valueOf(feedNewsDataList.get(position).getString("CAR_IMAGE_FRONT"));
                    final String CAR_IMAGE_BACK = String.valueOf(feedNewsDataList.get(position).getString("CAR_IMAGE_BACK"));
                    final String CAR_IMAGE_LEFT = String.valueOf(feedNewsDataList.get(position).getString("CAR_IMAGE_LEFT"));
                    final String CAR_IMAGE_RIGHT = String.valueOf(feedNewsDataList.get(position).getString("CAR_IMAGE_RIGHT"));
                    final String STATUS = String.valueOf(feedNewsDataList.get(position).getString("STATUS"));
                    final String CREATE_DATE = String.valueOf(feedNewsDataList.get(position).getString("CREATE_DATE"));
                    final String CREATE_BY = String.valueOf(feedNewsDataList.get(position).getString("CREATE_BY"));
                    final String DISPLAY_NAME = String.valueOf(feedNewsDataList.get(position).getString("DISPLAY_NAME"));
                    final String _USER_ID = String.valueOf(feedNewsDataList.get(position).getString("USER_ID"));
                    final String SHARE_LOCATION = String.valueOf(feedNewsDataList.get(position).getString("SHARE_LOCATION"));
                    final String LATITUDE = String.valueOf(feedNewsDataList.get(position).getString("LATITUDE"));
                    final String LONGITUDE = String.valueOf(feedNewsDataList.get(position).getString("LONGITUDE"));
                    final String PLACE = String.valueOf(feedNewsDataList.get(position).getString("PLACE"));
                    final String COUNT_COMMENT = String.valueOf(feedNewsDataList.get(position).getString("COUNT_COMMENT"));
                    final String USER_PICTURE = String.valueOf(feedNewsDataList.get(position).getString("USER_PICTURE"));
                    final String _IMEI = String.valueOf(feedNewsDataList.get(position).getString("IMEI"));
                    final String TEL_NUM = String.valueOf(feedNewsDataList.get(position).getString("TEL_NUM"));

                    // Cut String Date Time
                    String[] separated = CREATE_DATE.split("-");
                    String[] day = separated[2].split("T");
                    String[] time = day[1].split("\\.");
                    //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
                    String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];

                    holder.txtTitle.setText(DISPLAY_NAME + " : " + FEED_TYPE_NAME/* + String.valueOf(position)*/);
                    holder.txtVehicleDisplay.setText("ทะเบียน : " + VEHICLE_DISPLAY);
                    holder.txtDateTime.setText(dateTime);
                    holder.txtDetails.setText(FEED_NAME);

                    // set Image
                    Glide.with(NaviDrawerActivity.this)
                            .load(CAR_IMAGE_FRONT)
                            .error(R.drawable.blank_img)
                            .into(holder.imgVehicle);

                    holder.txtNumberOfComment.setText(COUNT_COMMENT);

                    /*String[][] arrData = FeedCommentDB.SelectAll(FEED_HEADER_ID);
                    if (arrData != null) {
                        holder.txtNumberOfComment.setText(String.valueOf(arrData.length));
                    } else {
                        holder.txtNumberOfComment.setVisibility(View.GONE);
                    }*/

                    holder.imgComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Prefs.putString("CAR_IMAGE_FRONT", CAR_IMAGE_FRONT);
                            Prefs.putString("CAR_IMAGE_BACK", CAR_IMAGE_BACK);
                            Prefs.putString("CAR_IMAGE_LEFT", CAR_IMAGE_LEFT);
                            Prefs.putString("CAR_IMAGE_RIGHT", CAR_IMAGE_RIGHT);

                            //Intent intent = new Intent(getApplicationContext(), FeedDetailsActivity.class);
                            //Intent intent = new Intent(getApplicationContext(), FeedDetailsNewActivity.class);
                            Intent intent = new Intent(getApplicationContext(), FeedDetailsLatestActivity.class);
                            intent.putExtra("FEED_HEADER_ID", FEED_HEADER_ID);
                            intent.putExtra("FEED_TYPE_NAME", FEED_TYPE_NAME);
                            intent.putExtra("FEED_NAME", FEED_NAME);
                            intent.putExtra("FEED_DETAIL", FEED_DETAIL);
                            intent.putExtra("VEHICLE_DISPLAY", VEHICLE_DISPLAY);
                            intent.putExtra("VEHICLE_TYPE_NAME", VEHICLE_TYPE_NAME);
                            intent.putExtra("VEHICLE_BRAND_NAME", VEHICLE_BRAND_NAME);
                            intent.putExtra("MODEL", MODEL);
                            intent.putExtra("VEHICLE_COLOR", VEHICLE_COLOR);
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
                            intent.putExtra("SHARE_LOCATION", SHARE_LOCATION);
                            intent.putExtra("LATITUDE", LATITUDE);
                            intent.putExtra("LONGITUDE", LONGITUDE);
                            intent.putExtra("PLACE", PLACE);
                            intent.putExtra("COUNT_COMMENT", COUNT_COMMENT);
                            intent.putExtra("USER_PICTURE", USER_PICTURE);
                            intent.putExtra("IMEI", _IMEI);
                            intent.putExtra("TEL_NUM", TEL_NUM);
                            startActivity(intent);
                        }
                    });

                } catch (Exception e) {

                }
            }

            return convertView;
        }

        public class ViewHolder {
            ImageView imgVehicle;
            TextView txtTitle;
            TextView txtVehicleDisplay;
            TextView txtDateTime;
            TextView txtDetails;
            ImageView imgComment;
            TextView txtNumberOfComment;
            ImageView btnEditFeed;
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
                        .add("FEED_HEADER_ID", params[3])
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
                            String FEED_DETAIL = String.valueOf(feedNewsDataList.get(i).getString("FEED_DETAIL"));
                            String VEHICLE_DISPLAY = String.valueOf(feedNewsDataList.get(i).getString("VEHICLE_DISPLAY"));
                            String VEHICLE_TYPE_NAME = String.valueOf(feedNewsDataList.get(i).getString("VEHICLE_TYPE_NAME"));
                            String VEHICLE_BRAND_NAME = String.valueOf(feedNewsDataList.get(i).getString("VEHICLE_BRAND_NAME"));
                            String MODEL = String.valueOf(feedNewsDataList.get(i).getString("MODEL"));
                            String VEHICLE_COLOR = String.valueOf(feedNewsDataList.get(i).getString("VEHICLE_COLOR"));
                            String YEAR = String.valueOf(feedNewsDataList.get(i).getString("YEAR"));
                            String CAR_IMAGE_FRONT = String.valueOf(feedNewsDataList.get(i).getString("CAR_IMAGE_FRONT"));
                            String CAR_IMAGE_BACK = String.valueOf(feedNewsDataList.get(i).getString("CAR_IMAGE_BACK"));
                            String CAR_IMAGE_LEFT = String.valueOf(feedNewsDataList.get(i).getString("CAR_IMAGE_LEFT"));
                            String CAR_IMAGE_RIGHT = String.valueOf(feedNewsDataList.get(i).getString("CAR_IMAGE_RIGHT"));
                            String STATUS = String.valueOf(feedNewsDataList.get(i).getString("STATUS"));
                            String CREATE_DATE = String.valueOf(feedNewsDataList.get(i).getString("CREATE_DATE"));
                            String CREATE_BY = String.valueOf(feedNewsDataList.get(i).getString("CREATE_BY"));
                            String DISPLAY_NAME = String.valueOf(feedNewsDataList.get(i).getString("DISPLAY_NAME"));
                            String _USER_ID = String.valueOf(feedNewsDataList.get(i).getString("USER_ID"));
                            String SHARE_LOCATION = String.valueOf(feedNewsDataList.get(i).getString("SHARE_LOCATION"));
                            String LATITUDE = String.valueOf(feedNewsDataList.get(i).getString("LATITUDE"));
                            String LONGITUDE = String.valueOf(feedNewsDataList.get(i).getString("LONGITUDE"));
                            String PLACE = String.valueOf(feedNewsDataList.get(i).getString("PLACE"));
                            String COUNT_COMMENT = String.valueOf(feedNewsDataList.get(i).getString("COUNT_COMMENT"));
                            String USER_PICTURE = String.valueOf(feedNewsDataList.get(i).getString("USER_PICTURE"));
                            String _IMEI = String.valueOf(feedNewsDataList.get(i).getString("IMEI"));
                            String TEL_NUM = String.valueOf(feedNewsDataList.get(i).getString("TEL_NUM"));

                            //Toast.makeText(getApplicationContext(), FEED_HEADER_ID, Toast.LENGTH_LONG).show();

                            new FeedAsynTaskComment().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_FEED_COMMENT", "LYd162fYt", FEED_HEADER_ID);

                        } catch (Exception e) {

                        }
                    }

                    mAdapter = new FeedNewsListViewAdapter();
                    mListView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                //Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
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
                //Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class NotiFeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(NaviDrawerActivity.this);
            nDialog.setMessage("Downloading..");
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

                    String[][] arrDataUnread = NotificationDB.SelectDataUnRead();
                    if (arrDataUnread != null) {
                        txtNumberNoti.setVisibility(View.VISIBLE);
                        txtNumberNoti.setText(String.valueOf(arrDataUnread.length));
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

    public class FeedAsynTaskAuthen extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;
        String _imei;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(NaviDrawerActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(false);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                _imei = params[2];

                // 1. connect server with okHttp
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        //.readTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(300, TimeUnit.SECONDS)
                        .build();


                // 2. assign post data
                RequestBody postData = new FormBody.Builder()
                        //.add("username", "admin")
                        //.add("password", "password")
                        .add("CODE_API", params[1])
                        .add("IMEI", params[2])
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

                feedDataListAuthen = CuteFeedJsonUtil.feed(s);
                if (feedDataListAuthen != null) {
                    for (int i = 0; i <= feedDataListAuthen.size(); i++) {
                        try {

                            String STATUS = String.valueOf(feedDataListAuthen.get(i).getString("STATUS"));

                            if (STATUS.contains("Success")) {

                                appLog.setLog("NaviDrawerActivity", "Authen " + _imei + " Success", USER_ID);

                                dialogAlertAuthenSuccess();

                            } else if (STATUS.contains("NotUp")) {
                                appLog.setLog("NaviDrawerActivity", "Authen " + _imei + " รถไม่อัพเดท", USER_ID);

                                dialogAlertVehicleNotUpdate();
                            } else {
                                appLog.setLog("NaviDrawerActivity", "Authen " + _imei + " Fail", USER_ID);

                                dialogAlertAuthenFail();
                            }


                        } catch (Exception e) {

                        }
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

    public class VehicleSelectListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return feedDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return feedDataList.size();
        }

        @Override
        public long getItemId(int position) {
            return feedDataList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.listview_item_select_vehicle, null);
                holder = new ViewHolder();

                holder.layout1 = (LinearLayout) convertView.findViewById(R.id.layout1);
                holder.imgVehicle = (ImageView) convertView.findViewById(R.id.imgVehicle);
                holder.txtVehicleName = (TextView) convertView.findViewById(R.id.txtVehicleName);
                holder.txtVehicleDateTime = (TextView) convertView.findViewById(R.id.txtVehicleDateTime);
                holder.txtVehicleStatus = (TextView) convertView.findViewById(R.id.txtVehicleStatus);
                holder.txtAuthenticate = (TextView) convertView.findViewById(R.id.txtAuthenticate);

                convertView.setTag(R.id.layout1,  holder.layout1);
                convertView.setTag(R.id.imgVehicle,  holder.imgVehicle);
                convertView.setTag(R.id.txtVehicleName,  holder.txtVehicleName);
                convertView.setTag(R.id.txtVehicleDateTime,  holder.txtVehicleDateTime);
                convertView.setTag(R.id.txtVehicleStatus,  holder.txtVehicleStatus);
                convertView.setTag(R.id.txtAuthenticate,  holder.txtAuthenticate);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();

                holder.layout1 = (LinearLayout) convertView.getTag(R.id.layout1);
                holder.imgVehicle = (ImageView) convertView.getTag(R.id.imgVehicle);
                holder.txtVehicleName = (TextView) convertView.getTag(R.id.txtVehicleName);
                holder.txtVehicleDateTime = (TextView) convertView.getTag(R.id.txtVehicleDateTime);
                holder.txtVehicleStatus = (TextView) convertView.getTag(R.id.txtVehicleStatus);
                holder.txtAuthenticate = (TextView) convertView.getTag(R.id.txtAuthenticate);
            }

            // Set Data
            if (feedDataList != null) {
                try {

                    final String strIMEI = String.valueOf(feedDataList.get(position).getString("IMEI"));
                    String strVEHICLE_DISPLAY = String.valueOf(feedDataList.get(position).getString("VEHICLE_DISPLAY"));
                    final String strVEHICLE_NAME = String.valueOf(feedDataList.get(position).getString("VEHICLE_NAME"));
                    String strHISTORY_DATETIME = String.valueOf(feedDataList.get(position).getString("HISTORY_DATETIME"));
                    final String strLATITUDE = String.valueOf(feedDataList.get(position).getString("LATITUDE"));
                    final String strLONGITUDE = String.valueOf(feedDataList.get(position).getString("LONGITUDE"));
                    String strPLACE = String.valueOf(feedDataList.get(position).getString("PLACE"));
                    String strANGLE = String.valueOf(feedDataList.get(position).getString("ANGLE"));
                    String strSPEED = String.valueOf(feedDataList.get(position).getString("SPEED"));
                    String strSTATUS = String.valueOf(feedDataList.get(position).getString("STATUS"));
                    final String strIS_CUT_ENGINE = String.valueOf(feedDataList.get(position).getString("IS_CUT_ENGINE"));
                    String strIS_IQNITION = String.valueOf(feedDataList.get(position).getString("IS_IQNITION"));
                    String strIS_AUTHEN = String.valueOf(feedDataList.get(position).getString("IS_AUTHEN"));
                    String strSIM = String.valueOf(feedDataList.get(position).getString("SIM"));
                    String strTEL_EMERGING_1 = String.valueOf(feedDataList.get(position).getString("TEL_EMERGING_1"));
                    String strTEL_EMERGING_2 = String.valueOf(feedDataList.get(position).getString("TEL_EMERGING_2"));
                    String strTEL_EMERGING_3 = String.valueOf(feedDataList.get(position).getString("TEL_EMERGING_3"));
                    String strIS_UNPLUG_GPS = String.valueOf(feedDataList.get(position).getString("IS_UNPLUG_GPS"));
                    String strGSM_SIGNAL = String.valueOf(feedDataList.get(position).getString("GSM_SIGNAL"));
                    String strNUM_SAT = String.valueOf(feedDataList.get(position).getString("NUM_SAT"));
                    String strCAR_IMAGE_FRONT = String.valueOf(feedDataList.get(position).getString("CAR_IMAGE_FRONT"));
                    String strCAR_IMAGE_BACK = String.valueOf(feedDataList.get(position).getString("CAR_IMAGE_BACK"));
                    String strCAR_IMAGE_LEFT = String.valueOf(feedDataList.get(position).getString("CAR_IMAGE_LEFT"));
                    String strCAR_IMAGE_RIGHT = String.valueOf(feedDataList.get(position).getString("CAR_IMAGE_RIGHT"));
                    String strSTATUS_UPDATE = String.valueOf(feedDataList.get(position).getString("STATUS_UPDATE"));
                    String strVEHICLE_TYPE_NAME = String.valueOf(feedDataList.get(position).getString("VEHICLE_TYPE_NAME"));
                    String strVEHICLE_BRAND_NAME = String.valueOf(feedDataList.get(position).getString("VEHICLE_BRAND_NAME"));
                    String strMODEL = String.valueOf(feedDataList.get(position).getString("MODEL"));
                    String strYEAR = String.valueOf(feedDataList.get(position).getString("YEAR"));
                    String strVEHICLE_COLOR = String.valueOf(feedDataList.get(position).getString("VEHICLE_COLOR"));

                    // Cut String Date Time
                    String[] separated = strHISTORY_DATETIME.split("-");
                    String[] day = separated[2].split("T");
                    //String[] time = day[1].split("\\.");
                    String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
                    //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];


                    String statusUpdate = "";
                    if (strSTATUS_UPDATE.equals("TRUE")) {
                        statusUpdate = "Online";
                        holder.txtVehicleStatus.setTextColor(Color.GREEN);
                    } else {
                        statusUpdate = "Offline";
                        holder.txtVehicleStatus.setTextColor(Color.RED);
                    }

                    holder.txtVehicleName.setText(strVEHICLE_NAME);
                    holder.txtVehicleDateTime.setText(dateTime);
                    holder.txtVehicleStatus.setText(statusUpdate);

                    // set Image
                    Glide.with(NaviDrawerActivity.this)
                            .load(strCAR_IMAGE_FRONT)
                            .error(R.drawable.image_car)
                            .bitmapTransform(new CropCircleTransformation(NaviDrawerActivity.this))
                            .into(holder.imgVehicle);

                    holder.layout1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Toast.makeText(getActivity().getApplicationContext(), strVEHICLE_NAME, Toast.LENGTH_LONG).show();

                            vehicleName = strVEHICLE_NAME;
                            IMEI = strIMEI;
                            LATITUDE = strLATITUDE;
                            LONGITUDE = strLONGITUDE;

                            appLog.setLog("NaviDrawerActivity", "เลือกรถ " + strVEHICLE_NAME, USER_ID);

                            switch (option) {
                                case "authen":

                                    appLog.setLog("NaviDrawerActivity", "เลือกรถ " + strVEHICLE_NAME + " Authen", USER_ID);

                                    dialogAlertAuthen();

                                    break;
                                case "setting":

                                    appLog.setLog("NaviDrawerActivity", "เลือกรถ " + strVEHICLE_NAME + " Setting", USER_ID);

                                    Intent intent = new Intent(getApplicationContext(), VehicleSettingActivity.class);
                                    //Intent intent = new Intent(getApplicationContext(), VehicleDetailActivity.class);
                                    intent.putExtra("USER_ID", USER_ID);
                                    intent.putExtra("vehicleName", strVEHICLE_NAME);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_right_to_left, R.anim.no_change);

                                    break;
                                case "share":

                                    appLog.setLog("NaviDrawerActivity", "เลือกรถ " + strVEHICLE_NAME + " Quick Share", USER_ID);

                                    //dialogAlertShareFeed("1", USER_ID);

                                    Intent intent1 = new Intent(getApplicationContext(), FeedAddActivity.class);
                                    intent1.putExtra("vehicleName", vehicleName);
                                    intent1.putExtra("FEED_HEADER_ID", "");
                                    intent1.putExtra("IMEI", IMEI);
                                    intent1.putExtra("USER_ID", USER_ID);
                                    startActivity(intent1);

                                    break;
                            }

                            dialog.dismiss();
                        }
                    });

                    switch (strIS_AUTHEN) {
                        case "true":
                            holder.txtAuthenticate.setTextColor(Color.GREEN);
                            holder.txtAuthenticate.setText("Authenticate");
                            break;
                        case "false":
                            holder.txtAuthenticate.setTextColor(Color.RED);
                            holder.txtAuthenticate.setText("Not authenticate");
                            break;
                        default:
                            holder.txtAuthenticate.setTextColor(Color.RED);
                            holder.txtAuthenticate.setText("Not authenticate");
                            break;
                    }


                } catch (Exception e) {

                }
            }

            return convertView;
        }

        public class ViewHolder {
            LinearLayout layout1;
            ImageView imgVehicle;
            TextView txtVehicleName;
            TextView txtVehicleDateTime;
            TextView txtVehicleStatus;
            TextView txtAuthenticate;
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

            appLog.setLog("NaviDrawerActivity", "ออกจาก Antif", USER_ID);

            moveTaskToBack(true);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                parkingLayout.setEnabled(true);
            }
        }
    }
}
