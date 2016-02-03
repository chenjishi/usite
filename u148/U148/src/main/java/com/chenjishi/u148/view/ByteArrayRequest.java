package com.chenjishi.u148.view;


import com.chenjishi.u148.volley.NetworkResponse;
import com.chenjishi.u148.volley.Request;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.toolbox.HttpHeaderParser;

/**
 * Created by chenjishi on 14/11/14.
 */
public class ByteArrayRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;

    public ByteArrayRequest(String url, Response.Listener<byte[]> listener,
                            Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }
}
