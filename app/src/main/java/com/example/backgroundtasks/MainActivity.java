package com.example.backgroundtasks;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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