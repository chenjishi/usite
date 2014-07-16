package com.chenjishi.u148.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.FeedItem;
import com.chenjishi.u148.sina.RequestListener;
import com.chenjishi.u148.sina.StatusesAPI;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.Utils;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.openapi.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by chenjishi on 14-6-17.
 */
public class ShareDialog extends Dialog implements View.OnClickListener, RequestListener {
    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 100;

    private static final String WX_APP_ID = "wxf862baa09e0df157";
    private static final String WB_APP_ID = "1792649719";

    private static final String ARTICLE_URL = "http://www.u148.net/article/%1$s.html";
    private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    private static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    private static final int SHARE_TO_SESSION = 100;
    private static final int SHARE_TO_FRIENDS = 101;
    private static final int SHARE_TO_WEIBO = 102;

    private Context mContext;

    private IWXAPI mWXAPI;
    private FeedItem mFeed;

    private String mImageUrl;

    public ShareDialog(Context context) {
        super(context, R.style.FullHeightDialog);

        mContext = context;

        setCanceledOnTouchOutside(true);

        mWXAPI = WXAPIFactory.createWXAPI(context, WX_APP_ID);
        mWXAPI.registerApp(WX_APP_ID);

        int paddingTop = Utils.dp2px(context, 12.f);
        LinearLayout container = new LinearLayout(context);
        container.setBackgroundColor(0xFFFFFFFF);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setWeightSum(3.f);
        container.setPadding(0, paddingTop, 0, paddingTop);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        int[] iconIds = {R.drawable.ic_session, R.drawable.ic_friend, R.drawable.ic_weibo};
        int[] nameIds = {R.string.share_session, R.string.share_friend, R.string.share_weibo};

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 1.f;

        int padding = Utils.dp2px(context, 8);
        for (int i = 0; i < iconIds.length; i++) {
            TextView tv = new TextView(context);
            tv.setLayoutParams(lp);
            tv.setCompoundDrawablesWithIntrinsicBounds(0, iconIds[i], 0, 0);
            tv.setCompoundDrawablePadding(padding);
            tv.setPadding(padding, padding, padding, padding);
            tv.setBackgroundResource(R.drawable.home_up_bg);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.f);
            tv.setTextColor(0xFF000000);
            tv.setText(nameIds[i]);
            tv.setTag(SHARE_TO_SESSION + i);
            tv.setOnClickListener(this);

            container.addView(tv);
        }

