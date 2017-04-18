package com.chenjishi.u148.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.UserInfo;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-2-26.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "u148.db";
    private static final int DB_VERSION = 1;

    private static final String TB_NAME_FAVORITE = "favorites";
    private static final String TB_NAME_ARTICLE = "article";

    private static final String COL_ID = "id";
    private static final String COL_UID = "uid";
    private static final String COL_CATEGORY = "category";
    private static final String COL_TITLE = "title";
    private static final String COL_SUMMARY = "summary";
    private static final String COL_PIC_MID = "picMid";
    private static final String COL_CREATE_TIME = "createTime";
    private static final String COL_ALIAS = "alias";
    private static final String COL_NICKNAME = "nickname";
    private static final String COL_USER_ICON = "icon";
    private static final String COL_CONTENT = "content";

    private static final String COL_OFFSET = "offset";
    private static final String COL_READ_STATE = "readState";

    private static SQLiteDatabase mDb = null;
    private static DBHelper INSTANCE = null;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (null == INSTANCE) {
            INSTANCE = new DBHelper(context);
            mDb = INSTANCE.getWritableDatabase();
        }

        return INSTANCE;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME_FAVORITE + " (" +
                COL_ID + " TEXT," +
                COL_UID + " TEXT," +
                COL_CATEGORY + " INTEGER," +
                COL_TITLE + " TEXT," +
                COL_SUMMARY + " TEXT," +
                COL_PIC_MID + " TEXT, " +
                COL_CREATE_TIME + " INTEGER," +
                COL_ALIAS + " TEXT," +
                COL_NICKNAME + " TEXT," +
                COL_USER_ICON + " TEXT," +
                COL_CONTENT + " TEXT," +
                " UNIQUE (" + COL_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME_ARTICLE + " (" +
                COL_ID + " TEXT," +
                COL_OFFSET + " INTEGER," +
                COL_READ_STATE + " INTEGER, UNIQUE (" + COL_ID + ") ON CONFLICT REPLACE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TB_NAME_FAVORITE);
        onCreate(db);
    }

    public void delete(String id) {
        final String sql = "DELETE FROM " + TB_NAME_FAVORITE + " WHERE " + COL_ID + " = ?";
        mDb.execSQL(sql, new String[]{id});
    }

    public boolean exist(String id) {
        final String sql = "SELECT * FROM " + TB_NAME_FAVORITE + " WHERE " + COL_ID + " = ?";
        boolean result = false;
        Cursor c = null;

        try {
            c = mDb.rawQuery(sql, new String[]{id});
            result = c.moveToNext();
        } finally {
            if (null != c) c.close();
        }

        return result;
    }

    public void updateArticleOffset(String id, int offset) {
        final String sql = "INSERT OR REPLACE INTO " + TB_NAME_ARTICLE +
                " VALUES (?, ?, ?)";
        mDb.execSQL(sql, new String[] {
                id,
                String.valueOf(offset),
                String.valueOf(0)
        });
    }

    public int getOffsetById(String id) {
        int result = 0;
        final String sql = "SELECT " + COL_ID + ", " +
                COL_OFFSET + ", " +
                COL_READ_STATE + " FROM " +
                TB_NAME_ARTICLE + " WHERE " +
                COL_ID + " = ?";
        Cursor c = null;

        try {
            c = mDb.rawQuery(sql, new String[]{id});
            if (c.moveToNext()) {
                result = c.getInt(1);
            }
        } finally {
            if (null != c) c.close();
        }

        return result;
    }

    public Feed getFavoriteById(String id) {
        if (TextUtils.isEmpty(id)) return null;

        Feed feed = null;
        final String sql = "SELECT * FROM " + TB_NAME_FAVORITE + " WHERE " + COL_ID + " = " + id;
        Cursor cursor = null;
        try {
            cursor = mDb.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                feed = new Feed();
                feed.id = cursor.getString(0);
                feed.uid = cursor.getString(1);
                feed.category = cursor.getInt(2);
                feed.title = cursor.getString(3);
                feed.summary = cursor.getString(4);
                feed.pic_mid = cursor.getString(5);
                feed.create_time = cursor.getLong(6);

                UserInfo usr = new UserInfo();
                usr.alias = cursor.getString(7);
                usr.nickname = cursor.getString(8);
                usr.icon = cursor.getString(9);
                feed.usr = usr;
            }
        } finally {
            if (null != cursor) cursor.close();
        }

        return feed;
    }

    public void insert(Feed feed) {
        if (null == feed) return;

        final UserInfo usr = feed.usr;
        final String sql = "INSERT OR REPLACE INTO " + TB_NAME_FAVORITE +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        mDb.execSQL(sql, new String[]{
                feed.id,
                feed.uid,
                String.valueOf(feed.category),
                feed.title,
                feed.summary,
                feed.pic_mid,
                String.valueOf(feed.create_time),
                null != usr ? usr.alias : "",
                null != usr ? usr.nickname : "",
                null != usr ? usr.icon : "",
                "reserved"});
    }

    public void loadAllOffset() {
        final String sql = "SELECT " + COL_ID + ", " +
                COL_OFFSET + ", " +
                COL_READ_STATE + " FROM " +
                TB_NAME_ARTICLE;

        Cursor c = null;

        try {
            c = mDb.rawQuery(sql, null);
            while (c.moveToNext()) {
                Log.i("test", c.getString(0) + " offset " + c.getInt(1));
            }
        } finally {
            if (null != c) c.close();
        }

    }

    public ArrayList<Feed> loadAll() {
        final String sql = "SELECT " + COL_ID + ", " +
                COL_UID + ", " +
                COL_CATEGORY + ", " +
                COL_TITLE + ", " +
                COL_SUMMARY + ", " +
                COL_PIC_MID + ", " +
                COL_CREATE_TIME + ", " +
                COL_ALIAS + ", " +
                COL_NICKNAME + ", " +
                COL_USER_ICON + ", " +
                COL_CONTENT + " FROM " +
                TB_NAME_FAVORITE;
        ArrayList<Feed> feedItems = null;
        Cursor c = null;

        try {
            c = mDb.rawQuery(sql, null);
            final int count = c.getCount();

            if (count > 0) {
                feedItems = new ArrayList<Feed>();

                while (c.moveToNext()) {
                    Feed feed = new Feed();
                    feed.id = c.getString(0);
                    feed.uid = c.getString(1);
                    feed.category = c.getInt(2);
                    feed.title = c.getString(3);
                    feed.summary = c.getString(4);
                    feed.pic_mid = c.getString(5);
                    feed.create_time = c.getLong(6);

                    UserInfo usr = new UserInfo();
                    usr.alias = c.getString(7);
                    usr.nickname = c.getString(8);
                    usr.icon = c.getString(9);
                    feed.usr = usr;

                    feedItems.add(feed);
                }
            }
        } finally {
            if (null != c) c.close();
        }

        return feedItems;
    }
}
