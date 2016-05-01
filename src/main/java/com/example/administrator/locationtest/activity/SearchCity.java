package com.example.administrator.locationtest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.administrator.locationtest.R;
import com.example.administrator.locationtest.adapter.SearchAdapter;
import com.example.administrator.locationtest.util.IsNetworkAvailable;
import com.example.administrator.locationtest.util.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class SearchCity extends Activity implements SearchView.OnQueryTextListener {


    private final static String fileName = "cityjson";

    private SearchView searchView;
    List<String> cityName = new ArrayList<String>();

    private SearchAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_city);
        searchView = (SearchView) findViewById(R.id.search_view);
        // 为该SearchView组件设置事件监听器
        searchView.setOnQueryTextListener(this);
        // 设置该SearchView显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        listView = (ListView) findViewById(R.id.addcity_listview);
        // 开启过滤功能
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (IsNetworkAvailable.isNetworkAvailable(MyApplication.getContext())) {
                    String addcityName = SearchAdapter.data.get(position);
                    Intent intent = new Intent();
                    intent.putExtra("add", addcityName);
                    setResult(RESULT_OK, intent);
                    Toast.makeText(getApplicationContext(), addcityName, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "没有网络，无法查询。请检查网络！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getCity(getcityjsontoString(fileName));
        adapter = new SearchAdapter(this, cityName);
        listView.setAdapter(adapter);

    }

    private String getcityjsontoString(String filename) {
        StringBuilder response = new StringBuilder();
        try {
            InputStream in = getResources().getAssets().open(filename);
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    private void getCity(String adress) {
        try {
            JSONArray jsonArray = new JSONObject(adress).getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String district = jsonObject.optString("district");
                cityName.add(district);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter instanceof Filterable) {
            Filter filter = ((Filterable) adapter).getFilter();
            if (newText == null || newText.length() == 0) {
                filter.filter(null);
            } else {
                filter.filter(newText);
            }
        }
        return true;
    }

}


