package com.chenjishi.u148.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.chenjishi.u148.volley.toolbox.StringRequest;

/**
 * Created by chenjishi on 14-1-7.
 */
public class User implements Parcelable {
    public String id;
    public String icon;
    public String alias;
    public String nickname;
    public String sex;
    public String status;
    public String token;

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User() {
    }

    public User(Parcel in) {
        id = in.readString();
        icon = in.readString();
        alias = in.readString();
        nickname = in.readString();
        sex = in.readString();
        status = in.readString();
        token = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(icon);
        dest.writeString(alias);
        dest.writeString(nickname);
        dest.writeString(sex);
        dest.writeString(status);
        dest.writeString(token);
    }
}
