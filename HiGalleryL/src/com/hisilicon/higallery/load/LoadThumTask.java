
package com.hisilicon.higallery.load;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Gallery;
import android.widget.ImageView;

import com.hisilicon.higallery.core.BitmapDecodeUtils;
import com.hisilicon.higallery.core.GalleryCore;

@SuppressWarnings("deprecation")
public class LoadThumTask extends AsyncTask<String, Void, Bitmap> {
    private Gallery mGallery;
    private int mSize;
    private String mPath;
    private ThumImageCache mCache;
    GalleryCore mGalleryCore;

    public LoadThumTask(GalleryCore core, Gallery gallery, int size, ThumImageCache cache) {
        mGallery = gallery;
        mSize = size;
        mCache = cache;
        mGalleryCore = core;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        mPath = params[0];
        return BitmapDecodeUtils.getThumBitmap(mGalleryCore, mPath, mSize, mSize);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView imageView = (ImageView) mGallery.findViewWithTag(mPath);
        if (imageView != null && bitmap != null) {
            imageView.setImageBitmap(bitmap);
            mCache.addBitmapToCache(mPath, bitmap);
        }
    }
}
