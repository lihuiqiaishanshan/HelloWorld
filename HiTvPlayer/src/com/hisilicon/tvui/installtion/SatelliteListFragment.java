package com.hisilicon.tvui.installtion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
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
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.hardware.Antenna;
import com.hisilicon.dtv.hardware.LNBData;
import com.hisilicon.dtv.network.DVBSNetwork;
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
import com.hisilicon.tvui.view.MyToast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SatelliteListFragment extends Fragment implements OnClickListener, IScanSubWnd {
    private static final int LONGITUDE_VALUE_RATE = 10;
    private static final int MAX_LONGITUDE_MAX = 3600;
    private static final int SATELLITE_MAX_COUNT = 64;

    private IScanMainWnd mScanMainWnd = null;
    private Context mContext = null;
    private View mSelfView = null;
    private List<Network> mSatelliteList = null;
    private NetworkManager mNetworkManager = null;
    private ChannelManager mChannelManager = null;

    private Button mBtnSearch;
    private Button mBtnEdit;
    private Button mBtnAdd;
    private Button mBtnDel;
    private SatelliteListAdapter mSatelliteAdapter;
    private ListView mlvSatellite;

    private TextView mDlgTitle = null;
    private TextView mDlgSatelliteName = null;
    private TextView mDlgLongitude = null;
    private Combox mDlgCbxLongType = null;
    private Dialog mSateltEditDlg = null;
    private Button mDlgBtnSave = null;
    private Button mDlgBtnCancel = null;

    private DVBSNetwork mCrtEditNetwork = null;
    private boolean mIsChanged = false;
    private ProgressDialog mSavePrsDlg = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSelfView = inflater.inflate(R.layout.install_dvbs_satellitelist, container, false);
        mContext = inflater.getContext();
        initCtrl(mSelfView);
        return mSelfView;
    }

    private void initCtrl(View parent) {
        mlvSatellite = (ListView) parent.findViewById(R.id.id_dvbs_stllite_list);
        mlvSatellite.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SatelliteHolder vHollder = (SatelliteHolder) view.getTag();
                vHollder.vSelect.toggle();
                DVBSNetwork item = (DVBSNetwork) mlvSatellite.getSelectedItem();
                if (null != item) {
                    item.setSelect(vHollder.vSelect.isChecked());
                }
            }
        });

        mBtnSearch = (Button) parent.findViewById(R.id.id_dvbs_network_startscan);
        mBtnEdit = (Button) parent.findViewById(R.id.id_dvbs_stllite_edit);
        mBtnAdd = (Button) parent.findViewById(R.id.id_dvbs_stllite_add);
        mBtnDel = (Button) parent.findViewById(R.id.id_dvbs_stllite_delete);

        mBtnEdit.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
        mBtnSearch.setOnClickListener(this);
        mBtnDel.setOnClickListener(this);
        ((DVBSInstallActivity) getActivity()).setCurrentIScanSubWnd(this);
        mScanMainWnd = (IScanMainWnd) getActivity();
    }

    @Override
    public void onPause() {
        LogTool.d(LogTool.MSCAN, " ==== mIsChanged= === " + mIsChanged);
        if (mIsChanged) {
            Runnable runnable = new Runnable() {
                public void run() {
                    mNetworkManager.saveNetworks();
                    mIsChanged = false;
                }
            };
            TaskUtil.post(runnable);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsChanged = false;
        refreshData();
    }

    private void refreshData() {
        DTV dtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mNetworkManager = dtv.getNetworkManager();
        mSatelliteList = mNetworkManager.getNetworks(EnNetworkType.SATELLITE);
        if (mSatelliteList != null && mSatelliteList.size() > 0) {
            LogTool.d(LogTool.MINSTALL, "==== Satellite list size " + mSatelliteList.size());
            for (Network satellite : mSatelliteList) {
                LogTool.d(LogTool.MINSTALL, "==== sate id " + satellite.getID() +
                        " name " + ((DVBSNetwork) satellite).getName());
            }
        }
        mChannelManager = dtv.getChannelManager();

        mSatelliteAdapter = new SatelliteListAdapter(mContext, 0, mSatelliteList);
        mlvSatellite.setAdapter(mSatelliteAdapter);
    }

    private boolean isSatelliteNameExisted(String strName, DVBSNetwork dvbsNetwork) {
        boolean bExist = false;
        for (Network network : mSatelliteList) {
            DVBSNetwork satellite = (DVBSNetwork) network;
            if ((null != dvbsNetwork) && (dvbsNetwork.getID() == satellite.getID())) {
                continue;
            }
            if (satellite.getName().equals(strName)) {
                bExist = true;
                break;
            }
        }

        return bExist;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_dvbs_stltedit_save: {
                if (null != mSateltEditDlg) {
                    String strName = mDlgSatelliteName.getText().toString();
                    strName = strName.trim();
                    String strLong = mDlgLongitude.getText().toString();
                    strLong = strLong.trim();
                    LogTool.d(LogTool.MSCAN, "mDlgLongitude=" + (0 == strLong.length()) + ",len=" + strLong.length());
                    if ((0 == strName.length()) || (0 == strLong.length())) {
                        mDlgSatelliteName.requestFocus();
                        String strTip = this.getString(R.string.str_no_input_tip);
                        MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isSatelliteNameExisted(strName, mCrtEditNetwork)) {
                        mDlgSatelliteName.requestFocus();
                        String strTip = this.getString(R.string.str_install_network_name_exist);
                        MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
                        return;
                    }
                    float lLong = Float.parseFloat(strLong) * LONGITUDE_VALUE_RATE;
                    int nLong = (int) lLong;
                    if ((nLong < 0) || (nLong > (MAX_LONGITUDE_MAX / 2))) {
                        mDlgLongitude.requestFocus();
                        String strForm = this.getString(R.string.str_install_range_out);
                        String strTip = String.format(strForm, this.getString(R.string.str_install_longitude), 0,
                                (MAX_LONGITUDE_MAX / LONGITUDE_VALUE_RATE / 2));
                        MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
                        // mDlgLongitude.getF
                        return;
                    }
                    if (!((Boolean) mDlgCbxLongType.getTag())) {
                        nLong = MAX_LONGITUDE_MAX - nLong;
                    }
                    mSateltEditDlg.cancel();
                    if (null == mCrtEditNetwork) {
                        DVBSNetwork satellite1 = (DVBSNetwork) mNetworkManager.createNetwork(EnNetworkType.SATELLITE);
                        if (null != satellite1) {
                            satellite1.setName(strName);
                            satellite1.setLongitude(nLong);
                            satellite1.setSelect(true);
                            // set default LNB data
                            Antenna defaultAntenna = satellite1.getAntenna();
                            defaultAntenna.setLNBData(new LNBData(LNBData.EnLNBBand.LNB_BAND_C,
                                    LNBData.EnLNBType.LNB_SINGLE_LO, 5150, 5150));
                            defaultAntenna.save();
                            LogTool.d(LogTool.MSCAN, "Add success satellite id=" + satellite1.getID());
                            mSatelliteList.add(satellite1);
                            mIsChanged = true;
                        } else {
                            MyToast.makeText(mContext, getString(R.string.str_install_fail_addsatellite),
                                    MyToast.LENGTH_LONG).show();
                        }
                    } else {
                        mCrtEditNetwork.setName(strName);
                        mCrtEditNetwork.setLongitude(nLong);
                        mIsChanged = true;

                    }
                    mSatelliteAdapter.notifyDataSetChanged();
                }
                break;
            }
            case R.id.id_dvbs_stltedit_cancel: {
                mSateltEditDlg.cancel();
                break;
            }
            case R.id.id_dvbs_network_startscan: {
                if (null != mScanMainWnd) {
                    mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_READY_TO_SCAN, null);
                }
                break;
            }
            default: {
                LogTool.d(LogTool.MSCAN, "onClick no view will be find");
                break;
            }
        }
    }

    public final class SatelliteHolder {
        public TextView vName;
        public TextView vLongitude;
        public CheckBox vSelect;
    }

    public class SatelliteListAdapter extends ArrayAdapter<Network> {
        private final LayoutInflater mLayoutInflater;

        public SatelliteListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        public SatelliteListAdapter(Context context, int resource, List<Network> items) {
            super(context, resource, items);
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String strLongValue;
            SatelliteHolder holder;
            if (null == convertView) {
                holder = new SatelliteHolder();
                convertView = mLayoutInflater.inflate(R.layout.install_dvbs_satelliteitem, null);
                holder.vName = (TextView) convertView.findViewById(R.id.id_dvbs_satellite_name);
                holder.vLongitude = (TextView) convertView.findViewById(R.id.id_dvbs_satellite_loc);
                holder.vSelect = (CheckBox) convertView.findViewById(R.id.id_dvbs_satellite_select);
            } else {
                holder = (SatelliteHolder) convertView.getTag();
            }

            DVBSNetwork satellite = (DVBSNetwork) getItem(position);
            holder.vSelect.setOnCheckedChangeListener(null);

            holder.vName.setText(satellite.getName());
            strLongValue = ((float) satellite.getLongitude() / LONGITUDE_VALUE_RATE) + " E";
            if (satellite.getLongitude() > (MAX_LONGITUDE_MAX / 2)) {
                strLongValue =
                        (((float) MAX_LONGITUDE_MAX - (float) satellite.getLongitude()) / LONGITUDE_VALUE_RATE) + " W";
            }

            holder.vLongitude.setText(strLongValue);
            holder.vSelect.setChecked(satellite.isSelected());
            holder.vSelect.setTag(satellite);
            convertView.setTag(holder);

            holder.vSelect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    DVBSNetwork item = (DVBSNetwork) buttonView.getTag();
                    item.setSelect(isChecked);
                    mIsChanged = true;
                    LogTool.d(LogTool.MSCAN, " ==== id=" + item.getID() + ",bool=" + item.isSelected());
                }

                ;
            });
            return convertView;
        }
    }

    private void showEditNetwork(DVBSNetwork satellite) {
        mCrtEditNetwork = satellite;
        prepareSateltEditDlg();
        mSateltEditDlg.show();
        mDlgSatelliteName.requestFocus();
        if (null != mCrtEditNetwork) {
            String strType = getResources().getString(R.string.str_install_east);
            float lValue = (float) mCrtEditNetwork.getLongitude() / LONGITUDE_VALUE_RATE;
            if (mCrtEditNetwork.getLongitude() > (MAX_LONGITUDE_MAX / 2)) {
                strType = getResources().getString(R.string.str_install_west);
                lValue = ((float) MAX_LONGITUDE_MAX - (float) mCrtEditNetwork.getLongitude()) / LONGITUDE_VALUE_RATE;
            }
            mDlgCbxLongType.setText(strType);
            mDlgLongitude.setText(String.valueOf(lValue));
            mDlgSatelliteName.setText(mCrtEditNetwork.getName());
            mDlgBtnSave.setText(getResources().getString(R.string.str_save));
            mDlgTitle.setText(getResources().getString(R.string.str_install_edit_satellite));
        } else {
            mDlgSatelliteName.setText("");
            mDlgLongitude.setText("");
            mDlgBtnSave.setText(getResources().getString(R.string.str_add));
            mDlgTitle.setText(getResources().getString(R.string.str_install_add_satellite));
        }
    }

    private void prepareSateltEditDlg() {
        if (mSateltEditDlg == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View layout = inflater.inflate(R.layout.install_dvbs_satelliteedit,
                    (ViewGroup) mSelfView.findViewById(R.id.id_dvbs_networkedit_dialog));

            mDlgTitle = (TextView) layout.findViewById(R.id.id_dvbs_stltedit_title);
            mDlgSatelliteName = (TextView) layout.findViewById(R.id.id_dvbs_stltedit_name);
            mDlgLongitude = (TextView) layout.findViewById(R.id.id_dvbs_stltedit_lgtvalue);
            mDlgCbxLongType = (Combox) layout.findViewById(R.id.id_dvbs_stltedit_longtype);
            mDlgBtnSave = (Button) layout.findViewById(R.id.id_dvbs_stltedit_save);
            mDlgBtnCancel = (Button) layout.findViewById(R.id.id_dvbs_stltedit_cancel);
            mDlgBtnSave.setOnClickListener(this);
            mDlgBtnCancel.setOnClickListener(this);

            LinkedHashMap<String, Object> lstLdType = new LinkedHashMap<String, Object>();
            lstLdType.put(getResources().getString(R.string.str_install_east), true);
            lstLdType.put(getResources().getString(R.string.str_install_west), false);
            mDlgCbxLongType.setData(lstLdType);

            mSateltEditDlg = new Dialog(mContext, R.style.DIM_STYLE);
            mSateltEditDlg.setCanceledOnTouchOutside(false);
            mSateltEditDlg.show();
            mSateltEditDlg.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
        }
    }

    private boolean deleteSatellite(DVBSNetwork satellite) {
        boolean isDeleteSuccess = false;

        if (0 == mChannelManager.deleteChannelsBySIElement(satellite)) {
            List<Multiplex> lstTPs = satellite.getMultiplexes();
            for (Multiplex tp : lstTPs) {
                if (0 != satellite.removeMultiplex(tp)) {
                    return isDeleteSuccess;
                }
            }
            isDeleteSuccess = (0 == mNetworkManager.removeNetwork(satellite));
        }
        return isDeleteSuccess;
    }

    private void showDelNetwork() {
        DVBSNetwork satellite = (DVBSNetwork) mlvSatellite.getSelectedItem();
        if (null == satellite) {
            MyToast.makeText(mContext, getResources().getString(R.string.str_install_network_noselect),
                    MyToast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.str_install_network_delete_query))
                .setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                DVBSNetwork satellite = null;
                List<Network> lstTmpDelSats = new ArrayList<Network>();
                for (int i = 0; i < mSatelliteList.size(); i++) {
                    Network obj = mSatelliteList.get(i);
                    if (obj.isSelected()) {
                        lstTmpDelSats.add(obj);
                    }
                }
                if (0 >= lstTmpDelSats.size()) {
                    MyToast.makeText(mContext, getResources().getString(R.string.str_install_network_noselect),
                            MyToast.LENGTH_LONG).show();
                } else {
                    for (int i = 0; i < lstTmpDelSats.size(); i++) {
                        Network obj = lstTmpDelSats.get(i);
                        satellite = (DVBSNetwork) obj;
                        if (deleteSatellite(satellite)) {
                            mIsChanged = true;
                            mSatelliteList.remove(satellite);
                        } else {
                            MyToast.makeText(mContext, getResources().getString(R.string.str_dtelete_fail),
                                    MyToast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    mSatelliteAdapter.notifyDataSetChanged();
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

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent) {
        KeyDoResult doRet = KeyDoResult.DO_NOTHING;
        if (!super.isAdded()) {
            return doRet;
        }
        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_GREEN: {
                DVBSNetwork satellite = (DVBSNetwork) mlvSatellite.getSelectedItem();
                if (null != satellite) {
                    showEditNetwork(satellite);
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_RED: {
                if (mSatelliteList.size() >= SATELLITE_MAX_COUNT) {
                    MyToast.makeText(mContext, getString(R.string.str_install_network_reach_max),
                            MyToast.LENGTH_LONG).show();
                } else {
                    showEditNetwork(null);
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_BLUE: {
                List<Network> lstTmpDelSats = new ArrayList<Network>();
                if (mSatelliteList != null) {
                    for (int i = 0; i < mSatelliteList.size(); i++) {
                        Network obj = mSatelliteList.get(i);
                        if (obj.isSelected()) {
                            lstTmpDelSats.add(obj);
                        }
                    }
                }
                LogTool.d(LogTool.MSCAN, " ==== id ==== ");
                if (0 >= lstTmpDelSats.size()) {
                    MyToast.makeText(mContext, getResources().getString(R.string.str_install_network_noselect),
                            MyToast.LENGTH_SHORT).show();
                } else {
                    showDelNetwork();
                }
                return KeyDoResult.DO_OVER;
            }
            default: {
                break;
            }
        }
        return doRet;
    }

    @Override
    public boolean isCanStartScan() {
        boolean bRet = false;
        if (super.isAdded()) {
            List<Network> lstNetwork = new ArrayList<Network>();
            for (Network network : mSatelliteList) {
                DVBSNetwork satellite = (DVBSNetwork) network;
                if (satellite.isSelected()) {
                    lstNetwork.add(satellite);
                }
            }
            bRet = 0 < lstNetwork.size();
            if (lstNetwork.size() == 0) {
                MyToast.makeText(mContext, getResources().getString(R.string.str_install_network_noselect),
                        MyToast.LENGTH_LONG).show();
            }
            ((DTVApplication) getActivity().getApplication()).setScanParam(lstNetwork);
        }
        return bRet;
    }

    @Override
    public boolean isNetworkScan() {
        return true;
    }

    @Override
    public void setMainWnd(IScanMainWnd parent) {
        mScanMainWnd = parent;
    }
}
