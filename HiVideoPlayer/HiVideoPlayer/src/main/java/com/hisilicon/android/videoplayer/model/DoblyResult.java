package com.hisilicon.android.videoplayer.model;

/**
 * Created on 2018/7/10.
 */

public class DoblyResult {


    private int error_flag = -1;
    private int atmos_flag = -1;
    private int audio_flag = -1;


    public int getError_flag() {
        return error_flag;
    }

    public void setError_flag(int error_flag) {
        this.error_flag = error_flag;
    }

    public int getAtmos_flag() {
        return atmos_flag;
    }

    public void setAtmos_flag(int atmos_flag) {
        this.atmos_flag = atmos_flag;
    }

    public int getAudio_flag() {
        return audio_flag;
    }

    public void setAudio_flag(int audio_flag) {
        this.audio_flag = audio_flag;
    }


    @Override
    public String toString() {
        return "DoblyResult{" +
                "error_flag=" + error_flag +
                ", atmos_flag=" + atmos_flag +
                ", audio_flag=" + audio_flag +
                '}';
    }
}
