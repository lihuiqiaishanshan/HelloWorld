package com.hisilicon.explorer.loader;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.storage.ExtraInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import com.hisilicon.explorer.model.GroupInfo;
import com.hisilicon.explorer.model.RootInfo;
import com.hisilicon.explorer.utils.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 */

public class LocalGroupInfoLoader extends BaseFileLoader<List<GroupInfo>> {

    private static final String TAG = LocalGroupInfoLoader.class.getSimpleName();

    private IProgress<List<GroupInfo>> iProgress;
    private Context mContext;

    public LocalGroupInfoLoader(IProgress<List<GroupInfo>> iProgress, Context context) {
        this.iProgress = iProgress;
        this.mContext = context;
    }

    @Override
    public List<GroupInfo> load() {
        if (iProgress != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    iProgress.onLoading();
                }
            });
        }
        if (Build.VERSION.SDK_INT <= 22) {
            return getGroupInfoBeforeM();
        } else {
            return getGroupInfoAfterM();
        }
    }

    @Override
    public void loadFail() {
        if (iProgress != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    iProgress.loadFail();
                }
            });
        }
    }

    @Override
    public void loadSuccess(List<GroupInfo> groupInfos) {
        if (iProgress != null) {
            iProgress.loadSuccess(groupInfos);
        }
    }

    private IBinder getMountService() {
        IBinder mount = null;
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            Method getService = sm.getDeclaredMethod("getService", String.class);
            mount = (IBinder) getService.invoke(sm, "mount");
        } catch (ClassNotFoundException e) {
            loadFail();
            LogUtils.LOGE(TAG, "ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            loadFail();
            LogUtils.LOGE(TAG, "NoSuchMethodException");
        } catch (InvocationTargetException e) {
            loadFail();
            LogUtils.LOGE(TAG, "InvocationTargetException");
        } catch (IllegalAccessException e) {
            loadFail();
            LogUtils.LOGE(TAG, "IllegalAccessException");
        }
        return mount;
    }

    private java.util.List<android.os.storage.ExtraInfo> getExtraInfo(IBinder mRemote) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        java.util.List<android.os.storage.ExtraInfo> _result = null;
        try {
            _data.writeInterfaceToken("IMountService");
            mRemote.transact(IBinder.FIRST_CALL_TRANSACTION + 35, _data, _reply, 0);
            _reply.readException();
            _result = _reply.createTypedArrayList(android.os.storage.ExtraInfo.CREATOR);
        } catch (RemoteException e) {
            loadFail();
           LogUtils.LOGE(TAG,"RemoteException");
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    /**
     * 6.0以前获取文件挂载信息
     * @return
     */
    private List<GroupInfo> getGroupInfoBeforeM() {
        List<ExtraInfo> extraInfos = getExtraInfo(getMountService());
        ArrayList<RootInfo> tempExtraInfos = new ArrayList<RootInfo>();
        if (null != extraInfos) {
        for (ExtraInfo exif : extraInfos) {
            RootInfo rootInfo = new RootInfo();
            rootInfo.setmDevType(exif.mDevType);
            rootInfo.setmLabel(exif.mLabel);
            rootInfo.setmUUID(exif.mUUID);
            rootInfo.setPath(exif.mMountPoint);
            tempExtraInfos.add(rootInfo);
        }
        }
        return classificationRootInfo(tempExtraInfos);
    }

    /**
     * 分类获取的目录
     * @param list 返回分类后的rootinfo
     * @return
     */
    private List<GroupInfo> classificationRootInfo(List<RootInfo> list) {
        ArrayList<GroupInfo> groupInfos = new ArrayList<GroupInfo>();
        ArrayList<RootInfo> sdcardRootInfos = new ArrayList<RootInfo>();
        ArrayList<RootInfo> sataRootInfos = new ArrayList<RootInfo>();
        ArrayList<RootInfo> usb2RootInfos = new ArrayList<RootInfo>();
        ArrayList<RootInfo> usb3RootInfos = new ArrayList<RootInfo>();
        ArrayList<RootInfo> unkownRootInfos = new ArrayList<RootInfo>();
        ArrayList<RootInfo> cdRootInfos = new ArrayList<RootInfo>();
        for (RootInfo rif : list) {
            if (rif.getPath() == null || rif.getPath().equals("/data")) {
                continue;
            }
            if (rif.getmDevType() == null) {
                rif.setmDevType("USB2.0");
            }
            if (rif.getPath().contains("/mnt/sdcard") || rif.getPath().contains("/storage/emulated/0")) {
                sdcardRootInfos.add(rif);
            } else if (rif.getmDevType().equals("SDCARD")) {
                sdcardRootInfos.add(rif);
            } else if (rif.getmDevType().equals("SATA")) {
                sataRootInfos.add(rif);
            } else if (rif.getmDevType().equals("USB2.0")) {
                usb2RootInfos.add(rif);
            } else if (rif.getmDevType().equals("USB3.0")) {
                usb3RootInfos.add(rif);
            } else if (rif.getmDevType().equals("UNKOWN")) {
                unkownRootInfos.add(rif);
            } else if (rif.getmDevType().equals("CD-ROM")) {
                cdRootInfos.add(rif);
            }
        }
        if (sdcardRootInfos.size() > 0) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setType("SDCARD");
            groupInfo.setRoots(sdcardRootInfos);
            groupInfos.add(groupInfo);
        }
        if (sataRootInfos.size() > 0) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setType("SATA");
            groupInfo.setRoots(sataRootInfos);
            groupInfos.add(groupInfo);
        }
        if (usb2RootInfos.size() > 0) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setType("USB2.0");
            groupInfo.setRoots(usb2RootInfos);
            groupInfos.add(groupInfo);
        }
        if (usb3RootInfos.size() > 0) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setType("USB3.0");
            groupInfo.setRoots(usb3RootInfos);
            groupInfos.add(groupInfo);
        }
        if (unkownRootInfos.size() > 0) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setType("UNKOWN");
            groupInfo.setRoots(unkownRootInfos);
            groupInfos.add(groupInfo);
        }
        if (cdRootInfos.size() > 0) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setType("CD-ROM");
            groupInfo.setRoots(cdRootInfos);
            groupInfos.add(groupInfo);
        }
        return groupInfos;
    }

    /**
     * 6.0后获取插入设备信息
     * @return 返回分组
     */
    private List<GroupInfo> getGroupInfoAfterM() {
        StorageManager st = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        ArrayList<RootInfo> rootInfos = new ArrayList<RootInfo>();
        try {
            Method getVolumes = st.getClass().getDeclaredMethod("getVolumes");
            List<VolumeInfo> mountList = (List<VolumeInfo>) getVolumes.invoke(st);
            for (VolumeInfo vi : mountList) {
                if (2 == vi.state) {   //only add deivces which state equals to mounted
                    RootInfo rootInfo = new RootInfo();
                    rootInfo.setmDevType(vi.devType);
                    if ("/storage/emulated".equals(vi.path)) {
                         vi.path = "/mnt/sdcard";
                    }
                    LogUtils.LOGD(TAG,"get mount path: "+vi.path+" type: "+vi.devType);
                    rootInfo.setPath(vi.path);
                    rootInfo.setmUUID(vi.fsUuid);
                    rootInfo.setmLabel(vi.fsLabel);
                    rootInfos.add(rootInfo);
                }
            }
        } catch (NoSuchMethodException e) {
            loadFail();
            LogUtils.LOGE(TAG, "NoSuchMethodException");
        } catch (InvocationTargetException e) {
            loadFail();
            LogUtils.LOGE(TAG, "InvocationTargetException");
        } catch (IllegalAccessException e) {
            loadFail();
            LogUtils.LOGE(TAG, "IllegalAccessException");
        }
        return classificationRootInfo(rootInfos);
    }
}
