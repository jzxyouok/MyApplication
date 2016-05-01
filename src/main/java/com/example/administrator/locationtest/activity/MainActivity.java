package com.example.administrator.locationtest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.locationtest.R;
import com.example.administrator.locationtest.adapter.WeatherDataAdapter;
import com.example.administrator.locationtest.util.DataCleanManager;
import com.example.administrator.locationtest.util.HttpCallbackListener;
import com.example.administrator.locationtest.util.HttpUtil;
import com.example.administrator.locationtest.util.IsNetworkAvailable;
import com.example.administrator.locationtest.util.WeatherData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends Activity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ListView listView;
    private List<WeatherData> weatherDataList = new ArrayList<WeatherData>();
    private WeatherDataAdapter adapter;

    private SharedPreferences preferences;

    private SwipeRefreshLayout mSwipeLayout;
    private String locationCityName;

    private Button open_citylist_Button;
    private Button refresh_weatherButton;

    private TextView cityNametextView;
    private TextView pmtextView;
    private TextView air_quslitytextView;
    private TextView current_datetextView;
    private TextView weather_desptextView;
    private TextView temptextView;
    private LocationManager locationManager;
    private Location location;
    private String provider;

    private Context context;
    private SharedPreferences.Editor editor;

    private LinearLayout layout;
    // String[] count = new String[]{cityName, dateDay, temperature, wind, pm, weather, time}
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String[] count = (String[]) msg.obj;
                    cityNametextView.setText(count[0]);               //城市名
                    temptextView.setText(getTempNow(count[1]));       //实时温度
                    String pm = count[4];                             //  PM2.5
                    if (pm.equals("")) {
                        air_quslitytextView.setText(" ");
                        pmtextView.setText("地区不是市级地区无法获得空气质量指数");
                    } else {
                        pmtextView.setText("空气质量指数：" + " " + count[4]);
                        int i = Integer.parseInt(pm);
                        setAirQuslity(i);
                    }
                    weather_desptextView.setText(count[5]);           //layout天气
                    current_datetextView.setText(count[6]);           //  年月日
                    break;
                case 4:
                    getWind((String) cityNametextView.getText());
                    mSwipeLayout.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), "刷新成功", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    layout.setBackgroundColor(R.drawable.background1);
                    break;
                case 7:
                    layout.setBackgroundColor(R.drawable.background2);
                    break;
                case 8:
                    layout.setBackgroundColor(R.drawable.background3);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        layout = (LinearLayout) findViewById(R.id.main_Layout);
        listView = (ListView) findViewById(R.id.list_view);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_purple, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeLayout.setDistanceToTriggerSync(200);
        mSwipeLayout.setProgressBackgroundColorSchemeResource(R.color.black);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //搜索城市
        open_citylist_Button = (Button) findViewById(R.id.open_city_list);
        open_citylist_Button.setOnClickListener(this);
        //刷新
        refresh_weatherButton = (Button) findViewById(R.id.refresh_weather);
        refresh_weatherButton.setOnClickListener(this);

        cityNametextView = (TextView) findViewById(R.id.city_name);
        pmtextView = (TextView) findViewById(R.id.pm);
        air_quslitytextView = (TextView) findViewById(R.id.air_quality);
        current_datetextView = (TextView) findViewById(R.id.current_date);
        weather_desptextView = (TextView) findViewById(R.id.weather_desp);
        temptextView = (TextView) findViewById(R.id.temp);

        if (IsNetworkAvailable.isNetworkAvailable(context)) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            provider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                getMapandCityName(location);
                locationCityName = preferences.getString("locationCityName", "");
                getWind(locationCityName);
                adapter = new WeatherDataAdapter(this, R.layout.item, weatherDataList);
                listView.setAdapter(adapter);
            }
        } else if (preferences.getString("locationCityName", "").equals("")) {
            Toast.makeText(context, "没有缓存数据，请连接网络！", Toast.LENGTH_LONG).show();
        } else {
            weatherDataList.clear();
            setTextdata(preferences.getString("locationCityName", ""));
            adapter = new WeatherDataAdapter(this, R.layout.item, weatherDataList);
            listView.setAdapter(adapter);
            Toast.makeText(getApplicationContext(), "没有网络", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_city_list:
                Intent intent = new Intent(getApplicationContext(), SearchCity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.refresh_weather:
                showMorehandleDialog();
                break;

        }
    }

    /**
     * 更多操作对话框
     */
    private void showMorehandleDialog() {
        final File file = new File("/data/data/com.example.administrator.locationtest/shared_prefs");
        Log.i("TAGAAA", getApplicationContext().getFilesDir().getAbsolutePath());
        try {
            final String fist = "清除缓存 " + DataCleanManager.getCacheSize(file);
            final String[] items = new String[]{fist, "更换背景颜色", "关于软件"};
            AlertDialog dialog = new AlertDialog.Builder(context).setTitle("请选择")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    DataCleanManager.cleanSharedPreference(context);
                                    Toast.makeText(MainActivity.this, "缓存数据已清空！", Toast.LENGTH_LONG).show();
                                    break;
                                case 1:
                                   // setBackColorDialog();
                                    handler.sendEmptyMessage(6);
                                    break;
                                case 2:
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context).setMessage("软件还有很多不足，以后会慢慢改进。谢谢你的使用！")
                                            .setPositiveButton("ok", null);
                                    alertDialog.show();
                                    break;
                            }
                        }
                    }).create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 选择颜色Dialog
     */
    private void setBackColorDialog() {
        String[] items = new String[]{"默认", "粉", "黄","绿"};
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("请选择背景颜色")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 1:
                                handler.sendEmptyMessage(6);
                                break;
                            case 2:
                                handler.sendEmptyMessage(7);
                                break;
                            case 3:
                                handler.sendEmptyMessage(8);
                                break;
                        }
                    }
                }).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.alpha = 0.5f; //透明度
        dialog.getWindow().setAttributes(params);
    }

    /**
     * 得到实时气温
     *
     * @param dateDay 周三 03月30日（实时：12°C）
     * @return 12°C
     */
    private String getTempNow(String dateDay) {
        String dateDa = dateDay.substring(0, dateDay.length() - 1);
        String[] array = dateDa.split("：");
        return array[1];
    }

    /**
     * 查询城市页面返回的数据
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    final String citydata = data.getStringExtra("add");
                    weatherDataList.clear();
                    getWind(citydata);
                }
                break;
            default:
        }
    }

    /**
     * 根据地图API得到JSON数据
     *
     * @param location 经纬度
     */
    public void getMapandCityName(final Location location) {
        //  SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();
        final StringBuilder url = new StringBuilder();
        url.append("http://api.map.baidu.com/geocoder?location=");
        url.append(location.getLatitude()).append(",");
        url.append(location.getLongitude());
        url.append("&output=json&ak=hr21Empz3qHSW4rfHS9BmHHr&"
                + "mcode=1C:6B:42:33:E8:A6:DC:A2:11:6E:26:EC:84:BD:42:E3:8E:6B:57:9A;"
                + "com.example.administrator.locationtest.util");
        HttpUtil.sendHttpRequest(url.toString(), new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response).getJSONObject("result");
                    JSONObject object = jsonObject.getJSONObject("addressComponent");
                    String cityName = object.optString("city");
                    editor.putString("locationCityName", cityName);
                    editor.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 根据天气API得到JSON数据，然后解析天气并储存
     *
     * @param cityName
     */
    public void getWind(final String cityName) {
        String url = "http://api.map.baidu.com/telematics/v3/weather?location=" + cityName
                + "&output=json&ak=8ixCCFzlBB617YX7tONI2P5B&" +
                "mcode=1C:6B:42:33:E8:A6:DC:A2:11:6E:26:EC:84:BD:42:E3:8E:6B:57:9A;" +
                "com.example.administrator.locationtest.util";
        //   final SharedPreferences.Editor editor = getSharedPreferences(cityName, 0).edit();
        editor = getSharedPreferences(cityName, 0).edit();
        HttpUtil.sendHttpRequest(url, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String time = jsonObject.optString("date");
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    if (jsonArray.length() > 0) {
                        JSONObject object = jsonArray.getJSONObject(0);
                        String pm = object.optString("pm25");
                        JSONArray array = object.getJSONArray("weather_data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject1 = array.getJSONObject(i);
                            String dateDay = jsonObject1.optString("date");    //星期几和实时温度
                            String weather = jsonObject1.optString("weather");
                            String wind = jsonObject1.optString("wind");
                            String temperature = jsonObject1.optString("temperature");
                            String[] count = new String[]{cityName, dateDay, temperature, wind, pm, weather, time};
                            Message message = new Message();
                            message.what = i;
                            message.obj = count;
                            handler.sendMessage(message);
                            WeatherData weatherData = new WeatherData(dateDay, weather, wind, temperature);
                            weatherDataList.add(weatherData);
                            editor.putString("daytextView" + i, dateDay);
                            editor.putString("temptextView" + i, temperature);
                            editor.putString("windtextView" + i, wind);
                            editor.putString("pmtextView" + i, pm);
                            editor.putString("weather_desptextView" + i, weather);
                            editor.putString("current_datetextView" + i, time);
                            editor.commit();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 根据PM2.5指数判断空气质量
     */
    private void setAirQuslity(int pm) {
        if (pm > 0 && pm <= 35) {
            air_quslitytextView.setText("空气质量：优");
        } else if (pm > 35 && pm <= 75) {
            air_quslitytextView.setText("空气质量：良");
        } else if (pm > 75 && pm <= 115) {
            air_quslitytextView.setText("空气质量：轻度污染");
        } else if (pm > 115 && pm <= 150) {
            air_quslitytextView.setText("空气质量：中度污染");
        } else if (pm > 150 && pm <= 250) {
            air_quslitytextView.setText("空气质量：重度污染");
        } else if (pm > 250) {
            air_quslitytextView.setText("空气质量：严重污染");
        }
    }

    /**
     * 下拉刷新后执行操作
     */
    @Override
    public void onRefresh() {
        if (IsNetworkAvailable.isNetworkAvailable(context)) {
            weatherDataList.clear();
            handler.sendEmptyMessageDelayed(4, 4000);
            // adapter.notifyDataSetChanged();
            Log.i("TAG", "刷新");
        } else {
            mSwipeLayout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), "没有网络,无法刷新", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 无网络状态下启动APP加载数据
     *
     * @param cityName
     */
    private void setTextdata(String cityName) {
        SharedPreferences preferences = context.getSharedPreferences(cityName, 0);
        cityNametextView.setText(cityName);
        String a = preferences.getString("daytextView0", "");
        if (a != null) {
            temptextView.setText(getTempNow(a));     //实时温度
        }
        String pm = preferences.getString("pmtextView0", "");
        if (pm.equals("")) {
            air_quslitytextView.setText(" ");
            pmtextView.setText("地区不是市级地区无法获得空气质量指数");
        } else {
            pmtextView.setText("空气质量指数:" + " " + pm);
            int i = Integer.parseInt(pm);
            setAirQuslity(i);
        }
        weather_desptextView.setText(preferences.getString("weather_desptextView0", ""));
        current_datetextView.setText(preferences.getString("current_datetextView0", ""));
        for (int i = 0; i < 4; i++) {
            String dateDay = preferences.getString("daytextView" + i, "");
            String weather = preferences.getString("weather_desptextView" + i, "");
            String wind = preferences.getString("windtextView" + i, "");
            String temperature = preferences.getString("temptextView" + i, "");
            WeatherData weatherData = new WeatherData(dateDay, weather, wind, temperature);
            weatherDataList.add(weatherData);
        }
    }
}