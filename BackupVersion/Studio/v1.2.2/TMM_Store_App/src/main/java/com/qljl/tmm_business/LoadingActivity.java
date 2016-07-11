package com.qljl.tmm_business;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class LoadingActivity extends Activity {
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		final ImageView imageView = (ImageView) findViewById(R.id.imageView);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent mainIntent = new Intent(LoadingActivity.this,
						MainActivity.class);
				LoadingActivity.this.startActivity(mainIntent);
				LoadingActivity.this.finish();
			}
		}, 3000);
	}

}
