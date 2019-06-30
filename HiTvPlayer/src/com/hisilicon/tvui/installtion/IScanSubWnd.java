package com.hisilicon.tvui.installtion;

import android.view.View;

public interface IScanSubWnd
{
    // 按键处理结果
    enum KeyDoResult
    {
        // 已处理结束，无需父窗口处理后续处理
        DO_OVER,
        // 已处理，无需父窗口处理，但需要系统继续处理
        DO_DONE_NEED_SYSTEM,
        // 未处理
        DO_NOTHING,
    }

    // The child window processing message (Fragment window cannot receive the message, need to
    // frame window transfer message), if the message, without subsequent processing, true is
    // returned, otherwise false.<br>
    // CN:子窗口处理按键消息（Fragment窗口无法接收按键消息，需要框架窗口传递按键消息），如果处理了消息，无需后续处理，则返回true,否则为false.
    KeyDoResult keyDispatch(int keyCode, android.view.KeyEvent event, View parent);

    // Search function units each window has, to share this part, here is a frame window receives
    // the search button or child window click on the search button, the window frame according to
    // the function return value to determine whether the push the start search, child window in
    // this function, can determine the starting station search conditions are ripe and set the
    // relevant information..
    // CN:搜台功能每个子窗口都有，将这部分共用，此处是框架窗口收到搜台按键后或子窗口中点击了搜台按钮，框架窗口根据此函数返回值判断是否推进启动搜台，子窗口在此函数里面，可以判断启动搜台条件是否成熟并设置相关信息...
    boolean isCanStartScan();

    // Search function units each window has, to share this part, this function is to confirm the
    // web search or frequency search (especially in DVBS), the frame window according to this
    // function to determine whether to enable the "search mode".
    // CN: 搜台功能每个子窗口都有，将这部分共用，此函数是确认是网络搜索还是频点搜索(主要用在DVBS里面)，框架窗口根据此函数来确定是否启用“搜索模式”...
    boolean isNetworkScan();

    // Set the main window interface.<br>
    // CN:设置主窗口接口.
    void setMainWnd(IScanMainWnd parent);
}
