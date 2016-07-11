package com.qljl.tmm.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/** 微信客户端回调activity示例 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	// IWXAPI 是第三方app和微信通信的openapi接口
	private IWXAPI api;
	private static final String TAG = "WXEntryActivity";
	public static final String APP_ID = "wx9ecb78a820f510e3";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		api = WXAPIFactory.createWXAPI(this, APP_ID, false);
		api.registerApp(APP_ID);
		api.handleIntent(getIntent(), this);
	}

	@Override
	public void onReq(BaseReq req) {

	}

	@Override
	public void onResp(BaseResp resp) {
		// LogManager.show(TAG, "resp.errCode:" + resp.errCode + ",resp.errStr:"
		// + resp.errStr, 1);

		Log.i(TAG, "lw    resp.errCode:" + resp.errCode + ",resp.errStr:"
				+ resp.errStr);
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			System.out.println("lw     分享成功");
			// 分享成功
			Toast.makeText(getApplicationContext(), "success",
					Toast.LENGTH_SHORT).show();
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			System.out.println("lw     分享取消");
			// 分享取消
			Toast.makeText(getApplicationContext(), "success",
					Toast.LENGTH_SHORT).show();
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			System.out.println("lw     分享拒绝");
			// 分享拒绝
			Toast.makeText(getApplicationContext(), "deny", Toast.LENGTH_SHORT)
					.show();
			break;
		}

		finish();
	}
}
