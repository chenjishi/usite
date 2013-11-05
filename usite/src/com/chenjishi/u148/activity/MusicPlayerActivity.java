package com.chenjishi.u148.activity;

import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.StringUtil;
import com.chenjishi.u148.util.UIUtil;
import com.chenjishi.u148.volley.RequestQueue;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.BitmapLruCache;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.chenjishi.u148.volley.toolbox.Volley;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-7-8
 * Time: 下午10:06
 * To change this template use File | Settings | File Templates.
 */
public class MusicPlayerActivity extends BaseActivity implements View.OnClickListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener {
    private MediaPlayer mPlayer;
    private String mp3Url;
    private String albumName;
    private String artistName;
    private String songName;
    private String coverImageUrl;

    private SeekBar mSeekBar;
    private ImageButton mPlayBtn;

    private ImageLoader mImageLoader;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_play_layout);

        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            String url = bundle.getString("url");
            parseMp3Url(url);
        }

        mSeekBar = (SeekBar) findViewById(R.id.mediacontroller_seekbar);
        mSeekBar.setMax(1000);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(requestQueue, new BitmapLruCache(this));

        mPlayer = new MediaPlayer();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("音乐加载中...");
        progressDialog.show();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.music_play_layout;
    }

    @Override
    protected void backIconClicked() {
        finish();
    }

    private void parseMp3Url(final String src) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(src);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    URL secondUrl = new URL(conn.getHeaderField("Location"));
                    String queryString = secondUrl.getQuery();
                    if (!TextUtils.isEmpty(queryString)) {
                        String[] params = queryString.split("=");
                        Log.i("test", "url " + params[1]);
                        Document doc = Jsoup.connect(params[1]).get();
                        Elements cover = doc.getElementsByTag("album_cover");
                        if (cover.size() > 0) {
                            coverImageUrl = cover.get(0).text();
                        }
                        Elements album = doc.getElementsByTag("album_name");
                        if (album.size() > 0) {
                            albumName = album.get(0).text();
                        }

                        Elements song = doc.getElementsByTag("song_name");
                        if (null != song && song.size() > 0) {
                            songName = song.get(0).text();
                        }

                        Elements artist = doc.getElementsByTag("artist_name");
                        if (artist.size() > 0) {
                            artistName = artist.get(0).text();
                        }

                        Elements elements = doc.getElementsByTag("location");
                        if (elements.size() > 0) {
                            String location = elements.get(0).text();
                            mp3Url = getLink(location);
                        }
                    }

                } catch (IOException e) {
                }
            }
        };

        Runnable postAction = new Runnable() {
            @Override
            public void run() {
                play();
            }
        };

        UIUtil.runWithoutMessage(action, postAction);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.mediacontroller_play_pause) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                mPlayBtn.setImageResource(R.drawable.mediacontroller_play_button);
            } else {
                mPlayer.start();
                mPlayBtn.setImageResource(R.drawable.mediacontroller_pause_button);
            }
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mSeekBar.setProgress(percent * 10);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        setupView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
        mPlayer = null;
    }

    private void setupView() {
        progressDialog.dismiss();
        final ImageView imageView = (ImageView) findViewById(R.id.iv_album);
        TextView albumText = (TextView) findViewById(R.id.tv_album_name);
        TextView artistText = (TextView) findViewById(R.id.tv_artist_name);

        mPlayBtn = (ImageButton) findViewById(R.id.mediacontroller_play_pause);
        mPlayBtn.setOnClickListener(this);

        mImageLoader.get(coverImageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                imageView.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        String result1 = StringUtil.isEmpty(songName) ? "未知" : songName;
        String result2 = StringUtil.isEmpty(artistName) ? "未知" : artistName;

        albumText.setText(String.format(getString(R.string.music_album_unknown), result1));
        artistText.setText(String.format(getString(R.string.music_artist_unknown, result2)));
    }

    private void play() {
        if (TextUtils.isEmpty(mp3Url)) return;

        try {
            mPlayer.setOnBufferingUpdateListener(this);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(mp3Url);

            mPlayer.prepare();
            mPlayer.start();

        } catch (IOException e) {
        }
    }

    //get xiami's real mp3 path
    private String getLink(String location) {
        int loc_2 = Integer.valueOf(location.substring(0, 1));
        String loc_3 = location.substring(1);
        int loc_4 = (int) Math.floor(Double.valueOf(loc_3.length()) / loc_2);
        int loc_5 = loc_3.length() % loc_2;
        ArrayList<String> loc_6 = new ArrayList<String>();
        int loc_7 = 0;
        StringBuilder loc_8 = new StringBuilder();
        String loc_9 = "";
        int loc_10;

        while (loc_7 < loc_5) {
            int start = (loc_4 + 1) * loc_7;
            int end = start + loc_4 + 1;
            String str = loc_3.substring(start, end);
            loc_6.add(str);
            loc_7++;
        }

        loc_7 = loc_5;
        while (loc_7 < loc_2) {
            int start = loc_4 * (loc_7 - loc_5) + (loc_4 + 1) * loc_5;
            int end = start + loc_4;
            String str = loc_3.substring(start, end);
            loc_6.add(str);
            loc_7++;
        }

        loc_7 = 0;
        while (loc_7 < loc_6.get(0).length()) {
            loc_10 = 0;
            while (loc_10 < loc_6.size()) {
                String str = loc_6.get(loc_10);
                int len = str.length();
                if (loc_7 < len) {
                    loc_8.append(str.toCharArray()[loc_7]);
                }
                loc_10++;
            }
            loc_7++;
        }

        try {
            loc_9 = URLDecoder.decode(loc_8.toString(), "UTF-8");
            loc_9 = loc_9.replace('^', '0');
        } catch (UnsupportedEncodingException e) {
        }

        return loc_9;
    }
}
