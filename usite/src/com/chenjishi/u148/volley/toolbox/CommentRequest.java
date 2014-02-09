package com.chenjishi.u148.volley.toolbox;

import com.chenjishi.u148.model.Comment;
import com.chenjishi.u148.parser.JsonParser;
import com.chenjishi.u148.volley.NetworkResponse;
import com.chenjishi.u148.volley.Request;
import com.chenjishi.u148.volley.Response;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-1-8.
 */
public class CommentRequest extends Request<ArrayList<Comment>> {
    private final Response.Listener<ArrayList<Comment>> mListener;

    public CommentRequest(String url,
                          Response.Listener<ArrayList<Comment>> listener,
                          Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<ArrayList<Comment>> parseNetworkResponse(NetworkResponse response) {
        String json = new String(response.data);
        ArrayList<Comment> comments = JsonParser.getCommentList(json);

        return Response.success(comments, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(ArrayList<Comment> response) {
        mListener.onResponse(response);
    }
}
