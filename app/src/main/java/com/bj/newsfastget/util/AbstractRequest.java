package com.bj.newsfastget.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.StringRequest;


public abstract class AbstractRequest<T> extends Request<Result<T>> {

    private Class<T> clazz;

    public AbstractRequest(String url, Class<T> clazz) {
        super(url, RequestMethod.GET);
        this.clazz = clazz;
    }

    public AbstractRequest(String url, RequestMethod requestMethod, Class<T> clazz) {
        super(url, requestMethod);
        this.clazz = clazz;
    }

    // 这个方法由继承的子类去实现，解析成我们真正想要的数据类型。
    protected abstract T getResult(String data);

    @Override
    public Result<T> parseResponse(Headers headers, byte[] body) throws Exception {
        int responseCode = headers.getResponseCode(); // 响应码。

        // 响应码等于200，Http层成功。
        if (responseCode == 200) {
            if (body == null || body.length == 0) {
                // 服务器包体为空。
                return new Result<>(true, null, headers, null);
            } else {
                // 这里可以统一打印所有请求的数据哦：
                String bodyString = StringRequest.parseResponseString(headers, body);
                try {
                    T result = getResult(bodyString);
                    return new Result<>(true, result, headers, null);
                    // 业务层成功。
//                    JSONObject bodyObject = JSON.parseObject(bodyString);
//                    if (bodyObject.getIntValue("errorCode") == 1) {
//                        String data = bodyObject.getString("data");
//                        // 重点、重点、重点：调用子类，解析出真正的数据。
//                        T result = getResult(data);
//                        return new Result<>(true, result, headers, null);
//                    } else {
//                        String error = bodyObject.getString("message");
//                        return new Result<>(false, null, headers, error);
//                    }
                } catch (Exception e) {
                    // 解析异常，测试时通过，正式发布后就是服务器的锅。
                    String error = "服务器返回数据格式错误，请稍后重试";
                    return new Result<>(false, null, headers, error);
                }
            }
        } else { // 其它响应码，如果和服务器没有约定，那就是服务器发生错误了。
            String error = "服务器返回数据格式错误，请稍后重试";
            return new Result<>(false, null, headers, error);
        }
    }
}