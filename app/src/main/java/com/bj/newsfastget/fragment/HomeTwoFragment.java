package com.bj.newsfastget.fragment;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.bj.newsfastget.App;
import com.bj.newsfastget.AppConfig;
import com.bj.newsfastget.BuildConfig;
import com.bj.newsfastget.R;
import com.bj.newsfastget.activity.MainActivity;
import com.bj.newsfastget.adapter.InfoAdapter;
import com.bj.newsfastget.simple.EventComm;
import com.bj.newsfastget.simple.SwipeSimpleFragment;
import com.bj.newsfastget.tts.AutoCheck;
import com.bj.newsfastget.tts.FileSaveListener;
import com.bj.newsfastget.tts.InitConfig;
import com.bj.newsfastget.tts.MainHandlerConstant;
import com.bj.newsfastget.tts.MySyntherizer;
import com.bj.newsfastget.tts.NonBlockSyntherizer;
import com.bj.newsfastget.tts.OnCallBack;
import com.bj.newsfastget.tts.UiMessageListener;
import com.bj.newsfastget.util.FileUtil;
import com.bj.newsfastget.util.OfflineResource;
import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import VideoHandle.EpDraw;
import VideoHandle.EpEditor;
import VideoHandle.EpText;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;
import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * com.bj.newsfastget.fragment
 *
 * @author Created by Ruan baojun on 11:43.2018/4/28.
 * @email 401763159@qq.com
 * @text
 */
