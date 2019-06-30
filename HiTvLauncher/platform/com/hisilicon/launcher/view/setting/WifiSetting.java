package com.hisilicon.launcher.view.setting;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.SupplicantState;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WpsInfo;
import android.os.Handler;
import android.os.Message;
import com.hisilicon.launcher.util.LogHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.WifiAdmin;
import com.hisilicon.launcher.model.WifiBaseAdapter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wireless network settings menu three
 *
 * @author huyq
 */
public class WifiSetting extends LinearLayout implements
        View.OnFocusChangeListener, DialogInterface.OnClickListener {
    private final static String TAG = "WifiSetting";

    // Combo scans can take 5-6s to complete - set to 10s.
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;

    private Context mContext;
    private WifiAdmin mWifiAdmin;
    private WifiManager mWifiManager;
    private WifiBaseAdapter mWifiBaseAdapter;

    // scan wifi connect list
    private List<AccessPoint> mWifiList;
    // Not to scan and properly configured WiFi connection list
    private List<WifiConfiguration> mConfigList;
    // The selected scan WiFi connection
    private AccessPoint mSelectedWifiItem;
    // Control NetSettingDialog display content
    private WifiDialog mDialog;
    private NetStateDialog mNetStateDialog;

    private NetworkInfo mLastNetworkInfo;
    private WifiInfo mLastInfo;

    private TextView mEmptyText;
    // wifi switch
    private CheckBox mSwitchCb;
    // listView of wifi
    private ListView mWifiListView;
    // text of refresh
    private TextView mRefreshText;
    // text of add
    private TextView mAddText;
    // text of direct
    private TextView mDirectText;
    private Toast mToast;
    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;
    private final Scanner mScanner;

    private final AtomicBoolean mConnected = new AtomicBoolean(false);
    private int mPosition = 0;
    public WifiSetting(Context context, Handler handler) {
        super(context);
        this.mContext = context;

        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
        mScanner = new Scanner();

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiAdmin = new WifiAdmin(mWifiManager);
        LayoutInflater inflater = LayoutInflater.from(context);
        View parent = inflater.inflate(R.layout.setting_wifi, this);
        initView(parent);
        mSwitchCb.requestFocus();
        mSwitchCb.setClickable(false);
        refreshList();
    }

    private void initView(View parent) {
        mEmptyText = (TextView) parent.findViewById(R.id.empty_wifi_txt);
        // wifi switch
        mSwitchCb = (CheckBox) parent.findViewById(R.id.wifi_switch_cb);
        // wifi list
        mWifiListView = (ListView) parent.findViewById(R.id.wifi_signal_list);
        mWifiListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                    long arg3) {
                selectWifiItem(position);
            }
        });
        mWifiListView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                    long id) {
                mPosition = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPosition = -1;
            }
        });
        // wifi refresh
        mRefreshText = (TextView) parent.findViewById(R.id.wifi_refresh_txt);
        mRefreshText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                refreshList();
            }
        });
        mRefreshText.setOnFocusChangeListener(this);
        // wifi add
        mAddText = (TextView) parent.findViewById(R.id.wifi_add_txt);
        mAddText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                mSelectedWifiItem = null;
                mDialog = new WifiDialog(mContext, WifiSetting.this,
                        mSelectedWifiItem, true);
                mDialog.show();
            }
        });
        mAddText.setOnFocusChangeListener(this);
        // wifi direct connect
        mDirectText = (TextView) parent.findViewById(R.id.wifi_direct_txt);
        mDirectText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WpsDialog wpsDialog = new WpsDialog(mContext, WpsInfo.PBC);
                wpsDialog.show();
                // TODO Do not need to click the effect
            }
        });
        mDirectText.setOnFocusChangeListener(this);
        getContext().registerReceiver(mReceiver, mFilter);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int wifiState = mWifiManager.getWifiState();
            //if wifi is opening or stopping,not response
            if (WifiManager.WIFI_STATE_ENABLING != wifiState && WifiManager.WIFI_STATE_DISABLING != wifiState){
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        LogHelper.d(TAG, "KeyEvent.KEYCODE_DPAD_LEFT");
                        if (mSwitchCb.isFocused() && mSwitchCb.isChecked()) {
                            mSwitchCb.setChecked(false);
                            mWifiAdmin.closeWifi();
                            LogHelper.d(TAG, "KeyEvent.KEYCODE_DPAD_LEFT--close");
                        }
                        break;

                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        LogHelper.d(TAG, "KeyEvent.KEYCODE_DPAD_RIGHT");
                        if (mSwitchCb.isFocused() && !mSwitchCb.isChecked()) {
                            mSwitchCb.setChecked(true);
                            mWifiAdmin.openWifi();
                            LogHelper.d(TAG, "KeyEvent.KEYCODE_DPAD_RIGHT--open");
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        LogHelper.d(TAG, "KeyEvent.KEYCODE_DPAD_CENTER");
                        if (mSwitchCb.isFocused()) {
                            if (mSwitchCb.isChecked()) {
                                mSwitchCb.setChecked(false);
                                mWifiAdmin.closeWifi();
                                LogHelper.d(TAG, "KeyEvent.KEYCODE_DPAD_CENTER--close");
                            } else {
                                mSwitchCb.setChecked(true);
                                mWifiAdmin.openWifi();
                                LogHelper.d(TAG, "KeyEvent.KEYCODE_DPAD_CENTER--open");
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    /**
     * connect the selected item
     */
    private void selectWifiItem(int position) {
        // TODO Auto-generated method stub
        if (position < mWifiList.size()) {
            mSelectedWifiItem = mWifiList.get(position);
            if (mSelectedWifiItem.security == AccessPoint.SECURITY_NONE && mSelectedWifiItem.networkId == INVALID_NETWORK_ID) {
                mSelectedWifiItem.generateOpenNetworkConfig();
                mWifiManager.connect(mSelectedWifiItem.getConfig(), null);
            } else {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                mDialog = new WifiDialog(mContext, this, mSelectedWifiItem,
                        false);
                mDialog.show();
            }
        }
    }

    /**
     * refresh the ui
     *
     * @param isWifiOpen
     */
    private void setViewInvalid(int wifi_state) {
        LogHelper.d(TAG,"handleEvent action setViewInvalid-->" + wifi_state);
        switch (wifi_state) {
        case WifiManager.WIFI_STATE_ENABLED:
            LogHelper.d(TAG,"handleEvent action setViewInvalid WIFI_STATE_ENABLED-->");
            mEmptyText.setVisibility(View.GONE);
            mWifiListView.setVisibility(View.VISIBLE);
            mRefreshText.setVisibility(View.VISIBLE);
            mAddText.setVisibility(View.VISIBLE);
            mDirectText.setVisibility(View.VISIBLE);
            mSwitchCb.setChecked(true);
            break;

        case WifiManager.WIFI_STATE_ENABLING:
            LogHelper.d(TAG,"handleEvent action setViewInvalid WIFI_STATE_ENABLING-->");
            mEmptyText.setText(R.string.wifi_starting);
            mEmptyText.setVisibility(View.VISIBLE);
            mWifiListView.setVisibility(View.GONE);
            mRefreshText.setVisibility(View.GONE);
            mAddText.setVisibility(View.GONE);
            mDirectText.setVisibility(View.GONE);
            mSwitchCb.setChecked(true);
            break;

        case WifiManager.WIFI_STATE_DISABLING:
            LogHelper.d(TAG,"handleEvent action setViewInvalid WIFI_STATE_DISABLING-->");
            mEmptyText.setText(R.string.wifi_stoping);
            mEmptyText.setVisibility(View.VISIBLE);
            mWifiListView.setVisibility(View.GONE);
            mWifiListView.setAdapter(null);
            mRefreshText.setVisibility(View.GONE);
            mAddText.setVisibility(View.GONE);
            mDirectText.setVisibility(View.GONE);
            mSwitchCb.setChecked(false);
            break;

        case WifiManager.WIFI_STATE_DISABLED:
            LogHelper.d(TAG,"handleEvent action setViewInvalid WIFI_STATE_DISABLED-->");
            mEmptyText.setText(R.string.wifi_stoped);
            mEmptyText.setVisibility(View.VISIBLE);
            mWifiListView.setVisibility(View.GONE);
            mWifiListView.setAdapter(null);
            mRefreshText.setVisibility(View.GONE);
            mAddText.setVisibility(View.GONE);
            mDirectText.setVisibility(View.GONE);
            mSwitchCb.setChecked(false);
            break;
        case WifiManager.WIFI_STATE_UNKNOWN:
            LogHelper.d(TAG,"handleEvent action setViewInvalid WIFI_STATE_UNKNOWN-->");
            mEmptyText.setVisibility(View.INVISIBLE);
            mWifiListView.setVisibility(View.GONE);
            mWifiListView.setAdapter(null);
            mRefreshText.setVisibility(View.GONE);
            mAddText.setVisibility(View.GONE);
            mDirectText.setVisibility(View.GONE);
            mSwitchCb.setChecked(false);
            if (mToast == null) {
                mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
            }
            mToast.setText(R.string.wifi_state_unknow);
            mToast.show();
            break;
        }
    }

    /**
     * reload and refresh the wifi list
     */
    private void refreshList() {
        final int wifiState = mWifiManager.getWifiState();

        setViewInvalid(wifiState);

        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            mWifiList = constructAccessPoints();
            mWifiBaseAdapter = new WifiBaseAdapter(mContext, mWifiList,
                    mConfigList, mWifiManager);
            mWifiListView.setAdapter(mWifiBaseAdapter);
            mWifiBaseAdapter.notifyDataSetChanged();
        }
        if (mWifiBaseAdapter != null && mWifiBaseAdapter.getCount() > 0) {
            if (mPosition >= 0 && mPosition < mWifiBaseAdapter.getCount()) {
                mWifiListView.setSelection(mPosition);
            } else if (mPosition >= mWifiBaseAdapter.getCount()) {
                mWifiListView.setSelection(mWifiBaseAdapter.getCount() - 1);
            }
        }
    }

    /** Returns sorted list of access points */
    private List<AccessPoint> constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        /**
         * Lookup table to more quickly update AccessPoints by only considering
         * objects with the correct SSID. Maps SSID -> List of AccessPoints with
         * the given SSID.
         */
        Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();

        final List<WifiConfiguration> configs = mWifiManager
                .getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(mContext, config);
                accessPoint.update(mLastInfo, mLastNetworkInfo);
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.ssid, accessPoint);
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0
                        || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(mContext, result);
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.ssid, accessPoint);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion
        Collections.sort(accessPoints);
        return accessPoints;
    }

    /** A restricted multimap for use in constructAccessPoints */
    private class Multimap<K, V> {
        private final HashMap<K, List<V>> store = new HashMap<K, List<V>>();

        /** retrieve a non-null list of values with key K */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V> emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<V>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    /**
     * make the child dialog dismiss
     */
    public void dismissChildDialog() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (null != mNetStateDialog && mNetStateDialog.isShowing()) {
            mNetStateDialog.dismiss();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        LogHelper.d(TAG, "hasFocus:" + hasFocus);
        if (hasFocus) {
            v.setBackgroundResource(R.drawable.launcher_set_focus);
            ((TextView) v).setTextColor(mContext.getResources().getColor(
                    R.color.black));
        } else {
            v.setBackgroundResource(R.drawable.button_transparent);
            ((TextView) v).setTextColor(mContext.getResources().getColor(
                    R.color.white));
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        LogHelper.d(TAG,"handleEvent action-->" + action);
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            LogHelper.d(TAG,"handleEvent action- WIFI_STATE_CHANGED_ACTION");
            setViewInvalid(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)
                || WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION
                        .equals(action)
                || WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            LogHelper.d(TAG,"handleEvent action-WIFI_STATE_CHANGED_ACTION");
            refreshList();
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            LogHelper.d(TAG,"handleEvent action-NETWORK_STATE_CHANGED_ACTION");
            NetworkInfo info = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info != null){
                mConnected.set(info.isConnected());
                updateConnectionState(info);
            }
            refreshList();
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            LogHelper.d(TAG,"handleEvent action-RSSI_CHANGED_ACTION");
            LogHelper.d(TAG,"rssi CHANGED");
            updateConnectionState(null);
            refreshList();
        }
    }

    private void updateConnectionState(NetworkInfo networkInfo) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }

        if (networkInfo != null &&
                networkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (networkInfo != null) {
            mLastNetworkInfo = networkInfo;
        }
        if (mWifiList == null){
            return;
        }
        for (int i = mWifiList.size() - 1; i >= 0; --i) {
            final AccessPoint accessPoint = mWifiList.get(i);
            accessPoint.update(mLastInfo, mLastNetworkInfo);
            mWifiList.set(i, accessPoint);
        }
        mWifiBaseAdapter.notifyDataSetChanged();
    }

    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                if (mContext != null) {
                    Toast.makeText(mContext, R.string.wifi_fail_to_scan,
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == WifiDialog.BUTTON_FORGET && mSelectedWifiItem != null) {
            forget();
        } else if (button == WifiDialog.BUTTON_SUBMIT) {
            if (mDialog != null) {
                submit(mDialog.getController());
            }
        }
    }

    /* package */
     void submit(WifiConfigController configController) {
        final WifiConfiguration config = configController.getConfig();

        if (config == null) {
            if (mSelectedWifiItem != null
                    && mSelectedWifiItem.networkId != INVALID_NETWORK_ID) {
                mWifiManager.connect(mSelectedWifiItem.networkId, null);
            }
        } else if (config.networkId != INVALID_NETWORK_ID) {
            if (mSelectedWifiItem != null) {
                mWifiManager.save(config, null);
            }
        } else {
            if (configController.isEdit()) {
                mWifiManager.save(config, null);
            } else {
                mWifiManager.connect(config, null);
            }
        }
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        refreshList();
    }

    void forget() {
        if (mSelectedWifiItem.networkId == INVALID_NETWORK_ID) {
            // Should not happen, but a monkey seems to triger it
            LogHelper.w(TAG,
                    "Failed to forget invalid network "
                            + mSelectedWifiItem.getConfig());
            return;
        }

        mWifiManager.forget(mSelectedWifiItem.networkId, null);
        refreshList();
    }
}
