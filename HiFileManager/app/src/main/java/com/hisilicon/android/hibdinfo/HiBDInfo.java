package com.hisilicon.android.hibdinfo;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.util.Log;
/**
 * HiBDInfo: get bluray info, independent from HiBDPlayer Usage: openBluray->do
 * something->closeBluray
 */
public class HiBDInfo
{
    /**
     * HiBDInfo.BDCommand: The bluray invoke command value
     * the commands' value will be reassigned in the jni register function
     */
    public static class BDCommand
    {
        /** open bluray command declaration */
        public static int BD_CMD_OPEN_BLURAY;
        /** close bluray command declaration */
        public static int BD_CMD_CLOSE_BLURAY;
        /** check disc infomation command declaration */
        public static int BD_CMD_CHECK_DISC_INFO;
        /** get title number command declaration */
        public static int BD_CMD_GET_TITLE_NUMBER;
        /** get chapter number command declaration */
        public static int BD_CMD_GET_CHAPTER_NUMBER;
        /** get playlist command declaration */
        public static int BD_CMD_GET_PLAYLIST;
        /** get default playlist command declaration */
        public static int BD_CMD_GET_DEFAULT_PLAYLIST;
        /** get chapter position declaration */
        public static int BD_CMD_GET_CHAPTER_POSITION;
        /** get default title command declaration */
        public static int BD_CMD_GET_DEFAULT_TITLE;
        /** get title command declaration */
        public static int BD_CMD_GET_TITLE;
        /** get subtitle language command declaration */
        public static int BD_CMD_GET_SUBTITLE_LANGUAGE;
        /** get audio track language command declaration */
        public static int BD_CMD_GET_AUDIO_TRACK_LANGUAGE;
        /** get current chapter command declaration */
        public static int BD_CMD_GET_CUR_CHAPTER;
        /** get duration command declaration */
        public static int BD_CMD_GET_DURATION;
        /** check 3D formate command declaration */
        public static int BD_CMD_CHECK_3D;
        /** get poster command declaration */
        public static int BD_CMD_GET_POSTER;
    }

    static
    {
        System.loadLibrary("bdinfo_jni");
    }

    private Object mLock = new Object();

    private native final int native_invoke(Parcel pRequest, Parcel pReply);

    /**
     * do invoke command.
     * <p>
     * Implementation of do invoke command. Passing parameters by pRequest and getting Results by pReply.<br>
     * <br>
     * @param pRequest pRequest indicates the parameter to be set.
     * @param pReply pReply indicates the result to return.
     * @return 0 if invoke successfully, fail otherwise
     */
    public int invoke(Parcel pRequest, Parcel pReply)
    {
        synchronized (mLock) {
            pRequest.setDataPosition(0);

            int _Ret = native_invoke(pRequest, pReply);
            pReply.setDataPosition(0);
            return _Ret;
        }
    }

    /**
     * open bluray.
     * <p>
     * open bluray file by pPath.<br>
     * <br>
     * @return 0 if open Bluray successfully, fail otherwise
     */
    public int openBluray(String pPath)
    {
        return excuteCommand(BDCommand.BD_CMD_OPEN_BLURAY, pPath);
    }

    /**
     * close bluray.
     * <p>
     * close the openned bluray file.<br>
     * <br>
     * @return 0 if close Bluray successfully, fail otherwise
     */
    public int closeBluray()
    {
        return excuteCommand(BDCommand.BD_CMD_CLOSE_BLURAY);
    }

    /**
     * check disc info.
     * <p>
     * Check whether the file is bluray .<br>
     * <br>
     * @return 0 if this is a bluray, otherwise not a bluray
     */
    public int checkDiscInfo()
    {
        return excuteCommand(BDCommand.BD_CMD_CHECK_DISC_INFO, true);
    }

