package com.chenjishi.u148.model;

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
    public String id;
    public String content;
    public String articleId;
    public long time;
    public int floor;

    public User user;

    public Comment() {

    }

    public Comment(Parcel in) {
        id = in.readString();
        content = in.readString();
        articleId = in.readString();
        time = in.readLong();
        floor = in.readInt();

        user = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(content);
        dest.writeString(articleId);
        dest.writeLong(time);

        dest.writeParcelable(user, flags);
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
