package com.hisilicon.tvui.hal;

//FOR DPT 7.0
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

import android.os.PowerManager;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.Display;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.storage.IStorageManager;
import android.os.ServiceManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.util.Log;
import android.hardware.display.DisplayManager;
import android.graphics.Rect;

import com.hisilicon.android.tv.TvListenerManager;
//import com.hisilicon.android.HiSysManager;
import com.hisilicon.android.tv.TvSourceManager;
import com.hisilicon.android.tv.listener.OnTvEventListener;
import com.hisilicon.android.tvapi.Audio;
import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.constant.EnumCecMenu;
import com.hisilicon.android.tvapi.constant.EnumCecUICommand;
import com.hisilicon.android.tvapi.constant.EnumSoundAdvEftParam;
import com.hisilicon.android.tvapi.impl.HdmiCECImpl;
import com.hisilicon.android.tvapi.vo.HdmiCecDeviceInfo;
import com.hisilicon.android.tvapi.vo.RectInfo;
import com.hisilicon.android.tvapi.vo.TimingInfo;
import com.hisilicon.android.tvapi.vo.AudioStreamInfo;
import com.hisilicon.android.tvapi.listener.TVMessage;
import com.hisilicon.android.tvapi.listener.ITVListener;

import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.R;


public class halApi
{
    private static boolean isStop = false;
    private static boolean requestMenu = true;

    private static HalTimingInfo mHalTimingInfo = new halApi().new HalTimingInfo();

    public class EnumSourceIndex
    {
        public static final int SOURCE_ATV    = 0;
        public static final int SOURCE_DVBC   = 1;
        public static final int SOURCE_DTMB   = 2;
        public static final int SOURCE_CVBS1  = 3;
        public static final int SOURCE_CVBS2  = 4;
        public static final int SOURCE_CVBS3  = 5;
        public static final int SOURCE_VGA    = 6;
        public static final int SOURCE_YPBPR1 = 7;
        public static final int SOURCE_YPBPR2 = 8;
        public static final int SOURCE_HDMI1  = 9;
        public static final int SOURCE_HDMI2  = 10;
        public static final int SOURCE_HDMI3  = 11;
        public static final int SOURCE_HDMI4  = 12;
        public static final int SOURCE_MEDIA  = 13;
        public static final int SOURCE_MEDIA2 = 14;
        public static final int SOURCE_SCART1 = 15;
        public static final int SOURCE_SCART2 = 16;
        public static final int SOURCE_AUTO   = 17;
        public static final int SOURCE_DVBT   = 18;
        public static final int SOURCE_ATSC   = 19;
        public static final int SOURCE_DVBS   = 20;
        public static final int SOURCE_ISDBT  = 21;
        public static final int SOURCE_BUTT  = 22;
    }

    public class EnumAtvClrsys
    {
        public static final int CLRSYS_AUTO     = 0;
        public static final int CLRSYS_PAL      = 1;
        public static final int CLRSYS_NTSC     = 2;
        public static final int CLRSYS_SECAM    = 3;
        public static final int CLRSYS_PAL_M    = 4;
        public static final int CLRSYS_PAL_N    = 5;
        public static final int CLRSYS_PAL_60   = 6;
        public static final int CLRSYS_NTSC443  = 7;
        public static final int CLRSYS_PAL_NC   = 8;
    }

    public class EnumAtvAudsys
    {
        public static final int AUDSYS_BG       = 0;
        public static final int AUDSYS_BG_A2    = 1;
        public static final int AUDSYS_BG_NICAM = 2;
        public static final int AUDSYS_I        = 3;
        public static final int AUDSYS_DK       = 4;
        public static final int AUDSYS_DK1_A2   = 5;
        public static final int AUDSYS_DK2_A2   = 6;
        public static final int AUDSYS_DK3_A2   = 7;
        public static final int AUDSYS_DK_NICAM = 8;
        public static final int AUDSYS_L        = 9;
        public static final int AUDSYS_M        = 10;
        public static final int AUDSYS_M_BTSC   = 11;
        public static final int AUDSYS_M_A2     = 12;
        public static final int AUDSYS_M_EIA_J  = 13;
        public static final int AUDSYS_NOTSTANDARD = 14;
        public static final int AUDSYS_LL       = 15;// sdk not define,just for apks compile
    }

