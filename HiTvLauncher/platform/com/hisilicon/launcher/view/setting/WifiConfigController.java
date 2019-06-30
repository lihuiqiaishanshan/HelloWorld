package com.hisilicon.launcher.view.setting;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkAddress;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.Proxy;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiEnterpriseConfig.Eap;
import android.net.wifi.WifiEnterpriseConfig.Phase2;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.UserHandle;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.TextUtils;
import com.hisilicon.launcher.util.LogHelper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.EtherInterface;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WifiConfigController implements TextWatcher,
        AdapterView.OnItemSelectedListener, OnCheckedChangeListener {
    private static final String TAG = "WifiConfigController";
    private final WifiDialog mWifiDialog;
    private final View mView;
    private final AccessPoint mAccessPoint;

    /* This value comes from "wifi_ip_settings" resource array */
    private static final int DHCP = 0;
    private static final int STATIC_IP = 1;

    /* These values come from "wifi_proxy_settings" resource array */
    public static final int PROXY_NONE = 0;
    public static final int PROXY_STATIC = 1;
    public static final int PROXY_PAC = 2;
    public static final int WIFI_EAP_METHOD_PEAP = 0;
    public static final int WIFI_EAP_METHOD_TLS  = 1;
    public static final int WIFI_EAP_METHOD_TTLS = 2;
    public static final int WIFI_EAP_METHOD_PWD  = 3;
    public static final int WIFI_PEAP_PHASE2_NONE         = 0;
    public static final int WIFI_PEAP_PHASE2_MSCHAPV2     = 1;
    public static final int WIFI_PEAP_PHASE2_GTC        = 2;
    /* Phase2 methods supported by PEAP are limited */
    private final ArrayAdapter<String> PHASE2_PEAP_ADAPTER;
    /* Full list of phase2 methods */
    private final ArrayAdapter<String> PHASE2_FULL_ADAPTER;
    private final Handler mTextViewChangedHandler;
    private int mAccessPointSecurity;
    private TextView mPasswordView;
    private String unspecifiedCert = "unspecified";
    private static final int unspecifiedCertIndex = 0;
    private Spinner mSecuritySpinner;
    private Spinner mEapMethodSpinner;
    private Spinner mEapCaCertSpinner;
    private Spinner mPhase2Spinner;
    private ArrayAdapter<String> mPhase2Adapter;
    private Spinner mEapUserCertSpinner;
    private TextView mEapIdentityView;
    private TextView mEapAnonymousView;

    private Spinner mIpSettingsSpinner;
    private TextView mIpAddressView;
    private TextView mGatewayView;
    private TextView mNetworkPrefixLengthView;
    private TextView mDns1View;
    private TextView mDns2View;

    private Spinner mProxySettingsSpinner;
    private TextView mProxyHostView;
    private TextView mProxyPortView;
    private TextView mProxyExclusionListView;
    private TextView mProxyPacView;

    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private ProxyInfo mHttpProxy = null;
    private StaticIpConfiguration mStaticIpConfiguration = null;

    private String[] mLevels;
    private boolean mEdit;
    private TextView mSsidView;

    private Context mContext;
    public WifiConfigController(WifiDialog dialog, View view,
            AccessPoint accessPoint, boolean edit) {
        mWifiDialog = dialog;
        mView = view;
        mAccessPoint = accessPoint;
        mAccessPointSecurity = (mAccessPoint == null) ? AccessPoint.SECURITY_NONE : mAccessPoint.security;
        mEdit = edit;
        mTextViewChangedHandler = new Handler();
        mContext = mWifiDialog.getContext();
        final Resources res = mContext.getResources();

        mLevels = res.getStringArray(R.array.wifi_signal);
        PHASE2_PEAP_ADAPTER = new ArrayAdapter<String>(
            mContext, android.R.layout.simple_spinner_item,
            res.getStringArray(R.array.wifi_peap_phase2_entries));
        PHASE2_PEAP_ADAPTER.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        PHASE2_FULL_ADAPTER = new ArrayAdapter<String>(
                mContext, android.R.layout.simple_spinner_item,
                res.getStringArray(R.array.wifi_phase2_entries));
        PHASE2_FULL_ADAPTER.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIpSettingsSpinner = (Spinner) mView.findViewById(R.id.ip_settings);
        mIpSettingsSpinner.setOnItemSelectedListener(this);
        mProxySettingsSpinner = (Spinner) mView.findViewById(R.id.proxy_settings);
        mProxySettingsSpinner.setOnItemSelectedListener(this);

        if (mAccessPoint == null) { // new network
            mWifiDialog.setTitle(R.string.wifi_add_network);

            mSsidView = (TextView) mView.findViewById(R.id.ssid);
            mSsidView.addTextChangedListener(this);
            mSecuritySpinner = ((Spinner) mView.findViewById(R.id.security));
            mSecuritySpinner.setOnItemSelectedListener(this);

            mView.findViewById(R.id.type).setVisibility(View.VISIBLE);

            showIpConfigFields();
            showProxyFields();
            mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
            ((CheckBox) mView.findViewById(R.id.wifi_advanced_togglebox)).setOnCheckedChangeListener(this);
            mWifiDialog.setSubmitButton(res.getString(R.string.wifi_save));
        } else {
            mWifiDialog.setTitle(mAccessPoint.ssid);
            ViewGroup group = (ViewGroup) mView.findViewById(R.id.info);

            boolean showAdvancedFields = false;
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
                WifiConfiguration config = mAccessPoint.getConfig();
                ProxySettings proxy = config.getProxySettings();
                IpAssignment ipAssignment = config.getIpAssignment();
                mIpSettingsSpinner.setSelection(DHCP);
                if (ipAssignment == IpAssignment.STATIC) {
                    mIpSettingsSpinner.setSelection(STATIC_IP);
                    showAdvancedFields = true;
                    StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
                    if (staticConfig != null && staticConfig.ipAddress != null) {
                        addRow(group, R.string.wifi_ip_address,
                           staticConfig.ipAddress.getAddress().getHostAddress());
                    }
                }
                if (proxy == ProxySettings.STATIC || proxy == ProxySettings.PAC) {
                    mProxySettingsSpinner.setSelection(
                            proxy == ProxySettings.STATIC ? PROXY_STATIC : PROXY_PAC);
                    showAdvancedFields = true;
                } else {
                    mProxySettingsSpinner.setSelection(PROXY_NONE);
                }
            }

            if ((mAccessPoint.networkId == INVALID_NETWORK_ID && !mAccessPoint.isActive())
                    || mEdit) {
                showSecurityFields();
                showIpConfigFields();
                showProxyFields();
                mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
                ((CheckBox) mView.findViewById(R.id.wifi_advanced_togglebox))
                        .setOnCheckedChangeListener(this);
                if (showAdvancedFields) {
                    ((CheckBox)mView.findViewById(R.id.wifi_advanced_togglebox)).setChecked(true);
                    mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                }
            }

            if (mEdit) {
                mWifiDialog.setSubmitButton(res
                        .getString(R.string.wifi_save));
            } else {
                final DetailedState state = mAccessPoint.getState();
                final String signalLevel = getSignalString();
                if (state == null && signalLevel != null) {
                    mWifiDialog.setSubmitButton(res.getString(R.string.wifi_connect));
                } else {
                    if (state != null) {
                        addRow(group, R.string.wifi_status, Summary.get(mContext,
                                state, mAccessPoint.networkId ==
                                WifiConfiguration.INVALID_NETWORK_ID));
                    }
                    if (signalLevel != null) {
                        addRow(group, R.string.wifi_signal, signalLevel);
                    }
                    WifiInfo info = mAccessPoint.getInfo();
                    if (info != null && info.getLinkSpeed() != -1) {
                        addRow(group, R.string.wifi_speed, info.getLinkSpeed()
                                + WifiInfo.LINK_SPEED_UNITS);
                    }
                    if (info != null && info.getFrequency() != -1) {
                        final int frequency = info.getFrequency();
                        String band = null;
                        if (frequency >= AccessPoint.LOWER_FREQ_24GHZ
                                && frequency < AccessPoint.HIGHER_FREQ_24GHZ) {
                            band = res.getString(R.string.wifi_band_24ghz);
                            addRow(group, R.string.wifi_frequency, band);
                        } else if (frequency >= AccessPoint.LOWER_FREQ_5GHZ
                                && frequency < AccessPoint.HIGHER_FREQ_5GHZ) {
                            band = res.getString(R.string.wifi_band_5ghz);
                            addRow(group, R.string.wifi_frequency, band);
                        } else {
                            LogHelper.w(TAG, "Unexpected frequency " + frequency);
                        }
                    }
                    addRow(group, R.string.wifi_security, mAccessPoint.getSecurityString());
                    mView.findViewById(R.id.ip_fields).setVisibility(View.GONE);
                }
                if ((mAccessPoint.networkId != INVALID_NETWORK_ID || mAccessPoint.isActive())
                        && ActivityManager.getCurrentUser() == UserHandle.USER_OWNER) {
                    mWifiDialog.setForgetButton(res.getString(R.string.wifi_forget));
                }
            }
        }
        boolean isCancel = (mEdit) || (mAccessPoint != null
                && mAccessPoint.getState() == null && mAccessPoint.getLevel() != -1);
        mWifiDialog.setCancelButton(isCancel ? res.getString(R.string.wifi_cancel)
                : res.getString(R.string.wifi_display_options_done));
        enableSubmitIfAppropriate();
    }

    private void addRow(ViewGroup group, int name, String value) {
        View row = mWifiDialog.getLayoutInflater().inflate(
                R.layout.wifi_dialog_row, group, false);
        ((TextView) row.findViewById(R.id.name)).setText(name);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        group.addView(row);
    }

    private String getSignalString(){
        final int level = mAccessPoint.getLevel();
        return (level > -1 && level < mLevels.length) ? mLevels[level] : null;
    }
    void hideSubmitButton() {
        Button submit = mWifiDialog.getSubmitButton();
        if (submit == null) return;
        submit.setVisibility(View.GONE);
    }
    /* show submit button if password, ip and proxy settings are valid */
    final void enableSubmitIfAppropriate() {
        Button submit = mWifiDialog.getSubmitButton();
        if (submit == null)
            return;

        boolean enabled = false;
        boolean passwordInvalid = false;

        if (mPasswordView != null
                && ((mAccessPointSecurity == AccessPoint.SECURITY_WEP && mPasswordView
                        .length() == 0) || (mAccessPointSecurity == AccessPoint.SECURITY_PSK && mPasswordView
                        .length() < 8))) {
            passwordInvalid = true;
        }

        if ((mSsidView != null && mSsidView.length() == 0)
                || ((mAccessPoint == null || mAccessPoint.networkId == INVALID_NETWORK_ID) && passwordInvalid)) {
            enabled = false;
        } else {
            if (ipAndProxyFieldsAreValid()) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        submit.setEnabled(enabled);
    }

    WifiConfiguration getConfig() {
        if (mAccessPoint != null
                && mAccessPoint.networkId != INVALID_NETWORK_ID && !mEdit) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();

        if (mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(mSsidView.getText()
                    .toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
            config.SSID = AccessPoint.convertToQuotedString(mAccessPoint.ssid);
        } else {
            config.networkId = mAccessPoint.networkId;
        }

        switch (mAccessPointSecurity) {
        case AccessPoint.SECURITY_NONE:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            break;

        case AccessPoint.SECURITY_WEP:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
            if (mPasswordView.length() != 0) {
                int length = mPasswordView.length();
                String password = mPasswordView.getText().toString();
                // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                if ((length == 10 || length == 26 || length == 58)
                        && password.matches("[0-9A-Fa-f]*")) {
                    config.wepKeys[0] = password;
                } else {
                    config.wepKeys[0] = '"' + password + '"';
                }
            }
            break;

        case AccessPoint.SECURITY_PSK:
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            if (mPasswordView.length() != 0) {
                String password = mPasswordView.getText().toString();
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = password;
                } else {
                    config.preSharedKey = '"' + password + '"';
                }
            }
            break;
            case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                config.enterpriseConfig = new WifiEnterpriseConfig();
                int eapMethod = mEapMethodSpinner.getSelectedItemPosition();
                int phase2Method = mPhase2Spinner.getSelectedItemPosition();
                config.enterpriseConfig.setEapMethod(eapMethod);
                switch (eapMethod) {
                    case Eap.PEAP:
                        switch(phase2Method) {
                            case WIFI_PEAP_PHASE2_NONE:
                                config.enterpriseConfig.setPhase2Method(Phase2.NONE);
                                break;
                            case WIFI_PEAP_PHASE2_MSCHAPV2:
                                config.enterpriseConfig.setPhase2Method(Phase2.MSCHAPV2);
                                break;
                            case WIFI_PEAP_PHASE2_GTC:
                                config.enterpriseConfig.setPhase2Method(Phase2.GTC);
                                break;
                            default:
                                LogHelper.w(TAG, "Unknown phase2 method" + phase2Method);
                                break;
                        }
                        break;
                    default:
                        config.enterpriseConfig.setPhase2Method(phase2Method);
                        break;
                }
                String caCert = (String) mEapCaCertSpinner.getSelectedItem();
                if (caCert.equals(unspecifiedCert)) caCert = "";
                config.enterpriseConfig.setCaCertificateAlias(caCert);
                String clientCert = (String) mEapUserCertSpinner.getSelectedItem();
                if (clientCert.equals(unspecifiedCert)) clientCert = "";
                config.enterpriseConfig.setClientCertificateAlias(clientCert);
                config.enterpriseConfig.setIdentity(mEapIdentityView.getText().toString());
                config.enterpriseConfig.setAnonymousIdentity(
                        mEapAnonymousView.getText().toString());
                if (mPasswordView.isShown()) {
                    if (mPasswordView.length() > 0) {
                        config.enterpriseConfig.setPassword(mPasswordView.getText().toString());
                    }
                } else {
                    config.enterpriseConfig.setPassword(mPasswordView.getText().toString());
                }
                break;
            default:
                return null;
        }
        config.setIpConfiguration(
                new IpConfiguration(mIpAssignment, mProxySettings,
                                    mStaticIpConfiguration, mHttpProxy));

        return config;
    }

    private boolean ipAndProxyFieldsAreValid() {
        mIpAssignment = (mIpSettingsSpinner != null &&
                mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) ?
                IpAssignment.STATIC : IpAssignment.DHCP;
        if (mIpAssignment == IpAssignment.STATIC) {
            mStaticIpConfiguration = new StaticIpConfiguration();
            int result = validateIpConfigFields(mStaticIpConfiguration);
            if (result != 0) {
                return false;
            }
        }

        final int selectedPosition = mProxySettingsSpinner.getSelectedItemPosition();
        mProxySettings = ProxySettings.NONE;
        mHttpProxy = null;
        if (selectedPosition == PROXY_STATIC && mProxyHostView != null) {
            mProxySettings = ProxySettings.STATIC;
            String host = mProxyHostView.getText().toString();
            String portStr = mProxyPortView.getText().toString();
            String exclusionList = mProxyExclusionListView.getText().toString();
            int port = 0;
            int result = 0;
            try {
                port = Integer.parseInt(portStr);
                result = proxyValidate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = R.string.proxy_error_invalid_port;
            }
            if (result == 0) {
                mHttpProxy = new ProxyInfo(host, port, exclusionList);
            } else {
                return false;
            }
        } else if (selectedPosition == PROXY_PAC && mProxyPacView != null) {
            mProxySettings = ProxySettings.PAC;
            CharSequence uriSequence = mProxyPacView.getText();
            if (TextUtils.isEmpty(uriSequence)) {
                return false;
            }
            Uri uri = Uri.parse(uriSequence.toString());
            if (uri == null) {
                return false;
            }
            mHttpProxy = new ProxyInfo(uri);
        }
        return true;
    }

    private Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException|ClassCastException e) {
            return null;
        }
    }
    private int validateIpConfigFields(StaticIpConfiguration staticIpConfiguration) {
        if (mIpAddressView == null) {
            return 0;
        }

        String ipAddr = mIpAddressView.getText().toString();
        if (TextUtils.isEmpty(ipAddr)) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        Inet4Address inetAddr = getIPv4Address(ipAddr);
        if (inetAddr == null) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                return R.string.wifi_ip_settings_invalid_network_prefix_length;
            }
            staticIpConfiguration.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);
        } catch (NumberFormatException e) {
            // Set the hint as default after user types in ip address
            mNetworkPrefixLengthView.setText(mWifiDialog.getContext()
                    .getString(R.string.wifi_network_prefix_length_hint));
        }

        String gateway = mGatewayView.getText().toString();
        if (TextUtils.isEmpty(gateway)) {
            try {
                // Extract a default gateway from IP address
                InetAddress netPart = NetworkUtils.getNetworkPart(inetAddr,
                        networkPrefixLength);
                byte[] addr = netPart.getAddress();
                addr[addr.length - 1] = 1;
                mGatewayView.setText(InetAddress.getByAddress(addr)
                        .getHostAddress());
            } catch (RuntimeException | java.net.UnknownHostException e) {
                LogHelper.e(TAG, e.getMessage());
            }
        } else {
            InetAddress gatewayAddr = getIPv4Address(gateway);
            if (gatewayAddr == null) {
                return R.string.wifi_ip_settings_invalid_gateway;
            }
            staticIpConfiguration.gateway = gatewayAddr;
        }

        String dns = mDns1View.getText().toString();
        InetAddress dnsAddr = null;

        if (TextUtils.isEmpty(dns)) {
            // If everything else is valid, provide hint as a default option
            mDns1View.setText(mWifiDialog.getContext().getString(
                    R.string.wifi_dns1_hint));
        } else {
            dnsAddr = getIPv4Address(dns);
            if (dnsAddr == null) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            staticIpConfiguration.dnsServers.add(dnsAddr);
        }

        if (mDns2View.length() > 0) {
            dns = mDns2View.getText().toString();
            dnsAddr = getIPv4Address(dns);
            if (dnsAddr == null) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            staticIpConfiguration.dnsServers.add(dnsAddr);
        }
        return 0;
    }

    private void showSecurityFields() {
        if (mAccessPointSecurity == AccessPoint.SECURITY_NONE) {
            mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);

        if (mPasswordView == null) {
            mPasswordView = (TextView) mView.findViewById(R.id.password);
            mPasswordView.addTextChangedListener(this);
            ((CheckBox) mView.findViewById(R.id.show_password))
                    .setOnCheckedChangeListener(this);

            if (mAccessPoint != null
                    && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                mPasswordView.setHint(R.string.wifi_unchanged);
            }
        }

        if (mAccessPointSecurity != AccessPoint.SECURITY_EAP) {
            mView.findViewById(R.id.eap).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.eap).setVisibility(View.VISIBLE);
        if (mEapMethodSpinner == null) {
            mEapMethodSpinner = (Spinner) mView.findViewById(R.id.method);
            mEapMethodSpinner.setOnItemSelectedListener(this);
            mPhase2Spinner = (Spinner) mView.findViewById(R.id.phase2);
            mEapCaCertSpinner = (Spinner) mView.findViewById(R.id.ca_cert);
            mEapUserCertSpinner = (Spinner) mView.findViewById(R.id.user_cert);
            mEapIdentityView = (TextView) mView.findViewById(R.id.identity);
            mEapAnonymousView = (TextView) mView.findViewById(R.id.anonymous);
            loadCertificates(mEapCaCertSpinner, Credentials.CA_CERTIFICATE);
            loadCertificates(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY);
            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                WifiEnterpriseConfig enterpriseConfig = mAccessPoint.getConfig().enterpriseConfig;
                int eapMethod = enterpriseConfig.getEapMethod();
                int phase2Method = enterpriseConfig.getPhase2Method();
                mEapMethodSpinner.setSelection(eapMethod);
                showEapFieldsByMethod(eapMethod);
                switch (eapMethod) {
                    case Eap.PEAP:
                        switch (phase2Method) {
                            case Phase2.NONE:
                                mPhase2Spinner.setSelection(WIFI_PEAP_PHASE2_NONE);
                                break;
                            case Phase2.MSCHAPV2:
                                mPhase2Spinner.setSelection(WIFI_PEAP_PHASE2_MSCHAPV2);
                                break;
                            case Phase2.GTC:
                                mPhase2Spinner.setSelection(WIFI_PEAP_PHASE2_GTC);
                                break;
                            default:
                                LogHelper.w(TAG, "Invalid phase 2 method " + phase2Method);
                                break;
                        }
                        break;
                    default:
                        mPhase2Spinner.setSelection(phase2Method);
                        break;
                }
                setSelection(mEapCaCertSpinner, enterpriseConfig.getCaCertificateAlias());
                setSelection(mEapUserCertSpinner, enterpriseConfig.getClientCertificateAlias());
                mEapIdentityView.setText(enterpriseConfig.getIdentity());
                mEapAnonymousView.setText(enterpriseConfig.getAnonymousIdentity());
            } else {
                mEapMethodSpinner.setSelection(Eap.PEAP);
                showEapFieldsByMethod(Eap.PEAP);
            }
        } else {
            showEapFieldsByMethod(mEapMethodSpinner.getSelectedItemPosition());
        }
    }

    /**
     * EAP-PWD valid fields include
     *   identity
     *   password
     * EAP-PEAP valid fields include
     *   phase2: MSCHAPV2, GTC
     *   ca_cert
     *   identity
     *   anonymous_identity
     *   password
     * EAP-TLS valid fields include
     *   user_cert
     *   ca_cert
     *   identity
     * EAP-TTLS valid fields include
     *   phase2: PAP, MSCHAP, MSCHAPV2, GTC
     *   ca_cert
     *   identity
     *   anonymous_identity
     *   password
     */
    private void showEapFieldsByMethod(int eapMethod) {
        // Common defaults
        mView.findViewById(R.id.l_method).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.l_identity).setVisibility(View.VISIBLE);

        // Defaults for most of the EAP methods and over-riden by
        // by certain EAP methods
        mView.findViewById(R.id.l_ca_cert).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.password_layout).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.show_password_layout).setVisibility(View.VISIBLE);

        switch (eapMethod) {
            case WIFI_EAP_METHOD_PWD:
                setPhase2Invisible();
                setCaCertInvisible();
                setAnonymousIdentInvisible();
                setUserCertInvisible();
                break;
            case WIFI_EAP_METHOD_TLS:
                mView.findViewById(R.id.l_user_cert).setVisibility(View.VISIBLE);
                setPhase2Invisible();
                setAnonymousIdentInvisible();
                setPasswordInvisible();
                break;
            case WIFI_EAP_METHOD_PEAP:
                // Reset adapter if needed
                if (mPhase2Adapter != PHASE2_PEAP_ADAPTER) {
                    mPhase2Adapter = PHASE2_PEAP_ADAPTER;
                    mPhase2Spinner.setAdapter(mPhase2Adapter);
                }
                mView.findViewById(R.id.l_phase2).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.l_anonymous).setVisibility(View.VISIBLE);
                setUserCertInvisible();
                break;
            case WIFI_EAP_METHOD_TTLS:
                // Reset adapter if needed
                if (mPhase2Adapter != PHASE2_FULL_ADAPTER) {
                    mPhase2Adapter = PHASE2_FULL_ADAPTER;
                    mPhase2Spinner.setAdapter(mPhase2Adapter);
                }
                mView.findViewById(R.id.l_phase2).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.l_anonymous).setVisibility(View.VISIBLE);
                setUserCertInvisible();
                break;
        }
    }

    private void setPhase2Invisible() {
        mView.findViewById(R.id.l_phase2).setVisibility(View.GONE);
        mPhase2Spinner.setSelection(Phase2.NONE);
    }

    private void setCaCertInvisible() {
        mView.findViewById(R.id.l_ca_cert).setVisibility(View.GONE);
        mEapCaCertSpinner.setSelection(unspecifiedCertIndex);
    }

    private void setUserCertInvisible() {
        mView.findViewById(R.id.l_user_cert).setVisibility(View.GONE);
        mEapUserCertSpinner.setSelection(unspecifiedCertIndex);
    }

    private void setAnonymousIdentInvisible() {
        mView.findViewById(R.id.l_anonymous).setVisibility(View.GONE);
        mEapAnonymousView.setText("");
    }

    private void setPasswordInvisible() {
        mPasswordView.setText("");
        mView.findViewById(R.id.password_layout).setVisibility(View.GONE);
        mView.findViewById(R.id.show_password_layout).setVisibility(View.GONE);
    }

    private void showIpConfigFields() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
            mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
            if (mIpAddressView == null) {
                mIpAddressView = (TextView) mView.findViewById(R.id.ipaddress);
                mIpAddressView.addTextChangedListener(this);
                mGatewayView = (TextView) mView.findViewById(R.id.gateway);
                mGatewayView.addTextChangedListener(this);
                mNetworkPrefixLengthView = (TextView) mView
                        .findViewById(R.id.network_prefix_length);
                mNetworkPrefixLengthView.addTextChangedListener(this);
                mDns1View = (TextView) mView.findViewById(R.id.dns1);
                mDns1View.addTextChangedListener(this);
                mDns2View = (TextView) mView.findViewById(R.id.dns2);
                mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
                StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
                if (staticConfig != null) {
                    if (staticConfig.ipAddress != null) {
                        mIpAddressView.setText(
                                staticConfig.ipAddress.getAddress().getHostAddress());
                        mNetworkPrefixLengthView.setText(Integer.toString(staticConfig.ipAddress
                                .getNetworkPrefixLength()));
                    }

                    if (staticConfig.gateway != null) {
                        mGatewayView.setText(staticConfig.gateway.getHostAddress());
                    }

                    Iterator<InetAddress> dnsIterator = staticConfig.dnsServers.iterator();
                    if (dnsIterator.hasNext()) {
                        mDns1View.setText(dnsIterator.next().getHostAddress());
                    }
                    if (dnsIterator.hasNext()) {
                        mDns2View.setText(dnsIterator.next().getHostAddress());
                    }
                }
            }
        } else {
            mView.findViewById(R.id.staticip).setVisibility(View.GONE);
        }
    }

    /**
     * validate syntax of hostname and port entries
     * @return 0 on success, string resource ID on failure
     */
    private int proxyValidate(String hostname, String port, String exclList) {
        switch (Proxy.validate(hostname, port, exclList)) {
            case Proxy.PROXY_VALID:
                return 0;
            case Proxy.PROXY_HOSTNAME_EMPTY:
                return R.string.proxy_error_empty_host_set_port;
            case Proxy.PROXY_HOSTNAME_INVALID:
                return R.string.proxy_error_invalid_host;
            case Proxy.PROXY_PORT_EMPTY:
                return R.string.proxy_error_empty_port;
            case Proxy.PROXY_PORT_INVALID:
                return R.string.proxy_error_invalid_port;
            case Proxy.PROXY_EXCLLIST_INVALID:
                return R.string.proxy_error_invalid_exclusion_list;
            default:
            // should neven happen
            LogHelper.w(TAG, "Unknown proxy settings error");
            return -1;
        }
    }

    private void showProxyFields() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.proxy_settings_fields).setVisibility(
                View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.proxy_pac_field).setVisibility(View.GONE);
            if (mProxyHostView == null) {
                mProxyHostView = (TextView) mView
                        .findViewById(R.id.proxy_hostname);
                mProxyHostView.addTextChangedListener(this);
                mProxyPortView = (TextView) mView.findViewById(R.id.proxy_port);
                mProxyPortView.addTextChangedListener(this);
                mProxyExclusionListView = (TextView) mView
                        .findViewById(R.id.proxy_exclusionlist);
                mProxyExclusionListView.addTextChangedListener(this);
            }
            if (config != null) {
                ProxyInfo proxyProperties = config.getHttpProxy();
                if (proxyProperties != null) {
                    mProxyHostView.setText(proxyProperties.getHost());
                    mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                    mProxyExclusionListView.setText(proxyProperties.getExclusionListAsString());
                }
            }
        } else if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_PAC) {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_pac_field).setVisibility(View.VISIBLE);
            if (mProxyPacView == null) {
                mProxyPacView = (TextView) mView.findViewById(R.id.proxy_pac);
                mProxyPacView.addTextChangedListener(this);
            }
            if (config != null) {
                ProxyInfo proxyInfo = config.getHttpProxy();
                if (proxyInfo != null) {
                    mProxyPacView.setText(proxyInfo.getPacFileUrl().toString());
                }
            }
        } else {
            mView.findViewById(R.id.proxy_warning_limited_support)
                    .setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_pac_field).setVisibility(View.GONE);
        }
    }

    private void loadCertificates(Spinner spinner, String prefix) {
        final Context context = mWifiDialog.getContext();
        String[] certs = KeyStore.getInstance().list(prefix, android.os.Process.WIFI_UID);
        if (certs == null || certs.length == 0) {
            certs = new String[] {unspecifiedCert};
        } else {
            final String[] array = new String[certs.length + 1];
            array[0] = unspecifiedCert;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, certs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    private void setSelection(Spinner spinner, String value) {
        if (value != null) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner
                    .getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; --i) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    public boolean isEdit() {
        return mEdit;
    }

    @Override
    public void afterTextChanged(Editable s) {
        mTextViewChangedHandler.post(new Runnable() {
            public void run() {
                enableSubmitIfAppropriate();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        // work done in afterTextChanged
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // work done in afterTextChanged
    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        if (view.getId() == R.id.show_password) {
            int pos = mPasswordView.getSelectionEnd();
            mPasswordView
                    .setInputType(InputType.TYPE_CLASS_TEXT
                            | (isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                                    : InputType.TYPE_TEXT_VARIATION_PASSWORD));
            if (pos >= 0) {
                ((EditText) mPasswordView).setSelection(pos);
            }
        } else if (view.getId() == R.id.wifi_advanced_togglebox) {
            if (isChecked) {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(
                        View.VISIBLE);
            } else {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(
                        View.GONE);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        if (parent == mSecuritySpinner) {
            mAccessPointSecurity = position;
            showSecurityFields();
        } else if (parent == mEapMethodSpinner) {
            showSecurityFields();
        } else if (parent == mProxySettingsSpinner) {
            showProxyFields();
        } else {
            showIpConfigFields();
        }
        enableSubmitIfAppropriate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //
    }

    /**
     * Make the characters of the password visible if show_password is checked.
     */
//    private void updatePasswordVisibility(boolean checked) {
//        int pos = mPasswordView.getSelectionEnd();
//        mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT
//                | (checked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
//                        : InputType.TYPE_TEXT_VARIATION_PASSWORD));
//        if (pos >= 0) {
//            ((EditText) mPasswordView).setSelection(pos);
//        }
//    }
}
