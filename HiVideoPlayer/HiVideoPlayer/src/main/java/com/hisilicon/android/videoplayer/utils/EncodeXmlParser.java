package com.hisilicon.android.videoplayer.utils;

import android.content.Context;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.model.EncodeNameValue;

import java.util.ArrayList;
import java.util.List;

public class EncodeXmlParser
{
    private Context context;

    private List < List < EncodeNameValue >> encodesList;

    public EncodeXmlParser(Context context)
    {
        this.context = context;
        parser();
    }

    public List < List < EncodeNameValue >> getEncodesList()
    {
        return encodesList;
    }

    private void parser()
    {
        String[] encodeName = context.getResources().getStringArray(
            R.array.subtitle_encode);
        encodesList = new ArrayList < List < EncodeNameValue >> ();
        List <EncodeNameValue> list = new ArrayList <EncodeNameValue>();
        for (int i = 0; i < encodeName.length; i++)
        {
            EncodeNameValue encodeNameValue = new EncodeNameValue();
            encodeNameValue.setEncodeName(encodeName[i]);
            encodeNameValue.setEncodeValue(i);
            list.add(encodeNameValue);
        }

        encodesList.add(list);
    }
}