        setContentView(container);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = context.getResources().getDisplayMetrics().widthPixels;
        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(layoutParams);
    }

    public void setShareFeed(FeedItem feed) {
        mFeed = feed;
    }

    public void setShareImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    private void shareToWX(final int type) {
        HttpUtils.getImageLoader().get(mImageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                if (null != bitmap) {
                    if (null != mFeed) {
                        sendWebPage(type, bitmap);
                    } else {
                        sendImage(type, bitmap);
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showToast(R.string.share_image_fail);
            }
        });
    }

    private void sendWebPage(int type, Bitmap bitmap) {
        Bitmap thumb = getThumbBitmap(bitmap);

        WXWebpageObject webObject = new WXWebpageObject(String.format(ARTICLE_URL, mFeed.id));
        WXMediaMessage msg = new WXMediaMessage(webObject);
        msg.title = mFeed.title;
        msg.description = mFeed.summary;
        msg.thumbData = FileUtils.bmpToByteArray(thumb, true);

        SendMessageToWX.Req request = new SendMessageToWX.Req();
        request.transaction = "image" + System.currentTimeMillis();
        request.message = msg;
        request.scene = type;

        mWXAPI.sendReq(request);
    }

    private void sendImage(int type, Bitmap bitmap) {
        Bitmap thumb = getThumbBitmap(bitmap);

        WXImageObject imageObject = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage(imageObject);
        msg.title = getContext().getString(R.string.app_name);
        msg.description = getContext().getString(R.string.share_image_tip);
        msg.thumbData = FileUtils.bmpToByteArray(thumb, true);

        SendMessageToWX.Req request = new SendMessageToWX.Req();
        request.transaction = "image" + System.currentTimeMillis();
        request.message = msg;
        request.scene = type;

        mWXAPI.sendReq(request);
    }

    private void shareToWB() {
        Oauth2AccessToken token = PrefsUtil.getAccessToken();

        String content;
        if (null != mFeed) {
            String url = String.format(ARTICLE_URL, mFeed.id);
            String title = mFeed.title + "-";
            int summaryLength = 140 - url.length() - title.length();

            String summary = mFeed.summary;
            if (summary.length() > summaryLength) {
                summary = summary.substring(0, summaryLength - 1);
            }

            content = title + summary + url;
        } else {
            content = getContext().getString(R.string.share_image_tip);
        }

        if (!token.isSessionValid()) {
            authorize(content);
        } else {
            updateStatus(content, token);
        }
    }

    private void updateStatus(String content, Oauth2AccessToken token) {
        StatusesAPI api = new StatusesAPI(token);

        if (!TextUtils.isEmpty(mImageUrl)) {
            api.uploadUrlText(content, mImageUrl, null, null, null, this);
        } else {
            api.update(content, null, null, this);
        }
    }

    private Bitmap getThumbBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int dstWidth;
        int dstHeight;

        //thumbData do not exceed 32KB
        if (w < h) {
            dstHeight = THUMB_SIZE;
            dstWidth = (w * THUMB_SIZE) / h;
        } else {
            dstWidth = THUMB_SIZE;
            dstHeight = (h * THUMB_SIZE) / w;
        }

        return Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
    }

    private void authorize(final String content) {
        WeiboAuth weiboAuth = new WeiboAuth(mContext, WB_APP_ID, REDIRECT_URL, SCOPE);
        weiboAuth.authorize(new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
                PrefsUtil.saveAccessToken(accessToken);

                updateStatus(content, accessToken);
            }

            @Override
            public void onWeiboException(WeiboException e) {

            }

            @Override
            public void onCancel() {

            }
        }, WeiboAuth.OBTAIN_AUTH_TOKEN);
    }

    @Override
    public void onClick(View v) {
        int tag = (Integer) v.getTag();

        switch (tag) {
            case SHARE_TO_SESSION:
                checkStatus(SendMessageToWX.Req.WXSceneSession);
                shareToWX(SendMessageToWX.Req.WXSceneSession);
                break;
            case SHARE_TO_FRIENDS:
                checkStatus(SendMessageToWX.Req.WXSceneTimeline);
                shareToWX(SendMessageToWX.Req.WXSceneTimeline);
                break;
            case SHARE_TO_WEIBO:
                shareToWB();
                break;
        }

        dismiss();
    }

    @Override
    public void onComplete(String response) {
        showWeiboMessage(true);
    }

    @Override
    public void onComplete4binary(ByteArrayOutputStream responseOS) {
    }

    @Override
    public void onIOException(IOException e) {
        showWeiboMessage(false);
    }

    @Override
    public void onError(WeiboException e) {
        showWeiboMessage(false);
    }

    private void showWeiboMessage(final boolean success) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(success ? R.string.weibo_share_success :
                        R.string.weibo_share_fail);
            }
        });
    }

    private void checkStatus(int type) {
        if (!mWXAPI.isWXAppInstalled()) {
            Utils.showToast(R.string.wechat_not_install);
            return;
        }

        if (!mWXAPI.isWXAppSupportAPI()) {
            Utils.showToast(R.string.wechat_not_support);
            return;
        }

        if (type == SHARE_TO_FRIENDS && mWXAPI.getWXAppSupportAPI() < TIMELINE_SUPPORTED_VERSION) {
            Utils.showToast(R.string.wechat_timeline_not_support);
        }
    }
}
