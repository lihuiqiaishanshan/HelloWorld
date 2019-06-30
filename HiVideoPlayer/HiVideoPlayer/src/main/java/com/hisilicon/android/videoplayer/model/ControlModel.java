package com.hisilicon.android.videoplayer.model;

/**
 * Created on 2018/6/4.
 */

public class ControlModel {

    private int rewindRate = 1;
    private int forwardRate = 1;

    private int soundRate = 1;
    private int selectedSubId = 0;
    private int selectedTrack = 0;
    private int selectedChannel = 0;
    private String selectedColor = "0xffffff";
    private int selectedColorPos = 0;
    private int selectedSizes = -1;
    private int selectedEffect = -1;
    private int selectedPosition = 36;
    private int selectedTime = -1;
    private int PositionStep = 18;
    private int selectedAudio = 0;
    private int selectedSubEncode = 0;
    private int selectedSpace = -1;
    private int selectedLSpace = -1;
    private int selectedVolume = -1;
    private int selectedDolbyRangeInfo = -1;

    public int getRewindRate() {
        return rewindRate;
    }

    public void setRewindRate(int rewindRate) {
        this.rewindRate = rewindRate;
    }

    public int getForwardRate() {
        return forwardRate;
    }

    public void setForwardRate(int forwardRate) {
        this.forwardRate = forwardRate;
    }

    public int getSoundRate() {
        return soundRate;
    }

    public void setSoundRate(int soundRate) {
        this.soundRate = soundRate;
    }

    public int getSelectedSubId() {
        return selectedSubId;
    }

    public void setSelectedSubId(int selectedSubId) {
        this.selectedSubId = selectedSubId;
    }

    public int getSelectedTrack() {
        return selectedTrack;
    }

    public void setSelectedTrack(int selectedTrack) {
        this.selectedTrack = selectedTrack;
    }

    public int getSelectedChannel() {
        return selectedChannel;
    }

    public void setSelectedChannel(int selectedChannel) {
        this.selectedChannel = selectedChannel;
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(String selectedColor) {
        this.selectedColor = selectedColor;
    }

    public int getSelectedColorPos() {
        return selectedColorPos;
    }

    public void setSelectedColorPos(int selectedColorPos) {
        this.selectedColorPos = selectedColorPos;
    }

    public int getSelectedSizes() {
        return selectedSizes;
    }

    public void setSelectedSizes(int selectedSizes) {
        this.selectedSizes = selectedSizes;
    }

    public int getSelectedEffect() {
        return selectedEffect;
    }

    public void setSelectedEffect(int selectedEffect) {
        this.selectedEffect = selectedEffect;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(int selectedTime) {
        this.selectedTime = selectedTime;
    }

    public int getPositionStep() {
        return PositionStep;
    }

    public void setPositionStep(int positionStep) {
        PositionStep = positionStep;
    }

    public int getSelectedAudio() {
        return selectedAudio;
    }

    public void setSelectedAudio(int selectedAudio) {
        this.selectedAudio = selectedAudio;
    }

    public int getSelectedSubEncode() {
        return selectedSubEncode;
    }

    public void setSelectedSubEncode(int selectedSubEncode) {
        this.selectedSubEncode = selectedSubEncode;
    }

    public int getSelectedSpace() {
        return selectedSpace;
    }

    public void setSelectedSpace(int selectedSpace) {
        this.selectedSpace = selectedSpace;
    }

    public int getSelectedLSpace() {
        return selectedLSpace;
    }

    public void setSelectedLSpace(int selectedLSpace) {
        this.selectedLSpace = selectedLSpace;
    }

    public int getSelectedVolume() {
        return selectedVolume;
    }

    public void setSelectedVolume(int selectedVolume) {
        this.selectedVolume = selectedVolume;
    }

    public int getSelectedDolbyRangeInfo() {
        return selectedDolbyRangeInfo;
    }

    public void setSelectedDolbyRangeInfo(int selectedDolbyRangeInfo) {
        this.selectedDolbyRangeInfo = selectedDolbyRangeInfo;
    }
}
