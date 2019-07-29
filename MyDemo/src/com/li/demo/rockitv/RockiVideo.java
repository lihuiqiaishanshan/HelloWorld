package com.li.demo.rockitv;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import iapp.eric.utils.base.Trace;
import com.li.demo.model.Video;
import com.rockitv.android.IRemote;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;

/**
 * 
 * Created on: 2014-5-9
 * 
 * @brief 获取一点视频相关类
 * @author Li Huiqi
 * @date Lastest modified on: 2014-5-9
 * @version V1.0.0.00
 * 
 */
public class RockiVideo {

	private static RockiVideo instance = null;

	private RockiVideo() {

	}

	public static RockiVideo getInstance() {
		if (null == instance) {
			instance = new RockiVideo();
		}
		return instance;
	}

	private IRemote mRemote = null;

	private VideoModel video = null;

	private rockiBindListener listener;

	public void bindService(Context context, rockiBindListener listener) {
		/* 绑定一点智能推荐的远程服务 * */
		Intent intent = new Intent();
		intent.setClassName("com.rockitv.android", "com.rockitv.android.RemoteService");
		intent.setAction("com.rockitv.android.Remote_BIND");
		context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		this.listener = listener;
		/* end bind */
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		// Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			mRemote = IRemote.Stub.asInterface(service);
			listener.onBind();
			Trace.Info("###bind adot sucess");
		}

		// Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
			Trace.Debug("remote binder faile");
			mRemote = null;
		}
	};

	public void getVideoList(final Context context,String tvName, final RockiVideoListener listener) {
		RockiData mrocki = new RockiData(mRemote);
		mrocki.GetEpgAndVideoData(tvName, new JsonListen() {

			@Override
			public void getJson(String epgjson, String videojson) {
				// TODO Auto-generated method stub
				if (videojson != null) {
					video = RockiTvJson.getVideoInfo(videojson);
					Trace.Info("evideo_json=" + videojson);
					List<Video> list = new ArrayList<Video>();
					for (VideoNode vn : video.videolist) {
						Video v = getTitleAndImg(context, vn);
						list.add(v);
					}
					listener.onVideo(list);
				} else {
					Trace.Info("###lhq videojson = null");
					return;
				}
				
				if (epgjson != null) {
					Trace.Info("epg_json=" + epgjson);
				}
			}
		});
	}

	public void unBindService(Context context) {
		if (mConnection != null) {
			context.unbindService(mConnection);
		}
	}

	public void playVideo(Context context, String url) {
		Intent intent = new Intent();
		intent.setClassName("com.rockitv.android", "com.rockitv.android.ui.AdotPlayer");
		intent.setData(Uri.parse(url)); // url即为返回的智能推荐结果JSON中每个视频的url字段
		context.startActivity(intent);
	}

	public Video getTitleAndImg(Context context, VideoNode vn) {
		Video video = new Video();
		if(vn.url.startsWith("http://t.rockitv.com")){
			video.setName(vn.title);
			video.setPicurl(vn.ima);
			video.setSummary("");
			video.setUrl(vn.url);
		}
		Trace.Info("###hehe" + vn.url);
		Trace.Info("###hehe" + vn.title);
		String urlStr = "http://cgi.connect.qq.com/qqconnectopen/get_urlinfoForQQ?xmlout=1&url=";
		String url = vn.url;
		Map<String, String> map = HttpUtils.doGet(urlStr+url, null);
		String content = map.get(HttpUtils.RETURN);
		video.setUrl(url);
		if (null != content && !content.equals("")) {
			Trace.Info("###得到xml开始解析" + content);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			XMLReader reader;
			try {
				reader = factory.newSAXParser().getXMLReader();
				XmlContentHandler xml = new XmlContentHandler();
				xml.setVideo(video);
				reader.setContentHandler(xml);
				reader.parse(new InputSource(new StringReader(content)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return video;
	}
}
