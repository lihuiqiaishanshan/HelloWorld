package com.hisilicon.launcher.model;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.WifiAdmin;
import com.hisilicon.launcher.view.setting.AccessPoint;

public class WifiBaseAdapter extends BaseAdapter {

    private Context mContext;
    // to fill xml
    private LayoutInflater mLayoutInflater;
    // the list of ScanResult
    private List<AccessPoint> mWifiList;
    // the list of WifiConfiguration
    private List<WifiConfiguration> mConfigurations;
    // WifiManager object
    private WifiManager mWifiManager;

    // the array of wifi Unlock image
    private int[] wifiUnlockImg = new int[] { R.drawable.wifi_unlock_signal_2,
            R.drawable.wifi_unlock_signal_3, R.drawable.wifi_unlock_signal_4,
            R.drawable.wifi_unlock_signal_5 };
    // the array of wifi locked image
    private int[] wifiLockedImg = new int[] { R.drawable.wifi_locked_signal_2,
            R.drawable.wifi_locked_signal_3, R.drawable.wifi_locked_signal_4,
            R.drawable.wifi_locked_signal_5 };

    public WifiBaseAdapter(Context context, List<AccessPoint> wifiList,
            List<WifiConfiguration> listConfiguration, WifiManager wifiManager) {
        this.mContext = context;
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mConfigurations = listConfiguration;
        this.mWifiList = wifiList;
        this.mWifiManager = wifiManager;
    }

    @Override
    public int getCount() {
        return mWifiList.size();
    }

    @Override
    public Object getItem(int position) {
        return mWifiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        synchronized (this) {
            ViewHolder viewHolder = new ViewHolder();
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.wifi_item, null);
                viewHolder.wifiSSID = (TextView) convertView
                        .findViewById(R.id.wifi_signal_ssid);
                viewHolder.wifiDescribe = (TextView) convertView
                        .findViewById(R.id.wifi_signal_describe);
                viewHolder.wifiConnected = (ImageView) convertView
                        .findViewById(R.id.wifi_signal_connected);
                viewHolder.wifiState = (ImageView) convertView
                        .findViewById(R.id.wifi_signal_state);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Wireless network scanning to
            AccessPoint result = mWifiList.get(position);
            viewHolder.wifiSSID.setText(result.ssid);
            viewHolder.wifiDescribe.setText(result.getSummary());
            String ssid = mWifiManager.getConnectionInfo().getSSID();
            int networkId = mWifiManager.getConnectionInfo().getNetworkId();
            ssid = AccessPoint.removeDoubleQuotes(ssid);

            if (ssid != null && ssid.equals(result.ssid)
                    && WifiAdmin.isWifiConnected(mContext)
                    && result.networkId == networkId) {
                viewHolder.wifiConnected.setVisibility(View.VISIBLE);
            } else {
                viewHolder.wifiConnected.setVisibility(View.INVISIBLE);
            }

            int level = result.getLevel();
            if (level > -1) {
                if (result.security == AccessPoint.SECURITY_NONE) {
                    viewHolder.wifiState.setImageResource(wifiUnlockImg[level]);
                } else {
                    viewHolder.wifiState.setImageResource(wifiLockedImg[level]);
                }
            } else {
                viewHolder.wifiState.setVisibility(View.INVISIBLE);
            }
            viewHolder.wifiState.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    private class ViewHolder {
        // wifi SSID
        TextView wifiSSID;
        // the describe of wifi
        TextView wifiDescribe;
        // wifi connect image
        ImageView wifiConnected;
        // wifi state image
        ImageView wifiState;
    }

}
