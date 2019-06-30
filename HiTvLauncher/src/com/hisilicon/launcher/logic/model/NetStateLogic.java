
package com.hisilicon.launcher.logic.model;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.EthernetManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.hisilicon.launcher.util.LogHelper;
import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.EtherInterface;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.util.Constant;

/**
 * net state
 *
 * @author wangchuanjian
 */
public class NetStateLogic implements InterfaceLogic {

    private Context mContext;
    private static final String TAG = "NetStateLogic";

    public NetStateLogic(Context mContext) {
        super();
        this.mContext = mContext;
        refreshNetStat();
    }

    @Override
    public List<WidgetType> getWidgetTypeList() {
        List<WidgetType> mWidgetList = new ArrayList<WidgetType>();
        Resources res = mContext.getResources();

        WidgetType mInfo = null;
        // local connection
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.net_state)[0]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // access to the local connection state, the state setInfoo
        mInfo.setInfo(res.getString(isEtherConnected && !isPppoeConnected ? R.string.netstat_connected
                : R.string.netstat_unconnected));
        mWidgetList.add(mInfo);

        // broadband dial-up
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.net_state)[1]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // access to broadband dial-up state, the state setInfo
        mInfo.setInfo(res.getString(isPppoeConnected ? R.string.netstat_connected
                : R.string.netstat_unconnected));
        mWidgetList.add(mInfo);

        // wireless connection
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.net_state)[2]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // access to wireless connection to the state, the state setInfo
        mInfo.setInfo(res.getString(isWifiConnected ? R.string.netstat_connected
                : R.string.netstat_unconnected));
        mWidgetList.add(mInfo);

        // Broadband:
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.net_state)[3]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // access to broadband address of the state, the state setInfo
        mInfo.setInfo(mIpAddress == null ? "" : mIpAddress);
        mWidgetList.add(mInfo);

        // subnet mask:
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.net_state)[4]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // access to the subnet mask of the state, the state setInfo
        mInfo.setInfo(mNetMask == null ? "" : mNetMask);
        mWidgetList.add(mInfo);

        // Gateway:
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.net_state)[5]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // access to the gateway of the state, the state setInfo
        mInfo.setInfo(mGateway == null ? "" : mGateway);
        mWidgetList.add(mInfo);

        // DNS:
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.net_state)[6]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // DNS access to the state, the state setInfo
        mInfo.setInfo(mDNS == null ? "" : mDNS);
        mWidgetList.add(mInfo);

        return mWidgetList;
    }

    @Override
    public void setHandler(Handler handler) {
    }

    @Override
    public void dismissDialog() {
        // TODO Auto-generated method stub
    }

    private boolean isEtherConnected = false;
    private boolean isPppoeConnected = false;
    private boolean isWifiConnected = false;
    private String mIpAddress = null;
    private String mNetMask = null;
    private String mGateway = null;
    private String mDNS = null;

    private void refreshNetStat() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo ethInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            EthernetManager ethernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
            if (ethernetManager != null) {
                String ethMode = ethernetManager.getEthernetMode();
                if (EthernetManager.ETHERNET_CONNECT_MODE_MANUAL.equals(ethMode)
                        || EthernetManager.ETHERNET_CONNECT_MODE_DHCP.equals(ethMode)) {
                    isEtherConnected = true;
                    isPppoeConnected = false;
                } else if (EthernetManager.ETHERNET_CONNECT_MODE_PPPOE.equals(ethMode)) {
                    isPppoeConnected = true;
                    isEtherConnected = false;
                }
            }
            NetworkInfo wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiInfo != null) {
                isWifiConnected = wifiInfo.isConnected();
            }
        }
        LogHelper.d(TAG," isEtherConnected = "+isEtherConnected+ " isWifiConnected = "+isWifiConnected + ",isPppoeConnected = " + isPppoeConnected);
        if (isEtherConnected || isPppoeConnected) {
            DhcpInfo dhcpInfo = EtherInterface.getDhcpInfo(mContext);
            if (dhcpInfo != null) {
                mIpAddress = NetworkUtils.intToInetAddress(dhcpInfo.ipAddress).getHostAddress();
                mNetMask = NetworkUtils.intToInetAddress(dhcpInfo.netmask).getHostAddress();
                mGateway = NetworkUtils.intToInetAddress(dhcpInfo.gateway).getHostAddress();
                mDNS = NetworkUtils.intToInetAddress(dhcpInfo.dns1).getHostAddress();
            }
        }

        if (isWifiConnected && !isEtherConnected && !isPppoeConnected
                && (mIpAddress == null || mIpAddress.equals("") || mIpAddress.equals("0.0.0.0"))) {
            WifiManager mWifiManager = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
            if (dhcpInfo != null) {
                mIpAddress = NetworkUtils.intToInetAddress(dhcpInfo.ipAddress).getHostAddress();
                mGateway = NetworkUtils.intToInetAddress(dhcpInfo.gateway).getHostAddress();
                mDNS = NetworkUtils.intToInetAddress(dhcpInfo.dns1).getHostAddress();
            }

            //get prefixLength rather than netMask in wi-fi environment
            int prefix = 0;
            ConnectivityManager mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network mNetwork = mWifiManager.getCurrentNetwork();
            if (mNetwork != null) {
                LinkProperties mLinkProperties = mConnectivityManager.getLinkProperties(mNetwork);
                if (mLinkProperties != null) {
                    for (LinkAddress addr : mLinkProperties.getLinkAddresses()) {
                        if (addr.getAddress() instanceof Inet4Address) {
                            prefix = addr.getPrefixLength();
                        }
                    }
                }
            }
            mNetMask = ipv4PrefixLengthToSubnetMask(prefix);
        }
    }

    private String ipv4PrefixLengthToSubnetMask(int prefixLength) {
        prefixLength = Math.max(Math.min(prefixLength, 32), 0);
        int[] masks = new int[4];
        for (int i = 0; i < prefixLength; i++) {
            masks[i / 8] = masks[i / 8] << 1 | 0x1;
        }
        return String.format("%d.%d.%d.%d", masks[0], masks[1], masks[2], masks[3]);
    }
}
