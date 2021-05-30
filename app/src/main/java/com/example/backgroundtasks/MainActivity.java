package com.example.backgroundtasks;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private EditText urlAddress;
    private TextView fileSize;
    private TextView fileType;
    private TextView downloadedB;
    private Button getInfoButton;
    private Button downloadFileButton;

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

        getInfoButton.setOnClickListener(v -> {
            TaskGetInfo taskGetInfo = new TaskGetInfo();
            taskGetInfo.execute(urlAddress.getText().toString());
        });
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
                fileSize.setText(fileInfo.getFileSize());
                fileType.setText(fileInfo.getFileType());
            }
        }
    }
}