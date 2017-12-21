package com.korsolution.antif;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class VehicleSettingActivity extends AppCompatActivity {

    private ImageView imgCarFront;
    private ImageView imgCarLeft;
    private ImageView imgCarRight;
    private ImageView imgCarBack;
    private ImageView imgTakePhotoFront;
    private ImageView imgTakePhotoLeft;
    private ImageView imgTakePhotoRight;
    private ImageView imgTakePhotoBack;
    private RelativeLayout layoutSetNumber1;
    private RelativeLayout layoutSetNumber2;
    private RelativeLayout layoutSetNumber3;
    private TextView txtNumber1;
    private TextView txtNumber2;
    private TextView txtNumber3;
    private EditText edtVehicleLicense;
    private Spinner spnVehicleType;
    private Spinner spnVehicleBrand;
    private EditText edtVehicleGeneration;
    private Spinner spnVehicleYear;
    private EditText edtVehicleColor;

    protected ArrayList<JSONObject> feedDataList;
    protected ArrayList<JSONObject> feedDataListSetVehicle;
    protected ArrayList<JSONObject> feedDataListVehicleType;
    protected ArrayList<JSONObject> feedDataListVehicleBrand;
    protected ArrayList<JSONObject> feedDataListChengeNumber;
    protected ArrayList<JSONObject> feedDataListSetVehicleImage;

    private VehicleDBClass VehicleDB;
    private VehicleTypeDBClass VehicleTypeDB;
    private VehicleBrandDBClass VehicleBrandDB;

    private String vehicleName;

    String IMEI;
    String SIM;
    String TEL_EMERGING_1;
    String TEL_EMERGING_2;
    String TEL_EMERGING_3;

    private String vehicleType;
    private String vehicleBrand;
    private String vehicleYear;

    private PicturePathDBClass PicturePathDB;

    private String photoSelect = "Front";

    private static final int ACTION_TAKE_PHOTO_B = 1;
    private String mCurrentPhotoPath;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private final static int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    protected static Uri photoUri = null;

    private String USER_ID;

    private AppLogClass appLog;

    private Integer uploadImageNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_setting);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        USER_ID = getIntent().getStringExtra("USER_ID");
        vehicleName = getIntent().getStringExtra("vehicleName");
        IMEI = getIntent().getStringExtra("IMEI");

        VehicleDB = new VehicleDBClass(this);
        VehicleTypeDB = new VehicleTypeDBClass(this);
        VehicleBrandDB = new VehicleBrandDBClass(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        PicturePathDB = new PicturePathDBClass(this);
        PicturePathDB.Delete();
        PicturePathDB.Insert("0", "0", "0", "0", "0");

        appLog = new AppLogClass(this);

        setupWidgets();

        loadData();
        loadVehicleType();
        loadVehicleBrand();
        loadVehicleYear();
    }

    private void loadVehicleYear() {
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

        if (vehicleYear != null) {
            if (!vehicleYear.equals(null)) {
                int spinnerPosition = adapter.getPosition(vehicleYear);
                spnVehicleYear.setSelection(spinnerPosition);
            }
        }
    }

    private void uploadData() {

        String vehicleLicense = edtVehicleLicense.getText().toString();
        String vehicleType = spnVehicleType.getSelectedItem().toString();
        String vehicleBrand = spnVehicleBrand.getSelectedItem().toString();
        String vehicleGeneration = edtVehicleGeneration.getText().toString();
        String vehcleYear = spnVehicleYear.getSelectedItem().toString();
        String vehicleColor = edtVehicleColor.getText().toString();

        if (vehicleLicense.length() > 0) {
            if (!vehicleType.equals("Select Vehicle Type")) {
                if (!vehicleBrand.equals("Select Vehicle Brand")) {
                    if (vehicleGeneration.length() > 0) {
                        if (!vehcleYear.equals("Select Vehicle Year")) {
                            if (vehicleColor.length() > 0) {

                                String _vehicleTypeID = "";
                                String _vehicleBrandID = "";

                                String[][] arrDataType = VehicleTypeDB.SelectAllByName(vehicleType);
                                if (arrDataType != null) {
                                    _vehicleTypeID = arrDataType[0][1].toString();
                                }

                                String[][] arrDataBrand = VehicleBrandDB.SelectAllByName(vehicleBrand);
                                if (arrDataBrand != null) {
                                    _vehicleBrandID = arrDataBrand[0][1].toString();
                                }

                                // get current date time
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String DateTime_Current_Internet = sdf.format(new Date());
                                //Toast.makeText(getBaseContext(), DateTime_Current_Internet, Toast.LENGTH_LONG).show();

                                String imgName1 = IMEI + "_1.jpg";
                                String imgName2 = IMEI + "_2.jpg";
                                String imgName3 = IMEI + "_3.jpg";
                                String imgName4 = IMEI + "_4.jpg";

                                new FeedAsynTaskSetVehicle().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_VEHICLE", "LYd162fYt",
                                        vehicleLicense, _vehicleTypeID, /*_vehicleBrandID*/vehicleBrand, vehicleGeneration, vehicleColor, vehcleYear, USER_ID,
                                        TEL_EMERGING_1, TEL_EMERGING_2, TEL_EMERGING_3, imgName1, imgName2, imgName3, imgName4, DateTime_Current_Internet, USER_ID, IMEI);

                            } else {
                                Toast.makeText(getApplicationContext(), "กรุณาระบุสี!!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "กรุณาเลือกปี!!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "กรุณาเลือกรุ่น!!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "กรุณาเลือกยี่ห้อ!!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "กรุณาเลือกประเภท!!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "กรุณาระบุทะเบียนรถ!!", Toast.LENGTH_LONG).show();
        }

    }

    private void uploadImage() {

        uploadImageNo = 0;

        String[][] arrData = PicturePathDB.SelectAll();
        if (arrData != null) {
            String _font = arrData[0][1].toString();
            String _back = arrData[0][2].toString();
            String _left = arrData[0][3].toString();
            String _right = arrData[0][4].toString();

            for (int i = 1; i <= 4; i++) {
                switch (i) {
                    case 1:
                        if (!_font.equals("0")) {

                            // image
                            Uri imgUri = Uri.parse("file://" + _font);
                            Bitmap mPhotoBitMap = BitmapHelper.readBitmap(this, imgUri);
                            if (mPhotoBitMap != null) {
                                mPhotoBitMap = BitmapHelper.shrinkBitmap(mPhotoBitMap, 500,	0);
                            }
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            mPhotoBitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] image = stream.toByteArray();
                            final String img_str = Base64.encodeToString(image, 0);

                            new FeedAsynTaskSetVehicleImage().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPLOAD_IMAGE_CAR", img_str, IMEI, "1");

                        }
                        break;
                    case 2:
                        if (!_back.equals("0")) {

                            // image
                            Uri imgUri1 = Uri.parse("file://" + _back);
                            Bitmap mPhotoBitMap1 = BitmapHelper.readBitmap(this, imgUri1);
                            if (mPhotoBitMap1 != null) {
                                mPhotoBitMap1 = BitmapHelper.shrinkBitmap(mPhotoBitMap1, 500,	0);
                            }
                            ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                            mPhotoBitMap1.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
                            byte[] image1 = stream1.toByteArray();
                            final String img_str1 = Base64.encodeToString(image1, 0);

                            new FeedAsynTaskSetVehicleImage().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPLOAD_IMAGE_CAR", img_str1, IMEI, "2");

                        }
                        break;
                    case 3:
                        if (!_left.equals("0")) {

                            // image
                            Uri imgUri2 = Uri.parse("file://" + _left);
                            Bitmap mPhotoBitMap2 = BitmapHelper.readBitmap(this, imgUri2);
                            if (mPhotoBitMap2 != null) {
                                mPhotoBitMap2 = BitmapHelper.shrinkBitmap(mPhotoBitMap2, 500,	0);
                            }
                            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                            mPhotoBitMap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                            byte[] image2 = stream2.toByteArray();
                            final String img_str2 = Base64.encodeToString(image2, 0);

                            new FeedAsynTaskSetVehicleImage().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPLOAD_IMAGE_CAR", img_str2, IMEI, "3");

                        }
                        break;
                    case 4:
                        if (!_right.equals("0")) {

                            // image
                            Uri imgUri3 = Uri.parse("file://" + _right);
                            Bitmap mPhotoBitMap3 = BitmapHelper.readBitmap(this, imgUri3);
                            if (mPhotoBitMap3 != null) {
                                mPhotoBitMap3 = BitmapHelper.shrinkBitmap(mPhotoBitMap3, 500,	0);
                            }
                            ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                            mPhotoBitMap3.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                            byte[] image3 = stream3.toByteArray();
                            final String img_str3 = Base64.encodeToString(image3, 0);

                            new FeedAsynTaskSetVehicleImage().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPLOAD_IMAGE_CAR", img_str3, IMEI, "4");
                        }
                        break;
                }
            }
        }

        /*
        String imgStr1 = "";
        String imgStr2 = "";
        String imgStr3 = "";
        String imgStr4 = "";

        String[][] arrData = PicturePathDB.SelectAll();
        if (arrData != null) {
            String _font = arrData[0][1].toString();
            String _back = arrData[0][2].toString();
            String _left = arrData[0][3].toString();
            String _right = arrData[0][4].toString();

            if (!_font.equals("0")) {

                // image
                Uri imgUri = Uri.parse("file://" + _font);
                Bitmap mPhotoBitMap = BitmapHelper.readBitmap(this, imgUri);
                if (mPhotoBitMap != null) {
                    mPhotoBitMap = BitmapHelper.shrinkBitmap(mPhotoBitMap, 500,	0);
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mPhotoBitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] image = stream.toByteArray();
                final String img_str = Base64.encodeToString(image, 0);

                imgStr1 = img_str;

            }

            if (!_back.equals("0")) {

                // image
                Uri imgUri1 = Uri.parse("file://" + _back);
                Bitmap mPhotoBitMap1 = BitmapHelper.readBitmap(this, imgUri1);
                if (mPhotoBitMap1 != null) {
                    mPhotoBitMap1 = BitmapHelper.shrinkBitmap(mPhotoBitMap1, 500,	0);
                }
                ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                mPhotoBitMap1.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
                byte[] image1 = stream1.toByteArray();
                final String img_str1 = Base64.encodeToString(image1, 0);

                imgStr2 = img_str1;

            }

            if (!_left.equals("0")) {

                // image
                Uri imgUri2 = Uri.parse("file://" + _left);
                Bitmap mPhotoBitMap2 = BitmapHelper.readBitmap(this, imgUri2);
                if (mPhotoBitMap2 != null) {
                    mPhotoBitMap2 = BitmapHelper.shrinkBitmap(mPhotoBitMap2, 500,	0);
                }
                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                mPhotoBitMap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                byte[] image2 = stream2.toByteArray();
                final String img_str2 = Base64.encodeToString(image2, 0);

                imgStr3 = img_str2;

            }

            if (!_right.equals("0")) {

                // image
                Uri imgUri3 = Uri.parse("file://" + _right);
                Bitmap mPhotoBitMap3 = BitmapHelper.readBitmap(this, imgUri3);
                if (mPhotoBitMap3 != null) {
                    mPhotoBitMap3 = BitmapHelper.shrinkBitmap(mPhotoBitMap3, 500,	0);
                }
                ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                mPhotoBitMap3.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                byte[] image3 = stream3.toByteArray();
                final String img_str3 = Base64.encodeToString(image3, 0);

                imgStr4 = img_str3;

            }
        }
*/
        //new FeedAsynTaskSetVehicleImage().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPLOAD_IMAGE_CAR", "", "", "");

    }

    private void loadVehicleBrand() {

        new FeedAsynTaskGetVehicleBrand().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_BRAND", "LYd162fYt");

    }

    private void loadVehicleType() {

        new FeedAsynTaskGetVehicleType().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_TYPE", "LYd162fYt");

    }

    private void loadData() {

        String[][] arrData = VehicleDB.SelectAllByVehicleName(vehicleName);
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

            IMEI = _IMEI;
            SIM = _SIM;
            TEL_EMERGING_1 = _TEL_EMERGING_1;
            TEL_EMERGING_2 = _TEL_EMERGING_2;
            TEL_EMERGING_3 = _TEL_EMERGING_3;

            if (!_TEL_EMERGING_1.equals("null")) {
                txtNumber1.setText(_TEL_EMERGING_1);
            }

            if (!_TEL_EMERGING_2.equals("null")) {
                txtNumber2.setText(_TEL_EMERGING_2);
            }

            if (!_TEL_EMERGING_3.equals("null")) {
                txtNumber3.setText(_TEL_EMERGING_3);
            }

            Glide.with(this)
                    .load(_CAR_IMAGE_FRONT)
                    .into(imgCarFront);

            Glide.with(this)
                    .load(_CAR_IMAGE_BACK)
                    .into(imgCarLeft);

            Glide.with(this)
                    .load(_CAR_IMAGE_LEFT)
                    .into(imgCarRight);

            Glide.with(this)
                    .load(_CAR_IMAGE_RIGHT)
                    .into(imgCarBack);

            edtVehicleLicense.setText(_VEHICLE_NAME);
/*
            String compareValue = "some value";
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.select_state, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            if (!compareValue.equals(null)) {
                int spinnerPosition = adapter.getPosition(compareValue);
                mSpinner.setSelection(spinnerPosition);
            }
*/

            if (!_MODEL.equals("null")) {
                edtVehicleGeneration.setText(_MODEL);
            }

            if (!_VEHICLE_COLOR.equals("null")) {
                edtVehicleColor.setText(_VEHICLE_COLOR);
            }

            vehicleType = _VEHICLE_TYPE_NAME;
            vehicleBrand = _VEHICLE_BRAND_NAME;
            vehicleYear = _YEAR;

        }

    }

    private void setupWidgets() {

        imgCarFront = (ImageView) findViewById(R.id.imgCarFront);
        imgCarLeft = (ImageView) findViewById(R.id.imgCarLeft);
        imgCarRight = (ImageView) findViewById(R.id.imgCarRight);
        imgCarBack = (ImageView) findViewById(R.id.imgCarBack);
        imgTakePhotoFront = (ImageView) findViewById(R.id.imgTakePhotoFront);
        imgTakePhotoLeft = (ImageView) findViewById(R.id.imgTakePhotoLeft);
        imgTakePhotoRight = (ImageView) findViewById(R.id.imgTakePhotoRight);
        imgTakePhotoBack = (ImageView) findViewById(R.id.imgTakePhotoBack);
        layoutSetNumber1 = (RelativeLayout) findViewById(R.id.layoutSetNumber1);
        layoutSetNumber2 = (RelativeLayout) findViewById(R.id.layoutSetNumber2);
        layoutSetNumber3 = (RelativeLayout) findViewById(R.id.layoutSetNumber3);
        txtNumber1 = (TextView) findViewById(R.id.txtNumber1);
        txtNumber2 = (TextView) findViewById(R.id.txtNumber2);
        txtNumber3 = (TextView) findViewById(R.id.txtNumber3);
        edtVehicleLicense = (EditText) findViewById(R.id.edtVehicleLicense);
        spnVehicleType = (Spinner) findViewById(R.id.spnVehicleType);
        spnVehicleBrand = (Spinner) findViewById(R.id.spnVehicleBrand);
        edtVehicleGeneration = (EditText) findViewById(R.id.edtVehicleGeneration);
        spnVehicleYear = (Spinner) findViewById(R.id.spnVehicleYear);
        edtVehicleColor = (EditText) findViewById(R.id.edtVehicleColor);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            imgTakePhotoFront.setEnabled(false);
            imgTakePhotoLeft.setEnabled(false);
            imgTakePhotoRight.setEnabled(false);
            imgTakePhotoBack.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        imgTakePhotoFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("VehicleSettingActivity", "กดปุ่ม ถ่ายรูป Car Front", USER_ID);

                photoSelect = "Front";
                dialogAlertSelectPhoto();

            }
        });

        imgTakePhotoLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("VehicleSettingActivity", "กดปุ่ม ถ่ายรูป Car Left", USER_ID);

                photoSelect = "Left";
                dialogAlertSelectPhoto();

            }
        });

        imgTakePhotoRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("VehicleSettingActivity", "กดปุ่ม ถ่ายรูป Car Right", USER_ID);

                photoSelect = "Right";
                dialogAlertSelectPhoto();

            }
        });

        imgTakePhotoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("VehicleSettingActivity", "กดปุ่ม ถ่ายรูป Car Back", USER_ID);

                photoSelect = "Back";
                dialogAlertSelectPhoto();

            }
        });

        layoutSetNumber1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appLog.setLog("VehicleSettingActivity", "กดปุ่ม Set Number 1", USER_ID);

                dialogAlertEmergencyNumbers(SIM, TEL_EMERGING_1, "1");
            }
        });

        layoutSetNumber2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appLog.setLog("VehicleSettingActivity", "กดปุ่ม Set Number 2", USER_ID);

                dialogAlertEmergencyNumbers(SIM, TEL_EMERGING_2, "2");
            }
        });

        layoutSetNumber3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appLog.setLog("VehicleSettingActivity", "กดปุ่ม Set Number 3", USER_ID);

                dialogAlertEmergencyNumbers(SIM, TEL_EMERGING_3, "3");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:

                appLog.setLog("VehicleSettingActivity", "กดปุ่ม Save", USER_ID);

                uploadData();

                break;
            case android.R.id.home:
                Intent homeIntent = new Intent(this, NaviDrawerActivity.class);
                //Intent homeIntent = new Intent(this, MainAntifActivity.class);
                startActivity(homeIntent);
                overridePendingTransition(R.anim.slide_left_to_right, R.anim.no_change);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void dialogAlertEmergencyNumbers(final String _SIM, final String _TEL_EMERGING, final String emerNo) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(VehicleSettingActivity.this);
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View Viewlayout = inflater.inflate(R.layout.dialog_emergency_tel_change, (ViewGroup) findViewById(R.id.layout_dialog));

        final EditText txtEmergencyNumbers = (EditText) Viewlayout.findViewById(R.id.txtEmergencyNumbers);


        CheckDialog.setTitle("เปลี่ยนเบอร์ฉุกเฉิน(" + _TEL_EMERGING + ")");
        CheckDialog.setIcon(R.drawable.ic_action_error);
        CheckDialog.setView(Viewlayout);

        // Button OK
        CheckDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String emergencyNumbers = txtEmergencyNumbers.getText().toString();
                //Toast.makeText(getApplicationContext(), emergencyNumbers, Toast.LENGTH_LONG).show();
                //dialogAlertSendSMSEmergencyTel(_SIM, _TEL_EMERGING, emergencyNumbers, emerNo);

                /*if (isOnline()) {
                    appLog.setLog("VehicleSettingActivity", "เปลี่ยนเบอร์ฉุกเฉิน(" + _TEL_EMERGING + ") เป็น " + emergencyNumbers, USER_ID);

                    new FeedAsynTaskChangeNumber().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_TEL_EMERGING", "LYd162fYt", IMEI, emergencyNumbers, emerNo, USER_ID);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }*/

                switch (emerNo) {
                    case "1":
                        TEL_EMERGING_1 = emergencyNumbers;
                        txtNumber1.setText(TEL_EMERGING_1);
                        break;
                    case "2":
                        TEL_EMERGING_2 = emergencyNumbers;
                        txtNumber2.setText(TEL_EMERGING_2);
                        break;
                    case "3":
                        TEL_EMERGING_3 = emergencyNumbers;
                        txtNumber3.setText(TEL_EMERGING_3);
                        break;
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

    public void dialogAlertChangeNumberSuccess(final String _emergencyNumber, final String _number) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleSettingActivity.this);

        completeDialog.setTitle("คุณได้เปลี่ยนเบอร์ฉุกเฉินที่" + _number + " เป็นเบอร์ " + _emergencyNumber);
        //completeDialog.setMessage("คุณต้องการ " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME + " ใช่หรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_accept);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (isOnline()) {
                    loadData();
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        completeDialog.show();
    }

    public void dialogAlertChangeNumberFail(String _imei, final String _emergencyNumber, final String _number) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleSettingActivity.this);

        completeDialog.setTitle("เปลี่ยนเบอร์ไม่สำเร็จ!");
        completeDialog.setMessage("คุณต้องการเปลี่ยนเบอร์ที่ " + _number + "เป็นเบอร์ (" + _emergencyNumber + ") อีกครั้งหรือไม่");
        completeDialog.setIcon(R.drawable.ic_action_cancel);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    new FeedAsynTaskChangeNumber().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_TEL_EMERGING", "LYd162fYt", IMEI, _emergencyNumber, _number, "2");
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

    public void dialogAlertVehicleNotUpdate() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleSettingActivity.this);

        completeDialog.setTitle("รถของคุณไม่อัพเดท!!");
        completeDialog.setMessage("กรุณาลองใหม่อีกครั้ง");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (isOnline()) {
                    loadData();
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        completeDialog.show();
    }

    public class FeedAsynTaskGetVehicleType extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(VehicleSettingActivity.this);
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
                                ArrayAdapter<String> arrAd = new ArrayAdapter<String>(VehicleSettingActivity.this, android.R.layout.simple_spinner_item, arrSpinner);
                                arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnVehicleType.setAdapter(arrAd);

                                if (!vehicleType.equals(null)) {
                                    int spinnerPosition = arrAd.getPosition(vehicleType);
                                    spnVehicleType.setSelection(spinnerPosition);
                                }
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
            nDialog = new ProgressDialog(VehicleSettingActivity.this);
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
                                ArrayAdapter<String> arrAd = new ArrayAdapter<String>(VehicleSettingActivity.this, android.R.layout.simple_spinner_item, arrSpinner);
                                arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnVehicleBrand.setAdapter(arrAd);

                                if (!vehicleBrand.equals(null)) {
                                    int spinnerPosition = arrAd.getPosition(vehicleBrand);
                                    spnVehicleBrand.setSelection(spinnerPosition);
                                }
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

    public class FeedAsynTaskChangeNumber extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;
        private String _IMEI;
        private String _TEL_EMERGING;
        private String _NUMBER;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(VehicleSettingActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(false);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                _IMEI = params[2];
                _TEL_EMERGING = params[3];
                _NUMBER = params[4];

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
                        .add("TEL_EMERGING", params[3])
                        .add("NUMBER", params[4])
                        .add("UPDATE_BY", params[5])
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

                feedDataListChengeNumber = CuteFeedJsonUtil.feed(s);
                if (feedDataListChengeNumber != null) {
                    for (int i = 0; i <= feedDataListChengeNumber.size(); i++) {
                        try {

                            String STATUS = String.valueOf(feedDataListChengeNumber.get(i).getString("STATUS"));

                            if (STATUS.contains("Success")) {
                                appLog.setLog("VehicleSettingActivity", "เปลี่ยนเบอร์ฉุกเฉิน(" + _NUMBER + ") เป็น " + _TEL_EMERGING + " Success", USER_ID);

                                dialogAlertChangeNumberSuccess(_TEL_EMERGING, _NUMBER);
                            } else if (STATUS.contains("NotUp")) {
                                appLog.setLog("VehicleSettingActivity", "เปลี่ยนเบอร์ฉุกเฉิน(" + _NUMBER + ") เป็น " + _TEL_EMERGING + " รถไม่อัพเดท", USER_ID);

                                dialogAlertVehicleNotUpdate();
                            } else {
                                appLog.setLog("VehicleSettingActivity", "เปลี่ยนเบอร์ฉุกเฉิน(" + _NUMBER + ") เป็น " + _TEL_EMERGING + " Fail", USER_ID);

                                dialogAlertChangeNumberFail(_IMEI, _TEL_EMERGING, _NUMBER);
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

    public void dialogAlertSelectPhoto() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleSettingActivity.this);

        completeDialog.setTitle("Choose photo from?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(/*android.R.string.yes*/"Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("VehicleSettingActivity", "กดปุ่ม Choose photo from Camera", USER_ID);

                dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);

            }
        }).setNegativeButton(/*android.R.string.no*/"Gallery", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("VehicleSettingActivity", "กดปุ่ม Choose photo from Gallery", USER_ID);

                startGalleryIntent();

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch(actionCode) {
            case ACTION_TAKE_PHOTO_B:
                File f = null;

                try {
                    /*f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));*/

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                        // Do something for lollipop and above versions

                        //Uri photoURI = Uri.fromFile( f);
                        Uri photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", createImageFile());
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    } else{
                        // do something for phones running an SDK before lollipop

                        f = setUpPhotoFile();
                        mCurrentPhotoPath = f.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name_add_car);
    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            galleryAddPic();
            mCurrentPhotoPath = null;
        }

    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            //Toast.makeText(getApplicationContext(), contentUri.toString(), Toast.LENGTH_SHORT).show();
            contentUri = Uri.parse(String.valueOf(contentUri).replace("external_files", "storage/emulated/0"));
            //Toast.makeText(getApplicationContext(), contentUri.toString(), Toast.LENGTH_SHORT).show();
        }

        // get file name
        String path = f.getAbsolutePath();
        String filename = path.substring(path.lastIndexOf("/") + 1);

        //Log.d("URI", String.valueOf(contentUri));
        switch (photoSelect) {
            case "Front":

                appLog.setLog("VehicleSettingActivity", "ถ่ายรูป Car Front " + filename, USER_ID);

                Glide.with(this)
                        .load(new File(contentUri.getPath()))
                        .into(imgCarFront);

                PicturePathDB.UpdateDataFront(contentUri.getPath(), "0");

                break;
            case "Back":

                appLog.setLog("VehicleSettingActivity", "ถ่ายรูป Car Back " + filename, USER_ID);

                Glide.with(this)
                        .load(new File(contentUri.getPath()))
                        .into(imgCarBack);

                PicturePathDB.UpdateDataBack(contentUri.getPath(), "0");

                break;
            case "Left":

                appLog.setLog("VehicleSettingActivity", "ถ่ายรูป Car Left " + filename, USER_ID);

                Glide.with(this)
                        .load(new File(contentUri.getPath()))
                        .into(imgCarLeft);

                PicturePathDB.UpdateDataLeft(contentUri.getPath(), "0");

                break;
            case "Right":

                appLog.setLog("VehicleSettingActivity", "ถ่ายรูป Car Right " + filename, USER_ID);

                Glide.with(this)
                        .load(new File(contentUri.getPath()))
                        .into(imgCarRight);

                PicturePathDB.UpdateDataRight(contentUri.getPath(), "0");

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            } // ACTION_TAKE_PHOTO_B

            case GALLERY_IMAGE_ACTIVITY_REQUEST_CODE: {
                try {
                    photoUri = data.getData();
                    String selectedImagePath = getImageFilePath(photoUri, this);
                    Log.d(getClass().getName(), selectedImagePath);

                    // get file name
                    //String path = f.getAbsolutePath();
                    String filename = selectedImagePath.substring(selectedImagePath.lastIndexOf("/") + 1);

                    switch (photoSelect) {
                        case "Front":

                            appLog.setLog("VehicleSettingActivity", "เลือกรูปจาก Gallery Car Front " + filename, USER_ID);

                            Glide.with(this)
                                    .load(new File(selectedImagePath))
                                    .into(imgCarFront);

                            PicturePathDB.UpdateDataFront(selectedImagePath, "0");

                            break;
                        case "Back":

                            appLog.setLog("VehicleSettingActivity", "เลือกรูปจาก Gallery Car Back " + filename, USER_ID);

                            Glide.with(this)
                                    .load(new File(selectedImagePath))
                                    .into(imgCarBack);

                            PicturePathDB.UpdateDataBack(selectedImagePath, "0");

                            break;
                        case "Left":

                            appLog.setLog("VehicleSettingActivity", "เลือกรูปจาก Gallery Car Left " + filename, USER_ID);

                            Glide.with(this)
                                    .load(new File(selectedImagePath))
                                    .into(imgCarLeft);

                            PicturePathDB.UpdateDataLeft(selectedImagePath, "0");

                            break;
                        case "Right":

                            appLog.setLog("VehicleSettingActivity", "เลือกรูปจาก Gallery Car Right " + filename, USER_ID);

                            Glide.with(this)
                                    .load(new File(selectedImagePath))
                                    .into(imgCarRight);

                            PicturePathDB.UpdateDataRight(selectedImagePath, "0");

                            break;
                    }

                } catch (Exception e) {

                }
                break;

            }   // Gallery

        } // switch
    }

    // Gallery
    private void startGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public static String getImageFilePath(Uri originalUri, Activity activity) {
        // get file path in string
        String selectedImagePath = null;
        String[] projection = { MediaStore.Images.ImageColumns.DATA };
        Cursor cursor = activity.managedQuery(originalUri, projection, null,
                null, null);
        if (cursor != null) {
            int index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            selectedImagePath = cursor.getString(index);
            if (selectedImagePath == null) {

                String id = originalUri.getLastPathSegment().split(":")[1];
                final String[] imageColumns = { MediaStore.Images.Media.DATA };
                final String imageOrderBy = null;

                Uri uri = getUri();

                Cursor imageCursor = activity.managedQuery(uri, imageColumns,
                        MediaStore.Images.Media._ID + "=" + id, null,
                        imageOrderBy);

                if (imageCursor.moveToFirst()) {
                    selectedImagePath = imageCursor.getString(imageCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                }
                Log.e("path", selectedImagePath); // use selectedImagePath
            }
        }
        return selectedImagePath;
    }

    // By using this method get the Uri of Internal/External Storage for Media
    private static Uri getUri() {
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                imgTakePhotoFront.setEnabled(true);
                imgTakePhotoLeft.setEnabled(true);
                imgTakePhotoRight.setEnabled(true);
                imgTakePhotoBack.setEnabled(true);
            }
        }
    }


    public void dialogAlertAddCarSuccess(){
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleSettingActivity.this);

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
        AlertDialog.Builder mDialog = new AlertDialog.Builder(VehicleSettingActivity.this);

        mDialog.setTitle("Add car fail!!");
        mDialog.setMessage("Do you want to try add vehicle agian?");
        mDialog.setIcon(R.drawable.ic_action_close);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                uploadData();

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
            nDialog = new ProgressDialog(VehicleSettingActivity.this);
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
                        .add("VEHICLE_COLOR", params[6])
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

                                //dialogAlertAddCarSuccess();
                                //uploadImage();

                                String[][] arrData = PicturePathDB.SelectAll();
                                if (arrData != null) {
                                    String _font = arrData[0][1].toString();
                                    String _back = arrData[0][2].toString();
                                    String _left = arrData[0][3].toString();
                                    String _right = arrData[0][4].toString();

                                    if (!_font.equals("0") || !_back.equals("0") || !_left.equals("0") || !_right.equals("0")) {
                                        uploadImage();
                                    } else {
                                        dialogAlertAddCarSuccess();
                                    }
                                }

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

    public class FeedAsynTaskSetVehicleImage extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(VehicleSettingActivity.this);
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
                        .add("Base64_IMAGE", params[1])
                        .add("IMEI", params[2])
                        .add("NUMBER", params[3])
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

                if (s.contains("Success")) {
                    appLog.setLog("AddCarDetailActivity", "Add Car Image Success", USER_ID);

                    Toast.makeText(getApplicationContext(), "Add Car Image Success", Toast.LENGTH_LONG).show();
                } else {
                    appLog.setLog("AddCarDetailActivity", "Add Car Image Fail", USER_ID);

                    Toast.makeText(getApplicationContext(), "Add Car Image Fail", Toast.LENGTH_LONG).show();
                }

                feedDataListSetVehicleImage = CuteFeedJsonUtil.feed(s);
                if (feedDataListSetVehicleImage != null) {
                    for (int i = 0; i <= feedDataListSetVehicleImage.size(); i++) {
                        try {

                            String strSTATUS = String.valueOf(feedDataListSetVehicleImage.get(i).getString("STATUS"));

                            if (strSTATUS.equals("Success")) {
                                appLog.setLog("AddCarDetailActivity", "Add Car Image Success", USER_ID);

                                Toast.makeText(getApplicationContext(), "Add Car Image Success", Toast.LENGTH_LONG).show();
                            } else {
                                appLog.setLog("AddCarDetailActivity", "Add Car Image Fail", USER_ID);

                                Toast.makeText(getApplicationContext(), "Add Car Image Fail", Toast.LENGTH_LONG).show();
                            }


                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

                uploadImageNo++;
                if (uploadImageNo == 4) {
                    appLog.setLog("AddCarDetailActivity", "Add Car Success", USER_ID);

                    dialogAlertAddCarSuccess();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();

                appLog.setLog("AddCarDetailActivity", "Add Car Image Fail", USER_ID);
                dialogAlertAddCarFail();
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
