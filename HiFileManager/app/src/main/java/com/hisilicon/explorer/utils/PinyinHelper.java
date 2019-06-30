package com.hisilicon.explorer.utils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import android.util.Log;

public class PinyinHelper{
    private static final String TAG = "HiFileManager_PyHelper";
    private static PinyinHelper instance;
    private Properties properties = null;

    public static String[] getUnformattedHanyuPinyinStringArray(char ch){
        return getInstance().getHanyuPinyinStringArray(ch);
    }

    private PinyinHelper(){
        initResource();
    }

    public static PinyinHelper getInstance(){
        if(instance==null){
            instance = new PinyinHelper();
        }
        return instance;
    }

    private void initResource(){
        BufferedInputStream in = null;
        try{
            final String resourceName = "/assets/unicode_to_hanyu_pinyin.txt";
//          final String resourceName = "/assets/unicode_py.ini";

            properties=new Properties();
            in = getResourceInputStream(resourceName);
            properties.load(in);

        } catch (FileNotFoundException e){
            Log.e(TAG,"Error : ",e);
        } catch (IOException e){
            Log.e(TAG,"Error : ",e);
        } finally{
            try {
            in.close();
            } catch (IOException e) {
                Log.e(TAG,"Error : ",e);
            }
        }
    }

    private BufferedInputStream getResourceInputStream(String resourceName){
        return new BufferedInputStream(PinyinHelper.class.getResourceAsStream(resourceName));
    }

    private String[] getHanyuPinyinStringArray(char ch){
        String pinyinRecord = getHanyuPinyinRecordFromChar(ch);

        if (null != pinyinRecord){
            int indexOfLeftBracket = pinyinRecord.indexOf(Field.LEFT_BRACKET);
            int indexOfRightBracket = pinyinRecord.lastIndexOf(Field.RIGHT_BRACKET);

            String stripedString = pinyinRecord.substring(indexOfLeftBracket
                    + Field.LEFT_BRACKET.length(), indexOfRightBracket);

            return stripedString.split(Field.COMMA);

        } else
            return null;

    }

    private String getHanyuPinyinRecordFromChar(char ch){
        int codePointOfChar = ch;
        String codepointHexStr = Integer.toHexString(codePointOfChar).toUpperCase();
        String foundRecord = properties.getProperty(codepointHexStr);
        return foundRecord;
    }

    class Field{
        static final String LEFT_BRACKET = "(";
        static final String RIGHT_BRACKET = ")";
        static final String COMMA = ",";
    }

}
