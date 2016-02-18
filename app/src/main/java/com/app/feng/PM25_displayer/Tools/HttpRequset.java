package com.app.feng.PM25_displayer.Tools;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.app.feng.PM25_displayer.Activity.CityChartActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by jiang on 2015/9/8.
 */
public class HttpRequset extends Thread {

    //设置服务器地址
    private static final String APPKEY = "rFffVcoTPNgcJEu6L6Ey";
    private static final String PM25URL = "http://www.pm25.in/api/querys/pm2_5.json";
    private static final String CITYURL = "http://www.pm25.in/api/querys.json";
    private static final String CHARTURL = "http://www.pm25.in/api/querys/aqi_ranking.json";
    //请求模式--请求城市列表或者请求pm25数据
    public static final int REQUEST_CITY = 1;
    public static final int REQUEST_PM25 = 0;
    public static final int REQUEST_CHART = 2;
    //请求模式
    private int requestMode;

    private Activity context;
    //临时数据表
    private List<String> city_list;

    private SQLDataHandler sqlDataHandler;
    //更新UI
    private Handler handler;

    private String city_name;
    //用来配合Handler更新UI
    private Message message;

    //这些常量赋值给arg1表示该信息的类型
    public static final int STATUS_ERROR = -1;
    public static final int INFORMATION = 0;

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public HttpRequset(Activity context, int requestMode, SQLDataHandler s, Handler handler) {
        this.context = context;
        this.requestMode = requestMode;
        this.sqlDataHandler = s;
        this.handler = handler;
        city_list = new ArrayList<>();
        message = new Message();
    }

    @Override
    public void run() {
        String u = null;
        if (requestMode == REQUEST_PM25)
            try {
                u = PM25URL + "?city=" + URLEncoder.encode(city_name, "utf-8") + "&token=" + APPKEY + "&stations=no";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        else if (requestMode == REQUEST_CITY)
            u = CITYURL + "?token=" + APPKEY;
        else if (requestMode == REQUEST_CHART) {
            u = CHARTURL + "?token=" + APPKEY;
        }

        URL url;
        try {
            url = new URL(u);
            Log.i("URL", u);
            //打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            //解析JSON文件
            StringBuffer sb = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }

            //测试
            String mytemp = "[{\"aqi\": 151,\"area\": \"" + city_name + "\",\"pm2_5\": 190,\"pm2_5_24h\": 115,\"quality\": \"中度污染\"," +
                    "\"time_point\": \"2013-04-16T11:00:00Z\"}]";
            Log.i("mytemp", sb.toString());

            // JSONTokener tokener = new JSONTokener(sb.toString());

            if (requestMode == REQUEST_PM25)
                parsePM25JSON(sb.toString());
            else if (requestMode == REQUEST_CITY)
                parseCITYJSON(sb.toString());
            else if (requestMode == REQUEST_CHART) {
                paresCHARTJSON(sb.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
            //通知Handler弹出错误提示窗口
            message.arg1 = STATUS_ERROR;
            message.obj = "服务器未响应";
            handler.sendMessage(message);
        }

    }

    public void parsePM25JSON(String json) {
        JSONArray array = null;
        try {
            //解析数据
            array = new JSONArray(json);
            JSONObject object = array.getJSONObject(0);
            String city = object.getString("area");
            int pm25 = object.getInt("pm2_5");
            String quality = object.getString("quality");
            String time = object.getString("time_point");
            int aqi = object.getInt("aqi");
            Log.i("info", "接收" + city + pm25);

            //写入数据到数据库
            sqlDataHandler.InsertOrUpdatePM25List(city, aqi, pm25, quality, time);

            //通知程序更新界面
            Bundle bundle = new Bundle();
            bundle.putString("city", city);
            bundle.putInt("pm25", pm25);
            Message mes = new Message();
            mes.setData(bundle);

            Log.i("info", "发送消息");
            handler.sendMessage(mes);

        } catch (JSONException e) {
            try {
                JSONObject object = new JSONObject(json);
                String error = object.getString("error");
                Log.i("ERROR", error);
                //通知Handler弹出错误提示窗口
                message.arg1 = STATUS_ERROR;
                message.obj = error;
                handler.sendMessage(message);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
    }

    public void parseCITYJSON(String json) {
        JSONObject object = null;
        try {
            object = new JSONObject(json);
            JSONArray array = object.getJSONArray("cities");
            for (int i = 0; i < array.length(); i++) {
                String temp = array.getString(i);
                city_list.add(temp);
            }
            //测试
            /*for (String temp : city_list) {
                Log.i("city", temp);
            }*/
            //写入
            sqlDataHandler.createCityList(city_list);

        } catch (JSONException e) {
            if (object != null)
                try {
                    String error = object.getString("error");
                    Log.i("ERROR", error);
                    //通知Handler弹出错误提示窗口
                    message.arg1 = STATUS_ERROR;
                    message.obj = error;
                    handler.sendMessage(message);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
        }
    }

    public void paresCHARTJSON(String json) {
        List<Map<String, String>> tempdata = new ArrayList<>();
        JSONArray array = null;
        try {
            array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                Map<String, String> tempMap = new HashMap<>();
                tempMap.put("num", String.valueOf(i + 1));
                JSONObject object = array.getJSONObject(i);
                String city = object.getString("area");
                String quality = object.getString("quality");
                int aqi = object.getInt("aqi");
                tempMap.put("city", city);
                tempMap.put("quality", quality);
                tempMap.put("aqi", String.valueOf(aqi));
                tempdata.add(tempMap);
            }
            message.arg1 = CityChartActivity.CITYCHART_ACTIVITY;
            message.obj = tempdata;
            handler.sendMessage(message);

        } catch (JSONException e) {
            try {
                JSONObject object = new JSONObject(json);
                String error = object.getString("error");
                Log.i("ERROR", error);
                message.arg1 = STATUS_ERROR;
                message.obj = error;
                handler.sendMessage(message);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
}
