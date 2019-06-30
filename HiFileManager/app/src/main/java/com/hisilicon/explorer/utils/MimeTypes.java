package com.hisilicon.explorer.utils;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.hisilicon.explorer.jni.bd.BDInfo;
import com.hisilicon.explorer.jni.bd.DVDInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

public final class MimeTypes {

    public static final String ALL_MIME_TYPES = "*/*";
    private static final HashMap<String, String> MIME_TYPES = new HashMap<String, String>();
    public static final String BASIC_MIME_TYPE = "application/octet-stream";
    private static BDInfo bd;
    private static DVDInfo mDVDInfo;

    private final static String DVD_MIMETYPE = "video/dvd";
    private final static String BD_MIMETYPE = "video/bd";
    private final static String DEFAULT_MIMETYPE = "*/*";
    private final static String DIERCTORY_MIMETYP = "directory/*";

    private MimeTypes() {
    }

    static {
        MIME_TYPES.put("asm", "text/x-asm");
        MIME_TYPES.put("def", "text/plain");
        MIME_TYPES.put("in", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        MIME_TYPES.put("list", "text/plain");
        MIME_TYPES.put("log", "text/plain");
        MIME_TYPES.put("pl", "text/plain");
        MIME_TYPES.put("prop", "text/plain");
        MIME_TYPES.put("properties", "text/plain");
        MIME_TYPES.put("rc", "text/plain");

        MIME_TYPES.put("epub", "application/epub+zip");
        MIME_TYPES.put("ibooks", "application/x-ibooks+zip");

        MIME_TYPES.put("ifb", "text/calendar");
        MIME_TYPES.put("eml", "message/rfc822");
        MIME_TYPES.put("msg", "application/vnd.ms-outlook");

        MIME_TYPES.put("ace", "application/x-ace-compressed");
        MIME_TYPES.put("bz", "application/x-bzip");
        MIME_TYPES.put("bz2", "application/x-bzip2");
        MIME_TYPES.put("cab", "application/vnd.ms-cab-compressed");
        MIME_TYPES.put("gz", "application/x-gzip");
        MIME_TYPES.put("lrf", BASIC_MIME_TYPE);
        MIME_TYPES.put("jar", "application/java-archive");
        MIME_TYPES.put("xz", "application/x-xz");
        MIME_TYPES.put("Z", "application/x-compress");

        MIME_TYPES.put("bat", "application/x-msdownload");
        MIME_TYPES.put("ksh", "text/plain");
        MIME_TYPES.put("sh", "application/x-sh");

        MIME_TYPES.put("db", BASIC_MIME_TYPE);
        MIME_TYPES.put("db3", BASIC_MIME_TYPE);

        MIME_TYPES.put("otf", "application/x-font-otf");
        MIME_TYPES.put("ttf", "application/x-font-ttf");
        MIME_TYPES.put("psf", "application/x-font-linux-psf");

        MIME_TYPES.put("wbmp", "image/wbmp");
        MIME_TYPES.put("jfif", "image/jfif");
        MIME_TYPES.put("jpe", "image/jpe");
        MIME_TYPES.put("tif", "image/tif");
        MIME_TYPES.put("tiff", "image/tiff");
        MIME_TYPES.put("rng", "image/rng");
        MIME_TYPES.put("dng", "image/dng");
        MIME_TYPES.put("jpg", "image/jpg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("bmp", "image/bmp");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("cgm", "image/cgm");
        MIME_TYPES.put("btif", "image/prs.btif");
        MIME_TYPES.put("dwg", "image/vnd.dwg");
        MIME_TYPES.put("dxf", "image/vnd.dxf");
        MIME_TYPES.put("fbs", "image/vnd.fastbidsheet");
        MIME_TYPES.put("fpx", "image/vnd.fpx");
        MIME_TYPES.put("fst", "image/vnd.fst");
        MIME_TYPES.put("mdi", "image/vnd.ms-mdi");
        MIME_TYPES.put("npx", "image/vnd.net-fpx");
        MIME_TYPES.put("xif", "image/vnd.xiff");
        MIME_TYPES.put("pct", "image/x-pict");
        MIME_TYPES.put("pic", "image/x-pict");
        MIME_TYPES.put("heic", "image/heic");//Add New Pic Specification for AndroidP
        MIME_TYPES.put("heif", "image/heif");//Add New Pic Specification for AndroidP

        MIME_TYPES.put("adp", "audio/adpcm");
        MIME_TYPES.put("au", "audio/basic");
        MIME_TYPES.put("snd", "audio/basic");
        MIME_TYPES.put("m2a", "audio/mpeg");
        MIME_TYPES.put("m3a", "audio/mpeg");
        MIME_TYPES.put("oga", "audio/ogg");
        MIME_TYPES.put("spx", "audio/ogg");
        MIME_TYPES.put("aac", "audio/x-aac");
        MIME_TYPES.put("mka", "audio/x-matroska");
        MIME_TYPES.put("ogg", "audio/ogg");
        MIME_TYPES.put("ape", "audio/ape");
        MIME_TYPES.put("wav", "audio/wav");
        MIME_TYPES.put("wma", "audio/wma");
        MIME_TYPES.put("m3u","audio/m3u");
        MIME_TYPES.put("cue","audio/cue");
        MIME_TYPES.put("pls","audio/pls");
        MIME_TYPES.put("mp2","audio/mp2");
        MIME_TYPES.put("mp3","audio/mp3");
        MIME_TYPES.put("cdda","audio/cdda");
        MIME_TYPES.put("m4a","audio/m4a");
        MIME_TYPES.put("aiff","audio/aiff");
        MIME_TYPES.put("flac","audio/flac");
        MIME_TYPES.put("ra","audio/ra");
        MIME_TYPES.put("ec3","audio/ec3");
        MIME_TYPES.put("ac3","audio/ac3");
        MIME_TYPES.put("dts","audio/dts");
        MIME_TYPES.put("mlp","audio/mlp");
        MIME_TYPES.put("mid","audio/mid");
        MIME_TYPES.put("midi","audio/midi");
        MIME_TYPES.put("xmf","audio/xmf");
        MIME_TYPES.put("rtttl","audio/rtttl");
        MIME_TYPES.put("smf","audio/smf");
        MIME_TYPES.put("imy","audio/imy");
        MIME_TYPES.put("rtx","audio/rtx");
        MIME_TYPES.put("ota","audio/ota");
        MIME_TYPES.put("awr","audio/awr");
        MIME_TYPES.put("awb","audio/awb");
        MIME_TYPES.put("aea","audio/aea");
        MIME_TYPES.put("apc","audio/apc");
        MIME_TYPES.put("daud","audio/daud");
        MIME_TYPES.put("oma","audio/oma");
        MIME_TYPES.put("eac3","audio/eac3");
        MIME_TYPES.put("gsm","audio/gsm");
        MIME_TYPES.put("truehd","audio/truehd");
        MIME_TYPES.put("tta","audio/tta");
        MIME_TYPES.put("mpc","audio/mpc");
        MIME_TYPES.put("mpc8","audio/mpc8");


        MIME_TYPES.put("jpgv", "video/jpev");
        MIME_TYPES.put("jpgm", "video/jpm");
        MIME_TYPES.put("jpm", "video/jpm");
        MIME_TYPES.put("mj2", "video/mj2");
        MIME_TYPES.put("mjp2", "video/mj2");
        MIME_TYPES.put("mpa", "video/mpeg");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mkv", "video/x-matroska");
        MIME_TYPES.put("iso", "video/iso");
        MIME_TYPES.put("rm", "video/rm");
        MIME_TYPES.put("avsts", "video/avsts");
        MIME_TYPES.put("mts", "video/mts");
        MIME_TYPES.put("m2t", "video/m2t");
        MIME_TYPES.put("m2ts", "video/m2ts");
        MIME_TYPES.put("trp", "video/trp");
        MIME_TYPES.put("tp", "video/tp");
        MIME_TYPES.put("ps", "video/ps");
        MIME_TYPES.put("avs", "video/avs");
        MIME_TYPES.put("swf", "video/swf");
        MIME_TYPES.put("ogm", "video/ogm");
        MIME_TYPES.put("flv", "video/flv");
        MIME_TYPES.put("rmvb", "video/rmvb");
        MIME_TYPES.put("3gp", "video/3gp");
        MIME_TYPES.put("m4v", "video/m4v");
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("mov", "video/mov");
        MIME_TYPES.put("mkv", "video/mkv");
        MIME_TYPES.put("ifo", "video/ifo");
        MIME_TYPES.put("divx", "video/divx");
        MIME_TYPES.put("ts", "video/ts");
        MIME_TYPES.put("avi", "video/avi");
        MIME_TYPES.put("dat", "video/dat");
        MIME_TYPES.put("vob", "video/vob");
        MIME_TYPES.put("mpg", "video/mpg");
        MIME_TYPES.put("mpeg", "video/mpeg");
        MIME_TYPES.put("asf", "video/asf");
        MIME_TYPES.put("wmv", "video/wmv");
        MIME_TYPES.put("3gpp", "video/3gpp");
        MIME_TYPES.put("3g2", "video/3g2");
        MIME_TYPES.put("3gpp2", "video/3gpp2");
        MIME_TYPES.put("f4v", "video/f4v");
        MIME_TYPES.put("m1v", "video/m1v");
        MIME_TYPES.put("m2v", "video/m2v");
        MIME_TYPES.put("m2p", "video/m2p");
        MIME_TYPES.put("dv", "video/dv");
        MIME_TYPES.put("iff", "video/iff");
        MIME_TYPES.put("mj2", "video/mj2");
        MIME_TYPES.put("anm", "video/anm");
        MIME_TYPES.put("h261", "video/h261");
        MIME_TYPES.put("h263", "video/h263");
        MIME_TYPES.put("h264", "video/h264");
        MIME_TYPES.put("yuv", "video/yuv");
        MIME_TYPES.put("cif", "video/cif");
        MIME_TYPES.put("qcif", "video/qcif");
        MIME_TYPES.put("rgb", "video/rgb");
        MIME_TYPES.put("vc1", "video/vc1");
        MIME_TYPES.put("y4m", "video/y4m");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("wvm", "video/wvm");
        MIME_TYPES.put("ssif", "video/ssif");
        MIME_TYPES.put("m3u8", "video/m3u8");
        MIME_TYPES.put("m3u9", "video/m3u9");
        init();
    }

    private static void init() {
        final File system_libbdinfo = new File("/system/lib/libbdinfo_jni.so");
        final File system64_libbdinfo = new File("/system/lib64/libbdinfo_jni.so");
        final File vendor_libbdinfo = new File("/vendor/lib/libbdinfo_jni.so");
        final File vendor64_libbdinfo = new File("/vendor/lib64/libbdinfo_jni.so");
        if (system_libbdinfo.exists() || vendor_libbdinfo.exists()
                || system64_libbdinfo.exists() || vendor64_libbdinfo.exists())
            bd = new BDInfo();
        else
            bd = null;
        final File system_libdvdinfo = new File("/system/lib/libdvdinfo_jni.so");
        final File system64_libdvdinfo = new File("/system/lib64/libdvdinfo_jni.so");
        final File vendor_libdvdinfo = new File("/vendor/lib/libdvdinfo_jni.so");
        final File vendor64_libdvdinfo = new File("/vendor/lib64/libdvdinfo_jni.so");
        if (system_libdvdinfo.exists() || vendor_libdvdinfo.exists()
                || system64_libdvdinfo.exists() || vendor64_libdvdinfo.exists())
            mDVDInfo = new DVDInfo();
        else
            mDVDInfo = null;
    }

    /**
     * 先从系统找，找不到再从map中找
     *
     * @param extension
     * @return
     */
    public static String getMimeTypeFromExtension(String extension) {
        String type = null;

        if (!TextUtils.isEmpty(extension)) {
            final String extensionLowerCase = extension.toLowerCase(Locale.getDefault());
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extensionLowerCase);
            if (type == null) {
                //extra mime types map
                type = MIME_TYPES.get(extensionLowerCase);
            }
        }
        return type;
    }

    /**
     * 通过系统方法获取文件后缀然后再获取其扩展的mimetype ps：getFileExtensionFromUrl这个方法获取到的扩展不全
     *
     * @param filePath
     * @return
     */
    public static String getMimeTypeFromPath(String filePath) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
        return getMimeTypeFromExtension(ext);
    }

