package com.bj.newsfastget.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.bean.news.NewsItemEntity;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xieye on 2017/4/10.
 */

public class InfoAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public InfoAdapter() {
        super(R.layout.item_info,new ArrayList<String>());
    }

    @Override
    protected void convert(BaseViewHolder helper, String itemEntity) {
        helper.setText(R.id.text,itemEntity);
    }

}
