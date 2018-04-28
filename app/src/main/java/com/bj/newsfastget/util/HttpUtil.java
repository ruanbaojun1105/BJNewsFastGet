package com.bj.newsfastget.util;

import android.content.Context;

import com.bj.newsfastget.AppConfig;
import com.bj.newsfastget.BuildConfig;
import com.bj.newsfastget.R;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.yanzhenjie.nohttp.BasicBinary;
import com.yanzhenjie.nohttp.FileBinary;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.OnUploadListener;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.download.DownloadListener;
import com.yanzhenjie.nohttp.download.DownloadRequest;
import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.ServerError;
import com.yanzhenjie.nohttp.error.StorageReadWriteError;
import com.yanzhenjie.nohttp.error.StorageSpaceNotEnoughError;
import com.yanzhenjie.nohttp.error.TimeoutError;
import com.yanzhenjie.nohttp.error.URLError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.yanzhenjie.nohttp.rest.AsyncRequestExecutor;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.rest.SimpleResponseListener;
import com.yanzhenjie.nohttp.rest.StringRequest;
import com.yanzhenjie.nohttp.rest.SyncRequestExecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import okhttp3.MediaType;

/**
 * Created by Administrator on 2016/9/12.
 */
public class HttpUtil {
    
    public interface OnHttpInterFace {
        void onSuccess(int status, String message, Object dataJson);
        void onFail(int status, String message);
    }

    public interface OnUploadInterFace {
        void onSuccess(int status, String message, Object dataJson);
        void onFinish();
        void onFail(int status, String message);
    }
    
