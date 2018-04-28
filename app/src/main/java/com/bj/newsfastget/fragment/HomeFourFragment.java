package com.bj.newsfastget.fragment;

import android.os.Bundle;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.simple.SwipeSimpleFragment;

import butterknife.BindView;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:44.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class HomeFourFragment  extends SwipeSimpleFragment {
    @BindView(R.id.text)
    TextView text;
    public static HomeFourFragment newInstance() {
        Bundle args = new Bundle();
        HomeFourFragment fragment = new HomeFourFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home1;
    }

    @Override
    protected void initEventAndData() {
        text.setText(getClass().getName());
    }
}
