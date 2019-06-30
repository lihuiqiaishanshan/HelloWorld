package com.hisilicon.android.videoplayer.model;

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.activity.VideoActivity;
import com.hisilicon.android.videoplayer.model.dao.DBHelper;
import com.hisilicon.android.videoplayer.utils.Constants;
import com.hisilicon.android.videoplayer.utils.SystemProperties;
import com.hisilicon.android.videoplayer.view.HisiVideoView;

public class Common {
    private static String TAG = "Common";
    private Context context;
    private Activity activity;
    private int screenWidth;

    private boolean isPositive = false;

    private int hour = 0;

    private int minute = 0;

    public int limitHour = 0;

    public int limitMinute = 0;

    public int step = 10;

    public int init_step = 10;

    public int acc_step = 10;

    private int mode = 0;

    private int subtitleEncode = 0;

    private static List<VideoModel> data = null;

    private static int duration = 0;

    //private static boolean isScanDialogShow = false;

    private static boolean isResume = false;

    private static boolean isLastMediaFile = false;

    private static boolean isLoadSuccess = false;

    private static boolean haveShowNoFileToast = false;

    private static boolean isShowLoadingToast = true;

    private static int sortCount = -1;

    private TimePicker timePicker;

    private Handler dHandler = null;
    private DThread dThread = null;
    private boolean isDShow = false;
    private long startTime = 0;
    private long startTime1 = 0;

    public static void setSortCount(int sortCount) {
        Common.sortCount = sortCount;
    }

    public static int getSortCount() {
        return Common.sortCount;
    }

    public static boolean isShowLoadingToast() {
        return isShowLoadingToast;
    }

    public static void setShowLoadingToast(boolean isShowLoadingToast) {
        Common.isShowLoadingToast = isShowLoadingToast;
    }

    public static boolean haveShowNoFileToast() {
        return haveShowNoFileToast;
    }

    public static void setShowNoFileToast(boolean haveShowNoFileToast) {
        Common.haveShowNoFileToast = haveShowNoFileToast;
    }

    public static boolean isLastMediaFile() {
        return isLastMediaFile;
    }

    public static void setLastMediaFile(boolean isLastMediaFile) {
        Common.isLastMediaFile = isLastMediaFile;
    }

    public static boolean isResume() {
        return isResume;
    }

    public static void setResume(boolean isResume) {
        Common.isResume = isResume;
    }

    private static final Object mObjLock = new Object();

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

    public static List<VideoModel> getData() {
        synchronized (mObjLock) {
            return data;
        }
    }

    public static void setData(List<VideoModel> data) {
        synchronized (mObjLock) {
            Common.data = data;
        }
    }

