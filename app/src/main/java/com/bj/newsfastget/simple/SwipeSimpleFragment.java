package com.bj.newsfastget.simple;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.yokeyword.fragmentation.SupportFragment;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by bj on 16/8/11.
 * 无MVP的Fragment基类
 */

public abstract class SwipeSimpleFragment extends SupportFragment implements EasyPermissions.PermissionCallbacks {

    protected View mView;
    private Unbinder mUnBinder;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(getLayoutId(), null);
//        EventBusActivityScope.getDefault(_mActivity).register(this);
        mUnBinder = ButterKnife.bind(this, mView);
        EventBus.getDefault().register(this);
        initCreateView();
        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mUnBinder.unbind();
        //EventBusActivityScope.getDefault(_mActivity).unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    protected abstract int getLayoutId();

    protected abstract void initCreateView();


    @Override
    public void onStart() {
        super.onStart();
        LogUtils.e("fragment---onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.e("fragment---onStop");
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(Boolean event) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Snackbar.make(mView, "您拒绝了所需要的相关权限请求!", Toast.LENGTH_SHORT).show();
    }
}
