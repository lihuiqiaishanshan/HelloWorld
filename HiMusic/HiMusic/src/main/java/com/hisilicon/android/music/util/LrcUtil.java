package com.hisilicon.android.music.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class LrcUtil {
    private static final String TAG = "HiMusic_LrcUtil";
    /**
     * According to the introduction of the track name and artist name, get a
     * list of search attention, Lynnfield cannot be null, otherwise there will
     * be unexpected abnormal this method calls another method, and the
     * combination of search, until the results or combination has finished
     * @param item
     *            The item you want to search
     * @return A list of search
     */
    public static List<SearchResult> search(Audio item) {
        List<SearchResult> list = new ArrayList<SearchResult>();

        try {
            List<SearchResult> temp = search(item.getArtist(), item.getTitle());

            if (temp.isEmpty()) {
                temp = search("", item.getTitle());

                if (temp.isEmpty()) {
                    temp = search("", item.getName());
                }
            }

            list.addAll(temp);
        }
           catch (IllegalArgumentException ex) {
            Log.e(TAG,"search",ex);
        }
         catch (IOException ex) {
            Log.e(TAG,"search",ex);
        }
         catch (ClassCastException ex) {
            Log.e(TAG,"search",ex);
        }
        catch (UnsupportedOperationException ex) {
            Log.e(TAG,"search",ex);
        }

        return list;
    }

    /*
     * Actually search method
     * @param singer
     * @param title
     * @return
     */
    public static List<SearchResult> search(String singer, String title)
    throws IllegalArgumentException, IOException, ClassCastException, UnsupportedOperationException  {
        if (singer == null) {
            singer = "";
        }

        if (title == null) {
            title = "";
        }

        return GAEUtil.getSearchResult(singer, title);
    }
/*for fority
    private static String readURL(String url) {
        BufferedReader br = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url)
                                     .openConnection();
            br = new BufferedReader(new InputStreamReader(
                                                       conn.getInputStream(), "GBK"));
            String temp = null;
            StringBuilder sb = new StringBuilder();

            while ((temp = br.readLine()) != null) {
                sb.append(temp).append("\n");
            }

            return sb.toString();
        } catch (Exception exe) {
            exe.printStackTrace();
            return null;
        } finally {
            try{
                if(br != null) br.close();
            }catch(IOException nohandle){
                nohandle.printStackTrace();
            }
        }
    }
*/
}
