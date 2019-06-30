package com.hisilicon.tvui.view;

import java.util.Iterator;
import java.util.LinkedHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.LogTool;

/**
 * The custom drop-down box.
 *
 * <p>
 * <b>XML attributes</b>
 * <p>
 * See {@link R.styleable#popupWindow popupWindow Attributes},
 * {@link R.styleable#View View Attributes}
 * @author z00120637
 * updated : z00184946
 *
 */
public class Combox extends Button
{
    private View mVwCbxSelf = null;
    private ListView mLstView = null;
    private PopupWindow mPwdwDowVw = null;
    private final Context mContext;
    private String mStrText = null;
    private LinkedHashMap<String, Object> mMapData = new LinkedHashMap<String, Object>();
    private OnComboxSelectChangeListener mSelectChangeListener = null;
    private TypedArray mPopupArray = null;
    private PopupAdapter mPopupAdapter = null;
    private static final int DEFAULTPOPUPHEIGHT = 186;
    private static final int DEFAULT_ITEM_HIGHT = 31;
    private int mLineHeight = DEFAULT_ITEM_HIGHT;
    private String[] mKeyArray = null;
    private Object[] mValueArray = null;
    private int popupHeight;

    /**
     * Interface definition for a callback to be invoked when an action is performed on the
     * drop-down items.
     */
    public interface OnComboxSelectChangeListener
    {
        void onComboxSelectChange(android.view.View arg0, String strText, Object obj, int index);
    }

    public Combox(Context context)
    {
        super(context);
        mContext = context;
        init();
    }

    public Combox(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
        getAttrValues(context, attrs);
        init();
    }

    public Combox(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        getAttrValues(context, attrs);
        init();
    }

    /**
     * Get the custom value set in the XML .
     * @param context
     * @param attrs
     */
    private void getAttrValues(Context context, AttributeSet attrs)
    {
        mPopupArray = context.obtainStyledAttributes(attrs, R.styleable.popupWindow);
        popupHeight = DEFAULTPOPUPHEIGHT;
        if (null == mPopupArray)
        {
            return;
        }
        popupHeight = (int) mPopupArray.getDimension(R.styleable.popupWindow_listHeight, DEFAULTPOPUPHEIGHT);

        mLineHeight = (int) mPopupArray.getDimension(R.styleable.popupWindow_lineHeight, DEFAULT_ITEM_HIGHT);
        int entriesID = mPopupArray.getResourceId(R.styleable.popupWindow_entries, 0);
        if (entriesID != 0)
        {
            mKeyArray = getResources().getStringArray(entriesID);
        }
        int entriesValuesID = mPopupArray.getResourceId(R.styleable.popupWindow_entriesValue, 0);
        if (entriesValuesID != 0)
        {
            mValueArray = getResources().getStringArray(entriesValuesID);
        }
    }

    /**
     * Set the height of the drop-down list
     * @param popupHeight
     */
    public void setPopupHeight(int height)
    {
        popupHeight = height;
    }

    /**
     * Get the height of the drop-down list
     * @return
     */
    public int getPopupHeight()
    {
        return popupHeight;
    }

    /**
     * Set the height of the drop-down list item
     * @param lineHeight
     */
    public void setPopupLineHeight(int lineHeight)
    {
        mLineHeight = lineHeight;
    }

    /**
     * Get the height of the drop-down list item
     * @return
     */
    public int getPopupLineHeight()
    {
        return mLineHeight;
    }

    public void setOnSelectChangeListener(OnComboxSelectChangeListener l)
    {
        mSelectChangeListener = l;
    }

    /**
     * Set the display content of the Combox
     * @param strValue the content to set
     */
    public void setText(String strValue)
    {
        mStrText = strValue;
        super.setText(mStrText);
    }

    @Override
    public void setTag(Object obj)
    {
        super.setTag(obj);
        boolean isNotFind = true;
        if ((null == mKeyArray) || (0 == mKeyArray.length))
        {
            return;
        }
        if (mMapData.containsValue(obj))
        {
            for (int index = 0; index < mValueArray.length; index++)
            {
                if (mValueArray[index].equals(obj))
                {
                    setText(mKeyArray[index]);
                    isNotFind = false;
                    break;
                }
            }
        }

        if (isNotFind)
        {
            setText(mKeyArray[0]);
        }
    }

    /**
     * Get the item corresponding to the item value
     */
    @Override
    public Object getTag()
    {
        if (mMapData.containsKey(mStrText))
        {
            return mMapData.get(mStrText);
        }
        return null;
    }

    /**
     * Get the display content of the Combox
     */
    @Override
    public CharSequence getText()
    {
        return mStrText;
    }

    /**
     * Get the LinkedHashMap of the item and the item value in the the drop-down list
     * @return
     */
    public LinkedHashMap<String, Object> getData()
    {
        return mMapData;
    }

    /**
     * Set the items of the drop-down list
     * @param key
     */
    public void setEntries(String[] key)
    {
        mKeyArray = key;
    }

    /**
     * Get the items of the drop-down list
     * @return
     */
    public String[] getEntries()
    {
        return mKeyArray;
    }

