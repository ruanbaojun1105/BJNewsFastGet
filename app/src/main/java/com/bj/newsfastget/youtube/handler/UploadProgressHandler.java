package com.bj.newsfastget.youtube.handler;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.bj.newsfastget.R;
import com.bj.newsfastget.youtube.activity.UploadVideoActivity;
import com.bj.newsfastget.youtube.task.YouTubeUploadTask;
import com.bj.newsfastget.youtube.util.DialogUtil;
import com.bj.newsfastget.youtube.util.SharedPreferenceUtil;
import com.google.api.services.youtube.model.Video;

import java.io.IOException;

public class UploadProgressHandler extends Handler {

	private UploadVideoActivity activity = null;

	private ProgressDialog progressDialog = null;
	private YouTubeUploadTask youtubeUploadTask = null;

	public UploadProgressHandler(UploadVideoActivity activity) {
		this.activity = activity;
	}

	public void handleMessage(Message msg) {
		super.handleMessage(msg);

		switch (msg.what) {
			case HandlerMessage.VIDEO_UPLOAD_START:
				String selectedGoogleAccount = SharedPreferenceUtil.getPreferenceItemByName(activity, SharedPreferenceUtil.selectedGoogleAccount);
				if(selectedGoogleAccount.isEmpty()) {
					activity.chooseAccount();
				}
				else {
					activity.setSelectedGoogleAccount(selectedGoogleAccount);
					activity.uploadYouTubeVideo();
				}
				break;
			case HandlerMessage.VIDEO_UPLOAD_INITIATION_STARTED:
				progressDialog = DialogUtil.showWaitingProgressDialog(activity, ProgressDialog.STYLE_SPINNER, activity.getString(R.string.uploadingVideo), false);
				break;
			case HandlerMessage.VIDEO_UPLOAD_PROGRESS_UPDATE:
				if(youtubeUploadTask != null) {
					try {
						int progress = (int)(youtubeUploadTask.getUploader().getProgress() * 100);
						if(progress < 10) {
							activity.getTextViewProgress().setText(" 0" + progress + "%");
						}
						else if(progress < 100) {
							activity.getTextViewProgress().setText(" " + progress + "%");
						}
						else {
							activity.getTextViewProgress().setText(progress + "%");
						}
						activity.getProgressBarUploadVideo().setProgress(progress);
					}
					catch (IOException e) {

					}
				}
				break;
			case HandlerMessage.VIDEO_UPLOAD_COMPLETED:
				progressDialog.dismiss();
				Toast.makeText(activity, R.string.videoUploadCompleted, Toast.LENGTH_LONG).show();
				if (youtubeUploadTask != null) {
					Video youtubeVideo = youtubeUploadTask.getUploadedVideo();
					activity.getTextViewVideoUrl().setText("https://www.youtube.com/watch?v=" + youtubeVideo.getId());
					activity.preventUploadingSameVideo();
				}
				break;
			case HandlerMessage.VIDEO_UPLOAD_FAILED:
				if(progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				DialogUtil.showExceptionAlertDialog(activity, "error", (String)(msg.obj));
				Toast.makeText(activity, R.string.videoUploadFailed+(String)(msg.obj), Toast.LENGTH_LONG).show();
				break;
			default:
				break;
		}
	}

	public void setTask(YouTubeUploadTask task) {
		this.youtubeUploadTask = task;
	}
}
