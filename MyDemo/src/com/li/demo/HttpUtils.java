package com.li.demo;

import iapp.eric.utils.base.Trace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author liliang
 * @date 2012-11-28
 * @desc http相关辅助类
 * 
 */
public class HttpUtils {
	private static final String TAG = "HttpUtils";
	public static final int TIMEOUT = 15000;
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";

	public static final String CODE = "code";
	public static final String RETURN = "return";

	public static final String CODE_OK_STR = "200";
	public static final int CODE_OK = 200;

	// http get 请求
	public static Map<String, String> doGet(String baseUrl,
			TreeMap<String, String> paramMap)  {
		
		Map<String, String> resultMap = new HashMap<String, String>();
		try {

			URL url = new URL(baseUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(TIMEOUT);
			con.setReadTimeout(TIMEOUT);
			con.setRequestMethod(METHOD_GET);
			/*
			 * if (con.getResponseCode() == 302) { //deal with redirect 302
			 * return null; }
			 */
			
			InputStream is = null;
			try {
				is = con.getInputStream();
			} catch (IOException e) {
				is = con.getErrorStream();
			}

			int statusCode = con.getResponseCode();
			resultMap.put(CODE, String.valueOf(statusCode));
			if(null != is){
				String content = getContent(is);
				resultMap.put(RETURN, content);
			}


			con.disconnect();

		} catch (MalformedURLException e) {
			// TODO: handle exception
		}catch (IOException e) {
			Trace.Info("SocketTimeoutException ffffffffffffffffff");
			// TODO: handle exception
		}

		return resultMap;
	}

	public static String getContent(InputStream is)
			throws UnsupportedEncodingException {
		if(is == null)
			return null;
		return new String(getContentBytes(is), "UTF-8");
	}

	public static byte[] getContentBytes(InputStream is)
			throws UnsupportedEncodingException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int len = 0;
		try {
			while ((len = is.read(buf)) != -1) {
				os.write(buf, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return os.toByteArray();
	}

}
