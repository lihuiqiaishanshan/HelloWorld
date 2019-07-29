package com.li.demo;

import java.util.ArrayList;
import java.util.List;

import com.konka.android.storage.KKStorageManager;
import com.li.demo.model.LocalDiskInfo;
import com.mstar.android.tvapi.common.AudioManager;

import android.content.Context;
import android.os.Environment;

/**
 * 
 * Created on: 2013-4-10
 * @brief 项目内通用的工具类
 * @author Eric Fung
 * @date Latest modified on: 2013-4-10
 * @version V1.0.00
 *
 */
public class Utils {
	/**
	 * @author Li HuiQi
	 * @brief 按照设备将dp转化为px值
	 * @param context
	 * @param dpValue
	 * @return int
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}	
	
	
	/**
	 * @brief 获取外部存储设备列表
	 * @param context
	 *            上下文
	 * @return 外部存储设备列表
	 */
	public static ArrayList<LocalDiskInfo> getExternalStorage(Context context) {
		ArrayList<LocalDiskInfo> sUsbList = new ArrayList<LocalDiskInfo>();
		KKStorageManager kksm = KKStorageManager.getInstance(context);
		String[] volumes = kksm.getVolumePaths();
		if (volumes == null) {
			return null;
		}

		for (int i = 0; i < volumes.length; ++i) {
			String state = kksm.getVolumeState(volumes[i]);

			if (state == null || !state.equals(Environment.MEDIA_MOUNTED)) {
				continue;
			}
			sUsbList.add(new LocalDiskInfo(volumes[i], kksm
					.getVolumeLabel(volumes[i])));
		}
		return sUsbList;
	}
}
