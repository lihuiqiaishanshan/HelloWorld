package com.hisilicon.tvui.view;

import java.text.DecimalFormat;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.network.DVBTNetwork;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.NetworkManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;

public class SetCountryCodeDialog extends Dialog implements OnComboxSelectChangeListener
{
    private DTV mDTV;
    private Context mContext;
    private Combox mCountryCombox = null;
    private EditText mAreaCodeEditText = null;
    private LinearLayout mAreaLinearLayout = null;
    private DTVConfig mDtvConfig;
    private NetworkManager mNetworkManager = null;
    private int mAreaCode = 0;
    private int mNewAreaCode = 0;

    public SetCountryCodeDialog(Context context, int theme)
    {
        super(context, theme);
        mContext = context;
        LogTool.d(LogTool.MPLAY, "SetCountryCodeDialog");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.country_code_dialog);
        LogTool.d(LogTool.MPLAY, "onCreate");
        initView();
    }

    private void initView()
    {
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mDtvConfig = mDTV.getConfig();
        mNetworkManager = mDTV.getNetworkManager();

        mAreaCode = mDTV.getAreaCode();

        //get country to set spinner
        String countrycode = mDtvConfig.getString(CommonValue.COUNTRY_CODE_KEY, "MYS");
        mCountryCombox = (Combox) this.findViewById(R.id.id_dialog_country_cbx);
        mAreaCodeEditText = (EditText) this.findViewById(R.id.editText_area_code);
        mAreaLinearLayout = (LinearLayout) this.findViewById(R.id.lay_area_info);

        String[] countryEntries = null;
        String[] countryEntriesValue = null;
        if (mNetworkManager.getCurrentNetworkType() == EnNetworkType.TERRESTRIAL)
        {
            countryEntries = mContext.getResources().getStringArray(R.array.dvbt_country_code);
            countryEntriesValue = mContext.getResources().getStringArray(R.array.dvbt_country_code_value);
        }
        else if (mNetworkManager.getCurrentNetworkType() == EnNetworkType.ISDB_TER)
        {
            countryEntries = mContext.getResources().getStringArray(R.array.isdbt_country_code);
            countryEntriesValue = mContext.getResources().getStringArray(R.array.isdbt_country_code_value);
        }
        else if (mNetworkManager.getCurrentNetworkType() == EnNetworkType.DTMB)
        {
            countryEntries = mContext.getResources().getStringArray(R.array.dtmb_country_code);
            countryEntriesValue = mContext.getResources().getStringArray(R.array.dtmb_country_code_value);
        }

        if (null != countryEntries && null != countryEntriesValue)
        {
            mCountryCombox.setEntries(countryEntries);
            mCountryCombox.setEntriesValue(countryEntriesValue);
        }

        if (null != countryEntries && countryEntries.length <= 3)
        {
            mCountryCombox.setPopupHeight(mCountryCombox.getPopupLineHeight() * countryEntries.length);
        }
        else
        {
            mCountryCombox.setPopupHeight(mCountryCombox.getPopupLineHeight() * 3);
        }

        mCountryCombox.setText(getKey(mCountryCombox, countrycode));
        mCountryCombox.setOnSelectChangeListener(this);

        mAreaLinearLayout.setVisibility(View.GONE);

        String countryCodeValue = getKeyValue(mCountryCombox, (String) mCountryCombox.getText());
        if (null != countryCodeValue)
        {
            if (countryCodeValue.equalsIgnoreCase("BRA") || countryCodeValue.equalsIgnoreCase("JPN") || countryCodeValue.equalsIgnoreCase("IDN"))
            {
                mAreaLinearLayout.setVisibility(View.VISIBLE);
                DecimalFormat format = new DecimalFormat("00000");
                mAreaCodeEditText.setText(format.format(mAreaCode));
            }
        }

        Button mOKButton = (Button) this.findViewById(R.id.country_code_yes);
        Button mCancelButton = (Button) this.findViewById(R.id.country_code_no);
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countryCodeValue = getKeyValue(mCountryCombox, (String) mCountryCombox.getText());
                mDtvConfig.setString(CommonValue.COUNTRY_CODE_KEY, countryCodeValue);
                mDTV.setCountry(countryCodeValue);

                List<Network> lstNetwork = null;
                DVBTNetwork mLastnetwork = null;
                DVBTNetwork mCurNetwork = null;
                if (mNetworkManager.getCurrentNetworkType() == EnNetworkType.DTMB || mNetworkManager.getCurrentNetworkType() == EnNetworkType.ISDB_TER
                        || mNetworkManager.getCurrentNetworkType() == EnNetworkType.TERRESTRIAL) {
                    lstNetwork = mNetworkManager.getNetworks(mNetworkManager.getCurrentNetworkType());
                    for (int i = 0; i < lstNetwork.size(); i++) {
                        DVBTNetwork dvbtNetwork = (DVBTNetwork) lstNetwork.get(i);
                        LogTool.d(LogTool.MPLAY, " network dvbtNetwork.name=" + dvbtNetwork.getCountry());

                        if (dvbtNetwork.isSelected()) {
                            mLastnetwork = dvbtNetwork;
                        }
                        if (null == countryCodeValue)
                        {
                            LogTool.w(LogTool.MPLAY, "countryCodeValue is null!");
                            return;
                        }
                        if (countryCodeValue.equalsIgnoreCase(dvbtNetwork.getCountry())) {
                            mCurNetwork = dvbtNetwork;
                        }
                    }

                    if (null != mCurNetwork && mCurNetwork != mLastnetwork) {
                        mCurNetwork.setSelect(true);
                        if (null != mLastnetwork) {
                            mLastnetwork.setSelect(false);
                            LogTool.d(LogTool.MPLAY, "last network name=" + mLastnetwork.getCountry());
                        }
                        LogTool.d(LogTool.MPLAY, "cur network name=" + mCurNetwork.getCountry());
                    }

                    String areaString = mAreaCodeEditText.getText().toString();
                    if (!areaString.isEmpty()) {
                        mNewAreaCode = Integer.parseInt(areaString);
                        if (mNewAreaCode != mAreaCode) {
                            mDTV.setAreaCode(mNewAreaCode);
                        }
                    }

                }

                SetCountryCodeDialog.this.cancel();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetCountryCodeDialog.this.cancel();
            }
        });
    }

    @Override
    public void onComboxSelectChange(View arg0, String strText, Object obj, int index)
    {
        if (R.id.id_dialog_country_cbx == arg0.getId())
        {
            mAreaLinearLayout.setVisibility(View.GONE);

            String countryCodeValue = getKeyValue(mCountryCombox, (String) mCountryCombox.getText());
            if (null != countryCodeValue)
            {
                if (countryCodeValue.equalsIgnoreCase("BRA") || countryCodeValue.equalsIgnoreCase("JPN") || countryCodeValue.equalsIgnoreCase("IDN")) {
                    mAreaLinearLayout.setVisibility(View.VISIBLE);
                    DecimalFormat format = new DecimalFormat("00000");
                    mAreaCodeEditText.setText(format.format(mAreaCode));
                }
            }
        }
    }

    @Override
    public void dismiss()
    {
        // TODO Auto-generated method stub
        mDtvConfig.setInt(CommonValue.SELECT_COUNTRYCODE, 0);
        super.dismiss();
    }

    private String getKey(Combox combox, String value)
    {
        Object[] entriesValue = combox.getEntriesValue();
        String[] entries = combox.getEntries();
        if ((null == entriesValue) || (null == entries))
            return null;
        int index = 0;
        for (index = 0; index < entriesValue.length; index++)
        {
            if (String.valueOf(entriesValue[index]).equals(value))
                break;
        }
        if (entries.length > index && (index < entriesValue.length))
        {
            return entries[index];
        }
        return entries[0];
    }

    private String getKeyValue(Combox combox, String entry)
    {
        Object[] entriesValue = combox.getEntriesValue();
        String[] entries = combox.getEntries();
        if ((null == entriesValue) || (null == entries))
            return null;
        int index = 0;
        for (index = 0; index < entries.length; index++)
        {
            if (entries[index].equals(entry))
                break;
        }
        if (entries.length > index && (index < entriesValue.length))
        {
            return String.valueOf(entriesValue[index]);
        }
        return String.valueOf(entriesValue[0]);
    }

}