    public class EnumSignalStat
    {
        public static final int SIGSTAT_SUPPORT     = 0;
        public static final int SIGSTAT_NOSIGNAL    = 1;
        public static final int SIGSTAT_UNSUPPORT   = 2;
        public static final int SIGSTAT_UNSTABLE    = 3;
    }

    public class EnumColorSystem
    {
        public static final int CLRSYS_AUTO     = 0;
        public static final int CLRSYS_PAL      = 1;
        public static final int CLRSYS_NTSC     = 2;
        public static final int CLRSYS_SECAM    = 3;
        public static final int CLRSYS_PAL_M    = 4;
        public static final int CLRSYS_PAL_N    = 5;
        public static final int CLRSYS_PAL_60   = 6;
        public static final int CLRSYS_NTSC443  = 7;
        public static final int CLRSYS_NTSC_50  = 8;
    }

    public class EnumLockSwitch
    {
        public static final int TOTAL_LOCK      = 0;
        public static final int SOURCE_LOCK     = 1;
        public static final int PROGRAM_LOCK    = 2;
        public static final int PARENTAL_LOCK   = 3;
    }

    public class EnumLockType
    {
        public static final int SOURCE_LOCK_TYPE    = 0;
        public static final int PROGRAM_LOCK_TYPE   = 1;
        public static final int PARENTAL_LOCK_TYPE  = 2;
    }

    public class EnumDevType
    {
        public static final int DPT             = 0;
        public static final int STB             = 1;
    }

    public class EnumPipPosition
    {
        public static final int LEFT_TOP     = 0;
        public static final int LEFT_BOTTOM  = 1;
        public static final int RIGHT_TOP    = 2;
        public static final int RIGHT_BOTTOM = 3;
    }

    public class HalTimingInfo extends TimingInfo
    {
        public static final int HI_MW_HDMI_FORMAT_HDMI = 0;
        public static final int HI_MW_HDMI_FORMAT_DVI  = 1;
        public static final int HI_MW_HDMI_FORMAT_MHL  = 2;
        public static final int HI_MW_SIGNAL_BUTT = 3;
    }

    public interface HalOnTvEventListener extends OnTvEventListener
    {

    }

    public interface HalITVListener extends ITVListener
    {

    }

    public static int[][] DTS_STREAM_TYPE_ARRAY =
    {
        {AudioStreamInfo.VALUE_DTS_DTS,    R.string.dts_dts},
        {AudioStreamInfo.VALUE_DTS_DTSEX,  R.string.dts_dtsex},
        {AudioStreamInfo.VALUE_DTS_DTSHD,  R.string.dts_dtshd},
        {AudioStreamInfo.VALUE_DTS_UNKNOW, R.string.dts_unknow}
    };
    public static int getDeviceType()
    {
        return EnumDevType.DPT;
    }

    public static Rect getDisplayRect(Context context)
    {
        Rect retRect = null;
        if (null != context)
        {
            DisplayManager displayManager = new DisplayManager(context);
            Display display = displayManager.getDisplay(0);
            retRect = new Rect();
            if (null != display)
            {
                display.getRectSize(retRect);
            }
        }
        return retRect;
    }

    public static boolean isDTVSource(int curSourceID)
    {
        return (curSourceID == EnumSourceIndex.SOURCE_DTMB
                || curSourceID == EnumSourceIndex.SOURCE_DVBC
                || curSourceID == EnumSourceIndex.SOURCE_DVBT
                || curSourceID == EnumSourceIndex.SOURCE_ATSC
                || curSourceID == EnumSourceIndex.SOURCE_DVBS
                || curSourceID == EnumSourceIndex.SOURCE_ISDBT);
    }

    public static boolean isATSCSource(int curSourceID) {
        return curSourceID == EnumSourceIndex.SOURCE_ATSC;
    }
    public static void goToSleep(PowerManager pm){
        pm.goToSleep(SystemClock.uptimeMillis(),
                PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON,
                PowerManager.GO_TO_SLEEP_FLAG_NO_DOZE);
    }
    public static boolean isATVSource(int curSourceID)
    {
        return (curSourceID == EnumSourceIndex.SOURCE_ATV);
    }

    public static boolean isATVSource()
    {
        return isATVSource(getCurSourceID());
    }

