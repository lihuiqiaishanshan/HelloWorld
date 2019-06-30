package com.hisilicon.tvui.play.audio;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.WindowManager.LayoutParams;
import com.hisilicon.dtv.network.service.AudioComponent;
import com.hisilicon.dtv.play.EnAudioTrackMode;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.pvrfileplay.PVRFilePlayer;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.Util;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;

public class AudioSelectorDialog extends Dialog implements  OnClickListener
{
    private static final String TAG = "AudioSelectorDialog";

    private Player mAvPlayer;
    private PVRFilePlayer mPvrPlayer;

    private List<AudioComponent> mAudiolList = null;

    private EnAudioTrackMode mEnAudioTrackMode;

    private Context mContext;

    private static final int DIALOG_CLOSE = 0;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg){
            if(msg.what == DIALOG_CLOSE)
            {
                AudioSelectorDialog.this.dismiss();
            }
        }
    };

    public AudioSelectorDialog(Context context, List<AudioComponent> audiolList, Player avPlayer)
    {
        super(context, R.style.Translucent_NoTitle);
        mContext = context;
        mAudiolList = audiolList;
        mAvPlayer = avPlayer;
        mPvrPlayer = null;
    }

    public AudioSelectorDialog(Context context)
    {
        super(context,R.style.Translucent_NoTitle);
        mContext = context;
    }

    public AudioSelectorDialog(Context context, List<AudioComponent> audiolList, PVRFilePlayer pvrPlayer)
    {
        super(context, R.style.Translucent_NoTitle);
        mContext = context;
        mAudiolList = audiolList;
        mAvPlayer = null;
        mPvrPlayer = pvrPlayer;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LayoutParams params = this.getWindow().getAttributes();
        params.y = (int) mContext.getResources().getDimension(R.dimen.dimen_390px);
        this.getWindow().setAttributes(params);
        initView();
    }

    protected void onStart()
    {
        super.onStart();
    }

    protected void onStop()
    {
        super.onStop();
    }

    private EnAudioTrackMode getAudioTrackMode()
    {
        if (null != mAvPlayer)
        {
            return mAvPlayer.getAudioTrackMode();
        }

        if (null != mPvrPlayer)
        {
            return mPvrPlayer.getAudioTrackMode();
        }

        return EnAudioTrackMode.AUDIO_TRACK_STEREO;
    }

    private void setAudioTrackMode(EnAudioTrackMode enTrackMode)
    {
        if (null != mAvPlayer)
        {
            mAvPlayer.setAudioTrackMode(enTrackMode);
        }

        if (null != mPvrPlayer)
        {
            mPvrPlayer.setAudioTrackMode(enTrackMode);
        }

        return;
    }

    private void initView()
    {
        AudioSelectorView view  = new AudioSelectorView(this, mContext, "Audio Switch");
        setContentView(view);
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event)
    {
        mHandler.removeMessages(DIALOG_CLOSE);
        mHandler.sendEmptyMessageDelayed(DIALOG_CLOSE, Util.DISPEAR_TIME);
        return super.onKeyDown(keyCode, event);
    }

    private EnAudioTrackMode getPreTrackMode(EnAudioTrackMode curTrackMode)
    {
        switch (curTrackMode)
        {
            case AUDIO_TRACK_STEREO:
            {
                return EnAudioTrackMode.AUDIO_TRACK_DOUBLE_RIGHT;
            }
            case AUDIO_TRACK_DOUBLE_LEFT:
            case AUDIO_TRACK_ONLY_LEFT:
            {
                return EnAudioTrackMode.AUDIO_TRACK_STEREO;
            }
            case AUDIO_TRACK_DOUBLE_RIGHT:
            case AUDIO_TRACK_ONLY_RIGHT:
            {
                return EnAudioTrackMode.AUDIO_TRACK_DOUBLE_LEFT;
            }
            default:
            {
                return EnAudioTrackMode.AUDIO_TRACK_STEREO;
            }
        }
    }

    private EnAudioTrackMode getNextTrackMode(EnAudioTrackMode curTrackMode)
    {
        switch (curTrackMode)
        {
            case AUDIO_TRACK_STEREO:
            {
                return EnAudioTrackMode.AUDIO_TRACK_DOUBLE_LEFT;
            }
            case AUDIO_TRACK_DOUBLE_LEFT:
            case AUDIO_TRACK_ONLY_LEFT:
            {
                return EnAudioTrackMode.AUDIO_TRACK_DOUBLE_RIGHT;
            }
            case AUDIO_TRACK_DOUBLE_RIGHT:
            case AUDIO_TRACK_ONLY_RIGHT:
            {
                return EnAudioTrackMode.AUDIO_TRACK_STEREO;
            }
            default:
            {
                return EnAudioTrackMode.AUDIO_TRACK_STEREO;
            }
        }
    }

    @Override
    public void onClick(View arg0)
    {

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        if(hasWindowFocus)
        {
            mHandler.removeMessages(DIALOG_CLOSE);
            mHandler.sendEmptyMessageDelayed(DIALOG_CLOSE, Util.DISPEAR_TIME);
        }
        else
        {
            mHandler.removeMessages(DIALOG_CLOSE);
        }
    }

    public String getCurrentAudio()
    {
        mEnAudioTrackMode = getAudioTrackMode();
        String ret = "S";
        if(null != mEnAudioTrackMode)
        {
            switch (mEnAudioTrackMode)
            {
                case AUDIO_TRACK_STEREO:
                {
                    ret = "S";
                    break;
                }
                case AUDIO_TRACK_DOUBLE_LEFT:
                {
                    ret = "L";
                    break;
                }
                case AUDIO_TRACK_DOUBLE_RIGHT:
                {
                    ret = "R";
                    break;
                }
            }
        }
        return ret;
    }

    public boolean setPreAudio()
    {
        EnAudioTrackMode mode = getAudioTrackMode();
        if(null != mode)
        {
            mEnAudioTrackMode  = getPreTrackMode(mode);
            setAudioTrackMode(mEnAudioTrackMode);
        }
        return true;
    }

    public boolean  setNextAudio()
    {
        EnAudioTrackMode mode = getAudioTrackMode();
        if(null != mode)
        {
            mEnAudioTrackMode  = getNextTrackMode(mode);
            setAudioTrackMode(mEnAudioTrackMode);
        }
        return true;
    }

}
