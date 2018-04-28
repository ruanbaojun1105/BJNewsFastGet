package com.bj.newsfastget.activity;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bj.newsfastget.R;
import com.bj.newsfastget.bean.TabSelectedEvent;
import com.bj.newsfastget.fragment.BaseMainFragment;
import com.bj.newsfastget.fragment.HomeFirstFragment;
import com.bj.newsfastget.fragment.HomeFourFragment;
import com.bj.newsfastget.fragment.MainFragment;
import com.bj.newsfastget.fragment.MainFragmentFour;
import com.bj.newsfastget.fragment.MainFragmentThree;
import com.bj.newsfastget.fragment.MainFragmentTwo;
import com.bj.newsfastget.fragment.HomeThreeFragment;
import com.bj.newsfastget.fragment.HomeTwoFragment;
import com.bj.newsfastget.simple.SimpleActivity;
import com.bj.newsfastget.simple.SwipeSimpleActivity;
import com.bj.newsfastget.view.BottomBar;
import com.bj.newsfastget.view.BottomBarTab;

import butterknife.BindView;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;

public class MainActivity extends SimpleActivity
        implements NavigationView.OnNavigationItemSelectedListener,BaseMainFragment.OnBackToFirstListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOURTH = 3;

    private SupportFragment[] mFragments = new SupportFragment[4];

    @BindView(R.id.bottomBar)
    BottomBar mBottomBar;

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initEventAndData() {
        setToolBar(toolbar,"主页");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);

        SupportFragment firstFragment = findFragment(MainFragment.class);
        if (firstFragment == null) {
            mFragments[FIRST] = MainFragment.newInstance();
            mFragments[SECOND] = MainFragmentTwo.newInstance();
            mFragments[THIRD] = MainFragmentThree.newInstance();
            mFragments[FOURTH] = MainFragmentFour.newInstance();

            loadMultipleRootFragment(R.id.fl_container, FIRST,
                    mFragments[FIRST],
                    mFragments[SECOND],
                    mFragments[THIRD],
                    mFragments[FOURTH]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题

            // 这里我们需要拿到mFragments的引用
            mFragments[FIRST] = firstFragment;
            mFragments[SECOND] = findFragment(MainFragmentTwo.class);
            mFragments[THIRD] = findFragment(MainFragmentThree.class);
            mFragments[FOURTH] = findFragment(MainFragmentFour.class);
        }
        mBottomBar.addItem(new BottomBarTab(this, R.drawable.ic_message_white_24dp))
                .addItem(new BottomBarTab(this, R.drawable.ic_message_white_24dp))
                .addItem(new BottomBarTab(this, R.drawable.ic_message_white_24dp))
                .addItem(new BottomBarTab(this, R.drawable.ic_message_white_24dp));

        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                showHideFragment(mFragments[position], mFragments[prePosition]);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                final SupportFragment currentFragment = mFragments[position];
                int count = currentFragment.getChildFragmentManager().getBackStackEntryCount();

                // 如果不在该类别Fragment的主页,则回到主页;
                if (count > 1) {
                    if (currentFragment instanceof MainFragment) {
                        currentFragment.popToChild(HomeFirstFragment.class, false);
                    } else if (currentFragment instanceof MainFragmentTwo) {
                        currentFragment.popToChild(HomeTwoFragment.class, false);
                    } else if (currentFragment instanceof MainFragmentThree) {
                        currentFragment.popToChild(HomeThreeFragment.class, false);
                    } else if (currentFragment instanceof MainFragmentFour) {
                        currentFragment.popToChild(HomeFourFragment.class, false);
                    }
                    return;
                }


                // 这里推荐使用EventBus来实现 -> 解耦
                if (count == 1) {
                    // 在FirstPagerFragment中接收, 因为是嵌套的孙子Fragment 所以用EventBus比较方便
                    // 主要为了交互: 重选tab 如果列表不在顶部则移动到顶部,如果已经在顶部,则刷新
                    EventBusActivityScope.getDefault(MainActivity.this).post(new TabSelectedEvent(position));
                }
            }
        });
    }

    @Override
    public void onBackPressedSupport() {
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressedSupport();
//        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                pop();
            } else {
                ActivityCompat.finishAfterTransition(this);
            }
        }
    }

    @Override
    public void onBackToFirstFragment() {
        mBottomBar.setCurrentItem(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
