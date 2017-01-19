package com.example.liusw.httpdemo;

import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.cache.DBCacheStore;


/**
 * Created by liusw on 2016/12/20.
 */
public class Application extends android.app.Application
{
    private static Application _instance;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        NoHttp.initialize(this);

        NoHttp.initialize(this,new NoHttp.Config()
                .setCacheStore(
                        new DBCacheStore(this)// 配置缓存到数据库。
                        .setEnable(true)));// true启用缓存，fasle禁用缓存。
     //   NoHttp.initialize(this,new NoHttp.Config().setNetworkExecutor(new OkHttpN));

    }

    public static Application getInstance(){
        return _instance;
    }

}
