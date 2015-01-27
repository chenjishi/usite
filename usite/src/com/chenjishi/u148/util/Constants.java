package com.chenjishi.u148.util;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午6:59
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static final String KEY_FEED = "feed";
    public static final String KEY_FEED_LIST = "feed_list";
    public static final String KEY_FEED_INDEX = "index";

    public static final int MODE_DAY = 0;
    public static final int MODE_NIGHT = 1;

    public static final String EVENT_ARTICLE_SHARE = "ArticleShare";
    public static final String EVENT_IMAGE_SHARE = "ArticleShare";

    public static final String PARAM_TITLE = "title";
    public static final String PARAM_URL = "url";

    public static final String BASE_URL = "http://api.u148.net/json/";

    public static final String API_FEED_LIST = BASE_URL + "%1$d/%2$d";
    public static final String API_ARTICLE = BASE_URL + "article/%1$s";
    public static final String API_ADD_FAVORITE = BASE_URL + "favourite?id=%1$s&token=%2$s";
    public static final String API_DELETE_FAVORITE = BASE_URL + "del_favourite?id=%1$s&token=%2$s";
    public static final String API_LOGIN = BASE_URL + "login";
    public static final String API_REGISTER = BASE_URL + "register";
    public static final String API_COMMENTS_GET = BASE_URL + "get_comment/%1$s/%2$d";
    public static final String API_COMMENT_POST = BASE_URL + "comment";
    public static final String API_FAVORITE_GET = BASE_URL + "get_favourite/0/%1$d?token=%2$s";
    public static final String API_UPGRADE = BASE_URL + "version";
}
