package com.hisilicon.tvui.record;

import java.io.File;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.MyToast;

public class RecordingListOptionDialog extends Dialog
{
    public static final int DIALOG_TYPE_PREVIEW = 0;
    public static final int DIALOG_TYPE_RENAME = 1;
    public static final int DIALOG_TYPE_DELETE = 2;
    public static final int DIALOG_TYPE_DELETEALL = 3;

    private static final String TAG = "RecordingListOptionDialog";

    private Context mContext = null;

    private int mDialogType = 0;

    private TextView mFileTitleTextView = null;

    private TextView mFileNameTextView = null;

    private EditText mFileNameEditText = null;

    private Button mCancelButton = null;

    private Button mOKButton = null;

    private ProgressDialog mProgressDialog = null;

    private RecordingListOptionDialog mRecordingListOptionDialog = null;

    private String mTitle = null;

    private String mTip = null;

    private RecordingListActivity mRecordingListActivity = null;

    private static final String TS_FILE_SUFFIX = ".ts";

    private File[] mFileList = null;

    public RecordingListOptionDialog(Context context, int dialogType, String title, String tip, RecordingListActivity recordingListActivity)
    {
        super(context, R.style.DIM_STYLE);

        this.mContext = context;

        this.mRecordingListOptionDialog = this;
        mDialogType = dialogType;
        this.mTitle = title;
        LogTool.d(LogTool.MREC, "mTitle = " + mTitle);

        this.mTip = tip;
        LogTool.d(LogTool.MREC, "mTip = " + mTip);

        this.mRecordingListActivity = recordingListActivity;

        LogTool.d(LogTool.MREC, "mRecordingListActivity.mSelectedRecordedFile.getName() = " + mRecordingListActivity.mSelectedRecordedFile.getName());

        // Set layout parameters of this dialog
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.x = 0;
        lp.y = 0;
        lp.alpha = (float) 1.0;
        this.getWindow().setAttributes(lp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.recording_list_option_dialog);

        TextView mTitleTextView = (TextView) findViewById(R.id.tv_recording_list_option_dialog_title);
        mTitleTextView.setText(mTitle);

        TextView mTipTextView = (TextView) findViewById(R.id.tv_recording_list_option_dialog_tip);
        mTipTextView.setText(mTip);

        mCancelButton = (Button) findViewById(R.id.btn_recording_list_option_dialog_cancel);
        mOKButton = (Button) findViewById(R.id.btn_recording_list_option_dialog_ok);

        mFileTitleTextView = (TextView) findViewById(R.id.tv_recording_list_option_dialog_file_name_title);
        mFileNameTextView = (TextView) findViewById(R.id.tv_recording_list_option_dialog_file_name);
        mFileNameEditText = (EditText) findViewById(R.id.et_recording_list_option_dialog_file_name);

        initialFileNameEditText();
        initialFileView();
        initialFileList();
        initialOKButton();
        initialCancelButton();
    }

