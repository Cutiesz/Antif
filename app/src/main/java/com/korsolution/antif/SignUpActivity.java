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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private TextView txtSignUpFail;
    private Button btnSignUp;

    protected ArrayList<JSONObject> feedDataList;

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        appLog = new AppLogClass(this);

        setupWidgets();
    }

    private void setupWidgets() {
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtConfirmPassword = (EditText) findViewById(R.id.edtConfirmPassword);
        txtSignUpFail = (TextView) findViewById(R.id.txtSignUpFail);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String _email = edtEmail.getText().toString();
                String _password = edtPassword.getText().toString();
                String _confirmPass = edtConfirmPassword.getText().toString();

                if (_email.length() > 0) {
                    if (_password.length() > 0) {
                        if (_confirmPass.length() > 0) {

                            if (_password.equals(_confirmPass)) {

                                gpsTracker = new GPSTracker(SignUpActivity.this);
                                if (gpsTracker.canGetLocation()) {

                                    mylat = gpsTracker.getLatitude();
                                    mylng = gpsTracker.getLongitude();

                                    LatLng currentlocation = new LatLng(mylat, mylng);

                                    if (isOnline()) {
                                        appLog.setLog("SignUpActivity", "กดปุ่ม Sign up (" + _email + ")", "");

                                        new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/L_REGISTER", "LYd162fYt", _email, _password, "Android", "EM", mylat.toString(), mylng.toString());
                                    } else {
                                        txtSignUpFail.setVisibility(View.VISIBLE);
                                        txtSignUpFail.setText("No Internet signal, Please try agian!!");
                                    }

                                } else {
                                    gpsTracker.showSettingsAlert();
                                }

                            } else {
                                txtSignUpFail.setVisibility(View.VISIBLE);
                                txtSignUpFail.setText("Passwords do not match!!");
                            }

                        } else {
                            txtSignUpFail.setVisibility(View.VISIBLE);
                            txtSignUpFail.setText("Please Enter Confirm Password!!");
                        }
                    } else {
                        txtSignUpFail.setVisibility(View.VISIBLE);
                        txtSignUpFail.setText("Please Enter Password!!");
                    }
                } else {
                    txtSignUpFail.setVisibility(View.VISIBLE);
                    txtSignUpFail.setText("Please Enter E-mail!!");
                }

            }
        });
    }

    public void dialogAlertSignUpSuccess() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(SignUpActivity.this);

        completeDialog.setTitle("Sign up successfully.");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_accept);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

            }
        });
        completeDialog.show();
    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        String _email;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(SignUpActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                _email = params[2];

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
                        .add("EMAIL", params[2])
                        .add("PASSWORD", params[3])
                        .add("USE_DEVICE", params[4])
                        .add("LOGIN_TYPE", params[5])
                        .add("LATITUDE", params[6])
                        .add("LONGITUDE", params[7])
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

                            if (!strSTATUS.equals("Fail")) {

                                appLog.setLog("SignUpActivity", "Sign up (" + _email + ")", "");

                                dialogAlertSignUpSuccess();
                            } else {
                                txtSignUpFail.setVisibility(View.VISIBLE);
                                txtSignUpFail.setText("Sign up fail! please try agian.");
                            }


                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                    txtSignUpFail.setVisibility(View.VISIBLE);
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
                txtSignUpFail.setVisibility(View.VISIBLE);
            }

            nDialog.dismiss();
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

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.putExtra("log_out", "true");
            startActivity(intent);
        }
        return false;
    }
}
