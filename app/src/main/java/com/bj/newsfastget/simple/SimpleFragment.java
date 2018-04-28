package com.bj.newsfastget.simple;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;

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

public abstract class SimpleFragment extends SupportFragment implements EasyPermissions.PermissionCallbacks {

    protected View mView;
    protected Activity mActivity;
    protected Context mContext;
    private Unbinder mUnBinder;

    @Override
    public void onAttach(Context context) {
        mActivity = (Activity) context;
        mContext = context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(getLayoutId(), null);
//        EventBusActivityScope.getDefault(_mActivity).register(this);
        mUnBinder = ButterKnife.bind(this, mView);
        EventBus.getDefault().register(this);
        initViewState();
        return mView;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        initEventAndData();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mUnBinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected abstract int getLayoutId();

    /**
     * 初始化一些变量和一些参数，不触及视图变更和试图加载数据
     */
    protected abstract void initViewState();

    /**
     * 初始化懒加载视图变更和试图加载数据
     */
    protected abstract void initEventAndData();

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
//        SnackbarUtils.with(mView).setMessage("您拒绝了所需要的相关权限请求!").setAction("前去开启", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent localIntent = new Intent();
//                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
//                localIntent.setData(Uri.fromParts("package", App.getContext().getPackageName(), null));
//                startActivity(localIntent);
//            }
//        }).setBottomMargin(App.dip2px(50)).showWarning();
    }
}
