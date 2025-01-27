package com.qljl.tmmdp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.http.SslError;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.qljl.tmmdp.adapter.HorizontalScrollViewAdapter;
import com.qljl.tmmdp.biz.MainPresenter;
import com.qljl.tmmdp.entity.VersionMessage;
import com.qljl.tmmdp.interfaces.IMainView;
import com.qljl.tmmdp.modle.imageloader.TaskQueue;
import com.qljl.tmmdp.reciver.IMyAidlInterface;
import com.qljl.tmmdp.service.downloader.DownloadProgressListener;
import com.qljl.tmmdp.service.downloader.FileDownloader;
import com.qljl.tmmdp.upgrade.SilenceUpdate;
import com.qljl.tmmdp.video.FileUtils;
import com.qljl.tmmdp.video.VideoHttp;
import com.qljl.tmmdp.view.FullScreenVideoView;
import com.qljl.tmmdp.view.MyHorizontalScrollView;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 入口
 */
public class MainActivity extends Activity implements IMainView{
    final String TAG = " dp     MainActivity";
    private Context context;
    WebView contentWeb; //web容器
    FullScreenVideoView videoView;    //视频播放器
    ProgressBar progressBar;    //进度条
    MyHorizontalScrollView horizontalScrollView;    //横向滚动
    SilenceUpdate silenceUpdate;    //版本升级
    final int HAVE_UPDATE = 1;  //有更新
    final int PROCESSING = 2;   //下载ing
    final int FAILURE = 3;  //下载失败
    final int GET_VIDEOMESSAGEED = 4; //获取音频信息后
    final int AUTO = 5;
    final int TIMEOUT = 6;  //超时
    final int SET_VIDEO = 7;    //设置video
    final int SET_IMAGES = 8;   //设置图片列表
    final int UPDATE_TIME = 9;  //更新
    final int HIDE_IMGS = 10;   //隐藏图片
    final int HIDE_VEDIO = 11;  //隐藏视频
    final int HIDE_VIEW = 12;   //隐藏导航栏
    final int HAVE_SERVICE_APK_UPDATE = 13; //升级服务apk
    final int NOTHAVE_SERVICE_APK_UPDATE = 14;//没有升级
    int totalSize = 0;

    boolean isTest = false;  //是否是测试服
    String ApkUrl = ""; //apk更新地址
    String ServiceApkUrl = "";  //服务apk更新地址
    String homeUrl = "";//主页地址

    FileUtils fileUtils;
    VideoHttp videoHttp;
    VersionMessage versionMessage = null;
    String savePath = Environment.getExternalStorageDirectory() + "/TmmVideo/";
    String fileName = "";

    private long timeout = 10000;    //超时
    private Timer timer = null;

    private float width, height, marginTop, marginLeft;    //视频宽高处理
    private String playUrl;
    android.widget.MediaController mc = null;
    private List<String> imgList;   //图片列表
    private HorizontalScrollViewAdapter adapter;    //图片填充器
    private float imgWidth, imgHeight, imgMarginTop, imgMarginLeft, imgMarginRight, imgMarginBottom;
    String strs;

    IMyAidlInterface aidlInterface = null;
    int reciverApkVersion = 0;
    MySeiviceConnection connection = null;
    boolean isConnection = false;   //是否连接的aidl
    VersionMessage serviceVersionMessage = null;
    View decorView;
    boolean first_load = true;

