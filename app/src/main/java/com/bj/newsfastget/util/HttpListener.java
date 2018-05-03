package com.bj.newsfastget.util;

/**
 * Created by Yan Zhenjie on 2016/12/17.
 */
public interface HttpListener<T> {

    void onSucceed(int what, Result<T> t);

    void onFailed(int what,Exception e);

    void onFinish(int what);

}