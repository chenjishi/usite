package com.chenjishi.u148.model;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-2-22.
 */
public class Favorite {
    public FavoriteData data;
    public String msg;
    public int code;

    public static class FavoriteData {
        public int total;
        public ArrayList<FavoriteItem> data;
    }
}
