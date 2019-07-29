package com.rockitv.android;

interface IRemote {
      
      //根据频道得到当前频道所播放内容的相关视频列表，返回一个json格式的文本
      //json格式如下{"title","当前所播放节目标题","videos":[{"title","相关视频标题","img","相关视频图片","url":"相关视频网址"},....]}
      String getVideoByChannel(String channel);
     
      //根据频道得到当前频道的EPG，返回一个json格式的文本
      //json格式如下{"time","当前时间","epg":[{"节目时间\名称"},....]}
       String getEpgByChannel(String channel);
   
}
