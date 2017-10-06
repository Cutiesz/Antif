package com.korsolution.antif;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class FeedAddActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private TextView txtName;
    private TextView txtVehicleName;
    private Switch mSwitch;
    private EditText edtFeedDetails;
    private EditText edtContactNumber1;
    private EditText edtContactNumber2;

    protected ArrayList<JSONObject> feedDataList;

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private String shareLocation;

    private String vehicleName;
    //private String IMEI;

    private String FEED_HEADER_ID;
    private String FEED_TYPE_NAME;
    private String FEED_NAME;
    private String FEED_DETAIL;
    private String VEHICLE_DISPLAY;
    private String VEHICLE_TYPE_NAME;
    private String VEHICLE_BRAND_NAME;
    private String MODEL;
    private String VEHICLE_COLOR;
    private String YEAR;
    private String CAR_IMAGE_FRONT;
    private String CAR_IMAGE_BACK;
    private String CAR_IMAGE_LEFT;
    private String CAR_IMAGE_RIGHT;
    private String STATUS;
    private String CREATE_DATE;
    private String CREATE_BY;
    private String DISPLAY_NAME;
    private String USER_ID;
    private String SHARE_LOCATION;
    private String LATITUDE;
    private String LONGITUDE;
    private String PLACE;
    private String COUNT_COMMENT;
    private String USER_PICTURE;
    private String IMEI;
    private String TEL_NUM;

    private AccountDBClass AccountDB;
    private VehicleDBClass VehicleDB;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_add);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        vehicleName = getIntent().getStringExtra("vehicleName");
        //FEED_HEADER_ID = getIntent().getStringExtra("FEED_HEADER_ID");
        //IMEI = getIntent().getStringExtra("IMEI");
        //USER_ID = getIntent().getStringExtra("USER_ID");

        FEED_HEADER_ID = getIntent().getStringExtra("FEED_HEADER_ID");
        FEED_TYPE_NAME = getIntent().getStringExtra("FEED_TYPE_NAME");
        FEED_NAME = getIntent().getStringExtra("FEED_NAME");
        FEED_DETAIL = getIntent().getStringExtra("FEED_DETAIL");
        VEHICLE_DISPLAY = getIntent().getStringExtra("VEHICLE_DISPLAY");
        VEHICLE_TYPE_NAME = getIntent().getStringExtra("VEHICLE_TYPE_NAME");
        VEHICLE_BRAND_NAME = getIntent().getStringExtra("VEHICLE_BRAND_NAME");
        MODEL = getIntent().getStringExtra("MODEL");
        VEHICLE_COLOR = getIntent().getStringExtra("VEHICLE_COLOR");
        YEAR = getIntent().getStringExtra("YEAR");
        CAR_IMAGE_FRONT = getIntent().getStringExtra("CAR_IMAGE_FRONT");
        CAR_IMAGE_BACK = getIntent().getStringExtra("CAR_IMAGE_BACK");
        CAR_IMAGE_LEFT = getIntent().getStringExtra("CAR_IMAGE_LEFT");
        CAR_IMAGE_RIGHT = getIntent().getStringExtra("CAR_IMAGE_RIGHT");
        STATUS = getIntent().getStringExtra("STATUS");
        CREATE_DATE = getIntent().getStringExtra("CREATE_DATE");
        CREATE_BY = getIntent().getStringExtra("CREATE_BY");
        DISPLAY_NAME = getIntent().getStringExtra("DISPLAY_NAME");
        USER_ID = getIntent().getStringExtra("USER_ID");
        SHARE_LOCATION = getIntent().getStringExtra("SHARE_LOCATION");
        LATITUDE = getIntent().getStringExtra("LATITUDE");
        LONGITUDE = getIntent().getStringExtra("LONGITUDE");
        PLACE = getIntent().getStringExtra("PLACE");
        COUNT_COMMENT = getIntent().getStringExtra("COUNT_COMMENT");
        USER_PICTURE = getIntent().getStringExtra("USER_PICTURE");
        IMEI = getIntent().getStringExtra("IMEI");
        TEL_NUM = getIntent().getStringExtra("TEL_NUM");

        AccountDB = new AccountDBClass(this);
        VehicleDB = new VehicleDBClass(this);

        appLog = new AppLogClass(this);

        setupWidgets();

        loadData();
    }

    private void setupWidgets() {

        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        txtName = (TextView) findViewById(R.id.txtName);
        txtVehicleName = (TextView) findViewById(R.id.txtVehicleName);
        mSwitch = (Switch) findViewById(R.id.mSwitch);
        edtFeedDetails = (EditText) findViewById(R.id.edtFeedDetails);
        edtContactNumber1 = (EditText) findViewById(R.id.edtContactNumber1);
        edtContactNumber2 = (EditText) findViewById(R.id.edtContactNumber2);

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // SHARE_LOCATION : 0 = no share / 1 = share
                if (isChecked) {
                    shareLocation = "1";
                } else {
                    shareLocation = "0";
                }
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

                appLog.setLog("FeedAddActivity", "กดปุ่ม Save", USER_ID);

                gpsTracker = new GPSTracker(FeedAddActivity.this);
                if (gpsTracker.canGetLocation()) {

                    mylat = gpsTracker.getLatitude();
                    mylng = gpsTracker.getLongitude();

                    String _lat = String.valueOf(mylat);
                    String _lng = String.valueOf(mylng);

                    appLog.setLog("FeedAddActivity", "กดปุ่ม Share feed (IMEI " + IMEI + ")", USER_ID);

                    shareFeed("1", _lat, _lng);

                } else {
                    gpsTracker.showSettingsAlert();
                }

                break;
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {

        if (!FEED_HEADER_ID.equals("")) {

            String[][] arrDataVehicle = VehicleDB.SelectAllByVehicleDisplay(VEHICLE_DISPLAY);
            if (arrDataVehicle != null) {
                String strIMEI = arrDataVehicle[0][1].toString();
                String strVEHICLE_DISPLAY = arrDataVehicle[0][2].toString();

                //IMEI = strIMEI;
                txtVehicleName.setText(strVEHICLE_DISPLAY);
            }

            // set Image
            Glide.with(FeedAddActivity.this)
                    .load(USER_PICTURE)
                    .error(R.drawable.blank_person_photo)
                    .into(imgProfile);

            txtName.setText(DISPLAY_NAME);

            edtFeedDetails.setText(FEED_NAME);

            if (TEL_NUM.contains(",")) {

                String[] separated = TEL_NUM.split(",");
                String contractNumber1 = separated[0].toString();
                String contractNumber2 = separated[1].toString();

            } else {

                edtContactNumber1.setText(TEL_NUM);

            }

            if (SHARE_LOCATION.equals("1")) {

                mSwitch.setChecked(true);

            } else {

                mSwitch.setChecked(false);

            }
            shareLocation = SHARE_LOCATION;

        } else {

            String[][] arrDataAccount = AccountDB.SelectAllAccount();
            if (arrDataAccount != null) {
                String strDISPLAY_NAME = arrDataAccount[0][5].toString();
                String strUSER_PICTURE = arrDataAccount[0][12].toString();

                // set Image
                Glide.with(FeedAddActivity.this)
                        .load(strUSER_PICTURE)
                        .error(R.drawable.blank_person_photo)
                        .into(imgProfile);

                txtName.setText(strDISPLAY_NAME);

                txtVehicleName.setText(vehicleName);
            }

            shareLocation = "0";
            mSwitch.setChecked(false);

        }

    }

    private void shareFeed(String feedType, String lat, String lng) {

        String feedName = edtFeedDetails.getText().toString();
        String contractNumber1 = edtContactNumber1.getText().toString();
        String contractNumber2 = edtContactNumber2.getText().toString();


        // Feed Type : 1 = Car lost / 2 = News
        appLog.setLog("FeedAddActivity", "Share feed " + feedName + " (IMEI " + IMEI + ") (Type " + feedType + ")", USER_ID);


        new ShareFeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_FEED", "LYd162fYt", FEED_HEADER_ID, feedType, feedName, IMEI, lat, lng, shareLocation, USER_ID, contractNumber1, contractNumber2);
    }

    public class ShareFeedAsynTask extends AsyncTask<String, Void, String> {

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
                        .add("FEED_HEADER_ID", params[2])
                        .add("FEED_TYPE_ID", params[3])
                        .add("FEED_NAME", params[4])
                        .add("IMEI", params[5])
                        .add("LATITUDE", params[6])
                        .add("LONGITUDE", params[7])
                        .add("SHARE_LOCATION", params[8])
                        .add("USER_ID", params[9])
                        .add("TEL_NUM_A", params[10])
                        .add("TEL_NUM_B", params[11])
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

                            if (!STATUS.equals("Fail")) {
                                Toast.makeText(getApplicationContext(), "Upload your feed success.", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                                intent.putExtra("USER_ID", USER_ID);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Upload your feed fail!! Please try agian.", Toast.LENGTH_LONG).show();
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
        }
    }
}
