package com.li.demo.rockitv;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.li.demo.model.Video;


/**
 * 
 * Created on: 2013-6-4
 * @brief XML解析工具类，SAX方法解析XML
 * @author Li Huiqi
 * @date Lastest modified on:  2013-6-4
 * @version V1.0.0.00
 *
 */
public class XmlContentHandler extends DefaultHandler {
	private static final String PICS = "pics";
	private static final String TITLE = "title";
	private static final String SUMMARY = "summary";
	private String mName = "";
	private Video video = null;
	
	private StringBuilder sb = new StringBuilder("");
	
	

	public Video getVideo() {
		return video;
	}

	public void setVideo(Video video) {
		this.video = video;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		if(mName.equalsIgnoreCase(PICS)){
			String pics = new String(ch, start, length);
			video.setPicurl(pics);
			Trace.Info("###haha1" + pics.trim());
		}
		if(mName.equalsIgnoreCase(TITLE)){
			String title = new String(ch, start, length);
			video.setName(title);
			Trace.Info("###haha" + title.trim());
		}
		if(mName.equalsIgnoreCase(SUMMARY)){
			String summary = new String(ch, start, length);
			sb.append(summary.trim());
		}
		super.characters(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		video.setSummary(sb.toString());
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		super.endElement(uri, localName, qName);
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
//		if(localName.equals(tag)){
//			map = new HashMap<String, String>();
//			for(int i=0;i<attributes.getLength();i++){
//				String name = attributes.getLocalName(i);
//				String value = attributes.getValue(i);
//				map.put(name, value);
//			}
//			uwiList.add(map);
//			
//		}
		mName = localName;
		super.startElement(uri, localName, qName, attributes);
	}
}
