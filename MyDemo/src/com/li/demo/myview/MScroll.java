package com.li.demo.myview;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.li.demo.R;
import com.li.demo.myview.BarrageMsg.GroupMsg;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 
 * 显示弹幕的控件
 * 
 * @author Li Huiqi
 * @version 1.0.0
 */
public class MScroll extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "MScroll";

	private int refresh_time; // 滚动刷新的时间间隔
	private int scroll_pixel; // 滚动刷新的间距

	private boolean runFlag = true;
	protected SurfaceHolder holder;
	private Thread myThread;
	private int maxWidth;// View长度，即滚动的起点

	private float mTextSize;

	private int mBackGround = 0;

	private String currentText = "";
	private ArrayList<String> text_list;// 待显示的文本列表
	private List<BarrageMsg> barrageMsgList = new ArrayList<BarrageMsg>();//存储弹幕的list，正是环境后将text_list替换
	//指定弹幕行数
	private static final int row = 5;
	
	private int [] rows = new int[row];
	
	private float [] textSizes = {20,40,32,30,16};
	
	private int [] textColors = {Color.WHITE,Color.GREEN,Color.BLUE,Color.YELLOW};
	
	public MScroll(Context context) {
		super(context);
		Log.d(TAG, "MScroll");
		holder = this.getHolder();
		holder.addCallback(this);
		holder.setFormat(PixelFormat.TRANSPARENT); // 顶层绘制SurfaceView设成透明
		setBackgroundResource(mBackGround);
		this.setZOrderOnTop(true);
		for(int i = 0;i<rows.length;i++){
			rows[i] = 0;
		}
	}

	boolean isAdd = true;
	private List<DanMuObj> mList = null;
	private int index = 0;

	/* 自定义线程 */
	class MyRunnable implements Runnable {
		private Paint paint = null;
		

		public void run() {
			Canvas canvas = null;

			// // 整个空间的宽度
			while (runFlag) {
				try {
					if (paint == null) {
						initPaint();
						mList = new ArrayList<MScroll.DanMuObj>();
						maxWidth = MScroll.this.getWidth();
					}
					long time = 0;
					long startTime = System.currentTimeMillis();
					canvas = holder.lockCanvas(new Rect(0, 0, MScroll.this
							.getWidth(), MScroll.this.getHeight())); // 获取画布
					if (canvas != null) { // 退出时holder.lockCanvas（）方法可能返回空，未免报空指针异常
						// 清除画布方法一
						canvas.drawColor(Color.TRANSPARENT,
								PorterDuff.Mode.CLEAR);

			 

						if (mList.size() != 0) {
							for (int i = index; i < mList.size(); i++) {
								mHandler.removeMessages(clearListMsg);
								DanMuObj dm = mList.get(i);
								List<Info> list = dm.list;
								for(Info info:list){
									//画文字
									if(info.getType() == 0){
										//颜色
	//									paint.setColor(textColors[dm.color]);
										//字体
										paint.setTextSize(textSizes[dm.textSize]);
										
							            // 设定颜色    
										paint.setColor(0xFFFFFF00);   
										// 设定阴影(柔边, X 轴位移, Y 轴位移, 阴影颜色)    
										paint.setShadowLayer(5, 3, 3, 0xFFFF00FF);   

										canvas.drawText(info.getText(), 0, info.getText()
												.length(), dm.scrollX, dm.scrollY+textSizes[dm.textSize], paint);
										float textWidth = paint.measureText(info.getText());
										dm.width = (int)textWidth ;
									}

									// 画表情
									if(info.getType() == 1){
										canvas.drawBitmap(info.getBm(), dm.scrollX+dm.width+20,dm.scrollY+10, paint);
										dm.width += info.getBm().getWidth()+10;
									}
									
									Log.d(TAG, "###dm.srollX" + dm.scrollX);
									Log.d(TAG, "###maxWidth" + maxWidth);
									Log.d(TAG, "###dm.width" + dm.width);
								}
								dm.scrollX -= scroll_pixel;
								if (dm.width + dm.scrollX < maxWidth - 50) {// 保证前后有50个距离的间隙
									Log.d(TAG, "###dmrow" + dm.row);
									if(!dm.flag){
										rows[dm.row]= 0;
										dm.flag = true;
									}
								}

								if (dm.scrollX + dm.width < 0) {
									dm.list.clear();
									// dm = null;
									index = i + 1;
									mHandler.removeMessages(clearListMsg);
									mHandler.sendEmptyMessageDelayed(0, 10000);
								}
							}
						}
						
						if(rows[0] == 1 && rows[1]==1&&rows[2]==1&&rows[3] == 1&& rows[4] ==1){
							isAdd = false;
						}else{
							isAdd = true;
						}

						findNext();

						holder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
						
						long endTime = System.currentTimeMillis();
						time = (endTime-startTime);
//						if(time > 40){
//							Log.d(TAG, "###fail");
//						}

					}
					Thread.sleep(refresh_time);
				} catch (InterruptedException e) {
					Log.e(TAG, "###InterruptedException");
					e.printStackTrace();
				} catch (Exception e) {
					Log.e(TAG, "###Exception");
					e.printStackTrace();
				}
			}
		}

		/**
		 * 初始化paint
		 */
		private void initPaint() {
			paint = new Paint();
			initFont(paint);
		}

		// 找到下一个文本。
		void findNext() {
			if (barrageMsgList.size() != 0) {
				if (mList.size() == 0 || isAdd) {
					int row = findRandom();
					
					BarrageMsg bMsg = barrageMsgList.get(0);
					DanMuObj dm = new DanMuObj();
					dm.color = new Random().nextInt(4);
					dm.textSize = new Random().nextInt(3);
					dm.avatarUrl = bMsg.avatarUrl;
					dm.groupId = bMsg.groupId;
					dm.groupName = bMsg.groupName;
					dm.groupNickName = bMsg.groupNickName;
					dm.nickName = bMsg.nickName;
					ArrayList<GroupMsg> msgList = bMsg.msgList;
					List<Info> list = new ArrayList<MScroll.Info>();
					for(GroupMsg gm : msgList){
						Info info = new Info();
						if(gm.msgType == 1){
							info.setText(gm.msgContent);
							info.setType(0);
							list.add(info);
						}
						if(gm.msgType == 2){
							info.setBm(String2Bm(gm.msgContent));
							info.setType(1);
							list.add(info);
						}
					}
					dm.setScrollX(maxWidth);
					dm.list = list;

					dm.row = row;
					Log.d(TAG, "###ablehehe" + row);
					rows[row] = 1;
					dm.scrollY = 100*(row);
					mList.add(dm);
					barrageMsgList.remove(0);
				}
			}
		}
		
		//将内容转化为bitmap，预留接口
		
		Bitmap String2Bm(String text){
			Bitmap bm = BitmapFactory.decodeResource(
					getResources(), R.drawable.emoji_000);
			return bm;
		}
		
		int findRandom(){
			List<String> l = new ArrayList<String>();
			for(int i = 0;i<rows.length;i++){
				if(rows[i] == 0){
					l.add(i+"");
				}
			}
			Log.d(TAG, "###ablelength" + l.size());
			if(l.size() == 1){
				return Integer.parseInt(l.get(0));
			}else if(l.size() > 1){
				int i = new Random().nextInt(l.size());
				return Integer.parseInt(l.get(i));
			}
			return 0;
		}

		/**
		 * 设置TextView的字体
		 * 
		 * @param paint
		 *            textview控件
		 * @param font
		 *            字体
		 */
		void initFont(Paint paint) {
			// paint.setTextAlign(Align.RIGHT);
			// 大小
			paint.setTextSize(mTextSize);
			// 字体
			Typeface typeface = Typeface.DEFAULT;
			paint.setTypeface(typeface);
			// 颜色
			paint.setColor(000000);
			
			paint.setAntiAlias(true);
			
//			//是否使用图像抖动处理
//			paint.setDither(true);

		}

	}

	@Override
	/**
	 * 当控件创建时自动执行的方法
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		// 启动自定义线程
		myThread = new Thread(new MyRunnable());
		runFlag = true;
		myThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	/**
	 * 当控件销毁时自动执行的方法
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		// 终止自定义线程
		runFlag = false;
		myThread.interrupt();
	}

	public int getScroll_pixel() {
		return scroll_pixel;
	}

	public void setScroll_pixel(int scroll_pixel) {
		this.scroll_pixel = scroll_pixel;
	}

	public String getText() {
		return currentText;
	}

	public void setText(String text) {
		this.currentText = text;
	}

	public long getRefresh_time() {
		return refresh_time;
	}

	public void setRefresh_time(int refresh_time) {
		this.refresh_time = refresh_time;
	}

	public ArrayList<String> getText_list() {
		return text_list;
	}

	public void setText_list(ArrayList<String> text_list) {
		this.text_list = text_list;
	}

	public void addText_list(ArrayList<String> text_list) {
		this.text_list.addAll(text_list);
	}

	public float getmTextSize() {
		return mTextSize;
	}

	public void setmTextSize(float mTextSize) {
		this.mTextSize = mTextSize;
	}

	public int getmBackGround() {
		return mBackGround;
	}

	public void setmBackGround(int mBackGround) {
		this.mBackGround = mBackGround;
	}

	class DanMuObj {
		public int scrollX = 0;
		public int scrollY = 0;
		public boolean canAdd = false;
		public int width;
		public int row = 0;
		public boolean flag = false;
		
		public int color = 0;
		
		private int textSize = 0;
		
		public List<Info> list;
		
		//合入BarrageMsg所需的数据
	    public long groupId; //群组ID
	    
	    public String groupName; //群组名称
	    
	    public String nickName; //发送者昵称
	    
	    public String groupNickName; //群名片
	    
	    public String avatarUrl = "http://q.qlogo.cn/qqapp/222222/5E47CBFC315E02CA4A464D70A35AED5D/100"; //头像URL

		public int getScrollX() {
			return scrollX;
		}

		public void setScrollX(int srollX) {
			this.scrollX = srollX;
		}
	}
	
	
	class Info{
		private Bitmap bm;
		
		private String text;
		//默认是文字0
		private int type = 0;
		public Bitmap getBm() {
			return bm;
		}
		public void setBm(Bitmap bm) {
			this.bm = bm;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		
	}

	// 使用该方法会一直不断累计list，找一个时机清空list
	private static final int clearListMsg = 0;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case clearListMsg:
				Log.d(TAG, "###清空list");
				mList.clear();
				index = 0;
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	public void addBMsg(BarrageMsg bMsg) {
		barrageMsgList.add(bMsg);
	}
	
	

}