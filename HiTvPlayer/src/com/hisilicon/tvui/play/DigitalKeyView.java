package com.hisilicon.tvui.play;

import java.util.List;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelFilter;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;

public class DigitalKeyView {
    private static final int DIGITAL_BIT = 4;
    private static final int STRING_BIT = 11;
    private static final int INVALID_NUMBER = 0;
    private static final int DTV_CHANELINPUT_MAX_SPACE = 1000;
    //1-5digits or 1-5digits +"-" or 1-5 digits +"-" + 1-5 digits
    private static final String ATSC_CH_NUM_REGEX = "\\d{1,5}(-\\d{0,5})?";

    private MainActivity mMainActivity = null;
    private TextView mInputKeyNumTextView = null;
    private int[] mDigitalKey = new int[DIGITAL_BIT];
    private StringBuffer mSB = new StringBuffer();
    private int mInputCount = 0;
    private Handler mDtvPlayerHandler = null;
    private String mInvalidChnNum = null;
    public ChannelManager mChannelManager = null;

    public DigitalKeyView(MainActivity arg0, ChannelManager mChannelManager) {
        mInputKeyNumTextView = (TextView) arg0.findViewById(R.id.tv_play_channel_id);
        mMainActivity = arg0;
        this.mChannelManager = mChannelManager;
        mDtvPlayerHandler = new Handler();
        mInvalidChnNum = mMainActivity.getResources().getString(R.string.play_channel_invalid);
        resetValue();
    }

    private void resetValue() {
        for (int i = 0; i < mDigitalKey.length; i++) {
            mDigitalKey[i] = INVALID_NUMBER;
        }
        mSB = new StringBuffer();
        mInputCount = 0;
        mDtvPlayerHandler.removeCallbacks(DelayInputChannel);
    }

    private Runnable DismissInvalid = new Runnable() {
        @Override
        public void run() {
            if (mInputKeyNumTextView.getText().equals(mInvalidChnNum)) {
                mInputKeyNumTextView.setVisibility(View.INVISIBLE);
            }
        }
    };

    private Runnable DelayInputChannel = new Runnable() {
        @Override
        public void run() {
            int curSourceId = mMainActivity.mCurSourceId;
            //ATSC channel
            if (curSourceId == halApi.EnumSourceIndex.SOURCE_ATSC || curSourceId == halApi.EnumSourceIndex.SOURCE_ISDBT) {
                changeChannelWithHyphen();
            } else {
                changeChannelDigitalOnly();
            }
        }
    };

    private void changeChannelDigitalOnly() {
        int inputChnNum = 0;

        for (int i = 0; i < mDigitalKey.length; i++) {
            inputChnNum += mDigitalKey[i] * Math.pow(10, i);
        }

        ChannelList curChannelList = ChannelHistory.getInstance().getCurrentList(mMainActivity.mCurSourceId);
        ChannelFilter channelFilter = curChannelList.getFilter();
        if (channelFilter != null) {
            List<EnTagType> mEditTypes = channelFilter.getTagType();
            for (int i = 0; i < mEditTypes.size(); i++) {
                if (mEditTypes.get(i) == EnTagType.HIDE) {
                    mEditTypes.remove(i);
                }
            }
            channelFilter.setTagType(mEditTypes);
            curChannelList.setFilter(channelFilter);
        }
        int tmpPos = curChannelList.getPosByChannelLcn(inputChnNum);
        Channel tempChannel = curChannelList.getChannelByIndex(tmpPos);
        if (null == tempChannel) {
            tempChannel = mChannelManager.getChannelByNo(inputChnNum);

            if (null == tempChannel) {
                mInputKeyNumTextView.setText(mInvalidChnNum);
                mDtvPlayerHandler.postDelayed(DismissInvalid, DTV_CHANELINPUT_MAX_SPACE);
            } else {
                mMainActivity.playChannel(curChannelList, tempChannel, true);
            }
        } else {
            mMainActivity.playChannel(curChannelList, tempChannel, true);
        }
        resetValue();
    }

