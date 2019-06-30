/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hisilicon.android.music.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.lang.NumberFormatException;
import java.lang.UnsupportedOperationException;
import java.lang.ClassCastException;
import java.lang.IllegalArgumentException;
import java.lang.SecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ConcurrentModificationException;
import java.net.MalformedURLException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BoundedInputStream;

import com.hisilicon.android.music.MediaPlaybackActivity;
import com.hisilicon.android.music.Common;

import android.graphics.Color;
import android.util.Log;

import java.io.InputStream;

/**
 * Said the lyrics of a song, it can draw yourself in some way
 * @author
 */
public class Lyric implements Serializable {

    private static final String TAG = "HiMusic_Lyric";
    private static final long serialVersionUID = 20071125L;
    private int width;// The width of the display area for Lyric.
    private int height;// The height of the display area for Lyric
    private long time;// Said that the current time is much. In milliseconds
    private long tempTime;// Said a temporary time, used in driving time, sure
    // where
    public List<Sentence> list = new ArrayList<Sentence>();// There was all the
    // sentences
    private boolean isMoving;// is Moving
    private boolean initDone;// is Init
    private transient Audio info;// info of the Audio
    private transient File file;// Lyric file
    private boolean enabled = true;// Whether to enabled the object, the default
    // is true.
    private long during = Integer.MAX_VALUE;// The song length
    private int offset;// Offset the whole song
    // For the cache of a regular expression object
    private static final Pattern pattern = Pattern
                                           .compile("(?<=\\[).*?(?=\\])");

    /**
     * Use ID3V1 tag name and byte to initialize the lyrics lyrics will
     * automatically in the local or network search related words and establish
     * the related local search will be hard coded into the user.home folder
     * under the Lyrics folder later changed to can be set manually
     * @param info
     */
    public Lyric(final Audio info) {
        this.offset = info.getOffset();
        this.info = info;
        this.file = info.getLyricFile();

        // As long as there is a correlation better, need not search the direct
        // use of it
        if (file != null && file.exists()) {
            init(file);
            initDone = true;
            return;
        } else {
            // Otherwise a thread to find, is the first local search, and then
            // the network to find
            new Thread() {
                public void run() {
                    doInit(info);
                    initDone = true;
                }
            } .start();
        }
    }

    /**
     * Read a lyrics file specified, then the constructor is generally used to
     * drag and drop files to the lyrics lyrics window when you call, drag and
     * drop, two automatic association
     * @param file
     * @param info
     */
    public Lyric(File file, Audio info) {
        this.offset = info.getOffset();
        this.file = file;
        this.info = info;
        init(file);
        initDone = true;
    }

    /**
     * According to the lyrics content and the play item to construct a lyrics
     * object
     * @param lyric
     * @param info
     */
    public Lyric(String lyric, Audio info) {
        this.offset = info.getOffset();
        this.info = info;
        this.init(lyric);
        initDone = true;
    }

    private void doInit(Audio info) {
        init(info);
        Sentence temp = null;

        // This time will go to the network to find out
        if (list.size() == 1) {

            try {
                String lyric = Util.getLyric(info);

                if (lyric != null) {
                    list.remove(0);//remove list only when getLyric(info)!=null
                    init(lyric);
                    info.setLyricFile(file);
                } else {// If the network is not found, will be back.
                    Log.i("MediaPlaybackActivity", "network lyric == null,use filename:");
                }
            } catch (IOException ex) {
                Log.e(TAG,"doInit",ex);
            }
        }
    }

    /**
     * The lyrics is used, otherwise it will not move
     * @param b
     *            is Enabled
     */
    public void setEnabled(boolean b) {
        this.enabled = b;
    }

    /**
     * get Lyric File
     * @return File
     */
    public File getLyricFile() {
        return file;
    }

    /**
     * Adjust the whole time, such as how much faster or lyrics lyrics unified
     * uniform slow many, is that to be fast, is negative to slow
     * @param time
     *            Want to adjust the time, in milliseconds
     */
    public void adjustTime(int time) {
        // If there is only one display, it shows no significance what effect on
        // the direct return,
        if (list.size() == 1) {
            return;
        }

        offset += time;
        info.setOffset(offset);
    }

