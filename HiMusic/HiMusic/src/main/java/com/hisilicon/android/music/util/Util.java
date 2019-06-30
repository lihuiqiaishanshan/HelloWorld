package com.hisilicon.android.music.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.input.BoundedInputStream;

import com.hisilicon.android.music.MediaPlaybackActivity;
import com.hisilicon.android.music.Common;

import android.os.Environment;
import android.util.Log;

/**
 * A utility class, is mainly responsible for the analysis of lyricsAnd find
 * lyrics download, and then save the file into a standard formatThere are some
 * commonly used method
 * @author
 */
public final class Util {

//    public static final String VERSION = "1.2"; for fority
    private static String proxyHost = "";
    private static String proxyPort = "";
    private static String proxyUserName = "";
    private static String proxyPwd = "";
    private static final String TAG = "HiMusic_Util";

    private Util() {
    }

//    /**
//     * According to the remote access to the version and the version now
//     * contrast can update
//     * @param remote
//     *            version
//     * @return is updated
//     */
//    private static boolean canUpdate(String version) {
//        if (version == null) {
//            return false;
//        }
//
//        return VERSION.compareTo(version) < 0;
//    }

    /**
     * A simple method to get pass, the singer and the lyrics of the title
     * search results, return to a list form
     * @param artist
     * @param title
     *            , not null
     * @return
     */
    public static List<SearchResult> getSearchResults(String artist,
                                                      String title) {
        List<SearchResult> list = new ArrayList<SearchResult>();

        try {
            list = LrcUtil.search(artist, title);
        }
         catch (IllegalArgumentException ex) {
            Log.e(TAG,"getSearchResults",ex);
        }
         catch (IOException ex) {
            Log.e(TAG,"getSearchResults",ex);
        }
         catch (ClassCastException ex) {
            Log.e(TAG,"getSearchResults",ex);
        }
        catch (UnsupportedOperationException ex) {
            Log.e(TAG,"getSearchResults",ex);
        }

        return list;
    }

    /**
     * From a int worth it to represent the number of bytes
     * @param i
     * @return byte[]
     */
    public static byte[] getBytesFromInt(int i) {
        byte[] data = new byte[4];
        data[0] = (byte)(i & 0xff);
        data[1] = (byte)((i >> 8) & 0xff);
        data[2] = (byte)((i >> 16) & 0xff);
        data[3] = (byte)((i >> 24) & 0xff);
        return data;
    }

    /**
     * A simple method, turn into another string of a string
     * @param source
     * @param encoding
     * @return String
     */
    public static String convertString(String source, String encoding) {
        try {
            byte[] data = source.getBytes("ISO8859-1");
            return new String(data, encoding);
        } catch (UnsupportedEncodingException ex) {
            return source;
        }
    }

    /**
     * A convenient method of transcoding
     * @param source
     * @param sourceEnc
     * @param distEnc
     * @return
     */
    public static String convertString(String source, String sourceEnc,
                                       String distEnc) {
        try {
            byte[] data = source.getBytes(sourceEnc);
            return new String(data, distEnc);
        } catch (UnsupportedEncodingException ex) {
            return source;
        }
    }