public class HomeTwoFragment extends SwipeSimpleFragment implements MainHandlerConstant {

    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.button1)
    Button button1;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button3)
    Button button3;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    private InfoAdapter itemAdapter;

    // ================== 初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected String appId = "11185341";

    protected String appKey = "2mNX46q1lPIzhI187hnW9Y5W";

    protected String secretKey = "2KQczRlkqYGywuRoU3xCeWtA8DwkOCdf";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;

    protected static String DESC = "请先看完说明。之后点击“合成并播放”按钮即可正常测试。\n"
            + "测试离线合成功能需要首次联网。\n"
            + "纯在线请修改代码里ttsMode为TtsMode.ONLINE， 没有纯离线。\n"
            + "本Demo的默认参数设置为wifi情况下在线合成, 其它网络（包括4G）使用离线合成。 在线普通女声发音，离线男声发音.\n"
            + "合成可以多次调用，SDK内部有缓存队列，会依次完成。\n\n";

    private static final String TAG = "HomeTwoFragment";

    public static HomeTwoFragment newInstance() {
        Bundle args = new Bundle();
        HomeTwoFragment fragment = new HomeTwoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home2;
    }

    @Override
    protected void initCreateView() {
        text.setText(getClass().getName());
        itemAdapter = new InfoAdapter();
        itemAdapter.openLoadAnimation(new AlphaInAnimation());
        itemAdapter.setEnableLoadMore(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);
        recycler.setAdapter(itemAdapter);
        checkPer();
    }

    @AfterPermissionGranted(100)
    private void checkPer() {
        String[] perms = {
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
        if (EasyPermissions.hasPermissions(_mActivity, perms)) {
            String itemPath = AppConfig.getInstance().getAPP_PATH_ROOT() + "/images";
            FileUtil.copyFilesFassets(_mActivity, "images", itemPath);

            String itemPath1 = AppConfig.getInstance().getAPP_PATH_ROOT() + "/music";
            FileUtil.copyFilesFassets(_mActivity, "music", itemPath1);

            String itemPath2 = AppConfig.getInstance().getAPP_PATH_ROOT() + "/video";
            FileUtil.copyFilesFassets(_mActivity, "video", itemPath2);

            String itemPath3 = AppConfig.getInstance().getAPP_PATH_ROOT() + "/ttf";
            FileUtil.copyFilesFassets(_mActivity, "ttf", itemPath3);

            itemAdapter.addData(0, "图片和音视频转存完毕");
            initialTtsFile(); // 初始化TTS引擎
            loadFFMpegBinary();
        } else {
            EasyPermissions.requestPermissions(_mActivity,
                    "请不要拒绝我们善意的权限请求,谢谢.\n\n需要赋予以下权限才能保证程序的正常运行:\n\n1.SD卡写入与读取", 100, perms);
        }
    }


    @OnClick({R.id.button1, R.id.button2, R.id.button3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button1:
                break;
            case R.id.button2:
                speak("请不要拒绝我们善意的权限请求,谢谢.需要赋予以下权限才能保证程序的正常运行:1.SD卡写入与读取");
                break;
            case R.id.button3:
//                speak("");
//                String text="众所周知美国作为世界第一大军事强国111,众所周知美国作为世界第一大军事强国222,众所周知美国作为世界第一大军事强国333";
                String text="众所周知，美国作为世界第一大军事强国，凭借强大的战斗力在100多个国家拥有自己的大使馆，每一个大使馆内部都有一批武装实弹的美国大兵，充当大使馆的保护神，一旦遇到危险的时候，他们就会第一时间进行出击！"+
                        "然而有一个国家内的美国大使馆士兵却没有枪，那就是中国！曾经驻华的美国士兵也是统一配置先进作战武器，但是因为一件事情的发生，导致到目前为止都禁止携带武器枪支，否则将会遭受到解放军的强势驱逐。究竟怎么回事？" +
                        "阮宝军也不知道啊去你妈的众所周知美国作为世界第一大军事强国111,众所周知美国作为世界第一大军事强国222,众所周知美国作为世界第一大军事强国333";
                newSyntWork(text,String.valueOf(System.currentTimeMillis()));
//                batchSpeak();
                break;
        }
    }

    private void newSyntWork(final String text, final String time) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //                synthesize("");//合成
                char[] arr=text.toCharArray();
                List<Pair<String, String>> texts = new ArrayList<Pair<String, String>>();
                StringBuilder builder=new StringBuilder(0);
                int pp=1;
                int len=(arr.length/150)+(arr.length%150>0?1:0);
                for (int i = 1; i <=arr.length; i++) {
                    builder.append(arr[i-1]);
                    if (i%150==0){
                        texts.add(new Pair<String, String>(builder.toString(), time+"-"+pp+"-"+len));
                        builder.setLength(0);
                        pp++;
                    }
                }
                if (builder.length()>0){
                    texts.add(new Pair<String, String>(builder.toString(),  time+"-"+pp+"-"+len));
                    builder.setLength(0);
                }
                int result = synthesizer.batchSpeak(texts);
                checkResult(result, "batchSpeak");
            }
        }).start();
    }

    protected void toPrint(final String str) {
        LogUtils.e("topring---" + str);
        recycler.post(new Runnable() {
            @Override
            public void run() {
                itemAdapter.addData(0, "topring---" + str);
                recycler.scrollToPosition(0);
            }
        });
    }

    private void loadFFMpegBinary() {
        try {
            FFmpeg ffmpeg = FFmpeg.getInstance(_mActivity);
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    itemAdapter.addData(0, "onfail load ff");
                }

                @Override
                public void onSuccess() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            itemAdapter.addData(0, e.toString());
        }
    }

    private void execFFmpegBinary(final String[] command, final OnCallBack callBack) {
        try {
            FFmpeg ffmpeg = FFmpeg.getInstance(_mActivity);
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(final String s) {
                    recycler.post(new Runnable() {
                        @Override
                        public void run() {
                            itemAdapter.addData(0, "comm:"+command+"\n---FAILED with output : " + s);
                            recycler.scrollToPosition(0);
                        }
                    });
                    LogUtils.e("FAILED with output : " + s);
                    if (callBack != null)
                        callBack.onFail();
                }

                @Override
                public void onSuccess(final String s) {
                    recycler.post(new Runnable() {
                        @Override
                        public void run() {
                            itemAdapter.addData(0, "SUCCESS with output : " + s);
                            recycler.scrollToPosition(0);
                        }
                    });
                    if (callBack != null)
                        callBack.onSucc();
                }

                @Override
                public void onProgress(final String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command.toString());
                    recycler.post(new Runnable() {
                        @Override
                        public void run() {
                            itemAdapter.addData(0, "progress : " + s);
                            recycler.scrollToPosition(0);
                        }
                    });
                }

                @Override
                public void onStart() {
                    recycler.post(new Runnable() {
                        @Override
                        public void run() {
                            itemAdapter.addData(0, "Started command : ffmpeg " + command.toString());
                            recycler.scrollToPosition(0);
                        }
                    });
                }

                @Override
                public void onFinish() {
                    recycler.post(new Runnable() {
                        @Override
                        public void run() {
                            itemAdapter.addData(0, "Finished command : ffmpeg " + command.toString());
                            recycler.scrollToPosition(0);
                        }
                    });
                }
            });
        } catch (final FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
            recycler.post(new Runnable() {
                @Override
                public void run() {
                    itemAdapter.addData(0, "FFmpegCommandAlreadyRunningException : " + e.toString());
                    recycler.scrollToPosition(0);
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final EventComm event) {
        if (event.getCode() == 100) {
            String[] data=event.getObject2().toString().split("-");
            final String time=data[0];
//            String index=data[1];
            final int count=Integer.parseInt(data[2]);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                       //AppConfig.getInstance().getAPP_PATH_ROOT()+"/baiduTTS/output-"+i+"-"+count+".wav"
                        /*String wavListFile =AppConfig.getInstance().getAPP_PATH_ROOT() + "/wav" + time + ".txt";
                        StringBuilder builder=new StringBuilder(0);
                        for (int i = 1; i <=count ; i++) {
                            //混合拼接
                            //builder.append("-i "+AppConfig.getInstance().getAPP_PATH_ROOT()+"/baiduTTS/output-"+time+"-"+i+"-"+count+".wav ");
                            builder.append(AppConfig.getInstance().getAPP_PATH_ROOT()+"/baiduTTS/output-"+time+"-"+i+"-"+count+".wav|");
                        }
                        FileUtil.contentToTxt(wavListFile, builder.toString());*/
                        final String ttfPath = AppConfig.getInstance().getAPP_PATH_ROOT() + "/ttf/youyuan.ttf";
                        String zimu=synthesizer.getNewText();
                        String[] ppp=zimu.split(";|,|\\?|!|，|。|？|！");
                        List<EpText> epTextList=new ArrayList<>();
                        for (int i = 0; i <ppp.length ; i++) {
                            EpText text=new EpText(10,290,20, EpText.Color.DarkBlue,ttfPath,ppp[i],new EpText.Time(i*3,i*3+2));
                            epTextList.add(text);
                        }
                        toChangeVideo("image",(String) event.getObject(),toCreatSrt(time),time,epTextList);
                    }
                }).start();
        }
        if ("TabSelectedEvent".equals(event.getType())){
            int po= (int) event.getObject();
            if (po== MainActivity.SECOND)
                return;
        }
        if ("RecyclerToTop".equals(event.getType())){
            recycler.scrollToPosition(0);
        }
    }

    /**
     * 自动生成字幕
     * @param time
     * @return
     */
    private String toCreatSrt(String time) {
        String srtFile = AppConfig.getInstance().getAPP_PATH_ROOT() + "/srt" + time + ".srt";
        String zimu=synthesizer.getNewText();
        String[] ppp=zimu.split(";|,|\\?|!|，|。|？|！");
        StringBuilder builder=new StringBuilder(0);
        for (int i = 0; i < ppp.length; i++) {
            builder.append((i+1)+"\n");
            builder.append(getStartTime(i*3));
            builder.append(" --> ");
            builder.append(getEndTime(i*3+3));
            builder.append("\n");
            builder.append(ppp[i]);
            builder.append("\n");
            builder.append("\n");
        }
        FileUtil.contentToTxt(srtFile,builder.toString());
        return srtFile;
    }

    private String getStartTime(int tempTime) {
        if(tempTime < 60){
            return "00:00:"+String.format("%02d",tempTime)+",100";
        }else if(tempTime > 60 && tempTime < 60*60){
            String time = String.format("%02d",tempTime/60);
            String time2 = String.format("%02d",tempTime%60);
            return "00:"+time+":"+time2+",100";
        }else if(tempTime >60*60 && tempTime < 60*60*24){
            String time = String.format("%02d",tempTime/(60*60));
            int time2 = tempTime%(60*60);
            String time11 = String.format("%02d",time2/60);
            String time22 = String.format("%02d",time2%60);
            return time+":"+time11+":"+time22+",100";
        }else {
            return "00:00:00,100";
        }
    }

    private String getEndTime(int tempTime) {
        if(tempTime < 60){
            return "00:00:"+String.format("%02d",tempTime)+",200";
        }else if(tempTime > 60 && tempTime < 60*60){
            String time = String.format("%02d",tempTime/60);
            String time2 = String.format("%02d",tempTime%60);
            return "00:"+time+":"+time2+",100";
        }else if(tempTime >60*60 && tempTime < 60*60*24){
            String time = String.format("%02d",tempTime/(60*60));
            int time2 = tempTime%(60*60);
            String time11 = String.format("%02d",time2/60);
            String time22 = String.format("%02d",time2%60);
            return time+":"+time11+":"+time22+",100";
        }else {
            return "00:00:00,100";
        }
    }

    public void toChangeVideo(final String images, final String wavPathData, final String srtFile, final String time, final List<EpText> epTextList) {
        //ffmpeg -f image2 -i /home/ttwang/images/image%d.jpg  -vcodec libx264 -r 10  tt.mp4
        final File file = new File(AppConfig.getInstance().getAPP_PATH_ROOT() + "/video");
        if (!file.exists())
            file.mkdir();
        String cmd;
        final String logo = AppConfig.getInstance().getAPP_PATH_ROOT() + "/images/logo.png";
        final String videoPath = AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/" + time + ".mp4";
        final String newVideoPath1 = AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/1new-" + time + ".mp4";
        final String newVideoPath2 = AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/2new-" + time + ".mp4";
        final String newVideoPath3 = AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/3new-" + time + ".mp4";
        cmd = "-threads 8 -y -r 16 -i " + AppConfig.getInstance().getAPP_PATH_ROOT() +"/images/"+images+"%d.jpg " + "-vf zoompan=z=1.1:x='if(eq(x,0),100,x-1)':s='480*320' -t 65.0 -vcodec libx264 -r 10 " + videoPath;
//        if (TextUtils.isEmpty(wavPath))
//            cmd = "-f image2 -i " + AppConfig.getInstance().getAPP_PATH_ROOT() + "/images/image%d.jpg -vcodec libx264 -r 1 " +
//                    AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/" + System.currentTimeMillis() + ".mp4";
//        else
//            //-threads 8 -y -r 25 -i /Users/lishengqiang/Documents/temp/2.png -vf zoompan=z=1.1:x='if(eq(x,0),100,x-1)':s='540*960' -t 2.0 /Users/lishengqiang/Documents/temp/output/0.mp4
//            cmd = "-threads 2 -y -r 10 -i " + wavPath + " -f image2 -framerate 12 -i " + AppConfig.getInstance().getAPP_PATH_ROOT() + "/images/image%d.jpg -vcodec libx264 -r 1 " +
//                    AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/" + System.currentTimeMillis() + ".mp4";
        execCmd(cmd, new OnCallBack() {
            @Override
            public void onSucc() {
                execCmd("-i " + wavPathData + " -i " + videoPath + " -preset ultrafast -y " + newVideoPath1, new OnCallBack() {
                    @Override
                    public void onSucc() {
                        EpVideo epVideo=new EpVideo(newVideoPath1);
                        epVideo.addDraw(new EpDraw(logo,10,10,50,50,false));
                        for (EpText te:epTextList) {
                            epVideo.addText(te);
                        }
                        EpEditor.OutputOption outputOption = new EpEditor.OutputOption(newVideoPath2);
                        EpEditor.exec(epVideo, outputOption, new OnEditorListener() {
                            @Override
                            public void onSuccess() {
                                LogUtils.e("------------字幕和水印添加成功  onSuccess  -开始清理文件");
                                if (new File(wavPathData).exists())
                                    new File(wavPathData).delete();
                                if (new File(wavPathData.replace("wav","pcm")).exists())
                                    new File(wavPathData.replace("wav","pcm")).delete();
                                if (new File(srtFile).exists())
                                    new File(srtFile).delete();
                                if (new File(newVideoPath1).exists())
                                    new File(newVideoPath1).delete();
                                if (new File(videoPath).exists())
                                    new File(videoPath).delete();
                                if (new File(srtFile).exists())
                                    new File(srtFile).delete();
                                File[] listFiles=new File(AppConfig.getInstance().getAPP_PATH_ROOT() + "/images/").listFiles();
                                for (int i = 0; i < listFiles.length; i++) {
                                    File image = listFiles[i];
                                    if (image.exists()&&image.getName().startsWith(images))
                                        image.delete();
                                }
                                //mkv转mp4
                                //ffmpeg -i input.mkv -filter_complex [0:v][0:s]overlay[v] -map [v] -map 0:a output.mp4
                                recycler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        itemAdapter.addData(0, "------------字幕和水印添加成功  onSuccess");
                                        recycler.scrollToPosition(0);
                                    }
                                });
                                //添加字幕ffmpeg -i video.avi -vf subtitles=subtitle.srt out.avi
                                //ffmpeg -i input.mp4 -i input.srt -map 0:v -map 0:a -map 1:s -c:v copy -c:a copy -c:s mov_text -movflags +faststart output.mp4
                                //execCmd("-i "+newVideoPath2+" -i "+srtFile+" -map 0:v -map 0:a -map 1:s -c:v copy -c:a copy -c:s mov_text -movflags +faststart "+newVideoPath3, new OnCallBack() {
//                                    @Override
//                                    public void onSucc() {
//                                        recycler.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                itemAdapter.addData(0, "------------字幕添加成功");
//                                                recycler.scrollToPosition(0);
//                                            }
//                                        });
//                                    }
//
//                                    @Override
//                                    public void onFail() {
//                                        recycler.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                itemAdapter.addData(0, "------------字幕添加失败");
//                                                recycler.scrollToPosition(0);
//                                            }
//                                        });
//                                    }
//                                });
                            }

                            @Override
                            public void onFailure() {
                                recycler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        itemAdapter.addData(0, "------------newVideoPath  onFailure");
                                        recycler.scrollToPosition(0);
                                    }
                                });
                            }

                            @Override
                            public void onProgress(final float progress) {
                                recycler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        itemAdapter.addData(0, "------------progress  progress"+progress);
                                        recycler.scrollToPosition(0);
                                    }
                                });

                            }
                        });
                    }

                    @Override
                    public void onFail() {

                    }
                });
