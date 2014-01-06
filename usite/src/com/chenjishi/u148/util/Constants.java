package com.chenjishi.u148.util;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午6:59
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static final int SOURCE_U148 = 100;
    public static final int SOURCE_JIANDAN = 101;
    public static final int SOURCE_NEWS = 102;

    public static final String WX_APP_ID = "wxf862baa09e0df157";
    public static final String WEIBO_APP_KEY = "1792649719";
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";

    public static final String BASE_URL = "http://www.u148.net";
    public static final String BASE_NEWS_URL = "http://news.cnblogs.com";

    public static final String CACHED_FILE_NAME = "u148.txt";
    public static final String CACHED_JIANDAN = "jiandan";
    public static final String CACHED_NEWS = "news";

    public static final String DOWNLOAD_STATUS_ACTION = "com.waqu.android.DOWNLOAD_STATUS";

    public static final int MSG_DOWNLOAD_ERROR = 1;
    public static final int MSG_DOWNLOAD_OVER = 2;
    public static final int MSG_DOWNLOAD_START = 3;
    public static final int MSG_DOWNLOAD_READY = 4;
    public static final int MSG_NO_UPDATE = 5;
    public static final int MSG_NO_SPACE_AVAILABLE = 6;

    public static final String KEY_VIDEO_SIZE = "video_size";
    public static final String KEY_MESSAGE_TYPE = "message_type";
    public static final int MSG_DOWNLOAD_SUCCESS = 0;

    public static final String EVENT_VIDEO_SHARE = "VideoShare";
    public static final String EVENT_ARTICLE_SHARE = "ArticleShare";
    public static final String EVENT_IMAGE_SHARE = "ArticleShare";
    public static final String EVENT_COMMENT_CLICK = "CommentClick";

    public static final String PARAM_TITLE = "title";
    public static final String PARAM_URL = "url";

    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";
}
