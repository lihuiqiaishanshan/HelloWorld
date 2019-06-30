package com.hisilicon.tvui.installtion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hisilicon.dtv.channel.AnalogChannel;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Multiplex;
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
 * FinetuneActivity
 *
 * @author wangchuanjian
 *
 */
public class FinetuneActivity extends BaseActivity implements OnKeyListener,
        OnFocusChangeListener, OnClickListener
{
    protected static final String TAG = "FinetuneActivity";
    private static final int TUNER_STEP = 50;//this value is same as middleware
    // seekBar of fineTune
    private SeekBar fineTuneSeekbar;
    private TextView curFreqTxt, curProgTxt, curBandTxt , curColorSystemTxt , curSoundSystemTxt;
    // image of fine left
    private ImageView fineLeftImg , colorSystemLeftImg , soundSystemLeftImg;
    // image of fine right
    private ImageView fineRightImg, colorSystemRightImg , soundSystemRightImg;
    // butTon of exit fineTune
    private Button exitFinetuneBtn;
    // current frequency number
    private int mCurrentFreq = 0;
    // max value of frequency
    private int mMaxFreq = 0;
    // min value of frequency
    private int mMinFreq = 0;

    private Network rfNetwork = null;

    private AnalogChannel curChannel = null;

    private Multiplex multiplex = null;

    private List<Multiplex> lstMultiplex = new ArrayList<Multiplex>();

    private String[] mAudioSystem;

    private int[] mAudioSystemValue = {0 , 3 , 4 , 10};

    private String[] mColorSystem;

    private int[] mColorSystemValue = {0 , 1 , 2 , 3};

    private int curColorSystemIndex = 0;

    private int curAudioSystemIndex = 0;

    private CheckPassWordDialog.Builder mCheckPWBuilder = null;

    private Dialog mCheckPassWordDialog = null;

    private ScanType mScanType = null;
/*
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case ACTIVITY_FINISH:
                Log.d("ACTIVITY_FINISH", ACTIVITY_FINISH + "");
                finish();
                break;
            default:
                break;
            }
        }
    };
*/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LogTool.i(LogTool.MCHANNEL, "===== onCreate =====");
        setContentView(R.layout.fine_tune);
        initView();
        curChannel = mAtvChannelManager.getDefaultOpenChannel();
        if(null == curChannel)
        {
            finish();
            return;
        }
        mScanType = new ScanType();
        mScanType.setBaseType(ScanType.EnBaseScanType.ATV_FINE);
        List<Network>  tmpLstCNetwork = mNetworkManager.getNetworks(EnNetworkType.RF);
        if (tmpLstCNetwork.size() != 0)
        {
           rfNetwork = tmpLstCNetwork.get(0);
        }

        if (mChnHistory.getCurrentList(halApi.EnumSourceIndex.SOURCE_ATV).getChannelCount() == 0)
        {
            LogTool.d(LogTool.MINSTALL, "AvailProgCount is null");
            mCurrentFreq = mMinFreq;
        }

        multiplex = curChannel.getBelongMultiplexe();
        if (null == multiplex)
        {
            multiplex = rfNetwork.createTmpMultiplex();
            multiplex.setFrequency(mCurrentFreq);
        }
        else
        {
            mCurrentFreq = multiplex.getFrequency();
        }

        mMaxFreq = ((RFNetwork)rfNetwork).getMaxTuneFreq();
        mMinFreq = ((RFNetwork)rfNetwork).getMinTuneFreq();

        int mCurrentProgress = curChannel.getChannelNo();
        showCurFreq(mCurrentFreq);
        curProgTxt.setText("" + mCurrentProgress);

        fineTuneSeekbar.setMax(mMaxFreq - mMinFreq);
        fineTuneSeekbar.setProgress(mCurrentFreq - mMinFreq);

        if (mCurrentFreq <= 120000)
        {
            curBandTxt.setText("VHF-L");
        }
        else if (mCurrentFreq <= 470000)
        {
            curBandTxt.setText("VHF-H");
        }
        else
        {
            curBandTxt.setText("UHF");
        }

        int audioSystem = curChannel.getAudioSystem();
        int colorSystem = curChannel.getColorSystem();

        for(int i = 0 ;i < mAudioSystemValue.length ;i++)
        {
            if(audioSystem == mAudioSystemValue[i])
            {
                curAudioSystemIndex = i;
            }
        }
        for(int i = 0 ;i < mColorSystemValue.length ;i++)
        {
            if(colorSystem == mColorSystemValue[i])
            {
                curColorSystemIndex = i;
            }
        }
        curSoundSystemTxt.setText(mAudioSystem[curAudioSystemIndex]);
        curColorSystemTxt.setText(mColorSystem[curColorSystemIndex]);
        delay();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LogTool.i(LogTool.MCHANNEL, "===== onResume =====");
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
                    public void onCheck(int which, String passWord) {
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
        }else{
            showContent(true);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        LogTool.i(LogTool.MCHANNEL, "===== onPause =====");
        finish();
    }

    /**
     * The initialization of all views
     */
    private void initView()
    {
        mAudioSystem = getResources().getStringArray(R.array.atv_audiosystem_values);
        mColorSystem = getResources().getStringArray(R.array.atv_colorsystem_values);
        fineTuneSeekbar = (SeekBar) findViewById(R.id.finetune_seekbar);
        LinearLayout fine_Soundsystem_lay = (LinearLayout) findViewById(R.id.fine_soundsystem_lay);
        LinearLayout fine_Colorsystem_lay = (LinearLayout) findViewById(R.id.fine_colorsystem_lay);
        LinearLayout fine_Seekbar_lay = (LinearLayout) findViewById(R.id.fine_seekbar_lay);

        fine_Soundsystem_lay.setOnKeyListener(this);
        fine_Soundsystem_lay.setOnFocusChangeListener(this);

        fine_Colorsystem_lay.setOnKeyListener(this);
        fine_Colorsystem_lay.setOnFocusChangeListener(this);

        fine_Seekbar_lay.setOnKeyListener(this);
        fine_Seekbar_lay.setOnFocusChangeListener(this);

        curFreqTxt = (TextView) findViewById(R.id.freq_value_txt);
        curProgTxt = (TextView) findViewById(R.id.channel_value_txt);
        curBandTxt = (TextView) findViewById(R.id.band_value_txt);
        curColorSystemTxt = (TextView) findViewById(R.id.fine_colorsystem_value_txt);
        curSoundSystemTxt = (TextView) findViewById(R.id.fine_soundsystem_value_txt);

        exitFinetuneBtn = (Button) findViewById(R.id.fine_exit_btn);
        exitFinetuneBtn.setOnFocusChangeListener(this);
        exitFinetuneBtn.setOnClickListener(this);

        fineLeftImg = (ImageView) findViewById(R.id.fine_left_img);
        fineRightImg = (ImageView) findViewById(R.id.fine_right_img);
        fineLeftImg.setOnClickListener(this);
        fineRightImg.setOnClickListener(this);


        colorSystemLeftImg = (ImageView) findViewById(R.id.fine_colorsystem_left_img);
        colorSystemRightImg = (ImageView) findViewById(R.id.fine_colorsystem_right_img);

        soundSystemLeftImg = (ImageView) findViewById(R.id.fine_soundsystem_left_img);
        soundSystemRightImg = (ImageView) findViewById(R.id.fine_soundsystem_right_img);
        fineTuneSeekbar.setEnabled(false);
    }

    @Override
    public boolean onKeyDown(int arg0, KeyEvent arg1)
    {
        switch (arg0)
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
        return super.onKeyDown(arg0, arg1);
    }

    /**
     * show current frequency
     *
     * @param curfreq
     */
    private void showCurFreq(int curfreq)
    {
        DecimalFormat df = new DecimalFormat("#.000");
        float f = ((float) curfreq) / 1000;
        curFreqTxt.setText(getString(R.string.all_scan_freq_rate, df.format(f)));
    }
    // @Override
    // protected void onDestroy() {
    // HitvManager.getInstance().unregisterListener(TVMessage.HI_TV_EVT_SCAN_SCHED,
    // mOnChannelScanListener);
    // super.onDestroy();
    // }
    /**
     * if do nothing in 10s,activity finish.
     */
    public void delay()
    {
        //mHandler.removeMessages(ACTIVITY_FINISH);
        //mHandler.sendEmptyMessageDelayed(ACTIVITY_FINISH,
       //         Constant.DISPEAR_TIME_10s);
    }

    @Override
    public boolean onKey(View v, int keycode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (keycode)
            {
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
                switch(v.getId())
                {
                case R.id.fine_seekbar_lay:
                    setFineTune(false);
                    break;
                case R.id.fine_colorsystem_lay:
                    curColorSystemIndex -- ;
                    if(curColorSystemIndex < 0)
                    {
                        curColorSystemIndex = mColorSystemValue.length -1;
                    }
                    curColorSystemTxt.setText(mColorSystem[curColorSystemIndex]);
                    curChannel.setColorSystem(mColorSystemValue[curColorSystemIndex]);
                    break;
                case R.id.fine_soundsystem_lay:
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
                    case R.id.fine_seekbar_lay:
                        setFineTune(true);
                        break;
                    case R.id.fine_colorsystem_lay:
                        curColorSystemIndex ++ ;
                        if(curColorSystemIndex == mColorSystemValue.length)
                        {
                            curColorSystemIndex = 0;
                        }
                        curColorSystemTxt.setText(mColorSystem[curColorSystemIndex]);
                        curChannel.setColorSystem(mColorSystemValue[curColorSystemIndex]);
                        break;
                    case R.id.fine_soundsystem_lay:
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
        case R.id.fine_seekbar_lay:
            delay();
            if (hasFocus)
            {
                fineLeftImg.setBackgroundResource(R.drawable.selector_arrow_left_blue);
                fineRightImg.setBackgroundResource(R.drawable.selector_arrow_right_blue);
            }
            else
            {
                fineLeftImg.setBackgroundResource(R.drawable.selector_arrow_left);
                fineRightImg.setBackgroundResource(R.drawable.selector_arrow_right);
            }
            break;
        case R.id.fine_colorsystem_lay:
            delay();
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
        case R.id.fine_soundsystem_lay:
            delay();
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
        case R.id.fine_exit_btn:
            delay();
            if (hasFocus)
            {
                exitFinetuneBtn.setBackgroundResource(R.drawable.btn_focus);
            }
            else
            {
                exitFinetuneBtn.setBackgroundResource(R.drawable.btn_unfocus);
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
        case R.id.fine_exit_btn:
            finish();
            break;
        case R.id.fine_left_img:
            setFineTune(false);
            break;
        case R.id.fine_right_img:
            setFineTune(true);
            break;
        default:
            break;
        }
    }

    public boolean setFineTune(boolean direct)
    {
        if (direct)
        {
            if ((mCurrentFreq + TUNER_STEP) > mMaxFreq)
            {
                return true;
            }
            if (mCurrentFreq <= mMaxFreq)
            {
                lstMultiplex.clear();
                multiplex.setFrequency(TUNER_STEP);
            }
        }
        else
        {
            if ((mCurrentFreq - TUNER_STEP) < mMinFreq)
            {
                return true;
            }
            if (mCurrentFreq >= mMinFreq)
            {
                lstMultiplex.clear();
                multiplex.setFrequency(0 - TUNER_STEP);
            }
        }
        lstMultiplex.add(multiplex);
        rfNetwork.setScanMultiplexes(lstMultiplex);
        rfNetwork.startScan(mScanType);
        curChannel = mAtvChannelManager.getDefaultOpenChannel();
        mCurrentFreq = curChannel.getBelongMultiplexe().getFrequency();
        showCurFreq(mCurrentFreq);
        curChannel.enableAFT(false);
        fineTuneSeekbar.setProgress(mCurrentFreq - mMinFreq);
        return false;
    }

    private void showContent(boolean flag)
    {
        View view = findViewById(R.id.finetune_lay);
        view.setVisibility(flag?View.VISIBLE:View.GONE);
    }
}
