package com.konka.multimedia.modules.music;  
interface IRemote{  
//当前时间
long getPlayerTime();
//歌曲名称  
String getTitle();
//歌手名称
String getSinger();
//音乐时长
long getDulcation();
//是否暂停 
boolean isPause();
//是否启动 
boolean isStart();

Bitmap getBit();
} 
