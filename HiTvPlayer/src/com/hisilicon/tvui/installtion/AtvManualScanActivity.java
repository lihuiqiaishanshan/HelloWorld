package com.hisilicon.tvui.installtion;

import java.text.DecimalFormat;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dolphin.dtv.network.LocalRFChannelDot;
import com.hisilicon.dtv.channel.AnalogChannel;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.RFNetwork;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.CheckPassWordDialog;
import com.hisilicon.tvui.view.CheckPassWordDialog.CheckPassWordDialogInterface;

/**
 * AtvManualScanActivity
 *
 * @author wangchuanjian
 *
 */
public class AtvManualScanActivity extends BaseActivity implements OnKeyListener,
        OnFocusChangeListener, OnClickListener
{
    protected static final String TAG = "AtvManualScanActivity";
    // image of left
    private ImageView manualChannelLeftImg ,manualLeftImg, colorSystemLeftImg , soundSystemLeftImg;
    // image of right
    private ImageView manualChannelRightImg ,manualRightImg, colorSystemRightImg , soundSystemRightImg;
    // text of channel
    private TextView channelTxt = null;
    // text of frequency
    private TextView freqTxt = null;
    // seekBar of manualScan
    private SeekBar manualSeekbar;
    // button of exit
    private Button manualExitBtn;
    // layout of manualScan
    private RelativeLayout manuaLayout;

    private TextView mFreqBand = null;
    // -1 mean unknow 0 mean left 1 mean rigth
    private Network rfNetwork = null;

    private ScanType mScanType = null;

    private TextView curColorSystemTxt , curSoundSystemTxt;

    private String[] mAudioSystem;

    private int[] mAudioSystemValue = {halApi.EnumAtvAudsys.AUDSYS_BG,
            halApi.EnumAtvAudsys.AUDSYS_I, halApi.EnumAtvAudsys.AUDSYS_DK,
            halApi.EnumAtvAudsys.AUDSYS_M, halApi.EnumAtvAudsys.AUDSYS_L};

    private String[] mColorSystem;

    private int[] mColorSystemValue = {halApi.EnumAtvClrsys.CLRSYS_AUTO ,
            halApi.EnumAtvClrsys.CLRSYS_PAL, halApi.EnumAtvClrsys.CLRSYS_NTSC,
            halApi.EnumAtvClrsys.CLRSYS_SECAM, halApi.EnumAtvClrsys.CLRSYS_PAL_M,
            halApi.EnumAtvClrsys.CLRSYS_PAL_N};

    private int curColorSystemIndex = 0;

    private int curAudioSystemIndex = 0;

    private AnalogChannel curChannel = null;

    private Dialog mCheckPassWordDialog = null;

    private CheckPassWordDialog.Builder mCheckPWBuilder = null;

    private boolean chnChange = false;
    /**
     * listener of channel scan
     */
    IDTVListener gScanListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            LogTool.d(LogTool.MSCAN, "gScanListener messageID = " + messageID);
            switch (messageID)
            {
            case DTVMessage.HI_ATV_EVT_SCAN_BEGIN:
            {
               /* int percent = 0;
                manualSeekbar.setMax(100);
                manualSeekbar.setProgress(param1);
                freqTxt.setText(percent + "%");*/
                break;
            }
            case DTVMessage.HI_ATV_EVT_SCAN_FINISH:
            {
                break;
            }
            case DTVMessage.HI_ATV_EVT_SCAN_LOCK:
            {
                AnalogChannel renewChan = mAtvChannelManager.getDefaultOpenChannel();
                if(renewChan != null){
                    int audioSystem = renewChan.getAudioSystem();
                    int colorSystem = renewChan.getColorSystem();
                    int audioSystemIndex = getMatchedAudio(audioSystem);
                    int colorSystemIndex = getMatchedColor(colorSystem);
                    curSoundSystemTxt.setText(mAudioSystem[audioSystemIndex]);
                    curColorSystemTxt.setText(mColorSystem[colorSystemIndex]);
                    channelTxt.setText("" + renewChan.getChannelNo());
                }
                break;
            }
            case DTVMessage.HI_ATV_EVT_SCAN_PROGRESS:
            {
                if(chnChange){
                    return;
                }
                manualSeekbar.setProgress(param1);
                DecimalFormat df = new DecimalFormat("#.000");
                float f = ((float) param2) / 1000;
                freqTxt.setText(getString(R.string.all_scan_freq_rate,df.format(f)));
                showFreqBand(param2);
                break;
            }
            default:
                break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LogTool.i(LogTool.MCHANNEL, "===== onCreate =====");
        setContentView(R.layout.install_atv_manual_scan_activity);
        mScanType = new ScanType();
        mScanType.setBaseType(ScanType.EnBaseScanType.ATV_MANUAL);
        List<Network>  tmpLstCNetwork = mNetworkManager.getNetworks(EnNetworkType.RF);
        if (tmpLstCNetwork.size() != 0)
        {
            rfNetwork = tmpLstCNetwork.get(0);
        }
        initView();
        initData();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        LogTool.i(LogTool.MCHANNEL, "===== onResume =====");
        subAtvScribeEvent();
        showContent(false);
        boolean bNeedPassword = CommonValue.MENU_LOCK_OPEN == mDtvConfig.getInt(CommonValue.MENU_LOCK, CommonValue.MENU_LOCK_CLOSE);
        // If the password lock, then display the password input box
        if ((bNeedPassword) && (!super.isFinishing()))
        {
            if (null == mCheckPassWordDialog)
            {
                if (mCheckPWBuilder == null)
                {
                    mCheckPWBuilder = new CheckPassWordDialog.Builder(this, R.style.DIM_STYLE);
                    mCheckPWBuilder.setOnKeyListener(this);
                }
                mCheckPassWordDialog = mCheckPWBuilder.create();
                mCheckPWBuilder.setPasswordTitle(getString(R.string.play_password_menu_lock));
                mCheckPassWordDialog.setCanceledOnTouchOutside(false);
                mCheckPWBuilder.setCheckPassWordsListener(new CheckPassWordDialogInterface()
                {
                    @Override
                    public void onCheck(int which, String passWord)
                    {
                        if (which == CheckPassWordDialog.CheckPassWordDialogInterface.PASSWORD_RIGHT)
                        {
                            showContent(true);
                        }
                    }
                });
                mCheckPassWordDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        if ((mCheckPWBuilder != null) && mCheckPWBuilder.getPasswordisRight())
                        {
                            showContent(true);
                            LogTool.v(LogTool.MSCAN, "mCheckPassWordDialog right");
                        }
                        else
                        {
                            finish();
                        }
                    }
                });
            }
            if (!mCheckPassWordDialog.isShowing())
            {
                mCheckPassWordDialog.show();
            }
        }
        else
        {
            showContent(true);
        }
    }

    /**
     * initView widget
     */
    public void initView()
    {
        mAudioSystem = getResources().getStringArray(R.array.atv_audiosystem_values);
        mColorSystem = getResources().getStringArray(R.array.atv_colorsystem_values);
        channelTxt = (TextView) findViewById(R.id.channel_value_txt);
        mFreqBand = (TextView)findViewById(R.id.band_value_txt);

        manualChannelLeftImg = (ImageView) findViewById(R.id.manual_channel_left_img);
        manualChannelRightImg = (ImageView) findViewById(R.id.manual_channel_right_img);
        manualLeftImg = (ImageView) findViewById(R.id.manual_left_img);
        manualRightImg = (ImageView) findViewById(R.id.manual_right_img);
        colorSystemLeftImg = (ImageView) findViewById(R.id.manual_colorsystem_left_img);
        colorSystemRightImg = (ImageView) findViewById(R.id.manual_colorsystem_right_img);
        soundSystemLeftImg = (ImageView) findViewById(R.id.manual_soundsystem_left_img);
        soundSystemRightImg = (ImageView) findViewById(R.id.manual_soundsystem_right_img);

        manualChannelLeftImg.setOnClickListener(this);
        manualChannelRightImg.setOnClickListener(this);
        manualLeftImg.setOnClickListener(this);
        manualRightImg.setOnClickListener(this);
        colorSystemLeftImg.setOnClickListener(this);
        colorSystemRightImg.setOnClickListener(this);
        soundSystemLeftImg.setOnClickListener(this);
        soundSystemRightImg.setOnClickListener(this);

        manualSeekbar = (SeekBar) findViewById(R.id.manual_seekbar);
        //manualSeekbar.setOnKeyListener(this);
        //manualSeekbar.setOnFocusChangeListener(this);
        manualExitBtn = (Button) findViewById(R.id.manual_exit);
        manualExitBtn.setOnFocusChangeListener(this);
        manualExitBtn.setOnClickListener(this);
        manuaLayout = (RelativeLayout) findViewById(R.id.manual_layout);
        manuaLayout.setGravity(1);
        manualLeftImg.setVisibility(View.VISIBLE);
        manualRightImg.setVisibility(View.VISIBLE);

        curColorSystemTxt = (TextView) findViewById(R.id.manual_colorsystem_value_txt);
        curSoundSystemTxt = (TextView) findViewById(R.id.manual_soundsystem_value_txt);
        LinearLayout manual_Channel_lay = (LinearLayout) findViewById(R.id.manual_channel_lay);
        LinearLayout manual_Soundsystem_lay = (LinearLayout) findViewById(R.id.manual_soundsystem_lay);
        LinearLayout manual_Colorsystem_lay = (LinearLayout) findViewById(R.id.manual_colorsystem_lay);
        LinearLayout manual_Seekbar_lay = (LinearLayout) findViewById(R.id.manual_seekbar_lay);

        manual_Channel_lay.setOnKeyListener(this);
        manual_Channel_lay.setOnFocusChangeListener(this);


        manual_Soundsystem_lay.setOnKeyListener(this);
        manual_Soundsystem_lay.setOnFocusChangeListener(this);

        manual_Colorsystem_lay.setOnKeyListener(this);
        manual_Colorsystem_lay.setOnFocusChangeListener(this);

        manual_Seekbar_lay.setOnKeyListener(this);
        manual_Seekbar_lay.setOnFocusChangeListener(this);
        manualSeekbar.setEnabled(false);
        manualSeekbar.setFocusable(false);
    }


    private void initData()
    {
        curChannel = mAtvChannelManager.getDefaultOpenChannel();
        int mMaxFreq = ((RFNetwork)rfNetwork).getMaxTuneFreq();
        int mMinFreq = ((RFNetwork)rfNetwork).getMinTuneFreq();
        int curFreqency = 0;
        freqTxt = (TextView) findViewById(R.id.freq_value_txt);
        DecimalFormat df = new DecimalFormat("#.000");

        if ((null != curChannel) && (null != curChannel.getBelongMultiplexe()))
        {
            curFreqency = curChannel.getBelongMultiplexe().getFrequency();
        }
        else
        {
            if (null == curChannel)
            {
                LocalRFChannelDot rfMultiplex = (LocalRFChannelDot)rfNetwork.createTmpMultiplex();
                curChannel = mAtvChannelManager.createChannel(rfMultiplex);
            }
            curFreqency = mMinFreq;
        }

        channelTxt.setText("" + curChannel.getChannelNo());

        float f = ((float)curFreqency) / 1000;
        freqTxt.setText(getString(R.string.all_scan_freq_rate,df.format(f)));
        showFreqBand(curFreqency);
        int percent = curFreqency - mMinFreq;
        percent *= 100;
        percent /= (mMaxFreq - mMinFreq);
        manualSeekbar.setMax(100);
        manualSeekbar.setProgress(percent);

        int audioSystem = curChannel.getAudioSystem();
        int colorSystem = curChannel.getColorSystem();

        curColorSystemIndex = getMatchedColor(colorSystem);
        curAudioSystemIndex = getMatchedAudio(audioSystem);

        curSoundSystemTxt.setText(mAudioSystem[curAudioSystemIndex]);
        curColorSystemTxt.setText(mColorSystem[curColorSystemIndex]);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent)
    {
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_BACK:
            finish();
            break;
        case KeyValue.DTV_KEYVALUE_HOME:
        case KeyValue.DTV_KEYVALUE_SOURCE:
            return true;
        default:
            break;
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    protected void onPause()
    {
        LogTool.i(LogTool.MCHANNEL, "===== onPause =====");
        unSubAtvScribeEvent();
        rfNetwork.stopScan(true);
        super.onPause();
        finish();
    }

    @Override
    public boolean onKey(View v, int keycode, KeyEvent event)
    {
        if(event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (keycode)
            {
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
                switch(v.getId())
                {
                case R.id.manual_channel_lay:
                    rfNetwork.stopScan(true);
                    chnChange = true;
                    changeNextChannel(false);
                    initData();
                    break;
                case R.id.manual_seekbar_lay:
                    chnChange = false;
                    mScanType.setAtvScanDirection(false);
                    rfNetwork.startScan(mScanType);
                    break;
                case R.id.manual_colorsystem_lay:
                    curColorSystemIndex -- ;
                    if(curColorSystemIndex < 0)
                    {
                        curColorSystemIndex = mColorSystemValue.length -1;
                    }
                    curColorSystemTxt.setText(mColorSystem[curColorSystemIndex]);
                    curChannel.setColorSystem(mColorSystemValue[curColorSystemIndex]);
                    break;
                case R.id.manual_soundsystem_lay:
                    curAudioSystemIndex -- ;
                    if(curAudioSystemIndex < 0)
                    {
                        curAudioSystemIndex = mAudioSystemValue.length -1;
                    }
                    curSoundSystemTxt.setText(mAudioSystem[curAudioSystemIndex]);
                    curChannel.setAudioSystem(mAudioSystemValue[curAudioSystemIndex]);
                    break;
                }
                break;
            case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
                switch(v.getId())
                {
                case R.id.manual_channel_lay:
                    rfNetwork.stopScan(true);
                    chnChange = true;
                    changeNextChannel(true);
                    initData();
                    break;
                case R.id.manual_seekbar_lay:
                    chnChange = false;
                    mScanType.setAtvScanDirection(true);
                    rfNetwork.startScan(mScanType);
                    return true;
                case R.id.manual_colorsystem_lay:
                    curColorSystemIndex ++ ;
                    if(curColorSystemIndex == mColorSystemValue.length)
                    {
                        curColorSystemIndex = 0;
                    }
                    curColorSystemTxt.setText(mColorSystem[curColorSystemIndex]);
                    curChannel.setColorSystem(mColorSystemValue[curColorSystemIndex]);
                    break;
                case R.id.manual_soundsystem_lay:
                    curAudioSystemIndex ++ ;
                    if(curAudioSystemIndex == mAudioSystemValue.length)
                    {
                        curAudioSystemIndex = 0;
                    }
                    curSoundSystemTxt.setText(mAudioSystem[curAudioSystemIndex]);
                    curChannel.setAudioSystem(mAudioSystemValue[curAudioSystemIndex]);
                    break;
                }
                break;
            default:
                break;
            }
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        switch (v.getId())
        {
        case R.id.manual_channel_lay:
            if (hasFocus)
            {
                manualChannelLeftImg.setBackgroundResource(R.drawable.selector_arrow_left_blue);
                manualChannelRightImg.setBackgroundResource(R.drawable.selector_arrow_right_blue);
            }
            else
            {
                manualChannelLeftImg.setBackgroundResource(R.drawable.selector_arrow_left);
                manualChannelRightImg.setBackgroundResource(R.drawable.selector_arrow_right);
            }
            break;
        case R.id.manual_seekbar_lay:
            if (hasFocus)
            {
                manualLeftImg.setBackgroundResource(R.drawable.selector_arrow_left_blue);
                manualRightImg.setBackgroundResource(R.drawable.selector_arrow_right_blue);
            }
            else
            {
                manualLeftImg.setBackgroundResource(R.drawable.selector_arrow_left);
                manualRightImg.setBackgroundResource(R.drawable.selector_arrow_right);
            }
            break;
        case R.id.manual_colorsystem_lay:
            if (hasFocus)
            {
                colorSystemLeftImg.setBackgroundResource(R.drawable.selector_arrow_left_blue);
                colorSystemRightImg.setBackgroundResource(R.drawable.selector_arrow_right_blue);
            }
            else
            {
                colorSystemLeftImg.setBackgroundResource(R.drawable.selector_arrow_left);
                colorSystemRightImg.setBackgroundResource(R.drawable.selector_arrow_right);
            }
            break;
        case R.id.manual_soundsystem_lay:
            if (hasFocus)
            {
                soundSystemLeftImg.setBackgroundResource(R.drawable.selector_arrow_left_blue);
                soundSystemRightImg.setBackgroundResource(R.drawable.selector_arrow_right_blue);
            }
            else
            {
                soundSystemLeftImg.setBackgroundResource(R.drawable.selector_arrow_left);
                soundSystemRightImg.setBackgroundResource(R.drawable.selector_arrow_right);
            }
            break;
        case R.id.manual_exit:
            if (hasFocus)
            {
                manualExitBtn.setBackgroundResource(R.drawable.btn_focus);
            }
            else
            {
                manualExitBtn.setBackgroundResource(R.drawable.btn_unfocus);
            }

            break;

        default:
            break;
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.manual_channel_left_img:
            changeNextChannel(false);
            initData();
            break;
        case R.id.manual_channel_right_img:
            changeNextChannel(true);
            initData();
            break;
        case R.id.manual_colorsystem_left_img:
            curColorSystemIndex -- ;
            if(curColorSystemIndex < 0)
            {
                curColorSystemIndex = mColorSystemValue.length -1;
            }
            curColorSystemTxt.setText(mColorSystem[curColorSystemIndex]);
            curChannel.setColorSystem(mColorSystemValue[curColorSystemIndex]);
            break;
        case R.id.manual_colorsystem_right_img:
            curColorSystemIndex ++ ;
            if(curColorSystemIndex == mColorSystemValue.length)
            {
                curColorSystemIndex = 0;
            }
            curColorSystemTxt.setText(mColorSystem[curColorSystemIndex]);
            curChannel.setColorSystem(mColorSystemValue[curColorSystemIndex]);
            break;
        case R.id.manual_soundsystem_left_img:
            curAudioSystemIndex -- ;
            if(curAudioSystemIndex < 0)
            {
                curAudioSystemIndex = mAudioSystemValue.length -1;
            }
            curSoundSystemTxt.setText(mAudioSystem[curAudioSystemIndex]);
            curChannel.setAudioSystem(mAudioSystemValue[curAudioSystemIndex]);
            break;
        case R.id.manual_soundsystem_right_img:
            curAudioSystemIndex ++ ;
            if(curAudioSystemIndex == mAudioSystemValue.length)
            {
                curAudioSystemIndex = 0;
            }
            curSoundSystemTxt.setText(mAudioSystem[curAudioSystemIndex]);
            curChannel.setAudioSystem(mAudioSystemValue[curAudioSystemIndex]);
            break;
        case R.id.manual_left_img:
            mScanType.setAtvScanDirection(false);
            rfNetwork.startScan(mScanType);
            break;
        case R.id.manual_right_img:
            mScanType.setAtvScanDirection(true);
            rfNetwork.startScan(mScanType);
            break;
        case R.id.manual_exit:
            rfNetwork.stopScan(true);
            finish();
            break;

        default:
            break;
        }
    }


    protected void showFreqBand(int freqvalue)
    {
        if (freqvalue <= 120000)
        {
            mFreqBand.setText("VHF-L");
        }
        else if (freqvalue <= 470000)
        {
            mFreqBand.setText("VHF-H");
        }
        else
        {
            mFreqBand.setText("UHF");
        }
    }

    private void subAtvScribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_BEGIN, gScanListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_PROGRESS, gScanListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_LOCK, gScanListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_FINISH, gScanListener, 0);
        }
    }

    private void unSubAtvScribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_BEGIN, gScanListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_PROGRESS, gScanListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_LOCK, gScanListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_FINISH, gScanListener);
        }
    }

    public Channel getNextChannel(ChannelList tmpCurList, Channel tmpCurChn, boolean bAdd)
    {
        int nNexCalcValue = bAdd ? 1 : -1;
        int tmpChnCount = tmpCurList.getChannelCount();
        int tmpIndex = tmpCurList.getPosByChannelID(tmpCurChn.getChannelID());
        Channel tmpChn = null;
        int programLockTag = mDtvConfig.getInt(CommonValue.PROGRAM_LOCK, CommonValue.PROGRAM_LOCK_CLOSE);
        for (int i = 0; i < tmpChnCount; i++)
        {
            tmpIndex = ((tmpIndex + tmpChnCount + nNexCalcValue) % tmpChnCount);
            tmpChn = tmpCurList.getChannelByIndex(tmpIndex);
            if (tmpChn == null || (tmpChn != null && tmpChn.getTag(EnTagType.HIDE))
                    ||(CommonValue.PROGRAM_LOCK_OPEN == programLockTag && tmpChn.getTag(EnTagType.LOCK)))
            {
                continue;
            }
            else
            {
                break;
            }
        }
        return tmpChn;
    }

    public void changeNextChannel(boolean bAdd)
    {
        ChannelList tmpCurList = getCurrentList();
        Channel tmpCurChn = mAtvChannelManager.getDefaultOpenChannel();
        if (null == tmpCurList)
        {
            return;
        }

        if (null == tmpCurChn)
        {
            return;
        }

        Channel tmpChn = getNextChannel(tmpCurList, tmpCurChn, bAdd);
        mChnHistory.setCurrent(halApi.EnumSourceIndex.SOURCE_ATV, tmpCurList, tmpChn);
        mPlayer.changeChannel(tmpChn);
    }

    //获取当前源的频道列表
    private ChannelList getCurrentList()
    {
        ChannelList tmpCurList = null;
        tmpCurList = mChnHistory.getCurrentList(halApi.EnumSourceIndex.SOURCE_ATV);
        return tmpCurList;
    }

    private void showContent(boolean flag)
    {
        if(flag)
        {
            manuaLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            manuaLayout.setVisibility(View.GONE);
        }
    }

    private int getMatchedColor(int colorVal){
        int colorSystemIndex;
        switch(colorVal) {
            case halApi.EnumAtvClrsys.CLRSYS_AUTO:
                colorSystemIndex = 0;
                break;
            case halApi.EnumAtvClrsys.CLRSYS_PAL:
            case halApi.EnumAtvClrsys.CLRSYS_PAL_60:
            case halApi.EnumAtvClrsys.CLRSYS_PAL_NC:
                colorSystemIndex = 1;
                break;
            case halApi.EnumAtvClrsys.CLRSYS_NTSC:
            case halApi.EnumAtvClrsys.CLRSYS_NTSC443:
                colorSystemIndex = 2;
                break;
            case halApi.EnumAtvClrsys.CLRSYS_SECAM:
                colorSystemIndex = 3;
                break;
            case halApi.EnumAtvClrsys.CLRSYS_PAL_M:
                colorSystemIndex = 4;
                break;
            case halApi.EnumAtvClrsys.CLRSYS_PAL_N:
                colorSystemIndex = 5;
                break;
            default :
                colorSystemIndex = 1;
                break;
        }
        return colorSystemIndex;
    }


    private int getMatchedAudio(int audioVal){
        int audioSystemIndex;
        switch(audioVal) {
            case halApi.EnumAtvAudsys.AUDSYS_BG:
            case halApi.EnumAtvAudsys.AUDSYS_BG_A2:
            case halApi.EnumAtvAudsys.AUDSYS_BG_NICAM:
                audioSystemIndex = 0;
                break;
            case halApi.EnumAtvAudsys.AUDSYS_I:
                audioSystemIndex = 1;
                break;
            case halApi.EnumAtvAudsys.AUDSYS_DK:
            case halApi.EnumAtvAudsys.AUDSYS_DK1_A2:
            case halApi.EnumAtvAudsys.AUDSYS_DK2_A2:
            case halApi.EnumAtvAudsys.AUDSYS_DK3_A2:
            case halApi.EnumAtvAudsys.AUDSYS_DK_NICAM:
                audioSystemIndex = 2;
                break;
            case halApi.EnumAtvAudsys.AUDSYS_M:
            case halApi.EnumAtvAudsys.AUDSYS_M_A2:
            case halApi.EnumAtvAudsys.AUDSYS_M_BTSC:
            case halApi.EnumAtvAudsys.AUDSYS_M_EIA_J:
                audioSystemIndex = 3;
                break;
            case halApi.EnumAtvAudsys.AUDSYS_L:
                audioSystemIndex = 4;
                break;
            default:
                audioSystemIndex = 2;
                break;
        }
        return audioSystemIndex;
    }
}
