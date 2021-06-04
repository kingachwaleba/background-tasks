package com.example.backgroundtasks;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MyIntentService extends IntentService {

    public static final String ACTION_EXERCISE1 = "com.example.intent_service.action.exercise1";
    public static final String PARAMETER1 = "com.example.intent_service.extra.parameter1";
    public static final String CHANNEL_ID = "com.example.intent_service.extra.channel_id";
    public static final String RECEIVER = "com.example.intent_service.extra.receiver";
    public static final String INFO = "info";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private Boolean ifDownloaded = false;
    private DownloadProgress downloadProgress = new DownloadProgress();

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
            int totalDownloaded = 0;

            downloadProgress.setSize(httpsURLConnection.getContentLength());
            downloadProgress.setDownloadedBytes(totalDownloaded);
            downloadProgress.setStatus(DownloadProgress.STATUS_IN_PROGRESS);
            sendBroadcast(downloadProgress);

            while (downloaded != -1) {
                fileOutputStream.write(buffer, 0, downloaded);

                totalDownloaded += downloaded;
                downloadProgress.setDownloadedBytes(totalDownloaded);
                sendBroadcast(downloadProgress);
                notificationManager.notify(NOTIFICATION_ID, createNotification(downloadProgress.getSize(), downloadProgress.getDownloadedBytes(), "Download file"));

                downloaded = dataInputStream.read(buffer, 0, BLOCK_SIZE);

                Log.d("Download file:" + outFile.getName(), totalDownloaded + " bytes.");
            }

            downloadProgress.setStatus(DownloadProgress.STATUS_FINISHED);
            sendBroadcast(downloadProgress);
            notificationManager.notify(NOTIFICATION_ID, createNotification(downloadProgress.getSize(), downloadProgress.getDownloadedBytes(), "Download finished"));

            Log.d("Downloaded file:" + outFile.getName(), totalDownloaded + " bytes.");

            ifDownloaded = true;

        } catch (Exception exception) {
            downloadProgress.setStatus(DownloadProgress.STATUS_ERROR);
            sendBroadcast(downloadProgress);

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

    public Notification createNotification(int max, int progress, String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        
        // Build the stack with notifications, which a user expects after return
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent =
                taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        Notification.Builder notificationBuilder = new Notification.Builder(this, CHANNEL_ID);
        notificationBuilder.setContentTitle(text)
                .setProgress(max, progress, false)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_2_foreground)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH);

        // If download still lasts
        notificationBuilder.setOngoing(!ifDownloaded);

        // Set the notification channel for the created notification
        notificationBuilder.setChannelId(CHANNEL_ID);

        // Create and return the notification
        return notificationBuilder.build();
    }

    public void sendBroadcast(DownloadProgress downloadProgress) {
        Intent intent = new Intent(RECEIVER);
        intent.putExtra(INFO, downloadProgress);

        // Send a message
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prepareNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(100, 0, "Download file"));

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
