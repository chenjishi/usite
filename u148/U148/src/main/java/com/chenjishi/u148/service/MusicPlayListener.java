package com.chenjishi.u148.service;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-10-27
 * Time: 下午11:35
 * To change this template use File | Settings | File Templates.
 */
public interface MusicPlayListener {

    public void onMusicStartParse();

    public void onMusicPrepared(String song, String artist);

    public void onMusicCompleted();

    public void onMusicParseError();
}
