package com.hisilicon.tvui.play;

import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.pvr.PvrActivity;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.CheckPassWordDialog;
import com.hisilicon.tvui.view.CheckPassWordDialog.CheckPassWordDialogInterface;

public class TipMsgView extends BaseView
{
    private static final String TAG = "TipMsgView";

    public static final int TIPMSG_PROGRAM_LOCK_DIALOG = 0;
    public static final int TIPMSG_PROGRAM_LOCK_TIP = 1;
    public static final int TIPMSG_PARENTAL_RATING = 2;
    public static final int TIPMSG_NOPROGRAM = 3;
    public static final int TIPMSG_NOSIGNAL = 4;
    public static final int TIPMSG_UNSUPPORT = 5;
    public static final int TIPMSG_FRONTEND_ERROR = 6;
    public static final int TIPMSG_CA = 7;
    public static final int TIPMSG_SOURCE_LOCK = 8;
    public static final int TIPMSG_BUTT = 9;

    private CheckPassWordDialog.Builder mBuilder = null;
    public String mProgLockTitle;
    public String mParentalRatingtitle;
    public String mSourceLockTitle;

    /**
     * Dialog use to input password to unlock Lock Channel.
     */
    private CheckPassWordDialog sPasswordDialog = null;

    private static class TipMsg
    {
        public int mMsgPrior = 0;
        public int mMsgType = TIPMSG_BUTT;
        public int mMsgDesc = 0;
        public boolean mMsgShow = false;

        public TipMsg(int msgPrior, int msgType, int msgDesc)
        {
            mMsgPrior = msgPrior;
            mMsgType = msgType;
            mMsgDesc = msgDesc;
            mMsgShow = false;
        }
    }

    private TipMsg[] mMsgString = null;

    private MainActivity mMainActivity =null;
    private PvrActivity mPvrActivity =null;
    private TextView mTipTitleTextView = null;

