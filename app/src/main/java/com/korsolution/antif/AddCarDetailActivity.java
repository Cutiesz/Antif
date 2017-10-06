package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.anton46.stepsview.StepsView;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AddCarDetailActivity extends AppCompatActivity {

    private EditText edtVehicleName;
    private Spinner spnVehicleType;
    private Spinner spnVehicleBrand;
    private EditText edtVehicleModel;
    private EditText edtVehicleColor;
    private Spinner spnVehicleYear;
    private Button btnFinish;

    private String IMEI;
    private String URI_CAR_FRONT;
    private String URI_CAR_BACK;
    private String URI_CAR_LEFT;
    private String URI_CAR_RIGHT;
    private String EMERGENCY_NUMBER_1;
    private String EMERGENCY_NUMBER_2;
    private String EMERGENCY_NUMBER_3;

    private VehicleTypeDBClass VehicleTypeDB;
    private VehicleBrandDBClass VehicleBrandDB;
    private VehicleColorDBClass VehicleColorDB;

    protected ArrayList<JSONObject> feedDataListVehicleType;
    protected ArrayList<JSONObject> feedDataListVehicleBrand;
    protected ArrayList<JSONObject> feedDataListVehicleColor;
    protected ArrayList<JSONObject> feedDataListSetVehicle;

    private AccountDBClass AccountDB;

    private StepsView mStepsView;

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car_detail);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        USER_ID = getIntent().getStringExtra("USER_ID");
        IMEI = getIntent().getStringExtra("IMEI");
        URI_CAR_FRONT = getIntent().getStringExtra("URI_CAR_FRONT");
        URI_CAR_BACK = getIntent().getStringExtra("URI_CAR_BACK");
        URI_CAR_LEFT = getIntent().getStringExtra("URI_CAR_LEFT");
        URI_CAR_RIGHT = getIntent().getStringExtra("URI_CAR_RIGHT");
        EMERGENCY_NUMBER_1 = getIntent().getStringExtra("EMERGENCY_NUMBER_1");
        EMERGENCY_NUMBER_2 = getIntent().getStringExtra("EMERGENCY_NUMBER_2");
        EMERGENCY_NUMBER_3 = getIntent().getStringExtra("EMERGENCY_NUMBER_3");

        VehicleTypeDB = new VehicleTypeDBClass(this);
        VehicleBrandDB = new VehicleBrandDBClass(this);
        VehicleColorDB = new VehicleColorDBClass(this);

        AccountDB = new AccountDBClass(this);

        appLog = new AppLogClass(this);

        setupWidgets();

        // steps view
        String[] labels = {"Step 1", "Step 2", "Step 3", "Step 4"};
        mStepsView = (StepsView) findViewById(R.id.stepsView);
        mStepsView.setCompletedPosition(labels.length - 1)
                .setLabels(labels)
                .setBarColorIndicator(this.getResources().getColor(R.color.material_blue_grey_800))
                .setProgressColorIndicator(this.getResources().getColor(R.color.colorAccent))
                .setLabelColorIndicator(this.getResources().getColor(R.color.colorAccent))
                .drawView();

        new FeedAsynTaskGetVehicleType().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_TYPE", "LYd162fYt");
        new FeedAsynTaskGetVehicleBrand().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_BRAND", "LYd162fYt");
        //new FeedAsynTaskGetVehicleColor().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_COLOR", "LYd162fYt");

        ArrayList<String> years = new ArrayList<String>();
        years.add("Select Vehicle Year");
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        /*for (int i = 1900; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }*/
        for (int i = thisYear; i >= 1900; i--) {
            years.add(Integer.toString(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);

        spnVehicleYear.setAdapter(adapter);
    }

    private void setupWidgets() {

        edtVehicleName = (EditText) findViewById(R.id.edtVehicleName);
        spnVehicleType = (Spinner) findViewById(R.id.spnVehicleType);
        spnVehicleBrand = (Spinner) findViewById(R.id.spnVehicleBrand);
        edtVehicleModel = (EditText) findViewById(R.id.edtVehicleModel);
        edtVehicleColor = (EditText) findViewById(R.id.edtVehicleColor);
        spnVehicleYear = (Spinner) findViewById(R.id.spnVehicleYear);

        btnFinish = (Button) findViewById(R.id.btnFinish);

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String _vehicleName = edtVehicleName.getText().toString();
                String _vehicleType = spnVehicleType.getSelectedItem().toString();
                String _vehicleBrand = spnVehicleBrand.getSelectedItem().toString();
                String _vehicleModel = edtVehicleModel.getText().toString();
                String _vehicleColor = edtVehicleColor.getText().toString();
                String _vehicleYear = spnVehicleYear.getSelectedItem().toString();


                if (_vehicleName.length() > 0) {

                    if (_vehicleType.equals("Select Vehicle Type")) {

                        if (_vehicleBrand.equals("Select Vehicle Brand")) {

                            if (_vehicleModel.length() > 0) {

                                if (_vehicleColor.length() > 0) {

                                    if (_vehicleYear.equals("Select Vehicle Year")) {

                                        if (isOnline()) {
                                            appLog.setLog("AddCarDetailActivity", "กดปุ่ม Finish", USER_ID);

                                            addCar(_vehicleName, _vehicleType, _vehicleBrand, _vehicleModel, _vehicleColor, _vehicleYear);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                                        }

                                    } else {
                                        Toast.makeText(getApplicationContext(), "กรุณาเลือกปี!!", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), "กรุณากรอกสี!!", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "กรุณากรอกรุ่นย่อย!!", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "กรุณาเลือกยี่ห้อ!!", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "กรุณาเลือกประเภท!!", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "กรุณากรอกชื่อ หรือ ทะเบียนรถ!!", Toast.LENGTH_LONG).show();
                }

                //Intent intent = new Intent(getApplicationContext(), MainPageActivity.class);
                /*Intent intent = new Intent(getApplicationContext(), MainAntifActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);*/
            }
        });

    }

    private void addCar(String _vehicleName, String _vehicleType, String _vehicleBrand, String _vehicleModel, String _vehicleColor, String _vehicleYear) {

        String _vehicleTypeID = "";
        String _vehicleBrandID = "";

        String[][] arrDataType = VehicleTypeDB.SelectAllByName(_vehicleType);
        if (arrDataType != null) {
            _vehicleTypeID = arrDataType[0][1].toString();
        }

        String[][] arrDataBrand = VehicleBrandDB.SelectAllByName(_vehicleBrand);
        if (arrDataBrand != null) {
            _vehicleBrandID = arrDataBrand[0][1].toString();
        }

        String imgStr1 = "";
        String imgStr2 = "";
        String imgStr3 = "";
        String imgStr4 = "";

        try {

            // image
            Uri imgUri = Uri.parse("file://" + URI_CAR_FRONT);
            Bitmap mPhotoBitMap = BitmapHelper.readBitmap(AddCarDetailActivity.this, imgUri);
            if (mPhotoBitMap != null) {
                mPhotoBitMap = BitmapHelper.shrinkBitmap(mPhotoBitMap, 500,	0);
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mPhotoBitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] image = stream.toByteArray();
            final String img_str = Base64.encodeToString(image, 0);

            imgStr1 = img_str;

        } catch (Exception e) {

        }

        try {

            // image
            Uri imgUri1 = Uri.parse("file://" + URI_CAR_BACK);
            Bitmap mPhotoBitMap1 = BitmapHelper.readBitmap(AddCarDetailActivity.this, imgUri1);
            if (mPhotoBitMap1 != null) {
                mPhotoBitMap1 = BitmapHelper.shrinkBitmap(mPhotoBitMap1, 500,	0);
            }
            ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
            mPhotoBitMap1.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
            byte[] image1 = stream1.toByteArray();
            final String img_str1 = Base64.encodeToString(image1, 0);

            imgStr2 = img_str1;

        } catch (Exception e) {

        }

        try {

            // image
            Uri imgUri2 = Uri.parse("file://" + URI_CAR_LEFT);
            Bitmap mPhotoBitMap2 = BitmapHelper.readBitmap(AddCarDetailActivity.this, imgUri2);
            if (mPhotoBitMap2 != null) {
                mPhotoBitMap2 = BitmapHelper.shrinkBitmap(mPhotoBitMap2, 500,	0);
            }
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            mPhotoBitMap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
            byte[] image2 = stream2.toByteArray();
            final String img_str2 = Base64.encodeToString(image2, 0);

            imgStr3 = img_str2;

        } catch (Exception e) {

        }

        try {

            // image
            Uri imgUri3 = Uri.parse("file://" + URI_CAR_RIGHT);
            Bitmap mPhotoBitMap3 = BitmapHelper.readBitmap(AddCarDetailActivity.this, imgUri3);
            if (mPhotoBitMap3 != null) {
                mPhotoBitMap3 = BitmapHelper.shrinkBitmap(mPhotoBitMap3, 500,	0);
            }
            ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
            mPhotoBitMap3.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
            byte[] image3 = stream3.toByteArray();
            final String img_str3 = Base64.encodeToString(image3, 0);

            imgStr4 = img_str3;

        } catch (Exception e) {

        }


        // get current date time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTime_Current_Internet = sdf.format(new Date());
        //Toast.makeText(getBaseContext(), DateTime_Current_Internet, Toast.LENGTH_LONG).show();

        String[][] arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            String _USER_ID = arrData[0][1].toString();

            new FeedAsynTaskSetVehicle().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_VEHICLE", "LYd162fYt",
                    _vehicleName, _vehicleTypeID, _vehicleBrandID, _vehicleModel, _vehicleColor, _vehicleYear, _USER_ID,
                    EMERGENCY_NUMBER_1, EMERGENCY_NUMBER_2, EMERGENCY_NUMBER_3, imgStr1, imgStr2, imgStr3, imgStr4, DateTime_Current_Internet, _USER_ID, IMEI);
        }

    }

    public void dialogAlertAddCarSuccess(){
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(AddCarDetailActivity.this);

        completeDialog.setTitle("Add car Successfully.");
        completeDialog.setIcon(R.drawable.ic_action_accept);
        completeDialog.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //Intent intent = new Intent(getApplicationContext(), MainAntifActivity.class);
                Intent intent = new Intent(getApplicationContext(), NaviDrawerActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);

            }
        });
        completeDialog.show();
    }

    public void dialogAlertAddCarFail(){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(AddCarDetailActivity.this);

        mDialog.setTitle("Add car fail!!");
        mDialog.setMessage("Do you want to try add vehicle agian?");
        mDialog.setIcon(R.drawable.ic_action_close);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                String _vehicleName = edtVehicleName.getText().toString();
                String _vehicleType = spnVehicleType.getSelectedItem().toString();
                String _vehicleBrand = spnVehicleBrand.getSelectedItem().toString();
                String _vehicleModel = edtVehicleModel.getText().toString();
                String _vehicleColor = edtVehicleColor.getText().toString();
                String _vehicleYear = spnVehicleYear.getSelectedItem().toString();

                if (_vehicleName.length() > 0) {

                    if (_vehicleType.equals("Select Vehicle Type")) {

                        if (_vehicleBrand.equals("Select Vehicle Brand")) {

                            if (_vehicleModel.length() > 0) {

                                if (_vehicleColor.length() > 0) {

                                    if (_vehicleYear.equals("Select Vehicle Year")) {

                                        if (isOnline()) {
                                            appLog.setLog("AddCarDetailActivity", "กดปุ่ม Try add car agian", USER_ID);

                                            addCar(_vehicleName, _vehicleType, _vehicleBrand, _vehicleModel, _vehicleColor, _vehicleYear);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                                        }

                                    } else {
                                        Toast.makeText(getApplicationContext(), "กรุณาเลือกปี!!", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), "กรุณากรอกสี!!", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "กรุณากรอกรุ่นย่อย!!", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "กรุณาเลือกยี่ห้อ!!", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "กรุณาเลือกประเภท!!", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "กรุณากรอกชื่อ หรือ ทะเบียนรถ!!", Toast.LENGTH_LONG).show();
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

    public class FeedAsynTaskSetVehicle extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(AddCarDetailActivity.this);
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
                        .add("VEHICLE_DISPLAY", params[2])
                        .add("VEHICLE_TYPE_ID", params[3])
                        .add("VEHICLE_BRAND_ID", params[4])
                        .add("MODEL", params[5])
                        .add("VEHICLE_COLOR_ID", params[6])
                        .add("YEAR", params[7])
                        .add("USER_ID", params[8])
                        .add("TEL_EMERGING_1", params[9])
                        .add("TEL_EMERGING_2", params[10])
                        .add("TEL_EMERGING_3", params[11])
                        .add("CAR_IMAGE_FRONT", params[12])
                        .add("CAR_IMAGE_BACK", params[13])
                        .add("CAR_IMAGE_LEFT", params[14])
                        .add("CAR_IMAGE_RIGHT", params[15])
                        .add("UPDATE_DATE", params[16])
                        .add("UPDATE_BY", params[17])
                        .add("IMEI", params[18])
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

                feedDataListSetVehicle = CuteFeedJsonUtil.feed(s);
                if (feedDataListSetVehicle != null) {
                    for (int i = 0; i <= feedDataListSetVehicle.size(); i++) {
                        try {

                            String strSTATUS = String.valueOf(feedDataListSetVehicle.get(i).getString("STATUS"));

                            if (strSTATUS.equals("Success")) {
                                appLog.setLog("AddCarDetailActivity", "Add Car Success", USER_ID);

                                dialogAlertAddCarSuccess();
                            } else {
                                appLog.setLog("AddCarDetailActivity", "Add Car Fail", USER_ID);

                                dialogAlertAddCarFail();
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

    public class FeedAsynTaskGetVehicleType extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(AddCarDetailActivity.this);
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

                VehicleTypeDB.Delete();

                feedDataListVehicleType = CuteFeedJsonUtil.feed(s);
                if (feedDataListVehicleType != null) {
                    for (int i = 0; i <= feedDataListVehicleType.size(); i++) {
                        try {

                            String strVEHICLE_TYPE_ID = String.valueOf(feedDataListVehicleType.get(i).getString("VEHICLE_TYPE_ID"));
                            String strVEHICLE_TYPE_NAME = String.valueOf(feedDataListVehicleType.get(i).getString("VEHICLE_TYPE_NAME"));

                            VehicleTypeDB.Insert(strVEHICLE_TYPE_ID, strVEHICLE_TYPE_NAME);

                            String[] arrVehicleType = VehicleTypeDB.SelectName();
                            if (arrVehicleType != null) {

                                String[] arrSpinner;
                                arrSpinner = new String[arrVehicleType.length+1];

                                arrSpinner[0] = "Select Vehicle Type";

                                for (int j = 0; j < arrVehicleType.length; j++) {
                                    arrSpinner[j+1] = arrVehicleType[j].toString();
                                }

                                // Set List Spinner
                                ArrayAdapter<String> arrAd = new ArrayAdapter<String>(AddCarDetailActivity.this, android.R.layout.simple_spinner_item, arrSpinner);
                                arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnVehicleType.setAdapter(arrAd);
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

    public class FeedAsynTaskGetVehicleBrand extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(AddCarDetailActivity.this);
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

                VehicleBrandDB.Delete();

                feedDataListVehicleBrand = CuteFeedJsonUtil.feed(s);
                if (feedDataListVehicleBrand != null) {
                    for (int i = 0; i <= feedDataListVehicleBrand.size(); i++) {
                        try {

                            String strVEHICLE_BRAND_ID = String.valueOf(feedDataListVehicleBrand.get(i).getString("VEHICLE_BRAND_ID"));
                            String strVEHICLE_BRAND_NAME = String.valueOf(feedDataListVehicleBrand.get(i).getString("VEHICLE_BRAND_NAME"));
                            String strVEHICLE_BRAND_ICON = String.valueOf(feedDataListVehicleBrand.get(i).getString("VEHICLE_BRAND_ICON"));

                            VehicleBrandDB.Insert(strVEHICLE_BRAND_ID, strVEHICLE_BRAND_NAME, strVEHICLE_BRAND_ICON);

                            String[] arrVehicleBrand = VehicleBrandDB.SelectName();
                            if (arrVehicleBrand != null) {

                                String[] arrSpinner;
                                arrSpinner = new String[arrVehicleBrand.length+1];

                                arrSpinner[0] = "Select Vehicle Brand";

                                for (int j = 0; j < arrVehicleBrand.length; j++) {
                                    arrSpinner[j+1] = arrVehicleBrand[j].toString();
                                }

                                // Set List Spinner
                                ArrayAdapter<String> arrAd = new ArrayAdapter<String>(AddCarDetailActivity.this, android.R.layout.simple_spinner_item, arrSpinner);
                                arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnVehicleBrand.setAdapter(arrAd);
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

    public class FeedAsynTaskGetVehicleColor extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(AddCarDetailActivity.this);
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

                VehicleColorDB.Delete();

                feedDataListVehicleColor = CuteFeedJsonUtil.feed(s);
                if (feedDataListVehicleColor != null) {
                    for (int i = 0; i <= feedDataListVehicleColor.size(); i++) {
                        try {

                            String strVEHICLE_COLOR_ID = String.valueOf(feedDataListVehicleColor.get(i).getString("VEHICLE_COLOR_ID"));
                            String strVEHICLE_COLOR_NAME = String.valueOf(feedDataListVehicleColor.get(i).getString("VEHICLE_COLOR_NAME"));
                            String strVEHICLE_COLOR_CODE = String.valueOf(feedDataListVehicleColor.get(i).getString("VEHICLE_COLOR_CODE"));

                            VehicleColorDB.Insert(strVEHICLE_COLOR_ID, strVEHICLE_COLOR_NAME, strVEHICLE_COLOR_CODE);

                            String[] arrVehicleColor = VehicleColorDB.SelectName();
                            if (arrVehicleColor != null) {

                                String[] arrSpinner;
                                arrSpinner = new String[arrVehicleColor.length+1];

                                arrSpinner[0] = "Select Vehicle Color";

                                for (int j = 0; j < arrVehicleColor.length; j++) {
                                    arrSpinner[j+1] = arrVehicleColor[j].toString();
                                }

                                // Set List Spinner
                                ArrayAdapter<String> arrAd = new ArrayAdapter<String>(AddCarDetailActivity.this, android.R.layout.simple_spinner_item, arrSpinner);
                                arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                //spnVehicleColor.setAdapter(arrAd);
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
