package com.hisilicon.tvui.play;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.AtvChannelManager;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelFilter;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.network.service.EnServiceType;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.si.SIElement;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.LogTool;

/* 单例模式。用来记录当前节目列表，当前播放频道等  */
public class ChannelHistory {
    private static final String TAG = "ChannelHistory";

    private final DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
    private final ChannelManager mChannelManager = mDTV.getChannelManager();
    private final AtvChannelManager mAtvChannelManager = mDTV.getAtvChannelManager();

    private boolean mIsRecording = false;

    private ChannelList mCurDtvList = null;
    private Channel mCurDtvChn = null;
    private ChannelList mPreDtvList = null;
    private Channel mPreDtvChn = null;

    private ChannelList mPreAtvList = null;
    private Channel mPreAtvChn = null;
    private ChannelList mCurAtvList = null;
    private Channel mCurAtvChn = null;

    private Channel mPreTVChn = null;
    private Channel mPreRADIOChn = null;
    private Channel mPreDATAChn = null;

    private ChannelList mPreTVList = null;
    private ChannelList mPreRADIOList = null;
    private ChannelList mPreDATAList = null;

    private ChannelHistory() {
        // DTV list
        int grouptype = mChannelManager.getDefaultOpenGroupType();
        mCurDtvList = mChannelManager.getChannelListByGroupType(grouptype);
        if (null == mCurDtvList) {
            ChannelFilter allFilter = new ChannelFilter();
            EnTVRadioFilter currentFilter = mChannelManager.getChannelServiceTypeMode();
            allFilter.setGroupType(currentFilter);
            mCurDtvList = mChannelManager.getChannelList(allFilter);
        }
        mCurDtvChn = mChannelManager.getDefaultOpenChannel();

        if (null == mCurDtvChn) {
            mCurDtvChn = mCurDtvList.getChannelByIndex(0);
        }

        // ATV list
        mCurAtvList = mAtvChannelManager.getAllChannelList();
        mCurAtvChn = mAtvChannelManager.getDefaultOpenChannel();

    }

    private static final ChannelHistory single = new ChannelHistory();

    public static ChannelHistory getInstance() {
        return single;
    }

