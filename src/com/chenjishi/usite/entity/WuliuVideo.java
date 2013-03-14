package com.chenjishi.usite.entity;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-2-2
 * Time: 下午10:30
 * To change this template use File | Settings | File Templates.
 */
public class WuliuVideo {
    public long fileSize;
    public long totalTime;
    public String url;
    public String type;

    public WuliuVideo(){

    }

    public WuliuVideo(long fileSize, long totalTime, String url, String type) {
        this.fileSize = fileSize;
        this.totalTime = totalTime;
        this.url = url;
        this.type = type;
    }
}
