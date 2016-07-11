package com.qljl.tmm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.qljl.tmm.http.HttpHelper;
import com.qljl.tmm.pay.Pays;
import com.qljl.tmm.upgrade.UpdateManager;
//import android.webkit.WebSettings;
import com.tencent.smtt.sdk.WebSettings;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

//import android.webkit.WebChromeClient;

/**
 * WebView
 * 
 * @author lw
 * 
 */
public class WebActivity extends Activity implements OnClickListener {
	private static final String TAG = "WebActivity";
	private Context context;
	private WebView webView;
	private ProgressBar progressBar;

	//觅鲜布局，页面跳转有赞布局
	private FrameLayout otherLinear, otherLinear2;
	//觅鲜返回按钮，页面跳转有赞的返回按钮
	private ImageView backImg, backImg2,shareImg;
	//页面跳转有赞的WebView
	private WebView /* otherWebView, */otherWebView2;
	//觅鲜WebView
	private com.tencent.smtt.sdk.WebView otherWebView;
	public String urls = "";
	
	public static HttpHelper httpHelper;
	private long mExitTime;
	//锁
	public static Lock lock;
	Pays pay;//支付结果
	UpdateManager manager;
	private double latitude, longitude;// 经纬度
	public boolean firstLocation = true;
	public String currentCity = "";
	//分享属性
	public String shareTitle,shareImage,shareUrls,shareDetail;
	boolean isFirst = true;

	// Handler:is first user
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				webView.loadUrl("javascript: window.device.getLocationCallBack('"
						+ longitude + "','"
						+ latitude + "','"
						+ currentCity + "')");
				mAMapLocationManager.removeUpdates(aMapLocationListener);
				mAMapLocationManager.destory();
				mAMapLocationManager = null;
				break;
			case 2:
				break;
			case 3:
				initWeb(urls);
				break;
			case 4:
				if (urls != "") {
					otherLinear.setVisibility(View.GONE);
				}
				break;
			case 5:
				backImg.setVisibility(View.VISIBLE);
				break;
			case 6:
				backImg.setVisibility(View.GONE);
				break;
			case 7:
				initWeb2(urls);
				break;
			case 8:
				WindowManager wm = (WindowManager) context
						.getSystemService(Context.WINDOW_SERVICE);
				int width = wm.getDefaultDisplay().getWidth();
				int height = wm.getDefaultDisplay().getHeight();
				DisplayMetrics metric = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metric);
				int densityDpi = metric.densityDpi;
				float density = metric.density;
				LayoutParams lp;
				lp = (LayoutParams) otherLinear.getLayoutParams();
				lp.width = width;
				lp.height = (int) (height - getStatusBarHeight() - 50 * density);
				otherLinear.setLayoutParams(lp);
				break;
			case 9:
				WindowManager wm2 = (WindowManager) context
						.getSystemService(Context.WINDOW_SERVICE);
				int width2 = wm2.getDefaultDisplay().getWidth();
				int height2 = wm2.getDefaultDisplay().getHeight();
				DisplayMetrics metric2 = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metric2);
				float density2 = metric2.density;
				LayoutParams lp2;
				lp2 = (LayoutParams) otherLinear.getLayoutParams();
				lp2.width = width2;
				lp2.height = height2 - getStatusBarHeight();
				otherLinear.setLayoutParams(lp2);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		context = WebActivity.this;
		// init view
		initView();
		loadWebView();
		/** init HttpHelper class */
		httpHelper = new HttpHelper(context);
		/** init aliyun */
		// initOneSDK(this);
		/** init UpdateManager */
		manager = new UpdateManager(context);
		/** Check software update */
		new Thread() {

			@Override
			public void run() {
				manager.checkUpdate();
			}

		}.start();
		// manager.checkUpdate();
		/** youmeng */
		checkVersion();

		// weixin
		// WeiXinShare wxs = new WeiXinShare();
		// // wxs.wechatShare(0, context, null);// friend
		// wxs.wechatShare(1, context, "test!");// Micro channel circle
	}
	
	/**
	 * 加载主页面
	 */
	private void loadWebView() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");       
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
		String str = formatter.format(curDate); 
		webView.loadUrl("https://m.365tmm.com/user/index.html"+"?"+str);
//		webView.loadUrl("http://test2.365tmm.net/user/index.html"+"?"+str);
//		webView.loadUrl("http://47.88.31.202:8888");
		//设置JS回调
		webView.addJavascriptInterface(getHtmlObject(), "jsObj");
