package com.bj.newsfastget.bean;

import java.util.Date;
import java.util.List;

import io.realm.RealmObject;

/**
 * com.bj.newsfastget.bean
 *
 * @author Created by Ruan baojun on 11:59.2018/5/9.
 * @email 401763159@qq.com
 * @text
 */
public class ReadDetail extends RealmObject{
    public boolean isUploadVideo;//是否已上传完成视频
    public boolean isSynthesisVideo;//是否已合成视频
    public String content_id;
    public String hp_title;
    public String sub_title;
    public String hp_author;
    public String auth_it;
    public String hp_author_introduce;
    public String hp_content;
    public Date hp_makettime;
    public String hide_flag;
    public String wb_name;
    public String wb_img_url;
    public Date last_update_date;
    public String web_url;
    public String guide_word;
    public String audio;
    public String anchor;
    public String editor_email;
    public String top_media_type;
    public String top_media_file;
    public String top_media_image;
    public String start_video;
    public String copyright;
    public String audio_duration;
    public String cover;
    //        public List<Author> author;
    public Date maketime;
    //        public List<Author_list> author_list;
    public String next_id;
    public String previous_id;
    //        public List<Tag_list> tag_list;
//        public Share_list share_list;
    public int praisenum;
    public int sharenum;
    public int commentnum;
}
