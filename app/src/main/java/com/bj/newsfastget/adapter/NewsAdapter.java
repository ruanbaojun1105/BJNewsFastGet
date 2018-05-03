package com.bj.newsfastget.adapter;

import android.content.Context;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bj.newsfastget.R;
import com.bj.newsfastget.bean.news.BaseEntity;
import com.bj.newsfastget.bean.news.NewsItemEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xieye on 2017/4/10.
 */

public class NewsAdapter extends BaseQuickAdapter<NewsItemEntity, BaseViewHolder> {
    public final static int VIEW_TYPE_NEWS = 0;
    public final static int VIEW_TYPE_VIDEO = 1;
    public final static int VIEW_TYPE_IMG = 2;
    public final static int VIEW_TYPE_IMGS = 3;
    public final static int VIEW_TYPE_FAQ = 4;
    public final static int VIEW_TYPE_GROUP = 5;


    public NewsAdapter() {
        super(new ArrayList<NewsItemEntity>());
        //Step.1
        setMultiTypeDelegate(new MultiTypeDelegate<NewsItemEntity>() {
            @Override
            protected int getItemType(NewsItemEntity entity) {
                //根据你的实体类来判断布局类型
                if (entity != null) {
                    if (entity.isHasVideo()) {
                        return VIEW_TYPE_VIDEO;
                    }
                    if (entity.getImgs() != null && entity.getImgs().size() > 0) {
                        //多图模式
                        return VIEW_TYPE_IMGS;
                    }
                    if (!TextUtils.isEmpty(entity.getImg())) {
                        return VIEW_TYPE_IMG;
                    }
                    return VIEW_TYPE_NEWS;
                }
                return VIEW_TYPE_IMG;
            }
        });
        //Step.2
        getMultiTypeDelegate()
                .registerItemType(VIEW_TYPE_NEWS, R.layout.item_news_text)
                .registerItemType(VIEW_TYPE_VIDEO, R.layout.item_news_text_video)
                .registerItemType(VIEW_TYPE_IMG, R.layout.item_news_text_img)
                .registerItemType(VIEW_TYPE_IMGS, R.layout.item_news_text_imgs)
                .registerItemType(VIEW_TYPE_FAQ, R.layout.item_news_faq)
                .registerItemType(VIEW_TYPE_GROUP, R.layout.item_news_text_img);
    }

    @Override
    protected void convert(BaseViewHolder helper, NewsItemEntity itemEntity) {
        switch (helper.getItemViewType()) {
            case VIEW_TYPE_IMG:
                bindNewsImg(new NewsImgHolder(helper.itemView), itemEntity);
                break;
            case VIEW_TYPE_IMGS:
                bindNewsImgs(new NewsImgsHolder(helper.itemView), itemEntity);
                break;
            case VIEW_TYPE_FAQ:
                break;
            case VIEW_TYPE_GROUP:
                break;
            case VIEW_TYPE_VIDEO:
                bindNewsVideo(new VideoHolder(helper.itemView), itemEntity);
                break;
            default:
            case VIEW_TYPE_NEWS:
                bindNewsText(new NewsHolder(helper.itemView), itemEntity);
                break;
        }
    }


