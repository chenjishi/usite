package com.chenjishi.u148.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.home.Feed;
import com.chenjishi.u148.utils.ErrorListener;
import com.chenjishi.u148.utils.Listener;
import com.chenjishi.u148.utils.NetworkRequest;
import com.chenjishi.u148.utils.Utils;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by chenjishi on 14-6-17.
 */
public class ShareDialog extends Dialog implements View.OnClickListener, WbShareCallback, Listener<byte[]>, ErrorListener {
    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 100;

    private static final String WX_APP_ID = "wxf862baa09e0df157";
    private static final String WB_APP_ID = "1792649719";
    private static final String QQ_APP_ID = "1101214227";

    private static final String ARTICLE_URL = "http://www.u148.net/article/%1$s.html";
    private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    private static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    private static final int SHARE_TO_SESSION = 100;
    private static final int SHARE_TO_FRIENDS = 101;
    private static final int SHARE_TO_WEIBO = 102;
    private static final int SHARE_TO_QZONE = 103;
    private static final int SHARE_TO_QQ = 104;

    private IWXAPI mWXAPI;
    private Feed mFeed;

    private int mType;

    private ArrayList<String> mImageList;

    private Tencent mTencent;

    private Context mContext;

    private final IUiListener mListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {

        }

        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

        }
    };

    public ShareDialog(Context context) {
        super(context, R.style.FullHeightDialog);
        setCanceledOnTouchOutside(true);
        mContext = context;

        int paddingTop = Utils.dp2px(context, 12.f);
        LinearLayout layout = new LinearLayout(context);
        layout.setBackgroundColor(Color.WHITE);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setWeightSum(5.f);
        layout.setPadding(0, paddingTop, 0, paddingTop);

        Resources res = context.getResources();
        int[] icons = {R.drawable.ic_session, R.drawable.ic_friend, R.drawable.ic_weibo,
                R.drawable.ic_qqzone, R.drawable.ic_qq};
        String[] titles = res.getStringArray(R.array.share_items);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, WRAP_CONTENT);
        lp.weight = 1.f;
        int padding = Utils.dp2px(context, 8);
        for (int i = 0; i < icons.length; i++) {
            TextView tv = Utils.generateTextView(context, titles[i], Color.BLACK, 12.f);
            tv.setCompoundDrawablesWithIntrinsicBounds(0, icons[i], 0, 0);
            tv.setCompoundDrawablePadding(padding);
            tv.setPadding(padding, padding, padding, padding);
            tv.setBackgroundResource(R.drawable.home_up_bg);
            tv.setGravity(Gravity.CENTER);
            tv.setTag(SHARE_TO_SESSION + i);
            tv.setOnClickListener(this);

            layout.addView(tv, lp);
        }
        setContentView(layout, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = res.getDisplayMetrics().widthPixels;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(layoutParams);

        mWXAPI = WXAPIFactory.createWXAPI(context, WX_APP_ID);
        mWXAPI.registerApp(WX_APP_ID);

        mTencent = Tencent.createInstance(QQ_APP_ID, context);
        WbSdk.install(context, new AuthInfo(context, WB_APP_ID, REDIRECT_URL, SCOPE));
    }

    public void setShareFeed(Feed feed) {
        mFeed = feed;
    }

    public void setImageList(ArrayList<String> imageList) {
        mImageList = imageList;
    }

    private void shareToWX(final int type) {
        if (!mWXAPI.isWXAppInstalled()) {
            Utils.showToast(getContext(), R.string.wechat_not_install);
            return;
        }

        if (!mWXAPI.isWXAppSupportAPI()) {
            Utils.showToast(getContext(), R.string.wechat_not_support);
            return;
        }

        if (type == SHARE_TO_FRIENDS && mWXAPI.getWXAppSupportAPI() < TIMELINE_SUPPORTED_VERSION) {
            Utils.showToast(getContext(), R.string.wechat_timeline_not_support);
        }

        String url = mImageList.get(0);
        NetworkRequest.getInstance().getBytes(url, this, this);
    }

    @Override
    public void onResponse(byte[] response) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(response, 0, response.length);
        if (null == bitmap) {
            Utils.showToast(getContext(), mType == SHARE_TO_WEIBO ?
                    R.string.weibo_share_fail : R.string.share_image_fail);
            return;
        }

        if (mType == SHARE_TO_WEIBO) {
            share2Weibo(response);
        } else {
            if (null != mFeed) {
                sendWebPage(mType, bitmap);
            } else {
                sendImage(mType, bitmap);
            }
        }
    }

    private void share2Weibo(byte[] bytes) {
        WbShareHandler shareHandler = new WbShareHandler((Activity) mContext);
        shareHandler.registerApp();

        WeiboMultiMessage message = new WeiboMultiMessage();

        String title, url, content;
        if (null != mFeed) {
            title = mFeed.title;
            url = String.format(ARTICLE_URL, mFeed.id);
            content = mFeed.summary;
            if (content.length() > 140) {
                content = content.substring(0, 138);
            }
        } else {
            title = getContext().getString(R.string.image_share);
            url = "http://www.u148.net/";
            content = getContext().getString(R.string.share_image_tip);
        }

        TextObject textObject = new TextObject();
        textObject.text = content;
        message.textObject = textObject;

        ImageObject imageObject = new ImageObject();
        imageObject.imageData = bytes;
        message.imageObject = imageObject;

        WebpageObject webpageObject = new WebpageObject();
        webpageObject.title = title;
        webpageObject.actionUrl = url;
        webpageObject.identify = "u148" + System.currentTimeMillis();

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        webpageObject.setThumbImage(getThumbBitmap(bitmap));
        shareHandler.shareMessage(message, false);
    }

    @Override
    public void onErrorResponse() {
        Utils.showToast(getContext(), mType == SHARE_TO_WEIBO ?
                R.string.weibo_share_fail : R.string.share_image_fail);
    }

    private void sendToQQFriends() {
        Bundle args = new Bundle();
        args.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        args.putString(QQShare.SHARE_TO_QQ_TITLE, mFeed.title);
        args.putString(QQShare.SHARE_TO_QQ_SUMMARY, mFeed.summary);
        args.putString(QQShare.SHARE_TO_QQ_TARGET_URL, String.format("http://www.u148.net/article/%1$s.html", mFeed.id));
        args.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, mImageList.get(0));

        mTencent.shareToQQ((Activity) mContext, args, mListener);
    }

    private void sendToQzone() {
        Bundle args = new Bundle();
        args.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        args.putString(QzoneShare.SHARE_TO_QQ_TITLE, mFeed.title);
        args.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, mFeed.summary);
        args.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, String.format("http://www.u148.net/article/%1$s.html", mFeed.id));
        args.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, mImageList);

        mTencent.shareToQzone((Activity) mContext, args, mListener);
    }

    private void sendWebPage(int type, Bitmap bitmap) {
        Bitmap thumb = getThumbBitmap(bitmap);

        WXWebpageObject webObject = new WXWebpageObject(String.format(ARTICLE_URL, mFeed.id));
        WXMediaMessage msg = new WXMediaMessage(webObject);
        msg.title = mFeed.title;
        msg.description = mFeed.summary;
        msg.thumbData = Utils.bmpToByteArray(thumb, true);

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
        msg.thumbData = Utils.bmpToByteArray(thumb, true);

        SendMessageToWX.Req request = new SendMessageToWX.Req();
        request.transaction = "image" + System.currentTimeMillis();
        request.message = msg;
        request.scene = type;

        mWXAPI.sendReq(request);
    }

    private void shareToWB() {
        String url = mImageList.get(0);
        NetworkRequest.getInstance().getBytes(url, this, this);
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

    @Override
    public void onClick(View v) {
        mType = (Integer) v.getTag();

        switch (mType) {
            case SHARE_TO_SESSION:
                shareToWX(SendMessageToWX.Req.WXSceneSession);
                break;
            case SHARE_TO_FRIENDS:
                shareToWX(SendMessageToWX.Req.WXSceneTimeline);
                break;
            case SHARE_TO_WEIBO:
                shareToWB();
                break;
            case SHARE_TO_QZONE:
                sendToQzone();
                break;
            case SHARE_TO_QQ:
                sendToQQFriends();
                break;
        }

        dismiss();
    }

    @Override
    public void onWbShareSuccess() {
        Utils.showToast(getContext(), R.string.weibo_share_success);
    }

    @Override
    public void onWbShareCancel() {

    }

    @Override
    public void onWbShareFail() {
        Utils.showToast(getContext(), R.string.weibo_share_fail);
    }
}
