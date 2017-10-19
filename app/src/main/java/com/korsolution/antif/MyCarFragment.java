package com.korsolution.antif;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyCarFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private MapView mapView;
    private TextView txtVehicleName;
    private TextView txtVehicleLocation;
    private TextView txtDateTime;
    //private Button btnSelectCar;
    private RelativeLayout layoutSelectVehicle;
    private ImageView imgVehicle;
    private TextView txtStatusUpdate;
    private ImageView imgMenuCar;
    private TextView txtStatus;
    private ImageView imgCutEngine;
    private ImageView imgAuthen;

    private AccountDBClass AccountDB;
    private VehicleDBClass VehicleDB;

    protected ArrayList<JSONObject> feedDataList;
    protected ArrayList<JSONObject> feedDataListAuthen;
    protected ArrayList<JSONObject> feedDataListCutEngine;

    private String vehicleName;
    private String IMEI;
    private String LATITUDE;
    private String LONGITUDE;
    String IS_CUT_ENGINE;
    String SIM;

    GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private String USER_ID;

    private AppLogClass appLog;

    private Dialog dialog;


    public MyCarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if(USER_ID == null && getArguments() != null) {
            String userID = getArguments().getString("USER_ID");
            this.USER_ID = userID;
        }

        return inflater.inflate(R.layout.fragment_my_car, container, false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // check permission location
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //User has previously accepted this permission
            if (ActivityCompat.checkSelfPermission(getActivity(),
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

        // Add a marker in Sydney, Australia, and move the camera.
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AccountDB = new AccountDBClass(getActivity());
        VehicleDB = new VehicleDBClass(getActivity());

        appLog = new AppLogClass(getActivity());

        // check permission Location
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // set Map
        mapView = (MapView) view.findViewById(R.id.mMapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        setupWidgets(view);

        loadMyCar();
    }

    private void setupWidgets(View view) {

        txtVehicleName = (TextView) view.findViewById(R.id.txtVehicleName);
        txtVehicleLocation = (TextView) view.findViewById(R.id.txtVehicleLocation);
        txtDateTime = (TextView) view.findViewById(R.id.txtDateTime);

        /*btnSelectCar = (Button) view.findViewById(R.id.btnSelectCar);
        btnSelectCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("MyCarFragment", "กดปุ่มเลือกรถ", USER_ID);

                dialogAlertCarList(v);
            }
        });*/

        txtStatusUpdate = (TextView) view.findViewById(R.id.txtStatusUpdate);

        layoutSelectVehicle = (RelativeLayout) view.findViewById(R.id.layoutSelectVehicle);
        imgVehicle = (ImageView) view.findViewById(R.id.imgVehicle);
        imgMenuCar = (ImageView) view.findViewById(R.id.imgMenuCar);
        imgMenuCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(getActivity().getApplicationContext(), VehicleDetailActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                intent.putExtra("vehicleName", vehicleName);
                startActivity(intent);*/

                appLog.setLog("MyCarFragment", "กดปุ่มเลือกรถ", USER_ID);

                dialogVehicleSelect();
            }
        });

        txtStatus = (TextView) view.findViewById(R.id.txtStatus);

        imgCutEngine = (ImageView) view.findViewById(R.id.imgCutEngine);
        imgAuthen = (ImageView) view.findViewById(R.id.imgAuthen);

        imgCutEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("MyCarFragment", "กดปุ่ม CutEngine", USER_ID);

                if (IS_CUT_ENGINE.equals("NULL")) {
                    dialogAlertDeviceNotAvailable();
                } else {
                    //dialogAlertSendSMSCutEngine(vehicleName, SIM, IS_CUT_ENGINE);

                    int randomPIN = (int)(Math.random()*9000)+1000;
                    dialogAlertRandomNumber(String.valueOf(randomPIN), "cut_engine", v);
                }
            }
        });

        imgAuthen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("MyCarFragment", "กดปุ่ม Authen", USER_ID);

                dialogAlertAuthen();
            }
        });
    }

    private void MapNavigation(String lat, String lng) {

        LatLng cusLocation = null;
        if (lat.length() > 0 && lng.length() > 0) {
            cusLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        }

        // Check if GPS Enabled
        gpsTracker = new GPSTracker(getActivity());
        if (gpsTracker.canGetLocation()) {

            mylat = gpsTracker.getLatitude();
            mylng = gpsTracker.getLongitude();

            if (cusLocation != null) {

                Intent intentNavigation = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + mylat + "," + mylng + "&daddr=" + cusLocation.latitude + "," + cusLocation.longitude));
                startActivity(intentNavigation);

            } else {
                Toast.makeText(getActivity().getApplicationContext(), "ไม่สามารถนำทางได้เนื่องจากไม่มีตำแหน่งรถ!", Toast.LENGTH_LONG).show();
            }

        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    public void loadMyCar() {

        // check Account
        String[][] arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            String _USER_ID = arrData[0][1].toString();

            if (isOnline()) {
                new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_VEHICLE", "LYd162fYt", _USER_ID, "");
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void dialogVehicleSelect() {
        dialog = new Dialog(getActivity());
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("Choose your car.");
        dialog.setContentView(R.layout.dialog_select_vehicle);
        dialog.setCancelable(true);

        ListView mListView = (ListView) dialog.findViewById(R.id.listview);
        if (feedDataList != null) {
            VehicleSelectListViewAdapter mAdapter = new VehicleSelectListViewAdapter();
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
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

    public void dialogAlertCarList(View view) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

                rb = new RadioButton(getActivity());
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

                appLog.setLog("MyCarFragment", "เลือกรถ " + vehicleName, USER_ID);

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
            String _IS_IQNITION = arrDataVehicle[0][12].toString();
            String _IS_AUTHEN = arrDataVehicle[0][13].toString();
            String _SIM = arrDataVehicle[0][14].toString();
            String _TEL_EMERGING_1 = arrDataVehicle[0][15].toString();
            String _TEL_EMERGING_2 = arrDataVehicle[0][16].toString();
            String _TEL_EMERGING_3 = arrDataVehicle[0][17].toString();
            String _IS_UNPLUG_GPS = arrDataVehicle[0][18].toString();
            String _GSM_SIGNAL = arrDataVehicle[0][19].toString();
            String _NUM_SAT = arrDataVehicle[0][20].toString();
            String _CAR_IMAGE_FRONT = arrDataVehicle[0][21].toString();
            String _CAR_IMAGE_BACK = arrDataVehicle[0][22].toString();
            String _CAR_IMAGE_LEFT = arrDataVehicle[0][23].toString();
            String _CAR_IMAGE_RIGHT = arrDataVehicle[0][24].toString();
            String _STATUS_UPDATE = arrDataVehicle[0][25].toString();
            String _VEHICLE_TYPE_NAME = arrDataVehicle[0][26].toString();
            String _VEHICLE_BRAND_NAME = arrDataVehicle[0][27].toString();
            String _MODEL = arrDataVehicle[0][28].toString();
            String _YEAR = arrDataVehicle[0][29].toString();
            String _VEHICLE_COLOR = arrDataVehicle[0][30].toString();

            // Cut String Date Time
            String[] separated = _HISTORY_DATETIME.split("-");
            String[] day = separated[2].split("T");
            String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];

            txtVehicleName.setText(_VEHICLE_NAME);
            txtVehicleLocation.setText(_PLACE);
            txtDateTime.setText(dateTime);

            placeMarkerAndAnimate(Double.parseDouble(_LATITUDE), Double.parseDouble(_LONGITUDE), _VEHICLE_NAME, _PLACE);

            LATITUDE = _LATITUDE;
            LONGITUDE = _LONGITUDE;

            // set Image
            Glide.with(getActivity())
                    .load(_CAR_IMAGE_FRONT)
                    .error(R.drawable.image_car)
                    .bitmapTransform(new CropCircleTransformation(getActivity()))
                    .into(imgVehicle);

            switch (_VEHICLE_TYPE_NAME) {
                case "รถยนต์":
                    if (_STATUS.equals("1")) {
                        txtStatus.setTextColor(Color.parseColor("#04B404"));
                        txtStatus.setText("รถวิ่ง");
                    } else if (_STATUS.equals("2")) {
                        txtStatus.setTextColor(Color.parseColor("#FFFF00"));
                        txtStatus.setText("จอดรถติดเครื่อง");
                    } else if (_STATUS.equals("3")) {
                        txtStatus.setTextColor(Color.parseColor("#A4A4A4"));
                        txtStatus.setText("จอดดับเครื่อง");
                    } else if (_STATUS.equals("4")) {
                        txtStatus.setTextColor(Color.parseColor("#4000FF"));
                        txtStatus.setText("ความเร็วเกิน");
                    } else  if (_STATUS.equals("0")) {
                        txtStatus.setTextColor(Color.parseColor("#4000FF"));
                        txtStatus.setText("เคลื่อนที่ขณะดับเครื่อง");
                    }
                    break;
                case "รถจักรยานยนต์":
                    if (_STATUS.equals("5")) {
                        txtStatus.setTextColor(Color.parseColor("#04B404"));
                        txtStatus.setText("มีการขับรถหรือจอดติดไฟแดง");
                    } else if (_STATUS.equals("4")) {
                        txtStatus.setTextColor(Color.parseColor("#FFFF00"));
                        txtStatus.setText("จอดอยู่แล้วมีคนมากระแทก");
                    } else if (_STATUS.equals("3")) {
                        txtStatus.setTextColor(Color.parseColor("#FF0000"));
                        txtStatus.setText("อุบัติเหตุ");
                    } else if (_STATUS.equals("2")) {
                        txtStatus.setTextColor(Color.parseColor("#4000FF"));
                        txtStatus.setText("จอดรถแล้วมีการโยกหรือขยับเล็กน้อย");
                    } else if (_STATUS.equals("1")) {
                        txtStatus.setTextColor(Color.parseColor("#FF9300"));
                        txtStatus.setText("ไฟจากแบตเตอรี่รถหมดหรือต่ำกว่าที่กำหนด กำลังใช้แบตสำรองในตัว");
                    } else  if (_STATUS.equals("0")) {
                        txtStatus.setTextColor(Color.parseColor("#A4A4A4"));
                        txtStatus.setText("มีการเคลื่อนที่รถ");
                    }
                    break;
            }

            switch (_STATUS_UPDATE) {
                case "TRUE":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        txtStatusUpdate.setBackground( getResources().getDrawable(R.drawable.badge_circle_green));
                    }
                    break;
                case "FALSE":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        txtStatusUpdate.setBackground( getResources().getDrawable(R.drawable.badge_circle));
                    }
                    break;
            }

            switch (_IS_CUT_ENGINE) {
                case "1":   // ตัด
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        imgCutEngine.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.circle_background_red));
                    }
                    break;
                case "0":   // ต่อ
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        imgCutEngine.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.circle_background_gray));
                    }
                    break;
                default:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        imgCutEngine.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.circle_background_gray));
                    }
                    break;
            }

            switch (_IS_AUTHEN) {
                case "true":   // ตัด
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        imgAuthen.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.circle_background_blue));
                    }
                    break;
                case "false":   // ต่อ
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        imgAuthen.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.circle_background_gray));
                    }
                    break;
                default:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        imgAuthen.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.circle_background_gray));
                    }
                    break;
            }
        }

    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(getActivity());
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

                            //placeMarker(Double.parseDouble(strLATITUDE), Double.parseDouble(strLONGITUDE), strVEHICLE_NAME, strPLACE);

                            if (feedDataList.size() > 1) {
                                LatLng latLng = new LatLng(Double.parseDouble(strLATITUDE), Double.parseDouble(strLONGITUDE));
                                mMap.addMarker(new MarkerOptions().position(latLng).title(strVEHICLE_NAME).snippet(strPLACE));
                            } else {
                                placeMarker(Double.parseDouble(strLATITUDE), Double.parseDouble(strLONGITUDE), strVEHICLE_NAME, strPLACE);
                            }

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

                        //vehicleName = _VEHICLE_NAME;

                        String vehicleSelected = Prefs.getString("VehicleSelected","");
                        if (vehicleSelected.equals("")) {
                            vehicleName = _VEHICLE_NAME;
                            Prefs.putString("VehicleSelected", _VEHICLE_NAME);
                        } else {
                            vehicleName = vehicleSelected;
                        }

                        txtVehicleName.setText(_VEHICLE_NAME);
                        txtVehicleLocation.setText(_PLACE);
                        txtDateTime.setText(dateTime);

                        IMEI = _IMEI;
                        LATITUDE = _LATITUDE;
                        LONGITUDE = _LONGITUDE;
                        IS_CUT_ENGINE = _IS_CUT_ENGINE;
                        SIM = _SIM;

                        selectCar(vehicleName);
                    }


                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
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
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_map1)))
                .showInfoWindow();  //snippet is sub title
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));    //animateCamera is method move and zoom monitor to marker
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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
                convertView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.listview_item_select_vehicle, null);
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
                    final String strSIM = String.valueOf(feedDataList.get(position).getString("SIM"));
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
                    Glide.with(getActivity())
                            .load(strCAR_IMAGE_FRONT)
                            .error(R.drawable.image_car)
                            .bitmapTransform(new CropCircleTransformation(getActivity()))
                            .into(holder.imgVehicle);

                    holder.layout1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Toast.makeText(getActivity().getApplicationContext(), strVEHICLE_NAME, Toast.LENGTH_LONG).show();

                            appLog.setLog("MyCarFragment", "เลือกรถ " + strVEHICLE_NAME, USER_ID);

                            Prefs.putString("VehicleSelected", strVEHICLE_NAME);

                            vehicleName = strVEHICLE_NAME;
                            IMEI = strIMEI;
                            LATITUDE = strLATITUDE;
                            LONGITUDE = strLONGITUDE;
                            IS_CUT_ENGINE = strIS_CUT_ENGINE;
                            SIM = strSIM;
                            selectCar(strVEHICLE_NAME);

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

    public void dialogAlertAuthen() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        completeDialog.setTitle("คุณต้องการ authen ใช่หรือไม่?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    appLog.setLog("VehicleDetailActivity", "Authen " + IMEI, USER_ID);

                    new FeedAsynTaskAuthen().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_AUTHEN", "LYd162fYt", IMEI);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
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

    public class FeedAsynTaskAuthen extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;
        String _imei;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(getActivity());
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

                                appLog.setLog("VehicleDetailActivity", "Authen " + _imei + " Success", USER_ID);

                                dialogAlertAuthenSuccess();

                            } else if (STATUS.contains("NotUp")) {
                                appLog.setLog("VehicleDetailActivity", "Authen " + _imei + " รถไม่อัพเดท", USER_ID);

                                dialogAlertVehicleNotUpdate();
                            } else {
                                appLog.setLog("VehicleDetailActivity", "Authen " + _imei + " Fail", USER_ID);

                                dialogAlertAuthenFail();
                            }


                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }

            nDialog.dismiss();
        }
    }

    public void dialogAlertAuthenSuccess() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        completeDialog.setTitle("Authen Success.");
        //completeDialog.setMessage("คุณต้องการ " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME + " ใช่หรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_accept);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (isOnline()) {
                    loadMyCar();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        completeDialog.show();
    }

    public void dialogAlertAuthenFail() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        completeDialog.setTitle("Authen Fail!");
        completeDialog.setMessage("คุณต้องการลอง authen ใหม่อีกครั้งหรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_cancel);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    new FeedAsynTaskAuthen().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_AUTHEN", "LYd162fYt", IMEI);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
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

    public void dialogAlertVehicleNotUpdate() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        completeDialog.setTitle("รถของคุณไม่อัพเดท!!");
        completeDialog.setMessage("กรุณาลองใหม่อีกครั้ง");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (isOnline()) {
                    loadMyCar();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        completeDialog.show();
    }

    public void dialogAlertDeviceNotAvailable() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        completeDialog.setTitle("อุปกรณ์ไม่พร้อมใช้งาน!!");
        completeDialog.setMessage("กรุณาติดต่อเจ้าหน้าที่เพื่อแก้ไข");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("VehicleDetailActivity", "อุปกรณ์ไม่พร้อมใช้งาน", USER_ID);

                dialog.dismiss();

            }
        });
        completeDialog.show();
    }

    public void dialogAlertRandomNumber(final String _randomNumber, final String _goPage, View view) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View Viewlayout = inflater.inflate(R.layout.dialog_random_number, (ViewGroup) view.findViewById(R.id.layout_dialog));

        final TextView txtRandomNumber = (TextView) Viewlayout.findViewById(R.id.txtRandomNumber);
        final EditText edtRandomNumber = (EditText) Viewlayout.findViewById(R.id.edtRandomNumber);

        txtRandomNumber.setText(_randomNumber);

        CheckDialog.setTitle("Please enter the number to match the above number.");
        CheckDialog.setIcon(R.drawable.ic_action_edit);
        CheckDialog.setView(Viewlayout);

        // Button OK
        CheckDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String randomnumber = edtRandomNumber.getText().toString();

                appLog.setLog("NaviDrawerActivity", "Random number (" + _randomNumber + "/" + randomnumber + ")", USER_ID);

                if (randomnumber.equals(_randomNumber)) {
                    if (_goPage.equals("cut_engine")) {

                        dialogAlertSendSMSCutEngine(vehicleName, SIM, IS_CUT_ENGINE);

                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter the number to match the above number!!", Toast.LENGTH_LONG).show();
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

    public void dialogAlertSendSMSCutEngine(final String _VEHICLE_NAME, final String _SIM, final String _IS_CUT_ENGINE) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        String strCutEngineTitle = null;
        String smsCutEngine = null;
        String statusEngine = null;

        //if (_IS_CUT_ENGINE.equals("true")) {
        if (_IS_CUT_ENGINE.equals("1")) {
            strCutEngineTitle = "ต่อ";
            smsCutEngine = "at$out1=0";
            statusEngine = "0";
            //} else if (_IS_CUT_ENGINE.equals("false")) {
        } else if (_IS_CUT_ENGINE.equals("0")) {
            strCutEngineTitle = "ตัด";
            smsCutEngine = "at$out1=1";
            statusEngine = "1";
        }

        completeDialog.setTitle("ส่ง " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME);
        completeDialog.setMessage("คุณต้องการ " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME + " ใช่หรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_error);

        final String finalSmsCutEngine = smsCutEngine;
        final String finalStatusEngine = statusEngine;

        completeDialog.setPositiveButton(/*android.R.string.yes*/strCutEngineTitle + "สตาร์ทด้วย SMS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //SmsManager smsManager = SmsManager.getDefault();
                //smsManager.sendTextMessage(_SIM, null, finalSmsCutEngine, null, null);

                // Refresh List
                /*if (isOnline()) {
                    new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/TEST_GET_VEHICLE_A", "LYd162fYt", vehicleName);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }*/

                dialogAlertCutEngineBySMS(_VEHICLE_NAME, _SIM, finalSmsCutEngine, finalStatusEngine);

            }
        }).setNegativeButton(/*android.R.string.no*/strCutEngineTitle + "สตาร์ทด้วย Internet", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();

                // 1 = ตัด , 0 = ต่อ
                /*if (isOnline()) {
                    new FeedAsynTaskCutEngine().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/D_SET_CUTENGIN", "LYd162fYt", IMEI, finalStatusEngine);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }*/

                dialogAlertCutEngineByInternet(_VEHICLE_NAME, finalStatusEngine);
            }
        });
        completeDialog.show();
    }

    public void dialogAlertCutEngineBySMS(final String _vehicleName, final String _sim, final String _smsCutEngine, String _isCut) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        String statusEngine = null;
        if (_isCut.equals("0")) {
            statusEngine = "ต่อ";
        } else if (_isCut.equals("1")) {
            statusEngine = "ตัด";
        }

        completeDialog.setTitle("คุณต้องการ" + statusEngine + "สตาร์ท(" + _vehicleName + ")ด้วย SMS ใช่หรือไม่?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        final String finalStatusEngine = statusEngine;

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("VehicleDetailActivity", finalStatusEngine + "สตาร์ท " + _vehicleName +  " by sms", USER_ID);

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(_sim, null, _smsCutEngine, null, null);

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertCutEngineByInternet(final String _vehicleName, final String _isCut) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        String statusEngine = null;
        if (_isCut.equals("0")) {
            statusEngine = "ต่อ";
        } else if (_isCut.equals("1")) {
            statusEngine = "ตัด";
        }

        completeDialog.setTitle("คุณต้องการ" + statusEngine + "สตาร์ท(" + _vehicleName + ")ด้วย internet ใช่หรือไม่?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        final String finalStatusEngine = statusEngine;
        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    appLog.setLog("VehicleDetailActivity", finalStatusEngine + "สตาร์ท " + _vehicleName +  " by Internet", USER_ID);

                    new FeedAsynTaskCutEngine().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_CUTENGIN", "LYd162fYt", IMEI, _isCut);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
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

    public class FeedAsynTaskCutEngine extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;
        private String statusEngine;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(getActivity());
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(false);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                statusEngine = params[3];

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
                        .add("STATUS", params[3])
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

                feedDataListCutEngine = CuteFeedJsonUtil.feed(s);
                if (feedDataListCutEngine != null) {
                    for (int i = 0; i <= feedDataListCutEngine.size(); i++) {
                        try {

                            String STATUS = String.valueOf(feedDataListCutEngine.get(i).getString("STATUS"));

                            String status = null;
                            if (statusEngine.equals("0")) {
                                status = "ต่อ";
                            } else if (statusEngine.equals("1")) {
                                status = "ตัด";
                            }

                            if (STATUS.contains("Success")) {
                                appLog.setLog("VehicleDetailActivity", status + "สตาร์ท Success", USER_ID);

                                dialogAlertCutEngineSuccess(statusEngine);
                            } else if (STATUS.contains("NotUp")) {
                                appLog.setLog("VehicleDetailActivity", status + "สตาร์ท รถไม่อัพเดท", USER_ID);

                                dialogAlertVehicleNotUpdate();
                            } else {
                                appLog.setLog("VehicleDetailActivity", status + "สตาร์ท Fail", USER_ID);

                                dialogAlertCutEngineFail(statusEngine);
                            }


                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }

            nDialog.dismiss();
        }
    }

    public void dialogAlertCutEngineSuccess(String _statusEngine) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        String status;
        // 1 = ตัด , 0 = ต่อ
        if (_statusEngine.equals("1")) {
            status = "ตัด";
        } else {
            status = "ต่อ";
        }

        completeDialog.setTitle(status + "สตาร์ท สำเร็จ");
        //completeDialog.setMessage("คุณต้องการ " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME + " ใช่หรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_accept);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (isOnline()) {
                    loadMyCar();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        completeDialog.show();
    }

    public void dialogAlertCutEngineFail(String _statusEngine) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(getActivity());

        String status;
        // 1 = ตัด , 0 = ต่อ
        if (_statusEngine.equals("1")) {
            status = "ตัด";
        } else {
            status = "ต่อ";
        }

        completeDialog.setTitle(status + "สตาร์ท ไม่สำเร็จ!");
        completeDialog.setMessage("คุณต้องการ " + status + "สตาร์ท อีกครั้งหรือไม่");
        completeDialog.setIcon(R.drawable.ic_action_cancel);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                dialogAlertSendSMSCutEngine(vehicleName, SIM, IS_CUT_ENGINE);
            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    // check permission location
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //  TODO: Prompt with explanation!

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
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

                    // check permission location
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //User has previously accepted this permission
                        if (ActivityCompat.checkSelfPermission(getActivity(),
                                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mMap.setMyLocationEnabled(true);
                        }
                    } else {
                        //Not in api-23, no need to prompt
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }
}
