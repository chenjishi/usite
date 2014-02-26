package com.chenjishi.u148.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chenjishi on 14-2-23.
 */
public class FeedItem implements Parcelable {
    public String id;
    public String status;
    public String uid;
    public int category;
    public String title;
    public String summary;
    public String pic_min;
    public String pic_mid;
    public int star;
    public long create_time;
    public int count_browse;
    public int count_review;

    public UserInfo usr;

    public static final Creator<FeedItem> CREATOR = new Creator<FeedItem>() {
        @Override
        public FeedItem createFromParcel(Parcel source) {
            return new FeedItem(source);
        }

        @Override
        public FeedItem[] newArray(int size) {
            return new FeedItem[size];
        }
    };

    public FeedItem() {

    }

    public FeedItem(Parcel in) {
        id = in.readString();
        status = in.readString();
        uid = in.readString();
        category = in.readInt();
        title = in.readString();
        summary = in.readString();
        pic_min = in.readString();
        pic_mid = in.readString();
        star = in.readInt();
        create_time = in.readLong();
        count_browse = in.readInt();
        count_review = in.readInt();

        usr = in.readParcelable(UserInfo.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(status);
        dest.writeString(uid);
        dest.writeInt(category);
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeString(pic_min);
        dest.writeString(pic_mid);
        dest.writeInt(star);
        dest.writeLong(create_time);
        dest.writeInt(count_browse);
        dest.writeInt(count_review);

        dest.writeParcelable(usr, flags);
    }
}
