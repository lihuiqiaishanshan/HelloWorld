package com.li.demo.ui;

import com.li.demo.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ManageActivity extends Activity {
	private LinearLayout[] mManageView = new LinearLayout[10];
	private ImageView[] mManageIcon = new ImageView[10];
	private TextView[] mManageType = new TextView[10];
	private TextView[] mManageUser = new TextView[10];
	private Button[] mManageRelerance = new Button[10];
	private int[] mManageViewId = { R.id.manage_diskView1,
			R.id.manage_diskView2, R.id.manage_diskView3,
			R.id.manage_diskView4, R.id.manage_diskView5,
			R.id.manage_diskView6, R.id.manage_diskView7,
			R.id.manage_diskView8, R.id.manage_diskView9,
			R.id.manage_diskView10 };
	private int[] mManageIconId = { R.id.manage_diskImage1,
			R.id.manage_diskImage2, R.id.manage_diskImage3,
			R.id.manage_diskImage4, R.id.manage_diskImage5,
			R.id.manage_diskImage6, R.id.manage_diskImage7,
			R.id.manage_diskImage8, R.id.manage_diskImage9,
			R.id.manage_diskImage10 };
	private int[] mManageTypeId = { R.id.manage_diskType1,
			R.id.manage_diskType2, R.id.manage_diskType3,
			R.id.manage_diskType4, R.id.manage_diskType5,
			R.id.manage_diskType6, R.id.manage_diskType7,
			R.id.manage_diskType8, R.id.manage_diskType9,
			R.id.manage_diskType10 };
	private int[] mManageUserId = { R.id.manage_diskUser1,
			R.id.manage_diskUser2, R.id.manage_diskUser3,
			R.id.manage_diskUser4, R.id.manage_diskUser5,
			R.id.manage_diskUser6, R.id.manage_diskUser7,
			R.id.manage_diskUser8, R.id.manage_diskUser9,
			R.id.manage_diskUser10 };
	private int[] mManageReleranceId = { R.id.manage_button_relevance1,
			R.id.manage_button_relevance2, R.id.manage_button_relevance3,
			R.id.manage_button_relevance4, R.id.manage_button_relevance5,
			R.id.manage_button_relevance6, R.id.manage_button_relevance7,
			R.id.manage_button_relevance8, R.id.manage_button_relevance9,
			R.id.manage_button_relevance10};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.choose_disk);
		initView();
		inflateView();

	}
	private void inflateView(){
		int count = 2;
		setManageViewGone();
		setManageViewVisible(count);
		for(int i = 0;i<count;i++){
			mManageIcon[i].setImageResource(R.drawable.ic_launcher);
			mManageType[i].setText("华为网盘");
			mManageUser[i].setText("lihuiqi239@126.com");
		}
		
		
		
	}
	//param 需要显示多少个盘
	private void setManageViewVisible(int count){
		for(int i = 0;i<count;i++){
			mManageView[i].setVisibility(View.VISIBLE);
		}
	}
	
	private void setManageViewGone(){
		for(int i = 0;i<10;i++){
			mManageView[i].setVisibility(View.GONE);
		}
		
	}

	private void initView(){
		for(int i = 0; i<10;i++){
			mManageView[i] = (LinearLayout) findViewById(mManageViewId[i]);
			mManageIcon[i] = (ImageView) findViewById(mManageIconId[i]);
			mManageType[i] = (TextView) findViewById(mManageTypeId[i]);
			mManageUser[i] = (TextView) findViewById(mManageUserId[i]);
			mManageRelerance[i] = (Button) findViewById(mManageReleranceId[i]);
			
		}
	}
}
