package com.chenjishi.u148.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by chenjishi on 13-12-7.
 */
public class Feed implements Parcelable {
    public FeedData data;
    public int code;
    public String msg;

    public static final Creator<Feed> CREATOR = new Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel source) {
            return new Feed(source);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };

    public Feed() {

    }

    public Feed(Parcel in) {
        data = in.readParcelable(FeedData.class.getClassLoader());
        code = in.readInt();
        msg = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
        dest.writeInt(code);
        dest.writeString(msg);
    }
}
