package com.app.feng.PM25_displayer.Tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.app.feng.PM25_displayer.Activity.CityInfoActivity;
import com.app.feng.PM25_displayer.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by feng on 2015/9/12.
 */
public class CityListListener implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    Activity main;

    AlertDialog dialog;

    private ChangeColorAdapter adapter;
    private List<Map<String,String>> data;
    private SQLDataHandler sqlDataHandler;

    private int select;
    private String city;

    public CityListListener(Activity main, ChangeColorAdapter adapter, List<Map<String, String>> data, SQLDataHandler sqlDataHandler) {
        this.main = main;
        this.adapter = adapter;
        this.data = data;
        this.sqlDataHandler = sqlDataHandler;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, String> map = (HashMap<String,String>) adapter.getItem(position);
        select = position;
        city = map.get("city");
        Intent intent = new Intent();
        intent.setClass(main, CityInfoActivity.class);
        intent.putExtra("city", city);
        main.startActivityForResult(intent,190);

    }

    private void createEditMemu() {
        LayoutInflater inflater = LayoutInflater.from(main);
        View view = inflater.inflate(R.layout.edit_dialog_layout, null);
        Button button_delete = (Button) view.findViewById(R.id.button_delete);
        button_delete.setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setTitle(" ");
        builder.setIcon(android.R.drawable.ic_menu_edit);
        builder.setView(view);

        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_delete:
                //删除数据库
                sqlDataHandler.deletePm25List(city);
                //删除列表项
                data.remove(select);
                //刷新列表
                adapter.notifyDataSetChanged();
                //关闭对话框
                dialog.dismiss();
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, String> map = (HashMap<String,String>) adapter.getItem(position);
        select = position;
        city = map.get("city");
        createEditMemu();

        return true;
    }
}
