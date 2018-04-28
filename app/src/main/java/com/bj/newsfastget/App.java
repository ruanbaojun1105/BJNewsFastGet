/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.bj.newsfastget;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import com.bj.newsfastget.util.Cockroach;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.yanzhenjie.nohttp.InitializationConfig;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.OkHttpNetworkExecutor;
import com.yanzhenjie.nohttp.cache.DBCacheStore;
import com.yanzhenjie.nohttp.cookie.DBCookieStore;

import java.util.HashSet;
import java.util.Set;

import me.yokeyword.fragmentation.Fragmentation;
import me.yokeyword.fragmentation.helper.ExceptionHandler;

public class App extends android.app.Application {

    private static final String TAG = "newsfastget-bj-401763159@qq.com";
    public static Context mContext;
    private static App _instance;
    private Set<Activity> allActivities;
    public Vibrator mVibrator;

    public static App getInstance() {
        return _instance;
    }

    public static Context getContext() {
        return mContext;
    }

    public App() {

    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(base);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        mContext = this;

        Cockroach.install(new Cockroach.ExceptionHandler() {
            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LogUtils.e(thread + "\n" + throwable.toString());
                            //Log.d("Cockroach", thread + "\n" + throwable.toString());
                            throwable.printStackTrace();
                        } catch (Throwable e) {
                        }
                    }
                });
            }
        });
        mVibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
        Utils.init(this);
        InitializationConfig configt = InitializationConfig.newBuilder(mContext)
                // 全局连接服务器超时时间，单位毫秒，默认10s。
                .connectionTimeout(20 * 1000)
                // 全局等待服务器响应超时时间，单位毫秒，默认10s。
                .readTimeout(20 * 1000)
                // 配置缓存，默认保存数据库DBCacheStore，保存到SD卡使用DiskCacheStore。
                .cacheStore(
                        // 如果不使用缓存，setEnable(false)禁用。
                        new DBCacheStore(mContext).setEnable(true)
                )
                // 配置Cookie，默认保存数据库DBCookieStore，开发者可以自己实现CookieStore接口。
                .cookieStore(
                        // 如果不维护cookie，setEnable(false)禁用。
                        new DBCookieStore(mContext).setEnable(true)
                )
//         配置网络层，默认URLConnectionNetworkExecutor，如果想用OkHttp：OkHttpNetworkExecutor。
        .networkExecutor(new OkHttpNetworkExecutor())
//         全局通用Header，add是添加，多次调用add不会覆盖上次add。
//        .addHeader()
//         全局通用Param，add是添加，多次调用add不会覆盖上次add。
//        .addParam()
//        .sslSocketFactory() // 全局SSLSocketFactory。
//        .hostnameVerifier() // 全局HostnameVerifier。
        .retry(2) // 全局重试次数，配置后每个请求失败都会重试x次。
        .build();
        NoHttp.initialize(configt);
        Logger.setDebug(BuildConfig.DEBUG);// 开启NoHttp的调试模式, 配置后可看到请求过程、日志和错误信息。
        Logger.setTag("NoHttp-balancingcar-tag");// 打印Log的tag。
        AppConfig.getInstance();
        Fragmentation.builder()
                // 设置 栈视图 模式为 （默认）悬浮球模式   SHAKE: 摇一摇唤出  NONE：隐藏， 仅在Debug环境生效
                .stackViewMode(Fragmentation.BUBBLE)
                .debug(BuildConfig.DEBUG)
                /**
                 * 可以获取到{@link me.yokeyword.fragmentation.exception.AfterSaveStateTransactionWarning}
                 * 在遇到After onSaveInstanceState时，不会抛出异常，会回调到下面的ExceptionHandler
                 */
                .handleException(new ExceptionHandler() {
                    @Override
                    public void onException(Exception e) {
                        // 以Bugtags为例子: 把捕获到的 Exception 传到 Bugtags 后台。
                        // Bugtags.sendException(e);
                    }
                })
                .install();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //Glide.get(this).clearDiskCache();
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).onTrimMemory(level);
    }

    public void addActivity(Activity act) {
        if (allActivities == null) {
            allActivities = new HashSet<>();
        }
        allActivities.add(act);
    }

    public void removeActivity(Activity act) {
        if (allActivities != null) {
            allActivities.remove(act);
        }
    }

    public void exitApp() {
        if (allActivities != null) {
            synchronized (allActivities) {
                for (Activity act : allActivities) {
                    act.finish();
                }
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(float pxValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static void sendLocalBroadCast(String action) {
        sendLocalBroadCast(action, null);
    }

    public static void sendLocalBroadCast(String action, Bundle bundle) {
        Intent bi = new Intent();
        bi.setAction(action);
        if (bundle != null)
            bi.putExtras(bundle);
        LocalBroadcastManager.getInstance(getContext())
                .sendBroadcast(bi);
    }


}
