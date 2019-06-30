package com.hisilicon.tvui.play;

import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelFilter;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.channellist.ChannelListAdapter;
import com.hisilicon.tvui.util.KeyValue;

public class ChannelFindView extends BaseView implements OnItemClickListener
{
    private final MainActivity mMainActivity;
    private final ListView mChannelListView;
    private final EditText editText_channel_find_search;

    private ChannelListAdapter mChannelListAdapter;
    private ChannelFilter mChannelFilter;
    private ChannelFilter mAtvChannelFilter;
    private String mSerachStr;

    public ChannelFindView(MainActivity arg0)
    {
        super((LinearLayout) arg0.findViewById(R.id.ly_channel_find));
        mMainActivity = arg0;
        editText_channel_find_search = (EditText) mMainActivity.findViewById(R.id.editText_channel_find_search);
        mChannelListView = (ListView) mMainActivity.findViewById(R.id.listView_channel_find_channel_list);

        editText_channel_find_search.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2)
            {
                if (arg1 == EditorInfo.IME_ACTION_SEARCH)
                {
                    InputMethodManager m = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    m.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
                else if (arg1 == EditorInfo.IME_ACTION_GO)
                {
                    InputMethodManager m = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    m.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }

                return false;
             }
        });

        editText_channel_find_search.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable editable)
            {
                String editTextStr = editText_channel_find_search.getText().toString();
                if (null == mSerachStr)
                {
                    mSerachStr = editTextStr;
                    searchByStr(editTextStr);
                }
                else if (!mSerachStr.equals(editTextStr))
                {
                    searchByStr(editTextStr);
                    mSerachStr = editTextStr;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

    }

    @Override
    public void show()
    {
        super.show();
        mChannelFilter = new ChannelFilter();
        EnTVRadioFilter tempFilter = mChannelManager.getChannelServiceTypeMode();
        mChannelFilter.setGroupType(tempFilter);
        ChannelList channelList = mChannelManager.getChannelList(mChannelFilter);

        mAtvChannelFilter = new ChannelFilter();
        ChannelList atvChannelList = mAtvChannelManager.getChannelList(mAtvChannelFilter);
        if (null != channelList)
        {
            mChannelListAdapter = new ChannelListAdapter(mMainActivity, channelList, atvChannelList);
            mChannelListView.setAdapter(mChannelListAdapter);
        }

        mChannelListView.setOnItemClickListener(this);

        InputMethodManager m = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        m.toggleSoftInput(InputMethodManager.RESULT_SHOWN, 0);

        if(null != editText_channel_find_search)
        {
            editText_channel_find_search.setText("");
        }
    }

    /**
     * Search by string.
     * @param editTextStr
     */
    private void searchByStr(String editTextStr)
    {
        if (null == mChannelFilter)
        {
            return;
        }
        mChannelFilter.setFirstLetters(editTextStr);
        //mChannelFilter.setServiceNameFilter(true);
        ChannelList channelList = mChannelManager.getChannelList(mChannelFilter);

        mAtvChannelFilter.setFirstLetters(editTextStr);
        ChannelList atvChannelList = mAtvChannelManager.getChannelList(mAtvChannelFilter);
        mChannelListAdapter.setChannelList(channelList, atvChannelList);
    }

    public int onListViewKeyDown(int keyCode, KeyEvent event)
    {
        if (KeyValue.DTV_KEYVALUE_BACK == keyCode)
        {
            hide();
        }
        return RET_SUPER_FALSE;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        switch (arg0.getId())
        {
        case R.id.listView_channel_find_channel_list:
        {
            Channel selectedChannel = (Channel) arg0.getSelectedItem();
            if (null == selectedChannel)
            {
                return;
            }
            List<ChannelList> allGroups = mChannelManager.getUseGroups();
            if (allGroups.isEmpty())
            {
                return;
            }
            ChannelList channelList = allGroups.get(0);
            mMainActivity.playChannel(channelList, selectedChannel, true);
            hide();
        }
            break;
        default:
            break;
        }
    }
}
