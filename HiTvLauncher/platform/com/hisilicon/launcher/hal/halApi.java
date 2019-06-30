package com.hisilicon.launcher.hal;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.LocaleList;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.storage.IStorageManager;
import android.os.IBinder;
import android.os.ServiceManager;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;

import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.android.tv.TvSourceManager;
import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.launcher.util.LogHelper;
import android.os.storage.VolumeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class halApi {

    private final static String TAG = "halApi";

    private static IStorageManager mStorageManager;

    public static void setLanguage(int index) {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            LocaleList localList = config.getLocales();
            int count = localList.size();
            int fromPosition = 0;
            boolean isHasLanguage = false;
            List<Locale> temp = new ArrayList<Locale>();
            Locale locale = index == 1 ? Locale.US : Locale.SIMPLIFIED_CHINESE;
            for(int i = 0; i < count; i++) {
                temp.add(localList.get(i));
                if(localList.get(i).equals(locale)) {
                    isHasLanguage = true;
                    fromPosition = i;
                }
            }

            if(isHasLanguage) {
                temp.remove(fromPosition);
            }
            temp.add(0, locale);
            config.setLocales(new LocaleList(temp.toArray(new Locale[temp.size()])));
            SystemProperties.set("persist.sys.local", config.locale.toLanguageTag());
            am.updatePersistentConfiguration(config);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public static void setSystemMute(Context context, boolean mute) {
        LogHelper.d(TAG, "setSystemMute " + mute);
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int flags = AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND
                    | AudioManager.FLAG_FROM_KEY;
            if (mute) {
                audioManager.adjustVolume (AudioManager.ADJUST_MUTE, flags);
            } else {
                audioManager.adjustVolume (AudioManager.ADJUST_UNMUTE, flags);
            }
        }
    }

    public static void enablePowerOnPanel(boolean enable) {
        LogHelper.d(TAG, "enablePowerOnPanel " + enable);
        HitvManager.getInstance().getSystemSetting().enablePowerOnPanel(enable);
    }

    public static void goToSleep(PowerManager pm){
        pm.goToSleep(SystemClock.uptimeMillis(),
                PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON,
                PowerManager.GO_TO_SLEEP_FLAG_NO_DOZE);
    }
    public static Map<String, ArrayList<String>> getUuidList(){
        IStorageManager service = getStorageManager();
        Map<String, ArrayList<String>> map = new HashMap<>();
        if (service == null) {
            return map;
        }
        try{
            VolumeInfo[] list = service.getVolumes(0);
            List<VolumeInfo> mountList = new ArrayList<VolumeInfo>();
            for(int i = 0; i < list.length ; i++) {
                if (2 == list[i].state) { //means only add deivces which state equals to mounted
                    mountList.add(list[i]);
                }
            }
            for (int i = 0; i < mountList.size(); i++) {
                ArrayList<String> uuidList = new ArrayList<String>();
                VolumeInfo volumeInfo = mountList.get(i);
                String uuid = volumeInfo.fsUuid;
                String type = volumeInfo.fsType;
                String internalPath = volumeInfo.internalPath;
                LogHelper.d(TAG, "uuid =" + uuid + "  type ="+ type);
                if (null == uuid ||null == type) {
                    continue;
                }
                LogHelper.d(TAG, " internalPath ="+internalPath);

                uuidList.add(uuid);
                uuidList.add(type);
                map.put(internalPath,uuidList);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static boolean isDTVSource(int curSourceID) {
        return (curSourceID == EnumSourceIndex.SOURCE_DTMB
                || curSourceID == EnumSourceIndex.SOURCE_DVBC
                || curSourceID == EnumSourceIndex.SOURCE_DVBT
                || curSourceID == EnumSourceIndex.SOURCE_ATSC
                || curSourceID == EnumSourceIndex.SOURCE_DVBS
                || curSourceID == EnumSourceIndex.SOURCE_ISDBT);
    }

    public static IStorageManager getStorageManager() {
        if(mStorageManager == null) {
            IBinder service = ServiceManager.getService("mount");
            if(service != null) {
                mStorageManager = IStorageManager.Stub.asInterface(service);
            }
        }
        return mStorageManager;
    }

    public static int getSelectSourceID() {
        return TvSourceManager.getInstance().getSelectSourceId();
    }
}
