package com.qljl.tmmdp.modle;

import android.content.Context;

import com.qljl.tmmdp.util.PersistentCookieStore;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * http帮助类
 * Created by luow on 2016/5/6.
 */
public class HttpHelper {
    public final String TAG = "lw  HttpHelper";
    OkHttpClient client;

    public HttpHelper(Context context) {
        super();
        client = new OkHttpClient();
        client.setCookieHandler(new CookieManager(
                new PersistentCookieStore(context),
                CookiePolicy.ACCEPT_ALL));
    }

    /**
     * get请求
     * @param url
     * @return
     */
    public Response getRequest(String url) {
        try {
            Request request = new Request.Builder()
                    .get()
                    .tag(this)
                    .url(url)
                    .build();
            return client.newCall(request).execute();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * post请求
     * @param url
     * @return
     */
    public Call postRequest(String url,HashMap<String,String> hashMap){
        try {
            FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
            Iterator it = hashMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                formEncodingBuilder.add(entry.getKey() + "", entry.getValue() + "");
            }
            RequestBody formBody = formEncodingBuilder.build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            
            return client.newCall(request);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
