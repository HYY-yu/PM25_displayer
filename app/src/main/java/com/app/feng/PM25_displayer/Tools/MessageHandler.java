package com.app.feng.PM25_displayer.Tools;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.app.feng.PM25_displayer.Activity.CityChartActivity;
import com.app.feng.PM25_displayer.Activity.CityInfoActivity;
import com.app.feng.PM25_displayer.Activity.MainActivity;
import com.app.feng.PM25_displayer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MessageHandler extends Handler {
    private Context context;

    private List<Map<String, String>> list;

    private ChangeColorAdapter simpleAdapter;

    private MainActivity mainActivity;

    private CityInfoActivity cityInfoActivity;

    private CityChartActivity cityChartActivity;

    //根据不同的传入参数，重载构造方法，使MesageHandler实现不同的功能。

    public MessageHandler(CityChartActivity cityChartActivity) {
        this.cityChartActivity = cityChartActivity;
        context = cityChartActivity;
    }

    public MessageHandler(CityInfoActivity cityInfoActivity) {
        this.cityInfoActivity = cityInfoActivity;
        context = cityInfoActivity;
    }

    public MessageHandler(MainActivity mainActivity, List<Map<String, String>> list, ChangeColorAdapter s) {
        super(mainActivity.getMainLooper());
        if (list == null)
            list = new ArrayList<>();
        this.list = list;
        simpleAdapter = s;
        this.mainActivity = mainActivity;
        context = mainActivity;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        switch (msg.arg1) {
            case HttpRequset.STATUS_ERROR:
                Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
                break;


            case HttpRequset.INFORMATION:
                //更新列表
                Log.i("info", "接受消息");
                Bundle bundle = msg.getData();
                String city = bundle.getString("city");
                int pm25 = bundle.getInt("pm25");
                Map<String, String> map = new HashMap<>();
                map.put("city", city);
                map.put("pm25", String.valueOf(pm25));
                Iterator<Map<String, String>> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Map<String, String> m = iterator.next();
                    if (m.get("city").equals(city)) {
                        iterator.remove();
                    }
                }
                list.add(map);

                simpleAdapter.notifyDataSetChanged();

                //加载动画
                LayoutAnimationController controller = new LayoutAnimationController(
                        AnimationUtils.loadAnimation(mainActivity, R.anim.translate_in));
                controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
                mainActivity.pm_list.setLayoutAnimation(controller);
                mainActivity.pm_list.startLayoutAnimation();
                break;


            case CityInfoActivity.CITYINFO_ACTIVITY:
                //CITYINFO模式
                //设置进度条的动画效果
                if (cityInfoActivity.tempProgress > 0 && cityInfoActivity.tempProgress <= 50) {
                    cityInfoActivity.progressBar.setColor(Color.parseColor("#4DE14D"));
                    cityInfoActivity.textView_quality.setTextColor(Color.parseColor("#4DE14D"));
                } else if (cityInfoActivity.tempProgress > 50 && cityInfoActivity.tempProgress <= 100) {
                    cityInfoActivity.progressBar.setColor(Color.parseColor("#CAF253"));
                    cityInfoActivity.textView_quality.setTextColor(Color.parseColor("#CAF253"));
                } else if (cityInfoActivity.tempProgress > 100 && cityInfoActivity.tempProgress <= 150) {
                    cityInfoActivity.progressBar.setColor(Color.parseColor("#F2F200"));
                    cityInfoActivity.textView_quality.setTextColor(Color.parseColor("#F2F200"));
                } else if (cityInfoActivity.tempProgress > 150 && cityInfoActivity.tempProgress <= 200) {
                    cityInfoActivity.progressBar.setColor(Color.parseColor("#FFDD57"));
                    cityInfoActivity.textView_quality.setTextColor(Color.parseColor("#FFDD57"));
                } else if (cityInfoActivity.tempProgress > 200 && cityInfoActivity.tempProgress <= 300) {
                    cityInfoActivity.progressBar.setColor(Color.parseColor("#FF9957"));
                    cityInfoActivity.textView_quality.setTextColor(Color.parseColor("#FF9957"));
                } else if (cityInfoActivity.tempProgress > 300 && cityInfoActivity.tempProgress <= 400) {
                    cityInfoActivity.progressBar.setColor(Color.parseColor("#FE5758"));
                    cityInfoActivity.textView_quality.setTextColor(Color.parseColor("#FE5758"));
                } else {
                    cityInfoActivity.progressBar.setColor(Color.parseColor("#E54FA8"));
                    cityInfoActivity.textView_quality.setTextColor(Color.parseColor("#E54FA8"));
                }

                if (msg.what == 0) {
                    cityInfoActivity.progressBar.setProgress(cityInfoActivity.tempProgress);
                } else if (msg.what == 1) {
                    cityInfoActivity.progressBar.setProgress(cityInfoActivity.mProgress);
                }
                break;


            case CityChartActivity.CITYCHART_ACTIVITY:
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
                break;
        }
    }
}
