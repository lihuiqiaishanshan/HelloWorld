/**
 *
 */
package com.hisilicon.tvui.view;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.EnFavTag;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.KeyValue;

public class FavoriteSelectDialog extends Dialog implements OnItemClickListener
{
    private Context mContext = null;
    private ListView listView_fav_selete;
    private Channel mChannel;

    public FavoriteSelectDialog(Context context, int theme, Channel channel, float alpha)
    {
        super(context, theme);
        mContext = context;
        mChannel = channel;
        WindowManager.LayoutParams lp=this.getWindow().getAttributes();
        lp.alpha = alpha;
        this.getWindow().setAttributes(lp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_BACK:
            this.dismiss();
            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView()
    {
        setContentView(R.layout.add_to_fav_dialog);
        listView_fav_selete = (ListView) findViewById(R.id.listView_fav_selete);
        SparseBooleanArray boolFavMultiChoiseDialog = getChannelFavSparseBooleanArray();

        String[] favMultiChoiseString = new String[EnFavTag.FAV_16.ordinal()];
        EnFavTag[] favValues = EnFavTag.values();
        for (int i = 1; i < favValues.length; i++)
        {
            favMultiChoiseString[i - 1] = favValues[i].toString();
        }

        FavChoicAdapter mAdapter = new FavChoicAdapter(mContext, favMultiChoiseString, boolFavMultiChoiseDialog);
        listView_fav_selete.setAdapter(mAdapter);
        listView_fav_selete.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView_fav_selete.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        SparseBooleanArray boolChoiceArray = ((FavChoicAdapter) listView_fav_selete.getAdapter()).getSparseBooleanArray();
        if (null == boolChoiceArray)
        {
            return;
        }
        boolean isCheck = boolChoiceArray.get(arg2);
        if (isCheck)
        {
            boolChoiceArray.put(arg2, false);
        }
        else
        {
            boolChoiceArray.put(arg2, true);
        }

        ((FavChoicAdapter) listView_fav_selete.getAdapter()).setSparseBooleanArray(boolChoiceArray);

    }

    /**
     * Refresh by channel.
     * @param channle
     */
    public void refreshByChannel(Channel channle)
    {
        if (null == channle)
        {
            return;
        }
        mChannel = channle;
        SparseBooleanArray boolFavMultiChoiseDialog = getChannelFavSparseBooleanArray();
        ((FavChoicAdapter) listView_fav_selete.getAdapter()).setSparseBooleanArray(boolFavMultiChoiseDialog);
    }

    /**
     * Get SparseBooleanArray about favorite tag.
     * @return
     */
    private SparseBooleanArray getChannelFavSparseBooleanArray()
    {
        SparseBooleanArray boolFavMultiChoiseDialog = new SparseBooleanArray();
        if (null == mChannel)
        {
            return boolFavMultiChoiseDialog;
        }
        List<EnFavTag> favTags = mChannel.getFavTag();
        if(null != favTags)
        {
            if (favTags.contains(EnFavTag.FAV_ALL))
            {
                favTags.remove(EnFavTag.FAV_ALL);
            }

            for (int i = 0; i < favTags.size(); i++)
            {
                boolFavMultiChoiseDialog.append(favTags.get(i).ordinal() - 1, true);
            }
        }

        return boolFavMultiChoiseDialog;
    }

    /**
     * Get SparseBooleanArray about checked item.
     * @return
     */
    public SparseBooleanArray getListCheckedItemPositions()
    {
        return ((FavChoicAdapter) listView_fav_selete.getAdapter()).getSparseBooleanArray();
    }
}