    private MainPresenter mPresenter;

    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HAVE_UPDATE://apk有更新
                    print("apk有更新！");
                    silenceUpdate.downloadApk(aidlInterface);
                    break;
                case PROCESSING:// 下载时
                    int size = msg.getData().getInt("size");// 从消息中获取已经下载的数据长度
                    if (size == totalSize) {//下载完成
                        //把版本号写入版本文件
                        fileUtils.writeSDcard(versionMessage.getVersion() + "");
                        playVideo(savePath + fileName);
                        fileUtils.deleteOldVideo(fileName);
                    }
                    break;
                case FAILURE:
                    startVideoCheck();
                    break;
                case GET_VIDEOMESSAGEED:
                    print("videomessage:" + versionMessage);
                    checkVideo();
                    break;
                case TIMEOUT://超时
                    Toast.makeText(context,"请检查你的网络",Toast.LENGTH_SHORT).show();
                    break;
                case SET_VIDEO:
                    videoView.setVisibility(View.VISIBLE);
                    FrameLayout.LayoutParams lp;
                    lp = (FrameLayout.LayoutParams) videoView.getLayoutParams();
                    lp.width = px2dip(context, width);
                    lp.height = px2dip(context, height);
                    lp.setMargins(px2dip(context, marginLeft), px2dip(context, marginTop), 0, 0);
                    videoView.setLayoutParams(lp);
                    playVideo(playUrl);
                    break;
                case SET_IMAGES://设置图片列表
                    int uiOption = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                    decorView.setSystemUiVisibility(uiOption);
                    horizontalScrollView.setVisibility(View.VISIBLE);
                    imgList = null;
                    imgList = new ArrayList();
                    String[] str = strs.split(",");
                    for (int i = 0; i < str.length; i++) {
                        imgList.add(str[i]);
                    }
                    initHorizontalScrollView();
                    break;
                case UPDATE_TIME://凌晨更新
                    contentWeb.loadUrl(homeUrl);
                    checkVersion();
                    break;
                case HIDE_IMGS://隐藏图片
                    horizontalScrollView.setVisibility(View.GONE);
                    adapter = null;
                    break;
                case HIDE_VEDIO://隐藏视频
                    videoView.setVisibility(View.GONE);
                    if(videoView.isPlaying()){
                        videoView.pause();
                    }
                    break;
                case HIDE_VIEW://隐藏导航栏
                    break;
                case HAVE_SERVICE_APK_UPDATE://升级服务apk有更新
                    silenceUpdate.downloadServiceApk(serviceVersionMessage.getUrl());
                    break;
                case NOTHAVE_SERVICE_APK_UPDATE://升级服务apk没有更新或信息有误
                    checkVersion();
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_main);
        /* 是否是由服务apk启动此apk */
        Bundle bundle = this.getIntent().getExtras();
        if(bundle!=null && bundle.getString("from")!=null){
            print("onCreate:"+bundle.getString("from"));
        }
        context = MainActivity.this;
        initView();
        initData();
        fileUtils = new FileUtils(context);
        videoHttp = new VideoHttp(context);
        silenceUpdate = new SilenceUpdate(context);
        /* 日志保存在sd卡 */
      //  LogcatHelper.getInstance(this).start();
        /* 检测服务apk */
        checkServiceApk();
        timing();
        // startVideoCheck();
        /* 开启推送 */
        PushAgent mPushAgent = PushAgent.getInstance(context);
        mPushAgent.enable(new IUmengRegisterCallback() {

            @Override
            public void onRegistered(final String registrationId) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        //onRegistered方法的参数registrationId即是device_token
                        Log.d("lww device_token", registrationId);
                    }
                });
            }
        });
        PushAgent.getInstance(context).onAppStart();
        mPresenter = new MainPresenter(this);
    }

    /**
     * 初始化View
     */
    private void initView() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        contentWeb = (WebView) findViewById(R.id.contentWeb);
        horizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.horizontalScrollView);
        contentWeb.getSettings().setJavaScriptEnabled(true);
        contentWeb.addJavascriptInterface(getHtmlObject(), "jsObj");
        videoView = (FullScreenVideoView) findViewById(R.id.videoView);
        /* 配置web容器 */
        if(Build.VERSION.SDK_INT >= 19) {//WebView先不要自动加载图片，等页面finish后再发起图片加载
            contentWeb.getSettings().setLoadsImagesAutomatically(true);
        } else {
            contentWeb.getSettings().setLoadsImagesAutomatically(false);
        }
        contentWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        contentWeb.getSettings().setPluginState(WebSettings.PluginState.ON);
        contentWeb.getSettings().setAllowFileAccess(true);
        contentWeb.getSettings().setDefaultTextEncodingName("UTF-8");
        contentWeb.getSettings().setBuiltInZoomControls(true);
        contentWeb.getSettings().setDisplayZoomControls(false);
        contentWeb.getSettings().setSupportZoom(true);
        contentWeb.getSettings().setDomStorageEnabled(true);
        contentWeb.getSettings().setDatabaseEnabled(true);
        String databasePath = contentWeb.getContext().getDir("databases", Context.MODE_PRIVATE).getPath();
        contentWeb.getSettings().setDatabasePath(databasePath);
        /* 缓存 */
        contentWeb.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);// 设置缓冲大小，我设的是8M
        String appCacheDir = this.getApplicationContext()
                .getDir("cache", Context.MODE_PRIVATE).getPath();
        contentWeb.getSettings().setAppCachePath(appCacheDir);
        contentWeb.getSettings().setAllowFileAccess(true);
        //  contentWeb.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        contentWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Return the app name after finish loading
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }

                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        contentWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                handler.proceed();// Acceptance certificate
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // 这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
                view.loadUrl("file:///android_asset/notnet.html");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (first_load) {
                    timer = new Timer();
                    TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                        /*
                        * 超时后,首先判断页面加载进度,超时并且进度小于100,就执行超时后的动作
                        */
                            first_load = false;
                            if (progressBar.getProgress() < 100) {
                                Message msg = new Message();
                                msg.what = TIMEOUT;
                                mHandler.sendMessage(msg);
                                timer.cancel();
                                timer.purge();
                            }
                            if (progressBar.getProgress() == 100) {
                                if (timer != null) {
                                    timer.cancel();
                                    timer.purge();
                                }
                            }
                        }
                    };
                    timer.schedule(tt, timeout, 1);
                }

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                //恢复图片加载
                if(!contentWeb.getSettings().getLoadsImagesAutomatically()) {
                    contentWeb.getSettings().setLoadsImagesAutomatically(true);
                }
            }
        });
        //禁止复制粘贴
        contentWeb.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        handlerWeb.sendEmptyMessageDelayed(AUTO, 1000 * 3);
        /* 隐藏导航栏 */
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int i = decorView.getSystemUiVisibility();
                if (i == View.SYSTEM_UI_FLAG_VISIBLE) {
                    int uiOption = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                    decorView.setSystemUiVisibility(uiOption);
                }
                return true;
            }
        });
    }

    private void initData(){
        if(isTest){//测试服
            homeUrl ="http://pad.365tmm.net/front";
           // homeUrl = "http://192.168.0.77:8000/home.html";
            ApkUrl = "http://test2.365tmm.net/index.php?r=admin/tmm_software/query&pad=pad&apk=apk";
            ServiceApkUrl = "http://test2.365tmm.net/index.php?r=admin/tmm_software/query&pad=pad&dpr=dpr";
        }else{//正式服
            homeUrl ="https://pad.365tmm.com/front";
            ApkUrl = "http://m.365tmm.com/index.php?r=admin/tmm_software/query&pad=pad&apk=apk";
            ServiceApkUrl = "http://m.365tmm.com/index.php?r=admin/tmm_software/query&pad=pad&dpr=dpr";
        }
        contentWeb.loadUrl(homeUrl);
    }

    /**
     * 图片列表设置
     */
    private void initHorizontalScrollView() {
        try {
            adapter = new HorizontalScrollViewAdapter(context, imgList, imgWidth, imgHeight, imgMarginLeft, imgMarginTop, imgMarginRight, imgMarginBottom);
            horizontalScrollView.initDatas(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable TimerRunnable;
    private Handler TimerHandler = new Handler();

    // 启动定时器的方法
    public void timing() {
        if (TimerRunnable != null && TimerHandler != null) {
            //停止Timer
            TimerHandler.removeCallbacks(TimerRunnable);
        }
        // 获取到现在的时间  11:20:30  23:59:59
        Date now = new Date();
        // 当前小时
        int now_h = now.getHours();
        // 当前分
        int now_m = now.getMinutes();
        // 当前秒
        int now_s = now.getSeconds();
        // 0点时间  23:59:59
        int target_h = 23;
        int target_m = 59;
        int target_s = 59;
        // 计算差值
        int h = target_h - now_h;
        int m = target_m - now_m;
        final int s = target_s - now_s;
        // 计算差值
        long time = (h * 60 * 60 + m * 60 + s) * 1000;
        // 获取到 24点的时间值  当前时间到0点的时间差
        TimerRunnable = new Runnable() {
            public void run() {
                if (TimerHandler != null && TimerRunnable != null) {
                    // 更新
                    handler.sendEmptyMessage(UPDATE_TIME);
                }
            }
        };
        // 开始Timer
        TimerHandler.postDelayed(TimerRunnable, time);
    }

    private Handler handlerWeb = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case AUTO://播放视频
                    contentWeb.loadUrl("javascript: autoplay()");
                    handlerWeb.sendEmptyMessageDelayed(AUTO, 1000 * 3);
                    break;
            }
        }
    };


    private Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            //检测服务器Video是否有更新
            getVideoMessage();
            handlerWeb.postDelayed(this, 1000 * 60 * 60 * 24);
        }
    };

    /**
     * 开启Video检测
     */
    private void startVideoCheck() {
        //开始计时
        mHandler.removeCallbacks(runnable);
        mHandler.postDelayed(runnable, 0);
    }

    public void getVideoMessage() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                versionMessage = videoHttp.getVideoMessage("http://192.168.0.111/test/videoMessage"); //获取音频版本信息
                handler.sendEmptyMessage(GET_VIDEOMESSAGEED);
            }
        }.start();
    }

    /**
     * 检测Video是否存在
     */
    private void checkVideo() {
        if (fileUtils.hasSDcard()) { // 获取SDCard是否存在
            File saveDir = new File(savePath);
            //目录是否存在
            if (saveDir.exists()) {//如果目录存在需要对比版本
                if (fileUtils.getOldVideo() != null) {
                    print(fileUtils.getOldVideo());
                    File f = new File(fileUtils.getOldVideo());
                    //如果文件存在直接播放
                    if (f.exists()) {
                        playVideo(fileUtils.getOldVideo());
                    }
                }
                if (versionMessage != null) {
                    if (fileUtils.readSDcard() == null || "".equals(fileUtils.readSDcard())) {
                    } else {
                        if (Integer.parseInt(fileUtils.readSDcard()) < versionMessage.getVersion()) {//如果
                            print("有更新:" + versionMessage.getUrl());
                            download(versionMessage.getUrl(), saveDir);
                        }
                    }
                }
            } else {//如果没有忽略对比直接下载video
                if (versionMessage != null) {
                    download(versionMessage.getUrl(), saveDir);
                }
            }

        }
    }

    /**
     * 初始化Video
     */
    private void playVideo(String file) {
        //   File video = new File(file);
        //  if (video.exists()) {//判断我们的video文件是否存在,如果存在就播放
        videoView.setVideoPath(file);// 获取视频文件的绝对路径
        // 设置videoView与mController建立关联ontext
        if(mc == null) {
            mc = new android.widget.MediaController(this);
            mc.setVisibility(View.GONE);
        }
        videoView.setMediaController(mc);
        // 让VideoView获取焦点
        videoView.requestFocus();
             /* 调用VideoView.start()自动播放 */
        videoView.start();
        //循环播放
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.start();
                mp.setLooping(true);
            }
        });
        // }
    }

    private Object getHtmlObject() {
        Object insertObj = new Object() {
            @JavascriptInterface
            public String getSerialNumber() {//序列号
                /*final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                return tm.getDeviceId() + "";*/
                WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

                WifiInfo info = wifi.getConnectionInfo();
                return info.getMacAddress();
            }

            /**
             * 提示信息
             * @param str
             */
            @JavascriptInterface
            public void prompt(String str) {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }

            /**
             * 关闭定时器
             */
            @JavascriptInterface
            public void removeTimeHandler() {
                //移除播放视频定时器
                handlerWeb.removeMessages(AUTO);
            }


            /**
             * 设置视频播放
             * @param videowidth
             * @param videoheight
             * @param videomarginTop
             * @param videomarginLeft
             * @param url
             */
            @JavascriptInterface
            public void setVideo(float videowidth, float videoheight, float videomarginTop, float videomarginLeft, String url) {
                width = videowidth;
                height = videoheight;
                marginTop = videomarginTop;
                marginLeft = videomarginLeft;
                playUrl = url;
                handler.sendEmptyMessage(SET_VIDEO);
            }

            /**
             * 设置图片列表
             * @param imgwidth
             * @param imgheight
             * @param imgmarginLeft
             * @param imgmarginRight
             */
            @JavascriptInterface
            public void setImages(float imgwidth, float imgheight, float imgmarginTop, float imgmarginLeft, float imgmarginRight, float imgmarginBottom, String arrays) {
                imgWidth = imgwidth;
                imgHeight = imgheight;
                imgMarginTop = imgmarginTop;
                imgMarginLeft = imgmarginLeft;
                imgMarginRight = imgmarginRight;
                imgMarginBottom = imgmarginBottom;
                strs = arrays;
                handler.sendEmptyMessage(SET_IMAGES);
            }

            /**
             * 隐藏图片集
             */
            @JavascriptInterface
            public void hideImages(){
                handler.sendEmptyMessage(HIDE_IMGS);
            }

            /**
             * 隐藏视频
             */
            @JavascriptInterface
            public void hideVedio(){
                handler.sendEmptyMessage(HIDE_VEDIO);
            }

            /**
             * 隐藏导航栏
             */
            @JavascriptInterface
            public void hideNavigationBar(){
                handler.sendEmptyMessage(HIDE_VIEW);
            }
        };
        return insertObj;
    }

    /**
     * 版本检查
     */
    private void checkVersion() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                boolean bool = silenceUpdate.versionUpdate(ApkUrl);
                if (bool) {
                    handler.sendEmptyMessage(HAVE_UPDATE);
                }
            }
        }.start();
    }

    /**
     * 升级服务apk检查
     */
    private void checkServiceApk() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    serviceVersionMessage = silenceUpdate.getReciverVersion(ServiceApkUrl);
                    //检查机器上有没有安装服务apk
                    if(isAppInstalled(context,"com.qljl.tmmdp.reciver")) {
                        print("versionReciver:" + serviceVersionMessage.getVersion());
                        connection = new MySeiviceConnection();
                        Intent intent = new Intent("com.qljl.tmmdp.reciver.aidl");
                        bindService(intent, connection, BIND_AUTO_CREATE);
                    }else{//如果没有安装直接安装
                        handler.sendEmptyMessage(HAVE_SERVICE_APK_UPDATE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 检查指定apk是否已经安装
     * @param context       上下文
     * @param packageName   apk包名
     * @return
     */
    public static boolean isAppInstalled(Context context,String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed =false;
        try {
            pm.getPackageInfo(packageName,PackageManager.GET_ACTIVITIES);
            installed =true;
        } catch(PackageManager.NameNotFoundException e) {
            //捕捉到异常,说明未安装
            installed =false;
        }
        return installed;
    }

    // 由于用户的输入事件（点击button，触摸屏幕...）是由主线程负责处理的
    // 如果主线程处于工作状态
    // 此时用户产生的输入时间如果没能在5秒内得到处理，系统就会报应用无响应的错误
    // 所以在主线程里不能执行一件比较耗时的工作，否则会因主线程阻塞而无法处理用户的输入事件
    // 导致“应用无响应”错误的出现，耗时的工作应该在子线程里执行
    private DownloadTask task;// 声明下载执行者

    /**
     * 退出下载
     */
    public void exit() {
        if (task != null)
            task.exit();
    }

    /**
     * 下载资源，声明下载执行者并开辟线程开始下载
     *
     * @param path    下载的路径
     * @param saveDir 保存文件
     */
    private void download(String path, File saveDir) {
        task = new DownloadTask(path, saveDir);// 实例化下载业务
        new Thread(task).start();
    }

    /**
     * UI控制画面的重绘（更新）是由主线程负责处理的，如果在子线程中更新UI控件值，更新后值不会重绘到屏幕上
     * 一定要在主线程里更新UI控件的值，这样才能在屏幕上显示出来，不能在子线程中更新UI控件的值
     */
    private final class DownloadTask implements Runnable {
        private String path;// 下载路径
        private File saveDir;// 下载到保存到的文件
        private FileDownloader loader;// 文件下载器（下载线程的容器）

        /**
         * 构造方法，实现变量的初始化
         *
         * @param path    下载路径
         * @param saveDir 下载要保存到的文件
         */
        public DownloadTask(String path, File saveDir) {
            this.path = path;
            this.saveDir = saveDir;
        }

        /**
         * 退出下载
         */
        public void exit() {
            if (loader != null)
                loader.exit();// 如果下载器存在的话则退出下载
        }

        DownloadProgressListener downloadProgressListener = new DownloadProgressListener() {
            /**
             * 下载的文件长度会不断地被传入该回调方法
             */
            public void onDownloadSize(int size) {
                Message msg = new Message();
                msg.what = PROCESSING;
                msg.getData().putInt("size", size);
                fileName = loader.filename;
                handler.sendMessage(msg);
            }
        };

        public void run() {
            // TODO Auto-generated method stub
            try {
                loader = new FileDownloader(getApplicationContext(), path,
                        saveDir, 3);
                totalSize = loader.getFileSize();// 设置进度条的最大刻度
                loader.download(downloadProgressListener);
            } catch (Exception e) {//下载失败
                // TODO: handle exception
                e.printStackTrace();
                handler.sendMessage(handler.obtainMessage(FAILURE));

            }
        }
    }

    /**
     * dp转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * log打印
     *
     * @param str
     */
    private void print(String str) {
        Log.d(TAG, str);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //重置计时
        mPresenter.resetTipsTimer();
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
           /* if(videoView.isPlaying()){
                videoView.pause();
            }
            if(isConnection){
                unbindService(connection);
                isConnection = false;
            }*/
          // LogcatHelper.getInstance(context).stop();
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        TaskQueue.TASK_QUEUE.resumeAllTask(this);
        int uiOption = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOption);
        //启动默认开始计时
        mPresenter.startTipsTimer();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        TaskQueue.TASK_QUEUE.pauseAllTask(this);
        if(isConnection){
            unbindService(connection);
            isConnection = false;
        }
        //有其他操作时结束计时
        mPresenter.endTipsTimer();
    }



    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        TaskQueue.TASK_QUEUE.destroyAllTask(this);
       // LogcatHelper.getInstance(context).stop();
        if(timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(videoView != null){
            videoView.stopPlayback();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        print("-.-onRestart");
        Bundle bundle = this.getIntent().getExtras();
        if(bundle!=null && bundle.getString("from")!=null){
            print("onRestart:"+bundle.getString("from"));
            if("fromService".equals(bundle.getString("from"))){
                print("-.-检测升级");
                checkVersion();
            }
        }
    }

    private class MySeiviceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            aidlInterface = IMyAidlInterface.Stub.asInterface(service);
            if(aidlInterface == null){
                return;
            }
            isConnection = true;
            try {
                reciverApkVersion = aidlInterface.haveUpdate();
                print("服务apkVersion:"+reciverApkVersion);
                if (reciverApkVersion > 0 && reciverApkVersion < serviceVersionMessage.getVersion()) {//服务apk有升级
                    handler.sendEmptyMessage(HAVE_SERVICE_APK_UPDATE);
                } else {
                    handler.sendEmptyMessage(NOTHAVE_SERVICE_APK_UPDATE);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

  /*  @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
       //重置计时
        mPresenter.resetTipsTimer();
        return false;
    }*/

   /* @Override
    protected void onResume()
    {
        //启动默认开始计时
        mPresenter.startTipsTimer();
        super.onResume();
    }*/

    /*@Override
    protected void onPause()
    {
        //有其他操作时结束计时
        mPresenter.endTipsTimer();
        super.onPause();
    }*/

    @Override
    public void showTipsView()
    {
        //展示屏保界面
        Intent intent = new Intent(MainActivity.this, ScreenSaversActivity.class);
        startActivity(intent);
    }
}
