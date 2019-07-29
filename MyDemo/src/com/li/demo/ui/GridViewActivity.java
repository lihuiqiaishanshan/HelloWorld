package com.li.demo.ui;

import iapp.eric.utils.base.Trace;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.li.demo.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewActivity extends Activity{
	int [] mcurlist = {R.drawable.ic_launcher,R.drawable.ic_launcher,R.drawable.ic_launcher};
	ViewHolder holder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gridview);
		GridView mGrid = (GridView) findViewById(R.id.gridView);
		mGrid.setAdapter(new PhotoFileAdapter());
		mGrid.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				Trace.Info("###keycode");
				return false;
			}
		});
		
	}
	
	
	
	/**
	 * 
	 * 
	 * 
	 * Created on: 2013-6-7
	 * 
	 * @brief 文件列表gridview 适配器
	 * @author Li Huiqi
	 * @date Lastest modified on: 2013-6-7
	 * @version V1.0.0.00
	 * 
	 */

	public class PhotoFileAdapter extends BaseAdapter {

		public ExecutorService executorService = Executors
				.newFixedThreadPool(10);

		public PhotoFileAdapter() {
		}

		@Override
		public int getCount() {
			return mcurlist.length;
		}

		@Override
		public Object getItem(int position) {
			return mcurlist[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				holder = new ViewHolder();
				LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
				convertView = mInflater.inflate(R.layout.file_pop_item, null);

				holder.img = (ImageView) convertView
						.findViewById(R.id.file_pop_item_img);

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();

			}
			holder.img.setImageResource(R.drawable.music_listbggreen_selector);

			return convertView;
		}
	}

	public final class ViewHolder {

		public ImageView img;

	}

}
