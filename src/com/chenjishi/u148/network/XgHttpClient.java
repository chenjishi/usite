package com.chenjishi.u148.network;

import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-2-2
 * Time: 下午10:21
 * To change this template use File | Settings | File Templates.
 */
public class XgHttpClient {
    private static final String DEFAULT_CHARSET_ENCODING = "UTF-8";
    private static final String GET = "GET";
    private static final String POST = "POST";
    private int tryNum;
    private int connectTime;
    private int readTime;

    public static XgHttpClient getDefaultXgHttpClient() {
        XgHttpClient client = new XgHttpClient();
        client.setTryNum(2);
        client.setConnectTime(20000);
        client.setReadTime(20000);

        return client;
    }

    public static XgHttpClient getImageXgHttpClient() {
        XgHttpClient client = new XgHttpClient();
        client.setTryNum(1);
        client.setConnectTime(8000);
        client.setReadTime(10000);
        return client;
    }

    public String doGet(String urlStr){
        byte[] data = doRequest(urlStr);
        String s = null;
        if (data != null) {
            s = new String(data);
            data = null;
        }
        return s;
    }

    public byte[] doRequest(String urlStr){
        int num = tryNum;
        while (num > 0) {
            try {
                byte[] data = request(urlStr);
                return data;
            } catch (Exception ste) {
                num--;
                if (num == 0) {
                    return null;
                }
            }
        }
        return null;
    }

    private byte[] request(String urlStr){
        byte[] data = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            conn = getHttpURLConnection(urlStr, XgHttpClient.GET);
            if (conn.getResponseCode() == 403) {
                is = conn.getErrorStream();
            } else {
                is = conn.getInputStream();
            }

            String contentEncoding = conn.getContentEncoding();

            if (contentEncoding != null
                    && contentEncoding.indexOf("gzip") != -1) {
                is = new GZIPInputStream(is);
            } else if (contentEncoding != null
                    && contentEncoding.indexOf("deflate") != -1) {
                is = new InflaterInputStream(is);
            }
            data = readInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return data;
    }

    private HttpURLConnection getHttpURLConnection(String urlStr, String method)
            throws IOException {
        HttpURLConnection conn = null;
        URL url = new URL(urlStr);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(connectTime);
        conn.setReadTimeout(readTime);
        conn.setRequestProperty("Accept-Charset", DEFAULT_CHARSET_ENCODING);
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        disableConnectionReuseIfNecessary();
        return conn;
    }

    private static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Integer.parseInt(Build.VERSION.SDK) < 8) {
            System.setProperty("http.keepAlive", "false");
        }
    }


    public static byte[] readInputStream(InputStream is){
        byte[] data = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            data = baos.toByteArray();
            baos.close();
            baos = null;
        } catch (Exception e) {
        }

        return data;
    }

    public int getTryNum() {
        return tryNum;
    }

    public void setTryNum(int tryNum) {
        this.tryNum = tryNum;
    }

    public int getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(int connectTime) {
        this.connectTime = connectTime;
    }

    public int getReadTime() {
        return readTime;
    }

    public void setReadTime(int readTime) {
        this.readTime = readTime;
    }
}
