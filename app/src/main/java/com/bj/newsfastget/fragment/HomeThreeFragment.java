package com.bj.newsfastget.fragment;

import android.os.Bundle;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.simple.SwipeSimpleFragment;

import butterknife.BindView;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:43.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class HomeThreeFragment  extends SwipeSimpleFragment {
    @BindView(R.id.text)
    TextView text;
    public static HomeThreeFragment newInstance() {
        Bundle args = new Bundle();
        HomeThreeFragment fragment = new HomeThreeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home1;
    }

    @Override
    protected void initCreateView() {
        text.setText(getClass().getName());
    }
}