//		webView.loadUrl("javascript: alert('123')");
	}

	/** 
	 *  友盟检测升级
	 *  */
	private void checkVersion() {
		UmengUpdateAgent.setUpdateOnlyWifi(true);
		UmengUpdateAgent.setUpdateAutoPopup(true);
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

			@Override
			public void onUpdateReturned(int updateStatus,
					UpdateResponse updateInfo) {
				// if (updateStatus == 0 && updateInfo != null) {
				// showUpdateDialog(updateInfo.path, updateInfo.updateLog);
				// }
				switch (updateStatus) {
				case 0: // has update
					break;
				case 1: // has no update
					break;
				case 2: // none wifi
					break;
				case 3: // time out
					break;
				default:
					break;
				}
			}
		});

		UmengUpdateAgent.update(this);
	}

	/**
	 * 初始化AlibabaSDK
	 * 
	 * @param applicationContext
	 */
	// private void initOneSDK(final Context applicationContext) {
	//
	// // AlibabaSDK.setEnvironment(Environment.ONLINE);
	// AlibabaSDK.setEnvironment(com.alibaba.sdk.android.Environment.ONLINE);
	//
	// AlibabaSDK.asyncInit(applicationContext, new InitResultCallback() {
	//
	// @Override
	// public void onSuccess() {
	// Log.d(TAG, "init onesdk success");
	// // alibabaSDK初始化成功后，初始化云推送通道
	// initCloudChannel(applicationContext);
	// }
	//
	// @Override
	// public void onFailure(int code, String message) {
	// Log.e(TAG, "init onesdk failed : " + message);
	//
	// AlertDialog.Builder dialog = new AlertDialog.Builder(
	// getApplicationContext());
	// dialog.setTitle("Duang ~");
	// dialog.setIcon(android.R.drawable.ic_dialog_info);
	// dialog.setMessage("SDK init failed");
	// dialog.setPositiveButton("好吧",
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	// System.exit(0);
	// }
	// });
	// AlertDialog mDialog = dialog.create();
	// mDialog.getWindow().setType(
	// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	// mDialog.show();
	// }
	// });
	// }

	/**
	 * 初始化云推送通道
	 * 
	 * @param applicationContext
	 */
	// private void initCloudChannel(Context applicationContext) {
	//
	// CloudChannelConstants.ENV = CloudChannelEnv.ONLINE;
	// CloudChannelConstants.setCustomConfig(
	// CloudChannelConstants.KEY_OF_DATA_RESPONSE_TIMEOUT,
	// (60 * 1000 * 5) + "");
	//
	// CloudPushService cloudPushService = AlibabaSDK
	// .getService(CloudPushService.class);
	// if (cloudPushService != null) {
	// cloudPushService.register(applicationContext,
	// new RunnableCallbackAdapter<Void>() {
	// @Override
	// public void onSuccess(Void result) {
	// Log.d(CloudChannelConstants.TAG,
	// "init cloudchannel success");
	// // Toast.makeText(getApplicationContext(),
	// // "Success",
	// // Toast.LENGTH_SHORT).show();
	// }
	//
	// @Override
	// public void onFailed(Exception exception) {
	// Log.d(CloudChannelConstants.TAG,
	// "init cloudchannel failed");
	// Toast.makeText(getApplicationContext(), "Failed",
	// Toast.LENGTH_SHORT).show();
	// System.out
	// .println("lw      init cloudchannel failed ");
	// }
	// });
	// } else {
	// Log.i(CloudChannelConstants.TAG, "CloudPushService is null");
	// }
	// }

	/**
	 * youmeng update Dialog
	 * 
	 * @param downloadUrl
	 * @param message
	 */
	private void showUpdateDialog(final String downloadUrl, final String message) {
		AlertDialog.Builder updateAlertDialog = new AlertDialog.Builder(this);
		updateAlertDialog.setIcon(R.drawable.back);
		updateAlertDialog.setTitle(R.string.app_name);
		updateAlertDialog.setMessage(getString(R.string.soft_update_info,
				message));
		updateAlertDialog.setNegativeButton(R.string.soft_update_updatebtn,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri
									.parse(downloadUrl)));
						} catch (Exception ex) {

						}
					}
				}).setPositiveButton(R.string.soft_update_cancel, null);
		if (!isFinishing())
			updateAlertDialog.show();
	}

	/**
	 * init view
	 */
	@SuppressLint("NewApi")
	private void initView() {
		shareImg = (ImageView) findViewById(R.id.shareImg);
		shareImg.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		webView = (WebView) findViewById(R.id.webView);
		webView.requestFocus();
		// Set the WebView property to execute the Javascript script
		webView.getSettings().setJavaScriptEnabled(true);
		// Technical settings
		webView.getSettings().setSupportZoom(true); // Scale switch
		webView.getSettings().setSupportMultipleWindows(true); // Multi window
		// start Application H5 Caches
		webView.getSettings().setAppCacheEnabled(true);
		// strat databases torage API
		webView.getSettings().setDatabaseEnabled(true);
		// start DOMstorage API
		webView.getSettings().setDomStorageEnabled(true);

		/* 缓存 */
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);// 设置缓冲大小，我设的是8M
		String appCacheDir = this.getApplicationContext()
				.getDir("cache", Context.MODE_PRIVATE).getPath();
		webView.getSettings().setAppCachePath(appCacheDir);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();// Acceptance certificate
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
//				System.out.println("lw     onReceivedSslError");
				// 这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
				// view.loadData(errorHtml, "text/html", "UTF-8");
				view.loadUrl("file:///android_asset/notnet.html");
			}


			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
				if(isFirst){
					isFirst = false;
					getLocation();// 获取经纬度
				}
			}
		});
		// webView.setWebChromeClient(new WebChrome());
		/** WebView Listener */
		webView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
						if (otherLinear.getVisibility() == View.VISIBLE) {
							otherLinear.setVisibility(View.GONE);
						}
						if (otherLinear2.getVisibility() == View.VISIBLE) {
							otherLinear2.setVisibility(View.GONE);
						} else {
							webView.goBack(); // back
						}
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_BACK) {
						if ((System.currentTimeMillis() - mExitTime) > 2000) {
							Object mHelperUtils;
							Toast.makeText(context, "再按一次退出！",

							Toast.LENGTH_SHORT).show();
							mExitTime = System.currentTimeMillis();

						} else {
							 finish();
						}
						return true;
					}
				}
				return false;
			}
		});
		int sysVersion = Integer.parseInt(VERSION.SDK);
		if (sysVersion > 20) {
			webView.getSettings().setMixedContentMode(2);
			CookieManager.getInstance().setAcceptThirdPartyCookies(webView,
					true);
		}
		
		android.webkit.WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDefaultTextEncodingName("utf-8");
		webView.setWebChromeClient(new WebChromeClient() {
		});

		backImg = (ImageView) findViewById(R.id.backImg);
		backImg.setOnClickListener(this);
		backImg2 = (ImageView) findViewById(R.id.backImg2);
		backImg2.setOnClickListener(this);
		otherLinear = (FrameLayout) findViewById(R.id.otherLinear);
		otherLinear2 = (FrameLayout) findViewById(R.id.otherLinear2);
	}

	private boolean isRedirect = false;// 是否重定向
	private ArrayList<String> loadHistoryUrls;

	/**
	 * 初始化觅鲜WebView
	 * 
	 * @param url
	 */
	public void initWeb(String url) {
		loadHistoryUrls = new ArrayList<String>();
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int densityDpi = metric.densityDpi;
		float density = metric.density;
		LayoutParams lp;
		lp = (LayoutParams) otherLinear.getLayoutParams();
		lp.width = width;
		lp.height = (int) (height - getStatusBarHeight() - 50 * density);
		otherLinear.setLayoutParams(lp);
		Rect outRect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);

		otherWebView = (com.tencent.smtt.sdk.WebView) findViewById(R.id.otherWebView);
		WebSettings setting = otherWebView.getSettings();
		// 设置WebView执行JavaScript脚本
		setting.setJavaScriptEnabled(true);
		// Technical settings
		setting.setSupportZoom(true); // Scale switch
		setting.setSupportMultipleWindows(true); // Multi window
		// 开始应用H5缓存api
		setting.setAppCacheEnabled(true);
		// 开启数据库存储API
		setting.setDatabaseEnabled(true);
		// 开启 DOMstorage API
		setting.setDomStorageEnabled(true);

		/* 缓存 */
		setting.setAppCacheMaxSize(1024 * 1024 * 8);// 设置缓冲大小，我设的是8M
		String appCacheDir = this.getApplicationContext()
				.getDir("cache", Context.MODE_PRIVATE).getPath();
		setting.setAppCachePath(appCacheDir);
		setting.setAllowFileAccess(true);
		// 设置缓冲的模式
		// otherWebView.getSettings().setCacheMode(
		// WebSettings.LOAD_CACHE_ELSE_NETWORK);
		// 支持通过JS打开新窗口
		setting.setJavaScriptCanOpenWindowsAutomatically(true);
		otherWebView.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(
					com.tencent.smtt.sdk.WebView view, String url) {
				// TODO Auto-generated method stub
				// System.out.println("lw   ________________URL===" + url);
				com.tencent.smtt.sdk.WebView.HitTestResult hit = view
						.getHitTestResult();
				if (hit != null) {
					if (url.indexOf("weixin://") != -1) {
						isRedirect = true;
						if (checkApkExist(context, "com.tencent.mm")) {
							Intent intent = new Intent();
							intent.setAction("android.intent.action.VIEW");
							Uri content_url = Uri.parse(url);
							intent.setData(content_url);
							context.startActivity(intent);
						}
					} else {
						// isRedirect = false;
						// 将过滤到的url加入历史栈中
						if (!isRedirect) {
							loadHistoryUrls.add(url);
						}
						view.loadUrl(url);
						return true;
					}
					return true;
				} else {
					// isRedirect = false;
					// 将过滤到的url加入历史栈中
					loadHistoryUrls.add(url);
					view.loadUrl(url);
					return true;
				}
			}

			@Override
			public void onPageStarted(com.tencent.smtt.sdk.WebView view,
					String url, Bitmap favicon) {
				// System.out.println("lw    start---url==" + url);
//				if (urls == url || urls.equals(url)) {
//					mHandler.sendEmptyMessageDelayed(8, 0);
//				} else {
//					mHandler.sendEmptyMessageDelayed(9, 0);
//				}
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(com.tencent.smtt.sdk.WebView view,
					String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				if (otherWebView.canGoBack()) {
					mHandler.sendEmptyMessageDelayed(5, 0);
				} else {
					mHandler.sendEmptyMessageDelayed(6, 0);
				}
			}

		});

		otherWebView
				.setOnKeyListener(new com.tencent.smtt.sdk.WebView.OnKeyListener() {

					@Override
					protected void finalize() throws Throwable {
						// TODO Auto-generated method stub
						super.finalize();
					}

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							if (keyCode == KeyEvent.KEYCODE_BACK
									&& otherWebView.canGoBack()) {
								// System.out.println("lw     goback....");
								otherWebView.goBack(); // back
								return true;
							} else if (keyCode == KeyEvent.KEYCODE_BACK) {
								if ((System.currentTimeMillis() - mExitTime) > 2000) {
									Object mHelperUtils;
									Toast.makeText(context, "再按一次退出！",

									Toast.LENGTH_SHORT).show();
									mExitTime = System.currentTimeMillis();

								} else {
									 finish();
								}
								return true;
							}
						}
						return false;
					}
				});
		otherLinear.setVisibility(View.VISIBLE);
		otherWebView.loadUrl(url);
	}

	/**
	 * 检测是否安装了某个APK
	 * @param context	上下文
	 * @param packageName	包名
	 * @return	boolean 
	 */
	public boolean checkApkExist(Context context, String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(packageName,
							PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	// 是否重定向
	private boolean isRedirect2 = false;
	//保存由网页进入的有赞页面历史记录
	private ArrayList<String> loadHistoryUrls2;

	/**
	 * 初始化由网页进入的有赞页面
	 * @param url	网址
	 */
	public void initWeb2(String url) {
		//初始化保存记录
		loadHistoryUrls2 = new ArrayList<String>();
		//获取系统服务，手动设置宽高
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int densityDpi = metric.densityDpi;
		float density = metric.density;
		LayoutParams lp;
		lp = (LayoutParams) otherLinear2.getLayoutParams();
		lp.width = width;
		lp.height = height - getStatusBarHeight();
		otherLinear2.setLayoutParams(lp);
		Rect outRect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
		otherWebView2 = (WebView) findViewById(R.id.otherWebView2);

		// 设置WebView执行Javascript
		otherWebView2.getSettings().setJavaScriptEnabled(true);
		// Technical settings
		otherWebView2.getSettings().setSupportZoom(true); // Scale switch
		//多窗口设置
		otherWebView2.getSettings().setSupportMultipleWindows(true); 
		// 开启 Application H5缓存
		otherWebView2.getSettings().setAppCacheEnabled(true);
		// 开启 databases torage API
		otherWebView2.getSettings().setDatabaseEnabled(true);
		// 开启 DOMstorage API
		otherWebView2.getSettings().setDomStorageEnabled(true);

		/* 缓存 */
		otherWebView2.getSettings().setDomStorageEnabled(true);
		// 设置缓冲大小，我设的是8M
		otherWebView2.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
		String appCacheDir = this.getApplicationContext()
				.getDir("cache", Context.MODE_PRIVATE).getPath();
		otherWebView2.getSettings().setAppCachePath(appCacheDir);
		otherWebView2.getSettings().setAllowFileAccess(true);
		//设置加载模式
		otherWebView2.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		//允许JS多窗口打开
		otherWebView2.getSettings().setJavaScriptCanOpenWindowsAutomatically(
				true);
		otherWebView2.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				HitTestResult hit = view.getHitTestResult();
				if (hit != null) {
					if (url.indexOf("weixin://") != -1) {
						isRedirect2 = true;
						if (checkApkExist(context, "com.tencent.mm")) {
							Intent intent = new Intent();
							intent.setAction("android.intent.action.VIEW");
							Uri content_url = Uri.parse(url);
							intent.setData(content_url);
							context.startActivity(intent);
						}
					} else {
						// 将过滤到的url加入历史栈中
						if (!isRedirect2) {
							loadHistoryUrls2.add(url);
						}
						view.loadUrl(url);
						return true;
					}
					return true;
				} else {
					// 将过滤到的url加入历史栈中
					loadHistoryUrls2.add(url);
					view.loadUrl(url);
					return true;
				}
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
			}

		});
		
		//按键监听
		otherWebView2.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK
							&& otherWebView2.canGoBack()) {
						otherWebView2.goBack(); // back
						if (isRedirect2) {
							isRedirect2 = false;
							loadHistoryUrls2.remove(loadHistoryUrls2
									.get(loadHistoryUrls2.size() - 1));
							// 加载重定向之前的页
							otherWebView2.loadUrl(loadHistoryUrls2
									.get(loadHistoryUrls2.size() - 1));
						} else {
							otherWebView2.goBack();
						}
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_BACK) {
						if (otherLinear2.getVisibility() == View.VISIBLE) {
							otherLinear2.setVisibility(View.GONE);
						}
						return true;
					}
				}
				return false;
			}
		});
		//隐藏otherLinear2
		otherLinear2.setVisibility(View.VISIBLE);
		//加载url页面
		otherWebView2.loadUrl(url);
		otherWebView2.postDelayed(new Runnable() {
			@Override
			public void run() {
				otherWebView2.clearHistory();
			}
		}, 1000);
		backImg2.setVisibility(View.VISIBLE);
	}

	/**
	 * 获得标题栏高度
	 * @return	题栏高度
	 */
	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	/**
	 * JS回调处理
	 * @return
	 */
	private Object getHtmlObject() {
		Object insertObj = new Object() {
			/**
			 * js Call java
			 * 
			 * @return string
			 */
			@JavascriptInterface
			public String HtmlcallJava() {
				return "Html call Java!";
			}

			/**
			 * 打电话
			 */
			@JavascriptInterface
			public void callPhone(String str) {
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
						+ str)));
			}

			/**
			 * 扫码
			 * 
			 * @return
			 */
			// @JavascriptInterface
			// public String scanCode() {
			// Intent intent = new Intent();
			// intent.setClass(context, CameraTestActivity.class);
			// startActivityForResult(intent, 0);
			// return intent.getStringExtra("Code");
			// }

			/**
			 * 支付
			 * 
			 * @param str
			 * @return 返回支付结果0：失败 1：成功 2：支付中
			 */
			@JavascriptInterface
			public void payMoney(/* final */String str) {
				pay = new Pays(context, str);
				pay.pay();
			}

			/**
			 * 提示
			 * 
			 * @param str
			 */
			@JavascriptInterface
			public void prompt(String str) {
				Toast.makeText(context, str, 2000).show();
			}

			/**
			 * 跳转到觅鲜
			 * 
			 * @param url
			 */
			@JavascriptInterface
			public void jumpActivity(String str, int type) {
//				 System.out.println("lw      jumpActivity    str===" + str
//				 + ",type===" + type);
				urls = str;
				if (type == 0) {
					mHandler.sendEmptyMessageDelayed(3, 0);
				} else {
					mHandler.sendEmptyMessageDelayed(7, 0);
				}
			}
			
			/**
			 * 跳转到农产品
			 * 
			 * @param url
			 */
			@JavascriptInterface
			public void jumpActivity(String str, int type,String title,
					String image,String url,String detail) {
//				 System.out.println("lw      jumpActivity    str===" + str
//				 + ",type===" + type);
				urls = str;
				if (type == 0) {
					mHandler.sendEmptyMessageDelayed(3, 0);
				} else if (type == 2) {
					shareTitle = title;
					shareImage = image;
					shareUrls = url;
					shareDetail = detail;
					mHandler.sendEmptyMessageDelayed(7, 0);
				}
			}

			/**
			 * 隐藏觅鲜
			 */
			@JavascriptInterface
			public void hindJumpActivity() {
				mHandler.sendEmptyMessageDelayed(4, 0);
			}

			/**
			 * 分享
			 * 
			 * @param title
			 *            标题 ，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
			 * @param titleUrl
			 *            标题的网络链接 ，仅在人人网和QQ空间使用
			 * @param text
			 *            分享文本，所有平台都需要这个字段
			 * @param imgPath
			 *            图片的本地路径，Linked-In以外的平台都支持此参数
			 * @param url
			 *            在微信（包括好友和朋友圈）中使用
			 * @param comment
			 *            我对这条分享的评论，仅在人人网和QQ空间使用
			 * @param siteUrl
			 *            分享此内容的网站地址，仅在QQ空间使用
			 */
			@JavascriptInterface
			public void showShare(String title, String titleUrl,
					final String text, String imgPath, final String url,
					String comment, String siteUrl) {
				downImg(imgPath);
				ShareSDK.initSDK(context);
				OnekeyShare oks = new OnekeyShare();
				// 关闭sso授权
				oks.disableSSOWhenAuthorize();
				oks.setSilent(true); // 隐藏编辑页面

				// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
				// oks.setNotification(R.drawable.ic_launcher,
				// getString(R.string.app_name));
				// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
				oks.setTitle(/* getString(R.string.share) */title);
				// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
				oks.setTitleUrl(url);
				// text是分享文本，所有平台都需要这个字段
				oks.setText(/* "我是分享文本" */text);
				// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
				oks.setImagePath("/sdcard/img.png");// 确保SDcard下面存在此张图片
				// oks.setImageUrl(imgPath);
				// url仅在微信（包括好友和朋友圈）中使用
				oks.setUrl(/* "http://sharesdk.cn" */url);
				// comment是我对这条分享的评论，仅在人人网和QQ空间使用
				oks.setComment(/* "我是测试评论文本" */comment);
				// site是分享此内容的网站名称，仅在QQ空间使用
				oks.setSite(getString(R.string.app_name));
				// siteUrl是分享此内容的网站地址，仅在QQ空间使用
				oks.setSiteUrl(/* "http://sharesdk.cn" */url);

				// 此处为本demo关键为一键分享折子定义分享回调函数 shareContentCustomuzeCallback
				// 自定义平台可以通过判断不同的平台来实现不同平台间的不同操作
				oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
					// 自定义分享的回调想要函数
					@Override
					public void onShare(
							Platform platform,
							cn.sharesdk.framework.Platform.ShareParams paramsToShare) {
						// 点击新浪微博
						if ("SinaWeibo".equals(platform.getName())) {
							paramsToShare.setText(text + url);
						}

					}
				});
				// 启动分享GUI
				oks.show(context);
			}

			/**
			 * 地图导航
			 * 
			 * @param city
			 *            城市
			 * @param address
			 *            详细地址
			 */
			@JavascriptInterface
			public void jumpMap(String city, String address) {
//				 System.out.println("lw     jumpMap  city = " + city
//				 + "address = " + address);
//				Intent intent = new Intent();
//				intent.setClass(context, MapActivity.class);
//				intent.putExtra("city", city);
//				intent.putExtra("address", address);
//				startActivity(intent);
			}
			
			/**
			 * 地图导航
			 * 
			 * @param Latitude
			 *            纬度
			 * @param Longitude
			 *            纬度
			 */
			@JavascriptInterface
			public void jumpMaps(String longitude, String latitude,String address,String city) {
//				 System.out.println("lw     jumpMap   " +latitude+","+longitude+","+city
//				 + "address = " + address);
				Intent intent = new Intent();
				intent.setClass(context, MapActivity.class);
				intent.putExtra("mLatitude", latitude);
				intent.putExtra("mLongitude", longitude);
				intent.putExtra("mAddress", address);
				intent.putExtra("mCity", city);
				startActivity(intent);
			}
			
			/**
			 * 获取地址
			 */
			@JavascriptInterface
			public void getAddress(){
//				System.out.println("lw     getAddress~~~~~~~~~~");
				getLocation();
			}

			/**
			 * js Call java
			 * 
			 * @param param
			 * 
			 * @return string
			 */
			public String HtmlcallJava2(String param) {
				return "Html call Java : " + param + "test";
			}

			/** java Call js */
			public void JavacallHtml() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						webView.loadUrl("javascript: showFromHtml()");
						Toast.makeText(WebActivity.this, "clickBtn",
								Toast.LENGTH_SHORT).show();
					}
				});
			}

			/** java Call js */
			public void JavacallHtml2() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						webView.loadUrl("javascript: showFromHtml_tow('Android to Html...')");
					}
				});
			}
		};

		return insertObj;
	}

	/**
	 * 下载图片到本地
	 * @param urlPath
	 */
	public void downImg(String urlPath) {
		try {
			URL url = new URL(urlPath);
			InputStream is = url.openStream();
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			is.close();
			is = url.openStream();
			File testFile = new File(Environment.getExternalStorageDirectory(),
					"img.png");
			FileOutputStream os = new FileOutputStream(testFile);
			byte[] buff = new byte[1024];
			int hasRead = 0;
			while ((hasRead = is.read(buff)) > 0) {
				os.write(buff, 0, hasRead);
			}
			is.close();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 返回的条形码数据
		String code = data.getStringExtra("Code");
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		TCAgent.onPause(this);
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		TCAgent.onResume(this);
		MobclickAgent.onResume(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backImg://觅鲜返回按钮
			if (isRedirect) {//如果是重定向
				isRedirect = false;
				//从历史记录中移除最后一条记录
				loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls
						.size() - 1));
				// 加载重定向之前的页
				otherWebView
						.loadUrl(loadHistoryUrls.get(loadHistoryUrls.size() - 1));
			} else {
				otherWebView.goBack();
			}
			otherWebView.goBack();
			break;
		case R.id.backImg2://有页面跳转到有赞模块的返回按钮
			if (isRedirect2) {//如果是重定向
				isRedirect2 = false;
				loadHistoryUrls2.remove(loadHistoryUrls2.get(loadHistoryUrls2
						.size() - 1));
				// loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls
				// .size() - 1));
				// 加载重定向之前的页
				otherWebView2.loadUrl(loadHistoryUrls2.get(loadHistoryUrls2
						.size() - 1));
			} else if (otherWebView2.canGoBack()) {
				otherWebView2.goBack();
			} else {
				otherLinear2.setVisibility(View.GONE);
			}
			break;
		case R.id.shareImg:
			showShares(shareTitle, shareUrls, shareDetail, shareImage, shareUrls, "", shareUrls);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (otherWebView != null && otherWebView.canGoBack()) {
				if (isRedirect) {
					isRedirect = false;
					loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls
							.size() - 1));
					// 加载重定向之前的页
					otherWebView.loadUrl(loadHistoryUrls.get(loadHistoryUrls
							.size() - 1));
				} else {
					otherWebView.goBack();
				}
				// otherWebView.goBack();
				return true;
			} else if (webView.canGoBack()) {
				webView.goBack();
				mHandler.sendEmptyMessageDelayed(4, 0);
				return true;
			}

			if (otherWebView2 != null && otherWebView2.canGoBack()) {
				if (isRedirect2) {
					isRedirect2 = false;
					loadHistoryUrls2.remove(loadHistoryUrls2
							.get(loadHistoryUrls2.size() - 1));
					// loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls
					// .size() - 1));
					// 加载重定向之前的页
					otherWebView2.loadUrl(loadHistoryUrls2.get(loadHistoryUrls2
							.size() - 1));
				} else {
					otherWebView2.goBack();
				}
				return true;
			} else if (webView.canGoBack()) {
				webView.goBack();
				return true;
			}

			if (otherLinear2.getVisibility() == View.VISIBLE) {
				otherLinear2.setVisibility(View.GONE);
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private LocationManagerProxy mAMapLocationManager;

	// 获取经纬度
	public void getLocation() {
		if (mAMapLocationManager == null) {
			System.out.println("lw   mAMapLocationManager== null");
			mAMapLocationManager = LocationManagerProxy.getInstance(context);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用destroy()方法
			// 其中如果间隔时间为-1，则定位只定一次
			// 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
			mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 30 * 60 * 1000, 10,
					aMapLocationListener);
		}

	}

	/***
	 * 定位变化监听
	 */
	private AMapLocationListener aMapLocationListener = new AMapLocationListener() {

		@Override
		public void onLocationChanged(AMapLocation aMapLocation) {

			if (aMapLocation != null
					&& aMapLocation.getAMapException().getErrorCode() == 0) {
//				System.out.println("lw     aMapLocation.getCityCode()="
//						+ aMapLocation.getCityCode());
//				System.out.println("lw     aMapLocation.getCity()="
//						+ aMapLocation.getCity());
//				System.out.println("lw     经纬度：" + aMapLocation.getLatitude()
//						+ "," + aMapLocation.getLongitude());
				// showToast("定位成功 aMapLocation=" + aMapLocation.toString());
				if(firstLocation){
				webView.loadUrl("javascript: window.device.getAddress('"
						+ aMapLocation.getLongitude() + "','"
						+ aMapLocation.getLatitude() + "','"
						+ aMapLocation.getCity() + "')");
				firstLocation = false;
				//取消定位
				mAMapLocationManager.removeUpdates(aMapLocationListener);
				mAMapLocationManager.destory();
				mAMapLocationManager = null;
				}else{
					latitude = aMapLocation.getLatitude();
					longitude = aMapLocation.getLongitude();
					currentCity = aMapLocation.getCity();
					mHandler.sendEmptyMessage(1);
				}
				//取消定位
//				mAMapLocationManager.destory();
//				mAMapLocationManager = null;
			} else {
				if(firstLocation){
					webView.loadUrl("javascript: window.device.getAddress('"
							+ "" + "','"
							+ "" + "','"
							+ "" + "')");
					firstLocation = false;
					}else{
//						System.out.println("lw    avascript: window.device.getLocationCallBack");
						webView.loadUrl("javascript: window.device.getLocationCallBack('"
								+ "" + "','"
								+ "" + "','"
								+ "" + "')");
					}
//				initGPS();
				Log.e("AmapErr", "lw  Location ERR:"
						+ aMapLocation.getAMapException().getErrorCode());
			}

		}

		@Override
		public void onLocationChanged(Location location) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

	};

	/**
	* 监听GPS
    */
   private void initGPS() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS模块是否开启，如果没有则开启
        if (!locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
//            Toast.makeText(context, "请打开GPS",
//                    Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("无法定位，请打开GPS。");
            dialog.setPositiveButton("立即设置",
                    new android.content.DialogInterface.OnClickListener() {
 
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
 
                            // 转到手机设置界面，用户设置GPS
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
 
                        }
                    });
            dialog.setNeutralButton("取消", new android.content.DialogInterface.OnClickListener() {
                 
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            } );
            dialog.show();
        } else {
            // 弹出Toast
//          Toast.makeText(MainActivity.this, "GPS is ready",
//                  Toast.LENGTH_LONG).show();
//          // 弹出对话框
//          new AlertDialog.Builder(this).setMessage("GPS is ready")
//                  .setPositiveButton("OK", null).show();
        }
    }
   
   public void showShares(String title, String titleUrl,
			final String text, String imgPath, final String url,
			String comment, String siteUrl) {
		downImg(imgPath);
		ShareSDK.initSDK(context);
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();
		oks.setSilent(true); // 隐藏编辑页面

		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(/* getString(R.string.share) */title);
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl(url);
		// text是分享文本，所有平台都需要这个字段
		oks.setText(/* "我是分享文本" */text);
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImagePath("/sdcard/img.png");// 确保SDcard下面存在此张图片
		// oks.setImageUrl(imgPath);
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(/* "http://sharesdk.cn" */url);
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment(/* "我是测试评论文本" */comment);
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl(/* "http://sharesdk.cn" */url);

		// 此处为本demo关键为一键分享折子定义分享回调函数 shareContentCustomuzeCallback
		// 自定义平台可以通过判断不同的平台来实现不同平台间的不同操作
		oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
			// 自定义分享的回调想要函数
			@Override
			public void onShare(
					Platform platform,
					cn.sharesdk.framework.Platform.ShareParams paramsToShare) {
				// 点击新浪微博
				if ("SinaWeibo".equals(platform.getName())) {
					paramsToShare.setText(text + url);
				}

			}
		});
		// 启动分享GUI
		oks.show(context);
	}
}
