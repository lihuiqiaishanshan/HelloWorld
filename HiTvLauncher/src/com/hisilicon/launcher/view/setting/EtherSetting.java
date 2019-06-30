package com.hisilicon.launcher.view.setting;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.ServiceManager;
import android.provider.Settings;
import com.hisilicon.launcher.util.LogHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.EtherInterface;
import com.hisilicon.launcher.util.Constant;

/**
 * Network wire three level menu
 */
public class EtherSetting extends LinearLayout {
    private static final String TAG = "EtherSetting";
    private Context mContext;
    private ConnectivityManager mConnectivityManager;
    private EthernetManager mEthernetManager;
    // layout of AutoIP
    private LinearLayout mAutoIPLayout;
    // text of AutoIP
    private TextView mAutoIPText;
    // layout of ManualIP
    private LinearLayout mManualIPLayout;
    // text of ManualIP
    private TextView mManualIPText;
    //layout of PPPOE
    private LinearLayout mPPPOELayout;
    // text of PPPOE
    private TextView mPPPOEText;
    private CheckBox mSwitchCb;
    // text of AutoSelect
    private TextView mAutoSelectText;
    // view of ManualSelect
    private View mManualSelect;
    // view of PppoeSelect
    private TextView mPppoeSelect;

    // Control NetSettingDialog display content
    private NetStateDialog netStateDialog;
    private EtherInputDialog etherInputDialog;
    private EtherShowIPDialog mEtherDialog;
    private EtherSetIPDialog mEtherSetDialog;

    private final IntentFilter mIntentFilter;
    // ethernet current status
    private int ethernet_current_status = -1;
    // pppoe current status
    private int pppoe_current_status = -1;
    // flag of is Ethernet On
    private boolean isEthernetOn = false;

    private String DhcpIP = "";
    private String DhcpGatewary = "";
    private String DhcpMask = "";
    private String DhcpDns = "";

