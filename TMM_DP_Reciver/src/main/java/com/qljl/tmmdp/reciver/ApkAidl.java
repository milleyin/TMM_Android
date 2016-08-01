package com.qljl.tmmdp.reciver;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import cn.trinea.android.common.util.PackageUtils;

/**
 * Created by luow on 2016/6/27.
 */
public class ApkAidl extends Service {
    String sdpath = Environment.getExternalStorageDirectory()
            + "/";
    String mSavePath = sdpath + "download";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private class MyBinder extends IMyAidlInterface.Stub{
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public int haveUpdate() throws RemoteException {
            // 获取packagemanager的实例
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = null;
            try {
                packInfo = packageManager.getPackageInfo("com.qljl.tmmdp.reciver",0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            int version = packInfo.versionCode;
            return version;
        }

        @Override
        public void updateApk(String apkName) throws RemoteException {
            if(PackageUtils.installSilent(getApplicationContext(), mSavePath + "/" + apkName)==1) {
                Intent intent3 = getPackageManager().getLaunchIntentForPackage("com.qljl.tmmdp");
                startActivity(intent3);
            }
        }


    }


}
