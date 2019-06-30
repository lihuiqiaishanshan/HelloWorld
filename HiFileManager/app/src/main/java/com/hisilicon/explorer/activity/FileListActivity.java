package com.hisilicon.explorer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.explorer.Config;
import com.hisilicon.explorer.R;
import com.hisilicon.explorer.adapter.FileListAdapter;
import com.hisilicon.explorer.adapter.GridListAdapter;
import com.hisilicon.explorer.loader.FileInfoLoader;
import com.hisilicon.explorer.loader.IProgress;
import com.hisilicon.explorer.loader.SearchFileLoader;
import com.hisilicon.explorer.loader.async.Observable;
import com.hisilicon.explorer.loader.async.Subscriber;
import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.model.MenuInfo;
import com.hisilicon.explorer.ui.FileOperateContinueWindow;
import com.hisilicon.explorer.ui.MenuChildCheckedWindow;
import com.hisilicon.explorer.ui.MenuChildEditWindow;
import com.hisilicon.explorer.ui.MenuParentWindow;
import com.hisilicon.explorer.ui.MenuRenameWindow;
import com.hisilicon.explorer.ui.MultipleChoiceWindow;
import com.hisilicon.explorer.ui.PreviewDialog;
import com.hisilicon.explorer.utils.FileUtils;
import com.hisilicon.explorer.utils.LogUtils;
import com.hisilicon.explorer.utils.MenuUtils;
import com.hisilicon.explorer.utils.ToastUtil;
import com.hisilicon.explorer.utils.SerializablePair;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Arrays;

/**
 */

public class FileListActivity extends BaseFileActivity implements IProgress<List<FileInfo>>, MenuInfo.IMenuClick, MenuInfo.IFileOperate {

    private final String TAG = FileListActivity.class.getSimpleName();
    private FileInfoLoader fileInfoLoader;
    private ListView lv_file_list;
    private GridView gv_file_grid;
    private final String DIRECTORY_MIMETYPE_START = "directory";
    //保存当前显示文件信息
    private List<FileInfo> fileInfos = new ArrayList<FileInfo>();
    //列表适配器
    private FileListAdapter fileListAdapter;
    //缩略图适配器
    private GridListAdapter gridListAdapter;
    //顶部标题
    private TextView tv_folder_title;
    //顶部文件数量
    private TextView tv_file_count;
    //保存当前顶部标题信息字符串
    private String currentPath = "";
    //用于保存临时的标题栏信息，如果加载成功就将此值付给currentPath失败上一个值不变
    private String tempCurrentPath = "";
    //当前位于文件什么位置，这个位置不是从0开始计算的是从1开始的
    private int currentFilePosition = 0;
    //文件夹的文件总数
    private int totalFileCount = 0;
    //保存文件进入退出的栈
    Stack<SerializablePair<String, Integer>> fileBackStack = new Stack<>();
    //用于记录返回后pop出的当前位置和路径
    private SerializablePair<String, Integer> currentPathPair;
    private MenuParentWindow menuParentWindow;
    private View rl_parent;
    private SearchFileLoader searchFileLoader;
    //保存搜索结果
    private List<FileInfo> searchResult;
    private String SEARCHTAG = "search";
    //根路径
    private String rootpath;
    private String nickName;
    /**
     * 当由于低内存当前Activity被销毁时, 保存必要的数据, 用于恢复回收时的场景
     * {@link #currentPath}
     * {@link #currentFilePosition}
     * {@link #searchResult}
     * {@link #fileBackStack}
     */
    private String[] saveFields = {"currentPath", "currentFilePosition", "searchResult", "fileBackStack"};
    // 是否有保存数据在Bundle中.
    private boolean hasSaveInstance = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hasSaveInstance = loadDataFromSaveInstanceState(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_list_file;
    }

