package com.qljl.tmm;

import android.app.Application;

import com.qljl.tmm.WebActivity.MyHandler;

public class MyApp extends Application {
	private MyHandler handler = null;

	public MyHandler getHandler() {
		return handler;
	}

	public void setHandler(MyHandler handler) {
		this.handler = handler;
	}

}
