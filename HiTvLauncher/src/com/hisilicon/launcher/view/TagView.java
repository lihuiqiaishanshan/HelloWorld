
package com.hisilicon.launcher.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.hisilicon.launcher.util.LogHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.SystemProperties;
import com.hisilicon.launcher.MainActivity;
import com.hisilicon.launcher.R;
import com.hisilicon.launcher.util.Constant;

/**
 * The bottom tag is used to quickly Switching the screen and show the current
 * page outstanding
 *
 * @author qian_shengwei
 */
public class TagView extends LinearLayout implements View.OnKeyListener,
        View.OnFocusChangeListener {

    private static final String TAG = "TagView";
    private MainActivity mContext;
    // focused page number
    private int mFocusedPage;

    // layout of App
    private RelativeLayout mTagApp;
    // layout of Setting
    private RelativeLayout mTagSetting;
    // list of all tag layout
    private RelativeLayout[] mTagList;

    // text of app
    private TextView appText;
    // text of setting
    private TextView settingText;
    // list of all tag text
    private TextView[] mTextList;

    // image of app
    private ImageView appImg;
    // image of setting
    private ImageView settingImg;
    // list of all tag image
    private ImageView[] mImgList;

    private int mKeyCode = -1;

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = (MainActivity) context;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View parent = inflater.inflate(R.layout.tag_view, this);
        initView(parent);
        mFocusedPage = mContext.getFocusedPage();
    }

    /**
     * The initialization of view
     *
     * @param parent
     */
    private void initView(View parent) {
        mTagApp = (RelativeLayout) parent.findViewById(R.id.tag_app);
        mTagSetting = (RelativeLayout) parent.findViewById(R.id.tag_setting);
        mTagApp.setOnKeyListener(this);
        mTagSetting.setOnKeyListener(this);
        mTagApp.setOnFocusChangeListener(this);
        mTagSetting.setOnFocusChangeListener(this);
        mTagList = new RelativeLayout[] {mTagApp, mTagSetting};

        appText = (TextView) mTagApp.findViewById(R.id.tag_app_txt);
        settingText = (TextView) mTagSetting.findViewById(R.id.tag_setting_txt);
        mTextList = new TextView[] {appText, settingText};

        appImg = (ImageView) mTagApp.findViewById(R.id.tag_app_img);
        settingImg = (ImageView) mTagSetting.findViewById(R.id.tag_setting_img);
        mImgList = new ImageView[] {appImg, settingImg};
    }

    /**
     * set view when select change
     *
     * @param focusedPage
     */
    public void setViewOnSelectChange(int focusedPage) {
        LogHelper.d(TAG, "setViewOnSelectChange page : " + focusedPage);
        mContext.setFocusePage(focusedPage);
        mFocusedPage = focusedPage;
        for (int i = 0; i < mTagList.length; i++) {
            boolean hasFocus = focusedPage == i ;
            mTextList[i].setTextSize(hasFocus ? 32F : 26F);
            mTextList[i].setTextColor(hasFocus ?
                    Color.WHITE : mContext.getResources().getColor(R.color.tagunfocus));
            mImgList[i].setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * get list of all tag layout
     *
     * @return
     */
    public RelativeLayout[] getTagList() {
        return mTagList;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return true;
        LogHelper.d(TAG, "onKey keyCode : " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mKeyCode = KeyEvent.KEYCODE_DPAD_LEFT;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mKeyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                mKeyCode = -1;
                View[] views = mContext.getRoot().getCurScreen().getImgViews();
                mFocusedPage = mContext.getFocusedPage();
                switch (mFocusedPage) {
                    case MainPageApp.PAGENUM:
                        if (views.length == 7) {
                            views[4].requestFocus();
                        }
                        break;
                    case MainPageSetting.PAGENUM:
                        if (views.length == 8) {
                            views[7].requestFocus();
                        }
                        break;
                    default:
                        break;
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mKeyCode = -1;
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        LogHelper.d(TAG, "onFocusChange hasFocus : " + hasFocus);
        if (hasFocus) {
            switch (mKeyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mContext.snapToPreScreen();
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mContext.snapToNextScreen();
                    break;
                default:
                    break;
            }
            mKeyCode = -1;
            LogHelper.d(TAG, "onFocusChange mFocusedPage = " + mFocusedPage );
            int curFocus = mFocusedPage;
            switch (v.getId()) {
                case R.id.tag_app:
                    mFocusedPage = MainPageApp.PAGENUM;
                    break;
                case R.id.tag_setting:
                    mFocusedPage = MainPageSetting.PAGENUM;
                    break;
                default :
                    break;
            }
            if (curFocus == MainPageApp.PAGENUM && mFocusedPage == MainPageSetting.PAGENUM) {
                mContext.snapToNextScreen();
            } else if (curFocus == MainPageSetting.PAGENUM && mFocusedPage == MainPageApp.PAGENUM) {
                mContext.snapToPreScreen();
            }else {
                LogHelper.d(TAG, "onFocusChange focus no change" );
            }
            LogHelper.d(TAG, "onFocusChange focus : " + mFocusedPage);
        }
        setViewOnSelectChange(mFocusedPage);
    }

}
