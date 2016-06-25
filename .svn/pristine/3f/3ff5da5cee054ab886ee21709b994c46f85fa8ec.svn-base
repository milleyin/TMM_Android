package com.qljl.tmm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.zip.ZipException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.qljl.tmm.http.HttpHelper;
import com.qljl.tmm.pay.Pays;
import com.qljl.tmm.service.WebService;
import com.qljl.tmm.upgrade.UpdateManager;
//import android.webkit.WebSettings;
import com.tencent.smtt.sdk.WebSettings;
import com.tendcloud.tenddata.TCAgent;
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
	// is first use
	boolean isFirstIn = false;
	// Delay 3 seconds
	private static final long SPLASH_DELAY_MILLIS = 300;
	// sharedprerencesName
	private static final String SHAREDPREFERENCES_NAME = "first_pref";

	private Context context;
	private WebView webView;
	private ProgressBar progressBar;

	//
	private FrameLayout otherLinear, otherLinear2;
	private ImageView backImg, backImg2;
	private WebView /* otherWebView, */otherWebView2;
	private com.tencent.smtt.sdk.WebView otherWebView;
	public String urls = "";

	// 主页路径--用户端
	// public String INDEX = Environment.getExternalStorageDirectory()
	// + "/tmm/files/user/src/index.html";

	public String INDEX = "data/data/com.qljl.tmm/files/user/src/index.html";

	private Intent intent;
	private Intent intent_zip;
	public static HttpHelper httpHelper;
	private long mExitTime;

	public static Lock lock;
	Pays pay;
	UpdateManager manager;

	// Handler:is first user
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:// not first
				loadWebView();
				// 檢測是否有更新的zip
				checkZip();
				// System.out.println("lw     not   first!");
				break;
			case 2:// is first，form asset copy zip to sdcard
					// System.out.println("lw        first!");
					// Copy file
				try {
					httpHelper.copyDataBase();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Decompression file
				String path = httpHelper.DB_PATH + httpHelper.DB_NAME;
				// System.out.println("lw     path===" + path);
				File zipFile = new File(path);
				try {
					// System.out.println("lw   httpHelper.DB_TOPATH==="
					// + httpHelper.DB_TOPATH);
					httpHelper.upZipFile(zipFile, httpHelper.DB_TOPATH);
					// load
					loadWebView();
					// 檢測是否有更新的zip
					checkZip();
				} catch (ZipException e) {
					// System.out.println("lw   upZipFile  error");
					e.printStackTrace();
				} catch (IOException e) {
					// System.out.println("lw   upZipFile  error");
					e.printStackTrace();
				}

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
		if (isServiceRunning()) {
			if (intent != null) {
				stopService(intent);
			}
		}
		if (isZipServiceRunning()) {
			if (intent_zip != null) {
				stopService(intent_zip);
			}
		}
		// Start Android Service
		intent = new Intent(this, WebService.class);
		startService(intent);

		/** init HttpHelper class */
		httpHelper = new HttpHelper(context);
		/** init user */
		// init();
		initApp();
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

	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.qljl.tmm.service.WebService".equals(service.service
					.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private boolean isZipServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.qljl.tmm.service.UpdataZipService".equals(service.service
					.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Judge whether the first use of the user
	 */
	private void init() {
		// init SharedPreferences
		SharedPreferences preferences = getSharedPreferences(
				SHAREDPREFERENCES_NAME, MODE_PRIVATE);

		// Obtain the corresponding value, if not the value, the description is
		// not written, with true as the default value
		isFirstIn = preferences.getBoolean("isFirstIn", true);

		// Judgment procedure with the first run, if it is the first run then
		// jump to the boot interface, or jump to the main interface
		if (!isFirstIn) {
			// Using postDelayed's Handler method, if not the first use of a
			// direct load home
			preferences.edit().putBoolean("isFirstIn", false);
			mHandler.sendEmptyMessageDelayed(1, SPLASH_DELAY_MILLIS);
		} else {
			// If it is the first time to use copy from zip to SD card asset
			mHandler.sendEmptyMessageDelayed(2, SPLASH_DELAY_MILLIS);
		}

	}

	public static String PACKAGE_NAME = "com.qljl.tmm";
	public static String VERSION_KEY = "VERSION_KEY";

	public void initApp() {
		PackageInfo info;
		try {
			info = getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			int currentVersion = info.versionCode;
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			int lastVersion = prefs.getInt(VERSION_KEY, 0);
			if (currentVersion > lastVersion) {
				// 如果当前版本大于上次版本，该版本属于<a
				// href="https://www.baidu.com/s?wd=%E7%AC%AC%E4%B8%80%E6%AC%A1&tn=44039180_cpr&fenlei=mv6quAkxTZn0IZRqIHckPjm4nH00T1YLuWwWuWnLnjI9P1n3Pvn10ZwV5Hcvrjm3rH6sPfKWUMw85HfYnjn4nH6sgvPsT6K1TL0qnfK1TL0z5HD0IgF_5y9YIZ0lQzqlpA-bmyt8mh7GuZR8mvqVQL7dugPYpyq8Q16srHDkn1RzPWnsPW6vPH6kn6"
				// target="_blank" class="baidu-highlight">第一次</a>启动

				// 将当前版本写入preference中，则下次启动的时候，据此判断，不再为首次启动
				prefs.edit().putInt(VERSION_KEY, currentVersion).commit();
				// System.out.println("lw      @@@@@@@@@@@@@@@@@@第一次启动！");
				mHandler.sendEmptyMessageDelayed(2, SPLASH_DELAY_MILLIS);
			} else {
				// System.out.println("lw     **********************非第一次启动！");
				mHandler.sendEmptyMessageDelayed(1, SPLASH_DELAY_MILLIS);
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test compression package is updated Download
	 */
	public static void checkZip() {
		new Thread() {

			@Override
			public void run() {
				// To determine whether the server is updated
				httpHelper.hasUpdate();
			}

		}.start();

	}

	/** Version detection */
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
					// isFirstIn = false;
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
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		webView = (WebView) findViewById(R.id.webView);
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
							Intent intent = new Intent(Intent.ACTION_MAIN);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.addCategory(Intent.CATEGORY_HOME);
							startActivity(intent);
							// finish();
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
				System.out.println("lw   ________________URL===" + url);
				com.tencent.smtt.sdk.WebView.HitTestResult hit = view
						.getHitTestResult();
				if (hit != null) {
					if (url.indexOf("weixin://") != -1) {
						System.out.println("lw     1111111111111111111111");
						isRedirect = true;
						if (checkApkExist(context, "com.tencent.mm")) {
							Intent intent = new Intent();
							intent.setAction("android.intent.action.VIEW");
							Uri content_url = Uri.parse(url);
							intent.setData(content_url);
							context.startActivity(intent);
						}
					} else {
						System.out.println("lw    22222222222222222");
						// isRedirect = false;
						// 将过滤到的url加入历史栈中
						if (!isRedirect) {
							loadHistoryUrls.add(url);
							System.out
									.println("lw     loadHistoryUrls======length:"
											+ loadHistoryUrls.size());
						}
						view.loadUrl(url);
						return true;
					}
					return true;
				} else {
					// isRedirect = false;
					// 将过滤到的url加入历史栈中
					loadHistoryUrls.add(url);
					System.out.println("lw     2loadHistoryUrls======length:"
							+ loadHistoryUrls.size());
					view.loadUrl(url);
					return true;
				}
				// view.loadUrl(url);
				// return true;
			}

			@Override
			public void onPageStarted(com.tencent.smtt.sdk.WebView view,
					String url, Bitmap favicon) {
				// System.out.println("lw    start---url==" + url);
				if (urls == url || urls.equals(url)) {
					mHandler.sendEmptyMessageDelayed(8, 0);
				} else {
					mHandler.sendEmptyMessageDelayed(9, 0);
				}
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
								System.out.println("lw     goback....");
								otherWebView.goBack(); // back
								// if (isRedirect) {
								// System.out.println("lw        1重定向！");
								// // 加载重定向之前的页
								// otherWebView.loadUrl(loadHistoryUrls
								// .get(loadHistoryUrls.size() - 2));
								// isRedirect = false;
								// } else {
								// System.out.println("lw        1not重定向！");
								// otherWebView.goBack();
								// }
								return true;
							} else if (keyCode == KeyEvent.KEYCODE_BACK) {
								System.out.println("lw    back2");
								if ((System.currentTimeMillis() - mExitTime) > 2000) {
									System.out.println("lw    1");
									Object mHelperUtils;
									Toast.makeText(context, "再按一次退出！",

									Toast.LENGTH_SHORT).show();
									mExitTime = System.currentTimeMillis();

								} else {
									System.out.println("lw    2");
									Intent intent = new Intent(
											Intent.ACTION_MAIN);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.addCategory(Intent.CATEGORY_HOME);
									startActivity(intent);
									// finish();
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

	public void initWeb2(String url) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int densityDpi = metric.densityDpi;
		float density = metric.density;
		// otherLinear2 = (FrameLayout) findViewById(R.id.otherLinear2);

		LayoutParams lp;
		lp = (LayoutParams) otherLinear2.getLayoutParams();
		lp.width = width;
		lp.height = height - getStatusBarHeight();
		otherLinear2.setLayoutParams(lp);
		// System.out.println("lw      density===" + density + "");
		Rect outRect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
		// System.out.println("lw      top====" + getStatusBarHeight());
		otherWebView2 = (WebView) findViewById(R.id.otherWebView2);

		// Set the WebView property to execute the Javascript script
		otherWebView2.getSettings().setJavaScriptEnabled(true);
		// Technical settings
		otherWebView2.getSettings().setSupportZoom(true); // Scale switch
		otherWebView2.getSettings().setSupportMultipleWindows(true); // Multi
																		// window
		// start Application H5 Caches
		otherWebView2.getSettings().setAppCacheEnabled(true);
		// strat databases torage API
		otherWebView2.getSettings().setDatabaseEnabled(true);
		// start DOMstorage API
		otherWebView2.getSettings().setDomStorageEnabled(true);

		/* 缓存 */
		otherWebView2.getSettings().setDomStorageEnabled(true);
		otherWebView2.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);// 设置缓冲大小，我设的是8M
		String appCacheDir = this.getApplicationContext()
				.getDir("cache", Context.MODE_PRIVATE).getPath();
		otherWebView2.getSettings().setAppCachePath(appCacheDir);
		otherWebView2.getSettings().setAllowFileAccess(true);
		otherWebView2.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

		otherWebView2.getSettings().setJavaScriptCanOpenWindowsAutomatically(
				true);
		otherWebView2.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				HitTestResult hit = view.getHitTestResult();
				if (hit != null) {
					if (url.indexOf("weixin://") != -1) {
						if (checkApkExist(context, "com.tencent.mm")) {
							Intent intent = new Intent();
							intent.setAction("android.intent.action.VIEW");
							Uri content_url = Uri.parse(url);
							intent.setData(content_url);
							context.startActivity(intent);
						}
					} else {
						view.loadUrl(url);
						return true;
					}
					return true;
				} else {
					view.loadUrl(url);
					return true;
				}
				// view.loadUrl(url);
				// return true;
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

		otherWebView2.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK
							&& otherWebView2.canGoBack()) {
						otherWebView2.goBack(); // back
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
		otherLinear2.setVisibility(View.VISIBLE);
		otherWebView2.loadUrl(url);
		backImg2.setVisibility(View.VISIBLE);
	}

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
	 * Load WebView
	 */
	private void loadWebView() {
		webView.loadUrl("http://127.0.0.1:" + WebService.PORT + "/" + INDEX);
		webView.addJavascriptInterface(getHtmlObject(), "jsObj");
	}

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
				// System.out.println("lw      jumpActivity    str===" + str
				// + ",type===" + type);
				urls = str;
				if (type == 0) {
					mHandler.sendEmptyMessageDelayed(3, 0);
				} else if (type == 1) {
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		TCAgent.onResume(this);
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
		stopService(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backImg:
			if (isRedirect) {
				isRedirect = false;
				loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls
						.size() - 1));
				// loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls
				// .size() - 1));
				// 加载重定向之前的页
				otherWebView
						.loadUrl(loadHistoryUrls.get(loadHistoryUrls.size() - 1));
			} else {
				otherWebView.goBack();
			}
			otherWebView.goBack();
			break;
		case R.id.backImg2:
			if (otherWebView2.canGoBack()) {
				otherWebView2.goBack();
			} else {
				otherLinear2.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.out.println("lw    back!");
			if (otherWebView != null && otherWebView.canGoBack()) {
				if (isRedirect) {
					isRedirect = false;
					loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls
							.size() - 1));
					// loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls
					// .size() - 1));
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
			if (otherLinear2.getVisibility() == View.VISIBLE) {
				otherLinear2.setVisibility(View.GONE);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
