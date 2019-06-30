package com.hisilicon.tvui.play;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.android.tvapi.impl.SourceManagerImpl;
import com.hisilicon.dtv.hardware.Tuner;
import com.hisilicon.tvui.interfaces.SystemSettingInterface;
import com.hisilicon.tvui.pvr.RecordPlayStatus;
import com.hisilicon.tvui.util.ParentalControlUtil;
import com.hisilicon.android.tvapi.constant.EnumSystemTvSystem;
import com.hisilicon.android.tvapi.impl.SystemSettingImpl;
import com.hisilicon.android.tvapi.vo.AudioStreamInfo;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelFilter;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.EnScrambleFilter;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.epg.EPGEvent;
import com.hisilicon.dtv.hardware.EnModulation;
import com.hisilicon.dtv.hardware.EnTunerStatus;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.DVBTChannelDot;
import com.hisilicon.dtv.network.DVBTNetwork;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.dtv.network.service.AudioComponent;
import com.hisilicon.dtv.network.service.EnServiceType;
import com.hisilicon.dtv.network.service.EnStreamType;
import com.hisilicon.dtv.network.service.TeletextComponent;
import com.hisilicon.dtv.play.EnPlayStatus;
import com.hisilicon.dtv.play.EnStopType;
import com.hisilicon.dtv.play.TeletextControl;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.channelmanager.ChannelManagerActivity;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.play.audio.AudioSelectorDialog;
import com.hisilicon.tvui.play.cc.ATVClosedCaptionDialog;
import com.hisilicon.tvui.play.cc.ClosedCaptionDialog;
import com.hisilicon.tvui.play.subtitle.SubtitleDialog;
import com.hisilicon.tvui.play.subtitle.SubtitleSelectorDialog;
import com.hisilicon.tvui.play.teletext.ATVTeletextDialog;
import com.hisilicon.tvui.play.teletext.TeletextDialog;
import com.hisilicon.tvui.service.DTVService;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.util.UsbManagerPrivate;
import com.hisilicon.tvui.util.PlayerProviderObserver;
import com.hisilicon.tvui.util.Util;
import com.hisilicon.tvui.view.ConfirmDialog;
import com.hisilicon.tvui.view.ConfirmDialog.OnConfirmDialogListener;
import com.hisilicon.tvui.view.FavoriteSelectDialog;
import com.hisilicon.tvui.view.MyToast;
import com.hisilicon.tvui.view.SignalShow;
import com.hisilicon.tvui.view.VGAAdjustingDialog;
import com.hisilicon.dtv.pc.ParentalControlManager;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final String DEFAULT_LAN = "eng";
    private static final int MSG_COUNT_TIME_DEC = 101;
    private static final int MSG_SHOW_COUNT_TIME_DIALOG = 102;
    private static final int MSG_SHOW_NOSIGNAL_DIALOG = 103;
    public static final int MSG_AUTOSLEEP_SETTINGS_CHANGE = 104;
    private static final int MSG_DOLBY_ONOFF = 105;

    private static final int OTA_AREA = 3;
    private static final int PVR_TIMESHIFT_SUP_ONOROFF_TIME = 120;
    private static final int MSG_CLOSE_EAS = 110;
    private static final int SERVICE_TYPEMODE_ALL = 0;
    private static final int SERVICE_TYPEMODE_TV = 1;
    private static final int SERVICE_TYPEMODE_RADIO = 2;
    private static final int SERVICE_TYPEMODE_DATA = 3;
    private static final int SERVICE_TYPEMODE_BUTT = 4;
    private static final long DURATION_BETWEEN_TWO_ONKEYDOWN_EVENT = 800;
    private static final String GINGA_MODE = "bGingaMode";
    private static final String CURRENT_SORT_TYPE = "current_sort_type";
    private boolean isInitTvPlayFinish = true;
    private long hotkeyChangeSourceStartTime = 0L;
    private boolean isBookArriving = false;
    private boolean dolbyFlag = false;
    /**
     * a map that mapping bookType to SourceId,
     * if currentSourceId doesn't matches bookType, change sourceId
     */
    private static HashMap<EnNetworkType, Integer> mapBookType2SrcId = new HashMap<EnNetworkType, Integer>();

    static {
        mapBookType2SrcId.put(EnNetworkType.CABLE, halApi.EnumSourceIndex.SOURCE_DVBC);
        mapBookType2SrcId.put(EnNetworkType.TERRESTRIAL, halApi.EnumSourceIndex.SOURCE_DVBT);
        mapBookType2SrcId.put(EnNetworkType.DTMB, halApi.EnumSourceIndex.SOURCE_DTMB);
        mapBookType2SrcId.put(EnNetworkType.ISDB_TER, halApi.EnumSourceIndex.SOURCE_ISDBT);
        mapBookType2SrcId.put(EnNetworkType.SATELLITE, halApi.EnumSourceIndex.SOURCE_DVBS);
    }

    private ChannelInfoBarView mChnInfoView = null;
    private DetailInfoView mDetailInfoView = null;
    private ChannelListView mChannelListView = null;
    private SignalShow mSignalShow = null;
    private TipMsgView mTipMsgView = null;
    private PvrRecordView mPvrRecordView = null;
    private TimeshiftView mTimeshiftView = null;
    private DigitalKeyView mDigitalKeyView = null;
    private PipView mPipView = null;
    private ChannelFindView mChannelFindView = null;
    private SurfaceView mSubTeletextSurfaceView = null;
    private SurfaceView mAtvTeletextSurfaceView = null;
    private SurfaceHolder mSubSurfaceHolder = null;
    private SurfaceHolder mAtvSurfaceHolder = null;
    private SurfaceView mPlaySurfaceView = null;
    private SurfaceHolder mPlaySurfaceHolder = null;
    private LinearLayout mRadioBGLinearLayout = null;
    private BookAlarmReceiver mBookAlarmReceiver = null;
    private BookArriveReceiver mBookArriveReceiver = null;
    private EwsPlayReceiver mEwsPlayReceiver = null;
    private ViewShowReceiver mViewShowReceiver = null;
    private PlayDTVReceiver mPlayDTVReceiver = null;
    private FavoriteSelectDialog mAddToFavoriteDialog = null;
    private ConfirmDialog mPuConfirmDialog = null;
    private ConfirmDialog mUsbConfirmDialog = null;
    private ProgressDialog mRescanProgressDialog = null;
    private Context mContext = null;

    private ConfirmDialog mSsuConfirmDialog = null;
    Network mCurNetwork = null;
    Multiplex mCurMultiplex = null;
    VGAAdjustingDialog mVGAAdjustingDialog = null;
    private ATVTeletextDialog mTeletextDialog = null;

    private int autoSleepTime = 0;
    private TextView mCountTimeText;
    private AlertDialog mCountTimeDialog;
    private int mCountTime = 0;
    private String mStringCountTime;
    private PlayerProviderObserver mAutoSleepSettingProviderObserver;
    private PlayerProviderObserver mDolbyProviderObserver;
    private boolean mGotoFullPlayRecordFile = false;
    private boolean isPause = false;
    private int mDestSourceId = -1;
    private int mPreSourceId = -1;
    public int mCurSourceId = -1;
    private int fromCEC = 0;
    private int fromTimeWake = 0;
    private SubtitleSelectorDialog mSubtitleDialog = null;
    private AudioSelectorDialog mAudioDialog = null;
    private TextView mDRA = null;
    private TextView mDRAChannel = null;
    private TextView mDTSStreamType = null;
    private ImageView mDolbyView = null;
    private boolean isDTVScanning = false;
    private boolean isCurMultiplexScan = false;
    private boolean mHasATVSetDisplay = false;
    private boolean isPIPSurfaceCreated = false;
    private boolean isWindowKey = false;
    private boolean isInPipMode = false;
    private EnTVRadioFilter menTVRadioFilter = EnTVRadioFilter.ALL;
    private int fromStandbyWake = 0;
    private String bookTaskValue = "none";
    private boolean isStandbyScanEpg = false;
    private boolean isSdandbyScanChannel = false;
    //EAS
    private boolean isInEASMode = false;
    private List<Channel> mixSortChannelList;
    private List<Integer> sourceList;
    private boolean isEASChangedChannel;
    private TextView mEASTextView = null;
    private TeletextDialog tmpTeletextDialog;
    private SurfaceView gingaSurfaceView;
    private SurfaceHolder gingaSurfaceHolder;
    private static MainActivity mainActivity;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COUNT_TIME_DEC:
                    if (mCountTime > 0) {
                        mCountTimeText.setText(mCountTime + mStringCountTime);
                        mHandler.sendEmptyMessageDelayed(MSG_COUNT_TIME_DEC, 1000);
                        mCountTime--;
                    } else {
                        if (mCountTimeDialog != null && mCountTimeDialog.isShowing()) {
                            mCountTimeDialog.dismiss();
                        }
                        goToShutdown();
                    }
                    break;
                case MSG_SHOW_COUNT_TIME_DIALOG:
                    showCountTimeDialog();
                    break;
                case MSG_SHOW_NOSIGNAL_DIALOG:
                    if (!mTipMsgView.isShow(TipMsgView.TIPMSG_SOURCE_LOCK)) {
                        if (halApi.getSignalStatus() == halApi.EnumSignalStat.SIGSTAT_UNSUPPORT) {
                            LogTool.i(LogTool.MPLAY, "signal: " + mCurSourceId + " signal unsupport");
                            setSignalUnSupportFlag(true);
                        }
                        if (halApi.getSignalStatus() == halApi.EnumSignalStat.SIGSTAT_NOSIGNAL
                                || (halApi.isTVSource(mCurSourceId)
                                && mChnHistory.getCurrentChn(mCurSourceId) == null)) {
                            LogTool.i(LogTool.MPLAY, "signal: " + mCurSourceId + " no signal");
                            showNoSignalStatus(halApi.EnumSignalStat.SIGSTAT_NOSIGNAL);
                            setSignalFlag(false);
                        }
                        if (halApi.isDTVSource(mCurSourceId)) {
                            if (null != mPlayer && null != mPlayer.getTuner()) {
                                // if is fast terminal,need access TunerStatus to get signal status
                                if (mPlayer.getTuner().getTunerStatus() == EnTunerStatus.LOCK) {
                                    LogTool.i(LogTool.MPLAY, "getTuner signal: " + mCurSourceId + " have signal");
                                    setSignalFlag(true);
                                } else {
                                    LogTool.i(LogTool.MPLAY, "getTuner signal: " + mCurSourceId + " no signal");
                                    setSignalFlag(false);
                                }
                            }
                        }
                    } else {
                        LogTool.i(LogTool.MPLAY, "source lock is show");
                    }
                    break;
                case MSG_AUTOSLEEP_SETTINGS_CHANGE:
                    autoSleepTime = getAutoSleepTime();
                    int status = halApi.getSignalStatus();
                    showNoSignalStatus(status);
                    if (status != halApi.EnumSignalStat.SIGSTAT_NOSIGNAL && mTipMsgView.isShow(TipMsgView.TIPMSG_NOSIGNAL)) {
                        LogTool.i(LogTool.MPLAY, "signal: " + mCurSourceId + " have signal");
                        setSignalFlag(true);
                    }
                    break;
                case MSG_DOLBY_ONOFF:
                    int dolbyOnOff = Settings.System.getInt(mContext.getContentResolver(), Util.SETTING_DOLBY_ONOFF, 1);
                    LogTool.i(LogTool.MPLAY, "MSG_DOLBY_ONOFF dolbyOnOff: " + dolbyOnOff + " dolbyFlag = " + dolbyFlag);
                    if (dolbyOnOff == 0) {
                        setDolbyTagVisibility(View.GONE);
                    } else if (dolbyOnOff == 1 && dolbyFlag) {
                        mDolbyView.setImageResource(R.drawable.dolby_audio);
                        setDolbyTagVisibility(View.VISIBLE);
                    }
                    break;
                case MSG_CLOSE_EAS:
                    closeEAS();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle arg0) {
        LogTool.i(LogTool.MPLAY, "===== OnCreate =====");
        super.onCreate(arg0);
        ((DTVApplication) this.getApplication()).setMainActivity(this);
        ((DTVApplication) getApplication()).setEnabledStop(false);
        mContext = this;
        setContentView(R.layout.main_view);
        initView();
        startService();
        subBroadcast();
        getDestSourceIdx(this.getIntent());
    }

    @Override
    protected void onStart() {
        LogTool.i(LogTool.MPLAY, "===== onStart =====");
        super.onStart();

        subDtvNormalScribeEvent();
        //subDtvScribeEvent();
        subAtvScribeEvent();
        halApi.registerOnTvEventListener(tvEventListener);
    }

    public void openGinga(boolean isOpen) {
        if (null != ginga && mCurSourceId == EnumSourceIndex.SOURCE_ISDBT) {
            if (isOpen) {
                ginga.init();
                LogTool.i(LogTool.MPLAY, " open Ginga ");
            } else {
                ginga.deinit();
                LogTool.i(LogTool.MPLAY, " close Ginga ");
            }
        }
    }

    public List<String> getAppList() {
        int gingaMode = mDtvConfig.getInt(GINGA_MODE, 0);
        if (1 == gingaMode && null != ginga && mCurSourceId == EnumSourceIndex.SOURCE_ISDBT) {
            LogTool.i(LogTool.MPLAY, "Ginga getAppList");
            return ginga.getAppList();
        }
        return null;
    }

    public void startApp(String appName) {
        int gingaMode = mDtvConfig.getInt(GINGA_MODE, 0);
        if (1 == gingaMode && null != ginga && mCurSourceId == EnumSourceIndex.SOURCE_ISDBT) {
            ginga.startApp(appName);
            LogTool.i(LogTool.MPLAY, "Ginga startApp");
        }
    }

    //add for CEC
    private void getDestSourceIdx(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            mDestSourceId = intent.getExtras().getInt("SourceName", -1);
            fromCEC = intent.getExtras().getInt("fromCEC", 0);
            fromTimeWake = intent.getExtras().getInt("fromTimeWake", 0);
            fromStandbyWake = intent.getExtras().getInt("fromStandbyWake", 0);
            bookTaskValue = intent.getExtras().getString("bookTaskValue", "none");
            LogTool.d(LogTool.MPLAY, " mDestSourceId : " + mDestSourceId
                    + "; fromCEC = " + fromCEC
                    + "; fromTimeWake : " + fromTimeWake
                    + "; fromStandbyWake : " + fromStandbyWake
                    + "; bookTaskValue : " + bookTaskValue);
            if (bookTaskValue.equals("book_epg")) {
                isStandbyScanEpg = true;
                checkEitTimeout();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogTool.i(LogTool.MPLAY, "===== onNewItent =====");
        super.onNewIntent(intent);
        getDestSourceIdx(intent);
        //for one-key change source in current window
        if (hasWindowFocus()) {
            handleResume();
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        LogTool.d(LogTool.MPLAY, "onWindowFocusChanged " + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            String bootState = SystemProperties.get("persist.sys.bootup");
            //Wait for bootvideo end
            if (bootState.equals("1")) {
                SystemProperties.set("persist.sys.bootup", "" + 0);
                TaskUtil.post(new Runnable() {
                    public void run() {
                        String bootOp = SystemProperties.get("prop.service.bootop.type", "bootanim");
                        if (bootOp.equals("bootanim")) {
                            while ("running".equals(SystemProperties.get("init.svc.bootanim", ""))) {
                                try {
                                    Thread.currentThread().sleep(10);
                                } catch (InterruptedException e) {
                                    LogTool.i(LogTool.MPLAY, "bootanim thread sleep fail");
                                }
                            }
                        } else if (bootOp.equals("bootvideo")) {
                            while ("run".equals(SystemProperties.get("persist.prop.bootvideo.status", "none"))) {
                                try {
                                    Thread.currentThread().sleep(10);
                                } catch (InterruptedException e) {
                                    LogTool.i(LogTool.MPLAY, "bootvideo thread sleep fail");
                                }
                            }
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                handleResume();
                            }
                        });
                    }
                });
            } else {
                handleResume();
            }
        } else {
            // switch source ahead of time to avoid affecting
            // the playback state in launcher
            if (isFinishing()) {
                handleStop();
            }
        }
    }

    @Override
    protected void onResume() {
        LogTool.i(LogTool.MPLAY, "===== onResume =====");
        super.onResume();
        LogTool.d(LogTool.MPLAY, "onResume mHasATVSetDisplay:  " + mHasATVSetDisplay);
        isInitTvPlayFinish = false;
        if (mSubtitleDialog != null) {
            if (mSubtitleDialog.isShowing()) {
                mSubtitleDialog.dismiss();
            }
        }
        ((DTVApplication) getApplication()).setEnabledStop(false);
        isDTVScanning = false;
        isPause = false;
        isWindowKey = false;
        mGotoFullPlayRecordFile = false;
        mCurSourceId = halApi.getCurSourceID();
        // Check Radio background status
        Channel channel = mChnHistory.getCurrentChn(mCurSourceId);
        if (channel != null && halApi.isDTVSource(mCurSourceId) && (HI_INVALID_PID <= channel.getVideoPID()
                && !(channel.getVideoPID() == HI_INVALID_PID && channel.getAudioPID() == HI_INVALID_PID))) {
            LogTool.d(LogTool.MPLAY, " mCurSourceId is DTVSource; mCurSourceId:  " + mCurSourceId + ";  Image of Radio is visible");
            mRadioBGLinearLayout.setVisibility(View.VISIBLE);
        } else {
            LogTool.d(LogTool.MPLAY, " mCurSourceId is not DTVSource; mCurSourceId:  " + mCurSourceId + "; Image of Radio is gone");
            mRadioBGLinearLayout.setVisibility(View.GONE);
        }

        isInitTvPlayFinish = true;
        if (!halApi.isFullWindow()) {
            halApi.setFullVideo();
        }
        //check dolby stream type when going to full window frow small window
        if (halApi.isHDMISource(mCurSourceId)) {
            getHDMIAudioStreamInfo();
        }
        setPipSurfaceToDisplay();
        mHandler.sendEmptyMessageDelayed(MSG_SHOW_NOSIGNAL_DIALOG, 2000);

        String secondLan = "";
        String firstEvtLang = mDtvConfig.getString(CommonValue.SYS_FIRST_EPG_LANG_KEY, DEFAULT_LAN);
        mDTV.getEPG().setEventLang(firstEvtLang, secondLan);

        //check usb status
        String mRecordPath = mDtvConfig.getString("au8RecordFilePath", "/mnt/sdcard");
        if (halApi.isTVSource(mCurSourceId) && !mChannelListView.isShow()) {
            ChannelList tmpCurList = getCurrentList();
            if (null != tmpCurList) {
                int tmpChnCount = tmpCurList.getChannelCount();
                if (tmpChnCount > 0) {
                    mChnInfoView.show(false);
                }
            }
        }
        if (null != mRecordPath && !mRecordPath.equals("/mnt/sdcard")) {
            File file = new File(mRecordPath);
            if (!file.exists() && !file.isDirectory()) {
                mDtvConfig.setString("au8RecordFilePath", "/mnt/sdcard");
            }
        }
        initMixChannelList();
        subDtvOnlyFrontScribeEvent();
        LogTool.i(LogTool.MPLAY, "===== onResume end=====");
    }

    private void initMixChannelList() {
        sourceList = HitvManager.getInstance().getSourceManager().getSourceList();
        if (sourceList.contains(halApi.EnumSourceIndex.SOURCE_ATSC) || sourceList.contains(halApi.EnumSourceIndex.SOURCE_ISDBT)) {
            ChannelManagerActivity cmActivity = new ChannelManagerActivity();
            mixSortChannelList = cmActivity.mixSort();
        }
    }

    private void closePIP() {
        LogTool.d(LogTool.MPLAY, "closePIP");
        Intent intent = new Intent(Intent.ACTION_MEDIA_RESOURCE_GRANTED);
        intent.putExtra(Intent.EXTRA_PACKAGES, new String[]{getPackageName()});
        intent.putExtra(Intent.EXTRA_MEDIA_RESOURCE_TYPE,
                Intent.EXTRA_MEDIA_RESOURCE_TYPE_VIDEO_CODEC);
        CommonDef.sendBroadcastEx(this, intent);
    }

    private void initTvPlay() {
        mCurSourceId = halApi.getCurSourceID();
        boolean needPrepareDTV = true;
        //if mCurSourceId is the same as mDestSourceId ,no need to change source,so need prepareDTV
        if (mCurSourceId == mDestSourceId && mCurSourceId != -1) {
            LogTool.d(LogTool.MPLAY, "resumeResource mCurSourceId == mDestSourceId mCurSourceId" + mCurSourceId + " mDestSourceId " + mDestSourceId);
            if (halApi.isDTVSource(mCurSourceId)) {
                mPlayer.resumeResource();
                needPrepareDTV = false;
            }
        }
        if (fromCEC == 1) {
            LogTool.d(LogTool.MPLAY, "initTvPlay fromCEC:  " + fromCEC);
            mCurSourceId = halApi.getCurSourceID();
            if (mCurSourceId != mDestSourceId) {
                halApi.setSourceHolder();
                halApi.changeSource(mCurSourceId, mDestSourceId);
                mCurSourceId = mDestSourceId;
                refreshInfo();
            }
            fromCEC = 0;
            return;
        }

        // mDestSourceIdx是其他apk通过intent发过来的，表示需要切到的目标源
        if (mDestSourceId != -1) {
            int mChannlId = 0;
            Channel mChannel = null;
            if (halApi.isATVSource(mDestSourceId)) {
                if (fromTimeWake == 1) {
                    mChannlId = Settings.System.getInt(getContentResolver(),
                            Util.SETTING_POWERON_ATVCHANNEL, 0);
                }
                LogTool.d(LogTool.MPLAY, "initTvPlay atv channelID = " + mChannlId);
                if (mChannlId != 0) {
                    mChannel = mAtvChannelManager.getChannelByID(mChannlId);
                }
            } else if (halApi.isDTVSource(mDestSourceId)) {
                if (fromStandbyWake == 1) {
                    if (bookTaskValue.equals("book_scan")) {
                        halApi.setSourceHolder();
                        LogTool.d(LogTool.MPLAY, "changeSource to : " + mDestSourceId);
                        halApi.changeSource(mCurSourceId, mDestSourceId);
                        mCurSourceId = mDestSourceId;
                        mDestSourceId = -1;
                        fullScanDvbt();
                        return;
                    }
                    fromStandbyWake = 0;
                    bookTaskValue = "none";
                }
                if (fromTimeWake == 1) {
                    mChannlId = Settings.System.getInt(getContentResolver(),
                            Util.SETTING_POWERON_DTVCHANNEL, 0);
                }
                LogTool.d(LogTool.MPLAY, "onresume dtv channelID = " + mChannlId);
                if (mChannlId != 0) {
                    mChannel = mChannelManager.getChannelByID(mChannlId);
                    mChannelManager.setDefaultOpenChannel(mChannel, mChannelManager.getDefaultOpenGroupType());
                }
            }
            fromTimeWake = 0;
            if (mChannel != null) {
                mChnHistory.setCurrent(mDestSourceId, mChnHistory.getCurrentList(mDestSourceId), mChannel);
            }

            // 如果目标源与当前源不一致，需要切到目标源
            halApi.setSourceHolder();
            LogTool.d(LogTool.MPLAY, " changeSource to : " + mDestSourceId);

            if (halApi.isATVSource(mDestSourceId) && mChannel != null) {
                halApi.changeSource(mCurSourceId, mDestSourceId, mChannlId);
            } else {
                halApi.changeSource(mCurSourceId, mDestSourceId);
            }
            mCurSourceId = mDestSourceId;

            mDestSourceId = -1;
        } else {
            LogTool.d(LogTool.MPLAY, "cur source :" + mCurSourceId);
            if (mCurSourceId == halApi.EnumSourceIndex.SOURCE_MEDIA) {
                int preSource = halApi.getPreSourceID();
                LogTool.d(LogTool.MPLAY, "pre source : " + preSource);
                halApi.setSourceHolder();
                if (preSource == halApi.EnumSourceIndex.SOURCE_BUTT
                        || preSource == halApi.EnumSourceIndex.SOURCE_MEDIA) {
                    int tvSource = halApi.getSelectSourceID();
                    halApi.changeSource(mCurSourceId, tvSource);
                    mCurSourceId = tvSource;
                } else {
                    halApi.changeSource(mCurSourceId, preSource);
                    mCurSourceId = preSource;
                }
            }
        }

        //在之前的代码段中changeSource可能已经被调用了，因此对curSourceID重新赋值
        LogTool.d(LogTool.MPLAY, "Current source : " + mCurSourceId +
                " , pre source : " + halApi.getPreSourceID());

        // mCurrentSourceIdx是当前的源ID，也就是上一次切源后的源ID，与当前源不一致时，表示已经有了新的切源操作
        // 此处添加有切源操作时需要执行的操作，并保存新的当前源ID
        if (mPreSourceId != mCurSourceId) {
            mPreSourceId = mCurSourceId;
            if (halApi.isDTVSource(mCurSourceId)) {
                LogTool.d(LogTool.MPLAY, "resetDtvChannel");
                // 当切到DTV的源时，重置一下mChnHistory，以便获取当前信号类型的History数据
                mChnHistory.resetDtvChannel();
            }

            // 发生切源时，清除所有消息框
            mTipMsgView.hideAll();
        }

        if (halApi.isDTVSource(mCurSourceId)) {
            if (needPrepareDTV) {
                LogTool.d(LogTool.MPLAY, "resumeResource needPrepareDTV");
                mDTV.prepareDTV();
                //resume play resource
                mPlayer.resumeResource();
            }

            playChannel(mChnHistory.getCurrentList(mCurSourceId),
                    mChnHistory.getCurrentChn(mCurSourceId), true);
            String secondLan = "";
            String firstEvtLang = mDtvConfig.getString(CommonValue.SYS_FIRST_EPG_LANG_KEY, DEFAULT_LAN);
            mDTV.getEPG().setEventLang(firstEvtLang, secondLan);
            makeGetFreeFast();
        } else if (halApi.isATVSource(mCurSourceId)) {
            mPlayer.releaseResource(0);
            playChannel(mChnHistory.getCurrentList(mCurSourceId),
                    mChnHistory.getCurrentChn(mCurSourceId), true, false);
        }
        // 非TV源或者TV源没有频道的情况下，检测切源锁
        if (!halApi.isTVSource(mCurSourceId) || null == mChnHistory.getCurrentChn(mCurSourceId)) {
            processLock(mCurSourceId, null);
        }

        mPlayer.setSurface(mSubSurfaceHolder);
        setSurfaceVisible(mCurSourceId);
        refreshInfo();
    }

    // resume resource after unlocking the channel lock
    public void resumeResourceAfterUnlock() {
        LogTool.d(LogTool.MPLAY, "resumeResource AfterUnlock");
        mPlayer.resumeResource();
    }

    public void setSurfaceVisible(int source) {
        if (halApi.isDTVSource(source)) {
            mSubTeletextSurfaceView.setVisibility(View.VISIBLE);
            mAtvTeletextSurfaceView.setVisibility(View.GONE);
        } else {
            mSubTeletextSurfaceView.setVisibility(View.GONE);
            mAtvTeletextSurfaceView.setVisibility(View.VISIBLE);
        }
        if (EnumSourceIndex.SOURCE_ISDBT == source) {
            gingaSurfaceView.setVisibility(View.VISIBLE);
        } else {
            gingaSurfaceView.setVisibility(View.GONE);
        }
    }

    public void setPipSurfaceToDisplay() {
        if (mPlaySurfaceHolder == null || !isPIPSurfaceCreated) return;
        LogTool.d(LogTool.MPLAY, "setPipSurfaceToDisplay mCurSourceId : " + mCurSourceId);

        if (mHasATVSetDisplay) {
            halApi.clearVideoDisplay(mPlaySurfaceHolder);
        } else {
            mPlayer.clearDisplay(mPlaySurfaceHolder.getSurface());
        }

        if (halApi.isDTVSource(mCurSourceId)) {
            mPlayer.setDisplay(mPlaySurfaceHolder.getSurface());
            mHasATVSetDisplay = false;
        } else {
            halApi.setVideoDisplay(mPlaySurfaceHolder);
            mHasATVSetDisplay = true;
        }
        LogTool.d(LogTool.MPLAY, "setPipSurfaceToDisplay end, ATVSetDisplay : "
                + mHasATVSetDisplay);
    }

    private void clearPipSurface() {
        if (mPlaySurfaceHolder == null || !isPIPSurfaceCreated) return;
        LogTool.d(LogTool.MPLAY, "clearPipSurface");

        if (mHasATVSetDisplay) {
            halApi.clearVideoDisplay(mPlaySurfaceHolder);
        } else {
            mPlayer.clearDisplay(mPlaySurfaceHolder.getSurface());
        }
    }

    private void setDRATagVisibility(int visibility) {
        if (mDRA != null) {
            mDRA.setVisibility(visibility);
        }

        if (mDRAChannel != null) {
            mDRAChannel.setVisibility(visibility);
        }
    }

    private void setDTSStreamTagVisibility(int visibility) {
        if (mDTSStreamType != null) {
            mDTSStreamType.setVisibility(visibility);
        }
    }

    //get StreamInfo from MW
    private void getHDMIAudioStreamInfo() {
        if (!halApi.isHDMISource(mCurSourceId)) {
            return;
        }
        AudioStreamInfo audioStreamInfo = halApi.getAudioStreamInfo();
        LogTool.d(LogTool.MPLAY, "audioStreamInfo= " + audioStreamInfo);
        if (audioStreamInfo == null) {
            dolbyFlag = false;
            hideAudioIcon();
            return;
        } else {
            int value = audioStreamInfo.getValue();
            int type = audioStreamInfo.getType();
            showAudioStreamInfo(type, value);
        }
    }

    //get AudioStreamInfo from DTV
    private void getDTVAudioStreamInfo() {
        int dolbyOnOff = Settings.System.getInt(mContext.getContentResolver(), Util.SETTING_DOLBY_ONOFF, 1);
        dolbyFlag = false;
        if (!halApi.isDTVSource(mCurSourceId) && !halApi.isHDMISource(mCurSourceId)) {
            setDolbyTagVisibility(View.GONE);
            setDRATagVisibility(View.GONE);
            return;
        }
        AudioComponent mCurAudioInfo = mPlayer.getCurrentAudio();
        LogTool.d(LogTool.MPLAY, "mCurAudioInfo= " + mCurAudioInfo);
        if (mCurAudioInfo == null) {
            setDolbyTagVisibility(View.GONE);
            setDRATagVisibility(View.GONE);
            return;
        }
        LogTool.d(LogTool.MPLAY, "mCurAudioInfo.getType()= " + mCurAudioInfo.getType());
        EnStreamType audioType = mCurAudioInfo.getType();
        switch (audioType) {
            case HI_PSISI_STREAM_AUDIO_AC3:
            case HI_PSISI_STREAM_AUDIO_EAC3:
                setDRATagVisibility(View.GONE);
                if (dolbyOnOff == 1) {
                    mDolbyView.setImageResource(R.drawable.dolby_audio);
                    setDolbyTagVisibility(View.VISIBLE);
                } else {
                    setDolbyTagVisibility(View.GONE);
                }
                dolbyFlag = true;
                break;
            case HI_PSISI_STREAM_AUDIO_DRA:
                setDolbyTagVisibility(View.GONE);
                mDRA.setText(R.string.dra_tag);
                int mDRARawChannel = mPlayer.getDRARawChannel();
                Resources res = mContext.getResources();
                if (mDRARawChannel > -1 && mDRARawChannel < 10) {
                    mDRAChannel.setText(res.getStringArray(
                            R.array.dra_raw_channel_array)[mDRARawChannel]);
                } else {
                    mDRAChannel.setText(R.string.dra_error);
                }
                if (!isPause) {
                    setDRATagVisibility(View.VISIBLE);
                } else {
                    setDRATagVisibility(View.GONE);
                }
                break;
            default:
                setDolbyTagVisibility(View.GONE);
                setDRATagVisibility(View.GONE);
                break;
        }
        LogTool.d(LogTool.MPLAY, "dolbyFlag = " + dolbyFlag);
    }

    //hide dolby-Icon/DTS-Icon/DRA
    private void hideAudioIcon() {
        setDolbyTagVisibility(View.GONE);
        setDTSStreamTagVisibility(View.GONE);
        setDRATagVisibility(View.GONE);
    }

    private void showAudioStreamInfo(int type, int value) {
        int dolbyOnOff = Settings.System.getInt(mContext.getContentResolver(), Util.SETTING_DOLBY_ONOFF, 1);
        dolbyFlag = false;
        LogTool.d(LogTool.MPLAY, "type = " + type + ", value = " + value + ", dolbyOnOff = " + dolbyOnOff);
        switch (type) {
            //for dolby
            case AudioStreamInfo.TYPE_DOLBY:
                dolbyFlag = true;
                setDTSStreamTagVisibility(View.GONE);
                if (dolbyOnOff == 0) {
                    setDolbyTagVisibility(View.GONE);
                    break;
                }
                if (value == AudioStreamInfo.VALUE_DOLBY_ATMOS) {
                    if (dolbyOnOff == 1 && halApi.isDolbyAtmosEnable()) {
                        mDolbyView.setImageResource(R.drawable.dolby_atmos);
                    } else {
                        mDolbyView.setImageResource(R.drawable.dobly_atmos_notification);
                    }
                    setDolbyTagVisibility(View.VISIBLE);
                } else if (value == AudioStreamInfo.VALUE_DOLBY_AUDIO) {
                    mDolbyView.setImageResource(R.drawable.dolby_audio);
                    setDolbyTagVisibility(View.VISIBLE);
                } else {
                    setDolbyTagVisibility(View.GONE);
                }
                break;
            //for DTS
            case AudioStreamInfo.TYPE_DTS:
                setDolbyTagVisibility(View.GONE);
                mDTSStreamType.setText(
                        getResources().getString(halApi.DTS_STREAM_TYPE_ARRAY[value][1]));
                setDTSStreamTagVisibility(View.VISIBLE);
                break;
            default:
                setDolbyTagVisibility(View.GONE);
                setDTSStreamTagVisibility(View.GONE);
                break;
        }
    }

    private void setDolbyTagVisibility(int visibility) {
        if (mDolbyView != null) {
            mDolbyView.setVisibility(visibility);
        }
    }

    @Override
    protected void onPause() {
        LogTool.i(LogTool.MPLAY, "===== onPause =====");
        ((DTVApplication) getApplication()).setEnabledStop(true);

        super.onPause();
        isPause = true;
        openGinga(false);
        if (!isWindowKey) {
            clearPipSurface();
        }

        if (mHandler.hasMessages(MSG_COUNT_TIME_DEC)) {
            mHandler.removeMessages(MSG_COUNT_TIME_DEC);
        }

        if (mHandler.hasMessages(MSG_SHOW_COUNT_TIME_DIALOG)) {
            mHandler.removeMessages(MSG_SHOW_COUNT_TIME_DIALOG);
        }

        if (mHandler.hasMessages(MSG_SHOW_NOSIGNAL_DIALOG)) {
            mHandler.removeMessages(MSG_SHOW_NOSIGNAL_DIALOG);
        }
        if (!mPvrRecordView.isShow() && !mTimeshiftView.isShow()) {
            dismissView();
        }
        if (mCountTimeDialog != null && mCountTimeDialog.isShowing()) {
            mCountTimeDialog.dismiss();
        }
        unSubDtvOnlyFrontScribeEvent();
    }

    @Override
    protected void onStop() {
        LogTool.i(LogTool.MPLAY, "=====  onStop =====");
        super.onStop();
        if (!isFinishing()) {
            if (isInPipMode) {
                finish();
            }
            handleStop();
        }
        isCurMultiplexScan = false;
        isSdandbyScanChannel = false;
        isStandbyScanEpg = false;
        mTipMsgView.hideAll();
        mRadioBGLinearLayout.setVisibility(View.GONE);
        hideAudioIcon();
        mTimeshiftView.closeTimeShiftConfirmDialog();
        mTimeshiftView.closeCatchUpLiveDialog();
        mPvrRecordView.closeStopRecordDialog();
    }

    @Override
    protected void onDestroy() {
        LogTool.i(LogTool.MPLAY, "===== onDestroy =====");
        mRadioBGLinearLayout = null;
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        unSubBroadcast();
        if (mAutoSleepSettingProviderObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mAutoSleepSettingProviderObserver);
        }
        if (mDolbyProviderObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mDolbyProviderObserver);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogTool.i(LogTool.MPLAY, "  DTV onKeyUp . KeyCode = " + keyCode);
        if (mChannelListView.isShow()) {
            if (keyCode == KeyValue.DTV_KEYVALUE_BLUE || keyCode == KeyValue.DTV_KEYVALUE_YELLOW
                    || keyCode == KeyValue.DTV_KEYVALUE_RED || keyCode == KeyValue.DTV_KEYVALUE_GREEN) {
                //new dialog
                mChannelListView.onListViewKeyDown(keyCode, event);
                return true;
            }
        }
        if (mCurSourceId == halApi.EnumSourceIndex.SOURCE_ATSC || mCurSourceId == halApi.EnumSourceIndex.SOURCE_ISDBT) {
            if (KeyValue.DTV_KEYVALUE_BLUE == keyCode) {
                return true;
            }
        }

        if (mTimeshiftView.isShow()) {
            int ret = mTimeshiftView.onListViewKeyUp(keyCode, event);
            LogTool.i(LogTool.MPLAY, "  mTimeshiftView onKeyUp . ret = " + ret);
            if (RET_SUPER_FALSE == ret) {
                return false;
            } else if (RET_SUPER_TRUE == ret) {
                return true;
            }
        }
        if (mPvrRecordView.isShow()) {
            int ret = mPvrRecordView.onListViewKeyUp(keyCode, event);
            if (RET_SUPER_FALSE == ret) {
                return false;
            } else if (RET_SUPER_TRUE == ret) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        LogTool.i(LogTool.MPLAY, "  DTV onKeyDown . KeyCode = " + keyCode);
        LogTool.d(LogTool.MPLAY, "onResume isInitTvPlayFinish:  " + isInitTvPlayFinish);
        if (isInEASMode) {
            LogTool.v(LogTool.MPLAY, "block key event in EAS");
            return true;
        }
        //信号源被锁时不响应按键
        if (mTipMsgView.isShow(TipMsgView.TIPMSG_SOURCE_LOCK)) {
            return true;
        }

        if (halApi.isHDMISource(mCurSourceId)) {
            if (halApi.setUICommand(keyCode)) {
                return true;
            }
        }
        if (mTipMsgView.isShow()) {
            /* 节目未解锁时  */
            if (mTipMsgView.isShow(TipMsgView.TIPMSG_PROGRAM_LOCK_DIALOG) || mTipMsgView.isShow(TipMsgView.TIPMSG_PARENTAL_RATING)
                    || mTipMsgView.isShow(TipMsgView.TIPMSG_PROGRAM_LOCK_TIP)) {
                /* 节目未解锁时，且PIP时，需要禁止掉的按键  */
                if (mPipView.isShow()) {
                    /* 大节目未解锁，不允许大小互切 */
                    if (KeyValue.DTV_KEYVALUE_BLUE == keyCode) {
                        MyToast.makeText(this, R.string.play_unlock_fail, MyToast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                /* 节目未解锁时，且OK List时，需要禁止掉的按键  */
                else if (mChannelListView.isShow()) {
                    /* 节目未解锁，不允许BOOK */
                    if (KeyValue.DTV_KEYVALUE_BLUE == keyCode) {
                        MyToast.makeText(this, R.string.play_unlock_fail, MyToast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                /* 节目未解锁时，其它情况时，需要禁止掉的按键  */
                else {
                    if (KeyValue.DTV_KEYVALUE_TXT == keyCode
                            || KeyValue.DTV_KEYVALUE_SUB == keyCode
                            || KeyValue.DTV_KEYVALUE_AUDIO == keyCode
                            || KeyValue.DTV_KEYVALUE_MEDIA_PLAY_PAUSE == keyCode
                            || KeyValue.DTV_KEYVALUE_RED == keyCode
//                            || KeyValue.DTV_KEYVALUE_YELLOW == keyCode
//                            || KeyValue.DTV_KEYVALUE_BLUE == keyCode
                            || KeyValue.DTV_KEYVALUE_GREEN == keyCode) {
                        MyToast.makeText(this, R.string.play_unlock_fail, MyToast.LENGTH_SHORT).show();
                        return true;
                    }
                }
            }
        }
        /*MTS按键在在ATV下为丽声模式，DTV下为声道设置。在DTV下需要在MainActivity中处理MST按键事件，防止
        传递到子View中，调用了设置丽声模式的接口，以至于切源到了ATV，导致DTV无法正常播放*/
        if (keyCode == KeyValue.DTV_KEYVALUE_AUDIO) {
            // TODO: 待优化
            if (null != mPlayer && halApi.isDTVSource(mCurSourceId)) {
                Channel tmpCurChn = mChnHistory.getCurrentChn(mCurSourceId);
                if ((null != tmpCurChn) && (null != tmpCurChn.getAudioComponents())) {
                    mAudioDialog = new AudioSelectorDialog(this, tmpCurChn.getAudioComponents(), mPlayer);
                    mAudioDialog.show();
                } else {
                    MyToast.makeText(this, R.string.audio_nodata, MyToast.LENGTH_SHORT).show();
                }
                return true;
            }
        }

        if (mPipView.isShow()) {
            int ret = mPipView.onListViewKeyDown(keyCode, keyEvent);
            LogTool.i(LogTool.MPLAY, "  mPipView onKeyDown . ret = " + ret);
            if (RET_SUPER_TRUE == ret) {
                return true;
            }
        }
        if (keyCode == KeyValue.DTV_KEYVALUE_BACKWARD) {
            if (halApi.isTVSource(mCurSourceId) && !mPvrRecordView.isShow() && !mTimeshiftView.isShow()) {
                returnPreviousChannel();
            }
        }
        if (keyCode == KeyValue.DTV_KEYVALUE_MEDIA_FAST_FORWARD) {
            if (halApi.isDTVSource(mCurSourceId) && !mPvrRecordView.isShow() && !mTimeshiftView.isShow()) {
                toggleTvRadio();
                return true;
            }
        }
        if (mChannelListView.isShow()) {
            LogTool.i(LogTool.MPLAY, "  mChannelListView onKeyDown . ");
            if (keyCode == KeyValue.DTV_KEYVALUE_EPG) {
                mChnInfoView.hide();
                mChannelListView.toggle();
                openGinga(true);
                return true;
            } else if (keyCode == KeyValue.DTV_KEYVALUE_BLUE || keyCode == KeyValue.DTV_KEYVALUE_YELLOW
                    || keyCode == KeyValue.DTV_KEYVALUE_RED || keyCode == KeyValue.DTV_KEYVALUE_GREEN) {
                return true;
            }
            return mChannelListView.onListViewKeyDown(keyCode, keyEvent);
        }
        if (mDetailInfoView.isShow()) {
            LogTool.i(LogTool.MPLAY, "  mDetailInfoView onKeyDown . ");
            return mDetailInfoView.onListViewKeyDown(keyCode, keyEvent);
        }

        if (mTimeshiftView.isShow()) {
            int ret = mTimeshiftView.onListViewKeyDown(keyCode, keyEvent);
            LogTool.i(LogTool.MPLAY, "  mTimeshiftView onKeyDown . ret = " + ret);
            if (RET_SUPER_FALSE == ret) {
                return false;
            } else if (RET_SUPER_TRUE == ret) {
                return true;
            }
        }

        if (mPvrRecordView.isShow()) {
            int ret = mPvrRecordView.onListViewKeyDown(keyCode, keyEvent);
            LogTool.i(LogTool.MPLAY, "  mPvrRecordView onKeyDown . ret = " + ret);
            if (RET_SUPER_FALSE == ret) {
                return false;
            } else if (RET_SUPER_TRUE == ret) {
                return true;
            }
        }

        if (mChannelFindView.isShow()) {
            int ret = mChannelFindView.onListViewKeyDown(keyCode, keyEvent);
            LogTool.i(LogTool.MPLAY, "  mChannelFindView onKeyDown . ret = " + ret);
            //ret always equals RET_SUPER_FALSE
            return false;
        }

        if (keyCode >= KeyValue.DTV_KEYVALUE_0 && keyCode <= KeyValue.DTV_KEYVALUE_9) {
            if (halApi.isTVSource(mCurSourceId)) {
                mDigitalKeyView.inputKey(keyCode);
            }
        } else if (keyCode == KeyValue.DTV_KEYVALUE_CHNLLIST) {
            if ((mCurSourceId == halApi.EnumSourceIndex.SOURCE_ATSC || mCurSourceId == halApi.EnumSourceIndex.SOURCE_ISDBT) && mDigitalKeyView.isShow()) {
                mDigitalKeyView.inputKey(keyCode);
                LogTool.i(LogTool.MPLAY, "DTV onKeyDown end. ");
                return super.onKeyDown(keyCode, keyEvent);
            }
        } else {
            mDigitalKeyView.hide();
        }
        int gingaMode = mDtvConfig.getInt(GINGA_MODE, 0);
        if (1 == gingaMode && null != ginga && mCurSourceId == EnumSourceIndex.SOURCE_ISDBT) {
            LogTool.i(LogTool.MPLAY, "send ginga keycode = " + keyCode);
            if (ginga.dispatchKey(keyCode, true)) {
                LogTool.i(LogTool.MPLAY, "send ginga keycode is Consumed ,keycode = " + keyCode);
                return true;
            }
        }
        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_TXT:
                int ret = SystemSettingImpl.getInstance().getTvSystem();
                if (ret == EnumSystemTvSystem.TVSYSTEM_DVBT_T2_C || ret == EnumSystemTvSystem.TVSYSTEM_DVBT_T2_S_S2_C) {
                    if (halApi.isDTVSource(mCurSourceId) && (null != mPlayer)) {
                        LogTool.i(LogTool.MPLAY, "isDTVSource");
                        TeletextControl tmpTeletextControl = mPlayer.getTeletextControl();
                        if (null != tmpTeletextControl) {
                            TeletextComponent tmpTeletextComponent = tmpTeletextControl.getCurrentTTX();
                            if (null != tmpTeletextComponent) {
                                tmpTeletextDialog = new TeletextDialog(this, R.style.dialog_transparent,
                                        TeletextDialog.ttxDialogTypeAV);
                                tmpTeletextDialog.show();
                                closeCC();
                            } else {
                                MyToast.makeText(this, R.string.teletext_nodata, MyToast.LENGTH_SHORT).show();
                            }
                        }
                    } else if (halApi.isATVSource(mCurSourceId) || mCurSourceId == EnumSourceIndex.SOURCE_CVBS1
                            || mCurSourceId == EnumSourceIndex.SOURCE_YPBPR1) {
                        LogTool.i(LogTool.MPLAY, "isATVSource");
                        mTeletextDialog = new ATVTeletextDialog(MainActivity.this, R.style.dialog_transparent);
                        mTeletextDialog.show();
                        closeCC();
                    }
                    return true;
                } else {
                    // to respond CC hotkey
                    Channel tmpCurChannel = mPlayer.getCurrentChannel();
                    LogTool.i(LogTool.MPLAY, "respond cc/teletext key to start to show CC");
                    if (isCCSupportArea() && null != tmpCurChannel && EnNetworkType.RF != tmpCurChannel.getNetworkType()) {
                        if (halApi.isDTVSource(mCurSourceId) && null != mDTV.getCCManager() && null != mDTV.getCCManager().getUsedCCLists()) {
                            ClosedCaptionDialog tmpCCDialog = new ClosedCaptionDialog(this, R.style.dialog_transparent);
                            tmpCCDialog.show();
                            break;
                        } else if (halApi.EnumSourceIndex.SOURCE_ATV == mCurSourceId
                                || halApi.EnumSourceIndex.SOURCE_CVBS1 == mCurSourceId) {
                            ATVClosedCaptionDialog tmpCCDialog = new ATVClosedCaptionDialog(this, R.style.dialog_transparent);
                            tmpCCDialog.show();
                            break;
                        }
                    }
                    MyToast.makeText(this, R.string.cc_nodata, MyToast.LENGTH_SHORT).show();
                    break;
                }
            case KeyEvent.KEYCODE_ZOOM:
                openGinga(false);
                break;
            case KeyValue.DTV_KEYVALUE_MENU: {
                mChnInfoView.hide();
                //mNaviView.toggle();
                Intent intent = new Intent("com.hisilicon.android.intent.action.tvsetting");
                startActivity(intent);
                break;
            }
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT: {
                adjustLowerVolume();
                break;
            }
            case KeyValue.DTV_KEYVALUE_DPAD_RIGHT: {
                adjustRaiseVolume();
                break;
            }
            case KeyValue.DTV_KEYVALUE_CHANNEL_UP:
            case KeyValue.DTV_KEYVALUE_DPAD_UP: {
                if (halApi.isTVSource(mCurSourceId)) {
                    changeNextChannel(true);
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_CHANNEL_DOWN:
            case KeyValue.DTV_KEYVALUE_DPAD_DOWN: {
                if (halApi.isTVSource(mCurSourceId)) {
                    changeNextChannel(false);
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_PAGEUP: {
                Channel tmpPreChn = mChnHistory.getPreChn(mCurSourceId);
                Channel tmpCurChn = mChnHistory.getCurrentChn(mCurSourceId);
                /* The previous one is TV and the current is radio, or the previous one is radio and the current is TV. */
                if (tmpPreChn != null && tmpCurChn != null) {
                    if ((EnServiceType.getRadioServiceTypes().contains(tmpPreChn.getServiceType())
                            && !EnServiceType.getRadioServiceTypes().contains(tmpCurChn.getServiceType()))
                            || (EnServiceType.getTVServiceTypes().contains(tmpPreChn.getServiceType())
                            && !EnServiceType.getTVServiceTypes().contains(tmpCurChn.getServiceType()))
                            || (EnServiceType.getDATAServiceTypes().contains(tmpPreChn.getServiceType())
                            && !EnServiceType.getDATAServiceTypes().contains(tmpCurChn.getServiceType()))) {
                        toggleTvRadio();
                    }
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_SEARCH: {
                if (halApi.isTVSource(mCurSourceId)) {
                    mChannelFindView.show();
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_CHNLLIST: {
                if (halApi.isTVSource(mCurSourceId)) {
                    mChnInfoView.hide();
                    mChannelListView.toggle();
                }
                break;
            }

            case KeyValue.DTV_KEYVALUE_BACK: {
                if (mChnInfoView.isShow()) {
                    mChnInfoView.hide();
                    return true;
                }
                break;
            }

            case KeyValue.DTV_KEYVALUE_SUB: {
                // TODO: 待优化
                if (null != mPlayer) {
                    Channel mCurChannel = mPlayer.getCurrentChannel();
                    if ((null != mCurChannel) && (null != mCurChannel.getSubtitleComponents())) {
                        mSubtitleDialog = new SubtitleSelectorDialog(this, R.style.dialog_transparent, SubtitleDialog.subtDialogTypeAV);
                        if (mSubtitleDialog.isShowing()) {
                            mSubtitleDialog.dismiss();
                        } else {
                            mSubtitleDialog.show();
                        }
                    } else {
                        if (halApi.isDTVSource(mCurSourceId)) {
                            MyToast.makeText(this, R.string.subtitle_nodata, MyToast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_INFOBAR: {
                if (mDetailInfoView.isShow()) {
                    mDetailInfoView.hide();
                } else if (halApi.isTVSource(mCurSourceId)) {
                    if (mChnHistory.getCurrentChn(mCurSourceId) == null) {
                        MyToast.makeText(this, R.string.play_no_channel_title, MyToast.LENGTH_LONG).show();
                        return true;
                    }
                    if (mChnInfoView.isShow()) {
                        mChnInfoView.hide();
                        if (halApi.isDTVSource(mCurSourceId)) {
                            mDetailInfoView.show();
                        }
                    } else {
                        mChnInfoView.show(false);
                    }
                } else {
                    mSignalShow.toggle(mCurSourceId);
                }
                break;
            }

            case KeyValue.DTV_KEYVALUE_RED: {
                if (!halApi.isDTVSource(mCurSourceId)) {
                    return true;
                }
                //MyToast.makeText(this, R.string.str_pvr_not_support, MyToast.LENGTH_SHORT).show();
                //break;
                Channel channel = mChnHistory.getCurrentChn(mCurSourceId);
                if (null == channel) {
                    return true;
                }

                if (isCanRec(channel)) {
                    mPvrRecordView.showSetTimeView();
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_MEDIA_PLAY_PAUSE:
            case KeyValue.DTV_KEYVALUE_GREEN: {
                if (!halApi.isDTVSource(mCurSourceId)) {
                    return true;
                }

                //MyToast.makeText(this, R.string.str_timeshift_not_support, MyToast.LENGTH_SHORT).show();
                //break;
                Channel channel = mChnHistory.getCurrentChn(mCurSourceId);
                if (null == channel) {
                    return true;
                }
                LogTool.d(LogTool.MPLAY, "channel.getServiceType()=" + channel.getServiceType());
                if (isCanRec(channel)) {
                    mChnInfoView.hide();
                    mTimeshiftView.timeshiftStart();
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_VOD: {
                if (SystemProperties.get("ro.source.dvbchange.enable").equals("true")) {
                    mPlayer.releaseResource(0);
                    finish();
                    return false;
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_EPG: {
                if (halApi.isDTVSource(mCurSourceId)) {
                    mChnInfoView.hide();
                    mChannelListView.directShowEPG();
                    openGinga(false);
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_YELLOW:
            case KeyValue.DTV_KEYVALUE_BLUE: {
                int sourceId = EnumSourceIndex.SOURCE_ATV;
                if (keyCode == KeyValue.DTV_KEYVALUE_YELLOW) {
                    sourceId = Settings.System.getInt(getContentResolver(), "setting_source_source1", EnumSourceIndex.SOURCE_ATV);
                } else {
                    sourceId = Settings.System.getInt(getContentResolver(), "setting_source_source2", EnumSourceIndex.SOURCE_DVBC);
                }
                if (mCurSourceId == sourceId) {
                    return true;
                }
                if (keyCode != KeyValue.DTV_KEYVALUE_YELLOW &&
                        (mCurSourceId == halApi.EnumSourceIndex.SOURCE_ATSC
                                || mCurSourceId == halApi.EnumSourceIndex.SOURCE_ISDBT)) {
                    LogTool.d(LogTool.MPLAY, "ATSC and ISDBT don't support turn to DVBC");
                    return true;
                }
                if (Math.abs(System.currentTimeMillis() - hotkeyChangeSourceStartTime) < DURATION_BETWEEN_TWO_ONKEYDOWN_EVENT) {
                    LogTool.d(LogTool.MPLAY, "press hotkey too fast,cancel the onkeydown event");
                    return true;
                }
                hotkeyChangeSourceStartTime = System.currentTimeMillis();
                if (!isInitTvPlayFinish) {
                    LogTool.d(LogTool.MPLAY, "the last operation is not end,cancel the onkeydown event");
                    return true;
                }
                break;
            }
            default:
                break;
        }
        LogTool.i(LogTool.MPLAY, " DTV onKeyDown end. ");
        return super.onKeyDown(keyCode, keyEvent);
    }

    private void closeCC() {
        if (null != ccManager && ccManager.isCCShow()) {
            ccManager.showCC(false);
        }
        if (1 == SourceManagerImpl.getInstance().getCcEnable()) {
            SourceManagerImpl.getInstance().setCcEnable(0);
        }
    }

    private boolean isCCSupportArea() {
        int tvSystem = halApi.getTvSystemType();
        if (tvSystem == EnumSystemTvSystem.TVSYSTEM_ISDBT
                || tvSystem == EnumSystemTvSystem.TVSYSTEM_ATSC
                || tvSystem == EnumSystemTvSystem.TVSYSTEM_ATV) {
            return true;
        }
        return false;
    }

    private Rect getPipRect() {
        Rect rect;
        int mode = Settings.System.getInt(mContext.getContentResolver(), Util.SETTING_PIP_POSITION, 0);
        //pip window size is 480 * 270, default position is right-bottom.
        int dimen112 = (int) getResources().getDimension(R.dimen.dimen_112px);
        int dimen54 = (int) getResources().getDimension(R.dimen.dimen_54px);
        int dimen592 = (int) getResources().getDimension(R.dimen.dimen_592px);
        int dimen324 = (int) getResources().getDimension(R.dimen.dimen_324px);
        int dimen756 = (int) getResources().getDimension(R.dimen.dimen_756px);
        int dimen1026 = (int) getResources().getDimension(R.dimen.dimen_1026px);
        int dimen1328 = (int) getResources().getDimension(R.dimen.dimen_1328px);
        int dimen1808 = (int) getResources().getDimension(R.dimen.dimen_1808px);
        switch (mode) {
            case halApi.EnumPipPosition.LEFT_TOP:
                rect = new Rect(dimen112, dimen54, dimen592, dimen324);
                break;
            case halApi.EnumPipPosition.LEFT_BOTTOM:
                rect = new Rect(dimen112, dimen756, dimen592, dimen1026);
                break;
            case halApi.EnumPipPosition.RIGHT_TOP:
                rect = new Rect(dimen1328, dimen54, dimen1808, dimen324);
                break;
            case halApi.EnumPipPosition.RIGHT_BOTTOM:
                rect = new Rect(dimen1328, dimen756, dimen1808, dimen1026);
                break;
            default:
                rect = new Rect(dimen1328, dimen756, dimen1808, dimen1026);
                break;
        }
        return rect;
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode,
                                              Configuration newConfig) {
        LogTool.d(LogTool.MPLAY, "isInPictureInPictureMode : " + isInPictureInPictureMode);
        isInPipMode = isInPictureInPictureMode;
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    @SuppressWarnings("deprecation")
    private void initSurfaceView() {
        LogTool.d(LogTool.MPLAY, "initSurfaceView");

        mPlaySurfaceView = (SurfaceView) findViewById(R.id.play_surface);
        mPlaySurfaceView.setVisibility(View.VISIBLE);
        mPlaySurfaceHolder = mPlaySurfaceView.getHolder();
        mPlaySurfaceHolder.setFixedSize((int) getResources().getDimension(R.dimen.dimen_1920px),
                (int) getResources().getDimension(R.dimen.dimen_1080px));
        mPlaySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        mPlaySurfaceHolder.setFormat(PixelFormat.RGBA_8888);
        mPlaySurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                LogTool.d(LogTool.MPLAY, "PIP surfaceChanged");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogTool.d(LogTool.MPLAY, "PIP surfaceCreated");
                mPlaySurfaceHolder = holder;
                if (halApi.isDTVSource(mCurSourceId)) {
                    mPlayer.setDisplay(mPlaySurfaceHolder.getSurface());
                    mHasATVSetDisplay = false;
                } else {
                    halApi.setVideoDisplay(mPlaySurfaceHolder);
                    mHasATVSetDisplay = true;
                }
                isPIPSurfaceCreated = true;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogTool.d(LogTool.MPLAY, "PIP surfaceDestroyed");
                clearPipSurface();
                isPIPSurfaceCreated = false;
                mPlaySurfaceHolder = null;
            }
        });

        mSubTeletextSurfaceView = (SurfaceView) findViewById(R.id.play_surfaceview);
        mSubSurfaceHolder = mSubTeletextSurfaceView.getHolder();
        mSubSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);


        mSubSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LogTool.d(LogTool.MPLAY, "SUB surfaceChanged");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogTool.d(LogTool.MPLAY, "SUB surfaceCreated");
                mSubTeletextSurfaceView.setVisibility(View.VISIBLE);
                if (null != mPlayer) {
                    LogTool.d(LogTool.MPLAY, "SUB begin setSurface");
                    if (-1 != mPlayer.setSurface(holder)) {
                        LogTool.d(LogTool.MPLAY, "SUB cur holder (" + holder + ")");
                        mSubSurfaceHolder = holder;
                    } else {
                        LogTool.d(LogTool.MPLAY, "SUB set error!! ");
                    }
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogTool.d(LogTool.MPLAY, "SUB surfaceDestroyed");
                if (null != mPlayer) {

                    if (0 == mPlayer.setSurface((SurfaceHolder) null)) {
                        LogTool.d(LogTool.MPLAY, "clear sub surface success");
                    } else {
                        LogTool.d(LogTool.MPLAY, "clear sub surface error!! ");
                    }
                }
            }
        });


        mAtvTeletextSurfaceView = (SurfaceView) findViewById(R.id.play_atv_surfaceview);
        mAtvSurfaceHolder = mAtvTeletextSurfaceView.getHolder();
        mAtvSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);


        mAtvSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (null != mPlayer) {
                    halApi.setTTXSurface(mAtvSurfaceHolder);
                    TeletextControl teletextControl = mPlayer.getTeletextControl();
                    if (teletextControl.isTTXVisible()) {
                        mTeletextDialog = new ATVTeletextDialog(MainActivity.this, R.style.dialog_transparent);
                        mTeletextDialog.show();
                    }
                }

                LogTool.d(LogTool.MPLAY, "ATV SUB surfaceChanged");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogTool.d(LogTool.MPLAY, "ATV SUB surfaceCreated");
                mAtvTeletextSurfaceView.setVisibility(View.VISIBLE);

                LogTool.d(LogTool.MPLAY, "ATV SUB set ATV Surface");
                halApi.setTTXSurface(mAtvSurfaceHolder);
                if (null != mPlayer) {
                    TeletextControl teletextControl = mPlayer.getTeletextControl();
                    if (teletextControl.isTTXVisible()) {
                        mTeletextDialog = new ATVTeletextDialog(MainActivity.this, R.style.dialog_transparent);
                        mTeletextDialog.show();
                    } else if (halApi.getCcEnable() != 0 && halApi.isCcVisible()) {
                        halApi.showCc(true);
                    }
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogTool.d(LogTool.MPLAY, "ATV SUB surfaceDestroyed");
            }
        });

        gingaSurfaceView = (SurfaceView) findViewById(R.id.play_ginga_surfaceview);
        gingaSurfaceHolder = gingaSurfaceView.getHolder();
        gingaSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        gingaSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LogTool.d(LogTool.MPLAY, "gingaSurfaceView surfaceChanged");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogTool.d(LogTool.MPLAY, "gingaSurfaceView surfaceCreated");
                if (null != ginga) {
                    ginga.setGraphicSurface(gingaSurfaceHolder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogTool.d(LogTool.MPLAY, "gingaSurfaceView surfaceDestroyed");
            }
        });
    }

    /**
     * Start DTV service.
     */
    private void startService() {
        Intent startSrvIntent = new Intent(this, DTVService.class);
        startSrvIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        CommonDef.startServiceEx(this, startSrvIntent);
    }

    private void initView() {
        mStringCountTime = getResources().getString(R.string.str_count_time);
        autoSleepTime = getAutoSleepTime();
        mAutoSleepSettingProviderObserver = new PlayerProviderObserver(this, mHandler, MSG_AUTOSLEEP_SETTINGS_CHANGE);
        mDolbyProviderObserver = new PlayerProviderObserver(this, mHandler, MSG_DOLBY_ONOFF);
        registerContentObservers();

        mChnInfoView = new ChannelInfoBarView(this);
        mDetailInfoView = new DetailInfoView(this);
        mChannelListView = new ChannelListView(this);
        mTipMsgView = new TipMsgView(this);
        mPvrRecordView = new PvrRecordView(this, mChnInfoView);
        mTimeshiftView = new TimeshiftView(this);
        mDigitalKeyView = new DigitalKeyView(this, mChannelManager);
        mPipView = new PipView(this);
        mChannelFindView = new ChannelFindView(this);
        initSurfaceView();
        mRadioBGLinearLayout = (LinearLayout) findViewById(R.id.ly_radio_bg);
        mSignalShow = (SignalShow) findViewById(R.id.tv_play_signal_show);
        mDRA = (TextView) findViewById(R.id.dra);
        mDRAChannel = (TextView) findViewById(R.id.dra_channel);
        mDolbyView = (ImageView) findViewById(R.id.dolby_icon);
        mDTSStreamType = (TextView) findViewById(R.id.dts_type);

        String ssuDescription = mContext.getResources().getString(R.string.ssu_str_description);
        mSsuConfirmDialog = new ConfirmDialog(mContext, R.style.DIM_STYLE, "", ssuDescription, 1f);
        OnConfirmDialogListener tmpCatchListener = new OnConfirmDialogListener() {
            @Override
            public void onCheck(int which) {
                if (which == ConfirmDialog.OnConfirmDialogListener.OK) {

                    CommonDef.startActivityEx(MainActivity.this, new Intent(MainActivity.this, OadProgressActivity.class));
                    //MyToast.makeText(mContext, R.string.str_pu_program_updated, MyToast.LENGTH_SHORT).show();
                }

                mSsuConfirmDialog.cancel();
            }
        };
        mSsuConfirmDialog.setConfirmDialogListener(tmpCatchListener);
        mEASTextView = (TextView) findViewById(R.id.eas_text);
    }

    private void dismissView() {
        mChnInfoView.hide();
        mDetailInfoView.hide();
        mChannelListView.hide();
        mSignalShow.hide();
        mTipMsgView.dismissAll();
        mDigitalKeyView.hide();
        mPipView.hide();
        mChannelFindView.hide();
        if (mChnHistory.isRecording() && !mGotoFullPlayRecordFile) {
            mPvrRecordView.stopPvr();
        }

        if (mTimeshiftView.isShow()) {
            mTimeshiftView.stop();
        }

        TeletextControl mTeletextControl = mPlayer.getTeletextControl();
        if ((mTeletextControl != null) && (mTeletextControl.isTTXVisible())) {
            mTeletextControl.showTTX(false);
        }
        if (null != tmpTeletextDialog) {
            tmpTeletextDialog.dismiss();
        }
        if (null != mTeletextDialog) {
            mTeletextDialog.dismiss();
        }
        if ((null != mSubtitleDialog) && (mSubtitleDialog.isShowing())) {
            mSubtitleDialog.hide();
        }

        if (null != mAudioDialog) {
            mAudioDialog.hide();
        }

        if (null != mRescanProgressDialog) {
            mRescanProgressDialog.dismiss();
        }
    }

    private void makeGetFreeFast() {
        TaskUtil.post(new Runnable() {
            /* To make getFreeSpace faster next time. */
            public void run() {
                String mRecordPath = mDTV.getConfig().getString("au8RecordFilePath", "/mnt/sdcard");
                if (null != mRecordPath) {
                    File file = new File(mRecordPath);
                    if (null != file && file.exists()) {
                        long mPvrFreeSize = file.getFreeSpace();
                        LogTool.d(LogTool.MPLAY, "mRecordPath = " + mRecordPath + "mPvrFreeSize = " + mPvrFreeSize);
                    }
                }
            }
        });
    }

    private void stopAvplay() {
        TaskUtil.post(new Runnable() {
            public void run() {
                mPlayer.stop(EnStopType.BLACKSCREEN);
            }
        });
    }

    public void hideTipMsgView() {
        if (mTipMsgView != null) {
            mTipMsgView.hideAll();
        }
    }

    /* 播放频道接口。理论上，所有apk中播放的动作都应该调用该函数来实现。不允许直接调用dtv java api中的changeChannel. */
    public void playChannel(ChannelList chnList, Channel channel, boolean bShowInfoBar) {
        playChannel(chnList, channel, bShowInfoBar, true);
    }

    public void playChannel(ChannelList chnList, Channel channel, boolean bShowInfoBar, boolean bNeedChange) {
        LogTool.i(LogTool.MPLAY, "playChannel begin");

        if (null == channel) {
            //mTipMsgView.show(TipMsgView.TIPMSG_NOPROGRAM);
            return;
        }

        Settings.Secure.putInt(getContentResolver(), "cur_channel_id", channel.getChannelID());
        // 显示切源密码框时，不允许切频道
        if (mTipMsgView.isShow(TipMsgView.TIPMSG_SOURCE_LOCK)) {
            return;
        }

        if (EnNetworkType.RF != channel.getNetworkType() && HI_INVALID_PID <= channel.getVideoPID()
                && mCurSourceId != halApi.EnumSourceIndex.SOURCE_ATSC &&
                !(channel.getVideoPID() == HI_INVALID_PID && channel.getAudioPID() == HI_INVALID_PID)) {
            mRadioBGLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mRadioBGLinearLayout.setVisibility(View.GONE);
        }

        /* 当前已经播放出来的，和要播放的是一个频道，则直接返回  */
        if (null != mPlayer.getCurrentChannel() && mPlayer.getCurrentChannel().getChannelID() == channel.getChannelID()
                && EnPlayStatus.STOP != mPlayer.getStatus()) {
            LogTool.i(LogTool.MPLAY, "playChannel same channel ");

            /* InforBar显示 */
            if (bShowInfoBar) {
                mChnInfoView.show(false);
            }

            /* 记录当前频道，可能节目未变，分组发生改变 */
            mChnHistory.setCurrent(mCurSourceId, chnList, channel);

            processLock(mCurSourceId, channel);
            return;
        }

        if (null != mChnHistory.getLastChn(mCurSourceId)) {
            if (mChnHistory.getLastChn(mCurSourceId).getChannelID() != channel.getChannelID()) {
                /* 清空之前状态框  */
                mTipMsgView.hideAll();
            }
        } else {
            /* 清空之前状态框  */
            mTipMsgView.hideAll();
        }

        /* Since all the dialogs have been cleared before, the Scramble needs to be displayed when the current channel is of type CA.  */
        if (channel.isScramble()) {
            mTipMsgView.show(TipMsgView.TIPMSG_CA);
        }

        /* 记录当前频道 */
        mChnHistory.setCurrent(mCurSourceId, chnList, channel);

        /* 各种锁判断，DTV源下，锁的判断在changeChannel之前*/
        if (halApi.isDTVSource(mCurSourceId) && processLock(mCurSourceId, channel)) {
            if (mChnInfoView.isShow()) {
                mChnInfoView.hide();
            }
            return;
        }

        // @bNeedChange ATV 是否需要切台
        /* 播放 */
        if (bNeedChange) {
            LogTool.i(LogTool.MPLAY, "playChannel changeChannel");
            mPlayer.resumeResource();
            mPlayer.changeChannel(channel);
            // TODO: how to get and update software version is decided by user
            mDTV.getOTA().startOta(3, 1,1,1,2);
        }

        /* 各种锁判断，ATV源下，锁的判断在changeChannel之后*/
        if (halApi.isATVSource(mCurSourceId)) {
            if (processLock(mCurSourceId, channel)) {
                return;
            }
        }
        if (bShowInfoBar) {
            mChnInfoView.show(false);
        }
        LogTool.i(LogTool.MPLAY, "playChannel end ");
    }

    public Channel getNextChannel(ChannelList tmpCurList, Channel tmpCurChn, boolean bAdd) {
        Channel tmpChn = null;
        int nNexCalcValue = bAdd ? 1 : -1;
        int tmpChnCount = tmpCurList.getChannelCount();
        if (tmpChnCount > 0) {
            int tmpIndex = tmpCurList.getPosByChannelID(tmpCurChn.getChannelID());
            for (int i = 0; i < tmpChnCount; i++) {
                tmpIndex = ((tmpIndex + tmpChnCount + nNexCalcValue) % tmpChnCount);
                tmpChn = tmpCurList.getChannelByIndex(tmpIndex);
                if (tmpChn == null || (tmpChn != null && tmpChn.getTag(EnTagType.HIDE))) {
                    continue;
                } else {
                    break;
                }
            }
        }
        return tmpChn;
    }


    public void changeNextChannel(boolean bAdd) {
        LogTool.i(LogTool.MPLAY, "changeNextChannel");
        if (sourceList.contains(halApi.EnumSourceIndex.SOURCE_ATSC) || sourceList.contains(halApi.EnumSourceIndex.SOURCE_ISDBT)) {
            Channel tmpCurChn = mChnHistory.getCurrentChn(mCurSourceId);
            if (null == mixSortChannelList || mixSortChannelList.size() == 0 || null == tmpCurChn) {
                return;
            }
            Channel nextChannel = null;
            for (int i = 0; i < mixSortChannelList.size(); i++) {
                if (tmpCurChn.getSortTag() == mixSortChannelList.get(i).getSortTag()) {
                    //up
                    if (bAdd) {
                        if (i + 1 < mixSortChannelList.size()) {
                            nextChannel = mixSortChannelList.get(i + 1);
                        } else {
                            nextChannel = mixSortChannelList.get(0);
                        }
                    } else {
                        if (i - 1 >= 0) {
                            nextChannel = mixSortChannelList.get(i - 1);
                        } else {
                            nextChannel = mixSortChannelList.get(mixSortChannelList.size() - 1);
                        }
                    }
                    break;
                }
            }
            if (nextChannel == null) {
                return;
            }
            // if this channel tag is skip
            if (nextChannel.getTag(EnTagType.HIDE)) {
                mChnHistory.setCurrent(mCurSourceId, null, nextChannel);
                changeNextChannel(bAdd);
                return;
            }
            // If the program type does not correspond to the current source, switch the source first.
            if (EnNetworkType.RF == nextChannel.getNetworkType() && halApi.isDTVSource(mCurSourceId)) {
                halApi.changeSource(mCurSourceId, halApi.EnumSourceIndex.SOURCE_ATV, nextChannel.getChannelID());
                mCurSourceId = halApi.EnumSourceIndex.SOURCE_ATV;
                hideTipMsgView();
            } else if (EnNetworkType.RF != nextChannel.getNetworkType() && halApi.isATVSource(mCurSourceId)) {
                int dstSource = halApi.EnumSourceIndex.SOURCE_DVBC;
                switch (nextChannel.getNetworkType()) {
                    case CABLE:
                        dstSource = halApi.EnumSourceIndex.SOURCE_DVBC;
                        break;
                    case TERRESTRIAL:
                        dstSource = halApi.EnumSourceIndex.SOURCE_DVBT;
                        break;
                    case DTMB:
                        dstSource = halApi.EnumSourceIndex.SOURCE_DTMB;
                        break;
                    case ISDB_TER:
                        dstSource = halApi.EnumSourceIndex.SOURCE_ISDBT;
                        break;
                    case ATSC_T:
                        dstSource = halApi.EnumSourceIndex.SOURCE_ATSC;
                        break;
                    case ATSC_CAB:
                        dstSource = halApi.EnumSourceIndex.SOURCE_ATSC;
                        break;
                    default:
                        break;
                }
                // current not DTV, change source to DTV
                halApi.setSourceHolder();
                halApi.changeSource(mCurSourceId, dstSource);
                mCurSourceId = dstSource;
                setSurfaceVisible(mCurSourceId);
                hideTipMsgView();
                // resume DTV resource
                mDTV.prepareDTV();
                mPlayer.resumeResource();
            }
            playChannel(null, nextChannel, true);
        } else {
            ChannelList tmpCurList = getCurrentList();
            Channel tmpCurChn = mChnHistory.getCurrentChn(mCurSourceId);
            if (null == tmpCurList) {
                return;
            }
            Channel tmpChn = getNextChannel(tmpCurList, tmpCurChn, bAdd);
            playChannel(tmpCurList, tmpChn, true);
        }

    }

    private ChannelList getCurrentList() {
        ChannelList tmpCurList = null;
        tmpCurList = mChnHistory.getCurrentList(mCurSourceId);
        return tmpCurList;
    }

    //返回到上一个播放频道
    private void returnPreviousChannel() {
        ChannelList tmpPreList = mChnHistory.getPreList(mCurSourceId);
        Channel previousChannel = mPlayer.getPreviousChannel(EnTVRadioFilter.ALL);
        Channel curChannel = mPlayer.getCurrentChannel();
        EnServiceType preEnServiceType = previousChannel.getServiceType();
        EnServiceType curEnServiceType = curChannel.getServiceType();
        if (preEnServiceType != curEnServiceType) {
            if (preEnServiceType == EnServiceType.RADIO) {
                toggleToRadioList();
                return;
            } else if (preEnServiceType == EnServiceType.TV) {
                toggleToTvList();
                return;
            } else if (preEnServiceType == EnServiceType.DATABROADCAST) {
                toggleToDataList();
                return;
            }
        }

        if (null == tmpPreList || null == previousChannel) {
            return;
        }

        if (previousChannel.getNetworkType() == EnNetworkType.RF) {
            previousChannel = mAtvChannelManager.getChannelByID(previousChannel.getChannelID());
        } else {
            previousChannel = mChannelManager.getChannelByID(previousChannel.getChannelID());
        }

        if (null == previousChannel) {
            return;
        }

        if (previousChannel.getNetworkType() != curChannel.getNetworkType()) {
            return;
        }

        playChannel(tmpPreList, previousChannel, true);
        if (mChannelListView.isShow()) {
            mChannelListView.show();
        }
    }

    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (halApi.isDTVSource(mCurSourceId)) {
                if (0 == msg.what) {
                    LogTool.i(LogTool.MPLAY, "DTV have signal");
                    setSignalUnSupportFlag(false);
                    setSignalFlag(true);
                    ChannelList tmpCurrentList = mChnHistory.getCurrentList(halApi.EnumSourceIndex.SOURCE_AUTO);
                    if (tmpCurrentList != null && tmpCurrentList.getChannelCount() > 0) {
                        showVideo(true);
                    }
                    changeSourceInitGinga(true);
                } else {
                    LogTool.i(LogTool.MPLAY, "DTV no signal");
                    setSignalFlag(false);
                    setDolbyTagVisibility(View.GONE);
                    showVideo(false);
                }
            } else {
                if (0 == msg.what) {
                    LogTool.i(LogTool.MPLAY, "ATV have signal");
                    setSignalUnSupportFlag(false);
                    setSignalFlag(true);
                    changeSourceInitGinga(false);
                } else if (2 == msg.what) {
                    LogTool.i(LogTool.MPLAY, "ATV signal unsupport");
                    setSignalUnSupportFlag(true);
                    setDolbyTagVisibility(View.GONE);
                    setDTSStreamTagVisibility(View.GONE);
                } else {
                    LogTool.i(LogTool.MPLAY, "ATV no signal");
                    setSignalUnSupportFlag(false);
                    setSignalFlag(false);
                    setDolbyTagVisibility(View.GONE);
                    setDTSStreamTagVisibility(View.GONE);
                }
            }
            LogTool.i(LogTool.MPLAY, "mUIHandler msg.what = " + msg.what + "mCountTimeDialog = " + mCountTimeDialog);
            if (mCountTimeDialog != null && 1 != msg.what && mCountTimeDialog.isShowing()) {
                mCountTimeDialog.dismiss();
            }
        }
    };

    private void changeSourceInitGinga(boolean isOpen) {
        int gingaMode = mDtvConfig.getInt(GINGA_MODE, 0);
        if (null != ginga && 1 == gingaMode) {
            if (isOpen) {
                ginga.init();
            } else {
                ginga.deinit();
            }
            LogTool.d(LogTool.MPLAY, "dtv change to atv should close ginga");
        }
    }

    halApi.HalOnTvEventListener tvEventListener = new halApi.HalOnTvEventListener() {
        @Override
        public void onSignalStatus(int status) {
            //notify autosleep signal change
            LogTool.d(LogTool.MSERVICE, "onSignalStatus :" + status);
            autoSleepTime = getAutoSleepTime();
            showNoSignalStatus(status);

            Message message = new Message();
            message.what = status;
            mUIHandler.sendMessage(message);
        }
    };

    IDTVListener dvbPlayListener = new IDTVListener() {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            LogTool.d(LogTool.MPLAY, "get notify message : " + messageID);
            LogTool.d(LogTool.MPLAY, "dvbPlayListener messageID: " + messageID + " dvbPlayListener param1: " + param1 + " dvbPlayListener param2: " + param2);
            switch (messageID) {
                case DTVMessage.HI_SVR_EVT_AV_CA: {
                    if (1 == param1) {
                        mTipMsgView.hide(TipMsgView.TIPMSG_FRONTEND_ERROR);
                        mTipMsgView.show(TipMsgView.TIPMSG_CA);
                    } else {
                        mTipMsgView.hide(TipMsgView.TIPMSG_CA);
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_FRONTEND_STOP: {
                    setDRATagVisibility(View.GONE);
                    mTipMsgView.hide(TipMsgView.TIPMSG_CA);
                    mTipMsgView.show(TipMsgView.TIPMSG_FRONTEND_ERROR);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_FRONTEND_RESUME: {
                    mTipMsgView.hide(TipMsgView.TIPMSG_FRONTEND_ERROR);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_PLAY_SUCCESS: {
                    mTipMsgView.hide(TipMsgView.TIPMSG_CA);
                    mTipMsgView.hide(TipMsgView.TIPMSG_FRONTEND_ERROR);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING://TODO:
                {
                    LogTool.i(LogTool.MPLAY, "hide RecordPlayStatus.getInstance().isPVR_PLAYING() " + RecordPlayStatus.getInstance().isPlaying());
                    if (mCurSourceId != halApi.getCurSourceID() || RecordPlayStatus.getInstance().isPlaying()) {
                        break;
                    }
                    if (checkDtvParentLocked(mCurSourceId, mChnHistory.getCurrentChn(mCurSourceId))) {
                        stopAvplay();
                        if (mTimeshiftView.isShow()) {
                            mTimeshiftView.stop();
                        }
                        // 如果更新后需要加锁，停止节目播放，显示密码框
                        mTipMsgView.show(TipMsgView.TIPMSG_PARENTAL_RATING);
                        LogTool.d(LogTool.MPLAY, "update TIPMSG_PARENTAL_RATING ");
                    } else {
                        LogTool.d(LogTool.MPLAY, "hide TIPMSG_PARENTAL_RATING ");
                        mTipMsgView.hideAll();
                        // 如果更新后不需要加锁，重新播放节目
                        showVideo(true);
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PARENTAL_LOCK_STATUS:
                    LogTool.d(LogTool.MCHANNEL, "HI_SVR_EVT_EPG_PARENTAL_LOCK_STATUS, status = " + param1 + ", curSource = " + mCurSourceId);
                    if (mCurSourceId != halApi.EnumSourceIndex.SOURCE_ATSC) {
                        return;
                    }
                    boolean lockStatusEnable = SystemSettingInterface.getLockEnable(3);
                    mTipMsgView.showCurrentRate(true, ParentalControlUtil.getInstance().getRateString(mChnHistory.getCurrentChn(mCurSourceId), null));
                    if ((param1 == 1) && lockStatusEnable) {
                        mTipMsgView.show(TipMsgView.TIPMSG_PARENTAL_RATING);
                        halApi.setDtvLockEnable(true);
                    } else if (param1 == 0) {
                        mTipMsgView.hide(TipMsgView.TIPMSG_PARENTAL_RATING);
                        halApi.setDtvLockEnable(false);
                    }
                    break;
                case DTVMessage.HI_SVR_EVT_PU_RESCAN://TODO:
                {
                    puRescan(param1, obj);
                    initMixChannelList();
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PU_PROG_UPDATED://TODO:
                {
                    LogTool.d(LogTool.MPLAY, "DTVMessage param1 = " + param1 + "DTVMessage param2 = " + param2);
                    mChannelListView.hide();
                    MyToast.makeText(mContext, R.string.str_pu_program_updated, MyToast.LENGTH_SHORT).show();
                    initMixChannelList();
                    Channel tmpCurChannel = mPlayer.getCurrentChannel();
                    mChnHistory.setCurrent(mCurSourceId, null, tmpCurChannel);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PU_RESCAN_CURRENT_TP: {
                    if (mChnHistory.isRecording()) {
                        mPvrRecordView.stopPvr();
                    }

                    if (mTimeshiftView.isShow()) {
                        mTimeshiftView.stop();
                    }
                    rescanCurrentMultiplex();
                    isCurMultiplexScan = true;

                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_BEGIN: {
                    isDTVScanning = true;
                    setDolbyTagVisibility(View.GONE);
                    if (mPvrRecordView.isShow()) {
                        mPvrRecordView.stopPvr();
                    }
                    if (mTimeshiftView.isShow()) {
                        mTimeshiftView.stop();
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_FINISH: {
                    LogTool.d(LogTool.MSCAN, "sdandby scan over, HI_SVR_EVT_SRCH_FINISH,isSdandbyScanChannel=" + isSdandbyScanChannel);
                    if (isSdandbyScanChannel) {
                        LogTool.d(LogTool.MSCAN, "sdandby scan over, turn to standby");
                        playFirstDvbtChannel();
                        isSdandbyScanChannel = false;
                        //tell launcher to call standby
                        if (SystemProperties.getInt(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_IDLE) == Util.BOOK_TASK_TYPE_WORKING) {
                            SystemProperties.set(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_FINISH + "");
                            goToShutdown();
                        }
                    } else {
                        isDTVScanning = false;
                        if (isCurMultiplexScan) {
                            rescanOver();
                            isCurMultiplexScan = false;
                        }
                    }
                    // reset sortType to default
                    Editor editor = getSharedPreferences(CURRENT_SORT_TYPE, Context.MODE_PRIVATE).edit();
                    editor.putString(CURRENT_SORT_TYPE, "default");
                    editor.apply();
                    //reset end
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SSU_UPDATE: {
                    LogTool.d(LogTool.MPLAY, "get update message source  ");
                    mSsuConfirmDialog.show();
                    break;
                }
                case DTVMessage.HI_ATV_EVT_TIMMING_CHANGED: {
                    refreshInfo();
                    break;
                }
                case DTVMessage.HI_ATV_EVT_PLUGIN: {
                    break;
                }
                case DTVMessage.HI_ATV_EVT_PLUGOUT: {
                    break;
                }
                case DTVMessage.HI_ATV_EVT_PC_ADJ_STATUS: {
                    ShoeVgaAdjustDialog(param1);
                    break;
                }
                case DTVMessage.HI_ATV_EVT_CEC_SELECT_SOURCE: {
                    if (isDTVScanning) {
                        return;
                    }
                    if (mPvrRecordView.isShow() || mTimeshiftView.isShow()) {
                        return;
                    }
                    int sourceId = param1;
                    LogTool.d(LogTool.MPLAY, "HI_ATV_EVT_CEC_SELECT_SOURCE source : " + sourceId);
                    if (mCurSourceId != sourceId) {
                        halApi.changeSource(mCurSourceId, sourceId);
                        mCurSourceId = sourceId;
                        Intent intent = new Intent("android.intent.hisiaction.HisiATV");
                        intent.putExtra("SourceName", sourceId);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mContext.startActivity(intent);
                    }
                    int bookTaskStatus = SystemProperties.getInt(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_IDLE);
                    //待机pvr执行中
                    if (bookTaskStatus == Util.BOOK_TASK_TYPE_WORKING) {
                        //认为power on 了，亮屏开声音
                        SystemProperties.set(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_POWER_ON + "");
                    }
                    break;
                }
                case DTVMessage.HI_ATV_EVT_PLAYER_LOCK_CHANGED: {
                    LogTool.i(LogTool.MPLAY, " LOCK_CHANGE ");
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    list = (ArrayList<Integer>) obj;
                    LogTool.d(LogTool.MPLAY, " LOCK_CHANGE list = " + list);
                    if (list.get(2) == 1) {
                        LogTool.d(LogTool.MPLAY, " LOCK_CHANGE LOCK ");
                        halApi.showCc(false);
                        if (!isPause) {
                            mTipMsgView.show(TipMsgView.TIPMSG_PARENTAL_RATING);
                        }
                    } else {
                        if (mTipMsgView.isShow(TipMsgView.TIPMSG_PARENTAL_RATING)) {
                            mTipMsgView.hide(TipMsgView.TIPMSG_PARENTAL_RATING);
                        }
                        if (halApi.getCcEnable() != 0) {
                            LogTool.d(LogTool.MPLAY, " LOCK_CHANGE SHOW CC ");
                            halApi.showCc(true);
                        }
                    }
                    break;
                }
                case DTVMessage.HI_ATV_EVT_SCAN_FINISH: {
                    // reset sortType to default
                    Editor editor = getSharedPreferences(CURRENT_SORT_TYPE, Context.MODE_PRIVATE).edit();
                    editor.putString(CURRENT_SORT_TYPE, "default");
                    editor.apply();
                    //reset end
                    break;
                }
                case DTVMessage.HI_ATV_EVT_AUDIO_STREAM_TYPE_CHANGE: {
                    LogTool.d(LogTool.MPLAY, "onAudioStreamChanged");
                    showAudioStreamInfo(param1, param2);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_NEW_AUDIO: {
                    LogTool.d(LogTool.MPLAY, "DTV AudioStream Changed");
                    getDTVAudioStreamInfo();
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_STOP: {
                    LogTool.d(LogTool.MPLAY, "DTV AudioStream stoped");
                    dolbyFlag = false;
                    setDolbyTagVisibility(View.GONE);
                    setDRATagVisibility(View.GONE);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EAS: {
                    int alertPriority = param1;
                    LogTool.v(LogTool.MPLAY, "HI_SVR_EVT_EAS come, u8AlertPriority : " + alertPriority);
                    if (alertPriority == 0) {
                        return;
                    }

                    String strEasEventCode = "";
                    strEasEventCode = ((Parcel) obj).readString();
                    LogTool.v(LogTool.MPLAY, "strEasEventCode : " + strEasEventCode);

                    if ("EAT".equals(strEasEventCode)) {
                        closeEAS();
                        return;
                    }

                    String strNatureOfActivationText = "";
                    strNatureOfActivationText = ((Parcel) obj).readString();
                    LogTool.v(LogTool.MPLAY, "strNatureOfActivationText : " + strNatureOfActivationText);

                    int u8AlertMessageTimeRemaining = ((Parcel) obj).readInt();
                    int u8AlertPriority = ((Parcel) obj).readInt();
                    int u16DetailsMajorChannelNumber = ((Parcel) obj).readInt();
                    int u16DetailsMinorChannelNumber = ((Parcel) obj).readInt();

                    LogTool.v(LogTool.MPLAY, "u8AlertMessageTimeRemaining : " + u8AlertMessageTimeRemaining);
                    LogTool.v(LogTool.MPLAY, "u8AlertPriority : " + u8AlertPriority);
                    LogTool.v(LogTool.MPLAY, "u16DetailsMajorChannelNumber : " + u16DetailsMajorChannelNumber);
                    LogTool.v(LogTool.MPLAY, "u16DetailsMinorChannelNumber : " + u16DetailsMinorChannelNumber);

                    String strAlertText = "";
                    strAlertText = ((Parcel) obj).readString();
                    LogTool.v(LogTool.MPLAY, "strAlertText : " + strAlertText);

                    if (TextUtils.isEmpty(strAlertText)) {
                        return;
                    }

                    //show text
                    mEASTextView.setText(strEasEventCode + "  " + strAlertText);
                    mEASTextView.setVisibility(View.VISIBLE);
                    mEASTextView.requestFocus();

                    //change channel
                    int channelID = ((u16DetailsMajorChannelNumber & 0xff) << 16) | u16DetailsMinorChannelNumber;
                    if (0 != channelID) {
                        Channel tempChannel = mChannelManager.getChannelByNo(channelID);
                        if (tempChannel != null) {
                            playChannel(mChnHistory.getCurrentList(mCurSourceId), tempChannel, false);
                            isEASChangedChannel = true;
                        }
                    }
                    //block key
                    isInEASMode = true;
                    getWindow().addPrivateFlags(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                            | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY
                            | Util.HI_PRIVATE_FLAG_INTERCEPT_FUNC_KEY);

                    //count time
                    mHandler.removeMessages(MSG_CLOSE_EAS);
                    mHandler.sendEmptyMessageDelayed(MSG_CLOSE_EAS, u8AlertMessageTimeRemaining * 1000);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_AUDIO_ONLY:
                    LogTool.d(LogTool.MPLAY, "HI_SVR_EVT_AV_AUDIO_ONLY show Radio BG");
                    mRadioBGLinearLayout.setVisibility(View.VISIBLE);
                    break;
                case DTVMessage.HI_SVR_EVT_AV_SIGNAL_STAUTS:
                    LogTool.d(LogTool.MPLAY, "HI_SVR_EVT_AV_SIGNAL_STAUTS " + param1);
                    if (param1 == 1 && !isPause) {
                        halApi.setFullVideo();
                    }
                    break;
                case DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH:
                    LogTool.d(LogTool.MPLAY, "====== HI_ADP_EVT_EPG_SCH_CURR_FREQ_FINISH ======");
                    if (isStandbyScanEpg) {
                        //tell launcher to call standby
                        if (SystemProperties.getInt(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_IDLE) == Util.BOOK_TASK_TYPE_WORKING) {
                            SystemProperties.set(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_FINISH + "");
                            goToShutdown();
                        }
                        isStandbyScanEpg = false;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void subAtvScribeEvent() {
        if (null != mDTV) {
            //mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SIGNAL_STATUS, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_TIMMING_CHANGED, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_PLUGIN, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_PLUGOUT, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_PC_ADJ_STATUS, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_CEC_SELECT_SOURCE, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_PLAYER_LOCK_CHANGED, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_FINISH, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_AUDIO_STREAM_TYPE_CHANGE, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EAS, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_AUDIO_ONLY, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_SIGNAL_STAUTS, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_NEW_AUDIO, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_STOP, dvbPlayListener, 0);
        }
    }

    private void unSubAtvScribeEvent() {
        if (null != mDTV) {
            //mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SIGNAL_STATUS, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_TIMMING_CHANGED, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_PLUGIN, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_PLUGOUT, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_PC_ADJ_STATUS, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_CEC_SELECT_SOURCE, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_PLAYER_LOCK_CHANGED, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_FINISH, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_AUDIO_STREAM_TYPE_CHANGE, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EAS, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_AUDIO_ONLY, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_SIGNAL_STAUTS, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_NEW_AUDIO, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_STOP, dvbPlayListener);
        }
    }

    /**
     * Register message/event send from DTVStack.
     */
    private void subDtvOnlyFrontScribeEvent() {
        LogTool.i(LogTool.MPLAY, "subDtvOnlyFrontScribeEvent start");
        if (null != mDTV) {
            //mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_CA, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PU_RESCAN, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PU_RESCAN_CURRENT_TP, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PU_PROG_UPDATED, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SSU_UPDATE, dvbPlayListener, 0);
        }
    }

    private void unSubDtvOnlyFrontScribeEvent() {
        LogTool.d(LogTool.MPLAY, "unSubDtvOnlyFrontScribeEvent");
        if (null != mDTV) {
            //mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_CA, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PU_RESCAN, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PU_RESCAN_CURRENT_TP, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PU_PROG_UPDATED, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SSU_UPDATE, dvbPlayListener);
        }
    }

    private void subDtvNormalScribeEvent() {
        LogTool.i(LogTool.MPLAY, "subDtvNormalScribeEvent start");
        if (null != mDTV) {
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_CA, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_PLAY_FAILURE, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_PLAY_SUCCESS, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_FRONTEND_STOP, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_FRONTEND_RESUME, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_LOCK_STATUS, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_BEGIN, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_FINISH, dvbPlayListener, 0);

        }
    }

    private void unSubDtvNormalScribeEvent() {
        LogTool.d(LogTool.MPLAY, "unSubDtvNormalScribeEvent");
        if (null != mDTV) {
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_CA, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_PLAY_FAILURE, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_PLAY_SUCCESS, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_FRONTEND_STOP, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_FRONTEND_RESUME, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_LOCK_STATUS, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_BEGIN, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_FINISH, dvbPlayListener);
        }
    }

    private void subBroadcast() {
        IntentFilter bookAlarmFilter = new IntentFilter(CommonValue.DTV_BOOK_ALARM_REMINDE_PLAY);
        mBookAlarmReceiver = new BookAlarmReceiver();
        registerReceiver(mBookAlarmReceiver, bookAlarmFilter);

        IntentFilter bookArriveFilter = new IntentFilter(CommonValue.DTV_BOOK_ALARM_ARRIVE_PLAY);
        mBookArriveReceiver = new BookArriveReceiver();
        registerReceiver(mBookArriveReceiver, bookArriveFilter);

        IntentFilter ewsPlayFilter = new IntentFilter(CommonValue.DTV_EWS_ALARM_PLAY);
        mEwsPlayReceiver = new EwsPlayReceiver();
        registerReceiver(mEwsPlayReceiver, ewsPlayFilter);

        IntentFilter viewShowFilter = new IntentFilter(CommonValue.CHANNEL_LIST_SHOW);
        IntentFilter favViewShowFilter = new IntentFilter(CommonValue.FAV_CHANNEL_LIST_SHOW);
        mViewShowReceiver = new ViewShowReceiver();
        registerReceiver(mViewShowReceiver, viewShowFilter);
        registerReceiver(mViewShowReceiver, favViewShowFilter);

        IntentFilter playDTVFilter = new IntentFilter(CommonValue.PLAY_DTV_ACTION);
        IntentFilter hideRadioBGFilter = new IntentFilter(CommonValue.HIDE_RADIOBG);
        IntentFilter hideDRA = new IntentFilter(CommonValue.HIDE_DRA);
        IntentFilter hideTipView = new IntentFilter(CommonValue.DTV_INTENT_DISMISS_TIP);

        mPlayDTVReceiver = new PlayDTVReceiver();
        registerReceiver(mPlayDTVReceiver, playDTVFilter);
        registerReceiver(mPlayDTVReceiver, hideRadioBGFilter);
        registerReceiver(mPlayDTVReceiver, hideDRA);
        registerReceiver(mPlayDTVReceiver, hideTipView);
    }

    private void unSubBroadcast() {
        unregisterReceiver(mBookAlarmReceiver);
        mBookAlarmReceiver = null;
        unregisterReceiver(mBookArriveReceiver);
        mBookArriveReceiver = null;
        unregisterReceiver(mEwsPlayReceiver);
        mEwsPlayReceiver = null;
        unregisterReceiver(mViewShowReceiver);
        mViewShowReceiver = null;
        unregisterReceiver(mPlayDTVReceiver);
        mPlayDTVReceiver = null;
    }

    private int changeServiceType(EnTVRadioFilter enTvOrRadio) {
        ArrayList<ChannelList> groupsList = null;
        ChannelList tempList = null;
        boolean bFound = false;
        ChannelList tmpCurrentList = mChnHistory.getCurrentList(halApi.EnumSourceIndex.SOURCE_AUTO);
        mChannelManager.setChannelServiceTypeMode(enTvOrRadio);
        groupsList = (ArrayList<ChannelList>) mChannelManager.getUseGroups();

        if ((null == groupsList) || (groupsList.isEmpty())) {
            return -1;
        }

        int i = 0;
        for (i = 0; i < groupsList.size(); i++) {
            if (null != tmpCurrentList) {
                if (tmpCurrentList.getListName().equals(groupsList.get(i).getListName())) {
                    bFound = true;
                    break;
                }
            }
        }

        if (bFound) {
            tempList = groupsList.get(i);
            if (tempList.getChannelCount() == 0) {
                tempList = groupsList.get(0);
            }
        } else {
            tempList = groupsList.get(0);
        }

        if ((null != tempList) && (0 < tempList.getChannelCount())) {
            Channel tmpPreChannel = mChnHistory.getPreTvRadioChn(mChannelManager.getChannelServiceTypeMode());
            if (null == tmpPreChannel) {
                playChannel(tempList, tempList.getChannelByIndex(0), true);
            } else {
                playChannel(mChnHistory.getPreTvRadioList(mChannelManager.getChannelServiceTypeMode()), tmpPreChannel, true);
            }
        } else {
            return -1;
        }
        return 0;
    }

    /**
     * Switch between radio, Data and TV
     */
    private void toggleTvRadio() {
        int ret = 0;
        EnTVRadioFilter curPlayMode = mChannelManager.getChannelServiceTypeMode();
        if (EnTVRadioFilter.TV == curPlayMode) {
            if (toggleToRadioList() != 0) {
                if (toggleToDataList() != 0) {
                    mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.TV);
                    MyToast.makeText(this, R.string.play_no_radio_and_data, MyToast.LENGTH_SHORT).show();
                }
            }
        } else if (EnTVRadioFilter.RADIO == curPlayMode) {
            if (toggleToDataList() != 0) {
                if (toggleToTvList() != 0) {
                    mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.RADIO);
                    MyToast.makeText(this, R.string.play_no_data_and_tv, MyToast.LENGTH_SHORT).show();
                }
            }
        } else if (EnTVRadioFilter.DATA == curPlayMode) {
            if (toggleToTvList() != 0) {
                if (toggleToRadioList() != 0) {
                    mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.DATA);
                    MyToast.makeText(this, R.string.play_no_tv_and_radio, MyToast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * to TV
     *
     * @return 0 means the switch is successful, the other fails.
     */
    private int toggleToTvList() {
        int ret = changeServiceType(EnTVRadioFilter.TV);
        if (0 != ret) {
            mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.DATA);
        } else {
            mChannelListView.show();
        }
        return ret;
    }

    /**
     * to DATA
     *
     * @return 0 means the switch is successful, the other fails.
     */
    private int toggleToDataList() {
        int ret = changeServiceType(EnTVRadioFilter.DATA);
        if (0 != ret) {
            mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.RADIO);
        } else {
            mChannelListView.show();
        }
        return ret;
    }

    /**
     * to RADIO
     *
     * @return 0 means the switch is successful, the other fails.
     */
    private int toggleToRadioList() {
        int ret = changeServiceType(EnTVRadioFilter.RADIO);
        if (0 != ret) {
            mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.TV);
        } else {
            mChannelListView.show();
        }
        return ret;
    }

    private class RescanTp {
        public int Freq;
        public EnModulation Modulation;
        public int Bandwidth;
    }

    private List<RescanTp> mlstRescanTp = new ArrayList<RescanTp>();

    private void startSearch() {
        DVBTNetwork dvbtNetwork = null;
        int i = 0;

        Channel currentChannel = ChannelHistory.getInstance().getCurrentChn(mCurSourceId);
        if (null == currentChannel) {
            return;
        }

        dvbtNetwork = (DVBTNetwork) currentChannel.getBelongNetwork();

        if (null != dvbtNetwork) {
            List<Network> lstNetwork = new ArrayList<Network>();
            lstNetwork.add(dvbtNetwork);
            ((DTVApplication) getApplication()).setScanParam(lstNetwork);
        } else {
            LogTool.w(LogTool.MSCAN, "dvbtNetwork is null");
            return;
        }

        ScanType type = new ScanType();
        type.setBaseType(ScanType.EnBaseScanType.SINGLE_MULTI);

        type.enableNit(false);
        type.setFTAFilter(EnScrambleFilter.ALL);
        type.setTVRadioFilter(EnTVRadioFilter.ALL);

        ((DTVApplication) getApplication()).setScanType(dvbtNetwork.getNetworkType(), type);

        List<Multiplex> lstMultiplex = new ArrayList<Multiplex>();

        DVBTChannelDot NewMultiplex = null;

        for (i = 0; i < mlstRescanTp.size(); i++) {
            RescanTp rescanTp = mlstRescanTp.get(i);

            NewMultiplex = (DVBTChannelDot) dvbtNetwork.createTmpMultiplex();
            NewMultiplex.setBandWidth(rescanTp.Bandwidth);
            NewMultiplex.setModulation(rescanTp.Modulation);
            NewMultiplex.setFrequency(rescanTp.Freq);
            LogTool.d(LogTool.MSCAN, "this mtp is new creat = " + NewMultiplex.getID() + ",freq=" + NewMultiplex.getFrequency() + " bandwidth = "
                    + NewMultiplex.getBandWidth());

            lstMultiplex.add(NewMultiplex);
        }

        LogTool.d(LogTool.MSCAN, "tp size =" + lstMultiplex.size());
        dvbtNetwork.setScanMultiplexes(lstMultiplex);

        //CommonDef.startActivityEx(this, new Intent(this, AutoScanActivity.class));
    }

    private void catchPuDialog() {
        if (null == mPuConfirmDialog) {
            String tip = getResources().getString(R.string.str_pu_rescan_tip);
            String tip_title = getResources().getString(R.string.str_pu_program_updated);
            mPuConfirmDialog = new ConfirmDialog(this, R.style.DIM_STYLE, tip_title, tip, 1f);
            mPuConfirmDialog.setConfirmDialogListener(new OnConfirmDialogListener() {
                @Override
                public void onCheck(int which) {
                    if (which == ConfirmDialog.OnConfirmDialogListener.OK) {
                        startSearch();
                    }

                    mPuConfirmDialog.cancel();
                }
            });
        }

        mPuConfirmDialog.show();
    }

    private void catchNoStorageDeviceDialog() {
        if (null == mUsbConfirmDialog) {
            String tip = getResources().getString(R.string.str_pvr_not_support_usb);
            String tip_title = getResources().getString(R.string.str_pvr_not_support_tip);
            mUsbConfirmDialog = new ConfirmDialog(this, R.style.DIM_STYLE, tip_title, tip, 1f);
            mUsbConfirmDialog.setConfirmDialogListener(new OnConfirmDialogListener() {
                @Override
                public void onCheck(int which) {
                    mUsbConfirmDialog.cancel();
                }
            });
        }

        mUsbConfirmDialog.show();
    }

    public void puRescan(int tpCount, Object obj) {
        int i;
        int modulation = 0;

        mlstRescanTp.clear();

        for (i = 0; i < tpCount; i++) {
            RescanTp item = new RescanTp();
            item.Freq = ((Parcel) obj).readInt();

            modulation = ((Parcel) obj).readInt();
            if (0 == modulation) {
                item.Modulation = EnModulation.QPSK;
            } else if (1 == modulation) {
                item.Modulation = EnModulation.QAM16;
            } else if (2 == modulation) {
                item.Modulation = EnModulation.QAM64;
            }

            item.Bandwidth = ((Parcel) obj).readInt();
            item.Bandwidth = item.Bandwidth * 1000;

            mlstRescanTp.add(item);
        }

        catchPuDialog();
    }

    public void rescanCurrentMultiplex() {
        ScanType type = new ScanType();
        type.setBaseType(ScanType.EnBaseScanType.SINGLE_MULTI);
        type.enableNit(false);
        type.setFTAFilter(EnScrambleFilter.ALL);
        type.setTVRadioFilter(EnTVRadioFilter.ALL);

        Channel currentChannel = ChannelHistory.getInstance().getCurrentChn(mCurSourceId);
        ChannelList currentChannelList = ChannelHistory.getInstance().getCurrentList(mCurSourceId);
        if ((null == currentChannel) || (null == currentChannelList)) {
            return;
        }

        mCurNetwork = currentChannel.getBelongNetwork();
        mCurMultiplex = currentChannel.getBelongMultiplexe();
        List<Multiplex> lstMultiplex = new ArrayList<Multiplex>();

        if ((null == mCurNetwork) || (null == mCurMultiplex) || (null == lstMultiplex)) {
            LogTool.w(LogTool.MPLAY, "get current tp info failed");
            return;
        }

        menTVRadioFilter = currentChannelList.getFilter().getGroupType();

        lstMultiplex.add(mCurMultiplex);
        mCurNetwork.setScanMultiplexes(lstMultiplex);
        dismissView();

        if (null == mRescanProgressDialog) {
            mRescanProgressDialog = new ProgressDialog(this) {
                public boolean onKeyDown(int keyCode, KeyEvent event) {
                    //同频点切换码流时短暂屏蔽source键,MTS键
                    if ((keyCode == KeyValue.DTV_KEYVALUE_SOURCE)
                            || (keyCode == KeyValue.DTV_KEYVALUE_AUDIO)) {
                        return true;
                    }
                    return super.onKeyDown(keyCode, event);
                }

            };
        }
        mRescanProgressDialog.setTitle(R.string.play_channel_search);
        mRescanProgressDialog.setMessage(getResources().getString(R.string.play_wait_dialog_message));
        mRescanProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mRescanProgressDialog.show();

        mChannelManager.deleteChannelsBySIElement(mCurMultiplex);

        mPlayer.releaseResource(0);

        mCurNetwork.startScan(type);

        return;
    }

    public void rescanOver() {
        if (null != mCurNetwork) {
            LogTool.d(LogTool.MPLAY, "HI_SVR_EVT_SRCH_FINISH");

            mCurNetwork.stopScan(false);
            mChannelManager.rebuildAllGroup();
            mNetworkManager.saveNetworks();

            Channel tempChannel = null;
            ChannelFilter tempFilter = new ChannelFilter();
            tempFilter.setSIElement(mCurMultiplex);
            tempFilter.setGroupType(menTVRadioFilter);
            ChannelList tempList = mChannelManager.getChannelList(tempFilter);
            /*move resumeResource to front of playChannel to fix play problem*/
            mPlayer.resumeResource();
            if (null != tempList) {
                tempChannel = tempList.getChannelByIndex(0);
                if (null != tempChannel) {
                    playChannel(null, tempChannel, true);
                    mPlayer.changeChannel(tempChannel);
                }
            }

            if (null == tempChannel) {
                tempFilter.setGroupType(EnTVRadioFilter.ALL);
                tempList = mChannelManager.getChannelList(tempFilter);
                if (null != tempList) {
                    tempChannel = tempList.getChannelByIndex(0);
                    if (null != tempChannel) {
                        playChannel(null, tempChannel, true);
                        mPlayer.changeChannel(tempChannel);
                    }
                }
            }
            if (null != mRescanProgressDialog) {
                mRescanProgressDialog.dismiss();
            }
        }

        return;
    }

    /* 启动录制和时移的条件判断。加扰节目，无信号，前端停播时，不允许启动   */
    private boolean isCanRec(Channel chn) {
        EnTVRadioFilter enTVRadioFilter = mChannelManager.getChannelServiceTypeMode();
        if (enTVRadioFilter != null) {
            int type = enTVRadioFilter.getValue();
            LogTool.d(LogTool.MPLAY, "mCurSourceId:" + mCurSourceId + ";Type" + type);
            if (type == SERVICE_TYPEMODE_DATA) {
                MyToast.makeText(this, R.string.pvr_rec_nodata, MyToast.LENGTH_LONG).show();
                return false;
            }
        }
        UsbManagerPrivate usbManagerPrivate = new UsbManagerPrivate(this.getApplicationContext());
        String mRecordPath = mDtvConfig.getString("au8RecordFilePath", "");
        LogTool.d(LogTool.MPLAY, "mRecordPath=" + mRecordPath);
        if (!usbManagerPrivate.hasExternalStorgeDevice() || mRecordPath.equals("/storage/emulated/0")) {
            catchNoStorageDeviceDialog();
            MyToast.makeText(this, R.string.pvr_record_path_error, MyToast.LENGTH_LONG).show();
            return false;
        }
        if (halApi.isATSCSource(mCurSourceId)) {
            MyToast.makeText(this, R.string.pvr_timeshift_start_ATSC, MyToast.LENGTH_LONG).show();
            return false;
        }
        if (chn.isScramble() || mTipMsgView.isShow(TipMsgView.TIPMSG_NOSIGNAL)
                || mTipMsgView.isShow(TipMsgView.TIPMSG_UNSUPPORT)
                || mTipMsgView.isShow(TipMsgView.TIPMSG_FRONTEND_ERROR)) {
            MyToast.makeText(this, R.string.pvr_timeshift_start_fail, MyToast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean isParentalRatingNeedBlock(Channel channel) {
        int parentalRating = 0;
        String strCountry = mDtvConfig.getString(CommonValue.COUNTRY_CODE_KEY, "");
        EPGEvent epgPresentEvent = null;
        boolean bEqualBlock = false;
        boolean bNeedBlock = false;

        if (null == channel) {
            LogTool.d(LogTool.MPLAY, "isParentalRatingNeedBlock TIPMSG_PARENTAL_RATING null == channel");
            return false;
        }

        if (null == mPCManager) {
            LogTool.d(LogTool.MPLAY, "isParentalRatingNeedBlock TIPMSG_PARENTAL_RATING null == mPCManager");
            return false;
        }
        int userParentalRating = mPCManager.getParentLockAge();

        parentalRating = mPCManager.getParental(channel.getChannelID());
        LogTool.d(LogTool.MPLAY, "isParentalRatingNeedBlock TIPMSG_PARENTAL_RATING userParentalRating:" + userParentalRating + " parentalRating:" + parentalRating + " strCountry:" + strCountry);
        if ((0 == parentalRating) || (0 == userParentalRating)) {
            return false;
        }

        if (!TextUtils.isEmpty(strCountry)) {
            if (strCountry.equalsIgnoreCase("MYS") || strCountry.equalsIgnoreCase("IDN") || strCountry.equalsIgnoreCase("NZL")
                    || strCountry.equalsIgnoreCase("SGP") || strCountry.equalsIgnoreCase("THA") || strCountry.equalsIgnoreCase("VNM")
                    || strCountry.equalsIgnoreCase("BRA") || strCountry.equalsIgnoreCase("RUS")) {
                bEqualBlock = true;
            } else {
                bEqualBlock = false;
            }
        }
        if (parentalRating > userParentalRating) {
            bNeedBlock = true;
        } else if ((parentalRating == userParentalRating) && (bEqualBlock)) {
            bNeedBlock = true;
        }

        return bNeedBlock;
    }

    private class BookAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            int channelID = bundle.getInt(CommonValue.DTV_BOOK_CHANNEL_ID);
            Channel bookedChannel = mChannelManager.getChannelByID(channelID);
            if (null == bookedChannel) {
                return;
            }
            dismissView();

            LogTool.d(LogTool.MPLAY, "call change channel.");

            EnNetworkType bookNetworkType = bookedChannel.getNetworkType();
            LogTool.d(LogTool.MPLAY, "BookAlarmReceiver bookNetworkType :" + bookNetworkType +
                    ", cur source : " + mCurSourceId);
            int sourceId = mapBookType2SrcId.get(bookNetworkType);
            if (mapBookType2SrcId.containsKey(bookNetworkType) && mCurSourceId != sourceId) {
                halApi.setSourceHolder();
                halApi.changeSource(mCurSourceId, sourceId);
                mCurSourceId = sourceId;
            }
            mPlayer.resumeResource();
            playChannel(mChnHistory.getCurrentList(mCurSourceId), bookedChannel, true);
            if (mCurSourceId != sourceId) {
                setPipSurfaceToDisplay();
            }
        }
    }

    private class BookArriveReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            int channelID = bundle.getInt(CommonValue.DTV_BOOK_CHANNEL_ID);
            int type = bundle.getInt(CommonValue.DTV_BOOK_TYPE);
            int duration = bundle.getInt(CommonValue.DTV_BOOK_DURATION);
            int mBookTastID = bundle.getInt(CommonValue.DTV_BOOK_ID);
            Channel bookedChannel = mChannelManager.getChannelByID(channelID);
            if (null == bookedChannel) {
                return;
            }
            dismissView();

            EnNetworkType bookNetworkType = bookedChannel.getNetworkType();
            LogTool.d(LogTool.MPLAY, "BookArriveReceiver bookNetworkType :" + bookNetworkType +
                    ", cur source : " + mCurSourceId);
            int sourceId = mapBookType2SrcId.get(bookNetworkType);
            if (mapBookType2SrcId.containsKey(bookNetworkType) && mCurSourceId != sourceId) {
                halApi.setSourceHolder();
                halApi.changeSource(mCurSourceId, sourceId);
                mCurSourceId = sourceId;
            }

            LogTool.d(LogTool.MPLAY, "call change channel.");
            mPlayer.resumeResource();
            playChannel(mChnHistory.getCurrentList(mCurSourceId), bookedChannel, true);
            if (mCurSourceId != sourceId) {
                setPipSurfaceToDisplay();
            }
            if (0 == type) {
                BookTask currentBookTask = mDTV.getBookManager().getTaskByID(mBookTastID);
                assert (null != currentBookTask);
                assert (null != currentBookTask.getStartDate());
                isBookArriving = true;
                processLock(mCurSourceId, mChnHistory.getCurrentChn(mCurSourceId));
                mPvrRecordView.startPvr(bookedChannel, duration, currentBookTask);

                //add temp because of pvr not support
                /*
                if (currentBookTask != null && EnTaskCycle.ONETIME == currentBookTask.getCycle())
                {
                    mDTV.getBookManager().deleteTask(currentBookTask);
                }

                MyToast.makeText(mContext, mContext.getResources().getString(R.string.str_pvr_not_support), MyToast.LENGTH_LONG).show();
                */
            }
        }
    }

    private class EwsPlayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            int channelID = bundle.getInt(CommonValue.DTV_EWS_CHANNEL_ID);
            LogTool.d(LogTool.MPLAY, "EwsPlayReceiver channelID=" + channelID);
            if (0 != channelID) {
                Channel tempChannel = mChannelManager.getChannelByID(channelID);
                mChnHistory.setCurrent(mCurSourceId, null, tempChannel);
            }
        }
    }

    private class ViewShowReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent data) {
            if (data.getAction().equals(CommonValue.CHANNEL_LIST_SHOW)) {
                mChannelListView.show(ChannelListView.EnEntranceType.ALL_GROUP);
            } else if (data.getAction().equals(CommonValue.FAV_CHANNEL_LIST_SHOW)) {
                mChannelListView.directShowFavlist();
            }
        }
    }

    private class PlayDTVReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent data) {
            if (data.getAction().equals(CommonValue.PLAY_DTV_ACTION)) {
                LogTool.d(LogTool.MPLAY, "data.getAction() :" + data.getAction());
                playDTV();
            } else if (data.getAction().equals(CommonValue.HIDE_RADIOBG)) {
                LogTool.d(LogTool.MPLAY, "hide radio bg");
                mRadioBGLinearLayout.setVisibility(View.GONE);
            } else if (data.getAction().equals(CommonValue.HIDE_DRA)) {
                LogTool.d(LogTool.MPLAY, "hide DRA ");
                setDRATagVisibility(View.GONE);
                setDTSStreamTagVisibility(View.GONE);
            } else if (data.getAction().equals(CommonValue.DTV_INTENT_DISMISS_TIP)) {
                LogTool.d(LogTool.MPLAY, "hide tip ");
                dismissView();
                mTipMsgView.hideAll();
            }
        }
    }

    public void ShoeVgaAdjustDialog(int flag) {
        if (mVGAAdjustingDialog != null && mVGAAdjustingDialog.isShowing()) {
            mVGAAdjustingDialog.dismiss();
        }

        mVGAAdjustingDialog = new VGAAdjustingDialog(this, flag);
        Window window = mVGAAdjustingDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.width = LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        mVGAAdjustingDialog.show();
    }

    private void setSignalFlag(boolean flag) {
        LogTool.i(LogTool.MPLAY, "setSignalFlag : " + flag + " isPause = " + isPause);
        if (flag) {
            mTipMsgView.hide(TipMsgView.TIPMSG_NOSIGNAL);
        } else {
            if (!isPause) {
                mTipMsgView.show(TipMsgView.TIPMSG_NOSIGNAL);
                setDRATagVisibility(View.GONE);
                setDTSStreamTagVisibility(View.GONE);
            }
        }
    }

    private void setSignalUnSupportFlag(boolean flag) {
        LogTool.i(LogTool.MPLAY, "setSignalUnSupportFlag : " + flag + " isPause = " + isPause);
        if (flag && !isPause) {
            if (mTipMsgView.isShow(TipMsgView.TIPMSG_NOSIGNAL)) {
                mTipMsgView.hide(TipMsgView.TIPMSG_NOSIGNAL);
            }
            mTipMsgView.show(TipMsgView.TIPMSG_UNSUPPORT);
            setDRATagVisibility(View.GONE);
        } else {
            mTipMsgView.hide(TipMsgView.TIPMSG_UNSUPPORT);
        }
    }

    private void refreshInfo() {
        mCurSourceId = halApi.getCurSourceID();
        if (mSignalShow.isShow()) {
            mSignalShow.hide();
        }

        if (mChnInfoView.isShow()) {
            mChnInfoView.hide();
        }


        if (!halApi.isTVSource(mCurSourceId)) {
            mSignalShow.refreshPanelInfo(mCurSourceId);
        }
    }

    private boolean checkSourceLocked(int sourceId) {
        // 切源锁开关关闭
        if (!halApi.getLockEnable(halApi.EnumLockSwitch.SOURCE_LOCK)) {
            return false;
        }

        // 切源锁已解锁过
        if (halApi.getPwdStatus(halApi.EnumLockType.SOURCE_LOCK_TYPE)) {
            return false;
        }

        return halApi.getSrcLockEnable(sourceId);
    }

    private boolean checkProgramLocked(int sourceId, Channel channel) {
        LogTool.d(LogTool.MPLAY, "isBookArriving" + isBookArriving);
        if (null == channel) {
            return false;
        }
        // 非TV源
        if (!halApi.isTVSource(sourceId)) {
            return false;
        }

        // 频道锁开关关闭
        int programLockTag = mDtvConfig.getInt(CommonValue.PROGRAM_LOCK, CommonValue.PROGRAM_LOCK_CLOSE);
        if (CommonValue.PROGRAM_LOCK_OPEN != programLockTag) {
            return false;
        }

        // 频道锁已解锁过
        if (halApi.getPwdStatus(halApi.EnumLockType.PROGRAM_LOCK_TYPE)) {
            return false;
        }

        if (mTipMsgView.isShow(TipMsgView.TIPMSG_SOURCE_LOCK)) {
            return false;
        }

        if (channel.getTag(EnTagType.LOCK)) {
            return true;
        }
        return false;
    }

    private boolean checkDtvParentLocked(int sourceId, Channel channel) {
        if (null == channel) {
            return false;
        }

        // 非TV源
        if (!halApi.isTVSource(sourceId)) {
            return false;
        }

        // 父母锁已解锁过
        if (halApi.getPwdStatus(halApi.EnumLockType.PARENTAL_LOCK_TYPE)) {
            LogTool.d(LogTool.MPLAY, "getPwdStatus TIPMSG_PARENTAL_RATING ");
            return false;
        }

        if (isParentalRatingNeedBlock(channel)) {
            return true;
        }

        return false;
    }

    private boolean processLock(int sourceId, Channel channel) {
        LogTool.d(LogTool.MPLAY, " isBookArriving= " + isBookArriving);
        if (isBookArriving) {
            isBookArriving = false;
            mTipMsgView.setBookArrivingStatus();
            playChannel(mChnHistory.getCurrentList(sourceId), channel, false);
            return false;
        }
        // 检测切源锁
        if (checkSourceLocked(sourceId)) {
            if (mTipMsgView.isShow(TipMsgView.TIPMSG_NOSIGNAL)) {
                mTipMsgView.hide(TipMsgView.TIPMSG_NOSIGNAL);
            }
            mTipMsgView.show(TipMsgView.TIPMSG_SOURCE_LOCK);
            lockTV();
            LogTool.d(LogTool.MPLAY, " processLock playChannel TIPMSG_SOURCE_LOCK ");
            return true;
        }

        // channel为null情况不再检测频道锁和父母锁
        if (null == channel) {
            return false;
        }

        // 检测频道锁
        if (checkProgramLocked(sourceId, channel)) {
            mTipMsgView.show(TipMsgView.TIPMSG_PROGRAM_LOCK_DIALOG);
            lockTV();
            LogTool.i(LogTool.MPLAY, "processLock  playChannel TIPMSG_PROGRAM_LOCK_DIALOG ");
            return true;
        }

        // 检测父母锁
        if (EnNetworkType.RF != channel.getNetworkType()) {
            if (checkDtvParentLocked(sourceId, channel)) {
                mTipMsgView.show(TipMsgView.TIPMSG_PARENTAL_RATING);
                lockTV();
                LogTool.i(LogTool.MPLAY, " processLock playChannel DTV TIPMSG_PARENTAL_RATING ");
                return true;
            }
        } else {
            if (halApi.isCurrentLocked().get(2) == 1) {
                halApi.showCc(false);
                mTipMsgView.show(TipMsgView.TIPMSG_PARENTAL_RATING);
                LogTool.d(LogTool.MPLAY, "processLock  playChannel ATV TIPMSG_PARENTAL_RATING ");
                return true;
            }
        }

        return false;
    }

    public void lockTV() {
        if (halApi.isDTVSource(mCurSourceId)) {
            stopAvplay();
        }
    }

    public int getAutoSleepTime() {
        int delay = Settings.System.getInt(mContext.getContentResolver(), Util.SETTING_AUTO_SLEEP_TIME, 0);
        return delay * Util.MIN_TO_MSECOND;
    }

    private void showNoSignalStatus(int status) {
        LogTool.d(LogTool.MSERVICE, "MSG_NO_NOSIGNAL signalStatus:" + status + " autoSleepTime:" + autoSleepTime);
        if (status == halApi.EnumSignalStat.SIGSTAT_NOSIGNAL && autoSleepTime > 0 && !isPause) {
            mHandler.removeMessages(MSG_SHOW_COUNT_TIME_DIALOG);
            mHandler.sendEmptyMessageDelayed(MSG_SHOW_COUNT_TIME_DIALOG, autoSleepTime - Util.NOSIGNAL_TIME_SHOWUI * 1000);
        } else {
            mHandler.removeMessages(MSG_SHOW_COUNT_TIME_DIALOG);
        }
    }

    private void showCountTimeDialog() {
        mCountTimeText = new TextView(this.getApplicationContext());
        mCountTimeText.setTextSize(25);
        mCountTimeText.setBackgroundResource(R.drawable.channellist_bg);
        mCountTimeText.setTextColor(Color.WHITE);
        mCountTimeText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mCountTimeText.setGravity(Gravity.CENTER);
        int padding = (int) getResources().getDimension(R.dimen.dimen_50px);
        mCountTimeText.setPadding(padding, 0, padding, 0);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams pm = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout.addView(mCountTimeText, pm);
        layout.setGravity(Gravity.CENTER);

        mCountTimeDialog = new AlertDialog.Builder(this.getApplicationContext(), R.style.Translucent_NoTitle_Noframe).setView(layout).create();
        Window dialogWindow = mCountTimeDialog.getWindow();
        LayoutParams lp = dialogWindow.getAttributes();
        lp.y = (int) getResources().getDimension(R.dimen.dimen_390px);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mCountTimeDialog.show();
        mCountTime = Util.NOSIGNAL_TIME_SHOWUI;
        mHandler.sendEmptyMessage(MSG_COUNT_TIME_DEC);


        mCountTimeDialog.setOnDismissListener(dismissListener);
        mCountTimeDialog.setOnKeyListener(keyListener);
    }

    DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            if (mCountTime > 0) {
                mHandler.removeMessages(MSG_COUNT_TIME_DEC);
            }
            autoSleepTime = getAutoSleepTime();
            int status = halApi.getSignalStatus();
            showNoSignalStatus(status);
        }
    };

    DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
            // TODO Auto-generated method stub
            mCountTimeDialog.dismiss();
            return false;
        }
    };

    public void goToShutdown() {
        int source = halApi.getSelectSourceID();
        if ((halApi.isHDMISource(source)
                && halApi.getSrcWakeupEnable(halApi.EnumSourceIndex.SOURCE_HDMI1))
                || (source == halApi.EnumSourceIndex.SOURCE_VGA
                && halApi.getSrcWakeupEnable(halApi.EnumSourceIndex.SOURCE_VGA))) {
            halApi.setNoSignalSuspend(source);
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (SystemProperties.get(("persist.prop.suspend.mode")).equals("lightsleep")) {
            LogTool.d(LogTool.MSERVICE, "powersave suspend");
            pm.setPowerSaveMode(true);
        } else if (SystemProperties.get("persist.prop.suspend.mode").equals("str")) {
            LogTool.d(LogTool.MSERVICE, "STR suspend");
            pm.goToSleep(SystemClock.uptimeMillis());
        } else if (SystemProperties.get("persist.prop.suspend.mode").equals("shutdown")) {
            LogTool.d(LogTool.MSERVICE, "shut down");
            Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
            intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        LogTool.d(LogTool.MSERVICE, "goToShutdown power off!");
    }

    private void registerContentObservers() {
        Uri autoSleepUri = Settings.System.getUriFor(Util.SETTING_AUTO_SLEEP_TIME);
        mContext.getContentResolver().registerContentObserver(autoSleepUri, true, mAutoSleepSettingProviderObserver);
        Uri dolbyOnOffUri = Settings.System.getUriFor(Util.SETTING_DOLBY_ONOFF);
        mContext.getContentResolver().registerContentObserver(dolbyOnOffUri, true, mDolbyProviderObserver);
    }

    public void playDTV() {
        LogTool.d(LogTool.MPLAY, "playDTV");
        mDTV.prepareDTV();
        mPlayer.resumeResource();
        playChannel(mChnHistory.getCurrentList(mCurSourceId),
                mChnHistory.getCurrentChn(mCurSourceId), false);
        halApi.setFullVideo();
    }

    public void gotoFullPlayRecordFile(boolean flag) {
        mGotoFullPlayRecordFile = flag;
        mPlayer.stop(EnStopType.BLACKSCREEN);
        mPlayer.releaseResource(0);
    }

    public void showVideo(boolean show) {
        LogTool.d(LogTool.MPLAY, "showVideo(" + show + ")");
        mPlayer.showVideo(show);
    }

    public void closeEAS() {
        LogTool.v(LogTool.MPLAY, "closeEAS");
        //finish key block
        isInEASMode = false;
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.privateFlags &= ~(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY
                | Util.HI_PRIVATE_FLAG_INTERCEPT_FUNC_KEY);
        window.addPrivateFlags(lp.privateFlags);

        //change to pre channel
        if (isEASChangedChannel) {
            returnPreviousChannel();
        }
        isEASChangedChannel = false;

        //hide text
        mEASTextView.setVisibility(View.GONE);
    }

    private void fullScanDvbt() {
        LogTool.d(LogTool.MSCAN, "fullScanDvbt");
        if (mChnHistory.isRecording()) {
            mPvrRecordView.stopPvr();
        }

        if (mPvrRecordView.isShow()) {
            mPvrRecordView.hideSetTimeView();
        }

        if (mTimeshiftView.isShow()) {
            mTimeshiftView.stop();
        }

        ScanType type = new ScanType();
        type.setBaseType(ScanType.EnBaseScanType.AUTO_FULL);
        type.enableNit(false);
        type.setTVRadioFilter(EnTVRadioFilter.ALL);

        /* Get DVBT network. */
        mNetworkManager.setCurrentNetworkType(EnNetworkType.TERRESTRIAL);

        /* Start DVBT scan. */
        mCurNetwork = mNetworkManager.getNetworks(EnNetworkType.TERRESTRIAL).get(0);
        dismissView();

        mChannelManager.deleteChannelsBySIElement(mCurMultiplex);

        mPlayer.releaseResource(0);

        mCurNetwork.startScan(type);
        isSdandbyScanChannel = true;
    }

    public void scanATSCFreq(EnNetworkType networkType, int freq) {
        if (mChnHistory.isRecording()) {
            mPvrRecordView.stopPvr();
        }

        if (mPvrRecordView.isShow()) {
            mPvrRecordView.hideSetTimeView();
        }

        if (mTimeshiftView.isShow()) {
            mTimeshiftView.stop();
        }

        ScanType type = new ScanType();
        type.setBaseType(ScanType.EnBaseScanType.SINGLE_MULTI);

        /* Get ATSC network. */
        mNetworkManager.setCurrentNetworkType(networkType);

        /* Start ATSC scan. */
        mCurNetwork = mNetworkManager.getNetworks(networkType).get(0);
        mCurMultiplex = mCurNetwork.createTmpMultiplex();
        mCurMultiplex.setFrequency(freq);
        startNumberScan(type);
    }

    private void startNumberScan(ScanType type) {
        List<Multiplex> lstMultiplex = new ArrayList<>();
        menTVRadioFilter = EnTVRadioFilter.ALL;
        lstMultiplex.add(mCurMultiplex);
        mCurNetwork.setScanMultiplexes(lstMultiplex);
        dismissView();
        TaskUtil.post(new Runnable() {
            Tuner tuner = mCurNetwork.getTuner();

            @Override
            public void run() {
                tuner.connect(mCurMultiplex, 0, false);
            }
        });

        if (null == mRescanProgressDialog) {
            mRescanProgressDialog = new ProgressDialog(this);
        }
        mRescanProgressDialog.setTitle(R.string.play_channel_search);
        mRescanProgressDialog.setMessage(getResources().getString(R.string.play_wait_dialog_message));
        mRescanProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mRescanProgressDialog.show();

        mChannelManager.deleteChannelsBySIElement(mCurMultiplex);

        mPlayer.releaseResource(0);

        mCurNetwork.startScan(type);
        isCurMultiplexScan = true;
    }

    public void scanISDBTFreq(EnNetworkType networkType, int majorNum) {
        if (mChnHistory.isRecording()) {
            mPvrRecordView.stopPvr();
        }

        if (mPvrRecordView.isShow()) {
            mPvrRecordView.hideSetTimeView();
        }

        if (mTimeshiftView.isShow()) {
            mTimeshiftView.stop();
        }
        ScanType type = new ScanType();
        type.setBaseType(ScanType.EnBaseScanType.SINGLE_MULTI);
        // get ISDBT network
        mNetworkManager.setCurrentNetworkType(networkType);
        mCurNetwork = mNetworkManager.getNetworks(networkType).get(0);
        mCurMultiplex = mCurNetwork.createTmpMultiplex();
        // ISDBT frequency is between 7-69
        if (null != mCurNetwork.getPresetMultiplexes()) {
            mCurMultiplex.setFrequency(mCurNetwork.getPresetMultiplexes().get(majorNum - 7).getFrequency());
        }
        startNumberScan(type);
    }

    private void playFirstDvbtChannel() {
        if (null != mCurNetwork) {
            LogTool.d(LogTool.MPLAY, "playFirstDvbtChannel");

            mCurNetwork.stopScan(false);
            mChannelManager.rebuildAllGroup();
            mNetworkManager.saveNetworks();

            Channel tempChannel = null;
            ChannelFilter tempFilter = new ChannelFilter();
            tempFilter.setGroupType(menTVRadioFilter);
            ChannelList tempList = mChannelManager.getChannelList(tempFilter);
            if (null != tempList) {
                tempChannel = tempList.getChannelByIndex(0);
                if (null != tempChannel) {
                    mPlayer.resumeResource();
                    playChannel(null, tempChannel, true);
                    Settings.System.putInt(getContentResolver(), Util.SETTING_NEW_SERVICE_FOUND, 1);
                }
            }
            LogTool.d(LogTool.MSCAN, "tempList = " + tempList + "; tempChannel = " + tempChannel);
        }
    }

    private void checkEitTimeout() {
        TaskUtil.post(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                while (SystemProperties.getInt(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_IDLE) == Util.BOOK_TASK_TYPE_WORKING) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime >= 2 * 60 * 1000) {
                        SystemProperties.set(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_FINISH + "");
                        LogTool.w(LogTool.MEPG, "Eit timeout goToShutdown");
                        goToShutdown();
                        break;
                    }
                    try {
                        Thread.currentThread().sleep(50);
                    } catch (InterruptedException e) {
                        LogTool.e(LogTool.MEPG, "mEitTimeoutRunnable sleep fail");
                    }
                }
            }
        });
    }

    private void handleResume() {
        LogTool.d(LogTool.MPLAY, "handleResume");
        closePIP();
        initTvPlay();
        int gingaMode = mDtvConfig.getInt(GINGA_MODE, 0);
        if (null != ginga && mCurSourceId == EnumSourceIndex.SOURCE_ISDBT) {
            if (1 == gingaMode) {
                ginga.init();
                LogTool.d(LogTool.MPLAY, "handleResume open ginga");
            } else {
                ginga.deinit();
                LogTool.d(LogTool.MPLAY, "handleResume close ginga");
            }
        }
        if (!halApi.isDTVSource(mCurSourceId)) {
            mDTV.getOTA().stopOta();
        }
    }

    private void handleStop() {
        LogTool.d(LogTool.MPLAY, "handleStop");
        halApi.unregisterOnTvEventListener(tvEventListener);
        unSubDtvNormalScribeEvent();
        //unSubDtvScribeEvent();
        unSubAtvScribeEvent();
        if (((DTVApplication) getApplication()).isEnabledStop()
                && !mGotoFullPlayRecordFile) {
            halApi.setSourceHolder();
            halApi.changeSource(mCurSourceId, halApi.EnumSourceIndex.SOURCE_MEDIA);
            mCurSourceId = halApi.EnumSourceIndex.SOURCE_MEDIA;
        }
    }
}
