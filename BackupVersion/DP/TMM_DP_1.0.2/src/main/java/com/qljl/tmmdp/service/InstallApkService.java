package com.qljl.tmmdp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qljl.tmmdp.MainActivity;

/**
 * Created by luow on 2016/6/17.
 */
public class InstallApkService extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        /*if(intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            Log.e("lw   ","删除了一个程序");
        }
        if(intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            Log.e("lw   ","升级了一个程序");
        }
        if(intent.getAction().equals("android.intent.action.PACKAGE_ADDED")){//新安装了一个apk
            Log.e("lw    ","新装了一个apk");
            Toast.makeText(context,"安装了一个新apk",Toast.LENGTH_LONG).show();
            if(intent.getDataString().equals("com.qljl.tmmdp")){//如果安装的是自己的apk则启动
                Intent intent2 = new Intent(context, MainActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//注意,不能少
                context.startActivity(intent2);
            }
        }*/
        if (intent.getAction().equals(ACTION)) {
            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }
}
