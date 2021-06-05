package com.example.backgroundtasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private EditText urlAddress;
    private TextView fileSize;
    private TextView fileType;
    private TextView downloadedB;
    private Button getInfoButton;
    private Button downloadFileButton;
    private ProgressBar progressBar;

    private final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            DownloadProgress downloadProgress = bundle.getParcelable(MyIntentService.INFO);

            int downloadedBytes = downloadProgress.getDownloadedBytes();
            int totalSize = downloadProgress.getSize();
            progressBar.setMax(totalSize);

            if (downloadProgress.getStatus() == DownloadProgress.STATUS_IN_PROGRESS) {
                downloadedB.setText(String.format(Locale.getDefault(), "%d", downloadedBytes));
                progressBar.setProgress(downloadedBytes);
            }
            else if (downloadProgress.getStatus() == DownloadProgress.STATUS_FINISHED) {
                downloadedB.setText(String.format(Locale.getDefault(), "%d", downloadedBytes));
                progressBar.setProgress(totalSize);
                Toast.makeText(getApplicationContext(), R.string.download_completed, Toast.LENGTH_SHORT).show();
                downloadedB.setText(String.format(Locale.getDefault(), "%s", "completed"));
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // Register a broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(MyIntentService.RECEIVER));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister a broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlAddress = findViewById(R.id.inputURLaddress);

        fileSize = findViewById(R.id.fileSizeValue);
        fileType = findViewById(R.id.fileTypeValue);
        downloadedB = findViewById(R.id.downloadedBvalue);

        getInfoButton = findViewById(R.id.getInfoButton);
        downloadFileButton = findViewById(R.id.downloadFileButton);

        progressBar = findViewById(R.id.progressBar);

        getInfoButton.setOnClickListener(v -> {
            if (checkUrlAddressField(urlAddress)) {
                TaskGetInfo taskGetInfo = new TaskGetInfo();
                taskGetInfo.execute(urlAddress.getText().toString());
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.incorrect_url_address, Toast.LENGTH_SHORT).show();
            }
        });

        downloadFileButton.setOnClickListener(v -> {
            if (checkUrlAddressField(urlAddress)) {
                // There is permission so download can start
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    MyIntentService.runService(MainActivity.this, urlAddress.getText().toString());
                }
                // There is no permission
                else {
                    // User refused permission previously
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Log.e("download_permission", getString(R.string.no_permission_message));
                    }

                    // Ask for permissions
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
                }
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.incorrect_url_address, Toast.LENGTH_SHORT).show();
            }
        });

        if (savedInstanceState != null) {
            String savedUrlAddress = savedInstanceState.getString("url address");
            String savedFileSize = savedInstanceState.getString("file size");
            String savedFileType = savedInstanceState.getString("file type");
            String savedDownloadedB = savedInstanceState.getString("downloaded B");

            urlAddress.setText(savedUrlAddress);
            fileSize.setText(savedFileSize);
            fileType.setText(savedFileType);
            downloadedB.setText(savedDownloadedB);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        urlAddress = findViewById(R.id.inputURLaddress);
        fileSize = findViewById(R.id.fileSizeValue);
        fileType = findViewById(R.id.fileTypeValue);
        downloadedB = findViewById(R.id.downloadedBvalue);
        progressBar = findViewById(R.id.progressBar);

        outState.putString("url address", urlAddress.getText().toString());
        outState.putString("file size", fileSize.getText().toString());
        outState.putString("file type", fileType.getText().toString());
        outState.putString("downloaded B", downloadedB.getText().toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // There is permission so download can start
        if (requestCode == WRITE_EXTERNAL_STORAGE_CODE) {
            if (permissions.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MyIntentService.runService(MainActivity.this, urlAddress.getText().toString());
            }
            // There is no permission
            else {
                Toast.makeText(getApplicationContext(), R.string.no_permission_message_2, Toast.LENGTH_SHORT).show();
            }
        }
        // Unknown requestCode
        else {
            Log.e("intent_service", getString(R.string.unknown_reguest_code));
        }
    }

    public boolean checkUrlAddressField(EditText editText) {
        return !isEmpty(editText) && startWithHttps(editText);
    }

    public boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    public boolean startWithHttps(EditText editText) {
        return editText.getText().toString().startsWith("https://");
    }

    public class TaskGetInfo extends AsyncTask<String, Void, FileInfo> {

        @Override
        protected FileInfo doInBackground(String... strings) {

            HttpsURLConnection httpsURLConnection = null;
            FileInfo fileInfo = null;

            try {
                URL url = new URL(strings[0]);

                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("GET");

                fileInfo = new FileInfo();

                fileInfo.setFileSize(httpsURLConnection.getContentLength());
                fileInfo.setFileType(httpsURLConnection.getContentType());
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                if (httpsURLConnection != null)
                    httpsURLConnection.disconnect();
            }

            return fileInfo;
        }

        @Override
        protected void onPostExecute(FileInfo fileInfo) {
            super.onPostExecute(fileInfo);

            if (fileInfo != null) {
                fileSize.setText(String.format(Locale.getDefault(), "%d", fileInfo.getFileSize()));
                fileType.setText(fileInfo.getFileType());
            }
        }
    }
}