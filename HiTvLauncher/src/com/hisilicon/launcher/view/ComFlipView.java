
package com.hisilicon.launcher.view;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.hisilicon.launcher.R;

/**
 * Rewrite ViewFlipper
 *
 * @author janey
 */
public class ComFlipView extends FrameLayout {

    private Context mContext = null;
    // Image resources
    private int mResource[] = null;
    // Switching pictures view
    private ViewFlipper mViewflipper = null;

    // Polka Dot
    // private CircleDots dots = null;

    /**
     * Constructor
     *
     * @param context
     */
    public ComFlipView(Context context, final int resource[]) {
        super(context);
        mContext = context;
        mResource = resource;

        setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT, 1));
        setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
        init();
    }

    /**
     * Create images according to resource view
     */
    private void init() {
        if (mResource == null || mResource.length <= 0) {
            return;
        }
        mViewflipper = new ViewFlipper(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
        mViewflipper.setLayoutParams(params);
        LinearLayout layout = null;
        for (int i = 0; i < mResource.length; i++) {
            layout = new LinearLayout(mContext);
            layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));
            layout.setBackgroundResource(mResource[i]);
            layout.setFocusable(false);
            mViewflipper.addView(layout);
        }
        addView(mViewflipper);
        LinearLayout layouts = new LinearLayout(mContext);
        LayoutParams layoutsparams = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
        layouts.setOrientation(LinearLayout.VERTICAL);
        layouts.setLayoutParams(layoutsparams);
        LinearLayout layout1 = new LinearLayout(mContext);
        layout1.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                android.widget.LinearLayout.LayoutParams.FILL_PARENT, 2));
        // dots = new CircleDots(mContext, mResource.length, 3,
        // R.drawable.point_bg_1);
        layouts.addView(layout1);
        // layouts.addView(dots);
        addView(layouts);

    }

    /**
     * showed the next
     */
    public void showNext() {
        if (mViewflipper != null) {
            /*
             * mViewflipper.setInAnimation(mContext, R.animator.push_right_in);
             * mViewflipper.setOutAnimation(mContext,
             * R.animator.push_right_out);
             */
            mViewflipper.setInAnimation(mContext, R.animator.push_left_in);
            mViewflipper.setOutAnimation(mContext, R.animator.push_left_out);
            mViewflipper.showNext();
            // if (dots != null) {
            // dots.showNext();
            // }
        }
    }

    /**
     * showed the previous
     */
    public void showPrevious() {
        if (mViewflipper != null) {
            /*
             * mViewflipper.setInAnimation(mContext, R.animator.push_left_in);
             * mViewflipper.setOutAnimation(mContext, R.animator.push_left_out);
             */
            mViewflipper.setInAnimation(mContext, R.animator.push_right_in);
            mViewflipper.setOutAnimation(mContext, R.animator.push_right_out);
            // mViewflipper.showPrevious();
            // if (dots != null) {
            // dots.showPre();
            // }
        }
    }
}
