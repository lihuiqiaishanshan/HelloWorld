package com.li.demo.map;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.li.demo.R;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;

public class MapDemo extends Activity {
	BMapManager mBMapMan = null;
	MapView mMapView = null;
	MapController mMapController = null;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			mMapController.setZoom(10);
			super.handleMessage(msg);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init("AEfbd27dfc411bcdc05953f98a6f3175", null);
		// 注意：请在试用setContentView前初始化BMapManager对象，否则会报错
		setContentView(R.layout.activity_baidumap);
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mMapView.setBuiltInZoomControls(true);
		// 设置启用内置的缩放控件
		mMapController = mMapView.getController();
		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		GeoPoint point = new GeoPoint((int) (22.551666 * 1E6),
				(int) (113.92516 * 1E6));
		// 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
		mMapController.setCenter(point);// 设置地图中心点
		mMapController.setZoom(12);// 设置地图zoom级别
		
		
		/**
		 *  在想要添加Overlay的地方使用以下代码，
		 *  比如Activity的onCreate()中
		 */
		//准备overlay图像数据，根据实情情况修复
		Drawable mark= getResources().getDrawable(R.drawable.zoom_in2);
		//用OverlayItem准备Overlay数据
		OverlayItem item1 = new OverlayItem(point,"item1","item1");
		//使用setMarker()方法设置overlay图片,如果不设置则使用构建ItemizedOverlay时的默认设置
		 
		//创建IteminizedOverlay
		OverlayTest itemOverlay = new OverlayTest(mark, mMapView);
		//将IteminizedOverlay添加到MapView中
	
		mMapView.getOverlays().clear();
		mMapView.getOverlays().add(itemOverlay);
		 
		//现在所有准备工作已准备好，使用以下方法管理overlay.
		//添加overlay, 当批量添加Overlay时使用addItem(List<OverlayItem>)效率更高
		itemOverlay.addItem(item1);
		mMapView.refresh();
		//删除overlay .
		//itemOverlay.removeItem(itemOverlay.getItem(0));
		//mMapView.refresh();
		//清除overlay
		// itemOverlay.removeAll();
		// mMapView.refresh();

	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		if (mBMapMan != null) {
			mBMapMan.start();
		}
		super.onResume();
	}
	
	/*
	 * 要处理overlay点击事件时需要继承ItemizedOverlay
	 * 不处理点击事件时可直接生成ItemizedOverlay.
	 */
	class OverlayTest extends ItemizedOverlay<OverlayItem> {
	    //用MapView构造ItemizedOverlay
	    public OverlayTest(Drawable mark,MapView mapView){
	            super(mark,mapView);
	    }
	    protected boolean onTap(int index) {
	        //在此处理item点击事件
	        System.out.println("item onTap: "+index);
	        return true;
	    }
	        public boolean onTap(GeoPoint pt, MapView mapView){
	                //在此处理MapView的点击事件，当返回 true时
	                super.onTap(pt,mapView);
	                return false;
	        }

	}   
}