    public int getStep() {
        return step * 1000;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getInitStep() {
        return init_step * 1000;
    }

    public void setInitStep(int init_step) {
        this.init_step = init_step;
    }

    public int getAccStep() {
        return acc_step * 1000;
    }

    public void setAccStep(int acc_step) {
        this.acc_step = acc_step;
    }

    public static boolean isLoadSuccess() {
        return isLoadSuccess;
    }

    public static void setLoadSuccess(boolean isLoadSuccess) {
        Common.isLoadSuccess = isLoadSuccess;
    }

    public Common(Context context, Activity activity, int screenWidth) {
        this.context = context;
        this.activity = activity;
        this.screenWidth = screenWidth;
    }


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

    public int ifContinue() {
        return 1;
    }

    public class MarkComparator implements Comparator<HashMap<String, Object>> {
        public int compare(HashMap<String, Object> object1, HashMap<String, Object> object2) {
            HashMap<String, Object> m1 = object1;
            HashMap<String, Object> m2 = object2;
            int i1 = Integer.parseInt(m1.get("mark").toString());
            int i2 = Integer.parseInt(m2.get("mark").toString());
            if (i1 >= i2) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    class DThread implements Runnable {
        Dialog dialog = null;
        Dialog predialog = null;

        public DThread(Dialog dialog) {
            this.dialog = dialog;
        }

        public void SetDialog(Dialog dialog) {
            if (this.predialog != null && this.predialog.isShowing()) {//If isShowing,this predialog need to dismiss later. So can't replease it.
                this.dialog = dialog;
            } else {
                this.predialog = this.dialog;
                this.dialog = dialog;
            }
        }

        public void run() {
            long endTime = System.currentTimeMillis();
            long distance = endTime - startTime1;

            if (distance >= Constants.DIALOG_SHOW) {
                if (dialog != null)
                    dialog.dismiss();
                if (predialog != null)
                    predialog.dismiss();

                dHandler.removeCallbacks(dThread);
                isDShow = false;
            } else {
                dHandler.postDelayed(this, 1000);
            }
        }

        public void dialogDismissNow() {
            if (dialog != null)
                dialog.dismiss();
            if (predialog != null)
                predialog.dismiss();

            dHandler.removeCallbacks(dThread);
            isDShow = false;
        }
    }

    public void updatestartTime1() {
        startTime1 = System.currentTimeMillis();
    }

    public void dialogAutoDismiss(Dialog dialog) {
        if (!isDShow) {
            dHandler = new Handler();
            dThread = new DThread(dialog);
            startTime1 = System.currentTimeMillis();
            dHandler.post(dThread);
            isDShow = true;
        } else {
            if (dThread != null)
                dThread.SetDialog(dialog);
            startTime1 = System.currentTimeMillis();
        }
    }

    public void dialogDismissAtOnce() {
        if (dThread != null)
            dThread.dialogDismissNow();
    }

    public void setJumpToDialog(final int title, int miniTime, final HisiVideoView videoView) {
        int m = miniTime / 1000 / 60;

        minute = m % 60;
        hour = m / 60;
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        LayoutInflater inflater = activity.getLayoutInflater();
        RelativeLayout linear = (RelativeLayout) inflater.inflate(R.layout.jumpto, null);
        timePicker = (TimePicker) linear.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                startTime1 = System.currentTimeMillis();
                if (view.getCurrentHour() > limitHour) {
                    view.setCurrentHour(limitHour);
                }

                int timeMinute = limitMinute % 60;
                if (view.getCurrentHour() == limitHour && view.getCurrentMinute() > timeMinute) {
                    view.setCurrentMinute(timeMinute);
                }

                                                    /*if (view.getCurrentHour() > 0)
                                                    {
                                                        int timeMinute = limitMinute % 60;
                                                        if (view.getCurrentMinute() > timeMinute)
                                                        {
                                                            view.setCurrentMinute(timeMinute);
                                                        }
                                                    }

                                                    if (view.getCurrentMinute() > limitMinute)
                                                    {
                                                        view.setCurrentMinute(limitMinute);
                                                    }

                                                    hour = view.getCurrentHour();
                                                    Common.this.minute = view.getCurrentMinute();*/
            }
        });
        builder.setView(linear);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                timePicker.clearFocus();
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
                if (!((hour > limitHour) || (minute > limitMinute))) {
                    if (activity instanceof VideoActivity) {
                        VideoActivity va = (VideoActivity) activity;
                        va.resetFastSpeedToNomal();
                    }
                    videoView.seekTo(hour * 60 * 60 * 1000 + minute * 60 * 1000);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                startTime1 = System.currentTimeMillis();
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    timePicker.clearFocus();
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();
                    if (!((hour > limitHour) || (hour == limitHour && minute > (limitMinute % 60)))) {
                        if (activity instanceof VideoActivity) {
                            VideoActivity va = (VideoActivity) activity;
                            va.resetFastSpeedToNomal();
                        }
                        videoView.seekTo(hour * 60 * 60 * 1000 + minute * 60 * 1000);
                        dialog.dismiss();
                    }
                }
                return false;
            }
        });
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).requestFocus();
        dialogAutoDismiss(dialog);
    }

