package com.li.demo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MyView extends TextView implements OnClickListener {

	public final static String TAG = MyView.class.getSimpleName();

	// 需要显示的内容

	private String text = "";

	// 文本横纵坐标

	private float textX = 0f;

	private float textY = 0f;

	// 文本长度

	private float textLength = 0f;

	// 控件长度

	private float viewWidth = 0f;

	// 一个文本长度 + 一个控件长度*************A----------------B

	private float temp1 = 0.0f;

	// 两个文本长度 + 一个控件长度A2----------------B2*************A----------------B

	// 文字从A走到A2

	private float temp2 = 0.0f;

	// 标志文本是否在滚动

	public boolean isRunning = false;

	// 画笔

	private Paint paint = null;

	public MyView(Context context) {

		super(context);

		initView();

	}

	public MyView(Context context, AttributeSet attrs) {

		super(context, attrs);

		initView();

	}

	public MyView(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);

		initView();

	}

	// 初始化控件监听器

	private void initView() {

		setOnClickListener(this);

	}

	// 文本初始化，每次更改文本内容或者文本效果等之后都需要重新初始化一下

	public void initText(WindowManager windowManager) {

		paint = getPaint();

		text = getText().toString();

		// 得到文本长度

		textLength = paint.measureText(text);

		// 得到控件长度

		viewWidth = this.getWidth();

		if (viewWidth == 0) {

			// 如果控件长度为0,用WindowManager获取一个默认的显示器，并获取其长度

			if (windowManager != null) {

				Display display = windowManager.getDefaultDisplay();

				viewWidth = display.getWidth();

			}

		}

		// 文本开始滚动的位置（上图A点）横坐标

		textX = textLength;

		temp1 = viewWidth + textLength;

		temp2 = viewWidth + textLength * 2;

		// 文本开始滚动的位置（上图A点）纵坐标

		textY = getTextSize() + getPaddingTop();

	}

	// 保存状态数据至saveState

	// @return

	@Override
	public Parcelable onSaveInstanceState() {

		Parcelable superState = super.onSaveInstanceState();

		SavedState saveState = new SavedState(superState);

		saveState.textX = textX;

		saveState.isRunning = isRunning;

		return saveState;

	}

	// 从state中读取上次保存的数据

	@Override
	public void onRestoreInstanceState(Parcelable state) {

		if (!(state instanceof SavedState)) {

			super.onRestoreInstanceState(state);

			return;

		}

		SavedState saveState = (SavedState) state;

		super.onRestoreInstanceState(saveState.getSuperState());

		textX = saveState.textX;

		isRunning = saveState.isRunning;

	}

	public static class SavedState extends BaseSavedState

	{

		public boolean isRunning = false;

		public float textX = 0.0f;

		// @param superState 提供将数据写入Parcel的接口

		public SavedState(Parcelable superState) {

			super(superState);

		}

		// 写入isStarting和文本横坐标数据到Parcel

		// @param out Parcel：承装数据的容器

		// @param flags

		@Override
		public void writeToParcel(Parcel out, int flags) {

			super.writeToParcel(out, flags);

			out.writeBooleanArray(new boolean[] { isRunning });

			out.writeFloat(textX);

		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			public SavedState[] newArray(int size) {

				return new SavedState[size];

			}

			@Override
			public SavedState createFromParcel(Parcel in) {

				return new SavedState(in);

			}

		};

		// 将in中保存的isRunning，textX读出来

		// @param in

		private SavedState(Parcel in) {

			super(in);

			boolean[] b = null;

			in.readBooleanArray(b);

			if (b != null && b.length > 0)

				isRunning = b[0];

			textX = in.readFloat();

		}

	}

	// 开始滚动

	public void startScroll() {

		isRunning = true;

		// 重画View

		invalidate();

	}

	// 停止滚动

	public void stopScroll() {

		isRunning = false;

		// 重画View

		invalidate();

	}

	@Override
	public void onDraw(Canvas canvas) {

		// 从A点开始显示文本，横坐标依次减小

		canvas.drawText(text, temp1 - textX, textY, paint);

		if (!isRunning) {

			return;

		}

		textX += 0.5;

		// 当文本走到A2点时候

		if (textX > temp2)

			textX = textLength;

		// 重画View

		invalidate();

	}

	@Override
	public void onClick(View v) {

		if (isRunning)

			stopScroll();

		else

			startScroll();

	}

}
