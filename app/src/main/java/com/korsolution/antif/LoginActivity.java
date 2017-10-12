package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView txtLogInFail;
    private TextView txtSignUp;

    protected ArrayList<JSONObject> feedDataList;
    private AccountDBClass AccountDB;
    private TokenDBClass TokenDB;

    // Facebook
    private LoginButton loginButton;
    private CallbackManager callbackManager; // result call-back
    private AccessTokenTracker mAccessTokenTracker; // permission change tracking
    private ProfileTracker mProfileTracker; // profile change tracking

    // Google Sign in
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;

    private String log_out;
    private LoginManager loginManager;

    private GPSTracker gpsTracker;
    private Double mylat;
    private Double mylng;

    private String modelName = "Android";

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        getSupportActionBar().hide();

        AccountDB = new AccountDBClass(this);
        AccountDB.getWritableDatabase();
        TokenDB = new TokenDBClass(this);

        log_out = getIntent().getStringExtra("log_out");

        appLog = new AppLogClass(this);

        setupWidgets();

        //User : noom_gtt@hotmail.com  Pass : 123456
        //User : test03@kontin.net  Pass : 1234
        //User : test02@kontin.net  Pass : 1234
        //edtEmail.setText("noom_gtt@hotmail.com");
        //edtPassword.setText("123456");
        //login("test01@kontin.net", "", "FB");

        // Facebook Login Button
        loginButton = (LoginButton) findViewById(R.id.login_button);
        // set up facebook log in
        setupFacebookLogin();

        //Google Sign in

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.btnLogout).setOnClickListener(this);

        // check log out facebook & google
        if (log_out != null) {
            // Log out Facebook
            loginManager.getInstance().logOut();

            // Log out Google
            //signOut();
        }

        /*

        Log in Type {"EM", "FB", "GM"}

         */

        gpsTracker = new GPSTracker(LoginActivity.this);
        if (gpsTracker.canGetLocation()) {

            mylat = gpsTracker.getLatitude();
            mylng = gpsTracker.getLongitude();

            LatLng currentlocation = new LatLng(mylat, mylng);

        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    private void login(String _email, String _password, String _loginType) {

        gpsTracker = new GPSTracker(LoginActivity.this);
        if (gpsTracker.canGetLocation()) {

            mylat = gpsTracker.getLatitude();
            mylng = gpsTracker.getLongitude();

            modelName = getDeviceName();

            String _lat = String.valueOf(mylat);
            String _lng = String.valueOf(mylng);
            new FeedAsynTaskRegister().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_REGISTER", "LYd162fYt", _email, _password, modelName, _loginType, _lat, _lng);

        } else {
            loginManager.getInstance().logOut();
            gpsTracker.showSettingsAlert();
        }

        //new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_LOGIN", "LYd162fYt", _email, _password, _loginType);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }
    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void setupWidgets() {
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtLogInFail = (TextView) findViewById(R.id.txtLogInFail);
        txtSignUp = (TextView) findViewById(R.id.txtSignUp);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String _email = edtEmail.getText().toString();
                String _password = edtPassword.getText().toString();

                if (isOnline()) {
                    if (_email.length() > 0) {
                        if (_password.length() > 0) {

                            gpsTracker = new GPSTracker(LoginActivity.this);
                            if (gpsTracker.canGetLocation()) {

                                mylat = gpsTracker.getLatitude();
                                mylng = gpsTracker.getLongitude();

                                String _lat = String.valueOf(mylat);
                                String _lng = String.valueOf(mylng);

                                appLog.setLog("LoginActivity", "กดปุ่ม Log in (EM)", "");

                                login(_email, _password, "EM");

                            } else {
                                gpsTracker.showSettingsAlert();
                            }
                        } else {
                            txtLogInFail.setVisibility(View.VISIBLE);
                            txtLogInFail.setText("Please Enter Password!!");
                        }
                    } else {
                        txtLogInFail.setVisibility(View.VISIBLE);
                        txtLogInFail.setText("Please Enter Username!!");
                    }
                } else {
                    txtLogInFail.setVisibility(View.VISIBLE);
                    txtLogInFail.setText("No Internet signal, Please try agian!!");
                }

            }
        });

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appLog.setLog("LoginActivity", "กดปุ่ม Sign up", "");

                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                intent.putExtra("LoginType", "EM");
                startActivity(intent);
            }
        });
    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        String _type;
        String _pass;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(LoginActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                _type = params[4];
                _pass = params[3];

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
                        .add("LOGIN_TYPE", params[4])
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

                AccountDB.DeleteAccount();

                feedDataList = CuteFeedJsonUtil.feed(s);
                if (feedDataList != null) {
                    for (int i = 0; i <= feedDataList.size(); i++) {
                        try {

                            String strUSER_ID = String.valueOf(feedDataList.get(i).getString("USER_ID"));
                            String strPERMISSION_ID = String.valueOf(feedDataList.get(i).getString("PERMISSION_ID"));
                            String strEMAIL = String.valueOf(feedDataList.get(i).getString("EMAIL"));
                            String strPASSWORD = String.valueOf(feedDataList.get(i).getString("PASSWORD"));
                            String strDISPLAY_NAME = String.valueOf(feedDataList.get(i).getString("DISPLAY_NAME"));
                            String strFIRST_NAME = String.valueOf(feedDataList.get(i).getString("FIRST_NAME"));
                            String strLAST_NAME = String.valueOf(feedDataList.get(i).getString("LAST_NAME"));
                            String strTEL = String.valueOf(feedDataList.get(i).getString("TEL"));
                            String strLOGIN_TYPE = String.valueOf(feedDataList.get(i).getString("LOGIN_TYPE"));
                            String strID_CARD = String.valueOf(feedDataList.get(i).getString("ID_CARD"));
                            String strLICENSE_EXP = String.valueOf(feedDataList.get(i).getString("LICENSE_EXP"));
                            String strUSER_PICTURE = String.valueOf(feedDataList.get(i).getString("USER_PICTURE"));
                            String strADDRESS = String.valueOf(feedDataList.get(i).getString("ADDRESS"));
                            String District_ID = String.valueOf(feedDataList.get(i).getString("District_ID"));
                            String strDistrict_Name = String.valueOf(feedDataList.get(i).getString("District_Name"));
                            String strAmphur_ID = String.valueOf(feedDataList.get(i).getString("Amphur_ID"));
                            String strAmphur_Name = String.valueOf(feedDataList.get(i).getString("Amphur_Name"));
                            String strProvince_ID = String.valueOf(feedDataList.get(i).getString("Province_ID"));
                            String strProvince_Name = String.valueOf(feedDataList.get(i).getString("Province_Name"));
                            String strPOSTCODE = String.valueOf(feedDataList.get(i).getString("POSTCODE"));
                            String strLATITUDE_ADDRESS = String.valueOf(feedDataList.get(i).getString("LATITUDE_ADDRESS"));
                            String strLONGITUDE_ADDRESS = String.valueOf(feedDataList.get(i).getString("LONGITUDE_ADDRESS"));

                            AccountDB.InsertAccount(strUSER_ID, strPERMISSION_ID, strEMAIL, strPASSWORD, strDISPLAY_NAME,
                                    strFIRST_NAME, strLAST_NAME, strTEL, strLOGIN_TYPE, strID_CARD, strLICENSE_EXP, strUSER_PICTURE,
                                    strADDRESS, District_ID, strDistrict_Name, strAmphur_ID, strAmphur_Name,
                                    strProvince_ID, strProvince_Name, strPOSTCODE, strLATITUDE_ADDRESS, strLONGITUDE_ADDRESS);

                            String modelName = getDeviceName();

                            String[][] arrToken = TokenDB.SelectAll();
                            if (arrToken != null) {
                                String _token = arrToken[0][1].toString();

                                //register Token with my server
                                new TokenAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_TOKENS", "LYd162fYt", _token, strUSER_ID, modelName, "ANDROID");
                            }

                            appLog.setLog("LoginActivity", "Log in (" + _type + ")", strUSER_ID);

                            //if (!_pass.equals("")) {
                            if (!strTEL.equals("")) {
                                //Intent intent = new Intent(getApplicationContext(), MainAntifActivity.class);
                                Intent intent = new Intent(getApplicationContext(), NaviDrawerActivity.class);
                                intent.putExtra("USER_ID", strUSER_ID);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
                                intent.putExtra("USER_ID", strUSER_ID);
                                startActivity(intent);
                            }


                        } catch (Exception e) {

                        }
                    }

                } else {
                    //Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                    txtLogInFail.setVisibility(View.INVISIBLE);
                    txtLogInFail.setText("Log in fail!!");
                }

            } else {
                //Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
                txtLogInFail.setVisibility(View.INVISIBLE);
                txtLogInFail.setText("Log in fail!!");
            }

            nDialog.dismiss();
        }
    }

    public class TokenAsynTask extends AsyncTask<String, Void, String> {

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
                        .add("TOKEN_CODE", params[2])
                        .add("USER_ID", params[3])
                        .add("DEVICE", params[4])
                        .add("PLATEFORM", params[5])
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

                            if (strSTATUS.equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Token registration : Success.", Toast.LENGTH_LONG).show();



                            } else {
                                Toast.makeText(getApplicationContext(), "Token registration : Fail!!", Toast.LENGTH_LONG).show();
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

    public class FeedAsynTaskRegister extends AsyncTask<String, Void, String> {

        String email;
        String pass;
        String type;

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(LoginActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                email = params[2];
                pass = params[3];
                type = params[5];

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

                                appLog.setLog("LoginActivity", "Log in (" + type + ")", strSTATUS);

                                new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_LOGIN", "LYd162fYt", email, pass, type);

                            } else {
                                txtLogInFail.setVisibility(View.INVISIBLE);
                                txtLogInFail.setText("Log in fail!!");
                            }

                        } catch (Exception e) {

                        }
                    }

                } else {
                    //Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                    txtLogInFail.setVisibility(View.INVISIBLE);
                    txtLogInFail.setText("Log in fail!!");
                }

            } else {
                //Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
                txtLogInFail.setVisibility(View.INVISIBLE);
                txtLogInFail.setText("Log in fail!!");
            }

            nDialog.dismiss();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            System.out.println("KEYCODE_BACK");

            moveTaskToBack(true);
        }
        return false;
    }

    /*
     * Facebook Utility
     */
    private void setupFacebookLogin() {

        loginButton.setReadPermissions("public_profile, email");


        // Tracking token when login state changed
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    Log.i("codemobiles", "FB login: log-outed");
                    //nameTextView.setText("Facebook Profile");
                    //emailTextView.setText("Facebook Email");

                    //imgLogo.setImageDrawable(getResources().getDrawable(R.drawable.com_facebook_profile_picture_blank_square));

                }
            }
        };

        // Tracking when profile changed
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, final Profile currentProfile) {

                if (currentProfile != null) {
                    updateProfile();
                }
            }
        };



        // create callback Manager
        callbackManager = CallbackManager.Factory.create();

        // register call-back
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                appLog.setLog("LoginActivity", "กดปุ่ม Log in (FB)", "");

                updateProfile();
                showFBEmail(loginResult, txtSignUp);
                Log.i("codemobiles", "FB login succecceed");

            }

            @Override
            public void onCancel() {
                // called when cancelling login
                Log.i("codemobiles", "FB login cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("codemobiles", "FB login error due to" + error.getMessage());

                txtLogInFail.setVisibility(View.VISIBLE);
                txtLogInFail.setText("Facebook Log in fail!!");
            }
        });
    }

    public void showFBEmail(LoginResult loginResult,final TextView emailTextView){

        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String email = object.getString("email");
                            Prefs.putString("email", email);
                            emailTextView.setText(email);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email, gender, birthday, location"); // Parámetros que pedimos a facebook
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void updateProfile() {
        Profile profile = Profile.getCurrentProfile();

        if (profile != null) {
            txtLogInFail.setText(constructWelcomeMessage(profile));
            //txtSignUp.setText(Prefs.getString("email",""));
            Uri imageUri = profile.getProfilePictureUri(100, 100);

            String firstName = profile.getFirstName();
            String lastName = profile.getLastName();
            String middleName = profile.getMiddleName();
            Prefs.putString("firstName", firstName);
            Prefs.putString("lastName", lastName);
            Prefs.putString("middleName", middleName);

            String strImgProfile = imageUri.toString();
            Prefs.putString("uriImgProfile", strImgProfile);

            String _email = Prefs.getString("email","");

            /*Glide.with(this).load(imageUri)
                    .bitmapTransform(new CropCircleTransformation(this))
                    .into(imgLogo);*/

            login(_email, "", "FB");

        }
    }

    private String constructWelcomeMessage(Profile profile) {
        StringBuffer stringBuffer = new StringBuffer();
        if (profile != null) {
            stringBuffer.append("Welcome " + profile.getName());
        }else {
            stringBuffer.append("Hello World!");
        }
        return stringBuffer.toString();
    }

    /**
     * For handling when rotation and update profile
     */
    @Override
    protected void onResume() {
        super.onResume();
        // start tracking
        mAccessTokenTracker.startTracking();
        mProfileTracker.startTracking();

        updateProfile();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mAccessTokenTracker.stopTracking();
        mProfileTracker.stopTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // facebook
        callbackManager.onActivityResult(requestCode,resultCode, data);

        // google
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /*
     * Google Sign in
     */

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in_button:

                appLog.setLog("LoginActivity", "กดปุ่ม Log in (GM)", "");

                signIn();

                break;
            case R.id.btnLogout:

                signOut();

                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // do something
                        //txtSignUp.setText("Forgot password?");
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            //txtSignUp.setText(getString(R.string.signed_in_fmt, acct.getDisplayName() + acct.getId() + acct.getEmail()));      // acct.getEmail()

            /*
                Log in Google
             */
            // do something

            String _email = acct.getEmail();
            Prefs.putString("email", _email);

            gpsTracker = new GPSTracker(LoginActivity.this);
            if (gpsTracker.canGetLocation()) {

                mylat = gpsTracker.getLatitude();
                mylng = gpsTracker.getLongitude();

                modelName = getDeviceName();

                String _lat = String.valueOf(mylat);
                String _lng = String.valueOf(mylng);

                login(_email, "", "GM");

            } else {
                loginManager.getInstance().logOut();
                gpsTracker.showSettingsAlert();
            }

        } else {
            // Signed out, show unauthenticated UI.
            // updateUI(false);

            txtLogInFail.setVisibility(View.VISIBLE);
            txtLogInFail.setText("Google Log in fail!!");
        }
    }
}
