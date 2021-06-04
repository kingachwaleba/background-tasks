package com.example.backgroundtasks;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadProgress implements Parcelable {

    private int downloadedBytes;
    private int size;
    private int status;

    public static final int STATUS_ERROR = -1;
    public static final int STATUS_IN_PROGRESS = 0;
    public static final int STATUS_FINISHED = 1;

    public DownloadProgress() {
        downloadedBytes = 0;
        size = 0;
        status = 0;
    }

    public DownloadProgress(Parcel in) {
        downloadedBytes = in.readInt();
        size = in.readInt();
        status = in.readInt();
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
        dest.writeInt(downloadedBytes);
        dest.writeInt(size);
        dest.writeInt(status);
    }

    public int getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(int downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
