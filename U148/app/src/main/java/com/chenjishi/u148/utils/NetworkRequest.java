package com.chenjishi.u148.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

/**
 * Created by chenjishi on 16/12/10.
 */
public class NetworkRequest {
    private static final NetworkRequest INSTANCE = new NetworkRequest();

    private OkHttpClient mHttpClient;

    private Gson mGson;

    private Handler mHandler;

    private NetworkRequest() {
        mHttpClient = new OkHttpClient();
        mGson = new GsonBuilder().create();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static NetworkRequest getInstance() {
        return INSTANCE;
    }

    public <T> void get(String url,
                        final Class<T> clazz,
                        final Listener<T> listener,
                        final ErrorListener errorListener) {

        Request.Builder request = new Request.Builder().url(url);
        request.cacheControl(CacheControl.FORCE_NETWORK);

        mHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError(errorListener);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                parseJson(response.body().string(), clazz, listener, errorListener);
            }
        });
    }

    public void post(String url,
                     Map<String, String> params,
                     final Listener<String> listener,
                     final ErrorListener errorListener) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : params.keySet()) {
            builder.add(key, params.get(key));
        }

        Request.Builder request = new Request.Builder()
                .url(url)
                .post(builder.build());

        mHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError(errorListener);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                onSuccess(listener, response.body().string());
            }
        });
    }

    public void getBytes(String url,
                         final Listener<byte[]> listener,
                         final ErrorListener errorListener) {
        Request.Builder request = new Request.Builder()
                .url(url);

        mHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError(errorListener);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                listener.onResponse(response.body().bytes());
            }
        });
    }

    public void get(String url,
                    final Listener<String> listener,
                    final ErrorListener errorListener) {
        Request.Builder request = new Request.Builder()
                .url(url);

        mHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError(errorListener);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                onSuccess(listener, response.body().string());
            }
        });
    }

    private void onError(final ErrorListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onErrorResponse();
            }
        });
    }

    private <T> void parseJson(final String json,
                               final Class<T> clazz,
                               final Listener<T> listener,
                               final ErrorListener errorListener) {
        if (TextUtils.isEmpty(json)) {
            onError(errorListener);
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    T obj = mGson.fromJson(json, clazz);
                    listener.onResponse(obj);
                } catch (JsonSyntaxException e) {
                    onError(errorListener);
                }
            }
        });
    }

    private void onSuccess(final Listener<String> listener, final String json) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(json);
            }
        });
    }
}
