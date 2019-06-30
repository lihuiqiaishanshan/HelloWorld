
package com.hisilicon.higallery.load;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ThumImageCache {
    private LruCache<String, Bitmap> mLruCache;

    public ThumImageCache() {
        int totalSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = totalSize / 12;
        if (cacheSize > 8*1024) {
            cacheSize = 8*1024;
        }
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToCache(String path, Bitmap bitmap) {
        if (mLruCache.get(path) == null) {
            mLruCache.put(path, bitmap);
        }
    }

    public Bitmap getBitmapFromCache(String path) {
        return mLruCache.get(path);
    }

    public void recycle() {
        if(mLruCache != null){
            mLruCache.evictAll();
        }
    }
}