    public TipMsgView(MainActivity arg0)
    {
        super((LinearLayout) arg0.findViewById(R.id.ly_tip_msg));
        mMainActivity = arg0;
        mTipTitleTextView = (TextView) mMainActivity.findViewById(R.id.tv_tip_msg_title);
        mMsgString = new TipMsg[TIPMSG_BUTT];
        mMsgString[TIPMSG_PROGRAM_LOCK_DIALOG] = new TipMsg(TIPMSG_PROGRAM_LOCK_DIALOG, TIPMSG_PROGRAM_LOCK_DIALOG, R.string.play_unlock_fail);
        mMsgString[TIPMSG_PROGRAM_LOCK_TIP] = new TipMsg(TIPMSG_PROGRAM_LOCK_TIP, TIPMSG_PROGRAM_LOCK_TIP, R.string.play_unlock_fail);
        mMsgString[TIPMSG_PARENTAL_RATING] = new TipMsg(TIPMSG_PARENTAL_RATING, TIPMSG_PARENTAL_RATING, R.string.play_unlock_fail);
        mMsgString[TIPMSG_NOPROGRAM] = new TipMsg(TIPMSG_NOPROGRAM, TIPMSG_NOPROGRAM, R.string.play_no_channel_title);
        mMsgString[TIPMSG_NOSIGNAL] = new TipMsg(TIPMSG_NOSIGNAL, TIPMSG_NOSIGNAL, R.string.play_no_signal_title);
        mMsgString[TIPMSG_UNSUPPORT] = new TipMsg(TIPMSG_UNSUPPORT, TIPMSG_UNSUPPORT, R.string.play_signal_unsupport);
        mMsgString[TIPMSG_FRONTEND_ERROR] = new TipMsg(TIPMSG_FRONTEND_ERROR, TIPMSG_FRONTEND_ERROR, R.string.play_av_stop_title);
        mMsgString[TIPMSG_CA] = new TipMsg(TIPMSG_CA, TIPMSG_CA, R.string.play_ca_title);
        mMsgString[TIPMSG_SOURCE_LOCK] = new TipMsg(TIPMSG_SOURCE_LOCK, TIPMSG_SOURCE_LOCK, R.string.setting_source_lock);
        mProgLockTitle = mMainActivity.getString(R.string.play_password_program_lock);
        mParentalRatingtitle = mMainActivity.getString(R.string.play_password_parental_rating);
        mSourceLockTitle = mMainActivity.getString(R.string.play_password_source_lock);
        mBuilder = new CheckPassWordDialog.Builder(mMainActivity, R.style.DIM_STYLE);
        sPasswordDialog = mBuilder.setCheckPassWordsListener(new CheckPassWordListener()).create();
        sPasswordDialog.setCanceledOnTouchOutside(false);
        sPasswordDialog.setOnKeyListener(new DialogListener());

    }
    public TipMsgView(PvrActivity arg0)
    {
        super((LinearLayout) arg0.findViewById(R.id.pvr_tip_msg));
        mPvrActivity = arg0;
        mTipTitleTextView = (TextView) mPvrActivity.findViewById(R.id.pvr_tip_msg_title);
        mMsgString = new TipMsg[TIPMSG_BUTT];
        mMsgString[TIPMSG_PROGRAM_LOCK_DIALOG] = new TipMsg(TIPMSG_PROGRAM_LOCK_DIALOG, TIPMSG_PROGRAM_LOCK_DIALOG, R.string.play_unlock_fail);
        mMsgString[TIPMSG_PROGRAM_LOCK_TIP] = new TipMsg(TIPMSG_PROGRAM_LOCK_TIP, TIPMSG_PROGRAM_LOCK_TIP, R.string.play_unlock_fail);
        mMsgString[TIPMSG_PARENTAL_RATING] = new TipMsg(TIPMSG_PARENTAL_RATING, TIPMSG_PARENTAL_RATING, R.string.play_unlock_fail);
        mMsgString[TIPMSG_NOPROGRAM] = new TipMsg(TIPMSG_NOPROGRAM, TIPMSG_NOPROGRAM, R.string.play_no_channel_title);
        mMsgString[TIPMSG_NOSIGNAL] = new TipMsg(TIPMSG_NOSIGNAL, TIPMSG_NOSIGNAL, R.string.play_no_signal_title);
        mMsgString[TIPMSG_UNSUPPORT] = new TipMsg(TIPMSG_UNSUPPORT, TIPMSG_UNSUPPORT, R.string.play_signal_unsupport);
        mMsgString[TIPMSG_FRONTEND_ERROR] = new TipMsg(TIPMSG_FRONTEND_ERROR, TIPMSG_FRONTEND_ERROR, R.string.play_av_stop_title);
        mMsgString[TIPMSG_CA] = new TipMsg(TIPMSG_CA, TIPMSG_CA, R.string.play_ca_title);
        mMsgString[TIPMSG_SOURCE_LOCK] = new TipMsg(TIPMSG_SOURCE_LOCK, TIPMSG_SOURCE_LOCK, R.string.setting_source_lock);
        mProgLockTitle = mPvrActivity.getString(R.string.play_password_program_lock);
        mParentalRatingtitle = mPvrActivity.getString(R.string.play_password_parental_rating);
        mSourceLockTitle = mPvrActivity.getString(R.string.play_password_source_lock);
        mBuilder = new CheckPassWordDialog.Builder(mPvrActivity, R.style.DIM_STYLE);
        sPasswordDialog = mBuilder.setCheckPassWordsListener(new CheckPassWordInPvrActivityListener()).create();
        sPasswordDialog.setCanceledOnTouchOutside(false);
        sPasswordDialog.setOnKeyListener(new DialogListener());

    }

