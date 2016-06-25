package com.qljl.tmm;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;

import com.tencent.smtt.sdk.WebSettings;

public class TestWebActivity extends Activity {
	private com.tencent.smtt.sdk.WebView testWebView;
	private Context context;
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testwebview);
		initView();
	}

	private void initView() {
		webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.loadUrl("http://wap.koudaitong.com/v2/showcase/feature?alias=129wsjuci");

		testWebView = (com.tencent.smtt.sdk.WebView) findViewById(R.id.testWebView);
		WebSettings setting = testWebView.getSettings();
		setting.setJavaScriptEnabled(true);
		setting.setJavaScriptCanOpenWindowsAutomatically(true);
		testWebView
				.loadUrl("http://wap.koudaitong.com/v2/showcase/feature?alias=129wsjuci");
	}

}
