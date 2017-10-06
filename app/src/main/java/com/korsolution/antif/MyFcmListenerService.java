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

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class MyFcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "CMGcmListenerService";


    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message){
        String from = message.getFrom();
        //Map data = message.getData();

        RemoteMessage.Notification notification = message.getNotification();
        Map<String, String> data = message.getData();
        Log.e("FROM", message.getFrom());
        //sendNotification(notification, data);

        String textTitle = message.getNotification().getTitle();
        String textMessage = message.getNotification().getBody();

        boolean isActivityRunning = ((MainApplication) getApplication()).mIsActivityRunning;
        if (isActivityRunning == true) {
            Intent receivedNewMsgComplete = new Intent(FireBasePreferences.RECEIVED_NEW_MESSAGE);
            receivedNewMsgComplete.putExtra("MSG", textMessage);
            LocalBroadcastManager.getInstance(this).sendBroadcast(receivedNewMsgComplete);
        }else{
            //sendNotification(textTitle, textMessage);
            sendNotification(notification, data);

            KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if( myKM.inKeyguardRestrictedInputMode()) {
                //it is locked

                Intent mServiceIntent = new Intent(this, PopupMessageService.class);
                mServiceIntent.putExtra("MSG", textMessage);
                startService(mServiceIntent);

            } else {
                //it is not locked

            }
        }


    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */

    private void sendNotification(String title, String message) {

        Intent intent = new Intent(this, NaviDrawerActivity.class);
        //Intent intent = new Intent(this, MainAntifActivity.class);
        //Intent intent = new Intent(this, ReceiveMassageActivity.class);
        //Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("MSG", message);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
/*
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
*/
        //notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

        Notification notification = new NotificationCompat.Builder(this)
                .setCategory(Notification.CATEGORY_PROMO)
                .setContentTitle(title)
                .setContentText(message)
                .setContentInfo(title)
                //.setSmallIcon(R.drawable.ic_stat_ic_notification)
                //.setWhen(1000)
                .setShowWhen(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setVisibility(1)
                //.addAction(android.R.drawable.ic_menu_view, "View details", pendingIntent)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
        //adding LED lights to notification
        notification.defaults |= Notification.DEFAULT_LIGHTS;
    }

    /*public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) ctx
                .getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager
                .getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if (componentInfo.getPackageName().equals(myPackage))
            return true;
        return false;
    }*/

    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Intent intent = new Intent(this, NaviDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle bundle = new Bundle();
        bundle.putString("MSG", data.get("MSG"));
        intent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.win))
                .setContentIntent(pendingIntent)
                //.setContentInfo("Hello")
                .setLargeIcon(icon)
                .setColor(Color.RED)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_launcher);

        try {
            String picture_url = data.get("picture_url");
            if (picture_url != null && !"".equals(picture_url)) {
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.getBody())
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
