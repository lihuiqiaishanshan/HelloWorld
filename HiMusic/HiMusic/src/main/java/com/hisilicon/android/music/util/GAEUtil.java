package com.hisilicon.android.music.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.StreamCorruptedException;
import java.io.EOFException;
import java.io.UTFDataFormatException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.net.UnknownServiceException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.IllegalArgumentException;
import java.lang.SecurityException;

import android.util.Log;

import com.hisilicon.android.music.util.SearchResult.Task;

/**
 * @author
 */
public final class GAEUtil {

    private static final String getSingleResultURL = "";
    private static final String getLyricContentURL = "";
    private static final String getResultListURL = "";
    private static final String voteURL = "";
    private static final String versionURL = "";
    private static final Logger log = Logger.getLogger(GAEUtil.class.getName());
    private static final String TAG = "HiMusic_GAEUtil";

    public static List<SearchResult> getSearchResult(String artistParam, String titleParam)
    throws IllegalArgumentException, IOException, ClassCastException, UnsupportedOperationException {
        ObjectInputStream ois = null;
        List<SearchResult> list = null;
        try{
            String urlContent = MessageFormat.format(getResultListURL,
                                                     urlEncode(artistParam), urlEncode(titleParam));
            ois = getObjectInputStream(urlContent);
            int back = ois.readInt();
            list = new ArrayList<SearchResult>();

            if (back == 1) {
                int size = ois.readInt();

                for (int i = 0; i < size; i++) {
                    final String artist = ois.readUTF();
                    final String lrcCode = ois.readUTF();
                    final String lrcId = ois.readUTF();
                    final String title = ois.readUTF();
                    final String id = ois.readUTF();
                    final Task task = new Task() {
                        public String getLyricContent() {
                            return getLyricContent_S(id, lrcId, lrcCode, artist,
                                                     title);
                        }
                    };
                    list.add(new SearchResult(id, lrcId, lrcCode, artist, title,
                                              task));
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (ClassCastException e) {
            throw e;
        } finally{
            try{
                if(ois!= null) ois.close();
            }catch(IOException nohandle){
                Log.e(TAG,"getSearchResult",nohandle);
            }
        }
        return list;
    }

/*for fority
    private static String getSingleResult(String artistParam, String titleParam)
    throws Exception {
        String urlContent = MessageFormat.format(getSingleResultURL,
                                                 $(artistParam), $(titleParam));
        ObjectInputStream ois = getObjectInputStream(urlContent);
        int back = ois.readInt();
        String retStr = null;
        if (back == 1) {
            retStr = ois.readUTF();
        }

        ois.close();
        return retStr;
    }
*/

    private static String getLyricContent_S(String id, String lrcId,
                                            String lrcCode, String artist, String title) {
        ObjectInputStream ois = null;

        try {
            String urlContent = MessageFormat.format(getLyricContentURL, urlEncode(id),
                                                     urlEncode(lrcId), urlEncode(lrcCode), urlEncode(artist), urlEncode(title));
            ois = getObjectInputStream(urlContent);
            int back = ois.readInt();

            if (back == 1) {
                return ois.readUTF();
            } else {
                return "";
            }
        } catch (IllegalArgumentException e) {
            Log.e( TAG, "MessageFormat.format", e);
            return "";
        } catch (MalformedURLException e) {
            Log.e( TAG, "getObjectInputStream", e);
            return "";
        } catch (SecurityException e) {
            Log.e( TAG, "getObjectInputStream", e);
            return "";
        } catch (UnknownServiceException e) {
            Log.e( TAG, "getObjectInputStream", e);
            return "";
        } catch (StreamCorruptedException e) {
            Log.e( TAG, "getObjectInputStream", e);
            return "";
        } catch (EOFException e) {
            Log.e( TAG, "ois.readInt", e);
            return "";
        } catch (UTFDataFormatException e) {
            Log.e( TAG, "ois.readUTF", e);
            return "";
        } catch (IOException e) {
            Log.e( TAG, "getObjectInputStream, readUTF or readInt", e);
            return "";
        } finally{
            try{
                if(ois!= null) ois.close();
            }catch(IOException nohandle){
                Log.e( TAG, "ois.close", nohandle);
            }
        }
    }

    private static ObjectInputStream getObjectInputStream(String urlContent)
    throws MalformedURLException, IOException, SecurityException, UnknownServiceException, StreamCorruptedException {
        URL url = new URL(urlContent);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream inputstream = conn.getInputStream();
        if (null == inputstream) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(inputstream);
        } catch (IOException e) {
            inputstream.close();
            return null;
        }
        return ois;
    }

    private static String urlEncode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "GBK");
    }
}
