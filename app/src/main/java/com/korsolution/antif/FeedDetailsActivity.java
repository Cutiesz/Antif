package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class FeedDetailsActivity extends AppCompatActivity {

    private ImageView imgVehicle1;
    private ImageView imgVehicle2;
    private ImageView imgVehicle3;
    private ImageView imgVehicle4;
    private TextView txtTitle;
    private TextView txtDateTime;
    private TextView txtDetails;
    private ListView mListView;
    private EditText edtComment;
    private ImageButton btnSend;

    private String FEED_HEADER_ID;
    private String FEED_TYPE_NAME;
    private String FEED_NAME;
    private String VEHICLE_DISPLAY;
    private String VEHICLE_TYPE_NAME;
    private String VEHICLE_BRAND_NAME;
    private String MODEL;
    private String VEHICLE_COLOR_NAME;
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

    protected ArrayList<JSONObject> feedDataList;
    protected ArrayList<JSONObject> feedUploadCommentDataList;

    private FeedCommentDBClass FeedCommentDB;

    private FeedNewsCommentListViewAdapter mAdapter;

    private AccountDBClass AccountDB;

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_details);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        FEED_HEADER_ID = getIntent().getStringExtra("FEED_HEADER_ID");
        FEED_TYPE_NAME = getIntent().getStringExtra("FEED_TYPE_NAME");
        FEED_NAME = getIntent().getStringExtra("FEED_NAME");
        VEHICLE_DISPLAY = getIntent().getStringExtra("VEHICLE_DISPLAY");
        VEHICLE_TYPE_NAME = getIntent().getStringExtra("VEHICLE_TYPE_NAME");
        VEHICLE_BRAND_NAME = getIntent().getStringExtra("VEHICLE_BRAND_NAME");
        MODEL = getIntent().getStringExtra("MODEL");
        VEHICLE_COLOR_NAME = getIntent().getStringExtra("VEHICLE_COLOR_NAME");
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

        FeedCommentDB = new FeedCommentDBClass(this);
        AccountDB = new AccountDBClass(this);

        appLog = new AppLogClass(this);

        setupWidgets();

        loadData();
    }

    private void loadData() {
        if (!CAR_IMAGE_FRONT.equals("")) {
            // set Image
            Glide.with(this)
                    .load(CAR_IMAGE_FRONT)
                    .into(imgVehicle1);
        } else {
            // set Image
            Glide.with(this)
                    .load(R.drawable.blank_img)
                    .into(imgVehicle1);
        }

        if (!CAR_IMAGE_BACK.equals("")) {
            // set Image
            Glide.with(this)
                    .load(CAR_IMAGE_BACK)
                    .into(imgVehicle2);
        } else {
            // set Image
            Glide.with(this)
                    .load(R.drawable.blank_img)
                    .into(imgVehicle2);
        }

        if (!CAR_IMAGE_LEFT.equals("")) {
            // set Image
            Glide.with(this)
                    .load(CAR_IMAGE_LEFT)
                    .into(imgVehicle3);
        } else {
            // set Image
            Glide.with(this)
                    .load(R.drawable.blank_img)
                    .into(imgVehicle3);
        }

        if (!CAR_IMAGE_RIGHT.equals("")) {
            // set Image
            Glide.with(this)
                    .load(CAR_IMAGE_RIGHT)
                    .into(imgVehicle4);
        } else {
            // set Image
            Glide.with(this)
                    .load(R.drawable.blank_img)
                    .into(imgVehicle4);
        }

        // Cut String Date Time
        String[] separated = CREATE_DATE.split("-");
        String[] day = separated[2].split("T");
        String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];

        txtTitle.setText(DISPLAY_NAME + " : " + FEED_TYPE_NAME);
        txtDateTime.setText(dateTime);
        txtDetails.setText(FEED_NAME);

        new FeedAsynTaskComment().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_FEED_COMMENT", "LYd162fYt", FEED_HEADER_ID);
    }

    private void uploadComment() {

        String _comment = edtComment.getText().toString();

        if (_comment.length() > 0) {

            String[][] arrData = AccountDB.SelectAllAccount();
            if (arrData != null) {
                String _userID = arrData[0][1].toString();

                gpsTracker = new GPSTracker(FeedDetailsActivity.this);
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
        imgVehicle1 = (ImageView) findViewById(R.id.imgVehicle1);
        imgVehicle2 = (ImageView) findViewById(R.id.imgVehicle2);
        imgVehicle3 = (ImageView) findViewById(R.id.imgVehicle3);
        imgVehicle4 = (ImageView) findViewById(R.id.imgVehicle4);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtDateTime = (TextView) findViewById(R.id.txtDateTime);
        txtDetails = (TextView) findViewById(R.id.txtDetails);
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
    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(FeedDetailsActivity.this);
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

                            mAdapter = new FeedNewsCommentListViewAdapter();
                            mListView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

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
                        String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];

                        holder.txtDisplayName.setText(DISPLAY_NAME);
                        holder.txtDateTime.setText(dateTime);
                        holder.txtComment.setText(COMMENT);

                        if (!USER_PICTURE.equals("")) {
                            // set Image
                            Glide.with(FeedDetailsActivity.this)
                                    .load(USER_PICTURE)
                                    .into(holder.imgProfile);
                        } else {
                            // set Image
                            Glide.with(FeedDetailsActivity.this)
                                    .load(R.drawable.blank_img)
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

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
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
