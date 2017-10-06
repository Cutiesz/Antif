package com.korsolution.antif;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Kontin58 on 26/8/2559.
 */
public class PopupMessageService extends Service {

    private static final String TAG = PopupMessageService.class.getSimpleName();
    WindowManager mWindowManager;
    View mView;
    Animation mAnimation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Popup Message Service Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        registerOverlayReceiver();

        try {
            String message = intent.getExtras().getString("MSG").toString();
            if (message != null || !message.isEmpty()) {
                showPopupMessage(message);
            }
        }catch (Exception e) {

        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void showPopupMessage(final String _message) {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mView = View.inflate(getApplicationContext(), R.layout.dialog_alert_message, null);

        int top = getApplicationContext().getResources().getDisplayMetrics().heightPixels / 2;

        TextView txtMessage = (TextView) mView.findViewById(R.id.txtMessage);
        Button btnView = (Button) mView.findViewById(R.id.btnView);
        Button btnClose = (Button) mView.findViewById(R.id.btnClose);

        txtMessage.setText(_message);

        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideDialog();

                // intent after click button
                Intent intent = new Intent(getApplicationContext(), NaviDrawerActivity.class);
                //Intent intent = new Intent(getApplicationContext(), MainAntifActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("MSG", _message);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
                try {
                    pendingIntent.send(getApplicationContext(), 0, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

                // Dismiss Notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(0 /* Request code */);

            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideDialog();

            }
        });

        final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON ,
                PixelFormat.RGBA_8888);

        // Define the position of the window within the screen
        mLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        mLayoutParams.x = top;
        mLayoutParams.y = top;

        mView.setVisibility(View.VISIBLE);
        //mAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in);
        //mView.startAnimation(mAnimation);
        mWindowManager.addView(mView, mLayoutParams);
    }

    private void hideDialog(){
        if(mView != null && mWindowManager != null){
            mWindowManager.removeView(mView);
            mView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterOverlayReceiver();
    }

    private void registerOverlayReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(popupMessageReceiver, filter);
    }

    private void unregisterOverlayReceiver() {
        hideDialog();
        unregisterReceiver(popupMessageReceiver);
    }

    private BroadcastReceiver popupMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "[onReceive]" + action);
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                //showDialog("Esto es una prueba y se levanto desde");
            }
            else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                hideDialog();
            }
            else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                hideDialog();
            }
        }
    };
}
