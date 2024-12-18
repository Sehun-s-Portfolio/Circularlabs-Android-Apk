package com.ubx.rfid_demo.utils;

import android.device.DeviceManager;

public class DeviceHelper {

    /**
     *  是否开启 手柄按钮 扫码二维码 （扫描头出光）
     *  在自己的业务逻辑需要关闭或打开之前，调用该方法（注意：不能在按下手柄按钮后调用，因为按钮按下后就会触发出光扫码）。
     * @param isopen     true:开启   false:关闭
     */
    public static void setOpenScan523(boolean isopen) {
        DeviceManager mDeviceManager = new DeviceManager();
        if (mDeviceManager != null) {
            if (isopen) {
                //TODO 设置触发 523键值(手柄按钮) 扫描出光
                mDeviceManager.setSettingProperty("persist-persist.sys.rfid.key", "0-");
                mDeviceManager.setSettingProperty("persist-persist.sys.scan.key", "520-521-522-523-");//这里入参传入了哪些键值，在按下键值的的时候就会调起扫描头出光
            }else {
                //TODO 设置触发 523键值(手柄按钮) 不扫描出光
                mDeviceManager.setSettingProperty("persist-persist.sys.rfid.key", "0-");
                mDeviceManager.setSettingProperty("persist-persist.sys.scan.key", "520-521-522-");//这里入参传入了哪些键值，在按下键值的的时候就会调起扫描头出光
            }
        }
    }

    /**
     *  设置对应键值扫码二维码 （扫描头出光）
     * @param keyList     生效扫描的键值，用 - 隔开
     */
    public static void setScanKey(String keyList) {
        DeviceManager mDeviceManager = new DeviceManager();
        if (keyList==null){
            keyList = "";
        }
        if (mDeviceManager != null) {
            //TODO 设置触发  对应键值 扫描出光
            mDeviceManager.setSettingProperty("persist-persist.sys.rfid.key", "0-");
            mDeviceManager.setSettingProperty("persist-persist.sys.scan.key", keyList);//这里入参传入了哪些键值，在按下键值的的时候就会调起扫描头出光
        }
    }

}
