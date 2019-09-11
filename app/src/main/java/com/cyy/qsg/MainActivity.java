package com.cyy.qsg;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.cyy.qsg.ui.main.SectionsPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    String TAG = "qsg_info";
    NfcUtils nfcUtils = null;
    final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS).build();
    String postPath = "http://39.105.74.138:8777/static/saveArriveNFC";
    SoundPool soundPool;
    int voiceId;
    Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        nfcUtils = new NfcUtils(this);

        vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);

        //sdk版本21是SoundPool 的一个分水岭
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入最多播放音频数量,
            builder.setMaxStreams(1);
            //AudioAttributes是一个封装音频各种属性的方法
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适的属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            //加载一个AudioAttributes
            builder.setAudioAttributes(attrBuilder.build());
            soundPool = builder.build();
        } else {
            /**
             * 第一个参数：int maxStreams：SoundPool对象的最大并发流数
             * 第二个参数：int streamType：AudioManager中描述的音频流类型
             *第三个参数：int srcQuality：采样率转换器的质量。 目前没有效果。 使用0作为默认值。
             */
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        voiceId = soundPool.load(getApplicationContext(), R.raw.finish, 1);
        //异步需要等待加载完成，音频才能播放成功
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    Toast.makeText(MainActivity.this,  "资源加载成功!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //在onResume中开启前台调度
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "--------------2-------------");
        //设定intentfilter和tech-list。如果两个都为null就代表优先接收任何形式的TAG action。也就是说系统会主动发TAG intent。
        if (NfcUtils.mNfcAdapter != null) {
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        }
    }


    //在onNewIntent中处理由NFC设备传递过来的intent
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    //  这块的processIntent() 就是处理卡中数据的方法
    public void processIntent(Intent intent) {
        /*Parcelable[] rawmsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawmsgs[0];
        NdefRecord[] records = msg.getRecords();
        String resultStr = new String(records[0].getPayload());
        // 返回的是NFC检查到卡中的数据
        Log.e(TAG, "processIntent: " + resultStr);*/
        try {
            // 检测卡的id
            //String id = NfcUtils.readNFCId(intent);
            //Log.e(TAG, "processIntent--id: " + id);
            // NfcUtils中获取卡中数据的方法、取出学生id
            String stuId = NfcUtils.readNFCFromTag(intent);
            sendGetRequestAsync(stuId);
        } catch (Exception e) {
            e.printStackTrace();
            vibrator.vibrate(2000);
            Toast.makeText(MainActivity.this,  "打卡失败", Toast.LENGTH_LONG).show();
        }
    }

    private void playNotifySound() {
        //第一个参数soundID
        //第二个参数leftVolume为左侧音量值（范围= 0.0到1.0）
        //第三个参数rightVolume为右的音量值（范围= 0.0到1.0）
        //第四个参数priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理
        //第五个参数loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
        //第六个参数 rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率)
        soundPool.play(voiceId, 1, 1, 1, 0, 1);
    }
    //https://blog.csdn.net/qq_38340601/article/details/81837660 重新参考这个

    private void sendGetRequestAsync(String stuId) {

        final Request request = new Request.Builder()
                .get()
                .url(postPath+"?stuId="+stuId)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response;
                try {
                    response = client.newCall(request).execute();
                    //todo 看看网络超时怎么搞
                        if (response.isSuccessful()) {
                        final String rs = response.body().string();
                        vibrator.vibrate(200);
                        playNotifySound();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,  "打卡成功:"+rs, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        throw new IOException("Unexpected code " + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}