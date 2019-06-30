
package com.hisilicon.launcher.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import java.lang.reflect.Method;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.hardware.usb.IUsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import com.hisilicon.launcher.util.LogHelper;
import android.widget.Toast;

import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.launcher.MyApplication;
import com.hisilicon.launcher.R;
import com.hisilicon.launcher.view.LayoutTag;

/**
 * util
 *
 * @author janey
 */
public class Util {

    // Access to all applications (to change for change)
    public static final int ALL_APP = 0;
    // To clear the data successfully
    public static final int CLEAR_USER_DATA = 1;
    // Remove data failed
    public static final int NOT_CLEAR_USER_DATA = 2;

    private static final String PackageDTV = "com.hisilicon.dtv";
    private static final String TAG = "Util";

    /**
     * get all app
     *
     * @param mContext
     * @return applist
     */
    public static List<ResolveInfo> getAllApps(Context context) {
        if (context == null) {
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        // all app list
        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN, null);
        leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> tempAppList = packageManager.queryIntentActivities(
                leanbackIntent, 0);

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        if (tempAppList != null) {
            tempAppList.addAll(packageManager.queryIntentActivities(
                    mainIntent, 0));
            LogHelper.d(TAG, "tempAppList->" + tempAppList);
            // Remove repeat applications
            tempAppList = removeRepeat(tempAppList);
            // System application of filter does not need to display
            HashMap<String, Boolean> map = filterAppParse(context);
            LogHelper.d(TAG, "tempAppList size before filter->" + tempAppList.size());
            for (int i = 0; i < tempAppList.size(); ) {
                ResolveInfo info = tempAppList.get(i);
                String pkg = info.activityInfo.packageName;
                if (map.get(pkg) != null) {
                    tempAppList.remove(i);
                } else {
                    i++;
                }
            }
            LogHelper.d(TAG, "tempAppList size after filter->" + tempAppList.size());
            Collections.sort(tempAppList, new ResolveInfo.DisplayNameComparator(
                    packageManager));
            LogHelper.d(TAG, "tempAppList size after sort->" + tempAppList.size());
        }
        return tempAppList;
    }

