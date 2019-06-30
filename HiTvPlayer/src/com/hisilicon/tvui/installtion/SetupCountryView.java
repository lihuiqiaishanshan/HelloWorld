package com.hisilicon.tvui.installtion;

import java.util.ArrayList;

import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.pc.ParentalControlManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.KeyValue;

public class SetupCountryView extends BaseView implements IScanSubWnd
{
    private static final String TAG = "SetupCountryView";
    private static final int NextPage = 1;
    private static final int PrePage = 0;

    private ArrayList<String> mLocaleCountryDisplayNames = new ArrayList<String>();
    private GridItemAdapter languageItemAdapter = null;

    private BaseActivity mParentWnd = null;
    private IScanMainWnd mScanMainWnd = null;

    private LinearLayout mCountryLayout = null;
    private GridView country_gv = null;
    private String[] mCountries = null;
    private String[] mCountries_= null;
    private int country_len = 0;
    private int pageCou = 0;
    private int pageNum = 15;
    private int currPage = 0;
    private int mCurrCountryIndex = 0;
    private String[] mCountriesValue = null;
    private EnNetworkType mNetworkTypeSelect = EnNetworkType.NONE;

    public SetupCountryView(BaseActivity arg0)
    {
        super((LinearLayout) arg0.findViewById(R.id.ly_setup_country));
        mParentWnd = arg0;
        mScanMainWnd = (IScanMainWnd)arg0;
        mNetworkTypeSelect = mNetworkManager.getCurrentNetworkType();
    }

    private void initCountry()
    {
        String countrycode = mDtvConfig.getString(CommonValue.COUNTRY_CODE_KEY, "MYS");
        if (mNetworkTypeSelect == EnNetworkType.DTMB)
        {
            mCountries_ = mParentWnd.getResources().getStringArray(R.array.dtmb_country_code);
            mCountriesValue = mParentWnd.getResources().getStringArray(R.array.dtmb_country_code_value);
        }
        else if (mNetworkTypeSelect== EnNetworkType.CABLE)
        {
            mCountries_ = mParentWnd.getResources().getStringArray(R.array.dvbc_country_code);
            mCountriesValue = mParentWnd.getResources().getStringArray(R.array.dvbc_country_code_value);
        }
        else if (mNetworkTypeSelect== EnNetworkType.TERRESTRIAL)
        {
            mCountries_ = mParentWnd.getResources().getStringArray(R.array.dvbt_country_code);
            mCountriesValue = mParentWnd.getResources().getStringArray(R.array.dvbt_country_code_value);
        }
        else if (mNetworkTypeSelect == EnNetworkType.ISDB_TER)
        {
            mCountries_ = mParentWnd.getResources().getStringArray(R.array.isdbt_country_code);
            mCountriesValue = mParentWnd.getResources().getStringArray(R.array.isdbt_country_code_value);
        } else if (mNetworkTypeSelect == EnNetworkType.ATSC_T || mNetworkTypeSelect == EnNetworkType.ATSC_CAB) {
            mCountries_ = mParentWnd.getResources().getStringArray(R.array.atsc_country_code);
            mCountriesValue = mParentWnd.getResources().getStringArray(R.array.atsc_country_code_value);
        } else
        {
            mCountries_ = mParentWnd.getResources().getStringArray(R.array.dtmb_country_code);
            mCountriesValue = mParentWnd.getResources().getStringArray(R.array.dtmb_country_code_value);
        }

        for(int index = 0 ; index <mCountries_.length ;index++)
        {
            if(mCountriesValue[index].contains(countrycode))
            {
                mCurrCountryIndex = index;
            }
        }
        pageCou = mCountries_.length/pageNum;
        if(mCountries_.length%pageNum > 0)
        {
            pageCou += 1;
        }

        if(mCurrCountryIndex < pageNum)
        {
            currPage = 1;
        }
        else
        {
            currPage = (mCurrCountryIndex/pageNum)+1;
        }

        int startIndex = (currPage-1)*pageNum;
        if(currPage < pageCou)
        {
            country_len = currPage*pageNum;
            mCountries = new String[pageNum];
        }
        else
        {
            country_len = mCountries_.length;
            mCountries = new String[country_len-startIndex];
        }

        for(int i=startIndex;i<country_len && i<mCountries_.length;i++)
        {
            mCountries[i-startIndex] = mCountries_[i];
        }

        for(int i=0;i<mCountries.length;i++)
        {
            mLocaleCountryDisplayNames.add(mCountries[i]);
        }

        for(int i=0;i<pageNum-mCountries.length;i++)
        {
            mLocaleCountryDisplayNames.add("");
        }
    }

