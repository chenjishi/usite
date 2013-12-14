package com.chenjishi.u148.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.sina.RequestListener;
import com.chenjishi.u148.sina.StatusesAPI;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.openapi.*;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-26
 * Time: 下午1:40
 * To change this template use File | Settings | File Templates.
 */
public class ShareUtils {
    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 100;

    public static final int SHARE_SESSION = 1;
    public static final int SHARE_FRIEND = 2;
    public static final int SHARE_WEIBO = 3;

    public static void shareWebpage(Context context, String url, int type, String title, Bitmap thumb) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, Constants.WX_APP_ID);
        api.registerApp(Constants.WX_APP_ID);

        checkStatus(api, context, type);
        WXWebpageObject webObject = new WXWebpageObject();
        webObject.webpageUrl = url;

        WXMediaMessage msg = new WXMediaMessage(webObject);
        msg.title = title;
        msg.description = title;

        if (null != thumb) {
            //thumbData do not exceed 32KB
            int w = thumb.getWidth();
            int h = thumb.getHeight();

            int desiredW;
            int desireH;

            if (w < h) {
                desireH = THUMB_SIZE;
                desiredW = (w * THUMB_SIZE) / h;
            } else {
                desiredW = THUMB_SIZE;
                desireH = (h * THUMB_SIZE) / w;
            }

            Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, desiredW, desireH, true);
            msg.thumbData = FileUtils.bmpToByteArray(thumbBmp, true);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "webpage" + System.currentTimeMillis();
        req.message = msg;
        req.scene = type == SHARE_FRIEND ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    public static void shareImage(Context context, String url, int type, Bitmap thumb) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, Constants.WX_APP_ID);

        checkStatus(api, context, type);

        String path = FileCache.getTempDirectory(context) + "temp";

        File file = new File(path);
        if (!file.exists()) {
            CommonUtil.showToast(R.string.share_file_error);
            return;
        }

        WXImageObject imgObj = new WXImageObject();
        imgObj.setImagePath(path);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        String title = context.getString(R.string.app_name_simple);
        msg.title = title;
        msg.description = title;

        if (null != thumb) {
            //thumbData do not exceed 32KB
            int w = thumb.getWidth();
            int h = thumb.getHeight();

            int desiredW;
            int desireH;

            if (w < h) {
                desireH = THUMB_SIZE;
                desiredW = (w * THUMB_SIZE) / h;
            } else {
                desiredW = THUMB_SIZE;
                desireH = (h * THUMB_SIZE) / w;
            }

            Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, desiredW, desireH, true);
            msg.thumbData = FileUtils.bmpToByteArray(thumbBmp, true);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "img" + System.currentTimeMillis();
        req.message = msg;
        req.scene = type == SHARE_FRIEND ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    public static void shareVideo(Context context, String url, int type, String title, Bitmap thumb) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, Constants.WX_APP_ID);

        checkStatus(api, context, type);

        WXVideoObject videoObject = new WXVideoObject();
        videoObject.videoUrl = url;

        WXMediaMessage msg = new WXMediaMessage(videoObject);
        msg.title = title;
        msg.description = title;

        if (null != thumb) {
            //thumbData do not exceed 32KB
            int w = thumb.getWidth();
            int h = thumb.getHeight();

            int desiredW;
            int desireH;

            if (w < h) {
                desireH = THUMB_SIZE;
                desiredW = (w * THUMB_SIZE) / h;
            } else {
                desiredW = THUMB_SIZE;
                desireH = (h * THUMB_SIZE) / w;
            }

            Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, desiredW, desireH, true);
            msg.thumbData = FileUtils.bmpToByteArray(thumbBmp, true);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "video" + System.currentTimeMillis();
        req.message = msg;
        req.scene = type == SHARE_FRIEND ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    private static void checkStatus(IWXAPI api, Context context, int type) {
        if (!api.isWXAppInstalled()) {
            Toast.makeText(context, context.getString(R.string.wechat_not_install), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!api.isWXAppSupportAPI()) {
            Toast.makeText(context, context.getString(R.string.wechat_not_support), Toast.LENGTH_SHORT).show();
            return;
        }

        if (type == SHARE_FRIEND) {
            int wxSdkVersion = api.getWXAppSupportAPI();
            if (wxSdkVersion < TIMELINE_SUPPORTED_VERSION) {
                Toast.makeText(context, context.getString(R.string.wechat_timeline_not_support), Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    public static void shareToWeibo(Context context, String content, String filePath, String imageUrl, RequestListener listener) {
        Oauth2AccessToken token = PrefsUtil.getAccessToken();

        if (!token.isSessionValid()) {
            authorize(context, content, filePath, imageUrl, listener);
        } else {
            StatusesAPI api = new StatusesAPI(token);
            if (null != filePath) {
//                api.uploadPic(content, filePath, listener);
                api.upload(content, filePath, null, null, listener);
                return;
            }

            if (null != imageUrl) {
//                api.update(content, null, null, listener);
                api.uploadUrlText(content, imageUrl, null, null, null, listener);
            }
        }
    }

    private static void authorize(Context context,
                                  final String content,
                                  final String filePath,
                                  final String imageUrl,
                                  final RequestListener listener) {
        WeiboAuth weiboAuth = new WeiboAuth(context, Constants.WEIBO_APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        weiboAuth.anthorize(new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
                PrefsUtil.saveAccessToken(accessToken);

                StatusesAPI api = new StatusesAPI(accessToken);
                if (null != filePath) {
                    api.upload(content, filePath, null, null, listener);
                    return;
                }

                if (null != imageUrl) {
//                    api.update(content, null, null, listener);
                    api.uploadUrlText(content, imageUrl, null, null, null, listener);
                }

            }

            @Override
            public void onWeiboException(WeiboException e) {

            }

            @Override
            public void onCancel() {

            }
        });
    }
}