    /**
     * 有视频
     *
     * @param holder
     * @param itemEntity
     */
    private void bindNewsVideo(VideoHolder holder, final NewsItemEntity itemEntity) {
        if (itemEntity == null) {
            return;
        }
        String img = itemEntity.getImg();
        if (!TextUtils.isEmpty(img)) {
            if (!img.startsWith("http:")) {
                img = new StringBuilder("http:").append(img).toString();
            }
            Glide.with(mContext).load(img).into(holder.ivRight);
        }
        holder.tvTitle.setText(itemEntity.getTitle());
        if (!TextUtils.isEmpty(itemEntity.getCommentCount()) && !itemEntity.getCommentCount().equals("0")) {
            holder.tvBrowse.setText(itemEntity.getCommentCount() + "人看过");
        }

        if (!TextUtils.isEmpty(itemEntity.getTimeStr())) {
            holder.tvTime.setText(itemEntity.getTimeStr());
        }

        holder.tvDuration.setText(itemEntity.getVideoDurationStr());
        holder.tvAuthor.setText(itemEntity.getAuthor());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onNewsClick != null) {
                    onNewsClick.OnItemClick(itemEntity);
                }
            }
        });
    }

    /**
     * 有多张图
     *
     * @param holder
     * @param itemEntity
     */
    private void bindNewsImgs(NewsImgsHolder holder, final NewsItemEntity itemEntity) {
        if (itemEntity == null) {
            return;
        }
        List<String> imgs = itemEntity.getImgs();
        if (imgs != null && imgs.size() != 0) {
            int index = 0;
            for (String img : imgs) {
                if (!img.startsWith("http:")) {
                    img = new StringBuilder("http:").append(img).toString();
                }
                if (index == 0) {
                    Glide.with(mContext).load(img).into(holder.ivCenter1);
                } else if (index == 1) {
                    Glide.with(mContext).load(img).into(holder.ivCenter2);
                } else if (index == 2) {
                    Glide.with(mContext).load(img).into(holder.ivCenter3);
                }
                index++;
            }
        }
        if (!TextUtils.isEmpty(itemEntity.getCommentCount()) && !itemEntity.getCommentCount().equals("0")) {
            holder.tvBrowse.setText(itemEntity.getCommentCount() + "人看过");
        }

        if (!TextUtils.isEmpty(itemEntity.getTimeStr())) {
            holder.tvTime.setText(itemEntity.getTimeStr());
        }
        holder.tvTitle.setText(itemEntity.getTitle());
        holder.tvAuthor.setText(itemEntity.getAuthor());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onNewsClick != null) {
                    onNewsClick.OnItemClick(itemEntity);
                }
            }
        });
    }

    /**
     * 有一张图
     *
     * @param holder
     * @param itemEntity
     */
    private void bindNewsImg(NewsImgHolder holder, final NewsItemEntity itemEntity) {
        if (itemEntity == null) {
            return;
        }
        String img = itemEntity.getImg();
        if (!TextUtils.isEmpty(img)) {
            if (!img.startsWith("http:")) {
                img = new StringBuilder("http:").append(img).toString();
            }
            Glide.with(mContext).load(img).into(holder.ivRight);
        }
        if (!TextUtils.isEmpty(itemEntity.getCommentCount()) && !itemEntity.getCommentCount().equals("0")) {
            holder.tvBrowse.setText(itemEntity.getCommentCount() + "人看过");
        }

        if (!TextUtils.isEmpty(itemEntity.getTimeStr())) {
            holder.tvTime.setText(itemEntity.getTimeStr());
        }
        holder.tvTitle.setText(itemEntity.getTitle());
        holder.tvAuthor.setText(itemEntity.getAuthor());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onNewsClick != null) {
                    onNewsClick.OnItemClick(itemEntity);
                }
            }
        });
    }

    /**
     * 没有图
     *
     * @param holder
     * @param itemEntity
     */
    private void bindNewsText(NewsHolder holder, final NewsItemEntity itemEntity) {
        if (itemEntity == null) {
            return;
        }
        if (!TextUtils.isEmpty(itemEntity.getCommentCount()) && !itemEntity.getCommentCount().equals("0")) {
            holder.tvBrowse.setText(itemEntity.getCommentCount() + "人看过");
        }

        if (!TextUtils.isEmpty(itemEntity.getTimeStr())) {
            holder.tvTime.setText(itemEntity.getTimeStr());
        }
        holder.tvTitle.setText(itemEntity.getTitle());
        holder.tvAuthor.setText(itemEntity.getAuthor());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onNewsClick != null) {
                    onNewsClick.OnItemClick(itemEntity);
                }
            }
        });
    }


    static class NewsHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_author)
        TextView tvAuthor;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_browse)
        TextView tvBrowse;

        NewsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class NewsImgHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_right)
        ImageView ivRight;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_author)
        TextView tvAuthor;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_browse)
        TextView tvBrowse;

        NewsImgHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    static class NewsImgsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_center1)
        ImageView ivCenter1;
        @BindView(R.id.iv_center2)
        ImageView ivCenter2;
        @BindView(R.id.iv_center3)
        ImageView ivCenter3;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_author)
        TextView tvAuthor;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_browse)
        TextView tvBrowse;


        NewsImgsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    static class VideoHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_right)
        ImageView ivRight;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_author)
        TextView tvAuthor;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_browse)
        TextView tvBrowse;
        @BindView(R.id.tv_duration)
        TextView tvDuration;

        VideoHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    static class FaqHold extends RecyclerView.ViewHolder {

        FaqHold(View itemView) {
            super(itemView);
        }
    }

    private OnNewsClick onNewsClick;

    public void setOnNewsClick(OnNewsClick onNewsClick) {
        this.onNewsClick = onNewsClick;
    }

    public interface OnNewsClick {
        void OnItemClick(NewsItemEntity entity);
    }
}
