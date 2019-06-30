package com.hisilicon.tvui.pvr;

public class RecordPlayStatus {
    private static int PVR_PAUSE = 0;
    private static int PVR_PLAY = 1;
    private static int PVR_REWIND = 2;
    private static int PVR_FAST = 3;
    private static int PVR_SLOW = 4;
    private boolean isPlaying = false;

    private static class RecordPlayStatusHolder {
        private static RecordPlayStatus instance = new RecordPlayStatus();
    }

    private RecordPlayStatus() {

    }

    public static RecordPlayStatus getInstance() {
        return RecordPlayStatusHolder.instance;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
