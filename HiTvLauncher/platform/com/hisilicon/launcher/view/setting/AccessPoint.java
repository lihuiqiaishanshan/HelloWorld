package com.hisilicon.launcher.view.setting;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import com.hisilicon.launcher.util.LogHelper;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.view.setting.Summary;

import java.util.Map;

public class AccessPoint implements Comparable<Object> {
    private static final String TAG = "AccessPoint";

    private Context mContext;
    /**
     * Lower bound on the 2.4 GHz (802.11b/g/n) WLAN channels
     */
    public static final int LOWER_FREQ_24GHZ = 2400;
    /**
     * Upper bound on the 2.4 GHz (802.11b/g/n) WLAN channels
     */
    public static final int HIGHER_FREQ_24GHZ = 2500;
    /**
     * Lower bound on the 5.0 GHz (802.11a/h/j/n/ac) WLAN channels
     */
    public static final int LOWER_FREQ_5GHZ = 4900;
    /**
     * Upper bound on the 5.0 GHz (802.11a/h/j/n/ac) WLAN channels
     */
    public static final int HIGHER_FREQ_5GHZ = 5900;
    /**
     * Experimental: we should be able to show the user the list of BSSIDs and bands
     *  for that SSID.
     *  For now this data is used only with Verbose Logging so as to show the band and number
     *  of BSSIDs on which that network is seen.
     */
    public LruCache<String, ScanResult> mScanResultCache;
    private static final String KEY_NETWORKINFO = "key_networkinfo";
    private static final String KEY_WIFIINFO = "key_wifiinfo";
    private static final String KEY_SCANRESULT = "key_scanresult";
    private static final String KEY_CONFIG = "key_config";

    private static final int[] STATE_SECURED = {
        R.attr.state_encrypted
    };
    private static final int[] STATE_NONE = {};

    /**
     * These values are matched in string arrays -- changes must be kept in sync
     */
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    public String ssid;
    public String bssid;
    public int security;
    public int networkId = -1;
    boolean wpsAvailable = false;
    boolean showSummary = true;

    private String mAccessPointSummary ="";

    PskType pskType = PskType.UNKNOWN;

    private WifiConfiguration mConfig;
    /* package */ScanResult mScanResult;

    private int mRssi = Integer.MAX_VALUE;
    private long mSeen = 0;
    private WifiInfo mInfo;
    private NetworkInfo mNetworkInfo;
    private TextView mSummaryView;

    private static final int VISIBILITY_MAX_AGE_IN_MILLI = 1000000;
    private static final int VISIBILITY_OUTDATED_AGE_IN_MILLI = 20000;
    private static final int SECOND_TO_MILLI = 1000;
    static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public String getSecurityString() {
        switch (security) {
            case SECURITY_EAP:
                return mContext.getString(R.string.wifi_security_eap);
            case SECURITY_PSK:
                switch (pskType) {
                    case WPA:
                        return mContext.getString(R.string.wifi_security_wpa);
                    case WPA2:
                        return mContext.getString(R.string.wifi_security_wpa2);
                    case WPA_WPA2:
                        return mContext.getString(R.string.wifi_security_wpa_wpa2);
                    case UNKNOWN:
                    default:
                        return mContext.getString(R.string.wifi_security_psk_generic);
                }
            case SECURITY_WEP:
                return mContext.getString(R.string.wifi_security_wep);
            case SECURITY_NONE:
            default:
                return mContext.getString(R.string.wifi_security_none);
        }
    }

