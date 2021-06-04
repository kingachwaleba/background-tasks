package com.example.backgroundtasks;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MyIntentService extends IntentService {

    private static final String ACTION_EXERCISE1 = "com.example.intent_service.action.exercise1";
    private static final String PARAMETER1 = "com.example.intent_service.extra.parameter1";
    private static final String CHANNEL_ID = "com.example.intent_service.extra.channel_id";
    private static final String RECEIVER = "com.example.intent_service.extra.receiver";
    private static final String INFO = "info";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private Boolean idDownloaded = false;

    public MyIntentService() {
        super("MyIntentService");
    }

    public static void runService(Context context, String parameter) {
        Intent intent = new Intent(context, MyIntentService.class);

        intent.setAction(ACTION_EXERCISE1);
        intent.putExtra(PARAMETER1, parameter);

        context.startService(intent);
    }

    public void downloadFile(String stringUrl) {
        FileOutputStream fileOutputStream = null;
        HttpsURLConnection httpsURLConnection = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(stringUrl);

            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestMethod("GET");

            File temporaryFile = new File(url.getFile());
            File outFile =
                    new File(Environment.getExternalStorageDirectory()
                            + File.separator + temporaryFile.getName());

            if (outFile.exists())
                outFile.delete();

            DataInputStream dataInputStream = new DataInputStream(httpsURLConnection.getInputStream());
            fileOutputStream = new FileOutputStream(outFile.getPath());
            int BLOCK_SIZE = 1024 * 1024 * 5;
            byte[] buffer = new byte[BLOCK_SIZE];

            int downloaded = dataInputStream.read(buffer, 0, BLOCK_SIZE);
            int totalDownloaded = downloaded;
            while (downloaded != -1) {
                fileOutputStream.write(buffer, 0, downloaded);

                downloaded = dataInputStream.read(buffer, 0, BLOCK_SIZE);
                totalDownloaded += downloaded;
                Log.d("Downloading file:" + outFile.getName(), Integer.toString(totalDownloaded));
            }

            idDownloaded = true;

        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            if (httpsURLConnection != null)
                httpsURLConnection.disconnect();
        }
    }

    public void prepareNotificationChannel() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        CharSequence name = getString(R.string.app_name);

        NotificationChannel notificationChannel =
                new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);

        notificationManager.createNotificationChannel(notificationChannel);
    }

    public Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        // notificationIntent.putExtra();

        // Build the stack with notifications, which a user expects after return
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent =
                taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        Notification.Builder notificationBuilder = new Notification.Builder(this, CHANNEL_ID);
        notificationBuilder.setContentTitle(getString(R.string.content_title))
                .setProgress(100, 10, false)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_2_foreground)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH);

        // If downloading still lasts
        notificationBuilder.setOngoing(idDownloaded);

        // Set the notification channel for the created notification
        notificationBuilder.setChannelId(CHANNEL_ID);

        // Create and return the notification
        return notificationBuilder.build();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prepareNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_EXERCISE1.equals(action)) {
                final String url = intent.getStringExtra(PARAMETER1);

                downloadFile(url);
            }
            else {
                Log.e("intent_service", "unknown action");
            }
        }

        Log.e("intent_service", "service did a task");
    }
}
