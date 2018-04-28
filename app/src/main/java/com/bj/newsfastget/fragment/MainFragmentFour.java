package com.bj.newsfastget.fragment;

import android.os.Bundle;

import com.bj.newsfastget.R;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:26.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class MainFragmentFour extends BaseMainFragment{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initEventAndData() {
        if (findChildFragment(HomeFourFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeFourFragment.newInstance());
        }
    }

    public static MainFragmentFour newInstance() {
        Bundle args = new Bundle();
        MainFragmentFour fragment = new MainFragmentFour();
        fragment.setArguments(args);
        return fragment;
    }
}
