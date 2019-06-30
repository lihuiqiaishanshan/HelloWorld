
package com.hisilicon.launcher.view.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.EtherInterface;

/**
 * the dialog of input Ethernet info
 *
 * @author huyq
 */
public class EtherInputDialog extends Dialog implements
        android.view.View.OnClickListener {
    private Context mContext;


    private ContentResolver mContentResolver;

    // EditText of account
    private EditText accountEdit;
    // EditText of password
    private EditText passwordEdit;
    // CheckBox of autoConnect
    private CheckBox autoConnectCb;
    // button of positive
    private Button positiveBtn;
    // button of cancel
    private Button cancelBtn;
    // flag of is AutoReconnect
    private Boolean isAutoReconnect = false;

    public EtherInputDialog(Context context, Handler handler) {
        super(context, R.style.Translucent_NoTitle);
        mContext = context;
        mContentResolver = mContext.getContentResolver();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ether_input_dialog);
        initView();
    }

    /**
     * The initialization of view
     */
    private void initView() {
        accountEdit = (EditText) findViewById(R.id.ether_account_input);
        accountEdit.setText(EtherInterface.getPppoeUsername(mContext));

        passwordEdit = (EditText) findViewById(R.id.ether_pwd_input);
        passwordEdit.setText(EtherInterface.getPppoePassword(mContext));

        autoConnectCb = (CheckBox) findViewById(R.id.ether_autoconnect_cb);
        isAutoReconnect = EtherInterface.getAutoReconnectState(mContext);
        autoConnectCb.setChecked(isAutoReconnect);

        positiveBtn = (Button) findViewById(R.id.ether_ok_btn);
        positiveBtn.setOnClickListener(this);
        cancelBtn = (Button) findViewById(R.id.ether_cancle_btn);
        cancelBtn.setOnClickListener(this);
        autoConnectCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                isAutoReconnect = isChecked;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ether_ok_btn) {
            String account = accountEdit.getText().toString();
            String pwd = passwordEdit.getText().toString();
            if (null == account || account.isEmpty() || null == pwd || pwd.isEmpty()) {
                return;
            }
            EtherInterface.connectPppoe(mContext, mContentResolver, account, pwd, isAutoReconnect);
            dismiss();
        } else if (v.getId() == R.id.ether_cancle_btn) {
            dismiss();
        }
    }
}
