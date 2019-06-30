package com.hisilicon.android.videoplayer.activity;

import android.annotation.IdRes;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.activity.adapter.MenuAdapter;
import com.hisilicon.android.videoplayer.activity.adapter.MenuListAdapter;
import com.hisilicon.android.videoplayer.activity.adapter.VersionControl;
import com.hisilicon.android.videoplayer.activity.base.MenuBaseActivity;
import com.hisilicon.android.videoplayer.activity.inter.OnPlayerDestroyListener;
import com.hisilicon.android.videoplayer.menu.MenuConfig;
import com.hisilicon.android.videoplayer.menu.MenuKeyValuePair;
import com.hisilicon.android.videoplayer.menu.SecondMenuConfig;
import com.hisilicon.android.videoplayer.menu.ThirdMenuConfig;
import com.hisilicon.android.videoplayer.model.Common;
import com.hisilicon.android.videoplayer.model.ControlModel;
import com.hisilicon.android.videoplayer.model.DoblyResult;
import com.hisilicon.android.videoplayer.model.EncodeNameValue;
import com.hisilicon.android.videoplayer.model.ModeMkvInfo;
import com.hisilicon.android.videoplayer.model.PlayerController;
import com.hisilicon.android.videoplayer.model.VideoModel;
import com.hisilicon.android.videoplayer.model.bluray.DispFmt;
import com.hisilicon.android.videoplayer.model.bluray.HiDisplayManager;
import com.hisilicon.android.videoplayer.model.bluray.base.AudioFormat;
import com.hisilicon.android.videoplayer.model.bluray.base.LanguageXmlParser;
import com.hisilicon.android.videoplayer.model.dao.DBHelper;
import com.hisilicon.android.videoplayer.model.listmanager.FMMediaFileList;
import com.hisilicon.android.videoplayer.model.listmanager.MediaFileList;
import com.hisilicon.android.videoplayer.observer.PlayerProviderObserver;
import com.hisilicon.android.videoplayer.utils.Constants;
import com.hisilicon.android.videoplayer.utils.DialogTool;
import com.hisilicon.android.videoplayer.utils.EncodeXmlParser;
import com.hisilicon.android.videoplayer.utils.HiMediaPlayer;
import com.hisilicon.android.videoplayer.utils.HiMediaPlayerInvoke;
import com.hisilicon.android.videoplayer.utils.LogTool;
import com.hisilicon.android.videoplayer.utils.PlayerJugdment;
import com.hisilicon.android.videoplayer.utils.ShooterApiQuery;
import com.hisilicon.android.videoplayer.utils.ShooterHttpDownload;
import com.hisilicon.android.videoplayer.utils.ShooterMD5;
import com.hisilicon.android.videoplayer.utils.ShooterSubinfo;
import com.hisilicon.android.videoplayer.utils.SystemProperties;
import com.hisilicon.android.videoplayer.utils.TimeTool;
import com.hisilicon.android.videoplayer.utils.ToastUtil;
import com.hisilicon.android.videoplayer.utils.Tools;
import com.hisilicon.android.videoplayer.view.CrossView;
import com.hisilicon.android.videoplayer.view.DptOMultipleListDialog;
import com.hisilicon.android.videoplayer.view.HisiVideoView;
import com.hisilicon.android.videoplayer.view.MouseView;
import com.hisilicon.android.videoplayer.view.MultiPlaySurfaceView;
import com.hisilicon.android.videoplayer.view.MultiPlayTextureView;
import com.hisilicon.android.videoplayer.view.MultipleListDialog;
import com.hisilicon.android.videoplayer.view.MySeekBar;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created on 2018/6/29.
 */

public class VideoActivity extends MenuBaseActivity {

    private final static String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    private final String ONE_KEY_PLAY_TAG = "onekeyplay";
    private final int ONE_KEY_PLAY_DEFAULT = 0;//1 a key cut source
    private static int HI_2D_MODE = 0;
    private static int HI_3D_MODE = 1;
    private static int HI_3D_MODE_SBS = 2;
    private static int HI_3D_MODE_TAB = 3;
    private static final int HI_VIDEOFORMAT_TYPE_MVC = 35;
    private static final long dolby_decoder = 2179993616L;
    private static final int subtitleMarginVertical = 60;
    private static final int subtitleMaxSizeHeight = 180;
    private int subtitleTopMargin = 60;
    private MultiPlayTextureView multiPlayTextureView = null;
    private MultiPlaySurfaceView multiPlaySurfaceView = null;
    private HisiVideoView videoView = null;
    private VideoView subtitleView = null;
    private RelativeLayout mediaInfoLayout = null;
    private LinearLayout btnLinearLayout;
    private View mediaControllerLayout;
    private TextView tvMediaName, tvMediaSize, tvSub, tvAudio;
    private ImageView ivPlayStatus;
    private TextView tvTime, tvTotalTime, mDolby, tvSubtitleForApp;
    private MySeekBar mySeekBar;

    private ImageView ivVolumeCtl, ivPageUp, ivRewind, ivForward, ivPlayPause, ivPageDown, ivMenu;
    private FrameLayout frameLayout;
    private CrossView crossView;
    private MouseView mouseView;
    private String[] colorValue = {"0xffffff", "0x000000", "0xff0000", "0xffff00", "0x0000ff", "0x00ff00"};
    private String[] videoFormatValue = {"MPEG2", "MPEG4", "AVS", "H263", "H264", "REAL8", "REAL9",
            "VC1", "VP6", "VP6F", "VP6A", "MJPEG", "SORENSON", "DIVX3",
            "RAW", "JPEG", "VP8", "MSMPEG4V1", "MSMPEG4V2", "MSVIDEO1",
            "WMV1", "WMV2", "RV10", "RV20", "SVQ1", "SVQ3", "H261", "VP3",
            "VP5", "CINEPAK", "INDEO2", "INDEO3", "INDEO4", "INDEO5",
            "MJPEGB", "MVC", "HEVC", "DV", "WMV3", "HUFFYUV", "REALMAGICMPEG4",
            "DIVX", "BUTT"};
    private String[] monoInfo = {"1+1", "1/0", "2/0", "3/0", "2/1", "3/1", "2/2", "3/2"};
    private boolean isFullScreen = false;

    protected static ArrayList<ShooterSubinfo> mShooterSubinfoList = new ArrayList<ShooterSubinfo>();
    private Common common;
    private static PlayerController playerController;
    private ControlModel controlModel;
    private DBHelper mDbHelper;
    private SQLiteDatabase database = null;

    private MediaFileList mediaFileList = null;
    //    private static MediaFileListService mediaFileListService = null;
    // Add for low - power state
    private PowerManager.WakeLock wakeLock = null;
    //for DTS2018032806521
    private boolean isPause = false;
    private boolean isStop = false;

    private boolean isFirstPlay = true;

    //Record multiple play mode.
    private boolean isMultiplePlay = false;
    //for 8k start
    private boolean mIs8KMouseMode = false;
    private boolean mIs8KMouseInScale = false;

    private boolean mIs8KAreaMode = false;
    //for 8k end
    private boolean isServiceBind = false;
    private int openFromMark = -1;

    //for 8k start
    private float xRatioFor8K;

    private float yRatioFor8K;

    private int realVideoWidth;

    private int realVideoHeight;
    private int multiPlayStyle = 0;

    private HiDisplayManager hiDisplayManager;
    private LanguageXmlParser xmlParser;

    private long startTime;
    private int speedChoiced;
    private ArrayList<ModeMkvInfo> mkvInfos;
    private String[] titles;
    private boolean isThreadStart;
    private boolean isSeekBarSelected = true;
    private int progerssFwRwind;
    private boolean haveLeftRightOpration;
    private int time = 0;
    private boolean isClickBlueButton;


    private Dialog menuDialog = null;
    private Dialog secondMenuDialog = null;
    private Dialog thirdMenuDialog = null;
    private Dialog subSetDialog = null;
    private Dialog subtitleSetDialog = null;
    private Dialog pointDialog = null;
    private Dialog multiplayDialog = null;
    private Dialog multiplayListDialog = null;
    private Dialog notSptDialog;
    private int defaultFocus;
    private boolean isBeginFinish;

    private MenuConfig menuConfig;
    private SecondMenuConfig secondMenuConfig;
    private ThirdMenuConfig thirdMenuConfig;
    private List<MenuKeyValuePair> menuKeyValuePairs;
    private List<MenuKeyValuePair> secodeMenuKeyValuePairs;
    private List<MenuKeyValuePair> thirdMenuKeyValuePairs;

    private List<EncodeNameValue> encodeList;
    private int m3DSubtitle;
    private int hiType2;
    private double hiType1;

    private boolean isPipMode = false;
    private long timeNetworkErrToast = 0;
    private boolean isNewIntent = false;

    private boolean isNotSupportShow = false;

    @Override
    protected int getLayoutResID() {

        if (VersionControl.isIptvEnable()) {
            return R.layout.main_portrait_dpt_o;
        } else {
            return R.layout.main_dpt_o;
        }
    }

    @Override
    protected void initUi() {
        initWindowAttr();
        initView();
    }

