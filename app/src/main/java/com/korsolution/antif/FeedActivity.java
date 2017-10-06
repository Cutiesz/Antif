package com.korsolution.antif;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.paginate.Paginate;
import com.paginate.abslistview.LoadingListItemCreator;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class FeedActivity extends AppCompatActivity {

    private ListView mListView;
    private TextView txtNoData;

    protected ArrayList<JSONObject> feedDataList;
    protected ArrayList<JSONObject> feedNumberOfCommentDataList;

    private FeedNewsListViewAdapter mAdapter;
    final int delay = 1000;
    private boolean mIsLoading = true;
    private boolean hasLoadedAll = false;
    private int mPageIndex = 1;
    private SwipeRefreshLayout mRefreshView;

    private FeedCommentDBClass FeedCommentDB;

    private VehicleDBClass VehicleDB;

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private String vehicleName;

    private String USER_ID;

    private AppLogClass appLog;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        FeedCommentDB = new FeedCommentDBClass(this);
        VehicleDB = new VehicleDBClass(this);

        USER_ID = getIntent().getStringExtra("USER_ID");

        appLog = new AppLogClass(this);

        setupWidgets();

        feedData();
    }

    private void feedData() {

        appLog.setLog("FeedActivity", "Feed Data", USER_ID);

        new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_FEED", "LYd162fYt", "", "");
    }

    private void setupWidgets() {
        mRefreshView = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        mListView = (ListView) findViewById(R.id.listview);
        txtNoData = (TextView) findViewById(R.id.txtNoData);

        mRefreshView.setColorSchemeResources(android.R.color.holo_blue_bright);
        mRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //mDataArray.clear();
                feedDataList.clear();
                mAdapter.notifyDataSetChanged();
                mPageIndex = 1;
                hasLoadedAll = false;

                // reload
                mIsLoading = true;
                feedData();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);

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

        if (id == R.id.action_share) {

            gpsTracker = new GPSTracker(FeedActivity.this);
            if (gpsTracker.canGetLocation()) {

                mylat = gpsTracker.getLatitude();
                mylng = gpsTracker.getLongitude();

                String _lat = String.valueOf(mylat);
                String _lng = String.valueOf(mylng);

                appLog.setLog("FeedActivity", "กดปุ่ม Share", USER_ID);

                //dialogAlertShare(USER_ID);
                //dialogAlertCarList(USER_ID);

                int randomPIN = (int)(Math.random()*9000)+1000;
                dialogAlertRandomNumber(String.valueOf(randomPIN));

            } else {
                gpsTracker.showSettingsAlert();
            }

            return true;
        }

        if (id == android.R.id.home) {
            Intent homeIntent = new Intent(this, NaviDrawerActivity.class);
            homeIntent.putExtra("USER_ID", USER_ID);
            startActivity(homeIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void dialogAlertRandomNumber(final String _randomNumber) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(FeedActivity.this);
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

                    String[][] arrData = VehicleDB.SelectAll();
                    if (arrData != null) {
                        if (arrData.length > 1) {
                            //dialogAlertCarList(USER_ID);

                            dialogVehicleSelect();
                        } else if (arrData.length == 1) {
                            //dialogAlertShareFeed("1", USER_ID);

                            String IMEI = arrData[0][1].toString();

                            Intent intent = new Intent(getApplicationContext(), FeedAddActivity.class);
                            intent.putExtra("vehicleName", vehicleName);
                            intent.putExtra("FEED_HEADER_ID", "");
                            intent.putExtra("IMEI", IMEI);
                            intent.putExtra("USER_ID", USER_ID);
                            startActivity(intent);
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

    public void dialogAlertEditShareFeed(final String _feedHeaderId, final String _feedName, final String _userID) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(FeedActivity.this);
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View Viewlayout = inflater.inflate(R.layout.dialog_share_feed, (ViewGroup) findViewById(R.id.layout_dialog));

        final EditText edtFeed = (EditText) Viewlayout.findViewById(R.id.edtFeed);

        edtFeed.setText(_feedName);
        edtFeed.selectAll();

        String _imei = "";

        String[][] arrData = VehicleDB.SelectAllByVehicleName(vehicleName);
        if (arrData != null) {
            _imei = arrData[0][1].toString();
        }

        CheckDialog.setTitle("Do you want to Edit feed?");
        CheckDialog.setIcon(R.drawable.ic_action_error);
        CheckDialog.setView(Viewlayout);

        // Button OK
        final String final_imei = _imei;

        CheckDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String textFeed = edtFeed.getText().toString();
                //Toast.makeText(getApplicationContext(), emergencyNumbers, Toast.LENGTH_LONG).show();

                gpsTracker = new GPSTracker(FeedActivity.this);
                if (gpsTracker.canGetLocation()) {

                    mylat = gpsTracker.getLatitude();
                    mylng = gpsTracker.getLongitude();

                    String _lat = String.valueOf(mylat);
                    String _lng = String.valueOf(mylng);

                    appLog.setLog("FeedActivity", "กดปุ่ม OK Edit feed " + textFeed + " (IMEI " + final_imei + ")", USER_ID);

                    editFeed(textFeed, final_imei, _feedHeaderId, _lat, _lng, _userID);

                } else {
                    gpsTracker.showSettingsAlert();
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

    private void editFeed(String _feedName, String _imei, String _feedHeaderId, String _lat, String _lng, String _userID) {

        appLog.setLog("FeedActivity", "Edit feed " + _feedName + " (IMEI " + _imei + ") (FEED_HEADER_ID " + _feedHeaderId + ")", USER_ID);

        new EditShareFeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPDATE_FEED", "LYd162fYt", _feedName, _imei, _feedHeaderId, _lat, _lng, _userID);
    }

    public class EditShareFeedAsynTask extends AsyncTask<String, Void, String> {

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
                        .add("FEED_NAME", params[2])
                        .add("IMEI", params[3])
                        .add("FEED_HEADER_ID", params[4])
                        .add("LATITUDE", params[5])
                        .add("LONGITUDE", params[6])
                        .add("USER_ID", params[7])
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
                                feedData();
                                Toast.makeText(getApplicationContext(), "Edit your feed success.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Edit your feed fail!! Please try agian.", Toast.LENGTH_LONG).show();
                            }

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

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

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

                mRefreshView.setRefreshing(false);

                feedDataList = CuteFeedJsonUtil.feed(s);
                if (feedDataList != null) {

                    for (int i = 0; i <= feedDataList.size(); i++) {
                        try {

                            String FEED_HEADER_ID = String.valueOf(feedDataList.get(i).getString("FEED_HEADER_ID"));
                            String FEED_TYPE_NAME = String.valueOf(feedDataList.get(i).getString("FEED_TYPE_NAME"));
                            String FEED_NAME = String.valueOf(feedDataList.get(i).getString("FEED_NAME"));
                            String VEHICLE_DISPLAY = String.valueOf(feedDataList.get(i).getString("VEHICLE_DISPLAY"));
                            String VEHICLE_TYPE_NAME = String.valueOf(feedDataList.get(i).getString("VEHICLE_TYPE_NAME"));
                            String VEHICLE_BRAND_NAME = String.valueOf(feedDataList.get(i).getString("VEHICLE_BRAND_NAME"));
                            String MODEL = String.valueOf(feedDataList.get(i).getString("MODEL"));
                            String VEHICLE_COLOR = String.valueOf(feedDataList.get(i).getString("VEHICLE_COLOR"));
                            String YEAR = String.valueOf(feedDataList.get(i).getString("YEAR"));
                            String CAR_IMAGE_FRONT = String.valueOf(feedDataList.get(i).getString("CAR_IMAGE_FRONT"));
                            String CAR_IMAGE_BACK = String.valueOf(feedDataList.get(i).getString("CAR_IMAGE_BACK"));
                            String CAR_IMAGE_LEFT = String.valueOf(feedDataList.get(i).getString("CAR_IMAGE_LEFT"));
                            String CAR_IMAGE_RIGHT = String.valueOf(feedDataList.get(i).getString("CAR_IMAGE_RIGHT"));
                            String STATUS = String.valueOf(feedDataList.get(i).getString("STATUS"));
                            String CREATE_DATE = String.valueOf(feedDataList.get(i).getString("CREATE_DATE"));
                            String CREATE_BY = String.valueOf(feedDataList.get(i).getString("CREATE_BY"));
                            String DISPLAY_NAME = String.valueOf(feedDataList.get(i).getString("DISPLAY_NAME"));
                            String USER_ID = String.valueOf(feedDataList.get(i).getString("USER_ID"));
                            String SHARE_LOCATION = String.valueOf(feedDataList.get(i).getString("SHARE_LOCATION"));
                            String LATITUDE = String.valueOf(feedDataList.get(i).getString("LATITUDE"));
                            String LONGITUDE = String.valueOf(feedDataList.get(i).getString("LONGITUDE"));
                            String PLACE = String.valueOf(feedDataList.get(i).getString("PLACE"));
                            String COUNT_COMMENT = String.valueOf(feedDataList.get(i).getString("COUNT_COMMENT"));
                            String USER_PICTURE = String.valueOf(feedDataList.get(i).getString("USER_PICTURE"));
                            String IMEI = String.valueOf(feedDataList.get(i).getString("IMEI"));
                            String TEL_NUM = String.valueOf(feedDataList.get(i).getString("TEL_NUM"));

                            new FeedAsynTaskComment().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_FEED_COMMENT", "LYd162fYt", FEED_HEADER_ID);

                            /*mAdapter = new FeedNewsListViewAdapter();
                            mListView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

                            Paginate.with(mListView, callbacks)
                                    .setLoadingTriggerThreshold(2)
                                    .addLoadingListItem(true)
                                    .setLoadingListItemCreator(new CustomLoadingListItemCreator())
                                    .build();

                            mIsLoading = false;*/

                        } catch (Exception e) {

                        }
                    }

                    mAdapter = new FeedNewsListViewAdapter();
                    mListView.setAdapter(mAdapter);

                    Paginate.with(mListView, callbacks)
                            .setLoadingTriggerThreshold(2)
                            .addLoadingListItem(true)
                            .setLoadingListItemCreator(new CustomLoadingListItemCreator())
                            .build();

                    mIsLoading = false;

                } else {
                    //Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                    txtNoData.setVisibility(View.VISIBLE);
                }

            } else {
                //Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class FeedMore extends AsyncTask<String, Void, String> {

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

                hasLoadedAll = true;
                /*ArrayList<JSONObject> feedDataListNew = CuteFeedJsonUtil.feed(s);
                if (feedDataListNew != null) {
                    feedDataList.addAll(feedDataListNew);
                    mPageIndex++;

                    if (feedDataListNew.size() < 1) {
                        hasLoadedAll = true;
                        txtNoData.setVisibility(View.VISIBLE);
                    }

                } else {
                    hasLoadedAll = true;
                    //Toast.makeText(getApplicationContext(), "hasLoadedAll", Toast.LENGTH_LONG).show();
                }*/

                mAdapter.notifyDataSetChanged();
                mIsLoading = false;

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

    Paginate.Callbacks callbacks = new Paginate.Callbacks() {
        @Override
        public void onLoadMore() {

            mIsLoading = true;

            // Delay and load more
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // load more

                    //Toast.makeText(getBaseContext(), "Load More!", Toast.LENGTH_SHORT).show();
                    //String.valueOf(mPageIndex)
                    new FeedMore().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_FEED", "LYd162fYt", "", "");

                }
            }, delay);

        }

        @Override
        public boolean isLoading() {
            // Indicate whether new page loading is in progress or not
            return mIsLoading;
        }

        @Override
        public boolean hasLoadedAllItems() {
            // Indicate whether all data (pages) are loaded or not
            return hasLoadedAll;
        }
    };

    private class CustomLoadingListItemCreator implements LoadingListItemCreator {
        @Override
        public View newView(int position, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.custom_loading_list_item, parent, false);
            return view;
        }

        @Override
        public void bindView(int position, View view) {
            // Bind custom loading row if needed
        }
    }

    public class FeedNewsListViewAdapter extends BaseAdapter {

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

            // Set Data
            if (feedDataList != null) {
                try {

                    final String FEED_HEADER_ID = String.valueOf(feedDataList.get(position).getString("FEED_HEADER_ID"));
                    final String FEED_TYPE_NAME = String.valueOf(feedDataList.get(position).getString("FEED_TYPE_NAME"));
                    final String FEED_NAME = String.valueOf(feedDataList.get(position).getString("FEED_NAME"));
                    final String FEED_DETAIL = String.valueOf(feedDataList.get(position).getString("FEED_DETAIL"));
                    final String VEHICLE_DISPLAY = String.valueOf(feedDataList.get(position).getString("VEHICLE_DISPLAY"));
                    final String VEHICLE_TYPE_NAME = String.valueOf(feedDataList.get(position).getString("VEHICLE_TYPE_NAME"));
                    final String VEHICLE_BRAND_NAME = String.valueOf(feedDataList.get(position).getString("VEHICLE_BRAND_NAME"));
                    final String MODEL = String.valueOf(feedDataList.get(position).getString("MODEL"));
                    final String VEHICLE_COLOR = String.valueOf(feedDataList.get(position).getString("VEHICLE_COLOR"));
                    final String YEAR = String.valueOf(feedDataList.get(position).getString("YEAR"));
                    final String CAR_IMAGE_FRONT = String.valueOf(feedDataList.get(position).getString("CAR_IMAGE_FRONT"));
                    final String CAR_IMAGE_BACK = String.valueOf(feedDataList.get(position).getString("CAR_IMAGE_BACK"));
                    final String CAR_IMAGE_LEFT = String.valueOf(feedDataList.get(position).getString("CAR_IMAGE_LEFT"));
                    final String CAR_IMAGE_RIGHT = String.valueOf(feedDataList.get(position).getString("CAR_IMAGE_RIGHT"));
                    final String STATUS = String.valueOf(feedDataList.get(position).getString("STATUS"));
                    final String CREATE_DATE = String.valueOf(feedDataList.get(position).getString("CREATE_DATE"));
                    final String CREATE_BY = String.valueOf(feedDataList.get(position).getString("CREATE_BY"));
                    final String DISPLAY_NAME = String.valueOf(feedDataList.get(position).getString("DISPLAY_NAME"));
                    final String _USER_ID = String.valueOf(feedDataList.get(position).getString("USER_ID"));
                    final String SHARE_LOCATION = String.valueOf(feedDataList.get(position).getString("SHARE_LOCATION"));
                    final String LATITUDE = String.valueOf(feedDataList.get(position).getString("LATITUDE"));
                    final String LONGITUDE = String.valueOf(feedDataList.get(position).getString("LONGITUDE"));
                    final String PLACE = String.valueOf(feedDataList.get(position).getString("PLACE"));
                    final String COUNT_COMMENT = String.valueOf(feedDataList.get(position).getString("COUNT_COMMENT"));
                    final String USER_PICTURE = String.valueOf(feedDataList.get(position).getString("USER_PICTURE"));
                    final String IMEI = String.valueOf(feedDataList.get(position).getString("IMEI"));
                    final String TEL_NUM = String.valueOf(feedDataList.get(position).getString("TEL_NUM"));

                    // Cut String Date Time
                    String[] separated = CREATE_DATE.split("-");
                    String[] day = separated[2].split("T");
                    String[] time = day[1].split("\\.");
                    //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
                    String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];

                    holder.txtTitle.setText(DISPLAY_NAME + " : " + FEED_TYPE_NAME);
                    holder.txtVehicleDisplay.setText("ทะเบียน : " + VEHICLE_DISPLAY);
                    holder.txtDateTime.setText(dateTime);
                    holder.txtDetails.setText(FEED_NAME);

                    // set Image
                    Glide.with(FeedActivity.this)
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
                            intent.putExtra("IMEI", IMEI);
                            intent.putExtra("TEL_NUM", TEL_NUM);
                            startActivity(intent);
                        }
                    });

                    if (USER_ID.equals(_USER_ID)) {
                        holder.btnEditFeed.setVisibility(View.VISIBLE);
                        holder.btnEditFeed.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                gpsTracker = new GPSTracker(FeedActivity.this);
                                if (gpsTracker.canGetLocation()) {

                                    mylat = gpsTracker.getLatitude();
                                    mylng = gpsTracker.getLongitude();

                                    String _lat = String.valueOf(mylat);
                                    String _lng = String.valueOf(mylng);

                                    //dialogAlertEditShareFeed(FEED_HEADER_ID, FEED_NAME, _USER_ID);

                                    Intent intent = new Intent(getApplicationContext(), FeedAddActivity.class);
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
                                    intent.putExtra("IMEI", IMEI);
                                    intent.putExtra("TEL_NUM", TEL_NUM);
                                    startActivity(intent);

                                } else {
                                    gpsTracker.showSettingsAlert();
                                }
                            }
                        });
                    } else {
                        holder.btnEditFeed.setVisibility(View.INVISIBLE);
                    }

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


    private void dialogVehicleSelect() {
        dialog = new Dialog(this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("Choose your car.");
        dialog.setContentView(R.layout.dialog_select_vehicle);
        dialog.setCancelable(true);

        ListView mListView = (ListView) dialog.findViewById(R.id.listview);
        String[][] arrData = VehicleDB.SelectAll();
        if (arrData != null) {
            mListView.setAdapter(new ImageAdapter(this, arrData));
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

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private String[][] arrList;

        public ImageAdapter(Context c, String[][] _list)
        {
            // TODO Auto-generated method stub
            context = c;
            arrList = _list;
        }

        public int getCount() {
            // TODO Auto-generated method stub
            return arrList.length;
        }

        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listview_item_select_vehicle, null);
            }

            LinearLayout layout1 = (LinearLayout) convertView.findViewById(R.id.layout1);
            ImageView imgVehicle = (ImageView) convertView.findViewById(R.id.imgVehicle);
            TextView txtVehicleName = (TextView) convertView.findViewById(R.id.txtVehicleName);
            TextView txtVehicleDateTime = (TextView) convertView.findViewById(R.id.txtVehicleDateTime);
            TextView txtVehicleStatus = (TextView) convertView.findViewById(R.id.txtVehicleStatus);
            TextView txtAuthenticate = (TextView) convertView.findViewById(R.id.txtAuthenticate);

            final String IMEI = arrList[position][1].toString();
            String VEHICLE_DISPLAY = arrList[position][2].toString();
            final String VEHICLE_NAME = arrList[position][3].toString();
            String HISTORY_DATETIME = arrList[position][4].toString();
            String LATITUDE = arrList[position][5].toString();
            String LONGITUDE = arrList[position][6].toString();
            String PLACE = arrList[position][7].toString();
            String ANGLE = arrList[position][8].toString();
            String SPEED = arrList[position][9].toString();
            String STATUS = arrList[position][10].toString();
            String IS_CUT_ENGINE = arrList[position][11].toString();
            String IS_IQNITION = arrList[position][12].toString();
            String IS_AUTHEN = arrList[position][13].toString();
            String SIM = arrList[position][14].toString();
            String TEL_EMERGING_1 = arrList[position][15].toString();
            String TEL_EMERGING_2 = arrList[position][16].toString();
            String TEL_EMERGING_3 = arrList[position][17].toString();
            String IS_UNPLUG_GPS = arrList[position][18].toString();
            String GSM_SIGNAL = arrList[position][19].toString();
            String NUM_SAT = arrList[position][20].toString();
            String CAR_IMAGE_FRONT = arrList[position][21].toString();
            String CAR_IMAGE_BACK = arrList[position][22].toString();
            String CAR_IMAGE_LEFT = arrList[position][23].toString();
            String CAR_IMAGE_RIGHT = arrList[position][24].toString();
            String STATUS_UPDATE = arrList[position][25].toString();
            String VEHICLE_TYPE_NAME = arrList[position][26].toString();
            String VEHICLE_BRAND_NAME = arrList[position][27].toString();
            String MODEL = arrList[position][28].toString();
            String YEAR = arrList[position][29].toString();
            String VEHICLE_COLOR = arrList[position][30].toString();

            // Cut String Date Time
            String[] separated = HISTORY_DATETIME.split("-");
            String[] day = separated[2].split("T");
            //String[] time = day[1].split("\\.");
            String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
            //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];


            String statusUpdate = "";
            if (STATUS_UPDATE.equals("TRUE")) {
                statusUpdate = "Online";
                txtVehicleStatus.setTextColor(Color.GREEN);
            } else {
                statusUpdate = "Offline";
                txtVehicleStatus.setTextColor(Color.RED);
            }

            txtVehicleName.setText(VEHICLE_NAME);
            txtVehicleDateTime.setText(dateTime);
            txtVehicleStatus.setText(statusUpdate);

            // set Image
            Glide.with(FeedActivity.this)
                    .load(CAR_IMAGE_FRONT)
                    .error(R.drawable.image_car)
                    .bitmapTransform(new CropCircleTransformation(FeedActivity.this))
                    .into(imgVehicle);

            layout1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getActivity().getApplicationContext(), strVEHICLE_NAME, Toast.LENGTH_LONG).show();

                    appLog.setLog("FeedActivity", "เลือกรถ " + VEHICLE_NAME, USER_ID);

                    vehicleName = VEHICLE_NAME;

                    //dialogAlertShareFeed("1", USER_ID);

                    Intent intent = new Intent(getApplicationContext(), FeedAddActivity.class);
                    intent.putExtra("vehicleName", vehicleName);
                    intent.putExtra("FEED_HEADER_ID", "");
                    intent.putExtra("IMEI", IMEI);
                    intent.putExtra("USER_ID", USER_ID);
                    startActivity(intent);

                    dialog.dismiss();
                }
            });

            switch (IS_AUTHEN) {
                case "true":
                    txtAuthenticate.setTextColor(Color.GREEN);
                    txtAuthenticate.setText("Authenticate");
                    break;
                case "false":
                    txtAuthenticate.setTextColor(Color.RED);
                    txtAuthenticate.setText("Not authenticate");
                    break;
                default:
                    txtAuthenticate.setTextColor(Color.RED);
                    txtAuthenticate.setText("Not authenticate");
                    break;
            }

            return convertView;
        }
    }
}
