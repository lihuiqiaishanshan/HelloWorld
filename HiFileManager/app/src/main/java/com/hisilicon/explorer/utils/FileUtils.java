package com.hisilicon.explorer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.net.Uri;
//import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.hisilicon.explorer.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final int BUFFER = 2048;

    /**
     * Regular expression for safe filenames: no spaces or metacharacters
     */
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("[\\w%+,./=_-]+");
    private static final File[] EMPTY = new File[0];

    /**
     * Test if a file lives under the given directory, either as a direct child
     * or a distant grandchild.
     * <p>
     * Both files <em>must</em> have been resolved using
     * {@link File#getCanonicalFile()} to avoid symlink or path traversal
     * attacks.
     */
    public static boolean contains(File[] dirs, File file) {
        for (File dir : dirs) {
            if (contains(dir, file)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test if a file lives under the given directory, either as a direct child
     * or a distant grandchild.
     * <p>
     * Both files <em>must</em> have been resolved using
     * {@link File#getCanonicalFile()} to avoid symlink or path traversal
     * attacks.
     */
    public static boolean contains(File dir, File file) {
        if (dir == null || file == null) return false;
        String dirPath = dir.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        if (dirPath.equals(filePath)) {
            return true;
        }
        if (!dirPath.endsWith("/")) {
            dirPath += "/";
        }
        return filePath.startsWith(dirPath);
    }

    public static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Log.w(TAG, "Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }

    private static boolean isValidExtFilenameChar(char c) {
        switch (c) {
            case '\0':
            case '/':
                return false;
            default:
                return true;
        }
    }

    /**
     * Check if given filename is valid for an ext4 filesystem.
     */
    public static boolean isValidExtFilename(String name) {
        return (name != null) && name.equals(buildValidExtFilename(name));
    }

    /**
     * Mutate the given filename to make it valid for an ext4 filesystem,
     * replacing any invalid characters with "_".
     */
    public static String buildValidExtFilename(String name) {
        if (TextUtils.isEmpty(name) || ".".equals(name) || "..".equals(name)) {
            return "(invalid)";
        }
        final StringBuilder res = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (isValidExtFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        trimFilename(res, 255);
        return res.toString();
    }

    private static boolean isValidFatFilenameChar(char c) {
        if ((0x00 <= c && c <= 0x1f)) {
            return false;
        }
        switch (c) {
            case '"':
            case '*':
            case '/':
            case ':':
            case '<':
            case '>':
            case '?':
            case '\\':
            case '|':
            case 0x7F:
                return false;
            default:
                return true;
        }
    }

    /**
     * Check if given filename is valid for a FAT filesystem.
     */
    public static boolean isValidFatFilename(String name) {
        return (name != null) && name.equals(buildValidFatFilename(name));
    }

    /**
     * Mutate the given filename to make it valid for a FAT filesystem,
     * replacing any invalid characters with "_".
     */
    public static String buildValidFatFilename(String name) {
        if (TextUtils.isEmpty(name) || ".".equals(name) || "..".equals(name)) {
            return "(invalid)";
        }
        final StringBuilder res = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (isValidFatFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        // Even though vfat allows 255 UCS-2 chars, we might eventually write to
        // ext4 through a FUSE layer, so use that limit.
        trimFilename(res, 255);
        return res.toString();
    }

    public static String trimFilename(String str, int maxBytes) {
        final StringBuilder res = new StringBuilder(str);
        trimFilename(res, maxBytes);
        return res.toString();
    }

    private static void trimFilename(StringBuilder res, int maxBytes) {
        byte[] raw = res.toString().getBytes(StandardCharsets.UTF_8);
        if (raw.length > maxBytes) {
            maxBytes -= 3;
            while (raw.length > maxBytes) {
                res.deleteCharAt(res.length() / 2);
                raw = res.toString().getBytes(StandardCharsets.UTF_8);
            }
            res.insert(res.length() / 2, "...");
        }
    }

    public static boolean renameFile(String fromPath, String name) {
        File file = new File(fromPath);
        String p = file.getParentFile().getPath() + File.separator;
        String newP = p + name;
        boolean b = file.renameTo(new File(newP));
        return b;
    }

    public static String rewriteAfterRename(File beforeDir, File afterDir, String path) {
        if (path == null) return null;
        final File result = rewriteAfterRename(beforeDir, afterDir, new File(path));
        return (result != null) ? result.getAbsolutePath() : null;
    }

    public static String[] rewriteAfterRename(File beforeDir, File afterDir, String[] paths) {
        if (paths == null) return null;
        final String[] result = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            result[i] = rewriteAfterRename(beforeDir, afterDir, paths[i]);
        }
        return result;
    }

    /**
     * Given a path under the "before" directory, rewrite it to live under the
     * "after" directory. For example, {@code /before/foo/bar.txt} would become
     * {@code /after/foo/bar.txt}.
     */
    public static File rewriteAfterRename(File beforeDir, File afterDir, File file) {
        if (file == null || beforeDir == null || afterDir == null) return null;
        if (contains(beforeDir, file)) {
            final String splice = file.getAbsolutePath().substring(
                    beforeDir.getAbsolutePath().length());
            return new File(afterDir, splice);
        }
        return null;
    }

    public static String formatFileCount(int count) {
        String value = NumberFormat.getInstance().format(count);
        return count == 0 ? "empty" : value + " file" + (count == 1 ? "" : "s");
    }

    private static List<File> searchFiles(File dir, FilenameFilter filter) {
        List<File> result = new ArrayList<File>();
        File[] filesFiltered = dir.listFiles(filter), filesAll = dir.listFiles();

        if (filesFiltered != null) {
            result.addAll(Arrays.asList(filesFiltered));
        }

        if (filesAll != null) {
            for (File file : filesAll) {
                if (file.isDirectory()) {
                    List<File> deeperList = searchFiles(file, filter);
                    result.addAll(deeperList);
                }
            }
        }
        return result;
    }

    public static ArrayList<File> searchDirectory(String searchPath, String searchQuery) {
        ArrayList<File> totalList = new ArrayList<File>();
        File searchDirectory = new File(searchPath);
        totalList.addAll(searchFiles(searchDirectory, new SearchFilter(searchQuery)));
        return totalList;
    }

    /**
     * 检测文件名在这个目录路径下是否存在同名文件
     *
     * @param fromName 检测的文件名
     * @param dirPath  目录路径
     * @return true 存在同名文件 else false
     */
    public static boolean checkHasSameNameFileExists(String fromName, String dirPath) {
        File dirFile = new File(dirPath);
        if (dirFile.isDirectory()) {
            File[] files = dirFile.listFiles();
            if (null != files && files.length > 0) {
                for (File file : files) {
                    if (fromName.equals(file.getName())) {
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
        return false;
    }

    public static boolean moveDocument(String fromPath, String toPath, String name) {
        return moveDocument(new File(fromPath), new File(toPath), name);
    }

    public static boolean moveDocument(File fileFrom, File fileTo, String name) {
        if (fileTo.isDirectory() && fileTo.canWrite()) {
            if (fileFrom.isFile()) {
                return copyDocument(fileFrom, fileTo, name);
            } else if (fileFrom.isDirectory()) {
                File[] filesInDir = fileFrom.listFiles();
                File filesToDir = new File(fileTo, fileFrom.getName());
                if (!filesToDir.mkdirs()) {
                    return false;
                }

                for (int i = 0; i < filesInDir.length; i++) {
                    moveDocument(filesInDir[i], filesToDir, null);
                }
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    public static boolean copyDocument(File file, File dest, String name) {
        if (!file.exists() || file.isDirectory()) {
            Log.v(TAG, "copyDocument: file not exist or is directory, " + file);
            return false;
        }
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        byte[] data = new byte[BUFFER];
        int read = 0;
        FileInputStream fi = null;
        FileOutputStream fo = null;
        try {
            fi = new FileInputStream(file);
            if (!dest.exists()) {
                if (!dest.mkdirs())
                    return false;
            }

            File destFile = new File(dest, !TextUtils.isEmpty(name)
                    ? name + "." + getExtFromFilename(file.getName())
                    : file.getName());

            int n = 0;
            while (destFile.exists() && n++ < 32) {
                String destName =
                        (!TextUtils.isEmpty(name)
                                ? name : getNameFromFilename(file.getName())) + " (" + n + ")" + "."
                                + getExtFromFilename(file.getName());
                destFile = new File(dest, destName);
            }

            if (!destFile.createNewFile())
                return false;
            bos = new BufferedOutputStream(new FileOutputStream(destFile));
            bis = new BufferedInputStream(new FileInputStream(file));
            while ((read = bis.read(data, 0, BUFFER)) != -1)
                bos.write(data, 0, read);

            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyDocument: file not found, " + file);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "copyDocument: " + e.toString());
        } finally {
            try {
                //flush and close
                if (null != bos) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "copyDocument: " + e.toString());
            }
            try {
                if (null != bis) {
                    bis.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "copyDocument: " + e.toString());
            }
            try {
                if (fi != null)
                    fi.close();
            } catch (IOException e) {
                Log.e(TAG, "copyDocument: " + e.toString());
            }
            try {
                if (fo != null)
                    fo.close();
            } catch (IOException e) {
                Log.e(TAG, "copyDocument: " + e.toString());
            }
        }
        return false;
    }

    public static boolean deleteFile(String path) {
        return deleteFile(new File(path));
    }

    public static boolean deleteFile(File file) {
        if (file != null && file.exists() && file.isFile() && file.canWrite()) {
            return file.delete();
        } else if (null != file && file.isDirectory()) {
            if (null != file && file.list() != null && file.list().length == 0) {
                return file.delete();
            } else {
                String[] fileList = file.list();
                if (null != fileList) {
                    for (String filePaths : fileList) {
                        File tempFile = new File(file.getAbsolutePath() + File.separator + filePaths);
                        if (tempFile.isFile()) {
                            tempFile.delete();
                        } else {
                            deleteFile(tempFile);
                            tempFile.delete();
                        }
                    }
                }

            }
            if (file.exists()) {
                return file.delete();
            }
        }
        return false;
    }


    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }


    /**
     * Remove file extension from name, but only if exact MIME type mapping
     * exists. This means we can reapply the extension later.
     */
    public static String removeExtension(String mimeType, String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            final String nameMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mimeType.equals(nameMime)) {
                return name.substring(0, lastDot);
            }
        }
        return name;
    }

    /**
     * Add file extension to name, but only if exact MIME type mapping exists.
     */
    public static String addExtension(String mimeType, String name) {
        final String extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType);
        if (extension != null) {
            return name + "." + extension;
        }
        return name;
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf(File.separator);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(index + 1);
        }
    }

    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf('.');
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    public static String getFullNameFromFilepath(String filename) {
        return removeExtension(getName(filename));
    }

    public static String getPathFromFilepath(String filepath) {
        int index = filepath.lastIndexOf(File.separator);
        if (index != -1) {
            int end = index + 1;
            if (end == 0) {
                end++;
            }
            return filepath.substring(0, end);
        }
        return "";
    }

    public static class SearchFilter implements FilenameFilter {
        String searchQuery;
        boolean onlyFolders;

        public SearchFilter(String search) {
            this.searchQuery = search;
        }

        @SuppressWarnings("unused")
        public SearchFilter(String search, boolean onlyFolders) {
            this.onlyFolders = onlyFolders;
        }

        @Override
        public boolean accept(File dir, String filename) {
            if (searchQuery == null) {
                return false;
            }
            if (!onlyFolders && (!filename.startsWith("."))) {
                if (isChineseChar(filename) && !isChineseChar(searchQuery)) {
                    if (fileFilter(searchQuery, filename)) {
                        return true;
                    }
                }
                return filename.toLowerCase(Resources.getSystem().getConfiguration().locale).contains(searchQuery.toLowerCase());
            } else {
                if (!dir.isDirectory() && !filename.startsWith(".")) {
                    if (isChineseChar(filename) && !isChineseChar(searchQuery)) {
                        if (fileFilter(searchQuery, filename)) {
                            return true;
                        }
                    }
                    return filename.toLowerCase(Resources.getSystem().getConfiguration().locale).contains(searchQuery.toLowerCase());
                }
            }
            return false;
        }
    }

    private static final int KILO = 1024;
    private static final int MEGA = KILO * KILO;
    private static final int GIGA = MEGA * KILO;

    /**
     * @return A string suitable for display in bytes, kilobytes or megabytes
     * depending on its size.
     */
    public static String convertToHumanReadableSize(Context context, long size) {
        final String count;
        if (size == 0) {
            return "";
        } else if (size < KILO) {
            count = String.valueOf(size);
            return context.getString(R.string.bytes, count);
        } else if (size < MEGA) {
            count = String.valueOf(size / KILO);
            return context.getString(R.string.kilobytes, count);
        } else if (size < GIGA) {
            count = String.valueOf(size / MEGA);
            return context.getString(R.string.megabytes, count);
        } else {
            DecimalFormat onePlace = new DecimalFormat("0.#");
            count = onePlace.format((float) size / (float) GIGA);
            return context.getString(R.string.gigabytes, count);
        }
    }

    public static String makeFilePath(String parentPath, String name) {
        if (TextUtils.isEmpty(parentPath) || TextUtils.isEmpty(name)) {
            return "";
        }
        return parentPath + File.separator + name;
    }

    /**
     * 进行文件夹创建
     *
     * @param parentPath
     * @param name
     * @return 0 创建成功  1 存在相同文件  2 文件创建失败
     */
    public static int createNewDirState(String parentPath, String name) {
        String absPath = makeFilePath(parentPath, name);
        File newFile = new File(absPath);
        if (newFile.exists()) {
            LogUtils.LOGD(TAG, "createNewDirState file exist : " + parentPath + "/" + name);
            return 1;
        }
        boolean b = newFile.mkdir();
        if (b) {
            LogUtils.LOGD(TAG, "createNewDirState crate file success : " + parentPath + "/" + name);
            return 0;
        } else {
            LogUtils.LOGD(TAG, "createNewDirState crate file error : " + parentPath + "/" + name);
            return 2;
        }
    }

    public static boolean createNewDir(String parentPath, String name) {
        String absPath = makeFilePath(parentPath, name);
        File newFile = new File(absPath);
        if (newFile.exists()) {
            return false;
        }
        boolean b = newFile.mkdir();
        return b;
    }

    public static String makeFilePath(File parentFile, String name) {
        if (null == parentFile || TextUtils.isEmpty(name)) {
            return "";
        }
        return new File(parentFile, name).getPath();
    }

    public static void updateMediaStore(Context context, String... pathsArray) {
        MediaScannerConnection.scanFile(context, pathsArray, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
                //Log.i("Scanner", "Scanned " + s + ":");
                //Log.i("Scanner", "-> uri=" + uri);
            }
        });
    }

    private static File buildFile(File parent, String name, String ext) {
        if (TextUtils.isEmpty(ext)) {
            return new File(parent, name);
        } else {
            return new File(parent, name + "." + ext);
        }
    }

    public static File[] listFilesOrEmpty(File dir) {
        File[] res = dir.listFiles();
        if (res != null) {
            return res;
        } else {
            return EMPTY;
        }
    }

    private static boolean fileFilter(String keyword, String filename) {
        boolean ret = false;
        PinYin4j pinyin = new PinYin4j();
        Set<String> Set = pinyin.getPinyin(filename);
        String prefixString = keyword.toString().toLowerCase();
        LogUtils.LOGD("FileManager", "fileFilter keyword : " + keyword + " File Name : " + filename);
        Iterator iterator = Set.iterator();
        while (iterator.hasNext()) {
            final String pinyin1 = iterator.next().toString().toLowerCase();
            int len = prefixString.length();
            if (len > pinyin1.length())
                break;
            if (pinyin1.contains(prefixString)) {
                return true;
            }
        }
        return ret;
    }

    public static boolean isChineseChar(String str) {
        boolean temp = false;
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            temp = true;
        }
        return temp;
    }
}