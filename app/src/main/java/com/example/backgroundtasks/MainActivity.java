package com.example.backgroundtasks;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    }

    public class TaskGetInfo extends AsyncTask<String, Void, FileInfo> {

        @Override
        protected FileInfo doInBackground(String... strings) {
            return null;
        }

        @Override
        protected void onPostExecute(FileInfo fileInfo) {
            super.onPostExecute(fileInfo);
        }
    }
}