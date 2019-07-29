package com.li.demo;

import com.konka.advert.KKAdManager;
import com.konka.advert.data.DeviceType;

import iapp.eric.utils.base.Trace;
import android.app.Application;


/**

 */
public class GlobalData extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        Trace.setTag("lhq");
        Trace.Info("### onCreate");
        KKAdManager.init(getApplicationContext(), "30051", "97f7b3d589957399f21fbf2a53fd5be11e7d99d3", "KONKA", DeviceType.TV);
        

        //初始化打印输出的TAG
//		Trace.setFilter(Trace.TRACE_FATAL | Trace.TRACE_WARNING | Trace.TRACE_INFO);

    }





    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }


}
