package com.chenjishi.u148.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-2-23.
 */
public class FeedData implements Parcelable {
    public int total;
    public ArrayList<FeedItem> data;

    public static final Creator<FeedData> CREATOR = new Creator<FeedData>() {
        @Override
        public FeedData createFromParcel(Parcel source) {
            return new FeedData(source);
        }

        @Override
        public FeedData[] newArray(int size) {
            return new FeedData[size];
        }
    };

    public FeedData() {
        data = new ArrayList<FeedItem>();
    }

    public FeedData(Parcel in) {
        this();

        total = in.readInt();
        in.readTypedList(data, FeedItem.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(total);
        dest.writeTypedList(data);
    }
}
