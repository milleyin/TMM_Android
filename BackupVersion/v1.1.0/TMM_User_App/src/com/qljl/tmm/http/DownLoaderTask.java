package com.qljl.tmm.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 下载zip
 * 
 * @author lw
 * 
 */
public class DownLoaderTask extends AsyncTask<Void, Integer, Long> {
	private final String TAG = "DownLoaderTask";
	private URL mUrl;
	private File mFile;
	// private ProgressDialog mDialog;
	private int mProgress = 0;
	private ProgressReportingOutputStream mOutputStream;
	private Context mContext;

	public DownLoaderTask(String url, String out, Context context) {
		super();
		if (context != null) {
			// mDialog = new ProgressDialog(context);
			mContext = context;
		} else {
			// mDialog = null;
		}

		try {
			mUrl = new URL(url);
			// String fileName = new File(mUrl.getFile()).getName();
			String fileName = "user.zip";
			System.out.println("lw    fileName=============>" + fileName);
			mFile = new File(out, fileName);
			Log.d(TAG, "lw out=" + out + ", name=" + fileName
					+ ",mUrl.getFile()=" + mUrl.getFile());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected Long doInBackground(Void... params) {
		// TODO Auto-generated method stub
		return download();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	}

	@Override
	protected void onPostExecute(Long result) {
		// TODO Auto-generated method stub
		// super.onPostExecute(result);
		// if (mDialog != null && mDialog.isShowing()) {
		// mDialog.dismiss();
		// }
		if (isCancelled())
			return;
		// ((MainActivity) mContext).showUnzipDialog();
		HttpHelper httpHelper = new HttpHelper(mContext);
		String path = httpHelper.DB_PATH + httpHelper.DB_NAME;
		File zipFile = new File(path);
		try {
			httpHelper.upZipFile(zipFile, httpHelper.DB_TOPATH);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private long download() {
		URLConnection connection = null;
		int bytesCopied = 0;
		try {
			connection = mUrl.openConnection();
			int length = connection.getContentLength();
			if (mFile.exists() && length == mFile.length()) {
				Log.d(TAG, "lw file " + mFile.getName() + " already exits!!");
				// return 0l;
			}
			mOutputStream = new ProgressReportingOutputStream(mFile);
			publishProgress(0, length);
			bytesCopied = copy(connection.getInputStream(), mOutputStream);
			if (bytesCopied != length && length != -1) {
				Log.e(TAG, "lw    Download incomplete bytesCopied="
						+ bytesCopied + ", length" + length);
			}
			mOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytesCopied;
	}

	private int copy(InputStream input, OutputStream output) {
		byte[] buffer = new byte[1024 * 8];
		BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
		BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
		int count = 0, n = 0;
		try {
			while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
				out.write(buffer, 0, n);
				count += n;
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return count;
	}

	private final class ProgressReportingOutputStream extends FileOutputStream {
		public ProgressReportingOutputStream(File file)
				throws FileNotFoundException {
			super(file);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void write(byte[] buffer, int byteOffset, int byteCount)
				throws IOException {
			// TODO Auto-generated method stub
			super.write(buffer, byteOffset, byteCount);
			mProgress += byteCount;
			publishProgress(mProgress);
		}

	}
}
