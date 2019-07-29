package com.li.demo.service;  

import com.li.demo.service.PictureListener;

interface ITakePicRemote{  
void register();
void unregister();
void takePicture(int w,int h,in PictureListener pl);


} 
