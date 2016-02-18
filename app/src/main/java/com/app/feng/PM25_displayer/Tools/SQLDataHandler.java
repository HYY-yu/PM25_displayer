package com.app.feng.PM25_displayer.Tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by feng on 2015/9/8.
 */
public class SQLDataHandler {

    private SQLiteDatabase database;
    private Context context;

    public SQLDataHandler(Context context) {
        this.context = context;
        database = context.openOrCreateDatabase("user_data.db", Context.MODE_PRIVATE, null);

        //建表
        database.execSQL("create table if not exists citylist(_id integer primary key autoincrement,city text)");

        //建立pm数据表，城市名为主键，包括aqi，pm25，quality，time。
        database.execSQL("create table if not exists pm25data(area text primary key ,aqi integer,pm25 integer,quality text ,time text)");
    }

    public void createCityList(List<String> city) {
        ContentValues values = new ContentValues();
        for (String temp : city
                ) {
            if (temp != null) {
                values.put("city", temp.trim());
                database.insert("citylist", null, values);
                values.clear();
            }

        }
    }

    public boolean checkCityExist(String cityname) {
        Cursor c = database.query("citylist", null, "city=?", new String[]{cityname}, null, null, null);
        if (c.moveToNext()) {
            c.close();
            return true;
        } else {
            return false;
        }

    }

    public List<Map<String,String>> readCityList() {
        List<Map<String,String>> temp_list = new ArrayList<>();

        Cursor c = database.query("pm25data", new String[]{"area", "pm25"}, null, null, null, null, null);

        while (c.moveToNext()) {
            Map<String, String> temp = new HashMap<>();
            String city = c.getString(c.getColumnIndex("area"));
            String pm25 = c.getString(c.getColumnIndex("pm25"));
            temp.put("city", city);
            temp.put("pm25", pm25);
            temp_list.add(temp);
        }
        c.close();
        return temp_list;
    }



    public void InsertOrUpdatePM25List(String city,int aqi,int pm25,String quality,String time) {
        ContentValues values = new ContentValues();
        values.put("area",city);
        values.put("aqi",aqi);
        values.put("pm25",pm25);
        values.put("quality",quality);
        values.put("time", time);

        if(checkPM25Exist(city)){
            //如果数据库中已存在城市的数据
            Log.i("info", "修改成功");
            database.update("pm25data",values,"area like ?",new String[]{city});
        }else {
            //如果数据库没有数据
            Log.i("info", "写入成功");
            database.insert("pm25data", null, values);
        }

    }

    public void deletePm25List(String city) {
        database.delete("pm25data", "area like ?", new String[]{city});

    }

    public boolean checkPM25Exist(String cityname){
        Cursor cursor = database.rawQuery("select * from pm25data where area like ?" , new String[]{cityname});
        if(cursor.moveToNext()) {
            cursor.close();
            return true;
        }else {
            cursor.close();
            return false;
        }

    }

    public void closeDatabase() {
        database.close();
    }

}
