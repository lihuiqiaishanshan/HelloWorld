package com.hisilicon.android.videoplayer.utils.base;

import java.io.File;

import com.hisilicon.android.videoplayer.utils.FileTool;

public class SubtitleFileFilter implements java.io.FileFilter
{
    private static final String MIME_SUBTITLE = "srt,ssa,sub,ass,idx,smi";

    public boolean accept(File pFile)
    {
        if (MIME_SUBTITLE.contains(FileTool.getExtension(pFile)))
        {
            return true;
        }
        return false;
    }
}
