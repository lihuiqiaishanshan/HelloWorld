package com.hisilicon.launcher.interfaces;

import android.content.Context;
import android.content.ContentResolver;
import android.net.DhcpInfo;
import android.net.EthernetManager;
import android.net.hipppoe.HiPppoeManager;

public class EtherInterface {
    private static final String TAG = "EtherInterface";
    public static final String ETHERNET_CONNECT_MODE_DHCP = EthernetManager.ETHERNET_CONNECT_MODE_DHCP;
    public static final String ETHERNET_CONNECT_MODE_MANUAL = EthernetManager.ETHERNET_CONNECT_MODE_MANUAL;
    public static final String ETHERNET_STATE_CHANGED_ACTION = EthernetManager.ETHERNET_STATE_CHANGED_ACTION;
    public static final String NETWORK_STATE_CHANGED_ACTION = EthernetManager.NETWORK_STATE_CHANGED_ACTION;
    public static final String EXTRA_ETHERNET_STATE = EthernetManager.EXTRA_ETHERNET_STATE;

    public static final int ETHERNET_STATE_ENABLED = EthernetManager.ETHERNET_STATE_ENABLED;
    public static final int EVENT_DHCP_CONNECT_SUCCESSED = EthernetManager.EVENT_DHCP_CONNECT_SUCCESSED;
    public static final int EVENT_DHCP_DISCONNECT_SUCCESSED = EthernetManager.EVENT_DHCP_DISCONNECT_SUCCESSED;
    public static final int EVENT_STATIC_CONNECT_SUCCESSED = EthernetManager.EVENT_STATIC_CONNECT_SUCCESSED;
    public static final int EVENT_STATIC_DISCONNECT_SUCCESSED = EthernetManager.EVENT_STATIC_DISCONNECT_SUCCESSED;
    public static final int EVENT_PHY_LINK_DOWN = EthernetManager.EVENT_PHY_LINK_DOWN;
    public static final int EVENT_STATIC_DISCONNECT_FAILED = EthernetManager.EVENT_STATIC_DISCONNECT_FAILED;
    public static final int EVENT_PHY_LINK_UP  = EthernetManager.EVENT_PHY_LINK_UP;
    public static final int EVENT_STATIC_CONNECT_FAILED = EthernetManager.EVENT_STATIC_CONNECT_FAILED;
    public static final int EVENT_DHCP_DISCONNECT_FAILED = EthernetManager.EVENT_DHCP_DISCONNECT_FAILED;
    public static final int EVENT_DHCP_CONNECT_FAILED = EthernetManager.EVENT_DHCP_CONNECT_FAILED;

    public static final String PPPOE_STATE_CHANGED_ACTION = HiPppoeManager.PPPOE_STATE_CHANGED_ACTION;
    public static final String EXTRA_PPPOE_STATE = HiPppoeManager.EXTRA_PPPOE_STATE;
    public static final int EVENT_PPPOE_CONNECT_SUCCESSED = HiPppoeManager.EVENT_CONNECT_SUCCESSED;
    public static final int EVENT_PPPOE_CONNECT_FAILED = HiPppoeManager.EVENT_CONNECT_FAILED;
    public static final int EVENT_PPPOE_CONNECTING = HiPppoeManager.EVENT_CONNECTING;
    public static final int EVENT_PPPOE_CONNECT_FAILED_AUTH_FAIL = HiPppoeManager.EVENT_CONNECT_FAILED_AUTH_FAIL;
    public static final int EVENT_PPPOE_AUTORECONNECTING = HiPppoeManager.EVENT_AUTORECONNECTING;
    public static final int EVENT_PPPOE_DISCONNECT_SUCCESSED = HiPppoeManager.EVENT_DISCONNECT_SUCCESSED;
    public static final int EVENT_PPPOE_DISCONNECT_FAILED = HiPppoeManager.EVENT_DISCONNECT_FAILED;

    private static HiPppoeManager mPppoeManager = null;
    private static EthernetManager mEthManager = null;

    static public EthernetManager getEthernetManager(Context context){
        if (mEthManager == null) {
            mEthManager =(EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);
        }
        return mEthManager;
    }

    static public DhcpInfo getDhcpInfo(Context context){
        return getEthernetManager(context).getDhcpInfo();
    }

    static public void startDhcp(Context context){
        setEthernetEnabled(context,false);
        getEthernetManager(context).setEthernetMode(EthernetManager.
                ETHERNET_CONNECT_MODE_DHCP,null);
        setEthernetEnabled(context,true);
    }

    static public void setEthernetDefaultConf(Context context){
        getEthernetManager(context).setEthernetMode(EthernetManager.
                ETHERNET_CONNECT_MODE_DHCP,null);
     }

    static public void setEthernetMode(Context context,String ethernetMode , DhcpInfo di){
        getEthernetManager(context).setEthernetMode(ethernetMode, di);
    }

    static public int getEthernetState(Context context){
        return getEthernetManager(context).getEthernetState();
    }
    static public void setEthernetEnabled(Context context,boolean enabled){
        getEthernetManager(context).setEthernetEnabled(enabled);
    }

    static public int getEthernetPersistedState(Context context){
        return getEthernetManager(context).getEthernetPersistedState();
    }

    static public void enableEthernet(Context context,boolean enable){
        getEthernetManager(context).enableEthernet(enable);
    }

    static public boolean isPppoeSupported(Context context) {
        return getPppoeManager(context) != null;
    }

    static public HiPppoeManager getPppoeManager(Context context) {
        if (mPppoeManager == null) {
            mPppoeManager = (HiPppoeManager) context.getSystemService(Context.PPPOE_SERVICE);
        }
        return mPppoeManager;
    }

    static public void connectPppoe(Context context, ContentResolver mContentResolver,
            String userName, String pwd, boolean autoReconnect) {
        if(!isPppoeSupported(context)) {
            return;
        }
        getPppoeManager(context).setPppoeUsername(userName);
        getPppoeManager(context).setPppoePassword(pwd);

        mEthManager.setEthernetEnabled(false);
        mEthManager.setEthernetMode(EthernetManager.ETHERNET_CONNECT_MODE_PPPOE, null);
        mEthManager.setEthernetEnabled(true);
        setAutoReconnectState(context, autoReconnect);
    }

    static public boolean isPppoeConnected(Context context) {
        if(!isPppoeSupported(context)) {
            return false;
        }
        return getPppoeManager(context).getPppoeState() == HiPppoeManager.PPPOE_STATE_CONNECT;
    }

    public static void setAutoReconnectState(Context context, boolean isAutoReconnect) {
        getEthernetManager(context).setAutoReconnectState(isAutoReconnect);
    }

    public static boolean getAutoReconnectState(Context context) {
        return getEthernetManager(context).getAutoReconnectState() == EthernetManager.ETHERNET_AUTORECONNECT_ENABLED;
    }

    public static String getPppoeUsername(Context context) {
        if(!isPppoeSupported(context)) {
            return "";
        }
        return getPppoeManager(context).getPppoeUsername();
    }

    public static String getPppoePassword(Context context) {
        if(!isPppoeSupported(context)) {
            return "";
        }
        return getPppoeManager(context).getPppoePassword();
    }
}
