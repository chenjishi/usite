package com.chenjishi.u148.network;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-2-2
 * Time: 下午10:27
 * To change this template use File | Settings | File Templates.
 */
public class HttpUtils {
    public static String callAPI(String url){
        if(url == null || "".equals(url)|| !url.startsWith("http://")){
            return null;
        }
        return XgHttpClient.getDefaultXgHttpClient().doGet(url);
    }

    /**
     * download picture
     */
    public static byte[] downloadImage(String urlStr){
        return XgHttpClient.getImageXgHttpClient().doRequest(urlStr);
    }
}
