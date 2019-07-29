//package com.li.demo.test;
//
//import android.os.Environment;
//
//
//private IMountService mMountService
//
//public class DeviceHelper {
//
//	public  DeviceHelper() {
//
//		mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
//		
//		
//
//	}
//	
//	
//    /**
//     * Gets the state of a volume via its mountpoint.
//     * @param mountPoint point for volume
//     * @return the state of the volume
//     */
//    public String getVolumeState(String mountPoint) {
//    	//System.out.println("Come in KKStoragemanager getVolumeState");
//    	if (mMountService == null) return Environment.MEDIA_REMOVED;
//        try {
//            return mMountService.getVolumeState(mountPoint);
//        } catch (RemoteException e) {
//            //Log.e(TAG, "Failed to get volume state", e);
//        	com.konka.android.util.LogPrint.error("konka add_on", java.lang.Thread.currentThread().getStackTrace()[2].getMethodName());
//            return null;
//        }
//
//    }
//	
//    /**
//     * Returns list of paths for all mountable volumes.
//     * @return list of paths for all mountable volumes
//     */
//    public String[] getVolumePaths() {
//    	//System.out.println("Come in KKStoragemanager getVolumePaths");
//    	StorageVolume[] volumes = getVolumeList();
//        if (volumes == null) return null;
//        int count = volumes.length;
//        String[] paths = new String[count];
//        for (int i = 0; i < count; i++) {
//            paths[i] = volumes[i].getPath();
//        }
//        com.konka.android.util.LogPrint.error("konka add_on", java.lang.Thread.currentThread().getStackTrace()[2].getMethodName());
//        return paths;
//        //return null;
//    }
//    
//	/**
//     * Returns list of all mountable volumes.
//     * @hide
//     */
//    private StorageVolume[] getVolumeList() {
//    	//System.out.println("Come in KKStoragemanager getVolumeList");
//    	com.konka.android.util.LogPrint.error("konka add_on", java.lang.Thread.currentThread().getStackTrace()[2].getMethodName());
//    	if (mMountService == null) return new StorageVolume[0];
//        try {
//              Parcelable[] list = mMountService.getVolumeList();
//              ArrayList<StorageVolume> result = new ArrayList<StorageVolume>();
//              for (int i=0;i<list.length;i++){
//                    StorageVolume volume = (StorageVolume) list[i];
//                    if (!volume.getPath().startsWith("/mnt/samba")){
//                        result.add(volume);
//                        continue;
//                    }
//                    System.out.println(volume);
//              }
//              return (StorageVolume[])result.toArray(new StorageVolume[result.size()]);
//        }catch(RemoteException e){
//            System.out.println(e);
//        }
//        
//        return null;
//    }
//}
