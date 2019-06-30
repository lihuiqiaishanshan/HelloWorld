package com.hisilicon.tvui.record;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.dtv.book.BookManager;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.book.EnTaskCycle;
import com.hisilicon.dtv.book.EnTaskType;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.si.TimeManager;
import com.hisilicon.dtv.pc.ParentalControlManager;
import com.hisilicon.dtv.play.EnPlayStatus;
import com.hisilicon.dtv.play.EnStopType;
import com.hisilicon.dtv.pvrfileplay.PVRFilePlayer;
import com.hisilicon.dtv.pvrfileplay.Resolution;
import com.hisilicon.dtv.record.PVREncryption;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.epg.BookDialog;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.play.TipMsgView;
import com.hisilicon.tvui.pvr.PvrActivity;
import com.hisilicon.tvui.pvr.RecordPlayStatus;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.DeviceInformation;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.MyToast;


public class RecordingListActivity extends BaseActivity
{
    private Context mContext = null;

    private LinearLayout mRecordedLinearLayout = null;

    private LinearLayout mRecordedKeyLinearLayout = null;

    private LinearLayout mBookListLinearLayout = null;

    private LinearLayout mBookListKeyLinearLayout = null;

    private LinearLayout mRecordingListRadioBackgroud = null;

    private ListView mRecordedListView = null;

    private ListView mBookListView = null;

    private SurfaceView mPlaySurfaceView = null;

//    private ProgressBar mProgressBar = null;

    private TextView mRecordedTextView = null;

    private TextView mBookRecordTextView = null;

    private TextView mBookViewTextView = null;

    private TextView mUsableSpaceTextView = null;

//    private TextView mTotalSpaceTextView = null;

    private TextView mPlayTipTextView = null;

    private TextView mNameTextView = null;

    private TextView mDurationTextView = null;

    private TextView mResolutionTextView = null;

    private TextView mSizeTextView = null;

    private ImageView mLock = null;

    private AlertDialog mSureToDelDialog;

    private AlertDialog mSureToDelAllDialog;

    private View mSureToDelDialogView;

    private View mSureToDelDialogAllView;

    private ProgressDialog mPlayProgressDialog = null;

    private ProgressDialog mDeleteAllTasksProgressDialog = null;

    private static String mRecordedFileDir = null;

    private TimeManager mTimeManager = null;

    public static final ArrayList<RecordedFile> mRecordedFileList = new ArrayList<RecordedFile>();

    private RecordedListAdapter mRecordedListAdapter = null;

    public RecordedFile mSelectedRecordedFile = null;

    private ArrayList<BookTask> mBookTaskList = null;

    private BookTask mSelectedBookTask = null;

    private int mSelectedBookTaskIndex = -1;

    private EditText mTempRequestfocuse = null;

    public MediaPlayerStautsEnum mMediaPlayerStautsEnum = MediaPlayerStautsEnum.ENDED;

    private RecordingListActivity mRecordingListActivity = null;

    private RecordingListStatusEnum mRecordingListStatusEnum = RecordingListStatusEnum.RECORDED;

    private RecordingListOptionDialog mRecordingListOptionDialog = null;

    private BookManager mBookManager = null;

    private PVRFilePlayer mPvrPlayer = null;

    private static final int SHOW_LOADING_TIP = 0;

    private static final int SHOW_LOADING_ERROR_TIP = 1;

    private static final int SHOW_START_ERROR_TIP = 2;

    private static final int SHOW_PLAY_STATUS_ERROR_TIP = 3;

    private static final int CLEAR_TIP = 4;

    private static final int UPDATE_DURATION = 5;

    private static final int UPDATE_RESOLUTION = 6;

    private static final int PLAY_RECORDED_FILE = 7;

    private static final int SWITCH_GROUP = 8;

    private static final int DO_OPTION_RENAME = 9;

    private static final int DO_OPTION_DELETE = 10;

    private static final int DO_OPTION_DELETE_ALL = 11;

    private static final int UPDATE_BOOKLIST = 12;

    private static final int SHOW_PLAY_DIALOG = 13;

    private static final int DISMISS_PLAY_DIALOG = 14;

    private static final int UPDATE_SPACE_INFO = 15;

    private static final int UPDATE_REC_FILE_LIST = 16;

    private boolean mIsSwitchGroup = false;

    private boolean mCanPlay = true;

    private static final long PLAY_FILTER_TIME = 1000;

    private static final long GROUP_FILTER_TIME = 100;

    private static final int RESUME_KEYRESPONSE_DELAY_TIME = 2000;

    private long mResumeTime = 0;

    private int mLastRecordListViewPosition = 0;

    private boolean mGotoFullSreenPlay = false;

    private PVREncryption mPvrEncryption;