    /**
     * 由于调用getFileExtensionFromUrl会让一些文件中有特殊字符的不能够获取后缀因此添加此方法
     *
     * @return mimetype
     */
    public static String getMimeTypeFromPath2(String filePath) {
        String ext = FileUtils.getExtFromFilename(filePath);
        return getMimeTypeFromExtension2(ext);
    }

    /**
     * 先从map取然后再到系统mimetype中取
     *
     * @param extension 文件后缀扩展
     * @return mimetype
     */
    public static String getMimeTypeFromExtension2(String extension) {
        String type = null;

        if (!TextUtils.isEmpty(extension)) {
            final String extensionLowerCase = extension.toLowerCase(Locale.getDefault());
            type = MIME_TYPES.get(extensionLowerCase);
            if (type == null) {
                //extra mime types map
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extensionLowerCase);
            }
        }
        return type;
    }

    /**
     * 添加几个特殊的mimetype
     *
     * @param f 需要判断的文件，可以是文件夹
     * @return
     */
    public static String getMimeTypesFile(File f) {
        if (bd != null && bd.isBDFile(f.getPath())) {
            return BD_MIMETYPE;
        } else if (mDVDInfo != null && mDVDInfo.isDVDFile(f.getPath()) && !f.getPath().toUpperCase().endsWith(".ISO")) {
            return DVD_MIMETYPE;
        } else if (f.isDirectory()) {
            return DIERCTORY_MIMETYP;
        } else {
            String mimeTypeFromPath = getMimeTypeFromPath2(f.getPath());
            if (TextUtils.isEmpty(mimeTypeFromPath)) {
                return DEFAULT_MIMETYPE;
            } else {
                return mimeTypeFromPath;
            }
        }
    }
    /**
     * 根据文件内容判断是否为视频或图片
     *
     * @param filePath
     * @return
     */
    public static String getMimeTypeByStream(String filePath) {
        String type = "";
        File f = new File(filePath);
        if (f != null && f.isFile()) {
            return getMimeTypesFile(f);
        }
        return type;
    }
}