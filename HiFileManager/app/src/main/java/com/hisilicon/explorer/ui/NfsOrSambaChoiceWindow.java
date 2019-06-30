package com.hisilicon.explorer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.adapter.NfsOrSamabaChoiceAdapter;
import com.hisilicon.explorer.model.BaseServerInfo;
import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.model.NFSItemInfo;
import com.hisilicon.explorer.utils.MenuUtils;

import java.util.ArrayList;
import java.util.List;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;
import static android.widget.AbsListView.CHOICE_MODE_SINGLE;

/**
 * 文件列表悬着窗口
 */

public class NfsOrSambaChoiceWindow<T extends BaseServerInfo> {
    private Context mContext;
    private List<T> list;
    private View btn_ok;
    private View btn_cancel;
    private IItemChoiced iItemChoiced;
    private ListView lv_choice;
    private AlertDialog alertDialog;
    private NfsOrSamabaChoiceAdapter multipleChoiceAdapter;
    private int selectPosition;

    public NfsOrSambaChoiceWindow(Context context, List<T> list, int selectPosition) {
        this.mContext = context;
        this.list = list;
        this.selectPosition = selectPosition;
        initView();
    }

    private void initView() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.multiple_choice_layout, null);
        alertDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
        alertDialog.setView(inflate);
        lv_choice = (ListView) inflate.findViewById(R.id.lv_choice);
        multipleChoiceAdapter = new NfsOrSamabaChoiceAdapter(mContext, list);
        lv_choice.setChoiceMode(CHOICE_MODE_MULTIPLE);
        lv_choice.setAdapter(multipleChoiceAdapter);
        lv_choice.setItemsCanFocus(false);
        lv_choice.setItemChecked(selectPosition, true);
        lv_choice.setSelection(selectPosition);
        btn_ok = (Button) inflate.findViewById(R.id.btn_ok);
        btn_ok.requestFocus();
        btn_cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(onClickListener);
        btn_cancel.setOnClickListener(onClickListener);
    }

    public void show() {
        alertDialog.show();
    }

    public void setChoicedListener(IItemChoiced iItemChoiced) {
        this.iItemChoiced = iItemChoiced;
    }

    public void setChoiceMode(int mode) {
        if (null != lv_choice) {
            lv_choice.setChoiceMode(mode);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_ok:
                    List<T> tempList = new ArrayList<T>();
                    long[] checkedItemIds = lv_choice.getCheckedItemIds();
                    if (null == checkedItemIds || checkedItemIds.length == 0) {
                        //TODO 提示请选择文件
                        alertDialog.dismiss();
                        return;
                    }
                    for (long id : checkedItemIds) {
                        tempList.add(list.get((int) id));
                    }
                    if (null != iItemChoiced) {
                        iItemChoiced.getChoiceItem(tempList);
                    }
                    alertDialog.dismiss();
                    break;
                case R.id.btn_cancel:
                    alertDialog.dismiss();
                    break;
            }
        }
    };

    public interface IItemChoiced<T> {
        void getChoiceItem(List<T> list);
    }
}
