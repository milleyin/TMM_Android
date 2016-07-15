package com.qljl.tmmdp.upgrade;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.qljl.tmmdp.entity.VersionMessage;
import com.qljl.tmmdp.modle.HttpHelper;
import com.qljl.tmmdp.reciver.IMyAidlInterface;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.trinea.android.common.util.PackageUtils;

/**
 * 静默升级
 * Created by luow on 2016/6/23.
 */
public class SilenceUpdate {
    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    /* 下载服务apk结束 */
    private static final int DOWNLOAD_SERVICE_APK_FINISH = 3;
    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelUpdate = false;

    private Context mContext;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private Dialog mDownloadDialog;

    HttpHelper httpHelper;
    VersionMessage versionMessage;
    final String apk_name = "TMM_DP";
    final String service_apk_name = "TMM_DP_Reciver";
    String service_apk_url = "";
    IMyAidlInterface aidlInterface = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                   // mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    installApk2();
                    break;
                case DOWNLOAD_SERVICE_APK_FINISH:
                    if(PackageUtils.installSilent(mContext, mSavePath + "/" + service_apk_name)==1) {
                        /*Intent intent3 = mContext.getPackageManager().getLaunchIntentForPackage("com.qljl.tmmdp.reciver");
                        mContext.startActivity(intent3);*/
                        Intent intent3 = new Intent();
                        ComponentName cn = new ComponentName("com.qljl.tmmdp.reciver","com.qljl.tmmdp.reciver.MainActivity");
                        intent3.setComponent(cn);
                        intent3.setAction("android.intent.action.MAIN");
                        try {
                            mContext.startActivity(intent3);
                        } catch (Exception e) {
                            Toast.makeText(mContext, "没有该子APP，请下载安装", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public SilenceUpdate(Context context) {
        this.mContext = context;
        httpHelper = new HttpHelper(context);
        this.mContext = context;
    }


    /**
     * 更新
     */
    public boolean versionUpdate(String url) {
        try {
            Response response = httpHelper.getRequest(url);
            if (null != response) {
                String jsonStr = response.body().string();
                if (jsonStr != "" || jsonStr != null) {
                    JSONObject resultJson = new JSONObject(jsonStr);
                    versionMessage = new VersionMessage();
                    versionMessage.setVersion(resultJson.getInt("version"));
                    versionMessage.setUrl(resultJson.getString("down_url"));
                    int oldVersion = getVersionCode(mContext);
                    //System.out.println("lw   oldVersion:"+oldVersion+",updateVersion:"+versionMessage.getVersion());
                    if(versionMessage.getVersion()>oldVersion){
                        return true;
                        // 显示提示对话框
                        //showNoticeDialog();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 获取广播辅助app在服务器上的版本
     * @param url
     * @return
     */
    public VersionMessage getReciverVersion(String url) {
        try {
            Response response = httpHelper.getRequest(url);
            if (null != response) {
                String jsonStr = response.body().string();
                if (jsonStr != "" || jsonStr != null) {
                    JSONObject resultJson = new JSONObject(jsonStr);
                    VersionMessage vm = new VersionMessage();
                    vm.setVersion(resultJson.getInt("version"));
                    vm.setUrl(resultJson.getString("down_url"));
                    return vm;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(
                    "com.qljl.tmmdp", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 显示软件更新对话框
     *//*
    public void showNoticeDialog() {
        System.out.println("lw     showNoticeDialog 更新提示框！");
        // 构造对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.soft_update_title);
        builder.setMessage(R.string.soft_update_info);
        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 显示下载对话框
                        showDownloadDialog();
                    }
                });
        // 稍后更新
        builder.setNegativeButton(R.string.soft_update_later,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    *//**
     * 显示软件下载对话框
     *//*
    private void showDownloadDialog() {
        // 构造软件下载对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.soft_updating);
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        builder.setView(v);
        // 取消更新
        builder.setNegativeButton(R.string.soft_update_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 设置取消状态
                        cancelUpdate = true;
                    }
                });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();
        // 现在文件
        downloadApk();
    }*/

    /**
     * 下载apk文件
     */
    public void downloadServiceApk(String url) {
        service_apk_url = url;
        // 启动新线程下载软件
        new downloadServiceApkThread().start();
    }

    /**
     * 下载文件线程
     *
     * @author coolszy
     * @date 2012-4-26
     * @blog http://blog.92coding.com
     */
    private class downloadServiceApkThread extends Thread {

        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory()
                            + "/";
                    mSavePath = sdpath + "download";
                    URL url = new URL(service_apk_url);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, service_apk_name);
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                      /*  // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);*/
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_SERVICE_APK_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 取消下载对话框显示
         //   mDownloadDialog.dismiss();
        }
    }

    /**
     * 下载apk文件
     */
    public void downloadApk(IMyAidlInterface iMyAidlInterface) {
         aidlInterface = iMyAidlInterface;
        // 启动新线程下载软件
        new downloadApkThread().start();
    }

    /**
     * 下载文件线程
     *
     * @author coolszy
     * @date 2012-4-26
     * @blog http://blog.92coding.com
     */
    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory()
                            + "/";
                    mSavePath = sdpath + "download";
                    URL url = new URL(versionMessage.getUrl());
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, apk_name);
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                      //  progress = (int) (((float) count / length) * 100);
                        // 更新进度
                       // mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 取消下载对话框显示
            //   mDownloadDialog.dismiss();
        }
    }


    /**
     * 安装APK文件
     */
    private void installApk() {
        System.out.println("lw   installApk");
        File apkfile = new File(mSavePath, apk_name);
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 静默安装
     */
    private void installApk2(){
        try {
            if(aidlInterface != null) {
                aidlInterface.updateApk(apk_name);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
