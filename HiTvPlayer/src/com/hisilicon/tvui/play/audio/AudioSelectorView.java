package com.hisilicon.tvui.play.audio;

import java.util.List;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;

public class AudioSelectorView extends RelativeLayout implements
                                    View.OnKeyListener, View.OnFocusChangeListener, OnTouchListener
{
    private static final String TAG = "AudioSelectorView";
    private Context mContext;
    // text of menu
    private TextView menuTxt;
    // menu of button
    private Button menuBtn;
    // title
    private String mTitle = "";
    private AudioSelectorDialog mDialog;

    public AudioSelectorView(AudioSelectorDialog dialog, Context context,String title)
    {
        super(context);
        mContext = context;
        mTitle = title;
        mDialog = dialog ;
        LayoutInflater mLinflater = LayoutInflater.from(getContext());
        mLinflater.inflate(R.layout.selector_view, this);
        initView();

    }

    /**
     * The initialization of selector
     */
    private void initView()
    {
        findViewById(R.id.left_arrow_img).setOnTouchListener(this);
        findViewById(R.id.right_arrow_img).setOnTouchListener(this);
        menuTxt = (TextView) findViewById(R.id.selector_name_txt);
        menuTxt.setText(mTitle);
        menuBtn = (Button) findViewById(R.id.menu_btn);
        menuBtn.setOnKeyListener(this);
        menuBtn.setOnFocusChangeListener(this);
        menuBtn.setText(mDialog.getCurrentAudio());
        menuBtn.setTextColor(Color.parseColor("#ff9900"));
//        menuTxt.setTextColor(Color.parseColor("#ff9900"));
        findViewById(R.id.left_arrow_img).setVisibility(View.VISIBLE);
        findViewById(R.id.right_arrow_img).setVisibility(View.VISIBLE);
    }

    /**
     *
     * @param
     */
    private void ChangeSrsState()
    {
        menuBtn.setText(mDialog.getCurrentAudio());
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
        mDialog.onKeyDown(keyCode, event);
        switch (v.getId())
        {
            case R.id.menu_btn:
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
                            mDialog.setPreAudio();
                            ChangeSrsState();
                            return true;

                        case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
                            mDialog.setNextAudio();
                            ChangeSrsState();
                            return true;

                        case KeyValue.DTV_KEYVALUE_BACK:
                        case KeyValue.DTV_KEYVALUE_AUDIO:
                            mDialog.dismiss();
                            return true;
                        default:
                            break;
                    }
                }
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        return false;
    }

    @Override
    public void onFocusChange(View arg0, boolean arg1)
    {
        // TODO Auto-generated method stub
    }

}
