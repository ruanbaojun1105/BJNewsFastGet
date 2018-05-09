package com.bj.newsfastget.youtube.handler;

import android.os.Handler;
import android.os.Message;

import com.bj.newsfastget.youtube.activity.UploadVideoActivity;

public class FetchTokenHandler extends Handler {

	private UploadVideoActivity activity;

	public FetchTokenHandler(UploadVideoActivity activity) {
		this.activity = activity;
	}

	public void handleMessage(Message msg) {
		if (msg.what == HandlerMessage.YOUTUBE_TOKEN_FETCHED) {
			activity.uploadYouTubeVideo();
		}
	}
}
