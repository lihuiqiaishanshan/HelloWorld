package com.hisilicon.tvui.record;

import java.io.File;

public class RecordedFile
{
    private String mDir = "";

    private String mName = "";

    private String mDate = "";

    private String mDuration = "";

    private String mResolution = "";

    private String mSize = "";

    private int mPosition = -1;

    public RecordedFile()
    {

    }

    public RecordedFile(String name, String date, String duration, String resolution, String size)
    {
        this.mName = name;
        this.mDate = date;
        this.mDuration = duration;
        this.mResolution = resolution;
        this.mSize = size;
    }

    public String getDir()
    {
        return mDir;
    }

    public void setDir(String dir)
    {
        if (null == dir)
        {
            dir = "";
        }
        this.mDir = dir;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        if (null == name)
        {
            name = "";
        }
        this.mName = name;
    }

    public String getDate()
    {
        return mDate;
    }

    public void setDate(String date)
    {
        if (null == date)
        {
            date = "";
        }
        this.mDate = date;
    }

    public String getDuration()
    {
        return mDuration;
    }

    public void setDuration(String duration)
    {
        if (null == duration)
        {
            duration = "";
        }
        this.mDuration = duration;
    }

    public String getResolution()
    {
        return mResolution;
    }

    public void setResolution(String resolution)
    {
        if (null == resolution)
        {
            resolution = "";
        }
        this.mResolution = resolution;
    }

    public String getSize()
    {
        return mSize;
    }

    public void setSize(String size)
    {
        if (null == size)
        {
            size = "";
        }
        this.mSize = size;
    }

    public boolean isRadio()
    {
        if (null != mName)
        {
            if (mName.contains(".ts.radio"))
            {
                return true;
            }
        }

        return false;
    }

    public String getFilePath()
    {
        return mDir + File.separator + mName;
    }

    public void setPosition(int position)
    {
        this.mPosition = position;
    }

    public int getPosition()
    {
        return mPosition;
    }
}
