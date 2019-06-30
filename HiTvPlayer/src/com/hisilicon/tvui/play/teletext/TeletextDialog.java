package com.hisilicon.tvui.play.teletext;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.network.service.TeletextComponent;
import com.hisilicon.dtv.play.EnCMDCode;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.dtv.play.TeletextControl;
import com.hisilicon.dtv.pvrfileplay.PVRFilePlayer;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.Util;

public class TeletextDialog extends Dialog
{
    public static final int ttxDialogTypeAV = 1;
    public static final int ttxDialogTypePVR = 2;

    private DTV mDTV;

    private int mTtxDialogType;

    private Player mAvPlayer;
    private PVRFilePlayer mPvrPlayer;

    private TeletextControl mTeletextControl;

    private boolean mRestartSubtitle = false;
    private long mEnterTime = 0;
    private static final String DEFAULT_LAN = "eng";

    public TeletextDialog(Context context, int theme, int ttxDialogType)
    {
        super(context, theme);
        mTtxDialogType = ttxDialogType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        LogTool.d(LogTool.MPLAY, "TTX:onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addPrivateFlags(Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY);
        initDTV();

        if (null != mTeletextControl)
        {
            TeletextComponent mTeletextComponent = mTeletextControl.getCurrentTTX();
            if (null != mTeletextComponent)
            {
                //Subtitle and teletext use the same SurfaceView to display.
                //When quit teletext,we should restore the original display state for subtitle.
                if (ttxDialogTypeAV == mTtxDialogType)
                {
                    if (mAvPlayer.isSubtitleVisible())
                    {
                        mAvPlayer.showSubtitle(false);
                        mRestartSubtitle = true;
                    }
                }
                else if (ttxDialogTypePVR == mTtxDialogType)
                {
                    if (mPvrPlayer.isSubtitleVisible())
                    {
                        mPvrPlayer.showSubtitle(false);
                        mRestartSubtitle = true;
                    }
                }

                String primaryTtxLang = mDTV.getConfig().getString(CommonValue.SYS_PRIMARY_TTX_LANG_KEY, DEFAULT_LAN);
                mTeletextControl.setTTXLanguage(primaryTtxLang);

                mTeletextControl.showTTX(true);

                LogTool.d(LogTool.MPLAY, "showTTX");
            }
        }
    }

    @Override
    protected void onStart()
    {
        LogTool.d(LogTool.MPLAY, "TTX:onStart");
        super.onStart();
        mEnterTime = SystemClock.elapsedRealtime();
    }

    @Override
    protected void onStop()
    {
        LogTool.d(LogTool.MPLAY, "TTX:onStop");
        super.onStop();
    }

    private void initDTV()
    {
        LogTool.d(LogTool.MPLAY, "TTX:initDTV");
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);

        if (ttxDialogTypeAV == mTtxDialogType)
        {
            PlayerManager mPlayerManager = mDTV.getPlayerManager();
            mAvPlayer = mPlayerManager.getPlayers().get(0);
            mTeletextControl = mAvPlayer.getTeletextControl();
        }
        else if (ttxDialogTypePVR == mTtxDialogType)
        {
            mPvrPlayer = mDTV.getPVRFilePlayer();
            mTeletextControl = mPvrPlayer.getTeletextControl();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mTeletextControl.isTTXVisible()){
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        LogTool.d(LogTool.MPLAY, "TTX:KeyEvent.ACTION_DOWN:keyCode = " + keyCode + ".");

        int KEY_EVENT_INTERVAL = 700;
        if ((SystemClock.elapsedRealtime() - mEnterTime) < KEY_EVENT_INTERVAL)
        {
            LogTool.d(LogTool.MPLAY, "TTX:KeyEvent.ACTION_DOWN:keyCode[bak/ttx] return");

            if ((KeyValue.DTV_KEYVALUE_BACK != keyCode) && (KeyValue.DTV_KEYVALUE_TXT != keyCode))
            {
                return true;
            }
        }

        switch (keyCode)
        {
            case KeyValue.DTV_KEYVALUE_BACK:
            case KeyValue.DTV_KEYVALUE_TXT: {
                LogTool.d(LogTool.MPLAY, "DTV_KEYVALUE_TXT: showTTX(false)");
                restartSubtitle();
                break;
            }
            case KeyValue.DTV_KEYVALUE_HOME: {
                LogTool.d(LogTool.MPLAY, "DTV_KEYVALUE_HOME: showTTX(false)");
                restartSubtitle();
                return false;
            }
        case KeyValue.DTV_KEYVALUE_0:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_0);
            break;
        }
        case KeyValue.DTV_KEYVALUE_1:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_1);
            break;
        }
        case KeyValue.DTV_KEYVALUE_2:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_2);
            break;
        }
        case KeyValue.DTV_KEYVALUE_3:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_3);
            break;
        }
        case KeyValue.DTV_KEYVALUE_4:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_4);
            break;
        }
        case KeyValue.DTV_KEYVALUE_5:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_5);
            break;
        }
        case KeyValue.DTV_KEYVALUE_6:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_6);
            break;
        }
        case KeyValue.DTV_KEYVALUE_7:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_7);
            break;
        }
        case KeyValue.DTV_KEYVALUE_8:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_8);
            break;
        }
        case KeyValue.DTV_KEYVALUE_9:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_9);
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_UP:
        case KeyValue.DTV_KEYVALUE_PAGEUP:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_PREVIOUS_PAGE);
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_DOWN:
        case KeyValue.DTV_KEYVALUE_PAGEDOWN:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_NEXT_PAGE);
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_PREVIOUS_SUBPAGE);
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_NEXT_SUBPAGE);
            break;
        }
        case KeyValue.DTV_KEYVALUE_RED:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_RED);
            break;
        }
        case KeyValue.DTV_KEYVALUE_GREEN:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_GREEN);
            break;
        }
        case KeyValue.DTV_KEYVALUE_YELLOW:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_YELLOW);
            return true;
        }
        case KeyValue.DTV_KEYVALUE_BLUE:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_CYAN);
            return true;
        }
        case KeyValue.DTV_KEYVALUE_MEDIA_PLAY_PAUSE:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_INDEX);
            break;
        }
        case KeyValue.DTV_KEYVALUE_CC:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_REVEAL);
            break;
        }
        case KeyValue.DTV_KEYVALUE_HOLD:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_HOLD);
            return true;
        }
        case KeyValue.DTV_KEYVALUE_MEDIA_REWIND:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_MIX);
            break;
        }
        case KeyValue.DTV_KEYVALUE_INFOBAR:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_UPDATE);
            break;
        }
        case KeyValue.DTV_KEYVALUE_SIZE: {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_ZOOM);
            break;
        }
        case KeyValue.DTV_KEYVALUE_MEDIA_STOP:
        {
            mTeletextControl.setCommand(EnCMDCode.TTX_KEY_SUBPAGE);
            break;
        }
        default:
        {
            break;
        }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void restartSubtitle() {
        mTeletextControl.showTTX(false);
        if (mRestartSubtitle) {
            if (ttxDialogTypeAV == mTtxDialogType) {
                mAvPlayer.showSubtitle(true);
            } else if (ttxDialogTypePVR == mTtxDialogType) {
                mPvrPlayer.showSubtitle(true);
            }
            mRestartSubtitle = false;
        }
        this.dismiss();
    }
}
