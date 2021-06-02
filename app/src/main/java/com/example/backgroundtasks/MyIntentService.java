package com.example.backgroundtasks;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    public MyIntentService() {
        super("MyIntentService");
    }

    private static void runService(Context context, int parameter) {
        Intent intent = new Intent(context, MyIntentService.class);

        intent.setAction(ACTION_EXERCISE1);
        intent.putExtra(PARAMETER1, parameter);

        context.startService(intent);
    }

    private void downloadFile(String stringUrl) {
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

            while (downloaded != -1) {
                fileOutputStream.write(buffer, 0, downloaded);
                
                downloaded = dataInputStream.read(buffer, 0, BLOCK_SIZE);
                Log.d("Downloading file:" + outFile.getName(), "");
            }
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

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
