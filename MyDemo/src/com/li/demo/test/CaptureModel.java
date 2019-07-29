package com.li.demo.test;

import com.li.demo.test.CaptureManager.PictureCallback;


public class CaptureModel {
	
	private int height;
	private int width;
	private PictureCallback listener;
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
	public PictureCallback getListener() {
		return listener;
	}
	public void setListener(PictureCallback listener) {
		this.listener = listener;
	}
	

}
