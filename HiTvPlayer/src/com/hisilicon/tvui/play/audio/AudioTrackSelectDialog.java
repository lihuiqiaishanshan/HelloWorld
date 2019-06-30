/**
 *
 */
package com.hisilicon.tvui.play.audio;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.play.EnAudioTrackMode;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;


/**
 * This dialog for Dolby test only
 * @author y00164887
 *
 */
public class AudioTrackSelectDialog extends Dialog implements OnItemClickListener, OnItemSelectedListener
{
    ListView mListView = null;
    AudioTrackListAdapter mAdapter = null;
    Context mContext = null;

    private Player mPlayer;

    public AudioTrackSelectDialog(Context context, int theme)
    {
        super(context, theme);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initDTV();
        initView();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_BACK:
            this.dismiss();
            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initDTV()
    {
        DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        PlayerManager mPlayerManager = mDTV.getPlayerManager();
        mPlayer = mPlayerManager.getPlayers().get(0);
    }

    private void initView()
    {
        setContentView(R.layout.play_audio_track);
        mListView = (ListView) findViewById(R.id.lv_audio_track);
        mAdapter = new AudioTrackListAdapter(mContext);
        EnAudioTrackMode mode = mPlayer.getAudioTrackMode();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemSelectedListener(this);
        if(mode != null)
        {
            int position = mode.getValue();
            mAdapter.setCurrentPosition(position);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        EnAudioTrackMode mode = EnAudioTrackMode.valueOf(arg2);
        LogTool.d(LogTool.MPLAY, "index:" + arg2 + "mode is:" + mode);
        mPlayer.setAudioTrackMode(mode);
        mAdapter.setCurrentPosition(arg2);
        mAdapter.notifyDataSetChanged();

    }
}
