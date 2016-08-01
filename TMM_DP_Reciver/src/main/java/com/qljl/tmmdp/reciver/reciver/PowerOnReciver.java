package com.qljl.tmmdp.reciver.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qljl.tmmdp.reciver.MainActivity;

/**
 * Created by luow on 2016/6/17.
 */
public class PowerOnReciver extends BroadcastReceiver {
   /* String sdpath = Environment.getExternalStorageDirectory()
            + "/";
    String mSavePath = sdpath + "download";*/
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
       /* if(intent.getAction().equals("COM.MESSAGE")){
            try {
                Log.e("lww ","更新主apk");
                String apk_name = intent.getExtras().getString("message");
                if(PackageUtils.installSilent(context, mSavePath + "/" + apk_name)==1) {
                    Intent intent3 = context.getPackageManager().getLaunchIntentForPackage("com.qljl.tmmdp");
                    context.startActivity(intent3);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }*/
        if (intent.getAction().equals(ACTION)) {//开机启动
            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }

}
