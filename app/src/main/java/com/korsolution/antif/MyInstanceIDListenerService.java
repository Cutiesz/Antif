/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.korsolution.antif;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "MyInstanceIDLS";

    protected ArrayList<JSONObject> feedDataList;
    private AccountDBClass AccountDB;
    private TokenDBClass TokenDB;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);

        // Subscribe to topic channels
        try {
            subscribeTopics(refreshedToken);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TokenDB = new TokenDBClass(MyInstanceIDListenerService.this);
        TokenDB.Delete();
        TokenDB.Insert(refreshedToken);
    }

    private void sendRegistrationToServer(final String token) {

        // Restore Preference
        /*SharedPreferences userDetails = getApplicationContext().getSharedPreferences("email", MODE_PRIVATE);
        String email = userDetails.getString("email_login", "");*/

        //String email = "cetiesz@gmail.com";
        /*Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                email = account.name;
            }
        }*/

        //String url = String.format("http://192.168.1.47/push_token/index.php?username=%s&device_token=%s",email, token);
        //new CMGcmRegistrationIntentService.SendTokenTask().execute(url);

        String modelName = getDeviceName();

        AccountDB = new AccountDBClass(MyInstanceIDListenerService.this);
        String[][] arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            String _USER_ID = arrData[0][1].toString();

            new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_TOKENS", "LYd162fYt", token, _USER_ID/*"3"*/, modelName, "ANDROID");
        }

    }

    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        /*GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }*/

        // Non-blocking methods. No need to use AsyncTask or background thread.
        FirebaseMessaging.getInstance().subscribeToTopic("mytopic");
        //FirebaseMessaging.getInstance().unsubscribeToTopic("mytopic");
    }
    // [END subscribe_topics]


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
                                Toast.makeText(MyInstanceIDListenerService.this, "Token registration : Success.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MyInstanceIDListenerService.this, "Token registration : Fail!!", Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(MyInstanceIDListenerService.this, "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(MyInstanceIDListenerService.this, "Fail!!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