    public class CheckPassWordInPvrActivityListener implements CheckPassWordDialogInterface {
        @Override
        public void onCheck(int which, String passWord) {

            if (which == CheckPassWordDialogInterface.PASSWORD_RIGHT) {

                if (mBuilder.getPasswordTitle().equals(mSourceLockTitle)) {
                    halApi.setPwdStatus(halApi.EnumLockType.SOURCE_LOCK_TYPE, true);
                    mMsgString[TIPMSG_SOURCE_LOCK].mMsgShow = false;
                    mMsgString[TIPMSG_PROGRAM_LOCK_TIP].mMsgShow = false;

                } else if (mBuilder.getPasswordTitle().equals(mProgLockTitle)) {
                    LogTool.d(LogTool.MCHANNEL, "check PC success");
                    halApi.setPwdStatus(halApi.EnumLockType.PROGRAM_LOCK_TYPE, true);
                    mMsgString[TIPMSG_PROGRAM_LOCK_DIALOG].mMsgShow = false;
                    mMsgString[TIPMSG_PROGRAM_LOCK_TIP].mMsgShow = false;

                } else if (mBuilder.getPasswordTitle().equals(mParentalRatingtitle)) {
                    halApi.setPwdStatus(halApi.EnumLockType.PARENTAL_LOCK_TYPE, true);
                    mMsgString[TIPMSG_PARENTAL_RATING].mMsgShow = false;
                    mMsgString[TIPMSG_PROGRAM_LOCK_TIP].mMsgShow = false;
                    if (mPvrActivity != null) {
                        mPvrActivity.mediaPlayPause();
                        mPvrActivity.showMediaController();
                        mPvrActivity.setmPvrPCBackgroud(false);
                    }
                }
            }
        }
    }
    public class CheckPassWordListener implements CheckPassWordDialogInterface
    {
        @Override
        public void onCheck(int which, String passWord)
        {

            if (which == CheckPassWordDialogInterface.PASSWORD_RIGHT)
            {
                int sourceId = mMainActivity.mCurSourceId;
                Channel channel = mChnHistory.getCurrentChn(sourceId);
                if (mBuilder.getPasswordTitle().equals(mSourceLockTitle))
                {
                    halApi.setPwdStatus(halApi.EnumLockType.SOURCE_LOCK_TYPE, true);
                    mMsgString[TIPMSG_SOURCE_LOCK].mMsgShow = false;
                    mMsgString[TIPMSG_PROGRAM_LOCK_TIP].mMsgShow = false;
                    mMainActivity.playChannel(mChnHistory.getCurrentList(sourceId), channel, false);
                }
                else if (mBuilder.getPasswordTitle().equals(mProgLockTitle))
                {
                    LogTool.d(LogTool.MCHANNEL, "check PC success");
                    halApi.setPwdStatus(halApi.EnumLockType.PROGRAM_LOCK_TYPE, true);
                    mMsgString[TIPMSG_PROGRAM_LOCK_DIALOG].mMsgShow = false;
                    mMsgString[TIPMSG_PROGRAM_LOCK_TIP].mMsgShow = false;
                    if (sourceId == halApi.EnumSourceIndex.SOURCE_ATSC) {//add ATSC lock
                        mMainActivity.showVideo(true);
                    }
                    mMainActivity.resumeResourceAfterUnlock();
                    mMainActivity.playChannel(mChnHistory.getCurrentList(sourceId), channel, false);
                }
                else if (mBuilder.getPasswordTitle().equals(mParentalRatingtitle))
                {
                    halApi.setPwdStatus(halApi.EnumLockType.PARENTAL_LOCK_TYPE, true);
                    if(halApi.isATVSource(sourceId) && halApi.getCcEnable() != 0)
                    {
                        halApi.showCc(true);
                    }
                    mMsgString[TIPMSG_PARENTAL_RATING].mMsgShow = false;
                    mMsgString[TIPMSG_PROGRAM_LOCK_TIP].mMsgShow = false;
                    mMainActivity.playChannel(mChnHistory.getCurrentList(sourceId),channel, false);
                }
                else
                {
                }
            }
        }
    }

