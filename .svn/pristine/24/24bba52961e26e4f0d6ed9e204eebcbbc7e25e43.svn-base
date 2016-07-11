package com.qljl.tmmdp.video;

import android.content.Context;

import com.qljl.tmmdp.entity.VersionMessage;
import com.qljl.tmmdp.modle.HttpHelper;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

/**
 * 视频http处理
 * Created by luow on 2016/6/2.
 */
public class VideoHttp {
    HttpHelper httpHelper;

    public VideoHttp(Context context) {
        httpHelper = new HttpHelper(context);
    }

    /**
     * 获取服务器的video信息
     * @param url
     * @return
     */
    public VersionMessage getVideoMessage(String url){
        try {
            Response response = httpHelper.getRequest(url);
            if (null != response) {
                String jsonStr = response.body().string();
                if (jsonStr != "" || jsonStr != null) {
                    JSONObject resultJson = new JSONObject(jsonStr);
                    VersionMessage versionMessage = new VersionMessage();
                    versionMessage.setVersion(resultJson.getInt("version"));
                    versionMessage.setUrl(resultJson.getString("down_url"));
                    return versionMessage;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
