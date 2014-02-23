package com.chenjishi.u148.util;

import android.text.TextUtils;
import android.util.Log;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-1-7.
 */
public class JsonParser {

    public static ArrayList<Feed> getFeedList(String json) {
        if (TextUtils.isEmpty(json)) return null;

        ArrayList<Feed> feedList = null;

        try {
            JSONObject jObj = new JSONObject(json);
            JSONObject dataObj = jObj.getJSONObject("data");
            JSONArray jArray = dataObj.getJSONArray("data");
            if (null != jArray && jArray.length() > 0) {
                int len = jArray.length();

                feedList = new ArrayList<Feed>();
                for (int i = 0; i < len; i++) {
                    JSONObject obj = jArray.getJSONObject(i);

                    Feed feed = new Feed();

                    feed.id = obj.optString("id", "");

                    User user = new User();
                    user.id = obj.optString("uid", "");

                    JSONObject uObj = obj.getJSONObject("usr");
                    user.alias = uObj.optString("alias", "");
                    user.nickname = uObj.optString("nickname", "");
                    user.sex = uObj.optString("sexStr", "");
                    user.status = uObj.optString("statusStr", "");

                    feed.user = user;

                    feed.category = obj.optInt("category", -1);
                    feed.title = obj.optString("title", "");
                    feed.summary = obj.optString("summary", "");
                    feed.picMin = obj.optString("pic_min", "");
                    feed.picMid = obj.optString("pic_mid", "");
                    feed.star = obj.optInt("star", 0);
                    feed.createTime = obj.optLong("create_time", -1L);
                    feed.countBrowse = obj.optInt("count_browse", 0);
                    feed.countReview = obj.optInt("count_review", 0);

                    //for stand for game article,we should remove that
                    if (feed.category != 4) {
                        feedList.add(feed);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("FeedParser", "getFeedList json parse error " + e);
        }

        return feedList;
    }
}