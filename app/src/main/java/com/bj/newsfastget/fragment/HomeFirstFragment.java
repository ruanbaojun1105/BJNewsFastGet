package com.bj.newsfastget.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bj.newsfastget.R;
import com.bj.newsfastget.adapter.NewsAdapter;
import com.bj.newsfastget.bean.news.JRTTEntity;
import com.bj.newsfastget.bean.news.NewsItemEntity;
import com.bj.newsfastget.bean.news.SHEntity;
import com.bj.newsfastget.bean.news.YKVideoAlbumListEntity;
import com.bj.newsfastget.bean.news.YKVideoEntity;
import com.bj.newsfastget.bean.news.YKVideoListEntity;
import com.bj.newsfastget.html.HtmlTestHandle;
import com.bj.newsfastget.simple.SwipeSimpleFragment;
import com.bj.newsfastget.util.AbstractRequest;
import com.bj.newsfastget.util.HtmlParseToEntityHandle;
import com.bj.newsfastget.util.Http;
import com.bj.newsfastget.util.HttpResponseHandler;
import com.bj.newsfastget.util.SimpleRxSubscriber;
import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.chad.library.adapter.base.loadmore.SimpleLoadMoreView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:43.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class HomeFirstFragment extends SwipeSimpleFragment {
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.refreshlayout)
    SwipeRefreshLayout refreshlayout;

    private NewsAdapter itemAdapter;

    private int curPage=0;

    public static String jrttUrl = "http://www.toutiao.com/search_content/?offset=0&format=json&keyword=%E7%8B%BC%E4%BA%BA%E6%9D%80&autoload=true&count=20&cur_tab=1";
    public static String shUrl = "http://mt.sohu.com/tags/67121.shtml";
    public static String zhUrl = "https://www.zhihu.com/topic/19846199/top-answers";
    public static String bdUrl = "http://news.baidu.com/ns?word=%1$s&pn=%2$s&cl=2&ct=0&tn=news&rn=20&ie=utf-8&bt=0&et=0";

    public static HomeFirstFragment newInstance() {
        Bundle args = new Bundle();
        HomeFirstFragment fragment = new HomeFirstFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home1;
    }

    @Override
    protected void initCreateView() {
//        text.setText(getClass().getName());
        itemAdapter = new NewsAdapter();
        itemAdapter.openLoadAnimation(new AlphaInAnimation());
        itemAdapter.setEnableLoadMore(true);
        itemAdapter.setLoadMoreView(new SimpleLoadMoreView());
        itemAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                getNewsFromJRRT("海洋", curPage, 20, 2);
            }
        }, recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);
        recycler.setAdapter(itemAdapter);
        refreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                curPage=0;
                getNewsFromJRRT("海洋", 0, 20, 2);
            }
        });
    }

    private List<String> wordWiths = new ArrayList<String>() {
        {
            add("规则");
            add("复盘");
            add("面杀");
            add("申屠");
            add("手狼");
            add("玩家");
            add("四阶");
            add("进阶");
            add("居然");
            add("震惊");
            add("u2");
        }
    };

    @OnClick(R.id.button)
    public void onViewClicked() {
        getNewsFromJRRT("海洋", 0, 20, 1);
    }


    public void getNewsFromJRRT(String keyWord, int offset, int count, int tab) {
        if (TextUtils.isEmpty(keyWord)) {
            keyWord = "海军";
            Random random = new Random();
            int wordWithCount = random.nextInt(3);
            //关键字挖掘
            for (; wordWithCount > 0; wordWithCount--) {
                String wordWith = wordWiths.get(random.nextInt(wordWiths.size()));
                keyWord = keyWord + "," + wordWith;
            }
        } else {
            keyWord = "海军," + keyWord;
        }


        Http.getJRTTList(keyWord, offset, count, tab, new HttpResponseHandler(JRTTEntity.class) {
            @Override
            public void onSuccess(Object response) {
                super.onSuccess(response);
                LogUtils.e(JSON.toJSONString(response));
                JRTTEntity jrttEntity = (JRTTEntity) response;
                parseJRRT2NewsList(jrttEntity);
            }

            @Override
            public void onFailure(AbstractRequest request, Exception e) {
                super.onFailure(request, e);
                LogUtils.e(e.getMessage());
                errorData();
            }
        });
    }

    private void errorData() {
        if (curPage==20) {
            itemAdapter.loadMoreEnd();
        }else
            itemAdapter.loadMoreFail();
        refreshlayout.setRefreshing(false);
    }


    private void parseJRRT2NewsList(JRTTEntity jrttEntity) {
        Observable.just(jrttEntity)
                .map(new Function<JRTTEntity, List<NewsItemEntity>>() {

                    @Override
                    public List<NewsItemEntity> apply(JRTTEntity jrttEntity) throws Exception {
                        List<JRTTEntity.DataBean> datas = jrttEntity.getData();
                        List<NewsItemEntity> resultList = new ArrayList<>();
                        for (JRTTEntity.DataBean dataBean : datas) {
                            NewsItemEntity itemEntity = new NewsItemEntity();
                            if (TextUtils.isEmpty(dataBean.getTitle())) {
                                continue;
                            }
                            itemEntity.setTitle(dataBean.getTitle());
                            itemEntity.setImg(dataBean.getImage_url());
                            List<String> imgs = new ArrayList<>();
                            if (dataBean.getImage_list() != null) {
                                for (JRTTEntity.Img img : dataBean.getImage_list()) {
                                    imgs.add(img.getUrl());
                                }
                            }

                            itemEntity.setImgs(imgs);
                            itemEntity.setAuthor(dataBean.getMedia_name());
                            itemEntity.setLink(dataBean.getUrl());
                            itemEntity.setTimeStr(dataBean.getDatetime());
                            itemEntity.setCommentCount(String.valueOf(dataBean.getComment_count()));
                            itemEntity.setHasVideo(dataBean.isHas_video());
                            itemEntity.setVideoDurationStr(dataBean.getVideo_duration_str());
                            resultList.add(itemEntity);
                        }
                        return resultList;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleRxSubscriber<List<NewsItemEntity>>() {
                    @Override
                    public void onNext(List<NewsItemEntity> newsItemEntities) {
                        if (curPage==20) {
                            curPage=0;
                            itemAdapter.addData(newsItemEntities);
                            itemAdapter.loadMoreEnd();
                        }else {
                            if (curPage==0) {
                                itemAdapter.setNewData(newsItemEntities);
                            }else {
                                itemAdapter.addData(newsItemEntities);
                            }
                            curPage++;
                            itemAdapter.loadMoreComplete();
                        }
                        refreshlayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorData();
                    }
                });
    }


    private void parseSH2NewsList(SHEntity shEntity) {
        Observable.just(shEntity)
                .filter(new Predicate<SHEntity>() {
                    @Override
                    public boolean test(SHEntity shEntity) throws Exception {
                        return shEntity.getCode() == 200;
                    }
                })
                .map(new Function<SHEntity, List<NewsItemEntity>>() {

                    @Override
                    public List<NewsItemEntity> apply(SHEntity shEntity) throws Exception {
                        List<SHEntity.ListBean> datas = shEntity.getList();
                        List<NewsItemEntity> resultList = new ArrayList<>();
                        for (SHEntity.ListBean dataBean : datas) {
                            NewsItemEntity itemEntity = new NewsItemEntity();
                            itemEntity.setTitle(dataBean.getTitle());
                            itemEntity.setImg(dataBean.getPicUrl());
                            itemEntity.setImgs(dataBean.getThumbnails());
                            itemEntity.setAuthor(dataBean.getAuthor());
                            itemEntity.setLink(dataBean.getPath());
                            itemEntity.setTimeStr(dataBean.getPostTimeAsString());
                            itemEntity.setCommentCount(String.valueOf(0));
                            itemEntity.setHasVideo(dataBean.isHasVideo());
                            resultList.add(itemEntity);
                        }
                        return resultList;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleRxSubscriber<List<NewsItemEntity>>() {
                    @Override
                    public void onNext(List<NewsItemEntity> newsItemEntities) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }


    private void parseYKAlbum2NewsList(YKVideoAlbumListEntity albumListEntity) {
        Observable.just(albumListEntity)
                .map(new Function<YKVideoAlbumListEntity, List<NewsItemEntity>>() {

                    @Override
                    public List<NewsItemEntity> apply(YKVideoAlbumListEntity ykVideoAlbumListEntity) throws Exception {
                        List<YKVideoAlbumListEntity.ResultsBean> datas = ykVideoAlbumListEntity.getResults();
                        List<NewsItemEntity> resultList = new ArrayList<>();
                        for (YKVideoAlbumListEntity.ResultsBean dataBean : datas) {
                            NewsItemEntity itemEntity = new NewsItemEntity();
                            itemEntity.setTitle(dataBean.getTitle());
                            itemEntity.setImg(dataBean.getThumburl());
                            itemEntity.setAuthor(dataBean.getUsername());
                            itemEntity.setLink(dataBean.getPlaylistid());
                            itemEntity.setTimeStr(dataBean.getPublish_time());
                            itemEntity.setCommentCount(String.valueOf(0));
                            resultList.add(itemEntity);
                        }
                        return resultList;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleRxSubscriber<List<NewsItemEntity>>() {
                    @Override
                    public void onNext(List<NewsItemEntity> newsItemEntities) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }


    private void parseYKList2NewsList(YKVideoListEntity albumListEntity) {
        Observable.just(albumListEntity)
                .map(new Function<YKVideoListEntity, List<NewsItemEntity>>() {

                    @Override
                    public List<NewsItemEntity> apply(YKVideoListEntity ykVideoListEntity) throws Exception {
                        List<YKVideoListEntity.ShowsBean> datas = ykVideoListEntity.getResultList().get(0).getShows();
                        List<NewsItemEntity> resultList = new ArrayList<>();
                        for (YKVideoListEntity.ShowsBean dataBean : datas) {
                            NewsItemEntity itemEntity = new NewsItemEntity();
                            itemEntity.setTitle(dataBean.getTitle());
                            itemEntity.setImg(dataBean.getThumburl());
                            itemEntity.setAuthor(dataBean.getUsername());
                            itemEntity.setLink(dataBean.getVideoid());
                            itemEntity.setVideoDurationStr(dataBean.getDuration());
                            itemEntity.setTimeStr(dataBean.getPublish_time());
                            itemEntity.setCommentCount(String.valueOf(0));
                            resultList.add(itemEntity);
                        }
                        return resultList;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleRxSubscriber<List<NewsItemEntity>>() {
                    @Override
                    public void onNext(List<NewsItemEntity> newsItemEntities) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    public void getNewsFromSH(String tagId, int pNo, int pSize) {
        Http.getSHList("67121", pNo, pSize, new HttpResponseHandler(SHEntity.class) {
            @Override
            public void onSuccess(Object response) {
                super.onSuccess(response);
                LogUtils.e(JSON.toJSONString(response));
                SHEntity shEntity = (SHEntity) response;
                parseSH2NewsList(shEntity);
            }

            @Override
            public void onFailure(AbstractRequest request, Exception e) {
                super.onFailure(request, e);
                LogUtils.e(e.getMessage());
            }
        });
    }

    public void getNewsFromZH(int page) {
        zhUrl = zhUrl + "?page=" + page;
        HtmlParseToEntityHandle handle = new HtmlParseToEntityHandle(zhUrl, HtmlTestHandle.getZhihuEntity());
        handle.parse()
                .map(new Function<List<NewsItemEntity>, List<NewsItemEntity>>() {
                    @Override
                    public List<NewsItemEntity> apply(List<NewsItemEntity> newsItemEntities) throws Exception {
                        for (NewsItemEntity itemEntity : newsItemEntities) {
                            String link = itemEntity.getLink();
                            if (!TextUtils.isEmpty(link) && (!link.startsWith("http://") || !link.startsWith("https://"))) {
                                itemEntity.setLink(new StringBuilder("http://www.zhihu.com").append(link).toString());
                            }
                        }
                        return newsItemEntities;
                    }
                }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleRxSubscriber<List<NewsItemEntity>>() {
                    @Override
                    public void onNext(List<NewsItemEntity> newsItemEntities) {
                        LogUtils.e("Next>>>" + JSON.toJSONString(newsItemEntities) + ">>");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }


    public void getYKVideoAlbums(String keyWord) {
        if (TextUtils.isEmpty(keyWord)) {
            keyWord = "狼人杀";
        }
        Observable.just(keyWord)
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
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleRxSubscriber<YKVideoAlbumListEntity>() {
                    @Override
                    public void onNext(YKVideoAlbumListEntity listEntity) {
                        parseYKAlbum2NewsList(listEntity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.e(e.getMessage());
                    }
                });
    }

    public void getYKVideoPlayList(String playListId) {
        Observable.just(playListId)
                .map(new Function<String, YKVideoListEntity>() {
                    @Override
                    public YKVideoListEntity apply(String s) throws Exception {
                        try {
                            String responseStr = Http.getYKVideoPlayList(s).get().toString();
                            YKVideoListEntity list = JSONObject.parseObject(responseStr, YKVideoListEntity.class);
                            return list;
                        } catch (Exception e) {
                            return new YKVideoListEntity();
                        }

                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleRxSubscriber<YKVideoListEntity>() {
                    @Override
                    public void onNext(YKVideoListEntity listEntity) {
                        parseYKList2NewsList(listEntity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.e(e.getMessage());
                    }
                });
    }

    public void getNewsFromBD(String keyWord, int page) {
        if (TextUtils.isEmpty(keyWord)) {
            keyWord = "狼人杀";
            Random random = new Random();
            String wordWith = wordWiths.get(random.nextInt(wordWiths.size()));
            keyWord = keyWord + wordWith;
        } else {
            keyWord = "狼人杀," + keyWord;
        }
        String url = String.format(bdUrl, keyWord, String.valueOf(page));
        HtmlParseToEntityHandle handle = new HtmlParseToEntityHandle(url, HtmlTestHandle.getBaiduEntity());
        handle.parse()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleRxSubscriber<List<NewsItemEntity>>() {
                    @Override
                    public void onNext(List<NewsItemEntity> newsItemEntities) {
                        LogUtils.e("Next>>>" + JSON.toJSONString(newsItemEntities) + ">>");
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.e("Next>>>" + e.getMessage());
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void getYKVideoInfo(String videoId) {
        Observable.just(videoId)
                .map(new Function<String, YKVideoEntity>() {

                    @Override
                    public YKVideoEntity apply(String s) throws Exception {
                        try {
                            String responseStr = Http.getYKVideoPlayList(s).get().toString();
                            YKVideoEntity list = JSONObject.parseObject(responseStr, YKVideoEntity.class);
                            return list;
                        } catch (Exception e) {
                            return new YKVideoEntity();
                        }

                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
