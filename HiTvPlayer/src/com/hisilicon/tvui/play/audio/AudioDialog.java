package com.hisilicon.tvui.play.audio;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.dtv.network.service.AudioComponent;
import com.hisilicon.dtv.play.EnAudioTrackMode;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.pvrfileplay.PVRFilePlayer;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;

/**
 *
 * Audio module.main function:switch audio track,switch audio language.<br>
 *
 */
public class AudioDialog extends Dialog implements OnItemClickListener, OnClickListener
{
    private Player mAvPlayer;

    private PVRFilePlayer mPvrPlayer;

    private ListView mListView;

    private List<AudioComponent> mAudiolList = null;

    private AudioListAdapter mAdapter;

    //    private Channel mCurChannel = null;

    private Context mContext;

    public AudioDialog(Context context, int theme, List<AudioComponent> audiolList, Player avPlayer)
    {
        super(context, theme);
        mContext = context;
        mAudiolList = audiolList;
        mAvPlayer = avPlayer;
        mPvrPlayer = null;
    }

    public AudioDialog(Context context, int theme, List<AudioComponent> audiolList, PVRFilePlayer pvrPlayer)
    {
        super(context, theme);
        mContext = context;
        mAudiolList = audiolList;
        mAvPlayer = null;
        mPvrPlayer = pvrPlayer;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        LogTool.d(LogTool.MAUDIO, "Audio:onCreate");

        super.onCreate(savedInstanceState);
        LayoutParams params = this.getWindow().getAttributes();
        params.x = (int) mContext.getResources().getDimension(R.dimen.dimen_450px);
        params.y = (int) mContext.getResources().getDimension(R.dimen.dimen_180px);
        params.dimAmount = 0.0f;
        this.getWindow().setAttributes(params);
        initView();
    }

    protected void onStart()
    {
        LogTool.d(LogTool.MAUDIO, "Audio:onStart");
        super.onStart();
    }

