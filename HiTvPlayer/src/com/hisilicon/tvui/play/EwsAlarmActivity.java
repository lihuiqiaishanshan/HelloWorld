package com.hisilicon.tvui.play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.Util;


public class EwsAlarmActivity extends BaseActivity
{
    public static final int LOCATION_TYPE_AWAS = 1;
    public static final int LOCATION_TYPE_SIAGA = 2;
    public static final int LOCATION_TYPE_WASPADA = 3;

    public static final int TIPMSG_PROGRAM_LOCK_TIP = 1;
    public static final int TIPMSG_PARENTAL_RATING = 2;

    private String mLocationTypeDesc = null;
    private String mLocationCodeDesc = null;
    private String mDisasterTypeDesc = null;
    private String mDisasterPositionDesc = null;
    private String mDisasterDateDesc = null;
    private String mDisasterCharacteristicDesc = null;
    private String mDisasterMessageDesc = null;

    private int mDisasterCode = 0;
    private int mAuthorityCode = 0;
    private int mLocationType = 0;
    private AudioManager mAudioManager = null;
    private int currentVolume = 0;
    private int DEF_VOLUME = 50;
    IDTVListener mDTVListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            LogTool.d(LogTool.MPLAY, "IDTVListener.notifyMessage(" + messageID + "," + param1 + "," + parm2 + "," + obj.toString() + ")");
            switch (messageID)
            {
            case DTVMessage.HI_SVR_EVT_EWS_STOP:
            {
                finish();
                break;
            }
            default:
                break;

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        LogTool.i(LogTool.MPLAY, "===== onCreate =====");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ews_alarm);
        mAudioManager = (AudioManager)this.getSystemService(AUDIO_SERVICE);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        initEwsData();
        initView();
    }

    /**
     * update EWS date.<br>
     */
    private void initEwsData()
    {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mDisasterCode = bundle.getInt(CommonValue.DTV_EWS_DISASTER_CODE);
        mAuthorityCode = bundle.getInt(CommonValue.DTV_EWS_AUTHORITY_CODE);
        mLocationType = bundle.getInt(CommonValue.DTV_EWS_LOCATION_CODE);
        mLocationCodeDesc = bundle.getString(CommonValue.DTV_EWS_LOCATION_DESC);
        mDisasterTypeDesc = bundle.getString(CommonValue.DTV_EWS_DISASTER_DESC);
        mDisasterPositionDesc = bundle.getString(CommonValue.DTV_EWS_POSITION_DESC);
        mDisasterDateDesc = bundle.getString(CommonValue.DTV_EWS_DATE_DESC);
        mDisasterCharacteristicDesc = bundle.getString(CommonValue.DTV_EWS_CHARACTER_DESC);
        mDisasterMessageDesc = bundle.getString(CommonValue.DTV_EWS_MESSAGE_DESC);

        LogTool.d(LogTool.MPLAY, "mDisasterCode= " + mDisasterCode + " mLocationCodeDesc= " + mLocationCodeDesc + " mDisasterTypeDesc= " + mDisasterTypeDesc
                + " mDisasterPositionDesc= " + mDisasterPositionDesc + " mDisasterDateDesc= " + mDisasterDateDesc + " mDisasterCharacteristicDesc= "
                + mDisasterCharacteristicDesc);
        LogTool.d(LogTool.MPLAY, "mDisasterMessageDesc= " + mDisasterMessageDesc);

        switch (mLocationType)
        {
        case LOCATION_TYPE_AWAS:
            mLocationTypeDesc = "STATUS AWAS";
            mDisasterMessageDesc += ", Daerah Anda: Status AWAS";
            break;
        case LOCATION_TYPE_SIAGA:
            mLocationTypeDesc = "STATUS SIAGA";
            mDisasterMessageDesc += ", Daerah Anda: Status SIAGA";
            break;
        case LOCATION_TYPE_WASPADA:
            mLocationTypeDesc = "STATUS WASPADA";
            mDisasterMessageDesc += ", Daerah Anda: Status WASPADA";
            break;
        default:
            break;
        }

//        mLocationTypeDesc = "STATUS AWAS";
//        mLocationCodeDesc = "CILACAP";
//        mDisasterTypeDesc = "Gampa";
//        mDisasterPositionDesc = "20/09/2011 Jan 23:21 wib";
//        mDisasterDateDesc = "108LS 258T";
//        mDisasterCharacteristicDesc = "Kelustan 5 Facher hedalarnmn 50m Kelustan 5 Facher hedalarnmn 50m Kelustan 5 Facher hedalarnmn 50m Kelustan 5 Facher hedalarnmn 50m";
//        mDisasterMessageDesc = "Berpotensi Tsunami, harap berjaga-jaga, Daerah Anda CILACAP Gampa 20/09/2011 Jan 23:21 wib Kelustan 5 Facher hedalarnmn 50m Kelustan 5 Facher hedalarnmn 50m\nstatus Awas";
    }

    private void initView()
    {
        RelativeLayout mEwsAwasInfoLayout = (RelativeLayout) findViewById(R.id.lay_ews_info_awas);
        RelativeLayout mEwsWspadaInfoLayout = (RelativeLayout) findViewById(R.id.lay_ews_info_waspada);

        TextView mDisasterMessageTextView = null;
        TextView mLocationCodeTextView = null;
        TextView mLocationTypeTextView = null;
        ImageView mAuthortyImage = null;
        ImageView mDisasterCodeImage = null;
        if (LOCATION_TYPE_AWAS == mLocationType || LOCATION_TYPE_SIAGA == mLocationType)
        {
            getWindow().addPrivateFlags(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                    | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY
                    | Util.HI_PRIVATE_FLAG_INTERCEPT_FUNC_KEY);
            mLocationTypeTextView = (TextView) findViewById(R.id.ews_info_location_type);
            mLocationCodeTextView = (TextView) findViewById(R.id.ews_info_location_code);
            TextView mDisasterTypeTextView = (TextView) findViewById(R.id.ews_info_disaster_type);
            TextView mDisasterPositionTextView = (TextView) findViewById(R.id.ews_info_disaster_position);
            TextView mDisasterDateTextView = (TextView) findViewById(R.id.ews_info_disaster_date);
            TextView mDisasterCharacteristicTextView = (TextView) findViewById(R.id.ews_info_disaster_characteristic);
            mDisasterMessageTextView = (TextView) findViewById(R.id.ews_info_disaster_message);
            mDisasterCodeImage = (ImageView) findViewById(R.id.ews_info_disaster_code);
            mAuthortyImage = (ImageView) findViewById(R.id.ews_info_authorty);

            mLocationTypeTextView.setText(mLocationTypeDesc);
            mLocationCodeTextView.setText(mLocationCodeDesc);
            mDisasterTypeTextView.setText(mDisasterTypeDesc);
            mDisasterPositionTextView.setText(mDisasterPositionDesc);
            mDisasterDateTextView.setText(mDisasterDateDesc);
            mDisasterCharacteristicTextView.setText(mDisasterCharacteristicDesc);
            mDisasterMessageTextView.setText(mDisasterMessageDesc);

            mEwsAwasInfoLayout.setVisibility(View.VISIBLE);
            mEwsWspadaInfoLayout.setVisibility(View.GONE);

            if (LOCATION_TYPE_SIAGA == mLocationType)
            {
                mEwsAwasInfoLayout.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dimen_1600px),
                        (int) getResources().getDimension(R.dimen.dimen_900px)));
                mLocationTypeTextView.setTextColor(getResources().getColor(R.color.orange));
            }
        }
        else
        {
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.privateFlags &= ~(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                    | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY
                    | Util.HI_PRIVATE_FLAG_INTERCEPT_FUNC_KEY);
            window.addPrivateFlags(lp.privateFlags);
            mLocationTypeTextView = (TextView) findViewById(R.id.ews_waspada_location_type);
            mLocationCodeTextView = (TextView) findViewById(R.id.ews_waspada_location_code);
            mDisasterCodeImage = (ImageView) findViewById(R.id.ews_waspada_disaster_code);
            mAuthortyImage = (ImageView) findViewById(R.id.ews_waspada_authorty);
            mDisasterMessageTextView = (TextView) findViewById(R.id.ews_waspada_disaster_message);

            mLocationTypeDesc += ": ";
            mLocationTypeTextView.setText(mLocationTypeDesc);
            mLocationCodeTextView.setText(mLocationCodeDesc);
            String  message = mDisasterTypeDesc + ", ";
            message += (mDisasterDateDesc + ", ");
            message += (mDisasterPositionDesc + ", ");
            message += (mDisasterCharacteristicDesc + ", ");
            message += (mDisasterMessageDesc);
            mDisasterMessageTextView.setText(message);

            mEwsAwasInfoLayout.setVisibility(View.GONE);
            mEwsWspadaInfoLayout.setVisibility(View.VISIBLE);
        }

        switch (mDisasterCode)
        {
        case 0x01:
            mDisasterCodeImage.setImageResource(R.drawable.ews_01_earthquake);
            break;
        case 0x02:
            mDisasterCodeImage.setImageResource(R.drawable.ews_02_tsunami);
            break;
        case 0x03:
            mDisasterCodeImage.setImageResource(R.drawable.ews_03_volcanic_eruptions);
            break;
        case 0x04:
            mDisasterCodeImage.setImageResource(R.drawable.ews_04_soil_movement);
            break;
        case 0x05:
            mDisasterCodeImage.setImageResource(R.drawable.ews_05_flooding);
            break;
        case 0x06:
            mDisasterCodeImage.setImageResource(R.drawable.ews_06_drought);
            break;
        case 0x07:
            mDisasterCodeImage.setImageResource(R.drawable.ews_07_land_and_forest_fire);
            break;
        case 0x08:
            mDisasterCodeImage.setImageResource(R.drawable.ews_08_erosion);
            break;
        case 0x09:
            mDisasterCodeImage.setImageResource(R.drawable.ews_09_fire_building_and_housing);
            break;
        case 0x0A:
            mDisasterCodeImage.setImageResource(R.drawable.ews_0a_extreme_waves_and_abrasion);
            break;
        case 0x0B:
            mDisasterCodeImage.setImageResource(R.drawable.ews_0b_extreme_weather);
            break;
        case 0x0C:
            mDisasterCodeImage.setImageResource(R.drawable.ews_0c_failure_technology);
            break;
        case 0x0D:
            mDisasterCodeImage.setImageResource(R.drawable.ews_0d_epidemics_and_disease_outbreaks);
            break;
        case 0x0E:
            mDisasterCodeImage.setImageResource(R.drawable.ews_0e_social_conflict);
            break;
        case 0xFF:
        default:
            mDisasterCodeImage.setImageResource(R.drawable.ews_ff_reserve);
            break;
        }

        switch (mAuthorityCode)
        {
        case 0x01:
            mAuthortyImage.setImageResource(R.drawable.ews_authority01_bmkg);
            break;
        case 0x02:
            mAuthortyImage.setImageResource(R.drawable.ews_authority02_bnpb);
            break;
        default:
            break;
        }

    }

    @Override
    public void onResume() {
        LogTool.i(LogTool.MPLAY, "===== onResume =====");
        super.onResume();
        LogTool.d(LogTool.MPLAY, "mLocationType" + mLocationType + "");

        if (LOCATION_TYPE_AWAS == mLocationType) {
            if (currentVolume == 0) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, DEF_VOLUME, 0);
            } else {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
            }

            mPlayer.ewsActionControl(true);
        } else if (LOCATION_TYPE_SIAGA == mLocationType) {
            if (currentVolume == 0) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, DEF_VOLUME, 0);
            } else {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
            }
            mPlayer.ewsActionControl(true);
        }

        subScribeEvent();
    }

    @Override
    public void onPause()
    {
        LogTool.i(LogTool.MPLAY, "===== onPause =====");
        unSubSribeEvent();
        super.onPause();
        mPlayer.ewsActionControl(false);
    }

    private void subScribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EWS_STOP, mDTVListener, 0);
        }
    }

    private void unSubSribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EWS_STOP, mDTVListener);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        LogTool.d(LogTool.MPLAY, "keyCode = " + keyCode);

        if (LOCATION_TYPE_AWAS == mLocationType || LOCATION_TYPE_SIAGA == mLocationType)
        {
            return true;
        }
        else
        {
            finish();
        }
        return true;
    }
}