    /**
     * Set the item values of the drop-down list
     * @return
     */
    public void setEntriesValue(Object[] value)
    {
        mValueArray = value;
    }

    /**
     * Get the item values of the drop-down list
     * @return
     */
    public Object[] getEntriesValue()
    {
        return mValueArray;
    }

    /**
     * Set the item and the item value of the the drop-down list
     * @param data the LinkedHashMap of the item and the item value
     */
    public void setData(LinkedHashMap<String, Object> data)
    {
        mPopupAdapter = null;
        mKeyArray = null;
        mValueArray = null;
        mMapData = null;
        setText(mContext.getString(R.string.str_no_data));
        if (null == data)
        {
            return;
        }
        mMapData = data;
        Iterator<String> keyIter = data.keySet().iterator();
        int length = mMapData.size();
        mKeyArray = new String[length];
        mValueArray = new Object[length];
        int index = 0;
        while (keyIter.hasNext())
        {
            String key = keyIter.next();
            mKeyArray[index] = key;
            mValueArray[index] = mMapData.get(key);
            index++;
        }

        if (mKeyArray.length > 0)
        {
            setText(mKeyArray[0]);
        }
    }

    @SuppressLint("InflateParams")
    private void init()
    {
        if (null == mVwCbxSelf)
        {
            mStrText = "" + super.getText();
            final Drawable btmDraw = getResources().getDrawable(android.R.drawable.arrow_down_float);
            if (null != btmDraw)
            {
                btmDraw.setBounds(-6, 0, btmDraw.getIntrinsicWidth() - 6, btmDraw.getIntrinsicHeight());
            }
            setCompoundDrawables(null, null, btmDraw, null);
            mVwCbxSelf = LayoutInflater.from(mContext).inflate(R.layout.view_combox_listview, null);
            mLstView = (ListView) mVwCbxSelf.findViewById(R.id.id_comm_cmb_lstvw);
            mLstView.setClickable(true);
            mLstView.setOnItemClickListener(new OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    mPwdwDowVw.dismiss();
                    if ((null == mKeyArray) || (0 == mKeyArray.length))
                    {
                        return;
                    }
                    if ((null == mValueArray) || (0 == mValueArray.length))
                    {
                        return;
                    }
                    String strTile = String.valueOf(mKeyArray[position]);
                    if (!mStrText.equals(strTile))
                    {
                        Combox.this.setText(strTile);

                        if (null != mSelectChangeListener)
                        {
                            mSelectChangeListener.onComboxSelectChange(Combox.this, strTile, mValueArray[position], position);
                        }
                    }
                }
            });

            setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if ((null == mKeyArray) || (0 == mKeyArray.length))
                    {
                        LogTool.w(LogTool.MBASE, "not can select,not data");
                        return;
                    }
                    int nHight = popupHeight > mLineHeight * mKeyArray.length ? mLineHeight * mKeyArray.length : popupHeight;
                    // int nHight = popupHeight;
                    if (mPwdwDowVw == null)
                    {
                        LogTool.d(LogTool.MBASE, "the width is " + Combox.this.getWidth());

                        mPwdwDowVw = new PopupWindow(Combox.this.mVwCbxSelf, Combox.this.getWidth() - 2, nHight, true);
                        mPopupAdapter = new PopupAdapter(mContext, mKeyArray, mPopupArray);
                        mLstView.setAdapter(mPopupAdapter);
                        mLstView.setSelection(getPopupDefaultSelectedIndex(Combox.this));
                        mPwdwDowVw.setBackgroundDrawable(new BitmapDrawable());
                        mPwdwDowVw.setOutsideTouchable(true);
                        mPwdwDowVw.showAsDropDown(Combox.this, 1, 0);
                    }
                    else
                    {
                        if (null == mPopupAdapter)
                        {
                            mPopupAdapter = new PopupAdapter(mContext, mKeyArray, mPopupArray);
                            mPopupAdapter.notifyDataSetChanged();
                            mLstView.setAdapter(mPopupAdapter);
                        }
                        if (mPwdwDowVw.isShowing())
                        {
                            mPwdwDowVw.dismiss();
                        }
                        else
                        {
                            mPwdwDowVw.setHeight(nHight);
                            mPwdwDowVw.showAsDropDown(Combox.this, 1, 0);
                        }
                    }
                }

            });
            setText(mContext.getString(R.string.str_no_data));
        }
    }

    /** use to hide view
     */
    public void hide()
    {
        if (mPwdwDowVw != null && mPwdwDowVw.isShowing())
        {
            mPwdwDowVw.dismiss();
        }
    }

    /**
     * Get the default selected item of the drop-down list.
     * @param combox the The specified Combox
     * @return the index of the selected item
     */
    private int getPopupDefaultSelectedIndex(Combox combox)
    {
        String text = combox.getText().toString();
        String[] entries = combox.getEntries();
        if (null != entries)
        {
            for (int loop = 0; loop < entries.length; loop++)
            {
                if (entries[loop].equals(text))
                    return loop;
            }
        }
        return 0;
    }
}
