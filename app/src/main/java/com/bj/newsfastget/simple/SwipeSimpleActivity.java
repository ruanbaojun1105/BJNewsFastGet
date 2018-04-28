package com.bj.newsfastget.simple;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bj.newsfastget.App;
import com.bj.newsfastget.R;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.SnackbarUtils;
import com.gyf.barlibrary.ImmersionBar;
import com.gyf.barlibrary.OSUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.yokeyword.fragmentation.anim.DefaultNoAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation_swipeback.SwipeBackActivity;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by bj
 * 无MVP的activity基类
 */

public abstract class SwipeSimpleActivity extends SwipeBackActivity implements EasyPermissions.PermissionCallbacks {

    private Unbinder mUnBinder;
    public ImmersionBar mImmersionBar;
    private static final String NAVIGATIONBAR_IS_MIN = "navigationbar_is_min";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        mUnBinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        getSwipeBackLayout().setEdgeLevel(App.dip2px(40));//可滑范围
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        initImmersionBar();
        initEventAndData();
        //解决华为emui3.0与3.1手机手动隐藏底部导航栏时，导航栏背景色未被隐藏的问题
        if (OSUtils.isEMUI3_1()) {
            //第一种
            getContentResolver().registerContentObserver(Settings.System.getUriFor
                    (NAVIGATIONBAR_IS_MIN), true, mNavigationStatusObserver);
            //第二种,禁止对导航栏的设置
            //mImmersionBar.navigationBarEnable(false).init();
        }
    }

    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        // 设置横向(和安卓4.x动画相同)
        return new DefaultNoAnimator();
    }

    /**
     * 限制SwipeBack的条件,默认栈内Fragment数 <= 1时 , 优先滑动退出Activity , 而不是Fragment
     *
     * @return true: Activity优先滑动退出;  false: Fragment优先滑动退出
     */
    @Override
    public boolean swipeBackPriority() {
        return super.swipeBackPriority();
    }


    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }


    private ContentObserver mNavigationStatusObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            int navigationBarIsMin = Settings.System.getInt(getContentResolver(),
                    NAVIGATIONBAR_IS_MIN, 0);
            if (navigationBarIsMin == 1) {
                //导航键隐藏了
                mImmersionBar.transparentNavigationBar().init();
            } else {
                //导航键显示了
                mImmersionBar.navigationBarColor(R.color.bg_page) //隐藏前导航栏的颜色
                        .fullScreen(false)
                        .init();
            }
        }
    };
    protected void initImmersionBar() {
        //在BaseActivity里初始化
        mImmersionBar = ImmersionBar.with(this).navigationBarColor(R.color.bg_page).navigationBarWithKitkatEnable(false);
        mImmersionBar.init();
    }

    protected void setToolBar(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        toolbar.setNavigationIcon(new IconDrawable(mContext, FontAwesomeIcons.fa_angle_left).color(Color.WHITE).sizeDp(25));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressedSupport();
            }
        });
    }

    @Override
    protected void onDestroy() {
        KeyboardUtils.hideSoftInput(this);
        EventBus.getDefault().unregister(this);
        if (mImmersionBar != null)
            mImmersionBar.destroy();  //必须调用该方法，防止内存泄漏，不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
        mUnBinder.unbind();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    protected abstract int getLayout();
    protected abstract void initEventAndData();

    @Override
    public void onStart() {
        super.onStart();
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        EventBus.getDefault().unregister(this);
//    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(Boolean event) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 屏幕横竖屏切换时避免出现window leak的问题
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if (isShouldHideKeyboard(v, ev)) {
                    KeyboardUtils.hideSoftInput(this);
                }
            }
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
//            if (!TextUtils.isEmpty(((EditText) v).getText().toString()))
//                return false;
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
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
        if (getSwipeBackLayout()!=null) {
            SnackbarUtils.with(getSwipeBackLayout()).setMessage("您拒绝了所需要的相关权限请求!").setAction("前去开启", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent localIntent = new Intent();
                    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    localIntent.setData(Uri.fromParts("package", App.getContext().getPackageName(), null));
                    startActivity(localIntent);
                }
            }).setBottomMargin(App.dip2px(50)).showWarning();
        }else
            Toast.makeText(this, "您拒绝了所需要的相关权限请求!", Toast.LENGTH_SHORT).show();
    }
}
