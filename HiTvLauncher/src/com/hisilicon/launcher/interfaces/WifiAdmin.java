package com.hisilicon.launcher.interfaces;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.launcher.view.setting.AccessPoint;

public class WifiAdmin {

    private static final String TAG = "WifiAdmin";

    // define wifiManager object
    private WifiManager mWifiManager;
    // define WifiInfo object
    private WifiInfo mWifiInfo;
    // scan the list of network connections
    private List<ScanResult> mWifiList;
    // configuration has good,and not to scan the list of network connections
    private List<WifiConfiguration> mWifiConfiguration = new ArrayList<WifiConfiguration>();
    // define a WifiLock
    private WifiLock mWifiLock;

    public WifiAdmin(WifiManager wifi) {
        // get WifiManager object
        mWifiManager = wifi;
        if (mWifiManager != null) {
            // get WifiInfo object
            mWifiInfo = mWifiManager.getConnectionInfo();
        }
    }

    /**
     * open WIFI
     */
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
            mWifiManager.startScan();
        }
    }

    /**
     * WIFI is available to determine
     *
     * @return
     */
    public boolean isWifiOpen() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * close WIFI
     *
     * @return
     */
    public boolean closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            return mWifiManager.setWifiEnabled(false);
        } else {
            return true;
        }
    }

    /**
     * check WIFI state
     *
     * @return
     */
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    /**
     * acquire WifiLock
     */
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    /**
     * relese WifiLock
     */
    public void releaseWifiLock() {
        // When judging the lock
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    /**
     * create a WifiLock
     */
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    /**
     * have been configured,without scanning the list of network connections
     *
     * @return
     */
    public List<WifiConfiguration> getConfiguration() {
        mWifiConfiguration.clear();
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        if (null != list) {
            for (WifiConfiguration wifiConfiguration : list) {
                boolean wifiHas = false;
                for (ScanResult result : mWifiList) {
                    if (wifiConfiguration.SSID.substring(1,
                            wifiConfiguration.SSID.length() - 1)
                            .equals(result.SSID)) {
                        wifiHas = true;
                        break;
                    }
                }
                if (!wifiHas) {
                    mWifiConfiguration.add(wifiConfiguration);
                }
            }
        }
        return mWifiConfiguration;
    }

    /**
     * to connect to the specified configured network
     *
     * @param index
     */
    public void connectConfiguration(int index) {
        // the index is greater than the network configured to return index
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // connect to the specified ID configured network
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
                true);
    }

    /**
     * start to scan
     *
     * @return
     */
    public boolean startScan() {
        return mWifiManager.startScan();
    }

    /**
     * look up scan result
     *
     * @return
     */
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder.append("Index_" + Integer.valueOf(i + 1).toString()
                    + ":");
            // ScanResult information is converted to a string
            // These include：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }

    /**
     * get MAC address
     *
     * @return
     */
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    /**
     * get the access point BSSID
     *
     * @return
     */
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    /**
     * get IP address
     *
     * @return
     */
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    /**
     * get connect network ID
     *
     * @return
     */
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /**
     * get all WifiInfo packets
     *
     * @return
     */
    public WifiInfo getWifiInfo() {
        return mWifiInfo;
    }

    /**
     * add a network,but the network is only a temporary existence
     *
     * @param wcg
     * @return
     */
    public int createNetworkID(WifiConfiguration wcg) {
        return mWifiManager.addNetwork(wcg);
    }

    /**
     * add a network.and save
     *
     * @param wcg
     * @return
     */
    public int createAndSaveNetworkID(WifiConfiguration wcg) {
        int networkID = mWifiManager.addNetwork(wcg);
        mWifiManager.saveConfiguration();
        return networkID;
    }

    /**
     * add a network and connect, connection is successful will save
     *
     * @param networkID
     * @param disableOthers
     * @return
     */
    public boolean addNetwork(int networkID, boolean disableOthers) {
        if (mWifiManager.enableNetwork(networkID, disableOthers)) {
            mWifiManager.saveConfiguration();
            mWifiManager.reassociate();
            return true;
        } else
            return false;
    }

    /**
     * disconnect wifi
     *
     * @param netId
     */
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    /**
     * create WifiConfiguration object for connect
     *
     * @param SSID
     * @param Password
     * @param Type
     * @return
     */
    public WifiConfiguration createWifiInfo(String SSID, String Password,
            int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedKeyManagement.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type == AccessPoint.SECURITY_NONE) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (Type == AccessPoint.SECURITY_WEP) {
            config.hiddenSSID = true;
            config.preSharedKey = "\"" + Password + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (Type == AccessPoint.SECURITY_PSK) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        } else if (Type == AccessPoint.SECURITY_EAP) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        }
        config.priority = 60;
        config.status = WifiConfiguration.Status.ENABLED;
        return config;
    }

    /**
     * judge whether wifi connection
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && wifiInfo != null) {
            State state = wifiInfo.getState();
            LogHelper.d(TAG, "State.CONNECTED == state  : "
                    + (State.CONNECTED == state) + " isConnected() : "
                    + wifiInfo.isConnected());
            if (State.CONNECTED == state) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
