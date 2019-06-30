package com.hisilicon.tvui.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 *
 * The class is used for pw encryption,used the MD5 algorithm<br>
 *
 */
public class SHA
{
    private static final String ENCRYPTION_ALGORITHM = "SHA-512";
    private static final int STRING_INDEX_TO_UPPER_BEGIN = 8;
    private static final int STRING_INDEX_TO_UPPER_END = 24;

    public static String toSHA(String str)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] byteArray = messageDigest.digest();
            StringBuffer shaStrBuff = new StringBuffer();
            for (int i = 0; i < byteArray.length; i++)
            {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                    shaStrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                else
                    shaStrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
            return shaStrBuff.substring(STRING_INDEX_TO_UPPER_BEGIN, STRING_INDEX_TO_UPPER_END).toUpperCase(Locale.getDefault());
        }
        catch (NoSuchAlgorithmException e)
        {
            //e.printStackTrace();
            return null;
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }
}