    /**
     * According to a folder, and a song information from the local search to
     * the best matching lyrics
     * @param dir
     * @param info
     * @return Lyric File
     */
    private File getMathedLyricFile(File dir, Audio info) {
        File matched = null;
        File[] fs = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(".lrc");
            }
        });

        if (null == fs) {
            return null;
        }

        for (File f : fs) {
            // Match all or part of the matching.
            if (matchAll(info, f) || matchSongName(info, f)) {
                matched = f;
                break;
            }
        }

        return matched;
    }

    /**
     * According to the song information to initialize, this time may find
     * lyrics file locally, and perhaps go to the Internet
     * @param info
     */
    public void init(Audio info) {
        File matched = null;
        // After the first song information, search the HOME folder,
        // if it does not exist, then create a directory, and then directly from
        // the matter
        StringBuffer sb = new StringBuffer(info.getPath());
        String curr_ = sb.substring(0, info.getPath().lastIndexOf("/"));
        File curr_dir = new File(curr_);

        if (curr_dir.exists()) {
            matched = getMathedLyricFile(curr_dir, info);
        }

        if (matched != null && matched.exists()) {
            info.setLyricFile(matched);
            file = matched;
            init(matched);
            return;
        }

        File dir = new File(MediaPlaybackActivity.getHOME(), "Lyrics"
                            + File.separator);
        if (!dir.exists()) {
            if(!dir.mkdirs())
                return;
        }

        matched = getMathedLyricFile(dir, info);

        if (matched != null && matched.exists()) {
            info.setLyricFile(matched);
            file = matched;
            init(matched);
        } else {
            init("");
        }
    }

    public static boolean isUTF8(File file){/*judge the content of file*/
        if("UTF-8".equals(getCharset(file)))
            return true;
        return false;
        /*don't use, because of fail in some lrc file
        try {
            String filecCntent = FileUtils.readFileToString(file,"UTF-8");
            if(!isGarbledCode(filecCntent)){
               return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
        */
    }

    public static String getCharset(File file) {
        String charset = "GBK";
        BufferedInputStream bis = null;
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1]
                    == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1]
                    == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)// 双字节 (0xC0 - 0xDF)
                            // (0x80 -
                            // 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        } finally {
            try {
                if (null != bis) {
                    bis.close();
                }
            } catch (Exception e){
                Log.e(TAG,e.toString());
            }
        }
        return charset;
    }

    public static boolean isGarbledCode(String strName) {/*judge the content of file*/
        if (null == strName || 0 == strName.trim().length()) {
            return true;
        }

        Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = ch.length;
        float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {

                if (!isChinese(c)) {
                    count = count + 1;
                }
            }
        }

        float result = count / chLength;
        if (result > 0.4) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isChinese(char c) {/*judge the content of file*/
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * According to the initialization file
     * @param file
     */
    @SuppressWarnings("null")
    private void init(File file) {
        BufferedReader br = null;
        WeakReference<StringBuilder> weakSb;
        WeakReference<BufferedReader> weakBr = null;
        StringBuilder sb;
        FileInputStream fis = null;
        BoundedInputStream bis = null;
        InputStreamReader isr = null;

        try {
            if (null == file)
            { return; }

            String charcode = "GBK";
            String strCode = getCharset(file);
            if (null != strCode) {
              strCode = strCode.toUpperCase();
              if (strCode.equals("UTF-8")) {
                   charcode = strCode;
              }
            }

            fis = new FileInputStream(file);
            bis = new BoundedInputStream(fis, 8096);
            isr = new InputStreamReader(fis, charcode);
            br = new BufferedReader(isr);
            weakBr = new WeakReference<BufferedReader>(br);
            sb = new StringBuilder();
            weakSb = new WeakReference<StringBuilder>(sb);
            String temp = null;
            WeakReference<String> weakTemp = new WeakReference<String>(temp);

            while ((temp = Common.readLine(weakBr.get())) != null) {
                weakSb.get().append(temp).append("\n");
            }

            init(weakSb.get().toString());
            sb = null;
            weakSb = null;
            System.gc();
        } catch (FileNotFoundException e) {
            Log.e(TAG,"FileInputStream : FileNotFoundException");
        } catch (SecurityException e) {
            Log.e(TAG,"FileInputStream : SecurityException");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG,"InputStreamReader : UnsupportedEncodingException");
        } finally {
            try{
                if(fis != null) fis.close();
            }catch(IOException nohandle){
                Log.e(TAG,"fis",nohandle);
            }
            try{
                if(isr!= null) isr.close();
            }catch(IOException nohandle){
                Log.e(TAG,"isr",nohandle);
            }
            try{
                if(bis!= null) bis.close();
            }catch(IOException nohandle){
                Log.e(TAG,"init bis",nohandle);
            }
            try {
                if (null != br)  br.close();
                weakSb = null;
                if (null != weakBr) weakBr.get().close();
                System.gc();
            }
              catch (IOException ex) {
                Log.e(TAG,"weakBr",ex);
            }
        }
    }

    /**
     * Match exactly, exactly matching refers directly correspond to the ID3V1
     * label, if the same, are perfect match.
     * @param info
     * @param file
     * @return is matched
     */
    private boolean matchAll(Audio info, File file) {
        String name = info.getFormattedName();
        String fn = file.getName()
                    .substring(0, file.getName().lastIndexOf("."));

        if (name.equals(fn)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Whether to match the song
     * @param info
     * @param file
     * @return is matched
     */
    private boolean matchSongName(Audio info, File file) {
        int startIndex = 0;
        String name = info.getFormattedName();
        String rn = file.getName()
                    .substring(0, file.getName().lastIndexOf("."));

        if (file.getName().lastIndexOf("-") < 0) {
            startIndex = 0;
        } else {
            startIndex = file.getName().lastIndexOf("-");
        }

        String tn = file.getName().substring(startIndex + 1,
                                             file.getName().lastIndexOf("."));

        if (name.equalsIgnoreCase(rn) || info.getTitle().equalsIgnoreCase(rn)
            || name.equalsIgnoreCase(tn)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * One of the most important ways, according to read the lyrics to
     * initialize, such as the words of a sentence and calculate good time
     * @param content
     */
    private void init(String content) {
        // If the content of the lyrics is empty,
        // then followed without executing the direct display song name on it
        if (content == null || content.trim().equals("")) {
            list.add(new Sentence(info.getFormattedName(), Integer.MIN_VALUE,
                                  Integer.MAX_VALUE));
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new StringReader(content));
            String temp = null;

            while ((temp = Common.readLine(br)) != null) {
                parseLine(temp.trim());
            }

            br.close();
            // Reading came in and sort.
            Collections.sort(list, new Comparator<Sentence>() {
                public int compare(Sentence o1, Sentence o2) {
                    return (int)(o1.getFromTime() - o2.getFromTime());
                }
            });

            // The first lyrics initial condition, no matter what,
            // plus the song name as the first sentence of the lyrics,
            // and put it at the end of the first sentence of the lyrics for the
            // real start
            if (list.size() == 0) {
                list.add(new Sentence(info.getFormattedName(), 0,
                                      Integer.MAX_VALUE));
                return;
            } else {
                Sentence first = list.get(0);
                list.add(
                    0,
                    new Sentence(info.getFormattedName(), 0, first
                                 .getFromTime()));
            }

            int size = list.size();

            for (int i = 0; i < size; i++) {
                Sentence next = null;

                if (i + 1 < size) {
                    next = list.get(i + 1);
                }

                Sentence now = list.get(i);

                if (next != null) {
                    now.setToTime(next.getFromTime() - 1);
                }
            }

            // If that is not how to do, it will show only one song.
            if (list.size() == 1) {
                list.get(0).setToTime(Integer.MAX_VALUE);
            } else {
                Sentence last = list.get(list.size() - 1);
                last.setToTime(info.getLength() == 0
                                    ? Integer.MAX_VALUE
                                    : info.getLength() * 1000 + 1000);
            }
        } catch (IOException e) {
            Log.e(TAG,"br.close error");
        } catch (ClassCastException e) {
            Log.e(TAG,"Collections.sort or list.add error");
        } catch (UnsupportedOperationException e) {
            Log.e(TAG,"Collections.sort or list.add error");
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"Collections.sort or list.add error");
        }
    }

    /**
     * Analysis of the offset of the whole
     * @param str
     * @return Offset, when the analysis does not come out, the largest positive
     *         returns
     */
    private int parseOffset(String str) throws NumberFormatException {
        String[] ss = str.split("\\:");

        if (ss.length == 2) {
            if (ss[0].equalsIgnoreCase("offset")) {
                int os = Integer.parseInt(ss[1]);
                return os;
            } else {
                return Integer.MAX_VALUE;
            }
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Analysis of this line of content, according to the number of the content
     * and label spanning several Sentence objects when the time label
     * distribution was not together, also want to be able to analyze and change
     * some of the implementation
     * @param line
     */
    private void parseLine(String line) {
        if (line.equals("")) {
            return;
        }

        Matcher matcher = pattern.matcher(line);
        List<String> temp = new ArrayList<String>();
        int lastIndex = -1;// The last time the index.
        int lastLength = -1;// The last time the Length.

        try {
            while (matcher.find()) {
                String s = matcher.group();
                int index = line.indexOf("[" + s + "]");

                if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
                    // If greater than the last, is sandwiched between the other
                    // content at this time there will be segmented.
                    String content = line.substring(lastIndex + lastLength + 2,
                                                index);

                    for (String str : temp) {
                        long t = parseTime(str);

                        if (t != -1) {
                            list.add(new Sentence(content, t));
                        }
                    }

                    temp.clear();
                }

                temp.add(s);
                lastIndex = index;
                lastLength = s.length();
            }

            // If the list is empty, said the bank did not analyze any label
            if (temp.isEmpty()) {
                return;
            }

            int length = lastLength + 2 + lastIndex;
            String content = line.substring(length > line.length() ? line
                                            .length() : length);

            // When there is time offset, will no longer analysis
            if (content.equals("") && offset == 0) {
                for (String s : temp) {
                    int of = parseOffset(s);

                    if (of != Integer.MAX_VALUE) {
                        offset = of;
                        info.setOffset(offset);
                        break;
                    }
                }

                return;
            }

            for (String s : temp) {
                long t = parseTime(s);

                if (t != -1) {
                    list.add(new Sentence(content, t));
                }
            }
        } catch (IllegalStateException e) {
            Log.e(TAG,"matcher.group : IllegalStateException");
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG,"line.substring",e);
        } catch (NumberFormatException e) {
            Log.e(TAG,"parseOffset",e);
        } catch (UnsupportedOperationException e) {
            Log.e(TAG,"list.add",e);
        } catch (ClassCastException e) {
            Log.e(TAG,"list.add",e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"list.add",e);
        }
    }

    /**
     * Such a string like 00:00.00 into a number of milliseconds of time, such
     * as 01:10.34 is one minute and 10 seconds and 340 milliseconds is returned
     * 70340 milliseconds
     * @param time
     * @return This time represents a millisecond
     */
    private long parseTime(String time) throws NumberFormatException {
        String[] ss = time.split("\\:|\\.");

        // If it is two bits, is illegal.
        if (ss.length < 2) {
            return -1;
        } else if (ss.length == 2) {// If just two seconds, even if
            try {
                // First to see if there is no one was recorded by whole offset
                if (offset == 0 && ss[0].equalsIgnoreCase("offset")) {
                    offset = Integer.parseInt(ss[1]);
                    info.setOffset(offset);
                    return -1;
                }

                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);

                if (min < 0 || sec < 0 || sec >= 60) {
                    Log.e(TAG,"Figures are not legitimate!");
                    return -1;
                }

                return (min * 60 + sec) * 1000L;
            } catch (NumberFormatException e) {
                throw e;
            }
        } else if (ss.length == 3) {// If just three, even every minute, ten
                                    // milliseconds
            try {
                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);
                int mm = Integer.parseInt(ss[2]);

                if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
                    Log.e(TAG,"Figures are not legitimate!");
                    return -1;
                }

                return (min * 60 + sec) * 1000L + mm * 10;
            } catch (NumberFormatException e) {
                throw e;
            }
        } else {
            return -1;
        }
    }

    /**
     * Set the height of the display area.
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Set the width of the display area.
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Set the time, set time, to offset the overall count time
     * @param time
     */
    public void setTime(long time) {
        if (!isMoving) {
            tempTime = this.time = time + offset;
        }
    }

    /**
     * Get is initialized.
     * @return boolean
     */
    public boolean isInitDone() {
        return initDone;
    }

    /**
     * Is currently playing the sentence index could not be found, because the
     * beginning to add your own sentences, so later on could not be found.
     * @return index
     */
    int getNowSentenceIndex(long t) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isInTime(t)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * If you can drag, only the lyrics can be dragged, or no meaning
     * @return boolean
     */
    public boolean canMove() {
        return list.size() > 1 && enabled;
    }

    /**
     * Get the current time, usually by the display panel call
     */
    public long getTime() {
        return tempTime;
    }

//for fority    /**
//     * After making changes to tempTime, check its value, can is it right? In
//     * effective range
//     */
//    private void checkTempTime() {
//        if (tempTime < 0) {
//            tempTime = 0;
//        } else if (tempTime > during) {
//            tempTime = during;
//        }
//    }

    /**
     * Tell the lyrics, began to move, in the meantime, direct time set all of
     * the lyrics are ignored
     */
    public void startMove() {
        isMoving = true;
    }

    /**
     * Tell the lyrics drag out, this time to change to ignore, and change
     */
    public void stopMove() {
        isMoving = false;
    }

}
