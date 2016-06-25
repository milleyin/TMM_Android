package com.qljl.tmm.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.qljl.tmm.MyApp;
import com.qljl.tmm.R;
import com.qljl.tmm.WebActivity.MyHandler;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	private static final String TAG = "lw   MicroMsg.SDKSample.WXPayEntryActivity";
	public static final String APP_ID = "wxeeabb8e9c700ca7f";

	private IWXAPI api;

	private static final int WXPAYRESULT = 111;

	private MyApp mApp = null;

	public MyHandler mHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_callback);
		api = WXAPIFactory.createWXAPI(this, APP_ID);
		api.handleIntent(getIntent(), this);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq resp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResp(BaseResp resp) {
		mApp = (MyApp) getApplication();
		// 获得该共享变量实例
		mHandler = mApp.getHandler();
		// TODO Auto-generated method stub
		Log.d(TAG, "lw onPayFinish, errCode = " + resp.errCode);

		// if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
		// AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setTitle("提示");
		// builder.setMessage(String.valueOf(resp.errCode));
		// builder.show();
		// }
		if (resp.errCode == 0) {
			mHandler.sendEmptyMessage(WXPAYRESULT);
		}
		finish();
	}

}