    private void initWindowAttr() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = metrics.widthPixels;
        lp.height = metrics.heightPixels;
        lp.flags |= WindowManager.LayoutParams.FLAG_SCALED;
        this.getWindow().setAttributes(lp);
    }

    private void initView() {

        mediaInfoLayout = (RelativeLayout) findViewById(R.id.mediaInfo);
        btnLinearLayout = (LinearLayout) findViewById(R.id.btnLinearLayout);
        mediaControllerLayout = findViewById(R.id.mediaControllerLayout);
        mediaControllerLayout.setVisibility(View.INVISIBLE);
        frameLayout = (FrameLayout) findViewById(R.id.activity_main);
        frameLayout.setBackgroundColor(Color.BLACK);
        multiPlaySurfaceView = (MultiPlaySurfaceView) findViewById(R.id.multiPlaySurfaceView);

        multiPlayTextureView = (MultiPlayTextureView) findViewById(R.id.textureView);
        if (multiPlayTextureView == null) {
            generateTextureView();
            multiPlayTextureView.setVisibility(View.INVISIBLE);
        }
        subtitleView = (VideoView) findViewById(R.id.surface_view_subtitle);
        subtitleView.setZOrderMediaOverlay(true);
        videoView = (HisiVideoView) findViewById(R.id.videoView);
        videoView.setSubtitleView(subtitleView);
        tvMediaName = (TextView) findViewById(R.id.mediaName);
        tvMediaSize = (TextView) findViewById(R.id.mediaSize);
        tvSub = (TextView) findViewById(R.id.sub);
        tvAudio = (TextView) findViewById(R.id.audio);
        ivPlayStatus = (ImageView) findViewById(R.id.playStauts_ImageView);
        tvTime = (TextView) findViewById(R.id.timeText);
        tvTotalTime = (TextView) findViewById(R.id.timetotal);
        mySeekBar = (MySeekBar) findViewById(R.id.videoSeekBar);
        ivVolumeCtl = (ImageView) findViewById(R.id.volume_ctl);
        ivPageUp = (ImageView) findViewById(R.id.page_up);
        ivRewind = (ImageView) findViewById(R.id.rewind);
        ivForward = (ImageView) findViewById(R.id.forward);
        ivPlayPause = (ImageView) findViewById(R.id.play_pause);
        ivPageDown = (ImageView) findViewById(R.id.page_down);
        ivMenu = (ImageView) findViewById(R.id.menu);
        ivMenu.setNextFocusRightId(R.id.volume_ctl);
        ivMenu.setNextFocusLeftId(R.id.page_up);
        ivVolumeCtl.setNextFocusRightId(R.id.page_down);
        ivVolumeCtl.setNextFocusLeftId(R.id.menu);
        ivDolbyAtmos = (ImageView) findViewById(R.id.iv_dolby);
        mDolby = (TextView) findViewById(R.id.dolby);
        if (mDolby != null) mDolby.setVisibility(View.GONE);
        //String brandName = SystemProperties.get(VersionControl.PRODUCT_BRAND);
        //if (!TextUtils.isEmpty(brandName) && brandName.startsWith(VersionControl.BRAND_HIDPT)) { // DPT 分支
        if (Build.VERSION.SDK_INT >= 26) {
            ivMenu.setVisibility(View.VISIBLE);
        }
        //}
        crossView = (CrossView) findViewById(R.id.crossView);
        if (crossView != null) {
            crossView.hideAllArea();
            crossView.setIAreaClickListener(crossViewListener);
        }
        mouseView = (MouseView) findViewById(R.id.mouseView);
        if (mouseView != null) {
            mouseView.setVisibility(View.GONE);
        }
        if (SystemProperties.getInt("persist.sys.video.cvrs", 1) == 0) {
            isFullScreen = true;
        } else {
            isFullScreen = false;
        }
        if (videoView.setVideoCvrs(Constants.SCREEN_FULL) != -1) {
            setVideoScale(Constants.SCREEN_FULL);
        }

        tvSubtitleForApp = new TextView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 20);
        tvSubtitleForApp.setLines(2);
        tvSubtitleForApp.setPadding(10, 10, 10, 10);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        tvSubtitleForApp.setLayoutParams(layoutParams);
        tvSubtitleForApp.setTextColor(Color.WHITE);
        frameLayout.addView(tvSubtitleForApp);
    }

    private void initPlayerProp() {
        if (SystemProperties.get("ro.dolby.dmacert.enable").equals("true") || SystemProperties.get("ro.dolby.iptvcert.enable").equals("true")) {
            playerController.setDolbyCertification(1);
        } else {
            playerController.setDolbyCertification(0);
        }
        if (SystemProperties.get("persist.sys.audio.dobly.output").equals("true")) {
            playerController.setDolbyRangeInfo(1);
        } else if (SystemProperties.get("persist.sys.audio.dobly.output").equals("false")) {
            playerController.setDolbyRangeInfo(0);
        }
        if ("true".equals(SystemProperties.get("persist.sys.video.adaptformat"))) {
            playerController.setFormatAdaption(1);
        } else {
            playerController.setFormatAdaption(0);
        }
        if (common.sharedPreferencesOpration(Constants.SHARED, "3DMVCAdapte", 0, 1, false) == 1) {
            playerController.setmVC3DAdapte(1);
        } else {
            playerController.setmVC3DAdapte(0);
        }
        initCommon_ControlModel();
    }

    /**
     * init Common  and ControlModel
     */
    protected void initCommon_ControlModel() {
        int selectedAudio = common.sharedPreferencesOpration(Constants.SHARED, "channer", 0, 0, false);
        controlModel.setSelectedAudio(selectedAudio);
        int selectedSubEncode = common.sharedPreferencesOpration(Constants.SHARED, "subtitleEncode", 0, 0, false);
        controlModel.setSelectedSubEncode(selectedSubEncode);
        int selectedSizes = common.sharedPreferencesOpration(Constants.SHARED, "subtitleSizes", 0, 25, false);
        controlModel.setSelectedSizes(selectedSizes);
        int selectedColorPos = common.sharedPreferencesOpration(Constants.SHARED, "selectedColor", 0, 0, false);
        controlModel.setSelectedColorPos(selectedColorPos);
        int selectedPosition = common.sharedPreferencesOpration(Constants.SHARED, "selectedPosition", 0, 36, false);
        controlModel.setSelectedPosition(selectedPosition);
        int selectedEffect = common.sharedPreferencesOpration(Constants.SHARED, "selectedEffect", 0, 0, false);
        controlModel.setSelectedEffect(selectedEffect);
        int selectedSpace = common.sharedPreferencesOpration(Constants.SHARED, "selectedSpace", 0, 0, false);
        controlModel.setSelectedSpace(selectedSpace);
        int selectedLSpace = common.sharedPreferencesOpration(Constants.SHARED, "selectedLSpace", 0, 0, false);
        controlModel.setSelectedSpace(selectedLSpace);
        int selectedVolume = common.sharedPreferencesOpration(Constants.SHARED, "selectedVolume", 0, 100, false);
        controlModel.setSelectedVolume(selectedVolume);
        int selectedDolbyRangeInfo = common.sharedPreferencesOpration(Constants.SHARED, "selectedDolbyRangeInfo", 0, 100, false);
        controlModel.setSelectedDolbyRangeInfo(selectedDolbyRangeInfo);
        String selectedColor = colorValue[selectedColorPos];
        controlModel.setSelectedColor(selectedColor);
        playerController.setSubtitleOn(common.sharedPreferencesOpration(Constants.SHARED, "subtitle_on_off", 1, 0, false) == 0);
    }

    private void setVideoScale(int flag) {
        //Log.i(TAG, "screenWidth:" + screenWidth + " screenHeight:" + screenHeight);
        switch (flag) {
            case Constants.SCREEN_FULL:
                subtitleTopMargin = 0;
                videoView.setVideoScale(screenWidth, screenHeight);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                setSubtitleViewPar(screenWidth, screenHeight);
                break;
            case Constants.SCREEN_DEFAULT:
                subtitleTopMargin = 60;
                int videoWidth = videoView.getDefaultWidth();
                int videoHeight = videoView.getDefaultHeight();
                int mHeight = screenHeight - 2 * mediaControllerLayout.getLayoutParams().height - 43;
                int mWidth = screenWidth * mHeight / screenHeight;
                if ((videoWidth > 0) && (videoHeight > 0)) {
                    if (videoWidth * mHeight > mWidth * videoHeight) {
                        mHeight = mWidth * videoHeight / videoWidth;
                    } else if (videoWidth * mHeight <= mWidth * videoHeight) {
                        mWidth = videoWidth * mHeight / videoHeight;
                    }
                }
                videoView.setVideoScale(mWidth, mHeight);
                setSubtitleViewPar(mWidth, mHeight);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case Constants.SCREEN_ONESIDE_FULL:
                subtitleTopMargin = 60;
                videoWidth = videoView.getDefaultWidth();
                videoHeight = videoView.getDefaultHeight();
                mHeight = screenHeight;
                mWidth = screenWidth;
                if ((videoWidth > 0) && (videoHeight > 0)) {
                    if (videoWidth * mHeight > mWidth * videoHeight) {
                        mHeight = mWidth * videoHeight / videoWidth;
                    } else if (videoWidth * mHeight <= mWidth * videoHeight) {
                        mWidth = videoWidth * mHeight / videoHeight;
                    }
                }
                videoView.setChangeableVideoScale(mWidth, mHeight, screenWidth, screenHeight);
                setSubtitleViewPar(mWidth, mHeight);
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
        }
    }

    protected void setSubtitleViewPar(int width, int height) {    //width not used because Left and right video distances are always equal ps:ignore pip mode
        ViewGroup.LayoutParams lp = subtitleView.getLayoutParams();
        lp.width = screenWidth;
        lp.height = screenHeight - subtitleMarginVertical;
        subtitleView.setLayoutParams(lp);
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(subtitleView.getLayoutParams());
        int x = 0;
        int y = (screenHeight - height) / 2;
        if (isPipMode) {
            margin.setMargins(x, 0, x, 0);
        } else {
            margin.setMargins(x, 30 + y + margin.topMargin - subtitleTopMargin, x, 30 + y + margin.bottomMargin);
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        subtitleView.setLayoutParams(layoutParams);
        subtitleView.invalidate();
    }

    @Override
    protected void setListener() {
        mySeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        videoView.setOnPreparedListener(preparedListener);
        videoView.setOnCompletionListener(completionListener);
        videoView.setOnErrorListener(errorListener);
        videoView.setOnInfoListener(infoListener);
        videoView.setOnPlayerDestroyListener(playerDestroyListener);
        videoView.setOnSeekCompleteListener(seekCompleteListener);
        videoView.setmOnTimedTextListener(onTimedTextListener);

        String brand_name = SystemProperties.get("ro.product.brand");
        if (brand_name.startsWith("HiDPT")) {
            if (PlayerJugdment.isGstreamer()) {
                videoView.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
            }
        }
        ivVolumeCtl.setOnClickListener(clickListener);
        ivPlayPause.setOnClickListener(clickListener);
        ivForward.setOnClickListener(clickListener);
        ivRewind.setOnClickListener(clickListener);
        ivMenu.setOnClickListener(clickListener);
        ivPageDown.setOnClickListener(clickListener);
        ivPageUp.setOnClickListener(clickListener);
        if (multiPlaySurfaceView != null) {
            multiPlaySurfaceView.setOnErrorListener(multiplePlayerOnErrorListener);
            multiPlaySurfaceView.setOnInfoListener(multiplePlayerOnInfoListener);
            multiPlaySurfaceView.setMultiplePlayerOnDestoryFinish(multiplePlayerOnDestoryFinishSurfaceView);
        }
        if (multiPlayTextureView != null) {
            multiPlayTextureView.setOnErrorListener(multiplePlayerOnErrorListener);
            multiPlayTextureView.setOnInfoListener(multiplePlayerOnInfoListener);
            multiPlayTextureView.setMultiplePlayerOnDestoryFinish(multiplePlayerOnDestoryFinish);
        }
    }


    private void beginFinish() {
        isBeginFinish = true;
        if (multiPlayStyle == DptOMultipleListDialog.STYLE_TEXTUREVIEW) {
            multiPlayTextureView.beginDestory();
        } else if (multiPlayStyle == DptOMultipleListDialog.STYLE_SURFACEVIEW) {
            multiPlaySurfaceView.beginDestory();
        }

    }


    MultiPlayTextureView.MultiplePlayerOnDestoryFinish multiplePlayerOnDestoryFinish = new MultiPlayTextureView.MultiplePlayerOnDestoryFinish() {
        @Override
        public void multiplePlayerOnDestoryFinish() {
            if (isBeginFinish) {
                isBeginFinish = false;
                finish();
            }
        }

        @Override
        public void onPrepared() {
            if (multiPlayTextureView != null) {
                multiPlayTextureView.setVisibility(View.VISIBLE);
            }
        }
    };
    MultiPlaySurfaceView.MultiplePlayerOnDestoryFinish multiplePlayerOnDestoryFinishSurfaceView = new MultiPlaySurfaceView.MultiplePlayerOnDestoryFinish() {
        @Override
        public void multiplePlayerOnDestoryFinish() {
            if (isBeginFinish) {
                isBeginFinish = false;
                finish();
            }
        }
    };
    MediaPlayer.OnInfoListener multiplePlayerOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            int messageId;
            if (what == HiMediaPlayer.MEDIA_INFO_VIDEO_FAIL) {
                String brand_name = SystemProperties.get("ro.product.brand");
                if (brand_name.startsWith("HiDPT")) { //only DPT need handle this info
                    if (multiPlayTextureView.isShown()) {
                        multiPlayTextureView.destroyPlayer();
                    }
                    if (multiPlaySurfaceView.isShown()) {
                        multiPlaySurfaceView.destroyPlayer();
                    }
                    messageId = getSystemResId("VideoView_error_text_unknown", "string");
                    notSupportDialog(messageId);
                }
            }
            return false;
        }
    };

    MediaPlayer.OnErrorListener multiplePlayerOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogTool.d("onError(what=" + what + ", extra=" + extra + ")");
            int messageId;
            if (null == mp) {
                return false;
            }
            if (what == HiMediaPlayer.MEDIA_ERROR_INVALID_OPERATION) {
                ToastUtil.showMessage(VideoActivity.this, R.string.valid_operation);
                return true;
            }
            if (mediaFileListService != null) {
                mediaFileListService.setStopFlag(true);
            }

            if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                messageId = getSystemResId("VideoView_error_text_invalid_progressive_playback", "string");
            } else {
                messageId = getSystemResId("VideoView_error_text_unknown", "string");
            }

            notSupportDialog(messageId);
            return true;
        }
    };
    private boolean isTextureViewAvailble = false;
    DptOMultipleListDialog.MultipleClickListener multipleClickListener = new DptOMultipleListDialog.MultipleClickListener() {
        @Override
        public void VideoClick(String path, int style) {
            isFirstPlay = false;
            multiPlayStyle = style;
            LogTool.d("VideoClick Path : " + path + " Style ： " + style);
            if (style == DptOMultipleListDialog.STYLE_TEXTUREVIEW) {
                if (isTextureViewAvailble) {
                    generateTextureView();
                    multiPlayTextureView.setVideoPath(path);
                    multiPlayTextureView.start();
                } else {
                    generateTextureView();
                    multiPlayTextureView.setVisibility(View.VISIBLE);
                    multiPlayTextureView.setVideoPath(path);
                    isTextureViewAvailble = true;
                }
            } else {
                multiPlaySurfaceView.setVideoPath(path);
                multiPlaySurfaceView.setVisibility(View.VISIBLE);
            }
            ivMenu.setBackgroundResource(R.drawable.multiple_on_button);
            isMultiplePlay = true;
            multiplayListDialog.dismiss();
        }
    };
    MultipleListDialog.MultipleClickListener textureViewMultipleClickListener = new MultipleListDialog.MultipleClickListener() {
        @Override
        public void VideoClick(String path) {
            isFirstPlay = false;
            if (multiPlayTextureView != null) {
                if (isTextureViewAvailble) {
                    generateTextureView();
                    multiPlayTextureView.setVideoPath(path);
                    multiPlayTextureView.setVisibility(View.VISIBLE);
                    multiPlayTextureView.start();
                } else {
                    generateTextureView();
                    multiPlayTextureView.setVideoPath(path);
                    multiPlayTextureView.setVisibility(View.VISIBLE);
                    isTextureViewAvailble = true;
                }
            }
            ivMenu.setBackgroundResource(R.drawable.multiple_on_button);
            isMultiplePlay = true;
            multiplayListDialog.dismiss();
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (playerController.isRewindOrForward()) {   //recover status if rewind or forward
                    resetFastSpeedToNomal();
                    //videoView.resume();/*move to ResetFastSpeedToNomal()*/
                }
                startTime = System.currentTimeMillis();
                videoView.seekTo(progress);
                if (videoView.isPlaying()) {
                    playerController.setSeekWhenPlaying(true);
                } else {
                    playerController.setSeekWhenPlaying(false);
                }
            }
            tvTime.setText(Common.getTimeFormatValue(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private boolean isAfterReturn = true;
    private int lastPosition;
    private int isShowSub;
    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final MediaPlayer mp) {
            speedChoiced = -1;
            realVideoWidth = mp.getVideoWidth();
            realVideoHeight = mp.getVideoHeight();
            xRatioFor8K = calculationPointXRatio(realVideoWidth);
            yRatioFor8K = calculationPointYRation(realVideoHeight);
            //Log.d(TAG, "onPrepared()");
            inflateAudioInfo();
            inflateSubtitleInfo();
            updateDraInfo();
            setVideoCvrs(); /*for STB:conflict with setting 'Add black side'*/
            setVideoStep();
            checkDbData(playerController.getCurrPlayPath());
            Parcel mediaInfo = videoView.getMediaInfo();
            if (mediaInfo == null) {
                LogTool.e("videoView.getMediaInfo() error!");
                return;
            }
            mediaInfo.readInt();
            playerController.setFormat(mediaInfo.readInt());
            mediaInfo.readInt();
            mediaInfo.readInt();//framework himediaplayer changed,use new
            if (PlayerJugdment.isHiPlayer()) {
                long mSize = mediaInfo.readLong();
                playerController.setCurrSize(mSize);
            } else {
                File mediaFile = new File(playerController.getCurrPlayPath());
                if (mediaFile.exists() && mediaFile.isFile()) {
                    playerController.setCurrSize(mediaFile.length());
                }
            }
            getMkvInfo(mediaInfo);
            mediaInfo.recycle();
            isThreadStart = false;
            //show video message
            showVideoMessageAndControlLayout();

            startThread();
            if (isPipMode) {
                int position = common.getPipVideoPosition();
                if (position > 0) {
                    videoView.seekTo(position);
                    common.savePipVideoPosition(0);
                }
            } else {
                if (common.getPipVideoTag()) {
                    int position = common.getPipVideoPosition();
                    if (position > 0) {
                        videoView.seekTo(position);
                        common.savePipVideoPosition(0);
                    }
                }
            }
            if (!isAfterReturn && !isPipMode) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder.setTitle(R.string.continue_playvideo);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                lastPosition = getLastPosition(playerController.getCurrPath());
                                mp.seekTo(lastPosition);
                                start();
                                if (playerController.getDolbyCertification() == 1) {
                                    getDuration();
                                    nameSizeHandler.postDelayed(dolbyDisplayRunnable, 3000);
                                } else {
                                    getDuration();
                                    doExtraOpration();
                                }
                                isAfterReturn = true;
                                initSDRMode();
                                if (playerController.getFormat() != HI_VIDEOFORMAT_TYPE_MVC) {
                                    if (playerController.getmCurrMode() != 0) {
                                        videoView.resetVideoFmt();
                                        hiDisplayManager.reset3DMode();
                                    }
                                } else {
                                    if (playerController.getmCurrMode() != 1) {
                                        videoView.resetVideoFmt();
                                        hiDisplayManager.reset3DMode();
                                    }
                                }
                            }
                        });
                builder.setNegativeButton(R.string.not,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                lastPosition = getLastPosition(playerController.getCurrPath());
                                start();
                                if (playerController.getDolbyCertification() == 1) {
                                    getDuration();
                                    nameSizeHandler.postDelayed(dolbyDisplayRunnable, 3000);
                                } else {
                                    getDuration();
                                    doExtraOpration();
                                }
                                isAfterReturn = true;
                                initSDRMode();
                                if (playerController.getFormat() != HI_VIDEOFORMAT_TYPE_MVC) {
                                    if (playerController.getmCurrMode() != 0) {
                                        initDisplayModeAndVideoFmt();
                                        playerController.setmCurrMode(0);
                                    }
                                } else {
                                    if (playerController.getmCurrMode() != 1) {
                                        initDisplayModeAndVideoFmt();
                                        playerController.setmCurrMode(1);
                                    }
                                }
                            }
                        });
                pointDialog = builder.create();
                pointDialog.getWindow().setDimAmount(0);
                pointDialog.show();
                return;
            }
            initDisplayModeAndVideoFmt();
            if (common.isValueTrue(SecondMenuConfig.ADVANCED_CONTINUE_PLAY)) {
                if (common.isValueTrue(SecondMenuConfig.ADVANCED_CONTINUE_PLAY) && (
                        lastPosition = getLastPosition(playerController.getCurrPlayPath())) / 1000 > 0 && (!isPipMode && !common.getPipVideoTag())) {
                    getFlashPlayDialog();
                } else {
                    start();
                    if (playerController.getDolbyCertification() == 1) {
                        getDuration();
                        nameSizeHandler.postDelayed(dolbyDisplayRunnable, 3000);
                    } else {
                        getDuration();
                        doExtraOpration();
                    }
                    if (openFromMark > 0) {
                        videoView.seekTo(openFromMark);
                        openFromMark = -1;
                    }
                }
            } else {
                start();
                if (playerController.getDolbyCertification() == 1) {
                    getDuration();
                    nameSizeHandler.postDelayed(dolbyDisplayRunnable, 3000);
                } else {
                    getDuration();
                    doExtraOpration();
                }
                if (openFromMark > 0) {
                    videoView.seekTo(openFromMark);
                    openFromMark = -1;
                }
            }
            dismissProgressDialog();
            initSDRMode();
//            if (isPipMode) {
//                int isShowSub = 1;
//                videoView.enableSubtitle(isShowSub);
//            } else {
            videoView.enableSubtitle(common.sharedPreferencesOpration(Constants.SHARED, "subtitle_on_off", 1, 0, false));
