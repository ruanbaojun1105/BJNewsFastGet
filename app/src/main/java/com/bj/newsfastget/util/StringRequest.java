package com.bj.newsfastget.util;

import com.yanzhenjie.nohttp.RequestMethod;

public class StringRequest extends AbstractRequest<String> {

    public StringRequest(String url, RequestMethod requestMethod) {
        super(url, requestMethod);
    }

    public StringRequest(String url) {
        super(url);
    }

    @Override
    protected String getResult(String data) {
        return data;
    }
}