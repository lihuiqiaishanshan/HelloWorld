package com.hisilicon.android.music;

import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.hisilicon.android.music.MusicModel;

/**
 * common class. used to get data.
 * @author
 */
public class Common {
    private static String TAG = "Common";
    private Context context;
    private Activity activity;
    private int screenWidth;
    /* the confirm button flag */
    private boolean isPositive = false;
    /* seek to the point: hour */
    private int hour = 0;
    /* seek to the point: minute */
    private int minute = 0;
    /* the video length. UNIT:hour */
    public int limitHour = 0;
    /* the video length. UNIT:minite */
    public int limitMinute = 0;
    /* the interval for forward and rewind */
    public int step = 10;
    /* play mode */
    private int mode = 0;

    private int subtitleEncode = 0;
    /* play list info */
    private static List<MusicModel> data = null;
    /* video length */
    private static int duration = 0;
    ///* whether to show scan dialog? */
    //public static boolean isScanDialogShow = false; //for fority
    ///* disable subtitle */
    //public static boolean isCloseSubForever = false; //for fority
    ///* whether the resume function has been called? */
    //public static boolean isResume = false; //for fority
    /* whether the current file is the last media file? used in order mode. */
    private static final Object objLock = new Object();
    private static boolean isLastMediaFile = false;
    public static boolean isLastMediaFile() {
        return Common.isLastMediaFile;
    }
    public static void setisLastMediaFile(boolean isLastMediaFile){
        Common.isLastMediaFile = isLastMediaFile;
    }

    /* whether the current file is the first media file? used in order mode. */
    private static boolean isFirstMediaFile = false;
    public static boolean isFirstMediaFile() {
        return isFirstMediaFile;
    }
    public static void setisFirstMediaFile(boolean isFirstMediaFile){
        Common.isFirstMediaFile = isFirstMediaFile;
    }

    /* Is data ready? */
    private static boolean isLoadSuccess = false;
    public static boolean isLoadSuccess() {
        return Common.isLoadSuccess;
    }
    public static void setisLoadSuccess(boolean isLoadSuccess){
        Common.isLoadSuccess = isLoadSuccess;
    }

    /* Has shown no filetoast? */
    private static boolean haveShowNoFileToast = false;
    public static boolean haveShowNoFileToast() {
        return Common.haveShowNoFileToast;
    }
    public static void sethaveShowNoFileToast(boolean haveShowNoFileToast){
        Common.haveShowNoFileToast = haveShowNoFileToast;
    }

    /* Is showing loading toast? */
    private static boolean isShowLoadingToast = true;
    public static boolean isShowLoadingToast() {
        return Common.isShowLoadingToast;
    }
    public static void setisShowLoadingToast(boolean isShowLoadingToast){
        Common.isShowLoadingToast = isShowLoadingToast;
    }

    /* sort mode */
    private static int mySortCount = -1;
    public static int sortCount() {
        return Common.mySortCount;
    }
    public static void setsortCount(int sortCount){
        Common.mySortCount = sortCount;
    }

    private static boolean isPrevButton = false;
    public static boolean isPrevButton() {
        return Common.isPrevButton;
    }
    public static void setisPrevButton(boolean isPrevButton){
        Common.isPrevButton = isPrevButton;
    }

    private static boolean isFileList = false;
    public static boolean isFileList() {
        return Common.isFileList;
    }
    public static void setisFileList(boolean isFileList){
        Common.isFileList = isFileList;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean isPositive) {
        this.isPositive = isPositive;
    }

    public static int getDuration() {
        return duration;
    }

