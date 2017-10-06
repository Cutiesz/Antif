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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.pixplicity.easyprefs.library.Prefs;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ParkingPhotoNewActivity extends AppCompatActivity {

    TestFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    private MenuItem itemCam;

    private static final int ACTION_TAKE_PHOTO_B = 1;
    private String mCurrentPhotoPath;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private VehicleDBClass VehicleDB;
    //private String vehicleName;
    private String imei = "";

    protected ArrayList<JSONObject> feedDataList;

    private TextView txtParkingDetail;
    private TextView txtParkingDateTime;
    private ImageView imgEdit;

    private String takePhoto;
    private String USER_ID;

    private AppLogClass appLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_photo_new);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAdapter = new TestFragmentAdapter(getSupportFragmentManager(), ParkingPhotoNewActivity.this);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        //mIndicator.setCurrentItem(3);


        takePhoto = getIntent().getStringExtra("takePhoto");
        USER_ID = getIntent().getStringExtra("USER_ID");
        //vehicleName = getIntent().getStringExtra("vehicleName");

        VehicleDB = new VehicleDBClass(this);

        appLog = new AppLogClass(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        setupWidgets();

        //loadVehicleData();
        checkPhotoAndText();


    }

    @Override
    protected void onStart() {
        super.onStart();

        checkPhotoAndText();
    }

    private void checkPhotoAndText() {

        File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                switch (files.length) {
                    case 1:
                        mIndicator.setCurrentItem(0);
                        break;
                    case 2:
                        mIndicator.setCurrentItem(1);
                        break;
                    case 3:
                        mIndicator.setCurrentItem(2);
                        break;
                    case 4:
                        mIndicator.setCurrentItem(3);
                        break;
                    default:
                        dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                        break;
                }
            } else {
                dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
            }

            if (takePhoto != null) {
                if (takePhoto.equals("1")) {
                    dialogAlertTakePhoto();
                }
            }

        } else {
            dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
        }

        txtParkingDateTime = (TextView) findViewById(R.id.txtParkingDateTime);
        String parkingDateTime = Prefs.getString("ParkingDateTime","");
        //txtParkingDateTime.setText(parkingDateTime);
        if (!parkingDateTime.equals("")) {
            // Cut String Date Time
            String[] separated = parkingDateTime.split("-");
            String[] day = separated[2].split(" ");
            String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
            txtParkingDateTime.setText(dateTime);
        } else {
            txtParkingDateTime.setVisibility(View.GONE);
        }

        txtParkingDetail = (TextView) findViewById(R.id.txtParkingDetail);
        String parkingDetail = Prefs.getString("ParkingDetails","");
        if (parkingDetail.equals("")) {
            txtParkingDetail.setText("Parking Details");
            txtParkingDateTime.setText("");
        } else {
            txtParkingDetail.setText(Prefs.getString("ParkingDetails",""));
        }

    }

    private void setupWidgets() {

        imgEdit = (ImageView) findViewById(R.id.imgEdit);
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAlertParkingDetails();
            }
        });

    }

    public void dialogAlertTakePhoto(){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(ParkingPhotoNewActivity.this);

        mDialog.setTitle("Do you want to take photo parking?");
        mDialog.setIcon(R.drawable.ic_action_camera_alt);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("ParkingPhotoActivity", "กดปุ่ม OK จาก Notification", USER_ID);

                txtParkingDetail.setText("");
                txtParkingDateTime.setText("");

                Prefs.putString("ParkingDetails", "");
                Prefs.putString("ParkingDateTime", "");

                File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
                if (storageDir.exists()) {
                    deleteDirectory(storageDir);
                }

                dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //Toast.makeText(getBaseContext(), "Fail", Toast.LENGTH_LONG).show();
                dialog.dismiss();

                //finish();
                //moveTaskToBack(true);
            }
        });
        mDialog.show();
    }

    public void dialogAlertParkingDetails() {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(ParkingPhotoNewActivity.this);
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View Viewlayout = inflater.inflate(R.layout.dialog_parking_detail, (ViewGroup) findViewById(R.id.layout_dialog));

        final EditText edtParkingDetails = (EditText) Viewlayout.findViewById(R.id.edtParkingDetails);
        edtParkingDetails.setText(Prefs.getString("ParkingDetails",""));
        edtParkingDetails.selectAll();

        CheckDialog.setTitle("Enter parking detail.");
        CheckDialog.setIcon(R.drawable.ic_action_error);
        CheckDialog.setView(Viewlayout);

        CheckDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String textParkingDetails = edtParkingDetails.getText().toString();
                Prefs.putString("ParkingDetails", textParkingDetails);
                txtParkingDetail.setText(textParkingDetails);

                //uploadParking();

                String parkingDetail = Prefs.getString("ParkingDetails","");
                if (parkingDetail.equals("")) {
                    txtParkingDetail.setText("Parking Details");
                    txtParkingDateTime.setText("");

                    txtParkingDateTime.setVisibility(View.GONE);
                    txtParkingDateTime.setText("");
                } else {

                    // get current date time
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String DateTime_Current_Internet = sdf.format(new Date());
                    Prefs.putString("ParkingDateTime", DateTime_Current_Internet);

                    // Cut String Date Time
                    String[] separated = DateTime_Current_Internet.split("-");
                    String[] day = separated[2].split(" ");
                    String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
                    txtParkingDateTime.setVisibility(View.VISIBLE);
                    txtParkingDateTime.setText(dateTime);
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

    public void dialogAlertDeleteAllImage(){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(ParkingPhotoNewActivity.this);

        mDialog.setTitle("Are you sure to clear all image?");
        mDialog.setIcon(R.drawable.ic_action_close);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("ParkingPhotoActivity", "กดปุ่ม Clear all image Parking", USER_ID);

                txtParkingDetail.setText("");
                txtParkingDateTime.setText("");

                Prefs.putString("ParkingDetails", "");
                Prefs.putString("ParkingDateTime", "");

                File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
                if (storageDir.exists()) {
                    deleteDirectory(storageDir);
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

    /*private void loadVehicleData() {

        String[][] arrData = VehicleDB.SelectAllByVehicleName(vehicleName);
        if (arrData != null) {
            String _IMEI = arrData[0][1].toString();

            imei = _IMEI;
        }

    }*/

    /*private void uploadParking() {

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


        // get current date time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTime_Current_Internet = sdf.format(new Date());
        Prefs.putString("ParkingDateTime", DateTime_Current_Internet);

        String parkingDateTime = Prefs.getString("ParkingDateTime","");
        if (!parkingDateTime.equals("")) {
            // Cut String Date Time
            String[] separated = parkingDateTime.split("-");
            String[] day = separated[2].split(" ");
            String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
            txtParkingDateTime.setText(dateTime);
        } else {
            txtParkingDateTime.setVisibility(View.GONE);
        }
    }*/

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(ParkingPhotoNewActivity.this);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);

        itemCam = menu.findItem(R.id.action_camera);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            itemCam.setEnabled(false);
            itemCam.setVisible(false);
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_camera:

                //Toast.makeText(getApplicationContext(), "Camera", Toast.LENGTH_LONG).show();
                //ParkingPhoto1Fragment.txtParkingDetail.setText("hello mister how do you do");

                File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
                if (storageDir.exists()) {
                    File[] files = storageDir.listFiles();
                    if (files != null) {
                        if (files.length >= 4) {
                            dialogAlertDeleteAllImage();
                        } else {
                            dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                        }
                    } else {
                        dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                    }
                } else {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                }

                return true;

            case android.R.id.home:
                Intent homeIntent = new Intent(this, NaviDrawerActivity.class);
                //Intent homeIntent = new Intent(this, MainAntifActivity.class);
                homeIntent.putExtra("USER_ID", USER_ID);
                startActivity(homeIntent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch(actionCode) {
            case ACTION_TAKE_PHOTO_B:
                File f = null;

                try {
                    //f = setUpPhotoFile();
                    //mCurrentPhotoPath = f.getAbsolutePath();
                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                        // Do something for lollipop and above versions

                        //Uri photoURI = Uri.fromFile( f);
                        Uri photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", createImageFile());
                        mCurrentPhotoPath = photoURI.getPath();
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            //Toast.makeText(getApplicationContext(), contentUri.toString(), Toast.LENGTH_SHORT).show();
            contentUri = Uri.parse(String.valueOf(contentUri).replace("external_files", "storage/emulated/0"));
            //Toast.makeText(getApplicationContext(), contentUri.toString(), Toast.LENGTH_SHORT).show();
        }

        //Log.d("URI", String.valueOf(contentUri));
        /*Glide.with(this)
                .load(new File(contentUri.getPath()))
                .into(imgParking1);*/

        File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();

            /*switch (files.length) {
                case 1:
                    Glide.with(this)
                            .load(new File(contentUri.getPath()))
                            .into(ParkingPhoto1Fragment.imgParking);
                    break;
                case 2:
                    Glide.with(this)
                            .load(new File(contentUri.getPath()))
                            .into(ParkingPhoto1Fragment.imgParking);
                    break;
                case 3:
                    Glide.with(this)
                            .load(new File(contentUri.getPath()))
                            .into(ParkingPhoto1Fragment.imgParking);
                    break;
                case 4:
                    Glide.with(this)
                            .load(new File(contentUri.getPath()))
                            .into(ParkingPhoto1Fragment.imgParking);
                    break;
            }*/

            /*Intent intent = new Intent(getApplicationContext(), ParkingPhotoNewActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);*/
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();

                    gpsTracker = new GPSTracker(ParkingPhotoNewActivity.this);
                    if (gpsTracker.canGetLocation()) {

                        mylat = gpsTracker.getLatitude();
                        mylng = gpsTracker.getLongitude();

                        Prefs.putString("Parking_Lat", String.valueOf(mylat));
                        Prefs.putString("Parking_Lng", String.valueOf(mylng));

                        /*if (isOnline()) {
                            uploadParking();
                        }*/

                        // get current date time
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String DateTime_Current_Internet = sdf.format(new Date());
                        Prefs.putString("ParkingDateTime", DateTime_Current_Internet);

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
                itemCam.setEnabled(true);
                itemCam.setVisible(true);
            }
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

            //moveTaskToBack(true);
            Intent intent = new Intent(getApplicationContext(), NaviDrawerActivity.class);
            intent.putExtra("USER_ID", USER_ID);
            startActivity(intent);
        }
        return false;
    }
}
