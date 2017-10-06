package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class FeedDetailsNewActivity extends AppCompatActivity {

    /*VehicleImageFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;*/

    private ImageView imgProfile;
    private TextView txtTitle;
    private TextView txtName;
    private TextView txtDateTime;
    private TextView txtDetails;
    private TextView txtHashTag1;
    private TextView txtHashTag2;

    private Switch mSwitch;

    private ImageView img1;
    private ImageView img2;
    private ImageView img3;
    private ImageView img4;

    private LinearLayout layoutCall;

    private ListView mListView;
    private EditText edtComment;
    private ImageButton btnSend;

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

    protected ArrayList<JSONObject> feedDataList;
    protected ArrayList<JSONObject> feedUploadCommentDataList;

    private FeedCommentDBClass FeedCommentDB;

    private FeedNewsCommentListViewAdapter mAdapterListView;

    private AccountDBClass AccountDB;

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private AppLogClass appLog;

    private MenuItem itemTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_details_new);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
/*
        mAdapter = new VehicleImageFragmentAdapter(getSupportFragmentManager(), FeedDetailsNewActivity.this);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
*/

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

        FeedCommentDB = new FeedCommentDBClass(this);
        AccountDB = new AccountDBClass(this);

        appLog = new AppLogClass(this);

        setupWidgets();

        loadData();
    }

    private void loadData() {

        // Cut String Date Time
        String[] separated = CREATE_DATE.split("-");
        String[] day = separated[2].split("T");
        String[] time = day[1].split("\\.");
        //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + "\n" + day[1];
        String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];

        Glide.with(this)
                .load(USER_PICTURE)
                .error(R.drawable.blank_person_oval)
                .bitmapTransform(new CropCircleTransformation(this))
                .into(imgProfile);

        Glide.with(this)
                .load(CAR_IMAGE_FRONT)
                .error(R.drawable.blank_img)
                .into(img1);
        Glide.with(this)
                .load(CAR_IMAGE_BACK)
                .error(R.drawable.blank_img)
                .into(img2);
        Glide.with(this)
                .load(CAR_IMAGE_LEFT)
                .error(R.drawable.blank_img)
                .into(img3);
        Glide.with(this)
                .load(CAR_IMAGE_RIGHT)
                .error(R.drawable.blank_img)
                .into(img4);

