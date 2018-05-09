package com.bj.newsfastget.adapter;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.bean.ContentList;
import com.bj.newsfastget.simple.GlideApp;
import com.bj.newsfastget.util.HttpListener;
import com.bj.newsfastget.util.HttpUtil;
import com.bj.newsfastget.util.Result;
import com.bj.newsfastget.util.StringRequest;
import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xieye on 2017/4/10.
 */

public class ReadAdapter extends BaseQuickAdapter<ContentList, BaseViewHolder> {

    @BindView(R.id.tv_type)
    TextView tvType;
    @BindView(R.id.tv_toolbar_title)
    TextView tvToolbarTitle;
    @BindView(R.id.tv_author)
    TextView tvAuthor;
    @BindView(R.id.iv_img)
    ImageView ivImg;
    @BindView(R.id.tv_forward)
    TextView tvForward;
    @BindView(R.id.rootView)
    CardView rootView;

    public ReadAdapter() {
        super(R.layout.item_read, new ArrayList<ContentList>());
    }

    @Override
    protected void convert(BaseViewHolder helper, final ContentList itemEntity) {
        ButterKnife.bind(this, helper.itemView);
        String type = "";
        switch (Integer.valueOf(itemEntity.content_type)) {
            case 1:
                type = "阅读";
                break;
            case 2:
                type = "连载";
                break;
            case 3:
                type = "问答";
                break;
        }
        if (itemEntity.tag_list.size() != 0) {
            type = itemEntity.tag_list.get(0).title;
        }
        helper.addOnClickListener(R.id.rootView);
        tvType.setText("- " + type + " -");
        tvToolbarTitle.setText(itemEntity.title);
        tvAuthor.setText("文／" + itemEntity.author.user_name);
        tvForward.setText(itemEntity.forward);
        GlideApp.with(mContext).load(itemEntity.img_url).into(ivImg);
    }


}
