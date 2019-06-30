package com.hisilicon.android.music.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.util.Log;

import com.hisilicon.android.music.MediaPlaybackActivity;

/**
 * To search the results of object representation
 * @author
 */
public class SearchResult {
    private String id;
    private String lrcId;
    private String lrcCode;
    private String artist;
    private String title;
    private Task task;
    private String content;
    private static final String TAG = "HiMusic_SearchResult";

    public static interface Task {

        public String getLyricContent();
    }

    public SearchResult(String id, String lrcId, String lrcCode, String artist,
                        String title, Task task) {
        this.id = id;
        this.lrcId = lrcId;
        this.lrcCode = lrcCode;
        this.artist = artist;
        this.title = title;
        this.task = task;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getLrcCode() {
        return lrcCode;
    }

    public String getLrcId() {
        return lrcId;
    }

    public String getContent() {
        if (content == null) {
            content = task.getLyricContent();
        }

        return content;
    }

    public void save(String name, Context context) throws IOException {
        BufferedWriter bw = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            fos = context.openFileOutput(MediaPlaybackActivity.getHOME()+"Lyrics/" + name,Context.MODE_PRIVATE);
            osw = new OutputStreamWriter(fos, "GBK");
            bw = new BufferedWriter(osw);
            bw.write(String.valueOf(getContent()));
        } catch(IOException throwthis) {
            throw throwthis;
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
            try{
                if(bw != null) bw.close();
            }catch(IOException nohandle){
                Log.e(TAG,"bw",nohandle);
            }
        }
    }

    public String toString() {
        return artist + ":" + title;
    }
}
