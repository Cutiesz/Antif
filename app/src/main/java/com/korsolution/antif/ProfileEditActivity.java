package com.korsolution.antif;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLngBounds;

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

public class ProfileEditActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private ImageView imgEdit;
    private EditText edtEmail;
    private EditText edtFirstName;
    private EditText edtLastName;
    private EditText edtDisplayName;
    private EditText edtTel;
    private EditText edtIdCard;
    private EditText edtLicenseEXP;
    private EditText edtAddress;
    private Spinner spnProvince;
    private Spinner spnAmphur;
    private Spinner spnDistrict;
    private EditText edtPostcode;
    private TextView txtEditProfileFail;
    private Button btnEditProfile;

    protected ArrayList<JSONObject> feedDataList;
    protected ArrayList<JSONObject> feedDataListImage;
    protected ArrayList<JSONObject> feedDataListProvince;
    protected ArrayList<JSONObject> feedDataListAmphur;
    protected ArrayList<JSONObject> feedDataListDistrict;

    private ProvinceDBClass ProvinceDB;
    private AmphoeDBClass AmphoeDB;
    private DistrictDBClass DistrictDB;
    private AccountDBClass AccountDB;

    private String provinceName = "Province";
    private String amphoeName = "Amphoe";
    private String districtName = "District";

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private String latAddress;
    private String lngAddress;

    private static final int ACTION_TAKE_PHOTO_B = 1;
    private String mCurrentPhotoPath;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private final static int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    protected static Uri photoUri = null;

    private String pathProfileImage;

    private String USER_ID;

    private AppLogClass appLog;
