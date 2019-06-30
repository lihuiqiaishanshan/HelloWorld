package com.hisilicon.explorer.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;

import com.hisilicon.explorer.Config;
import com.hisilicon.explorer.R;
import com.hisilicon.explorer.service.SambaService;
import com.hisilicon.explorer.ui.AnimationButton;
import com.hisilicon.explorer.utils.ImageReflect;
import com.hisilicon.explorer.utils.SystemProperties;

public class TabBarExample extends BaseActivity implements OnClickListener, OnKeyListener {
    private AnimationButton tabButton;
    private AnimationButton samBaButton;
    private AnimationButton UPnPButton;
    private AnimationButton nfsButton;
    private AnimationButton baiduPcs;
    private int focusViewId;
    private ImageView refImageView[] = new ImageView[3];

    private IntentFilter mIntenFilter = null;
    private BroadcastReceiver mReceiver = null;
    private boolean isDemoMode;
    private static final String TAG = "TabBarExample";

    @Override
    public int getLayoutId() {
        isDemoMode = Config.getInstance().ismIsIPTV();
        if (isDemoMode)
            return R.layout.main_home_view_ott;
        else
            return R.layout.main_home_view;
    }

    @Override
    public void init() {
        Log.d(TAG,"HiFileManager Init New");
        tabButton = (AnimationButton) findViewById(R.id.btn_local_explorer);
        samBaButton = (AnimationButton) findViewById(R.id.btn_samba_explorer);
        nfsButton = (AnimationButton) findViewById(R.id.btn_nfs_explorer);
        UPnPButton = (AnimationButton) findViewById(R.id.btn_upnp_explorer);
        baiduPcs = (AnimationButton) findViewById(R.id.btn_baidu_explorer);

        tabButton.setText(getResources().getString(R.string.local_tab_title));
        samBaButton.setText(getResources().getString(R.string.lan_tab_title));
        nfsButton.setText(getResources().getString(R.string.nfs_tab_title));
        UPnPButton.setText(getResources().getString(R.string.upnp_tab_title));
        baiduPcs.setText(getResources().getString(R.string.baidu_pcs));

        tabButton.setImage(R.drawable.tv_play);
        samBaButton.setImage(R.drawable.music_but);
        nfsButton.setImage(R.drawable.dongman);
        UPnPButton.setImage(R.drawable.zongyi);
        baiduPcs.setImage(R.drawable.live);

        tabButton.setImageBg(R.drawable.shadow_transverse);
        nfsButton.setImageBg(R.drawable.shadow_transverse);
        baiduPcs.setImageBg(R.drawable.shadow_transverse);
        if (isDemoMode) {
            samBaButton.setImageBg(R.drawable.shadow_transverse);
            UPnPButton.setImageBg(R.drawable.shadow_transverse);
        } else {
            samBaButton.setImageBg(R.drawable.shadow_square);
            UPnPButton.setImageBg(R.drawable.shadow_square);
        }

        tabButton.setOnClickListener(this);
        samBaButton.setOnClickListener(this);
        nfsButton.setOnClickListener(this);
        UPnPButton.setOnClickListener(this);
        baiduPcs.setOnClickListener(this);

        tabButton.setOnKeyListener(this);
        samBaButton.setOnKeyListener(this);
        nfsButton.setOnKeyListener(this);
        UPnPButton.setOnKeyListener(this);
        baiduPcs.setOnKeyListener(this);

        // 倒影图片
        refImageView[0] = (ImageView) findViewById(R.id.hot_reflected_img_0);
        refImageView[1] = (ImageView) findViewById(R.id.hot_reflected_img_1);
        refImageView[2] = (ImageView) findViewById(R.id.hot_reflected_img_2);


        ReflectedImage();
        mIntenFilter = new IntentFilter();
        mIntenFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mIntenFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntenFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        mIntenFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mIntenFilter.addAction("android.net.conn.INET_CONDITION_ACTION");

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
//              if (0 != tabHost.getCurrentTab()) {
                boolean bIsConnect = true;
                final String action = intent.getAction();
                if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    ConnectivityManager manager = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                    bIsConnect = activeNetwork==null?false:activeNetwork.isConnected();
                } else if (action
                        .equals(ConnectivityManager.CONNECTIVITY_ACTION)
                        || action
                        .equals("android.net.conn.INET_CONDITION_ACTION")) {
                    ConnectivityManager manager = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                    bIsConnect = activeNetwork==null?false:activeNetwork.isConnected();
                }
                if (false == bIsConnect) {
                    Toast.makeText(
                            TabBarExample.this,
                            getString(R.string.network_error_exitnetbrowse),
                            Toast.LENGTH_LONG).show();
                }
            }
        };
        registerReceiver(mReceiver, mIntenFilter);
        tabButton.requestFocus();
    }

    @Override
    protected void onResume() {
        if ("true".equals(SystemProperties.get("ro.samba.quick.search"))) {
            Intent sambaService = new Intent(TabBarExample.this,
                    SambaService.class);
            startService(sambaService);
        }
        super.onResume();
    }

    private void ReflectedImage() {
        if (isDemoMode) {
            refImageView[0].setImageBitmap(ImageReflect
                    .createCutReflectedImage(ImageReflect.convertViewToBitmap(
                            TabBarExample.this, R.drawable.live), 0, (int) getResources().getDimension(R.dimen.ref_image_live_width)));
            refImageView[1].setImageBitmap(ImageReflect
                    .createCutReflectedImage(ImageReflect.convertViewToBitmap(
                            TabBarExample.this, R.drawable.zongyi), 0, (int) getResources().getDimension(R.dimen.ref_image_zongyi_width)));
            refImageView[2].setImageBitmap(ImageReflect
                    .createCutReflectedImage(ImageReflect.convertViewToBitmap(
                            TabBarExample.this, R.drawable.music_but), 0, (int) getResources().getDimension(R.dimen.ref_image_music_but_width)));
        } else {
            refImageView[0].setImageBitmap(ImageReflect
                    .createCutReflectedImage(ImageReflect.convertViewToBitmap(
                            TabBarExample.this, R.drawable.dongman), 0, (int) getResources().getDimension(R.dimen.ref_image_live_width)));
            refImageView[1].setImageBitmap(ImageReflect
                    .createCutReflectedImage(ImageReflect.convertViewToBitmap(
                            TabBarExample.this, R.drawable.music_but), 0, (int) getResources().getDimension(R.dimen.ref_image_zongyi_width)));
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.i(TAG, "KeyCode " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        focusViewId = v.getId();
        switch (focusViewId) {
            case R.id.btn_local_explorer:
                Intent tabIntent = new Intent(TabBarExample.this, MainExplorerActivity.class);
                startActivity(tabIntent);
                break;

            case R.id.btn_samba_explorer:
                if (!IsNetworkDisconnect()) {
                    Intent sambaIntent = new Intent(TabBarExample.this, SambaActivity.class);
                    startActivity(sambaIntent);
                }
                break;

            case R.id.btn_nfs_explorer:
                Toast.makeText(TabBarExample.this, getString(R.string.no_activity_info), Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_upnp_explorer:
                try {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.hisilicon.dlna.dmp", "com.hisilicon.dlna.dmp.HiDMPActivity");
                    intent.setComponent(componentName);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(TabBarExample.this, getString(R.string.no_activity_info), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_baidu_explorer:
                try {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.hisilicon.baidu.pcs", "com.hisilicon.baidu.pcs.MainActivity");
                    intent.setComponent(componentName);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(TabBarExample.this, getString(R.string.no_activity_info), Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    public boolean IsNetworkDisconnect() {
        boolean bIsConnect = false;
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (null != mConnectivityManager) {
            NetworkInfo tmpInfo = mConnectivityManager.getActiveNetworkInfo();
            if ((null == tmpInfo) || (false == tmpInfo.isConnected())) {
                new AlertDialog.Builder(TabBarExample.this, AlertDialog.THEME_HOLO_DARK)
                        .setMessage(getString(R.string.network_error_notbrowse))
                        .setPositiveButton(getString(R.string.close), null)
                        .create().show();
                bIsConnect = true;
            }
        }
        return bIsConnect;
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Intent sambaService = new Intent(TabBarExample.this, SambaService.class);
        stopService(sambaService);
    }
}