//                final String wavPath=AppConfig.getInstance().getAPP_PATH_ROOT() + "/baiduTTS/"+time+".wav";
                //混音
                //wavPathData+"-filter_complex amix=inputs="+count+":duration=first:dropout_transition="+count+" -f mp3 "+wavPath
                //拼接
                //-i "concat:123.mp3|124.mp3" -acodec copy output.mp3
//                execCmd("-i concat:"+wavPathData+" -acodec copy "+wavPath, new OnCallBack() {
//                    @Override
//                    public void onSucc() {
//                        //delete wav list
//                        toPrint("合并wav list 成功~~~~");
//                    }
//
//                    @Override
//                    public void onFail() {
//                        toPrint("合并wav list 失败~~~~");
//                    }
//                });
            }
            @Override
            public void onFail() {
            }
        });
//        EpEditor.execCmd(cmd, 0, new OnEditorListener() {
//            @Override
//            public void onSuccess() {
//                LogUtils.e("onSuccess");
//                //-f s16le -ar 44.1k -ac 2 -i file.pcm file.wav
//                EpEditor.execCmd("-f s16le -ar 44.1k -ac 2 -i "+wavPath+" "+wav2, 0, new OnEditorListener() {
//                    @Override
//                    public void onSuccess() {
//                        EpEditor.execCmd("-i "+wav2+" -i "+videoPath+" -preset ultrafast -y -max_muxing_queue_size 9999 "+newVideoPath
//                                , 0, new OnEditorListener() {
//                                    @Override
//                                    public void onSuccess() {
//                                        LogUtils.e("视频音频合成onSuccess");
//                                    }
//
//                                    @Override
//                                    public void onFailure() {
//                                        LogUtils.e("视频音频合成onFailure");
//                                    }
//
//                                    @Override
//                                    public void onProgress(float v) {
//
//                                    }
//                                });
//                    }
//
//                    @Override
//                    public void onFailure() {
//
//                    }
//
//                    @Override
//                    public void onProgress(float v) {
//
//                    }
//                });

        //        添加背景音乐
