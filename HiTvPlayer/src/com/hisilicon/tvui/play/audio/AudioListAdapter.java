package com.hisilicon.tvui.play.audio;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.dtv.network.service.AudioComponent;
import com.hisilicon.dtv.play.EnAudioTrackMode;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.LanguageMap;

/**
 *
 *  Defined adapter for audio ListView.<br>
 *
 */
public class AudioListAdapter extends BaseAdapter
{
    private static final String AUDIOTRACK_STEREO = "S";

    private static final String AUDIOTRACK_LEFT = "L";

    private static final String AUDIOTRACK_RIGHT = "R";

    private LayoutInflater mLayoutInflater = null;

    private List<AudioComponent> mAudioList;

    private int mPosition = 0;

    private LanguageMap mLanguageMap;

    private EnAudioTrackMode mEnAudioTrackMode;

    private class ViewHolder
    {
        private TextView mtv_audio_title = null;

        private TextView miv_audio_type = null;

        private ImageView miv_audio_checkbox = null;
    }

    public AudioListAdapter(Context context, List<AudioComponent> audioList)
    {
        if (null != context)
        {
            this.mLayoutInflater = LayoutInflater.from(context);
            mLanguageMap = new LanguageMap(context);
        }
        this.mAudioList = audioList;

    }

    public void setmEnAudioTrackMode(EnAudioTrackMode mEnAudioTrackMode)
    {
        this.mEnAudioTrackMode = mEnAudioTrackMode;
    }

    // set current selected position
    public void setCurrentPosition(int pose)
    {
        mPosition = pose;
    }

    // get current selected position
    public int getCurrentPosition()
    {
        return mPosition;
    }

    public EnAudioTrackMode getmEnAudioTrackMode()
    {
        return mEnAudioTrackMode;
    }

    public List<AudioComponent> getSubtitelList()
    {
        return mAudioList;
    }

    @Override
    public int getCount()
    {
        return mAudioList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mAudioList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder mHolder = null;
        if (convertView == null)
        {
            mHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.audio_listitem, null);
            mHolder.mtv_audio_title = (TextView) convertView.findViewById(R.id.tv_audio_item_text);
            mHolder.miv_audio_type = (TextView) convertView.findViewById(R.id.iv_audio_item_type);
            mHolder.miv_audio_checkbox = (ImageView) convertView.findViewById(R.id.iv_audio_item_check);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder) convertView.getTag();
        }

        mHolder.mtv_audio_title.setText(mLanguageMap.getLanguage(mAudioList.get(position).getLanguageCode()));

        if (mPosition == position)
        {
            switch (mEnAudioTrackMode)
            {
            case AUDIO_TRACK_STEREO:
            {
                mHolder.miv_audio_type.setText(AUDIOTRACK_STEREO);
                break;
            }
            case AUDIO_TRACK_DOUBLE_LEFT:
            {
                mHolder.miv_audio_type.setText(AUDIOTRACK_LEFT);
                break;
            }
            case AUDIO_TRACK_DOUBLE_RIGHT:
            {
                mHolder.miv_audio_type.setText(AUDIOTRACK_RIGHT);
                break;
            }
            default:
            {
                break;
            }
            }

            mHolder.miv_audio_type.setVisibility(View.VISIBLE);
            mHolder.miv_audio_checkbox.setVisibility(View.VISIBLE);
        }
        else
        {
            mHolder.miv_audio_type.setVisibility(View.INVISIBLE);
            mHolder.miv_audio_checkbox.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }
}
