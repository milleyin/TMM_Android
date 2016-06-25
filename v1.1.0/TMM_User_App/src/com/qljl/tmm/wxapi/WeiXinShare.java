package com.qljl.tmm.wxapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.qljl.tmm.R;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 微信分享 （这里仅提供一个分享网页的示例，其它请参看官网示例代码）
 * 
 * @param flag
 *            (0:分享到微信好友，1：分享到微信朋友圈)
 */
public class WeiXinShare {
	public static final String APP_ID = "wx9ecb78a820f510e3";

	private IWXAPI wxApi;

	public void wechatShare(int flag, Context context, String content) {
		// 实例化
		wxApi = WXAPIFactory.createWXAPI(context, APP_ID);
		wxApi.registerApp(APP_ID);

		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://www.baidu.com/";

		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = content;
		msg.description = "这里填写内容";

		// 这里替换一张自己工程里的图片资源
		Bitmap thumb = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_launcher);
		Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, 50, 50, true);
		thumb.recycle();

		msg.setThumbImage(thumbBmp);

		SendMessageToWX.Req req = new SendMessageToWX.Req();

		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		System.out.println("lw   req.scene = flag====== " + req.scene + "----"
				+ flag);
		req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession
				: SendMessageToWX.Req.WXSceneTimeline;

		wxApi.sendReq(req);
		// 初始化一个WXTextObject对象
		// WXTextObject textObj = new WXTextObject();
		// textObj.text = "abc";
		//
		// // 用WXTextObject对象初始化一个WXMediaMessage对象
		// WXMediaMessage msg = new WXMediaMessage();
		// msg.mediaObject = textObj;
		// // 发送文本类型的消息时，title字段不起作用
		// // msg.title = "Will be ignored";
		// msg.description = "测试";
		//
		// // 构造一个Req
		// SendMessageToWX.Req req = new SendMessageToWX.Req();
		// req.transaction = String.valueOf(System.currentTimeMillis()); //
		// transaction字段用于唯一标识一个请求
		// req.message = msg;
		// req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession
		// : SendMessageToWX.Req.WXSceneTimeline;
		//
		// // 调用api接口发送数据到微信
		// wxApi.sendReq(req);
	}
}

// 在需要分享的地方添加代码：
// wechatShare(0);//分享到微信好友
// wechatShare(1);//分享到微信朋友圈
