package com.li.demo.map;

import java.util.ArrayList;
import com.li.demo.R;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.li.demo.model.GPSPonit;

/**
 * 在地图上标注已知GPS纬度经度值的一组建筑物
 * 
 * @author android_ls
 * 
 */
public class BaiduMapOverlayItemsActivity extends Activity {

	/** 地图引擎管理类 */
	private BMapManager mBMapManager = null;

	/** 显示地图的View */
	public static MapView mMapView = null;

	/**
	 * 经研究发现在申请KEY时：应用名称一定要写成my_app_应用名（也就是说"my_app_"是必须要有的）。
	 * 百度地图SDK提供的服务是免费的，接口无使用次数限制。您需先申请密钥（key)，才可使用该套SDK。
	 * */
	public static final String BAIDU_MAP_KEY = "AEfbd27dfc411bcdc05953f98a6f3175";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 注意：请在调用setContentView前初始化BMapManager对象，否则会报错
		mBMapManager = new BMapManager(this.getApplicationContext());
		mBMapManager.init(BAIDU_MAP_KEY, new MKGeneralListener() {

			@Override
			public void onGetNetworkState(int iError) {
				if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
					Toast.makeText(
							BaiduMapOverlayItemsActivity.this
									.getApplicationContext(), "您的网络出错啦！",
							Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onGetPermissionState(int iError) {
				if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
					// 授权Key错误：
					Toast.makeText(
							BaiduMapOverlayItemsActivity.this
									.getApplicationContext(),
							"请在 DemoApplication.java文件输入正确的授权Key！",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		setContentView(R.layout.activity_baidumap);

		mMapView = (MapView) this.findViewById(R.id.bmapsView);
		// 设置启用内置的缩放控件
		mMapView.setBuiltInZoomControls(true);

		// 获取地图控制器，可以用它控制平移和缩放
		MapController mMapController = mMapView.getController();
		// 设置地图的缩放级别。 这个值的取值范围是[3,18]。
		mMapController.setZoom(13);

		// TODO 构建一组数据
		GPSPonit gp1 = new GPSPonit(39.90923, 116.397428);
		GPSPonit gp2 = new GPSPonit(39.9022, 116.3922);
		GPSPonit gp3 = new GPSPonit(39.917723, 116.3722);
		GPSPonit gp4 = new GPSPonit(39.915, 116.404);

		/** 存放GPS纬度、经度值的数组 */
		GPSPonit[] mGPSPonit = new GPSPonit[4];
		mGPSPonit[0] = gp1;
		mGPSPonit[1] = gp2;
		mGPSPonit[2] = gp3;
		mGPSPonit[3] = gp4;

		Drawable drawable = this.getResources()
				.getDrawable(R.drawable.zoom_in2);
		// 创建覆盖物（MyOverlayItem）对象并添加到覆盖物列表中
		mMapView.getOverlays().add(new MyOverlayItem(drawable, mGPSPonit));

		// 刷新地图
		mMapView.refresh();
	}

	class MyOverlayItem extends ItemizedOverlay<OverlayItem> {

		/** 覆盖物列表集合 */
		private ArrayList<OverlayItem> mOverlayList = new ArrayList<OverlayItem>();

		// 场景：假如我们有一组建筑物的GPS经纬度值，想要把这些建筑物在地图上标注出来。

		// 传进来的Drawable对象用于在地图上标注一个地理坐标点
		public MyOverlayItem(Drawable drawable, GPSPonit[] gPSPonit) {
			super(drawable, BaiduMapOverlayItemsActivity.mMapView);

			for (int i = 0; i < gPSPonit.length; i++) {
				GPSPonit gpp = gPSPonit[i];

				GeoPoint geoPoint = new GeoPoint((int) (gpp.getmLat() * 1E6),
						(int) (gpp.getmLon() * 1E6));

				mOverlayList.add(new OverlayItem(geoPoint, "point" + i,
						"point1" + i));
			}
			
		}

		/*
		 * 返回的是从指定List集合中，取出的一个OverlayItem对象。 mOverlayList集合里一旦有了数据，在调用其之前，
		 * 一定的在MyOverlayItem的构造函数里调用这个方法populate();
		 */
		@Override
		protected OverlayItem createItem(int index) {
			return mOverlayList.get(index);
		}

		@Override
		public int size() {
			return mOverlayList.size();
		}

	}

	// 重写以下方法，管理API
	@Override
	protected void onResume() {
		mMapView.onResume();
		if (mBMapManager != null) {
			mBMapManager.start();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		if (mBMapManager != null) {
			mBMapManager.stop();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		if (mBMapManager != null) {
			mBMapManager.destroy();
			mBMapManager = null;
		}
		super.onDestroy();
	}

}
