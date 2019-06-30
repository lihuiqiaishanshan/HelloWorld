package com.hisilicon.tvui.installtion;

import com.hisilicon.dtv.network.Network;

public interface IScanMainWnd
{
    int MSG_ID_NEXT_STEP     = 100;
    int MSG_ID_READY_TO_SCAN = 101;
    int MSG_ID_STOP_SCAN = 102;
    /**
     * To the main window to send a message, for example, click the search button, the main window
     * displays the search settings, start the search schedule interface. <br>
     * CN: 向主窗口发送消息，例如点击了搜台按钮，交给主窗口显示搜台设置条件，启动搜台进度界面. <br>
     *
     * @param messageID the message id. now is MSG_ID_READY_TO_SCAN .<br>
     *        CN: 消息id,现在值是MSG_ID_READY_TO_SCAN .<br>
     *
     * @param obj Extended parameter, temporarily not used .<br>
     *        CN: 扩充参数，暂时没有使用.<br>
     */
    void sendMessage(int messageID, Object obj);

    /**
     * Set the current are using network objects (mainly considering satellite handover has some
     * time, at the same time for the user experience, in the open interface, the default open
     * recent satellite data using. <br>
     * CN: 设置当前正在使用的网络对象(主要考虑到卫星切换存在一定时间，同时为了用户体验，在打开其他界面时，默认打开最近使用的卫星数据). <br>
     *
     * @param Currently used by the network object .<br>
     *        CN:network 当前正在使用的网络对象 .<br>
     */
    void setCrtSelectedNetwor(Network network);

    /**
     * Gets the object in the current use of Network. <br>
     * CN: 获取当前使用的网络对象. <br>
     *
     * @return network The current Network object,null is no .<br>
     *         CN:network 当前网络对象 .<br>
     */
    Network getCrtSelectedNetwork();
}
