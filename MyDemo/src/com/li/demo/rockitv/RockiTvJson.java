/**
 * 
 */
package com.li.demo.rockitv;

import iapp.eric.utils.base.Trace;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** 
 * Created on: 2013-10-23 
 * @brief what to do 
 * @author Eric Fung 
 * @date Latest modified on: 2013-10-23 
 * @version V1.0.00 
 *  
 */
public class RockiTvJson {
	/***
	 * 
	 * @param json
	 * @return EpgModel epg对象
	 */
	public static EpgModel getEpgInfo(String json){
		EpgModel epg = null;
		if(json!=null){
			try {
				epg = new EpgModel();
				int indexOf = json.indexOf("{");
				int lastIndexOf = json.lastIndexOf("}");
				json = json.substring(indexOf,lastIndexOf+1);
				
				JSONObject object = new JSONObject(json);
				if(object.has("time")){
					epg.time = object.getString("time");
				}
				if(object.has("epg")){
					epg.epglist = new LinkedList<String>();
					JSONArray epglist = object.getJSONArray("epg");
					for (int i = 0; i < epglist.length(); i++) {
						epg.epglist.add(epglist.getString(i));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				Trace.Fatal("parse epg json error");
			}
		}
		return epg;
	}
	/***
	 * 
	 * @param json
	 * @return VideoModel
	 */
	public static VideoModel getVideoInfo(String json){
		VideoModel video = null;
		if(json!=null){
			try {
				int indexOf = json.indexOf("{");
				int lastIndexOf = json.lastIndexOf("}");
				json = json.substring(indexOf,lastIndexOf+1);
				video = new VideoModel();
				JSONObject object;
				
				object = new JSONObject(json);
				if(object.has("title")){
					video.title = object.getString("title");
				}
				if(object.has("time")){
					video.time = object.getString("time");
				}
				if(object.has("epg")){
					video.epg = object.getString("epg");
				}
				if(object.has("videos")){
					video.videolist = new LinkedList<VideoNode>();
					JSONArray  array = object.getJSONArray("videos");
					for (int i = 0; i < array.length(); i++) {
						VideoNode node = new VideoNode();
						node.title = array.getJSONObject(i).getString("title");
						node.ima = array.getJSONObject(i).getString("img");
						node.url = array.getJSONObject(i).getString("url");
						video.videolist.add(node);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				Trace.Fatal("parse video json error");
			}
			
		}
		return video;
	}
}
