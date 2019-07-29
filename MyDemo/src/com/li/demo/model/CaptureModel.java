package com.li.demo.model;

import com.li.demo.service.PictureListener;
import com.li.demo.test.CaptureManager.PictureCallback;


public class CaptureModel {
	
	private int height;
	private int width;
	private PictureListener listener;
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public PictureListener getListener() {
		return listener;
	}
	public void setListener(PictureListener listener) {
		this.listener = listener;
	}
	

}
