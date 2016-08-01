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
       /* Log.e("lww   ...", "receiver intent:" + intent.toString());
        if(intent.getAction().equals("COM.MESSAGE")){
            Intent intent2 = new Intent(context, MainActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//注意,不能少
            context.startActivity(intent2);
        }*/
        if (intent.getAction().equals(ACTION)) {
            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }
}
