package com.example.administrator.locationtest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.administrator.locationtest.R;
import com.example.administrator.locationtest.util.WeatherData;

import java.util.List;

/**
 * Created by Administrator on 2016/3/30.
 */
public class WeatherDataAdapter extends ArrayAdapter<WeatherData> {
    private int resourceId;

    public WeatherDataAdapter(Context context, int resource, List<WeatherData> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WeatherData weatherData = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.date = (TextView) view.findViewById(R.id.date);
            viewHolder.weather = (TextView) view.findViewById(R.id.weather);
            viewHolder.wind = (TextView) view.findViewById(R.id.wind);
            viewHolder.temp = (TextView) view.findViewById(R.id.temp);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.date.setText(weatherData.getDate());
        viewHolder.weather.setText(weatherData.getWeather());
        viewHolder.wind.setText(weatherData.getWind());
        viewHolder.temp.setText(weatherData.getTemp());
        return view;
    }

    class ViewHolder {
        TextView date;
        TextView weather;
        TextView wind;
        TextView temp;
    }
}
