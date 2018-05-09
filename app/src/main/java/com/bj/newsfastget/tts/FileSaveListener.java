package com.bj.newsfastget.tts;

import android.os.Handler;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.bj.newsfastget.AppConfig;
import com.bj.newsfastget.fragment.HomeTwoFragment;
import com.bj.newsfastget.simple.EventComm;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 保存回调音频流到文件。您也可以直接处理音频流
 * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
 * Created by fujiayi on 2017/9/15.
 */

public class FileSaveListener extends UiMessageListener {

    /**
     * 保存的文件名 baseName + utteranceId， 通常是 output-0.pcm
     */
    private String baseName = "output-";

    /**
     * 保存文件的目录
     */
    private String destDir;

    /**
     * 文件
     */
    private File ttsFile;


    /**
     * ttsFile 文件流
     */

    /**
     * ttsFile 文件buffer流
     */
    private BufferedOutputStream ttsFileBufferedOutputStream;

    private static final String TAG = "FileSaveListener";


    public FileSaveListener( String destDir) {
        super();
        this.destDir = destDir;
    }

    @Override
    public void onSynthesizeStart(String utteranceId) {
        if (utteranceId.startsWith("no"))
            return;
        String[] data=utteranceId.split("-");
        final String time=data[0];
        int index=Integer.parseInt(data[1]);
        final int count=Integer.parseInt(data[2]);
        if (index==1) {
            String filename = baseName + utteranceId + ".pcm";
            // 保存的语音文件是 16K采样率 16bits编码 单声道 pcm文件。
            ttsFile = new File(destDir, filename);
            Log.i(TAG, "try to write audio file to " + ttsFile.getAbsolutePath());
            try {
                if (ttsFile.exists()) {
                    ttsFile.delete();
                }
                ttsFile.createNewFile();
                // 创建FileOutputStream对象
                FileOutputStream ttsFileOutputStream = new FileOutputStream(ttsFile);
                // 创建BufferedOutputStream对象
                ttsFileBufferedOutputStream = new BufferedOutputStream(ttsFileOutputStream);
            } catch (IOException e) {
                // 请自行做错误处理
                e.printStackTrace();
                sendMessage("创建文件失败:" + destDir + "/" + filename);
                throw new RuntimeException(e);
            }
            sendMessage("创建文件成功:" + destDir + "/" + filename);
        }
    }

    /**
     * 语音流 16K采样率 16bits编码 单声道 。
     *
     * @param utteranceId
     * @param data        二进制语音 ，注意可能有空data的情况，可以忽略
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法保证和合成到第几个字对应。
     */
    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] data, int progress) {
        super.onSynthesizeDataArrived(utteranceId, data, progress);
        if (utteranceId.startsWith("no"))
            return;
        Log.i(TAG, "合成进度回调, progress：" + progress + ";序列号:" + utteranceId);
        try {
            ttsFileBufferedOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSynthesizeFinish(String utteranceId) {
        super.onSynthesizeFinish(utteranceId);
        if (utteranceId.startsWith("no"))
            return;
        close(utteranceId);
    }

    /**
     * 当合成或者播放过程中出错时回调此接口
     *
     * @param utteranceId
     * @param speechError 包含错误码和错误信息
     */
    @Override
    public void onError(String utteranceId, SpeechError speechError) {
        if (utteranceId.startsWith("no"))
            return;
        close(utteranceId);
        super.onError(utteranceId, speechError);
    }

    /**
     * 关闭流，注意可能stop导致该方法没有被调用
     */
    private void close(String utteranceId) {
        String[] data = utteranceId.split("-");
        final String time = data[0];
        int index = Integer.parseInt(data[1]);
        final int count = Integer.parseInt(data[2]);
        if (count == index) {
            if (ttsFileBufferedOutputStream != null) {
                try {
                    ttsFileBufferedOutputStream.flush();
                    ttsFileBufferedOutputStream.close();
                    ttsFileBufferedOutputStream = null;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            sendMessage("关闭文件成功");
            String filename = baseName + time+"-"+1+"-"+count+".pcm";
            String filename1 = baseName + time+"-"+1+"-"+count+ ".wav";
            ConvertPCMtoMP3 cpm = new ConvertPCMtoMP3();
            try {
                //pcm转换MP3
                cpm.convertAudioFiles(destDir + "/" + filename, destDir + "/" + filename1);
                //ChangePcmToWav.convertAudioFiles(destDir+"/"+filename,destDir+"/"+filename1);
                sendMessage("转换文件成功,开始转换视频");
//            String test = AppConfig.getInstance().getAPP_PATH_ROOT() + "/music/test.mp3";
                EventBus.getDefault().post(new EventComm(100, destDir + "/" + filename1, utteranceId));
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage("转换文件失败");
            }
        }
    }
}
