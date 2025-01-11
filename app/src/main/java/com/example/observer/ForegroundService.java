package com.example.observer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ForegroundService extends Service {
    public static DatabaseReference reff2;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (MainActivity.SearchingOn == true){
                            Log.e("Service", "Service is running...");
                            MainActivity.rootNode = FirebaseDatabase.getInstance();
                            //fingerprint or regisrter user:
                            reff2 = MainActivity.rootNode.getReference("Devices"+ "/"+ Build.BRAND+" "+Build.DEVICE+ "/"+Calendar.getInstance().getTime());
                           // MainActivity.reff.push().setValue(MainActivity.locData);
                            reff2.setValue(MainActivity.locData);

                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();
        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                .setContentText("Service is running")
                .setContentTitle("Service enabled")
                .setSmallIcon(R.drawable.ic_launcher_background);
        startForeground(1001,notification.build());
        return super.onStartCommand(intent, flags, startId);
    }
}
