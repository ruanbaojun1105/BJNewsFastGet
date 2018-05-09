package com.bj.newsfastget.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.adapter.InfoAdapter;
import com.bj.newsfastget.bean.DataList;
import com.bj.newsfastget.bean.ReadManager;
import com.bj.newsfastget.simple.EventComm;
import com.bj.newsfastget.simple.SwipeSimpleFragment;
import com.bj.newsfastget.util.EntityRequest;
import com.bj.newsfastget.util.HttpListener;
import com.bj.newsfastget.util.HttpUtil;
import com.bj.newsfastget.util.Result;
import com.chad.library.adapter.base.animation.AlphaInAnimation;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:44.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class HomeFourFragment extends SwipeSimpleFragment {
    @BindView(R.id.start)
    Button start;
    @BindView(R.id.recycler1)
    RecyclerView recycler1;
    @BindView(R.id.recycler2)
    RecyclerView recycler2;

    private InfoAdapter itemAdapter1;
    private InfoAdapter itemAdapter2;

    public static HomeFourFragment newInstance() {
        Bundle args = new Bundle();
        HomeFourFragment fragment = new HomeFourFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home4;
    }

    @Override
    protected void initCreateView() {
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toAddR1("开始加载数据到数据库");
                toLoadDataToDBAndSynthesisVideo();
            }
        });
        itemAdapter1 = new InfoAdapter();
        itemAdapter1.openLoadAnimation(new AlphaInAnimation());
        itemAdapter1.setEnableLoadMore(false);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        layoutManager1.setOrientation(LinearLayoutManager.VERTICAL);
        recycler1.setLayoutManager(layoutManager1);
        recycler1.setHasFixedSize(true);
        recycler1.setAdapter(itemAdapter1);

        itemAdapter2 = new InfoAdapter();
        itemAdapter2.openLoadAnimation(new AlphaInAnimation());
        itemAdapter2.setEnableLoadMore(false);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        recycler2.setLayoutManager(layoutManager2);
        recycler2.setHasFixedSize(true);
        recycler2.setAdapter(itemAdapter1);
    }

    private void toLoadDataToDBAndSynthesisVideo() {
        String taget="http://v3.wufazhuce.com:8000/api/channel/reading/more/0?channel=wdj&version=4.0.2&uuid=ffffffff-a90e-706a-63f7-ccf973aae5ee&platform=android";
        EntityRequest<DataList> request=new EntityRequest<>(taget,DataList.class);
        HttpUtil.postExecuteThead(request,new HttpListener() {
            @Override
            public void onSucceed(int what, Result t) {
                DataList dataList= (DataList) t.getResult();
//                ReadManager.getManager().saveTalkList(dataList.data);
            }

            @Override
            public void onFailed(int what,Exception e) {
            }

            @Override
            public void onFinish(int what) {

            }
        });
    }

    private void toAddR1(final String data) {
        recycler1.post(new Runnable() {
            @Override
            public void run() {
                itemAdapter1.addData(0,data);
                recycler1.scrollToPosition(0);
            }
        });

    }

    private void toAddR2(final String data) {
        recycler2.post(new Runnable() {
            @Override
            public void run() {
                itemAdapter2.addData(0,data);
                recycler2.scrollToPosition(0);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final EventComm event) {
        if ("RecyclerToTop".equals(event.getType())){
            recycler1.scrollToPosition(0);
            recycler2.scrollToPosition(0);
        }
    }

}
