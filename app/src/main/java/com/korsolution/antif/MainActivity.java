package com.korsolution.antif;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ImageView splashImageView;

    private AccountDBClass AccountDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        AccountDB = new AccountDBClass(this);

        String[][] arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            String USER_ID = arrData[0][1].toString();
            String TEL = arrData[0][8].toString();

            AppLogClass appLog = new AppLogClass(MainActivity.this);
            appLog.setLog("MainActivity", "เข้า Main Page App", USER_ID);
        }

        runSplashPage(3000);
        load();
    }

    private void runSplashPage(int i) {
        splashImageView = (ImageView) findViewById(R.id.splashImageView);
        splashImageView.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashImageView.setVisibility(View.GONE);
            }
        }, i);
    }

    private void load() {

        Timer t = new Timer();

        t.schedule(new TimerTask() {
            @Override
            public void run() {

                // check log in
                String[][] arrData = AccountDB.SelectAllAccount();
                if (arrData != null) {
                    String USER_ID = arrData[0][1].toString();
                    String TEL = arrData[0][8].toString();

                    //AppLogClass appLog = new AppLogClass(MainActivity.this);
                    //appLog.setLog("MainActivity", "เข้า Main Page App", USER_ID);

                    //Intent intent = new Intent(getApplicationContext(), MainPageActivity.class);
                    //Intent intent = new Intent(getApplicationContext(), MainAntifActivity.class);
                    //intent.putExtra("USER_ID", USER_ID);
                    //startActivity(intent);

                    if (!TEL.equals("")) {
                        //Intent intent = new Intent(getApplicationContext(), MainAntifActivity.class);
                        Intent intent = new Intent(getApplicationContext(), NaviDrawerActivity.class);
                        intent.putExtra("USER_ID", USER_ID);
                        //startActivity(intent);

                        Bundle bundle = getIntent().getExtras();
                        if (bundle != null) {
                            String tmp = "";
                            for (String key : bundle.keySet()) {
                                Object value = bundle.get(key);
                                tmp += key + ": " + value + "\n\n";
                            }
                            //txtFeedNews.setText(tmp);

                            String MSG = bundle.getString("MSG");
                            if (MSG != null) {
                                //txtFeedNews.setText(picture_url);
                                if (MSG.length() > 1) {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtras(bundle);
                                    //startActivity(intent);
                                }
                            }
                        }
                        startActivity(intent);

                    } else {
                        Intent intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
                        intent.putExtra("USER_ID", USER_ID);
                        startActivity(intent);
                    }

                } else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }

            }
        },3000);
    }
}
