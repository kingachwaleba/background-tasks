package com.example.backgroundtasks;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadProgress implements Parcelable {

    private int downloadedBytes;
    private int size;
    private int status;

    public DownloadProgress() {
        downloadedBytes = 0;
        size = 0;
        status = 0;
    }

    public DownloadProgress(Parcel in) {
    }

    public static final Creator<DownloadProgress> CREATOR = new Creator<DownloadProgress>() {
        @Override
        public DownloadProgress createFromParcel(Parcel in) {
            return new DownloadProgress(in);
        }

        @Override
        public DownloadProgress[] newArray(int size) {
            return new DownloadProgress[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