    public void setCurrent(int SourceID, ChannelList channelList, Channel channel) {
        if ((null == channelList) && (null == channel)) {
            if (halApi.EnumSourceIndex.SOURCE_ATV == SourceID) {
                mCurAtvList = null;
                mCurAtvChn = null;
            } else {
                mCurDtvList = null;
                mCurDtvChn = null;
            }
            return;
        }
        if (halApi.EnumSourceIndex.SOURCE_ATV == SourceID) {
            /* 相同节目，重复进行设置，不生效。以防止回看上一个节目出错问题。  */
            if (null != channel && null != mCurAtvChn && channel.getChannelID() == mCurAtvChn.getChannelID()) {
                /* 节目相同，列表不同，则更新列表。使切表列没切节目的场景生效  */
                if (null != channelList) {
                    mCurAtvList = channelList;
                }

                return;
            }

            mPreAtvList = mCurAtvList;
            mPreAtvChn = mCurAtvChn;
            mCurAtvList = channelList;
            mCurAtvChn = channel;
        } else {
            /* 节目radio或TV属性发生变化，则更新setChannelServiceTypeMode，并记录 */
            EnServiceType serviceType = null;
            if (null != channel) {
                serviceType = channel.getServiceType();
            }
            if(null == serviceType){
                LogTool.d(LogTool.MPLAY, " ServiceType is null");
                return;
            }
            EnTVRadioFilter curServiceType = mChannelManager.getChannelServiceTypeMode();

            if (EnServiceType.getRadioServiceTypes().contains(serviceType) && curServiceType == EnTVRadioFilter.TV) {
                LogTool.d(LogTool.MPLAY, "Set ServiceType to RADIO");
                mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.TV);
            } else if (EnServiceType.getTVServiceTypes().contains(serviceType) && curServiceType == EnTVRadioFilter.DATA) {
                LogTool.d(LogTool.MPLAY, "Set ServiceType to TV");
                mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.DATA);
            } else if (EnServiceType.getDATAServiceTypes().contains(serviceType) && curServiceType == EnTVRadioFilter.RADIO) {
                LogTool.d(LogTool.MPLAY, "Set ServiceType to DATA");
                mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.RADIO);
            }

            /* 相同节目，重复进行设置，不生效。以防止回看上一个节目出错问题。  */
            if (null != mCurDtvChn && channel.getChannelID() == mCurDtvChn.getChannelID()) {
                /* 节目相同，列表不同，则更新列表。使切表列没切节目的场景生效  */
                if (null != channelList) {
                    mCurDtvList = channelList;
                }
                LogTool.d(LogTool.MPLAY, "  the same channel");
                return;
            }

            /* 传入参数节目列表为空，则将列表置为All分组  */
            if (null == channelList) {
                List<ChannelList> tmpUseGroup = mChannelManager.getUseGroups();
                if (null != tmpUseGroup && 0 != tmpUseGroup.size()) {
                    channelList = tmpUseGroup.get(0);
                    filterHiddedChannel(channelList);
                }
            }

            if (null != mCurDtvChn) {
                if ((EnServiceType.getRadioServiceTypes().contains(mCurDtvChn.getServiceType())) && !EnServiceType.getRadioServiceTypes()
                        .contains(channel.getServiceType())) {
                    mPreRADIOChn = mCurDtvChn;
                    mPreRADIOList = mCurDtvList;
                } else if ((EnServiceType.getTVServiceTypes().contains(mCurDtvChn.getServiceType())) && !EnServiceType.getTVServiceTypes()
                        .contains(channel.getServiceType())) {
                    mPreTVChn = mCurDtvChn;
                    mPreTVList = mCurDtvList;
                } else if ((EnServiceType.getDATAServiceTypes().contains(mCurDtvChn.getServiceType())) && !EnServiceType.getDATAServiceTypes()
                        .contains(channel.getServiceType())) {
                    mPreDATAChn = mCurDtvChn;
                    mPreDATAList = mCurDtvList;
                }
            }

            mPreDtvList = mCurDtvList;
            mPreDtvChn = mCurDtvChn;
            mCurDtvList = channelList;
            mCurDtvChn = channel;
        }

    }

    public ChannelList getPreList(int SourceID) {
        if (halApi.EnumSourceIndex.SOURCE_ATV == SourceID) {
            if (null == mPreAtvList) {
                mPreAtvList = mCurAtvList;
            }
            return mPreAtvList;
        } else {
            if (null == mPreDtvList) {
                mPreDtvList = mCurDtvList;
            }
            return mPreDtvList;
        }
    }

    public Channel getPreChn(int SourceID) {
        if (halApi.EnumSourceIndex.SOURCE_ATV == SourceID) {
            /* PreChn有效性检测，可能该节目已被删除  */
            if (null != mPreAtvChn) {
                mPreAtvChn = mAtvChannelManager.getChannelByID(mPreAtvChn.getChannelID());
            }

            if (null == mPreAtvChn) {
                mPreAtvChn = mCurAtvChn;
                mPreAtvList = mCurAtvList;
            }
            return mPreAtvChn;
        } else {
            /* PreChn有效性检测，可能该节目已被删除  */
            if (null != mPreDtvChn) {
                mPreDtvChn = mChannelManager.getChannelByID(mPreDtvChn.getChannelID());
            }

            if (null == mPreDtvChn) {
                mPreDtvChn = mCurDtvChn;
                mPreDtvList = mCurDtvList;
            }
            return mPreDtvChn;
        }
    }

    public ChannelList getCurrentList(int sourceID) {
        if (sourceID == halApi.EnumSourceIndex.SOURCE_ATV) {
            if (null == mCurAtvList) {
                mCurAtvList = mAtvChannelManager.getAllChannelList();
            }
            return mCurAtvList;
        } else {
            int pos = -1;
            /* 当前节目不在当前分组，则置为all分组  */
            if (null != mCurDtvList && null != mCurDtvChn) {
                pos = mCurDtvList.getPosByChannelID(mCurDtvChn.getChannelID());
                if (pos < 0) {
                    List<ChannelList> tmpUseGroup = mChannelManager.getUseGroups();
                    if (null != tmpUseGroup && 0 != tmpUseGroup.size()) {
                        mCurDtvList = tmpUseGroup.get(0);
                        filterHiddedChannel(mCurDtvList);
                    }
                }
            }
            return mCurDtvList;
        }
    }

    /* 获取当前播放的频道。该函数会对频道有效性进行检测。如果保存的频道已经被删除，会认为频道无效。当频道无效时，会试图获取一个默认频道作为返回值   */
    public Channel getCurrentChn(int SourceID) {
        if (SourceID == halApi.EnumSourceIndex.SOURCE_ATV) {
            /* 频道有效性检测  */
            if (null != mCurAtvChn) {
                mCurAtvChn = mAtvChannelManager.getChannelByID(mCurAtvChn.getChannelID());
            }

            if (null == mCurAtvChn) {
                mCurAtvChn = mAtvChannelManager.getDefaultOpenChannel();
            }
            return mCurAtvChn;
        } else {
            /* 频道有效性检测  */
            if (null != mCurDtvChn) {
                mCurDtvChn = mChannelManager.getChannelByID(mCurDtvChn.getChannelID());
            }

            /* 频道无效，获取默认频道  */
            if (null == mCurDtvChn) {
                int grouptype = mChannelManager.getDefaultOpenGroupType();
                mChannelManager.rebuildAllGroup();
                mCurDtvList = mChannelManager.getChannelListByGroupType(grouptype);
                if (null != mCurDtvList) {
                    filterHiddedChannel(mCurDtvList);
                    mCurDtvChn = mCurDtvList.getChannelByIndex(0);
                }
            }
        }

        return mCurDtvChn;
    }

    /* 获取当前播放的频道。该函数会对频道有效性进行检测。如果保存的频道已经被删除，会认为频道无效。当频道无效时，会返回空！   */
    public Channel getLastChn(int SourceID) {
        if (SourceID == halApi.EnumSourceIndex.SOURCE_ATV) {
            /* 频道有效性检测  */
            if (null != mCurAtvChn) {
                mCurAtvChn = mAtvChannelManager.getChannelByID(mCurAtvChn.getChannelID());
            }
            return mCurAtvChn;
        } else {
            /* 频道有效性检测  */
            if (null != mCurDtvChn) {
                mCurDtvChn = mChannelManager.getChannelByID(mCurDtvChn.getChannelID());
            }
            return mCurDtvChn;
        }
    }

    public ChannelList getPreTvRadioList(EnTVRadioFilter tvradioFilter) {
        if (EnTVRadioFilter.TV == tvradioFilter) {
            return mPreTVList;
        } else if (EnTVRadioFilter.RADIO == tvradioFilter) {
            return mPreRADIOList;
        } else if (EnTVRadioFilter.DATA == tvradioFilter) {
            return mPreDATAList;
        }
        return null;
    }

    public Channel getPreTvRadioChn(EnTVRadioFilter tvradioFilter) {
        /* 频道有效性检测  */
        if (EnTVRadioFilter.TV == tvradioFilter) {
            if (null != mPreTVChn) {
                return mChannelManager.getChannelByID(mPreTVChn.getChannelID());
            }
        } else if (EnTVRadioFilter.RADIO == tvradioFilter) {
            if (null != mPreRADIOChn) {
                return mChannelManager.getChannelByID(mPreRADIOChn.getChannelID());
            }
        } else if (EnTVRadioFilter.DATA == tvradioFilter) {
            if (null != mPreDATAChn) {
                return mChannelManager.getChannelByID(mPreDATAChn.getChannelID());
            }
        }
        return null;
    }

    public void setIsRecording(boolean bIsRecording) {
        mIsRecording = bIsRecording;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void filterHiddedChannel(ChannelList channelList) {
        if (null != channelList) {
            ChannelFilter channelFilter = channelList.getFilter();
            if (channelFilter != null) {
                EnTagType mSkipTagType = EnTagType.HIDE;
                List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
                mEditTypes.add(mSkipTagType);
                channelFilter.setTagType(mEditTypes);
                channelList.setFilter(channelFilter);
            }
        }
    }

    public void resetDtvChannel() {
        mChannelManager.rebuildAllGroup();
        List<ChannelList> tmpUseGroup = mChannelManager.getUseGroups();
        if (null != tmpUseGroup && 0 != tmpUseGroup.size()) {
            mCurDtvList = tmpUseGroup.get(0);
        }
        if (mCurDtvList == null) {
            int grouptype = mChannelManager.getDefaultOpenGroupType();
            mCurDtvList = mChannelManager.getChannelListByGroupType(grouptype);
        }
        ChannelFilter channelFilter = mCurDtvList.getFilter();
        if (channelFilter != null) {
            EnTagType mSkipTagType = EnTagType.HIDE;
            List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
            mEditTypes.add(mSkipTagType);
            channelFilter.setTagType(mEditTypes);

            EnTVRadioFilter radioFilter = mChannelManager.getChannelServiceTypeMode();
            channelFilter.setGroupType(radioFilter);
            SIElement siElement = new SIElement() {
                public EnNetworkType getNetworkType() {
                    return EnNetworkType.NONE;
                }
            };
            channelFilter.setSIElement(siElement);
            mCurDtvList.setFilter(channelFilter);
        }

        mCurDtvChn = mChannelManager.getDefaultOpenChannel();
        if (null == mCurDtvChn) {
            mCurDtvChn = mCurDtvList.getChannelByIndex(0);
        }

        mPreTVList = null;
        mPreRADIOList = null;
        mPreDATAList = null;
    }
}
