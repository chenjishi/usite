package com.chenjishi.u148.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-5-4
 * Time: 下午1:01
 * To change this template use File | Settings | File Templates.
 */
public class Comment implements Parcelable {
    public String avatar;
    public String userName;
    public String time;
    public String content;

    public Comment() {

    }

    public Comment(Parcel in) {
        avatar = in.readString();
        userName = in.readString();
        time = in.readString();
        content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avatar);
        dest.writeString(userName);
        dest.writeString(time);
        dest.writeString(content);
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