    // Remove repeat applications
    public static List removeRepeat(List<ResolveInfo> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).activityInfo.packageName.equals(list.get(i).activityInfo.packageName)) {
                    list.remove(j);
                }
            }
        }
        return list;
    }

    /**
     * The analytical needs to filter the XML file
     *
     * @param context
     * @return Application of set
     */
    private static HashMap<String, Boolean> filterAppParse(Context context) {
        // The application list filter
        HashMap<String, Boolean> filterList = new HashMap<String, Boolean>();
        if (context != null) {
            InputStream fis = context.getResources().openRawResource(
                    R.raw.filter_apps);
            if (fis != null) {
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xmlPullParser = factory.newPullParser();
                    xmlPullParser.setInput(fis, "UTF-8");
                    int eventType = xmlPullParser.getEventType();
                    String name = "";
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                if (xmlPullParser.getName().equalsIgnoreCase("PackageName")) {
                                    name = xmlPullParser.nextText();
                                    if (!TextUtils.isEmpty(name)) {
                                        filterList.put(name.trim(), true);
                                    }
                                }
                                break;
                        }
                        eventType = xmlPullParser.next();
                    }
                } catch (XmlPullParserException e) {
                    LogHelper.e(TAG, e.getMessage());
                } catch (IOException e) {
                    LogHelper.e(TAG, e.getMessage());
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            LogHelper.e(TAG, e.getMessage());
                        }
                    }
                }
            }
        }
        return filterList;
    }

    /**
     * Determine the current language environment is Chinese
     *
     * @return isChinese
     */
    public static boolean isInChinese() {
        boolean isChinese = false;
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        if ("zh".equals(language)) {
            isChinese = true;
        }
        return isChinese;
    }

    /**
     * show toast by object
     *
     * @param context
     * @param object
     */
    public static void showToast(Context context, Object object) {
        if (object instanceof Integer) {
            int id = (Integer) object;
            Toast.makeText(context, context.getResources().getString(id),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, (String) object, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Determine whether the network is available
     *
     * @param context
     * @return boolean
     */
    public boolean isNetworkAvilable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
        } else {
            NetworkInfo[] networkInfos = connectivityManager
                    .getAllNetworkInfo();

            if (networkInfos != null) {
                for (int i = 0, count = networkInfos.length; i < count; i++) {
                    if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Judge a application is an application that is not a system, if it is to
     * return true, otherwise it returns false.
     *
     * @param info
     * @return
     */
    public static boolean filterApp(ApplicationInfo info) {
        // Some applications can be updated, if the user to download an
        // application system to update the original,
        // it is the system application, this is the judgment of this case
        if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {// Judging Is
            // System
            // application
            return true;
        }
        return false;
    }

    /**
     * unInstall app
     *
     * @param context
     * @param appInfo
     */
    public static void unLoad(final Context context, final ResolveInfo info) {
        final ApplicationInfo appInfo = info.activityInfo.applicationInfo;

        /*
         * new AlertDialog.Builder(context)
         * .setTitle(context.getText(R.string.please_sure))
         * .setIcon(android.R.drawable.ic_dialog_alert)
         * .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
         * { public void onClick(DialogInterface dialog, int which) { // unLoad
         * app here
         */// judge is a system application?
        if (filterApp(appInfo)) {
            showToast(context, R.string.no_del_sys_app);
        } else {
            String strUri = "package:" + appInfo.packageName;
            // Uri is used to access to uninstall the package name
            Uri uri = Uri.parse(strUri);
            Intent deleteIntent = new Intent();
            deleteIntent.setAction(Intent.ACTION_DELETE);
            deleteIntent.setData(uri);
            context.startActivity(deleteIntent);
        }
    }

    /**
     * Public warning dialog box
     *
     * @param context
     * @param title
     * @param content
     * @param drawable
     * @return
     */
    public static Builder createWarnDialog(final Context context, String title,
                                           String content, Drawable drawable) {
        Builder builder = new AlertDialog.Builder(context);

        if (drawable != null) {
            builder.setIcon(drawable);
        }

        if (!TextUtils.isEmpty(title)) {

            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(content)) {

            builder.setMessage(content);
        }
        return builder;
    }

    // Callback
    private static ClearUserDataObserver mClearUserDataObserver;

    /**
     * To determine whether the system application, kill the running program
     *
     * @param pckName
     */
    public static void ForceQuit(Context context, String pckName) {
        String pck = null;

        PackageManager packageManager = context.getPackageManager();
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningProcesses;
        runningProcesses = activityManager.getRunningAppProcesses();
        if (runningProcesses != null) {
            for (RunningAppProcessInfo rp : runningProcesses) {
                pck = rp.processName;
                try {
                    ApplicationInfo applicationInfo = packageManager.getPackageInfo(pck,
                            0).applicationInfo;
                    if (pckName.equals(pck) && filterApp(applicationInfo)) {
                        forceStopPackage(context, pck);
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * To stop an application
     *
     * @param pckName
     * @param allAppsActivity
     */
    public static void forceStopPackage(final Context context, final String
            pckName) {
        new AlertDialog.Builder(context)
                .setTitle(context.getText(R.string.force_stop_package))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // forceStopPackage here
                                ActivityManager am =
                                        (ActivityManager) context
                                                .getSystemService(Context.ACTIVITY_SERVICE);
                                am.forceStopPackage(pckName);
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }

    /**
     * Clear data
     *
     * @param context
     * @param info
     * @param handler
     */
    public static void clearData(final Context context, final ApplicationInfo
            info, final Handler handler) {
        if (info != null && info.manageSpaceActivityName != null) {
            if (!ActivityManager.isUserAMonkey()) {
                Intent intent = new Intent(Intent.ACTION_DEFAULT);
                intent.setClassName(info.packageName,
                        info.manageSpaceActivityName);
                ((Activity) context).startActivityForResult(intent, -1);
            }
        } else {
            new AlertDialog.Builder(context)
                    .setTitle(context.getText(R.string.clear_data_dlg_title))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(context.getText(R.string.clear_data_dlg_text))
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Clear user data here
                                    clearAppUserData(context, handler, info.packageName);
                                }
                            })
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
        }
    }

    // Clear APK data
    public static void clearAppUserData(final Context context, final Handler
            handler, final String pkgname) {
        if (mClearUserDataObserver == null) {

            mClearUserDataObserver = new ClearUserDataObserver(handler);
        }
        ActivityManager am = (ActivityManager)
                (((Activity) context)).getSystemService(Context.ACTIVITY_SERVICE);
        boolean res = am.clearApplicationUserData(pkgname,
                mClearUserDataObserver);
    }

    /**
     * Remove the default settings
     *
     * @param context
     * @param packageName
     */
    public static void clearDefault(final Context context, final String
            packageName) {
        new AlertDialog.Builder(context)
                .setTitle(context.getText(R.string.clear_default))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // clearDefault here
                                PackageManager packageManager = context.getPackageManager();
                                packageManager.clearPackagePreferredActivities(packageName);
                                try {
                                    IBinder b = ServiceManager.getService(Context.USB_SERVICE);
                                    IUsbManager mUsbManager = IUsbManager.Stub.asInterface(b);
                                    mUsbManager.clearDefaults(packageName, UserHandle.myUserId());
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }

    /**
     * get index of Parameters from array
     *
     * @param mode
     * @param arrays
     * @return index of Parameters
     */
    public static int getIndexFromArray(int mode, int[][] arrays) {
        int n = 0;
        LogHelper.d(TAG, "getIndexFromArray");
        for (int i = 0; i < arrays.length; i++) {
            LogHelper.d(TAG, "getIndexFromArray=" + i);
            if (arrays[i][0] == mode) {
                n = i;
                return n;
            }
        }
        return n;
    }

    /**
     * create array of parameters
     *
     * @param arrays
     * @return array of Parameters
     */
    public static int[] createArrayOfParameters(int[][] arrays) {
        int[] n = new int[arrays.length];
        LogHelper.d(TAG, "createArrayOfParameters");
        for (int i = 0; i < arrays.length; i++) {
            LogHelper.d(TAG, "createArrayOfParameters=" + i);
            n[i] = arrays[i][1];
        }
        return n;
    }

    /**
     * get value of Parameters from array
     *
     * @param mode
     * @param arrays
     * @return value of Parameters
     */
    public static int getValueFromArray(int mode, int[][] arrays) {
        int n = 0;
        LogHelper.i(TAG, "getValueFromArray");
        for (int i = 0; i < arrays.length; i++) {
            LogHelper.i(TAG, "getValueFromArray=" + i);
            if (arrays[i][0] == mode) {
                n = arrays[i][1];
                return n;
            }
        }
        return n;
    }

    public static void startActivity(Context context, String pkg, String cls, Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }
        ComponentName cn = new ComponentName(pkg, cls);
        try {
            intent.setComponent(cn);
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }
}
