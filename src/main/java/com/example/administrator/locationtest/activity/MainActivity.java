package com.example.administrator.locationtest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.locationtest.R;
import com.example.administrator.locationtest.util.HttpCallbackListener;
import com.example.administrator.locationtest.util.HttpUtil;
import com.example.administrator.locationtest.util.IsNetworkAvailable;
import com.example.administrator.locationtest.util.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements View.OnClickListener {

    private String locationCityName;

    private Button open_citylist_Button;
    private Button refresh_weatherButton;

    private TextView cityNametextView;
    private TextView pmtextView;
    private TextView air_quslitytextView;
    private TextView weathertextView1;
    private TextView windtextView1;
    private TextView temptextView1;

    private TextView daytextView2;
    private TextView weathertextView2;
    private TextView windtextView2;
    private TextView temptextView2;

    private TextView daytextView3;
    private TextView weathertextView3;
    private TextView windtextView3;
    private TextView temptextView3;

    private TextView daytextView4;
    private TextView weathertextView4;
    private TextView windtextView4;
    private TextView temptextView4;

    private TextView current_datetextView;
    private TextView weather_desptextView;
    private TextView temptextView;

    private LocationManager locationManager;
    private Location location;
    private String provider;

    private Context context;

    // String[] count = new String[]{cityName, dateDay, temperature, wind, pm, weather, time}
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String[] count = (String[]) msg.obj;
                    cityNametextView.setText(count[0]);               //城市名
                    temptextView.setText(getTempNow(count[1]));       //实时温度
                    windtextView1.setText(count[3]);                   //微风
                    temptextView1.setText(count[2]);                 //   温度范围
                    weathertextView1.setText(count[5]);             //  天气
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
                case 1:
                    String[] count2 = (String[]) msg.obj;
                    daytextView2.setText(count2[1]);
                    windtextView2.setText(count2[3]);
                    temptextView2.setText(count2[2]);
                    weathertextView2.setText(count2[5]);
                    break;
                case 2:
                    String[] count3 = (String[]) msg.obj;
                    daytextView3.setText(count3[1]);
                    windtextView3.setText(count3[3]);
                    temptextView3.setText(count3[2]);
                    weathertextView3.setText(count3[5]);
                    break;
                case 3:
                    String[] count4 = (String[]) msg.obj;
                    daytextView4.setText(count4[1]);
                    windtextView4.setText(count4[3]);
                    temptextView4.setText(count4[2]);
                    weathertextView4.setText(count4[5]);
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
        SharedPreferences preferences1 = PreferenceManager.getDefaultSharedPreferences(this);

        //添加城市
        open_citylist_Button = (Button) findViewById(R.id.open_city_list);
        open_citylist_Button.setOnClickListener(this);
        //刷新
        refresh_weatherButton = (Button) findViewById(R.id.refresh_weather);
        refresh_weatherButton.setOnClickListener(this);
        cityNametextView = (TextView) findViewById(R.id.city_name);
        pmtextView = (TextView) findViewById(R.id.pm);
        air_quslitytextView = (TextView) findViewById(R.id.air_quality);

        weathertextView1 = (TextView) findViewById(R.id.weather1);
        windtextView1 = (TextView) findViewById(R.id.wind1);
        temptextView1 = (TextView) findViewById(R.id.temp1);

        daytextView2 = (TextView) findViewById(R.id.day_data2);
        weathertextView2 = (TextView) findViewById(R.id.weather2);
        windtextView2 = (TextView) findViewById(R.id.wind2);
        temptextView2 = (TextView) findViewById(R.id.temp2);

        daytextView3 = (TextView) findViewById(R.id.day_data3);
        weathertextView3 = (TextView) findViewById(R.id.weather3);
        windtextView3 = (TextView) findViewById(R.id.wind3);
        temptextView3 = (TextView) findViewById(R.id.temp3);

        daytextView4 = (TextView) findViewById(R.id.day_data4);
        weathertextView4 = (TextView) findViewById(R.id.weather4);
        windtextView4 = (TextView) findViewById(R.id.wind4);
        temptextView4 = (TextView) findViewById(R.id.temp4);

        current_datetextView = (TextView) findViewById(R.id.current_date);
        weather_desptextView = (TextView) findViewById(R.id.weather_desp);
        temptextView = (TextView) findViewById(R.id.temp);
        if (IsNetworkAvailable.isNetworkAvailable(context)) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            provider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                getMapandCityName(location);
                locationCityName = preferences1.getString("locationCityName", "");
                getWind(locationCityName);
            }
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            setTextview(preferences.getString("locationCityName", ""));
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
                if (IsNetworkAvailable.isNetworkAvailable(context)) {
                    Toast.makeText(getApplicationContext(), "正在刷新...", Toast.LENGTH_SHORT).show();
                    getWind((String) cityNametextView.getText());
                    Toast.makeText(getApplicationContext(), "刷新成功", Toast.LENGTH_SHORT).show();
                    Log.i("TAG", "刷新");
                } else {
                    Toast.makeText(getApplicationContext(), "没有网络,无法刷新", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //得到实时气温
    private String getTempNow(String dateDay) {
        String dateDa = dateDay.substring(0, dateDay.length() - 1);
        String[] array = dateDa.split("：");
        return array[1];
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    final String citydata = data.getStringExtra("add");
                    getWind(citydata);
                }
                break;
            default:
        }
    }

    private void setTextview(String cityName) {
        SharedPreferences preferences = context.getSharedPreferences(cityName, 0);
        cityNametextView.setText(cityName);
        String a = preferences.getString("daytextView0", "");
        Log.i("AAA", a);
        if (a != null) {
            temptextView.setText(getTempNow(a));     //实时温度
        }
        weathertextView1.setText(preferences.getString("weather_desptextView0", ""));
        windtextView1.setText(preferences.getString("windtextView0", ""));
        temptextView1.setText(preferences.getString("temptextView0", ""));
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

        daytextView2.setText(preferences.getString("daytextView1", ""));
        weathertextView2.setText(preferences.getString("weather_desptextView1", ""));
        windtextView2.setText(preferences.getString("windtextView1", ""));
        temptextView2.setText(preferences.getString("temptextView1", ""));

        daytextView3.setText(preferences.getString("daytextView2", ""));
        weathertextView3.setText(preferences.getString("weather_desptextView2", ""));
        windtextView3.setText(preferences.getString("windtextView2", ""));
        temptextView3.setText(preferences.getString("temptextView2", ""));

        daytextView4.setText(preferences.getString("daytextView3", ""));
        weathertextView4.setText(preferences.getString("weather_desptextView3", ""));
        windtextView4.setText(preferences.getString("windtextView3", ""));
        temptextView4.setText(preferences.getString("temptextView3", ""));
    }

    //根据地图API得到JSON数据
    public void getMapandCityName(final Location location) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        final SharedPreferences.Editor editor = preferences.edit();
        final StringBuilder url = new StringBuilder();
        url.append("http://api.map.baidu.com/geocoder?location=");
        url.append(location.getLatitude()).append(",");
        url.append(location.getLongitude());
        url.append("&output=json&ak=hr21Empz3qHSW4rfHS9BmHHr&" +"mcode=1C:6B:42:33:E8:A6:DC:A2:11:6E:26:EC:84:BD:42:E3:8E:6B:57:9A;" +  "com.example.administrator.locationtest.util");
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

    //根据天气API得到JSON数据，然后解析天气并储存
    public void getWind(final String cityName) {
        String url = "http://api.map.baidu.com/telematics/v3/weather?location=" + cityName
                + "&output=json&ak=8ixCCFzlBB617YX7tONI2P5B&" +
                "mcode=1C:6B:42:33:E8:A6:DC:A2:11:6E:26:EC:84:BD:42:E3:8E:6B:57:9A;" +
                "com.example.administrator.locationtest.util";
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences(cityName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
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
                        Log.i("TAGPM", "aaaaa" + pm + "zheshipm");
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

    private void setAirQuslity(int pm) {
        if (pm > 0 && pm <= 35) {
            air_quslitytextView.setText("空气质量：优");
        } else if (pm > 35 && pm <= 75l) {
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
}