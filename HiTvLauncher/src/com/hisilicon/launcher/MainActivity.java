package com.hisilicon.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.EthernetManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dolphin.dtv.channel.LocalChannel;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.android.tvapi.constant.EnumWakeupMode;
import com.hisilicon.android.tvapi.vo.Wakeup;
import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.AtvChannelManager;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.epg.EPGEvent;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.si.TimeManager;
import com.hisilicon.dtv.pc.ParentalControlManager;
import com.hisilicon.dtv.play.EnStopType;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.launcher.hal.halApi;
import com.hisilicon.launcher.interfaces.AudioInterface;
import com.hisilicon.launcher.interfaces.EtherInterface;
import com.hisilicon.launcher.interfaces.InterfaceValueMaps;
import com.hisilicon.launcher.interfaces.SourceManagerInterface;
import com.hisilicon.launcher.interfaces.SystemSettingInterface;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.logic.factory.LogicFactory;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.util.LogHelper;
import com.hisilicon.launcher.util.TaskUtil;
import com.hisilicon.launcher.util.Util;
import com.hisilicon.launcher.view.MainPageApp;
import com.hisilicon.launcher.view.MainPageSetting;
import com.hisilicon.launcher.view.MainPageTv;
import com.hisilicon.launcher.view.ScrollLayout;
import com.hisilicon.launcher.view.TagView;
import com.hisilicon.launcher.view.setting.CustomSettingView;

