package example.com.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import example.com.weather.Service.UpdateService;
import example.com.weather.Until.HttpUntil;
import example.com.weather.Until.ParseUntil;
import example.com.weather.gson.Forecast;
import example.com.weather.gson.Weather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private Button navButton;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    private TextView dirtext;
    private TextView sctext;
    private TextView tiganText,shiduText,waterText,presText,qltyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);//设置成透明
        }
        setContentView(R.layout.activity_weather);
        initView();
    }

    private void initView() {
        qltyText=(TextView)findViewById(R.id.tv_qlty);//空气质量
        tiganText=(TextView)findViewById(R.id.tv_tigan);//体感
        shiduText=(TextView)findViewById(R.id.tv_shidu);//湿度
        waterText=(TextView)findViewById(R.id.tv_wate);//降水量
        presText=(TextView)findViewById(R.id.tv_pres);//气压
        dirtext = (TextView) findViewById(R.id.tv_fenxiang);//风向
        sctext = (TextView) findViewById(R.id.tv_fenli);//风力
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);//背景图
        sportText = (TextView) findViewById(R.id.sport_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);//天气状况例如：多云，晴
        degreeText = (TextView) findViewById(R.id.degree_text);//度数
        titleUpdateTime = (TextView) findViewById(R.id.tv_updata_time);
        titleCity = (TextView) findViewById(R.id.tv_title_city);
        navButton = (Button) findViewById(R.id.nav_button);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);// 第一个参数是“键”，第二个参数是如果数据不存在默认的返回数据
        //判断是否有数据有直接解析，没有去请求
        final String weatherId;
        if (weatherString != null) {
            //解析
            Weather weather = ParseUntil.weatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        loadPic();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void loadPic() {
        String url="http://guolin.tech/api/bing_pic";
        HttpUntil.sendRequset(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String picurl = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(picurl).into(bingPicImg);
                    }
                });
            }
        });
    }

    /*
    * 请求数据
    * */
    public void requestWeather(final String weatherId) {
        String url = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=0c039394cac14645808659073938e5e5";
        Log.d("TAG", weatherId);
        HttpUntil.sendRequset(url, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                final Weather weatherResponse = ParseUntil.weatherResponse(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherResponse != null && "ok".equals(weatherResponse.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseData);
                            editor.apply();
                            showWeatherInfo(weatherResponse);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {
        String qlty = weather.aqi.city.qlty;//空气质量
        String tigan = weather.now.tigan;//体感
        String shidu = weather.now.shidu;//湿度
        String water = weather.now.water;//降水量
        String pres = weather.now.pres;//气压
        String dir = weather.now.feng.dir;//风向
        String sc = weather.now.feng.sc;//风力
        String cityName = weather.basic.cityName;
        Log.d("TAG", cityName);
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        qltyText.setText(qlty);
        tiganText.setText("体感 "+tigan+"°");
        shiduText.setText("湿度 "+shidu+"%");
        waterText.setText("降水 "+water+"mm");
        presText.setText("气压 "+pres+"p");
        dirtext.setText(dir);
        sctext.setText(sc+"级");
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max + "℃");
            minText.setText(forecast.temperature.min + "℃");
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);
    }

}
