package com.hisilicon.tvui.play;

import android.graphics.Rect;
import android.os.SystemProperties;
import android.view.KeyEvent;

import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.network.service.EnServiceType;
import com.hisilicon.dtv.play.EnZOrder;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.MyToast;

public class PipView extends BaseView
{
    private MainActivity mMainActivity;
    private Player mSmallWndPlayer;
    private boolean mIsShowSmallPlayerWnd = false;
    private Channel mCurSmallChn = null;

    private static final int DTV_FRM_AV_PIP_STATE = 6;/*Notes: equal to HI_FRM_AV_PIP_STATE in HI_FRM_AV_PLAY_STATUS_E struct*/
    private static final int PIP_RECT_NUMBER = 4;
    private Rect[] PIP_RECT = new Rect[PIP_RECT_NUMBER];
    private int mCurPositon = 0;

    public PipView(MainActivity arg0)
    {
        super(null);
        mMainActivity = arg0;
        if (mPlayerManager.getPlayers().size() > 1)
        {
            mSmallWndPlayer = mPlayerManager.getPlayers().get(1);
        }
        else
        {
            mSmallWndPlayer = mPlayerManager.createPlayer();
        }
        int dimen192 = (int) mMainActivity.getResources().getDimension(R.dimen.dimen_192px);
        int dimen108 = (int) mMainActivity.getResources().getDimension(R.dimen.dimen_108px);
        int DEFAULT_DISPLAY_SMALL_WIDTH = dimen192 * 4;
        final int DEFAULT_DISPLAY_SMALL_HIGHT = dimen108 * 4;
        PIP_RECT[0] = new Rect(dimen192, dimen108, dimen192 + DEFAULT_DISPLAY_SMALL_WIDTH, dimen108 + DEFAULT_DISPLAY_SMALL_HIGHT);
        PIP_RECT[1] = new Rect(dimen192 * 5, dimen108, dimen192 * 5 + DEFAULT_DISPLAY_SMALL_WIDTH, dimen108 + DEFAULT_DISPLAY_SMALL_HIGHT);
        PIP_RECT[2] = new Rect(dimen192, dimen108 * 5, dimen192 + DEFAULT_DISPLAY_SMALL_WIDTH, dimen108 * 5 + DEFAULT_DISPLAY_SMALL_HIGHT);
        PIP_RECT[3] = new Rect(dimen192 * 5, dimen108 * 5, dimen192 * 5 + DEFAULT_DISPLAY_SMALL_WIDTH, dimen108 * 5 + DEFAULT_DISPLAY_SMALL_HIGHT);
    }

    private boolean isAllowPip(Channel tmpChn)
    {
        if (SystemProperties.get("ro.product.brand").contains("DPT") //DPT
                || (tmpChn != null && EnServiceType.getRadioServiceTypes().contains(tmpChn.getServiceType())) //Radio
                || (tmpChn != null && tmpChn.isScramble())) //Scramble
        {
            MyToast.makeText(mMainActivity, R.string.str_pip_not_support, MyToast.LENGTH_LONG).show();
            return false;
        }

        /* 4K stream and 4K display out DO NOT support now */
        /*if (halApi.is4K())
        {
            MyToast.makeText(mMainActivity, R.string.str_pip_not_support, MyToast.LENGTH_LONG).show();
            LogTool.e(LogTool.MREC, "4K stream and 4K display out DO NOT support now");
            return false;
        }*/
        int dimen = (int) mMainActivity.getResources().getDimension(R.dimen.dimen_1920px);
        if (mPlayer.getVideoResolutionWidth() > dimen)
        {
            MyToast.makeText(mMainActivity, R.string.str_pip_not_support, MyToast.LENGTH_LONG).show();
            LogTool.w(LogTool.MREC, "4K stream and 4K display out DO NOT support now");
            return false;
        }
        return true;
    }

    @Override
    public void show()
    {
        Channel tmpChn = mPlayer.getCurrentChannel();
        if (isAllowPip(tmpChn))
        {
            mSmallWndPlayer.resumeResource();
            mSmallWndPlayer.setMute(true);
            int ret = mSmallWndPlayer.changeChannel(tmpChn);
            if (0 == ret)
            {
                mCurSmallChn = tmpChn;
                mIsShowSmallPlayerWnd = true;
                Rect mSmallWndPlayerRect = PIP_RECT[mCurPositon % PIP_RECT_NUMBER];
                ret = mSmallWndPlayer.setWindowRect(mSmallWndPlayerRect);
                LogTool.d(LogTool.MPLAY, " setWindowRect ret = " + ret);
                ret = mSmallWndPlayer.setZOrder(EnZOrder.MOVEUP);
                LogTool.d(LogTool.MPLAY, " setZOrder ret = " + ret);
            }
        }
        super.show();
    }

    @Override
    public void hide()
    {
        mSmallWndPlayer.releaseResource(DTV_FRM_AV_PIP_STATE);
        mIsShowSmallPlayerWnd = false;
        super.hide();
    }

    @Override
    public void toggle()
    {

    }

    @Override
    public boolean isShow()
    {
        return mIsShowSmallPlayerWnd;
    }

