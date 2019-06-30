package com.hisilicon.explorer.activity;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hisilicon.explorer.Config;
import com.hisilicon.explorer.R;
import com.hisilicon.explorer.adapter.SambaItemAdapter;
import com.hisilicon.explorer.loader.IProgress;
import com.hisilicon.explorer.loader.SambaServerLoader;
import com.hisilicon.explorer.loader.async.Observable;
import com.hisilicon.explorer.loader.async.Subscriber;
import com.hisilicon.explorer.model.MenuInfo;
import com.hisilicon.explorer.model.SambaItemInfo;
import com.hisilicon.explorer.ui.MenuParentWindow;
import com.hisilicon.explorer.ui.MenuSambaNewOrEditWindow;
import com.hisilicon.explorer.ui.NfsOrSambaChoiceWindow;
import com.hisilicon.explorer.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import android.util.Log;

public class SambaActivity extends BaseFileActivity implements IProgress<List<SambaItemInfo>>, MenuInfo.IMenuClick, MenuInfo.IServerOperate<SambaItemInfo>, NfsOrSambaChoiceWindow.IItemChoiced<SambaItemInfo> {

    private final String TAG = SambaActivity.class.getSimpleName();
    private ListView lv_samba;
    private List<SambaItemInfo> itemInfos = new ArrayList<SambaItemInfo>();
    private SambaItemAdapter sambaItemAdapter;
    private SambaDataObserver sambaDataObserver;
    private SambaServerLoader sambaServerLoader;

    private Stack<List<SambaItemInfo>> listBackStack = new Stack<List<SambaItemInfo>>();

    //状态为0 目前不是search状态，状态为1目前处于search后获得的第一级目录，状态为2目前是search的第二级目录
    private int SEARCH_STATE = 0;
    //目前选中的位置
    private int currentSelectPosition = 0;

    private MenuParentWindow menuParentWindow;

    private static final String ID = "_id";
    private static final String SERVER_IP = "server_ip";

    private static final String NICK_NAME = "nick_name";

    private static final String SERVER_NAME = "server_name";

    private static final String WORK_PATH = "work_path";

    private static final String ACCOUNT = "account";

    private static final String PASSWORD = "password";

    private static final int SERVICE_EXIST = 0X0001;

    private MenuSambaNewOrEditWindow menuSambaNewOrEditWindow;

    @Override
    public int getLayoutId() {
        return R.layout.activity_samba;
    }

    @Override
    public void initView() {
        lv_samba = (ListView) findViewById(R.id.lv_samba);
        lv_samba.setOnItemSelectedListener(onItemSelectedListener);
        lv_samba.setOnItemClickListener(onItemClickListener);
        sambaItemAdapter = new SambaItemAdapter(this, itemInfos);
        lv_samba.setAdapter(sambaItemAdapter);
    }

    @Override
    protected void initData() {
        sambaDataObserver = new SambaDataObserver(new Handler());
        getContentResolver().registerContentObserver(Uri.parse(Config.getInstance().SERVERPROVIDERURI), true, sambaDataObserver);
        sambaServerLoader = new SambaServerLoader(this);
        sambaServerLoader.setiProgress(this);
        getDataFromDB();
    }

    private void getDataFromDB() {
        SEARCH_STATE = 0;
        sambaServerLoader.setSearchType(SambaServerLoader.SEARCH_FROM_DB);
        sambaServerLoader.run();
    }

    private void getDataFromServer() {
        sambaServerLoader.setSearchType(SambaServerLoader.SEARCH_FROM_SERVER);
        sambaServerLoader.run();
        showProgressDialog(GETSMBSERVER);
    }

    /**
     * 通过服务器名获取当前目录
     *
     * @param servername 服务器名地址
     */
    private void getDataFromServerServerName(String servername) {
        sambaServerLoader.setSearchType(SambaServerLoader.SEARCH_FROM_SERVER_NAME);
        sambaServerLoader.setServerServerName(servername);
        sambaServerLoader.run();
    }

