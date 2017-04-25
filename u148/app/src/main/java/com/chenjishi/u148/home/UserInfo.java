package com.chenjishi.u148.home;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chenjishi on 14-2-23.
 */
public class UserInfo implements Parcelable {
    public String icon;
    public String alias;
    public String nickname;
    public String sexStr;
    public String statusStr;
    public String id;
    public String token;

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public UserInfo() {
    }

    public UserInfo(Parcel in) {
        icon = in.readString();
        alias = in.readString();
        nickname = in.readString();
        sexStr = in.readString();
        statusStr = in.readString();
        id = in.readString();
        token = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(icon);
        dest.writeString(alias);
        dest.writeString(nickname);
        dest.writeString(sexStr);
        dest.writeString(statusStr);
        dest.writeString(id);
        dest.writeString(token);
    }
}
