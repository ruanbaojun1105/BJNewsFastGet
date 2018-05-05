package com.bj.newsfastget.simple;

/**
 * com.bj.newsfastget.simple
 *
 * @author Created by Ruan baojun on 15:27.2018/5/4.
 * @email 401763159@qq.com
 * @text
 */
public class EventComm {
    String type;
    int code;
    Object object;
    Object object2;

    public EventComm(int code, Object object, Object object2) {
        this.code = code;
        this.object = object;
        this.object2 = object2;
    }

    public EventComm(int code, Object object) {
        this.code = code;
        this.object = object;
    }

    public EventComm(String type, Object object) {
        this.type = type;
        this.object = object;
    }

    public Object getObject2() {
        return object2;
    }

    public void setObject2(Object object2) {
        this.object2 = object2;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
