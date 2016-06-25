package com.qljl.tmm;

import java.io.File;
import java.io.IOException;
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
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.qljl.tmm.http.HttpHelper;
import com.qljl.tmm.pay.Pays;
import com.qljl.tmm.service.WebService;
import com.qljl.tmm.upgrade.UpdateManager;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

/**
 * WebView
 * 
 * @author lw
 * 
 */
public class WebActivity extends Activity {
	private static final String TAG = "WebActivity";
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
			+ "/tmm/files/user/src/index.html";

	private Intent intent;
	public static HttpHelper httpHelper;
	private long mExitTime;

	public static Lock lock;
	Pays pay;

	// Handler:is first user
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:// not first
				loadWebView();
				// 檢測是否有更新的zip
				checkZip();
				System.out.println("lw     not   first!");
				break;
			case 2:// is first，form asset copy zip to sdcard
				System.out.println("lw        first!");
				progressBar.setVisibility(View.VISIBLE);
				// Copy file
				try {
					httpHelper.copyDataBase();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Decompression file
				String path = httpHelper.DB_PATH + httpHelper.DB_NAME;
				System.out.println("lw     path===" + path);
				File zipFile = new File(path);
				try {
					System.out.println("lw   httpHelper.DB_TOPATH==="
							+ httpHelper.DB_TOPATH);
					httpHelper.upZipFile(zipFile, httpHelper.DB_TOPATH);
					progressBar.setVisibility(View.GONE);
					// load
					loadWebView();
					// 檢測是否有更新的zip
					checkZip();
				} catch (ZipException e) {
					System.out.println("lw   upZipFile  error");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("lw   upZipFile  error");
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
		setContentView(R.layout.webview);
		context = WebActivity.this;
		// init view
		initView();
		if (isServiceRunning()) {
			stopService(intent);
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
	public void checkZip() {
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
				if (updateStatus == 0 && updateInfo != null) {
					showUpdateDialog(updateInfo.path, updateInfo.updateLog);
				}
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
		webView = (WebView) findViewById(R.id.webView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
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

			}
		});
		webView.setWebChromeClient(new WebChrome());
		/** WebView Listener */
		webView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
						webView.goBack(); // back
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_BACK) {
						if ((System.currentTimeMillis() - mExitTime) > 2000) {
							Object mHelperUtils;
							Toast.makeText(context,
									"And then press an exit procedure",

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
		webView.getSettings().setMixedContentMode(2);
		CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
	}

	class WebChrome extends WebChromeClient {

		@Override
		public void onReceivedTitle(WebView view, String title) {
			WebActivity.this.setTitle(title);
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

	/**
	 * webView and js Interactive
	 */
	private void showWebView() {
		try {
			webView = new WebView(this);
			setContentView(webView);
			webView.requestFocus();
			webView.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int progress) {
					WebActivity.this.setTitle("Loading...");
					WebActivity.this.setProgress(progress);

					if (progress >= 80) {
						WebActivity.this.setTitle("JsAndroid Test");
					}
				}
			});

			webView.setOnKeyListener(new View.OnKeyListener() { // webview can
																// go back
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
						webView.goBack();
						return true;
					}
					return false;
				}
			});

			WebSettings webSettings = webView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("utf-8");

			webView.addJavascriptInterface(getHtmlObject(), "jsObj");
			webView.loadUrl("http://192.168.1.118/test/index.html");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			public void callPhone() {
				System.out.println("lw    callPhone!");
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
						+ "13266630656")));
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
		SharedPreferences preferences = getSharedPreferences(
				SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		if (isFirstIn) {
			// Save data
			editor.putBoolean("isFirstIn", false);
		}
		// Commit data
		editor.commit();

		stopService(intent);
	}
}
