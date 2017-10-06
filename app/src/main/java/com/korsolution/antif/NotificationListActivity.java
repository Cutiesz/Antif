package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class NotificationListActivity extends AppCompatActivity {

    private SwipeRefreshLayout mRefreshView;
    private ListView mListView;
    private TextView txtNoData;

    protected ArrayList<JSONObject> feedDataList;

    private NotificationDBClass NotificationDB;

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        NotificationDB = new NotificationDBClass(this);

        USER_ID = getIntent().getStringExtra("USER_ID");

        appLog = new AppLogClass(this);

        setupWidgets();

        if (isOnline()) {

            feedData();

        } else {
            txtNoData.setVisibility(View.VISIBLE);
            txtNoData.setText("No Internet signal, Please try agian!!");
        }

    }

    private void feedData() {

        appLog.setLog("NotificationListActivity", "Feed Data Notification", USER_ID);

        new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_NOTI", "LYd162fYt", /*"2"*/USER_ID);

        /*String[][] arrData = NotificationDB.SelectAll();
        if (arrData != null) {
            mListView.setAdapter(new ImageAdapter(this, arrData));
        }*/
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
                if (isOnline()) {

                    feedData();

                } else {
                    txtNoData.setVisibility(View.VISIBLE);
                    txtNoData.setText("No Internet signal, Please try agian!!");
                }
            }
        });
    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(NotificationListActivity.this);
            nDialog.setMessage("Downloading..");
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

                NotificationDB.Delete();
                mRefreshView.setRefreshing(false);

                feedDataList = CuteFeedJsonUtil.feed(s);
                if (feedDataList != null) {

                    for (int i = 0; i <= feedDataList.size(); i++) {
                        try {

                            String NOTI_ID = String.valueOf(feedDataList.get(i).getString("NOTI_ID"));
                            String NOTI_NAME = String.valueOf(feedDataList.get(i).getString("NOTI_NAME"));
                            String READED = String.valueOf(feedDataList.get(i).getString("READED"));
                            String LATITUDE = String.valueOf(feedDataList.get(i).getString("LATITUDE"));
                            String LONGITUDE = String.valueOf(feedDataList.get(i).getString("LONGITUDE"));
                            String PLACE = String.valueOf(feedDataList.get(i).getString("PLACE"));
                            String UPDATE_DATE = String.valueOf(feedDataList.get(i).getString("UPDATE_DATE"));

                            NotificationDB.Insert(NOTI_ID, NOTI_NAME, READED, LATITUDE, LONGITUDE, PLACE, UPDATE_DATE);

                        } catch (Exception e) {

                        }
                    }

                    String[][] arrData = NotificationDB.SelectAll();
                    if (arrData != null) {
                        mListView.setAdapter(new ImageAdapter(NotificationListActivity.this, arrData));
                    } else {
                        txtNoData.setVisibility(View.VISIBLE);
                    }

                } else {
                    //Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                    txtNoData.setVisibility(View.VISIBLE);
                }

            } else {
                //Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
                txtNoData.setVisibility(View.VISIBLE);
            }

            nDialog.dismiss();
        }
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
                convertView = inflater.inflate(R.layout.listview_item_noti, null);
            }

            final LinearLayout mainlayout = (LinearLayout) convertView.findViewById(R.id.mainlayout);
            ImageView imgMapStatic = (ImageView) convertView.findViewById(R.id.imgMapStatic);
            TextView txtNoti = (TextView) convertView.findViewById(R.id.txtNoti);
            TextView txtPlace = (TextView) convertView.findViewById(R.id.txtPlace);
            TextView txtDateTimeNoti = (TextView) convertView.findViewById(R.id.txtDateTimeNoti);

            final String ID = arrList[position][0].toString();
            final String NOTI_ID = arrList[position][1].toString();
            final String NOTI_NAME = arrList[position][2].toString();
            final String READED = arrList[position][3].toString();
            final String LATITUDE = arrList[position][4].toString();
            final String LONGITUDE = arrList[position][5].toString();
            final String PLACE = arrList[position][6].toString();
            final String UPDATE_DATE = arrList[position][7].toString();

            String url = "http://maps.google.com/maps/api/staticmap?center=" + LATITUDE + "," + LONGITUDE + "&zoom=15&size=200x200&sensor=false&markers=" + LATITUDE + "," + LONGITUDE;

            Glide.with(NotificationListActivity.this)
                    .load(url)
                    //.error(R.drawable.blank_img)
                    //.bitmapTransform(new CropCircleTransformation(NotificationListActivity.this))
                    .into(imgMapStatic);

            if (READED.equals("1")) {
                mainlayout.setBackgroundColor(Color.LTGRAY);
            } else {
                mainlayout.setBackgroundColor(Color.WHITE);
            }

            // Cut String Date Time
            String[] separated = UPDATE_DATE.split("-");
            String[] day = separated[2].split("T");
            String[] time = day[1].split("\\.");
            //String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];
            String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + time[0];

            txtNoti.setText(NOTI_NAME);
            txtPlace.setText(PLACE);
            txtDateTimeNoti.setText(dateTime);

            mainlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (READED.equals("0")) {
                        mainlayout.setBackgroundColor(Color.LTGRAY);
                        NotificationDB.UpdateDataRead(ID, "1");

                        new FeedAsynTaskUpdateNoti().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_UPDATE_NOTI", "LYd162fYt", NOTI_ID);
                    }

                    Intent intent = new Intent(getApplicationContext(), NotificationMapActivity.class);
                    intent.putExtra("NOTI_NAME", NOTI_NAME);
                    intent.putExtra("LATITUDE", LATITUDE);
                    intent.putExtra("LONGITUDE", LONGITUDE);
                    intent.putExtra("PLACE", PLACE);
                    intent.putExtra("UPDATE_DATE", UPDATE_DATE);
                    intent.putExtra("USER_ID", USER_ID);
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

    public class FeedAsynTaskUpdateNoti extends AsyncTask<String, Void, String> {

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
                        .add("NOTI_ID", params[2])
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