    public static void setDuration(int duration) {
        Common.duration = duration;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getSubtitleEncode() {
        return subtitleEncode;
    }

    public void setSubtitleEncode(int encode) {
        this.subtitleEncode = encode;
    }

    public static List<MusicModel> getData() {
        synchronized (Common.objLock) {
            return data;
        }
    }

    public static void setData(List<MusicModel> data) {
        synchronized (Common.objLock) {
            Common.data = data;
        }
    }

    public int getStep() {
        return step * 1000;
    }

    public void setStep(int step) {
        this.step = step;
    }


    public Common(Context context, Activity activity, int screenWidth) {
        this.context = context;
        this.activity = activity;
        this.screenWidth = screenWidth;
    }

    /**
     * set the play mode
     * @param model
     *            mode
     */
    public void switchPlayModel(int model) {
        switch (model) {
            case Constants.ALLNOCYCLE:
                mode = Constants.ALLNOCYCLE;
                break;

            case Constants.ALLCYCLE:
                mode = Constants.ALLCYCLE;
                break;

            case Constants.ONECYCLE:
                mode = Constants.ONECYCLE;
                break;

            case Constants.ONENOCYCLE:
                mode = Constants.ONENOCYCLE;
                break;

            case Constants.RANDOM:
                mode = Constants.RANDOM;
                break;

            default:
                break;
        }
    }

    /**
     * whether to enable resume broken play?
     * @return
     * @author
     */
    public int ifContinue() {
        int value = 0;

        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.parse(Constants.CONTENT_URI);
        Uri updateIdUri = ContentUris.withAppendedId(uri,
                                            Constants.CONTINUE_PLAYER_FLAG);

        if ((null == cr) || (null == uri) || (null == updateIdUri)) {
           return value;
        }

        Cursor c = cr.query(updateIdUri, null, null, null, null);
        if (null != c) {
            c.moveToFirst();
            /* value equal to 1, means enable */
            value = c.getInt(c.getColumnIndex("VALUE"));
            c.close();
            c = null;
        }
        return value;
    }

    /**
     * compare the two operands in the list.
     * @author
     */
    public class MarkComparator implements Comparator<HashMap<String, Object>> {

        public int compare(HashMap<String, Object> object1,
                           HashMap<String, Object> object2) {
            int ret = -1;
            if ((null == object1) || (null == object2) || (null == object1.get("mark"))
                  || (null == object2.get("mark"))) {
                return ret;
            }
                
            int i1 = Integer.parseInt(object1.get("mark").toString());
            int i2 = Integer.parseInt(object2.get("mark").toString());

            if (i1 >= i2) {
                ret = 1;
            }
            return ret;
       }
    }

    /**
     * set the content of dialog
     * @param context
     * @param contentView
     *            contentView
     * @param title
     *            the title of dialog
     * @return linear Layout
     * @author
     */
    public static View setDialogTitleAndListView(Context context,
                                                 View contentView, int title) {
        LinearLayout parentLinear = new LinearLayout(context);
        parentLinear.setOrientation(LinearLayout.VERTICAL);

        if (title != -1) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(android.R.drawable.ic_menu_more);
            imageView.setPadding(10, 10, 4, 10);
            TextView textView = new TextView(context);
            textView.setTextSize(22);
            textView.setTextColor(Color.WHITE);
            textView.setText(title);
            textView.setPadding(4, 17, 10, 10);
            LinearLayout childLinear = new LinearLayout(context);
            childLinear.addView(imageView);
            childLinear.addView(textView);
            ImageView view = new ImageView(context);
            view.setBackgroundColor(Color.WHITE);
            view.setMaxHeight(1);
            view.setMinimumHeight(1);
            parentLinear.addView(childLinear);
            parentLinear.addView(view);
        }

        parentLinear.addView(contentView);
        return parentLinear;
    }

    /**
     * set the width of dialog
     * @param dialog
     *            the dialog to set width
     * @param screenWidth
     *            dialog width
     * @author
     */
    public static void setDialogWidth(Dialog dialog, int screenWidth, int x,
                                      int y) {
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = screenWidth / 2;
        params.x = x;
        params.y = y;
        dialog.getWindow().setAttributes(params);
    }

    /**
     * set the postion of dialog
     * @param dialog
     * @param content
     * @param x
     * @param y
     * @param rate
     * @author
     */
    public void setDialog(Dialog dialog, View content, int x, int y, int rate) {
        dialog.setContentView(content);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();

        if (rate != -1) {
            params.width = screenWidth / rate;
        }

        params.x = x;
        params.y = y;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    /**
     * edit or get the sharedpreferences
     * @param name
     *            sharedpreferences file name.
     * @param key
     *            the key of new sharedpreferences
     * @param value
     *            the value of new sharedpreferences
     * @param defaultValue
     *            the value of sharedpreferences. used for reading.
     * @param isEdit
     *            edit or read.
     * @return
     * @author
     */
    public int sharedPreferencesOpration(String name, String key, int value,
                                         int defaultValue, boolean isEdit) {
        if (isEdit) {
            SharedPreferences.Editor editor = context.getSharedPreferences(
                                                  name, context.MODE_PRIVATE).edit();
            editor.putInt(key, value);
            editor.commit();
            return 0;
        } else {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE)
                   .getInt(key, defaultValue);
        }
    }