    IDTVListener mDTVListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            LogTool.d(LogTool.MSERVICE, "IDTVListener.notifyMessage(" + messageID + "," + param1 + "," + parm2 + "," + obj.toString() + ")");
            switch (messageID)
            {
            case DTVMessage.HI_SVR_EVT_BOOK_TIME_END:
            {
                LogTool.d(LogTool.MREC, "IDTVListener HI_SVR_EVT_BOOK_TIME_END");
                BookTask bookTask = mBookManager.getTaskByID(param1);
                if (bookTask != null)
                {
                    EnTaskCycle enTaskCycle = bookTask.getCycle();
                    if ((null != enTaskCycle) && (EnTaskCycle.ONETIME == enTaskCycle))
                    {
                        try
                        {
                            Thread.sleep(3000);
                        }
                        catch (InterruptedException e)
                        {
                            LogTool.d(LogTool.MREC, e.toString());
                        }
                        updateBookListView();
                    }
                }
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR:
            case DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF:
            {
                mPvrPlayer.stop();
                mPvrPlayer.close();
                break;
            }
                case DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING: {//start parental
                    if(checkPvrPCLock()){
                        mPvrPlayer.stop();
                        mPvrPlayer.close();
                        mLock.setVisibility(View.VISIBLE);
                    }
                }
                break;
            default:
            {
                break;
            }
            }
        }
    };

    public static ArrayList<RecordedFile> GetRecordedFileList()
    {
        return mRecordedFileList;
    }

    /**
     * Define UI View update Handler object.<br>
     */
    private UIUpdateHandler mUIUpdateHandler = new UIUpdateHandler(this);

    /**
     * Defined Handler class used to update UI View.<br>
     *
     * @author z00209628
     * @see RecordingListActivity
     */
    private static class UIUpdateHandler extends Handler
    {
        WeakReference<RecordingListActivity> mActivity;

        public UIUpdateHandler(RecordingListActivity activity)
        {
            this.mActivity = new WeakReference<RecordingListActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            RecordingListActivity theActivity = mActivity.get();
            super.handleMessage(msg);
            switch (msg.what)
            {
            case SHOW_LOADING_TIP:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:SHOW_LOADING_TIP");
                theActivity.showLoadingTip();
                break;
            }
            case SHOW_LOADING_ERROR_TIP:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:SHOW_LOADING_ERROR_TIP");
                theActivity.showLoadingErrorTip();
                break;
            }
            case SHOW_START_ERROR_TIP:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:SHOW_START_ERROR_TIP");
                theActivity.showStartErrorTip();
                theActivity.mDurationTextView.setText("");
                theActivity.mResolutionTextView.setText("");
                if (null != theActivity.mSelectedRecordedFile)
                {
                    theActivity.mSizeTextView.setText(theActivity.mSelectedRecordedFile.getSize());
                }
                break;
            }
            case SHOW_PLAY_STATUS_ERROR_TIP:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:SHOW_PLAY_STATUS_ERROR_TIP");
                theActivity.showPlayStatusErrorTip();
                break;
            }
            case CLEAR_TIP:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:CLEAR_TIP");
                theActivity.clearTip();
                break;
            }
            case UPDATE_DURATION:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:UPDATE_DURATION");
                theActivity.initDuration();
                break;
            }
            case UPDATE_RESOLUTION:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:UPDATE_RESOLUTION");
                theActivity.initResolution();
                theActivity.initFileSize();
                break;
            }
            case SWITCH_GROUP:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:SWITCH_GROUP");
                theActivity.releasePlayer();
                theActivity.switchGroup();
                break;
            }
            case PLAY_RECORDED_FILE:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:PLAY_RECORDED_FILE");
                theActivity.initNameDurationResolutionSize();
                theActivity.playSelectedRecordedFile();
                break;
            }
            case DO_OPTION_RENAME:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:DO_OPTION_RENAME");
                theActivity.updateAdapterWhileRename();
                theActivity.playSelectedRecordedFile();
                theActivity.initDuration();
                theActivity.initResolution();
                theActivity.initFileSize();
                break;
            }
            case DO_OPTION_DELETE:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:DO_OPTION_DELETE");
                theActivity.updateAdapterWhileDelete();
                break;
            }
            case DO_OPTION_DELETE_ALL:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:DO_OPTION_DELETE_ALL");
                theActivity.updateAdapterWhileDeleteAll();
                break;
            }
            case UPDATE_BOOKLIST:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:UPDATE_BOOKLIST");
                theActivity.updateBookListView();
                break;
            }
            case SHOW_PLAY_DIALOG:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:SHOW_PLAY_DIALOG");
                theActivity.showPlayDialog();
                break;
            }
            case DISMISS_PLAY_DIALOG:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:DISMISS_PLAY_DIALOG");
                theActivity.dismissPlayDialog();
                break;
            }
            case UPDATE_SPACE_INFO:
            {
                LogTool.d(LogTool.MEPG, "UIUpdate HandleMessage:UPDATE_SPACE_INFO");
                long usableSpace = 0;
                Bundle spaceDataBundle = msg.getData();
                usableSpace = spaceDataBundle.getLong("usableSpace");
                theActivity.mUsableSpaceTextView.setText(theActivity.getFileSize(usableSpace));

                //String totalTitle = theActivity.getResources().getString(R.string.recording_list_total);
                //theActivity.mTotalSpaceTextView.setText(totalTitle + theActivity.getFileSize(totalSpace));

//                    float progress = ((float) uesedSpace) / totalSpace;
//                    LogTool.d(LogTool.MREC, "progress = " + progress);
//                    progress *= 100;
//                    theActivity.mProgressBar.setProgress((int) progress);
                break;
            }
            case UPDATE_REC_FILE_LIST:
            {
                theActivity.setRecordedListViewAdapter();
                break;
            }
            default:
            {
                break;
            }
            }
        }

    }

    private void initPVREncryption() {
        mPvrEncryption = new PVREncryption(PVREncryption.PVR_ENCRYPTION_TYPE_AES, DeviceInformation.getDeviceMac());
    }

    private void showLoadingTip()
    {
        mPlayTipTextView.setText(getResources().getString(R.string.recording_list_play_loading));
    }

    private void showLoadingErrorTip()
    {
        mPlayTipTextView.setText(getResources().getString(R.string.recording_list_play_loading_error));
    }

    private void showStartErrorTip()
    {
        mPlayTipTextView.setText(getResources().getString(R.string.recording_list_play_start_error));
    }

    private void showPlayStatusErrorTip()
    {
        mPlayTipTextView.setText(getResources().getString(R.string.recording_list_play_status_error));
    }

    private void clearTip()
    {
        mPlayTipTextView.setText("");
    }

    private void initNameDurationResolutionSize()
    {
        if (null != mSelectedRecordedFile)
        {
            mNameTextView.setText(mSelectedRecordedFile.getName());
            //           mSizeTextView.setText(mSelectedRecordedFile.getSize());
        }
        else
        {
            mNameTextView.setText("");
            mSizeTextView.setText("");
            mDurationTextView.setText("");
            mResolutionTextView.setText("");
        }
    }

    public void releasePlayer()
    {
        if (null != mPvrPlayer)
        {
            mPvrPlayer.stop();
            mPvrPlayer.close();
        }
    }

    private void initDuration()
    {
        //       mDurationTextView.setText(mSelectedRecordedFile.getDuration());
        int duration = mPvrPlayer.getPVRFileInfo().getDuration();
        //duration = (duration % 1000 > 500) ? (duration / 1000 + 1) : (duration / 1000);
        if (duration <= 0) {
            mDurationTextView.setText("");
        } else {
            int second = duration % 60;
            duration = duration / 60;
            int minute = duration % 60;
            duration = duration / 60;
            int hour = duration;
            mDurationTextView.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second));
        }
    }

    private void initResolution()
    {
//        mResolutionTextView.setText(mSelectedRecordedFile.getResolution());
        Resolution resolution = mPvrPlayer.getPVRFileInfo().getVideoResolution();
        if (0 != resolution.width)
        {
            mResolutionTextView.setText(resolution.width + "*" + resolution.height);
        }
        else
        {
            mResolutionTextView.setText(" ");
        }
    }

    private void initFileSize()
    {
        long size = mPvrPlayer.getPVRFileInfo().getSize();
        if (size < 0) {
            mSizeTextView.setText("");
        } else {
            mSizeTextView.setText(getFileSize(size));
        }
    }

    private void switchGroup()
    {
        if (RecordingListStatusEnum.RECORDED == mRecordingListStatusEnum)
        {
            mRecordedLinearLayout.setVisibility(View.VISIBLE);
            mBookListLinearLayout.setVisibility(View.GONE);

            mRecordedKeyLinearLayout.setVisibility(View.VISIBLE);
            mBookListKeyLinearLayout.setVisibility(View.GONE);

            setRecordedListViewAdapter();
        }
        else
        {
            mRecordedLinearLayout.setVisibility(View.GONE);
            mBookListLinearLayout.setVisibility(View.VISIBLE);

            mRecordedKeyLinearLayout.setVisibility(View.GONE);
            mBookListKeyLinearLayout.setVisibility(View.VISIBLE);

            updateBookListView();
        }

    }

    private void setRecordedListViewAdapter()
    {
        mRecordedListView.setAdapter(mRecordedListAdapter);

        mRecordedListView.requestFocus();

        if (null != mRecordedFileList)
        {
            mLastRecordListViewPosition = mLastRecordListViewPosition > mRecordedListView.getCount() ? mRecordedListView.getCount()
                    : mLastRecordListViewPosition;
            LogTool.d(LogTool.MREC, "mRecordedListView.setSelection(0)");
            mRecordedListView.setSelection(mLastRecordListViewPosition);
        }
        else if (RecordingListStatusEnum.RECORDED == mRecordingListStatusEnum)
        {
            /*
            Toast.makeText(
                    mContext,
                    mContext.getResources().getString(
                            R.string.recording_list_play_recorded_file_list_no_resource),
                    MyToast.LENGTH_SHORT).show();
                    */
            mRecordingListActivity.mSelectedRecordedFile = null;
            initNameDurationResolutionSize();
        }
    }

    private ArrayList<BookTask> getBookTaskList(EnTaskType enTaskType)
    {
        ArrayList<BookTask> allBookTaskList = (ArrayList<BookTask>) mBookManager.getAllTasks();
        ArrayList<BookTask> bookTaskList = new ArrayList<BookTask>();
        if (null == allBookTaskList)
        {
            return null;
        }

        int size = allBookTaskList.size();
        for (int i = 0; i < size; i++)
        {
            BookTask bookTask = allBookTaskList.get(i);
            if (null == bookTask)
            {
                continue;
            }

            EnTaskType bookTaskType = bookTask.getType();
            if (null == bookTaskType)
            {
                continue;
            }

            if (enTaskType == bookTaskType)
            {
                //BookTask tmp = bookTask;
                //Date start = tmp.getStartDate();
                //start.setTime(start.getTime() + mTimeManager.getTimeZone() * 1000);
                //tmp.setStartDate(start);
                bookTaskList.add(bookTask);
            }
        }

        return bookTaskList;
    }

    public void sendUpdateAdapterWhileRename()
    {
        mUIUpdateHandler.sendEmptyMessage(DO_OPTION_RENAME);
    }

    public void sendUpdateAdapterWhileDelete()
    {
        mUIUpdateHandler.sendEmptyMessage(DO_OPTION_DELETE);
    }

    public void sendUpdateAdapterWhileDeleteAll()
    {
        mUIUpdateHandler.sendEmptyMessage(DO_OPTION_DELETE_ALL);
    }

    private void updateAdapterWhileRename()
    {
        mRecordedListAdapter.notifyDataSetChanged();
        initNameDurationResolutionSize();
    }

    private void updateAdapterWhileDelete()
    {
        if (null != mRecordedFileList)
        {
            mRecordedFileList.remove(mSelectedRecordedFile);

            mRecordedListAdapter = null;

            mRecordedListAdapter = new RecordedListAdapter(mContext, mRecordedFileList);

            mRecordedListView.setAdapter(mRecordedListAdapter);

            if (mRecordedFileList.isEmpty())
            {
                mSelectedRecordedFile = null;
                initNameDurationResolutionSize();
            }
        }

        mRecordingListActivity.initSpaceTextView();
    }

    private void updateAdapterWhileDeleteAll()
    {
        mRecordingListActivity.initSpaceTextView();

        mRecordedFileList.clear();

        mSelectedRecordedFile = null;

        initNameDurationResolutionSize();

        mRecordedListAdapter.notifyDataSetChanged();
    }

    private void showPlayDialog()
    {
        String title = mContext.getResources().getString(R.string.recording_list_play_loading);
        String tip = mContext.getResources().getString(R.string.recording_list_play_waitting);

        mPlayProgressDialog = ProgressDialog.show(mContext, title, tip);
    }

    private void dismissPlayDialog()
    {
        if (null != mPlayProgressDialog)
        {
            mPlayProgressDialog.dismiss();

            mPlayProgressDialog = null;
        }
    }

    /**
     * Dismiss player window tip view while enter EPG window.<br>
     */
    private void dismissPlayerTipView()
    {
        LogTool.d(LogTool.MEPG, "dismissPlayerTipView()");
        Intent dissmissIntent = new Intent(CommonValue.DTV_INTENT_DISMISS_TIP);
        //this.sendBroadcast(dissmissIntent);
        CommonDef.sendBroadcastEx(RecordingListActivity.this, dissmissIntent);
    }

    private void initDTV()
    {
        mBookManager = mDTV.getBookManager();

        mTimeManager = mDTV.getNetworkManager().getTimeManager();

        if (null != mPlayerManager)
        {
            // Gets existed player object and create new one while null
            if (!mPlayerManager.getPlayers().isEmpty())
            {
                mPlayer = mPlayerManager.getPlayers().get(0);
                if (null == mPlayer)
                {
                    mPlayer = mPlayerManager.createPlayer();
                }
            }
        }

        mPvrPlayer = mDTV.getPVRFilePlayer();
    }

    private void initRecordedFilePath()
    {
        mRecordedFileDir = mDtvConfig.getString(CommonValue.RECORD_PATH, CommonValue.DEFAULT_RECORD_PATH);
        if (null == mRecordedFileDir)
        {
            mRecordedFileDir = CommonValue.DEFAULT_RECORD_PATH;
        }
        LogTool.d(LogTool.MREC, "initRecordedFilePath mRecordedFileDir = " + mRecordedFileDir);
    }

    @SuppressLint("InflateParams")
    private void initViewById()
    {
        mRecordedTextView = (TextView) findViewById(R.id.tv_recording_list_recorded_title);
        mBookRecordTextView = (TextView) findViewById(R.id.tv_recording_list_book_record_title);
        mBookViewTextView = (TextView) findViewById(R.id.tv_recording_list_book_view_title);
        mUsableSpaceTextView = (TextView) findViewById(R.id.tv_recorded_usable_space);
//        mTotalSpaceTextView = (TextView) findViewById(R.id.tv_recorded_total_space);
//        mProgressBar = (ProgressBar) findViewById(R.id.pb_recorded_used_total_space);
        mPlaySurfaceView = (SurfaceView) findViewById(R.id.sv_recording_list_play_area);
        mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mTempRequestfocuse = (EditText) findViewById(R.id.edt_temp_focuse);

        // Transparent
        mTempRequestfocuse.setAlpha(0);
        mTempRequestfocuse.setBackgroundColor(0);
        mTempRequestfocuse.setEnabled(false);
        mTempRequestfocuse.setFocusable(false);
        mPlayTipTextView = (TextView) findViewById(R.id.tv_recording_list_play_tip);
        mPlayTipTextView.setVisibility(View.VISIBLE);
        mNameTextView = (TextView) findViewById(R.id.tv_recording_list_information_name);
        mDurationTextView = (TextView) findViewById(R.id.tv_recording_list_information_duration);
        mResolutionTextView = (TextView) findViewById(R.id.tv_recording_list_information_resolution);
        mSizeTextView = (TextView) findViewById(R.id.tv_recording_list_information_size);
        mBookListView = (ListView) findViewById(R.id.lv_recording_list_book_list);
        mRecordedListView = (ListView) findViewById(R.id.lv_recording_list_recorded);
        mRecordedLinearLayout = (LinearLayout) findViewById(R.id.llay_recordinglist_recorded);
        mRecordedLinearLayout.setVisibility(View.VISIBLE);
        mRecordedKeyLinearLayout = (LinearLayout) findViewById(R.id.llay_recording_list_recorded_key);
        mRecordedKeyLinearLayout.setVisibility(View.VISIBLE);
        mBookListLinearLayout = (LinearLayout) findViewById(R.id.llay_recordinglist_book_list);
        mBookListLinearLayout.setVisibility(View.GONE);
        mBookListKeyLinearLayout = (LinearLayout) findViewById(R.id.llay_recording_list_book_list_key);
        mBookListKeyLinearLayout.setVisibility(View.GONE);
        mRecordingListRadioBackgroud = (LinearLayout) findViewById(R.id.recordinglist_radio_bg);
        mSureToDelDialogView = LayoutInflater.from(this).inflate(R.layout.book_sure_to_delete_dialog, null);
        mSureToDelDialogAllView = LayoutInflater.from(this).inflate(R.layout.book_sure_to_delete_all_dialog, null);
        mLock = (ImageView) findViewById(R.id.pvr_icon_lock);
        switchToRecorded();
        //initSurfaceHolder();
        initBookListView();

    }

    private void registerUSBReceiver()
    {
        IntentFilter usbIntentFilter = new IntentFilter();
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbIntentFilter.addDataScheme("file");
        registerReceiver(mUsbBroadCastReceiver, usbIntentFilter);
    }

    private void initSpaceTextView()
    {
        File fileDir = new File(mRecordedFileDir);
        if ((null == fileDir) || (!fileDir.isDirectory()))
        {
            return;
        }

        long freeSpace = fileDir.getFreeSpace();
        LogTool.d(LogTool.MREC, "initSpaceTextView freeSpace = " + freeSpace);

        long usableSpace = fileDir.getUsableSpace();
        LogTool.d(LogTool.MREC, "initSpaceTextView usableSpace = " + usableSpace);

        long totalSpace = fileDir.getTotalSpace();
        LogTool.d(LogTool.MREC, "initSpaceTextView totalSpace = " + totalSpace);

        Message msgUpdateSpace = new Message();
        Bundle dataSpace = new Bundle();
        msgUpdateSpace.what = UPDATE_SPACE_INFO;
        dataSpace.putLong("usableSpace", usableSpace);
        dataSpace.putLong("totalSpace", totalSpace);
        msgUpdateSpace.setData(dataSpace);
        mUIUpdateHandler.sendMessage(msgUpdateSpace);
    }

    private void initBookListView()
    {
        mBookListView.setItemsCanFocus(true);
        mBookListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mBookListView.setVisibility(View.VISIBLE);
        mBookListView.setFocusableInTouchMode(true);
        mBookListView.setFocusable(true);
        mBookListView.requestFocus();

        mBookListView.setOnItemClickListener(mBookListClickListener);

        mBookListView.setOnItemSelectedListener(mBookListSelectedListener);
    }

    private OnItemClickListener mBookListClickListener = new OnItemClickListener()
    {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            LogTool.d(LogTool.MREC, "mBookListClickListener onItemClick");
        }

    };

    private OnItemSelectedListener mBookListSelectedListener = new OnItemSelectedListener()
    {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
        {
            LogTool.d(LogTool.MREC, " mBookListSelectedListener onItemSelected(i = " + i + ")");
            mSelectedBookTaskIndex = i;

            mSelectedBookTask = (BookTask) adapterView.getAdapter().getItem(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView)
        {
            LogTool.d(LogTool.MREC, " mBookListSelectedListener onNothingSelected");

        }

    };

    private void initRecordedListView()
    {
        mRecordedListView.setItemsCanFocus(true);
        mRecordedListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mRecordedListView.setVisibility(View.VISIBLE);
        mRecordedListView.setFocusableInTouchMode(true);
        mRecordedListView.setFocusable(true);
        mRecordedListView.requestFocus();

        mRecordedListView.setOnItemClickListener(mRecordedListClickListener);

        mRecordedListView.setOnItemSelectedListener(mRecordedListSelectedListener);

        initRecordedListViewAdapter();
    }

    private void initRecordedListViewAdapter() {
        TaskUtil.post(new Runnable() {
            public void run() {
                Message msgUpdateRecordFileList = new Message();
                initRecordedFileList();
                mRecordedListAdapter = new RecordedListAdapter(mContext, mRecordedFileList);
                msgUpdateRecordFileList.what = UPDATE_REC_FILE_LIST;
                mUIUpdateHandler.sendMessage(msgUpdateRecordFileList);
            }
        });
    }

    private void initRecordedFileList()
    {
        mRecordedFileList.clear();

        mRecordedFileDir = mDtvConfig.getString(CommonValue.RECORD_PATH, CommonValue.DEFAULT_RECORD_PATH);
        if (null == mRecordedFileDir)
        {
            mRecordedFileDir = CommonValue.DEFAULT_RECORD_PATH;
        }
        ArrayList<RecordedFile> tmpList = getRecordedFiles(mRecordedFileDir);

        if (null != tmpList)
        {
            mRecordedFileList.addAll(tmpList);
        }
    }

    /* Sort files by the time in file name. */
    static class CompratorByLastModified implements Comparator<File>
    {
        /* eg.CCTV1-20141017_16-27-00.ts
         * dateStartIndex = length("20141017_16-27-00.ts") */
        private static final int dateStartIndex = 20;

        @Override
        public int compare(File f1, File f2)
        {
            if (f1.getName().length() <= dateStartIndex)
            {
                return 1;
            }
            if (f2.getName().length() <= dateStartIndex)
            {
                return -1;
            }
            String filedate1 = f1.getName().substring(f1.getName().length() - dateStartIndex);
            String filedate2 = f2.getName().substring(f2.getName().length() - dateStartIndex);
            int diff = filedate2.compareTo(filedate1);
            return diff;
        }

        @Override
        public boolean equals(Object obj)
        {
            return true;
        }
    }

    static class MyFilter implements FilenameFilter
    {
        private String type;

        public MyFilter(String type)
        {
            this.type = type;
        }

        @Override
        public boolean accept(File dir, String name)
        {
            /* 过滤掉形如 name.ts.0001.ts 的分片文件  */
            if (name.matches("(.*).ts.[0-9][0-9][0-9][0-9].ts"))
                return false;
            return name.endsWith(type);
        }
    }

    private ArrayList<RecordedFile> getRecordedFiles(String filePath)
    {
        File fileDir = new File(filePath);
        if ((null == fileDir) || (!fileDir.isDirectory()))
        {
            return null;
        }

        MyFilter filter = new MyFilter(".ts");
        File[] files = fileDir.listFiles(filter);
        if ((null == files) || (files.length <= 0))
        {
            return null;
        }

        Arrays.sort(files, new CompratorByLastModified());

        ArrayList<RecordedFile> list = new ArrayList<RecordedFile>();
        for (int i = 0; i < files.length; i++)
        {
//            LogTool.d(LogTool.MREC, fileDir.getName() + "/" + files[i].getName());
            String name = files[i].getName();

//            LogTool.d(LogTool.MREC, "name = " + name);
            RecordedFile recordedFile = new RecordedFile();
            recordedFile.setDir(mRecordedFileDir);
            recordedFile.setName(name);

            Date date = new Date(files[i].lastModified());

            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd/HH:mm", Locale.getDefault());
//            LogTool.d(LogTool.MREC, "date = " + format.format(date));
            recordedFile.setDate(format.format(date));
            String fileSize = getFileSize(files[i].length());
            recordedFile.setSize(fileSize);
            list.add(recordedFile);
        }

        if (list.isEmpty())
        {
            list = null;
        }
        return list;
    }

    private String getFileSize(long length)
    {
        String fileSize = null;

        DecimalFormat format = new DecimalFormat("#.##");

        if (0 == (length / 1024))
        {
            float size = (float) length;
            fileSize = format.format(size) + "B";
        }
        else if (0 == (length / (1024 * 1024)))
        {
            float size = ((float) length) / 1024;
            fileSize = format.format(size) + "K";
        }
        else if (0 == (length / (1024 * 1024 * 1024)))
        {
            float size = ((float) length) / (1024 * 1024);
            fileSize = format.format(size) + "M";
        }
        else
        {
            float size = ((float) length) / (1024 * 1024 * 1024);
            fileSize = format.format(size) + "G";
        }

//        LogTool.d(LogTool.MREC, "fileSize = " + fileSize);
        return fileSize;
    }

    private OnItemClickListener mRecordedListClickListener = new OnItemClickListener()
    {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            LogTool.d(LogTool.MREC, "mRecordedListClick onItemClick ");
            mCanPlay = false;
            mGotoFullSreenPlay = true;
            if (null != mSelectedRecordedFile)
            {
                //int version = android.os.Build.VERSION.SDK_INT;
                //LogTool.d(LogTool.MREC, "version = " + version);
                releasePlayer();
                Intent intent = new Intent(RecordingListActivity.this, PvrActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("VideoPath", mSelectedRecordedFile.getFilePath());
                LinearLayout mAllLayout = (LinearLayout) findViewById(R.id.llay_recording_list);
                mAllLayout.setVisibility(View.INVISIBLE);
                CommonDef.startActivityEx(RecordingListActivity.this, intent);
            }
        }

    };

    private OnItemSelectedListener mRecordedListSelectedListener = new OnItemSelectedListener()
    {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
        {
            LogTool.d(LogTool.MREC, "mRecordedListSelected onItemSelected(i = " + i + ")");

            mSelectedRecordedFile = (RecordedFile) adapterView.getAdapter().getItem(i);

            mSelectedRecordedFile.setPosition(i);

            sendPlayRecordedFileMessage();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView)
        {
            LogTool.d(LogTool.MREC, "mRecordedListSelected onNothingSelected");

        }

    };

    private void sendPlayRecordedFileMessage()
    {
        if (mUIUpdateHandler.hasMessages(PLAY_RECORDED_FILE))
        {
            mUIUpdateHandler.removeMessages(PLAY_RECORDED_FILE);
        }

        if (mCanPlay)
        {
            mUIUpdateHandler.sendEmptyMessageDelayed(PLAY_RECORDED_FILE, PLAY_FILTER_TIME);
        }
    }

    public void playSelectedRecordedFile()
    {
        mLock.setVisibility(View.GONE);
        if (!mCanPlay || (null == mSelectedRecordedFile))
        {
            return;
        }
        mPvrPlayer.stop();
        mPvrPlayer.close();
        LogTool.d(LogTool.MREC, "playFilePath = " + mSelectedRecordedFile.getFilePath());
        File playFile = new File(mSelectedRecordedFile.getFilePath());
        if (!(playFile.isFile() && playFile.exists()))
        {
            return;
        }
        if (null == mPlayProgressDialog)
        {
            mUIUpdateHandler.sendEmptyMessage(SHOW_PLAY_DIALOG);

            mUIUpdateHandler.sendEmptyMessage(SHOW_LOADING_TIP);
        }
        mPvrPlayer.open();

        if (0 == mPvrPlayer.start(mSelectedRecordedFile.getFilePath(), mPvrEncryption)) {
            LogTool.d(LogTool.MREC, "mPvrPlayer.start OK");

            if (mPvrPlayer.getPVRFileInfo().isRadio()) {
                mRecordingListRadioBackgroud.setVisibility(View.VISIBLE);
                mUIUpdateHandler.sendEmptyMessage(UPDATE_RESOLUTION);
            } else {
                mRecordingListRadioBackgroud.setVisibility(View.INVISIBLE);
            }

            Rect rect = new Rect();
            int location[] = new int[2];
            mPlaySurfaceView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + mPlaySurfaceView.getWidth();
            rect.bottom = rect.top + mPlaySurfaceView.getHeight();

            mPvrPlayer.setWindowRect(rect);

            mUIUpdateHandler.sendEmptyMessage(DISMISS_PLAY_DIALOG);
            mUIUpdateHandler.sendEmptyMessage(CLEAR_TIP);
            mUIUpdateHandler.sendEmptyMessage(UPDATE_DURATION);
            mUIUpdateHandler.sendEmptyMessage(UPDATE_RESOLUTION);
            mUIUpdateHandler.sendEmptyMessageDelayed(UPDATE_RESOLUTION, 2000);
            mUIUpdateHandler.sendEmptyMessageDelayed(UPDATE_DURATION, 2000);
            mUIUpdateHandler.sendEmptyMessageDelayed(UPDATE_RESOLUTION, 4000);
            mUIUpdateHandler.sendEmptyMessageDelayed(UPDATE_DURATION, 4000);
        } else {
            LogTool.d(LogTool.MREC, "mPvrPlayer.start NO");
            mUIUpdateHandler.sendEmptyMessage(DISMISS_PLAY_DIALOG);
            mUIUpdateHandler.sendEmptyMessage(SHOW_START_ERROR_TIP);
        }

    }

    @Override
    protected void onCreate(Bundle bundle)
    {
        LogTool.d(LogTool.MREC, "onCreate in");
        dismissPlayerTipView();
        super.onCreate(bundle);
        setContentView(R.layout.recording_list);
        this.mContext = this;
        this.mRecordingListActivity = this;
        initPVREncryption();
        initDTV();
        initRecordedFilePath();
        initViewById();
        //registerUSBReceiver();
        TaskUtil.post(new Runnable() {
            public void run() {
                releaseDTVPlayer();
                initSpaceTextView();
            }
        });
        LogTool.d(LogTool.MREC, "onCreate out");
    }

    @Override
    protected void onStart()
    {
        LogTool.d(LogTool.MREC, "onStart()");

        //pause subtitle
        if (null != mPvrPlayer)
        {
            mPvrPlayer.pauseSubtitle();
        }

        super.onStart();
    }

    @Override
    protected void onResume()
    {
        LogTool.d(LogTool.MREC, "onResume()");
        LinearLayout mAllLayout = (LinearLayout) findViewById(R.id.llay_recording_list);
        mAllLayout.setVisibility(View.VISIBLE);
        mResumeTime = SystemClock.elapsedRealtime();

        mCanPlay = true;
        mGotoFullSreenPlay = true;
        registerUSBReceiver();
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_TIME_END, mDTVListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF, mDTVListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR, mDTVListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING, mDTVListener, 0);

        RecordPlayStatus.getInstance().setPlaying(true);

        if (mIsSwitchGroup)
        {
            mIsSwitchGroup = false;

            mUIUpdateHandler.sendEmptyMessage(SWITCH_GROUP);
        }

        mUIUpdateHandler.sendEmptyMessage(DISMISS_PLAY_DIALOG);

        if (RecordingListStatusEnum.RECORDED == mRecordingListStatusEnum)
        {
            initRecordedListView();
        }
        else
        {
            updateBookListView();
        }

        //pause subtitle
        if (null != mPvrPlayer)
        {
            mPvrPlayer.pauseSubtitle();
        }

        LogTool.d(LogTool.MREC, "onResume() out");
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        LogTool.d(LogTool.MREC, "onPause()");

        mCanPlay = false;
        mLastRecordListViewPosition = mRecordedListView.getSelectedItemPosition();
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_TIME_END, mDTVListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF, mDTVListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR, mDTVListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING, mDTVListener);
        RecordPlayStatus.getInstance().setPlaying(false);
        if (mUsbBroadCastReceiver != null)
        {
            LogTool.d(LogTool.MPLAY, "record unregisterUSBReceiver");
            unregisterReceiver(mUsbBroadCastReceiver);
        }

        removeHandlerMessages();
        mPlayer.clearDisplay(mPlaySurfaceView.getHolder().getSurface());
        releasePlayer();

        //resume subtitle
        if (null != mPvrPlayer)
        {
            mPvrPlayer.resumeSubtitle();
        }

        super.onPause();
    }

    private void removeHandlerMessages()
    {
        if (mUIUpdateHandler.hasMessages(PLAY_RECORDED_FILE))
        {
            mUIUpdateHandler.removeMessages(PLAY_RECORDED_FILE);
        }

        if (mUIUpdateHandler.hasMessages(SWITCH_GROUP))
        {
            mUIUpdateHandler.removeMessages(SWITCH_GROUP);

            mIsSwitchGroup = true;
        }
    }

    @Override
    protected void onStop()
    {
        LogTool.d(LogTool.MREC, "onStop()");

        //resume subtitle
        if (null != mPvrPlayer)
        {
            mPvrPlayer.resumeSubtitle();
        }
        if(!mGotoFullSreenPlay){
            Intent mIntent = new Intent();
            mIntent.setAction(CommonValue.PLAY_DTV_ACTION);
            sendBroadcast(mIntent);
        }

        finish();
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        LogTool.d(LogTool.MREC, "onDestroy()");

        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_RED: {
                LogTool.d(LogTool.MEPG, "KeyValue.DTV_KEYVALUE_RED");
                if (RecordingListStatusEnum.RECORDED == mRecordingListStatusEnum) {
                    renameSelectedRecordedFile();
                } else {
                    editBookTask();
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_GREEN: {
                LogTool.d(LogTool.MEPG, "KeyValue.DTV_KEYVALUE_GREEN");
                if (RecordingListStatusEnum.RECORDED == mRecordingListStatusEnum) {
                    previewSelectedRecordedFile();
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_YELLOW: {
                LogTool.d(LogTool.MEPG, "KeyValue.DTV_KEYVALUE_YELLOW");
                if (RecordingListStatusEnum.RECORDED == mRecordingListStatusEnum) {
                    deleteSelectedRecordedFile();
                } else {
                    //deleteBookTask();
                    showSureToDelDialog();
                }
                return true;
            }
            case KeyValue.DTV_KEYVALUE_BLUE: {
                LogTool.d(LogTool.MEPG, "KeyValue.DTV_KEYVALUE_BLUE");
                if (RecordingListStatusEnum.RECORDED == mRecordingListStatusEnum) {
                    deleteAllSelectedRecordedFile();
                } else {
                    //deleteAllBookTasks();
                    showSureToDelAllDialog();
                }
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        LogTool.d(LogTool.MEPG, "onKeyDown(keyCode = " + keyCode + ")");

        if ((SystemClock.elapsedRealtime() - mResumeTime) < RESUME_KEYRESPONSE_DELAY_TIME) {
            return true;
        }

        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_BACK: {
                mCanPlay = false;
                mGotoFullSreenPlay = false;
                break;
            }
            case KeyValue.DTV_KEYVALUE_DPAD_UP: {
                LogTool.d(LogTool.MEPG, "KeyValue.DTV_KEYVALUE_DPAD_UP");

                setCircleSelectBottom();

                break;
            }
            case KeyValue.DTV_KEYVALUE_DPAD_DOWN: {
                LogTool.d(LogTool.MEPG, "KeyValue.DTV_KEYVALUE_DPAD_DOWN");

                setCircleSelectTop();

                break;
            }
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT: {
                LogTool.d(LogTool.MEPG, "KeyValue.DTV_KEYVALUE_DPAD_LEFT");
                mLastRecordListViewPosition = mRecordedListView.getSelectedItemPosition();
                switchLeftGroupView();

                sendSwitchGroupMessage();

                break;
            }
            case KeyValue.DTV_KEYVALUE_DPAD_RIGHT: {
                LogTool.d(LogTool.MEPG, "KeyValue.DTV_KEYVALUE_DPAD_RIGHT");
                mLastRecordListViewPosition = mRecordedListView.getSelectedItemPosition();
                switchRightGroupView();
                sendSwitchGroupMessage();

                break;
            }
            case KeyValue.DTV_KEYVALUE_RED: {
                return  true;
            }
            case KeyValue.DTV_KEYVALUE_GREEN: {

                return  true;
            }
            case KeyValue.DTV_KEYVALUE_YELLOW: {

                return true;
            }
            case KeyValue.DTV_KEYVALUE_BLUE: {

                return true;
            }
            case KeyValue.DTV_KEYVALUE_SWAP: {
                LogTool.d(LogTool.MEPG, "KeyValue.SWAP");
                // intercept SWAP_KEY finish RecordingActivity.
                finish();
                return false;
            }

            default: {
                break;
            }
        }

        return super.onKeyDown(keyCode, keyEvent);
    }

    private void setCircleSelectBottom()
    {
        if ((null != mRecordedListView) && (mRecordedListView.hasFocus()))
        {
            if (0 == mRecordedListView.getSelectedItemPosition())
            {
                mRecordedListView.setSelection(mRecordedListView.getCount() - 1);
            }
        }

        if ((null != mBookListView) && (mBookListView.hasFocus()))
        {
            if (0 == mBookListView.getSelectedItemPosition())
            {
                mBookListView.setSelection(mBookListView.getCount() - 1);
            }
        }
    }

    private void setCircleSelectTop()
    {
        if ((null != mRecordedListView) && (mRecordedListView.hasFocus()))
        {
            if (mRecordedListView.getSelectedItemPosition() == (mRecordedListView.getCount() - 1))
            {
                mRecordedListView.setSelectionFromTop(0, 0);
            }
        }

        if ((null != mBookListView) && (mBookListView.hasFocus()))
        {
            if (mBookListView.getSelectedItemPosition() == (mBookListView.getCount() - 1))
            {
                mBookListView.setSelectionFromTop(0, 0);
            }
        }
    }

    private void switchRightGroupView()
    {
        if (RecordingListStatusEnum.RECORDED == mRecordingListStatusEnum)
        {
            switchToRecord();
        }
        else if (RecordingListStatusEnum.BOOKRECORE == mRecordingListStatusEnum)
        {
            switchToView();
        }
        else if (RecordingListStatusEnum.BOOKVIEW == mRecordingListStatusEnum)
        {
            switchToRecorded();
        }
    }

    private void switchLeftGroupView()
    {
        if (RecordingListStatusEnum.RECORDED == mRecordingListStatusEnum)
        {
            switchToView();
        }
        else if (RecordingListStatusEnum.BOOKRECORE == mRecordingListStatusEnum)
        {
            switchToRecorded();
        }
        else if (RecordingListStatusEnum.BOOKVIEW == mRecordingListStatusEnum)
        {
            switchToRecord();
        }
    }

    private void sendSwitchGroupMessage()
    {
        if (null != mUIUpdateHandler)
        {
            removeHandlerMessages();

            mUIUpdateHandler.sendEmptyMessageDelayed(SWITCH_GROUP, GROUP_FILTER_TIME);
        }
    }

    private void switchToRecorded()
    {
        mRecordedTextView.setBackgroundResource(R.drawable.recording_list_title_select);
        mBookRecordTextView.setBackgroundResource(R.drawable.recording_list_title_normal);
        mBookViewTextView.setBackgroundResource(R.drawable.recording_list_title_normal);

        mRecordingListStatusEnum = RecordingListStatusEnum.RECORDED;
    }

    private void switchToRecord()
    {
        mRecordedTextView.setBackgroundResource(R.drawable.recording_list_title_normal);
        mBookRecordTextView.setBackgroundResource(R.drawable.recording_list_title_select);
        mBookViewTextView.setBackgroundResource(R.drawable.recording_list_title_normal);

        mRecordingListStatusEnum = RecordingListStatusEnum.BOOKRECORE;
    }

    private void switchToView()
    {
        mRecordedTextView.setBackgroundResource(R.drawable.recording_list_title_normal);
        mBookRecordTextView.setBackgroundResource(R.drawable.recording_list_title_normal);
        mBookViewTextView.setBackgroundResource(R.drawable.recording_list_title_select);

        mRecordingListStatusEnum = RecordingListStatusEnum.BOOKVIEW;
    }

    private void previewSelectedRecordedFile()
    {
        if (null != mSelectedRecordedFile)
        {
            String title = mContext.getResources().getString(R.string.recording_list_play_recorded_preview);
            String tip = mContext.getResources().getString(R.string.recording_list_play_recorded_preview_tip);
            mRecordingListOptionDialog = new RecordingListOptionDialog(mContext, RecordingListOptionDialog.DIALOG_TYPE_PREVIEW, title, tip,
                    mRecordingListActivity);
            preventYellowAndBlue(mRecordingListOptionDialog);
            mRecordingListOptionDialog.show();
        }
    }

    private void renameSelectedRecordedFile()
    {
        if (null != mSelectedRecordedFile)
        {
            String title = mContext.getResources().getString(R.string.recording_list_play_recorded_rename);
            String tip = mContext.getResources().getString(R.string.recording_list_play_recorded_rename_tip);
            mRecordingListOptionDialog = new RecordingListOptionDialog(mContext, RecordingListOptionDialog.DIALOG_TYPE_RENAME, title, tip,
                    mRecordingListActivity);

            mTempRequestfocuse.setFocusable(true);
            mTempRequestfocuse.setEnabled(true);
            mTempRequestfocuse.requestFocus();
            preventYellowAndBlue(mRecordingListOptionDialog);
            mRecordingListOptionDialog.show();
        }
    }

    private void deleteSelectedRecordedFile()
    {
        if (null != mSelectedRecordedFile)
        {
            String title = mContext.getResources().getString(R.string.recording_list_play_recorded_delete);
            String tip = mContext.getResources().getString(R.string.recording_list_play_recorded_delete_tip);
            mRecordingListOptionDialog = new RecordingListOptionDialog(mContext, RecordingListOptionDialog.DIALOG_TYPE_DELETE, title, tip,
                    mRecordingListActivity);
            preventYellowAndBlue(mRecordingListOptionDialog);
            mRecordingListOptionDialog.show();
        }
    }

    private void deleteAllSelectedRecordedFile() {
        if ((null != mSelectedRecordedFile) && (null != mRecordedFileList)) {
            String title = mContext.getResources().getString(R.string.recording_list_play_recorded_delete_all);
            String tip = mContext.getResources().getString(R.string.recording_list_play_recorded_delete_all_tip);
            mRecordingListOptionDialog = new RecordingListOptionDialog(mContext, RecordingListOptionDialog.DIALOG_TYPE_DELETEALL, title, tip,
                    mRecordingListActivity);
            preventYellowAndBlue(mRecordingListOptionDialog);
            mRecordingListOptionDialog.show();
        }
    }

    private void preventYellowAndBlue(final Dialog mDialog) {

        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (keyEvent.getKeyCode() == KeyValue.DTV_KEYVALUE_YELLOW || keyEvent.getKeyCode() == KeyValue.DTV_KEYVALUE_BLUE) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    // Temporary circumvention methods: solve the rename, cause the window to move the keyboard
    // interface problem
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && (mRecordingListStatusEnum == RecordingListStatusEnum.RECORDED) && (mTempRequestfocuse.isEnabled())) {
            mRecordedListView.requestFocus();
            mTempRequestfocuse.setFocusable(false);
            mTempRequestfocuse.setEnabled(false);

            mRecordedListView.requestFocus();
        }
    }

    private void editBookTask()
    {
        if (null != mSelectedBookTask)
        {
            logBookTask();
            BookTask bookTask = mBookManager.createTask();
            bookTask.setId(mSelectedBookTask.getId());
            bookTask.setChannelId(mSelectedBookTask.getChannelId());
            bookTask.setEventId(mSelectedBookTask.getEventId());
            bookTask.setName(mSelectedBookTask.getName());
            bookTask.setType(mSelectedBookTask.getType());
            bookTask.setCycle(mSelectedBookTask.getCycle());
            bookTask.setStartDate(mSelectedBookTask.getStartDate());
            bookTask.setStartDateCalendar(mSelectedBookTask.getStartDateCalendar());
            bookTask.setDuration(mSelectedBookTask.getDuration());
            bookTask.setEnable(mSelectedBookTask.isEnable());
            final BookDialog bookDialog = new BookDialog(mContext, mRecordingListActivity, bookTask, 0, 0, (float) 1.0);
            preventYellowAndBlue(bookDialog);
            bookDialog.show();
        }
    }

    /**
     * Test Code.<br>
     * @param bookTask
     */
    private void logBookTask()
    {
        LogTool.d(LogTool.MEPG, "logBookTask id = " + mSelectedBookTask.getId());
        LogTool.d(LogTool.MEPG, "logBookTask channelid = " + mSelectedBookTask.getChannelId());
        LogTool.d(LogTool.MEPG, "logBookTask duration = " + mSelectedBookTask.getDuration());
        LogTool.d(LogTool.MEPG, "logBookTask eventid = " + mSelectedBookTask.getEventId());
        LogTool.d(LogTool.MEPG, "logBookTask name = " + mSelectedBookTask.getName());
        LogTool.d(LogTool.MEPG, "logBookTask cycle = " + mSelectedBookTask.getCycle());
        LogTool.d(LogTool.MEPG, "logBookTask type = " + mSelectedBookTask.getType());
        LogTool.d(LogTool.MEPG, "logBookTask startDate = " + mSelectedBookTask.getStartDate());
    }

    public void updateBookListView()
    {
        if (RecordingListStatusEnum.BOOKRECORE == mRecordingListStatusEnum)
        {
            mBookTaskList = getBookTaskList(EnTaskType.RECORD);
            BookRecordListAdapter mBookRecordListAdapter = new BookRecordListAdapter(mContext, mBookTaskList);
            mBookListView.setAdapter(mBookRecordListAdapter);
        }
        else
        {
            mBookTaskList = getBookTaskList(EnTaskType.PLAY);
            BookViewListAdapter mBookViewListAdapter = new BookViewListAdapter(mContext, mBookTaskList);
            mBookListView.setAdapter(mBookViewListAdapter);
        }

        setBookListViewSelection();
    }

    private void setBookListViewSelection()
    {
        mBookListView.requestFocus();

        if ((null != mBookTaskList) && (!mBookTaskList.isEmpty()))
        {
            if (mSelectedBookTaskIndex >= 0)
            {
                if (mBookTaskList.size() - 1 >= mSelectedBookTaskIndex)
                {
                    mBookListView.setSelection(mSelectedBookTaskIndex);
                }
                else
                {
                    mBookListView.setSelection(mBookTaskList.size() - 1);
                }
            }
            else
            {
                mSelectedBookTaskIndex = 0;
                mBookListView.setSelection(0);
            }
        }
        else
        {
            mSelectedBookTask = null;
            mSelectedBookTaskIndex = -1;
        }
    }

    private void deleteBookTask()
    {
        if (null != mSelectedBookTask)
        {
            logBookTask();
            int ret = mBookManager.deleteTask(mSelectedBookTask);
            LogTool.d(LogTool.MEPG, "mBookManager.deleteTask:ret = " + ret);

            if (0 != ret)
            {
                String deleteFailure = mContext.getResources().getString(R.string.recording_list_play_recorded_delete_failure);
                MyToast.makeText(mContext, deleteFailure, MyToast.LENGTH_LONG).show();
            }
            else
            {
                String deleteSuccess = mContext.getResources().getString(R.string.recording_list_play_recorded_delete_success);
                MyToast.makeText(mContext, deleteSuccess, MyToast.LENGTH_LONG).show();

                updateBookListView();
            }
        }
    }

    private void deleteAllBookTasks()
    {
        if ((null != mBookTaskList) && (!mBookTaskList.isEmpty()))
        {
            if (null == mDeleteAllTasksProgressDialog)
            {
                String title = mContext.getResources().getString(R.string.recording_list_book_delete_all);
                String tip = mContext.getResources().getString(R.string.recording_list_play_waitting);
                mDeleteAllTasksProgressDialog = ProgressDialog.show(mContext, title, tip);
                TaskUtil.post(new Runnable() {
                    @Override
                    public void run() {
                        int size = mBookTaskList.size();
                        boolean isDeleted = false;
                        for (int i = 0; i < size; i++) {
                            BookTask bookTask = mBookTaskList.get(i);
                            int ret = mBookManager.deleteTask(bookTask);
                            LogTool.d(LogTool.MEPG, "mBookManager.deleteTask:ret = " + ret);
                            if (0 == ret) {
                                isDeleted = true;
                            }
                        }
                        if (null != mDeleteAllTasksProgressDialog) {
                            mDeleteAllTasksProgressDialog.dismiss();
                            mDeleteAllTasksProgressDialog = null;
                        }
                        if (isDeleted) {
                            mUIUpdateHandler.sendEmptyMessage(UPDATE_BOOKLIST);
                        }
                    }
                });
            }
        }
    }

    /**
     * Show make sure to delete channels dialog.
     */
    private void showSureToDelDialog()
    {
        if (null == mSureToDelDialogView || null == mSelectedBookTask)
        {
            return;
        }
        if (null == mSureToDelDialog)
        {
            mSureToDelDialog = new AlertDialog.Builder(this, R.style.DIM_STYLE).create();
            mSureToDelDialog.show();
            mSureToDelDialog.addContentView(mSureToDelDialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            preventYellowAndBlue(mSureToDelDialog);
            View.OnClickListener l = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (v.getId())
                    {
                    case R.id.button_book_sure_to_delete_cancel:
                    {

                    }
                        break;
                    case R.id.button_book_sure_to_delete_ok:
                    {
                        deleteBookTask();
                    }
                        break;
                    default:
                        break;
                    }
                    mSureToDelDialog.dismiss();
                }
            };

            Button cancelButton = (Button) mSureToDelDialogView.findViewById(R.id.button_book_sure_to_delete_cancel);
            Button okButton = (Button) mSureToDelDialogView.findViewById(R.id.button_book_sure_to_delete_ok);
            cancelButton.setOnClickListener(l);
            okButton.setOnClickListener(l);
            okButton.requestFocus();
        }
        else
        {
            Button okButton = (Button) mSureToDelDialogView.findViewById(R.id.button_book_sure_to_delete_ok);
            okButton.requestFocus();
        }

        if (!mSureToDelDialog.isShowing())
        {
            mSureToDelDialog.show();
        }
    }

    /**
     * Show make sure to delete all channels dialog.
     */
    private void showSureToDelAllDialog()
    {
        if (null == mSureToDelDialogAllView)
        {
            return;
        }

        if ((null == mBookTaskList) || (mBookTaskList.isEmpty()))
        {
            return;
        }

        if (null == mSureToDelAllDialog)
        {
            mSureToDelAllDialog = new AlertDialog.Builder(this, R.style.DIM_STYLE).create();
            mSureToDelAllDialog.show();
            mSureToDelAllDialog.addContentView(mSureToDelDialogAllView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            preventYellowAndBlue(mSureToDelAllDialog);
            View.OnClickListener l = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (v.getId())
                    {
                    case R.id.button_book_sure_to_delete_all_cancel:
                    {

                    }
                        break;
                    case R.id.button_book_sure_to_delete_all_ok:
                    {
                        deleteAllBookTasks();
                    }
                        break;
                    default:
                        break;
                    }
                    mSureToDelAllDialog.dismiss();
                }
            };

            Button cancelButton = (Button) mSureToDelDialogAllView.findViewById(R.id.button_book_sure_to_delete_all_cancel);
            Button okButton = (Button) mSureToDelDialogAllView.findViewById(R.id.button_book_sure_to_delete_all_ok);
            cancelButton.setOnClickListener(l);
            okButton.setOnClickListener(l);
            okButton.requestFocus();
        }
        else
        {
            Button okButton = (Button) mSureToDelDialogAllView.findViewById(R.id.button_book_sure_to_delete_all_ok);
            okButton.requestFocus();
        }

        if (!mSureToDelAllDialog.isShowing())
        {
            mSureToDelAllDialog.show();
        }
    }
    /**
     * 检查是否需要弹出父母锁
     * @return
     */
    private boolean checkPvrPCLock() {
        // 父母锁已解锁过
        if (halApi.getPwdStatus(halApi.EnumLockType.PARENTAL_LOCK_TYPE)) {
            LogTool.i(LogTool.MPLAY, "PARENTAL_LOCK_TYPE_OK");
            return false;
        }
        boolean bEqualBlock = false;
        Channel mPlayerCurrentChannel = mPvrPlayer.getCurrentChannel();
        if (mPlayerCurrentChannel == null || mDtvConfig == null) {
            LogTool.i(LogTool.MPLAY, "mPlayerCurrentChannel null");
            return false;
        }
        String strCountry = mDtvConfig.getString(CommonValue.COUNTRY_CODE_KEY, "");
        int id = mPlayerCurrentChannel.getChannelID();
        ParentalControlManager mPCManager = mDTV.getParentalControlManager();
        if (mPCManager == null) {
            LogTool.i(LogTool.MPLAY, "mPCManager null");
            return false;
        }
        int userParentalRating = mPCManager.getParentLockAge();
        int parentalRating = mPCManager.getParental(id);
        LogTool.i(LogTool.MPLAY, "parentalRating=" + parentalRating + ";userParentalRating=" + userParentalRating);
        if ((0 == parentalRating) || (0 == userParentalRating)) {
            return false;
        }
        if (null != strCountry) {
            if (strCountry.equalsIgnoreCase("MYS") || strCountry.equalsIgnoreCase("IDN") || strCountry.equalsIgnoreCase("NZL")
                    || strCountry.equalsIgnoreCase("SGP") || strCountry.equalsIgnoreCase("THA") || strCountry.equalsIgnoreCase("VNM")
                    || strCountry.equalsIgnoreCase("BRA") || strCountry.equalsIgnoreCase("RUS")) {
                bEqualBlock = true;
            } else {
                bEqualBlock = false;
            }
        }

        if (parentalRating > userParentalRating) {
            return true;
        } else if ((parentalRating == userParentalRating) && (bEqualBlock)) {
            return true;
        }

        return false;
    }
    private void releaseDTVPlayer()
    {
        EnPlayStatus enPlayStatus = mPlayer.getStatus();
        if ((null != enPlayStatus) && (EnPlayStatus.RELEASEPLAYRESOURCE != enPlayStatus))
        {
            LogTool.d(LogTool.MREC, "mPlayer.releaseResource(0)");
            Intent mIntent = new Intent();
            mIntent.setAction(CommonValue.HIDE_RADIOBG);
            sendBroadcast(mIntent);

            mPlayer.stop(EnStopType.BLACKSCREEN);
            mPlayer.releaseResource(0);
        }
    }

    private BroadcastReceiver mUsbBroadCastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String uMountPath = intent.getData().getPath();
            LogTool.d(LogTool.MREC, "uMountPath = " + uMountPath);
            if (uMountPath.equals(mRecordedFileDir))
            {
                String action = intent.getAction();
                LogTool.d(LogTool.MREC, "UsbBroadCastReceiver action = " + action);
                if (action.equals(Intent.ACTION_MEDIA_EJECT) || action.equals(Intent.ACTION_MEDIA_SHARED)
                    || action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED))
                {
                    //mIsUSBRemoved = true;

                    releasePlayer();
                    mDtvConfig.setString("au8RecordFilePath", "/mnt/sdcard");

                    initDTV();

                    releaseDTVPlayer();

                    initRecordedFilePath();
                    initSpaceTextView();
                    updateAdapterWhileDeleteAll();
                    mDurationTextView.setText(" ");
                    mResolutionTextView.setText(" ");
                    initRecordedFileList();
                    mRecordedListAdapter = new RecordedListAdapter(mContext, mRecordedFileList);
                    mUIUpdateHandler.sendEmptyMessage(UPDATE_REC_FILE_LIST);
                    LogTool.d(LogTool.MREC, "mRecordingListStatusEnum = " + mRecordingListStatusEnum);
                }

            }
        }

    };

}
