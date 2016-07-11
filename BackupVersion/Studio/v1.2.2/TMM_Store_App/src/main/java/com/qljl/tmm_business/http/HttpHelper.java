package com.qljl.tmm_business.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;

import com.qljl.tmm_business.bean.VersionCompareApk;

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
				VersionCompareApk vcApk = new VersionCompareApk();
				int version1 = resultJson.getInt("version_zip");
				String url1 = resultJson.getString("down_url");
				vcApk.setVersion(version1);
				vcApk.setUrl(url1);
				return vcApk;
			case 3:
				return null;
			default:
				return null;
			}
		} else {
			System.out.println("lw   jsonStr == null");
			return "-1";
		}
	}

}
