
package com.hisilicon.higallery;

import java.util.ArrayList;
import java.util.LinkedList;

import com.hisilicon.higallery.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;

import com.hisilicon.higallery.core.BitmapDecodeUtils;
import com.hisilicon.higallery.core.GalleryCore;
import com.hisilicon.higallery.load.LoadThumTask;
import com.hisilicon.higallery.load.ThumImageCache;

@SuppressWarnings("deprecation")
public class GalleryAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<String> mFilePath;
    private int mSize;
    private Gallery mGallery;
    private LayoutParams mParams;
    private ThumImageCache mImageCache;
    private Bitmap mLoadintBitmap;
    GalleryCore mGalleryCore;

    public GalleryAdapter(Context context, ArrayList<String> filePath, GalleryCore galleryCore, Gallery gallery, int size) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setDate(mFilePath);
        mSize = size;
        mParams = new LayoutParams(mSize, mSize);
        mGallery = gallery;
        mGalleryCore = galleryCore;
        mImageCache = new ThumImageCache();
        mLoadintBitmap = BitmapDecodeUtils.getThumBitmap(context.getResources(),
                R.drawable.picture_loading, mSize, mSize);
    }

    public void updateDate(ArrayList<String> filePath) {
        setDate(filePath);
        notifyDataSetChanged();
    }

    private void setDate(ArrayList<String> filePath) {
        if (filePath != null) {
            mFilePath = filePath;
        } else {
            mFilePath = new ArrayList<String>();
        }
    }

    @Override
    public int getCount() {
        return mFilePath.size();
//        return 7;
    }

    @Override
    public Object getItem(int position) {
        return mFilePath.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.thumbnail_item, null);
        }

        ImageView imageView = (ImageView) convertView;
        imageView.setLayoutParams(mParams);
        String imagePath = mFilePath.get(position);
        imageView.setTag(imagePath);
        if (mLoadintBitmap != null && !mLoadintBitmap.isRecycled()) {
            imageView.setImageBitmap(mLoadintBitmap);
        }

        loadBitmap(imagePath, imageView);
        return convertView;
    }

    private void loadBitmap(String path, ImageView imageView) {
        Bitmap bitmap = mImageCache.getBitmapFromCache(path);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            LoadThumTask task = new LoadThumTask(mGalleryCore, mGallery, mSize, mImageCache);
            task.execute(path);
        }
    }

    public void recycle() {
        mLoadintBitmap = null;
        mImageCache.recycle();
    }
}
