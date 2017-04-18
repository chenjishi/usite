package com.chenjishi.u148.utils;

/**
 * Created by jishichen on 2017/4/14.
 */
public interface Constants {

    String KEY_FEED = "feed";
    String KEY_FEED_LIST = "feed_list";
    String KEY_FEED_INDEX = "index";

    int MODE_DAY = 0;
    int MODE_NIGHT = 1;

    String EVENT_ARTICLE_SHARE = "ArticleShare";
    String EVENT_IMAGE_SHARE = "ArticleShare";

    String PARAM_TITLE = "title";
    String PARAM_URL = "url";

    String BASE_URL = "http://api.u148.net/json/";

    String API_FEED_LIST = BASE_URL + "%1$d/%2$d";
    String API_ARTICLE = BASE_URL + "article/%1$s";
    String API_ADD_FAVORITE = BASE_URL + "favourite?id=%1$s&token=%2$s";
    String API_DELETE_FAVORITE = BASE_URL + "del_favourite?id=%1$s&token=%2$s";
    String API_LOGIN = BASE_URL + "login";
    String API_REGISTER = BASE_URL + "register";
    String API_COMMENTS_GET = BASE_URL + "get_comment/%1$s/%2$d";
    String API_COMMENT_POST = BASE_URL + "comment";
    String API_FAVORITE_GET = BASE_URL + "get_favourite/0/%1$d?token=%2$s";
    String API_UPGRADE = BASE_URL + "version";
    String API_SEARCH = BASE_URL + "search/%1$d?keyword=%2$s";
    String XIAO_MI = "http://app.mi.com/details?id=com.chenjishi.u148&ref=search";
}