    public static boolean isHDMISource(int curSourceID)
    {
        return curSourceID == EnumSourceIndex.SOURCE_HDMI1
            || curSourceID == EnumSourceIndex.SOURCE_HDMI2
            || curSourceID == EnumSourceIndex.SOURCE_HDMI3
            || curSourceID == EnumSourceIndex.SOURCE_HDMI4;
    }

    public static boolean isTVSource()
    {
        return isTVSource(getCurSourceID());
    }

    public static boolean isTVSource(int curSourceID)
    {
        return (curSourceID == EnumSourceIndex.SOURCE_ATV
                || curSourceID == EnumSourceIndex.SOURCE_DTMB
                || curSourceID == EnumSourceIndex.SOURCE_DVBC
                || curSourceID == EnumSourceIndex.SOURCE_DVBT
                || curSourceID == EnumSourceIndex.SOURCE_ATSC
                || curSourceID == EnumSourceIndex.SOURCE_DVBS
                || curSourceID == EnumSourceIndex.SOURCE_ISDBT);
    }

    public static void changeSource(int mCurrentSourceIdx ,int mDestSourceIdx)
    {
        TvSourceManager.getInstance().selectSource(mDestSourceIdx, 0);
    }

    public static void changeSource(int mCurrentSourceIdx ,int mDestSourceIdx, int atvChannelId)
    {
        TvSourceManager.getInstance().selectSource(mDestSourceIdx, 0, atvChannelId);
    }

    static public void setSourceHolder()
    {
        TvSourceManager.getInstance().setSourceHolder("HiTvPlayer");
    }

    public static void setVideoDisplay(SurfaceHolder holder)
    {
        HitvManager.getInstance().getSourceManager().setVideoDisplay(holder);
    }

    public static void clearVideoDisplay(SurfaceHolder holder)
    {
        HitvManager.getInstance().getSourceManager().clearVideoDisplay(holder);
    }

    public static int getPreSourceID()
    {
        return TvSourceManager.getInstance().getPreSourceId();
    }

    public static int getCurSourceID()
    {
        return TvSourceManager.getInstance().getCurSourceId(0);
    }

    public static int getSelectSourceID()
    {
        return TvSourceManager.getInstance().getSelectSourceId();
    }

    // get current tvSystem
    public static int getTvSystemType()
    {
        return HitvManager.getInstance().getSystemSetting().getTvSystem();
    }

    public static void setFullVideo()
    {
        TvSourceManager.getInstance().setFullWindow(true);
    }

    public static boolean isFullWindow(){
        return TvSourceManager.getInstance().isFullWindow();
    }

