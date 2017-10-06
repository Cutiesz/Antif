package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.paginate.Paginate;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ParkingPhotoActivity extends AppCompatActivity {

    private ImageView imgParking1;
    private ImageView imgParking2;
    private ImageView imgParking3;
    private ImageView imgParking4;
    private EditText edtParkingDetails;
    private Button btnReset;
    private Button btnTakePhoto;

    private static final int ACTION_TAKE_PHOTO_B = 1;
    private String mCurrentPhotoPath;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private String vehicleName;

    private VehicleDBClass VehicleDB;

    private String imei = "";

    protected ArrayList<JSONObject> feedDataList;

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_photo);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        USER_ID = getIntent().getStringExtra("USER_ID");
        vehicleName = getIntent().getStringExtra("vehicleName");

        VehicleDB = new VehicleDBClass(this);

        appLog = new AppLogClass(this);

        setupWidgets();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        edtParkingDetails.setText(Prefs.getString("ParkingDetails",""));
        loadPhotoParking();

        loadVehicleData();
    }

    private void uploadParking() {

        String IMAGE_A = "";
        String IMAGE_B = "";
        String IMAGE_C = "";
        String IMAGE_D = "";

        File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            for (int i = 0; i < files.length; i++) {

                // get file name
                String path = files[i].getAbsolutePath();
                String filename = path.substring(path.lastIndexOf("/") + 1);
                //Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_LONG).show();

                if (i == 0) {
                    IMAGE_A = filename;
                }
                if (i == 1) {
                    IMAGE_B = filename;
                }
                if (i == 2) {
                    IMAGE_C = filename;
                }
                if (i == 3) {
                    IMAGE_D = filename;
                }

            }
        }

        //Toast.makeText(getApplicationContext(), IMAGE_A + IMAGE_B + IMAGE_C + IMAGE_D, Toast.LENGTH_LONG).show();

        String parkingDetail = Prefs.getString("ParkingDetails","");
        String parkingLat = Prefs.getString("Parking_Lat","");
        String parkingLng = Prefs.getString("Parking_Lng","");

        new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_PARKING", "LYd162fYt", imei,
                IMAGE_A, IMAGE_B, IMAGE_C, IMAGE_D, parkingDetail, parkingLat, parkingLng, USER_ID);
    }

    private void renameImage() {
        File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                String dir = files[i].getParent();
                files[i].renameTo(new File(dir, imei + "_" + String.valueOf(i + 1) + ".jpg"));
            }
        }
    }

    private void loadVehicleData() {

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
            String _SIM = arrData[0][12].toString();
            String _TEL_EMERGING_1 = arrData[0][13].toString();
            String _TEL_EMERGING_2 = arrData[0][14].toString();
            String _TEL_EMERGING_3 = arrData[0][15].toString();
            String _IS_UNPLUG_GPS = arrData[0][16].toString();
            String _GSM_SIGNAL = arrData[0][17].toString();
            String _NUM_SAT = arrData[0][18].toString();

            imei = _IMEI;

            /*if (_IMEI.equals(Prefs.getString("vehicleIMEI",""))) {
                edtParkingDetails.setText(Prefs.getString("ParkingDetails",""));
                loadPhotoParking();

            } else {
                File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
                if (storageDir.exists()) {
                    dialogAlertDeleteAllImage();
                }
            }*/
        }

    }

    private void loadPhotoParking() {
        File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            for (int i = 0; i < files.length; i++) {

                if (i == 0) {
                    Glide.with(this)
                            .load(files[i])
                            .error(R.drawable.blank_img)
                            .into(imgParking1);
                }
                if (i == 1) {
                    Glide.with(this)
                            .load(files[i])
                            .into(imgParking2);
                }
                if (i == 2) {
                    Glide.with(this)
                            .load(files[i])
                            .into(imgParking3);
                }
                if (i == 3) {
                    Glide.with(this)
                            .load(files[i])
                            .into(imgParking4);
                }

            }

            appLog.setLog("ParkingPhotoActivity", "Load image Parking", USER_ID);
        }
    }

    public void dialogAlertDeleteAllImage(){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(ParkingPhotoActivity.this);

        mDialog.setTitle("Are you sure to clear all image?");
        mDialog.setIcon(R.drawable.ic_action_close);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("ParkingPhotoActivity", "กดปุ่ม Clear all image Parking", USER_ID);

                edtParkingDetails.setText("");

                File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
                if (storageDir.exists()) {
                    deleteDirectory(storageDir);

                    Glide.with(ParkingPhotoActivity.this)
                            .load(R.drawable.blank_img)
                            .into(imgParking1);
                    Glide.with(ParkingPhotoActivity.this)
                            .load(R.drawable.blank_img)
                            .into(imgParking2);
                    Glide.with(ParkingPhotoActivity.this)
                            .load(R.drawable.blank_img)
                            .into(imgParking3);
                    Glide.with(ParkingPhotoActivity.this)
                            .load(R.drawable.blank_img)
                            .into(imgParking4);
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

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, NaviDrawerActivity.class);
                //Intent homeIntent = new Intent(this, MainAntifActivity.class);
                startActivity(homeIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupWidgets() {
        imgParking1 = (ImageView) findViewById(R.id.imgParking1);
        imgParking2 = (ImageView) findViewById(R.id.imgParking2);
        imgParking3 = (ImageView) findViewById(R.id.imgParking3);
        imgParking4 = (ImageView) findViewById(R.id.imgParking4);
        edtParkingDetails = (EditText) findViewById(R.id.edtParkingDetails);
        btnReset = (Button) findViewById(R.id.btnReset);
        btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);

        edtParkingDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String parkingDetails = edtParkingDetails.getText().toString();
                Prefs.putString("ParkingDetails", parkingDetails);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            btnTakePhoto.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("ParkingPhotoActivity", "กดปุ่ม Reset", USER_ID);

                Prefs.putString("ParkingDetails", "");

                File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
                if (storageDir.exists()) {
                    dialogAlertDeleteAllImage();
                } else {
                    Toast.makeText(getApplicationContext(), "No Image!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                appLog.setLog("ParkingPhotoActivity", "กดปุ่มถ่ายรูป", USER_ID);

                //dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                Prefs.putString("vehicleIMEI", imei);

                gpsTracker = new GPSTracker(ParkingPhotoActivity.this);
                if (gpsTracker.canGetLocation()) {

                    mylat = gpsTracker.getLatitude();
                    mylng = gpsTracker.getLongitude();

                    File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
                    if (storageDir.exists()) {
                        File[] files = storageDir.listFiles();
                        if (files.length >= 4) {
                            //Toast.makeText(getApplicationContext(), "Cannot take a photo more!!", Toast.LENGTH_LONG).show();
                            dialogAlertDeleteAllImage();
                        } else {
                            dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                        }
                    } else {
                        dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                    }

                } else {
                    gpsTracker.showSettingsAlert();
                }
            }
        });
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
        return getString(R.string.album_name);
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

        //Log.d("URI", String.valueOf(contentUri));
        /*Glide.with(this)
                .load(new File(contentUri.getPath()))
                .into(imgParking1);*/

        File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();

            switch (files.length) {
                case 1:
                    Glide.with(this)
                            .load(new File(contentUri.getPath()))
                            .into(imgParking1);
                    break;
                case 2:
                    Glide.with(this)
                            .load(new File(contentUri.getPath()))
                            .into(imgParking2);
                    break;
                case 3:
                    Glide.with(this)
                            .load(new File(contentUri.getPath()))
                            .into(imgParking3);
                    break;
                case 4:
                    Glide.with(this)
                            .load(new File(contentUri.getPath()))
                            .into(imgParking4);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();

                    gpsTracker = new GPSTracker(ParkingPhotoActivity.this);
                    if (gpsTracker.canGetLocation()) {

                        mylat = gpsTracker.getLatitude();
                        mylng = gpsTracker.getLongitude();

                        Prefs.putString("Parking_Lat", String.valueOf(mylat));
                        Prefs.putString("Parking_Lng", String.valueOf(mylng));

                        if (isOnline()) {
                            uploadParking();
                        }

                    } else {
                        gpsTracker.showSettingsAlert();
                    }
                }
                break;
            } // ACTION_TAKE_PHOTO_B
        } // switch
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                btnTakePhoto.setEnabled(true);
            }
        }
    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(ParkingPhotoActivity.this);
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
                        .add("IMEI", params[2])
                        .add("IMAGE_A", params[3])
                        .add("IMAGE_B", params[4])
                        .add("IMAGE_C", params[5])
                        .add("IMAGE_D", params[6])
                        .add("COMMENT", params[7])
                        .add("LATITUDE", params[8])
                        .add("LONGITUDE", params[9])
                        .add("USER_ID", params[10])
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

                            if (STATUS.equals("Success")) {
                                appLog.setLog("ParkingPhotoActivity", "Upload Parking success", USER_ID);

                                Toast.makeText(getApplicationContext(), "Success.", Toast.LENGTH_LONG).show();
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