    private void initView()
    {
        country_gv = (GridView)mParentWnd.findViewById(R.id.country_gv);
        languageItemAdapter = new GridItemAdapter(mParentWnd,null,mLocaleCountryDisplayNames);
        country_gv.setAdapter(languageItemAdapter);

        country_gv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // TODO Auto-generated method stub
                if(position < mCountries.length)
                {
                    mDtvConfig.setString(CommonValue.COUNTRY_CODE_KEY, mCountriesValue[position]);
                    mDTV.setCountry(mCountriesValue[position]);

                    if (mCurrCountryIndex != position)
                    {
                        mDTV.getParentalControlManager().setParentLockAge(0);
                    }

                    if (null != mScanMainWnd)
                    {
                        mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_NEXT_STEP, null);
                    }
                }
            }
        });

        country_gv.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                // TODO Auto-generated method stub
                if(event.getAction() == KeyEvent.ACTION_DOWN && currPage == pageCou)
                {
                    if(keyCode == KeyValue.DTV_KEYVALUE_DPAD_RIGHT)
                    {
                        int selectIndex = country_gv.getSelectedItemPosition() + 1;
                        if(selectIndex >= mLocaleCountryDisplayNames.size())
                        {
                            return true;
                        }
                        else
                        {
                            String strVal = mLocaleCountryDisplayNames.get(selectIndex);
                            if("".equals(strVal))
                            {
                                return true;
                            }
                        }
                    }
                    else if(keyCode == KeyValue.DTV_KEYVALUE_DPAD_DOWN)
                    {
                        int selectIndex = country_gv.getSelectedItemPosition() + 5;
                        if(selectIndex >= mLocaleCountryDisplayNames.size())
                        {
                            return true;
                        }
                        else
                        {
                            String strVal = mLocaleCountryDisplayNames.get(selectIndex);
                            if("".equals(strVal))
                            {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        });
        country_gv.setSelection(mCurrCountryIndex%pageNum);

    }

    public void updatePageView(int preOrnext)
    {
        if(preOrnext == NextPage)
        {
            if(currPage == pageCou)
            {
                return;
            }

            int i = 0;
            int startIndex = currPage*pageNum;
            currPage++;
            country_len = currPage*pageNum;

            if(country_len <= mCountries_.length)
            {
                mCountries = new String[pageNum];
            }
            else
            {
                mCountries = new String[pageNum-(country_len-mCountries_.length)];
            }
            for(i = startIndex;i<country_len && i<mCountries_.length;i++)
            {
                mCountries[i-startIndex] = mCountries_[i];
            }
            mLocaleCountryDisplayNames.clear();
            for(i = 0; i < mCountries.length; i++)
            {
                mLocaleCountryDisplayNames.add(mCountries[i]);
            }

            for(i = 0; i < pageNum-mCountries.length; i++)
            {
                mLocaleCountryDisplayNames.add("");
            }

            if(country_gv.getSelectedItemPosition() == 4 )
            {
                country_gv.setSelection(0);
            }
            else if(country_gv.getSelectedItemPosition() == 9)
            {
                if(mCountries.length > 5)
                {
                    country_gv.setSelection(5);
                }
                else
                {
                    country_gv.setSelection(mCountries.length-1);
                }
            }
            else if(country_gv.getSelectedItemPosition() == 14)
            {
                if(mCountries.length > 10)
                {
                    country_gv.setSelection(10);
                }
                else
                {
                    country_gv.setSelection(mCountries.length-1);
                }
            }

            languageItemAdapter.notifyDataSetChanged();

        }
        else if(preOrnext == PrePage)
        {
            if(currPage == 1)
            {
                return;
            }

            currPage--;
            int startIndex = (currPage-1)*pageNum;
            country_len = currPage*pageNum;

            mCountries = new String[pageNum];

            for(int i=startIndex;i<country_len && i<mCountries_.length;i++)
            {
                mCountries[i-startIndex] = mCountries_[i];
            }
            mLocaleCountryDisplayNames.clear();
            for(int i=0;i<mCountries.length;i++)
            {
                mLocaleCountryDisplayNames.add(mCountries[i]);
            }

            for(int i=0;i<pageNum-mCountries.length;i++)
            {
                mLocaleCountryDisplayNames.add("");
            }

            if(country_gv.getSelectedItemPosition() == 0 )
            {
                country_gv.setSelection(4);
            }
            else if(country_gv.getSelectedItemPosition() == 5)
            {
                country_gv.setSelection(9);
            }
            else if(country_gv.getSelectedItemPosition() == 10)
            {
                country_gv.setSelection(14);
            }

            languageItemAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void show()
    {
        mLocaleCountryDisplayNames.clear();
        mCurrCountryIndex = 0;
        initCountry();
        initView();
        super.show();
    }

    public void setNetworkType(EnNetworkType networkType)
    {
        mNetworkTypeSelect = networkType;
    }

    @Override
    public void hide()
    {
        super.hide();
    }

    @Override
    public void toggle()
    {
        if (super.isShow())
        {
            hide();
        }
        else
        {
            show();
        }
    }

    @Override
    public boolean isCanStartScan()
    {
        return true;
    }

    @Override
    public boolean isNetworkScan()
    {
        return false;
    }

    @Override
    public void setMainWnd(IScanMainWnd parent)
    {
        mScanMainWnd = parent;
    }

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent)
    {
        switch(keyCode)
        {
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
                updatePageView(PrePage);
                break;
            case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
                updatePageView(NextPage);
                break;
            case KeyValue.DTV_KEYVALUE_BACK:
            case KeyValue.DTV_KEYVALUE_MENU:
                break;
            default:
                break;
        }
        return KeyDoResult.DO_DONE_NEED_SYSTEM;
    }

}
