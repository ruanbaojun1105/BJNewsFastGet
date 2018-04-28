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
public class MainFragmentThree extends BaseMainFragment{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initEventAndData() {
        if (findChildFragment(HomeThreeFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeThreeFragment.newInstance());
        }
    }

    public static MainFragmentThree newInstance() {
        Bundle args = new Bundle();
        MainFragmentThree fragment = new MainFragmentThree();
        fragment.setArguments(args);
        return fragment;
    }
}
