package com.hisilicon.android.hidvdinfo;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;

/**
 * HiDVDInfo: get DVD info, Usage: openDVD->playback->closeDVD
 */
public class HiDVDInfo
{
    /**
     * HiDVDInfo.DVDCommand: The dvd invoke command value
     * the commands' value will be reassigned in the jni register function
     */
    public static class DVDCommand
    {
        /** command value for open dvd */
        public static int DVD_CMD_OPEN_DVD = 1051;
        /** command value for close dvd */
        public static int DVD_CMD_CLOSE_DVD = 1052;
        /** command value for check disc infomation */
        public static int DVD_CMD_CHECK_DVD_DISC_INFO = 1053;
        /** command value for get title number */
        public static int DVD_CMD_GET_DVD_TITLE_NUMBER = 1061;
        /** command value for get chapter number */
        public static int DVD_CMD_GET_DVD_CHAPTER_NUMBER = 1062;
        /** command value for get default title */
        public static int DVD_CMD_GET_DVD_DEFAULT_TITLE = 1064;
        /** command value for get subtitle language */
        public static int DVD_CMD_GET_DVD_SUBPICTURE_LANGUAGE = 1066;
        /** command value for get audio track language */
        public static int DVD_CMD_GET_DVD_AUDIO_TRACK_LANGUAGE = 1067;
        /** command value for get subtitle language */
        public static int DVD_CMD_GET_CUR_DVD_CHAPTER = 1068;
        /** command value for get duration */
        public static int DVD_CMD_GET_DVD_DURATION = 1069;
        /** command value for get chapter position */
        public static int DVD_CMD_GET_DVD_CHAPTER_DURATION = 1070;
    }

    static
    {
        System.loadLibrary("dvdinfo_jni");
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
     * open DVD.
     * <p>
     * open DVD file by pPath.<br>
     * <br>
     * @return 0 if open DVD successfully, fail otherwise
     */
    public int openDVD(String pPath)
    {
        return excuteCommand(DVDCommand.DVD_CMD_OPEN_DVD, pPath);
    }

    /**
     * close DVD.
     * <p>
     * close the openned DVD file.<br>
     * <br>
     * @return 0 if close DVD successfully, fail otherwise
     */
    public int closeDVD()
    {
        return excuteCommand(DVDCommand.DVD_CMD_CLOSE_DVD);
    }

    /**
     * check disc info.
     * <p>
     * Check whether the file is  DVD Video .<br>
     * <br>
     * @return 0 if this is a  DVD Video, otherwise not a  DVD Video
     */
    public int checkDiscInfo()
    {
        return excuteCommand(DVDCommand.DVD_CMD_CHECK_DVD_DISC_INFO, true);
    }

    /**
     * get title number.
     * <p>
     * get title number in the DVD file.<br>
     * <br>
     * @return title number
     */
    public int getTitleNumber()
    {
        return excuteCommand(DVDCommand.DVD_CMD_GET_DVD_TITLE_NUMBER, true);
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
        return excuteCommand(DVDCommand.DVD_CMD_GET_DVD_CHAPTER_NUMBER, pTitleId, true);
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
        return excuteCommand(DVDCommand.DVD_CMD_GET_DVD_DEFAULT_TITLE, true);
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
        return excuteCommand(DVDCommand.DVD_CMD_GET_CUR_DVD_CHAPTER, pTitleId, pPosition, true);
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
        return excuteCommand(DVDCommand.DVD_CMD_GET_DVD_DURATION, pTitleId, true);
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
        return excuteCommand(DVDCommand.DVD_CMD_GET_DVD_CHAPTER_DURATION, pTitleId, pChapterId, true);
    }

    /**
     * get subtitle language list.
     * <p>
     * get the subtitle language list which contains in DVD file.<br>
     * <br>
     * @return the subtitle language list
     */
    public List<String> getSubtitleLanguageList()
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        List<String> _LanguageList = new ArrayList<String>();

        _Request.writeInt(DVDCommand.DVD_CMD_GET_DVD_SUBPICTURE_LANGUAGE);

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
     * get audio track language list which contains in DVD file.<br>
     * <br>
     * @return the audio track language list
     */
    public List<String> getAudioTrackLanguageList()
    {
        Parcel _Request = Parcel.obtain();
        Parcel _Reply = Parcel.obtain();
        List<String> _LanguageList = new ArrayList<String>();

        _Request.writeInt(DVDCommand.DVD_CMD_GET_DVD_AUDIO_TRACK_LANGUAGE);

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
