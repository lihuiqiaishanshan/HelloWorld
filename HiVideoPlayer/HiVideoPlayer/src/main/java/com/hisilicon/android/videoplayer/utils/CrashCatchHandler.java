package com.hisilicon.android.videoplayer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 崩溃日志抓取
 * <p>
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告
 * <p>
 */
public class CrashCatchHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    private static final CrashCatchHandler INSTANCE = new CrashCatchHandler();// 单例模式
    private Context context;
    private UncaughtExceptionHandler defaultHandler;// 系统默认的UncaughtException处理类
    private Map<String, String> infosMap = new HashMap<String, String>(); // 用来存储设备信息和异常信息

    /**
     * 私有构造方法，保证只有一个CrashHandler实例
     */
    private CrashCatchHandler() {
    }

    /**
     * 获取CrashHandler，单例模式
     *
     * @return
     */
    public static CrashCatchHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        this.context = context;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
        Thread.setDefaultUncaughtExceptionHandler(this);// 设置当前CrashHandler为程序的默认处理器
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && defaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            defaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LogTool.e(TAG, "exception : ", e);
            }
            // 杀死进程
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return 如果处理了该异常信息, 返回true;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 使用Toast显示异常信息
        new Thread() {
            public void run() {
                Looper.prepare();
//                Toast.makeText(context, "程序出现未捕获的异常，即将退出！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();

        collectDeviceInfo(context);// 收集设备参数信息
//        saveCrashInfoToFile(ex);// 保存日志文件
        LogTool.e(CrashCatchHandler.TAG, ex.toString());
        return true;
    }

    /**
     * 收集设备信息
     *
     * @param context
     */
    public void collectDeviceInfo(Context context) {
        // 使用包管理器获取信息
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                // TODO 在这里得到包的信息
                String versionName = pi.versionName == null ? "" : pi.versionName;// 版本名;若versionName==null，则="null"；否则=versionName
                String versionCode = pi.versionCode + "";// 版本号
                infosMap.put("versionName", versionName);
                infosMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogTool.e(TAG, "an NameNotFoundException occured when collect package info");
        }

        // 使用反射获取获取系统的硬件信息
        Field[] fields = Build.class.getDeclaredFields();// 获得某个类的所有申明的字段，即包括public、private和proteced，
        for (Field field : fields) {
            field.setAccessible(true);// 暴力反射 ,获取私有的信息;类中的成员变量为private,故必须进行此操作
            try {
                infosMap.put(field.getName(), field.get(null).toString());
                LogTool.d(TAG, field.getName() + " : " + field.get(null));
            } catch (IllegalArgumentException e) {
                LogTool.e(TAG, "an IllegalArgumentException occured when collect reflect field info", e);
            } catch (IllegalAccessException e) {
                LogTool.e(TAG, "an IllegalAccessException occured when collect reflect field info", e);
            }
        }
    }

}