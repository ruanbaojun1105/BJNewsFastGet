package com.bj.newsfastget.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.simple.SwipeSimpleFragment;
import com.wms.youtubeuploader.sdk.activity.UploadVideoActivity;

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
    }
}
