package com.qljl.tmmdp.video;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * 文件操作
 * Created by luow on 2016/6/2.
 */
public class FileUtils {
    private Context context;
    String savePath = Environment.getExternalStorageDirectory()+"/TmmVideo/";

    public FileUtils(Context context) {
        this.context = context;
    }

    // 把数据写入SD卡
    public void writeSDcard(String str) {
        try {
            // 判断是否存在SD卡
            if (hasSDcard()) {
                // 获取SD卡的目录
                File file = new File(savePath);
                if(file.exists()){

                }else{
                    file.mkdir();
                }
                FileOutputStream fileW = new FileOutputStream(file.getCanonicalPath() + "/videoVersion.txt");
                fileW.write(str.getBytes());
                fileW.close();
            }else{
                //SD卡不存在
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 从SD卡中读取数据
    public String readSDcard() {
        StringBuffer str = new StringBuffer();
        try {
            // 判断是否存在SD
            if (hasSDcard()) {
                File file = new File(savePath+"videoVersion.txt");
                // 判断是否存在该文件
                if (file.exists()) {
                    // 打开文件输入流
                    FileInputStream fileR = new FileInputStream(file);
                    BufferedReader reads = new BufferedReader(
                            new InputStreamReader(fileR));
                    String st = null;
                    while ((st =reads.readLine())!=null ) {
                        str.append(st);
                        System.out.println("lw  读取txt内容st="+st);
                    }
                    fileR.close();
                } else {
                    //该目录下文件不存在
                }
            }else{
                //SD卡不存在
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    /**
     * 是否有sd卡
     * @return
     */
    public boolean hasSDcard(){
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取本地的video
     * @return
     */
    public String getOldVideo(){
        /*ContentResolver contentResolver = context.getContentResolver();
        String[] projection = new String[]{MediaStore.Video.Media.TITLE,MediaStore.Video.Media.DATA};
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
                null, null,null);
        cursor.moveToFirst();
        int fileNum = cursor.getCount();
        for(int counter = 0; counter < fileNum; counter++){
            Log.w("lw", "--------------file is: " + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
            Log.w("lw", "--------------file path is: " + cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
            //如果路径包含"/storage/emulated/0/TmmVideo/"则为我们的音频
            if(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)).contains(savePath)){
                return cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            }
            cursor.moveToNext();
        }
        cursor.close();
        return null;*/

        File mfile = new File(savePath);
        File[] files = mfile.listFiles();

        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            System.out.println("lw   file.path:" + file.getPath());
            String str = file.getPath();
            System.out.println("lw   filename:" + str.substring(str.lastIndexOf(".")+1));
            String strForm = str.substring(str.lastIndexOf(".")+1);
            if(strForm.equals("mp4") || strForm.equals("rmvb") || strForm.equals("avi")
                    || strForm.equals("mpeg") || strForm.equals("wmv") || strForm.equals("3gp")
                    || strForm.equals("dat") || strForm.equals("vob") || strForm.equals("flv")){
                //如果有这些格式的视频则返回它的路径
                return str;
            }
        }
        return null;
    }

    /**
     * 删除TmmVideo目录除了刚下载的video文件的其他音频文件
     * @param nowFile
     */
    public void deleteOldVideo(String nowFile){
        System.out.println("lw   nowFile:"+nowFile);
        // 得到该路径文件夹下所有的文件
        File mfile = new File(savePath);
        File[] files = mfile.listFiles();

        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            System.out.println("lw   file.path:" + file.getPath());
            String str = file.getPath();
            System.out.println("lw   filename:" + str.substring(str.lastIndexOf("/")+1));
            if(str.substring(str.lastIndexOf("/")+1).equals(nowFile) || str.substring(str.lastIndexOf("/")+1).equals("videoVersion.txt")){
                //什么也不做
            }else{
                file.delete();
            }
        }
    }

    /**
     * 获取SD卡内存大小
     * @return
     */
    public long SdSize(){
        //获得SD卡空间的信息
                 File path=Environment.getExternalStorageDirectory();
                 StatFs statFs=new StatFs(path.getPath());
                 long blocksize=statFs.getBlockSize();
                long totalblocks=statFs.getBlockCount();
                 long availableblocks=statFs.getAvailableBlocks();

                 //计算SD卡的空间大小
                 long totalsize=blocksize*totalblocks;
                 long availablesize=availableblocks*blocksize;
return totalsize;
    }
}
