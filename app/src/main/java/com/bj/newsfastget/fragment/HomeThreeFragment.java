package com.bj.newsfastget.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.adapter.InfoAdapter;
import com.bj.newsfastget.adapter.ReadAdapter;
import com.bj.newsfastget.bean.DataList;
import com.bj.newsfastget.bean.ReadDetail;
import com.bj.newsfastget.simple.SwipeSimpleFragment;
import com.bj.newsfastget.util.DefaultResponseListener;
import com.bj.newsfastget.util.EntityRequest;
import com.bj.newsfastget.util.Http;
import com.bj.newsfastget.util.HttpListener;
import com.bj.newsfastget.util.HttpUtil;
import com.bj.newsfastget.util.Result;
import com.bj.newsfastget.util.StringRequest;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.wms.youtubeuploader.sdk.activity.UploadVideoActivity;
import com.yanzhenjie.nohttp.rest.AsyncRequestExecutor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:43.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class HomeThreeFragment  extends SwipeSimpleFragment {
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    private ReadAdapter itemAdapter;
    private static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static HomeThreeFragment newInstance() {
        Bundle args = new Bundle();
        HomeThreeFragment fragment = new HomeThreeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home3;
    }

    @Override
    protected void initCreateView() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(_mActivity,UploadVideoActivity.class));
            }
        });
        itemAdapter = new ReadAdapter();
        itemAdapter.openLoadAnimation(new AlphaInAnimation());
        itemAdapter.setEnableLoadMore(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);
        refreshList(TimeUtils.getNowString(DEFAULT_FORMAT));
        itemAdapter.bindToRecyclerView(recycler);
        itemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                LogUtils.e("-----------");
            }
        });
        itemAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                LogUtils.e("-----------");
                switch (view.getId()) {
                    case R.id.rootView:
                        String taget = "http://v3.wufazhuce.com:8000/api/essay/" + itemAdapter.getItem(position).item_id + "?channel=wdj&source=channel_reading&source_id=9264&version=4.0.2&uuid=ffffffff-a90e-706a-63f7-ccf973aae5ee&platform=android";
                        EntityRequest<ReadDetail> request = new EntityRequest<>(taget,ReadDetail.class);
                        HttpUtil.postExecuteThead(request, new HttpListener() {
                            @Override
                            public void onSucceed(int what, Result t) {
                                LogUtils.e(t.getResult().toString());
                                ReadDetail readDetail= (ReadDetail) t.getResult();
                            }

                            @Override
                            public void onFailed(int what, Exception e) {
                            }

                            @Override
                            public void onFinish(int what) {

                            }
                        });
                        break;
                }
            }
        });
    }

    private void refreshList(String time) {
        //阅读详情
//        String taget="http://v3.wufazhuce.com:8000/api/essay/" + item_id + "?channel=wdj&source=channel_reading&source_id=9264&version=4.0.2&uuid=ffffffff-a90e-706a-63f7-ccf973aae5ee&platform=android";
        //连载
//        String taget="http://v3.wufazhuce.com:8000/api/serialcontent/bymonth/"+time+
//         "%2000:00:00?channel=wdj&version=4.0.2&uuid=ffffffff-a90e-706a-63f7-ccf973aae5ee&platform=android";

        //音乐
        //String taget="http://v3.wufazhuce.com:8000/api/music/bymonth/"+time+
        // "%2000:00:00?channel=wdj&version=4.0.2&uuid=ffffffff-a90e-706a-63f7-ccf973aae5ee&platform=android";

        //影视
//        String taget="http://v3.wufazhuce.com:8000/api/channel/movie/more/0?channel=wdj&version=4.0.2&uuid=ffffffff-a90e-706a-63f7-ccf973aae5ee&platform=android";

        //阅读
        String taget="http://v3.wufazhuce.com:8000/api/channel/reading/more/0?channel=wdj&version=4.0.2&uuid=ffffffff-a90e-706a-63f7-ccf973aae5ee&platform=android";
        EntityRequest<DataList> request=new EntityRequest<>(taget,DataList.class);
        HttpUtil.postExecuteThead(request,new HttpListener() {
            @Override
            public void onSucceed(int what, Result t) {
                DataList dataList= (DataList) t.getResult();
                itemAdapter.setNewData(dataList.data);
            }

            @Override
            public void onFailed(int what,Exception e) {
            }

            @Override
            public void onFinish(int what) {

            }
        });
    }
}
