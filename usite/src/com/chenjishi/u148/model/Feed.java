package com.chenjishi.u148.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chenjishi on 13-12-7.
 */
public class Feed implements Parcelable {
    public String id;
    public String url;
    public int category;
    public String title;
    public String summary;
    public String picMin;
    public String picMid;
    public int star;
    public long createTime;
    public int countBrowse;
    public int countReview;

    public User user;

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
        id = in.readString();
        url = in.readString();
        category = in.readInt();
        title = in.readString();
        summary = in.readString();
        picMin = in.readString();
        picMid = in.readString();
        star = in.readInt();
        createTime = in.readLong();
        countBrowse = in.readInt();
        countReview = in.readInt();

        user = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeInt(category);
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeString(picMin);
        dest.writeString(picMid);
        dest.writeInt(star);
        dest.writeLong(createTime);
        dest.writeInt(countBrowse);
        dest.writeInt(countReview);
        dest.writeParcelable(user, flags);
    }
}
