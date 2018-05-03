package com.bj.newsfastget.fragment;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.bj.newsfastget.App;
import com.bj.newsfastget.AppConfig;
import com.bj.newsfastget.BuildConfig;
import com.bj.newsfastget.R;
import com.bj.newsfastget.adapter.InfoAdapter;
import com.bj.newsfastget.simple.SwipeSimpleFragment;
import com.bj.newsfastget.tts.AutoCheck;
import com.bj.newsfastget.tts.FileSaveListener;
import com.bj.newsfastget.tts.InitConfig;
import com.bj.newsfastget.tts.MainHandlerConstant;
import com.bj.newsfastget.tts.MySyntherizer;
import com.bj.newsfastget.tts.NonBlockSyntherizer;
import com.bj.newsfastget.tts.UiMessageListener;
import com.bj.newsfastget.util.FileUtil;
import com.bj.newsfastget.util.OfflineResource;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SnackbarUtils;
import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import VideoHandle.EpEditor;
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
public class HomeTwoFragment extends SwipeSimpleFragment  implements MainHandlerConstant {

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
    protected Handler mainHandler;

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
        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handle(msg);
            }

        };
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
            itemAdapter.addData(0, "图片转存完毕");
            initialTtsFile(); // 初始化TTS引擎
        } else {
            EasyPermissions.requestPermissions(_mActivity,
                    "请不要拒绝我们善意的权限请求,谢谢.\n\n需要赋予以下权限才能保证程序的正常运行:\n\n1.SD卡写入与读取", 100, perms);
        }
    }

    protected void handle(Message msg) {
        int what = msg.what;
        switch (what) {
            case PRINT:
                LogUtils.e(msg);
                break;
            case UI_CHANGE_INPUT_TEXT_SELECTION:
                break;
            case UI_CHANGE_SYNTHES_TEXT_SELECTION:
                SpannableString colorfulText = new SpannableString("test测试");
                if (msg.arg1 <= colorfulText.toString().length()) {
                    colorfulText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, msg.arg1, Spannable
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                    LogUtils.e(colorfulText);
                }
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.button1, R.id.button2, R.id.button3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button1:
                break;
            case R.id.button2:
                itemAdapter.setNewData(new ArrayList<String>());
                toChangeVideo(null);
                break;
            case R.id.button3:
//                speak("");
                synthesize("");
//                batchSpeak();
                break;
        }
    }

    protected void toPrint(String str) {
        Message msg = Message.obtain();
        msg.obj = str;
        mainHandler.sendMessage(msg);
    }

    private void print(Message msg) {
        String message = (String) msg.obj;
        if (message != null) {
            itemAdapter.addData(0,message);
        }
    }


    public static void toChangeVideo(final String wavPath) {
        //ffmpeg -f image2 -i /home/ttwang/images/image%d.jpg  -vcodec libx264 -r 10  tt.mp4
        File file = new File(AppConfig.getInstance().getAPP_PATH_ROOT() + "/video");
        if (!file.exists())
            file.mkdir();
        String cmd;
        long time=System.currentTimeMillis();
        final String videoPath=AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/" + time+ ".mp4";
        final String newVideoPath=AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/new-" + time + ".mp4";
        StringBuilder imageStr=new StringBuilder(0);
        for (int i = 1; i < 14; i++) {
            imageStr.append(AppConfig.getInstance().getAPP_PATH_ROOT() + "/images/image"+i+".jpg ");
        }
        cmd="-threads 8 -y -r 25 -i "+ AppConfig.getInstance().getAPP_PATH_ROOT() + "/images/image%d.jpg "+"-vf zoompan=z=1.1:x='if(eq(x,0),100,x-1)':s='480*320' -t 50.0 -vcodec libx264 -r 10 " +videoPath;
//        if (TextUtils.isEmpty(wavPath))
//            cmd = "-f image2 -i " + AppConfig.getInstance().getAPP_PATH_ROOT() + "/images/image%d.jpg -vcodec libx264 -r 1 " +
//                    AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/" + System.currentTimeMillis() + ".mp4";
//        else
//            //-threads 8 -y -r 25 -i /Users/lishengqiang/Documents/temp/2.png -vf zoompan=z=1.1:x='if(eq(x,0),100,x-1)':s='540*960' -t 2.0 /Users/lishengqiang/Documents/temp/output/0.mp4
//            cmd = "-threads 2 -y -r 10 -i " + wavPath + " -f image2 -framerate 12 -i " + AppConfig.getInstance().getAPP_PATH_ROOT() + "/images/image%d.jpg -vcodec libx264 -r 1 " +
//                    AppConfig.getInstance().getAPP_PATH_ROOT() + "/video/" + System.currentTimeMillis() + ".mp4";
        EpEditor.execCmd(cmd, 0, new OnEditorListener() {
            @Override
            public void onSuccess() {
                LogUtils.e("onSuccess");
                //        添加背景音乐
//参数分别是视频路径，音频路径，输出路径,原始视频音量(1为100%,0.7为70%,以此类推),添加音频音量
                EpEditor.music(videoPath, wavPath, newVideoPath, 1, 1.0f, new OnEditorListener() {
                    @Override
                    public void onSuccess() {
                        LogUtils.e("视频音频合成onSuccess");
                    }

                    @Override
                    public void onFailure() {
                        LogUtils.e("视频音频合成onFailure");
                    }

                    @Override
                    public void onProgress(float progress) {
                        //这里获取处理进度
                    }
                });
            }

            @Override
            public void onFailure() {
                LogUtils.e("onFailure");
            }

            @Override
            public void onProgress(float v) {

            }
        });
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
    }

    private void speak(String text) {
        if (TextUtils.isEmpty(text))
            text= "百度语音，面向广大开发者永久免费开放语音合成技术。";
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
        SpeechSynthesizerListener listener = new FileSaveListener(mainHandler, tmpDir);
        Map<String, String> params = getParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode,  params, listener);
        synthesizer = new MySyntherizer(_mActivity, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
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
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);

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
        synthesizer = new NonBlockSyntherizer(_mActivity, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
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
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
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