/*//for lint
    public void setQuickPlayDialog(final int title, final HisiVideoView videoView)
    {
        Dialog dialog = new Dialog(context);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        LayoutInflater inflater = activity.getLayoutInflater();
        RelativeLayout relative = (RelativeLayout)inflater.inflate(R.layout.quick_play, null);

        TextView unit = (TextView)relative.findViewById(R.id.quickPlayUnit);
        unit.setText(" " + context.getString(R.string.second));

        final EditText quickPlay = (EditText)relative.findViewById(R.id.quickPlay);
        quickPlay.setWidth((int) (screenWidth * 0.4));
        quickPlay.setText(String.valueOf(sharedPreferencesOpration(Constants.SHARED, "quickplay", 0, 10, false)));

        builder.setView(relative);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                                  {
                                      public void onClick(DialogInterface dialog, int which)
                                      {
                                          int value = 0;
                                          String str = quickPlay.getText().toString();

                                          if (str.length() < 1)
                                          {
                                              value = Constants.MIN_SEEK;
                                          }
                                          else
                                          {
                                              value = Integer.parseInt(str);
                                          }

                                          if (value == 0)
                                          {
                                              value = 1;
                                          }

                                          sharedPreferencesOpration(Constants.SHARED, "quickplay", value, 0, true);
                                          setStep(value);
                                      }
                                  }).setNegativeButton(R.string.cancle, null);

        dialog = builder.create();
        dialog.show();
    }
*/

    public static View setDialogTitleAndListView(Context context, View contentView, int title) {
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

    public static void setDialogWidth(Dialog dialog, int screenWidth, int x, int y) {
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        if (isPortrait()) {
            params.width = screenWidth;
        } else {
            params.width = screenWidth / 2;
        }
        if (screenWidth == 640) {
            params.x = x - 150;
            params.y = y;
        } else if (screenWidth == 960) {
            params.x = x + 150;
            params.y = y - 60;
        }
        dialog.getWindow().setAttributes(params);
    }

    public void setDialog(Dialog dialog, View content, int x, int y, int rate) {
        dialog.setContentView(content);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        if ((rate != 0) && (rate != -1)) {
            params.width = screenWidth / rate;
        }

        params.x = x;
        params.y = y;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    public int sharedPreferencesOpration(String name, String key, int value, int defaultValue, boolean isEdit) {
        if (isEdit) {
            SharedPreferences.Editor editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
            editor.putInt(key, value);
            editor.commit();
            return 0;
        } else {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE).getInt(key, defaultValue);
        }
    }


    public boolean isValueTrue(String key) {
        return sharedPreferencesOpration(Constants.SHARED, key, 0, 0, false) == 1;
    }

    public int getPipVideoPosition() {
        return context.getSharedPreferences(Constants.SHARED, Context.MODE_PRIVATE).getInt(Constants.KEY_PIP_VIDEO_POS, 0);
    }

    public void savePipVideoPosition(int pos) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SHARED, Context.MODE_PRIVATE).edit();
        editor.putInt(Constants.KEY_PIP_VIDEO_POS, pos);
        editor.commit();
    }

    public boolean getPipVideoTag() {
        return context.getSharedPreferences(Constants.SHARED, Context.MODE_PRIVATE).getBoolean(Constants.KEY_PIP_VIDEO_TAG, false);
    }

    public void savePipVideoTag(boolean isPip){
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SHARED, Context.MODE_PRIVATE).edit();
        editor.putBoolean(Constants.KEY_PIP_VIDEO_TAG, isPip);
        editor.commit();
    }

    public static String transferredMeaning(String path) {
        boolean needTransfer = isNetURI(path);
        if (!needTransfer) {
            /*return when video is local path,transfer will error such as name is
              "%5B01%5D4K超高分 秒杀资源 T-ARA - Number 9%5BY4321.CC%5D(HEVC_3840x2160).mkv"
              another bug is for play video name contains #, such as
              [HD]_AVC_10.OMbps_1920x816_3#1_DTS_755Kbps_6channels_3track.mkv
            */
            return path;
        }

        path = Uri.decode(path);

        if (path.contains("%")) {
            path = path.replace("%", "%25");
        }

        if (path.contains("#")) {
            path = path.replace("#", "%23");
        }

        if (needTransfer && path.contains(" ")) {
            path = path.replace(" ", "%20");
        }

        return path;
    }

    public static String getTimeFormatValue(long time) {
        long t = time / 1000;

        return MessageFormat.format(
                "{0,number,00}:{1,number,00}:{2,number,00}",
                t / 60 / 60, t / 60 % 60, t % 60);
    }

    public static String getFormatDate(long date) {
        Date d = new Date(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd/kk:mm:ss");

        return sdf.format(d);
    }

    public static ArrayList<VideoModel> sortFile(ArrayList<VideoModel> fileList,
                                                 final int sortMethod) {
        Collections.sort(fileList, new Comparator<VideoModel>() {
            public int compare(VideoModel object1, VideoModel object2) {
                return compareFile(object1, object2, sortMethod);
            }
        });
        return fileList;
    }

    public static int compareFile(VideoModel object1, VideoModel object2,
                                  int sortMethod) {
        if (sortMethod == Constants.SORT_NAME) {
            return compareByName(object1, object2);
        } else if (sortMethod == Constants.SORT_SIZE) {
            int len = compareBySize(object1.getSize(), object2.getSize());
            if (len == 0) {
                return compareByName(object1, object2);
            } else {
                return compareBySize(object1.getSize(), object2.getSize());
            }
        } else if (sortMethod == Constants.SORT_TIME) {
            int len = compareByDate(object1.getAddedTime(), object2
                    .getAddedTime());
            if (len == 0) {
                return compareByName(object1, object2);
            } else {
                return compareByDate(object1.getAddedTime(), object2
                        .getAddedTime());
            }
        }

        return 0;
    }

    private static int compareByName(VideoModel object1, VideoModel object2) {
        String objectName1 = object1.getTitle().toLowerCase();
        String objectName2 = object2.getTitle().toLowerCase();
        int result = objectName1.compareTo(objectName2);

        if (result == 0) {
            return 0;
        } else if (result < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    private static int compareBySize(long object1, long object2) {
        long diff = object1 - object2;

        if (diff > 0) {
            return 1;
        } else if (diff == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    public static int compareByDate(long object1, long object2) {
        long diff = object1 - object2;

        if (diff > 0) {
            return 1;
        } else if (diff == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    public static boolean isPortrait() {
        String screenOrientation = SystemProperties.get("persist.sys.screenorientation");
        if (screenOrientation != null) {
            if ("portrait".equals(screenOrientation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNetURI(String path) {
        String uri = path.toLowerCase();
        if (uri.startsWith("http://"))
            return true;
        if (uri.startsWith("https://"))
            return true;
        if (uri.startsWith("rtsp://"))
            return true;
        if (uri.startsWith("mms://"))
            return true;
        if (uri.startsWith("mmst://"))
            return true;
        if (uri.startsWith("mmsu://"))
            return true;
        if (uri.startsWith("mmsh://"))
            return true;
        if (uri.startsWith("udp://"))
            return true;
        if (uri.startsWith("rtp://"))
            return true;
        if (uri.startsWith("rtmp://"))
            return true;
        if (uri.startsWith("rtmps://"))
            return true;
        if (uri.startsWith("rtmpt://"))
            return true;
        if (uri.startsWith("rtmpe://"))
            return true;
        return false;
    }

    public static boolean isSecurePath(String filePath) { //for fority:Path Manipulation
        String blackListChars = "..";
        return (filePath.indexOf(blackListChars) < 0);
    }

    public static String readLine(Reader reader) {
        char[] chars = new char[1];
        StringBuilder buf = new StringBuilder();
        try {
            int readCount = 0;
            while (reader.read(chars) != -1) {
                readCount++;
                if (chars[0] == '\n') {
                    break;
                }
                buf.append(chars[0]);
            }
            if (readCount == 0) {
                return null;
            }
        } catch (IOException e) {
        }
        return buf.toString();
    }

    public static boolean isSecureSqlValue(String strSqlValue) {
        return !(strSqlValue.contains(";") || strSqlValue.contains("'"));
    }
}
