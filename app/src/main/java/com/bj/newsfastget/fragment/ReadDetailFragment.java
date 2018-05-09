package com.bj.newsfastget.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.activity.MainActivity;
import com.bj.newsfastget.bean.ReadRootBean;
import com.bj.newsfastget.simple.EventComm;
import com.bj.newsfastget.simple.GlideApp;
import com.bj.newsfastget.simple.SwipeSimpleFragment;
import com.bj.newsfastget.util.EntityRequest;
import com.bj.newsfastget.util.HttpListener;
import com.bj.newsfastget.util.HttpUtil;
import com.bj.newsfastget.util.Result;
import com.bj.newsfastget.view.CDView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.bj.newsfastget.fragment.HomeTwoFragment.newSyntWork;
import static com.bj.newsfastget.fragment.HomeTwoFragment.pause;
import static com.bj.newsfastget.fragment.HomeTwoFragment.resume;
import static com.bj.newsfastget.fragment.HomeTwoFragment.stop;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:43.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class ReadDetailFragment extends SwipeSimpleFragment {
    @BindView(R.id.cdv_music)
    CDView cdvMusic;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.scroll)
    ScrollView scroll;
    @BindView(R.id.audio_play)
    Button audioPlay;
    @BindView(R.id.audio_state)
    Button audioState;

    public static ReadDetailFragment newInstance(String item_id, String image) {
        Bundle args = new Bundle();
        args.putString("item_id", item_id);
        args.putString("image", image);
        ReadDetailFragment fragment = new ReadDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_read_detail;
    }

    int state=1;
    @Override
    protected void initCreateView() {
        String item_id = getArguments().getString("item_id");
        final String image = getArguments().getString("image");
        String taget = "http://v3.wufazhuce.com:8000/api/essay/" + item_id + "?channel=wdj&source=channel_reading&source_id=9264&version=4.0.2&uuid=ffffffff-a90e-706a-63f7-ccf973aae5ee&platform=android";
        EntityRequest<ReadRootBean> request = new EntityRequest<>(taget, ReadRootBean.class);
        HttpUtil.postExecuteThead(request, new HttpListener() {
            @Override
            public void onSucceed(int what, Result t) {
                final ReadRootBean readDetail = (ReadRootBean) t.getResult();
                GlideApp.with(_mActivity).load(image).into(cdvMusic);
                cdvMusic.openAm();
                title.setText(readDetail.data.hp_title + "\n作者:" + readDetail.data.hp_author);
                final String text = readDetail.data.hp_content
                        .replaceAll("\\{[^}]*\\}","")
                        .replace("<p>", "\n")
                        .replace("</p>", "")
                        .replace("<br>", "");
                content.setText(text);
                audioPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newSyntWork(((MainActivity) _mActivity).getSynthesizer(),
                                readDetail.data.hp_title + "\n作者:" + readDetail.data.hp_author + "\n" + text,
                                String.valueOf(System.currentTimeMillis()), false);
                    }
                });
                audioState.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (state==1) {
                            pause(((MainActivity) _mActivity).getSynthesizer());
                            state=2;
                        }else {
                            resume(((MainActivity) _mActivity).getSynthesizer());
                            state=1;
                        }
                    }
                });
            }

            @Override
            public void onFailed(int what, Exception e) {
            }

            @Override
            public void onFinish(int what) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final EventComm event) {
        if ("RecyclerToTop".equals(event.getType())) {
            scroll.scrollTo(0, 0);
        }
    }

}
