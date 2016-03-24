package com.example.administrator.locationtest.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/12.
 */
public class SearchAdapter extends BaseAdapter implements Filterable {
    private Context context;
    public static List<String> data;// 这个数据会改变
    public List<String> copyData;// 这是原始的数据

    public SearchAdapter(Context context, List<String> data) {
        super();
        this.context = context;
        this.data = data;
        copyData = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (null == convertView) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
        } else {
            view = convertView;
        }
        TextView tv = (TextView) view.findViewById(android.R.id.text1);
        tv.setText(data.get(position));
        return view;
    }

    private Filter myFilter;

    @Override
    public Filter getFilter() {
        if (null == myFilter) {
            myFilter = new MyFilter();
        }
        return myFilter;
    }

    class MyFilter extends Filter {
        // 定义过滤规则
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            List<String> newValues = new ArrayList<String>();
            String filterString = constraint.toString().trim()
                    .toLowerCase();

            // 如果搜索框内容为空，就恢复原始数据
            if (TextUtils.isEmpty(filterString)) {
                newValues = copyData;
            } else {
                // 过滤出新数据
                for (String str : copyData) {
                    if (str.toLowerCase().indexOf(filterString) != -1) {
                        newValues.add(str);
                    }
                }
            }

            results.values = newValues;
            results.count = newValues.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            data = (List<String>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();  // 通知数据发生了改变
            } else {
                notifyDataSetInvalidated(); // 通知数据失效
            }
        }
    }
}

