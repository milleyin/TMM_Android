package com.qljl.tmm_business.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.qljl.tmm_business.bean.VersionCompareApk;
import com.qljl.tmm_business.bean.VersionCompareZip;
import com.qljl.tmm_business.service.WebService;

public class HttpHelper {
	public static final String url = "http://192.168.1.100";
	private Context context;

	public String ASSETS_NAME = "business.zip";
	public String DB_PATH = Environment.getExternalStorageDirectory()
			+ "/tmm/rar/";
	public String DB_TOPATH = Environment.getExternalStorageDirectory()
			+ "/tmm/files/";
	public String DB_NAME = "business.zip";

	public HttpHelper(Context context) {
		super();
		this.context = context;
	}

	/**
	 * 发送请求
	 */
	public void sendRequest() {
		String url_first = url + "/server/index.php?r=app";
		try {
			// 第一步，创建HttpGet对象
			HttpGet httpGet = new HttpGet(url_first);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			// 第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
			HttpResponse httpResponse = httpClient.execute(httpGet);
			Object resultObj = parseJSONString(1,
					getResponseResult(httpResponse));
			// 获取Cookie
			getCookie(httpClient);
			if (resultObj != null) {

			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
				int version_zip = resultJson.getInt("version");
				return version_zip;
			case 2:
				VersionCompareApk vcApk = new VersionCompareApk();
				int version1 = resultJson.getInt("version_zip");
				String url1 = resultJson.getString("down_url");
				vcApk.setVersion(version1);
				vcApk.setUrl(url1);
				return vcApk;
			case 3:
				VersionCompareZip vcZip = new VersionCompareZip();
				int version = resultJson.getInt("version");
				String url = resultJson.getString("down_url");
				vcZip.setVersion(version);
				vcZip.setUrl(url);
				return vcZip;
			default:
				return null;
			}
		} else {
			System.out.println("lw   jsonStr == null");
			return "-1";
		}
	}

