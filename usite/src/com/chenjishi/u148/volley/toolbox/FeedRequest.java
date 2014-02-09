package com.chenjishi.u148.volley.toolbox;

import com.chenjishi.u148.base.AppApplication;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.parser.JsonParser;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.util.StringUtil;
import com.chenjishi.u148.volley.NetworkResponse;
import com.chenjishi.u148.volley.Request;
import com.chenjishi.u148.volley.Response;

import java.util.ArrayList;

/**
 * Created by chenjishi on 13-12-7.
 */
public class FeedRequest extends Request<ArrayList<Feed>> {
    private final Response.Listener<ArrayList<Feed>> mListener;
    private final String mUrl;

    public FeedRequest(String url,
                       Response.Listener<ArrayList<Feed>> listener,
                       Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
        mUrl = url;
    }

    @Override
    protected Response<ArrayList<Feed>> parseNetworkResponse(NetworkResponse response) {
        String json = new String(response.data);
        ArrayList<Feed> feedList = JsonParser.getFeedList(json);

        int index = mUrl.lastIndexOf("/");
        if (index != -1) {
            String idx = mUrl.substring(index + 1);

            if (null != idx && idx.length() > 0) {
                int page = Integer.parseInt(idx);
                if (page == 1) {
                    String dir = FileCache.getDataCacheDir(AppApplication.getInstance());
                    String name = StringUtil.getMD5Str(mUrl);
                    String path = dir + name;
                    FileUtils.writeToFile(path, json);
                }
            }
        }

        return Response.success(feedList, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(ArrayList<Feed> response) {
        mListener.onResponse(response);
    }
}
