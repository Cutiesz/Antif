package com.korsolution.antif;

import android.app.Application;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.pixplicity.easyprefs.library.Prefs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by codemobiles2 on 2/17/2016 AD.
 */
public class MainApplication extends Application {

    public boolean mIsActivityRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        getKeyHash();
        initPreference();
    }

    private void initPreference(){
        // Initialize the Prefs class
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

    private void getKeyHash(){
        try {
            String PACKAGE_NAME = getApplicationContext().getPackageName();

            PackageInfo info = getPackageManager().getPackageInfo(PACKAGE_NAME,
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

}