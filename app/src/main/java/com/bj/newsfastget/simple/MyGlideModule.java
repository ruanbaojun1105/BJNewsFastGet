package com.bj.newsfastget.simple;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

public class MyGlideModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Apply options to the builder here.
        //获取系统分配给应用的总内存大小
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
//设置图片内存缓存占用八分之一
        int memoryCacheSize = maxMemory / 10;
//设置内存缓存大小
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        //registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(ProgressManager.getOkHttpClient()));
    }
}