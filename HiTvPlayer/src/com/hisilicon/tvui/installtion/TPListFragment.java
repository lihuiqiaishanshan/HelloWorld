package com.hisilicon.tvui.installtion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelFilter;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.network.DVBSNetwork;
import com.hisilicon.dtv.network.DVBSTransponder;
import com.hisilicon.dtv.network.DVBSTransponder.EnPolarity;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.NetworkManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;
import com.hisilicon.tvui.view.MyToast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TPListFragment extends Fragment implements OnClickListener, IScanSubWnd, OnComboxSelectChangeListener {
    private static final int TP_UNIT_RATE = 1000;

    private static final int SAT_C_MIN_KHZ = 3000000;
    private static final int SAT_C_MAX_KHZ = 4800000;
    private static final int SAT_KU_MIN_KHZ = 10600000;
    private static final int SAT_KU_MAX_KHZ = 12750000;
    private static final int SAT_SYMBOL_RATE_MAX = 60000000;
    /**
     * <(45000000)
     */

    private IScanMainWnd mScanMainWnd = null;

    private NetworkManager mNetworkManager = null;
    private ChannelManager mChannelManager = null;
    private List<Network> mSatelliteList = null;
    private boolean mbIsChange = false;

    private Button mBtnSearch;
    private ListView mLvTPs = null;
    private TPLstItemAdapter mTPAdapter = null;
    private Combox mCbxSatellite = null;

    private List<CTPSelect> mListTPData;

    private Button mBtnEdit;
    private Button mBtnAdd;
    private Button mBtnDel;

    private Context mContext = null;
    private View mSelfView = null;

    private DVBSNetwork mCrtSatellite = null;
    private DVBSTransponder mCrtTP = null;

    private Dialog mTpEditDlg = null;
    private TextView mDlgTitle = null;
    private TextView mDlgFrequency = null;
    private TextView mDlgRate = null;
    private Combox mDlgPolarityType = null;

    private Button mDlgBtnSave = null;
    private Button mDlgBtnCancel = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSelfView = inflater.inflate(R.layout.install_dvbs_tplist, container, false);
        mContext = inflater.getContext();
        initCtrl(mSelfView);
        refreshData();
        return mSelfView;
    }

    private void initCtrl(View parent) {
        mCbxSatellite = (Combox) parent.findViewById(R.id.id_dvbs_tp_satellite);
        mCbxSatellite.setOnSelectChangeListener(this);

        mLvTPs = (ListView) parent.findViewById(R.id.id_dvbs_tp_list);
        mLvTPs.setAdapter(mTPAdapter);

        Button btnStartScan = (Button) parent.findViewById(R.id.id_dvbs_tp_startscan);
        btnStartScan.setOnClickListener(this);

        mBtnSearch = (Button) parent.findViewById(R.id.id_dvbs_tp_search);
        mBtnEdit = (Button) parent.findViewById(R.id.id_dvbs_tp_edit);
        mBtnAdd = (Button) parent.findViewById(R.id.id_dvbs_tp_add);
        mBtnDel = (Button) parent.findViewById(R.id.id_dvbs_tp_delete);

        mBtnSearch.setOnClickListener(this);
        mBtnEdit.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
        mBtnDel.setOnClickListener(this);

        mListTPData = new ArrayList<CTPSelect>();
        mTPAdapter = new TPLstItemAdapter(mContext, R.layout.install_dvbs_tpitem, mListTPData);
        ((DVBSInstallActivity) getActivity()).setCurrentIScanSubWnd(this);
        mScanMainWnd = (IScanMainWnd) getActivity();
    }

    private void refreshData() {
        DTV dtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mNetworkManager = dtv.getNetworkManager();
        mChannelManager = dtv.getChannelManager();
        mSatelliteList = mNetworkManager.getNetworks(EnNetworkType.SATELLITE);

        LinkedHashMap<String, Object> lstSatellite = new LinkedHashMap<String, Object>();
        for (int i = 0; i < mSatelliteList.size(); i++) {
            DVBSNetwork satellite = (DVBSNetwork) mSatelliteList.get(i);
            if (satellite.isSelected()) {
                lstSatellite.put(satellite.getName(), satellite);
            }
        }
        mCbxSatellite.setData(lstSatellite);

        DVBSNetwork network = (DVBSNetwork) mScanMainWnd.getCrtSelectedNetwork();
        if (null == network) {
            network = (DVBSNetwork) mCbxSatellite.getTag();
        } else {
            mCbxSatellite.setText(network.getName());
        }
        refreshTPData(network);

        mLvTPs.setAdapter(mTPAdapter);
        mLvTPs.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder vHolder = (ViewHolder) view.getTag();
                vHolder.vSelect.toggle();
                CTPSelect item = (CTPSelect) mLvTPs.getSelectedItem();
                if (null != item) {
                    item.mbIsSelect = vHolder.vSelect.isChecked();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LogTool.d(LogTool.MSCAN, "onPause");
        if (mbIsChange) {
            Runnable runnable = new Runnable() {
                public void run() {
                    mCrtSatellite.saveMultiplexes();
                    mbIsChange = false;
                }
            };
            TaskUtil.post(runnable);
        }
    }

    private boolean deleteChannelByTP(DVBSTransponder item) {
        boolean isDeleteSucess = false;

        ChannelFilter tempFilter = new ChannelFilter();
        tempFilter.setSIElement(item);
        ChannelList tempList = mChannelManager.getChannelList(tempFilter);
        if ((null != tempList)) {
            isDeleteSucess = true;
            ArrayList<Integer> lstTPId = new ArrayList<Integer>();
            for (int i = 0; i < tempList.getChannelCount(); i++) {
                Channel channel = tempList.getChannelByIndex(i);
                if (null != channel) {
                    lstTPId.add(channel.getChannelID());
                }
            }

            for (int ID : lstTPId) {
                if (0 > mChannelManager.deleteChannelByID(ID)) {
                    isDeleteSucess = false;
                    break;
                }
            }
            if (isDeleteSucess) {
                isDeleteSucess = (0 == mCrtSatellite.removeMultiplex(item));
            }
        }

        return isDeleteSucess;
    }

    private void showDelTP() {
        String strTitle;
        if (null == mCrtSatellite) {
            return;
        }
        CTPSelect TPSelect = (CTPSelect) mLvTPs.getSelectedItem();
        if (null == TPSelect) {
            LogTool.d(LogTool.MSCAN, "Please select the tp first");
            return;
        }
        DVBSTransponder item = (DVBSTransponder) TPSelect.mTp;
        strTitle = String.format(getResources().getString(R.string.str_install_tpdelete_query), item.getFrequency());
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(strTitle)
                .setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        DVBSTransponder item = null;
                        List<CTPSelect> lstTmpDelTPs = new ArrayList<CTPSelect>();
                        for (int i = 0; i < mListTPData.size(); i++) {
                            CTPSelect obj = mListTPData.get(i);
                            if (obj.mbIsSelect) {
                                lstTmpDelTPs.add(obj);
                            }
                        }
                        if (0 >= lstTmpDelTPs.size()) {
                            MyToast.makeText(mContext, getResources().getString(R.string.str_install_tp_noselect),
                                    MyToast.LENGTH_LONG).show();
                        } else {
                            for (int i = 0; i < lstTmpDelTPs.size(); i++) {
                                CTPSelect obj = lstTmpDelTPs.get(i);
                                item = (DVBSTransponder) obj.mTp;
                                if (deleteChannelByTP(item)) {
                                    mListTPData.remove(obj);
                                } else {
                                    MyToast.makeText(mContext, getResources().getString(R.string.str_dtelete_fail),
                                            MyToast.LENGTH_LONG).show();
                                    break;
                                }
                            }
                            mTPAdapter.notifyDataSetChanged();
                            mbIsChange = true;
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void showEditTPDlg(DVBSTransponder tp) {
        if (null == mCrtSatellite) {
            LogTool.e(LogTool.MSCAN, "======= please insert satellite");
            return;
        }

        prepareTpEditDlg();
        mCrtTP = tp;
        mTpEditDlg.show();
        mDlgFrequency.requestFocus();
        if (null != mCrtTP) {
            mDlgFrequency.setText(String.valueOf(mCrtTP.getFrequency() / TP_UNIT_RATE));
            mDlgRate.setText(String.valueOf(mCrtTP.getSymbolRate() / TP_UNIT_RATE));
            mDlgPolarityType.setText(mCrtTP.getPolarity() == EnPolarity.VERTICAL ? "V" : "H");
            mDlgBtnSave.setText(getResources().getString(R.string.str_save));
            mDlgTitle.setText(getResources().getString(R.string.str_install_edit_tp));
        } else {
            mDlgFrequency.setText("");
            mDlgRate.setText("");
            mDlgPolarityType.setText("H");
            mDlgBtnSave.setText(getResources().getString(R.string.str_add));
            mDlgTitle.setText(getResources().getString(R.string.str_install_add_tp));
        }
    }

    private void prepareTpEditDlg() {
        if (mTpEditDlg == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View layout = inflater.inflate(R.layout.install_dvbs_tpedit,
                    (ViewGroup) mSelfView.findViewById(R.id.id_dvbs_tpedit_dialog));

            mDlgTitle = (TextView) layout.findViewById(R.id.id_dvbs_tpedit_title);
            mDlgFrequency = (TextView) layout.findViewById(R.id.id_dvbs_tpedit_frequency);
            mDlgPolarityType = (Combox) layout.findViewById(R.id.id_dvbs_tpedit_polarity);

            LinkedHashMap<String, Object> lstLdType = new LinkedHashMap<String, Object>();
            lstLdType.put("H", EnPolarity.HORIZONTAL);
            lstLdType.put("V", EnPolarity.VERTICAL);
            mDlgPolarityType.setData(lstLdType);

            mDlgRate = (TextView) layout.findViewById(R.id.id_dvbs_tpedit_rate);
            mDlgBtnSave = (Button) layout.findViewById(R.id.id_dvbs_tpedit_save);
            mDlgBtnCancel = (Button) layout.findViewById(R.id.id_dvbs_tpedit_cancel);
            mDlgBtnSave.setOnClickListener(this);
            mDlgBtnCancel.setOnClickListener(this);

            mTpEditDlg = new Dialog(mContext, R.style.DIM_STYLE);
            mTpEditDlg.setCanceledOnTouchOutside(false);
            mTpEditDlg.show();
            mTpEditDlg.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent) {
        KeyDoResult doRet = KeyDoResult.DO_NOTHING;
        if (!super.isAdded()) {
            return doRet;
        }
        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_GREEN: {
                CTPSelect tpselect = (CTPSelect) mLvTPs.getSelectedItem();
                if (null != tpselect) {
                    showEditTPDlg((DVBSTransponder) tpselect.mTp);
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_RED: {
                showEditTPDlg(null);
                break;
            }
            case KeyValue.DTV_KEYVALUE_BLUE: {
                showDelTP();
                return KeyDoResult.DO_OVER;
            }
            default:
                break;
        }
        return doRet;
    }

    @Override
    public boolean isCanStartScan() {
        boolean bRet = false;
        if (super.isAdded()) {
            List<Multiplex> lstTPs = new ArrayList<Multiplex>();
            for (int i = 0; i < mListTPData.size(); i++) {
                CTPSelect obj = mListTPData.get(i);
                if (obj.mbIsSelect) {
                    lstTPs.add(obj.mTp);
                }
            }
            bRet = 0 < lstTPs.size();
            if (false == bRet) {
                MyToast.makeText(mContext, getResources().getString(R.string.str_install_tp_noselect),
                        MyToast.LENGTH_LONG).show();
            } else {
                mCrtSatellite.setScanMultiplexes(lstTPs);
                List<Network> lstNetwork = new ArrayList<Network>();
                lstNetwork.add(mCrtSatellite);
                ((DTVApplication) getActivity().getApplication()).setScanParam(lstNetwork);
            }
        }
        return bRet;
    }

    @Override
    public boolean isNetworkScan() {
        return false;
    }

    @Override
    public void setMainWnd(IScanMainWnd parent) {
        mScanMainWnd = parent;
    }

    private void saveTPEdit() {
        if (null == mCrtSatellite) {
            return;
        }
        String strFreq = "" + mDlgFrequency.getText();
        strFreq = "" + strFreq.trim();
        String strRate = "" + mDlgRate.getText();
        strRate = "" + strRate.trim();
        if ((0 == strFreq.length()) || (0 == strRate.length())) {
            MyToast.makeText(mContext, this.getString(R.string.str_dvbs_validate_input), MyToast.LENGTH_SHORT).show();
            return;
        }
        int nRate = Integer.parseInt(strRate);
        int nFreq = Integer.parseInt(strFreq);
        nFreq = nFreq * TP_UNIT_RATE;
        nRate = nRate * TP_UNIT_RATE;

        if ((nFreq < SAT_C_MIN_KHZ) || ((nFreq > SAT_C_MAX_KHZ) && (nFreq < SAT_KU_MIN_KHZ))
                || (nFreq > SAT_KU_MAX_KHZ) || (nRate < 1) || (nRate > SAT_SYMBOL_RATE_MAX)) {
            String strTip = this.getString(R.string.str_dvbs_validate_input);
            MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
            return;
        }
        mTpEditDlg.cancel();
        if (null == mCrtTP) {
            int i = 0;
            DVBSTransponder tp = null;

            for (i = 0; i < mListTPData.size(); i++) {
                CTPSelect tempTpData = mListTPData.get(i);
                DVBSTransponder tempTp = (DVBSTransponder) tempTpData.mTp;
                if ((tempTp.getFrequency() / 1000 == nFreq / 1000) && (tempTp.getSymbolRate() / 1000 == nRate / 1000)
                        && (tempTp.getPolarity() == (EnPolarity) mDlgPolarityType.getTag())) {
                    break;
                }
            }

            if (i == mListTPData.size()) {
                tp = (DVBSTransponder) mCrtSatellite.createMultiplex();
                if (tp != null) {
                    tp.setFrequency(nFreq);
                    tp.setSymbolRate(nRate);
                    tp.setPolarity((EnPolarity) mDlgPolarityType.getTag());
                }
            }

            if (null == tp) {
                MyToast.makeText(mContext, this.getString(R.string.str_install_fail_addtp), MyToast.LENGTH_SHORT).show();
            } else {
                CTPSelect obj = new CTPSelect();
                obj.mTp = tp;
                mListTPData.add(obj);
                mTPAdapter.notifyDataSetChanged();
                mLvTPs.refreshDrawableState();
                mbIsChange = true;
            }
        } else {
            mCrtTP.setFrequency(nFreq);
            mCrtTP.setSymbolRate(nRate);
            mCrtTP.setPolarity((EnPolarity) mDlgPolarityType.getTag());
            mTPAdapter.notifyDataSetChanged();
            mbIsChange = true;
        }
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.id_dvbs_tpedit_save: {
                saveTPEdit();
                break;
            }
            case R.id.id_dvbs_tpedit_cancel: {
                mTpEditDlg.cancel();
                break;
            }
            case R.id.id_dvbs_tp_startscan:
            case R.id.id_dvbs_tp_search: {
                if (null != mScanMainWnd) {
                    mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_READY_TO_SCAN, null);
                }
                break;
            }
        }
    }

    private void refreshTPData(DVBSNetwork satellite) {
        if (null == satellite) {
            return;
        }
        if ((null != mCrtSatellite) && (mCrtSatellite.getID() == satellite.getID())) {
            return;
        }
        mCrtSatellite = satellite;
        mbIsChange = false;
        mListTPData.clear();
        if (null != mCrtSatellite) {
            List<Multiplex> lstTPs = mCrtSatellite.getMultiplexes();
            if (null != lstTPs) {
                for (int i = 0; i < lstTPs.size(); i++) {
                    CTPSelect obj = new CTPSelect();
                    obj.mTp = lstTPs.get(i);
                    mListTPData.add(obj);
                }
            }
        }
        mScanMainWnd.setCrtSelectedNetwor(mCrtSatellite);
        mTPAdapter.notifyDataSetChanged();
        mLvTPs.refreshDrawableState();
    }

    @Override
    public void onComboxSelectChange(View arg0, String strText, Object obj, int index) {
        if (R.id.id_dvbs_tp_satellite == arg0.getId()) {
            refreshTPData((DVBSNetwork) obj);
        }
    }

    public final class CTPSelect {
        public Multiplex mTp;
        public boolean mbIsSelect = false;
    }

    public final class ViewHolder {
        public TextView vFrequency;
        public TextView vRate;
        public TextView vPolarity;
        public CheckBox vSelect;
    }

    public class TPLstItemAdapter extends ArrayAdapter<CTPSelect> {

        int mResource;
        private final LayoutInflater mInflater;

        public TPLstItemAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            mResource = textViewResourceId;
            this.mInflater = LayoutInflater.from(context);
        }

        public TPLstItemAdapter(Context context, int resource, List<CTPSelect> items) {
            super(context, resource, items);
            mResource = resource;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.install_dvbs_tpitem, null);

                holder.vFrequency = (TextView) convertView.findViewById(R.id.id_install_tp_frq);
                holder.vRate = (TextView) convertView.findViewById(R.id.id_install_tp_rate);
                holder.vPolarity = (TextView) convertView.findViewById(R.id.id_install_tp_polarity);
                holder.vSelect = (CheckBox) convertView.findViewById(R.id.id_install_tp_select);
                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }
            holder.vSelect.setOnCheckedChangeListener(null);

            CTPSelect obj = getItem(position);
            DVBSTransponder tp = (DVBSTransponder) obj.mTp;
            holder.vFrequency.setText(String.valueOf(tp.getFrequency() / TP_UNIT_RATE)
                    + getResources().getString(R.string.str_freq_unit));
            holder.vRate.setText(String.valueOf(tp.getSymbolRate() / TP_UNIT_RATE)
                    + getResources().getString(R.string.str_rate_unit));

            holder.vPolarity.setText((tp.getPolarity() == EnPolarity.VERTICAL ? "V" : "H"));

            holder.vSelect.setChecked(obj.mbIsSelect);
            holder.vSelect.setTag(obj);

            holder.vSelect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    CTPSelect item = (CTPSelect) buttonView.getTag();
                    item.mbIsSelect = isChecked;
                }
            });
            return convertView;
        }
    }
}
