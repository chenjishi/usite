package com.chenjishi.u148.promotions;

import com.google.gson.annotations.SerializedName;

/**
 * Created by chenjishi on 15/9/9.
 */
public class Reply {

    @SerializedName("rid")
    public int replyId;

    @SerializedName("tid")
    public int topicId;

    @SerializedName("uid")
    public int userId;

    public String name;

    public String avatar;

    public String content;

    @SerializedName("create_time")
    public long createTime;
}
