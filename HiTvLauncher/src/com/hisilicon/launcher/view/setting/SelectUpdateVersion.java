package com.hisilicon.launcher.view.setting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import com.hisilicon.launcher.util.LogHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.util.TaskUtil;

public class SelectUpdateVersion extends LinearLayout {
    private static final String TAG = "SelectUpdateVersion";
    private static final String DEFAULT_URL = "http://10.67.212.121/image/json/version_dev.json";
    private static final int UPDATE_LIST = 0;
    private Context mContext;
    private SystemUpdateDialog mSystemUpdateDialog;
    private ListView list = null;
    private SimpleAdapter listItemAdapter = null;
    private ArrayList<HashMap<String, Object>> listItem = null;

    private String mVersion = "";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_LIST) {
                list.setAdapter(listItemAdapter);
                listItemAdapter.notifyDataSetChanged();
            }
        }
    };

    public SelectUpdateVersion(Context context) {
        super(context);
         mContext = context;
         LayoutInflater inflater = LayoutInflater.from(context);
         View parent = inflater.inflate(R.layout.select_update_version, this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVersion = "V5R2";
        } else {
            mVersion = "";
        }
         initView(parent, 0);
    }

    private void initView(View parent, int focus) {
        list = (ListView) findViewById(R.id.tv_version_listview);
        listItem = new ArrayList<HashMap<String, Object>>();
        if (isNetworkAvailable(mContext)) {
            getUpdataVersionFromServer();
        } else {
            createDialog((int) mContext.getResources().getDimension(R.dimen.dimen_350px),
                    (int) mContext.getResources().getDimension(R.dimen.dimen_400px), SystemUpdateDialog.NET_NO_UPDATE);
        }

        list.setSelected(true);
        list.requestFocus();
        list.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                LogHelper.d(TAG,  "select posion is " + position);
                String path = (String) listItem.get(position).get("TV_Version_File_path");
                if (path != null) {
                    if (null == mSystemUpdateDialog || !mSystemUpdateDialog.isShowing()) {
                        if (isNetworkAvailable(mContext)) {
                            NetUpdate.setserverFilePath(path);
                            createDialog((int) mContext.getResources().getDimension(R.dimen.dimen_350px),
                                    (int) mContext.getResources().getDimension(R.dimen.dimen_400px), SystemUpdateDialog.NET_HAVE_UPDATE);
                        } else{
                            createDialog((int) mContext.getResources().getDimension(R.dimen.dimen_350px),
                                    (int) mContext.getResources().getDimension(R.dimen.dimen_400px), SystemUpdateDialog.NET_NO_UPDATE);
                        }
                    }
                }
            }
        });
    }

    /**
     * create dialog by height,width and save
     *
     * @param height
     * @param width
     * @param save
     */
    public void createDialog(int height, int width, int save) {
        mSystemUpdateDialog = new SystemUpdateDialog(mContext, save, null);
        mSystemUpdateDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        Window window = mSystemUpdateDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = height;
        lp.width = width;
        window.setAttributes(lp);
        mSystemUpdateDialog.show();

    }
    /**
     * Determine the current network is available
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()) {
            return true;
        } else {
            if (null == cm) {
                return false;
            }
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Hidden child dialog if is showing
     */
    public void dismissChildDialog() {
        if (null != mSystemUpdateDialog && mSystemUpdateDialog.isShowing()) {
            mSystemUpdateDialog.dismiss();
        }
    }

    public void getUpdataVersionFromServer()
    {
        TaskUtil.post(new Runnable() {
            @Override
            public void run() {
                String urlString = SystemProperties.get("persist.launcher.updateurl", DEFAULT_URL);
                JSONObject param = new JSONObject();
                URL url = null;
                HttpURLConnection con = null;
                InputStream inputStream = null;
                ByteArrayOutputStream byteArrayOutputStream = null;
                BufferedInputStream bufferedInputStream = null;
                try {
                    url = new URL(urlString);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setConnectTimeout(5000);
                    inputStream = con.getInputStream();
                    if (inputStream != null) {
                        StringBuilder builder = new StringBuilder();
                        bufferedInputStream = new BufferedInputStream(inputStream);
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = bufferedInputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, len);
                        }
                        String tempString = byteArrayOutputStream.toString();
                        String[] ret = tempString.split("\n");
                        for (int i = 0; i < ret.length; i++) {
                            builder.append(ret[i]);
                        }
                        JSONObject jsonObject = new JSONObject(builder.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("version");
                        readJsonArray(jsonArray);

                        listItemAdapter = new SimpleAdapter(mContext, listItem, R.layout.list_update_version_item,
                                new String[]{"TV_Version_Name"},
                                new int[]{R.id.tv_version_name});
                        mHandler.sendEmptyMessage(UPDATE_LIST);
                    } else {
                        Toast.makeText(mContext, R.string.networkerror, Toast.LENGTH_SHORT).show();
                        LogHelper.w(TAG, "server return error");
                    }
                } catch (MalformedURLException e) {
                    LogHelper.e(TAG, e.getMessage());
                } catch (ProtocolException e) {
                    LogHelper.e(TAG, e.getMessage());
                } catch (IOException e) {
                    LogHelper.e(TAG, e.getMessage());
                } catch (JSONException e) {
                    LogHelper.e(TAG, e.getMessage());
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            LogHelper.e(TAG, e.getMessage());
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e) {
                            LogHelper.e(TAG, e.getMessage());
                        }
                    }
                }
            }
        });
    }

    private void readJsonArray(JSONArray jsonArray) {
        LogHelper.d(TAG, "readJsonArray");
        if (jsonArray == null) return;
        try {
            for (int i = 0; i < jsonArray.length(); i++ ){
                JSONObject parentJsonObject = (JSONObject)jsonArray.opt(i);
                if (parentJsonObject != null) {
                    String versionName = parentJsonObject.getString("versionName");
                    LogHelper.d(TAG, "readJsonArray find versionname = " + versionName);
                    if (parentJsonObject.getString("type").equals("folder")) {
                        if (versionName == null || !versionName.contains(mVersion)) continue;
                        JSONArray childJsonArray = parentJsonObject.getJSONArray("version");
                        readJsonArray(childJsonArray);
                    } else {
                        String CurrentDevice = SystemProperties.get("ro.product.device", "");
                        CurrentDevice = CurrentDevice.substring(0, 10);
                        String bit = SystemProperties.get("ro.zygote", "")
                                .contains("64") ? "64" : "32";
                        if (versionName != null && versionName.contains(CurrentDevice)) {
                            LogHelper.d(TAG, "readJsonArray add " + versionName);
                            HashMap<String, Object> map = new HashMap<String, Object>();
                            map.put("TV_Version_Name", versionName);
                            map.put("TV_Version_File_path", parentJsonObject.getString("url"));
                            listItem.add(map);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            LogHelper.e(TAG, e.getMessage());
        }
    }
}
