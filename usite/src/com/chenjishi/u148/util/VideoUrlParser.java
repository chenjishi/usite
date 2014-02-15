package com.chenjishi.u148.util;

import android.text.TextUtils;
import com.chenjishi.u148.model.Video;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-2-2
 * Time: 下午9:38
 * To change this template use File | Settings | File Templates.
 */
public class VideoUrlParser {
    private static final String CONVERT_URL = "http://dservice.wandoujia.com/convert?target=%1$s&f=phoenix2&v=3.44.1&u=d83dc65e84c34305a1afee4a95879a35943891e2&vc=4513&ch=wandoujia_pc_baidu_pt&type=VIDEO";
    private static String youkuJson;

    public static Video getQiyiVideo(String url) {
        Video video = null;
        Pattern pattern = Pattern.compile("/(\\w+)\\.swf");
        Matcher matcher = pattern.matcher(url);

        String videoId = "";
        while (matcher.find()) {
            videoId = matcher.group(1);
        }
        if (TextUtils.isEmpty(videoId)) return null;

        String targetUrl = "http://www.iqiyi.com/" + videoId + ".html";
        String result = HttpUtils.getSync(String.format(CONVERT_URL, targetUrl));
        if (TextUtils.isEmpty(result)) return null;

        try {
            JSONObject jObj = new JSONObject(result);
            JSONArray jArray = jObj.getJSONArray("result");
            if (null != jArray && jArray.length() > 0) {
                video = new Video();

                video.originalUrl = targetUrl;
                JSONObject obj = jArray.getJSONObject(0);

                String redirectUrl = obj.getString("url");
                String result2 = HttpUtils.getSync(redirectUrl);

                pattern = Pattern.compile("l\":\"(.*?)\"");
                matcher = pattern.matcher(result2);
                while (matcher.find()) {
                    video.url = matcher.group(1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return video;
    }

    public static Video getTudouUrl(String str) {
        Video video = null;
        HttpURLConnection conn = null;
        URL url;

        try {
            url = new URL(str);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            String location = conn.getHeaderField("Location");

            Pattern p = Pattern.compile("lCode=(\\w+)&");
            Matcher m = p.matcher(location);
            String vid1 = "";
            while (m.find()) {
                vid1 = m.group(1);
            }

            p = Pattern.compile("code=(\\w+)&");
            m = p.matcher(location);
            String vid2 = "";

            while (m.find()) {
                vid2 = m.group(1);
            }

            if (TextUtils.isEmpty(vid1)) return null;

            String link = String.format("http://www.tudou.com/listplay/%1$s/%2$s.html", vid1, vid2);
            String requestUrl = String.format(CONVERT_URL, link);
            String result = HttpUtils.getSync(requestUrl);

            if (TextUtils.isEmpty(result)) return null;

            JSONObject jObj = new JSONObject(result);
            video = new Video();

            video.originalUrl = jObj.getString("target");
            JSONArray jArr = jObj.getJSONArray("result");

            if (null != jArr && jArr.length() > 0) {
                JSONObject _obj = jArr.getJSONObject(0);
                video.url = _obj.getString("url");
            }
        } catch (IOException e) {
            video = null;
        } catch (JSONException e) {
            video = null;
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }

        return video;
    }


    public static Video get56VideoPath(String url) {
        Video video = null;

        Pattern p = Pattern.compile("(v_|cpm_)(\\w+)\\.swf");

        String vId = null;
        Matcher m = p.matcher(url);
        while (m.find()) vId = m.group(2);

        if (!TextUtils.isEmpty(vId)) {
            video = new Video();
            video.id = vId;
            String[] fileUrls = null;
            String json = HttpUtils.getSync(String.format("http://vxml.56.com/json/%s/?src=site", vId));
            if (!TextUtils.isEmpty(json)) {
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject dataObj = obj.getJSONObject("info");

                    video.thumbUrl = dataObj.optString("img", "");
                    video.title = dataObj.optString("Subject", "");

                    JSONArray arr = dataObj.getJSONArray("rfiles");
                    int len = arr.length();
                    fileUrls = new String[len];
                    for (int i = 0; i < len; i++) {
                        JSONObject _obj = arr.getJSONObject(i);
                        String filePath = _obj.optString("url", "");
                        if (filePath.length() > 0)
                            fileUrls[i] = filePath;
                    }
                } catch (JSONException e) {
                }
            }

            if (null != fileUrls && fileUrls.length > 0) {
                video.url = fileUrls[0];
            }
        }

        return video;
    }

    public static String getSinaUrl(String videoUrl) {
        String result = null;
        String videoId = null;
        String regex = "vid=(\\d+)_\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(videoUrl);

        if (matcher.find()) videoId = matcher.group(1);

        ArrayList<String> paths = null;
        try {
            paths = new ArrayList<String>();
            String url = "http://v.iask.com/v_play.php?vid=" + videoId;
            Connection c = Jsoup.connect(url);
            Document doc = c.get();
            Elements ems = doc.getElementsByTag("url");
            for (Element em : ems) paths.add(em.text());

            if (paths.size() > 0) result = paths.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String ParserQQvideo(String vid) throws IOException {
        String url = "http://vv.video.qq.com/geturl?ran=0.16436194255948067&otype=xml&vid=" + vid + "&platform=1&format=2";
        Connection c = Jsoup.connect(url);
        Document doc = c.get();
        Elements ems = doc.getElementsByTag("url");
        String vurls[] = ems.text().split("\\?");
        return vurls[0];
    }

    public static ArrayList<String> ParserQQvideof(String vid) throws IOException {
        String url = "http://web.qqvideo.tc.qq.com/" + vid + ".flv";
        ArrayList<String> urls = new ArrayList<String>();
        urls.add(url);
        return urls;
    }

    public static String ParserTudouvideo(String iid) throws IOException {
        String url = "http://v2.tudou.com/v?st=1%2C2%2C3%2C4%2C99&it=" + iid;
        Connection c = Jsoup.connect(url);
        Document doc = c.get();
        Elements ems = doc.getElementsByTag("f");

        for (Element em : ems) {
            em.attr("brt");
            String vurl[] = em.text().split("\\?");
        }
        return iid;
    }

    public static Video getYoukuUrl(String url) {
        Video video = null;
        String vId = null;
        String regex = "sid\\/(\\w+)\\/";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) vId = matcher.group(1);

        if (!TextUtils.isEmpty(vId)) {
            video = new Video();
            video.id = vId;
            try {
                String fileUrl = parseYouku(vId);
                if (null != fileUrl) video.url = fileUrl;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (null != youkuJson) {
            try {
                JSONObject obj = new JSONObject(youkuJson);
                JSONArray dataArray = obj.getJSONArray("data");
                if (null != dataArray && dataArray.length() > 0) {
                    JSONObject dataObj = dataArray.getJSONObject(0);
                    video.thumbUrl = dataObj.optString("logo", "");
                    video.title = dataObj.optString("title", "");
                }
            } catch (JSONException e) {
            }
        }

        return video;
    }

    private static String parseYouku(String id) throws Exception {
        double seed = 0;
        String key1;
        String key2;
        String fileids = null;
        String fileid = null;
        ArrayList<String> K = new ArrayList<String>();
        URL url = new URL(
                "http://v.youku.com/player/getPlayList/VideoIDS/" + id + "/timezone/+08/version/5/source/video?n=3&ran=4656");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(6 * 1000);
        if (conn.getResponseCode() != 200)
            throw new RuntimeException("请求url失败");
        InputStream is = conn.getInputStream();
        String jsonstring = readData(is, "UTF8");
        youkuJson = jsonstring;
        conn.disconnect();

        String regexstring = "\"seed\":(\\d+),.+\"key1\":\"(\\w+)\",\"key2\":\"(\\w+)\"";
        Pattern pattern = Pattern.compile(regexstring);
        Matcher matcher = pattern.matcher(jsonstring);
        while (matcher.find()) {
            seed = Double.parseDouble(matcher.group(1));
            key1 = matcher.group(2);
            key2 = matcher.group(3);
        }

        Pattern patternf = Pattern.compile("\"streamfileids\":\\{(.+?)\\}");

        Matcher matcherf = patternf.matcher(jsonstring);
        while (matcherf.find()) {
            fileids = matcherf.group(1);
        }

        Pattern patternfid = Pattern.compile("\"flv\":\"(.+?)\"");
        Matcher matcherfid = patternfid.matcher(fileids);
        while (matcherfid.find()) {
            fileid = matcherfid.group(1);
        }

        String no = null;
        Pattern patternc = Pattern.compile("\"flv\":\\[(.+?)\\]");
        Matcher matcherc = patternc.matcher(jsonstring);
        while (matcherc.find()) {
            no = matcherc.group(0);
        }

        JSONArray array = new JSONArray(no.substring(6));

        for (int i = 0; i < array.length(); i++) {
            JSONObject job = (JSONObject) array.get(i);
            K.add("?K=" + job.getString("k") + ",k2:" + job.getString("k2"));
        }

        String sid = genSid();
        //生成fileid
        String rfileid = getFileID(fileid, seed);
        ArrayList<String> paths = new ArrayList<String>();
        for (int i = 0; i < K.size(); i++) {
            //得到地址
            String u = "http://f.youku.com/player/getFlvPath/sid/" + "00" + "_" + String.format("%02d", i) +
                    "/st/" + "flv" + "/fileid/" + rfileid.substring(0, 8) + String.format("%02d", i)
                    + rfileid.substring(10) + K.get(i);
            paths.add(u);
        }

        ArrayList<String> rpaths = new ArrayList<String>();
        for (String path : paths) {
            rpaths.add(getLocationJump(path, false, false));
        }

        if (rpaths.size() > 0) {
            return rpaths.get(0);
        } else {
            return null;
        }
    }

    public static ArrayList<String> parserYoukuFlv(String id) throws Exception {
        double seed = 0;
        String key1;
        String key2;
        String fileids = null;
        String fileid = null;
        ArrayList<String> K = new ArrayList<String>();
        URL url = new URL(
                "http://v.youku.com/player/getPlayList/VideoIDS/" + id + "/timezone/+08/version/5/source/video?n=3&ran=4656");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(6 * 1000);
        if (conn.getResponseCode() != 200)
            throw new RuntimeException("请求url失败");
        InputStream is = conn.getInputStream();
        String jsonstring = readData(is, "UTF8");
        conn.disconnect();

        String regexstring = "\"seed\":(\\d+),.+\"key1\":\"(\\w+)\",\"key2\":\"(\\w+)\"";
        Pattern pattern = Pattern.compile(regexstring);
        Matcher matcher = pattern.matcher(jsonstring);
        while (matcher.find()) {
            seed = Double.parseDouble(matcher.group(1));
            key1 = matcher.group(2);
            key2 = matcher.group(3);
        }

        Pattern patternf = Pattern.compile("\"streamfileids\":\\{(.+?)\\}");

        Matcher matcherf = patternf.matcher(jsonstring);
        while (matcherf.find()) {
            fileids = matcherf.group(1);
        }

        Pattern patternfid = Pattern.compile("\"flv\":\"(.+?)\"");
        Matcher matcherfid = patternfid.matcher(fileids);
        while (matcherfid.find()) {
            fileid = matcherfid.group(1);
        }

        String no = null;
        Pattern patternc = Pattern.compile("\"flv\":\\[(.+?)\\]");
        Matcher matcherc = patternc.matcher(jsonstring);
        while (matcherc.find()) {
            no = matcherc.group(0);
        }

        JSONArray array = new JSONArray(no.substring(6));

        for (int i = 0; i < array.length(); i++) {
            JSONObject job = (JSONObject) array.get(i);
            K.add("?K=" + job.getString("k") + ",k2:" + job.getString("k2"));
        }

        String sid = genSid();
        //生成fileid
        String rfileid = getFileID(fileid, seed);
        ArrayList<String> paths = new ArrayList<String>();
        for (int i = 0; i < K.size(); i++) {
            //得到地址
            String u = "http://f.youku.com/player/getFlvPath/sid/" + "00" + "_" + String.format("%02d", i) +
                    "/st/" + "flv" + "/fileid/" + rfileid.substring(0, 8) + String.format("%02d", i)
                    + rfileid.substring(10) + K.get(i);
            paths.add(u);
        }

        ArrayList<String> rpaths = new ArrayList<String>();
        for (String path : paths) {
            rpaths.add(getLocationJump(path, false, false));
        }

        return rpaths;
    }

    public static String getLocationJump(String httpurl, String agent, boolean followRedirects) {
        String location = httpurl;
        try {
            URL url = new URL(httpurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (!followRedirects) {
                conn.setInstanceFollowRedirects(false);
                conn.setFollowRedirects(false);
            }

            conn.addRequestProperty("User-Agent", agent);
            conn.setRequestProperty("User-Agent", agent);
            location = conn.getHeaderField("Location");
            if (location == null) {
                location = httpurl;
            }
            if (!location.equalsIgnoreCase(httpurl)) {
                location = getLocationJump(location, agent, followRedirects);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return location;
    }

    public static String getLocationJump(String paramString, boolean paramBoolean1, boolean paramBoolean2) {
        String str = "Lavf52.106.0";
        if (!paramBoolean1)
            str = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.151 Safari/534.16";
        return getLocationJump(paramString, str, paramBoolean2);
    }


    public static String ParserYoukuvideo(String id) throws Exception {

        URL url = new URL(
                "http://v.youku.com/player/getPlayList/VideoIDS/" + id + "/timezone/+08/version/5/source/video?n=3&ran=4656");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(6 * 1000);
        if (conn.getResponseCode() != 200)
            throw new RuntimeException("请求url失败");
        InputStream is = conn.getInputStream();
        String jsonstring = readData(is, "UTF8");
        conn.disconnect();
        JSONObject jsonObject = new JSONObject(jsonstring);

        JSONArray jsarray = jsonObject.getJSONArray("data");
        JSONObject obj = (JSONObject) jsarray.get(0);
        Double seed = obj.getDouble("seed");
        String key1 = obj.getString("key1");
        String key2 = obj.getString("key2");
        JSONObject obj1 = obj.getJSONObject("streamfileids");
        String flvfileid = obj1.getString("flv");
        String mp4fileid = obj1.getString("mp4");
        JSONObject obj2 = obj.getJSONObject("segs");
        JSONObject objflv = (JSONObject) obj2.getJSONArray("flv").get(0);
        JSONObject objmp4 = (JSONObject) obj2.getJSONArray("mp4").get(0);

        int flvno = objflv.getInt("no");
        String flvk = objflv.getString("k");

        int mp4no = objmp4.getInt("no");
        String mp4k = objmp4.getString("k");

        for (int i = 0; i < mp4no + 1; i++) {
            //得到地址
            String u = "http://f.youku.com/player/getFlvPath/sid/" + genSid() + "_" + String.format("%02d", i) +
                    "/st/" + "flv" + "/fileid/" + getFileID(flvfileid, seed).substring(0, 8) + String.format("%02d", i)
                    + getFileID(flvfileid, seed).substring(10) + "?K=" + flvk;
        }
        return id;
    }


    public static String genKey(String key1, String key2) {
        int key = Long.valueOf("key1", 16).intValue();
        key ^= 0xA55AA5A5;
        return "key2" + Long.toHexString(key);
    }

    public static String getFileIDMixString(double seed) {
        StringBuilder mixed = new StringBuilder();
        StringBuilder source = new StringBuilder(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/\\:._-1234567890");
        int index, len = source.length();
        for (int i = 0; i < len; ++i) {
            seed = (seed * 211 + 30031) % 65536;
            index = (int) Math.floor(seed / 65536 * source.length());
            mixed.append(source.charAt(index));
            source.deleteCharAt(index);
        }
        return mixed.toString();
    }

    public static String getFileID(String fileid, double seed) {
        String mixed = getFileIDMixString(seed);
        String[] ids = fileid.split("\\*");
        StringBuilder realId = new StringBuilder();
        int idx;
        for (int i = 0; i < ids.length; i++) {
            idx = Integer.parseInt(ids[i]);
            realId.append(mixed.charAt(idx));
        }
        return realId.toString();
    }

    public static String genSid() {
        int i1 = (int) (1000 + Math.floor(Math.random() * 999));
        int i2 = (int) (1000 + Math.floor(Math.random() * 9000));
        return System.currentTimeMillis() + "" + i1 + "" + i2;
    }


    public static String readData(InputStream inSream, String charsetName) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inSream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inSream.close();
        return new String(data, charsetName);
    }

    //    private Video getQiyiVideo(String url) {
//        Pattern pattern = Pattern.compile("qiyi\\.com/(\\w+)/");
//        Matcher matcher = pattern.matcher(url);
//
//        String videoId = "";
//        while (matcher.find()) {
//            videoId = matcher.group(1);
//        }
//
//        if (TextUtils.isEmpty(videoId)) return null;
//
//        Video video = null;
//        HttpURLConnection conn = null;
//        try {
//            Document doc = Jsoup.connect("http://cache.video.qiyi.com/v/" + videoId).get();
//            if (null == doc) return null;
//
//            Elements fileUrls = doc.getElementsByTag("file");
//            if (null != fileUrls && fileUrls.size() > 0) {
//                String fileUrl = fileUrls.get(0).text();
//
//                pattern = Pattern.compile("/(\\w+)\\.f4v");
//                matcher = pattern.matcher(fileUrl);
//                String videoId2 = "";
//                while (matcher.find()) {
//                    videoId2 = matcher.group(1);
//                }
//
//                if (TextUtils.isEmpty(videoId2)) return null;
//
//                conn = (HttpURLConnection) (new URL("http://data.video.qiyi.com/" + videoId2 + ".ts").openConnection());
//                conn.setInstanceFollowRedirects(false);
//                conn.connect();
//                String location = conn.getHeaderField("Location");
//
//                if (TextUtils.isEmpty(location)) return null;
//
//                pattern = Pattern.compile("key=(\\w+)");
//                matcher = pattern.matcher(location);
//                String key = "";
//                while (matcher.find()) {
//                    key = matcher.group(1);
//                }
//
//                if (TextUtils.isEmpty(key)) return null;
//
//                video = new Video();
//                video.url = fileUrl + "?key=" + key;
//
//                String title = doc.getElementsByTag("title").get(0).ownText();
//                title = title.replace("<![CDATA[", "");
//                title = title.replace("]]>", "");
//                video.title = title;
//                video.thumbUrl = doc.getElementsByTag("pic").get(0).ownText();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (null != conn) {
//                conn.disconnect();
//            }
//        }
//
//        return video;
//    }
}
