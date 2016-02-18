package com.app.feng.PM25_displayer.Tools;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.feng.PM25_displayer.R;

import java.util.List;
import java.util.Map;

/**
 * 此类是listView的数据适配器，内部封装了改变Item中TextView的TextColor功能
 * Created by feng on 2015/9/12.
 */
public class ChangeColorAdapter extends BaseAdapter {

    //适配模式，是为排行榜适配还是为主界面适配
    public static final int MODE_CHART = 0;
    public static final int MODE_MAIN = 1;

    private int mode;

    private List<? extends Map<String, String>> mData;

    private int mResource;          //布局文件id
    private LayoutInflater mInflater;//将文件转换成View

    public ChangeColorAdapter(Context context, List<? extends Map<String, String>> mData,
                              int mResource, int mode) {
        this.mData = mData;
        this.mResource = mResource;
        this.mode = mode;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        MainViewHolder mHolder = null;
        ChartViewHolder cHolder = null;

        final Map<String, String> dataSet = mData.get(position);

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(mResource, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            if (mode == MODE_MAIN) {
                mHolder = new MainViewHolder(convertView);
                convertView.setTag(mHolder);
            } else if (mode == MODE_CHART) {
                cHolder = new ChartViewHolder(convertView);
                convertView.setTag(cHolder);
            }

        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            if (mode == MODE_MAIN) {
                mHolder = (MainViewHolder) convertView.getTag();
            } else if (mode == MODE_CHART) {
                cHolder = (ChartViewHolder) convertView.getTag();
            }

        }

        // Bind the data efficiently with the holder.
        if (mode == MODE_MAIN) {
            mHolder.city.setText(dataSet.get("city"));
            String pm25 = dataSet.get("pm25");

            mHolder.pm25.setText(pm25);
            mHolder.pm25.setTextColor(findColorByPm25(pm25));
        } else if (mode == MODE_CHART) {
            cHolder.textView_sortNum.setText(dataSet.get("num"));
            cHolder.textView_sortCity.setText(dataSet.get("city"));

            String aqi = dataSet.get("aqi");
            cHolder.textView_sortAQI.setText(dataSet.get("aqi"));
            cHolder.textView_sortQuality.setText(dataSet.get("quality"));
            cHolder.textView_sortQuality.setTextColor(findColorByAQI(aqi));
            cHolder.textView_sortAQI.setTextColor(findColorByAQI(aqi));
        }
        return convertView;
    }

    /**
     * 根据不同的pm25数值返回不同的颜色
     */
    private int findColorByPm25(String pm25) {
        try {
            int num = Integer.parseInt(pm25);
            if (num > 0 && num < 75) {
                return Color.rgb(77, 225, 80);
            } else if (num >= 75 && num < 150) {
                return Color.YELLOW;
            } else if (num >= 150) {
                return Color.RED;
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return Color.BLACK;
        }
        return Color.BLACK;
    }

    //跟据AQI的值改变颜色
    private int findColorByAQI(String aqi) {
        int num = Integer.parseInt(aqi);
        if (num > 0 && num <= 50) {
            return Color.parseColor("#4DE14D");
        } else if (num > 50 && num <= 100) {
            return Color.parseColor("#CAF253");
        } else if (num > 100 && num <= 150) {
            return Color.parseColor("#E5E600");
        } else if (num > 150 && num <= 200) {
            return Color.parseColor("#FFDD57");
        } else if (num > 200 && num <= 300) {
            return Color.parseColor("#FF9957");
        } else if (num > 300 && num <= 400) {
            return Color.parseColor("#FE5758");
        } else {
            return Color.parseColor("#E54FA8");
        }
    }

    class MainViewHolder {
        TextView city;
        TextView pm25;

        public MainViewHolder(View convertView) {
            city = (TextView) convertView.findViewById(R.id.textView_city);
            pm25 = (TextView) convertView.findViewById(R.id.textView_pm25);
        }
    }

    class ChartViewHolder {
        TextView textView_sortNum;
        TextView textView_sortCity;
        TextView textView_sortAQI;
        TextView textView_sortQuality;

        public ChartViewHolder(View v) {
            textView_sortNum = (TextView) v.findViewById(R.id.textView_sortNum);
            textView_sortCity = (TextView) v.findViewById(R.id.textView_sortCity);
            textView_sortAQI = (TextView) v.findViewById(R.id.textView_sortAQI);
            textView_sortQuality = (TextView) v.findViewById(R.id.textView_sortQuality);
        }
    }
}
