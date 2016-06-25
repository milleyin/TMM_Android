package com.qljl.tmm_business;

import java.io.File;
import java.io.IOException;
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
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.qljl.tmm_business.http.HttpHelper;
import com.qljl.tmm_business.service.WebService;
import com.qljl.tmm_business.upgrade.UpdateManager;
import com.qljl.tmm_business.util.MyLock;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class MainActivity extends Activity {
	public static boolean bool = false;
	// is first use
	boolean isFirstIn = false;
	// Delay 3 seconds
	private static final long SPLASH_DELAY_MILLIS = 3000;
	// sharedprerencesName
	private static final String SHAREDPREFERENCES_NAME = "first_pref";

	private Context context;
	private WebView webView;
	private ProgressBar progressBar;
	// 主页路径--用户端
	public String INDEX = Environment.getExternalStorageDirectory()
			+ "/tmm/files/business/src/index.html";

	private Intent intent;
	public static HttpHelper httpHelper;
	private long mExitTime;

	// Handler:is first user
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:// not first
				loadWebView();
				cheakZip();
				break;
			case 2:// is first，form asset copy zip to sdcard
					// Copy file
				try {
					httpHelper.copyDataBase();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Decompression file
				String path = httpHelper.DB_PATH + httpHelper.DB_NAME;
				File zipFile = new File(path);
				try {
					httpHelper.upZipFile(zipFile, httpHelper.DB_TOPATH);
					// load
					loadWebView();
					cheakZip();
				} catch (ZipException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = MainActivity.this;
		// init view
		initView();
		if (isServiceRunning()) {
			if (intent != null) {
				stopService(intent);
			}
		}
		// Start Android Service
		intent = new Intent(this, WebService.class);
		startService(intent);
		/** init HttpHelper class */
		httpHelper = new HttpHelper(context);
		/** init user */
		init();
		/** init aliyun */
		// initOneSDK(this);
		/** init UpdateManager */
		UpdateManager manager = new UpdateManager(context);
		/** Check software update */
		manager.checkUpdate();
		/** youmeng */
		checkVersion();

		// weixin
		// WeiXinShare wxs = new WeiXinShare();
		// // wxs.wechatShare(0, context, null);// friend
		// wxs.wechatShare(1, context, "test!");// Micro channel circle
	}

	/**
	 * Service是否在運行
	 * 
	 * @return
	 */
	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.qljl.tmm_business.service.WebService"
					.equals(service.service.getClassName())) {
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
			mHandler.sendEmptyMessageDelayed(1, SPLASH_DELAY_MILLIS);
		} else {
			// If it is the first time to use copy from zip to SD card asset
			mHandler.sendEmptyMessageDelayed(2, SPLASH_DELAY_MILLIS);
		}

	}

	/**
	 * Test compression package is updated Download
	 */
	public void cheakZip() {
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
					isFirstIn = false;
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
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();// Acceptance certificate
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
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
		webView.setWebChromeClient(new WebChrome());
		/** WebView Listener */
		webView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
						System.out.println("lw        goback!");
						if (bool) {
							MyLock.getSignal();
						}
						webView.goBack(); // back
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_BACK) {
						if ((System.currentTimeMillis() - mExitTime) > 2000) {
							Object mHelperUtils;
							Toast.makeText(context, "再按一次退出！",
									Toast.LENGTH_SHORT).show();
							mExitTime = System.currentTimeMillis();
							if (bool) {
								MyLock.getSignal();
							}
							System.out.println("lw        1back!");
						} else {
							System.out.println("lw        2back!");
							if (bool) {
								MyLock.getSignal();
							}
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
	}

	class WebChrome extends WebChromeClient {

		@Override
		public void onReceivedTitle(WebView view, String title) {
			MainActivity.this.setTitle(title);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
					newProgress * 100);
			if (newProgress == 100) {
				CookieSyncManager.getInstance().sync();
			}
		}
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
				System.out.println("lw    callPhone!====" + str);
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
						+ str)));
			}

			/**
			 * 扫码
			 * 
			 * @return
			 */
			@JavascriptInterface
			public String scanCode() {
				System.out.println("lw           scanCode");
				Intent intent = new Intent();
				intent.setClass(context, CaptureActivity.class);
				startActivityForResult(intent, 0);
				MyLock.getAwart();
				System.out.println("lw    scanCode_____"
						+ CaptureActivity.scanResult);
				return CaptureActivity.scanResult;
			}

			/**
			 * 支付
			 * 
			 * @param str
			 * @return 返回支付结果0：失败 1：成功 2：支付中
			 */
			// @JavascriptInterface
			// public String payMoney(final String str) {
			// System.out.println("lw      payMoney___支付");
			// Pays pay = new Pays(context, str);
			// pay.pay();
			// MyLock.getAwart();
			// return pay.payStatus + "";
			// }

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
						Toast.makeText(MainActivity.this, "clickBtn",
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
		SharedPreferences preferences = getSharedPreferences(
				SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		if (isFirstIn) {
			// Save data
			editor.putBoolean("isFirstIn", false);
		} else {
			editor.putBoolean("isFirstIn", true);
		}
		// Commit data
		editor.commit();

		// stopService(intent);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopService(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 监控/拦截/屏蔽返回键
			System.out.println("lw       KEYCODE_BACK");
			if (bool) {
				MyLock.getSignal();
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			System.out.println("lw       KEYCODE_MENU");
			if (bool) {
				MyLock.getSignal();
			}
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			System.out.println("lw       KEYCODE_HOME");
			if (bool) {
				MyLock.getSignal();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
