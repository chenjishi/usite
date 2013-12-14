package com.chenjishi.u148.volley.toolbox;

import com.chenjishi.u148.base.AppApplication;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.parser.FeedItemParser;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.volley.NetworkResponse;
import com.chenjishi.u148.volley.Request;
import com.chenjishi.u148.volley.Response;

import java.util.ArrayList;

/**
 * Created by chenjishi on 13-12-7.
 */
public class FeedRequest extends Request<ArrayList<Feed>> {
    private final int mSource;
    private final String mUrl;
    private final Response.Listener<ArrayList<Feed>> mListener;

    public FeedRequest(int source,
                       String url,
                       Response.Listener<ArrayList<Feed>> listener,
                       Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
        mSource = source;
        mUrl = url;
    }

    @Override
    protected Response<ArrayList<Feed>> parseNetworkResponse(NetworkResponse response) {
        String html = new String(response.data);
        ArrayList<Feed> feeds = null;
        int page = 0;
        int index = mUrl.lastIndexOf("/");
        if (index != -1) {
            String idx = mUrl.substring(index + 1);

            if (null != idx && idx.length() > 0) {
                page = Integer.parseInt(idx);
            }
        }

        if (mSource == Constants.SOURCE_JIANDAN) {
            if (page <= 1) {
                String path = FileCache.getDataCacheDirectory(AppApplication.getInstance()) + Constants.CACHED_JIANDAN;
                FileUtils.writeToFile(path, html);
                feeds = FeedItemParser.parseJianDanFeed(html);
            } else {
                feeds = FeedItemParser.parseJianDanFeed2(html);
            }
        }

        if (mSource == Constants.SOURCE_NEWS) {
            if (page <= 1) {
                String path = FileCache.getDataCacheDirectory(AppApplication.getInstance()) + Constants.CACHED_NEWS;
                FileUtils.writeToFile(path, html);
            }

            feeds = FeedItemParser.parseNews(html);
        }

        return Response.success(feeds, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(ArrayList<Feed> response) {
        mListener.onResponse(response);
    }
}
