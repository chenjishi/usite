package com.chenjishi.u148.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.QQAuthToken;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.util.Utils;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.openapi.*;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-6-17.
 */
public class ShareDialog extends Dialog implements View.OnClickListener {
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

    private Context mContext;

    private IWXAPI mWXAPI;
    private Feed mFeed;

    private ArrayList<String> mImageList;

    private Tencent mTencent;

    public ShareDialog(Context context) {
        super(context, R.style.FullHeightDialog);

        mContext = context;

        setCanceledOnTouchOutside(true);

        mWXAPI = WXAPIFactory.createWXAPI(context, WX_APP_ID);
        mWXAPI.registerApp(WX_APP_ID);

        mTencent = Tencent.createInstance(QQ_APP_ID, context.getApplicationContext());

        int paddingTop = Utils.dp2px(context, 12.f);
        LinearLayout container = new LinearLayout(context);
        container.setBackgroundColor(0xFFFFFFFF);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setWeightSum(5.f);
        container.setPadding(0, paddingTop, 0, paddingTop);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        int[] iconIds = {R.drawable.ic_session, R.drawable.ic_friend, R.drawable.ic_weibo,
                R.drawable.ic_qqzone, R.drawable.ic_qq};
        int[] nameIds = {R.string.share_session, R.string.share_friend, R.string.share_weibo,
                R.string.qqzone, R.string.qq_friends};

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
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(layoutParams);
    }

    public void setShareFeed(Feed feed) {
        mFeed = feed;
    }

    public void setImageList(ArrayList<String> imageList) {
        mImageList = imageList;
    }

    private void shareToWX(final int type) {
        requestImage(mImageList.get(0), type);
    }

    private void requestImage(String url, final int type) {
        DataSubscriber subscriber = new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                if (null != bitmap && !bitmap.isRecycled()) {
                    if (null != bitmap) {
                        if (null != mFeed) {
                            sendWebPage(type, bitmap);
                        } else {
                            sendImage(type, bitmap);
                        }
                    }
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                Utils.showToast(R.string.share_image_fail);
            }
        };

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url));
        ImageRequest request = builder.build();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(request, this);
        dataSource.subscribe(subscriber, UiThreadImmediateExecutorService.getInstance());
    }


    private void shareToQzone() {
        QQAuthToken authToken = PrefsUtil.getQQAuthToken();

        if (authToken.invalid()) {
            mTencent.setOpenId(authToken.open_id);
            long expireTime = (authToken.expires_in - System.currentTimeMillis()) / 1000;
            mTencent.setAccessToken(authToken.access_token, String.valueOf(expireTime));
            sendToQzone();
        } else {
            mTencent.login((Activity) mContext, "all", new IUiListener() {
                @Override
                public void onComplete(Object o) {
                    String result = o.toString();
                    if (!TextUtils.isEmpty(result)) {
                        saveQQToken(result);
                        sendToQzone();
                    } else {
                        Utils.showToast("QQ登陆失败，请稍后再试");
                    }
                }

                @Override
                public void onError(UiError uiError) {
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    private void saveQQToken(String json) {
        QQAuthToken authToken = new QQAuthToken();

        try {
            JSONObject jObj = new JSONObject(json);
            authToken.open_id = jObj.optString("openid", "");
            authToken.access_token = jObj.optString("access_token", "");
            authToken.expires_in = jObj.optLong("expires_in", 0);

            PrefsUtil.putQQAuthToken(authToken);
        } catch (JSONException e) {
        }
    }

    private void sendToQQFriends() {
        Bundle bundle = new Bundle();
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, mFeed.title);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, mFeed.summary);
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, String.format("http://www.u148.net/article/%1$s.html", mFeed.id));
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, mImageList.get(0));

        mTencent.shareToQQ((Activity) mContext, bundle, new IUiListener() {
            @Override
            public void onComplete(Object o) {
            }

            @Override
            public void onError(UiError uiError) {
            }

            @Override
            public void onCancel() {
            }
        });

    }

    private void sendToQzone() {
        Bundle bundle = new Bundle();
        bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, mFeed.title);
        bundle.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, mFeed.summary);
        bundle.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, String.format("http://www.u148.net/article/%1$s.html", mFeed.id));
        bundle.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, mImageList);

        mTencent.shareToQzone((Activity) mContext, bundle, new IUiListener() {
            @Override
            public void onComplete(Object o) {
            }

            @Override
            public void onError(UiError uiError) {
            }

            @Override
            public void onCancel() {
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
            case SHARE_TO_QZONE:
                sendToQzone();
                break;
            case SHARE_TO_QQ:
                sendToQQFriends();
                break;
        }

        dismiss();
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
