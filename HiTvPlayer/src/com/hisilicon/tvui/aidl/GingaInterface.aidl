package com.hisilicon.tvui.aidl;
import java.util.List;
interface GingaInterface {
    void open();
    void close();
    List<String> getAppList();
    void startApp(String appName);
}