/*
        Glide.with(this)
                .load("http://202.183.192.165/image_antif/IMAGE_CAR/862951028729201/thum_862951028729201_1.jpg")
                .error(R.drawable.car_real)
                .into(img1);
        Glide.with(this)
                .load("http://202.183.192.165/image_antif/IMAGE_CAR/862951028729201/thum_862951028729201_2.jpg")
                .error(R.drawable.car_real)
                .into(img2);
        Glide.with(this)
                .load("http://202.183.192.165/image_antif/IMAGE_CAR/862951028729201/thum_862951028729201_3.jpg")
                .error(R.drawable.car_real)
                .into(img3);
        Glide.with(this)
                .load("http://202.183.192.165/image_antif/IMAGE_CAR/862951028729201/thum_862951028729201_4.jpg")
                .error(R.drawable.car_real)
                .into(img4);
*/
        txtTitle.setText("แชร์" + FEED_TYPE_NAME);
        txtName.setText(DISPLAY_NAME);
        txtDateTime.setText("แชร์เมื่อ " + dateTime);
        txtDetails.setText(FEED_NAME + "\n" + FEED_DETAIL);
        txtHashTag1.setText("#" + VEHICLE_DISPLAY + " #" + VEHICLE_TYPE_NAME + " #" + VEHICLE_BRAND_NAME);
        txtHashTag2.setText("#พบเบาะแสกรุณาติดต่อ " + TEL_NUM);

        // SHARE_LOCATION : 0 = no share / 1 = share
        if (SHARE_LOCATION.equals("1")) {
            mSwitch.setChecked(true);
        } else {
            mSwitch.setChecked(false);
        }

        new FeedAsynTaskComment().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_FEED_COMMENT", "LYd162fYt", FEED_HEADER_ID);
    }

    private void uploadComment() {

        String _comment = edtComment.getText().toString();

        if (_comment.length() > 0) {

            String[][] arrData = AccountDB.SelectAllAccount();
            if (arrData != null) {
                String _userID = arrData[0][1].toString();

                gpsTracker = new GPSTracker(FeedDetailsNewActivity.this);
                if (gpsTracker.canGetLocation()) {

                    mylat = gpsTracker.getLatitude();
                    mylng = gpsTracker.getLongitude();

                    String _lat = String.valueOf(mylat);
                    String _lng = String.valueOf(mylng);

                    new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_FEED_COMMENT", "LYd162fYt", FEED_HEADER_ID, _comment, _lat, _lng, _userID);

                } else {
                    gpsTracker.showSettingsAlert();
                }
            }

        }

    }

    private void setupWidgets() {

        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtName = (TextView) findViewById(R.id.txtName);
        txtDateTime = (TextView) findViewById(R.id.txtDateTime);
        txtDetails = (TextView) findViewById(R.id.txtDetails);
        txtHashTag1 = (TextView) findViewById(R.id.txtHashTag1);
        txtHashTag2 = (TextView) findViewById(R.id.txtHashTag2);

        mSwitch = (Switch) findViewById(R.id.mSwitch);
        mSwitch.setClickable(false);

        img1 = (ImageView) findViewById(R.id.img1);
        img2 = (ImageView) findViewById(R.id.img2);
        img3 = (ImageView) findViewById(R.id.img3);
        img4 = (ImageView) findViewById(R.id.img4);

        layoutCall = (LinearLayout) findViewById(R.id.layoutCall);

        mListView = (ListView) findViewById(R.id.listview);
        edtComment = (EditText) findViewById(R.id.edtComment);
        btnSend = (ImageButton) findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("FeedDetailsActivity", "กดปุ่ม Send Comment", USER_ID);

                uploadComment();
            }
        });

        /*mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    Toast.makeText(getApplicationContext(), String.valueOf(isChecked), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), String.valueOf(isChecked), Toast.LENGTH_LONG).show();
                }
            }
        });*/

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewVehiclePhotoActivity.class);
                intent.putExtra("pathImage", CAR_IMAGE_FRONT);
                startActivity(intent);
            }
        });
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewVehiclePhotoActivity.class);
                intent.putExtra("pathImage", CAR_IMAGE_BACK);
                startActivity(intent);
            }
        });
        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewVehiclePhotoActivity.class);
                intent.putExtra("pathImage", CAR_IMAGE_LEFT);
                startActivity(intent);
            }
        });
        img4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewVehiclePhotoActivity.class);
                intent.putExtra("pathImage", CAR_IMAGE_RIGHT);
                startActivity(intent);
            }
        });

        layoutCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed_detail, menu);

        //MenuItem itemTracking = menu.findItem(R.id.action_tracking);
        itemTracking = menu.findItem(R.id.action_tracking);
        if (mSwitch.isChecked()) {
            itemTracking.setVisible(true);
        } else {
            itemTracking.setVisible(false);
        }

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    Toast.makeText(getApplicationContext(), String.valueOf(isChecked), Toast.LENGTH_LONG).show();
                    itemTracking.setVisible(true);
                } else {
                    Toast.makeText(getApplicationContext(), String.valueOf(isChecked), Toast.LENGTH_LONG).show();
                    itemTracking.setVisible(false);
                }
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

        if (id == R.id.action_tracking) {
            appLog.setLog("FeedDetailsNewActivity", "กดปุ่ม menu action bar Tracking", USER_ID);

            Intent intent = new Intent(getApplicationContext(), TrackingVehicleActivity.class);
            intent.putExtra("FEED_HEADER_ID", FEED_HEADER_ID);
            intent.putExtra("FEED_TYPE_NAME", FEED_TYPE_NAME);
            intent.putExtra("FEED_NAME", FEED_NAME);
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
            intent.putExtra("IMEI", IMEI);
            intent.putExtra("TEL_NUM", TEL_NUM);
            startActivity(intent);
        }

        if (id == android.R.id.home) {
            Intent homeIntent = new Intent(this, FeedActivity.class);
            homeIntent.putExtra("USER_ID", USER_ID);
            startActivity(homeIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(FeedDetailsNewActivity.this);
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
                        .add("FEED_HEADER_ID", params[2])
                        .add("COMMENT", params[3])
                        .add("LATITUDE", params[4])
                        .add("LONGITUDE", params[5])
                        .add("USER_ID", params[6])
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



                feedUploadCommentDataList = CuteFeedJsonUtil.feed(s);
                if (feedUploadCommentDataList != null) {
                    for (int i = 0; i <= feedUploadCommentDataList.size(); i++) {
                        try {

                            String STATUS = String.valueOf(feedUploadCommentDataList.get(i).getString("STATUS"));

                            if (STATUS.equals("Success")) {
                                appLog.setLog("FeedDetailsActivity", "Send Comment Success", USER_ID);

                                edtComment.setText("");
                                loadData();
                            } else {
                                Toast.makeText(getApplicationContext(), "Upload comment fail!! Please try agian.", Toast.LENGTH_LONG).show();
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

                feedDataList = CuteFeedJsonUtil.feed(s);
                if (feedDataList != null) {
                    for (int i = 0; i <= feedDataList.size(); i++) {
                        try {
                            String FEED_COMMENT_ID = String.valueOf(feedDataList.get(i).getString("FEED_COMMENT_ID"));
                            String COMMENT = String.valueOf(feedDataList.get(i).getString("COMMENT"));
                            String DISPLAY_NAME = String.valueOf(feedDataList.get(i).getString("DISPLAY_NAME"));
                            String USER_PICTURE = String.valueOf(feedDataList.get(i).getString("USER_PICTURE"));
                            String CREATE_DATE = String.valueOf(feedDataList.get(i).getString("CREATE_DATE"));

                            FeedCommentDB.Insert(feedHeaderID, FEED_COMMENT_ID, COMMENT, DISPLAY_NAME, USER_PICTURE, CREATE_DATE);

                            mAdapterListView = new FeedNewsCommentListViewAdapter();
                            mListView.setAdapter(mAdapterListView);
                            mAdapterListView.notifyDataSetChanged();

                            appLog.setLog("FeedDetailsActivity", "Load Feed Comment", USER_ID);

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

    public class FeedNewsCommentListViewAdapter extends BaseAdapter {

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
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.listview_item_feed_news_comment, null);
                holder = new ViewHolder();

                holder.imgProfile = (ImageView) convertView.findViewById(R.id.imgProfile);
                holder.txtDisplayName = (TextView) convertView.findViewById(R.id.txtDisplayName);
                holder.txtDateTime = (TextView) convertView.findViewById(R.id.txtDateTime);
                holder.txtComment = (TextView) convertView.findViewById(R.id.txtComment);
                holder.btnEditComment = (ImageButton) convertView.findViewById(R.id.btnEditComment);

                convertView.setTag(R.id.imgProfile,  holder.imgProfile);
                convertView.setTag(R.id.txtDisplayName,  holder.txtDisplayName);
                convertView.setTag(R.id.txtDateTime,  holder.txtDateTime);
                convertView.setTag(R.id.txtComment,  holder.txtComment);
                convertView.setTag(R.id.btnEditComment,  holder.btnEditComment);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();

                holder.imgProfile = (ImageView) convertView.getTag(R.id.imgProfile);
                holder.txtDisplayName = (TextView) convertView.getTag(R.id.txtDisplayName);
                holder.txtDateTime = (TextView) convertView.getTag(R.id.txtDateTime);
                holder.txtComment = (TextView) convertView.getTag(R.id.txtComment);
                holder.btnEditComment = (ImageButton) convertView.getTag(R.id.btnEditComment);
            }

            // Set Data
            if (feedDataList != null) {
                try {

                    final String FEED_COMMENT_ID = String.valueOf(feedDataList.get(position).getString("FEED_COMMENT_ID"));
                    final String COMMENT = String.valueOf(feedDataList.get(position).getString("COMMENT"));
                    final String DISPLAY_NAME = String.valueOf(feedDataList.get(position).getString("DISPLAY_NAME"));
                    final String USER_PICTURE = String.valueOf(feedDataList.get(position).getString("USER_PICTURE"));
                    final String CREATE_DATE = String.valueOf(feedDataList.get(position).getString("CREATE_DATE"));

                    // Cut String Date Time
                    String[] separated = CREATE_DATE.split("-");
                    String[] day = separated[2].split("T");
                    String[] time = day[1].split("\\.");
                    //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
                    String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];

                    holder.txtDisplayName.setText(DISPLAY_NAME);
                    holder.txtDateTime.setText(dateTime);
                    holder.txtComment.setText(COMMENT);

                    if (!USER_PICTURE.equals("")) {
                        // set Image
                        Glide.with(FeedDetailsNewActivity.this)
                                .load(USER_PICTURE)
                                .into(holder.imgProfile);
                    } else {
                        // set Image
                        Glide.with(FeedDetailsNewActivity.this)
                                .load(R.drawable.blank_person_oval)
                                .into(holder.imgProfile);
                    }

                    holder.btnEditComment.setVisibility(View.INVISIBLE);
                    holder.btnEditComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                } catch (Exception e) {

                }
            }

            return convertView;
        }

        public class ViewHolder {
            ImageView imgProfile;
            TextView txtDisplayName;
            TextView txtDateTime;
            TextView txtComment;
            ImageButton btnEditComment;
        }
    }
}
