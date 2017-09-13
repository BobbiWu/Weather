package example.com.weather.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.io.IOException;

import example.com.weather.Until.HttpUntil;
import example.com.weather.Until.ParseUntil;
import example.com.weather.WeatherActivity;
import example.com.weather.gson.Weather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateService extends Service {
    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager  manger = (AlarmManager) getSystemService(ALARM_SERVICE);
       int  mytime=2*60*60*1000;
        long time = SystemClock.elapsedRealtime()+mytime;
        Intent i = new Intent(this, WeatherActivity.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manger.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,time,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherData = preferences.getString("weather", null);
        if (weatherData!=null){
            Weather weather = ParseUntil.weatherResponse(weatherData);
            String weatherId = weather.basic.weatherId;
            String url = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=0c039394cac14645808659073938e5e5";
            HttpUntil.sendRequset(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();
                    Weather weather = ParseUntil.weatherResponse(responseData);
                    if (weather!=null&&"ok".equals(weather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(UpdateService.this).edit();
                        editor.putString("weather", responseData);
                        editor.apply();
                    }
                }
            });
        }
    }
}
