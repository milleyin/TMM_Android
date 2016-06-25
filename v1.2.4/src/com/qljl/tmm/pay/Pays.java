package com.qljl.tmm.pay;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;

/**
 * 支付结果
 * 
 * @author lw
 *
 */
public class Pays {
	private Context context;
	private static final int SDK_PAY_FLAG = 1;
	public static int payStatus = 15;
	String payInfo = "";

	// private final int AlibabaPAYRESULT = 112;

	// private MyApp mApp = null;
	//
	// public MyHandler handler = null;

	public Pays(Context context, String payInfo) {
		super();
		this.context = context;
		this.payInfo = payInfo;

		// mApp = (MyApp) context;
		// // 获得该共享变量实例
		// mHandler = mApp.getHandler();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				PayResults payResults = new PayResults((String) msg.obj);

				// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
				String resultInfo = payResults.getResult();

				String resultStatus = payResults.getResultStatus();
				String result = payResults.getResult();
				String str = "success=\"true\"";
				// if (result.contains(str)) {
				// System.out.println("lw      result包含success=\"true\"");
				// } else {
				// System.out.println("lw      result不包含success=\"true\"");
				// }
				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(resultStatus, "9000")
						&& result.contains(str)) {
					payStatus = 1;
					// handler.sendEmptyMessage(AlibabaPAYRESULT);
				} else {
					// 判断resultStatus 为非“9000”则代表可能支付失败
					// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						payStatus = 2;
					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						payStatus = 0;
					}
				}
				break;
			}

			default:
				break;
			}
		};
	};

	/**
	 * call alipay sdk pay. 调用SDK支付
	 * 
	 */
	public void pay() {

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask((Activity) context);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);
				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

}