    /**
     * Dialog OnKeyListener for check password Dialog.
     *
     */
    private class DialogListener implements android.content.DialogInterface.OnKeyListener
    {
        @Override
        public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2)
        {
            int action = arg2.getAction();
            if (KeyEvent.ACTION_DOWN == action)
            {
                switch (arg1)
                {
                case KeyValue.DTV_KEYVALUE_CHANNEL_UP:
                case KeyValue.DTV_KEYVALUE_DPAD_UP:
                case KeyValue.DTV_KEYVALUE_CHANNEL_DOWN:
                case KeyValue.DTV_KEYVALUE_DPAD_DOWN:
                case KeyValue.DTV_KEYVALUE_PAGEUP:
                {
                    if (mPvrActivity != null) {
                        break;
                    }
                    mMainActivity.onKeyDown(arg1, arg2);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_BACK:
                {
                    if (mPvrActivity != null) {
                        mPvrActivity.finish();
                        break;
                    }
                    arg0.dismiss();
                    boolean flag = false;
                    if(mMsgString[TIPMSG_SOURCE_LOCK].mMsgShow)
                    {
                        mMainActivity.finish();
                        flag = true;
                    }
                    mMsgString[TIPMSG_PROGRAM_LOCK_DIALOG].mMsgShow = false;
                    mMsgString[TIPMSG_PARENTAL_RATING].mMsgShow = false;
                    mMsgString[TIPMSG_SOURCE_LOCK].mMsgShow = false;
                    if(flag)
                    {
                        return true;
                    }
                    show(TIPMSG_PROGRAM_LOCK_TIP);
                    return true;
                }
                /*case KeyValue.DTV_KEYVALUE_MENU:
                {
                    return true;
                }*/
                default:
                    break;
                }
            }
            return false;
        }
    }
    public void setBookArrivingStatus(){
        halApi.setPwdStatus(halApi.EnumLockType.SOURCE_LOCK_TYPE, true);
        halApi.setPwdStatus(halApi.EnumLockType.PROGRAM_LOCK_TYPE, true);
        mMsgString[TipMsgView.TIPMSG_SOURCE_LOCK].mMsgShow = false;
        mMsgString[TipMsgView.TIPMSG_PROGRAM_LOCK_TIP].mMsgShow = false;
        mMsgString[TipMsgView.TIPMSG_PROGRAM_LOCK_DIALOG].mMsgShow = false;
        dismissAll();
    }
    public void showCurrentRate(boolean visible, String currentRate) {
        if (mBuilder != null) {
            mBuilder.setCurrentRate(visible, currentRate);
        }
    }

