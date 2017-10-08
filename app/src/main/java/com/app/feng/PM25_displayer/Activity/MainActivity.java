package com.app.feng.PM25_displayer.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.app.feng.PM25_displayer.R;
import com.app.feng.PM25_displayer.Tools.ChangeColorAdapter;
import com.app.feng.PM25_displayer.Tools.CityListListener;
import com.app.feng.PM25_displayer.Tools.HttpRequset;
import com.app.feng.PM25_displayer.Tools.MessageHandler;
import com.app.feng.PM25_displayer.Tools.SQLDataHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageView image_add;
    private HttpRequset httpRequset;
    private SQLDataHandler sqlDataHandler;

    private MessageHandler handler;

    private List<Map<String, String>> city_list_source;//数据源
    private ChangeColorAdapter simpleAdapter;

    private CityListListener cityListListener;

    public ListView pm_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //////功能类
        city_list_source = new ArrayList<>();
        simpleAdapter = new ChangeColorAdapter(this, city_list_source, R.layout.city_item_layout, ChangeColorAdapter.MODE_MAIN);
        sqlDataHandler = new SQLDataHandler(this);
        handler = new MessageHandler(this, city_list_source, simpleAdapter);
        //为列表项设置监听器
        cityListListener = new CityListListener(this, simpleAdapter, city_list_source, sqlDataHandler);

        //////UI类
        pm_list = (ListView) findViewById(R.id.listView);
        pm_list.setAdapter(simpleAdapter);
        pm_list.setOnItemLongClickListener(cityListListener);
        pm_list.setOnItemClickListener(cityListListener);

        //创建添加城市按钮
        image_add = (ImageView) findViewById(R.id.image_add);
        image_add.setOnClickListener(this);

        //程序创建时检查网络是否连接上
        if (checkNetworkAvailable(this)) {
            //有网络，检查是否有城市列表文件,无则创建
            Log.i("info", "有网络");
            if (!sqlDataHandler.checkCityExist("北京")) {
                //创建城市列表文件
                Log.i("info", "请求城市列表");
                httpRequset = new HttpRequset(this, HttpRequset.REQUEST_CITY, sqlDataHandler, handler);
                httpRequset.start();
            }

            //检查本地是否有JSON文件
            if (sqlDataHandler.checkCityExist("北京")) {
                //本地上存在JSON文件
                city_list_source.addAll(sqlDataHandler.readCityList());
                simpleAdapter.notifyDataSetChanged();
            }

        } else {
            //无网络
            //检查本地是否有JSON文件
            if (!sqlDataHandler.checkCityExist("北京")) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("网络错误");
                builder.setMessage("网络无法访问，请确认网络连接是否正常");
                builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                });

                builder.create().show();
            } else {
                //本地上存在JSON文件
                city_list_source.addAll(sqlDataHandler.readCityList());
                simpleAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("确认退出吗？");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    sqlDataHandler.closeDatabase();//关闭数据库
                    MainActivity.this.finish();
                }

            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean checkNetworkAvailable(Context context) {
        //获取系统服务
        ConnectivityManager conne = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conne != null) {
            NetworkInfo[] info = conne.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        if (anInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            getBaseContext();
                            return true;
                        } else if (anInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sort) {
            //排行榜
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, CityChartActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("info", "数据回传");
        Log.i("info", " " + requestCode + "," + resultCode);

        if (requestCode == 1120 && resultCode == 1120) {
            //接收SearchCity回传的城市名称，并发送给服务器
            Toast.makeText(MainActivity.this, "正在查询", Toast.LENGTH_SHORT).show();
            Log.i("info city name", data.getStringExtra("city"));
            httpRequset = null;
            httpRequset = new HttpRequset(this, HttpRequset.REQUEST_PM25, sqlDataHandler, handler);
            httpRequset.setCity_name(data.getStringExtra("city"));
            httpRequset.start();
        }
        if (requestCode == 190 && resultCode == 191) {
            Log.i("info", "更新列表");
            city_list_source.clear();
            city_list_source.addAll(sqlDataHandler.readCityList());

            simpleAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.image_add) {
            //打开选择城市Activity
            //并加载动画
            Intent intent = new Intent(MainActivity.this, SearchCity.class);
            startActivityForResult(intent, 1120);

            this.overridePendingTransition(R.anim.acticity_translate_in, R.anim.acticity_translate_out);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqlDataHandler.closeDatabase();
    }
}
