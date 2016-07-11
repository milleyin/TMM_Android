package com.qljl.tmm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.tendcloud.tenddata.TCAgent;
import com.umeng.analytics.MobclickAgent;

public class LoadingActivity extends Activity {
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		if (!isTaskRoot()) {
			finish();
			return;
		}
		final ImageView imageView = (ImageView) findViewById(R.id.imageView);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent mainIntent = new Intent(LoadingActivity.this,
						WebActivity.class /* TestWebActivity.class */);
				LoadingActivity.this.startActivity(mainIntent);
				LoadingActivity.this.finish();
			}
		}, 3000);
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
}
