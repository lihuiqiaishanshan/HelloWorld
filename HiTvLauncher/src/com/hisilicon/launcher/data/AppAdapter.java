
package com.hisilicon.launcher.data;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.hisilicon.launcher.util.LogHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.util.Constant;

public class AppAdapter extends BaseAdapter {
    private final String TAG = "AppAdapter";
    // bitmap cache
    private Map<String, SoftReference<Bitmap>> mImageCache = new HashMap<String, SoftReference<Bitmap>>();
    // the list of ResolveInfo
    private List<ResolveInfo> mAppList = new ArrayList<ResolveInfo>();
    private Context mContext = null;
    private LayoutInflater mLayoutInflater = null;
    // current page number
    private int mCurrentPage = 0;
    // PackageManager object
    private PackageManager mPackManager;
    // load end listener
    private LoadEndListener mLoadEndListerner;

    public AppAdapter(Context mContext, List<ResolveInfo> appList,
            int currentPage, int totalPages) {
        super();
        this.mAppList = appList;
        this.mContext = mContext;
        this.mCurrentPage = currentPage;
        mLayoutInflater = LayoutInflater.from(mContext);
        this.mPackManager = mContext.getPackageManager();
    }

    public void setmLoadEndListener(LoadEndListener mLoadEndListerner) {
        this.mLoadEndListerner = mLoadEndListerner;
    }

    /**
     * set Listener to monitor that every gridview has loaded data
     *
     * @author huyq
     */
    public interface LoadEndListener {
        void onEndListerner(int currentPage);
    }

    @Override
    public int getCount() {
        if (null != mAppList) {
            return mAppList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(null !=  mAppList) {
            if(mAppList.size() > position) {
                return mAppList.get(position);
            } else {
                return mAppList.get(0);
            }
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.app_grid_item, null);
            holder = new ViewHolder();
            holder.my_grid_layout = (RelativeLayout) convertView
                    .findViewById(R.id.my_grid_layout);
            holder.appIcon = (ImageView) convertView
                    .findViewById(R.id.my_grid_img);
            holder.appName = (TextView) convertView
                    .findViewById(R.id.my_grid_txt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // binding data
        ResolveInfo info = mAppList.get(position);
        Bitmap bitmap = getBitmapByPath(info.activityInfo.packageName);
        if (bitmap != null) {
            holder.appIcon
                    .setBackgroundDrawable(new BitmapDrawable(bitmap));
        } else {
            initItem(info, position, holder);
            holder.appIcon.setBackgroundDrawable(new BitmapDrawable(
                    getBitmapByPath(info.activityInfo.packageName)));
        }
        CharSequence label = info.loadLabel(mPackManager);
        if(label != null) {
            holder.appName.setText(label.toString());
        }else {
            holder.appName.setText("");
        }
        if (position == mAppList.size() - 1) {
            LogHelper.d(TAG, "mLoadEddListerner :" + mLoadEndListerner);
            if (mLoadEndListerner != null) {
                mLoadEndListerner.onEndListerner(mCurrentPage);
            }
        }
        return convertView;
    }

    /**
     * add the Bitmap to the Bitmap Cache
     *
     * @param path
     * @param bitmap
     */
    public void addBitmapToCache(String path, Bitmap bitmap) {
        // Strong references to Bitmap objects
        // Bitmap bitmap = BitmapFactory.decodeFile(path);
        // Soft references to Bitmap objects
        SoftReference<Bitmap> softBitmap = new SoftReference<Bitmap>(bitmap);
        // add the object to the Map to cache
        mImageCache.put(path, softBitmap);
    }

    /**
     * get bitmap by path
     *
     * @param path
     * @return
     */
    public Bitmap getBitmapByPath(String path) {
        // soft references to Bitmap objects from the cache
        SoftReference<Bitmap> softBitmap = mImageCache.get(path);
        // to determine whether there is a soft reference
        if (softBitmap == null) {
            return null;
        }
        // remove the Bitmap object, if due to insufficient memory Bitmap is
        // recovered,
        // will obtain the empty
        Bitmap bitmap = softBitmap.get();
        return bitmap;
    }

    /**
     * set up Item
     *
     * @param info
     * @param position
     * @param holder
     */
    private void initItem(ResolveInfo info, int position, ViewHolder holder) {
        if (info == null) {
            return;
        }
        String pkg = info.activityInfo.packageName;
        Drawable draw = null;
        int width = 0;
        int height = 0;
        // define pre converted image width and height
        int dimen180 = (int) mContext.getResources().getDimension(R.dimen.dimen_180px);
        int newWidth = dimen180;
        int newHeight = dimen180;
        // the create operation pictures of Matrix object
        Matrix matrix = new Matrix();
        Bitmap resizedBitmap = null;
        // set Icon
        /*
         * if (Util.PKG_MAP != null && Util.PKG_MAP.get(pkg) != null) { draw =
         * mContext.getResources().getDrawable(Util.PKG_MAP.get(pkg));
         * //view.setBackgroundDrawable(draw); } else {
         */
        draw = info.loadIcon(mPackManager);
        if (draw != null) {
            width = draw.getIntrinsicHeight();
            height = draw.getIntrinsicWidth();
            Bitmap bitmap = drawableToBitmap(draw);
            // calculate the scaling rate, new dimensions in addition to the
            // original size
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // scale the image motion
            matrix.postScale(scaleWidth, scaleHeight);
            // create a new picture
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth,
                    newHeight, true);
            addBitmapToCache(pkg, resizedBitmap);
            // will it create Bitmap converted to Drawable objects, so that it
            // can be used in ImageView
            // bmd = new BitmapDrawable(resizedBitmap);
            // holder.icon.setBackgroundDrawable(bmd);
        } else {
            Bitmap bitmapOrg = BitmapFactory.decodeResource(
                    mContext.getResources(), R.drawable.ic_launcher);
            width = bitmapOrg.getWidth();
            height = bitmapOrg.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // scale the image motion
            matrix.postScale(scaleWidth, scaleHeight);
            resizedBitmap = Bitmap.createScaledBitmap(bitmapOrg, newWidth,
                    newHeight, true);
            addBitmapToCache(pkg, resizedBitmap);
        }
    }

    /**
     * drawable to bitmap
     */
    private Bitmap drawableToBitmap(Drawable draw) {
        int width = draw.getIntrinsicWidth();
        int height = draw.getIntrinsicHeight();
        Bitmap.Config config = draw.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        draw.setBounds(0, 0, width, height);
        draw.draw(canvas);
        return bitmap;
    }

    /**
     * view holder
     */
    public class ViewHolder {
        public RelativeLayout my_grid_layout;
        public LinearLayout my_grid_img_bg;
        public ImageView appIcon;
        public TextView appName;
    }

}