    public void pvrPClockshow(int MsgType) {
        boolean bNeedShow = false;
        LogTool.d(LogTool.MPLAY, "Msg View show type = " + MsgType);
        if (TIPMSG_BUTT != MsgType) {
            mMsgString[MsgType].mMsgShow = true;
        }
        mBuilder.setCurrentRate(false, null);

        if (mMsgString[TIPMSG_SOURCE_LOCK].mMsgShow) {
            mBuilder.CleanWrongMsg();
            mBuilder.setPasswordTitle(mSourceLockTitle);
            sPasswordDialog.show();
            return;
        }
        if (mMsgString[TIPMSG_PROGRAM_LOCK_DIALOG].mMsgShow) {
            mBuilder.CleanWrongMsg();
            mBuilder.setPasswordTitle(mProgLockTitle);
            sPasswordDialog.show();
            return;
        }
        if (mMsgString[TIPMSG_PARENTAL_RATING].mMsgShow) {
            mBuilder.CleanWrongMsg();
            mBuilder.setPasswordTitle(mParentalRatingtitle);
            sPasswordDialog.show();
            return;
        }

        /* 总是显示优先级更高的文字  */
        for (int i = mMsgString.length - 1; i >= 0; i--) {
            if (mMsgString[i].mMsgShow) {
                mTipTitleTextView.setText(mMsgString[i].mMsgDesc);
                bNeedShow = true;
            }
        }
        if (bNeedShow) {
            super.show();
        }
    }
    public void show(int MsgType)
    {
        boolean bNeedShow = false;
        LogTool.d(LogTool.MPLAY, "Msg View show type = " + MsgType);
        if (TIPMSG_BUTT != MsgType)
        {
            mMsgString[MsgType].mMsgShow = true;
        }
        if (mMainActivity.mCurSourceId != halApi.EnumSourceIndex.SOURCE_ATSC) {
        mBuilder.setCurrentRate(false, null);
        }
        mBuilder.setmCurrentChannel("");
        if (mMsgString[TIPMSG_SOURCE_LOCK].mMsgShow)
        {
            mBuilder.CleanWrongMsg();
            mBuilder.setPasswordTitle(mSourceLockTitle);
            sPasswordDialog.show();
            return;
        }
        if (mMsgString[TIPMSG_PROGRAM_LOCK_DIALOG].mMsgShow)
        {
            mBuilder.CleanWrongMsg();
            mBuilder.setPasswordTitle(mProgLockTitle);
            setChannelInfo();
            sPasswordDialog.show();
            return;
        }
        if (mMsgString[TIPMSG_PARENTAL_RATING].mMsgShow)
        {
            mBuilder.CleanWrongMsg();
            mBuilder.setPasswordTitle(mParentalRatingtitle);
            setChannelInfo();
            sPasswordDialog.show();
            return;
        }

        /* 总是显示优先级更高的文字  */
        for (int i = mMsgString.length - 1; i >= 0; i--)
        {
            if (mMsgString[i].mMsgShow)
            {
                mTipTitleTextView.setText(mMsgString[i].mMsgDesc);
                bNeedShow = true;
            }
        }
        if (bNeedShow)
        {
            super.show();
        }
    }

    private void setChannelInfo() {
        int sourceId = mMainActivity.mCurSourceId;
        Channel channel = mChnHistory.getCurrentChn(sourceId);
        if (channel != null) {
            mBuilder.setmCurrentChannel(channel.getChannelName());
        }
    }

    @Override
    public boolean isShow()
    {
        for (int i = 0; i < mMsgString.length; i++)
        {
            if (mMsgString[i].mMsgShow)
            {
                return true;
            }
        }
        return false;
    }

    public void hide(int MsgType)
    {
        LogTool.d(LogTool.MPLAY, "Msg View hide MsgType = " + MsgType);

        if (mMsgString[MsgType].mMsgShow)
        {
            if (mBuilder.getPasswordTitle().equals(mParentalRatingtitle))
            {
                sPasswordDialog.dismiss();
            }
        }

        mMsgString[MsgType].mMsgShow = false;
        /* 总是显示优先级更高的文字  */
        for (int i = mMsgString.length - 1; i >= 0; i--) {
            if (mMsgString[i].mMsgShow) {
                mTipTitleTextView.setText(mMsgString[i].mMsgDesc);
            } else {
                super.hide();
            }
        }
        show(TIPMSG_BUTT);
    }

    public void hideAll()
    {
        LogTool.d(LogTool.MPLAY, "Msg View all hide");
        for (int i = 0; i < mMsgString.length; i++)
        {
            mMsgString[i].mMsgShow = false;
        }
        sPasswordDialog.hide();
        super.hide();
    }

    public void dismissAll()
    {
        mMsgString[TIPMSG_PROGRAM_LOCK_DIALOG].mMsgShow = false;
        mMsgString[TIPMSG_PARENTAL_RATING].mMsgShow = false;
        mMsgString[TIPMSG_SOURCE_LOCK].mMsgShow = false;
        mMsgString[TIPMSG_NOSIGNAL].mMsgShow = false;
        mMsgString[TIPMSG_UNSUPPORT].mMsgShow = false;
        //dismiss() can release resource,but hide cannot
        //when change source we have to dismiss concurrent passwordDialog
        sPasswordDialog.dismiss();
        super.hide();
    }

    public boolean isShow(int tipmsg)
    {
        if (tipmsg >= TIPMSG_BUTT)
        {
            return false;
        }
        return mMsgString[tipmsg].mMsgShow;
    }
}
