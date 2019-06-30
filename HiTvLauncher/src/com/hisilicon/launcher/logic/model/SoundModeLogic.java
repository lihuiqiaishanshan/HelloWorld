
package com.hisilicon.launcher.logic.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Handler;
import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.AudioInterface;
import com.hisilicon.launcher.interfaces.InterfaceValueMaps;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.AccessProgressInterface;
import com.hisilicon.launcher.model.WidgetType.AccessSysValueInterface;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.util.Util;

/**
 * SoundModeLogic
 *
 * @author wangchuanjian
 */
public class SoundModeLogic implements InterfaceLogic {

    private static final String TAG = "SoundModeLogic";
    private Context mContext;
    private AudioManager mAudioManager;

    public SoundModeLogic(Context mContext) {
        super();
        this.mContext = mContext;
        mAudioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public List<WidgetType> getWidgetTypeList() {
        List<WidgetType> mWidgetList = new ArrayList<WidgetType>();
        Resources res = mContext.getResources();
        // SoundMode
        WidgetType mSoundMode = new WidgetType();
        // set name for SoundMode
        mSoundMode.setName(res.getStringArray(R.array.sound_mode_string)[0]);
        // set type for SoundMode
        mSoundMode.setType(WidgetType.TYPE_SELECTOR);
        mSoundMode.setmAccessSysValueInterface(new AccessSysValueInterface() {

            @Override
            public int setSysValue(int i) {
                LogHelper.d(TAG, "mSoundMode setSysValue i = " + i);
                int ret = AudioInterface
                        .setSoundMode(InterfaceValueMaps.voice_mode_logic[i][0]);
                return ret;

            }

            @Override
            public int getSysValue() {
                int mode = AudioInterface.getSoundMode();
                return Util.getIndexFromArray(mode, InterfaceValueMaps.voice_mode_logic);
            }
        });
        // set data for SoundMode
        mSoundMode.setData(Util
                .createArrayOfParameters(InterfaceValueMaps.voice_mode_logic));
        mWidgetList.add(mSoundMode);

        // InputVolume
        WidgetType mInputVolume = new WidgetType();
        // set name for InputVolume
        mInputVolume.setName(res.getStringArray(R.array.sound_mode_string)[1]);
        // set type for InputVolume
        mInputVolume.setType(WidgetType.TYPE_PROGRESS);
        // set max value of progress
        mInputVolume.setMaxProgress(mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mInputVolume.setmAccessProgressInterface(new AccessProgressInterface() {

            @Override
            public int setProgress(int i) {
                LogHelper.d(TAG, "mInputVolume setProgress   mAudioManager.setStreamVolume = "
                        + i);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i,
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                return i;
            }

            @Override
            public int getProgress() {
                LogHelper.d(TAG,
                        "mInputVolume getProgress mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) = "
                                + mAudioManager
                                        .getStreamVolume(AudioManager.STREAM_MUSIC));
                int volume = mAudioManager
                        .getStreamVolume(AudioManager.STREAM_MUSIC);
                return volume;
            }
        });
        mWidgetList.add(mInputVolume);

        // Balance
        WidgetType mBalance = new WidgetType();
        // set name for Balance
        mBalance.setName(res.getStringArray(R.array.sound_mode_string)[2]);
        // set type for Balance
        mBalance.setType(WidgetType.TYPE_PROGRESS);
        // set max value of progress
        mBalance.setMaxProgress(Constant.BARLENGTH);
        // set offset
        mBalance.setOffset(-50);
        mBalance.setmAccessProgressInterface(new AccessProgressInterface() {

            @Override
            public int setProgress(int i) {
                LogHelper.d(TAG, "mBalance AudioInterface.setBalance(i)="
                        + AudioInterface.setBalance(i));

                return AudioInterface.setBalance(i);
            }

            @Override
            public int getProgress() {
                LogHelper.d(TAG, "mBalance AudioInterface.getBalance()="
                        + AudioInterface.getBalance());
                return AudioInterface.getBalance();
            }
        });
        mWidgetList.add(mBalance);
        return mWidgetList;
    }

    @Override
    public void setHandler(Handler handler) {
    }

    @Override
    public void dismissDialog() {
        // TODO Auto-generated method stub
    }

}
