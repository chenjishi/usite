package com.chenjishi.u148.model;

/**
 * Created by chenjishi on 14-6-24.
 */
public class UpdateInfo {
    public String msg;
    public int code;
    public UpdateData data;

    public static class UpdateData {
        public String url;
        public int versionCode;
        public String versionName;
    }
}
