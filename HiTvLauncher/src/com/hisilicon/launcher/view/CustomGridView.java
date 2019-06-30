
package com.hisilicon.launcher.view;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import com.hisilicon.launcher.util.LogHelper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;

import com.hisilicon.launcher.MyAppActivity;
import com.hisilicon.launcher.data.AppAdapter;
import com.hisilicon.launcher.interfaces.ShowAbleInterface;
import com.hisilicon.launcher.util.Constant;

/**
 * custom GridView
 *
 * @author huyq
 */
public class CustomGridView extends GridView implements ShowAbleInterface {

    private static final String TAG = "CustomeGridView";
    private MyAppActivity mMyAppActivity;
    private Handler mHandler;

    public CustomGridView(Context context, Handler handler) {
        super(context);
        mMyAppActivity = (MyAppActivity) context;
        mHandler = handler;
    }

    @Override
    public void onShow() {
        LogHelper.d(TAG, "onShow() " + this + "  "
                + (ResolveInfo) ((AppAdapter) getAdapter()).getItem(0));
        // invalidate();
        requestFocusFromTouch();
        if (MyAppActivity.isSpanRight()) {
            setSelection(0);
        } else {
            setSelection(5);
        }
        mHandler.removeMessages(MyAppActivity.REFRESH_PAGE);
        mHandler.sendEmptyMessageDelayed(MyAppActivity.REFRESH_PAGE, 455);

    }

    @Override
    public View[] getImgViews() {
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mMyAppActivity.isFinished()) {
            return true;
        }
        int position = getSelectedItemPosition();
        LogHelper.d(TAG, "onKeyDown key : " + keyCode + ", Position : " + position);
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            MyAppActivity.setIsSpanRight(false);
            if (position == 0 || position == 6 || position == 12) {
                mMyAppActivity.snapToPreScreen();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            MyAppActivity.setIsSpanRight(true);
            if (position == getCount() - 1 || position == 5 || position == 11) {
                mMyAppActivity.snapToNextScreen();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            mMyAppActivity.startApplication(getSelectedItemPosition());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
