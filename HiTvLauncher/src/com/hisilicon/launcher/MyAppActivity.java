
package com.hisilicon.launcher;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.pm.LauncherApps;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.text.TextUtils;
import com.hisilicon.launcher.util.LogHelper;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.content.ActivityNotFoundException;

import com.hisilicon.launcher.data.AppAdapter;
import com.hisilicon.launcher.data.AppAdapter.LoadEndListener;
import com.hisilicon.launcher.thread.MyAppAync;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.util.Util;
import com.hisilicon.launcher.view.CustomGridView;
import com.hisilicon.launcher.view.CustomProgressDialog;
import com.hisilicon.launcher.view.MyAppScrollLayout;
import com.hisilicon.launcher.view.MyPopWindow;

/**
 * MY Application
 */
@SuppressLint("UseSparseArrays")
public class MyAppActivity extends Activity implements OnItemClickListener,
        OnItemSelectedListener, LoadEndListener {
    private static final String TAG = "MyAppActivity";
    public static final int REFRESH_PAGE = 100;
    public static final int UPDATE_VIEW = 101;
    // private static final int LINE = 3;
    // private static final int LIST = 6;
    // asynchronous data loading
    private MyAppAync mMAppAync = null;
    private MyAppScrollLayout mRootLayout;
    private static boolean mIsSnapRight = false;
    // Is this the first time into the activity
    private boolean isfirst = true;
    private ResolveInfo mResolveInfo = null;
    // list of CustomGridView
    private List<CustomGridView> mCustomeGridViewlist = null;
    // pop window when click the menu
    private MyPopWindow mMultiPop;
    private CustomProgressDialog mCustomProgressDialog = null;
    // image of left arrow
    private ImageView mLeftArrowImg = null;
    // image of right arrow
    private ImageView mRightArrowImg = null;
    // text of page
    private TextView mPageText;
    // text of app title
    private TextView mAppTitleText;
    // text of info title
    private TextView mInfoTitleText;
    // the current page number
    private int mCurrentNum = 1;
    private int mPageNumber = 0;
    //the selected item index
    private int mClickItemPosition = 0;
    // application list
    private List<ResolveInfo> mAppList = null;
    // list for every gridview
    private List<ResolveInfo> mGridAppList = null;
    private MyApplication mApplication = null;

    private LauncherApps.Callback callback;
    private LauncherApps apps;

    /**
     * Registered receiver to monitor changes of application
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogHelper.d(TAG, "onReceive =intent:" + intent.getAction());
            mApplication = (MyApplication) context.getApplicationContext();
            mAppList = mApplication.getResolveInfos();
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                mApplication.clearList();
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)
                    || intent.getAction()
                            .equals(Intent.ACTION_PACKAGE_REPLACED)) {
                LogHelper.d(TAG, "inner receiver app added ");
                mApplication.clearList();
            }
            if (null != mRootLayout) {
                mRootLayout.setToScreen(0);
                mRootLayout.removeAllViews();
            }
            updataApp(true);
        }
    };

    public static void setIsSpanRight(boolean isSnapRight){
        mIsSnapRight = isSnapRight;
    }

    public static boolean isSpanRight(){
        return mIsSnapRight;
    }

    /**
     * clear data
     */
    private Handler mDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Util.CLEAR_USER_DATA:
                    Util.showToast(MyAppActivity.this, R.string.clear_data_suc);
                    break;
                case Util.NOT_CLEAR_USER_DATA:
                    Util.showToast(MyAppActivity.this, R.string.clear_data_fail);
                    break;
                default:
                    break;
            }
        }

    };

    /**
     * control the title bar to hide / appear
     */
    public void setTitleViewGoneOrVisible(boolean show) {
        if (show) {
            if (mCurrentNum == 1) {
                mLeftArrowImg.setVisibility(View.GONE);
            } else {
                mLeftArrowImg.setVisibility(View.VISIBLE);
            }
            mRightArrowImg.setVisibility(View.VISIBLE);
            mAppTitleText.setVisibility(View.VISIBLE);
            mInfoTitleText.setVisibility(View.VISIBLE);
        } else {
            mLeftArrowImg.setVisibility(View.GONE);
            mRightArrowImg.setVisibility(View.GONE);
            mAppTitleText.setVisibility(View.GONE);
            mInfoTitleText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.i(TAG, "===== onCreate =====");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.my_app_main);
        initView();
        initData();
    }

    /**
     * initialization of data
     */
    private void initData() {
        mApplication = (MyApplication) MyAppActivity.this.getApplication()
                .getApplicationContext();
        mApplication.setHandler(mFinishHandler);
        updataApp(true);
        registReceiver();
        this.registerCallback();
    }

    /**
     * initialization of widget
     */
    private void initView() {
        mRootLayout = (MyAppScrollLayout) findViewById(R.id.root);
        mLeftArrowImg = (ImageView) this.findViewById(R.id.left_arrow);
        mRightArrowImg = (ImageView) this.findViewById(R.id.right_arrow);
        mPageText = (TextView) findViewById(R.id.page_num);
        mAppTitleText = (TextView) findViewById(R.id.all_app_title);
        mInfoTitleText = (TextView) findViewById(R.id.info_title);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!mRootLayout.isFinished()) {
            return true;
        }
        LogHelper.d(TAG, "onKeyDown key : " + keyCode);
        CustomGridView focusedView = (CustomGridView) mRootLayout
                .getChildAt(mRootLayout.getCurrentScreen());
        if (focusedView == null) {
            return true;
        }
        ResolveInfo info = (ResolveInfo) focusedView.getSelectedItem();
      switch (keyCode){
        case KeyEvent.KEYCODE_MENU:
        int count = focusedView.getChildCount();
        if (info == null && count > 0) {
            if (count <= mClickItemPosition) {
                mClickItemPosition = count - 1;
            }
            info =(ResolveInfo) focusedView.getAdapter().getItem(mClickItemPosition);
        }
        mMultiPop = new MyPopWindow(MyAppActivity.this, getResources()
                .getStringArray(R.array.app_management), info);
        Window window = mMultiPop.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mMultiPop.show();
        mMultiPop.init();
        break;
      }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mRootLayout.isFinished()) {
            return true;
        }
        LogHelper.d(TAG, "onKeyDown key : " + keyCode);
        CustomGridView focusedView = (CustomGridView) mRootLayout
                .getChildAt(mRootLayout.getCurrentScreen());
        ResolveInfo info = (ResolveInfo) focusedView.getSelectedItem();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                break;
            case KeyEvent.KEYCODE_BACK:
                if (!isFinishing() || !isFinished()) {
                    finish();
                }
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onPause() {
        LogHelper.i(TAG, "===== onPause ===== ");
        super.onPause();
        mIsSnapRight = false;
        if (mMultiPop != null && mMultiPop.isShowing()) {
            mMultiPop.dismiss();
        }
    }

    @Override
    protected void onStop() {
        LogHelper.i(TAG, "===== onStop =====");
        if (mMAppAync != null && !mMAppAync.isCancelled()) {
            mMAppAync.cancel(true);
            mMAppAync = null;
        }
        mIsSnapRight = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LogHelper.i(TAG, "===== onDestroy =====");
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (null != mAppList && mAppList.size() > 0) {
            mAppList.clear();
            mAppList = null;
        }
        if (null != mApplication) {
            mApplication.clearList();
            mApplication = null;
        }
        this.unRegisterCallback();
    }

    /**
     * radio listeners
     */
    private void registReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL);
        intentFilter.addDataScheme("package");
        registerReceiver(mReceiver, intentFilter);
    }

    /**
     * refreshFlag if need the pop-up dialog box
     */
    private void updataApp(boolean showDialogFlag) {
        if (mMultiPop != null && mMultiPop.isShowing()) {
            mMultiPop.dismiss();
        }
        if (mMAppAync != null) {
            mMAppAync.cancel(true);
            mMAppAync = null;
        }
        if (null != mRootLayout) {
            mRootLayout.removeAllViews();
        }
        if (null != mCustomeGridViewlist && mCustomeGridViewlist.size() > 0) {
            mCustomeGridViewlist.clear();
            mCustomeGridViewlist = null;
        }
        showProgress();

        mMAppAync = new MyAppAync(MyAppActivity.this, showDialogFlag);
        mAppList = mApplication.getResolveInfos();
        if (mAppList == null || mAppList.size() == 0) {
            isfirst = true;
            mMAppAync.execute(Util.ALL_APP);
        } else {
            bindDataToContainer();
        }
    }

    /**
     * bind the application list
     */
    private void bindDataToContainer() {
        int lineNumber = 3;
        int listNumber = 6;
        mPageNumber = (mAppList.size() % (lineNumber * listNumber) == 0 ? 0 : 1)
                + (mAppList.size() / (lineNumber * listNumber));
        LogHelper.d(TAG, "pageNumber :" + mPageNumber +",mAppList.size() :" + mAppList.size());
        mCustomeGridViewlist = new ArrayList<CustomGridView>();
        for (int i = 0; i < mPageNumber; i++) {
            CustomGridView appPage = new CustomGridView(MyAppActivity.this,
                    mFinishHandler);
            appPage.setFocusable(true);
            appPage.setFocusableInTouchMode(true);
            appPage.setSelector(R.drawable.white_border);
            appPage.setOnItemClickListener(MyAppActivity.this);
            appPage.setOnItemSelectedListener(MyAppActivity.this);
            mGridAppList = new ArrayList<ResolveInfo>();
            for (int j = lineNumber * listNumber * i; j < lineNumber
                    * listNumber * (i + 1); j++) {
                if (j < mAppList.size()) {
                    mGridAppList.add(mAppList.get(j));
                } else {
                    break;
                }
            }
            // get the "i" page data
            appPage.setVerticalSpacing(6);
            appPage.setFocusable(true);
            appPage.setFocusableInTouchMode(true);
            appPage.setHorizontalSpacing(6);
            appPage.setNumColumns(6);
            AppAdapter adpter = new AppAdapter(MyAppActivity.this,
                    mGridAppList, i, mPageNumber);
            adpter.setmLoadEndListener(MyAppActivity.this);
            appPage.setAdapter(adpter);
            LogHelper.d(TAG,
                        "appPage=" + appPage + "childcount = "
                                + appPage.getChildCount());
            mRootLayout.addView(appPage);
            mCustomeGridViewlist.add(appPage);
        }
        mFinishHandler.sendEmptyMessage(REFRESH_PAGE);
    }

    /**
     * set current focus
     */
    private void resetFocus() {
        mCurrentNum = 1;
        if (null != mCustomeGridViewlist) {
            mCustomeGridViewlist.get(0).requestFocusFromTouch();
            mCustomeGridViewlist.get(0).setSelection(0);
            mPageText.setText((mRootLayout.getCurrentScreen() + 1) + "/"
                    + mPageNumber);
            mLeftArrowImg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if (mCustomeGridViewlist != null && mCustomeGridViewlist.size() > 1
                    && isfirst) {
                LogHelper.d(TAG, "CustomeGridViewlist.get(0).getChildCount()="
                            + mCustomeGridViewlist.get(0).getChildCount());
                resetFocus();
                isfirst = false;
            }
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private Handler mFinishHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    finish();
                    break;
                case 1:
                    break;
                case REFRESH_PAGE:
                    resetImg();
                    break;
                case UPDATE_VIEW:
                    updataApp(true);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    /**
     * show page number and set the arrow show or gone
     */
    private void resetImg() {
        if (null != mPageText) {
            mPageText.setText((mRootLayout.getCurrentScreen() + 1) + "/"
                    + mPageNumber);
        }
        if (mRootLayout.getCurrentScreen() == 0) {
            mLeftArrowImg.setVisibility(View.GONE);
            mRightArrowImg.setVisibility(mPageNumber == 1 ? View.GONE : View.VISIBLE);

        } else if (mRootLayout.getCurrentScreen() == mPageNumber - 1) {
            mLeftArrowImg.setVisibility(View.VISIBLE);
            mRightArrowImg.setVisibility(View.GONE);
        } else {
            mLeftArrowImg.setVisibility(View.VISIBLE);
            mRightArrowImg.setVisibility(View.VISIBLE);
        }
    }

    /**
     * display the progress dialog
     */
    public void showProgress() {
        if (mCustomProgressDialog == null) {
            mCustomProgressDialog = CustomProgressDialog.createDialog(this);
        }
        // set whether ProgressDialog can press return key to cancel
        mCustomProgressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        // let ProgressDialog display
        setTitleViewGoneOrVisible(false);
        mCustomProgressDialog.show();
    }

    /**
     * let the dialog dismiss
     */
    public void dismissDialog() {
        if (mCustomProgressDialog != null && mCustomProgressDialog.isShowing()) {
            setTitleViewGoneOrVisible(true);
            mCustomProgressDialog.dismiss();
            mCustomProgressDialog = null;
        }
    }

    /**
     * show next Screen
     */
    public void snapToNextScreen() {
        mRootLayout.snapToScreen(mRootLayout.getCurrentScreen() + 1);
    }

    /**
     * show previous Screen
     */
    public void snapToPreScreen() {
        mRootLayout.snapToScreen(mRootLayout.getCurrentScreen() - 1);
    }

    public boolean isFinished() {
        return mRootLayout.isFinished();
    }

    public void startApplication (int position) {
        String pkg = null;
        String cls = null;
        CustomGridView appPage = (CustomGridView)mRootLayout.getChildAt(
                mRootLayout.getCurrentScreen());
        if (position < 0 && position >= appPage.getAdapter().getCount()) {
            return;
        }
        ResolveInfo res = (ResolveInfo) appPage.getAdapter().getItem(position);
        if (null == res) {
            return;
        }
        pkg = res.activityInfo.packageName;
        cls = res.activityInfo.name;
        if (TextUtils.isEmpty(pkg) || TextUtils.isEmpty(cls)) {
            return;
        }
        mClickItemPosition = position;
        try{
            ComponentName componentName = new ComponentName(pkg, cls);
            Intent mIntent = new Intent(Intent.ACTION_MAIN);
            mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mIntent.setComponent(componentName);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(mIntent);
        } catch(ActivityNotFoundException e){
            LogHelper.e(TAG,e.getMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogHelper.d(TAG, "onItemClick position = " + position);
        parent.requestFocusFromTouch();
        parent.setSelection(position);
        startApplication(position);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        LogHelper.d(TAG, "onItemSelected position = " + position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * when currentPage is equals to mPageNumber-1 that means all the data has
     * loaded
     */
    @Override
    public void onEndListerner(int currentPage) {
        LogHelper.d(TAG, "onEndListerner :" + currentPage +" pageNumber :" + mPageNumber);
        if (currentPage == mPageNumber - 1) {
            dismissDialog();
        }
    }

    public int getCurrentNum() {
        return mCurrentNum;
    }

    public void setCurrentNum(int currentNum) {
        this.mCurrentNum = currentNum;
    }

    private void registerCallback(){
        this.apps = (LauncherApps) this.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.callback = new LauncherApps.Callback() {
            @Override
            public void onPackageRemoved(String packageName, UserHandle user) {
                //upView();
            }

            @Override
            public void onPackageAdded(String packageName, UserHandle user) {
                //upView();
            }

            @Override
            public void onPackageChanged(String packageName, UserHandle user) {
                upView();
            }

            @Override
            public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
                upView();
            }

            @Override
            public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
                upView();
            }

        };

        this.apps.registerCallback(this.callback);

    }

    private void unRegisterCallback(){
        if(this.apps == null || this.callback == null){
            return;
        }

        this.apps.unregisterCallback(this.callback);
    }

    private void upView(){
       mApplication = (MyApplication)getApplicationContext();
       mApplication.clearList();
       if (null != mRootLayout) {
           mRootLayout.setToScreen(0);
           mRootLayout.removeAllViews();
       }
       updataApp(true);
    }
}
