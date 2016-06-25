package com.qljl.tmm.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;

import com.qljl.tmm.bean.VersionCompareApk;

public class HttpHelper {
	private Context context;

	public HttpHelper(Context context) {
		super();
		this.context = context;
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
			default:
				return null;
			}
		} else {
			return "-1";
		}
	}

}
