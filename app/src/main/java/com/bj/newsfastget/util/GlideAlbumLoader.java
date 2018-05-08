/*
 * Copyright © Yan Zhenjie. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bj.newsfastget.util;

import android.webkit.URLUtil;
import android.widget.ImageView;

import com.bj.newsfastget.simple.GlideApp;
import com.bumptech.glide.Glide;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;
import com.yanzhenjie.album.task.DefaultAlbumLoader;

import java.io.File;

/**
 * Created by Yan Zhenjie on 2017/3/31.
 */
public class GlideAlbumLoader implements AlbumLoader {

    @Override
    public void loadAlbumFile(ImageView imageView, AlbumFile albumFile, int viewWidth, int viewHeight) {
        int mediaType = albumFile.getMediaType();
        if (mediaType == AlbumFile.TYPE_IMAGE) {
            GlideApp.with(imageView.getContext())
                    .load(albumFile.getPath())
                    .skipMemoryCache(true)
                    .into(imageView);
        } else if (mediaType == AlbumFile.TYPE_VIDEO) {
            GlideApp.with(imageView.getContext())
                    .load(albumFile.getPath())
                    .skipMemoryCache(true)
                    .into(imageView);
//            DefaultAlbumLoader.getInstance().loadAlbumFile(imageView, albumFile, viewWidth, viewHeight);
        }
    }

    @Override
    public void loadImage(ImageView imageView, String imagePath, int width, int height) {
        if (URLUtil.isNetworkUrl(imagePath)) {
            GlideApp.with(imageView.getContext())
                    .load(imagePath)
                    .skipMemoryCache(true)
                    .into(imageView);
        } else {
            GlideApp.with(imageView.getContext())
                    .load(new File(imagePath))
                    .skipMemoryCache(true)
                    .into(imageView);
        }
    }

}