    @Override
    public void initView() {
        rl_parent = findViewById(R.id.rl_parent);
        lv_file_list = (ListView) findViewById(R.id.lv_file_list);
        fileListAdapter = new FileListAdapter(this, fileInfos);
        lv_file_list.setAdapter(fileListAdapter);
        lv_file_list.setOnItemClickListener(onItemClickListener);
        lv_file_list.setOnItemSelectedListener(onItemSelectedListener);
        gv_file_grid = (GridView) findViewById(R.id.gv_file_grid);
        gridListAdapter = new GridListAdapter(this, fileInfos);
        gv_file_grid.setAdapter(gridListAdapter);
        gv_file_grid.setOnItemClickListener(onItemClickListener);
        gv_file_grid.setOnItemSelectedListener(onItemSelectedListener);
        styleChange(Config.getInstance().getFileShowType());
        tv_folder_title = (TextView) findViewById(R.id.tv_folder_title);
        tv_file_count = (TextView) findViewById(R.id.tv_file_count);
    }

    @Override
    protected void initData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            rootpath = extras.getString("rootpath");
            nickName = extras.getString("nickName");
            if(!hasSaveInstance){
                tempCurrentPath = currentPath = rootpath;
            } else{
                tempCurrentPath = currentPath;
            }
            fileInfoLoader = new FileInfoLoader(this);
            getFileFloderData(currentPath);
        }
    }

    @Override
    protected void stateChange(int state, boolean bstate, Intent intent) {
        switch (state) {
            case USB_STATE_CHANGE:
                if (!bstate) {
                    String mntP = intent.getData().getPath();
                    if (currentPath.startsWith(mntP)) {
                        FileListActivity.this.finish();
                    }
                }
                break;
            case NETWORK_STATE_CHANGE:
                //如果在浏览本地文件跳转出去
                if (currentPath.startsWith("/mnt") || currentPath.startsWith("/storage")) {
                    break;
                }
                if (!bstate) {
                    showNetworkNotify();
                } else {
                    dismissNetworkNotify();
                }
                break;
        }
    }

    @Override
    void progressCancelByUserListener(int operate) {
        switch (operate) {
            case FILELISTLOADING:
                fileInfoLoader.cancelLoadFile();
                LogUtils.LOGD(TAG, "progress cancel by user . cancel load file .tag = FILELISTLOADING");
                break;
            case SEARCHFILELIST:
                searchFileLoader.cancelSearch();
                LogUtils.LOGD(TAG, "progress cancel by user . cancel load file .tag = SEARCHFILELIST");
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(saveFields[0], currentPath);
        outState.putInt(saveFields[1], currentFilePosition);
        if(searchResult != null) {
            outState.putParcelableArray(saveFields[2], (Parcelable[]) searchResult.toArray());
        }
        outState.putSerializable(saveFields[3], fileBackStack);
        super.onSaveInstanceState(outState);
    }

    /**
     * 尝试从saveInstanceState里面取Activity被销毁时保存的数据, 取到则返回 true, 反之 false
     * @param savedInstanceState 保存的信息
     * @return 是否取到
     */
    private boolean loadDataFromSaveInstanceState(Bundle savedInstanceState){
        if(savedInstanceState == null) {
            return false;
        } else{
            currentPath = savedInstanceState.getString(saveFields[0]);
            currentFilePosition = savedInstanceState.getInt(saveFields[1]);
            FileInfo[] fileInfoArray = (FileInfo[]) savedInstanceState.getParcelableArray(saveFields[2]);
            if(fileInfoArray != null){
                searchResult = new ArrayList<>(Arrays.asList(fileInfoArray));
            }
            // Any object that implements both java.util.List and java.io.Serializable will become ArrayList after
            // intent.putExtra(EXTRA_TAG, suchObject)/startActivity(intent)/intent.getSerializableExtra(EXTRA_TAG).
            // https://code.google.com/p/android/issues/detail?id=3847
            List list = (List) savedInstanceState.getSerializable(saveFields[3]);
            if(list instanceof Stack){
                //noinspection unchecked
                fileBackStack = (Stack<SerializablePair<String, Integer>>) list;
            } else{
                //noinspection unchecked
               fileBackStack.addAll(list);
            }
            return true;
        }
    }

    @Override
    public void onLoading() {
        showProgressDialog(FILELISTLOADING);
    }

    @Override
    public void loadSuccess(List<FileInfo> a) {
        fileInfos.clear();
        fileInfos.addAll(a);
        fileListAdapter.notifyDataSetChanged();
        gridListAdapter.notifyDataSetChanged();

        //currentPathPair不为空并且文件数量大于0并且选择的位置小于文件数量才可以设置选择位置
        //PS:在进入下一个文件夹的时候需要将currentPathPair置换为空，以便刷新选择位置
        Integer selectedPosition = 0;
        if (currentPathPair != null) {
            selectedPosition = currentPathPair.second;
        }
        if (fileInfos.size() <= selectedPosition) {
            selectedPosition = 0;
        }
        if (Config.getInstance().getFileShowType() == MenuUtils.FileShowType.LIST) {
            if (fileInfos.size() > 0) {
                lv_file_list.setSelection(selectedPosition);
            }
        } else {
            if (fileInfos.size() > 0) {
                //如果使用鼠标不先清除焦点，焦点不会及时更新
                gv_file_grid.clearFocus();
                gv_file_grid.setSelection(selectedPosition);
            }
        }

        //这里是更新加载成功后的顶部信息
        totalFileCount = fileInfos.size();
        if (totalFileCount == 0) {
            currentFilePosition = 0;
        }
        currentPath = tempCurrentPath;
        refreshTitleBarInfo();

        dismissProgressDialog();
    }

    private void refreshCurrentDir() {
        getFileFloderData(currentPath);
    }

    //通过路径加载文件
    private void getFileFloderData(String path) {
        fileInfoLoader.setLoadPath(path, Config.getInstance().getFileFilterType(), "");
        fileInfoLoader.setSortType(Config.getInstance().getFileSortType());
        fileInfoLoader.run();
    }

    //刷新标题栏信息
    private void refreshTitleBarInfo() {
        if (!TextUtils.isEmpty(nickName)) {
            StringBuilder temp = new StringBuilder(currentPath);
            //不减1是因为路径后边带了一个挂载的数
            temp.replace(0, rootpath.length(), nickName.substring(2).replace("\\", "/"));
            tv_folder_title.setText(temp.toString());
        } else {
            tv_folder_title.setText(currentPath);
        }
        tv_file_count.setText("[" + currentFilePosition + "/" + totalFileCount + "]");
    }

    @Override
    public void loadFail() {
        fileBackStack.pop();
        dismissProgressDialog();
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

    private void showMenuOption() {
        if (null == menuParentWindow) {
            menuParentWindow = new MenuParentWindow(this, Config.getInstance().getMenuInfoList(), R.style.PopupAnimation);
            menuParentWindow.setIMenuClick(this);
        }
        menuParentWindow.showAtLocation(rl_parent, Gravity.BOTTOM, 0, 0);
        menuParentWindow.selectFirstPosition();
    }

    private void dismissMenuParent() {
        if (null != menuParentWindow) {
            menuParentWindow.dismiss();
        }
    }

    /**
     * 按返回键时如果文件栈中有文件则退回上级目录，如果没有就处理返回键
     *
     * @return
     */
    private boolean dealBackEvent() {
        if (fileBackStack.empty()) {
            return false;
        }
        currentPathPair = fileBackStack.pop();
        if (currentPathPair != null) {
            tempCurrentPath = currentPathPair.first;
            if (SEARCHTAG.equals(tempCurrentPath)) {
                if (null != searchResult) {
                    loadSuccess(searchResult);
                }
            } else {
                getFileFloderData(currentPathPair.first);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 显示新建文件夹窗口
     *
     * @param menuInfo
     */
    private void showNewFolderWindow(MenuInfo menuInfo) {
        MenuChildEditWindow newFolderWindow = new MenuChildEditWindow(this, menuInfo);
        newFolderWindow.setOperationListener(this);
        newFolderWindow.show();
        dismissMenuParent();
    }

    /**
     * 显示搜索文件夹窗口
     *
     * @param menuInfo
     */
    private void showSearchMenuWindow(MenuInfo menuInfo) {
        MenuChildEditWindow searchWindow = new MenuChildEditWindow(this, menuInfo);
        searchWindow.setOperationListener(this);
        searchWindow.show();
        dismissMenuParent();
    }

    private void showOptionMenuWindow(MenuInfo menuInfo) {
        MenuChildCheckedWindow menuChildCheckedWindow = new MenuChildCheckedWindow(this, menuInfo);
        menuChildCheckedWindow.setOperationListener(this);
        menuChildCheckedWindow.show();
        dismissMenuParent();
    }

    @Override
    public void menuClick(int type, MenuInfo menuInfo) {
        switch (type) {
            case MenuInfo.MenuFuctionType.FILE_FILTER:
            case MenuInfo.MenuFuctionType.FILE_SORT:
            case MenuInfo.MenuFuctionType.OPTIONS:
            case MenuInfo.MenuFuctionType.SHOW_STYLE:
                showOptionMenuWindow(menuInfo);
                break;
            case MenuInfo.MenuFuctionType.NEW_FOLDER:
                showNewFolderWindow(menuInfo);
                break;
            case MenuInfo.MenuFuctionType.PREVIEW:
                menuPreview();
                break;
            case MenuInfo.MenuFuctionType.SEARCH:
                showSearchMenuWindow(menuInfo);
                break;
        }
    }

    @Override
    public void menuNewFolder(final String name) {
        Observable.create(new Observable.OnSubscribe() {
            @Override
            public void call(Subscriber subscriber) {
                int newDirState = FileUtils.createNewDirState(currentPath, name);
                switch (newDirState) {
                    case 0:
                        subscriber.onCompleted();
                        break;
                    case 1:
                        subscriber.onError("exist");
                        break;
                    case 2:
                        subscriber.onError();
                        break;
                }
            }
        })
                //执行放入子线程
                .subscribeOn()
                //完成或者回调放入主线程
                .observeOn()
                .subscribe(new Subscriber() {
                    @Override
                    public void onCompleted() {
                        refreshCurrentDir();
                    }

                    @Override
                    public void onError() {
                        //TODO 执行创建文件失败的处理
                        ToastUtil.showMessage(FileListActivity.this, R.string.error);
                    }

                    @Override
                    public void onError(Object obj) {
                        if (!TextUtils.isEmpty(obj + "")) {
                            if ("exist".equals(obj + "")) {
                                FileOperateContinueWindow tipWindow = new FileOperateContinueWindow(FileListActivity.this);
                                tipWindow.setContinueOperationListener(new FileOperateContinueWindow.IFileOperateContinue() {
                                    @Override
                                    public void fileOperateContinue() {
                                        Observable.create(new Observable.OnSubscribe() {
                                            @Override
                                            public void call(Subscriber subscriber) {
                                                //先删除，如果删除成功就第二次创建
                                                boolean b = FileUtils.deleteFile(currentPath + File.separator + name);
                                                if (b) {
                                                    boolean newDir = FileUtils.createNewDir(currentPath, name);
                                                    if (newDir) {
                                                        subscriber.onCompleted();
                                                    } else {
                                                        onError();
                                                    }
                                                } else {
                                                    subscriber.onError();
                                                }
                                            }
                                        }).subscribeOn().observeOn().subscribe(new Subscriber() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError() {
                                                ToastUtil.showMessage(FileListActivity.this, R.string.error);
                                            }
                                        });
                                    }
                                });
                                tipWindow.show();
                            }
                        }
                    }
                });
    }

    @Override
    public void menuSearchFile(String keyword) {
        searchFileLoader = new SearchFileLoader(currentPath, keyword);
        searchFileLoader.setSearchFileInfoLoaderLinstenter(new IProgress<List<FileInfo>>() {
            @Override
            public void onLoading() {
                showProgressDialog(SEARCHFILELIST);
            }

            @Override
            public void loadSuccess(List<FileInfo> a) {
                int position = 0;
                if (currentFilePosition > 0) {
                    position = currentFilePosition - 1;
                }
                SerializablePair<String, Integer> stringIntegerPair = new SerializablePair(currentPath, position);
                fileBackStack.push(stringIntegerPair);
                //主要用于进入文件夹后刷新选中的位置
                currentPathPair = null;
                //更新临时的标题
                tempCurrentPath = SEARCHTAG;
                searchResult = a;
                FileListActivity.this.loadSuccess(a);
                dismissProgressDialog();
            }

            @Override
            public void loadFail() {

            }
        });
        searchFileLoader.run();
    }

    private void showOperationChoiceWindow(int type) {
        int choicePosition = 0;
        if (currentFilePosition - 1 < 0) {
            choicePosition = 0;
        } else {
            choicePosition = currentFilePosition - 1;
        }
        MultipleChoiceWindow multipleChoiceWindow = new MultipleChoiceWindow(this, fileInfos, type, choicePosition);
        multipleChoiceWindow.setChoicedListener(iFileChoiced);
        multipleChoiceWindow.show();
        dismissMenuParent();
    }

    @Override
    public void menuOperationFile(int type) {
        switch (type) {
            case MenuUtils.FileOperationType.COPY:
            case MenuUtils.FileOperationType.CUT:
            case MenuUtils.FileOperationType.DELETE:
            case MenuUtils.FileOperationType.RENAME:
                showOperationChoiceWindow(type);
                break;
            case MenuUtils.FileOperationType.PASTE:
                operateCutOrCopy();
                Config.getInstance().refreshNormalMenuList();
                break;
        }
        dismissMenuParent();
    }

    /**
     * 进行剪切或者复制的操作
     */
    private void operateCutOrCopy() {
        if (checkHasSameNameFile()) {
            //TODO 弹出提示框，点击ok直接删除掉文件然后，进行后续操作
            FileOperateContinueWindow fileOperateContinueWindow = new FileOperateContinueWindow(this);
            fileOperateContinueWindow.setContinueOperationListener(new FileOperateContinueWindow.IFileOperateContinue() {
                @Override
                public void fileOperateContinue() {
                    deleteSameNameFile();
                }
            });
            fileOperateContinueWindow.show();
            return;
        }
        operateCutOrCopyImm();
    }

    private void operateCutOrCopyImm() {
        switch (Config.getInstance().getFileOperateType()) {
            case MenuUtils.FileOperationType.COPY:
                copyFile();
                break;
            case MenuUtils.FileOperationType.CUT:
                cutFile();
                break;
        }
    }

    /**
     * 仅仅为剪切和复制进行检测操作
     *
     * @return true有相同文件 false没有
     */
    private boolean checkHasSameNameFile() {
        if (null == fileInfos || fileInfos.size() == 0) {
            return false;
        }
        List<FileInfo> fileOperateList = Config.getInstance().getFileOperateList();
        if (fileOperateList == null || fileOperateList.size() <= 0) {
            return true;
        }
        for (FileInfo fileInfo : fileInfos) {
            for (FileInfo f : fileOperateList) {
                if (fileInfo.getDisplayName().equals(f.getDisplayName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 仅仅为剪切和复制进行检测操作
     */
    private void deleteSameNameFile() {
        if (null == fileInfos || fileInfos.size() == 0) {
            return;
        }
        Observable.create(new Observable.OnSubscribe() {
            @Override
            public void call(Subscriber subscriber) {
                List<FileInfo> fileOperateList = Config.getInstance().getFileOperateList();
                if (fileOperateList == null || fileOperateList.size() <= 0) {
                    return;
                }
                for (FileInfo fileInfo : fileInfos) {
                    for (FileInfo f : fileOperateList) {
                        if (fileInfo.getDisplayName().equals(f.getDisplayName())) {
                            FileUtils.deleteFile(fileInfo.getPath());
                            break;
                        }
                    }
                }
                subscriber.onCompleted();
            }
        }).subscribeOn().observeOn().subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                operateCutOrCopyImm();
            }

            @Override
            public void onError() {

            }
        });
    }

    private void copyFile() {
        Observable.create(new Observable.OnSubscribe() {
            @Override
            public void call(Subscriber subscriber) {
                List<FileInfo> fileOperateList = Config.getInstance().getFileOperateList();
                if (fileOperateList == null || fileOperateList.size() <= 0) {
                    subscriber.onCompleted();
                    return;
                }
                for (FileInfo fileInfo : fileOperateList) {
                    boolean bMoveSuccess = FileUtils.moveDocument(fileInfo.getPath(), currentPath, "");
                    if (!bMoveSuccess) {
                        subscriber.onError();
                        return;
                    }
                }
                subscriber.onCompleted();
            }
        }).subscribeOn().observeOn().subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                refreshCurrentDir();
            }

            @Override
            public void onError() {
                refreshCurrentDir();
            }
        });
    }


    private void cutFile() {
        Observable.create(new Observable.OnSubscribe() {
            @Override
            public void call(Subscriber subscriber) {
                List<FileInfo> fileOperateList = Config.getInstance().getFileOperateList();
                if (fileOperateList != null && fileOperateList.size() > 0) {
                    for (FileInfo fileInfo : fileOperateList) {
                        boolean bMoveSuccess = FileUtils.moveDocument(fileInfo.getPath(), currentPath, "");
                        if (bMoveSuccess) {
                            boolean bDelete = FileUtils.deleteFile(fileInfo.getPath());
                            //TODO 或许应该对删除失败的做处理
                        } else {
                            subscriber.onError();
                            return;
                        }
                    }
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn().observeOn().subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                refreshCurrentDir();
            }

            @Override
            public void onError() {
                //TODO 文件剪切中断
                refreshCurrentDir();
            }
        });
    }

    @Override
    public void menuFilterFile(int type) {
        switch (type) {
            case MenuUtils.FileFilterType.ALL:
                MenuUtils.setFileFilterType(MenuUtils.FileFilterType.ALL);
                Config.getInstance().setFileFilterType(MenuUtils.FileFilterType.ALL);
                break;
            case MenuUtils.FileFilterType.IMAGE:
                MenuUtils.setFileFilterType(MenuUtils.FileFilterType.IMAGE);
                Config.getInstance().setFileFilterType(MenuUtils.FileFilterType.IMAGE);
                break;
            case MenuUtils.FileFilterType.AUDIO:
                MenuUtils.setFileFilterType(MenuUtils.FileFilterType.AUDIO);
                Config.getInstance().setFileFilterType(MenuUtils.FileFilterType.AUDIO);
                break;
            case MenuUtils.FileFilterType.VIDEO:
                MenuUtils.setFileFilterType(MenuUtils.FileFilterType.VIDEO);
                Config.getInstance().setFileFilterType(MenuUtils.FileFilterType.VIDEO);
                break;
        }
        refreshCurrentDir();
    }

    /**
     * 切换list和thumbnails
     *
     * @param type 显示类型
     */
    private void styleChange(int type) {
        if (type == MenuUtils.FileShowType.LIST) {
            lv_file_list.setVisibility(View.VISIBLE);
            gv_file_grid.setVisibility(View.GONE);
        } else {
            lv_file_list.setVisibility(View.GONE);
            gv_file_grid.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void menuShowStyle(int type) {
        switch (type) {
            case MenuUtils.FileShowType.LIST:
                //必须同步设置，主要是为了保存是保存，读取还是从目前配置中读取快些
                MenuUtils.setFileShowType(MenuUtils.FileShowType.LIST);
                Config.getInstance().setFileShowType(MenuUtils.FileShowType.LIST);
                break;
            case MenuUtils.FileShowType.THUMBNAIL:
                MenuUtils.setFileShowType(MenuUtils.FileShowType.THUMBNAIL);
                Config.getInstance().setFileShowType(MenuUtils.FileShowType.THUMBNAIL);
                break;
        }
        styleChange(type);
        //refreshCurrentDir();
    }

    @Override
    public void menuSortFile(int type) {
        switch (type) {
            case MenuUtils.FileSortType.DEFAULT:
                MenuUtils.setFileSortType(MenuUtils.FileSortType.DEFAULT);
                Config.getInstance().setFileSortType(MenuUtils.FileSortType.DEFAULT);
                break;
            case MenuUtils.FileSortType.FILENAME:
                MenuUtils.setFileSortType(MenuUtils.FileSortType.FILENAME);
                Config.getInstance().setFileSortType(MenuUtils.FileSortType.FILENAME);
                break;
            case MenuUtils.FileSortType.FILESIZE:
                MenuUtils.setFileSortType(MenuUtils.FileSortType.FILESIZE);
                Config.getInstance().setFileSortType(MenuUtils.FileSortType.FILESIZE);
                break;
            case MenuUtils.FileSortType.LASTMODIFYTIME:
                MenuUtils.setFileSortType(MenuUtils.FileSortType.LASTMODIFYTIME);
                Config.getInstance().setFileSortType(MenuUtils.FileSortType.LASTMODIFYTIME);
                break;
        }
        refreshCurrentDir();
    }

    @Override
    public void menuPreview() {
        //鼠标这里应该支持的不好，因为位置不好确定
        if (null == fileInfos || fileInfos.size() == 0) {
            return;
        }
        //这里是沿用以前版本的dialog么有进行任何修改
        int position = currentFilePosition - 1;
        //使用鼠标在4.4上没有焦点所以position小于0，所以默认查看第0个位置信息
        if (position < 0) {
            position = 0;
        }
        PreviewDialog dialog = new PreviewDialog(this, fileInfos.get(position), this);
        dialog.show();
        dismissMenuParent();
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            FileInfo fileInfo = fileInfos.get(position);
            if (DIRECTORY_MIMETYPE_START.equals(fileInfo.getMimeTypes().split("/")[0])) {
                //准备进入下级目录将pathpair进栈
                if (!hasExistBackStack(currentPath)) {
                    SerializablePair<String, Integer> stringIntegerPair = new SerializablePair(currentPath, position);
                    fileBackStack.push(stringIntegerPair);
                }
                //主要用于进入文件夹后刷新选中的位置
                currentPathPair = null;
                //更新临时的标题
                tempCurrentPath = fileInfo.getPath();
                getFileFloderData(fileInfo.getPath());
            } else {
                openFile(FileListActivity.this, fileInfo);
            }
        }
    };

    /**
     * 删除文件
     *
     * @param files 删除列表
     */
    private void deleteFiles(final List<FileInfo> files) {
        if (null == files || files.size() <= 0) {
            return;
        }
        Observable.create(new Observable.OnSubscribe() {
            @Override
            public void call(Subscriber subscriber) {
                for (FileInfo fileInfo : files) {
                    boolean bSuccess = FileUtils.deleteFile(fileInfo.getPath());
                    if (bSuccess) {
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError();
                    }
                }
            }
        }).subscribeOn().observeOn().subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                refreshCurrentDir();
            }

            @Override
            public void onError() {

            }
        });
    }

    private void showRenameWindow(List<FileInfo> list) {
        if (null == list || list.size() <= 0) {
            return;
        }
        final FileInfo fileInfo = list.get(0);
        MenuRenameWindow menuRenameWindow = new MenuRenameWindow(this, fileInfo.getDisplayName());
        menuRenameWindow.setRenameOperationListener(new MenuRenameWindow.IFileOperateRename() {
            @Override
            public void fileOperateRename(final String newName) {
                Observable.create(new Observable.OnSubscribe() {
                    @Override
                    public void call(Subscriber subscriber) {
                        boolean bSuccess = FileUtils.renameFile(fileInfo.getPath(), newName);
                        if (bSuccess) {
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError();
                        }
                    }
                }).subscribeOn().observeOn().subscribe(new Subscriber() {
                    @Override
                    public void onCompleted() {
                        refreshCurrentDir();
                    }

                    @Override
                    public void onError() {

                    }
                });
            }
        });
        menuRenameWindow.show();
    }

    /**
     * 判断回退栈中是否存在相同路径
     *
     * @param path 需要判断的路径
     * @return true存在相同路径，false不存在
     */
    private boolean hasExistBackStack(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }
        Iterator<SerializablePair<String, Integer>> iterator = fileBackStack.iterator();
        while (iterator.hasNext()) {
            SerializablePair<String, Integer> next = iterator.next();
            if (path.equals(next.first)) {
                return true;
            }
        }
        return false;
    }

    //获取多选窗口获取的文件
    MultipleChoiceWindow.IFileChoiced iFileChoiced = new MultipleChoiceWindow.IFileChoiced() {
        @Override
        public void getChoiceFile(List<FileInfo> list, int type) {
            switch (type) {
                case MenuUtils.FileOperationType.COPY:
                case MenuUtils.FileOperationType.CUT:
                    //保存全局方便操作到任何地方
                    Config.getInstance().setFileOperateType(type);
                    Config.getInstance().setFileOperateList(list);
                    Config.getInstance().refreshPastMenuList();
                    break;
                case MenuUtils.FileOperationType.DELETE:
                    deleteFiles(list);
                    break;
                case MenuUtils.FileOperationType.RENAME:
                    showRenameWindow(list);
                    break;
            }
        }
    };

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            currentFilePosition = position + 1;
            refreshTitleBarInfo();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

}
