package com.bj.newsfastget.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.simple.SwipeSimpleFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:43.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class HomeFirstFragment extends SwipeSimpleFragment {

    @BindView(R.id.text)
    TextView text;

    public static HomeFirstFragment newInstance() {
        Bundle args = new Bundle();
        HomeFirstFragment fragment = new HomeFirstFragment();
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