    /**
     * get title number.
     * <p>
     * get title number in the bluray file.<br>
     * <br>
     * @return title number
     */
    public int getTitleNumber()
    {
        return excuteCommand(BDCommand.BD_CMD_GET_TITLE_NUMBER, true);
    }

    /**
     * get chapter number by title id.
     * <p>
     * get chapter number by specified title id.<br>
     * <br>
     * @param pTitleId pTitleId indicates the title id to be set.
     * @return chapter number
     */
    public int getChapterNumberByTitleId(int pTitleId)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_CHAPTER_NUMBER, pTitleId, true);
    }

    /**
     * get playlist by title id.
     * <p>
     * get playlist by specified title id.<br>
     * <br>
     * @param pTitleId pTitleId indicates the title id to be set.
     * @return playlist
     */
    public int getPlaylistByTitleId(int pTitleId)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_PLAYLIST, pTitleId, true);
    }

    /**
     * get default playlist.
     * <p>
     * return the default playlist.<br>
     * <br>
     * @return default playlist
     */
    public int getDefaultPlaylist()
    {
        return excuteCommand(BDCommand.BD_CMD_GET_DEFAULT_PLAYLIST, true);
    }

    /**
     * Check whether is 3D format.
     * <p>
     * Check whether is 3D format. the return value only have 0/-1/1.<br>
     * <br>
     * @return 1 if 3D format, 0 if not 3D format, -1 if open error.
     */
    public int checkBluray3D()
    {
        return excuteCommand(BDCommand.BD_CMD_CHECK_3D, true);
    }

    /**
     * get poster.
     * <p>
     * get the poster contains in bluray file.<br>
     * <br>
     * @return poster.
     */
    public byte[] getPoster()
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();

        _Request.setDataPosition(0);
        _Request.writeInt(BDCommand.BD_CMD_GET_POSTER);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return null;
        }

        int _Result = _Reply.readInt();
        int post_size = _Reply.readInt();
        byte[] poster = new byte[post_size];

        _Reply.readByteArray(poster);
        _Request.recycle();
        _Reply.recycle();

        return poster;
    }

    /**
     * get default title id.
     * <p>
     * return the default title id.<br>
     * <br>
     * @return default title id.
     */
    public int getDefaultTitleId()
    {
        return excuteCommand(BDCommand.BD_CMD_GET_DEFAULT_TITLE, true);
    }

    /**
     * get title id by playlist.
     * <p>
     * get playlist and return title id.<br>
     * <br>
     * @param pPlaylist pPlaylist indicates the playlist id to be set.
     * @return The corresponding title id
     */
    public int getTitleIdByPlaylist(int pPlaylist)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_TITLE, pPlaylist, true);
    }

    /**
     * get chapter position.
     * <p>
     * get chapter position by title id and chapter id.<br>
     * <br>
     * @param pTitleId pTitleId indicates the title id to be set.
     * @param pChapterId pChapterId indicates the chapter id to be set.
     * @return The corresponding chapter position
     */
    public int getChapterPosition(int pTitleId, int pChapterId)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_CHAPTER_POSITION, pTitleId, pChapterId, true);
    }

    /**
     * get current chapter id.
     * <p>
     * get current chapter id by title id and position.<br>
     * <br>
     * @param pTitleId pTitleId indicates the title id to be set.
     * @param pPosition pPosition indicates the position to be set.
     * @return The corresponding chapter id
     */
    public int getCurChapterId(int pTitleId, int pPosition)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_CUR_CHAPTER, pTitleId, pPosition, true);
    }

    /**
     * get duration by title id.
     * <p>
     * return duration according title id.<br>
     * <br>
     * @param pTitleId pTitleId indicates the title id to be set.
     * @return duration
     */
    public int getDurationByTitleId(int pTitleId)
    {
        return excuteCommand(BDCommand.BD_CMD_GET_DURATION, pTitleId, true);
    }

    /**
     * get subtitle language list.
     * <p>
     * get the subtitle language list which contains in bluray file.<br>
     * <br>
     * @return the subtitle language list
     */
    public List<String> getSubtitleLanguageList()
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        List<String> _LanguageList = new ArrayList<String>();

        _Request.writeInt(BDCommand.BD_CMD_GET_SUBTITLE_LANGUAGE);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return null;
        }

        // for get
        _Reply.readInt();
        int _SubtitleNum = _Reply.readInt();
        String _Language = "";

        for (int i = 0; i < _SubtitleNum; i++)
        {
            _Language = _Reply.readString();
            if (_Language.equals(""))
            {
                _Language = "und";
            }
            _LanguageList.add(_Language);
        }

        _Request.recycle();
        _Reply.recycle();

        return _LanguageList;
    }

    /**
     * get audio track language list.
     * <p>
     * get audio track language list which contains in bluray file.<br>
     * <br>
     * @return the audio track language list
     */
    public List<String> getAudioTrackLanguageList()
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        List<String> _LanguageList = new ArrayList<String>();

        _Request.writeInt(BDCommand.BD_CMD_GET_AUDIO_TRACK_LANGUAGE);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return null;
        }

        // for get
        _Reply.readInt();
        int _AudioTrackNum = _Reply.readInt();
        String _Language = "";

        for (int i = 0; i < _AudioTrackNum; i++)
        {
            _Language = _Reply.readString();
            if (_Language.equals(""))
            {
                _Language = "und";
            }
            _LanguageList.add(_Language);
        }

        _Request.recycle();
        _Reply.recycle();

        return _LanguageList;
    }

    private int excuteCommand(int pCmdId, boolean pIsGet)
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        _Request.setDataPosition(0);
        _Request.writeInt(pCmdId);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return -1;
        }

        if (pIsGet)
        {
            _Reply.readInt();
        }

        int _Result = _Reply.readInt();

        _Request.recycle();
        _Reply.recycle();

        return _Result;
    }

    private int excuteCommand(int pCmdId, int pArg1, boolean pIsGet)
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        _Request.setDataPosition(0);
        _Request.writeInt(pCmdId);
        _Request.writeInt(pArg1);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return -1;
        }

        if (pIsGet)
        {
            _Reply.readInt();
        }

        int _Result = _Reply.readInt();

        _Request.recycle();
        _Reply.recycle();

        return _Result;
    }

    private int excuteCommand(int pCmdId, String pArg1, boolean pIsGet)
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        _Request.setDataPosition(0);
        _Request.writeInt(pCmdId);
        _Request.writeString(pArg1);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return -1;
        }

        if (pIsGet)
        {
            _Reply.readInt();
        }

        int _Result = _Reply.readInt();

        _Request.recycle();
        _Reply.recycle();

        return _Result;
    }

    private int excuteCommand(int pCmdId, int pArg1, int pArg2, boolean pIsGet)
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        _Request.setDataPosition(0);
        _Request.writeInt(pCmdId);
        _Request.writeInt(pArg1);
        _Request.writeInt(pArg2);

        if (invoke(_Request, _Reply) != 0)
        {
            _Request.recycle();
            _Reply.recycle();
            return -1;
        }

        if (pIsGet)
        {
            _Reply.readInt();
        }

        int _Result = _Reply.readInt();

        _Request.recycle();
        _Reply.recycle();

        return _Result;
    }

    private int excuteCommand(int pCmdId)
    {
        return excuteCommand(pCmdId, false);
    }

    private int excuteCommand(int pCmdId, int pArg1)
    {
        return excuteCommand(pCmdId, pArg1, false);
    }

    private int excuteCommand(int pCmdId, String pArg1)
    {
        return excuteCommand(pCmdId, pArg1, false);
    }

    private int excuteCommand(int pCmdId, int pArg1, int pArg2)
    {
        return excuteCommand(pCmdId, pArg1, pArg2, false);
    }
}
