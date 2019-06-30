package com.hisilicon.explorer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.hisilicon.explorer.R;

/**
 */

public abstract class BaseActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(R.drawable.background_def);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        init();
    }

    public abstract int getLayoutId();
    public abstract void init();
}
