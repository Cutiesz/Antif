package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.anton46.stepsview.StepsView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AddCarActivity extends AppCompatActivity {

    private EditText edtBarcode;
    private Button btnScanBarcode;
    private Button btnNext;
    private Button btnOK;

    protected ArrayList<JSONObject> feedDataList;

    private StepsView mStepsView;

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        USER_ID = getIntent().getStringExtra("USER_ID");

        appLog = new AppLogClass(this);

        setupWidgets();

        //edtBarcode.setText("03750528");

        // steps view
        String[] labels = {"Step 1", "Step 2", "Step 3", "Step 4"};
        mStepsView = (StepsView) findViewById(R.id.stepsView);
        mStepsView.setCompletedPosition(labels.length - 4)
                .setLabels(labels)
                .setBarColorIndicator(this.getResources().getColor(R.color.material_blue_grey_800))
                .setProgressColorIndicator(this.getResources().getColor(R.color.colorAccent))
                .setLabelColorIndicator(this.getResources().getColor(R.color.colorAccent))
                .drawView();
    }

    private void setupWidgets() {
        edtBarcode = (EditText) findViewById(R.id.edtBarcode);
        btnScanBarcode = (Button) findViewById(R.id.btnScanBarcode);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnOK = (Button) findViewById(R.id.btnOK);
        btnOK.setVisibility(View.GONE);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appLog.setLog("AddCarActivity", "กดปุ่ม OK", USER_ID);

                String _barcode = edtBarcode.getText().toString();

                if (isOnline()) {
                    new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_CHECK_BARCODE", "LYd162fYt", _barcode/*"03750528"*/);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appLog.setLog("AddCarActivity", "กดปุ่ม Scan Barcode", USER_ID);

                new IntentIntegrator(AddCarActivity.this).initiateScan();
            }
        });

        //btnNext.setVisibility(View.VISIBLE);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appLog.setLog("AddCarActivity", "กดปุ่ม Next", USER_ID);

                Intent intent = new Intent(getApplicationContext(), AddCarPictureActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                edtBarcode.setText(result.getContents());
                new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_CHECK_BARCODE", "LYd162fYt", result.getContents()/*"03750528"*/);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void dialogAlertScanBarcodeUsed() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(AddCarActivity.this);

        completeDialog.setTitle("บาร์โค๊ดนี้ถูกใช้ไปแล้ว!!");
        completeDialog.setMessage("กรุณาลองใหม่อีกครั้ง.");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();

            }
        });

        completeDialog.show();
    }

    public void dialogAlertScanBarcodeFail() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(AddCarActivity.this);

        completeDialog.setTitle("บาร์โค๊ดไม่ถูกต้อง!!");
        completeDialog.setMessage("กรุณาลองใหม่อีกครั้ง.");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();

            }
        });
        completeDialog.show();
    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        String _barcode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(AddCarActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(false);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                _barcode = params[2];

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
                        .add("BARCODE", params[2])
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

                            String strSTATUS = String.valueOf(feedDataList.get(i).getString("STATUS"));
                            String strIMEI = String.valueOf(feedDataList.get(i).getString("IMEI"));

                            if (strSTATUS.equals("READY")) {
                                appLog.setLog("AddCarActivity", "Scan Barcode " + _barcode + " (IMEI " + strIMEI + ") success.", USER_ID);

                                Intent intent = new Intent(getApplicationContext(), AddCarPictureActivity.class);
                                intent.putExtra("USER_ID", USER_ID);
                                intent.putExtra("IMEI", strIMEI);
                                startActivity(intent);
                            } else if (strSTATUS.equals("USED")) {

                                /*if (strIMEI.length() > 0) {
                                    appLog.setLog("AddCarActivity", "Scan Barcode " + _barcode + " (IMEI " + strIMEI + ") success.", USER_ID);

                                    Intent intent = new Intent(getApplicationContext(), AddCarPictureActivity.class);
                                    intent.putExtra("USER_ID", USER_ID);
                                    intent.putExtra("IMEI", strIMEI);
                                    startActivity(intent);
                                } else {
                                    appLog.setLog("AddCarActivity", "Barcode " + _barcode + " ถูกใช้ไปแล้ว", USER_ID);

                                    dialogAlertScanBarcodeUsed();
                                }*/

                                dialogAlertScanBarcodeUsed();

                            } else if (strSTATUS.equals("CLOSE")) {
                                appLog.setLog("AddCarActivity", "Barcode " + _barcode + " ไม่ถูกต้อง", USER_ID);

                                dialogAlertScanBarcodeFail();
                            } else {
                                appLog.setLog("AddCarActivity", "Barcode " + _barcode + " ไม่ถูกต้อง", USER_ID);

                                dialogAlertScanBarcodeFail();
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
