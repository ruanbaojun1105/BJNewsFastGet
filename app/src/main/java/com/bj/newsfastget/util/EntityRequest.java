package com.bj.newsfastget.util;

import com.alibaba.fastjson.JSON;
import com.yanzhenjie.nohttp.RequestMethod;

public class EntityRequest<T> extends AbstractRequest<T> {

    private Class<T> clazz;

    public EntityRequest(String url, RequestMethod requestMethod, Class<T> clazz) {
        super(url, requestMethod);
        this.clazz = clazz;
    }

    @Override
    protected T getResult(String data) throws Exception {
        return JSON.parseObject(data, clazz);
    }
}