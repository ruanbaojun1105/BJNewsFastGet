package com.bj.newsfastget.util;

import com.alibaba.fastjson.JSON;
import com.yanzhenjie.nohttp.RequestMethod;

import java.util.List;

public class EntityListRequest<T> extends AbstractRequest<List<T>> {

    private Class<T> clazz;

    public EntityListRequest(String url, RequestMethod requestMethod, Class<T> clazz) {
        super(url, requestMethod);
        this.clazz = clazz;
    }

    @Override
    protected List<T> getResult(String data) throws Exception {
        return JSON.parseArray(data, clazz);
    }
}