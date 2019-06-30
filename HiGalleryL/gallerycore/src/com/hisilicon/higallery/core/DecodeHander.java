
package com.hisilicon.higallery.core;

import android.os.Handler;
import android.os.Looper;

public class DecodeHander extends Handler {
    private DecodeThread mThumDecodeRunnable;

    public DecodeHander(Looper looper) {
        super(looper);
    }

    public void postDecode(DecodeThread decodeThread) {
        stop();
        mThumDecodeRunnable = decodeThread;
        post(mThumDecodeRunnable);
    }

    public void stop() {
        if (mThumDecodeRunnable != null) {
            mThumDecodeRunnable.quit();
            removeCallbacks(mThumDecodeRunnable);
            mThumDecodeRunnable = null;
        }
    }

}