//参数分别是视频路径，音频路径，输出路径,原始视频音量(1为100%,0.7为70%,以此类推),添加音频音量
//                EpEditor.music(videoPath, wavPath, newVideoPath, 1, 1.0f, new OnEditorListener() {
//                    @Override
//                    public void onSuccess() {
//                        LogUtils.e("视频音频合成onSuccess");
//                    }
//
//                    @Override
//                    public void onFailure() {
//                        LogUtils.e("视频音频合成onFailure");
//                    }
//
//                    @Override
//                    public void onProgress(float progress) {
//                        //这里获取处理进度
//                    }
//                });
    }

//            @Override
//            public void onFailure() {
//                LogUtils.e("onFailure");
//            }
//
//            @Override
//            public void onProgress(float v) {
//
//            }
//        });


    ////参数分别是图片集合路径,输出路径,输出视频的宽度，输出视频的高度，输出视频的帧率
//        EpEditor.pic2video(AppConfig.getInstance().getAPP_PATH_ROOT() + "/images/image%d.jpg", videoPath, 480, 320, 10, new OnEditorListener() {
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onFailure() {
//                LogUtils.e("onFailure");
//            }
//
//            @Override
//            public void onProgress(float progress) {
//
//            }
//        });
//    }

    private String[] execCmd(String cmd, OnCallBack callBack) {
        String[] command = cmd.split(" ");
        if (command.length != 0) {
            execFFmpegBinary(command, callBack);
        } else {
            Toast.makeText(_mActivity, "空", Toast.LENGTH_LONG).show();
        }
        return command;
    }

    private void speak(String text) {
        if (TextUtils.isEmpty(text))
            text = "百度语音，面向广大开发者永久免费开放语音合成技术。";
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        int result = synthesizer.speak(text);
        checkResult(result, "speak");
    }

    /**
     * 合成但是不播放，
     * 音频流保存为文件的方法可以参见SaveFileActivity及FileSaveListener
     */
    private void synthesize(String text) {
        if (TextUtils.isEmpty(text)) {
            text = "欢迎使用百度语音合成SDK,百度语音为你提供支持。";
        }
        int result = synthesizer.synthesize(text);
        checkResult(result, "synthesize");
    }

    /**
     * 批量播放
     */
    private void batchSpeak() {
        List<Pair<String, String>> texts = new ArrayList<Pair<String, String>>();
        texts.add(new Pair<String, String>("开始批量播放，", "a0"));
        texts.add(new Pair<String, String>("123456，", "a1"));
        texts.add(new Pair<String, String>("欢迎使用百度语音，，，", "a2"));
        texts.add(new Pair<String, String>("重(chong3)量这个是多音字示例", "a3"));
        int result = synthesizer.batchSpeak(texts);
        checkResult(result, "batchSpeak");
    }


    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
    private void loadModel(String mode) {
        offlineVoice = mode;
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        toPrint("切换离线语音：" + offlineResource.getModelFilename());
        int result = synthesizer.loadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        checkResult(result, "loadModel");
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            toPrint("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    protected void initialTtsFile() {
        String tmpDir = FileUtil.createTmpDir(_mActivity);
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new FileSaveListener(tmpDir);
        Map<String, String> params = getParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        if (synthesizer!=null) {
            synthesizer.release();
            synthesizer = null;
        }
        synthesizer = new MySyntherizer(_mActivity, initConfig); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    @Deprecated
    protected void initialTts() {
        LoggerProxy.printable(BuildConfig.DEBUG); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener();

        Map<String, String> params = getParams();


        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);

        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoCheck.getInstance(App.getContext()).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        toPrint(message); // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });
        synthesizer = new NonBlockSyntherizer(_mActivity, initConfig); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "3");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(_mActivity, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            toPrint("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }

}