//            }
            //operater.updatePlayTime(VideoActivity.this, getCurrId);
            saveKeyCutSourcePath(playerController.getCurrPath());
            if (common.isValueTrue(SecondMenuConfig.ADVANCED_FULL_SCREEN)) {
                if (videoView.setVideoCvrs(Constants.SCREEN_FULL) != -1) {
                    setVideoScale(Constants.SCREEN_FULL);
                }
            } else {
                setVideoScale(Constants.SCREEN_ONESIDE_FULL);
            }
            if (!isPipMode && common.getPipVideoTag()) {
                common.savePipVideoTag(false);
            }
        }
    };

    private void showVideoMessageAndControlLayout() {
        if (isPipMode) {
            return;
        }
        LogTool.d(VideoActivity.class.getSimpleName(), "showVideoMessageAndControlLayout");
        mediaInfoLayout.setVisibility(View.VISIBLE);
        mediaControllerLayout.setVisibility(View.VISIBLE);
    }


    private void disMediaMsgAndControlLayout() {
        mediaControllerLayout.setVisibility(View.INVISIBLE);
        mediaInfoLayout.setVisibility(View.INVISIBLE);
    }

    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            //Log.d(TAG, "onCompletion()");
            //isContinue = false;
            if (isThreadStart) {
                ctrlbarDismiss();
            }
            VideoActivity.this.removeAtmos();
            mHandler.removeCallbacksAndMessages(null);
            Common.setShowLoadingToast(false);
            updatePositon(playerController.getCurrPlayPath());

            mp.setLooping(false);
            if (common.getMode() != Constants.ONECYCLE) {
                videoView.clearDraw();
            }
            LogTool.d("player---: mode : " + common.getMode());
            if (common.getMode() == Constants.ALLNOCYCLE) {
                if (mediaFileList != null) {
                    getVideoInfo_noCycle(mediaFileList.getNextVideoInfo_NoCycle(null));
                    if (Common.isLastMediaFile()) {
                        finishPlayer();
                    } else {
                        controlModel.setSelectedSubId(0);
                        videoView.setVideoPath(playerController.getCurrPath());
                    }
                } else {
                    finishPlayer();
                }
            } else if (common.getMode() == Constants.ALLCYCLE) {
                if (mediaFileList != null) {
                    controlModel.setSelectedSubId(0);
                    getVideoInfo(mediaFileList.getNextVideoInfo(null));
                } else {
                    finishPlayer();
                }
            } else if (common.getMode() == Constants.ONECYCLE) {
                mp.setLooping(true);
                videoView.setVideoPath(playerController.getCurrPath());
                isTo3DOriginal = false;
                isTo2DOriginal = false;
            } else if (common.getMode() == Constants.ONENOCYCLE) {
                finishPlayer();
            } else if (common.getMode() == Constants.RANDOM) {
                if (mediaFileList != null) {
                    controlModel.setSelectedSubId(0);
                    getVideoInfo(mediaFileList.getRandomVideoInfo(null));
                } else {
                    finishPlayer();
                }
            } else {
                if (mediaFileList != null) {
                    controlModel.setSelectedSubId(0);
                    getVideoInfo(mediaFileList.getNextVideoInfo(null));
                } else {
                    finishPlayer();
                }
            }

            initSeekSecondaryProgress();

            videoView.setStereoVideoFmt(0);
            hiDisplayManager.setRightEyeFirst(0);
            hiDisplayManager.set3DMode(HI_2D_MODE, 0);
            playerController.setMvcType(0);
            playerController.setmCurrMode(0);
            common.dialogDismissAtOnce();
        }
    };

    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogTool.d(VideoActivity.class.getSimpleName(), "onError(what=" + what + ", extra=" + extra + ")");
            int messageId;

            if (null == mp) {
                return false;
            }
            if (what == HiMediaPlayer.MEDIA_ERROR_INVALID_OPERATION) {
                ToastUtil.showMessage(VideoActivity.this, R.string.valid_operation);
                if (videoView.isPlaying()) {
                    ivPlayPause.setBackgroundResource(R.drawable.pause_button);
                    isPause = false;
                } else {
                    ivPlayPause.setBackgroundResource(R.drawable.play_button);
                    isPause = true;
                }
                return true;
            }
            videoView.destroyPlayer();
            if (mediaFileListService != null) {
                mediaFileListService.setStopFlag(true);
            }

            if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                messageId = getSystemResId("VideoView_error_text_invalid_progressive_playback", "string");
            } else {
                messageId = getSystemResId("VideoView_error_text_unknown", "string");
            }
            notSupportDialog(messageId);
            return true;
        }
    };

    private MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            int messageId;
            LogTool.d(VideoActivity.class.getSimpleName(), "onInfo(what=" + what + ", extra=" + extra + ")");
            switch (what) {
                case HiMediaPlayer.MEDIA_INFO_FAST_BACKWORD_COMPLETE:
                    resetFastSpeedToNomal();
                    break;

                //Above androidP hiplayer will
                //MEDIA_INFO_AUDIO_FAIL=1000;
                //MEDIA_INFO_VIDEO_FAIL=1001;
                //The two modifications were 804 and 805 for native mediaplayer;
                //the code used to view mediaplayerd found that they are:
                //Public static final int MEDIA_INFO_AUDIO_NOT_PLAYING = 804;
                //Public static final int MEDIA_INFO_VIDEO_NOT_PLAYING = 805;
                //So add the native judgment in HiVideoPlayer's infoListener callback.

                //Since our original code only processed MEDIA_INFO_VIDEO_FAIL,
                //so only MEDIA_INFO_VIDEO_NOT_PLAYING was added here.
                case MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING:
                case HiMediaPlayer.MEDIA_INFO_VIDEO_FAIL: {
                    videoView.destroyPlayer();
//                    messageId = com.android.internal.R.string.VideoView_error_text_unknown;
                    messageId = getSystemResId("VideoView_error_text_unknown", "string");
                    notSupportDialog(messageId);
                }
                break;
                //Above androidP hiplayer will
                //MEDIA_INFO_UPDATE_FILE_INFO=1014;
                //The two modifications were 802 for native mediaplayer;
                //the code used to view mediaplayerd found that they are:
                //public static final int MEDIA_INFO_METADATA_UPDATE = 802;
                //So add the native judgment in HiVideoPlayer's infoListener callback.
                case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                case HiMediaPlayer.MEDIA_INFO_UPDATE_FILE_INFO:
                    inflateSubtitleInfo();
                    break;
                case HiMediaPlayer.MEDIA_INFO_NETWORK: {
                    long currTime = System.currentTimeMillis();
                    if (currTime - timeNetworkErrToast < 3000) {
                        break;
                    }
                    timeNetworkErrToast = currTime;
                    switch (extra) {
                        case HiMediaPlayer.MEDIA_INFO_NETWORK_ERROR_CONNECT_FAILED:
                            ToastUtil.showMessage(VideoActivity.this, getString(R.string.toastNetworkConnectFail));
                            break;
                        case HiMediaPlayer.MEDIA_INFO_NETWORK_ERROR_TIMEOUT:
                            ToastUtil.showMessage(VideoActivity.this, getString(R.string.toastNetworkTimeout));
                            break;
                        case HiMediaPlayer.MEDIA_INFO_NETWORK_ERROR_UNKNOW:
                            ToastUtil.showMessage(VideoActivity.this, getString(R.string.toastNetworkUnknowError));
                            break;
                        case HiMediaPlayer.MEDIA_INFO_NETWORK_ERROR_DISCONNECT:
                            ToastUtil.showMessage(VideoActivity.this, getString(R.string.toastNetworkDisconnect));
                            break;
                        case HiMediaPlayer.MEDIA_INFO_NETWORK_ERROR_NOT_FOUND:
                            ToastUtil.showMessage(VideoActivity.this, getString(R.string.toastNetworkResourceNotFound));
                            break;
                        case HiMediaPlayer.MEDIA_INFO_NETWORK_NORMAL:
                        default:
                            break;
                    }
                }
                break;
            }
            return false;
        }
    };

    private MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
            int videoWidth = width;
            int videoHeight = height;
            int mHeight = screenHeight;
            int mWidth = screenWidth;
            if ((videoWidth > 0) && (videoHeight > 0)) {
                if (videoWidth * mHeight > mWidth * videoHeight) {
                    mHeight = mWidth * videoHeight / videoWidth;
                } else if (videoWidth * mHeight <= mWidth * videoHeight) {
                    mWidth = videoWidth * mHeight / videoHeight;
                }
            }
            videoView.setChangeableVideoScale(mWidth, mHeight, screenWidth, screenHeight);
            setSubtitleViewPar(mWidth, mHeight);
        }
    };

    private OnPlayerDestroyListener playerDestroyListener = new OnPlayerDestroyListener() {
        @Override
        public void onPlayerDestroy() {
            openFromMark = videoView.getCurrentPosition();
            saveCurrentPosition();
        }
    };

    private double timeSetRewind;
    private double timeSetForward;
    private MediaPlayer.OnSeekCompleteListener seekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            //Log.d(TAG, "onSeekComplete()");
            if (System.currentTimeMillis() - timeSetRewind < 300 || System.currentTimeMillis() - timeSetForward < 300) {
                //onSeekComplete event may delay 100-200ms, when press fastplay key during this 100-200ms, it will resume.so keep fastplay here.
                return;
            }
            initSeekSecondaryProgress();
            if (playerController.isSeekWhenPlaying()) {
                playerController.setSeekWhenPlaying(false);
                videoStart();
                ivPlayPause.setBackgroundResource(R.drawable.pause_button);
            }
        }
    };
    private MediaPlayer.OnTimedTextListener onTimedTextListener = new MediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(MediaPlayer mediaPlayer, TimedText timedText) {
            if (timedText != null && !TextUtils.isEmpty(timedText.getText())) {
                tvSubtitleForApp.setText(timedText.getText());
                LogTool.d("APK LOG " + timedText.getText());
            } else {
                tvSubtitleForApp.setText("");
                LogTool.d("APK LOG APK Clear");
            }
        }
    };
    CrossView.IAreaClickListener crossViewListener = new CrossView.IAreaClickListener() {
        @Override
        public void areaClick(CrossView.AREAID ID) {
            int halfOfRealVideoWidth = realVideoWidth / 2;
            int halfOfRealVideoHeight = realVideoHeight / 2;
            int ret = 0;
            switch (ID) {
                case LEFT_TOP:
                    ret = videoView.setVideoRect(0, 0, halfOfRealVideoWidth, halfOfRealVideoHeight);
                    break;
                case RIGHT_TOP:
                    ret = videoView.setVideoRect(halfOfRealVideoWidth, 0, halfOfRealVideoWidth, halfOfRealVideoHeight);
                    break;
                case LEFT_BOTTOM:
                    ret = videoView.setVideoRect(0, halfOfRealVideoHeight, halfOfRealVideoWidth, halfOfRealVideoHeight);
                    break;
                case RIGHT_BOTTOM:
                    ret = videoView.setVideoRect(halfOfRealVideoWidth, halfOfRealVideoHeight, halfOfRealVideoWidth, halfOfRealVideoHeight);
                    break;
            }
            if (ret == 0) {
                crossView.hideAllArea();
            }
        }
    };
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rewind:
                    setRewind();
                    startTime = System.currentTimeMillis();
                    break;
                case R.id.forward:
                    setForward();
                    startTime = System.currentTimeMillis();
                    break;
                case R.id.page_down:
                    if (playerController.isRewindOrForward()) {
                        ivPlayStatus.setVisibility(View.INVISIBLE);
                        controlModel.setForwardRate(1);
                        controlModel.setRewindRate(1);
                        playerController.setRewindOrForward(false);
                        videoView.resume();
                        ivPlayPause.setBackgroundResource(R.drawable.pause_button);
                    }

                    if (mediaFileList != null) {
                        nameSizeHandler.removeCallbacks(nameSizeDismissRunnable);
                        Common.setResume(false);
                        ivPlayPause.setBackgroundResource(R.drawable.pause_button);
                        initSeekSecondaryProgress();
                        Common.setShowLoadingToast(true);
                        videoView.clearDraw();
                        getVideoInfo(mediaFileList.getNextVideoInfo(null));
                    }
                    break;
                case R.id.page_up:
                    if (playerController.isRewindOrForward()) {
                        ivPlayStatus.setVisibility(View.INVISIBLE);
                        controlModel.setForwardRate(1);
                        controlModel.setRewindRate(1);
                        playerController.setRewindOrForward(false);
                        videoView.resume();
                        ivPlayPause.setBackgroundResource(R.drawable.pause_button);
                    }
                    if (mediaFileList != null) {
                        nameSizeHandler.removeCallbacks(nameSizeDismissRunnable);
                        Common.setResume(false);
                        ivPlayPause.setBackgroundResource(R.drawable.pause_button);
                        initSeekSecondaryProgress();
                        Common.setShowLoadingToast(true);
                        videoView.clearDraw();
                        getVideoInfo(mediaFileList.getPreVideoInfo(null));
                    }
                    break;
                case R.id.play_pause:
                    if (playerController.isRewindOrForward()) {
                        ivPlayStatus.setVisibility(View.INVISIBLE);
                        controlModel.setForwardRate(1);
                        controlModel.setRewindRate(1);
                        playerController.setRewindOrForward(false);
                        videoView.resume();
                        speedChoiced = -1;
                        ivPlayPause.setBackgroundResource(R.drawable.pause_button);
                    } else if (!haveLeftRightOpration) {
                        play_pause();
                    }
                    startTime = System.currentTimeMillis();
                    break;
                case R.id.volume_ctl:
                    subSeekbar(0, -230, R.string.volume_ctl, 100);
                    break;
                case R.id.menu:
                    //ro.prop.mulvideo.disable true does not support multiple channels of video
                    if (SystemProperties.get("ro.prop.mulvideo.disable", "false").equals("true")) {
                        Toast.makeText(VideoActivity.this, R.string.multiple_not_support, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    String brand_name = SystemProperties.get("ro.product.brand");
                    if (SystemProperties.get("media.hp.ff.hiplayer", "false").equals("false")) {
                        Toast.makeText(VideoActivity.this, R.string.multiple_not_support, Toast.LENGTH_SHORT).show();
                    } else if (multiPlayTextureView.getVisibility() != View.VISIBLE && multiPlaySurfaceView.getVisibility() != View.VISIBLE) {
                        if (mediaFileListService == null) {
                            ToastUtil.showMessage(VideoActivity.this, R.string.no_playlist_show);
                            return;
                        }
                        if (isFirstPlay) {
                            if (multiplayListDialog != null && multiplayListDialog.isShowing()) {
                                multiplayListDialog.dismiss();
                            }
                            multiplayListDialog = new DptOMultipleListDialog(VideoActivity.this,
                                    mediaFileListService.getList(),
                                    (multiPlayTextureView.getVisibility() == View.VISIBLE) &&
                                            (multiPlaySurfaceView.getVisibility() == View.VISIBLE),
                                    multipleClickListener, brand_name != null && brand_name.startsWith("HiDPT"));
                            multiplayListDialog.show();
                            return;
                        }
                        if (multiPlayStyle == DptOMultipleListDialog.STYLE_SURFACEVIEW) {
                            multiPlaySurfaceView.setVisibility(View.VISIBLE);
                        } else if (multiPlayStyle == DptOMultipleListDialog.STYLE_TEXTUREVIEW) {
                            multiPlayTextureView.setVisibility(View.VISIBLE);
                        }
                        ivMenu.setBackgroundResource(R.drawable.multiple_on_button);
                    } else {
                        showMultipleDialog();
                    }
                    break;
            }
        }
    };


    private AdapterView.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            common.updatestartTime1();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemClickListener menuClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            doMenuClick(position);
        }
    };

    private void doMenuClick(int position) {
        if (menuKeyValuePairs == null || menuKeyValuePairs.size() < position) {
            return;
        }
        MenuKeyValuePair keyValuePair = menuKeyValuePairs.get(position);
        if (keyValuePair == null) {
            return;
        }
        String key = keyValuePair.getKey();
        if (MenuConfig.JUMP_TIME.equals(key)) {
            common.setJumpToDialog(R.string.jumpto, videoView.getCurrentPosition(), videoView);
        } else if (MenuConfig.MODE_360.equals(key)) {
            // toPanoMode();
        } else if (MenuConfig.SPEED.equals(key)) {
            showSpeedDialog();
        } else if (MenuConfig.MODE_2D.equals(key)) {
            mode2d(keyValuePair);
        } else if (MenuConfig.MODE_3D.equals(key)) {
            mode3d(keyValuePair);
        } else if (MenuConfig.ADVANCED_OPTIONS.equals(key)) {
            showAdvancedOptions();
        } else if (MenuConfig.MODE_MKV.equals(key)) {
            showChapterListDialog();
        } else {
            secodeMenuKeyValuePairs = secondMenuConfig.getMenuConfigByKey(key);
            popUpSecondMenuDialog(keyValuePair.getValueResId());
        }
    }

    private void showChapterListDialog() {
        if (mkvInfos == null) {
            return;
        }
        AlertDialog.Builder _Builder = new AlertDialog.Builder(this);
        _Builder.setTitle(getString(R.string.dialogTitleChapterSelect,
                new Object[]{mkvInfos.size()}));
        _Builder.setSingleChoiceItems(titles, 0,
                new OnChapterListDialogClickListener());
        Dialog _Dialog = _Builder.show();
        DialogTool.disableBackgroundDim(_Dialog);
        DialogTool.setLayout(_Dialog, 450, 480);
    }

    private class OnChapterListDialogClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface pDialog, int pWhich) {
            resetFastSpeedToNomal();
            pDialog.dismiss();
            videoView.seekTo(mkvInfos.get(pWhich).getChapterTimeStart());
        }
    }

    protected void showAudioChannelListDialog() {
        String[] audioChannelList = getResources().getStringArray(R.array.audio_channel_mode);
        List<String> _List = new ArrayList<String>();
        for (int i = 0; i < audioChannelList.length; i++) {
            _List.add(audioChannelList[i]);
        }
        AlertDialog.Builder _Builder = new AlertDialog.Builder(this);
        _Builder.setTitle(getString(R.string.dialogTitleAudioChannelSelect, new Object[]
                {7}));
        String[] _Strings = new String[_List.size()];
        _Builder.setSingleChoiceItems(_List.toArray(_Strings),
                controlModel.getSelectedChannel(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface pDialog, int pWhich) {
                        doSwitchAudioChannel(pWhich);
                        pDialog.dismiss();
                    }
                });
        _Builder.setOnItemSelectedListener(selectedListener);
        Dialog _Dialog = getShowDialog(_Builder);
        DialogTool.disableBackgroundDim(_Dialog);
        DialogTool.setDefaultSelectDisplay(_Dialog);
        common.dialogAutoDismiss(_Dialog);
    }


    private void doSwitchAudioChannel(int pAudioId) {
        if (videoView.setAudioChannelPid(pAudioId) == 0) {
            controlModel.setSelectedChannel(pAudioId);
            videoView.setSelectAudioChannelId(pAudioId);
        }
    }

    protected void showAudioListDialog() {
        List<String> _List = videoView.getAudioTrackLanguageList();

        if (_List == null) {
            return;
        }

        AlertDialog.Builder _Builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        _Builder.setTitle(getString(R.string.dialogTitleAudioSelect, new Object[]
                {videoView.getAudioTrackNumber()}));
        String[] _Strings = new String[_List.size()];
        int checkedItem;
        if (playerController.getDolbyCertification() == 0) {
            checkedItem = videoView.getAudioTrackPid();
        } else {
            checkedItem = videoView.getSelectAudioTrackId();
        }
        _Builder.setSingleChoiceItems(_List.toArray(_Strings),
                checkedItem, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface pDialog, int pWhich) {
                        doSwitchAudio(pWhich);
                        pDialog.dismiss();
                    }
                });
        _Builder.setOnItemSelectedListener(selectedListener);
        Dialog _Dialog = getShowDialog(_Builder);
        DialogTool.disableBackgroundDim(_Dialog);
        DialogTool.setDefaultSelectDisplay(_Dialog);
        common.dialogAutoDismiss(_Dialog);
    }

    private AlertDialog getShowDialog(AlertDialog.Builder _Builder) {
        AlertDialog dialog = _Builder.show();
        dialog.getWindow().setDimAmount(0);
        return dialog;
    }

    protected void doSwitchAudio(int pAudioId) {
        if (videoView.setAudioTrackPid(pAudioId) == 0) {
            controlModel.setSelectedTrack(pAudioId);
            videoView.setSelectAudioTrackId(pAudioId);
            mHandler.sendEmptyMessageDelayed(Constants.GET_DOLBY_INFO,1000);
            ToastUtil.showMessage(this, getString(R.string.toastAudio, new Object[]
                    {videoView.getAudioTrackLanguageList().get(pAudioId)}), Toast.LENGTH_SHORT);
        } else {
            controlModel.setSelectedTrack(videoView.getAudioTrackPid());
            videoView.setSelectAudioTrackId(videoView.getAudioTrackPid());
            ToastUtil.showMessage(this, getString(R.string.switchFailed), Toast.LENGTH_SHORT);
        }
    }


    private void showAdvancedOptions() {
        AlertDialog.Builder _Builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        _Builder.setTitle(getString(R.string.AdvancedOptions));
        final List<MenuKeyValuePair> keyValuePairs = secondMenuConfig.loadAdvancedOptVp();
        boolean[] flags = new boolean[keyValuePairs.size()];
        for (int i = 0; i < keyValuePairs.size(); i++) {
            if (SecondMenuConfig.ADVANCED_SINGLE_CYCLY.equals(keyValuePairs.get(i).getKey())) {
                flags[i] = common.getMode() == Constants.ONECYCLE;
                continue;
            }
            flags[i] = common.sharedPreferencesOpration(Constants.SHARED, keyValuePairs.get(i).getKey(), 0, 0, false) == 1;
        }
        _Builder.setMultiChoiceItems(R.array.setting_items_dpt_high, flags, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface pDialog, int pWhich, boolean isChecked) {
                common.updatestartTime1();
                String key = keyValuePairs.get(pWhich).getKey();
                if (SecondMenuConfig.ADVANCED_CONTINUE_PLAY.equals(key)) {
                    common.sharedPreferencesOpration(Constants.SHARED,
                            SecondMenuConfig.ADVANCED_CONTINUE_PLAY, isChecked ? 1 : 0, 0, true);
                } else if (SecondMenuConfig.ADVANCED_FORWARD_REWIND.equals(key)) {
                    showForwardRewind(isChecked);
//                        ivRewind.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
//                        ivForward.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                    common.sharedPreferencesOpration(Constants.SHARED,
                            SecondMenuConfig.ADVANCED_FORWARD_REWIND, isChecked ? 1 : 0, 0, true);
                } else if (SecondMenuConfig.ADVANCED_FULL_SCREEN.equals(key)) {
                    if (isChecked) {
                        if (videoView.setVideoCvrs(Constants.SCREEN_FULL) != -1) {
                            setVideoScale(Constants.SCREEN_FULL);
                        }
                        isFullScreen = true;
                        common.sharedPreferencesOpration(Constants.SHARED,
                                SecondMenuConfig.ADVANCED_FULL_SCREEN, 1, 0, true);
                    } else {
                        setVideoScale(Constants.SCREEN_ONESIDE_FULL);
                        isFullScreen = false;
                        common.sharedPreferencesOpration(Constants.SHARED,
                                SecondMenuConfig.ADVANCED_FULL_SCREEN, 0, 0, true);
                    }
                } else if (SecondMenuConfig.ADVANCED_SINGLE_CYCLY.equals(key)) {
                    common.setMode(isChecked ? Constants.ONECYCLE : Constants.ALLNOCYCLE);
                    common.sharedPreferencesOpration(Constants.SHARED,
                            SecondMenuConfig.ADVANCED_SINGLE_CYCLY,
                            isChecked ? Constants.ONECYCLE : Constants.ALLNOCYCLE, 0, true);
                } else if (SecondMenuConfig.ADVANCED_3D_MVC_ADPTION.equals(key)) {
                    common.sharedPreferencesOpration(Constants.SHARED,
                            SecondMenuConfig.ADVANCED_3D_MVC_ADPTION, isChecked ? 1 : 0, 0, true);
                } else if (SecondMenuConfig.ADVANCED_SDR_MODE.equals(key)) {
                    videoView.setHDRInvoke(isChecked ? 0 : 1);
                    common.sharedPreferencesOpration(Constants.SHARED,
                            SecondMenuConfig.ADVANCED_SDR_MODE, isChecked ? 1 : 0, 0, true);
                } else if (SecondMenuConfig.ADVANCED_DTS_DRC.equals(key)) {
                } else {
                }
            }
        });
        _Builder.setOnItemSelectedListener(selectedListener);
        _Builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (menuDialog != null && !menuDialog.isShowing()) {
                    menuDialog.show();
                }
            }
        });
        Dialog _Dialog = getShowDialog(_Builder);
        DialogTool.disableBackgroundDim(_Dialog);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        DialogTool.setLayout(_Dialog, width / 3, height / 2);