    protected void onStop()
    {
        LogTool.d(LogTool.MAUDIO, "Audio:onStop");
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
        LogTool.d(LogTool.MAUDIO, "Audio:initView");
        setContentView(R.layout.audio);
        mListView = (ListView) findViewById(R.id.lv_audio_list);
        TextView mAudioTrackSTEREO = (TextView) findViewById(R.id.tv_audio_s);
        TextView mAudioTrackLeft = (TextView) findViewById(R.id.tv_audio_l);
        TextView mAudioTrackRight = (TextView) findViewById(R.id.tv_audio_r);
        ImageView mAudioLeft = (ImageView) findViewById(R.id.iv_audio_left);
        ImageView mAudioRight = (ImageView) findViewById(R.id.iv_audio_right);
        mAudioTrackSTEREO.setOnClickListener(this);
        mAudioTrackLeft.setOnClickListener(this);
        mAudioTrackRight.setOnClickListener(this);
        mAudioLeft.setOnClickListener(this);
        mAudioRight.setOnClickListener(this);

        AudioComponent mCurAudioInfo;
        if (null != mAvPlayer)
        {
            mCurAudioInfo = mAvPlayer.getCurrentAudio();
        }
        else
        {
            mCurAudioInfo = mPvrPlayer.getCurrentAudio();
        }

        if (null == mAudiolList)
        {
            LogTool.w(LogTool.MAUDIO, "the mAudioList is null");
            return;
        }

        for (AudioComponent temp : mAudiolList)
        {
            LogTool.d(LogTool.MAUDIO, "Audio:" + temp.getLanguageCode() + " " + temp.getPID() + " " + temp.getType());
        }

        if (null != mCurAudioInfo)
        {
            LogTool.d(LogTool.MAUDIO, "Audio:" + mCurAudioInfo.getLanguageCode() + " " + mCurAudioInfo.getPID() + " " + mCurAudioInfo.getType());
        }
        else
        {
            LogTool.d(LogTool.MAUDIO, "Audio:off");
        }

        mAdapter = new AudioListAdapter(mContext, mAudiolList);
        int curIndex = getIndexOfList(mCurAudioInfo, mAudiolList);
        mAdapter.setCurrentPosition(curIndex);

        if (null != getAudioTrackMode())
        {
            switch (getAudioTrackMode())
            {
            case AUDIO_TRACK_STEREO:
            {
                mAdapter.setmEnAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_STEREO);
                break;
            }
            case AUDIO_TRACK_DOUBLE_LEFT:
            case AUDIO_TRACK_ONLY_LEFT:
            {
                mAdapter.setmEnAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_DOUBLE_LEFT);
                break;
            }
            case AUDIO_TRACK_DOUBLE_RIGHT:
            case AUDIO_TRACK_ONLY_RIGHT:
            {
                mAdapter.setmEnAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_DOUBLE_RIGHT);
                break;
            }
            default:
            {
                LogTool.d(LogTool.MAUDIO, "Audio:" + getAudioTrackMode().toString());
                setAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_STEREO);
                mAdapter.setmEnAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_STEREO);
                break;
            }
            }
        }

        mListView.setAdapter(mAdapter);
        mListView.setSelection(curIndex);
        mListView.setOnItemClickListener(this);
    }

    private int getIndexOfList(AudioComponent audio, List<AudioComponent> list)
    {
        if ((null != audio) && (null != list))
        {
            for (int i = 0; i < list.size(); i++)
            {
                if ((audio.getLanguageCode().equals(list.get(i).getLanguageCode())) && (audio.getPID() == list.get(i).getPID())
                        && (audio.getType() == list.get(i).getType()))
                {
                    return i;
                }
            }
        }

        return 0;
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event)
    {
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_BACK:
        case KeyValue.DTV_KEYVALUE_AUDIO:
        {
            this.dismiss();
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_UP:
        {
            if (mListView.getSelectedItemPosition() == 0)
            {
                mListView.setSelection(mListView.getCount() - 1);
            }

            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_DOWN:
        {
            if (mListView.getSelectedItemPosition() == (mListView.getCount() - 1))
            {
                mListView.setSelectionFromTop(0, 0);
            }

            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
        {
            EnAudioTrackMode mEnAudioTrackMode = getPreTrackMode(getAudioTrackMode());
            setAudioTrackMode(mEnAudioTrackMode);
            mAdapter.setmEnAudioTrackMode(mEnAudioTrackMode);
            mAdapter.notifyDataSetChanged();
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
        {
            EnAudioTrackMode mEnAudioTrackMode = getNextTrackMode(getAudioTrackMode());
            setAudioTrackMode(mEnAudioTrackMode);
            mAdapter.setmEnAudioTrackMode(mEnAudioTrackMode);
            mAdapter.notifyDataSetChanged();
            break;
        }
        default:
            break;
        }

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        // TODO Auto-generated method stub
        AudioComponent mCurAudioInfo = (AudioComponent) mAdapter.getItem(position);

        if (null != mCurAudioInfo)
        {
            if (null != mAvPlayer)
            {
                mAvPlayer.selectAudio(mCurAudioInfo);
            }

            if (null != mPvrPlayer)
            {
                mPvrPlayer.selectAudio(mCurAudioInfo);
            }

            //            mPlayer.setAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_STEREO);

            mAdapter.setCurrentPosition(position);
            mAdapter.notifyDataSetChanged();

            // this.dismiss();
        }
    }

    @Override
    public void onClick(View view)
    {
        // TODO Auto-generated method stub
        EnAudioTrackMode mEnAudioTrackMode;

        switch (view.getId())
        {
        case R.id.tv_audio_l:
        {
            setAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_DOUBLE_LEFT);
            mAdapter.setmEnAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_DOUBLE_LEFT);
            mAdapter.notifyDataSetChanged();
            break;
        }
        case R.id.tv_audio_r:
        {
            setAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_DOUBLE_RIGHT);
            mAdapter.setmEnAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_DOUBLE_RIGHT);
            mAdapter.notifyDataSetChanged();
            break;
        }
        case R.id.tv_audio_s:
        {
            setAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_STEREO);
            mAdapter.setmEnAudioTrackMode(EnAudioTrackMode.AUDIO_TRACK_STEREO);
            mAdapter.notifyDataSetChanged();
            break;
        }
        case R.id.iv_audio_left:
        {
            mEnAudioTrackMode = getPreTrackMode(getAudioTrackMode());
            setAudioTrackMode(mEnAudioTrackMode);
            mAdapter.setmEnAudioTrackMode(mEnAudioTrackMode);
            mAdapter.notifyDataSetChanged();
            break;
        }
        case R.id.iv_audio_right:
        {
            mEnAudioTrackMode = getNextTrackMode(getAudioTrackMode());
            setAudioTrackMode(mEnAudioTrackMode);
            mAdapter.setmEnAudioTrackMode(mEnAudioTrackMode);
            mAdapter.notifyDataSetChanged();
            break;
        }
        default:
        {
            break;
        }
        }
    }
}