	/**
	 * 得到Cookie
	 * 
	 * @param httpClient
	 */
	private void getCookie(HttpClient httpClient) {
		List<Cookie> cookies = ((AbstractHttpClient) httpClient)
				.getCookieStore().getCookies();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cookies.size(); i++) {
			Cookie cookie = cookies.get(i);
			String cookieName = cookie.getName();
			String cookieValue = cookie.getValue();
			if (!TextUtils.isEmpty(cookieName)
					&& !TextUtils.isEmpty(cookieValue)) {
				sb.append(cookieName + "=");
				sb.append(cookieValue + ";");
			}
		}
	}

	/**
	 * 检测服务器是否有压缩包更新
	 */
	public void hasUpdate() {
		String url_first = "http://m.365tmm.com/index.php?r=admin/tmm_software/query&store=store&zip=zip";
		try {
			// 第一步，创建HttpGet对象
			HttpGet httpGet = new HttpGet(url_first);
			DefaultHttpClient httpClient = new DefaultHttpClient();

			// 第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
			HttpResponse httpResponse = httpClient.execute(httpGet);
			Object resultObj = parseJSONString(3,
					getResponseResult(httpResponse));
			int version_zip = getVersionZip();
			if (resultObj != null) {// 如果resultObj不为空则是有版本更新
				// System.out.println("lw    zip有更新！");
				// // 下载zip
				// doDownLoadWork((String) resultObj);
				VersionCompareZip vc = (VersionCompareZip) resultObj;
				if (vc.getVersion() > version_zip) {
					// 下载zip
					doDownLoadWork(vc.getUrl());
				}
			} else {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getVersionZip() {
		String url_second = "http://127.0.0.1:" + WebService.PORT + "/"
				+ Environment.getExternalStorageDirectory()
				+ "/tmm/files/business/version_zip";
		try {
			// 第一步，创建HttpGet对象
			HttpGet httpGet = new HttpGet(url_second);
			DefaultHttpClient httpClient = new DefaultHttpClient();

			// 第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
			HttpResponse httpResponse = httpClient.execute(httpGet);
			Object resultObj = parseJSONString(1,
					getResponseResult(httpResponse));
			return (Integer) resultObj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 解压
	 */
	public void doZipExtractorWork() {
		// ZipExtractorTask task = new
		// ZipExtractorTask("/storage/usb3/system.zip",
		// "/storage/emulated/legacy/", this, true);
		ZipExtractorTask task = new ZipExtractorTask(DB_PATH + "datas.zip",
				DB_TOPATH, context, true);
		task.execute();
	}

	/**
	 * 下载
	 * 
	 * @param url
	 */
	private void doDownLoadWork(String url) {
		// DownLoaderTask task = new DownLoaderTask(
		// "http://192.168.9.155/johnny/testzip.zip",
		// "/storage/emulated/legacy/", context);
		DownLoaderTask task = new DownLoaderTask(url, DB_PATH, context);
		// DownLoaderTask task = new
		// DownLoaderTask("http://192.168.9.155/johnny/test.h264",
		// getCacheDir().getAbsolutePath()+"/", this);
		task.execute();
	}

	public void copyDataBase() throws IOException {
		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;
		// 判断目录是否存在。如不存在则创建一个目录
		File file = new File(DB_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(outFileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(ASSETS_NAME);
		// Open the empty db as the output stream128
		OutputStream myOutput = new FileOutputStream(outFileName);
		// transfer bytes from the inputfile to the outputfile130
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		// Close the streams136
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	/**
	 * 解压缩功能. 将zipFile文件解压到folderPath目录下.
	 * 
	 * @param zipFile
	 *            要解压的压缩文件
	 * @param folderPath
	 *            解压缩的目标目录
	 * @throws IOException
	 *             当解压缩过程出错时抛出
	 */
	public int upZipFile(File zipFile, String folderPath) throws ZipException,
			IOException {
		// public static void upZipFile() throws Exception{
		ZipFile zfile = new ZipFile(zipFile);
		Enumeration zList = zfile.entries();
		ZipEntry ze = null;
		byte[] buf = new byte[1024];
		while (zList.hasMoreElements()) {
			ze = (ZipEntry) zList.nextElement();
			if (ze.isDirectory()) {
				String dirstr = folderPath + /* ze.getName() */"business";
				// dirstr.trim();
				dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
				File f = new File(dirstr);
				f.mkdir();
				continue;
			}
			OutputStream os = new BufferedOutputStream(new FileOutputStream(
					getRealFileName(folderPath, ze.getName())));
			InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
			int readLen = 0;
			while ((readLen = is.read(buf, 0, 1024)) != -1) {
				os.write(buf, 0, readLen);
			}
			is.close();
			os.close();
		}
		zfile.close();
		System.out.println("lw   finish!");
		return 0;
	}

	/**
	 * 给定根目录，返回一个相对路径所对应的实际文件名.
	 * 
	 * @param baseDir
	 *            指定根目录
	 * @param absFileName
	 *            相对路径名，来自于ZipEntry中的name
	 * @return java.io.File 实际的文件
	 */
	public static File getRealFileName(String baseDir, String absFileName) {
		String[] dirs = absFileName.split("/");
		File ret = new File(baseDir);
		String substr = null;
		if (dirs.length > 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				substr = dirs[i];
				try {
					// substr.trim();
					substr = new String(substr.getBytes("8859_1"), "GB2312");

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ret = new File(ret, substr);

			}
			if (!ret.exists())
				ret.mkdirs();
			substr = dirs[dirs.length - 1];
			try {
				// substr.trim();
				substr = new String(substr.getBytes("8859_1"), "GB2312");
				// System.out.println("lw   substr = " + substr);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ret = new File(ret, substr);
			return ret;
		}
		return ret;
	}
}