    private static boolean haveCECDevice()
    {
        ArrayList<HdmiCecDeviceInfo> arr = HitvManager.getInstance().getHdmiCEC().getDeviceList();
        int curId = TvSourceManager.getInstance().getSelectSourceId();
        for (int i = 0; i < arr.size(); i++)
        {
            if (arr.get(i).getHdmiDevPort() == curId)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean setUICommand(int keyCode)
    {
        int value = -1;
        boolean isCECEnable = HdmiCECImpl.getInstance().getUIStatus(EnumCecMenu.CEC_MENU_ENABLE);
        boolean isCECDevMenuCtrlEnable =  HdmiCECImpl.getInstance().getUIStatus( EnumCecMenu.CEC_MENU_DEVMENUCTRL);
        if (isCECEnable && isCECDevMenuCtrlEnable&& haveCECDevice())
        {
            switch (keyCode)
            {
            case KeyValue.DTV_KEYVALUE_DPAD_UP:
                value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_UP);
                break;
            case KeyValue.DTV_KEYVALUE_DPAD_DOWN:
                value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_DOWN);
                break;
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
                value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_LEFT);
                break;
            case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
                value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_RIGHT);
                break;
            case KeyValue.DTV_KEYVALUE_MEDIA_PLAY_PAUSE:
                if (isStop)
                {
                    value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_PLAY);
                    isStop = false;
                }
                else
                {
                    value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_PAUSE);
                    isStop = true;
                }
                break;
            case KeyValue.DTV_KEYVALUE_DPAD_CENTER:
                value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_SELECT);
                break;
            case KeyValue.DTV_KEYVALUE_MEDIA_REWIND:
                value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_REWIND);
                break;
            case KeyValue.DTV_KEYVALUE_MEDIA_FAST_FORWARD:
                value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_FASTFORWARD);
                break;
            case KeyValue.DTV_KEYVALUE_MEDIA_STOP:
                value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_STOP);
                break;
            case KeyValue.DTV_KEYVALUE_MENU:
                requestMenu = !requestMenu;
                value = HdmiCECImpl.getInstance().requestMenuStat(-1, requestMenu);
                break;
            //mapping KeyValue.DTV_KEYVALUE_BACK to EnumCecUICommand.CEC_MENU_EXIT of CEC device
            case KeyValue.DTV_KEYVALUE_BACK:
                value = HdmiCECImpl.getInstance().setUICommand(EnumCecUICommand.CEC_MENU_EXIT);
                break;
            default:
                break;
            }
        }
        if (value != -1)
        {
            return true;
        }
        return false;
    }

    public static HalTimingInfo getTimingInfo()
    {
        TimingInfo ti = HitvManager.getInstance().getSourceManager().getTimingInfo();
        mHalTimingInfo.setiWidth(ti.getiWidth());
        mHalTimingInfo.setiHeight(ti.getiHeight());
        mHalTimingInfo.setiFrame(ti.getiFrame());
        mHalTimingInfo.setbInterlace(ti.isbInterlace());
        mHalTimingInfo.setiHDMIFmt(ti.getiHDMIFmt());
        mHalTimingInfo.setI3dFmt(ti.getI3dFmt());
        mHalTimingInfo.setiColorSpace(ti.getiColorSpace());
        mHalTimingInfo.setbMHL(ti.isbMHL());
        return mHalTimingInfo;
    }

    public static int getColorSystem()
    {
        return HitvManager.getInstance().getPicture().getRealColorSystem();
    }

    public static int getSignalStatus()
    {
        return TvSourceManager.getInstance().getSignalStatus();
    }

    private static TvListenerManager getTvListenerManager()
    {
        return TvListenerManager.getInstance();
    }

    public static void registerOnTvEventListener(HalOnTvEventListener tvEventListener)
    {
        getTvListenerManager().registerOnTvEventListener(tvEventListener);
    }

    public static void unregisterOnTvEventListener(HalOnTvEventListener tvEventListener)
    {
        getTvListenerManager().unregisterOnTvEventListener(tvEventListener);
    }

    public static int getSuspendMode()
    {
       return HitvManager.getInstance().getFactory().getSuspendMode();
    }

    public static void system_upgrade(String path)
    {
//        HiSysManager hisys = new HiSysManager();
//        hisys.upgrade(path);
    }

    //get USB device List
    public static ArrayList<String> getUsbDeviceList()
    {
        ArrayList<String> usbDeviceList = new ArrayList<String>();
        try
        {
            // support for DevType
            IBinder service = ServiceManager.getService("mount");
            if (null != service)
            {
                IStorageManager mountService = IStorageManager.Stub.asInterface(service);
                VolumeInfo[] list = mountService.getVolumes(0);
                List<VolumeInfo> mountList = new ArrayList<VolumeInfo>();
                for(int i = 0; i < list.length ; i++)
                {
                   if(2 == list[i].state)  //means only add deivces which state equals to mounted
                   {
                       mountList.add(list[i]);
                   }
                }
				
				//temporary to shield to solve compile issue
                //VolumeInfo sdcard = new VolumeInfo("sdcard", 0, null, null, "SDCARD");
                //sdcard.internalPath = "/mnt/sdcard";
                //sdcard.state = VolumeInfo.STATE_MOUNTED;
                //mountList.add(sdcard);

                int index = mountList.size();

                for (int i = 0; i < index; i++)
                {
                    String path = mountList.get(i).internalPath;
                    if (null == path)
                    {
                       continue;
                    }
                    usbDeviceList.add(path);
                }
            }
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        return usbDeviceList;
    }

    //get Source List
    public static ArrayList<Integer> getSourceList()
    {
        return HitvManager.getInstance().getSourceManager().getSourceList();
    }


    //All Lock Status
    public static ArrayList<Integer> isCurrentLocked()
    {
        return HitvManager.getInstance().getSystemSetting().isCurrentLocked();
    }

    public static int setLockEnable(int lockSwitchType, boolean bLock)
    {
        return HitvManager.getInstance().getSystemSetting().setLockEnable(lockSwitchType, bLock);
    }

    public static boolean getLockEnable(int lockSwitchType)
    {
        return HitvManager.getInstance().getSystemSetting().getLockEnable(lockSwitchType);
    }

    //All Pwd Status
    public static int setPwdStatus(int lockType, boolean bLock)
    {
        return HitvManager.getInstance().getSystemSetting().setPwdStatus(lockType, bLock);
    }

    public static boolean getPwdStatus(int lockType)
    {
        return HitvManager.getInstance().getSystemSetting().getPwdStatus(lockType);
    }

    //Source Lock
    public static int setSrcLockEnable(int source, boolean bLock)
    {
        return HitvManager.getInstance().getSystemSetting().setSrcLockEnable(source, bLock);
    }

    public static boolean getSrcLockEnable(int source)
    {
        return HitvManager.getInstance().getSystemSetting().getSrcLockEnable(source);
    }

    //Key Lock
    public static int enableLSADCKey(boolean onoff)
    {
        return HitvManager.getInstance().getSystemSetting().enableLSADCKey(onoff);
    }

    public static boolean isLSADCKeyEnable()
    {
        return HitvManager.getInstance().getSystemSetting().isLSADCKeyEnable();
    }

    //CC Vchip
    public static int setTTXSurface(SurfaceHolder holder)
    {
        return HitvManager.getInstance().getSourceManager().setTTXSurface(holder);
    }

    public static int setCcEnable(int mode)
    {
        return HitvManager.getInstance().getSourceManager().setCcEnable(mode);
    }


    public static int setPvrLight(boolean bLightOn) {
        return HitvManager.getInstance().getSystemSetting().setPVRLedEnable(bLightOn);
    }

    public static int getCcEnable()
    {
        return HitvManager.getInstance().getSourceManager().getCcEnable();
    }

    public static ArrayList<Integer> getCcVchipInfo()
    {
        return HitvManager.getInstance().getSourceManager().getCcVchipInfo();
    }

    public static int showCc(Boolean show)
    {
        int value = 0;
        if(show)
        {
            value = 1;
        }
        else
        {
            value = 0;
        }
        return HitvManager.getInstance().getSourceManager().showCc(value);
    }

    public static boolean isCcVisible()
    {
        return HitvManager.getInstance().getSourceManager().isCcVisible();
    }

    public static int setCcChannel(int channel)
    {
        return HitvManager.getInstance().getSourceManager().setCcChannel(channel);
    }

    public static int getCcChannel()
    {
        return HitvManager.getInstance().getSourceManager().getCcChannel();
    }

    public static int setNoSignalSuspend(int source)
    {
        return HitvManager.getInstance().getSystemSetting().setNoSignalSuspend(source);
    }

    public static boolean getSrcWakeupEnable(int source)
    {
        return HitvManager.getInstance().getSystemSetting().getSrcWakeupEnable(source);
    }

    public static boolean isGraphicsMode()
    {
        return HitvManager.getInstance().getPicture().isGraphicsMode();
    }

    //no signal blue screen
    public static void setBlueScreen()
    {
        boolean isBlueScreen = HitvManager.getInstance().getSystemSetting().isScreenBlueEnable();
        HitvManager.getInstance().getSystemSetting().enableScreenBlue(isBlueScreen);
    }

    private static Audio getAudioManager() {
        return HitvManager.getInstance().getAudio();
    }

    public static AudioStreamInfo getAudioStreamInfo(){
        return getAudioManager().getAudioStreamInfo();
    }

    public static boolean isDolbyAtmosEnable(){
        int dolbyAtmos = getAudioManager().getAdvancedEffectParameter(
                EnumSoundAdvEftParam.E_DAP_DOLBY_ATMOS_ONOFF);
        int DAPEnable = getAudioManager().getAdvancedEffectParameter(
                EnumSoundAdvEftParam.E_DAP_ONOFF);
        if (dolbyAtmos == 1 && DAPEnable == 1){
            return true;
        } else {
            return false;
        }
    }

    public static int setDtvLockEnable(boolean enable) {
        int ret =  HitvManager.getInstance().getSystemSetting().setDtvLockEnable(enable);
        return ret;
    }

    public static boolean getDTVLockEnable() {
        boolean ret =  HitvManager.getInstance().getSystemSetting().getDTVLockEnable();
        return ret;
    }
}
