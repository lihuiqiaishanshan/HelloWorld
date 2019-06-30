package com.hisilicon.android.videoplayer.utils;


public class HiMediaPlayer {
    /**
     * <b>error of invalid operation</b><br>
     */
    public static final int MEDIA_ERROR_INVALID_OPERATION = -38;
    /**
     * Unspecified media player error.no extra parameter.
     */
    public static final int MEDIA_ERROR_UNKNOWN = 1;

    /**
     * <b>Currently not implemented</b>
     * Media server died. In this case, the application must release the
     * HiMediaPlayer object and instantiate a new one.
     */
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    /**
     * <b>Currently not implemented</b><br>
     * The video is streamed and its container is not valid for progressive
     * playback i.e the video's index (e.g moov atom) is not at the start of the
     * file.
     */
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    /**
     * <b>Currently not implemented</b><br>
     * Unspecified media player info.
     */
    public static final int MEDIA_INFO_UNKNOWN = 1;

    /**
     * <b>Currently not implemented</b><br>
     * The video is too complex for the decoder: it can't decode frames fast
     * enough. Possibly only the audio plays fine at this stage.
     */
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;

    /**
     * <b>Currently not implemented</b><br>
     * Android defined buffer event<br>
     * HiMediaPlayer is temporarily pausing playback internally in order to<br>
     * buffer more data.<br>
     */
    public static final int MEDIA_INFO_BUFFERING_START = 701;

    /**
     * <b>Currently not implemented</b><br>
     * Android defined buffer event
     * HiMediaPlayer is resuming playback after filling buffers.
     */
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    /**
     * <b>Currently not implemented</b>
     */
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    /**
     * Prepare progress message.
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}
     * extra data is progress(0--100).
     */
    public static final int MEDIA_INFO_PREPARE_PROGRESS = 710;

    /**
     * <b>Currently not implemented</b><br>
     * Bad interleaving means that a media has been improperly interleaved or
     * not interleaved at all, e.g has all the video samples first then all the
     * audio ones. Video is playing but a lot of disk seeks may be happening.
     */
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;

    /**
     * <b>Currently not implemented</b>
     * The media cannot be seeked (e.g live stream)
     */
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;

    /**
     * <b>Currently not implemented</b><br>
     * A new set of metadata is available.
     */
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;

    //Add Hisi Media Info
    /**
     * Audio play fail    e.g. Fail to start audio decoder
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--none
     */
    public static final int MEDIA_INFO_AUDIO_FAIL = 1000;
    /**
     * Video play fail    e.g. Fail to start video decoder
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--none
     */
    public static final int MEDIA_INFO_VIDEO_FAIL = 1001;
    /**
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra---{@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
     */
    public static final int MEDIA_INFO_NETWORK = 1002;
    /**
     * <b>Currently not implemented</b>
     */
    public static final int MEDIA_INFO_TIMEOUT = 1003;
    /**
     * <b>Currently not implemented</b>
     */
    public static final int MEDIA_INFO_NOT_SUPPORT = 1004;
    /**
     * For network stream playback.Local cache buffer is empty<br>
     * For details,refer to {@see #setBufferSizeConfig} and
     * {@see #setBufferTimeConfig}<br>
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--none
     */
    public static final int MEDIA_INFO_BUFFER_EMPTY = 1005;
    /**
     * For network stream playback.Begin cache local data<br>
     * For details,refer to {@see #setBufferSizeConfig} and
     * {@see #setBufferTimeConfig}<br>
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--none
     */
    public static final int MEDIA_INFO_BUFFER_START = 1006;
    /**
     * For network stream playback.The local cache data is enough to
     * continue playing.<br>
     * For details,refer to {@see #setBufferSizeConfig} and
     * {@see #setBufferTimeConfig}<br>
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--none
     */
    public static final int MEDIA_INFO_BUFFER_ENOUGH = 1007;
    /**
     * @deprecated<br> For network stream playback.The local cache data is full,do
     * not download again.<br>
     * For details,refer to {@see #setBufferSizeConfig} and
     * {@see #setBufferTimeConfig}<br>
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--none
     */
    public static final int MEDIA_INFO_BUFFER_FULL = 1008;
    /**
     * For network stream playback.Finish downloading file
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--none
     */
    public static final int MEDIA_INFO_BUFFER_DOWNLOAD_FIN = 1009;
    /**
     * The decoded time(ms) of first frame from  operation<br>
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--time(ms)
     */
    public static final int MEDIA_INFO_FIRST_FRAME_TIME = 1010;
    /**
     * For update file info
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--none
     */
    public static final int MEDIA_INFO_UPDATE_FILE_INFO = 1014;
    /**
     * For fast backword complete
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * extra--none
     */
    public static final int MEDIA_INFO_FAST_BACKWORD_COMPLETE = 1015;
    /**
     * the video featrue of bitstream not support.
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * it is extra value ,what--MEDIA_INFO_UNKNOWN
     */
    public static final int MEDIA_INFO_VIDEO_CODEC_UNSUPPORT = -8000;
    /**
     * the audio featrue of bitstream not support.
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * it is extra value ,what--MEDIA_INFO_UNKNOWN
     */
    public static final int MEDIA_INFO_AUDIO_CODEC_UNSUPPORT = -8001;

    /**
     * unknown network error
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * it is extra data of {@link #MEDIA_INFO_NETWORK}.
     * {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
     */
    public static final int MEDIA_INFO_NETWORK_ERROR_UNKNOW = 0;
    /**
     * connection error
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * it is extra data of {@link #MEDIA_INFO_NETWORK}.
     * {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
     */
    public static final int MEDIA_INFO_NETWORK_ERROR_CONNECT_FAILED = 1;
    /**
     * operation timeout
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * it is extra data of {@link #MEDIA_INFO_NETWORK}.
     * {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
     */
    public static final int MEDIA_INFO_NETWORK_ERROR_TIMEOUT = 2;
    /**
     * network disconnect
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * it is extra data of {@link #MEDIA_INFO_NETWORK}.
     * {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
     */
    public static final int MEDIA_INFO_NETWORK_ERROR_DISCONNECT = 3;
    /**
     * file not found
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * it is extra data of {@link #MEDIA_INFO_NETWORK}.
     * {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
     */
    public static final int MEDIA_INFO_NETWORK_ERROR_NOT_FOUND = 4;
    /**
     * status of network is normal
     * this message is reported by {@see com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener}<br>
     * it is extra data of {@link #MEDIA_INFO_NETWORK}.
     * {@see HiMediaPlayerDefine.DEFINE_NETWORK_EVENT}
     */
    public static final int MEDIA_INFO_NETWORK_NORMAL = 5;

}
