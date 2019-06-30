
package com.hisilicon.tvui.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.hal.halApi.HalTimingInfo;
import com.hisilicon.tvui.util.Util;
import com.hisilicon.tvui.util.LogTool;

/**
 * when you change channel ,SignalShow will show. it includes source
 * name,channel number,color format,sound format,timing.
 *
 * @author wangchuanjian
 */
public class SignalShow extends LinearLayout
{
    private static final String TAG = "SignalShow";
    // text of source name
    private TextView sourceNameTxt;
    // text of channel number
    private TextView channelNumberTxt;
    // text of color format
    private TextView colorFormatTxt;
    // text of sound format
    private TextView soundFormatTxt;
    // text of timing
    private TextView timingTxt;
    // text of change line
    private TextView changelineTxt;
    /**
     * handler of set SignalShow gone
     */
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            setVisibility(View.INVISIBLE);
        }
    };

    public SignalShow(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater mLinflater = LayoutInflater.from(getContext());
        mLinflater.inflate(R.layout.singnal_show, this);
        sourceNameTxt = (TextView) findViewById(R.id.source_name_txt);
        channelNumberTxt = (TextView) findViewById(R.id.channel_number_txt);
        colorFormatTxt = (TextView) findViewById(R.id.color_format_txt);
        soundFormatTxt = (TextView) findViewById(R.id.sound_format_txt);
        timingTxt = (TextView) findViewById(R.id.timing_txt);
        changelineTxt = (TextView) findViewById(R.id.change_line_txt);
    }

    public String getCurSourceName(int sourceid)
    {
        String sourceName = "";
        switch (sourceid)
        {
            case halApi.EnumSourceIndex.SOURCE_CVBS1:
                sourceName = getResources().getString(R.string.cvbs1);
                break;
            case halApi.EnumSourceIndex.SOURCE_CVBS2:
                sourceName = getResources().getString(R.string.cvbs2);
                break;
            case halApi.EnumSourceIndex.SOURCE_YPBPR1:
                sourceName = getResources().getString(R.string.ypbpr1);
                break;
            case halApi.EnumSourceIndex.SOURCE_HDMI1:
                sourceName = getResources().getString(R.string.hdmi1);
                break;
            case halApi.EnumSourceIndex.SOURCE_HDMI2:
                sourceName = getResources().getString(R.string.hdmi2);
                break;
            case halApi.EnumSourceIndex.SOURCE_HDMI3:
                sourceName = getResources().getString(R.string.hdmi3);
                break;
            case halApi.EnumSourceIndex.SOURCE_HDMI4:
                sourceName = getResources().getString(R.string.hdmi4);
                break;
            case halApi.EnumSourceIndex.SOURCE_VGA:
                sourceName = getResources().getString(R.string.vga);
                break;
            default:
                sourceName = getResources().getString(R.string.atv);
                break;
        }
        return sourceName;

    }

    /**
     * refresh panel info by sourceId and force
     *
     * @param sourceId
     * @param force
     */
    public void refreshPanelInfo(int sourceId)
    {
        hide();
        sourceNameTxt.setVisibility(View.VISIBLE);
        sourceNameTxt.setText(getCurSourceName(sourceId));
        int signalStatus = halApi.getSignalStatus();
        LogTool.d(LogTool.MPLAY, "refreshPanelInfo() SourceID: " + sourceId + ", Signal:" + signalStatus);
        if (sourceId == halApi.EnumSourceIndex.SOURCE_CVBS1
                || sourceId == halApi.EnumSourceIndex.SOURCE_CVBS2
                || sourceId == halApi.EnumSourceIndex.SOURCE_CVBS3)
        {
            String colorStr = "";
            if (signalStatus == halApi.EnumSignalStat.SIGSTAT_SUPPORT)
            {
                int color = halApi.getColorSystem();
                switch (color)
                {
                    case halApi.EnumColorSystem.CLRSYS_AUTO:
                        colorStr = "AUTO";
                        break;
                    case halApi.EnumColorSystem.CLRSYS_PAL:
                    case halApi.EnumColorSystem.CLRSYS_PAL_60:
                        colorStr = "PAL";
                        break;
                    case halApi.EnumColorSystem.CLRSYS_NTSC:
                    case halApi.EnumColorSystem.CLRSYS_NTSC443:
                    case halApi.EnumColorSystem.CLRSYS_NTSC_50:
                        colorStr = "NTSC";
                        break;
                    case halApi.EnumColorSystem.CLRSYS_SECAM:
                        colorStr = "SECAM";
                        break;
                    case halApi.EnumColorSystem.CLRSYS_PAL_M:
                        colorStr = "PAL M";
                        break;
                    case halApi.EnumColorSystem.CLRSYS_PAL_N:
                        colorStr = "PAL N";
                        break;
                    default:
                        colorStr = "";
                        break;
                }
            }
            colorFormatTxt.setText(colorStr);
        }
        else
        {   // Non RF source of Timing information
            String timing = "";
            HalTimingInfo ti = halApi.getTimingInfo();
            int width = ti.getiWidth();
            int height = ti.getiHeight();
            int frame = ti.getiFrame();
            boolean isInterlace = ti.isbInterlace();
            LogTool.d(LogTool.MPLAY, "refreshPanelInfo_width:" + width + ", height: " + height +
                        ", frame: " + frame + ", isInterlace: " + isInterlace);
            if(signalStatus == halApi.EnumSignalStat.SIGSTAT_SUPPORT)
            {
                if (sourceId == halApi.EnumSourceIndex.SOURCE_YPBPR1
                        || sourceId == halApi.EnumSourceIndex.SOURCE_YPBPR2)
                {
                    if (isInterlace)
                    { // Interlace
                        timing = height + "i" + "/" + frame + "Hz";
                    }
                    else
                    {
                        timing = height + "p" + "/" + frame + "Hz";
                    }
                }
                else if (halApi.isHDMISource(sourceId))
                {
                    int hdmiFmt = ti.getiHDMIFmt();

                    LogTool.d(LogTool.MPLAY, "hdmiFmt:"  + hdmiFmt);
                    if (hdmiFmt == HalTimingInfo.HI_MW_HDMI_FORMAT_HDMI
                            || hdmiFmt == HalTimingInfo.HI_MW_HDMI_FORMAT_MHL) // HI_MW_HDMI_FORMAT_HDMI MHL
                    {
                        if (halApi.isGraphicsMode())
                        {
                            timing = "" + width + "x" + height + "/" + frame + "Hz";
                        }
                        else
                        {
                            if (isInterlace)
                            { // Interlaced
                                timing = "" + height + "i" + "/" + frame + "Hz";
                            }
                            else
                            {
                                timing = "" + height + "p" + "/" + frame + "Hz";
                            }
                        }
                    }
                    else if (hdmiFmt == HalTimingInfo.HI_MW_HDMI_FORMAT_DVI)
                    { // HI_MW_HDMI_FORMAT_DVI
                        timing = "" + width + "x" + height + "/" + frame + "Hz";
                    }
                }
                else if (sourceId == halApi.EnumSourceIndex.SOURCE_VGA)
                {
                    timing = "" + width + "x" + height + "/" + frame + "Hz";
                }
            }
            colorFormatTxt.setText(timing);
        }

        colorFormatTxt.setVisibility(View.VISIBLE);
        this.setVisibility(View.VISIBLE);
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 5000);
    }

    public void focusHide()
    {
        mHandler.removeMessages(0);
        this.setVisibility(View.GONE);
    }

    public void toggle(int sourceId)
    {
        if (getVisibility() == View.VISIBLE)
        {
            hide();
        }
        else
        {
            refreshPanelInfo(sourceId);
        }
    }

    public void hide()
    {
        mHandler.removeMessages(0);
        this.setVisibility(View.GONE);
        sourceNameTxt.setVisibility(View.GONE);
        channelNumberTxt.setVisibility(View.GONE);
        colorFormatTxt.setVisibility(View.GONE);
        soundFormatTxt.setVisibility(View.GONE);
        timingTxt.setVisibility(View.GONE);
        changelineTxt.setVisibility(View.GONE);
    }

    public boolean isShow()
    {
        return getVisibility() == View.VISIBLE;
    }

}
