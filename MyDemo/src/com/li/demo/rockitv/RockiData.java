/**
 * 
 */
package com.li.demo.rockitv;
import android.os.AsyncTask;
import android.os.RemoteException;
import com.rockitv.android.IRemote;

/** 
 * Created on: 2013-11-7 
 * @brief what to do 
 * @author Eric Fung 
 * @date Latest modified on: 2013-11-7 
 * @version V1.0.00 
 *  
 */
public class RockiData {
	private String mepgjson = null;
	private String mvideojson = null;
	private IRemote mremote = null;
	private JsonListen mlisten = null;
	public RockiData(IRemote remote){
		mremote = remote;
	}
	public void GetEpgAndVideoData(final String tvkey,final JsonListen json){
		if(mremote!=null){
			mlisten = json;
			new GetDataTask().execute(tvkey);
		}
	}
	private class GetDataTask extends AsyncTask<String, String, String>{
		@Override
		protected String doInBackground(String... params) {
			 try {
				 mepgjson = mremote.getEpgByChannel(params[0]);
				 mvideojson = mremote.getVideoByChannel(params[0]);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			if(mlisten!=null){
				mlisten.getJson(mepgjson, mvideojson);
			}
		}
	}
	
}