/*
    private static final int DIALOG_DATE = 1;
    private Calendar dateTime = Calendar.getInstance();
    //private SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd yyyy");
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
*/
/*
    public Calendar mCalendar;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "kk:mm";
    DialogFragment dateFragment;
    DialogFragment timeFragment;
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        USER_ID = getIntent().getStringExtra("USER_ID");

        ProvinceDB = new ProvinceDBClass(this);
        AmphoeDB = new AmphoeDBClass(this);
        DistrictDB = new DistrictDBClass(this);
        AccountDB = new AccountDBClass(this);

        appLog = new AppLogClass(this);

        setupWidgets();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        loadProfileData();

        //mCalendar = Calendar.getInstance();
    }

    private void loadSpinnerDistrict() {

        // spinner Car Brand
        String[] arrSpinner = new String[1];
        //arrSpinner[0] = "District";
        arrSpinner[0] = districtName;
        // Set List Spinner
        ArrayAdapter<String> arrAd = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrSpinner);
        arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDistrict.setAdapter(arrAd);

    }

    private void loadSpinnerAmphur() {

        // spinner Car Brand
        String[] arrSpinner = new String[1];
        //arrSpinner[0] = "Amphur";
        arrSpinner[0] = amphoeName;
        // Set List Spinner
        ArrayAdapter<String> arrAd = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrSpinner);
        arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAmphur.setAdapter(arrAd);

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

            edtEmail.setText(EMAIL);

            if (!FIRST_NAME.equals("null")) {
                edtFirstName.setText(FIRST_NAME);
            }
            if (!LAST_NAME.equals("null")) {
                edtLastName.setText(LAST_NAME);
            }
            if (!DISPLAY_NAME.equals("null")) {
                edtDisplayName.setText(DISPLAY_NAME);
            }
            if (!TEL.equals("null")) {
                edtTel.setText(TEL);
            }
            if (!ID_CARD.equals("null")) {
                edtIdCard.setText(ID_CARD);
            }
            if (!LICENSE_EXP.equals("null")) {
                // Cut String Date Time
                String[] separated = LICENSE_EXP.split("-");
                String[] day = separated[2].split("T");
                //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
                String dateTime = separated[0] + "-" + separated[1] + "-" + day[0];

                edtLicenseEXP.setText(dateTime);
            }

            if (!ADDRESS.equals("null")) {
                edtAddress.setText(ADDRESS);
            }
            if (!Province_Name.equals("null")) {
                provinceName = Province_Name;
            }
            if (!Amphur_Name.equals("null")) {
                amphoeName = Amphur_Name;
            }
            if (!District_Name.equals("null")) {
                districtName = District_Name;
            }
            if (!POSTCODE.equals("null")) {
                edtPostcode.setText(POSTCODE);
            }

            latAddress = LATITUDE_ADDRESS;
            lngAddress = LONGITUDE_ADDRESS;

            // set Profile Image
            Glide.with(this)
                    .load(USER_PICTURE)
                    .error(R.drawable.blank_person_photo)
                    .into(imgProfile);

            appLog.setLog("ProfileActivity", "Load Profile Data", USER_ID);
        }

        // get Province
        new FeedAsynTaskGetProvince().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_PROVINCE", "LYd162fYt");
        loadSpinnerAmphur();
        loadSpinnerDistrict();
    }

    private void uploadProfileData() {

        String _email = edtEmail.getText().toString();
        String _firstName = edtFirstName.getText().toString();
        String _lastName = edtLastName.getText().toString();
        String _displayName = edtDisplayName.getText().toString();
        String _tel = edtTel.getText().toString();
        String _idCard = edtIdCard.getText().toString();
        String _licenseEXP = edtLicenseEXP.getText().toString();

        String _address = edtAddress.getText().toString();
        String _province = spnProvince.getSelectedItem().toString();
        String _amphur = spnAmphur.getSelectedItem().toString();
        String _district = spnDistrict.getSelectedItem().toString();
        String _postcode = edtPostcode.getText().toString();

        String modelName = "Android";
        modelName = getDeviceName();

        String filename = "";
        try {

            // get file name
            //String path = f.getAbsolutePath();
            filename = pathProfileImage.substring(pathProfileImage.lastIndexOf("/") + 1);

        } catch (Exception e) {

        }

        gpsTracker = new GPSTracker(ProfileEditActivity.this);
        if (gpsTracker.canGetLocation()) {

            mylat = gpsTracker.getLatitude();
            mylng = gpsTracker.getLongitude();

            String _lat = String.valueOf(mylat);
            String _lng = String.valueOf(mylng);

            //new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPDATE_USER", "LYd162fYt",
            //        _displayName, _firstName, _lastName, _tel, _idCard, _licenseEXP, modelName, _lat, _lng, filename,
            //        _address, _postcode, latAddress, lngAddress, _district, _amphur, _province, USER_ID);

            if (_firstName.length() > 0) {
                if (_lastName.length() > 0) {
                    if (_displayName.length() > 0) {
                        if (_tel.length() > 0) {
                            /*if (_address.length() > 0) {
                                if (!_province.equals("Province")) {
                                    if (!_amphur.equals("Amphoe")) {
                                        if (!_district.equals("District")) {
                                            if (_postcode.length() > 0) {*/

                                                new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPDATE_USER", "LYd162fYt",
                                                        _displayName, _firstName, _lastName, _tel, _idCard, _licenseEXP, modelName, _lat, _lng, filename,
                                                        _address, _postcode, latAddress, lngAddress, _district, _amphur, _province, USER_ID);

                                            /*} else {
                                                txtEditProfileFail.setVisibility(View.VISIBLE);
                                                txtEditProfileFail.setText("Please Enter Postcode.");
                                            }
                                        } else {
                                            txtEditProfileFail.setVisibility(View.VISIBLE);
                                            txtEditProfileFail.setText("Please Select District.");
                                        }
                                    } else {
                                        txtEditProfileFail.setVisibility(View.VISIBLE);
                                        txtEditProfileFail.setText("Please Select Amphoe.");
                                    }
                                } else {
                                    txtEditProfileFail.setVisibility(View.VISIBLE);
                                    txtEditProfileFail.setText("Please Select Province.");
                                }
                            } else {
                                txtEditProfileFail.setVisibility(View.VISIBLE);
                                txtEditProfileFail.setText("Please Enter Address.");
                            }*/
                        } else {
                            txtEditProfileFail.setVisibility(View.VISIBLE);
                            txtEditProfileFail.setText("Please Enter Telephone Number.");
                        }
                    } else {
                        txtEditProfileFail.setVisibility(View.VISIBLE);
                        txtEditProfileFail.setText("Please Enter Display Name.");
                    }
                } else {
                    txtEditProfileFail.setVisibility(View.VISIBLE);
                    txtEditProfileFail.setText("Please Enter Last Name.");
                }
            } else {
                txtEditProfileFail.setVisibility(View.VISIBLE);
                txtEditProfileFail.setText("Please Enter First Name.");
            }

        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    private void uploadProfileImg() {

        File f = new File(pathProfileImage);
        if (f.exists()) {
            try {
                // image
                Uri imgUri = Uri.parse("file://" + pathProfileImage);
                Bitmap mPhotoBitMap = BitmapHelper.readBitmap(ProfileEditActivity.this, imgUri);
                if (mPhotoBitMap != null) {
                    mPhotoBitMap = BitmapHelper.shrinkBitmap(mPhotoBitMap, 500,	0);
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mPhotoBitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] image = stream.toByteArray();
                final String img_str = Base64.encodeToString(image, 0);

                new FeedAsynTaskImageProfile().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPLOAD_IMAGE_USER", img_str, USER_ID);

            } catch (Exception e) {

            }
        } else {
            dialogAlertEditProfileSuccess();
        }
    }

    private void setupWidgets() {

        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        imgEdit = (ImageView) findViewById(R.id.imgEdit);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtFirstName = (EditText) findViewById(R.id.edtFirstName);
        edtLastName = (EditText) findViewById(R.id.edtLastName);
        edtDisplayName = (EditText) findViewById(R.id.edtDisplayName);
        edtTel = (EditText)findViewById(R.id.edtTel);
        edtIdCard = (EditText) findViewById(R.id.edtIdCard);
        edtLicenseEXP = (EditText) findViewById(R.id.edtLicenseEXP);
        edtAddress = (EditText) findViewById(R.id.edtAddress);
        spnProvince = (Spinner) findViewById(R.id.spnProvince);
        spnAmphur = (Spinner) findViewById(R.id.spnAmphur);
        spnDistrict = (Spinner) findViewById(R.id.spnDistrict);
        edtPostcode = (EditText) findViewById(R.id.edtPostcode);
        txtEditProfileFail = (TextView) findViewById(R.id.txtEditProfileFail);
        btnEditProfile = (Button) findViewById(R.id.btnEditProfile);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            imgEdit.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        spnProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String _provinceName = spnProvince.getSelectedItem().toString();

                new FeedAsynTaskGetAmphur().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_AMPHUR", "LYd162fYt", _provinceName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnAmphur.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String _amphoeName = spnAmphur.getSelectedItem().toString();

                new FeedAsynTaskGetDistrict().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_DISTRICT", "LYd162fYt", _amphoeName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edtLicenseEXP.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (edtLicenseEXP.hasFocus()) {
                    //showDialog(DIALOG_DATE);

                    DialogFragment newFragment = new SelectDateFragment();
                    newFragment.show(getFragmentManager(), "DatePicker");
                }

            }
        });

        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("ProfileEditActivity", "กดปุ่ม Edit Profile Image", USER_ID);

                dialogAlertSelectPhoto();
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline()) {
                    appLog.setLog("ProfileEditActivity", "กดปุ่ม Edit Profile", USER_ID);

                    uploadProfileData();
                }
            }
        });
    }

    public void dialogAlertSelectPhoto() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(ProfileEditActivity.this);

        completeDialog.setTitle("Choose photo from?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(/*android.R.string.yes*/"Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("ProfileEditActivity", "กดปุ่ม Choose photo from Camera", USER_ID);

                dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);

            }
        }).setNegativeButton(/*android.R.string.no*/"Gallery", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("ProfileEditActivity", "กดปุ่ม Choose photo from Gallery", USER_ID);

                startGalleryIntent();

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertEditProfileSuccess(){
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(ProfileEditActivity.this);

        completeDialog.setTitle("Edit Profile Successfully.");
        completeDialog.setIcon(R.drawable.ic_action_accept);
        completeDialog.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                Intent intent = new Intent(getApplicationContext(), NaviDrawerActivity.class);
                //Intent intent = new Intent(getApplicationContext(), MainAntifActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);

            }
        });
        completeDialog.show();
    }

    public void dialogAlertAddCarFail(){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(ProfileEditActivity.this);

        mDialog.setTitle("Edit Profile Fail!!");
        mDialog.setMessage("Do you want to try edit profile agian?");
        mDialog.setIcon(R.drawable.ic_action_close);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    appLog.setLog("ProfileEditActivity", "กดปุ่ม Edit Profile agian", USER_ID);

                    uploadProfileData();
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

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(ProfileEditActivity.this);
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
                        .add("DISPLAY_NAME", params[2])
                        .add("FIRST_NAME", params[3])
                        .add("LAST_NAME", params[4])
                        .add("TEL", params[5])
                        .add("ID_CARD", params[6])
                        .add("LICENSE_EXP", params[7])
                        .add("USE_DEVICE", params[8])
                        .add("LATITUDE", params[9])
                        .add("LONGITUDE", params[10])
                        .add("USER_PICTURE", params[11])
                        .add("ADDRESS", params[12])
                        .add("POSTCODE", params[13])
                        .add("LATITUDE_ADDRESS", params[14])
                        .add("LONGITUDE_ADDRESS", params[15])
                        .add("District_Name", params[16])
                        .add("Amphur_Name", params[17])
                        .add("Province_Name", params[18])
                        .add("USER_ID", params[19])
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

                            String STATUS = String.valueOf(feedDataList.get(i).getString("STATUS"));

                            /*if (STATUS.equals("Success")) {

                                appLog.setLog("ProfileEditActivity", "Upload Profile Data Success", USER_ID);

                                uploadProfileImg();

                            } else {
                                appLog.setLog("ProfileEditActivity", "Upload Profile Image Fail", USER_ID);

                                dialogAlertAddCarFail();
                            }*/

                            appLog.setLog("ProfileEditActivity", "Upload Profile Data Success", USER_ID);
                            uploadProfileImg();

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

    public class FeedAsynTaskImageProfile extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(ProfileEditActivity.this);
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

                if (s.toLowerCase().contains("Success".toLowerCase())) {
                    appLog.setLog("ProfileEditActivity", "Upload Profile Image Success", USER_ID);
                    dialogAlertEditProfileSuccess();
                } else {
                    appLog.setLog("ProfileEditActivity", "Upload Profile Image Fail", USER_ID);
                    dialogAlertAddCarFail();
                }

                /*feedDataListImage = CuteFeedJsonUtil.feed(s);
                if (feedDataListImage != null) {

                    for (int i = 0; i <= feedDataListImage.size(); i++) {
                        try {

                            String STATUS = String.valueOf(feedDataListImage.get(i).getString("STATUS"));
                            if (STATUS.equals("Success")) {
                                appLog.setLog("ProfileEditActivity", "Upload Profile Image Success", USER_ID);

                                dialogAlertEditProfileSuccess();
                            } else {
                                appLog.setLog("ProfileEditActivity", "Upload Profile Image Fail", USER_ID);

                                dialogAlertAddCarFail();
                            }

                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }*/

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }

            nDialog.dismiss();
        }
    }

    public class FeedAsynTaskGetProvince extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(ProfileEditActivity.this);
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

                ProvinceDB.Delete();

                feedDataListProvince = CuteFeedJsonUtil.feed(s);
                if (feedDataListProvince != null) {
                    for (int i = 0; i <= feedDataListProvince.size(); i++) {
                        try {

                            String Province_ID = String.valueOf(feedDataListProvince.get(i).getString("Province_ID"));
                            String Province_Name = String.valueOf(feedDataListProvince.get(i).getString("Province_Name"));

                            ProvinceDB.Insert(Province_ID, Province_Name);

                            String[] arrData = ProvinceDB.SelectName();
                            if (arrData != null) {

                                String[] arrSpinner;
                                arrSpinner = new String[arrData.length+1];

                                //arrSpinner[0] = "Select Province";
                                arrSpinner[0] = provinceName;

                                for (int j = 0; j < arrData.length; j++) {
                                    arrSpinner[j+1] = arrData[j].toString();
                                }

                                // Set List Spinner
                                ArrayAdapter<String> arrAd = new ArrayAdapter<String>(ProfileEditActivity.this, android.R.layout.simple_spinner_item, arrSpinner);
                                arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnProvince.setAdapter(arrAd);
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

    public class FeedAsynTaskGetAmphur extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(ProfileEditActivity.this);
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
                        .add("Province_Name", params[2])
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

                AmphoeDB.Delete();

                feedDataListAmphur = CuteFeedJsonUtil.feed(s);
                if (feedDataListAmphur != null) {
                    for (int i = 0; i <= feedDataListAmphur.size(); i++) {
                        try {

                            String Amphur_ID = String.valueOf(feedDataListAmphur.get(i).getString("Amphur_ID"));
                            String Amphur_Name = String.valueOf(feedDataListAmphur.get(i).getString("Amphur_Name"));

                            AmphoeDB.Insert(Amphur_ID, Amphur_Name);

                            String[] arrData = AmphoeDB.SelectName();
                            if (arrData != null) {

                                String[] arrSpinner;
                                arrSpinner = new String[arrData.length+1];

                                arrSpinner[0] = "Select Amphur";

                                for (int j = 0; j < arrData.length; j++) {
                                    arrSpinner[j+1] = arrData[j].toString();
                                }

                                // Set List Spinner
                                ArrayAdapter<String> arrAd = new ArrayAdapter<String>(ProfileEditActivity.this, android.R.layout.simple_spinner_item, arrSpinner);
                                arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnAmphur.setAdapter(arrAd);
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

    public class FeedAsynTaskGetDistrict extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(ProfileEditActivity.this);
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
                        .add("Amphur_Name", params[2])
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

                DistrictDB.Delete();

                feedDataListDistrict = CuteFeedJsonUtil.feed(s);
                if (feedDataListDistrict != null) {
                    for (int i = 0; i <= feedDataListDistrict.size(); i++) {
                        try {

                            String District_ID = String.valueOf(feedDataListDistrict.get(i).getString("District_ID"));
                            String District_Name = String.valueOf(feedDataListDistrict.get(i).getString("District_Name"));

                            DistrictDB.Insert(District_ID, District_Name);

                            String[] arrData = DistrictDB.SelectName();
                            if (arrData != null) {

                                String[] arrSpinner;
                                arrSpinner = new String[arrData.length+1];

                                arrSpinner[0] = "Select District";

                                for (int j = 0; j < arrData.length; j++) {
                                    arrSpinner[j+1] = arrData[j].toString();
                                }

                                // Set List Spinner
                                ArrayAdapter<String> arrAd = new ArrayAdapter<String>(ProfileEditActivity.this, android.R.layout.simple_spinner_item, arrSpinner);
                                arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnDistrict.setAdapter(arrAd);
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

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch(actionCode) {
            case ACTION_TAKE_PHOTO_B:
                File f = null;

                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
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
        return getString(R.string.album_name_profile);
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

        // get file name
        String path = f.getAbsolutePath();
        String filename = path.substring(path.lastIndexOf("/") + 1);

        appLog.setLog("ProfileEditActivity", "ถ่ายรูป Profile " + filename, USER_ID);

        pathProfileImage = contentUri.getPath();

        Glide.with(this)
                .load(new File(contentUri.getPath()))
                .into(imgProfile);
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

                    appLog.setLog("ProfileEditActivity", "เลือกรูป Profile จาก Gallery  " + filename, USER_ID);

                    pathProfileImage = selectedImagePath;

                    Glide.with(this)
                            .load(new File(selectedImagePath))
                            .into(imgProfile);

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
                imgEdit.setEnabled(true);
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

    @SuppressLint("validFragment")
    public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm+1, dd);
        }
        public void populateSetDate(int year, int month, int day) {
            //edtLicenseEXP.setText(month + "/" + day + "/" + year);
            edtLicenseEXP.setText(year + "-" + month + "-" + day);
        }

    }
/*
    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case DIALOG_DATE:
                return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
                {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth)
                    {
                        dateTime.set(year, monthOfYear, dayOfMonth);
                        //txtstdate.setText(dateFormatter.format(dateTime.getTime()));
                        edtLicenseEXP.setText(dateFormatter.format(dateTime.getTime()));
                    }
                }, dateTime.get(Calendar.YEAR),
                        dateTime.get(Calendar.MONTH),
                        dateTime.get(Calendar.DAY_OF_MONTH));

            case DIALOG_TIME:
                return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener()
                {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute)
                    {
                        dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        dateTime.set(Calendar.MINUTE, minute);
                        txtsttime.setText(timeFormatter.format(dateTime.getTime()));
                        txtAddtime.setText(timeFormatter.format(dateTime.getTime()));
                    }
                }, dateTime.get(Calendar.HOUR_OF_DAY),
                        dateTime.get(Calendar.MINUTE), false);

        }
        return null;
    }
*/
/*
    public void showDatePickerDialog(View v) {
        dateFragment = new DatePickerFragment();
        dateFragment.show(getFragmentManager(), "datePicker");

    }

    public void showTimePickerDialog(View v) {
        timeFragment = new TimePickerFragment();
        timeFragment.show(getFragmentManager(), "timePicker");
    }

    public void updateDateButtonText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateForButton = dateFormat.format(mCalendar.getTime());
        edtLicenseEXP.setText(dateForButton);
    }
    private void updateTimeButtonText() {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        String timeForButton = timeFormat.format(mCalendar.getTime());
        edtLicenseEXP.setText(timeForButton);
    }

    class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateButtonText();
        }
    }

    public class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendar.set(Calendar.MINUTE, minute);
            updateTimeButtonText();
        }
    }
*/
}