    private static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            LogHelper.d(TAG, "Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }

    AccessPoint(Context context, WifiConfiguration config) {
        mContext = context;
        loadConfig(config);
        refresh();
    }

    AccessPoint(Context context, ScanResult result) {
        mContext = context;
        loadResult(result);
        refresh();
    }

    AccessPoint(Context context, Bundle savedState) {
        mContext = context;
        mConfig = savedState.getParcelable(KEY_CONFIG);
        if (mConfig != null) {
            loadConfig(mConfig);
        }
        mScanResult = (ScanResult) savedState.getParcelable(KEY_SCANRESULT);
        if (mScanResult != null) {
            loadResult(mScanResult);
        }
        mInfo = (WifiInfo) savedState.getParcelable(KEY_WIFIINFO);
        if (savedState.containsKey(KEY_NETWORKINFO)) {
            mNetworkInfo = savedState.getParcelable(KEY_NETWORKINFO);
        }
        update(mInfo, mNetworkInfo);
    }
    public void saveWifiState(Bundle savedState) {
        savedState.putParcelable(KEY_CONFIG, mConfig);
        savedState.putParcelable(KEY_SCANRESULT, mScanResult);
        savedState.putParcelable(KEY_WIFIINFO, mInfo);
        if (mNetworkInfo != null) {
            savedState.putParcelable(KEY_NETWORKINFO, mNetworkInfo);
        }
    }
    private void loadConfig(WifiConfiguration config) {
        ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        bssid = config.BSSID;
        security = getSecurity(config);
        networkId = config.networkId;
        mConfig = config;
    }

    private void loadResult(ScanResult result) {
        ssid = result.SSID;
        bssid = result.BSSID;
        security = getSecurity(result);
        wpsAvailable = security != SECURITY_EAP && result.capabilities.contains("WPS");
        if (security == SECURITY_PSK)
            pskType = getPskType(result);
        mRssi = result.level;
        mScanResult = result;
        if (result.seen > mSeen) {
            mSeen = result.seen;
        }
    }

    public int compareTo(Object otherAP) {
        if (!(otherAP instanceof AccessPoint)) {
            return 1;
        }
        AccessPoint other = (AccessPoint) otherAP;
        // Active one goes first.
        if (isActive() && !other.isActive()) return -1;
        if (!isActive() && other.isActive()) return 1;

        // Reachable one goes before unreachable one.
        if (mRssi != Integer.MAX_VALUE && other.mRssi == Integer.MAX_VALUE)
            return -1;
        if (mRssi == Integer.MAX_VALUE && other.mRssi != Integer.MAX_VALUE)
            return 1;

        // Configured one goes before unconfigured one.
        if (networkId != WifiConfiguration.INVALID_NETWORK_ID
                && other.networkId == WifiConfiguration.INVALID_NETWORK_ID)
            return -1;
        if (networkId == WifiConfiguration.INVALID_NETWORK_ID
                && other.networkId != WifiConfiguration.INVALID_NETWORK_ID)
            return 1;

        // Sort by signal strength.
        int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
        if (difference != 0) {
            return difference;
        }
        // Sort by ssid.
        return ssid.compareToIgnoreCase(other.ssid);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AccessPoint)) return false;
        return (this.compareTo((AccessPoint) other) == 0);
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (mInfo != null) result += 13 * mInfo.hashCode();
        result += 19 * mRssi;
        result += 23 * networkId;
        result += 29 * ssid.hashCode();
        return result;
    }

    boolean update(ScanResult result) {
        if (result.seen > mSeen) {
            mSeen = result.seen;
        }
        if (ssid.equals(result.SSID) && security == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                int oldLevel = getLevel();
                mRssi = result.level;
            }
            // This flag only comes from scans, is not easily saved in config
            if (security == SECURITY_PSK) {
                pskType = getPskType(result);
            }
            mScanResult = result;
            refresh();
            return true;
        }
        return false;
    }

    /** Return whether the given {@link WifiInfo} is for this access point. */
    private boolean isInfoForThisAccessPoint(WifiInfo info) {
        if (networkId != WifiConfiguration.INVALID_NETWORK_ID) {
            return networkId == info.getNetworkId();
        } else {
            // Might be an ephemeral connection with no WifiConfiguration. Try matching on SSID.
            // (Note that we only do this if the WifiConfiguration explicitly equals INVALID).
            // TODO: Handle hex string SSIDs.
            return ssid.equals(removeDoubleQuotes(info.getSSID()));
        }
    }
    final void update(WifiInfo info, NetworkInfo networkInfo) {
        boolean reorder = false;
        if (info != null && isInfoForThisAccessPoint(info)) {
            reorder = (mInfo == null);
            mRssi = info.getRssi();
            mInfo = info;
            mNetworkInfo = networkInfo;
            refresh();
        } else if (mInfo != null) {
            reorder = true;
            mInfo = null;
            mNetworkInfo = null;
            refresh();
        }
    }

    public int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, 4);
    }

    public WifiConfiguration getConfig() {
        return mConfig;
    }

    public WifiInfo getInfo() {
        return mInfo;
    }

    NetworkInfo getNetworkInfo() {
        return mNetworkInfo;
    }
    DetailedState getState() {
        return mNetworkInfo != null ? mNetworkInfo.getDetailedState() : null;
    }

    public static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    /**
     * Shows or Hides the Summary of an AccessPoint.
     *
     * @param showSummary true will show the summary, false will hide the summary
     */
    public void setShowSummary(boolean showSummary) {
        this.showSummary = showSummary;
        if (mSummaryView != null) {
            mSummaryView.setVisibility(showSummary ? View.VISIBLE : View.GONE);
        } // otherwise, will be handled in onBindView.
    }

    /**
     * Return whether this is the active connection.
     * For ephemeral connections (networkId is invalid), this returns false if the network is
     * disconnected.
     */
    boolean isActive() {
        return mNetworkInfo != null &&
                (networkId != WifiConfiguration.INVALID_NETWORK_ID ||
                 mNetworkInfo.getState() != State.DISCONNECTED);
    }
    /** Updates the title and summary; may indirectly call notifyChanged() */
    private void refresh() {
        mAccessPointSummary = "";
        StringBuilder summary = new StringBuilder();
        if (isActive()) { // This is the active connection
            summary.append(Summary.get(mContext, getState(),
                    networkId == WifiConfiguration.INVALID_NETWORK_ID));
        } else if (mConfig != null
                && mConfig.hasNoInternetAccess()) {
            summary.append(mContext.getString(R.string.wifi_no_internet));
        } else if (mConfig != null && !mConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
            WifiConfiguration.NetworkSelectionStatus networkStatus =
                    mConfig.getNetworkSelectionStatus();
            switch (networkStatus.getNetworkSelectionDisableReason()) {
                case WifiConfiguration.NetworkSelectionStatus.DISABLED_AUTHENTICATION_FAILURE:
                    summary.append(mContext.getString(R.string.wifi_disabled_password_failure));
                    break;
                case WifiConfiguration.NetworkSelectionStatus.DISABLED_DHCP_FAILURE:
                case WifiConfiguration.NetworkSelectionStatus.DISABLED_DNS_FAILURE:
                /*case WifiConfiguration.NetworkSelectionStatus.DISABLED_PPPOE_FAILURE:
                    summary.append(mContext.getString(R.string.wifi_disabled_network_failure));
                    break;*/
                case WifiConfiguration.NetworkSelectionStatus.DISABLED_ASSOCIATION_REJECTION:
                    summary.append(mContext.getString(R.string.wifi_disabled_generic));
                    break;
                /*case WifiConfiguration.NetworkSelectionStatus.DISABLED_PPPOE_AUTH_FAILURE:
                    summary.append(mContext.getString(R.string.wifi_pppoe_auth_failure));
                    break;*/
            }
        } else if (mRssi == Integer.MAX_VALUE) { // Wifi out of range
            summary.append(mContext.getString(R.string.wifi_not_in_range));
        } else { // In range, not disabled.
            if (mConfig != null) { // Is saved network
                summary.append(mContext.getString(R.string.wifi_remembered));
            }
        }

        if (summary.length() > 0) {
            mAccessPointSummary = summary.toString();
            setShowSummary(true);
        } else {
            setShowSummary(false);
        }
    }

    public String getSummary() {
        return mAccessPointSummary;
    }

    /**
     * Generate and save a default wifiConfiguration with common values. Can
     * only be called for unsecured networks.
     *
     * @hide
     */
    protected void generateOpenNetworkConfig() {
        if (security != SECURITY_NONE)
            throw new IllegalStateException();
        if (mConfig != null)
            return;
        mConfig = new WifiConfiguration();
        mConfig.SSID = AccessPoint.convertToQuotedString(ssid);
        mConfig.allowedKeyManagement.set(KeyMgmt.NONE);
    }
}