    private Channel getSmallNextChn(boolean bAdd)
    {
        Channel newChn = mCurSmallChn;
        do
        {
            // TODO: PIP可以播放加父母锁节目
            newChn = mMainActivity.getNextChannel(mChnHistory.getCurrentList(halApi.EnumSourceIndex.SOURCE_AUTO), newChn, bAdd);
            if (newChn.isScramble() || newChn.getTag(EnTagType.LOCK) || mMainActivity.isParentalRatingNeedBlock(newChn))
            {
                continue;
            }
            else
            {
                break;
            }
        }
        while (newChn.getChannelID() != mCurSmallChn.getChannelID());
        return newChn;
    }

    /* 0 return false , 1 return true, 2 not return  */
    public int onListViewKeyDown(int keyCode, KeyEvent keyEvent)
    {
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_VOLUME_MUTE:
        case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
        case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
        case KeyValue.DTV_KEYVALUE_VOLUME_DOWN:
        case KeyValue.DTV_KEYVALUE_VOLUME_UP:
        case KeyValue.DTV_KEYVALUE_INFOBAR:
        {
            return RET_MAIN;
        }

        //Change the PIP position
        case KeyValue.DTV_KEYVALUE_RED:
        {
            mSmallWndPlayer.setWindowRect(PIP_RECT[++mCurPositon % PIP_RECT_NUMBER]);
            break;
        }
        //Switch PIP and main AV
        case KeyValue.DTV_KEYVALUE_BLUE:
        {
            Channel curMainChn = mChnHistory.getCurrentChn(mMainActivity.mCurSourceId);
            if (curMainChn.getChannelID() == mCurSmallChn.getChannelID())
            {
                break;
            }
            if (curMainChn.isScramble() || curMainChn.getTag(EnTagType.LOCK) || mMainActivity.isParentalRatingNeedBlock(curMainChn))
            {
                MyToast.makeText(mMainActivity, R.string.str_pip_close_first, MyToast.LENGTH_SHORT).show();
                break;
            }
            mSmallWndPlayer.changeChannel(curMainChn);
            mMainActivity.playChannel(mChnHistory.getCurrentList(mMainActivity.mCurSourceId), mCurSmallChn, false);
            mCurSmallChn = curMainChn;
            break;
        }
        //Close PIP
        case KeyValue.DTV_KEYVALUE_BACK:
        case KeyValue.DTV_KEYVALUE_YELLOW:
        {
            hide();
            break;
        }
        case KeyValue.DTV_KEYVALUE_CHANNEL_UP:
        {
            Channel newChn = getSmallNextChn(true);
            if (newChn.getBelongMultiplexe().getID() == mCurSmallChn.getBelongMultiplexe().getID())
            {
                mSmallWndPlayer.changeChannel(newChn);
                mCurSmallChn = newChn;
                return RET_SUPER_TRUE;
            }
            else
            {
                MyToast.makeText(mMainActivity, R.string.str_pip_close_first, MyToast.LENGTH_SHORT).show();
            }
            break;
        }
        case KeyValue.DTV_KEYVALUE_CHANNEL_DOWN:
        {
            Channel newChn = getSmallNextChn(false);
            if (newChn.getBelongMultiplexe().getID() == mCurSmallChn.getBelongMultiplexe().getID())
            {
                mSmallWndPlayer.changeChannel(newChn);
                mCurSmallChn = newChn;
                return RET_SUPER_TRUE;
            }
            else
            {
                MyToast.makeText(mMainActivity, R.string.str_pip_close_first, MyToast.LENGTH_SHORT).show();
            }
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_UP:
        {
            Channel newChn = mMainActivity.getNextChannel(
                    mChnHistory.getCurrentList(halApi.EnumSourceIndex.SOURCE_AUTO),
                    mChnHistory.getCurrentChn(mMainActivity.mCurSourceId), true);
            if (newChn.getBelongMultiplexe().getID() == mChnHistory.
                    getCurrentChn(mMainActivity.mCurSourceId).getBelongMultiplexe().getID())
            {
                return RET_MAIN;
            }
            else
            {
                MyToast.makeText(mMainActivity, R.string.str_pip_close_first, MyToast.LENGTH_SHORT).show();
            }
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_DOWN:
        {
            Channel newChn = mMainActivity.getNextChannel(
                    mChnHistory.getCurrentList(halApi.EnumSourceIndex.SOURCE_AUTO),
                    mChnHistory.getCurrentChn(mMainActivity.mCurSourceId), false);
            if (newChn.getBelongMultiplexe().getID() == mChnHistory.
                    getCurrentChn(mMainActivity.mCurSourceId).getBelongMultiplexe().getID())
            {
                return RET_MAIN;
            }
            else
            {
                MyToast.makeText(mMainActivity, R.string.str_pip_close_first, MyToast.LENGTH_SHORT).show();
            }
            break;
        }
        default:
        {
            MyToast.makeText(mMainActivity, R.string.str_pip_close_first, MyToast.LENGTH_SHORT).show();
            break;
        }
        }
        return RET_SUPER_TRUE;
    }
}