//        DialogTool.setDefaultSelectDisplay(_Dialog);
        common.dialogAutoDismiss(_Dialog);
    }


    private boolean isTo2DOriginal = false;
    private boolean isTo3DOriginal = false;

    private void mode2d(MenuKeyValuePair keyValuePair) {
        secodeMenuKeyValuePairs = isTo2DOriginal ? secondMenuConfig.load2Dto2DOriginalVp() : secondMenuConfig.load2Dto2DAfterVp();
        popUpSecondMenuDialog(keyValuePair.getValueResId());

    }

    private void mode3d(MenuKeyValuePair keyValuePair) {
        secodeMenuKeyValuePairs = isTo3DOriginal ?
                (isTo2DOriginal ? secondMenuConfig.load2Dto2DOriginalVp() : secondMenuConfig.load3Dto2DVp())
                : secondMenuConfig.load2Dto3DVp();
        popUpSecondMenuDialog(keyValuePair.getValueResId());
    }


    //360mode
    /*
    private synchronized void toPanoMode() {
        String brand_name = SystemProperties.get("ro.product.brand");
        if (brand_name.startsWith("HiDPT")) {
            LogTool.i("DPT not support this function !");
            return;
        }
        if (menuDialog != null && menuDialog.isShowing()) {
            menuDialog.dismiss();
        }
        Intent intent = new Intent(this, PanoramicActivity.class);
        intent.putExtra("videopath", playerController.getCurrPlayPath());
        startActivity(intent);
        finish();
    } */

    private void showSpeedDialog() {
        AlertDialog.Builder _Builder = new AlertDialog.Builder(new ContextThemeWrapper(this,
                R.style.AlertDialogCustom));
        _Builder.setTitle(getString(R.string.dialogTitleSpeedSelect));
        String[] speedArray = getResources().getStringArray(R.array.video_speed);
        _Builder.setSingleChoiceItems(speedArray, speedChoiced, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface pDialog, int pWhich) {
                doSwitchSpeed(pWhich);
                pDialog.dismiss();
            }
        });
        _Builder.setOnItemSelectedListener(selectedListener);
        _Builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // popUpMenuDialog();
            }
        });
        Dialog _Dialog = getShowDialog(_Builder);
        DialogTool.disableBackgroundDim(_Dialog);
        DialogTool.setDefaultSelectDisplay(_Dialog);
        common.dialogAutoDismiss(_Dialog);
    }

    private void doSwitchSpeed(int p) {
        if (!(videoView.isPlaying())) {
            videoView.start();
        }
        if (playerController.isRewindOrForward()) {
            ivPlayStatus.setVisibility(View.INVISIBLE);
            controlModel.setForwardRate(1);
            controlModel.setRewindRate(1);
        } else {
            ivPlayPause.setBackgroundResource(R.drawable.play_button);
        }
        speedChoiced = p;
        String[] speedArray = getResources().getStringArray(R.array.video_speed);
        double tempSpeed = Double.parseDouble(speedArray[p]);
        videoView.setSpeed(tempSpeed);
        playerController.setRewindOrForward(true);//The same principle with fast forward and rewind
    }

    private ShooterApiQuery.SearchResultListener mSearchResultListener = new ShooterApiQuery.SearchResultListener() {
        public void onSearchResult(String path, ArrayList<ShooterSubinfo> sublist) {
            //Log.i(TAG,"onSearchResult:"+path);
            //Log.i(TAG,"onSearchResult:"+sublist);
            dismissProgressDialog();
            if (sublist == null || sublist.size() == 0) {
                ToastUtil.showMessage(VideoActivity.this, getString(R.string.searchSubtitleFailed), Toast.LENGTH_SHORT);
                return;
            } else {
                mShooterSubinfoList.clear();
                mShooterSubinfoList = sublist;
                Message msg = mShooterHandler.obtainMessage(0);
                mShooterHandler.sendMessage(msg);
            }
        }
    };


    protected Handler mShooterHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    List<String> mShooterSubList = new ArrayList<String>();

                    for (int i = 0; i < mShooterSubinfoList.size(); i++) {
                        ShooterSubinfo tempSubinfo = mShooterSubinfoList.get(i);
                        StringBuffer _Buf = new StringBuffer();
                        _Buf.append(i + 1);
                        _Buf.append(".");
                        _Buf.append(playerController.getCurrName() + "." + tempSubinfo.getExt());
                        _Buf.append(" ");
                        _Buf.append(tempSubinfo.getDesctribe());

                        mShooterSubList.add(_Buf.toString());

                    }

                    List<String> _List = mShooterSubList;

                    AlertDialog.Builder _Builder = new AlertDialog.Builder(new ContextThemeWrapper(VideoActivity.this, R.style.AlertDialogCustom));
                    _Builder.setTitle(getString(R.string.dialogTitleSubtitleSelect, new Object[]{mShooterSubinfoList.size()}));
                    String[] _Strings = new String[_List.size()];
                    _Builder.setSingleChoiceItems(_List.toArray(_Strings), 0, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface pDialog, int pWhich) {
                            doHttpDownload(pWhich);
                            pDialog.dismiss();
                        }
                    });
                    _Builder.setOnItemSelectedListener(selectedListener);
                    Dialog _Dialog = getShowDialog(_Builder);
                    DialogTool.disableBackgroundDim(_Dialog);
                    DialogTool.setDefaultSelectDisplay(_Dialog);
                    common.dialogAutoDismiss(_Dialog);
                }
            }
        }
    };

    protected void doHttpDownload(int pSubId) {
        if (getCacheDir() != null) {
            showProgressDialog(R.string.dialogTitleLoading, R.string.downloadMessageLoading);
            final ShooterSubinfo tempSubinfo = mShooterSubinfoList.get(pSubId);
            ShooterHttpDownload mShooterHttpDownload = new ShooterHttpDownload(mDownloadResultListener);
            mShooterHttpDownload.httpDownloadSubTitie(tempSubinfo.getLink(),
                    playerController.getCurrName() + "." + tempSubinfo.getExt(),VideoActivity.this);
        } else {
            LogTool.e("getCacheDir fail, doHttpDownload aborted!");
        }
    }

    private ShooterHttpDownload.DownloadResultListener mDownloadResultListener = new ShooterHttpDownload.DownloadResultListener() {
        public void onDownloadResult(String path) {
            //Log.i(TAG,"onDownloadResult:"+path);
            dismissProgressDialog();
            if (path == null) {
                ToastUtil.showMessage(VideoActivity.this, getString(R.string.downloadSubtitleFailed), Toast.LENGTH_SHORT);
            } else if (videoView != null)
                videoView.setSubtitlePath(path);
        }
    };

    private AdapterView.OnItemClickListener secondMenuClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            doSecondMenuClick(position);
        }
    };

    private void doSecondMenuClick(int position) {
        if (secodeMenuKeyValuePairs == null || secodeMenuKeyValuePairs.size() < position) {
            return;
        }
        MenuKeyValuePair keyValuePair = secodeMenuKeyValuePairs.get(position);
        if (keyValuePair == null) {
            return;
        }
        String key = keyValuePair.getKey();
        if (SecondMenuConfig.MODE_2D_TO_2D_AFTER.equals(key)) {
            videoView.setStereoVideoFmt(0);
            hiDisplayManager.set3DMode(HI_2D_MODE, 0);
            isTo2DOriginal = false;
            isTo3DOriginal = false;
        } else if (SecondMenuConfig.MODE_2D_TO_2D_BEFORE_SBS.equals(key)) {
            videoView.setStereoVideoFmt(1);
            hiDisplayManager.set3DMode(HI_2D_MODE, 0);
            isTo2DOriginal = true;
            isTo3DOriginal = true;
        } else if (SecondMenuConfig.MODE_2D_TO_2D_BEFORE_TAB.equals(key)) {
            videoView.setStereoVideoFmt(2);
            hiDisplayManager.set3DMode(HI_2D_MODE, 0);
            isTo2DOriginal = true;
            isTo3DOriginal = true;
        } else if (SecondMenuConfig.MODE_3D_TO_2D.equals(key)) {
            hiDisplayManager.set3DMode(HI_2D_MODE, 0);
            isTo3DOriginal = true;
            isTo2DOriginal = true;
        } else if (SecondMenuConfig.MODE_3D_TO_2D_SIDE_EXCHANGE.equals(key)) {
            videoView.setStereoVideoFmt(2);
            hiDisplayManager.set3DMode(HI_2D_MODE, 0);
            isTo3DOriginal = true;
        } else if (SecondMenuConfig.MODE_3D_TO_2D_STEREO_DEPTH.equals(key)) {
            showdepthchooseDialog();
            isTo3DOriginal = true;
        } else if (SecondMenuConfig.MODE_MVC_2D_TO_3D.equals(key)) {
            hiDisplayManager.set3DMode(HI_3D_MODE, 0);
            isTo3DOriginal = true;
        } else if (SecondMenuConfig.MODE_SBS_2D_TO_3D.equals(key)) {
            videoView.setStereoVideoFmt(1);
            hiDisplayManager.set3DMode(HI_3D_MODE_SBS, 0);
            if (hiDisplayManager.getRightEyeFirst() == 0) {
                hiDisplayManager.setRightEyeFirst(1);
            } else if (hiDisplayManager.getRightEyeFirst() == 1) {
                hiDisplayManager.setRightEyeFirst(0);
            }
            isTo3DOriginal = true;
        } else if (SecondMenuConfig.MODE_TAB_2D_TO_3D.equals(key)) {
            videoView.setStereoVideoFmt(2);
            hiDisplayManager.set3DMode(HI_3D_MODE_TAB, 0);
            if (hiDisplayManager.getRightEyeFirst() == 0) {
                hiDisplayManager.setRightEyeFirst(1);
            } else if (hiDisplayManager.getRightEyeFirst() == 1) {
                hiDisplayManager.setRightEyeFirst(0);
            }
            isTo3DOriginal = true;
        } else if (SecondMenuConfig.AUDIO_OPT_CHANNEL_SWITCH.equals(key)) {
            showAudioChannelListDialog();
        } else if (SecondMenuConfig.AUDIO_OPT_TRACK_SWITCH.equals(key)) {
            showAudioListDialog();
        } else if (SecondMenuConfig.SUB_OPT_ADVANCED.equals(key)) {
            if (!playerController.isSubtitleOn()) {
                ToastUtil.showMessage(VideoActivity.this, getString(R.string.subtitleOffToast), Toast.LENGTH_SHORT);
                return;
            }
            thirdMenuKeyValuePairs = thirdMenuConfig.loadSubAdvOptVp();
            popThirdMenuDialog(keyValuePair.getValueResId());
        } else if (SecondMenuConfig.SUB_OPT_SWITCHING.equals(key)) {
            showSubtitleSelectDialog();
        } else if (SecondMenuConfig.SUB_OPT_SEARCH.equals(key)) {
            ShooterApiQuery mShooterApiQuery = new ShooterApiQuery(mSearchResultListener);
            File file = new File(playerController.getCurrPlayPath());
            mShooterApiQuery.querySubTitie(ShooterMD5.getFileMD5(file), playerController.getCurrPlayPath());
            showProgressDialog(R.string.dialogTitleLoading, R.string.searchMessageLoading);
        } else if (SecondMenuConfig.SUB_OPT_SELECT.equals(key)) {
            showSubList();
        } else if (SecondMenuConfig.SUB_OPT_ON_OFF.equals(key)) {
            subOnOff();
        } else if (SecondMenuConfig.MODE_8K_MOUSE.equals(key)) {
            Toast.makeText(this, getString(R.string.mouseScaleTip), Toast.LENGTH_SHORT).show();
            mouseView.setVisibility(View.VISIBLE);
            mIs8KMouseMode = true;
        } else if (SecondMenuConfig.MODE_8K_CROSS.equals(key)) {
            crossView.showAllArea();
            mIs8KAreaMode = true;
        }
        if (secondMenuDialog != null) {
            secondMenuDialog.dismiss();
        }
    }


    private void showdepthchooseDialog() {
        LayoutInflater layout = getLayoutInflater();
        RelativeLayout relative = (RelativeLayout) layout.inflate(R.layout.setseekbar_depth, null);
        final TextView textview = (TextView) relative.findViewById(R.id.seekbarvalue);
        final SeekBar s = (SeekBar) relative.findViewById(R.id.seekbar);
        s.setProgress(hiDisplayManager.getstereoDepth());
        s.setMax(10);
        final Dialog d = new Dialog(this, R.style.dialog);
        d.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                        || (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    dialog.cancel();
                }

                return false;
            }
        });
        d.setContentView(relative);
        d.show();

        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                textview.setText(progress + "");
                hiDisplayManager.setStereoDepth(progress);
                common.dialogAutoDismiss(d);
            }
        });
    }

    protected void showSubList() {
        String currDirectory = playerController.getCurrPlayPath().substring(0,
                playerController.getCurrPlayPath().length() - playerController.getCurrPlayPath().length());
        Intent intent = new Intent(this, FileListAcvitity.class);
        //intent.setClassName("com.hisilicon.android.videoplayer","com.hisilicon.android.videoplayer.activity.FileListAcvitity");
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("path", currDirectory);
        startActivityForResult(intent, 100);
    }


    protected void showSubtitleSelectDialog() {
        List<String> _List = videoView.getSubtitleLanguageList();
        //AlertDialog.Builder _Builder = new AlertDialog.Builder(this);
        AlertDialog.Builder _Builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        final int _SubNum = _List.size();
        final int _ExtNum = videoView.getExtSubtitleNumber();
        _Builder.setTitle(getString(R.string.dialogTitleSubtitleSelect, new Object[]
                {videoView.getSubtitleNumber()}));
        String[] _Strings = new String[_List.size()];
        int _Select = videoView.getSubtitleId();
        if (_ExtNum != 0) {
            if (_Select >= (_SubNum - _ExtNum))
                _Select = _Select - (_SubNum - _ExtNum);
            else if (_Select < (_SubNum - _ExtNum))
                _Select = _Select + _ExtNum;
        }
        _Builder.setSingleChoiceItems(_List.toArray(_Strings), _Select,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface pDialog, int pWhich) {
                        int _SubID = pWhich;
                        if (_ExtNum == 0) {
                            doSwitchSubtitle(_SubID, pWhich);
                            pDialog.dismiss();
                        } else {
                            if (pWhich >= _ExtNum)
                                _SubID = pWhich - _ExtNum;
                            else if (pWhich < _ExtNum)
                                _SubID = pWhich + (_SubNum - _ExtNum);
                            doSwitchSubtitle(_SubID, pWhich);
                            pDialog.dismiss();
                        }
                    }
                });
        _Builder.setOnItemSelectedListener(selectedListener);
        Dialog _Dialog = getShowDialog(_Builder);
        DialogTool.disableBackgroundDim(_Dialog);
        DialogTool.setDefaultSelectDisplay(_Dialog);
        common.dialogAutoDismiss(_Dialog);
    }

    protected void doSwitchSubtitle(int pSubtitleId, int select) {
        if (videoView.setSubtitleId(pSubtitleId) == 0) {
            controlModel.setSelectedSubId(pSubtitleId);
            videoView.setSelectSubtitleId(pSubtitleId);
            ToastUtil.showMessage(this, getString(R.string.toastSubtitle, new Object[]
                    {videoView.getSubtitleLanguageList().get(select)}), Toast.LENGTH_SHORT);
        } else
            ToastUtil.showMessage(this, getString(R.string.switchFailed), Toast.LENGTH_SHORT);
    }

    private void subOnOff() {
        List<String> _List = new ArrayList<String>();
        _List.add(getString(R.string.showsub));
        _List.add(getString(R.string.closesub));

        AlertDialog.Builder _Builder = new AlertDialog.Builder(this);
        _Builder.setTitle(getString(R.string.subsetTitle));
        String[] _Strings = new String[_List.size()];
        _Builder.setSingleChoiceItems(_List.toArray(_Strings),
                playerController.isSubtitleOn() ? 0 : 1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface pDialog, int pWhich) {
                        if (pWhich == 0) {
                            isShowSub = 0;
                            common.sharedPreferencesOpration(Constants.SHARED, "subtitle_on_off", 0, 0, true);
                            playerController.setSubtitleOn(true);
                            videoView.enableSubtitle(isShowSub);
                        } else if (pWhich == 1) {
                            isShowSub = 1;
                            common.sharedPreferencesOpration(Constants.SHARED, "subtitle_on_off", 1, 0, true);
                            playerController.setSubtitleOn(false);
                            videoView.enableSubtitle(isShowSub);
                        }
                        pDialog.dismiss();
                    }
                });
        _Builder.setOnItemSelectedListener(selectedListener);
        Dialog _Dialog = getShowDialog(_Builder);
        DialogTool.disableBackgroundDim(_Dialog);
        DialogTool.setDefaultSelectDisplay(_Dialog);
        common.dialogAutoDismiss(_Dialog);
    }


    private AdapterView.OnItemClickListener thirdMenuClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            doThirdMenuClick(position);
        }
    };

    private void doThirdMenuClick(int postion) {
        if (thirdMenuKeyValuePairs == null || thirdMenuKeyValuePairs.size() < postion) {
            return;
        }
        MenuKeyValuePair keyValuePair = thirdMenuKeyValuePairs.get(postion);
        String key = keyValuePair.getKey();
        if (ThirdMenuConfig.SUB_ADVANCED_TIME_ADJUSTEMNT.equals(key)) {
            subSeekbar(0, -230, R.string.time, 2000);
        } else if (ThirdMenuConfig.SUB_ADVANCED_SUBTITLE_ENCODE.equals(key)) {
            int pos = encodeList.size() - 1;
            String[] encode = new String[pos + 1];
            int selectedEncode = getSubEncode(HiMediaPlayerInvoke.CMD_GET_SUB_FONT_ENCODE);
            for (int i = 0; i < encodeList.size(); i++) {
                encode[i] = encodeList.get(i).getEncodeName();
                if (selectedEncode == encodeList.get(i).getEncodeValue()) {
                    pos = i;
                }
            }

            int selectedSubEncode = common.sharedPreferencesOpration(Constants.SHARED, "subtitleEncode", 0, 0, false);
            controlModel.setSelectedSubEncode(selectedSubEncode);
            if (selectedSubEncode == 0) {
                pos = 0;
            }

            setSubDialog(500, -150, screenWidth / 2, keyValuePair.getValueResId(),
                    pos, encode, selectedListener,
                    new EachItemClickListener("encode"));
        } else if (ThirdMenuConfig.SUB_ADVANCED_FONT_COLOR.equals(key)) {
            String[] colors = getResources().getStringArray(R.array.colors);
            int position = 0;
            for (int i = 0; i < colorValue.length; i++) {
                if (colorValue[i].equals(controlModel.getSelectedColor())) {
                    position = i;
                    break;
                }
            }

            setSubDialog(500, -120, screenWidth / 3, keyValuePair.getValueResId(), position, colors, selectedListener,
                    new EachItemClickListener("colors"));
        } else if (ThirdMenuConfig.SUB_ADVANCED_FONT_SIZE.equals(key)) {
            subSeekbar(0, -230, R.string.size, 100);
        } else if (ThirdMenuConfig.SUB_ADVANCED_FONT_EFFECT.equals(key)) {
            String[] effect = getResources().getStringArray(R.array.effect);
            setSubDialog(500, -120, screenWidth / 3, keyValuePair.getValueResId(), controlModel.getSelectedEffect(), effect,
                    selectedListener, new EachItemClickListener("effect"));
        } else if (ThirdMenuConfig.SUB_ADVANCED_THE_LINE_PACING.equals(key)) {
            subSeekbar(0, -230, R.string.line_space, 100);
        } else if (ThirdMenuConfig.SUB_ADVANCED_CHARACTER_SPACING.equals(key)) {
            subSeekbar(0, -230, R.string.space, 100);
        } else if (ThirdMenuConfig.SUB_ADVANCED_3D_SUBTITLE_ADJECT.equals(key)) {
            if (m3DSubtitle == 0) {
                m3DSubtitle = 1;
            } else if (m3DSubtitle == 1) {
                m3DSubtitle = 0;
            }
            setNewFounction(HiMediaPlayerInvoke.CMD_SET_3D_SUBTITLE_CUT_METHOD, m3DSubtitle);
        }
    }

    private void checkHitype() {
        Parcel reply = getInfo(HiMediaPlayerInvoke.CMD_GET_SUB_INFO);
        reply.readInt();
        playerController.setSubCount(reply.readInt());
        reply.recycle();
        if (playerController.getSubCount() != 0) {
            if (playerController.isSubtitleOn()) {
                if (playerController.getSubCount() == 1) {
                    Parcel replytype = getInfo(HiMediaPlayerInvoke.CMD_GET_SUB_ISBMP);
                    replytype.readInt();
                    int type = replytype.readInt();
                    replytype.recycle();
                    hiType1 = type;
                } else if (playerController.getSubCount() >= 2) {
                    Parcel replytype = getInfo(HiMediaPlayerInvoke.CMD_GET_SUB_ISBMP);
                    replytype.readInt();
                    int type = replytype.readInt();
                    replytype.recycle();
                    hiType1 = type;
                }
            }
        }
    }

    public void setSubDialog(int x, int y, int width, @IdRes int title, int selectedItem, String[] subSettingItems,
                             AdapterView.OnItemSelectedListener subItemSelectedListener,
                             AdapterView.OnItemClickListener subAdvItemClickListener) {
        ListView subItemList = new ListView(this);

        subItemList.setFocusableInTouchMode(true);
        subItemList.setAdapter(new MenuListAdapter(this, subSettingItems, 1, 0, true, -1));
        subItemList.setSelection(selectedItem);
        subItemList.setOnItemSelectedListener(subItemSelectedListener);
        subItemList.setOnItemClickListener(subAdvItemClickListener);
        subItemList.setBackgroundResource(R.drawable.dialog_background_selector);
        subItemList.setSelector(getResources().getDrawable(R.drawable.item_background_selector));
        subSetDialog = getDefaultSettingDialog(title, subItemList);
        subSetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                //dialogDismiss();

                int type = 0;

                Parcel replytype = getInfo(HiMediaPlayerInvoke.CMD_GET_SUB_ISBMP);
                replytype.readInt();
                type = replytype.readInt();
                replytype.recycle();
                hiType2 = type;
                if ((hiType1 != hiType2) && (subtitleSetDialog != null)) {
                    MenuKeyValuePair keyValuePair = new MenuKeyValuePair(SecondMenuConfig.SUB_OPT_ADVANCED,
                            R.string.subtitle_advanced, R.drawable.iconu);
                    popUpSecondMenuDialog(keyValuePair.getValueResId());
                    thirdMenuDialog.dismiss();
                }
            }
        });
        Common.setDialogWidth(subSetDialog, width, x, y);
        subSetDialog.show();
        common.dialogAutoDismiss(subSetDialog);
    }


    class EachItemClickListener implements AdapterView.OnItemClickListener {
        private String tag;

        EachItemClickListener(String tag) {
            this.tag = tag;
        }

        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            if ("no".equals(tag)) {
            } else if ("colors".equals(tag)) {
                String selectedColor = colorValue[arg2];
                controlModel.setSelectedColor(selectedColor);
                setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_COLOR, Integer.parseInt(selectedColor.replace("0x", ""), 16));
                controlModel.setSelectedColorPos(arg2);
                common.sharedPreferencesOpration(Constants.SHARED, "selectedColor", arg2, 0, true);
            } else if ("tracks".equals(tag)) {
                setNewFounction(HiMediaPlayerInvoke.CMD_SET_AUDIO_TRACK_PID, arg2);
                controlModel.setSelectedTrack(arg2);
            } else if ("audio_chan_mode".equals(tag)) {
                controlModel.setSelectedAudio(arg2);
                setNewFounction(HiMediaPlayerInvoke.CMD_SET_AUDIO_CHANNEL_MODE, arg2);
                common.sharedPreferencesOpration(Constants.SHARED, "channer", arg2, 0, true);
            } else if ("effect".equals(tag)) {
                controlModel.setSelectedEffect(arg2);
                setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_STYLE, arg2);
                common.sharedPreferencesOpration(Constants.SHARED, "selectedEffect", arg2, 0, true);
            } else if ("change".equals(tag)) {
                videoView.setSubTrack(arg2);
            } else if ("encode".equals(tag)) {
                int selectedEncode = encodeList.get(arg2).getEncodeValue();
                {
                    setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_ENCODE, selectedEncode);
                    common.sharedPreferencesOpration(Constants.SHARED, "subtitleEncode", selectedEncode, 0, true); /*Get user's setting of play mode*/       /*CNcomment: 保存字幕选择 */
                }
            }

            common.updatestartTime1();
        }

    }


    /******************************/
    private void setRewind() {

        if (controlModel.getRewindRate() < 32) {
            if (!(videoView.isPlaying())) {
                videoView.start();
            }
            controlModel.setForwardRate(1);
            controlModel.setRewindRate(controlModel.getRewindRate() * 2);
            videoView.setSpeed(-controlModel.getRewindRate());

            if (!ivPlayStatus.isShown()) {
                timeSetRewind = System.currentTimeMillis();
                ivPlayStatus.setVisibility(View.VISIBLE);
            }

            switch (controlModel.getRewindRate()) {
                case 2:
                    ivPlayStatus.setImageResource(R.drawable.fb_x2);
                    break;
                case 4:
                    ivPlayStatus.setImageResource(R.drawable.fb_x4);
                    break;
                case 8:
                    ivPlayStatus.setImageResource(R.drawable.fb_x8);
                    break;
                case 16:
                    ivPlayStatus.setImageResource(R.drawable.fb_x16);
                    break;
                case 32:
                    ivPlayStatus.setImageResource(R.drawable.fb_x32);
                    break;
            }

            ivPlayPause.setBackgroundResource(R.drawable.play_button);
            playerController.setRewindOrForward(true);
        } else if (playerController.isRewindOrForward()) {
            ivPlayStatus.setVisibility(View.INVISIBLE);
            controlModel.setForwardRate(1);
            controlModel.setRewindRate(1);
            playerController.setRewindOrForward(false);
            videoView.resume();
            ivPlayPause.setBackgroundResource(R.drawable.pause_button);
        }
    }

    private void setForward() {
        if (controlModel.getForwardRate() < 32) {
            if (!(videoView.isPlaying())) {
                videoView.start();
            }
            controlModel.setRewindRate(1);
            controlModel.setForwardRate(controlModel.getForwardRate() * 2);
            videoView.setSpeed(controlModel.getForwardRate());

            if (!ivPlayStatus.isShown()) {
                timeSetForward = System.currentTimeMillis();
                ivPlayStatus.setVisibility(View.VISIBLE);
            }

            switch (controlModel.getForwardRate()) {
                case 2:
                    ivPlayStatus.setImageResource(R.drawable.ff_x2);
                    break;
                case 4:
                    ivPlayStatus.setImageResource(R.drawable.ff_x4);
                    break;
                case 8:
                    ivPlayStatus.setImageResource(R.drawable.ff_x8);
                    break;
                case 16:
                    ivPlayStatus.setImageResource(R.drawable.ff_x16);
                    break;
                case 32:
                    ivPlayStatus.setImageResource(R.drawable.ff_x32);
                    break;
            }

            ivPlayPause.setBackgroundResource(R.drawable.play_button);
            playerController.setRewindOrForward(true);
        } else if (playerController.isRewindOrForward()) {
            ivPlayStatus.setVisibility(View.INVISIBLE);
            controlModel.setForwardRate(1);
            controlModel.setRewindRate(1);
            playerController.setRewindOrForward(false);
            videoView.resume();
            ivPlayPause.setBackgroundResource(R.drawable.pause_button);
        }
    }

    private void videoJump(boolean goNext) {
        if (playerController.isRewindOrForward()) {
            ivPlayStatus.setVisibility(View.INVISIBLE);
            controlModel.setForwardRate(1);
            controlModel.setRewindRate(1);
            playerController.setRewindOrForward(false);
            videoView.resume();
        }

        if (mediaFileList != null) {
            nameSizeHandler.removeCallbacks(nameSizeDismissRunnable);
            Common.setResume(false);
            ivPlayPause.setBackgroundResource(R.drawable.pause_button);
            initSeekSecondaryProgress();
            Common.setShowLoadingToast(true);
            getVideoInfo(goNext ? mediaFileList.getNextVideoInfo(null) : mediaFileList.getPreVideoInfo(null));
            mySeekBar.setProgress(0);
            controlModel.setSelectedSubId(0);
        }
    }

    private void videoPause(boolean force) {
        if (haveLeftRightOpration) {
            mySeekBar.setProgress(progerssFwRwind);
            videoView.seekTo((int) progerssFwRwind);
            initSeekSecondaryProgress();
        } else {
            if (playerController.isRewindOrForward()) {
                ivPlayStatus.setVisibility(View.INVISIBLE);
                controlModel.setForwardRate(1);
                controlModel.setRewindRate(1);
                playerController.setRewindOrForward(false);
                speedChoiced = -1;
                videoView.resume();
                ivPlayPause.setBackgroundResource(R.drawable.pause_button);
            } else {
                if (force) {
                    play_pause();
                } else {
                    isSeekBarSelected = true;
                }
            }
        }
    }

    private void play_pause() {
        if (Common.isResume()) {
            Common.setResume(false);
        }
        if (videoView.isPlaying()) {
            ivPlayPause.setBackgroundResource(R.drawable.play_button);
            videoView.pause();
            isPause = true;
        } else {
            if (this.isStop) {
                videoView = (HisiVideoView) findViewById(R.id.videoView);
                videoView.setVideoPath(playerController.getCurrPath());
                common.sharedPreferencesOpration(Constants.SHARED, SecondMenuConfig.ADVANCED_CONTINUE_PLAY, 0, 0, true);
                ivPlayPause.setBackgroundResource(R.drawable.pause_button);
                this.isStop = false;
            } else {
                ivPlayPause.setBackgroundResource(R.drawable.pause_button);
                videoStart();
            }
        }
    }

    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        isPipMode = isInPictureInPictureMode;
        common.savePipVideoPosition(videoView.getCurrentPosition());
        if (isPipMode) {
            common.savePipVideoTag(isInPictureInPictureMode);
        } else {
            Intent intent = getIntent();
            if (intent != null) {
                MediaFileList mediaFileList = intent.getParcelableExtra("MediaFileList");
                if (mediaFileList != null) {
                    intent.putExtra("MediaFileList", new FMMediaFileList(playerController.getCurrPath(), playerController.getCurrName()));
                    setIntent(intent);
                }
            }

        }
        LogTool.d(VideoActivity.class.getSimpleName(), " isInPictureInPictureMode " + isInPictureInPictureMode);
    }


    /**************************/

    /***********************************/

    private void generateTextureView() {
        frameLayout.removeView(multiPlayTextureView);
        multiPlayTextureView = new MultiPlayTextureView(this);
        multiPlayTextureView.setOnErrorListener(multiplePlayerOnErrorListener);
        multiPlayTextureView.setOnInfoListener(multiplePlayerOnInfoListener);
        multiPlayTextureView.setMultiplePlayerOnDestoryFinish(multiplePlayerOnDestoryFinish);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(320, 180);
        params.width = screenWidth / 2;
        params.height = screenHeight / 2;
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        multiPlayTextureView.setPadding(20, 20, 20, 20);
        multiPlayTextureView.setLayoutParams(params);
        frameLayout.addView(multiPlayTextureView);
        multiPlayTextureView.bringToFront();
    }

    private void showMultipleDialog() {
        if (multiplayDialog != null && multiplayDialog.isShowing()) {
            multiplayDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.multiple_dialog_show);
        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        multiPlayTextureView.setVisibility(View.GONE);
                        multiPlaySurfaceView.setVisibility(View.GONE);
                        if (multiPlayStyle == DptOMultipleListDialog.STYLE_SURFACEVIEW) {
                            multiPlaySurfaceView.beginDestory();
                            isFirstPlay = true;
                        }
                        if (multiPlayStyle == DptOMultipleListDialog.STYLE_TEXTUREVIEW) {
                            multiPlayTextureView.beginDestory();
                            isFirstPlay = true;
                        }
                        multiplayDialog.dismiss();
                        ivMenu.setBackgroundResource(R.drawable.multiple_off_button);
                    }
                });
        builder.setNegativeButton(R.string.not,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        multiplayDialog.dismiss();
                    }
                });
        multiplayDialog = builder.create();
        multiplayDialog.show();
    }

    private void subSeekbar(int x, int y, final int title, final int valueMax) {
        LayoutInflater layout = getLayoutInflater();
        RelativeLayout relative = (RelativeLayout) layout.inflate(R.layout.setseekbar, null);
        TextView seekbarTitle = (TextView) relative.findViewById(R.id.seekBarTitle);
        final TextView seekbarValue = (TextView) relative.findViewById(R.id.seekBarValue);
        SeekBar seekbar = (SeekBar) relative.findViewById(R.id.seekBar);

        if (title == R.string.time) {
            int max = 100;
            seekbarTitle.setText(R.string.time);
            seekbar.setMax(max);
            if (controlModel.getSelectedTime() == -1) {
                seekbarValue.setText("0ms");
                seekbar.setProgress(50);
            } else {
                seekbarValue.setText(controlModel.getSelectedTime() + "ms");
                seekbar.setProgress((controlModel.getSelectedTime() + Constants.timeMax) * max / valueMax);
            }
        } else if (title == R.string.size) {
            int max = 20;
            seekbarTitle.setText(R.string.size);
            seekbar.setMax(max);
            if (controlModel.getSelectedSizes() == -1) {
                seekbarValue.setText("25");
                seekbar.setProgress(5);
            } else {
                seekbarValue.setText("" + controlModel.getSelectedSizes());
                seekbar.setProgress(controlModel.getSelectedSizes() * max / valueMax);
            }
        } else if (title == R.string.space) {
            int max = 20;
            seekbarTitle.setText(R.string.space);
            seekbar.setMax(20);
            if (controlModel.getSelectedSpace() == -1) {
                seekbarValue.setText("10");
                seekbar.setProgress(2);
            } else {
                seekbarValue.setText("" + controlModel.getSelectedSpace());
                seekbar.setProgress(controlModel.getSelectedSpace() * max / valueMax);
            }
        } else if (title == R.string.line_space) {
            int max = 20;
            seekbarTitle.setText(R.string.line_space);
            seekbar.setMax(20);
            int selectedLSpace = controlModel.getSelectedLSpace();
            if (selectedLSpace == -1) {
                seekbarValue.setText("20");
                seekbar.setProgress(4);
            } else {
                seekbarValue.setText("" + selectedLSpace);
                seekbar.setProgress(selectedLSpace * max / valueMax);
            }
        } else if (title == R.string.volume_ctl) {
            int max = 100;
            seekbarTitle.setText(R.string.volume_ctl);
            seekbar.setMax(100);
            int selectedVolume = controlModel.getSelectedVolume();
            if (selectedVolume == -1) {
                seekbarValue.setText("20");
                seekbar.setProgress(4);
            } else {
                seekbarValue.setText("" + selectedVolume);
                seekbar.setProgress(selectedVolume * max / valueMax);
            }
        }
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (title == R.string.time) {
                    int selectedTime = progress * valueMax / seekBar.getMax()
                            - Constants.timeMax;
                    controlModel.setSelectedTime(selectedTime);
                    subTime(selectedTime);
                    seekbarValue.setText(selectedTime + "ms");
                } else if (title == R.string.position) {
                    int selectedPosition = progress * valueMax / seekBar.getMax();
                    controlModel.setSelectedPosition(selectedPosition);
                    setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_VERTICAL, selectedPosition);
                    seekbarValue.setText("" + selectedPosition);
                    common.sharedPreferencesOpration(Constants.SHARED, "selectedPosition", selectedPosition, 0, true);
                } else if (title == R.string.size) {
                    if (progress < 3) {
                        progress = 3;
                        seekBar.setProgress(progress);
                    }
                    int selectedSizes = progress * valueMax / seekBar.getMax();
                    controlModel.setSelectedSizes(selectedSizes);
                    setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_SIZE, selectedSizes);
                    seekbarValue.setText("" + selectedSizes);
                    common.sharedPreferencesOpration(Constants.SHARED, "subtitleSizes", selectedSizes, 0, true);
                } else if (title == R.string.space) {
                    int selectedSpace = progress * valueMax / seekBar.getMax();
                    controlModel.setSelectedSpace(selectedSpace);
                    setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_SPACE, selectedSpace);
                    seekbarValue.setText("" + selectedSpace);
                    common.sharedPreferencesOpration(Constants.SHARED, "selectedSpace", selectedSpace, 0, true);
                } else if (title == R.string.line_space) {
                    int selectedLSpace = progress * valueMax / seekBar.getMax();
                    controlModel.setSelectedLSpace(selectedLSpace);
                    setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_LINESPACE, selectedLSpace);
                    seekbarValue.setText("" + selectedLSpace);
                    common.sharedPreferencesOpration(Constants.SHARED, "selectedLSpace", selectedLSpace, 0, true);
                } else if (title == R.string.DolbyRangeInfo) {
                    int selectedDolbyRangeInfo = progress * valueMax / seekBar.getMax();
                    controlModel.setSelectedDolbyRangeInfo(selectedDolbyRangeInfo);
                    setNewFounction(HiMediaPlayerInvoke.CMD_SET_DOLBY_RANGEINFO, selectedDolbyRangeInfo);
                    seekbarValue.setText("" + selectedDolbyRangeInfo);
                    common.sharedPreferencesOpration(Constants.SHARED, "selectedDolbyRangeInfo", selectedDolbyRangeInfo, 0, true);
                    ToastUtil.showMessage(VideoActivity.this, R.string.DolbyRangeInfoToast);
                } else if (title == R.string.volume_ctl) {
                    int selectedVolume = progress * valueMax / seekBar.getMax();
                    LogTool.d("SelectedVolume : " + selectedVolume + " Progress : " + progress);
                    controlModel.setSelectedVolume(selectedVolume);
                    seekbarValue.setText("" + selectedVolume);
                    common.sharedPreferencesOpration(Constants.SHARED, "selectedVolume", selectedVolume, 100, true);
                    //Log.i(TAG,"set volume:"+progress/100f);
                    videoView.setVolume(progress / 100f, progress / 100f);
                }

                common.updatestartTime1();
            }
        });

        Dialog dialog = new Dialog(VideoActivity.this, R.style.dialog);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                        || (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    dialog.cancel();
                }

                return false;
            }
        });
        common.setDialog(dialog, relative, x, y, 5);
        common.dialogAutoDismiss(dialog);
    }

    protected void notSupportDialog(int msg) {
        if (isPipMode) {
            showOneDialog(msg);
        } else {
            isNotSupportShow = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            int messageId = getSystemResId("VideoView_error_title", "string");
            builder.setTitle(messageId);
            builder.setMessage(msg);
            builder.setPositiveButton(R.string.confirm, null);
            notSptDialog = builder.create();
            notSptDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            notSptDialog.show();
        }
    }

    protected void notVideoReady(String msg, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.confirm, null);
        notSptDialog = builder.create();
        notSptDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        notSptDialog.show();
    }

    private void getFlashPlayDialog() {
        if (pointDialog != null) {
            pointDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.continue_playvideo);

        StringBuffer msgstr = new StringBuffer();
        msgstr.append(getString(R.string.willjumpto));
        msgstr.append(" ");
        msgstr.append(Common.getTimeFormatValue(lastPosition));
        msgstr.append(" ");
        msgstr.append(getString(R.string.toplay));
        builder.setMessage(msgstr.toString());

        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        common.setPositive(true);
                    }
                }).setNegativeButton(R.string.not, null);
        pointDialog = builder.create();
        pointDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (common.isPositive()) {
                    videoView.seekTo(lastPosition);
                } else {
                    updatePositon(playerController.getCurrPath());
                }

                start();
                getDuration();
                doExtraOpration();
                common.setPositive(false);
                initSDRMode();
            }
        });
        pointDialog.show();
    }


    /*************************************/
    private void finishPlayer() {
        ctrlBarHandler.removeCallbacks(ctrlRunnable);
        ivPlayStatus.setBackgroundResource(R.drawable.play_button);
        finish();
        LogTool.d(VideoActivity.class.getSimpleName(), "finishPlayer ");
    }


    //save current path for cut source.
    protected void saveKeyCutSourcePath(String path) {
        SharedPreferences sharedPreferences = getSharedPreferences(ONE_KEY_PLAY_TAG, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(ONE_KEY_PLAY_TAG, path).commit();
    }

    public void resetFastSpeedToNomal() {
        speedChoiced = -1;
        playerController.setRewindOrForward(false);
        controlModel.setRewindRate(1);
        controlModel.setForwardRate(1);
        videoView.resume();
        ivPlayPause.setBackgroundResource(R.drawable.pause_button);
        ivPlayStatus.setVisibility(View.INVISIBLE);
    }


    private void on8KItemsClicked(int position) {
        switch (position) {
            case 0:
                Toast.makeText(this, getString(R.string.mouseScaleTip), Toast.LENGTH_SHORT).show();
                mouseView.setVisibility(View.VISIBLE);
                mIs8KMouseMode = true;
                break;
            case 1:
                crossView.showAllArea();
                mIs8KAreaMode = true;
                break;
        }
    }

    public void reset8KToNormal() {
        int ret = videoView.setVideoRect(0, 0, realVideoWidth, realVideoHeight);
        if (ret == 0) {
            LogTool.i("reset 8k to normal success!");
        }
    }

    public int[] calculation8KVideoRect(int startPointX, int startPointY, int endPointX, int endPointY, float ratioX, float ratioY) {
        int[] points = new int[4];
        int videoStartX = (int) (startPointX / ratioX);
        int videoStartY = (int) (startPointY / ratioY);
        int videoEndX = (int) (endPointX / ratioX);
        int videoEndY = (int) (endPointY / ratioY);
        points[0] = videoStartX;
        points[1] = videoStartY;
        points[2] = videoEndX - videoStartX;
        points[3] = videoEndY - videoStartY;
        return points;
    }


    public float calculationPointXRatio(int width) {
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        return (screenWidth * 1.0f) / width;
    }

    public float calculationPointYRation(int height) {
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        return (screenHeight * 1.0f) / height;
    }

    private void inflateAudioInfo() {
        List<String> _AudioInfoList = videoView.getAudioInfoList();
        List<String> _AudioLanguageList = new ArrayList<String>();
        List<String> _AudioFormatList = new ArrayList<String>();
        List<String> _AudioSampleRateList = new ArrayList<String>();
        List<String> _AudioChannelList = new ArrayList<String>();

        if (_AudioInfoList == null) {
            LogTool.e("TAG", "inflateAudioInfo failed");
            return;
        }
        if (PlayerJugdment.isHiPlayer()) {
            String _Language = "";
            String _Format = "";
            int _Index = 0;
            for (int i = 0; i < _AudioInfoList.size(); i++) {
                if (i % 4 == 0) {
                    _Language = xmlParser.getLanguage(_AudioInfoList.get(i));
                } else if (i % 4 == 1) {
                    _Index = Integer.parseInt(_AudioInfoList.get(i));
                    _Format = AudioFormat.getFormat(_Index);
                    if (_Format.isEmpty()) {
                        StringBuffer _Buf = new StringBuffer();
                        _Buf.append(getString(R.string.mediaInfoUnknown));
                        _Buf.append("(");
                        _Buf.append(_Index);
                        _Buf.append(")");
                        _Format = _Buf.toString();
                    } else if (_Format.equals("AC3") && playerController.getDolbyCertification() == 1) {
                        _Format = "Dolby Digital";
                    } else if (_Format.equals("EAC3") && playerController.getDolbyCertification() == 1) {
                        _Format = "Dolby Digital Plus";
                    }
                    _AudioFormatList.add(_Format);

                } else if (i % 4 == 2) {
                    String _SampleRate = Integer.parseInt(_AudioInfoList.get(i)) / 1000 + "KHz";
                    _AudioSampleRateList.add(_SampleRate);
                } else if ((i % 4 == 3) && playerController.getDolbyCertification() == 0) {
                    String _Channel = _AudioInfoList.get(i);
                    _AudioChannelList.add(_Channel);
                    StringBuffer _Buf = new StringBuffer();
                    _Buf.append((i / 4 + 1));
                    _Buf.append(".");
                    _Buf.append(_Language);
                    _Buf.append(" ");
                    _Buf.append(_Format);
                    _Buf.append(" ");
                    _Buf.append(_Channel);
                    _AudioLanguageList.add(_Buf.toString());
                } else if ((i % 4 == 3) && playerController.getDolbyCertification() == 1) {
                    String _Channel = _AudioInfoList.get(i);
                    _AudioChannelList.add(_Channel);
                    StringBuffer _Buf = new StringBuffer();
                    _Buf.append((i / 4 + 1));
                    _Buf.append(".");
                    _Buf.append(_Language);
                    _Buf.append(" ");
                    _Buf.append(_Format);
                    _AudioLanguageList.add(_Buf.toString());
                }
            }
        } else {
            _AudioLanguageList.addAll(_AudioInfoList);
        }
        videoView.setAudioTrackLanguageList(_AudioLanguageList);
        videoView.setAudioTrackNumber(_AudioLanguageList.size());
        // set to 0 when play a new video. solve OfBoundsException
        controlModel.setSelectedTrack(0);
        videoView.setAudioFormatList(_AudioFormatList);
        videoView.setAudioSampleRateList(_AudioSampleRateList);
        videoView.setAudioChannelList(_AudioChannelList);
    }

    private void inflateSubtitleInfo() {
        if (PlayerJugdment.isHiPlayer()) {
            List<String> _TempInternalList = videoView.getInternalSubtitleLanguageInfoList();
            List<String> _TempExtList = videoView.getExtSubtitleLanguageInfoList();
            List<String> _ResultInternalList = new ArrayList<String>();
            List<String> _ResultExtList = new ArrayList<String>();

            for (int i = 0; i < _TempInternalList.size(); i++) {
                _ResultInternalList.add(xmlParser.getLanguage(_TempInternalList.get(i)));
            }

            for (int i = 0; i < _TempExtList.size(); i++) {
                _ResultExtList.add(xmlParser.getLanguage(_TempExtList.get(i)));
            }

            List<String> _PGSInternalList = _ResultInternalList;
            List<String> _PGSExtList = _ResultExtList;
            List<String> _List = new ArrayList<String>();

            int _Index = 0;

            for (int i = 0; i < _PGSExtList.size(); i = i + 2) {
                _Index++;
                StringBuffer _Buf = new StringBuffer();
                _Buf.append(_Index);
                _Buf.append(".");
                _Buf.append(getResources().getString(R.string.subexttitle));
                _Buf.append(" ");
                _Buf.append(_PGSExtList.get(i));
                _Buf.append(" ");
                _Buf.append(_PGSExtList.get(i + 1));
                _List.add(_Buf.toString());
            }

            for (int i = 0; i < _PGSInternalList.size(); i = i + 2) {
                _Index++;
                StringBuffer _Buf = new StringBuffer();
                _Buf.append(_Index);
                _Buf.append(".");
                _Buf.append(getResources().getString(R.string.subintitle));
                _Buf.append(" ");
                _Buf.append(_PGSInternalList.get(i));
                _Buf.append(" ");
                _Buf.append(_PGSInternalList.get(i + 1));
                _List.add(_Buf.toString());
            }
            videoView.setSubtitleLanguageList(_List);
            videoView.setSubtitleNumber(_List.size());
            videoView.setExtSubtitleNumber(_PGSExtList.size() / 2);
            if (_PGSExtList.size() != 0) {
                videoView.setSelectSubtitleId(_List.size() - _PGSExtList.size() / 2);
                videoView.setSubtitleId(_List.size() - _PGSExtList.size() / 2);
                int selectedSubId = _List.size() - _PGSExtList.size() / 2;
                controlModel.setSelectedSubId(selectedSubId);
            }
        } else {
            List<String> subtitles = videoView.getSubtitles();
            int _Index = 0;
            List<String> _List = new ArrayList<String>();
            for (int i = 0; i < subtitles.size(); i++) {
                _Index++;
                StringBuffer _Buf = new StringBuffer();
                _Buf.append(_Index);
                _Buf.append(".");
                _Buf.append(subtitles.get(i));
                _List.add(_Buf.toString());
            }
            videoView.setSubtitleLanguageList(_List);
            videoView.setSubtitleNumber(_List.size());
        }
        if (controlModel.getSelectedSubId() >= videoView.getSubtitleNumber()) {
            controlModel.setSelectedSubId(0);
        }
        videoView.setSubtitleId(controlModel.getSelectedSubId());
        setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_VERTICAL, controlModel.getSelectedPosition());
        setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_STYLE, controlModel.getSelectedEffect());
        setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_SIZE, controlModel.getSelectedSizes());
        setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_SPACE, controlModel.getSelectedSpace());
        setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_LINESPACE, controlModel.getSelectedLSpace());
        setNewFounction(HiMediaPlayerInvoke.CMD_SET_SUB_FONT_COLOR, Integer.parseInt(controlModel.getSelectedColor().replace("0x", ""), 16));
        if (controlModel.getSelectedVolume() <= 20) {
            controlModel.setSelectedVolume(20);
        }

        videoView.setVolume(controlModel.getSelectedVolume() / 100f, controlModel.getSelectedVolume() / 100f);
    }

    private void updateDraInfo() {
        sendMsgWhatOnly(Constants.UPDATEDRAINFO);
    }

    private void setVideoCvrs() {
        if (isFullScreen) {
            if (videoView.setVideoCvrs(Constants.SCREEN_FULL) != -1) {
                setVideoScale(Constants.SCREEN_FULL);
            }
        } else {
            if (videoView.setVideoCvrs(Constants.SCREEN_DEFAULT) != -1) {
                String brand_name = SystemProperties.get("ro.product.brand");
                if (brand_name.startsWith("HiDPT")) {
                    setVideoScale(Constants.SCREEN_ONESIDE_FULL);
                }
            }
        }
    }

    private void setVideoStep() {
        int duration = videoView.getDuration() / 1000;
        if (duration < 2 * 60) {
            //Log.i(TAG,"setVideoStep:1,1");
            common.setStep(1);
            common.setInitStep(1);
            common.setAccStep(1);
        } else if (duration < 10 * 60) {
            //Log.i(TAG,"setVideoStep:1,2");
            common.setStep(1);
            common.setInitStep(1);
            common.setAccStep(2);
        } else if (duration < 30 * 60) {
            //Log.i(TAG,"setVideoStep:5,5");
            common.setStep(5);
            common.setInitStep(5);
            common.setAccStep(5);
        } else {
            //Log.i(TAG,"setVideoStep:5,10");
            common.setStep(5);
            common.setInitStep(5);
            common.setAccStep(10);
        }
    }

    private void checkDbData(String path) {
        Cursor cursor = null;

        try {
            cursor = database.query(Constants.TABLE_VIDEO,
                    new String[]{"_id"}, " _data = ? ", new String[]{path},
                    null, null, null);
            if (cursor.getCount() == 0) {
                ContentValues value = new ContentValues();
                value.put("_data", path);
                value.put("recommended", 0);
                database.insert(Constants.TABLE_VIDEO, null, value);
            }
        } catch (SQLException e) {
            LogTool.e("in checkDbData" + e.toString());
        } catch (IllegalArgumentException e) {
            LogTool.e("in checkDbData", e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    protected void getMkvInfo(Parcel mediaInfo) {
        if (null != mediaInfo) {
            mediaInfo.readInt();
            mediaInfo.readString();
            mediaInfo.readString();
            mediaInfo.readString();
            mediaInfo.readString();
            mediaInfo.readString();
            mediaInfo.readString();
            int chapterNum = mediaInfo.readInt();
            if (chapterNum > 0) {
                mkvInfos = new ArrayList<ModeMkvInfo>();
                for (int i = 0; i < chapterNum; i++) {
                    ModeMkvInfo mkvInfo = new ModeMkvInfo();
                    mkvInfo.setChapterUID(mediaInfo.readLong());
                    mkvInfo.setChapterTimeStart(mediaInfo.readInt());
                    mkvInfo.setChapterTimeEnd(mediaInfo.readInt());
                    mkvInfo.setChapTitle(mediaInfo.readString());
                    mkvInfo.setChapLanguage(mediaInfo.readString());
                    mkvInfos.add(mkvInfo);
                }
                titles = new String[mkvInfos.size()];
                for (int i = 0; i < titles.length; i++) {
                    StringBuilder sb = new StringBuilder();
                    if (i < 9)
                        sb.append(0);
                    sb.append(i + 1);
                    sb.append(". ");
                    sb.append(mkvInfos.get(i).getChapTitle());
                    sb.append("\n");
                    sb.append(TimeTool.mill2String(mkvInfos.get(i)
                            .getChapterTimeStart()));
                    titles[i] = sb.toString();
                }
                playerController.setMkvVideo(true);
            } else {
                playerController.setMkvVideo(false);
            }
        }
    }


    private void setInfoCue() {
        TextView tmpCue;
        if (playerController.getDolbyCertification() == 1) {
            tmpCue = tvAudio;
        } else {
            tmpCue = tvSub;
        }
        String trackStr = videoView.getCurrSubTrack();
        if (!TextUtils.isEmpty(trackStr)) {
            tmpCue.setText(trackStr);
        } else {
            tmpCue.setText(R.string.nosubTitle);
        }
        Parcel reply;
        if (playerController.getDolbyCertification() == 0) {
            List<String> audioList = videoView.getAudioTrackLanguageList();
            if (audioList != null && audioList.size() != 0) {
                int trackPid = videoView.getAudioTrackPid();
                LogTool.d(VideoActivity.class.getSimpleName(), "Audio" + trackPid);
                if (trackPid >= 0 && trackPid < audioList.size()) {
                    String currAudio = audioList.get(trackPid);
                    if (PlayerJugdment.isHiPlayer()) {
                        currAudio = currAudio.substring(2, currAudio.length());
                    }
                    LogTool.d(VideoActivity.class.getSimpleName(), "Audio : " + trackPid + " str : " + currAudio);
                    tvAudio.setText(currAudio);
                } else {
                    tvAudio.setText(R.string.noTrack);
                }
            } else {
                tvAudio.setText(R.string.noTrack);
            }
        } else {
            String mDolbyInfo = null;

            List<String> audioList = videoView.getAudioTrackLanguageList();
            if (audioList != null && audioList.size() != 0) {
                String currAudio = audioList.get(videoView.getSelectAudioTrackId());
                currAudio = currAudio.substring(2, currAudio.length());
                mDolbyInfo = getResources().getString(R.string.DolbyTrack) + currAudio + "   \t";
            } else {
                mDolbyInfo = getResources().getString(R.string.DolbyTrack) + getResources().getString(R.string.noTrack) + "\t";
            }

            reply = getInfo(HiMediaPlayerInvoke.CMD_GET_DOLBYINFO);

            reply.readInt();
            int accode = reply.readInt();
            reply.recycle();

            mDolbyInfo += getResources().getString(R.string.DolbyMono) + getMonoInfo(accode) + "   \t";
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mDolbyInfo += getResources().getString(R.string.DolbyVolume) + current + "   \t";

            reply = getInfo(HiMediaPlayerInvoke.CMD_GET_AUDIO_CHANNEL_MODE);
            reply.readInt();
            int streamtype = reply.readInt();
            String[] channels = getResources().getStringArray(R.array.channel);
            mDolbyInfo += getResources().getString(R.string.DolbyChannel) + channels[streamtype];
            tvSub.setText(mDolbyInfo);
            reply.recycle();
        }

        TextView mediaSize = (TextView) findViewById(R.id.mediaSize);
        if (playerController.getDolbyCertification() == 0) {
            mediaSize.setText(new Tools().formatSize(playerController.getCurrSize()));
        } else if (playerController.getDolbyCertification() == 1) {
            reply = getInfo(HiMediaPlayerInvoke.CMD_GET_VIDEO_INFO);
            reply.readInt();
            int format = reply.readInt();
            reply.recycle();
            mediaSize.setText(getResources().getString(R.string.DolbyVideo) + videoFormatValue[format] + "\t"
                    + new Tools().formatSize(playerController.getCurrSize()));
        }
    }

    private void start() {
        if (PlayerJugdment.isHiPlayer()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    videoStart();
                    setNewFounction(HiMediaPlayerInvoke.CMD_SET_AUDIO_CHANNEL_MODE, controlModel.getSelectedAudio());
                    sendMsgWhatOnly(Constants.GET_DOLBY_INFO);
                }
            }, 200);
        } else {
            videoStart();
            setNewFounction(HiMediaPlayerInvoke.CMD_SET_AUDIO_CHANNEL_MODE, controlModel.getSelectedAudio());
            sendMsgWhatOnly(Constants.GET_DOLBY_INFO);
        }
    }

    private void sendMsgWhatOnly(int getDolbyInfo) {
        Message message = new Message();
        message.what = getDolbyInfo;
        mHandler.removeMessages(getDolbyInfo);
        mHandler.sendMessageDelayed(message, 2000);
    }

    private void videoStart() {
        videoView.start();
        mySeekBar.setMax(videoView.getDuration());
        controlModel.setForwardRate(1);
        controlModel.setRewindRate(1);
        playerController.setRewindOrForward(false);
        //for rewind to next video
        ivPlayPause.setBackgroundResource(R.drawable.pause_button);//for rewind to next video
        ivPlayStatus.setVisibility(View.INVISIBLE);
        videoView = (HisiVideoView) findViewById(R.id.videoView);
        isPause = false;
        String brandName = SystemProperties.get(VersionControl.PRODUCT_BRAND);
        if (brandName.startsWith(VersionControl.BRAND_HIDPT) && (Build.VERSION_CODES.P > VERSION.SDK_INT)
                && !isCheckAtmos) {
            isCheckAtmos = true;
            atmosHandler.postDelayed(atmosRunable, 2000);
        }
    }

    private boolean isCheckAtmos = false;
    Handler atmosHandler = new Handler();
    Runnable atmosRunable = new Runnable() {
        @Override
        public void run() {
            DoblyResult dolbyAtmos = videoView.getDolbyAtmos();
            showAtmosIcon(dolbyAtmos);
            if (isCheckAtmos) {
                atmosHandler.postDelayed(atmosRunable, 3000);
            }
        }
    };

    private void saveCurrentPosition() {
        if (common.isValueTrue(SecondMenuConfig.ADVANCED_CONTINUE_PLAY)) {
            if (database != null) {
                int currPosition = 0;
                if (isClickBlueButton) {
                    currPosition = time;
                    isClickBlueButton = false;
                } else {
                    currPosition = videoView.getCurrentPosition();
                    if (currPosition == 0) {
                        return;
                    }
                }
                ContentValues values = new ContentValues();
                if ((videoView.getDuration() - 10000) > currPosition) {
                    values.put("last_play_postion", currPosition);
                    database.update(Constants.TABLE_VIDEO, values, "_data=?",
                            new String[]{playerController.getCurrPlayPath()});
                } else {
                    values.put("last_play_postion", 0);
                    database.update(Constants.TABLE_VIDEO, values, "_data=?",
                            new String[]{playerController.getCurrPlayPath()});
                }
            }
        }
    }


    private void getVideoInfo(VideoModel model) {
        saveCurrentPosition();  //when press pageup\pagedown key, save currentPosition for next time to contine play
        if (!playerController.isFirstClick()) {
            videoView.setStereoVideoFmt(0);
            hiDisplayManager.setRightEyeFirst(0);
            hiDisplayManager.set3DMode(HI_2D_MODE, 0);
            playerController.setmCurrMode(0);
            playerController.setMvcType(0);
            ctrlbarDismiss();
            if (!isPipMode && !common.getPipVideoTag()) {
                showLoadProgressDialog();
            }
            videoView.reset();
        }
        isTo2DOriginal = false;
        isTo3DOriginal = false;
        getVideoInfo_noCycle(model);
        LogTool.d("player video name:" + playerController.getCurrName() + " path : " + playerController.getCurrPath());
        videoView.setVideoPath(playerController.getCurrPath());
    }

    protected int getLastPosition(String path) {
        int positon = 0;
        Cursor cursor = null;
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        try {
            cursor = database.query(Constants.TABLE_VIDEO, new String[]{
                            "_id", "last_play_postion"
                    }, " _data = ? ",
                    new String[]{path}, null, null, null);

            if (cursor.moveToNext()) {
                positon = cursor.getInt(cursor.getColumnIndex("last_play_postion"));
            }
        } catch (SQLException e) {
            LogTool.e("in getLastPosition");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return positon;
    }

    protected void updatePositon(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        ContentValues values = new ContentValues();

        values.put("last_play_postion", 0);
        if (database == null) {
            database = mDbHelper.getWritableDatabase();
        }

        database.update(Constants.TABLE_VIDEO, values, "_data=?", new String[]{path});
    }

    private String getMonoInfo(int accode) {
        if (accode < 0 || accode >= monoInfo.length)
            return "";
        return monoInfo[accode];
    }

    private void initSDRMode() {
        if (common.sharedPreferencesOpration(Constants.SHARED, "SDRMode", 0, 0, false) == 1) {
            videoView.setHDRInvoke(0);
        } else {
            videoView.setHDRInvoke(1);
        }
    }

    public void initDisplayModeAndVideoFmt() {
        if (playerController.is3DMode() == true && playerController.getFormat() == HI_VIDEOFORMAT_TYPE_MVC) {
            //Log.i(TAG, "HI_VIDEOFORMAT_TYPE_MVC");
            if (playerController.is3DOutput() && playerController.getmVC3DAdapte() == 1) {
                // videoView.setStereoStrategy(1);
                hiDisplayManager.set3DMode(1, 0);
                playerController.setmCurrMode(1);
            } else {
                playerController.setmCurrMode(0);
            }
            playerController.setMvcType(1);
            return;
        }
        videoView.setStereoVideoFmt(0);
        hiDisplayManager.setRightEyeFirst(0);
        //mDisplayManager.set3DMode(HI_2D_MODE,0);
    }

    private void getDuration() {
        int total = videoView.getDuration();
        if (total > 0) {
            tvTotalTime.setText(Common.getTimeFormatValue(total));
        } else {
            tvTotalTime.setText(R.string.nototaltime);
        }

        int temp = total / 1000 / 60;
        common.limitHour = temp / 60;
        common.limitMinute = temp;
        Common.setDuration(total);

        if (playerController.getDolbyCertification() == 0) {
            tvMediaSize.setText(new Tools().formatSize(playerController.getCurrSize()));
        } else if (playerController.getDolbyCertification() == 1) {
            Parcel reply = getInfo(HiMediaPlayerInvoke.CMD_GET_VIDEO_INFO);
            reply.readInt();
            int format = reply.readInt();
            reply.recycle();
            tvMediaSize.setText(getResources().getString(R.string.DolbyVideo) + videoFormatValue[format]
                    + "\t" + new Tools().formatSize(playerController.getCurrSize()));
        }
        tvMediaName.setText(playerController.getCurrName());
    }

    private void doExtraOpration() {
        setInfoCue();
        if (nameSizeHandler != null)
            nameSizeHandler.postDelayed(nameSizeDismissRunnable, Constants.NAME_SIZE_HIDE_TIME);
    }

    private Handler ctrlBarHandler = new Handler();
    private Runnable ctrlRunnable = new Runnable() {
        @Override
        public void run() {
            long endTime = System.currentTimeMillis();
            long distance = endTime - startTime;

            if (distance >= Constants.CTRLBAR_HIDE_TIME) {
                ctrlbarDismiss();
                return;
            }
            ctrlBarHandler.postDelayed(this, 1000);
        }
    };
    private Handler nameSizeHandler = new Handler();
    private Runnable nameSizeDismissRunnable = new Runnable() {
        @Override
        public void run() {
            mediaInfoLayout.setVisibility(View.INVISIBLE);
            nameSizeHandler.removeCallbacks(this);
        }
    };

    private Runnable dolbyDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            doExtraOpration();
        }
    };


    private void startThread() {
        if (!isThreadStart) {
            startTime = System.currentTimeMillis();
            mHandler.removeMessages(Constants.showMediaController);
            mHandler.sendEmptyMessage(Constants.showMediaController);
            ctrlBarHandler.post(ctrlRunnable);
            isThreadStart = true;
        } else {
            startTime = System.currentTimeMillis();
        }
    }

    private void ctrlbarDismiss() {
        LogTool.d(VideoActivity.class.getSimpleName(), " Ctrl BAR Dismiss", new Exception("show ctrl Bar"));
        disMediaMsgAndControlLayout();

        initSeekSecondaryProgress();

        ctrlBarHandler.removeCallbacks(ctrlRunnable);
        isThreadStart = false;
        isSeekBarSelected = true;
    }


    private void initSeekSecondaryProgress() {
        progerssFwRwind = -1;
        mySeekBar.setSecondaryProgress(0);
        haveLeftRightOpration = false;
    }

    @Override
    protected void initData() {
        if (notSptDialog != null && notSptDialog.isShowing()) {
            notSptDialog.dismiss();
        }
        if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isPipMode = isInPictureInPictureMode();
            if (isPipMode) {
                disMediaMsgAndControlLayout();
            }
        }
        getUSB();
        // Init Suspend Mode when equal 'deep_resume'
        if (SystemProperties.get("persist.suspend.mode").equals("deep_resume")) {
            PowerControllerInit();
        }
        Intent intent = getIntent();
        int oneKeyPlayTag = intent.getIntExtra(ONE_KEY_PLAY_TAG, ONE_KEY_PLAY_DEFAULT);
        String oneKeyPlayPath = "";
        if (ONE_KEY_PLAY_DEFAULT != oneKeyPlayTag) {
            SharedPreferences sharedPreferences = getSharedPreferences(ONE_KEY_PLAY_TAG, Context.MODE_PRIVATE);
            oneKeyPlayPath = sharedPreferences.getString(ONE_KEY_PLAY_TAG, "");
            File file = new File(oneKeyPlayPath);
            if (TextUtils.isEmpty(oneKeyPlayPath) || (!file.exists())) {
                Intent mExpIntent = new Intent();
                mExpIntent.setClassName("com.hisilicon.explorer", "com.hisilicon.explorer.activity.TabBarExample");
                startActivity(mExpIntent);
                finish();
                return;
            }
        }
        Common.setShowNoFileToast(false);
        menuConfig = new MenuConfig(VideoActivity.this);
        secondMenuConfig = new SecondMenuConfig();
        thirdMenuConfig = new ThirdMenuConfig();
        common = new Common(VideoActivity.this, VideoActivity.this, screenWidth);
        playerController = new PlayerController();
        controlModel = new ControlModel();
        this.initDB();
        hiDisplayManager = new HiDisplayManager();
        parseMode();
        LogTool.d(VideoActivity.class.getSimpleName(), "init Data isPIP : " + isPipMode
                + " common.getPipVideoTag() : " + common.getPipVideoTag());
        if (!isPipMode && !common.getPipVideoTag()) {
            showLoadProgressDialog();
            Common.setLastMediaFile(false);
        }
        xmlParser = new LanguageXmlParser(this);
        mediaFileList = intent.getParcelableExtra("MediaFileList");
        EncodeXmlParser encodexmlParser = new EncodeXmlParser(this);
        encodeList = encodexmlParser.getEncodesList().get(0);
        if (mediaFileList != null) {
            if (mediaFileList.getId() == 1) {
                Intent service = new Intent(Constants.ACTION);
                service.setPackage("com.hisilicon.android.videoplayer");
                bindService(service, conn, Context.BIND_AUTO_CREATE);
                isServiceBind = true;
            }
            playerController.setFirstClick(true);
            getVideoInfo_noCycle(mediaFileList.getCurrVideoInfo());
        } else if (ONE_KEY_PLAY_DEFAULT != oneKeyPlayTag) {
            playerController.setCurrPlayPath(oneKeyPlayPath);
            playerController.setCurrPath(oneKeyPlayPath);
            if (!Common.isSecurePath(playerController.getCurrPlayPath())) {
                LogTool.e("getCurrPath:Path Manipulation");
                finish();
            }
            File file = new File(playerController.getCurrPlayPath());
            playerController.setCurrName(file.getName());
            playerController.setCurrSize(file.length());
            playerController.setCurrPlayPath(playerController.getCurrPath());
        } else {
            Uri uri = intent.getData();
            if (null == uri) {
                LogTool.d("Fail get Uri is null");
                return;
            }
            playerController.setCurrPath(uri.toString());
            if (!Common.isSecurePath(playerController.getCurrPath())) {
                LogTool.e("getCurrPath:Path Manipulation");
                finish();
            }
            File file = new File(playerController.getCurrPath());
            playerController.setCurrName(file.getName());
            playerController.setCurrSize(file.length());
            playerController.setCurrPlayPath(uri.toString());
        }
        videoView.setVideoPath(playerController.getCurrPath());
        initPlayerProp();
        if (common.isValueTrue(SecondMenuConfig.ADVANCED_FORWARD_REWIND)) {
            showForwardRewind(true);
        }
        common.setMode(common.sharedPreferencesOpration(Constants.SHARED,
                SecondMenuConfig.ADVANCED_SINGLE_CYCLY, 0, 0, false));
        if(this.isNewIntent){
           multiPlayTextureView.setVisibility(View.INVISIBLE);
           multiPlaySurfaceView.setVisibility(View.INVISIBLE);
           ivMenu.setBackgroundResource(R.drawable.multiple_off_button);
        }
    }

    private void showForwardRewind(boolean isShow) {
        if (isShow) {
            ivForward.setVisibility(View.VISIBLE);
            ivRewind.setVisibility(View.VISIBLE);
        } else {
            ivForward.setVisibility(View.GONE);
            ivRewind.setVisibility(View.GONE);
        }
    }

    private void parseMode() {
        DispFmt mDispFmt = new DispFmt(hiDisplayManager.getDisplayCapability());
        String brand = SystemProperties.get(VersionControl.PRODUCT_BRAND);
        if (Build.VERSION.SDK_INT >= 26 && brand != null && brand.startsWith(VersionControl.BRAND_HISTP)) {
            if (mDispFmt.getDispFmtObj() == null) {
                playerController.setIs3DMode(false);
            } else if (SystemProperties.get("persist.prop.fpk.enforce", "true").equals("true") && (0 != (mDispFmt.getIs_support_3d() & 0x01))) {
                playerController.setIs3DMode(true);
                playerController.setIs3DOutput(true);
            } else if (SystemProperties.get("persist.prop.fpk.enforce", "true").equals("false") && (0 != (mDispFmt.getIs_support_3d() & 0x01))) {
                playerController.setIs3DMode(true);
                if (0 != (mDispFmt.getIs_support_3d() & 0x02)) {
                    playerController.setIs3DOutput(true);
                } else {
                    playerController.setIs3DOutput(false);
                }
            } else {
                playerController.setIs3DMode(false);
                playerController.setIs3DOutput(false);
            }
        } else {
            if (mDispFmt.getDispFmtObj() == null) {
                playerController.setIs3DMode(false);
            } else if (SystemProperties.get("persist.sys.fpk.enforce", "true").equals("true") && (0 != (mDispFmt.getIs_support_3d() & 0x01))) {
                playerController.setIs3DMode(true);
                playerController.setIs3DOutput(true);
            } else if (SystemProperties.get("persist.sys.fpk.enforce", "true").equals("false") && (0 != (mDispFmt.getIs_support_3d() & 0x01))) {
                playerController.setIs3DMode(true);
                if (0 != (mDispFmt.getIs_support_3d() & 0x02)) {
                    playerController.setIs3DOutput(true);
                } else {
                    playerController.setIs3DOutput(false);
                }
            } else {
                playerController.setIs3DMode(false);
                playerController.setIs3DOutput(false);
            }
        }
        playerController.setOriginalFmt(hiDisplayManager.getFmt());
        playerController.setMvcType(0);
        playerController.setIs3DTiming(false);
    }

    private void getVideoInfo_noCycle(VideoModel model) {
        if (model != null) {
            if (Common.isLoadSuccess() || playerController.isFirstClick()) {
                playerController.setCurrPath(model.getPath());
                playerController.setCurrMode(model.getMimeType());
                playerController.setCurrSize(model.getSize());
                playerController.setCurrName(model.getTitle());
                playerController.setCurrId(model.getId());
                playerController.setFirstClick(false);
                playerController.setCurrPlayPath(playerController.getCurrPath());
            }
        } else {
            if (Common.isLoadSuccess()) {
                if (!Common.haveShowNoFileToast()) {
                    ToastUtil.showMessage(this, R.string.havenofile);
                    Common.setShowNoFileToast(true);
                }
            } else {
                if (Common.isShowLoadingToast()) {
                    ToastUtil.showMessage(this, R.string.isloading);
                }
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case Constants.USBUNINSTALL:
                    finish();
                    break;
                case Constants.showMediaController:
                    msg = obtainMessage(Constants.showMediaController);
                    int time = videoView.getCurrentPosition();
                    if (!haveLeftRightOpration) {
                        mySeekBar.setProgress(time);
                        //timeTextView.setText(Common.getTimeFormatValue(time));
                    }
                    tvTime.setText(Common.getTimeFormatValue(time));
                    sendMessageDelayed(msg, 500);
                    break;
                case Constants.hideMediaController:
                    removeMessages(Constants.showMediaController);
                    removeMessages(Constants.hideMediaController);
                    break;
                case Constants.switchSubtitle:
                    if (controlModel.getSelectedSubId() - 1 < 0) {
                        controlModel.setSelectedSubId(videoView.getSubtitleNumber() - 1);
                    } else {
                        controlModel.setSelectedSubId(controlModel.getSelectedSubId() - 1);
                    }
                    if (videoView.setSubtitleId(controlModel.getSelectedSubId()) == 0) {
                        videoView.setSelectSubtitleId(controlModel.getSelectedSubId());

                    } else
                        ToastUtil.showMessage(VideoActivity.this, getString(R.string.switchFailed), Toast.LENGTH_SHORT);
                    break;
                case Constants.switchAudioTrack:
                    if (videoView.setAudioTrackPid(controlModel.getSelectedTrack()) == 0) {
                        videoView.setSelectAudioTrackId(controlModel.getSelectedTrack());
                    } else {
                        ToastUtil.showMessage(VideoActivity.this, getString(R.string.switchFailed), Toast.LENGTH_SHORT);
                    }
                    break;
                case Constants.UPDATEDRAINFO:
                    setInfoCue();
                    break;
                case Constants.POWER_OFF:
                    powerOff();
                    break;
                case Constants.GET_DOLBY_INFO:
                    getDolbyInfo();
                    break;
                default:
                    break;
            }
        }
    };

    private void getDolbyInfo() {
        Parcel getCount;
        getCount = getInfo(HiMediaPlayerInvoke.CMD_GET_DOLBYINFO);
        int error = getCount.readInt();
        int s32ACMode = getCount.readInt();
        int s32StreamType = getCount.readInt();
        long u32DecoderType = getCount.readLong();
        getCount.recycle();
        LogTool.d(VideoActivity.class.getSimpleName(), "error = " + error + " acmode = " + s32ACMode + " s32StreamType = " + s32StreamType + " u32DecoderType = " + u32DecoderType);
        if (s32StreamType == 0 && u32DecoderType == dolby_decoder) {
            mDolby.setText(R.string.dolby);
            mDolby.setVisibility(View.VISIBLE);
        } else if (s32StreamType == 1 && u32DecoderType == dolby_decoder) {
            mDolby.setText(R.string.dolby_plus);
            mDolby.setVisibility(View.VISIBLE);
        } else {
            mDolby.setVisibility(View.INVISIBLE);
        }
    }

    private void powerOff() {
        acquireWakeLock();
        if (videoView != null && videoView.mMediaPlayer != null) {
            if (videoView.isPlaying()) {
                videoView.pause();
                ivPlayPause.setBackgroundResource(R.drawable.play_button);
            }
        } else {
            //Log.i(TAG, "videoView is not Ready!");
        }
        // release Lock
        wakeLock.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerObserver();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isNewIntent = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.isNewIntent = true;
        setIntent(intent);
        initData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isNotSupportShow) {
            finish();
        }
    }
    private void unregisterPowerReceiver(){
        if (!SystemProperties.get("persist.suspend.mode").equals("deep_resume")) {
            return;
        }
        if (mPowerReceiver != null) {
            unregisterReceiver(mPowerReceiver);
        }
    }
    private void unregisterUsbReceiver(){
        if (usbReceiver != null) {
            unregisterReceiver(usbReceiver);
        }
    }

    private void initDB(){
        if(this.isNewIntent){
            return;
        }
        mDbHelper = new DBHelper(this, Constants.MARK_DB_NAME, null, Constants.VERSION);
        database = mDbHelper.getWritableDatabase();
    }

    private void clearDB(){
        if(database != null){
            database.close();
            database = null;
        }

        if(mDbHelper != null){
            mDbHelper.close();
            mDbHelper = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBind) {
            unbindService(conn);
            isServiceBind = false;
        }
        this.unregisterPowerReceiver();
        this.unregisterUsbReceiver();
        this.clearDB();
        unregisterObserver();
        this.removeAtmos();

    }

    private void removeAtmos(){
        atmosHandler.removeCallbacks(null);
        this.isCheckAtmos = false;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (mIs8KMouseMode) {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                mouseView.setDown(event.getX(), event.getY());

            } else if (MotionEvent.ACTION_MOVE == event.getAction()) {
                mouseView.setMove(event.getX(), event.getY());
                mouseView.setWorkStatus(true);
            } else if (MotionEvent.ACTION_UP == event.getAction()) {
                if (!mIs8KMouseInScale && mouseView.getWorkStatus()) {
                    int[] point = mouseView.getRectPoint();
                    int virtualVideoWidth = point[2] - point[0];
                    int virtualVideoHeight = point[3] - point[1];
                    if (virtualVideoWidth > 0 && virtualVideoHeight > 0) {
                        int[] realPoints = calculation8KVideoRect(point[0], point[1], point[2], point[3], xRatioFor8K, yRatioFor8K);
                        int ret = videoView.setVideoRect(realPoints[0], realPoints[1], realPoints[2], realPoints[3]);
                        if (ret == 0) {
                            mIs8KMouseInScale = true;
                            LogTool.i("reset 8k to normal success!");
                        }
                    }
                } else {
                    Toast.makeText(this, getString(R.string.mouseScaleOnlyOneTip), Toast.LENGTH_SHORT).show();
                }
                mouseView.setWorkStatus(false);
            } else {
                mouseView.setWorkStatus(false);
            }
            return super.onTouchEvent(event);
        }
        if ((MotionEvent.ACTION_DOWN == event.getAction()) || (MotionEvent.ACTION_MOVE == event.getAction())) {
            setInfoCue();
            if (!isPipMode) {
                mediaControllerLayout.setVisibility(View.VISIBLE);
                mediaInfoLayout.setVisibility(View.VISIBLE);
            }
            startThread();
            return false;
        }

        return super.onTouchEvent(event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (haveLeftRightOpration) {
                    mySeekBar.setProgress(progerssFwRwind);
                    videoView.seekTo((int) progerssFwRwind);
                    mySeekBar.clearFocus();
                    //initSeekSecondaryProgress();
                    common.setStep(common.getInitStep() / 1000);
                    //play.setBackgroundResource(R.drawable.pause_button);
                    //videoStart();
                }
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //如果是区域点击模式，已经放大一次点击返回变成现实所有区域
        //所有区域点击返回退出8k模式
        if (mIs8KAreaMode) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (crossView.getAreaIsHide()) {
                    crossView.showAllArea();
                    //退出放大模式，变回小的调用媒体接口
                    reset8KToNormal();
                } else {
                    crossView.hideAllArea();
                    mIs8KAreaMode = false;
                }
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        if (mIs8KMouseMode) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mIs8KMouseInScale) {
                    reset8KToNormal();
                    mIs8KMouseInScale = false;
                } else {
                    Toast.makeText(this, getString(R.string.mouseScaleExit), Toast.LENGTH_SHORT).show();
                    mIs8KMouseMode = false;
                    mouseView.setVisibility(View.GONE);
                }
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE || keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            if (videoView != null && (videoView.isPlaying() || (isPause == true)) && videoView.getCurrentPosition() != 0) {
                time = videoView.getCurrentPosition();
                isClickBlueButton = true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (isThreadStart) {
                ctrlbarDismiss();
                return true;
            }else {
                if (isMultiplePlay) {
                    beginFinish();
                    return true;
                } else {
                    saveCurrentPosition();
                    videoView.destroyPlayer();
                    finish();
                }
            }
        } else if ((keyCode != KeyEvent.KEYCODE_VOLUME_UP) && (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN)
                && (keyCode != KeyEvent.KEYCODE_VOLUME_MUTE)) {
            //Log.i(TAG, "keyCode:" + keyCode);
            if ((keyCode == Constants.TV_INFO)
                    || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                    || (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                    || (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD)
                    || (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND)
                    || (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                    || (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT)
                    || (keyCode == KeyEvent.KEYCODE_ENTER)
                    || (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
                if ((keyCode == KeyEvent.KEYCODE_ENTER) || (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
                    //don't pause when first time press ENTER key
                    if (mediaControllerLayout.getVisibility() == View.VISIBLE && !playerController.isRewindOrForward()) {
                        //Log.i(TAG, "keyCode:call pause--" + keyCode);
                        play_pause();
                    }
                }
                setInfoCue();
                showVideoMessageAndControlLayout();
                startThread();
            }

            return dealWithKeyCode(keyCode, event);
            // return true;
        }

        return super.onKeyDown(keyCode, event);

    }

    private boolean dealWithKeyCode(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                popUpMenuDialog();
                break;
            case KeyEvent.KEYCODE_INFO:
                setInfoCue();
                if (!isPipMode) {
                    mediaInfoLayout.setVisibility(View.VISIBLE);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return dealWithRight();
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return dealWithLeft();
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                setForward();

                break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                setRewind();

                break;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                finish();
                break;
            case KeyEvent.KEYCODE_PAGE_DOWN:
                videoJump(true);
                break;
            case KeyEvent.KEYCODE_PAGE_UP:
                videoJump(false);
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                videoPause(true);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (isThreadStart) {
                    isSeekBarSelected = true;
                    //btnLinearLayout.getChildAt(defaultFocus).clearFocus();
                    mySeekBar.requestFocus();
                    ivPlayPause.clearFocus();
                } else {
                    controlModel.setSelectedPosition(controlModel.getSelectedPosition() + controlModel.getPositionStep());
                    controlModel.setSelectedPosition(Math.min(controlModel.getSelectedPosition(), screenHeight - subtitleMarginVertical - subtitleMaxSizeHeight));
                    videoView.setSubVertical(controlModel.getSelectedPosition());
                    common.sharedPreferencesOpration(Constants.SHARED, "selectedPosition", controlModel.getSelectedPosition(), 0, true);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (isThreadStart) {
                    //btnLinearLayout.getChildAt(defaultFocus).requestFocus();
                    isSeekBarSelected = false;
                    ivPlayPause.requestFocus();
                    mySeekBar.clearFocus();
                    defaultFocus = 2;
                } else {
                    controlModel.setSelectedPosition(controlModel.getSelectedPosition() - controlModel.getPositionStep());
                    controlModel.setSelectedPosition(Math.max(controlModel.getSelectedPosition(), 0));
                    videoView.setSubVertical(controlModel.getSelectedPosition());
                    common.sharedPreferencesOpration(Constants.SHARED, "selectedPosition", controlModel.getSelectedPosition(), 0, true);
                }
                break;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mySeekBar.requestFocus();
                videoPause(false);
                break;
            case KeyEvent.KEYCODE_CAPTIONS:
                if (videoView.getSubtitleNumber() != 0) {
                    Message message = new Message();
                    message.what = Constants.switchSubtitle;

                    int _SubNum = videoView.getSubtitleNumber();
                    int _ExtNum = videoView.getExtSubtitleNumber();
                    int _Select = controlModel.getSelectedSubId();

                    if (_ExtNum != 0) {
                        if (_Select >= (_SubNum - _ExtNum))
                            _Select = _Select - (_SubNum - _ExtNum);
                        else if (_Select < (_SubNum - _ExtNum))
                            _Select = _Select + _ExtNum;
                    }
                    ToastUtil.showMessage(VideoActivity.this, getString(R.string.toastSubtitle, new Object[]
                            {videoView.getSubtitleLanguageList().get(_Select)}), Toast.LENGTH_SHORT);

                    int subId = (controlModel.getSelectedSubId() + 1) % videoView.getSubtitleNumber();
                    controlModel.setSelectedSubId(subId);
                    mHandler.removeMessages(Constants.switchSubtitle);
                    mHandler.sendMessageDelayed(message, 2000);
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
                if (videoView.getAudioTrackNumber() != 0) {
                    Message message = new Message();
                    message.what = Constants.switchAudioTrack;

                    int track = (controlModel.getSelectedTrack() + 1) % videoView.getAudioTrackNumber();
                    controlModel.setSelectedTrack(track);
                    ToastUtil.showMessage(VideoActivity.this, getString(R.string.toastAudio, new Object[]
                            {videoView.getAudioTrackLanguageList().get(controlModel.getSelectedTrack())}), Toast.LENGTH_SHORT);

                    mHandler.removeMessages(Constants.switchAudioTrack);
                    mHandler.sendMessageDelayed(message, 2000);

                }
                break;
            case KeyEvent.KEYCODE_F4:
                try {
                    Method method = VideoActivity.this.getClass().getDeclaredMethod("enterPictureInPictureMode", Void.class);
                    method.invoke(VideoActivity.this);
                } catch (Exception e) {
                    LogTool.d("KeyCodeF4 : " + e.toString());
                }
//                 activity.enterPictureInPictureMode(); //for PIP
                return true;
        /*case KeyEvent.KEY_TVSETUP: //for quicksetting
            return false;*/
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean dealWithLeft() {
        boolean isConsume = true;
        if (isSeekBarSelected) {
            // Because of Hiplayer not support 'pause' when rewind or forward
            if (playerController.isRewindOrForward()) {
                ivPlayStatus.setVisibility(View.INVISIBLE);
                controlModel.setForwardRate(1);
                controlModel.setRewindRate(1);
                playerController.setRewindOrForward(false);
                videoView.resume();
                ivPlayPause.setBackgroundResource(R.drawable.pause_button);
            }
            mySeekBar.requestFocus();

            //to set te init pos when seek firstly
            if (-1 == progerssFwRwind) {
                progerssFwRwind = videoView.getCurrentPosition();
            }
            advanceAndRetreat(common.getStep(), false);
            haveLeftRightOpration = true;
            ivPlayPause.clearFocus();
        } else {//volume_ctl
            focusLeftChanged();
        }
        return isConsume;
    }

    private void focusLeftChanged() {
        int count = btnLinearLayout.getChildCount();
        if (defaultFocus < 1) {
            defaultFocus = count - 1;
        } else if (common.isValueTrue(SecondMenuConfig.ADVANCED_FORWARD_REWIND)) {
            defaultFocus -= 1;
        } else {
            defaultFocus -= 2;
        }

        if (ivPageUp.isFocused() && ivVolumeCtl.getVisibility() == View.VISIBLE) {
            ivVolumeCtl.requestFocus();
        } else if (ivVolumeCtl.isFocused() && ivMenu.getVisibility() == View.VISIBLE) {
            ivMenu.requestFocus();
        } else if (ivMenu.isFocused() && ivMenu.getVisibility() == View.VISIBLE) {
            btnLinearLayout.getChildAt(count - 1).requestFocus();
            defaultFocus = count - 1;
        } else if (ivVolumeCtl.isFocused() && ivMenu.getVisibility() != View.VISIBLE) {
            ivPageDown.requestFocus();
        } else {
            if (null == btnLinearLayout.getChildAt(defaultFocus)) {
                return;
            }
            LogTool.d("FocusLeft ", "Focus Index : " + defaultFocus);

            btnLinearLayout.getChildAt(defaultFocus).requestFocus();
        }
    }

    private boolean dealWithRight() {
        boolean isConsume = true;
        if (isSeekBarSelected) {
            // Because of Hiplayer not support 'pause' when rewind or forward
            if (playerController.isRewindOrForward()) {
                ivPlayStatus.setVisibility(View.INVISIBLE);
                controlModel.setForwardRate(1);
                controlModel.setRewindRate(1);
                playerController.setRewindOrForward(false);
                videoView.resume();
                ivPlayPause.setBackgroundResource(R.drawable.pause_button);
            }
            mySeekBar.requestFocus();

            //to set te init pos when seek firstly
            if (-1 == progerssFwRwind) {
                progerssFwRwind = videoView.getCurrentPosition();
            }
            advanceAndRetreat(common.getStep(), true);
            haveLeftRightOpration = true;
            ivPlayPause.clearFocus();
        } else {
            focuseRightChanged();
        }
        return isConsume;
    }

    //chang focuse right.
    private void focuseRightChanged() {
        int count = btnLinearLayout.getChildCount();
        if (defaultFocus >= count - 1) {
            defaultFocus = 0;
        } else if (common.isValueTrue(SecondMenuConfig.ADVANCED_FORWARD_REWIND)) {
            defaultFocus += 1;
        } else {
            defaultFocus += 2;
        }
        if (ivPageDown.isFocused() && ivMenu.getVisibility() == View.VISIBLE) {
            ivMenu.requestFocus();
        } else if (ivMenu.isFocused() && ivVolumeCtl.getVisibility() == View.VISIBLE) {
            ivVolumeCtl.requestFocus();
        } else if (ivVolumeCtl.isFocused() && ivVolumeCtl.getVisibility() == View.VISIBLE) {
            btnLinearLayout.getChildAt(0).requestFocus();
            defaultFocus = 0;
        } else if (ivPageDown.isFocused() && ivMenu.getVisibility() != View.VISIBLE) {
            ivVolumeCtl.requestFocus();
        } else {
            if (null == btnLinearLayout.getChildAt(defaultFocus)) {
                return;
            }
            LogTool.d("FocusRight ", "Focus Index : " + defaultFocus);
            btnLinearLayout.getChildAt(defaultFocus).requestFocus();
        }
    }

    private void advanceAndRetreat(int step, boolean isAdvance) {
        if (videoView.isPlaying()) {
            ivPlayPause.setBackgroundResource(R.drawable.play_button);
            videoView.pause();
            //update position
            progerssFwRwind = videoView.getCurrentPosition();
            playerController.setSeekWhenPlaying(true);
        }

        if (isAdvance) {
            progerssFwRwind += step;
        } else {
            progerssFwRwind -= step;
        }
        progerssFwRwind = Math.max(0, Math.min(progerssFwRwind, videoView.getDuration()));

        mySeekBar.setProgress(progerssFwRwind);
        startThread();
        step = step + common.getAccStep();
        if (step > common.getAccStep() * 18)
            step = common.getAccStep() * 18;
        common.setStep(step / 1000);
    }


    private void popUpMenuDialog() {
        ListView menuList = new ListView(this);
        if (menuKeyValuePairs == null) {
            menuKeyValuePairs = menuConfig.getPlayMenuNormalMode(playerController.isMkvVideo());
        }
        if (playerController.is3DMode()) {
            menuKeyValuePairs = menuConfig.getPlayMenu3DMode(playerController.isMkvVideo());
        } else {
            menuKeyValuePairs = menuConfig.getPlayMenuNormalMode(playerController.isMkvVideo());
        }
        menuList.setAdapter(new MenuAdapter(VideoActivity.this, menuKeyValuePairs));
        menuList.setOnItemSelectedListener(selectedListener);
        menuList.setOnItemClickListener(menuClickListener);
        menuList.setBackgroundResource(R.drawable.dialog_background_selector);
        menuList.setSelector(getResources().getDrawable(R.drawable.item_background_selector));
        menuDialog = getDefaultSettingDialog(R.string.video_control_menu, menuList);
        Common.setDialogWidth(menuDialog, screenWidth / 2, 500, -120);
        menuDialog.show();
        common.dialogAutoDismiss(menuDialog);
    }

    private int secondDialogTitleTmp;

    private void popUpSecondMenuDialog(@IdRes int title) {
        if (secodeMenuKeyValuePairs == null) {
            return;
        }
        secondDialogTitleTmp = title;
        ListView subItemList = new ListView(this);
        subItemList.setFocusableInTouchMode(true);
        subItemList.setAdapter(new MenuAdapter(this, secodeMenuKeyValuePairs));
        subItemList.setOnItemSelectedListener(selectedListener);
        subItemList.setOnItemClickListener(secondMenuClickListener);
        subItemList.setBackgroundResource(R.drawable.dialog_background_selector);
        subItemList.setSelector(getResources().getDrawable(R.drawable.item_background_selector));
        secondMenuDialog = getDefaultSettingDialog(title, subItemList);
        secondMenuDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                popUpMenuDialog();
            }
        });
        Common.setDialogWidth(secondMenuDialog, screenWidth / 2, 500, -120);
        if (menuDialog != null) {
            menuDialog.dismiss();
        }
        secondMenuDialog.show();
        common.dialogAutoDismiss(secondMenuDialog);
    }

    private void popThirdMenuDialog(@IdRes int title) {
        if (thirdMenuKeyValuePairs == null) {
            return;
        }
        checkHitype();
        ListView subItemList = new ListView(this);
        subItemList.setFocusableInTouchMode(true);
        if (title == R.string.subtitle_advanced) {
            subItemList.setAdapter(getSubAdvAdapter());
        } else {
            subItemList.setAdapter(new MenuAdapter(this, thirdMenuKeyValuePairs));
        }
        subItemList.setOnItemSelectedListener(selectedListener);
        subItemList.setOnItemClickListener(thirdMenuClickListener);
        subItemList.setBackgroundResource(R.drawable.dialog_background_selector);
        subItemList.setSelector(getResources().getDrawable(R.drawable.item_background_selector));
        thirdMenuDialog = getDefaultSettingDialog(title, subItemList);
        thirdMenuDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                popUpSecondMenuDialog(secondDialogTitleTmp);
            }
        });
        Common.setDialogWidth(thirdMenuDialog, screenWidth / 2, 500, -120);
        if (secondMenuDialog != null) {
            secondMenuDialog.dismiss();
        }
        thirdMenuDialog.show();
        common.dialogAutoDismiss(thirdMenuDialog);
    }

    private MenuAdapter getSubAdvAdapter() {
        String subTitle = videoView.getCurrSubTrack();
        LogTool.d(VideoActivity.class.getSimpleName(), "Subtitle====  : " + subTitle);
        if (TextUtils.isEmpty(subTitle)) {
            return new MenuAdapter(VideoActivity.this, thirdMenuKeyValuePairs, 0);
        }
        String lowerCase = subTitle.toLowerCase();
        //"ASS", "LRC", "SRT", "SMI", "SUB", "TXT", "PGS"
        if (TextUtils.isEmpty(lowerCase)) {
            return new MenuAdapter(VideoActivity.this, thirdMenuKeyValuePairs, 0);
        } else if (lowerCase.contains("srt") || lowerCase.contains("ssa") || lowerCase.contains("ass") || lowerCase.contains("smi")
                || lowerCase.contains("lrc") || lowerCase.contains("txt") || lowerCase.contains("text") || lowerCase.contains("ttml") || lowerCase.contains("sub")) {
            return new MenuAdapter(VideoActivity.this, thirdMenuKeyValuePairs);
        } else {
            return new MenuAdapter(VideoActivity.this, thirdMenuKeyValuePairs, 0);
        }
    }

    /*************************************/
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaFileListService = ((MediaFileListService.MediaFileListBinder) service).getService();
            if (mediaFileListService != null && !Common.isLoadSuccess()) {
                if (mediaFileListService.getThread() != null)
                    mediaFileListService.setThreadStart();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void getUSB() {
        if(this.isNewIntent){
            return;
        }
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbFilter.addDataScheme("file");
        registerReceiver(usbReceiver, usbFilter);
    }

    private boolean isCurrentPlayUsb(String path) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(playerController.getCurrPlayPath())) {
            return false;
        }
        return playerController.getCurrPlayPath().startsWith(path);
    }

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Uri uri = intent.getData();
            if (null == action || null == uri) {
                return;
            }
            if (action.equals(Intent.ACTION_MEDIA_EJECT) && isCurrentPlayUsb(uri.getPath())) {
                mHandler.sendEmptyMessage(Constants.USBUNINSTALL);
            }
        }
    };

    /**
     * Init Power Controller
     */
    public void PowerControllerInit() {
        if(this.isNewIntent){
            return;
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());

        // PowerManager Broadcast
        IntentFilter powerIntentFilter = new IntentFilter();
        powerIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        powerIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        powerIntentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mPowerReceiver, powerIntentFilter);
    }

    private final BroadcastReceiver mPowerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*Power Down*/
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                LogTool.w("recevie Intent.ACTION_SCREEN_OFF");
                // require Lock
                acquireWakeLock();
                if (videoView != null && videoView.mMediaPlayer != null) {
                    if (videoView.isPlaying()) {
                        videoView.pause();
                        ivPlayPause.setBackgroundResource(R.drawable.play_button);
                    }
                } else {
                    //Log.i(TAG, "videoView is not Ready!");
                }
                // release Lock
                wakeLock.release();
            }
        }
    };

    /**
     * require WakeLock
     */
    private void acquireWakeLock() {
        synchronized (wakeLock) {
            try {
                wakeLock.acquire();
            } catch (RuntimeException e) {
                LogTool.e("exception in acquireWakeLock()");
            }
        }
    }

    private void setNewFounction(int flag, int rate) {
        Parcel requestParcel = Parcel.obtain();

        requestParcel.writeInterfaceToken(IMEDIA_PLAYER);
        requestParcel.writeInt(flag);
        requestParcel.writeInt(rate);
        Parcel replyParcel = Parcel.obtain();
        videoView.invoke(requestParcel, replyParcel);

        requestParcel.recycle();
        replyParcel.recycle();
    }

    private Parcel getInfo(int flag) {
        Parcel requestParcel = Parcel.obtain();
        requestParcel.writeInterfaceToken(IMEDIA_PLAYER);
        requestParcel.writeInt(flag);
        Parcel replyParcel = Parcel.obtain();
        videoView.invoke(requestParcel, replyParcel);
        replyParcel.setDataPosition(0);
        requestParcel.recycle();
        return replyParcel;
    }

    protected void subTime(int value) {
        controlModel.setSelectedTime(value);
        Parcel requestParcel = Parcel.obtain();
        requestParcel.writeInterfaceToken(IMEDIA_PLAYER);

        requestParcel.writeInt(HiMediaPlayerInvoke.CMD_SET_SUB_TIME_SYNC);
        requestParcel.writeInt(0);
        requestParcel.writeInt(0);
        requestParcel.writeInt(value);
        Parcel replyParcel = Parcel.obtain();
        videoView.invoke(requestParcel, replyParcel);

        requestParcel.recycle();
        replyParcel.recycle();
    }

    protected int getSubEncode(int flag) {
        Parcel replyParcel = getInfo(flag);

        replyParcel.readInt();
        int ret = replyParcel.readInt();
        replyParcel.recycle();
        return ret;
    }

    public static int getFormatAdption() {
        return playerController.getFormatAdaption();
    }

    public static int getMvcType() {
        return playerController.getMvcType();
    }

    public static int get3dModeAdpate() {
        return playerController.getmVC3DAdapte();
    }


    /**************
     * dobly atmos
     ***************/

    private ImageView ivDolbyAtmos;
    /**
     * Dolby Notification
     */
    public static final String SETTING_DOLBY_ONOFF = "setting_dolby_onoff";
    private final static int MSG_WHAT_DOLBY_CHANGE = 223;
    private Handler dolbyOnOffHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_DOLBY_CHANGE:
                    dolbyNotificationOnOff();
                    break;
            }
        }
    };

    private PlayerProviderObserver observer = new PlayerProviderObserver(dolbyOnOffHandler, MSG_WHAT_DOLBY_CHANGE);

    private void dolbyNotificationOnOff() {
        showAtmosIcon(videoView.getDolbyAtmos());
        LogTool.d("Dolby Notification OnOff : " + getDolbyNotificationOnOffStatus());
    }

    /**
     * 1:dolby开关都打开 ，2：Notifition打开，Atmos关闭，0：都关闭
     *
     * @return
     */
    private int getDolbyNotificationOnOffStatus() {
        return Settings.System.getInt(getContentResolver(), SETTING_DOLBY_ONOFF, -1);
    }

    private void registerObserver() {
        String brandName = SystemProperties.get(VersionControl.PRODUCT_BRAND);
        if (brandName.startsWith(VersionControl.BRAND_HIDPT) && (Build.VERSION_CODES.P > VERSION.SDK_INT)) {
            getContentResolver().registerContentObserver(Settings.System.getUriFor(SETTING_DOLBY_ONOFF), true, observer);
        }
    }

    private void unregisterObserver() {
        String brandName = SystemProperties.get(VersionControl.PRODUCT_BRAND);
        if (brandName.startsWith(VersionControl.BRAND_HIDPT) && (Build.VERSION_CODES.P > VERSION.SDK_INT)) {
            getContentResolver().unregisterContentObserver(observer);
        }
    }

    private void showAtmosIcon(DoblyResult result) {
        if (ivDolbyAtmos == null) {
            return;
        }
        if (result.getError_flag() != 0 || (result.getAudio_flag() == 0 && result.getAtmos_flag() == 0)) {
            ivDolbyAtmos.setVisibility(View.INVISIBLE);
        } else {
            int status = getDolbyNotificationOnOffStatus();
            switch (status) {
                case 0: //都 关闭
                    ivDolbyAtmos.setVisibility(View.INVISIBLE);
                    break;
                case 1: //dolby开关都打开
                    if (result.getAudio_flag() == 1) {
                        dolbyImagePar(screenHeight / 6, screenHeight / 6);
                        ivDolbyAtmos.setImageResource(R.drawable.dolby_audio);
                        ivDolbyAtmos.setVisibility(View.VISIBLE);
                    } else if (result.getAtmos_flag() == 1) {
                        dolbyImagePar(screenHeight / 6, screenHeight / 6);
                        ivDolbyAtmos.setImageResource(R.drawable.dolby_atmos);
                        ivDolbyAtmos.setVisibility(View.VISIBLE);
                    } else {
                        ivDolbyAtmos.setVisibility(View.INVISIBLE);
                    }
                    break;
                case 2://2：Notifition打开，Atmos关闭  dolby audio 显示audio 图标 ， Atmos显示Notification图标
                    if (result.getAtmos_flag() == 1) {
                        dolbyImagePar(screenWidth / 8, screenHeight / 8);
                        ivDolbyAtmos.setImageResource(R.drawable.dolby_atmos_notification);
                        ivDolbyAtmos.setVisibility(View.VISIBLE);
                    } else if (result.getAudio_flag() == 1) {
                        dolbyImagePar(screenHeight / 6, screenHeight / 6);
                        ivDolbyAtmos.setImageResource(R.drawable.dolby_audio);
                        ivDolbyAtmos.setVisibility(View.VISIBLE);
                    } else {
                        ivDolbyAtmos.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }
    }

    private void dolbyImagePar(int width, int height) {
        if (ivDolbyAtmos == null) {
            return;
        }

        ViewGroup.LayoutParams params = ivDolbyAtmos.getLayoutParams();
        params.width = width;
        params.height = height;
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
            marginLayoutParams.setMargins(0, 0, screenWidth / 10, screenHeight / 10);
        }
        ivDolbyAtmos.setLayoutParams(params);
    }
}
