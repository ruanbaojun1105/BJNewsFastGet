package com.bj.newsfastget.util;

public abstract class SimpleHttpListener<T> implements HttpListener<T> {
    @Override
    public void onSucceed(int what, Result<T> t) {
    }

    @Override
    public void onFailed(int what) {

    }

    @Override
    public void onFinish(int what) {

    }
}