package com.hisilicon.android.videoplayer.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import com.hisilicon.android.videoplayer.R;

public class CrossView extends FrameLayout implements View.OnClickListener{

    private View leftTopArea;
    private View rightTopArea;
    private View leftBottomArea;
    private View rightBottomArea;

    private IAreaClickListener iAreaClickListener;

    private boolean mIsAreaHide = false;
    private View topLine;
    private View leftLine;
    private View rightLine;
    private View bottomLine;
    private final int BLOD_LINE_VALUE = 6;
    private final int NORMAL_LINE_VALUE = 3;

    public CrossView(Context context) {
        super(context);
        initView(context);
    }

    public CrossView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.widget_cross_layout, this);
        leftTopArea = inflate.findViewById(R.id.left_top_area);
        rightTopArea = inflate.findViewById(R.id.right_top_area);
        leftBottomArea = inflate.findViewById(R.id.left_bottom_area);
        rightBottomArea = inflate.findViewById(R.id.right_bottom_area);
        leftTopArea.setOnClickListener(this);
        rightTopArea.setOnClickListener(this);
        leftBottomArea.setOnClickListener(this);
        rightBottomArea.setOnClickListener(this);
        leftTopArea.requestFocus();

        leftTopArea.setOnFocusChangeListener(onFocusChangeListener);
        rightTopArea.setOnFocusChangeListener(onFocusChangeListener);
        leftBottomArea.setOnFocusChangeListener(onFocusChangeListener);
        rightBottomArea.setOnFocusChangeListener(onFocusChangeListener);

        topLine = inflate.findViewById(R.id.top_line);
        leftLine = inflate.findViewById(R.id.left_line);
        rightLine = inflate.findViewById(R.id.right_line);
        bottomLine = inflate.findViewById(R.id.bottom_line);
    }

    @Override
    public void onClick(View v) {
        if (iAreaClickListener == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.left_top_area:
                iAreaClickListener.areaClick(AREAID.LEFT_TOP);
                break;
            case R.id.right_top_area:
                iAreaClickListener.areaClick(AREAID.RIGHT_TOP);
                break;
            case R.id.left_bottom_area:
                iAreaClickListener.areaClick(AREAID.LEFT_BOTTOM);
                break;
            case R.id.right_bottom_area:
                iAreaClickListener.areaClick(AREAID.RIGHT_BOTTOM);
                break;
        }
    }

    OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            setAreaFoucesSelected();
        }
    };

    private void setAreaFoucesSelected() {
        refreshAllFouces();
        if (leftTopArea.hasFocus()) {
            leftTopFoucesSelected();
        } else if(rightTopArea.hasFocus()) {
            rightTopFoucesSelected();
        } else if (leftBottomArea.hasFocus()) {
            leftBottomFoucesSelected();
        } else if (rightBottomArea.hasFocus()) {
            rightBottomFoucesSelected();
        }
    }

    private void leftTopFoucesSelected() {
        topLine.setBackgroundColor(Color.RED);
        leftLine.setBackgroundColor(Color.RED);
        ViewGroup.LayoutParams topLineLp = topLine.getLayoutParams();
        topLineLp.width = BLOD_LINE_VALUE;
        topLine.setLayoutParams(topLineLp);
        ViewGroup.LayoutParams leftLineLp = leftLine.getLayoutParams();
        leftLineLp.height = BLOD_LINE_VALUE;
        leftLine.setLayoutParams(leftLineLp);
    }

    private void rightTopFoucesSelected() {
        topLine.setBackgroundColor(Color.RED);
        rightLine.setBackgroundColor(Color.RED);
        ViewGroup.LayoutParams topLineLp = topLine.getLayoutParams();
        topLineLp.width = BLOD_LINE_VALUE;
        topLine.setLayoutParams(topLineLp);
        ViewGroup.LayoutParams rightLineLp = rightLine.getLayoutParams();
        rightLineLp.height = BLOD_LINE_VALUE;
        rightLine.setLayoutParams(rightLineLp);
    }

    private void leftBottomFoucesSelected() {
        leftLine.setBackgroundColor(Color.RED);
        bottomLine.setBackgroundColor(Color.RED);
        ViewGroup.LayoutParams leftLineLp = leftLine.getLayoutParams();
        leftLineLp.height = BLOD_LINE_VALUE;
        leftLine.setLayoutParams(leftLineLp);
        ViewGroup.LayoutParams bottomLineLp = bottomLine.getLayoutParams();
        bottomLineLp.width = BLOD_LINE_VALUE;
        bottomLine.setLayoutParams(bottomLineLp);
    }

    private void rightBottomFoucesSelected() {
        rightLine.setBackgroundColor(Color.RED);
        bottomLine.setBackgroundColor(Color.RED);
        ViewGroup.LayoutParams bottomLineLp = bottomLine.getLayoutParams();
        bottomLineLp.width = BLOD_LINE_VALUE;
        bottomLine.setLayoutParams(bottomLineLp);
        ViewGroup.LayoutParams rightLineLp = rightLine.getLayoutParams();
        rightLineLp.height = BLOD_LINE_VALUE;
        rightLine.setLayoutParams(rightLineLp);
    }

    private void refreshAllFouces() {
        ViewGroup.LayoutParams topLineLp = topLine.getLayoutParams();
        topLineLp.width = NORMAL_LINE_VALUE;
        topLine.setLayoutParams(topLineLp);
        ViewGroup.LayoutParams leftLineLp = leftLine.getLayoutParams();
        leftLineLp.height = NORMAL_LINE_VALUE;
        leftLine.setLayoutParams(leftLineLp);
        ViewGroup.LayoutParams rightLineLp = rightLine.getLayoutParams();
        rightLineLp.height = NORMAL_LINE_VALUE;
        rightLine.setLayoutParams(rightLineLp);
        ViewGroup.LayoutParams bottomLineLp = bottomLine.getLayoutParams();
        bottomLineLp.width = NORMAL_LINE_VALUE;
        bottomLine.setLayoutParams(bottomLineLp);

        topLine.setBackgroundColor(Color.BLACK);
        leftLine.setBackgroundColor(Color.BLACK);
        rightLine.setBackgroundColor(Color.BLACK);
        bottomLine.setBackgroundColor(Color.BLACK);
    }

    public boolean getAreaIsHide() {
        return mIsAreaHide;
    }

    public void hideAllArea() {
        leftTopArea.setVisibility(GONE);
        rightTopArea.setVisibility(GONE);
        leftBottomArea.setVisibility(GONE);
        rightBottomArea.setVisibility(GONE);
        leftLine.setVisibility(GONE);
        topLine.setVisibility(GONE);
        rightLine.setVisibility(GONE);
        bottomLine.setVisibility(GONE);
        mIsAreaHide = true;
    }

    public void showAllArea() {
        leftTopArea.setVisibility(VISIBLE);
        rightTopArea.setVisibility(VISIBLE);
        leftBottomArea.setVisibility(VISIBLE);
        rightBottomArea.setVisibility(VISIBLE);
        leftLine.setVisibility(VISIBLE);
        topLine.setVisibility(VISIBLE);
        rightLine.setVisibility(VISIBLE);
        bottomLine.setVisibility(VISIBLE);
        requestFocus();
        mIsAreaHide = false;
    }

    public void setIAreaClickListener(IAreaClickListener i) {
        iAreaClickListener = i;
    }

    public interface IAreaClickListener{
        void areaClick(AREAID ID);
    }

    public enum AREAID{
        LEFT_TOP,
        RIGHT_TOP,
        LEFT_BOTTOM,
        RIGHT_BOTTOM
    }

}
