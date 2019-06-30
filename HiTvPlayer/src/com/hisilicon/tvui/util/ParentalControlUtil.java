package com.hisilicon.tvui.util;


import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.pc.ChannelRate;
import com.hisilicon.dtv.pc.ParentalControlManager;
import com.hisilicon.dtv.pc.ParentalControlManager.EnUstvAllType;
import com.hisilicon.dtv.pc.ParentalControlManager.EnUstvFvType;
import com.hisilicon.dtv.pc.ParentalControlManager.EnUstvVType;
import com.hisilicon.dtv.pc.ParentalControlManager.EnUstvSType;
import com.hisilicon.dtv.pc.ParentalControlManager.EnUstvLType;
import com.hisilicon.dtv.pc.ParentalControlManager.EnUstvDType;
import com.hisilicon.dtv.pc.ParentalControlManager.EnMpaaType;
import com.hisilicon.dtv.pc.ParentalControlManager.EnCaenType;
import com.hisilicon.dtv.pc.ParentalControlManager.EnCafrType;
import com.hisilicon.dtv.pc.ParentalControlManager.EnRrtRegion;
import com.hisilicon.dtv.pc.RRTInfo;

import com.hisilicon.tvui.base.CommonValue;

public class ParentalControlUtil {
    private static ParentalControlUtil sInstance = null;

    private static EnRrtRegion DEFAULT_RRT_REGION = EnRrtRegion.RRT_REGION_5;

    private DTV mDTV;
    private ParentalControlManager mPCManager;

    public static synchronized ParentalControlUtil getInstance() {
        if (sInstance == null) {
            sInstance = new ParentalControlUtil();
        }
        return sInstance;
    }

    private ParentalControlUtil() {
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mPCManager = mDTV.getParentalControlManager();
    }

    /**
     * String to show channel V-CHIP rate
     * @param channel channel to show rate
     * @param enRRTRegion RRTRegion, which used to get RRT info, if null, use default RRT_REGION_5
     * @return channel rate string, use "/" to separate different rate type such as TV, MPAA etc.
     */
    public String getRateString(Channel channel, EnRrtRegion enRRTRegion) {
        if (channel == null) {
            return "TV-NONE";
        }
        StringBuilder sb = new StringBuilder();

        //handle US-MPAA, US-TV, CA-EN, CA-FR
        ChannelRate channelRate = mPCManager.getChannelParental(channel);
        if (channelRate != null) {
            sb.append(getUsMpaaRateString(channelRate))
                    .append(getUsTvRateString(channelRate))
                    .append(getCaEnRateString(channelRate))
                    .append(getCaFrRateString(channelRate));
        }

        //handle RRT
        if (enRRTRegion == null) {
            enRRTRegion = DEFAULT_RRT_REGION;
        }
        RRTInfo rRTInfo = mPCManager.getRRT(enRRTRegion);
        if (rRTInfo != null) {
            int dmsNum  = rRTInfo.GetRRTDimensionsNum();
            for (int dmsIdx = 0; dmsIdx < dmsNum; dmsIdx ++) {
                int typeNum = rRTInfo.getRRTDimensionTypeNum(dmsIdx);
                int channelTypeIdx = mPCManager.getChannelParental(channel, enRRTRegion, dmsIdx);
                if (channelTypeIdx >= 0 && channelTypeIdx < typeNum) {
                    sb.append(rRTInfo.getRRTDimensionValueName(dmsIdx, channelTypeIdx)).append(",");
                }
            }
        }

        // handle none rate
        if (sb.length() == 0) {
            return "TV-NONE";
        }

        //RRT ends with "," while others ends with "/"
        String rateString = sb.toString();
        if (rateString.endsWith("/") || rateString.endsWith(",")) {
            return rateString.substring(0, rateString.length() - 1);
        }
        return rateString;
    }

    private String getUsTvRateString(ChannelRate channelRate) {
        StringBuilder sb = new StringBuilder();
        EnUstvAllType enUstvAll = channelRate.mEnUstvAll;
        EnUstvFvType enUstvFv = channelRate.mEnUstvFv;
        EnUstvVType enUstvV = channelRate.mEnUstvV;
        EnUstvSType enUstvS = channelRate.mEnUstvS;
        EnUstvLType enUstvL = channelRate.mEnUstvL;
        EnUstvDType enUstvD = channelRate.mEnUstvD;

        // handle main rate
        // no main rate
        if (enUstvAll == EnUstvAllType.USTV_NONE) {
            return "";
        }
        sb.append(enUstvAll.toString().substring(2).replace('_', '-'));

        // handle sub rate
        sb.append("-");
        if (enUstvFv != EnUstvFvType.USTV_NONE) {
            sb.append("FV");
        }
        if (enUstvD != EnUstvDType.USTV_NONE) {
            sb.append("D");
        }
        if (enUstvL != EnUstvLType.USTV_NONE) {
            sb.append("L");
        }
        if (enUstvS != EnUstvSType.USTV_NONE) {
            sb.append("S");
        }
        if (enUstvV != EnUstvVType.USTV_NONE) {
            sb.append("V");
        }
        // no sub rate
        if (sb.toString().endsWith("-")) {
            return sb.substring(0, sb.length() - 1) + "/";
        }
        return sb.append("/").toString();
    }

    private String getUsMpaaRateString(ChannelRate channelRate) {
        EnMpaaType enMpaa = channelRate.mEnMpaa;
        if (enMpaa == EnMpaaType.MPAA_NONE) {
            return "";
        }
        //replace first, so MPAA_NC_17 shows MPAA-NC_17, as same as S3 board
        return enMpaa.toString().replaceFirst("_","-") + "/";
    }

    private String getCaEnRateString(ChannelRate channelRate) {
        EnCaenType enCaen = channelRate.mEnCaen;
        if (enCaen == EnCaenType.CAEN_NONE) {
            return "";
        }
        return enCaen.toString().replace('_', '-').replaceFirst("-PLUS", "+") + "/";
    }

    private String getCaFrRateString(ChannelRate channelRate) {
        EnCafrType enCafr = channelRate.mEnCafr;
        if (enCafr == EnCafrType.CAFR_NONE) {
            return "";
        }
        return enCafr.toString().replace('_', '-').replaceFirst("-ANS", "+") + "/";
    }
}
