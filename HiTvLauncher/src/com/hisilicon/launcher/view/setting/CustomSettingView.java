
package com.hisilicon.launcher.view.setting;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.hisilicon.launcher.util.LogHelper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.Refreshable;
import com.hisilicon.launcher.util.Constant;

/**
 * In addition to the network settings and use the help, were created using the
 * CustomSettingView dialog
 */
public class CustomSettingView extends LinearLayout {
    private static final String TAG = "CustomSettingView";
    private Context mContext;
    // the list of WidgetType
    private List<WidgetType> mWidgetList;
    // the flag of show dialog
    private boolean mShowDialog = false;
    // the list of Refreshable
    private List<Refreshable> mWidgetViewList;

    /**
     * get the flag of show dialog
     *
     * @return
     */
    public boolean isShowDialog() {
        return mShowDialog;
    }

    /**
     * the handler for refresh UI
     */
    private Handler mRefreshHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == Constant.SETTING_UI_REFRESH_VIEWS) {
                List<String> list = (List<String>) msg.obj;
                LogHelper.d(TAG, "mRefreshHandler list size = " + list.size());
                refreshUI(list);
            }
        }
    };

    /**
     * set the flag of show dialog
     *
     * @param showDialog
     */
    public void setShowDialog(boolean showDialog) {
        mShowDialog = showDialog;
    }

    public CustomSettingView(Context context, String title,
            InterfaceLogic interfaceLogic) {
        super(context);
        mContext = context;
        interfaceLogic.setHandler(mRefreshHandler);
        this.mWidgetList = interfaceLogic.getWidgetTypeList();
        setGravity(Gravity.TOP);
        setOrientation(VERTICAL);
        addViews(title);
    }

    /**
     * add view based on the widget type
     *
     * @param title
     */
    private void addViews(String title) {
        if (title != null && !"".equals(title) && mWidgetList.size() > 1) {
            addView(new TitleView(mContext, title), new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        LogHelper.d(TAG, mWidgetList.toString());
        mWidgetViewList = new ArrayList<Refreshable>();
        for (WidgetType mWidgetType : mWidgetList) {
            LogHelper.d(TAG, "Get into");
            switch(mWidgetType.getType()) {
                case WidgetType.TYPE_SELECTOR : // the type of selector
                    SelectorView SelectorView = new SelectorView(this, mContext,
                            mWidgetType, mWidgetList);
                    addView(SelectorView, new LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    mWidgetViewList.add(SelectorView);
                    break;
                case WidgetType.TYPE_PROGRESS : // the type of progress
                    ProgressView ProgressView = new ProgressView(this, mContext,
                            mWidgetType, mWidgetList);
                    addView(ProgressView, new LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    mWidgetViewList.add(ProgressView);
                    break;
                case WidgetType.TYPE_TEXTVIEW : // the type of textView
                    SystemTextView SystemTextView = new SystemTextView(mContext,
                            this, mWidgetList, mWidgetType);
                    addView(SystemTextView, new LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    mWidgetViewList.add(SystemTextView);
                    break;
                case WidgetType.TYPE_ONLYSELECTOR : // the type of only selector
                    ButtonView ButtonView = new ButtonView(mContext, this,
                            mWidgetType, mWidgetList);
                    addView(ButtonView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));
                    if (mWidgetList.get(0) == mWidgetType) {
                        ButtonView.requestFocus();
                    }
                    ButtonView.setTag(mWidgetType.getTag());
                    mWidgetViewList.add(ButtonView);
                    break;
                case WidgetType.TYPE_LONG_TEXT : //the type of long textView
                    LongSystemTextView LongystemTextView = new LongSystemTextView(mContext,
                            this, mWidgetList, mWidgetType);
                    addView(LongystemTextView, new LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    mWidgetViewList.add(LongystemTextView);
                default :
                    break;
            }
        }
        int mWidgetTypeListSize = mWidgetList.size();

        if (mWidgetTypeListSize > 1) {
            setBackgroundResource(R.drawable.launcher_set_bg);
        } else if (mWidgetTypeListSize == 1) {
            setBackgroundResource(R.drawable.sigle_item_bg);
            setLayoutParams(new LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT, (int) mContext.getResources().getDimension(R.dimen.dimen_100px)));
        }
    }

    /**
     * get the list of WidgetType
     *
     * @return
     */
    public List<WidgetType> getList() {
        return mWidgetList;
    }

    /**
     * set the list of WidgetType
     *
     * @param list
     */
    public void setList(List<WidgetType> list) {
        this.mWidgetList = list;
    }

    /**
     * refresh UI
     *
     * @param list
     */
    public void refreshUI(List<String> list) {
        if (list != null && list.size() > 0) {
            for (String name : list) {
                Refreshable mRefreshable = getViewByName(name);
                if (mRefreshable != null) {
                    mRefreshable.refreshUI();
                }

            }
        }
    }

    /**
     * get Refreshable by name
     *
     * @param name
     * @return
     */
    private Refreshable getViewByName(String name) {
        if (name == null) {
            return null;
        }
        if (mWidgetViewList != null && mWidgetViewList.size() > 0) {
            for (Refreshable refreshable : mWidgetViewList) {
                WidgetType widgetType = refreshable.getWidgetType();
                if (widgetType != null && widgetType.getName().equals(name)) {
                    return refreshable;
                }
            }
        }
        return null;
    }

    /**
     * Subclasses can call this method
     *
     * @param v
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
            // the key up
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (mWidgetViewList.get(0).getIsFocus()) {
                        ((View) mWidgetViewList.get(mWidgetViewList.size() - 1))
                                .requestFocus();
                        return true;
                    }
                    break;
                // the key down
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (mWidgetViewList.get(mWidgetViewList.size() - 1)
                            .getIsFocus()) {
                        ((View) mWidgetViewList.get(0)).requestFocus();
                        return true;
                    }
                    break;
                default:
                    break;
            }

            return false;
        }
        return false;
    }
}
