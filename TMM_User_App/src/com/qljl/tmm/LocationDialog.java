package com.qljl.tmm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

public class LocationDialog extends AlertDialog {
	TextView goSetting, noSetting;
	private Context context;

	protected LocationDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_dialog);
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		goSetting = (TextView) findViewById(R.id.goSetting);
		noSetting = (TextView) findViewById(R.id.noSetting);
		goSetting.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startAppSettings();
				dismiss();
			}
		}

		);
		noSetting.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
	}

	// 启动应用的设置
	private void startAppSettings() {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.setData(Uri.parse("package:" + context.getPackageName()));
		context.startActivity(intent);
	}

}
