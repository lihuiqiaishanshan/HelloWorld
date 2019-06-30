package com.hisilicon.tvui.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import android.content.Context;

/**
 *
 * Map language code into Chinese or English.
 *
 */
public class LanguageMap
{
    private static final String DEFAULT_FILE = "en-US_ISO639.properties";

    private Context mContext;

    /*qaa*/
    private static final String en_str_qaa = "Original Language";
    private static final String zh_str_qaa = "\u539f\u59cb\u8bed\u8a00";

    /*qad*/
    private static final String en_str_qad = "Audio Description";
    private static final String zh_str_qad = "\u97f3\u9891\u63cf\u8ff0";

    public LanguageMap(Context context)
    {
        mContext = context;
    }

    private String getPropertiesFileName()
    {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        String mFileName = language + "-" + country + "_ISO639.properties";
        return mFileName;
    }

    public String getLanguage(String languageCode)
    {

        Properties languageProperties = new Properties();
        boolean isFileExist = true;
        InputStream in = null;
        try
        {
            in = mContext.getAssets().open(getPropertiesFileName());
            languageProperties.load(in);
        }
        catch (IOException e)
        {
            LogTool.e(LogTool.MAUDIO, "getLauguage open PropertiesFile failed: " + e.getMessage());
            isFileExist = false;
        }
        finally
        {
            if(null != in)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    LogTool.e(LogTool.MAUDIO, "getLauguage close InputStream failed: " + e.getMessage());
                }
            }
        }
        if (!isFileExist)
        {
            InputStream defaultIn = null;
            try
            {
                defaultIn = mContext.getAssets().open(DEFAULT_FILE);
                languageProperties.load(defaultIn);
            }
            catch (IOException e)
            {
                LogTool.e(LogTool.MAUDIO, "getLauguage open DefaultFile failed: " + e.getMessage());
            }
            finally
            {
                if(null != defaultIn)
                {
                    try
                    {
                        defaultIn.close();
                    }
                    catch (IOException e)
                    {
                        LogTool.e(LogTool.MAUDIO, "getLauguage close InputStream failed: " + e.getMessage());
                    }
                }
            }
        }
        String systemLanguage = Locale.getDefault().getLanguage();

        LogTool.d(LogTool.MAUDIO, "systemLanguage: " + systemLanguage );

        String language = languageProperties.getProperty(languageCode.toLowerCase(Locale.getDefault()));
        if (language != null)
        {
            return language;
        }
        else if ((languageCode.length() > 0) && ((languageCode.charAt(0) == 'q') || (languageCode.charAt(0) == 'Q')))
        {
            // if(languageCode.charAt(0)=='q') //Reserved for internal use
            if (systemLanguage.equals("zh"))
            {
                if (languageCode.equalsIgnoreCase("qaa") || languageCode.equalsIgnoreCase("qab") || languageCode.equalsIgnoreCase("qac"))
                {
                    return zh_str_qaa;
                }
                else if (languageCode.equalsIgnoreCase("qad"))
                {
                    return zh_str_qad;
                }
                else
                {
                    return languageProperties.getProperty("reserved");
                }
            }
            else
            {
                if (languageCode.equalsIgnoreCase("qaa") || languageCode.equalsIgnoreCase("qab") || languageCode.equalsIgnoreCase("qac"))
                {
                    return en_str_qaa;
                }
                else if (languageCode.equalsIgnoreCase("qad"))
                {
                    return en_str_qad;
                }
                else
                {
                    return languageCode;
                }
            }
        }
        else if ((languageCode.length() > 0) && (languageCode.equalsIgnoreCase("nar")))
        {
            return languageCode;
        }
        else
        {
            return languageProperties.getProperty("unknown");
        }
    }
}
