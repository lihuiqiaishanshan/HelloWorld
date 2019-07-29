package com.li.demo.model;

import java.util.List;

public class Videos {
	private String name;
	
	private List<Video> videoList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Video> getVideoList() {
		return videoList;
	}

	public void setVideoList(List<Video> videoList) {
		this.videoList = videoList;
	}
}
