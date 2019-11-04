package com.example.timetracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final  String CHANNEL_ID = "timerServiceChannel";
    public static NotificationManager manager;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel(){

        // API 레벨 26 이상은 노티피케이션을 설정해 주려면 노피티케이션 채널을 추가해줘야함.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
              CHANNEL_ID,
              "Timer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }

    }
}
