package com.app.feng.PM25_displayer.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.app.feng.PM25_displayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liu on 2015/9/8.
 */
public class SearchCity extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener
        , View.OnClickListener {
    //搜索栏
    private SearchView searchView;
    //城市列表
    private ListView listView;
    //用于读取数据库
    private SQLiteDatabase database;
    //数据源
    private List<String> cityList = new ArrayList<>();
    //数据适配器
    private ArrayAdapter<String> arrayAdapter;
    //定位功能按钮
    private ImageView imageview_Location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.hide();
        }
        //搜索栏
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        //定位功能
        imageview_Location = (ImageView) findViewById(R.id.imageView_Location);
        //点击事件的监听器
        imageview_Location.setOnClickListener(this);
        //接入数据库
        database = this.openOrCreateDatabase("user_data.db", MODE_PRIVATE, null);
        //读取城市信息
        Cursor cursor = database.rawQuery("select * from citylist", null);
        while (cursor.moveToNext()) {
            String temp = cursor.getString(1);
            cityList.add(temp);
        }
        cursor.close();
        //写入数据适配器
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1
                , cityList);
        listView = (ListView) findViewById(R.id.listView_search);
        //显示列表
        listView.setAdapter(arrayAdapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //回传数据
        Log.i("search", "查询" + query);
        //返回搜索框中选择的城市
        Intent intent = new Intent();
        intent.putExtra("city", query);
        setResult(1120, intent);
        //关闭窗口
        this.finish();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            //清空数据筛选器
            listView.clearTextFilter();
        } else {
            //为列表设置数据筛选器
            listView.setFilterText(newText);
        }

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ///返回选择的城市
        String clickCity = arrayAdapter.getItem(position);
        searchView.setQuery(clickCity, true);

        Intent intent = new Intent();
        intent.putExtra("city", searchView.getQuery());

        setResult(1120, intent);
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //接收定位功能回传的当前所在城市
        if (requestCode == 101 && resultCode == 100) {

            String city_name = data.getStringExtra("city");
            String error = data.getStringExtra("error");
            if (city_name != null) {
                //返回的城市名
                String city = city_name.substring(0, city_name.length() - 1);

                Log.i("Locaiton city", " " + city);

                Intent intent = new Intent();
                intent.putExtra("city", city);
                setResult(1120, intent);
                this.finish();

            } else if (error != null) {
                Toast.makeText(this, " 定位失败" + error, Toast.LENGTH_SHORT).show();
                Log.i("error_Location", " " + error);
            }
        }
    }

    @Override
    public void onClick(View v) {
        //打开定位功能
        Intent intent = new Intent();
        intent.setClass(SearchCity.this, CityLocationActivity.class);
        startActivityForResult(intent, 101);
    }
}
