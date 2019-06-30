
package com.hisilicon.higallery.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hisilicon.higallery.R;
import com.hisilicon.higallery.core.BitmapDecodeUtils;
import android.util.Log;

public class Utils {
    public static final int EXPLORE_MODE = 0;
    public static final int ROTATE_MODE = 1;
    public static final int SCALE_MODE = 2;
    public static final int SLIDEIND_MODE = 3;
    public static final int SHOW_INFO = 4;
    public static final int DISMISS_INFO = 5;

    static final String TAG = "Utils";

    public static void showInfo(Context context, Handler handler, int mode) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View mItemLayout;
        switch (mode) {
            case EXPLORE_MODE:
                mItemLayout = inflater.inflate(R.layout.help_info_explore, null);
                break;
            case ROTATE_MODE:
                mItemLayout = inflater.inflate(R.layout.help_info_rotate, null);
                break;
            case SCALE_MODE:
                mItemLayout = inflater.inflate(R.layout.help_info_scale, null);
                break;
            // case SLIDEIND_MODE:
            // break;
            default:
                mItemLayout = inflater.inflate(R.layout.help_info_explore, null);
        }
        Message message = new Message();
        message.obj = mItemLayout;
        message.what = SHOW_INFO;
        handler.sendMessage(message);
    }
    public static String getProductLine(Context context){
        String str = "product";
        String result = getConfig(context, str);
        return result;
    }

    public static boolean getThumbnail(Context context){
        String str = "thumbnailEnable";
        String strRet = getConfig(context, str);
        boolean result = false;
        if(strRet.equals("true")){
            result = true;
        }
        return result;
    }

    public static boolean getBackgroundMusic(Context context){
        String str = "backgroundMusicEnable";
        String strRet = getConfig(context, str);
        boolean result = false;
        if(strRet.equals("true")){
            result = true;
        }
        return result;
    }
    public static boolean getHDR(Context context){
        String str = "HDREnable";
        String strRet = getConfig(context, str);
        boolean result = false;
        if(strRet.equals("true")){
            result = true;
        }
        return result;
    }

    public static boolean getNetOpenSource(Context context){
        String str = "netOpenSourceEnable";
        String strRet = getConfig(context, str);
        boolean result = false;
        if(strRet.equals("true")){
            result = true;
        }
        return result;
    }

    public static String getConfig(Context context, String key) {
        String value = "";
        try {
            XmlResourceParser xrp;
            String brand_name = SystemProperties.get("ro.product.vendor.brand");
            if(brand_name.startsWith("HiDPT")) {
                xrp = context.getResources().getXml(R.xml.product_dpt_config);
            } else {
                xrp = context.getResources().getXml(R.xml.product_stb_config);
            }
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return "";
                    }
                    xrp.next();
                }
                if (xrp.getName().equals("config")) {
                    String id = xrp.getAttributeValue(0);
                    String val = xrp.nextText();
                    if(id.equals(key)){
                        value = val;
                    }
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Ill-formatted getConfig.xml file");
        } catch (java.io.IOException ioe) {
            Log.e(TAG, "Unable to read getConfig.xml file");
        }
        if(value.equals("")){
            Log.e(TAG,"key param error");
        }

        Log.i(TAG,"get information ["+key+","+value+"]");
        return value;

    }
}
