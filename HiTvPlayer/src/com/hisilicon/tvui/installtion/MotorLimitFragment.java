package com.hisilicon.tvui.installtion;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.hardware.Motor;
import com.hisilicon.dtv.hardware.Motor.EnLimitType;
import com.hisilicon.dtv.hardware.Motor.EnMotorMoveType;
import com.hisilicon.dtv.hardware.Motor.EnMotorType;
import com.hisilicon.dtv.hardware.MotorDiSEqC12;
import com.hisilicon.dtv.network.DVBSNetwork;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;
import com.hisilicon.tvui.view.MyToast;

import java.util.LinkedHashMap;

public class MotorLimitFragment extends Fragment implements OnClickListener, IScanSubWnd, OnComboxSelectChangeListener {
    private static final int FE_COMMAND_CALL_MIN_SPACE = 150;
    private Combox mCbxLimitType = null;
    private Combox mCbxMoveStep = null;
    private Button mBtnWest = null;
    private Button mBtnEast = null;
    private Button mBtnSave = null;
    private Button mBtnGotoRef = null;
    private MotorDiSEqC12 mMotor = null;
    private Context mContext = null;

    private boolean mMoving = false;
    private IScanMainWnd mScanMainWnd = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.install_dvbs_limit, container, false);
        mContext = inflater.getContext();
        initCtrl(view);
        return view;
    }

    private void initCtrl(View parent) {
        Context context = parent.getContext();
        mCbxLimitType = (Combox) parent.findViewById(R.id.id_motor_limit_type);
        LinkedHashMap<String, Object> mapLimitType = new LinkedHashMap<String, Object>();
        mapLimitType.put(context.getString(R.string.str_install_west), EnLimitType.WEST);
        mapLimitType.put(context.getString(R.string.str_install_east), EnLimitType.EAST);
        mapLimitType.put(context.getString(R.string.str_install_disable), EnLimitType.DISABLE);
        mCbxLimitType.setData(mapLimitType);
        mCbxLimitType.setOnSelectChangeListener(this);

        mCbxMoveStep = (Combox) parent.findViewById(R.id.id_motor_limit_step);
        LinkedHashMap<String, Object> mapMoveStep = new LinkedHashMap<String, Object>();
        mapMoveStep.put(EnMotorMoveType.CONTINUE.toString(), EnMotorMoveType.CONTINUE);
        mapMoveStep.put(EnMotorMoveType.FAST.toString(), EnMotorMoveType.FAST);
        mapMoveStep.put(EnMotorMoveType.SLOW.toString(), EnMotorMoveType.SLOW);
        mCbxMoveStep.setData(mapMoveStep);
        mCbxMoveStep.setOnSelectChangeListener(this);

        mBtnWest = (Button) parent.findViewById(R.id.id_motor_limit_move_west);
        mBtnEast = (Button) parent.findViewById(R.id.id_motor_limit_move_east);
        mBtnGotoRef = (Button) parent.findViewById(R.id.id_motor_limit_refrence);
        mBtnSave = (Button) parent.findViewById(R.id.id_motor_limit_save);

        ((DVBSInstallActivity) getActivity()).setCurrentIScanSubWnd(this);
        mScanMainWnd = (IScanMainWnd) getActivity();

        mBtnWest.setOnClickListener(this);
        mBtnEast.setOnClickListener(this);
        mBtnGotoRef.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
    }

    private void refreshData() {
        DTV dtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        // 1.3 support 1.2 commands，so just use 1.2
        mMotor = (MotorDiSEqC12) dtv.getHWManager().getMotor(EnMotorType.DISEQC12);
        mMoving = false;
    }

    @Override
    public void onComboxSelectChange(View arg0, String strText, Object obj, int index) {
        if(mMotor.stopMove() != 0) {
            LogTool.d(LogTool.MBASE, "stopMove fail");
        }
        mMoving = false;
        if (arg0.getId() == R.id.id_motor_limit_type) {
            boolean enabled = EnLimitType.DISABLE != mCbxLimitType.getTag();

            mCbxMoveStep.setEnabled(enabled);
            mBtnWest.setEnabled(enabled);
            mBtnEast.setEnabled(enabled);

            mCbxMoveStep.setFocusable(enabled);
            mBtnWest.setFocusable(enabled);
            mBtnEast.setFocusable(enabled);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DTV dtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        DVBSNetwork satellite = (DVBSNetwork) mScanMainWnd.getCrtSelectedNetwork();

        boolean bEnable = (null != satellite) && (satellite.getAntenna().getMotorType() != Motor.EnMotorType.NONE);
        mCbxLimitType.setEnabled(bEnable);
        mCbxMoveStep.setEnabled(bEnable);
        mBtnWest.setEnabled(bEnable);
        mBtnEast.setEnabled(bEnable);
        mBtnSave.setEnabled(bEnable);
        mBtnGotoRef.setEnabled(bEnable);

        mCbxLimitType.setFocusable(bEnable);
        mCbxMoveStep.setFocusable(bEnable);
        mBtnWest.setFocusable(bEnable);
        mBtnEast.setFocusable(bEnable);
        mBtnSave.setFocusable(bEnable);
        mBtnGotoRef.setFocusable(bEnable);

        refreshData();
        if (null != mMotor) {
            mMotor.setAutoRolationSwitch(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mMotor) {
            if (mMoving) {
                mMoving = 0 != mMotor.stopMove();
            }
            mMotor.setAutoRolationSwitch(true);
        }
    }

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent) {
        if (!super.isAdded()) {
            return KeyDoResult.DO_NOTHING;
        }
        if ((null != mBtnEast) && (mBtnEast.isFocused()) && (KeyEvent.KEYCODE_DPAD_LEFT == keyCode)) {
            return KeyDoResult.DO_DONE_NEED_SYSTEM;
        }
        return KeyDoResult.DO_NOTHING;
    }

    @Override
    public boolean isCanStartScan() {
        return false;
    }

    @Override
    public boolean isNetworkScan() {
        return false;
    }

    @Override
    public void setMainWnd(IScanMainWnd parent) {
        mScanMainWnd = parent;
    }

    @Override
    public void onClick(View arg0) {

        if ((arg0.getId() == R.id.id_motor_limit_move_west) || (arg0.getId() == R.id.id_motor_limit_move_east)) {
            boolean bIsWest = (arg0.getId() == R.id.id_motor_limit_move_west);
            EnMotorMoveType moveType = (EnMotorMoveType) mCbxMoveStep.getTag();
            if (mMoving) {
                mMoving = (0 != mMotor.stopMove());
                mBtnWest.setEnabled(true);
                mBtnEast.setEnabled(true);
                mBtnWest.setFocusable(true);
                mBtnEast.setFocusable(true);
                mBtnWest.setText(getResources().getString(R.string.str_install_to_west));
                mBtnEast.setText(getResources().getString(R.string.str_install_to_east));

            } else {
                mMoving = (0 == mMotor.move(moveType, bIsWest));
                if (moveType != EnMotorMoveType.CONTINUE) {
                    mMoving = false;
                } else {
                    if (true == bIsWest) {
                        mBtnEast.setEnabled(false);
                        mBtnEast.setFocusable(false);
                        mBtnWest.setText(getResources().getString(R.string.str_install_stop_move));
                    } else {
                        mBtnWest.setEnabled(false);
                        mBtnWest.setFocusable(false);
                        mBtnEast.setText(getResources().getString(R.string.str_install_stop_move));
                    }
                }
            }
        } else if (R.id.id_motor_limit_save == arg0.getId()) {
            if (mMoving) {
                mMoving = (0 != mMotor.stopMove());
                mBtnWest.setEnabled(true);
                mBtnEast.setEnabled(true);
                mBtnWest.setFocusable(true);
                mBtnEast.setFocusable(true);
                mBtnWest.setText(getResources().getString(R.string.str_install_to_west));
                mBtnEast.setText(getResources().getString(R.string.str_install_to_east));

                SystemClock.sleep(FE_COMMAND_CALL_MIN_SPACE);
            }
            String strTip = this.getString(R.string.str_save_success);
            int sret = mMotor.setLimit((EnLimitType) mCbxLimitType.getTag());
            if (sret != 0) strTip = this.getString(R.string.str_save_fail);
            MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
        } else if (R.id.id_motor_limit_refrence == arg0.getId()) {
            if (mMoving) {
                mMoving = (0 != mMotor.stopMove());
                mBtnWest.setEnabled(true);
                mBtnEast.setEnabled(true);
                mBtnWest.setFocusable(true);
                mBtnEast.setFocusable(true);
                mBtnWest.setText(getResources().getString(R.string.str_install_to_west));
                mBtnEast.setText(getResources().getString(R.string.str_install_to_east));

                SystemClock.sleep(FE_COMMAND_CALL_MIN_SPACE);
            }
            mMotor.gotoZero();
        }
    }
}
