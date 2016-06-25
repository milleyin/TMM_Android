package com.qljl.tmm.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

/**
 * SD文件帮助类
 * 
 * @author lw
 * 
 */
public class FileUtils {
	private String SDPATH;

	public String getSDPATH() {
		return SDPATH;
	}

	public FileUtils() {
		// 得到当前外部存储设备的目录
		SDPATH = Environment.getExternalStorageDirectory() + "/";
	}

	/**
	 * 在SD卡上创建文件
	 * 
	 * @throws IOException
	 */
	public File creatSDFile(String fileName) throws IOException {

		File file = new File(SDPATH + fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * 在SD卡上创建目录
	 * 
	 * @param dirName
	 */
	public File creatSDDir(String dirName) {
		File dir = new File(SDPATH + dirName);
		dir.mkdir();
		return dir;
	}

	/**
	 * 判断SD卡上的文件夹是否存在
	 */
	public boolean isFileExist(String fileName) {

		File file = new File(SDPATH + fileName);
		file.delete();
		return file.exists();

	}

	/**
	 * 将一个InputStream里面的数据写入到SD卡中
	 */
	public File writeToSDFromInput(String path, String fileName,
			InputStream input) {

		File file = null;
		OutputStream output = null;
		try {
			creatSDDir(path);
			file = creatSDFile(path + fileName);
			output = new FileOutputStream(file);
			byte buffer[] = new byte[1024];
			int len = 0;
			// 如果下载成功就开往SD卡里些数据
			while ((len = input.read(buffer)) != -1) {
				output.write(buffer, 0, len);
			}
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return file;
	}

	/**
	 * 拷贝assets的文件目录
	 * 
	 * @param context
	 * @param dirname
	 * @throws IOException
	 */
	public static void copyAssetDirToFiles(Context context, String dirname) {
		File dir = new File(context.getFilesDir() + "/" + dirname);
		dir.mkdirs();
		AssetManager assetManager = context.getAssets();
		String[] children = null;
		try {
			children = assetManager.list(dirname);
			for (String child : children) {
				child = dirname + '/' + child;
				String[] grandChildren = assetManager.list(child);
				if (0 == grandChildren.length) {
					copyAssetFileToFiles(context, child);
				} else {
					copyAssetDirToFiles(context, child);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝asset的文件
	 * 
	 * @param context
	 * @param filename
	 * @throws IOException
	 */
	public static void copyAssetFileToFiles(Context context, String filename) {
		InputStream is;
		try {
			is = context.getAssets().open(filename);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			is.close();

			File of = new File(context.getFilesDir() + "/" + filename);
			of.createNewFile();
			FileOutputStream os = new FileOutputStream(of);
			os.write(buffer);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String readString(String path) {
		String result = "";
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
			String s = "";
			while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
				result = result + s + "\n";
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
