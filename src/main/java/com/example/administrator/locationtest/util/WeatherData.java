package com.example.administrator.locationtest.util;

/**
 * Created by Administrator on 2016/3/30.
 */
public class WeatherData {
    private String date;
    private String weather;
    private String wind;
    private String temp;

    public WeatherData(String date, String weather, String wind, String temp) {
        this.date = date;
        this.weather = weather;
        this.wind = wind;
        this.temp = temp;
    }

    public String getDate() {
        return date;
    }

    public String getTemp() {
        return temp;
    }

    public String getWind() {
        return wind;
    }

    public String getWeather() {
        return weather;
    }
}