    private void changeChannelWithHyphen() {
        int inputChnNum = 0;

        int majorNum = -1;
        int minorNum = -1;
        String channelNumStr = mInputKeyNumTextView.getText().toString();
        String[] channelNumStrs = channelNumStr.split("-");
        if (channelNumStr.length() > 0) {
            majorNum = Integer.valueOf(channelNumStrs[0]);
        }
        if (channelNumStrs.length > 1) {
            minorNum = Integer.valueOf(channelNumStrs[1]);
        }

        EnNetworkType networkType = mMainActivity.mNetworkManager.getCurrentNetworkType();

        ChannelList curChannelList = ChannelHistory.getInstance().getCurrentList(mMainActivity.mCurSourceId);
        ChannelFilter channelFilter = curChannelList.getFilter();
        if (channelFilter != null) {
            List<EnTagType> mEditTypes = channelFilter.getTagType();
            for (int i = 0; i < mEditTypes.size(); i++) {
                if (mEditTypes.get(i) == EnTagType.HIDE) {
                    mEditTypes.remove(i);
                }
            }
            channelFilter.setTagType(mEditTypes);
            curChannelList.setFilter(channelFilter);
        }

        Channel channelToBePlay = null;
        //No hyphens or just hyphens, only frequency points
        if (channelNumStrs.length == 1) {
            //Have this frequency, cut the first program at this frequency
            boolean foundFreq = false;
            int channelCount = curChannelList.getChannelCount();
            for (int i = 0; i < channelCount; i++) {
                Channel channel = curChannelList.getChannelByIndex(i);
                if (((channel.getLCN() >> 16) & 0xffff) == majorNum) {
                    channelToBePlay = channel;
                    foundFreq = true;
                    break;
                }
            }
            //No such frequency, automatically search for this frequency
            if (!foundFreq) {
                //Frequency out of valid range
                if ((networkType == EnNetworkType.ATSC_T && (majorNum < 2 || majorNum > 69))
                        || (networkType == EnNetworkType.ATSC_CAB && (majorNum < 1 || majorNum > 135))) {
                    mInputKeyNumTextView.setText(mInvalidChnNum);
                    mDtvPlayerHandler.postDelayed(DismissInvalid, DTV_CHANELINPUT_MAX_SPACE);
                    Toast.makeText(mMainActivity, R.string.str_atsc_validate_input, Toast.LENGTH_SHORT).show();
                    resetValue();
                    return;
                } else if (networkType == EnNetworkType.ISDB_TER && (majorNum < 7 || majorNum > 69)) {
                    mInputKeyNumTextView.setText(mInvalidChnNum);
                    mDtvPlayerHandler.postDelayed(DismissInvalid, DTV_CHANELINPUT_MAX_SPACE);
                    Toast.makeText(mMainActivity, R.string.str_atsc_validate_input, Toast.LENGTH_SHORT).show();
                    resetValue();
                    return;
                } else {
                    if (networkType == EnNetworkType.ISDB_TER) {
                        mMainActivity.scanISDBTFreq(networkType,majorNum);
                    } else {
                        mMainActivity.scanATSCFreq(networkType, majorNum);
                    }
                }
            }
        }
        //Have a hyphen to switch to the corresponding channel
        else {
            inputChnNum = (majorNum << 16 & 0xffff0000) | (minorNum & 0xffff);
        }

        if (channelToBePlay == null && inputChnNum != 0) {
            int tmpChannelID = curChannelList.getPosByChannelLcn(inputChnNum);
            channelToBePlay = curChannelList.getChannelByIndex(tmpChannelID);
        }

        if (channelToBePlay == null) {
            mInputKeyNumTextView.setText(mInvalidChnNum);
            mDtvPlayerHandler.postDelayed(DismissInvalid, DTV_CHANELINPUT_MAX_SPACE);
        } else {
            mMainActivity.playChannel(curChannelList, channelToBePlay, true);
        }
        resetValue();
    }

    /* Receiving number keys */
    public void inputKey(int keyCode) {
        /* Retime */
        mDtvPlayerHandler.removeCallbacks(DelayInputChannel);

        int curSourceId = mMainActivity.mCurSourceId;
        String textNumberString = "";
        if (curSourceId == halApi.EnumSourceIndex.SOURCE_ATSC || curSourceId == halApi.EnumSourceIndex.SOURCE_ISDBT) {
            textNumberString = inputKeyWithHyphen(keyCode);
            if (textNumberString.length() == 0) {
                return;
            }
        } else {
            textNumberString = inputKeyDigitalOnly(keyCode);
        }

        mInputKeyNumTextView.setText(textNumberString);
        mInputKeyNumTextView.setVisibility(View.VISIBLE);
        if (curSourceId == halApi.EnumSourceIndex.SOURCE_ATSC || curSourceId == halApi.EnumSourceIndex.SOURCE_ISDBT) {
            if (STRING_BIT == mInputCount) {
                /* change channel now */
                mDtvPlayerHandler.post(DelayInputChannel);
                return;
            }
        } else if (DIGITAL_BIT == mInputCount) {
            /* change channel now */
            mDtvPlayerHandler.post(DelayInputChannel);
            return;
        }
        /* change channel after 1 second */
        mDtvPlayerHandler.postDelayed(DelayInputChannel, DTV_CHANELINPUT_MAX_SPACE);
    }

    public void hide() {
        mInputKeyNumTextView.setVisibility(View.INVISIBLE);
        resetValue();
    }

    private String inputKeyDigitalOnly(int keyCode) {
        /* Move one bit forward */
        for (int i = mDigitalKey.length - 1; i > 0; i--) {
            mDigitalKey[i] = mDigitalKey[i - 1];
        }
        mDigitalKey[0] = keyCode - KeyValue.DTV_KEYVALUE_0;
        mInputCount++;
        String textNumberString = "";
        for (int i = mDigitalKey.length - 1; i >= 0; i--) {
            textNumberString += String.valueOf(mDigitalKey[i]);
        }
        return textNumberString;
    }

    private String inputKeyWithHyphen(int keyCode) {
        String newChar = "";
        if (keyCode == KeyValue.DTV_KEYVALUE_CHNLLIST) {
            newChar = "-";
        } else {
            newChar = (keyCode - KeyValue.DTV_KEYVALUE_0) + "";
        }
        String newStr = mSB.toString() + newChar;
        if (newStr.matches(ATSC_CH_NUM_REGEX)) {
            mSB.append(newChar);
            mInputCount++;
        }
        return mSB.toString();
    }

    public boolean isShow() {
        return mInputKeyNumTextView.getVisibility() == View.VISIBLE && mInputCount > 0;
    }
}
