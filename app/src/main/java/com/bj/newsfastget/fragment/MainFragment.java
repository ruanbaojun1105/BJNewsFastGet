package com.bj.newsfastget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bj.newsfastget.R;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:26.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class MainFragment extends BaseMainFragment{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initCreateView() {

    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        if (findChildFragment(HomeFirstFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeFirstFragment.newInstance());
        }
    }

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