    /**
     * 处理返回时间
     *
     * @return 是否拦截
     */
    private boolean dealBackEvent() {
        if (SEARCH_STATE == 2) {
            if (listBackStack.size() == 2) {
                List<SambaItemInfo> listItem = listBackStack.pop();
                SEARCH_STATE = 1;
                SambaActivity.this.loadSuccess(listItem);
                return true;
            }
        } else if (SEARCH_STATE == 1) {
            if (listBackStack.size() == 1) {
                List<SambaItemInfo> listItem = listBackStack.pop();
                SEARCH_STATE = 0;
                SambaActivity.this.loadSuccess(listItem);
                getDataFromDB();
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前item的menu选项
     * SEARCH_STATE 为0状态有两种,1/选中search有两个menu 2、选中普通item 三个
     * SEARCH_STATE 为1 不显示menu
     * SEARCH_STATE 为2 显示add shortcut
     * menus列表长度为0，不显示任何item
     * 显示menu
     */
    private void showMenuOption() {
        if (SEARCH_STATE == 1) {
            return;
        }
        List<MenuInfo> menus = getCurrentMenuItem();
        if (menus.size() == 0) {
            return;
        }
        menuParentWindow = new MenuParentWindow(this, menus, R.style.PopupAnimation);
        menuParentWindow.setIMenuClick(this);
        menuParentWindow.showAtLocation(lv_samba, Gravity.BOTTOM, 0, 0);
        menuParentWindow.selectFirstPosition();
    }

    /**
     * @return menu列表
     */
    private List<MenuInfo> getCurrentMenuItem() {
        ArrayList<MenuInfo> menuInfos = new ArrayList<MenuInfo>();
        MenuInfo newmenu = new MenuInfo();
        newmenu.setResTitle(R.string.str_new);
        newmenu.setResIcon(R.drawable.menu_fullscreen);
        newmenu.setmMenuFuctionType(MenuInfo.MenuFuctionTypeForItem.NEW);

        MenuInfo editmenu = new MenuInfo();
        editmenu.setResTitle(R.string.edit);
        editmenu.setResIcon(R.drawable.menu_fullscreen);
        editmenu.setmMenuFuctionType(MenuInfo.MenuFuctionTypeForItem.EDIT);

        MenuInfo deletemenu = new MenuInfo();
        deletemenu.setResTitle(R.string.delete);
        deletemenu.setResIcon(R.drawable.menu_fullscreen);
        deletemenu.setmMenuFuctionType(MenuInfo.MenuFuctionTypeForItem.DELETE);

        MenuInfo shortcutmenu = new MenuInfo();
        shortcutmenu.setResTitle(R.string.add_shortcut);
        shortcutmenu.setResIcon(R.drawable.menu_fullscreen);
        shortcutmenu.setmMenuFuctionType(MenuInfo.MenuFuctionTypeForItem.ADDSHORTCUT);
        if (currentSelectPosition < itemInfos.size()) {
            SambaItemInfo nfsItemInfo = itemInfos.get(currentSelectPosition);
            if (nfsItemInfo.getType() == SambaItemInfo.TYPE_SEARCH) {
                if (itemInfos.size() == 1) {
                    menuInfos.add(newmenu);
                } else {
                    menuInfos.add(newmenu);
                    menuInfos.add(deletemenu);
                    menuInfos.add(editmenu);
                }
            } else {
                if (SEARCH_STATE == 0) {
                    menuInfos.add(newmenu);
                    menuInfos.add(editmenu);
                    menuInfos.add(deletemenu);
                } else if (SEARCH_STATE == 2) {
                    menuInfos.add(shortcutmenu);
                }
            }
        } else {
            if (SEARCH_STATE == 0) {
                menuInfos.add(newmenu);
                menuInfos.add(deletemenu);
            }
        }
        return menuInfos;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                boolean bBack = dealBackEvent();
                if (bBack) {
                    return bBack;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                showMenuOption();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void stateChange(int state, boolean bstate, Intent intent) {

    }

    @Override
    void progressCancelByUserListener(int operate) {
        switch (operate) {
            case GETSMBSERVER:
                cancelSearchSamba();
                LogUtils.LOGD(TAG, "progress cancel by user . cancel load server .tag = GETNFSSERVER");
                break;
        }
    }

    @Override
    public void onLoading() {

    }

    @Override
    public void loadSuccess(List<SambaItemInfo> a) {
        if (null == a) {
            return;
        }
        itemInfos.clear();
        itemInfos.addAll(a);
        sambaItemAdapter.notifyDataSetChanged();
        dismissProgressDialog();
    }

    @Override
    public void loadFail() {

    }

    /**
     * 显示编辑窗口
     *
     * @param menuInfo menu信息
     */
    private void showServerEditWindow(MenuInfo menuInfo, SambaItemInfo sambaItemInfo, boolean focuseable) {
        menuSambaNewOrEditWindow = new MenuSambaNewOrEditWindow(this, menuInfo);
        if (null != sambaItemInfo) {
            menuSambaNewOrEditWindow.setSambaItem(sambaItemInfo);
        }
        menuSambaNewOrEditWindow.setAddressAndWorkEnable(focuseable);
        menuSambaNewOrEditWindow.setOperationListener(this);
        menuSambaNewOrEditWindow.show();
    }

    private void showServerDeleteWindow() {
        ArrayList<SambaItemInfo> tempInfos = new ArrayList<SambaItemInfo>();
        tempInfos.addAll(itemInfos);
        tempInfos.remove(0);
        int tempPosition;
        if (itemInfos.get(currentSelectPosition).getType() == SambaItemInfo.TYPE_SEARCH) {
            tempPosition = 0;
        } else {
            tempPosition = currentSelectPosition - 1;
        }
        NfsOrSambaChoiceWindow nfsOrSambaChoiceWindow = new NfsOrSambaChoiceWindow(this, tempInfos, tempPosition);
        nfsOrSambaChoiceWindow.setChoicedListener(this);
        nfsOrSambaChoiceWindow.show();
    }

    @Override
    public void menuClick(int type, MenuInfo menuInfo) {
        switch (type) {
            case MenuInfo.MenuFuctionTypeForItem.DELETE:
                showServerDeleteWindow();
                break;
            case MenuInfo.MenuFuctionTypeForItem.NEW:
                showServerEditWindow(menuInfo, null, true);
                break;
            default:
                showServerEditWindow(menuInfo, itemInfos.get(currentSelectPosition), true);

        }
        menuParentWindow.dismiss();
    }

    private void dismissMenuNewOrEditWindow() {
        if (null != menuSambaNewOrEditWindow) {
            menuSambaNewOrEditWindow.dismiss();
        }
    }

    /**
     * 查询数据库中是否含有此对象
     *
     * @param sambaItemInfo 需要判断的对象
     * @return true 有此对象 false没有这个对象
     */
    private boolean hasSambaItemInDatabase(SambaItemInfo sambaItemInfo) {
        Cursor query = getContentResolver().query(Uri.parse(Config.getInstance().SERVERPROVIDERURI + "/samba"), new String[]{ID}, SERVER_IP
                + "=? and " + WORK_PATH + "=?", new String[]{
                sambaItemInfo.getServerIp(), sambaItemInfo.getWorkPath()}, null, null);
        if (null != query) {
        if (query.moveToFirst()) {
            query.close();
            return true;
        } else {
            query.close();
            return false;
        }
        }
        return false;
    }

    public void saveServerItem(final SambaItemInfo sambaItemInfo) {
        Observable.create(new Observable.OnSubscribe() {
            @Override
            public void call(Subscriber subscriber) {
                if (hasSambaItemInDatabase(sambaItemInfo)) {
                    //TODO 提醒用户有相同item
                    subscriber.onError(SambaActivity.SERVICE_EXIST);
                    subscriber.onError();
                    return;
                }
                //然后判断ip是否可达
                if (!isServerReachable(sambaItemInfo.getServerIp())) {
                    //TODO 还需要告诉为何挂载失败使用一个字符串记录失败原因
                    subscriber.onError("");
                    subscriber.onError();
                    return;
                }
                //通过contentprovider挂载目录
                Bundle bundle = new Bundle();
                bundle.putString("address", sambaItemInfo.getServerIp());
                bundle.putString("workpath", sambaItemInfo.getWorkPath());
                bundle.putString("user", sambaItemInfo.getAccount());
                bundle.putString("password", sambaItemInfo.getPwd());
                Bundle resultBundle = getContentResolver().call(Uri.parse(Config.getInstance().SERVERPROVIDERURI), "smbmount", null, bundle);
                boolean mountresult = resultBundle.getBoolean("mountresult");
                if (mountresult) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(NICK_NAME, sambaItemInfo.getNickName());
                    contentValues.put(SERVER_IP, sambaItemInfo.getServerIp());
                    contentValues.put(WORK_PATH, sambaItemInfo.getWorkPath());
                    contentValues.put(ACCOUNT, sambaItemInfo.getAccount());
                    contentValues.put(PASSWORD, sambaItemInfo.getPwd());
                    contentValues.put(SERVER_NAME, sambaItemInfo.getServerName());
                    getContentResolver().insert(Uri.parse(Config.getInstance().SERVERPROVIDERURI + "/samba"), contentValues);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError();
                    //TODO 还需要告诉为何挂载失败使用一个字符串记录失败原因
                    subscriber.onError("");
                }
            }
        }).subscribeOn().observeOn().subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                dismissMenuNewOrEditWindow();
            }

            @Override
            public void onError(Object obj) {
                if (!(obj instanceof Integer)){
                    return;
                }
                int what = (int)obj;
                switch (what){
                    case SambaActivity.SERVICE_EXIST :
                            String serviceExist = SambaActivity.this.getResources().getString(R.string.service_exist);
                            Toast.makeText(SambaActivity.this,serviceExist,Toast.LENGTH_LONG).show();
                            break;
                    default:break;
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public void serverAdd(SambaItemInfo sambaItemInfo) {
        saveServerItem(sambaItemInfo);
    }

    public void updateServerItem(final SambaItemInfo sambaItemInfo) {
        Observable.create(new Observable.OnSubscribe() {
            @Override
            public void call(Subscriber subscriber) {
                if (!isServerReachable(sambaItemInfo.getServerIp())) {
                    //TODO 还需要告诉为何挂载失败使用一个字符串记录失败原因
                    subscriber.onError("");
                    subscriber.onError();
                    return;
                }
                //通过contentprovider挂载目录
                Bundle bundle = new Bundle();
                bundle.putString("address", sambaItemInfo.getServerIp());
                bundle.putString("workpath", sambaItemInfo.getWorkPath());
                bundle.putString("user", sambaItemInfo.getAccount());
                bundle.putString("password", sambaItemInfo.getPwd());
                Bundle resultBundle = getContentResolver().call(Uri.parse(Config.getInstance().SERVERPROVIDERURI), "smbmount", null, bundle);
                boolean mountresult = resultBundle.getBoolean("mountresult");
                if (mountresult) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(NICK_NAME, sambaItemInfo.getNickName());
                    contentValues.put(SERVER_IP, sambaItemInfo.getServerIp());
                    contentValues.put(WORK_PATH, sambaItemInfo.getWorkPath());
                    contentValues.put(ACCOUNT, sambaItemInfo.getAccount());
                    contentValues.put(PASSWORD, sambaItemInfo.getPwd());
                    contentValues.put(SERVER_NAME, sambaItemInfo.getServerName());
                    int update = getContentResolver().update(Uri.parse(Config.getInstance().SERVERPROVIDERURI + "/samba"), contentValues, ID + "=?",
                            new String[]{String.valueOf(sambaItemInfo.getShortId())});
                    if (update > 0) {
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError();
                    }
                } else {
                    subscriber.onError();
                    //TODO 还需要告诉为何挂载失败使用一个字符串记录失败原因
                    subscriber.onError("");
                }
            }
        }).subscribeOn().observeOn().subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                dismissMenuNewOrEditWindow();
            }

            @Override
            public void onError(Object obj) {

            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public void serverEdit(SambaItemInfo sambaItemInfo) {
        updateServerItem(sambaItemInfo);
    }

    @Override
    public void serverAddShortCut(SambaItemInfo sambaItemInfo) {
        saveServerItem(sambaItemInfo);
    }

    private void deleteServerItem(final List<SambaItemInfo> list) {
        Observable.create(new Observable.OnSubscribe() {
            @Override
            public void call(Subscriber subscriber) {
                for (SambaItemInfo sambaItemInfo : list) {
                    Uri uri = ContentUris.withAppendedId(Uri.parse(Config.getInstance().SERVERPROVIDERURI + "/samba"), sambaItemInfo.getShortId());
                    getContentResolver().delete(uri, ID + "=?",
                            new String[]{String.valueOf(sambaItemInfo.getShortId())});
                }
                subscriber.onCompleted();
            }
        }).subscribeOn().observeOn().subscribe(new Subscriber() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public void getChoiceItem(List<SambaItemInfo> list) {
        deleteServerItem(list);
    }

    /**
     * 取消搜索smb服务器
     */
    private void cancelSearchSamba() {
        sambaServerLoader.cancelSearchSamba();
        SEARCH_STATE = 0;
    }

    private void startFileListActivity(String mountPoint, String nickName) {
        Bundle bundle = new Bundle();
        bundle.putString("rootpath", mountPoint);
        bundle.putString("nickName", nickName);
        Intent intent = new Intent(SambaActivity.this, FileListActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 获取当前的挂载节点
     *
     * @param sambaItemInfo 需要获取的item
     * @return 挂载节点
     */
    private String getMountPoint(SambaItemInfo sambaItemInfo) {
        Bundle bundle = new Bundle();
        bundle.putString("path", sambaItemInfo.getServerIp() + "/" + sambaItemInfo.getWorkPath());
        Bundle getNFSMountPointBundle = getContentResolver().call(Uri.parse(Config.getInstance()
                .SERVERPROVIDERURI), "getSmbMountPoint", null, bundle);
        String mountpoint = getNFSMountPointBundle.getString("getmountpoint", "");
        return mountpoint;
    }

    /**
     * 挂载路径进入文件浏览界面
     * 1、先判断ip是否可达
     * 2、能否直接后去挂载点，可以直接进入
     * 3、不能获取挂载点，挂载，然后获取挂载点
     * 4、如果挂载失败，弹出信息框，输入信息，进行挂载，如果成功进入
     *
     * @param sambaItemInfo smb
     */
    private void mountPath(final SambaItemInfo sambaItemInfo) {
        Observable.create(new Observable.OnSubscribe() {
            @Override
            public void call(Subscriber subscriber) {
                if (!isServerReachable(sambaItemInfo.getServerIp())) {
                    subscriber.onError();
                    return;
                }
                //如果获取挂载节点不为空直接进入目录
                String p = getMountPoint(sambaItemInfo);
                if (!TextUtils.isEmpty(p)) {
                    subscriber.onCompleted(p);
                    return;
                }
                //获取挂载节点为空挂载然后进入activity
                Bundle bundle = new Bundle();
                bundle.putString("address", sambaItemInfo.getServerIp());
                bundle.putString("workpath", sambaItemInfo.getWorkPath());
                bundle.putString("user", sambaItemInfo.getAccount());
                bundle.putString("password", sambaItemInfo.getPwd());
                Bundle resultBundle = getContentResolver().call(Uri.parse(Config.getInstance().SERVERPROVIDERURI), "smbmount", null, bundle);
                boolean mountresult = resultBundle.getBoolean("mountresult");
                if (mountresult) {
                    String point = getMountPoint(sambaItemInfo);
                    if (TextUtils.isEmpty(point)) {
                        subscriber.onError();
                    } else {
                        subscriber.onCompleted(point);
                        subscriber.onCompleted();
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            MenuInfo menuInfo = new MenuInfo();
                            menuInfo.setmMenuFuctionType(MenuInfo.MenuFuctionTypeForItem.NEW);
                            showServerEditWindow(menuInfo, sambaItemInfo, false);
                        }
                    });
                }
            }
        }).subscribeOn().observeOn().subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onCompleted(Object obj) {
                if (!hasSambaItemInDatabase(sambaItemInfo)) {
                    sambaItemInfo.setNickName("\\\\" + sambaItemInfo.getServerIp() + "\\" + sambaItemInfo.getNickName());
                    saveServerItem(sambaItemInfo);
                }
                if (!TextUtils.isEmpty(sambaItemInfo.getNickName())&&!sambaItemInfo.getNickName().contains("\\")) {
                    startFileListActivity((String) obj, "\\\\" + sambaItemInfo.getServerIp() + "\\" + sambaItemInfo.getNickName());
                } else {
                    startFileListActivity((String) obj, sambaItemInfo.getNickName());
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SambaItemInfo sambaItemInfo = itemInfos.get(position);
            if (sambaItemInfo.getType() == SambaItemInfo.TYPE_SEARCH) {
                getDataFromServer();
                SEARCH_STATE = 1;
                ArrayList<SambaItemInfo> tempInfos = new ArrayList<SambaItemInfo>();
                tempInfos.addAll(itemInfos);
                listBackStack.push(tempInfos);
            } else {
                if (SEARCH_STATE == 1) {
                    ArrayList<SambaItemInfo> tempInfos = new ArrayList<SambaItemInfo>();
                    tempInfos.addAll(itemInfos);
                    listBackStack.push(tempInfos);
                    getDataFromServerServerName(sambaItemInfo.getServerName());
                    SEARCH_STATE = 2;
                } else if (SEARCH_STATE == 2) {
                    // 进入列表查看文件
                    mountPath(sambaItemInfo);
                } else {
                    // 本地操作挂载文件，挂载成功后进入FileListActivity查看文件
                    mountPath(sambaItemInfo);
                }
            }
        }
    };

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            currentSelectPosition = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            currentSelectPosition = 0;
        }
    };

    private class SambaDataObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public SambaDataObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //仅仅为了编辑的时候方便些，但是在search状态的时候不需要更新这个玩意儿需要添加search的判断
            if (SEARCH_STATE == 0) {
                lv_samba.clearFocus();
                getDataFromDB();
            }
        }
    }
}
