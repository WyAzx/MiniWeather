package cn.edu.pku.wangyun.miniweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.edu.pku.wangyun.bean.TodayWeather;
import cn.edu.pku.wangyun.util.NetUtil;

public class MainActivity extends Activity implements View.OnClickListener {

    private ImageView mUpdateBtn;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private final static String TAG = "myWeather";

    private static final int UPDATE_TODAY_WEATHER = 1;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    void initView() {
        city_name_Tv = findViewById(R.id.title_city_name);
        cityTv = findViewById(R.id.city);
        timeTv = findViewById(R.id.time);
        humidityTv = findViewById(R.id.humidity);
        weekTv = findViewById(R.id.week_today);
        pmDataTv = findViewById(R.id.pm_data);
        pmQualityTv = findViewById(R.id.pm2_5_quality);
        pmImg = findViewById(R.id.pm2_5_img);
        temperatureTv = findViewById(R.id.temperature);
        climateTv = findViewById(R.id.climate);
        windTv = findViewById(R.id.wind);
        weatherImg = findViewById(R.id.weather_img);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
    }

    void updateTodayWeather(TodayWeather todayWeather) {
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:" + todayWeather.getFengli());
        Toast.makeText(MainActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
    }

    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d(TAG, address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather tw;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d(TAG, str);
                    }
                    String responseStr = response.toString();
                    Log.d(TAG, responseStr);
                    tw = parseXML(responseStr);
                    if (tw != null) {
                        Log.d(TAG, "run: " + tw.toString());
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = tw;
                        mHandler.sendMessage(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmlData) {
        TodayWeather tw = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = fac.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();
            Log.d(TAG, "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {

                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        if (xpp.getName().equals("resp")) {
                            tw = new TodayWeather();
                        }
                        if (tw != null) {
                            if (xpp.getName().equals("city")) {
                                xpp.next();
                                tw.setCity(xpp.getText());
                            } else if (xpp.getName().equals("updatetime")) {
                                xpp.next();
                                tw.setUpdatetime(xpp.getText());
                            } else if (xpp.getName().equals("shidu")) {
                                xpp.next();
                                tw.setShidu(xpp.getText());
                            } else if (xpp.getName().equals("wendu")) {
                                xpp.next();
                                tw.setWendu(xpp.getText());
                            } else if (xpp.getName().equals("pm25")) {
                                xpp.next();
                                tw.setPm25(xpp.getText());
                            } else if (xpp.getName().equals("quality")) {
                                xpp.next();
                                tw.setQuality(xpp.getText());
                            } else if (xpp.getName().equals("fengxiang") && fengxiangCount == 0) {
                                xpp.next();
                                tw.setFengxiang(xpp.getText());
                                fengxiangCount++;
                            } else if (xpp.getName().equals("fengli") && fengliCount == 0) {
                                xpp.next();
                                tw.setFengli(xpp.getText());
                                fengliCount++;
                            } else if (xpp.getName().equals("date") && dateCount == 0) {
                                xpp.next();
                                tw.setDate(xpp.getText());
                                dateCount++;
                            } else if (xpp.getName().equals("high") && highCount == 0) {
                                xpp.next();
                                tw.setHigh(xpp.getText().substring(2).trim());
                                highCount++;
                            } else if (xpp.getName().equals("low") && lowCount == 0) {
                                xpp.next();
                                tw.setLow(xpp.getText().substring(2).trim());
                                lowCount++;
                            } else if (xpp.getName().equals("type") && typeCount == 0) {
                                xpp.next();
                                tw.setType(xpp.getText());
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tw;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        mUpdateBtn = findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d(TAG, "网络ok");
            Toast.makeText(MainActivity.this, "网络ok", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了", Toast.LENGTH_LONG).show();
        }

        initView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_update_btn) {
            SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sp.getString("main_city_code", "101010100");
            Log.d(TAG, cityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d(TAG, "网络ok");
                queryWeatherCode(cityCode);
            } else {
                Log.d(TAG, "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了", Toast.LENGTH_LONG).show();
            }
        }
    }
}
