package com.hisilicon.tvui.util;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.HashMap;
import java.util.Iterator;

public class UsbManagerPrivate {
    UsbManager usbManager = null;
    final static int MASS_STORAGE = 8;

    public UsbManagerPrivate(Context context) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    public boolean hasExternalStorgeDevice() {
        if (usbManager != null) {
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            LogTool.d(LogTool.MPLAY, "UsbManagerPrivate deviceIterator.size:" + deviceList.size());
            while (deviceIterator.hasNext()) {
                UsbDevice usbDevice = deviceIterator.next();
                int deviceClass = usbDevice.getDeviceClass();
                if (deviceClass == 0) {
                    UsbInterface anInterface = usbDevice.getInterface(0);
                    if (anInterface.getInterfaceClass() == MASS_STORAGE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
