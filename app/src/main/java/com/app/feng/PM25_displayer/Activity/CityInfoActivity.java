package com.app.feng.PM25_displayer.Activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TimeUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.app.feng.PM25_displayer.R;
import com.app.feng.PM25_displayer.Tools.HttpRequset;
import com.app.feng.PM25_displayer.Tools.MessageHandler;
import com.app.feng.PM25_displayer.Tools.SQLDataHandler;
import com.app.feng.circleprogressbar.CircleProgressBar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by feng on 2015/9/14.
 */
public class CityInfoActivity extends AppCompatActivity {
    //此类需要 城市名 pm25 aqi 空气等级 ，需要SQLHander取出数据，开始时需要城市名信息。
    //本身要设置Progressbar，等级textView，建议textView，时间textView。

    //调用Handler
    public static final int CITYINFO_ACTIVITY = 99;

    //-------UI组件
    public TextView textView_suggest;
    public TextView textView_time;
    public TextView textView_quality;
    public CircleProgressBar progressBar;

    //------数据
    private SQLiteDatabase sqLiteDatabase;
    private String city_name;
    private String aqi;
    private String pm25;
    private String quality;
    private String time;
    public int mProgress;
    public int tempProgress;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        setContentView(R.layout.city_info_activity_layout);

        city_name = this.getIntent().getStringExtra("city");
        if (actionBar != null) {
            Log.i("ActionBar", "Exists");
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle(city_name);
            actionBar.show();
        }
        readDatabase();
        initView();

        handler = new MessageHandler(this);

    }

    private void initView() {
        textView_time = (TextView) findViewById(R.id.textView_time);
        textView_suggest = (TextView) findViewById(R.id.textView_suggest);
        textView_quality = (TextView) findViewById(R.id.textView_quality);
        progressBar = (CircleProgressBar) findViewById(R.id.circleProgressBar);


        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CHINA);
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            time = " ";
        }
        textView_time.setText(String.format("更新于：%s",
                parse != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(parse) : " 无数据 "));
        textView_quality.setText(quality);
        textView_suggest.setText(getSuggest(Integer.parseInt(aqi)));

        //为progressBar设置动画
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = handler.obtainMessage();
                message.arg1 = CITYINFO_ACTIVITY;
                if (tempProgress < mProgress) {
                    message.what = 0;
                    tempProgress++;
                } else {
                    message.what = 1;
                    this.cancel();
                }
                handler.sendMessage(message);

            }
        }, 800, 6);

    }

    private String getSuggest(int aqi) {
        final String suggest1 = "空气质量令人满意，基本无空气污染，各类人群可正常活动";
        final String suggest2 = "空气质量可接受，但某些污染物可能对极少数异常敏感人群健康有较弱影响，建议极少数异常敏感人群应减少户外活动";
        final String suggest3 = "易感人群症状有轻度加剧，健康人群出现刺激症状。建议儿童、老年人及心脏病、呼吸系统疾病患者应减少长时间、高强度的户外锻炼";
        final String suggest4 = "进一步加剧易感人群症状，可能对健康人群心脏、呼吸系统有影响，建议疾病患者避免长时间、高强度的户外锻练，一般人群适量减少户外运动";
        final String suggest5 = "心脏病和肺病患者症状显著加剧，运动耐受力降低，健康人群普遍出现症状，建议儿童、老年人和心脏病、肺病患者应停留在室内，一般人群减少户外运动";
        final String suggest6 = "健康人群运动耐受力降低，有明显强烈症状，提前出现某些疾病，建议儿童、老年人和病人应当留在室内，避免体力消耗，一般人群应避免户外活动";

        if (aqi > 0 && aqi <= 50) {
            return suggest1;
        } else if (aqi > 50 && aqi <= 100) {
            return suggest2;
        } else if (aqi > 100 && aqi <= 150) {
            return suggest3;
        } else if (aqi > 150 && aqi <= 200) {
            return suggest4;
        } else if (aqi > 200 && aqi <= 300) {
            return suggest5;
        } else if (aqi > 300 && aqi <= 400) {
            return suggest6;
        } else {
            return suggest6;
        }
    }

    private void readDatabase() {
        sqLiteDatabase = this.openOrCreateDatabase("user_data.db", MODE_PRIVATE, null);
        Cursor cursor = sqLiteDatabase.query("pm25data", new String[]{"area", "aqi", "pm25", "quality", "time"}, "area like ?", new String[]{city_name}, null, null, null);
        while (cursor.moveToNext()) {
            aqi = cursor.getString(cursor.getColumnIndex("aqi"));
            pm25 = cursor.getString(cursor.getColumnIndex("pm25"));
            quality = cursor.getString(cursor.getColumnIndex("quality"));
            time = cursor.getString(cursor.getColumnIndex("time"));

        }
        cursor.close();

        mProgress = Integer.parseInt(aqi);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        sqLiteDatabase.close();
        setResult(191);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //此intent用于跳转回父类
                sqLiteDatabase.close();
                setResult(191);
                this.finish();
                break;
            case R.id.refresh:
                Log.i("refresh", "点击刷新");
                SQLDataHandler sqlDataHandler = new SQLDataHandler(this);
                HttpRequset requset = new HttpRequset(this, HttpRequset.REQUEST_PM25, sqlDataHandler, handler);
                requset.setCity_name(city_name);
                requset.start();
                try {
                    requset.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sqlDataHandler.closeDatabase();
                readDatabase();
                initView();
                break;
        }
        return true;
    }
}
