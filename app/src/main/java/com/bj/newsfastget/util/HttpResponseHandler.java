/*
    Android Asynchronous Http Client
    Copyright (c) 2011 James Smith <james@loopj.com>
    http://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.bj.newsfastget.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class HttpResponseHandler {
    protected static final int SUCCESS_MESSAGE = 0;
    protected static final int FAILURE_MESSAGE = 1;

    private Handler handler;
    public Class clazz;

    /**
     * Creates a new AsyncHttpResponseHandler
     */
    public HttpResponseHandler(Class t) {
        this.clazz = t;
        // Set up a handler to post events back to the correct thread if possible
        if (Looper.myLooper() != null) {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    HttpResponseHandler.this.handleMessage(msg);
                }
            };
        }
    }


    //
    // Callbacks to be overridden, typically anonymously
    //

    /**
     * Fired when a request returns successfully, override to handle in your own code
     *
     * @param response the body of the HTTP RESTApi response from the server
     */
    public void onSuccess(Object response) {
    }

    /**
     * Called when the request could not be executed due to cancellation, a
     * connectivity problem or timeout. Because networks can fail during an
     * exchange, it is possible that the remote server accepted the request
     * before the failure.
     */
    public void onFailure(AbstractRequest request, Exception e) {
    }


    protected void sendSuccessMessage(Object t) {
        try {
            sendMessage(obtainMessage(SUCCESS_MESSAGE, t));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void sendFailureMessage(AbstractRequest request, Exception e) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, request}));
    }

    //
    // Pre-processing of messages (in original calling thread, typically the UI thread)
    //

    protected void handleSuccessMessage(Object response) {
        onSuccess(response);
    }

    protected void handleFailureMessage(AbstractRequest request, Exception e) {
        onFailure(request, e);
    }


    // Methods which emulate android's Handler and Message methods
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case SUCCESS_MESSAGE:
                handleSuccessMessage(msg.obj);
                break;
            case FAILURE_MESSAGE:
                Object[] response = (Object[]) msg.obj;
                handleFailureMessage((AbstractRequest) response[1], (Exception) response[0]);
                break;
        }
    }

    protected void sendMessage(Message msg) {
        if (handler != null) {
            handler.sendMessage(msg);
        } else {
            handleMessage(msg);
        }
    }

    protected Message obtainMessage(int responseMessage, Object response) {
        Message msg = null;
        if (handler != null) {
            msg = this.handler.obtainMessage(responseMessage, response);
        } else {
            msg = Message.obtain();
            msg.what = responseMessage;
            msg.obj = response;
        }
        return msg;
    }
}
