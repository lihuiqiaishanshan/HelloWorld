package com.hisilicon.android.videoplayer.view;

import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Build;

import com.hisilicon.android.videoplayer.utils.LogTool;

import java.util.ArrayList;
import java.util.List;

public interface TrackHandler {
    TrackHandler HANDLER = new TrackHandler() {
        @Override
        public int getCurrTrackId(MediaPlayer mediaPlayer, int trackType) {
            if (mediaPlayer == null || trackType < 0 || trackType > 5) {
                return -1;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int track = mediaPlayer.getSelectedTrack(trackType);
                int index = -1;
                MediaPlayer.TrackInfo[] trackInfos = mediaPlayer.getTrackInfo();
                LogTool.d(TrackHandler.class.getSimpleName(), " track :" + track + "length : " +
                        (trackInfos != null ? trackInfos.length : -1));
                if (trackInfos == null || trackInfos.length <= track) {
                    return -1;
                }
                for (int i = 0; i < trackInfos.length; i++) {
                    if (trackType == trackInfos[i].getTrackType()) {
                        index++;
                    }
                    if (i == track) {
                        return index;
                    }
                }
                return index;
            }

            return -1;
        }

        @Override
        public String getCurrTrackName(MediaPlayer mediaPlayer, int trackType) {
            if (mediaPlayer == null || trackType < 0 || trackType > 5) {
                return null;
            }
            MediaPlayer.TrackInfo[] trackInfos = mediaPlayer.getTrackInfo();
            if (trackInfos == null) {
                return null;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int track = mediaPlayer.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE);
                if (trackInfos == null || trackInfos.length <= track) {
                    return null;
                }
                int index = 0;
                for (int i = 0; i < trackInfos.length; i++) {
                    if (trackType == trackInfos[i].getTrackType()) {
                        index++;
                        if (i == track) {
                            MediaPlayer.TrackInfo info = trackInfos[i];
                            String subtitle = (info.getFormat() == null ? null :
                                    info.getFormat().getString(MediaFormat.KEY_MIME)) + "\t" + info.getLanguage();
                            return index + "." + subtitle;
                        }
                    }
                }
                return null;
            }

            return null;
        }

        @Override
        public List<String> getTracks(MediaPlayer mediaPlayer, int trackType) {
            List<String> tracks = new ArrayList<String>();
            if (mediaPlayer == null || trackType < 0 || trackType > 5) {
                return tracks;
            }
            MediaPlayer.TrackInfo[] trackInfos = mediaPlayer.getTrackInfo();
            if (trackInfos == null) {
                return tracks;
            }

            for (MediaPlayer.TrackInfo info : trackInfos) {
                if (info.getTrackType() == trackType) {
                    String track = "";
                    if (trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                        track = (info.getFormat() == null ? "AUDIO" :
                                info.getFormat().getString(MediaFormat.KEY_MIME)) + "-" + info.getLanguage();
                    } else if (trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
                        track = (info.getFormat() == null ? "SUBTITLE" :
                                info.getFormat().getString(MediaFormat.KEY_MIME)) + "-" + info.getLanguage();
                    } else {
                        track = (info.getFormat() == null ? "" :
                                info.getFormat().getString(MediaFormat.KEY_MIME)) + "-" + info.getLanguage();
                    }
                    tracks.add(track);
                    LogTool.d(TrackHandler.class.getSimpleName(), "Track :　" + track + info.toString());
                }
            }
            return tracks;
        }

        @Override
        public void selectOrDeselectTrack(MediaPlayer mediaPlayer, boolean select, int trackType, int trackId) {
            if (mediaPlayer == null || trackType < 0 || trackType > 5) {
                return;
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                MediaPlayer.TrackInfo[] trackInfo = mediaPlayer.getTrackInfo();
                int index = -1;
                for (int i = 0; i < trackInfo.length; i++) {
                    if (trackInfo[i].getTrackType() == trackType) {
                        index++;
                    }
                    if (index == trackId) {
                        if (select) {
                            mediaPlayer.selectTrack(i);
                        } else {
                            mediaPlayer.deselectTrack(i);
                        }
                    }
                }
            }
        }

        @Override
        public int selectTrack(MediaPlayer mediaPlayer, int index, int trackType) {
            if (mediaPlayer == null || trackType < 0 || trackType > 5) {
                return -1;
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                MediaPlayer.TrackInfo[] trackInfos = mediaPlayer.getTrackInfo();
                int _id = -1;
                for (int i = 0; i < trackInfos.length; i++) {
                    if (trackType == trackInfos[i].getTrackType()) {
                        _id++;
                    }
                    if (_id == index) {
                        mediaPlayer.selectTrack(i);
                        return 0;
                    }
                }
            }
            return -1;
        }
    };

    /**
     * Current track id
     * <p>Currently ,the track type </p>
     *
     * @param mediaPlayer
     * @param trackType   track type
     * @return
     * @see MediaPlayer.TrackInfo
     */
    int getCurrTrackId(MediaPlayer mediaPlayer, int trackType);

    /**
     * Current track info
     * <p>Currently ,the track name </p>
     *
     * @param mediaPlayer
     * @param trackType   track type
     * @return
     * @see MediaPlayer.TrackInfo
     */
    String getCurrTrackName(MediaPlayer mediaPlayer, int trackType);

    /**
     * getTracks
     * <p>tracks </p>
     *
     * @param mediaPlayer
     * @param trackType   track type
     * @return
     * @see MediaPlayer.TrackInfo
     */
    List<String> getTracks(MediaPlayer mediaPlayer, int trackType);

    /**
     * selectOrDeselectTrack
     * <p>select or deselect track </p>
     *
     * @param mediaPlayer
     * @param select
     * @param trackType   track type
     * @return
     * @see MediaPlayer.TrackInfo
     */
    void selectOrDeselectTrack(MediaPlayer mediaPlayer, boolean select, int trackType, int trackId);

    /**
     * select track by index
     * <p>Currently select,the track type </p>
     *
     * @param mediaPlayer
     * @param index
     * @param trackType   track type
     * @return
     * @see MediaPlayer.TrackInfo
     */
    int selectTrack(MediaPlayer mediaPlayer, int index, int trackType);
}
