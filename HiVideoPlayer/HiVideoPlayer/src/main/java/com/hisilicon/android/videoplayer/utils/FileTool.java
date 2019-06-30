package com.hisilicon.android.videoplayer.utils;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class FileTool {

    public static void cp(InputStream in, String to, Context context) throws IOException {
        cp(in, context.openFileOutput(to, Context.MODE_PRIVATE));
    }

    private static void cp(InputStream in, OutputStream out) throws IOException {
        // 1K byte buffer
        byte[] buf = new byte[1024];
        int count;
        try {
            while ((count = in.read(buf)) != -1) {
                out.write(buf, 0, count);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException nohandle) {
            }
            try {
                if (out != null) out.close();
            } catch (IOException nohandle) {
            }
        }
    }

    public static String getExtension(File pFile) {
        String _FileName = pFile.getName();
        String _Extension = " ";
        if (_FileName != null && _FileName.length() > 0) {
            int _DotIndex = _FileName.lastIndexOf('.');
            if (_DotIndex > -1 && (_DotIndex < _FileName.length() - 1)) {
                _Extension = _FileName.substring(_DotIndex + 1).toLowerCase(Locale.getDefault());
            }
        }

        return _Extension;
    }
}
