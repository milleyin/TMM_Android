package com.qljl.tmm.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;

import com.qljl.tmm.bean.ShareMessage;
import com.qljl.tmm.bean.VersionCompareApk;

public class HttpHelper {
	private Context context;
	final String url = "https://m.365tmm.com/index.php?r=api/farmOuter/share&url=";

	public HttpHelper(Context context) {
		super();
		this.context = context;
	}

	/**
	 * 发送请求
	 */
	public ShareMessage getShareManager(String link) {
		String urls = url + link;
		try {
			HttpGet httpGet = new HttpGet(urls);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse = httpClient.execute(httpGet);
			Object resultObj = parseJSONString(4,
					getResponseResult(httpResponse));
			ShareMessage shareMessage = (ShareMessage) resultObj;
			// System.out.println("lw    status===" + status);
			return shareMessage;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 返回得到的数据
	 * 
	 * @param response
	 */
	public String getResponseResult(HttpResponse response) {
		if (null == response) {
			return "";
		}
		HttpEntity httpEntity = response.getEntity();
		try {
			InputStream inputStream = httpEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String result = "";
			String line = "";
			while (null != (line = reader.readLine())) {
				result += line;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}

	/**
	 * 解析Json
	 * 
	 * @param code
	 * @param jsonStr
	 * @return
	 * @throws Exception
	 */
	@SuppressLint("NewApi")
	public Object parseJSONString(int code, String jsonStr) throws Exception {
		if (jsonStr != "" || jsonStr != null) {
			JSONObject resultJson = new JSONObject(jsonStr);
			switch (code) {
			case 1:
				int version_zip = resultJson.getInt("version_zip");
				return version_zip;
			case 2:
				return resultJson.getString("APP_CSRF");
			case 3:
				VersionCompareApk vc = new VersionCompareApk();
				int version = resultJson.getInt("version");
				String url = resultJson.getString("down_url");
				vc.setVersion(version);
				vc.setUrl(url);
				return vc;
			case 4:
				int status = resultJson.getInt("status");
				if (status == 0) {
					return 0;
				} else if (status == 1) {
					JSONObject jsonObj = resultJson.getJSONObject("data");
					ShareMessage sm = new ShareMessage();
					sm.setShareTitle(jsonObj.getString("name"));
					sm.setShareImg(jsonObj.getString("image"));
					sm.setShareUrl(jsonObj.getString("link"));
					sm.setShareDetail(jsonObj.getString("info"));
					// System.out.println("lw     img:"
					// + jsonObj.getString("image"));
					// System.out.println("lw     url:"
					// + jsonObj.getString("link"));
					// Constants.shareMessage = sm;
					return sm;
				}
			default:
				return null;
			}
		} else {
			return "-1";
		}
	}

}
