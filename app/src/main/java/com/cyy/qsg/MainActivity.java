package com.cyy.qsg;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    String TAG = "qsg_info";
    NfcUtils nfcUtils = null;
    final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS).build();
    String callNameApi = "http://39.105.74.138:8777/static/saveArriveNFC";
    String todayListApi = "http://39.105.74.138:8777/static/todayList";
    SoundPool soundPool;
    int voiceId;
    Vibrator vibrator;
    private List<Student> studentList = new ArrayList<>();
    StudentAdapter myStuAdapter = null;
    boolean isCallNameMode = false;//点名模式只显示未到的，点后从列表移除。否则显示全部，颜色标记已到的
    String currentGrade = "0";//0 显示所有
    String currentClass = "0";
    boolean isZw = true;
    List<Student> studentListAfterFilter = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setGradeAndClassClickListener();

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isZw){
                    loadStudentData("ws");// 初始化学生数据
                    Snackbar.make(view, "已切换到晚上名单", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    isZw = false;
                }else{
                    loadStudentData("zw");// 初始化学生数据
                    Snackbar.make(view, "已切换到中午名单", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    isZw = true;
                }

            }
        });


        myStuAdapter = new StudentAdapter(MainActivity.this,R.layout.student_item, studentListAfterFilter);
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(myStuAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id){
                Student student = studentListAfterFilter.get(position);
                String tips = "1".equals(student.getArrive())?"打卡时间:"+student.getArriveTime().split("T")[1].split("\\+")[0]:"未打卡";
                Toast.makeText(MainActivity.this, tips,Toast.LENGTH_LONG).show();

            }
        });
        loadStudentData("zw");// 初始化学生数据




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

    private void setGradeAndClassClickListener() {
        final Resources resources=getBaseContext().getResources();
        final Drawable drawable = resources.getDrawable(R.drawable.textview_border);
        final Drawable drawable_unclick = resources.getDrawable(R.drawable.textview_border_unclick);
        final TextView tvGrade1 = findViewById(R.id.grade1);
        final TextView tvGrade2 = findViewById(R.id.grade2);
        final TextView tvGrade3 = findViewById(R.id.grade3);
        final TextView tvGrade4 = findViewById(R.id.grade4);
        final TextView tvGrade5 = findViewById(R.id.grade5);
        final TextView tvGrade6 = findViewById(R.id.grade6);

        final TextView tvClass1 = findViewById(R.id.class1);
        final TextView tvClass2 = findViewById(R.id.class2);
        final TextView tvClass3 = findViewById(R.id.class3);
        final TextView tvClass4 = findViewById(R.id.class4);
        final TextView tvClass5 = findViewById(R.id.class5);
        final TextView tvClass6 = findViewById(R.id.class6);
        final TextView tvClass7 = findViewById(R.id.class7);
        final TextView tvClass8 = findViewById(R.id.class8);

        tvGrade1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentGrade = "1";
                    List<Student> studentsAfterFilter = new ArrayList<>();
                    tvGrade1.setBackground(drawable);
                    tvGrade2.setBackground(drawable_unclick);
                    tvGrade3.setBackground(drawable_unclick);
                    tvGrade4.setBackground(drawable_unclick);
                    tvGrade5.setBackground(drawable_unclick);
                    tvGrade6.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvGrade2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentGrade = "2";
                    tvGrade1.setBackground(drawable_unclick);
                    tvGrade2.setBackground(drawable);
                    tvGrade3.setBackground(drawable_unclick);
                    tvGrade4.setBackground(drawable_unclick);
                    tvGrade5.setBackground(drawable_unclick);
                    tvGrade6.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvGrade3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentGrade = "3";
                    tvGrade1.setBackground(drawable_unclick);
                    tvGrade2.setBackground(drawable_unclick);
                    tvGrade3.setBackground(drawable);
                    tvGrade4.setBackground(drawable_unclick);
                    tvGrade5.setBackground(drawable_unclick);
                    tvGrade6.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvGrade4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentGrade = "4";
                    tvGrade1.setBackground(drawable_unclick);
                    tvGrade2.setBackground(drawable_unclick);
                    tvGrade3.setBackground(drawable_unclick);
                    tvGrade4.setBackground(drawable);
                    tvGrade5.setBackground(drawable_unclick);
                    tvGrade6.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvGrade5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentGrade = "5";
                    tvGrade1.setBackground(drawable_unclick);
                    tvGrade2.setBackground(drawable_unclick);
                    tvGrade3.setBackground(drawable_unclick);
                    tvGrade4.setBackground(drawable_unclick);
                    tvGrade5.setBackground(drawable);
                    tvGrade6.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvGrade6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentGrade = "6";
                    tvGrade1.setBackground(drawable_unclick);
                    tvGrade2.setBackground(drawable_unclick);
                    tvGrade3.setBackground(drawable_unclick);
                    tvGrade4.setBackground(drawable_unclick);
                    tvGrade5.setBackground(drawable_unclick);
                    tvGrade6.setBackground(drawable);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });


        tvClass1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentClass = "1";
                    tvClass1.setBackground(drawable);
                    tvClass2.setBackground(drawable_unclick);
                    tvClass3.setBackground(drawable_unclick);
                    tvClass4.setBackground(drawable_unclick);
                    tvClass5.setBackground(drawable_unclick);
                    tvClass6.setBackground(drawable_unclick);
                    tvClass7.setBackground(drawable_unclick);
                    tvClass8.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvClass2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentClass = "2";
                    tvClass1.setBackground(drawable_unclick);
                    tvClass2.setBackground(drawable);
                    tvClass3.setBackground(drawable_unclick);
                    tvClass4.setBackground(drawable_unclick);
                    tvClass5.setBackground(drawable_unclick);
                    tvClass6.setBackground(drawable_unclick);
                    tvClass7.setBackground(drawable_unclick);
                    tvClass8.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvClass3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentClass = "3";
                    tvClass1.setBackground(drawable_unclick);
                    tvClass2.setBackground(drawable_unclick);
                    tvClass3.setBackground(drawable);
                    tvClass4.setBackground(drawable_unclick);
                    tvClass5.setBackground(drawable_unclick);
                    tvClass6.setBackground(drawable_unclick);
                    tvClass7.setBackground(drawable_unclick);
                    tvClass8.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvClass4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentClass = "4";
                    tvClass1.setBackground(drawable_unclick);
                    tvClass2.setBackground(drawable_unclick);
                    tvClass3.setBackground(drawable_unclick);
                    tvClass4.setBackground(drawable);
                    tvClass5.setBackground(drawable_unclick);
                    tvClass6.setBackground(drawable_unclick);
                    tvClass7.setBackground(drawable_unclick);
                    tvClass8.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvClass5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentClass = "5";
                    tvClass1.setBackground(drawable_unclick);
                    tvClass2.setBackground(drawable_unclick);
                    tvClass3.setBackground(drawable_unclick);
                    tvClass4.setBackground(drawable_unclick);
                    tvClass5.setBackground(drawable);
                    tvClass6.setBackground(drawable_unclick);
                    tvClass7.setBackground(drawable_unclick);
                    tvClass8.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvClass6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentClass = "6";
                    tvClass1.setBackground(drawable_unclick);
                    tvClass2.setBackground(drawable_unclick);
                    tvClass3.setBackground(drawable_unclick);
                    tvClass4.setBackground(drawable_unclick);
                    tvClass5.setBackground(drawable_unclick);
                    tvClass6.setBackground(drawable);
                    tvClass7.setBackground(drawable_unclick);
                    tvClass8.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvClass7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentClass = "7";
                    tvClass1.setBackground(drawable_unclick);
                    tvClass2.setBackground(drawable_unclick);
                    tvClass3.setBackground(drawable_unclick);
                    tvClass4.setBackground(drawable_unclick);
                    tvClass5.setBackground(drawable_unclick);
                    tvClass6.setBackground(drawable_unclick);
                    tvClass7.setBackground(drawable);
                    tvClass8.setBackground(drawable_unclick);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
                }
            }
        });

        tvClass8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Drawable drawable = resources.getDrawable(R.drawable.textview_border);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    currentClass = "8";
                    tvClass1.setBackground(drawable_unclick);
                    tvClass2.setBackground(drawable_unclick);
                    tvClass3.setBackground(drawable_unclick);
                    tvClass4.setBackground(drawable_unclick);
                    tvClass5.setBackground(drawable_unclick);
                    tvClass6.setBackground(drawable_unclick);
                    tvClass7.setBackground(drawable_unclick);
                    tvClass8.setBackground(drawable);
                    Toast.makeText(MainActivity.this,  "grade"+currentGrade+"class"+currentClass, Toast.LENGTH_LONG).show();
                    studentListAfterFilter.clear();
                    studentListAfterFilter.addAll(filterByGradeAndClass(studentList,currentGrade,currentClass));
                    myStuAdapter.notifyDataSetChanged();
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
            sendCallNameRequestAsync(stuId);
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

    //再不修改studentList的情况下，过滤满足班级和年级的学生，返回过滤后的列表
    public List<Student> filterByGradeAndClass(List<Student> studentList, String grade, String clazz) {
        List<Student> studentsAfterFilterGrade = new ArrayList<>();
        List<Student> studentsAfterFilterClass = new ArrayList<>();
        //先过滤年级，当过滤条件为0时表示不过滤
        for (Student s :studentList){
            if (grade.equals(s.getGrade())||grade.equals("0")){
                studentsAfterFilterGrade.add(s);
            }
        }
        //再年级过滤后，再过滤班级
        for(Student s :studentsAfterFilterGrade)
        {
            if (clazz.equals(s.getClazz())||clazz.equals("0")){
                studentsAfterFilterClass.add(s);
            }
        }
        return studentsAfterFilterClass;
    }

    private void sendCallNameRequestAsync(final String stuId) {

        final Request request = new Request.Builder()
                .get()
                .url(callNameApi+"?stuId="+stuId)
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
                                //打卡成功，如果是点名模式,删除条目,否则在adpter中会自动标记颜色
                                Student currentStu = null;
                                for(Student s :studentList){
                                    if (  stuId.equals(s.getId())){
                                        currentStu = s;
                                        break;
                                    }
                                }
                                studentList.remove(currentStu);
                                myStuAdapter.notifyDataSetChanged();
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


    private void loadStudentData(final String banci){

        final Request request = new Request.Builder()
                .get()
                .url(todayListApi+"?banci=" + banci)
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
                        try{
                            JSONArray jsonArray=new JSONArray(rs);
                            studentList.clear();
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Student stu=new Student();
                                stu.setId(jsonObject.get("Id").toString());
                                stu.setName(jsonObject.get("Name").toString());
                                stu.setArriveTime(jsonObject.get("ArriveTime").toString());
                                stu.setArrive(jsonObject.get("Arrive").toString());
                                stu.setGrade(jsonObject.get("Grade").toString());
                                stu.setClazz(jsonObject.get("Class").toString());
                                if(isCallNameMode && "1".equals(jsonObject.get("Arrive").toString())){
                                    continue;//点名模式过滤已到达的学生
                                }
                                studentList.add(stu);
                            }
                            studentListAfterFilter.addAll(studentList);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    myStuAdapter.notifyDataSetChanged();

                                    Toast.makeText(MainActivity.this,  "count:"+studentList.size(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }catch (Exception e){e.printStackTrace();}

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