    /**
     * This array of integer size from incoming number
     * @param data
     *            byte[]
     * @return integer
     */
    public static int getInt(byte[] data) {
        if (data.length != 4) {
            throw new IllegalArgumentException(
                "The length of the array to be illegal, length 4!");
        }

        return (data[0] & 0xff) | ((data[1] & 0xff) << 8)
               | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24);
    }

    /**
     * The byte array composed of long integer results from incoming byte array
     * @param data
     *            byte[]
     * @return long
     */
    /*public static long getLong(byte[] data) {
        if (data.length != 8) {
            throw new IllegalArgumentException(
                "The length of the array to be illegal, length 4!");
        }

        return (data[0] & 0xff) | ((data[1] & 0xff) << 8)
               | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24)
               | ((data[4] & 0xff) << 32) | ((data[5] & 0xff) << 40)
               | ((data[6] & 0xff) << 48) | ((data[7] & 0xff) << 56);
    }*/

    /**
     * Get file Type
     * @param f
     *            File
     * @return Type
     */
    public static String getType(File f) {
        String name = f.getName();
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * According to the file return the name of the song
     * @param f
     *            file
     * @return the name of the song
     */
    public static String getSongName(File f) {
        String name = f.getName();
        name = name.substring(0, name.lastIndexOf("."));
        return name;
    }

    /**
     * According to the file name return the name of the song
     * @param name
     *            file name
     * @return the name of the song
     */
    public static String getSongName(String name) {
        try {
            int index = name.lastIndexOf(File.separator);
            name = name.substring(index + 1, name.lastIndexOf("."));
            return name;
        } catch (IndexOutOfBoundsException exe) {
            return name;
        }
    }

    /**
     * According to the information content of the lyrics of songs to download
     * @param info
     * @return Lyric
     */
    public static String getLyric(Audio info) throws IOException {
        String ly = getLyricInternet(info);

        return ly;
    }

    /**
     * The number of seconds to convert a string like 00:00
     * @param sec
     *            seconds
     * @return string
     */
    public static String secondToString(int sec) {
        DecimalFormat df = new DecimalFormat("00");
        StringBuilder sb = new StringBuilder();
        sb.append(df.format(sec / 60)).append(":").append(df.format(sec % 60));
        return sb.toString();
    }

    /**
     * Get to the lyrics in Baidu search content
     * @param key
     * @return content
     * @throw IOException
     */
    private static String getInternet_Lyric(String key, String artist) throws IOException {
        URL url = null;
        StringBuffer sb = new StringBuffer();
        // Lynnfield if Chinese characters, then to encode conversion
        String key_url = "";
        String artist_url = "";

        try {
            key_url = URLEncoder.encode(key, "utf-8");
            artist_url = URLEncoder.encode(artist, "utf-8");
        } catch (UnsupportedEncodingException e2) {
            // TODO Auto-generated catch block
            Log.e(TAG,"URLEncoder.encode",e2);
        }

        String strUrl = "http://box.zhangmen.baidu.com/x?op=12&count=1&title="
                        + key_url + "$$" + artist_url + "$$$$";

        try {
            url = new URL(strUrl);
        } catch (MalformedURLException e1) {
            Log.e(TAG,"new URL",e1);
        }
        if (url == null) {
            Log.e(TAG,"error,url is null!");
            return "";
        }
        BoundedInputStream bis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        String s;

        try {
            HttpURLConnection httpConn = (HttpURLConnection) url
                                         .openConnection();
            httpConn.connect();
            bis = new BoundedInputStream(httpConn.getInputStream(), 8096);
            isr = new InputStreamReader(bis);
            br = new BufferedReader(isr);
            if (br == null) {
                Log.e("HiMusic","error,br is null!");
                return "";
            }

            while ((s = Common.readLine(br)) != null) {
                sb.append(s + "/r/n");
                br.close();
            }
        } catch (IOException e1) {
            Log.e(TAG,"sb.append",e1);
        } finally {
            try{
                if(bis != null) bis.close();
            }catch(IOException nohandle){
                Log.e(TAG,"getInternet_Lyric bis close",nohandle);
            }
            try{
                if(isr != null) isr.close();
            }catch(IOException nohandle){
                Log.e(TAG,"getInternet_Lyric isr close",nohandle);
            }
            try{
                if(br != null) br.close();
            }catch(IOException nohandle){
                Log.e(TAG,"getInternet_Lyric br close",nohandle);
            }

        }


        int begin = 0, end = 0, number = 0;
        String strid = "";
        begin = sb.indexOf("<lrcid>");

        try {
            if (begin != -1) {
                end = sb.indexOf("</lrcid>", begin);
                strid = sb.substring(begin + 7, end);
                number = Integer.parseInt(strid);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG,"Integer.parseInt error");
            return "";
        }

        String geciURL = "http://box.zhangmen.baidu.com/bdlrc/" + number / 100
                         + "/" + number + ".lrc";
        String ss = new String();

        try {
            url = new URL(geciURL);
        } catch (MalformedURLException e2) {
            Log.e(TAG,"geciURL",e2);
        }

        StringBuffer sbLrc = new StringBuffer();
        ArrayList<String> gcContent = new ArrayList<String>();
        bis = new BoundedInputStream(url.openStream(), 8096);
        isr = new InputStreamReader(bis,"GB2312");
        br = new BufferedReader(isr);

        if (br == null) {
        } else {
            try {
                while ((ss = Common.readLine(br)) != null) {
                    sbLrc.append(ss);
                    gcContent.add(ss);
                }

                saveLyric(gcContent, key, artist);
            } finally {
                try{
                    if(bis != null) bis.close();
                }catch(IOException nohandle){
                    Log.e(TAG,"saveLyric",nohandle);
                }
                try{
                    if(isr != null) isr.close();
                }catch(IOException nohandle){
                    Log.e(TAG,"saveLyric_isr",nohandle);
                }
                try {
                if(br != null) br.close();
                } catch(IOException nohandle) {
                    Log.e(TAG,"saveLyric brs",nohandle);
                }
            }
        }

        return sbLrc.toString();
    }

    /**
     * Download the lyrics saved, lest the next to find
     * @param lrcList
     * @param title
     * @param artist
     */
    private static void saveLyric(ArrayList<String> lrcList, String title,
                                  String artist) {
        BufferedWriter bw = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            // If the singer is not empty, the singers name + song for the best
            // combination
            String name = artist + "-" + title + ".lrc";
            File dir = new File(MediaPlaybackActivity.getHOME(), "Lyrics"
                                + File.separator);
            if (dir == null || !dir.mkdirs())
                return;
            File file = new File(dir, name);
            file.setWritable(true,true);
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "GBK");
            bw = new BufferedWriter(osw);

            for (int i = 0; i < lrcList.size(); i++) {
                bw.write(lrcList.get(i));
                bw.newLine();
            }
        } catch (SecurityException e) {
            Log.e(TAG,"saveLyric : SecurityException");
        } catch (FileNotFoundException e) {
            Log.e(TAG,"saveLyric : FileNotFoundException");
        } catch (IOException e) {
            Log.e(TAG,"saveLyric : IOException");
        } finally {
            try{
                if(fos != null) fos.close();
            }catch(IOException nohandle){
                Log.e(TAG,"fos",nohandle);
            }
            try{
                if(osw != null) osw.close();
            }catch(IOException nohandle){
                Log.e(TAG,"osw",nohandle);
            }
            try {
            if(bw != null) bw.close();
            } catch(IOException nohandle) {
                Log.e(TAG,"bw.close",nohandle);
            }
        }
    }

    /**
     * delete HTML tag
     * @param str1
     * @return String
     */
    public static String htmlTrim(String str1) {
        String str = "";
        str = str1;
        str = str.replaceAll("</?[^>]+>", "");
        str = str.replaceAll("\\s", "");
        str = str.replaceAll("&nbsp;", "");
        str = str.replaceAll("&amp;", "&");
        str = str.replace(".", "");
        str = str.replace("\"", "‘");
        str = str.replace("'", "‘");
        return str;
    }

    /**
     * To search the lyrics from Baidu
     * @param info
     * @return
     */
    private static String getLyricInternet(Audio info) {
        try {
            String song = info.getArtist();
            String name = info.getFormattedName();
            String s = getInternet_Lyric(name, song);
            return s;
        } catch (IOException e) {
            Log.e(TAG,"getInternet_Lyric : IOException");
            return null;
        }
    }

    static enum Test {

        Album, TITLE;
    }

}