    public static MediaType JSON=MediaType.parse("App/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

//    http://39.108.182.251:28080/
    private static String ip="http://192.168.1.110:";
//    private static String ip="http://39.108.182.251:";
//    public static String server="http://39.108.182.251:28080/";
    public static String server= false?(ip+"8082/"):"http://39.108.182.251:28080/";//社区
    public static String server1= false?(ip+"8083/"):"http://39.108.182.251:38080/";//商城
    public static String server2= server;
    public static String server3= BuildConfig.DEBUG?(ip+"8084/"):"http://39.108.182.251:48080/";//订单
    public static String imageServer="http://39.108.182.251:90/images/";
    public static String downPath="https://fir.im/rslk";
    /**
     * 根据数据获取请求链接
     * @param api
     * @return
     */
    public static String getHttpRequestUrl(String api){
        return server+api;
    }
    public static String getStoreRequestUrl(String api){
        return server1+api;
    }
    public static String getStoreNewRequestUrl(String api){
        return server2+api;
    }
    public static String getStoreNew3RequestUrl(String api){
        return server3+api;
    }

    //同步请求
    public static void postExecute(Map<String, Object> params, String api, OnHttpInterFace onHttpInterFace) {
        StringRequest req = new StringRequest(getHttpRequestUrl(api), RequestMethod.POST);
        if (params!=null)
            req.add(params);
        Response<String> response= SyncRequestExecutor.INSTANCE.execute(req);
        if (response.isSucceed()) {
           LogUtils.e("--API:"+api+"--SUCCESS HTTP:"+response.get());
            try {
                JSONObject jsonObject= new JSONObject(response.get());
                int status=jsonObject.getInt("status");
                String msg=jsonObject.getString("msg");
                Object data=jsonObject.get("data");
                if (status==200){
                    if (onHttpInterFace!=null){
                        onHttpInterFace.onSuccess(status,msg,data);
                    }
                }
                else {
                    if (onHttpInterFace!=null){
                        onHttpInterFace.onFail(status,msg);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (onHttpInterFace!=null){
                    onHttpInterFace.onFail(500,"数据解析错误！");
                }
            }
        } else {
            LogUtils.e("--API:"+api+"--FAIL HTTP:"+response.getException().toString());
            if (onHttpInterFace!=null){
                String msg="服务器请求失败！";
                if (response.getException() instanceof TimeoutException)
                    msg="服务器请求超时";
                onHttpInterFace.onFail(response.responseCode(),msg);
            }
        }
    }
    //异步线程池无优先级请求
    public static void postExecuteThead(Map<String, Object> params, final String api, final OnHttpInterFace onHttpInterFace) {
        postExecuteThead(params, api, onHttpInterFace,0);
    }

    public static void postExecuteThead(Map<String, Object> params, final String api, final OnHttpInterFace onHttpInterFace,int serverTag) {
        StringRequest request=null;
        if (serverTag==0)
            request = new StringRequest(getHttpRequestUrl(api), RequestMethod.POST);
        else if (serverTag==1)
            request = new StringRequest(getStoreRequestUrl(api), RequestMethod.POST);
        else if (serverTag==2)
            request = new StringRequest(getStoreNewRequestUrl(api), RequestMethod.POST);
        else if (serverTag==3)
            request = new StringRequest(getStoreNew3RequestUrl(api), RequestMethod.POST);
        if (request==null) {
            if (onHttpInterFace!=null){
                onHttpInterFace.onFail(500,"没有请求实体");
            }
            return;
        }
        if (params!=null)
            request.add(params);
        AsyncRequestExecutor.INSTANCE.execute(0, request, new SimpleResponseListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                LogUtils.e("--API:"+api+"--SUCCESS HTTP:"+response.get());
//                com.blankj.utilcode.util.LogUtils.e("--API:"+api+"--SUCCESS HTTP:"+response.get());
                try {
                    JSONObject jsonObject= new JSONObject(response.get());
                    int status=jsonObject.getInt("status");
                    String msg=jsonObject.getString("msg");
                    Object data=jsonObject.get("data");
                    if (status==200){
                        if (onHttpInterFace!=null){
                            onHttpInterFace.onSuccess(status,msg,data);
                        }
                    }
                    else {
                        if (onHttpInterFace!=null){
                            onHttpInterFace.onFail(status,msg);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (onHttpInterFace!=null){
                        onHttpInterFace.onFail(500,"数据解析错误！");
                    }
                }

            }

            @Override
            public void onFailed(int what, Response<String> response) {
                LogUtils.e("--API:"+api+"--FAIL HTTP:"+response.getException().toString());
                if (onHttpInterFace!=null){
                    String msg="服务器请求失败！";
                    if (response.getException() instanceof TimeoutException)
                        msg="服务器请求超时";
                    onHttpInterFace.onFail(response.responseCode(),msg);
                }
            }
        });
    }

    public static  void uploadMultiFile(Map<String, Object> params, String api, List<File> filePaths,
                                        OnUploadListener mOnUploadListener, final OnUploadInterFace onHttpInterFace) {
        uploadMultiFile(params, api, filePaths, mOnUploadListener, onHttpInterFace,false);
    }
    /**
     * 上传多个文件。
     */
    public static  void uploadMultiFile(Map<String, Object> params, String api, List<File> filePaths,
                                        OnUploadListener mOnUploadListener, final OnUploadInterFace onHttpInterFace, boolean selfAPI) {
        Request<String> request = NoHttp.createStringRequest(selfAPI?api:getHttpRequestUrl(api), RequestMethod.POST);//"http://api.nohttp.net/upload"
        //request.setContentType("application/x-www-form-urlencoded");
        // 添加普通参数。
        if (params!=null)
            request.add(params);
        //request.add("user", "yolanda");
        // 上传文件需要实现NoHttp的Binary接口，NoHttp默认实现了FileBinary、InputStreamBinary、ByteArrayBitnary、BitmapBinary。
        for (int i = 0; i < filePaths.size(); i++) {
            // FileBinary用法
            LogUtils.e("filePaths:"+i+"-----"+filePaths.get(i));
            BasicBinary binary = new FileBinary(filePaths.get(i));
            if (mOnUploadListener!=null)
                binary.setUploadListener(i, mOnUploadListener);
            request.add("uploadFile", binary);// 添加1个文件
        }


        CallServer.getInstance().request(0, request, new SimpleResponseListener<String>() {
            @Override
            public void onStart(int what) {
                LogUtils.e("onStart");
                super.onStart(what);
            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                LogUtils.e("onSucceed:"+response.get());
                super.onSucceed(what, response);
                if (onHttpInterFace!=null)
                    onHttpInterFace.onSuccess(200,"上传完成",response.get());
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                LogUtils.e("onFailed");
                super.onFailed(what, response);
                if (onHttpInterFace!=null)
                    onHttpInterFace.onFail(500,"上传失败");
            }

            @Override
            public void onFinish(int what) {
                LogUtils.e("onFinish");
                super.onFinish(what);
                if (onHttpInterFace!=null)
                    onHttpInterFace.onFinish();
            }
        });
    }
    public static  void uploadSingleFile(Map<String, Object> params,String api,String filePath,OnUploadListener mOnUploadListener,
                                         final OnUploadInterFace onHttpInterFace) {
        uploadSingleFile(params, api, filePath, mOnUploadListener, onHttpInterFace,false);
    }
    /**
     * 上传单个文件。
     */
    public static  void uploadSingleFile(Map<String, Object> params,String api,String filePath,OnUploadListener mOnUploadListener,
                                         final OnUploadInterFace onHttpInterFace,boolean selfAPI) {
        Request<String> request = NoHttp.createStringRequest(selfAPI?api:getHttpRequestUrl(api), RequestMethod.POST);

        // 添加普通参数。
        if (params!=null)
            request.add(params);
        //request.add("user", "yolanda");
        // 上传文件需要实现NoHttp的Binary接口，NoHttp默认实现了FileBinary、InputStreamBinary、ByteArrayBitnary、BitmapBinary。
        // FileBinary用法
        BasicBinary binary = new FileBinary(new File(filePath));
//        File file=new File(filePath);
//        BasicBinary binary = null;
//        try {
//            binary = new InputStreamBinary(new FileInputStream(file), file.getName());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        /**
         * 监听上传过程，如果不需要监听就不用设置。
         * 第一个参数：what，what和handler的what一样，会在回调被调用的回调你开发者，作用是一个Listener可以监听多个文件的上传状态。
         * 第二个参数： 监听器。
         */
        if (mOnUploadListener!=null)
            binary.setUploadListener(0x01, mOnUploadListener);

        request.add("uploadFile", binary);// 添加1个文件
//            request.add("image1", fileBinary1);// 添加2个文件

        CallServer.getInstance().request(0, request, new SimpleResponseListener<String>() {
            @Override
            public void onStart(int what) {
                LogUtils.e("onStart");
                super.onStart(what);
            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                LogUtils.e("onSucceed:"+response.get());
                super.onSucceed(what, response);
                try {
                    JSONObject jsonObject= new JSONObject(response.get());
                    int status=jsonObject.getInt("status");
                    String msg=jsonObject.getString("msg");
                    Object data=jsonObject.get("data");
                    if (status==200){
                        if (onHttpInterFace!=null){
                            onHttpInterFace.onSuccess(status,"上传成功！",data);
                        }
                    }else {
                        if (onHttpInterFace!=null){
                            onHttpInterFace.onFail(status,msg);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (onHttpInterFace!=null){
                        onHttpInterFace.onFail(500,"数据解析错误！");
                    }
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                LogUtils.e("onFailed");
                super.onFailed(what, response);
                if (onHttpInterFace!=null)
                    onHttpInterFace.onFail(500,"上传失败");
            }

            @Override
            public void onFinish(int what) {
                LogUtils.e("onFinish");
                super.onFinish(what);
                if (onHttpInterFace!=null)
                    onHttpInterFace.onFinish();
            }
        });
    }

    public  static void downFile(final Context context, String downUrl, final OnHttpInterFace uploadInterFace){
        downFile(context, downUrl,null, uploadInterFace);
    }
    public  static void downFile(final Context context, String downUrl,String fileName, final OnHttpInterFace uploadInterFace){
        DownloadRequest mDownloadRequest = new DownloadRequest(downUrl, RequestMethod.GET,
                AppConfig.getInstance().APP_PATH_ROOT,fileName,
                true, false);

        // what 区分下载。
        // downloadRequest 下载请求对象。
        // downloadListener 下载监听。
        CallServer.getInstance().download(0, mDownloadRequest, new DownloadListener() {
            @Override
            public void onDownloadError(int what, Exception exception) {
                String message = context.getString(R.string.download_error);
                String messageContent;
                if (exception instanceof ServerError) {
                    messageContent = context.getString(R.string.download_error_server);
                } else if (exception instanceof NetworkError) {
                    messageContent = context.getString(R.string.download_error_network);
                } else if (exception instanceof StorageReadWriteError) {
                    messageContent = context.getString(R.string.download_error_storage);
                } else if (exception instanceof StorageSpaceNotEnoughError) {
                    messageContent = context.getString(R.string.download_error_space);
                } else if (exception instanceof TimeoutError) {
                    messageContent = context.getString(R.string.download_error_timeout);
                } else if (exception instanceof UnKnownHostError) {
                    messageContent = context.getString(R.string.download_error_un_know_host);
                } else if (exception instanceof URLError) {
                    messageContent = context.getString(R.string.download_error_url);
                } else {
                    messageContent = context.getString(R.string.download_error_un);
                }
                message = String.format(Locale.getDefault(), message, messageContent);
                ToastUtils.showLong(message);
            }

            @Override
            public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {

            }

            @Override
            public void onProgress(int what, int progress, long fileCount, long speed) {

            }

            @Override
            public void onFinish(int what, String filePath) {
                uploadInterFace.onSuccess(what,filePath,null);
            }

            @Override
            public void onCancel(int what) {

            }
        });
    }
}