import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends Activity implements IDTVListener {
    private static final String TAG = "MainActivity";
    private static final String DTV_PLUGIN_NAME =
            "dtv-live://plugin.libhi_dtvplg";
    public static final int HI_INVALID_PID = 0x1FFF;
    public static final int VALUE_HAVE_SIGNAL = 1;
    public static final int VALUE_NO_SIGNAL = 0;

    private static final int SELECT_TVSOURCE = 1;
    private static final int DELECT_TVSOURCE = 2;

    private static final int ATV_PROGRAM_LOCK = 2;
    private static final int LOCK_OPEN = 1;
    private static final int CHECK_BOOK_TASK_STATUS = 5;
    private static final String KEY_LAST_PAGE = "key_last_page";
    private static final String BOOKTASK = "booktask";
    private static final String WAKE_UP_LAUNCH = "wake_up_launch";
    private static final String TV_AUTO_TIME = "tv_auto_time";
    private String bookTaskValue = "none";

    private volatile boolean isOnStop = false;

    private boolean isSelectSource = true;

    public void setIsSelectSource(boolean isSelectSource) {
        this.isSelectSource = isSelectSource;
    }

    public static final int SWITCH_SOURCE_DELAY = 450;
    // mark for control focus
    private static boolean isSnapLeftOrRight = false;
    // Whether the locale is changed
    private static boolean isChangeLocale = false;
    // Mark whether the focus is at the top
    private boolean isFocusUp = true;
    // Whether it is sliding to the left
    private boolean isSnapLeft = false;

    // The page recording focus
    private int mFocusedPage = 0;
    // Record the focus window
    private int mFocusedView = 0;
    private boolean mAdjustWinRect = false;

    private EthernetManager mEthernetManager;
    private boolean isNewIntent = false;
    private ScrollLayout mRootLayout;
    private MainPageApp mAppPage;
    private MainPageSetting mSettingPage;
    private TagView mTagView;
    private ImageView wifiImg;
    private ImageView interImg;
    private ImageView logoImg;
    private ImageView radioImg;
    private ImageView lockImg;
    private WifiManager mWifiManager;
    private AudioManager mAudioManager;
    private InterfaceLogic mInterfaceLogic;

    private volatile int TvSourceIdx = EnumSourceIndex.SOURCE_ATV;
    private int TvSourceIdxBeforeStandby = EnumSourceIndex.SOURCE_ATV;
    private volatile boolean isNeedResetAtvChannel = false;
    private boolean mIsWakeUpLaunch = false;
    private LogicFactory mLogicFactory = null;
    // language setting dialog
    private AlertDialog mLocalChangedDialog;
    private DTV mDTV = null;
    private Player mPlayer = null;
    public DTVConfig mDtvConfig = null;
    public ChannelManager mChannelManager = null;
    private ParentalControlManager mPCManager;
    public AtvChannelManager mAtvChannelManager = null;
    public PlayerManager mPlayerManager = null;
    private PowerManager mPowerManager;
    // array of wifi image
    private int[] wifiImage = new int[]{R.drawable.main_wifi_signal_1,
            R.drawable.main_wifi_signal_2, R.drawable.main_wifi_signal_3,
            R.drawable.main_wifi_signal_4, R.drawable.main_wifi_signal_5};
    private AlertDialog mNewServiceFoundDialog = null;
    private boolean isParentLockMessage;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case SELECT_TVSOURCE:
                    selectTvSource(false);
                    break;
                case DELECT_TVSOURCE:
                    deselectTvSource();
                    break;
                case CHECK_BOOK_TASK_STATUS:
                    int bookTaskStatus = SystemProperties.getInt(Constant.PROPERTY_BOOK_TASK_TYPE, Constant.BOOK_TASK_TYPE_IDLE);
                    if (bookTaskStatus == Constant.BOOK_TASK_TYPE_POWER_ON) {
                        //开屏开声音
                        halApi.enablePowerOnPanel(true);
                        halApi.setSystemMute(MainActivity.this, false);
                        SystemProperties.set(Constant.PROPERTY_BOOK_TASK_TYPE, Constant.BOOK_TASK_TYPE_IDLE + "");
                        return;
                    } else if (bookTaskStatus == Constant.BOOK_TASK_TYPE_FINISH) {
                        SourceManagerInterface.selectSource(TvSourceIdxBeforeStandby, 0);
                        halApi.setSystemMute(MainActivity.this, false);
                        goToShutdown();
                        return;
                    } else if (bookTaskStatus == Constant.BOOK_TASK_TYPE_IDLE) {
                        //0的时候不该去监听 正常不应该走进来
                        LogHelper.w(TAG, "warning: check booktasktype without booktask");
                        return;
                    }
                    mHandler.sendEmptyMessageDelayed(CHECK_BOOK_TASK_STATUS, 1000);
                    break;
                default:
                    break;
            }
        }
    };

    private void refreshSourceView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAppPage.setTextValue(TvSourceIdx);
                mAppPage.hideOrShowText(true);
            }
        });
    }

    //change to MEDIA source
    private void deselectTvSource() {
        TaskUtil.post(new Runnable() {
            @Override
            public void run() {
                SourceManagerInterface.setSourceHolder();
                SourceManagerInterface.selectSource(EnumSourceIndex.SOURCE_MEDIA, 0);
            }
        });
    }

    private void selectTvSource(final boolean needUpdateSourceId) {
        TaskUtil.post(new Runnable() {
            @Override
            public void run() {
                if (needUpdateSourceId) {
                    TvSourceIdx = SourceManagerInterface.getSelectSourceId();
                }
                LogHelper.d(TAG, "will select source to " + TvSourceIdx);
                int atvChannelId = Settings.System.getInt(getContentResolver(),
                        Constant.SETTING_POWERON_ATVCHANNEL, 0);
                isNeedResetAtvChannel = false;
                refreshSourceView();
                SourceManagerInterface.setSourceHolder();
                if (TvSourceIdx == EnumSourceIndex.SOURCE_ATV && isNeedResetAtvChannel) {
                    SourceManagerInterface.selectSource(TvSourceIdx, 0, atvChannelId);
                } else {
                    SourceManagerInterface.selectSource(TvSourceIdx, 0);
                }
                if (!isOnStop) {
                    LogHelper.d(TAG, "setFullWindow false");
                    SourceManagerInterface.setFullWindow(false);
                }
                checkLock();
            }
        });
    }

    public static void setSnapLeftOrRight(boolean snapLeftOrRight) {
        isSnapLeftOrRight = snapLeftOrRight;
    }

    public static boolean isSnapLeftOrRight() {
        return isSnapLeftOrRight;
    }

    public static void setChangeLocale(boolean changeLocale) {
        isChangeLocale = changeLocale;
    }

    public static boolean isChangeLocale() {
        return isChangeLocale;
    }

    /**
     * Registered receiver monitor network changes
     */
    public BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String mode = mEthernetManager.getEthernetMode();
            if (intent.getAction().equals(EtherInterface.ETHERNET_STATE_CHANGED_ACTION)) {
                int message = -1;
                message = intent.getIntExtra(EtherInterface.EXTRA_ETHERNET_STATE, -1);
                LogHelper.d(TAG, "Main ethernet state : " + message);
                switch (message) {
                    // Dynamic IP connection is successful event
                    case EtherInterface.EVENT_DHCP_CONNECT_SUCCESSED:
                    case EtherInterface.EVENT_STATIC_CONNECT_SUCCESSED:
                        interImg.setBackgroundResource(R.drawable.interactive_connect);
                        break;
                    case EtherInterface.EVENT_DHCP_CONNECT_FAILED:
                    case EtherInterface.EVENT_DHCP_DISCONNECT_SUCCESSED:
                    case EtherInterface.EVENT_DHCP_DISCONNECT_FAILED:
                        if (EthernetManager.ETHERNET_CONNECT_MODE_DHCP.equals(mode)) {
                            interImg.setBackgroundResource(R.drawable.interactive_no_connect);
                        }
                        break;
                    case EtherInterface.EVENT_STATIC_CONNECT_FAILED:
                    case EtherInterface.EVENT_STATIC_DISCONNECT_SUCCESSED:
                    case EtherInterface.EVENT_STATIC_DISCONNECT_FAILED:
                        if (EthernetManager.ETHERNET_CONNECT_MODE_MANUAL.equals(mode)) {
                            interImg.setBackgroundResource(R.drawable.interactive_no_connect);
                        }
                        break;
                    case EtherInterface.EVENT_PHY_LINK_DOWN:
                        interImg.setBackgroundResource(R.drawable.interactive_no_connect);
                        break;
                    default:
                        break;
                }
            } else if (intent.getAction().equals(EtherInterface.PPPOE_STATE_CHANGED_ACTION)) {
                int message = -1;
                message = intent.getIntExtra(EtherInterface.EXTRA_PPPOE_STATE, -1);
                LogHelper.i(TAG, "Main pppoe state : " + message);
                switch (message) {
                    // pppoe connect success events
                    case EtherInterface.EVENT_PPPOE_CONNECT_SUCCESSED:
                        interImg.setBackgroundResource(R.drawable.interactive_connect);
                        break;
                    case EtherInterface.EVENT_PPPOE_CONNECT_FAILED:
                    case EtherInterface.EVENT_PPPOE_CONNECTING:
                    case EtherInterface.EVENT_PPPOE_DISCONNECT_SUCCESSED:
                    case EtherInterface.EVENT_PPPOE_AUTORECONNECTING:
                        if (EthernetManager.ETHERNET_CONNECT_MODE_PPPOE.equals(mode)) {
                            interImg.setBackgroundResource(R.drawable.interactive_no_connect);
                        }
                        break;
                    default:
                        break;
                }
            } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getExtra(
                        WifiManager.EXTRA_NETWORK_INFO, null);
                if (networkInfo == null) {
                    return;
                }
                LogHelper.i(TAG, "Main wifi state : " + networkInfo.getState());
                if (networkInfo.isConnected()) {
                    WifiInfo info = mWifiManager.getConnectionInfo();
                    int level = WifiManager.calculateSignalLevel(info.getRssi(), 5);
                    wifiImg.setBackgroundResource(wifiImage[level]);
                } else {
                    wifiImg.setBackgroundResource(R.drawable.wifi_signal_off);
                }
            }
        }
    };

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        LogHelper.i(TAG, "===== onCreate =====");
        super.onCreate(savedInstanceState);

        //If don't setup provision, default to use HOME_KEY
        int isProvisioned = Settings.Secure.getIntForUser(MyApplication.getAppContext().getContentResolver(),
                Settings.Secure.DEVICE_PROVISIONED, 0, UserHandle.USER_CURRENT);
        if (isProvisioned == 0) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.DEVICE_PROVISIONED, 1);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.TV_USER_SETUP_COMPLETE, 1);
        }

        int lastPage = -1;
        if (savedInstanceState != null) {
            lastPage = savedInstanceState.getInt(KEY_LAST_PAGE, -1);
            savedInstanceState.putInt(KEY_LAST_PAGE, -1);
        }
        updateWakeMode(getIntent());
        setContentView(R.layout.activity_main);
        initView(lastPage);
        initManagers();
        registerBroadcastReceiver();
        initTV();
        getTimeSyncTag();
        if (("quickplay".equals(SystemProperties.get("prop.service.bootop.type", "bootanim"))
                && SystemProperties.get("service.quickplay.setting.status", "0").equals("1"))
                || (Settings.Secure.getInt(MyApplication.getAppContext().getContentResolver(), "setup_wizard_source_position", 0) == 1
                && !SystemProperties.get("service.quickplay.setting.status").equals("0"))) {
            int sourceName = halApi.getSelectSourceID();
            Intent intent = new Intent();
            intent.putExtra("SourceName", sourceName);
            Util.startActivity(this, "com.hisilicon.tvui", "com.hisilicon.tvui.play.MainActivity", intent);
            SystemProperties.set("service.quickplay.setting.status", "0");
            netReceiver = null;
            finish();
        }
        LogHelper.d(TAG, "Launcher onCreate end");
    }

    /**
     * .get time sync time
     */
    private void getTimeSyncTag() {
        int networkSync = 0;
        int dtvSync = 0;
        try {
            networkSync = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME);
            dtvSync = Settings.Global.getInt(getContentResolver(), TV_AUTO_TIME);
            initSyncTime(networkSync, dtvSync);
        } catch (Settings.SettingNotFoundException e) {
            LogHelper.d(TAG, "onCreate exception : " + e.getMessage());
            initSyncTime(networkSync, dtvSync);
        }
    }

    /**
     * .init sync time
     */
    private void initSyncTime(int networkSync, int dtvSync) {
        LogHelper.d(TAG, "onCreate networkSync = " + networkSync);
        LogHelper.d(TAG, "onCreate dtvSync = " + dtvSync);
        TimeManager mTimeManager = mDTV.getNetworkManager().getTimeManager();
        if (networkSync > 0) { // sync network time
            mTimeManager.syncTime(false);
            mTimeManager.syncTimeZone(false);
            mTimeManager.setTimeToSystem(false);
            mTimeManager.setCalendarTime(Calendar.getInstance());
        } else if (dtvSync > 0) { // sync tv time
            mTimeManager.syncTime(true);
            mTimeManager.syncTimeZone(true);
            mTimeManager.setTimeToSystem(true);
        } else {
            mTimeManager.setTimeToSystem(false);
        }
    }

    private void initManagers() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //set sound device, default is SPEAKER
        int device = mAudioManager.getMediaOutDevice();
        LogHelper.d(TAG, " onCreate get device : " + device);
        if (device == AudioManager.DEVICE_NONE) {
            device = AudioManager.DEVICE_OUT_SPEAKER;
        }
        mAudioManager.setMediaOutDevice(device);

        mEthernetManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);
    }

    private void initTV() {
        mDTV = DTV.getInstance(DTV_PLUGIN_NAME);
        mChannelManager = mDTV.getChannelManager();
        mAtvChannelManager = mDTV.getAtvChannelManager();
        mDtvConfig = mDTV.getConfig();
        mPCManager = mDTV.getParentalControlManager();
        PlayerManager mPlayerManager = mDTV.getPlayerManager();

        if (mPlayerManager.getPlayers().size() > 0) {
            mPlayer = mPlayerManager.getPlayers().get(0);
        } else {
            mPlayer = mPlayerManager.createPlayer();
        }
    }

    /**
     * Initialize views
     */
    private void initView(int lastPage) {
        mRootLayout = (ScrollLayout) findViewById(R.id.root);
        mAppPage = (MainPageApp) findViewById(R.id.app_page);
        mSettingPage = (MainPageSetting) findViewById(R.id.set_page);
        mTagView = (TagView) findViewById(R.id.tag_view);
        wifiImg = (ImageView) findViewById(R.id.wifi);
        interImg = (ImageView) findViewById(R.id.interactive);
        logoImg = (ImageView) findViewById(R.id.logo);
        radioImg = (ImageView) findViewById(R.id.view_radio);
        lockImg = (ImageView) findViewById(R.id.view_lock);
        mLogicFactory = new LogicFactory(this);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // If the language is changed into the main interface, directly display
        // the language change dialog
        if (checkLocaleChanged()) {
            isChangeLocale = true;
            showAllVisbleOrGone(false);
            SharedPreferences preferences = MyApplication.getAppContext()
                    .getSharedPreferences(Constant.SET_LOCALE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constant.RESET_LOCALE, false);
            editor.commit();
            createLocaleChangeDialog(R.id.set_item_advanced);
        } else if (lastPage != -1) {
            switch (lastPage) {
                case MainPageApp.PAGENUM:
                    mRootLayout.setMainPageToAppScreen(mRootLayout.getCurScreen().getId());
                    break;
                case MainPageSetting.PAGENUM:
                    mRootLayout.setMainPageToSettingScreen(mRootLayout.getCurScreen().getId());
                default:
                    break;
            }
        } else {
            mRootLayout.setMainPage(mRootLayout.getCurScreen().getId());
        }
    }

    /**
     * create change language dialog
     *
     * @param Index
     */
    private void createLocaleChangeDialog(int Index) {
        mInterfaceLogic = mLogicFactory.createLogic(Index);
        if (mInterfaceLogic != null && mInterfaceLogic.getWidgetTypeList() != null) {
            mLocalChangedDialog = new AlertDialog.Builder(this,
                    R.style.Translucent_NoTitle).create();
            Window window = mLocalChangedDialog.getWindow();
            mLocalChangedDialog.show();
            mLocalChangedDialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface arg0) {
                    showAllVisbleOrGone(true);
                }
            });
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.height = (int) getResources().getDimension(R.dimen.dimen_600px);
            lp.width = (int) getResources().getDimension(R.dimen.dimen_800px);
            window.setAttributes(lp);
            window.setContentView(new CustomSettingView(this, getResources().getString(
                    Util.getValueFromArray(Index,
                            InterfaceValueMaps.app_item_values)), mInterfaceLogic));
        }
    }

    /**
     * make all views visible or gone
     *
     * @param visible
     */
    private void showAllVisbleOrGone(boolean visible) {
        if (visible) {
            mRootLayout.setVisibility(View.VISIBLE);
            wifiImg.setVisibility(View.VISIBLE);
            interImg.setVisibility(View.VISIBLE);
            logoImg.setVisibility(View.VISIBLE);
            mTagView.setVisibility(View.VISIBLE);
            if (!isNewIntent) {
                isChangeLocale = true;
                mRootLayout.setMainPageToSettingScreen(mRootLayout.getCurScreen().getId());
            }
            isNewIntent = false;
        } else {
            mRootLayout.setVisibility(View.GONE);
            wifiImg.setVisibility(View.GONE);
            interImg.setVisibility(View.GONE);
            logoImg.setVisibility(View.GONE);
            mTagView.setVisibility(View.GONE);
        }
    }

    /**
     * check Locale Changed or not
     *
     * @return
     */
    private boolean checkLocaleChanged() {
        SharedPreferences preferences = MyApplication.getAppContext()
                .getSharedPreferences(Constant.SET_LOCALE, Context.MODE_PRIVATE);
        boolean isChanged = preferences.getBoolean(Constant.RESET_LOCALE, false);
        LogHelper.d(TAG, "checkLocaleChanged = " + isChanged);
        return isChanged;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Avoid large windows to small window splashes
        int sourceId = SourceManagerInterface.getCurSourceId();
        if (halApi.isDTVSource(sourceId)) {
            stopPlayer();
        } else {
            SourceManagerInterface.deselectSource(sourceId, true);
        }
        isNewIntent = true;
        updateWakeMode(intent);
        String action = intent.getAction();
        LogHelper.d(TAG, "onNewIntent = " + intent.getAction() + "; id "
                + mRootLayout.getCurScreen().getId());
        if (null != mLocalChangedDialog && mLocalChangedDialog.isShowing()) {
            mLocalChangedDialog.dismiss();
        }
        mSettingPage.dismissDialog();
        isSnapLeftOrRight = false;
        mRootLayout.setMainPage(mRootLayout.getCurScreen().getId());
        if (hasWindowFocus()) {
            checkMainPage();
        }
    }

    @Override
    protected void onStart() {
        LogHelper.i(TAG, "===== onStart =====");
        super.onStart();
        if (isScreenOn()) {
            isOnStop = false;
            isSelectSource = true;
            if (mDTV != null) {
                mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_AV_SIGNAL_STAUTS, this, 0);
                mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING, this, 0);
                mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_CEC_SELECT_SOURCE, this, 0);
                mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_PLAYER_LOCK_CHANGED, this, 0);
            }
            //Because Google releases surface in setWindowStopped method,
            //in order to relayout to generate effective surface after STR wakes up,
            //it is necessary to actively change the visible state in the application.
            setVisible(true);
            setFocus();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        LogHelper.i(TAG, "onWindowFocusChanged " + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (isScreenOn()) {
                showNewServiceFoundIfNesessary();
                checkFirstBoot();
            }
        } else {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void checkFirstBoot() {
        isNeedResetAtvChannel = false;
        String bootFlag = SystemProperties.get("persist.sys.bootup");
        String powersave = SystemProperties.get("persist.prop.suspend.mode");
        LogHelper.d(TAG, "launcher bootFlag : " + bootFlag + ",powersave-->" + powersave);
        int sourceId = -1;
        if (!bootFlag.equals("1") && !mIsWakeUpLaunch) {
            LogHelper.d(TAG, "launcher go to mainpage");
            checkMainPage();
        } else {
            SystemProperties.set("persist.sys.bootup", "" + 0);
            mIsWakeUpLaunch = false;
            //powersave do not support wakeup mode currently,so select the pre source directly
            if (!powersave.equals("lightsleep")) {
                Wakeup mWakeup = SystemSettingInterface.getWakeup();
                LogHelper.d(TAG, "launcher first bootup, get wakeup mode : " + mWakeup.getWakeupMode()
                        + " source :" + mWakeup.getWakeupSource());
                int mWakeupMode = mWakeup.getWakeupMode();
                switch (mWakeupMode) {
                    case EnumWakeupMode.WAKEUP_IR:
                        sourceId = mWakeup.getWakeupSource();
                        break;
                    case EnumWakeupMode.WAKEUP_TIMEOUT:
                        sourceId = mWakeup.getWakeupSource();
                        timeoutWakeupStart();
                        break;
                    case EnumWakeupMode.WAKEUP_CEC:
                    case EnumWakeupMode.WAKEUP_HDMI:
                    case EnumWakeupMode.WAKEUP_VGA:
                        sourceId = mWakeup.getWakeupSource();
                        Intent intent = new Intent();
                        intent.setAction(Constant.INTENT_ATV);
                        intent.putExtra("SourceName", sourceId);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        LogHelper.d(TAG, "launcher CEC/HDMI/VGA wake source: " + sourceId);
                        startActivity(intent);
                        return;
                    case EnumWakeupMode.WAKEUP_SCART:
                        // TODO
                        break;
                    case EnumWakeupMode.WAKEUP_TIMEOUT_BG:
                        LogHelper.d(TAG, "WakeUpMode for Europe standby = " + mWakeupMode);
                        if (!TextUtils.isEmpty(Settings.Secure.getString(getContentResolver(), BOOKTASK))) {
                            bookTaskValue = Settings.Secure.getString(getContentResolver(), BOOKTASK);
                        }
                        LogHelper.d(TAG, "bookTaskValue = " + bookTaskValue);
                        SystemProperties.set(Constant.PROPERTY_BOOK_TASK_TYPE, Constant.BOOK_TASK_TYPE_WORKING + "");
                        mHandler.sendEmptyMessage(CHECK_BOOK_TASK_STATUS);
                        halApi.setSystemMute(this, true);
                        TvSourceIdxBeforeStandby = mWakeup.getWakeupSource();
                        sourceId = EnumSourceIndex.SOURCE_DVBT;
                        Intent intent1 = new Intent();
                        intent1.setAction(Constant.INTENT_ATV);
                        intent1.putExtra("SourceName", EnumSourceIndex.SOURCE_DVBT);
                        intent1.putExtra("fromStandbyWake", 1);
                        intent1.putExtra("bookTaskValue", bookTaskValue);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        LogHelper.d(TAG, "launcher WAKEUP_TIME_FOR_SCAN/EPG/PVR wake source: " + sourceId);
                        startActivity(intent1);
                        return;
                    case EnumWakeupMode.WAKEUP_BUTT:
                        LogHelper.d(TAG, "WAKEUP_BUTT = " + EnumWakeupMode.WAKEUP_BUTT);
                        sourceId = mWakeup.getWakeupSource();
                        break;
                    default:
                        break;
                }
            }
            if (sourceId >= 0) {
                TvSourceIdx = sourceId;
                initSourceOnBoot(false);
            } else {
                initSourceOnBoot(true);
            }
        }
    }

    //wait for bootanim or bootvideo end
    private void initSourceOnBoot(boolean needUpdateSourceId) {
        if (mFocusedPage == MainPageApp.PAGENUM) {
            TaskUtil.post(new Runnable() {
                public void run() {
                    String bootOp = SystemProperties.get("prop.service.bootop.type", "bootanim");
                    if (bootOp.equals("bootanim")) {
                        while ("running".equals(SystemProperties.get("init.svc.bootanim", ""))) {
                            try {
                                Thread.currentThread().sleep(10);
                            } catch (InterruptedException e) {
                                LogHelper.e(TAG, "bootanim thread sleep fail");
                            }
                        }
                    } else if (bootOp.equals("bootvideo")) {
                        while ("run".equals(SystemProperties.get("persist.prop.bootvideo.status", "none"))) {
                            try {
                                Thread.currentThread().sleep(10);
                            } catch (InterruptedException e) {
                                LogHelper.e(TAG, "bootvideo thread sleep fail");
                            }
                        }
                    }
                    selectTvSource(needUpdateSourceId);
                }
            });
        } else {
            deselectTvSource();
        }
    }

    private void showNewServiceFoundIfNesessary() {
        if (Settings.System.getInt(getContentResolver(), Constant.SETTING_NEW_SERVICE_FOUND, 0) > 0) {
            if (mNewServiceFoundDialog == null) {
                mNewServiceFoundDialog = new AlertDialog.Builder(this)
                        .setTitle(this.getText(R.string.new_service_found))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(this.getText(R.string.new_service_found))
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Settings.System.putInt(getContentResolver(), Constant.SETTING_NEW_SERVICE_FOUND, 0);
                                    }
                                }).setCancelable(false).create();
            }
            mNewServiceFoundDialog.show();
        } else if (mNewServiceFoundDialog != null && mNewServiceFoundDialog.isShowing()) {
            mNewServiceFoundDialog.dismiss();
        }
    }

    private void checkMainPage() {
        if (mFocusedPage != MainPageApp.PAGENUM) {
            if (mFocusedPage == MainPageTv.PAGENUM) {
                LogHelper.d(TAG, "close PIP");
                recyclePipResource();
            }
            if (!isOnStop) {
                LogHelper.d(TAG, "change source to Media");
                mHandler.sendEmptyMessage(DELECT_TVSOURCE);
            }
        } else {
            LogHelper.d(TAG, "change source to TV");
            selectTvSource(true);
        }
    }

    private void timeoutWakeupStart() {
        int mode = Settings.System.getInt(getContentResolver(),
                Constant.SETTING_BOOT_MODE, 0);
        int source = Settings.System.getInt(getContentResolver(),
                Constant.SETTING_BOOT_MODE_SOURCE, 0);
        LogHelper.d(TAG, "launcher get boot mode = " + mode + " source = " + source);
        if (mode > 0 && source >= 0) {
            // setting power wake
            if (Settings.System.getInt(getContentResolver(),
                    Constant.SETTING_POWERON_REPEAT, 0) == 1) {
                Settings.System.putInt(getContentResolver(),
                        Constant.SETTING_POWERON_REPEAT, 0);
                Settings.System.putInt(getContentResolver(), Constant.SETTING_POWERON_TIME, 0);
            }
            TvSourceIdx = source;
            int volume = Settings.System.getInt(getContentResolver(),
                    Constant.SETTING_POWERON_VALUME, 10);
            LogHelper.d(TAG, "launcher time wake , set volume : " + volume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            Intent intent = new Intent();
            switch (source) {
                case EnumSourceIndex.SOURCE_DTMB:
                case EnumSourceIndex.SOURCE_DVBC:
                case EnumSourceIndex.SOURCE_DVBT:
                case EnumSourceIndex.SOURCE_ATSC:
                    LogHelper.d(TAG, "DTV");
                    intent.setAction(Constant.INTENT_DTV);
                    break;
                case EnumSourceIndex.SOURCE_MEDIA:
                    intent.setClassName("com.hisilicon.explorer",
                            "com.hisilicon.explorer.activity.TabBarExample");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(intent);
                    break;
                default:
                    LogHelper.d(TAG, "ATV");
                    isNeedResetAtvChannel = true;
                    intent.setAction(Constant.INTENT_ATV);
                    break;
            }
            intent.putExtra("SourceName", source);
            intent.putExtra("fromTimeWake", 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LogHelper.d(TAG, "launcher setting power wake, source :" + source);
            this.startActivity(intent);
        } else {
            // epg pvr
            LogHelper.d(TAG, "launcher epg pvr book wake");
            Intent intent = new Intent();
            intent.setAction(Constant.INTENT_DTV);
            intent.putExtra("SourceName", TvSourceIdx);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        LogHelper.i(TAG, "===== onDestroy =====");
        super.onDestroy();
        if (mNewServiceFoundDialog != null && mNewServiceFoundDialog.isShowing()) {
            mNewServiceFoundDialog.dismiss();
            mNewServiceFoundDialog = null;
        }
        mLogicFactory = null;
        if (netReceiver != null) {
            unregisterReceiver(netReceiver);
        }
        if (mAppPage != null) {
            mAppPage.onDestroy();
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogHelper.d(TAG, "onSavedInstanceState mFocusedPage = " + mFocusedPage);
        outState.putInt(KEY_LAST_PAGE, mFocusedPage);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isOnStop = true;
        LogHelper.i(TAG, "===== onStop =====");
        //Because Google releases surface in setWindowStopped method,
        //in order to relayout to generate effective surface after STR wakes up,
        //it is necessary to actively change the visible state in the application.
        setVisible(false);
        dismissViews();

        mHandler.removeMessages(DELECT_TVSOURCE);
        mHandler.removeMessages(SELECT_TVSOURCE);

        if (mDTV != null) {
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_AV_SIGNAL_STAUTS, this);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING, this);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_CEC_SELECT_SOURCE, this);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_PLAYER_LOCK_CHANGED, this);
        }
        //don't deselectTvSource when str because HiRMService has switch source
        if (isSelectSource) {
            LogHelper.d(TAG, "launcher onStop will change source to Media");
            deselectTvSource();
        }
    }

    private void updateWakeMode(Intent intent) {
        if (intent != null) {
            mIsWakeUpLaunch = intent.getBooleanExtra("wake_up_launch", false);
        }
    }

    private void dismissViews() {
        lockImg.setVisibility(View.GONE);
        mAppPage.hideOrShowText(false);
        radioImg.setVisibility(View.GONE);
        if (null != mLocalChangedDialog && mLocalChangedDialog.isShowing()) {
            mLocalChangedDialog.dismiss();
        }
        mSettingPage.dismissDialog();
    }

    /**
     * register BroadcastReceiver to monitor network status
     */
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(EtherInterface.ETHERNET_STATE_CHANGED_ACTION);
        filter.addAction(EtherInterface.PPPOE_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(netReceiver, filter);
    }

    /**
     * set focus
     */
    private void setFocus() {
        if (mRootLayout.getCurScreen().getId() == MainPageApp.PAGENUM) {
            mFocusedPage = MainPageApp.PAGENUM;
            mFocusedView = 0;
            isFocusUp = true;
            mAppPage.onShow();
        } else {
            mFocusedPage = MainPageSetting.PAGENUM;
            mFocusedView = 0;
            isFocusUp = true;
            mSettingPage.onShow();
        }
        LogHelper.d(TAG, "setFocus curScreen = " + mFocusedPage);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        isSnapLeftOrRight = false;
        if (!mRootLayout.isFinished()) {
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    isSnapLeft = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    isSnapLeft = false;
                    break;
                default:
                    break;
            }

            switch (mRootLayout.getCurScreen().getId()) {
                case MainPageApp.PAGENUM:
                    return mAppPage.onKeyDown(keyCode, event);
                case MainPageSetting.PAGENUM:
                    return mSettingPage.onKeyDown(keyCode, event);
                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void switchSource() {
        if (mFocusedPage == MainPageTv.PAGENUM) {
            LogHelper.d(TAG, "close PIP");
            recyclePipResource();
        }
        if (mFocusedPage == MainPageApp.PAGENUM) {
            LogHelper.d(TAG, "switchSource to TV delayed");
            if (!isOnStop) {
                mHandler.removeMessages(SELECT_TVSOURCE);
                mHandler.removeMessages(DELECT_TVSOURCE);
                mHandler.sendEmptyMessageDelayed(SELECT_TVSOURCE, SWITCH_SOURCE_DELAY);
            }
        } else {
            LogHelper.d(TAG, "switchSource to Media delayed");
            if (!isOnStop) {
                mHandler.removeMessages(SELECT_TVSOURCE);
                mHandler.removeMessages(DELECT_TVSOURCE);
                mHandler.sendEmptyMessageDelayed(DELECT_TVSOURCE, SWITCH_SOURCE_DELAY);
            }
        }

    }

    /**
     * show next Screen
     */
    public void snapToNextScreen() {
        LogHelper.d(TAG, "snapToNextScreen");
        mRootLayout.snapToScreen(mRootLayout.getCurrentScreen() + 1);
        mFocusedPage = mRootLayout.getCurScreen().getId();
        switchSource();
    }

    /**
     * show previous Screen
     */
    public void snapToPreScreen() {
        LogHelper.d(TAG, "snapToPreScreen");
        mRootLayout.snapToScreen(mRootLayout.getCurrentScreen() - 1);
        mFocusedPage = mRootLayout.getCurScreen().getId();
        switchSource();
    }

    public void setFocusUp(boolean focusUp) {
        this.isFocusUp = focusUp;
    }

    public boolean isFocusUp() {
        return isFocusUp;
    }

    public boolean isSnapLeft() {
        return isSnapLeft;
    }

    public ScrollLayout getRoot() {
        return mRootLayout;
    }

    public TagView getTagView() {
        return mTagView;
    }

    public ImageView getLogoImg() {
        return logoImg;
    }

    public MainPageSetting getSettingPage() {
        return mSettingPage;
    }

    public ImageView getWifiImg() {
        return wifiImg;
    }

    public ImageView getInterImg() {
        return interImg;
    }

    public void setFocusePage(int page) {
        this.mFocusedPage = page;
    }

    public int getFocusedPage() {
        return mFocusedPage;
    }

    public int getFocusedView() {
        return mFocusedView;
    }

    public void setFocusedView(int focusedView) {
        this.mFocusedView = focusedView;
    }

    @Override
    public void notifyMessage(int messageID, int param1, int param2, Object obj) {
        LogHelper.d(TAG, " messageID:" + messageID + " param2:" + param2);
        switch (messageID) {
            case DTVMessage.HI_SVR_EVT_AV_SIGNAL_STAUTS:
                if (VALUE_NO_SIGNAL == param1) {
                    LogHelper.d(TAG, "DTV ShowVideo false");
                    mPlayer.showVideo(false);
                } else if (VALUE_HAVE_SIGNAL == param1) {
                    LogHelper.d(TAG, "DTV ShowVideo true");
                    if (isSelectSource && !isOnStop) {
                        SourceManagerInterface.setFullWindow(false);
                    }
                    mPlayer.showVideo(true);
                }
                break;
            case DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING:
                checkLock();
                break;
            case DTVMessage.HI_SVR_EVT_EPG_PARENTAL_LOCK_STATUS:
                isParentLockMessage = true;
                checkLock();
                break;
            case DTVMessage.HI_ATV_EVT_CEC_SELECT_SOURCE:
                int source = param1;
                LogHelper.d(TAG, "launcher ATV CEC , source : " + source);
                if (EnumSourceIndex.SOURCE_ATV != source) {
                    isSelectSource = false;
                    Intent intent = new Intent();
                    intent.putExtra("SourceName", source);
                    intent.setAction(Constant.INTENT_ATV);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(intent);
                    if (SourceManagerInterface.isDTVSource(TvSourceIdx)) {
                        stopPlayer();
                    }
                }
                int bookTaskStatus = SystemProperties.getInt(Constant.PROPERTY_BOOK_TASK_TYPE, Constant.BOOK_TASK_TYPE_IDLE);
                //待机pvr执行中
                if (bookTaskStatus == Constant.BOOK_TASK_TYPE_WORKING) {
                    //认为power on 了，亮屏开声音
                    SystemProperties.set(Constant.PROPERTY_BOOK_TASK_TYPE, Constant.BOOK_TASK_TYPE_POWER_ON + "");
                }
                break;
            case DTVMessage.HI_ATV_EVT_PLAYER_LOCK_CHANGED:
                ArrayList<Integer> list = new ArrayList<Integer>();
                list = (ArrayList<Integer>) obj;
                LogHelper.d(TAG, "launcher ATV lock change , list : " + list);
                if (list.get(ATV_PROGRAM_LOCK) == LOCK_OPEN) {
                    lockImg.setVisibility(View.VISIBLE);
                } else {
                    lockImg.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

    public void changeChannel(Channel channel) {
        mPlayer.resumeResource();
        //DTV 重新播放后，需要重新配置无信号蓝屏
        SystemSettingInterface.setBlueScreen();
        if (channel != null) {
            LogHelper.d(TAG, "mPlayer changeChannel." + channel);
            mPlayer.changeChannel(channel);
            refreshRadioView(channel);
        }
    }

    public void stopPlayer() {
        if (mPlayer != null) {
            LogHelper.d(TAG, "mPlayer onStop.");
            mPlayer.stop(EnStopType.BLACKSCREEN);
        }
    }

    public void releaseResourcePlayer() {
        if (mPlayer != null) {
            LogHelper.d(TAG, "mPlayer releaseResource.");
            mPlayer.releaseResource(0);
        }
    }

    private void refreshRadioView(Channel channel) {
        int videoPid = channel.getVideoPID();
        int audioPid = channel.getAudioPID();
        LogHelper.d(TAG, "RefreshRadioView videioPid:"+videoPid+",audioPid:"+audioPid);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (videoPid >= HI_INVALID_PID && audioPid != HI_INVALID_PID) {
                    LogHelper.d(TAG, "radio image visible");
                    radioImg.setVisibility(View.VISIBLE);
                } else {
                    radioImg.setVisibility(View.GONE);
                }
            }
        });
    }

    private void refreshLockImage(final boolean isLock) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lockImg.setVisibility(isLock ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void checkLock() {
        int channelId = Settings.Secure.getInt(getContentResolver(), "cur_channel_id", -1);
        Channel channel = null;
        if (channelId != -1) {
            if (SourceManagerInterface.isDTVSource(TvSourceIdx)) {
                channel = mChannelManager.getChannelByID(channelId);
            } else if (TvSourceIdx == EnumSourceIndex.SOURCE_ATV) {
                channel = mAtvChannelManager.getChannelByID(channelId);
            }
        }
        if (channel == null) {
            if (SourceManagerInterface.isDTVSource(TvSourceIdx)) {
                channel = mChannelManager.getDefaultOpenChannel();
            } else if (TvSourceIdx == EnumSourceIndex.SOURCE_ATV) {
                channel = mAtvChannelManager.getDefaultOpenChannel();
            }
        }
        boolean isLock = false;
        if (curSourceLocked(TvSourceIdx) || curProgramLocked(channel, TvSourceIdx)
                || curParentLocked(channel, TvSourceIdx)) {
            isLock = true;
        }
        refreshLockImage(isLock);
        lockTV(isLock, channel);
    }

    public void lockTV(boolean locked, Channel channel) {
        if (TvSourceIdx == EnumSourceIndex.SOURCE_ATSC && isParentLockMessage) {
            AudioInterface.setMute(TvSourceIdx, locked);
            isParentLockMessage = false;
        }
        LogHelper.d(TAG, "lock TV locked = " + locked);
        if (locked) {
            if (SourceManagerInterface.isDTVSource(TvSourceIdx)) {
                mPlayer.stop(EnStopType.BLACKSCREEN);
            }
        } else {
            if (SourceManagerInterface.isDTVSource(TvSourceIdx)) {
                changeChannel(channel);
            }
        }
    }

    private boolean curSourceLocked(int sourceId) {
        if (!SystemSettingInterface.getLockEnable(Constant.SOURCE_LOCK)) {
            return false;
        }
        if (SystemSettingInterface.getPwdStatus(Constant.SOURCE_LOCK_TYPE)) {
            return false;
        }
        return SystemSettingInterface.getSrcLockEnable(sourceId);
    }

    private boolean curProgramLocked(Channel channel, int sourceId) {
        if (channel == null) {
            return false;
        }
        if (!SystemSettingInterface.getLockEnable(Constant.CHANNEL_LOCK) ||
                SystemSettingInterface.getPwdStatus(Constant.CHANNEL_LOCK_TYPE)) {
            return false;
        } else if (SourceManagerInterface.isTVSource(sourceId) && channel.getTag(EnTagType.LOCK)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean curParentLocked(Channel channel, int sourceId) {
        if (channel == null) {
            return false;
        }
        if (SystemSettingInterface.getPwdStatus(Constant.PARENTAL_LOCK_TYPE)) {
            return false;
        }
        if (SourceManagerInterface.isDTVSource(sourceId)) {
            if (isParentalRatingNeedBlock(channel)) {
                return true;
            }
        } else if (SystemSettingInterface.isCurrentLocked().get(2) == 1) {
            return true;
        }
        return false;
    }

    public boolean isParentalRatingNeedBlock(Channel channel) {
        int parentalRating = 0;
        String strCountry = "";
        EPGEvent epgPresentEvent = null;
        boolean bEqualBlock = false;
        boolean bNeedBlock = false;

        if (null == channel) {
            return false;
        }

        int userParentalRating = mPCManager.getParentLockAge();

        parentalRating = channel.getParentLockLevel();
        if (parentalRating != 0) {
            strCountry = channel.getParentCountryCode();
        } else {
            epgPresentEvent = channel.getPresentEvent();
            if (null != epgPresentEvent) {
                parentalRating = epgPresentEvent.getParentLockLevel();
                strCountry = epgPresentEvent.getParentCountryCode();
            }
        }

        if (strCountry != null && strCountry.equalsIgnoreCase("BRA")) {
            parentalRating = parentalRating & 0xf;
            if ((parentalRating <= 1) || (parentalRating >= 7)) {
                parentalRating = 0;
            } else {
                parentalRating = (parentalRating + 3) * 2;
            }
        }

        if ((0 == parentalRating) || (0 == userParentalRating)) {
            return false;
        }

        if (strCountry != null) {
            if (strCountry.equalsIgnoreCase("MYS") || strCountry.equalsIgnoreCase("IDN")
                    || strCountry.equalsIgnoreCase("NZL")
                    || strCountry.equalsIgnoreCase("SGP")
                    || strCountry.equalsIgnoreCase("THA")
                    || strCountry.equalsIgnoreCase("VNM")
                    || strCountry.equalsIgnoreCase("BRA")) {
                bEqualBlock = true;
            } else {
                bEqualBlock = false;
            }
        }

        if (parentalRating > userParentalRating ||
                (parentalRating == userParentalRating && true == bEqualBlock)) {
            bNeedBlock = true;
        }

        return bNeedBlock;
    }

    private void goToShutdown() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (SystemProperties.get("persist.prop.suspend.mode").equals("str")) {
            LogHelper.d(TAG, "STR suspend");
            halApi.goToSleep(pm);
        } else if (SystemProperties.get("persist.prop.suspend.mode").equals("shutdown")) {
            LogHelper.d(TAG, "shut down");
            Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
            intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        LogHelper.d(TAG, "power off!");
    }


    private boolean isScreenOn() {
        if (mPowerManager == null) {
            mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        }
        return mPowerManager.isInteractive();
    }

    private void recyclePipResource() {
        String pkgNames[] = {getApplicationInfo().packageName};
        Intent intent = new Intent(Intent.ACTION_MEDIA_RESOURCE_GRANTED);
        intent.putExtra(Intent.EXTRA_PACKAGES, pkgNames);
        intent.putExtra(Intent.EXTRA_MEDIA_RESOURCE_TYPE, Intent.EXTRA_MEDIA_RESOURCE_TYPE_VIDEO_CODEC);
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }
}
