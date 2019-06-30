
package com.hisilicon.higallery.core;

public class DecodeThread implements Runnable {
    private boolean mIsContinue = true;
    private DecodeListener mListener;

    @Override
    public void run() {
        if (!mIsContinue) {
            return;
        }

        if (mListener != null) {
            mListener.onStartDecode();
        }
    }

    public void setListener(DecodeListener decodeListener) {
        this.mListener = decodeListener;
    }

    public void quit() {
        mIsContinue = false;
    }

    public interface DecodeListener {
        void onStartDecode();
    }
}