    /**
     * cheractor transfer meaning
     * @param path
     * @return
     */
    public static String transferredMeaning(String path) {
        if (path.contains("%")) {
            path = path.replace("%", "%25");
        }

        if (path.contains("#")) {
            path = path.replace("#", "%23");
        }

        return path;
    }

    /**
     * transform the time format
     * @param time
     * @return
     */
    public static String getTimeFormatValue(long time) {
        long t = time / 1000;
        return MessageFormat.format(
                   "{0,number,00}:{1,number,00}:{2,number,00}", t / 60 / 60,
                   t / 60 % 60, t % 60);
    }

    /**
     * transform the date format
     * @param date
     * @return the string of date
     * @author
     */
/*for lint
    public static String getFormatDate(long date) {
        Date d = new Date(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd/kk:mm:ss");
        return sdf.format(d);
    }
*/

    /**
     * sort the file list.
     * @param fileList
     * @param sortMethod
     * @return the result
     */
    public static ArrayList<MusicModel> sortFile(
        ArrayList<MusicModel> fileList, final int sortMethod) {
        Collections.sort(fileList, new Comparator<MusicModel>() {
            public int compare(MusicModel object1, MusicModel object2) {
                return compareFile(object1, object2, sortMethod);
            }
        });
        return fileList;
    }

    /**
     * compare files through an appointed sort method.
     * @param object1
     *            the first file
     * @param object2
     *            the second file
     * @param sortMethod
     *            the sort method
     * @return the result
     */
    public static int compareFile(MusicModel object1, MusicModel object2,
                                  int sortMethod) {
        if (sortMethod == Constants.SORT_NAME) {
            return compareByName(object1, object2);
        } else if (sortMethod == Constants.SORT_SIZE) {
            return compareBySize(object1.getSize(), object2.getSize());
        } else if (sortMethod == Constants.SORT_TIME) {
            return compareByDate(object1.getAddedTime(),
                                    object2.getAddedTime());
        }

        return 0;
    }

    /**
     * compare files by name.
     * @param object1
     *            the first file.
     * @param object2
     *            the second file
     * @return the result
     */
    private static int compareByName(MusicModel object1, MusicModel object2) {
        String objectName1 = object1.getTitle();
        String objectName2 = object2.getTitle();
        int result = objectName1.compareTo(objectName2);

        if (result == 0) {
            return 0;
        } else if (result < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * coomare files by size
     * @param object1
     *            the first file
     * @param object2
     *            the second file
     * @return the result.
     */
    private static int compareBySize(long object1, long object2) {
        long diff = object1 - object2;

        if (diff > 0)
        { return 1; }
        else if (diff == 0)
        { return 0; }
        else
        { return -1; }
    }

    /**
     * compare files by date
     * @param object1
     *            the first file
     * @param object2
     *            the second file
     * @return the result.
     */
    public static int compareByDate(long object1, long object2) {
        long diff = object1 - object2;

        if (diff > 0)
        { return 1; }
        else if (diff == 0)
        { return 0; }
        else
        { return -1; }
    }

    public static boolean isSecurePath(String filePath) { //for fority:Path Manipulation
        String blackListChars = "..";
        return (filePath.indexOf(blackListChars)< 0);
    }

    public static String readLine(Reader reader)
    {
        char[] chars = new char[1];
        StringBuilder buf = new StringBuilder();
        try
        {
            int readCount = 0;
            while (reader.read(chars) != -1)
            {
                readCount++;
                if (chars[0] == '\n')
                {
                    break;
                }
                buf.append(chars[0]);
            }
            if (readCount == 0)
            {
                return null;
            }
        }
        catch (IOException e)
        {
            Log.e(TAG,"IOException in readLine");
        }
        return buf.toString();
    }

    public static boolean isSecureSqlValue(String strSqlValue) {
       return (strSqlValue.indexOf(";") < 0) ;
    }
}
