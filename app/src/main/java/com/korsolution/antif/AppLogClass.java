package com.korsolution.antif;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Kontin58 on 9/3/2560.
 */

public class AppLogClass {

    private Context context = null;
    private LogDBClass LogDB;
    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    protected ArrayList<JSONObject> feedDataList;

    public AppLogClass(Context context) {
        this.context = context;

        LogDB = new LogDBClass(context);
        gpsTracker = new GPSTracker(context);
    }

    public void setLog(String HEADER, String COMMENT, String CREATE_BY) {

        // get current date time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTime_Current_Internet = sdf.format(new Date());

        if (gpsTracker.canGetLocation()) {

            mylat = gpsTracker.getLatitude();
            mylng = gpsTracker.getLongitude();

            String _lat = String.valueOf(mylat);
            String _lng = String.valueOf(mylng);

            LogDB.Insert(HEADER, _lat, _lng, COMMENT, CREATE_BY, DateTime_Current_Internet);

        } else {
            //gpsTracker.showSettingsAlert();

            LogDB.Insert(HEADER, "0.0", "0.0", COMMENT, CREATE_BY, DateTime_Current_Internet);
        }
    }

    public String[][] getLog() {

        String[][] arrData = null;

        arrData = LogDB.SelectAll();
        if (arrData != null) {
            return arrData;
        }

        return null;
    }

    public void delLog(String logID) {

        LogDB.Delete(logID);
    }

    public void uploadLog() {

        String[][] arrData = LogDB.SelectAll();
        if (arrData != null) {
            for (int i=0; i<arrData.length;i++) {
                String ID = arrData[i][0].toString();
                String HEADER = arrData[i][1].toString();
                String LATITUDE = arrData[i][2].toString();
                String LONGITUDE = arrData[i][3].toString();
                String COMMENT = arrData[i][4].toString();
                String CREATE_BY = arrData[i][5].toString();
                String CREATE_DATE = arrData[i][6].toString();

                new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_LOG", "LYd162fYt", HEADER, LATITUDE, LONGITUDE, COMMENT, CREATE_BY, ID);
            }
        } else {

        }
    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        String _id;

        /*private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(context);
            nDialog.setMessage("Uploading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();

        }*/

        @Override
        protected String doInBackground(String... params) {

            try{

                _id = params[7];

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
                        .add("HEADER", params[2])
                        .add("LATITUDE", params[3])
                        .add("LONGITUDE", params[4])
                        .add("COMMENT", params[5])
                        .add("CREATE_BY", params[6])
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

                                delLog(_id);

                            }

                            Log.d("LOG", STATUS);


                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(context, "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(context, "Fail!!", Toast.LENGTH_LONG).show();
            }

            //nDialog.dismiss();
        }
    }
}