    /**
     * Control NetStateDialog display content
     */
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (netStateDialog == null) {
                netStateDialog = NetStateDialog.createDialog(mContext,
                        mHandler, msg.what);
            }
            switch (msg.what) {
                case NetStateDialog.CONNECTING:
                case NetStateDialog.CONNECT_FAILED:
                case NetStateDialog.SETTING_IP:
                case NetStateDialog.SET_IP_FAILED:
                case NetStateDialog.GETTING_IP:
                case NetStateDialog.PPPOE_AUTH_FAILED:
                    if (!netStateDialog.isShowing()) {
                        netStateDialog.show();
                    }
                    netStateDialog.refreshView(msg.what);
                    break;
                case NetStateDialog.MSG_CLEAR:
                    if (netStateDialog != null) {
                        netStateDialog.dismiss();
                        netStateDialog = null;
                    }
                    break;
                default:
                    break;
            }
        };
    };

    public EtherSetting(Context context, Handler handler) {
        super(context);
        this.mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View parent = inflater.inflate(R.layout.setting_ether, this);
        mIntentFilter = new IntentFilter(
                EtherInterface.ETHERNET_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(EtherInterface.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(EtherInterface.PPPOE_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mEthSettingsReceiver, mIntentFilter);
        IBinder b = ServiceManager
                .getService(Context.NETWORKMANAGEMENT_SERVICE);
        INetworkManagementService mNwService = INetworkManagementService.Stub
                .asInterface(b);
        mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        mEthernetManager = (EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);

        int state = EtherInterface.getEthernetState(mContext);
        LogHelper.d(TAG, "ether state = " + state);
        // According to the status of Ethernet Ethernet switch setting
        isEthernetOn = state == EtherInterface.ETHERNET_STATE_ENABLED
                || EtherInterface.isPppoeConnected(mContext);

        initView(parent);
        setViewInvalid(isEthernetOn);
        mSwitchCb.requestFocus();
    }

    /**
     * The initialization of view
     *
     * @param parent
     */
    private void initView(View parent) {
        final ContentResolver cr = mContext.getContentResolver();
        // Ethernet switch
        mSwitchCb = (CheckBox) parent.findViewById(R.id.ether_switch_cb);
        // just init checked state
        mSwitchCb.setChecked(isEthernetOn);
        mSwitchCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
               LogHelper.d(TAG , "mSwitchCb ethernet status = "+ ethernet_current_status);
               etherNetOn(isChecked);
               mSwitchCb.setFocusable(!isChecked);
               setViewInvalid(isChecked);
            }
        });
        // Automatic IP
        mAutoIPLayout = (LinearLayout) parent
                .findViewById(R.id.ether_autoip_lay);
        mAutoSelectText = (TextView) findViewById(R.id.ether_autosel_txt);
        mAutoIPText = (TextView) parent.findViewById(R.id.ether_autoip_txt);
        mAutoIPLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAutoSelectText.getVisibility() == View.GONE) {
                    Message message = mHandler.obtainMessage();
                    message.what = NetStateDialog.GETTING_IP;
                    mHandler.sendMessageDelayed(message, 100);
                    // DHCP When not selected
                    cleanIP();
                    EtherInterface.startDhcp(mContext);
                    ethernet_current_status = EtherInterface.EVENT_PHY_LINK_UP;
                } else {
                    // DHCP has been selected
                    mEtherDialog = new EtherShowIPDialog(mContext);
                    mEtherDialog.show();
                }
            }
        });
        mAutoIPLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAutoIPLayout.setBackgroundResource(R.drawable.launcher_set_focus);
                    mAutoIPText.setTextColor(mContext.getResources().getColor(R.color.black));
                } else {
                    mAutoIPLayout.setBackgroundResource(R.drawable.button_transparent);
                    mAutoIPText.setTextColor(mContext.getResources().getColor(R.color.white));
                }
            }
        });

        // Manual IP
        mManualIPLayout = (LinearLayout) parent
                .findViewById(R.id.ether_manualip_lay);
        mManualSelect = (TextView) parent
                .findViewById(R.id.ether_manualsel_txt);
        mManualIPText = (TextView) parent.findViewById(R.id.ether_manualip_txt);
        mManualIPLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.d(TAG, "onClick Ethernet_current_status = " + ethernet_current_status);
                mEtherSetDialog = new EtherSetIPDialog(mContext, mHandler);
                mEtherSetDialog.show();
                mEtherSetDialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface arg0) {
                        // TODO Auto-generated method stub
                        if (ethernet_current_status == EtherInterface.EVENT_DHCP_CONNECT_SUCCESSED) {
                            mAutoSelectText.setVisibility(View.VISIBLE);
                            mManualSelect.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        mManualIPLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mManualIPLayout.setBackgroundResource(R.drawable.launcher_set_focus);
                    mManualIPText.setTextColor(mContext.getResources().getColor(R.color.black));
                } else {
                    mManualIPLayout.setBackgroundResource(R.drawable.button_transparent);
                    mManualIPText.setTextColor(mContext.getResources().getColor(R.color.white));
                }
            }
        });

        // Broadband dial-up
        mPPPOELayout = (LinearLayout)parent.findViewById(R.id.ether_pppoe_lay);
        mPPPOEText = (TextView) parent.findViewById(R.id.ether_pppoe_txt);
        if(!EtherInterface.isPppoeSupported(mContext)) {
            mPPPOELayout.setClickable(false);
            mPPPOELayout.setFocusable(false);
            mPPPOELayout.setFocusableInTouchMode(false);
            mPPPOEText.setTextColor(mContext.getResources().getColor(
                    R.color.grey));
        }
        mPppoeSelect = (TextView) parent.findViewById(R.id.ether_pppoesel_txt);
        mPPPOELayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etherInputDialog = new EtherInputDialog(mContext, mHandler);
                etherInputDialog.show();
            }
        });
        mPPPOELayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mPPPOELayout.setBackgroundResource(R.drawable.launcher_set_focus);
                    mPPPOEText.setTextColor(mContext.getResources().getColor(R.color.black));
                } else {
                    mPPPOELayout.setBackgroundResource(R.drawable.button_transparent);
                    mPPPOEText.setTextColor(mContext.getResources().getColor(R.color.white));
                }
            }
        });
    }

    /**
     * etherNet switch
     *
     * @param isChecked
     */
    public void etherNetOn(boolean isChecked) {
        int state = EtherInterface.getEthernetPersistedState(mContext);
        LogHelper.d(TAG, "state = " + state + " isChecked = " + isChecked);
        EtherInterface.enableEthernet(mContext,isChecked);
        isEthernetOn = isChecked;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (mSwitchCb.isFocused() && mSwitchCb.isChecked()) {
                    etherNetOn(false);
                    mSwitchCb.setChecked(false);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (mSwitchCb.isFocused() && !mSwitchCb.isChecked()) {
                    etherNetOn(true);
                    mSwitchCb.setChecked(true);
                }
                break;

            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * set all view invalid
     *
     * @param isEtherOpen
     */
    private void setViewInvalid(boolean isEtherOpen) {
        LogHelper.d(TAG, "setViewInvalid() = " + isEtherOpen);
        mAutoIPText.setEnabled(isEthernetOn);
        mManualIPText.setEnabled(isEthernetOn);
        mPPPOEText.setEnabled(isEthernetOn);
        if (isEtherOpen) {
            mAutoIPLayout.setClickable(true);
            mAutoIPLayout.setFocusable(true);
            mAutoIPLayout.setFocusableInTouchMode(true);
            mAutoIPText.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            mManualIPLayout.setClickable(true);
            mManualIPLayout.setFocusable(true);
            mManualIPLayout.setFocusableInTouchMode(true);
            mManualIPText.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            if (EtherInterface.isPppoeSupported(mContext)) {
                mPPPOELayout.setClickable(true);
                mPPPOELayout.setFocusable(true);
                mPPPOELayout.setFocusableInTouchMode(true);
                mPPPOEText.setTextColor(mContext.getResources().getColor(
                        R.color.white));
            }
        } else {
            mAutoIPLayout.setClickable(false);
            mAutoIPLayout.setFocusable(false);
            mAutoIPLayout.setFocusableInTouchMode(false);
            mAutoIPText.setTextColor(mContext.getResources().getColor(
                    R.color.grey));
            mManualIPLayout.setClickable(false);
            mManualIPLayout.setFocusable(false);
            mManualIPLayout.setFocusableInTouchMode(false);
            mManualIPText.setTextColor(mContext.getResources().getColor(
                    R.color.grey));
            mPPPOELayout.setClickable(false);
            mPPPOELayout.setFocusable(false);
            mPPPOELayout.setFocusableInTouchMode(false);
            mPPPOEText.setTextColor(mContext.getResources().getColor(
                    R.color.grey));
            mAutoSelectText.setVisibility(View.GONE);
            mManualSelect.setVisibility(View.GONE);
            mPppoeSelect.setVisibility(View.GONE);
            setFocus();
        }
    }

    private void setFocus(){
        if(!mSwitchCb.isFocused()){
           LogHelper.d(TAG, "mSwitchCb set Focus");
           mSwitchCb.setFocusable(true);
           mSwitchCb.requestFocus();
        }
    }

    /**
     * Broadcast of Ethernet
     */
    private final BroadcastReceiver mEthSettingsReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int message = -1;
            int rel = -1;
            ContentResolver resolver = mContext.getContentResolver();
            final String mode = mEthernetManager.getEthernetMode();
            if (intent.getAction().equals(EtherInterface.ETHERNET_STATE_CHANGED_ACTION)) {
                message = intent.getIntExtra(EtherInterface.EXTRA_ETHERNET_STATE, rel);
                LogHelper.d(TAG, "onReceive ether message : " + message);
                switch (message) {
                // Dynamic IP connection is successful event 10
                    case EtherInterface.EVENT_DHCP_CONNECT_SUCCESSED:
                        setFocus();
                        setViewInvalid(true);
                        mAutoSelectText.setVisibility(View.VISIBLE);
                        mManualSelect.setVisibility(View.GONE);
                        mPppoeSelect.setVisibility(View.GONE);
                        Message msg = mHandler.obtainMessage();
                        msg.what = NetStateDialog.MSG_CLEAR;
                        mHandler.sendMessageDelayed(msg, 100);
                        ethernet_current_status = EtherInterface.EVENT_DHCP_CONNECT_SUCCESSED;
                        break;
                    // Event dynamic IP connection failed 11
                    case EtherInterface.EVENT_DHCP_CONNECT_FAILED:
                        setFocus();
                        if(mode.equals(EthernetManager.ETHERNET_CONNECT_MODE_DHCP)
                                && !mHandler.hasMessages(NetStateDialog.CONNECT_FAILED)) {
                            Message msg1 = mHandler.obtainMessage();
                            msg1.what = NetStateDialog.CONNECT_FAILED;
                            mHandler.sendMessageDelayed(msg1, 100);
                        }
                        if(isEthernetOn){
                            setViewInvalid(true);
                        }
                        ethernet_current_status = EtherInterface.EVENT_DHCP_CONNECT_FAILED;
                        break;
                    // Dynamic IP disconnect the successful event 12
                    case EtherInterface.EVENT_DHCP_DISCONNECT_SUCCESSED:
                        setFocus();
                        mAutoSelectText.setVisibility(View.GONE);
                        ethernet_current_status = EtherInterface.EVENT_DHCP_DISCONNECT_SUCCESSED;
                        break;
                    // Event dynamic IP open failed 13
                    case EtherInterface.EVENT_DHCP_DISCONNECT_FAILED:
                        setFocus();
                        ethernet_current_status = EtherInterface.EVENT_DHCP_DISCONNECT_FAILED;
                        break;
                    // Static IP connection is successful event 14
                    case EtherInterface.EVENT_STATIC_CONNECT_SUCCESSED:
                        LogHelper.d(TAG, "EVENT_STATIC_CONNECT_SUCCESSED");
                        setFocus();
                        setViewInvalid(true);
                        mManualSelect.setVisibility(View.VISIBLE);
                        mAutoSelectText.setVisibility(View.GONE);
                        mPppoeSelect.setVisibility(View.GONE);
                        Message msg2 = mHandler.obtainMessage();
                        msg2.what = NetStateDialog.MSG_CLEAR;
                        mHandler.sendMessageDelayed(msg2, 100);
                        ethernet_current_status = EtherInterface.EVENT_STATIC_CONNECT_SUCCESSED;
                        break;
                    // Event of static IP connection failed 15
                    case EtherInterface.EVENT_STATIC_CONNECT_FAILED:
                        setFocus();
                        if(mode.equals(EthernetManager.ETHERNET_CONNECT_MODE_MANUAL)
                                && !mHandler.hasMessages(NetStateDialog.CONNECT_FAILED)) {
                            Message msg4 = mHandler.obtainMessage();
                            msg4.what = NetStateDialog.CONNECT_FAILED;
                            mHandler.sendMessageDelayed(msg4, 100);
                        }
                        ethernet_current_status = EtherInterface.EVENT_STATIC_CONNECT_FAILED;
                        break;
                    // Static IP disconnect the successful event 16
                    case EtherInterface.EVENT_STATIC_DISCONNECT_SUCCESSED:
                        setFocus();
                        mManualSelect.setVisibility(View.GONE);
                        ethernet_current_status = EtherInterface.EVENT_STATIC_DISCONNECT_SUCCESSED;
                        break;
                    // Static IP disconnection failure events 17
                    case EtherInterface.EVENT_STATIC_DISCONNECT_FAILED:
                        setFocus();
                        ethernet_current_status = EtherInterface.EVENT_STATIC_DISCONNECT_FAILED;
                        break;
                    // The cable connections on the event 18
                    case EtherInterface.EVENT_PHY_LINK_UP:
                        Message msg3 = mHandler.obtainMessage();
                        msg3.what = NetStateDialog.CONNECTING;
                        mHandler.sendMessageDelayed(msg3, 100);
                        ethernet_current_status = EtherInterface.EVENT_PHY_LINK_UP;
                        break;
                    // Cable disconnect events 19
                    case EtherInterface.EVENT_PHY_LINK_DOWN:
                        if (mEtherSetDialog != null && mEtherSetDialog.isShowing()) {
                            mEtherSetDialog.dismiss();
                        }
                        Message msg4 = mHandler.obtainMessage();
                        msg4.what = NetStateDialog.MSG_CLEAR;
                        mHandler.sendMessageDelayed(msg4, 100);
                        setViewInvalid(false);
                        cleanIP();
                        ethernet_current_status = EtherInterface.EVENT_PHY_LINK_DOWN;
                        break;
                    default:
                        break;
                }
            }else if (intent.getAction().equals(
                    EtherInterface.PPPOE_STATE_CHANGED_ACTION)) {
                message = intent.getIntExtra(
                        EtherInterface.EXTRA_PPPOE_STATE, rel);
                LogHelper.d(TAG, "onReceive pppoe message : " + message);
                switch (message) {
                    // pppoe connect success events
                    case EtherInterface.EVENT_PPPOE_CONNECT_SUCCESSED:
                        setFocus();
                        setViewInvalid(true);
                        mPppoeSelect.setVisibility(View.VISIBLE);
                        mAutoSelectText.setVisibility(View.GONE);
                        mManualSelect.setVisibility(View.GONE);
                        Message msg = mHandler.obtainMessage();
                        msg.what = NetStateDialog.MSG_CLEAR;
                        mHandler.sendMessageDelayed(msg, 100);
                        ethernet_current_status = EtherInterface.EVENT_PPPOE_CONNECT_SUCCESSED;
                        break;
                    // pppoe connect failed events
                    case EtherInterface.EVENT_PPPOE_CONNECT_FAILED:
                        setFocus();
                        if(mode.equals(EthernetManager.ETHERNET_CONNECT_MODE_PPPOE)
                                && !mHandler.hasMessages(NetStateDialog.CONNECT_FAILED)) {
                            Message msg1 = mHandler.obtainMessage();
                            msg1.what = NetStateDialog.CONNECT_FAILED;
                            mHandler.sendMessageDelayed(msg1, 100);
                        }
                        ethernet_current_status = EtherInterface.EVENT_PPPOE_CONNECT_FAILED;
                        break;
                    // pppoe auto reconnect failed events

                    case EtherInterface.EVENT_PPPOE_CONNECT_FAILED_AUTH_FAIL:
                        setFocus();
                        if(mode.equals(EthernetManager.ETHERNET_CONNECT_MODE_PPPOE)
                                && !mHandler.hasMessages(NetStateDialog.PPPOE_AUTH_FAILED)) {
                            Message msg2 = mHandler.obtainMessage();
                            msg2.what = NetStateDialog.PPPOE_AUTH_FAILED;
                            mHandler.sendMessageDelayed(msg2, 100);
                        }
                        ethernet_current_status =EtherInterface.EVENT_PPPOE_CONNECT_FAILED_AUTH_FAIL;
                        break;
                    case EtherInterface.EVENT_PPPOE_CONNECTING:
                        Message msg3 = mHandler.obtainMessage();
                        msg3.what = NetStateDialog.CONNECTING;
                        mHandler.sendMessageDelayed(msg3, 100);
                        ethernet_current_status = EtherInterface.EVENT_PPPOE_CONNECTING;
                        break;
                    // pppoe disconnect success events
                    case EtherInterface.EVENT_PPPOE_DISCONNECT_SUCCESSED:
                        setFocus();
                        mPppoeSelect.setVisibility(View.GONE);
                        ethernet_current_status = EtherInterface.EVENT_PPPOE_DISCONNECT_SUCCESSED;
                        break;
                    // pppoe disconnect failed events
                    case EtherInterface.EVENT_PPPOE_DISCONNECT_FAILED:
                        setFocus();
                        ethernet_current_status = EtherInterface.EVENT_PPPOE_DISCONNECT_FAILED;
                        break;
                    // pppoe auto reconnecting events
                    case EtherInterface.EVENT_PPPOE_AUTORECONNECTING:
                        Message msg4 = mHandler.obtainMessage();
                        msg4.what = NetStateDialog.CONNECTING;
                        mHandler.sendMessageDelayed(msg4, 100);
                        ethernet_current_status = EtherInterface.EVENT_PPPOE_AUTORECONNECTING;
                        break;
                    default:
                        break;
                }
            }
        }
    };

    /**
     * make the child dialog shutdown
     */
    public void dismissChildDialog() {
        if (mEtherDialog != null && mEtherDialog.isShowing()) {
            mEtherDialog.dismiss();
        }
        if (null != mEtherSetDialog && mEtherSetDialog.isShowing()) {
            mEtherSetDialog.dismiss();
        }
        if (null != netStateDialog && netStateDialog.isShowing()) {
            netStateDialog.dismiss();
        }
        if (null != etherInputDialog && etherInputDialog.isShowing()) {
            etherInputDialog.dismiss();
        }
    }

    public void onStop() {
        dismissChildDialog();
        mContext.unregisterReceiver(mEthSettingsReceiver);
    }

    private void cleanIP() {
        DhcpIP = "";
        DhcpGatewary = "";
        DhcpMask = "";
        DhcpDns = "";
    }
}
