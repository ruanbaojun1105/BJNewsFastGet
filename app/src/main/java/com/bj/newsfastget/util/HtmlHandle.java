package com.bj.newsfastget.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.bj.newsfastget.bean.news.JRTTVideoEntity;
import com.bj.newsfastget.bean.news.YKVideoAlbumListEntity;
import com.bj.newsfastget.bean.news.YKVideoEntity;
import com.bj.newsfastget.bean.news.YKVideoListEntity;
import com.blankj.utilcode.util.LogUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xieye on 2017/4/13.
 */

public class HtmlHandle {


    /**
     * 打开今日头条的视频
     *
     * @param context
     * @param url     原始url
     */
    @SuppressLint("CheckResult")
    public static void openJRTTVideo(final Context context, final String url, final String title) {
        HtmlHandle.getHtml(url)//Document, String
                .map(new Function<Document, String>() {
                    @Override
                    public String apply(Document document) throws Exception {
                        String documentStr = document.toString();
                        if (TextUtils.isEmpty(documentStr)) {
                            return "";
                        }
                        List<String> results = new ArrayList<>();
                        Pattern pattern = Pattern.compile("(?<=videoid:\')[\\w]*(?=\')");
                        Matcher matcher = pattern.matcher(documentStr);
                        while (matcher.find()) {
                            results.add(matcher.group());
                        }
                        if (results.size() >= 1) {
                            return results.get(0);
                        }
                        return "";
                    }
                })
                .map(new Function<String, String>() {

                    @Override
                    public String apply(String s) throws Exception {
                        String responseStr = Http.getJRTTVideo(s).get().toString();
                        JRTTVideoEntity videoEntity = JSONObject.parseObject(responseStr, JRTTVideoEntity.class);
                        JRTTVideoEntity.DataBean.VideoListBean videoList = videoEntity.getData().getVideo_list();

                        String url_origin_1 = videoList.getVideo_1().getMain_url();
                        String url_origin_2 = videoList.getVideo_2().getMain_url();
                        String url_origin_3 = videoList.getVideo_3().getMain_url();


                        if (!TextUtils.isEmpty(url_origin_1))
                            return url_origin_1;
                        if (!TextUtils.isEmpty(url_origin_2))
                            return url_origin_2;
                        if (!TextUtils.isEmpty(url_origin_3))
                            return url_origin_3;
                        return "";
                    }
                    }
                )

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String videoUrl) throws Exception {
                        Log.e("videoUrl>>>" , videoUrl);
                    }
                });

    }

    /**
     * 打开优酷的视频
     *
     * @param context
     * @param videoId 视频id
     */
    @SuppressLint("CheckResult")
    public static void openYKVideo(final Context context, final String videoId, final String title) {
        Observable.just(videoId)
                .map(new Function<String, YKVideoEntity>() {
                    @Override
                    public YKVideoEntity apply(String s) throws Exception {
                        try {
                            String responseStr = Http.getYKVideoInfo(s).get().toString();
                            YKVideoEntity list = JSONObject.parseObject(responseStr, YKVideoEntity.class);
                            return list;
                        } catch (Exception e) {
                            return new YKVideoEntity();
                        }

                    }
                })
                .map(new Function<YKVideoEntity, String>() {

                    @Override
                    public String apply(YKVideoEntity ykVideoEntity) throws Exception {
                        String videoUrl = ykVideoEntity.getData().getStream().get(0).getSegs().get(0).getCdn_url();
                        return videoUrl;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtils.e("videoUrl>>>" , s);
                    }
                });

    }

    public static Observable<YKVideoListEntity> getYouKuVideo(String keyWord) {
        return Observable.just(keyWord)
                .map(new Function<String, YKVideoAlbumListEntity>() {
                    @Override
                    public YKVideoAlbumListEntity apply(String s) throws Exception {
                        try {
                            String responseStr = Http.getYKVideoAlbums(s).get().toString();
                            YKVideoAlbumListEntity list = JSONObject.parseObject(responseStr, YKVideoAlbumListEntity.class);
                            return list;
                        } catch (Exception e) {
                            return new YKVideoAlbumListEntity();
                        }

                    }
                })
                .map(new Function<YKVideoAlbumListEntity, YKVideoListEntity>() {
                    @Override
                    public YKVideoListEntity apply(YKVideoAlbumListEntity ykVideoAlbumListEntity) throws Exception {
                        if (ykVideoAlbumListEntity != null && ykVideoAlbumListEntity.getResults() != null && ykVideoAlbumListEntity.getResults().size() > 0) {
                            try {
                                String responseStr = Http.getYKVideoPlayList(ykVideoAlbumListEntity.getResults().get(0).getPlaylistid()).get().toString();
                                YKVideoListEntity list = JSONObject.parseObject(responseStr, YKVideoListEntity.class);
                                return list;
                            } catch (Exception e) {
                                return null;
                            }

                        } else {
                            return null;
                        }
                    }
                })
                .subscribeOn(Schedulers.computation());
    }


    /**
     * 拿到原始html数据
     *
     * @param url
     * @return
     */
    public static Observable<Document> getHtml(final String url) {
        return new Observable<Document>() {
            @Override
            protected void subscribeActual(Observer<? super Document> observer) {
                try {
                    Connection connection = Jsoup.connect(url);
                    Document doc = Jsoup.connect(url).timeout(5000).get();
                    Document content = Jsoup.parse(doc.toString());
//                Document content = Jsoup.parse(pageXml);
                    observer.onNext(content);
                    observer.onComplete();
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
        }.subscribeOn(Schedulers.newThread());
    }
}