    private void initialFileNameEditText()
    {
        mFileNameEditText.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    String newFileName = mFileNameEditText.getText().toString();
                    if (!newFileName.endsWith(TS_FILE_SUFFIX))
                    {
                        newFileName += TS_FILE_SUFFIX;
                        mFileNameEditText.setText(newFileName);
                    }
                }
                else
                {
                    mFileNameEditText.selectAll();
                }
            }
        });
    }

    private void initialFileView()
    {
        if (DIALOG_TYPE_RENAME == mDialogType)
        {
            mFileTitleTextView.setText(R.string.recording_list_play_recorded_new_file);

            mFileNameTextView.setVisibility(View.GONE);

            mFileNameEditText.setVisibility(View.VISIBLE);

            mFileNameEditText.setText(mRecordingListActivity.mSelectedRecordedFile.getName());

            mFileNameEditText.requestFocus();
        }
        else if (DIALOG_TYPE_DELETEALL == mDialogType)
        {
            mFileTitleTextView.setVisibility(View.GONE);
            mFileNameTextView.setVisibility(View.GONE);
            mFileNameEditText.setVisibility(View.GONE);
        }
        else
        {
            mFileTitleTextView.setText(R.string.recording_list_play_recorded_current_file);

            mFileNameTextView.setVisibility(View.VISIBLE);

            mFileNameTextView.setText(mRecordingListActivity.mSelectedRecordedFile.getName());

            mFileNameEditText.setVisibility(View.GONE);

            mOKButton.requestFocus();
        }
    }

    private void initialFileList()
    {
        File fileDir = new File(mRecordingListActivity.mSelectedRecordedFile.getDir());

        if ((null == fileDir) || (!fileDir.isDirectory()))
        {
            return;
        }

        mFileList = fileDir.listFiles();
    }

    private void initialOKButton() {
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecordingListOptionDialog.dismiss();
                mProgressDialog = ProgressDialog.show(mContext, mTitle, mContext.getResources().getString(R.string.recording_list_play_waitting));
                TaskUtil.post(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        doOption();
                        mProgressDialog.dismiss();
                    }
                });
            }
        });
    }

    private void doOption()
    {
        if ((null == mRecordingListActivity.mSelectedRecordedFile) || (null == RecordingListActivity.mRecordedFileList))
        {
            return;
        }

        switch (mDialogType)
        {
        case DIALOG_TYPE_PREVIEW:
            doOptionPreview();
            break;
        case DIALOG_TYPE_RENAME:
            doOptionRename();
            break;
        case DIALOG_TYPE_DELETE:
            doOptionDelete();
            break;
        case DIALOG_TYPE_DELETEALL:
            doOptionDeleteAll();
            break;
        default:
            break;
        }
    }

    private void doOptionPreview()
    {
        mRecordingListActivity.playSelectedRecordedFile();
    }

    private void doOptionRename()
    {
        String oldFilePathName = mRecordingListActivity.mSelectedRecordedFile.getFilePath();

        String newFileName = mFileNameEditText.getText().toString();

        if (!newFileName.endsWith(TS_FILE_SUFFIX))
        {
            newFileName += TS_FILE_SUFFIX;
        }

        String newFilePathName = mRecordingListActivity.mSelectedRecordedFile.getDir() + File.separator + newFileName;

        boolean bSuccess = renameFile(oldFilePathName, newFilePathName);

        if (bSuccess)
        {
            mRecordingListActivity.mSelectedRecordedFile.setName(newFileName);
            mRecordingListActivity.sendUpdateAdapterWhileRename();
        }
        else
        {
        }
    }

    private boolean renameFile(String oldFilePathName, String newFilePathName)
    {
        final String fileExisted = oldFilePathName + " " + mContext.getResources().getString(R.string.recording_list_play_recorded_file_existed);

        final String fileNotExisted = oldFilePathName + " " + mContext.getResources().getString(R.string.recording_list_play_recorded_file_not_existed);

        final String renameSuccess = oldFilePathName + " " + mContext.getResources().getString(R.string.recording_list_play_recorded_rename_success);

        File oldFile = new File(oldFilePathName);
        File newFile = new File(newFilePathName);

        if ((!oldFile.isFile()) || (!oldFile.exists()))
        {
            mRecordingListActivity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    MyToast.makeText(mContext, fileNotExisted, MyToast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
        else if ((newFile.isFile()) && newFile.exists())
        {
            mRecordingListActivity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    MyToast.makeText(mContext, fileExisted, MyToast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
        else
        {
            for (int i = 0; i < mFileList.length; i++)
            {
                File file = mFileList[i];
                if (file.getPath().startsWith(oldFilePathName))
                {
                    int tailLength = file.getPath().length() - oldFilePathName.length();
                    String newTmpFileName = newFilePathName
                            + file.getPath().substring(file.getPath().length() - tailLength, file.getPath().length());
                    File newTmpFile = new File(newTmpFileName);
                    if (!file.renameTo(newTmpFile))
                    {
                        LogTool.w(LogTool.MREC, "renameFile failed to calls renameTo from " + file.getAbsolutePath()
                                + "/" + file.getName() + " to " + newTmpFileName);
                    }
                }
            }

            mRecordingListActivity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    MyToast.makeText(mContext, renameSuccess, MyToast.LENGTH_SHORT).show();
                }
            });
        }

        return true;
    }

    private void doOptionDelete()
    {
        String filePathName = mRecordingListActivity.mSelectedRecordedFile.getFilePath();

        final String deleteSuccess = filePathName + " " + mContext.getResources().getString(R.string.recording_list_play_recorded_delete_success);

        mRecordingListActivity.releasePlayer();
        for (int i = 0; i < mFileList.length; i++)
        {
            if (mFileList[i].getPath().startsWith(filePathName))
            {
                File file = mFileList[i];
                if(!file.delete())
                {
                    LogTool.w(LogTool.MREC, "doOptionDelete failed to delete file: " + file.getAbsolutePath() + "/" + file.getName());
                }
            }
        }

        mRecordingListActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                MyToast.makeText(mContext, deleteSuccess, MyToast.LENGTH_SHORT).show();
            }
        });
        mRecordingListActivity.sendUpdateAdapterWhileDelete();
    }

    private void doOptionDeleteAll()
    {
        mRecordingListActivity.releasePlayer();
        final String deleteAllSuccess = mContext.getResources().getString(R.string.recording_list_play_recorded_delete_all_success);

        for (int i = 0; i < mFileList.length; i++)
        {
            File file = mFileList[i];
            if (file.getPath().contains(TS_FILE_SUFFIX))
            {
                if (!file.delete())
                {
                    LogTool.w(LogTool.MREC, "doOptionDeleteAll failed to delete file: " + file.getAbsolutePath() + "/" +file.getName());
                }
            }
        }

        mRecordingListActivity.sendUpdateAdapterWhileDeleteAll();

        mRecordingListActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                MyToast.makeText(mContext, deleteAllSuccess, MyToast.LENGTH_SHORT).show();
            }
        });
    }

    private void initialCancelButton()
    {
        mCancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mRecordingListOptionDialog.dismiss();
            }
        });
    }
}
