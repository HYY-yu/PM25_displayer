package com.app.feng.PM25_displayer.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.app.feng.PM25_displayer.R;
import com.app.feng.PM25_displayer.Tools.ChangeColorAdapter;
import com.app.feng.PM25_displayer.Tools.HttpRequset;
import com.app.feng.PM25_displayer.Tools.MessageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *本类使用ScollView（滚动面板）来实现整个界面的滑动效果，但是其中的两个ListVeiw（列表）在滚动面板中
 * 不能正确的设置自己的高度，查阅网络资料得reSetListViewHeight()函数，使其能正确显示出来。
 * Created by jiang on 2015/9/16.
 */
public class CityChartActivity extends AppCompatActivity {
    //本类的标志
    public static final int CITYCHART_ACTIVITY = 98;


    //两个列表
    public ListView listView_best;
    public ListView listView_bad;

    //改变颜色的数据适配器
    public ChangeColorAdapter bastAdapter;
    public ChangeColorAdapter badAdapter;

    //数据源
    public List<Map<String, String>> mdata;
    public List<Map<String, String>> mBastdata = new ArrayList<>();
    public List<Map<String, String>> mBaddata = new ArrayList<>();

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置UI界面。
        setContentView(R.layout.city_chart_layout);

        //打开导航栏
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            Log.i("ActionBar", "Exists");
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle("实时排行");
            actionBar.show();
        }

        handler = new MessageHandler(this);

        //初始化ListView
        listView_bad = (ListView) findViewById(R.id.listView_bad);
        listView_best = (ListView) findViewById(R.id.listView_best);
        //从服务器获得数据，只是调用了服务器类的方法
        HttpRequset httpRequset = new HttpRequset(this, HttpRequset.REQUEST_CHART, null, handler);
        httpRequset.start();
        //添加适配器
        badAdapter = new ChangeColorAdapter(this, mBaddata, R.layout.city_chart_item_layout, ChangeColorAdapter.MODE_CHART);
        bastAdapter = new ChangeColorAdapter(this, mBastdata, R.layout.city_chart_item_layout, ChangeColorAdapter.MODE_CHART);
        listView_best.setAdapter(bastAdapter);
        listView_bad.setAdapter(badAdapter);

        reSetListViewHeight(listView_best);
        reSetListViewHeight(listView_bad);
    }

    public void reSetListViewHeight(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //ActionBar的返回按钮
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    ///////此Handler中的代码已转移到MessageHandler，更新UI统一由MessageHandler负责
    //在UI线程中不能更新数据，所以服务器的数据是有MessageHandler负责的

 /*   static class ListHandler extends android.os.Handler {
        //弱引用
        WeakReference<CityChartActivity> mActivity;

        public ListHandler(CityChartActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        //处理服务器返回的数据
        @Override
        public void handleMessage(Message msg) {
            //接收数据
            CityChartActivity cityChartActivity = mActivity.get();
            if (msg.arg1 == HttpRequset.INFORMATION) {
                //msg里面就是服务器返回的数据
                cityChartActivity.mdata = (List<Map<String, String>>) msg.obj;
                //将数据源转换成列表显示的数据
                cityChartActivity.mBastdata.addAll(cityChartActivity.mdata.subList(0, 10));
                cityChartActivity.mBaddata.addAll(cityChartActivity.mdata.subList(cityChartActivity.mdata.size() - 11, cityChartActivity.mdata.size() - 1));
                //将List逆置
                Collections.reverse(cityChartActivity.mBaddata);
                //改变序号
                int i = 1;
                for (Map<String, String> temp : cityChartActivity.mBaddata) {
                    temp.put("num", String.valueOf(i));
                    i++;
                }
                //通知数据适配器刷新
                cityChartActivity.bastAdapter.notifyDataSetChanged();
                cityChartActivity.badAdapter.notifyDataSetChanged();
                //改变列表高度
                cityChartActivity.reSetListViewHeight(cityChartActivity.listView_bad);
                cityChartActivity.reSetListViewHeight(cityChartActivity.listView_best);

                //Toast.makeText(cityChartActivity, "更新数据", Toast.LENGTH_SHORT).show();
            }else if(msg.arg1 == HttpRequset.STATUS_ERROR) {
                //提示用户网络错误
                String error = (String) msg.obj;
                Toast.makeText(cityChartActivity, " " + error, Toast.LENGTH_SHORT).show();
            }
        }
    }*/
}
