package com.hisilicon.explorer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.adapter.MenuParentAdapter;
import com.hisilicon.explorer.adapter.MultipleChoiceAdapter;
import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.model.MenuInfo;
import com.hisilicon.explorer.utils.MenuUtils;

import java.util.ArrayList;
import java.util.List;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;
import static android.widget.AbsListView.CHOICE_MODE_SINGLE;

/**
 * 文件列表悬着窗口
 */

public class MultipleChoiceWindow {
    private Context mContext;
    private List<FileInfo> list;
    private View btn_ok;
    private View btn_cancel;
    private IFileChoiced iFileChoiced;
    private ListView lv_choice;
    private AlertDialog alertDialog;
    private MultipleChoiceAdapter multipleChoiceAdapter;
    private int operateType;
    private int selectPosition;

    public MultipleChoiceWindow(Context context, List<FileInfo> list, int type, int selectPosition) {
        this.mContext = context;
        this.list = list;
        operateType = type;
        this.selectPosition = selectPosition;
        initView();
    }

    private void initView() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.multiple_choice_layout, null);
        alertDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
        alertDialog.setView(inflate);
        lv_choice = (ListView) inflate.findViewById(R.id.lv_choice);
        multipleChoiceAdapter = new MultipleChoiceAdapter(mContext, list);
        lv_choice.setAdapter(multipleChoiceAdapter);
        if (operateType == MenuUtils.FileOperationType.CUT || operateType == MenuUtils.FileOperationType.COPY
                || operateType == MenuUtils.FileOperationType.DELETE) {
            lv_choice.setChoiceMode(CHOICE_MODE_MULTIPLE);
        } else {
            lv_choice.setChoiceMode(CHOICE_MODE_SINGLE);
        }
        lv_choice.setItemsCanFocus(false);
        if (list.size() != 0) {
            lv_choice.setItemChecked(selectPosition, true);
            lv_choice.setSelection(selectPosition);
        }
        btn_ok = (Button) inflate.findViewById(R.id.btn_ok);
        btn_ok.requestFocus();
        btn_cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(onClickListener);
        btn_cancel.setOnClickListener(onClickListener);
    }

    public void show() {
        alertDialog.show();
    }

    public void setChoicedListener(IFileChoiced iFileChoiced) {
        this.iFileChoiced = iFileChoiced;
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
                    List<FileInfo> tempList = new ArrayList<FileInfo>();
                    long[] checkedItemIds = lv_choice.getCheckedItemIds();
                    if (null == checkedItemIds || checkedItemIds.length == 0) {
                        //TODO 提示请选择文件
                        alertDialog.dismiss();
                        return;
                    }
                    for (long id : checkedItemIds) {
                        tempList.add(list.get((int) id));
                    }
                    if (null != iFileChoiced) {
                        iFileChoiced.getChoiceFile(tempList, operateType);
                    }
                    alertDialog.dismiss();
                    break;
                case R.id.btn_cancel:
                    alertDialog.dismiss();
                    break;
            }
        }
    };

    public interface IFileChoiced {
        void getChoiceFile(List<FileInfo> list, int type);
    }
}
