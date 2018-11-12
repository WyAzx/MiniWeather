package cn.edu.pku.wangyun.miniweather;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

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
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.wangyun.adapter.WeatherPagerAdapter;
import cn.edu.pku.wangyun.app.MyApplication;
import cn.edu.pku.wangyun.bean.City;
import cn.edu.pku.wangyun.bean.TodayWeather;
import cn.edu.pku.wangyun.fragments.FirstWeatherFragment;
import cn.edu.pku.wangyun.fragments.SecondWeatherFragment;
import cn.edu.pku.wangyun.indicator.CirclePageIndicator;
import cn.edu.pku.wangyun.util.NetUtil;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private ImageView mUpdateBtn;
    private ImageView mLocationBtn;
    private ProgressBar mUpdateProgress;

    private ImageView mCitySelect;

    private City mCurCity;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;
    private ViewPager mViewPager;
    private WeatherPagerAdapter mWeatherPagerAdapter;

    private List<Fragment> fragments;

    public LocationClient mLocationClient = null;

    private final static String TAG = "myWeather";

    private static final int LOACTION_OK = 0;
    private static final int UPDATE_TODAY_WEATHER = 1;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LOACTION_OK:
                    String cityName = (String) msg.obj;
                    MyApplication mApp = (MyApplication) getApplication();
                    mCurCity = mApp.getCity(cityName);
                    if(mCurCity != null) {
                        cityTv.setText(mCurCity.getCity());
                        queryWeatherCode(mCurCity.getNumber());
                        Log.d(TAG, "handleMessage: location " + cityName);
                    }else{
                        Log.d(TAG, "handleMessage: err"+cityName);
                    }
                    break;
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    mUpdateBtn.setVisibility(View.VISIBLE);
                    mUpdateProgress.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private BDLocationListener mLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d("location", "onReceiveLocation: ");
            mUpdateBtn.setVisibility(View.VISIBLE);
            mUpdateProgress.setVisibility(View.GONE);
            //定位失败反馈
//            if (location == null || TextUtils.isEmpty(location.getCity())) {
//                // T.showShort(getApplicationContext(), "location = null");
//                final Dialog dialog = IphoneDialog.getTwoBtnDialog(
//                        MainActivity.this, "定位失败", "是否手动选择城市?");
//                ((Button) dialog.findViewById(R.id.ok))
//                        .setOnClickListener(new View.OnClickListener() {
//
//                            @Override
//                            public void onClick(View v) {
//                                startActivityForResult();
//                                dialog.dismiss();
//                            }
//                        });
//                dialog.show();
//                return;
//            }
            String cityName = location.getCity();
            mLocationClient.stop();
            Message msg = mHandler.obtainMessage();
            msg.what = LOACTION_OK;
            msg.obj = cityName;
            mHandler.sendMessage(msg);// 更新天气
        }
    };

    // 初始化控件
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
        mUpdateProgress = findViewById(R.id.title_update_progress);
        mLocationClient = new LocationClient(getApplicationContext());

        // 未来天气viewpager
        fragments = new ArrayList<>();
        fragments.add(new FirstWeatherFragment());
        fragments.add(new SecondWeatherFragment());
        mWeatherPagerAdapter = new WeatherPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(mWeatherPagerAdapter);
        ((CirclePageIndicator) findViewById(R.id.indicator))
                .setViewPager(mViewPager);

        mUpdateBtn = findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        mCitySelect = findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        mLocationBtn = findViewById(R.id.title_location);
        mLocationBtn.setOnClickListener(this);


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

    // 根据TodayWeather更新天气
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

        // 更新未来天气
        if (fragments.size() > 0) {
            ((FirstWeatherFragment) mWeatherPagerAdapter.getItem(0))
                    .updateWeather(todayWeather);
            ((SecondWeatherFragment) mWeatherPagerAdapter.getItem(1))
                    .updateWeather(todayWeather);
        }
    }

    // 根据city code更新天气
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d(TAG, address);
        mUpdateBtn.setVisibility(View.GONE);
        mUpdateProgress.setVisibility(View.VISIBLE);
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

    // 转换天气XML 返回天气TodayWeather
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
                            } else if (xpp.getName().equals("fengxiang")) {
                                xpp.next();
                                switch (fengxiangCount) {
                                    case 0:
                                        tw.setFengxiang(xpp.getText());
                                        break;
                                    case 2:
                                        tw.setFengxiang2(xpp.getText());
                                        break;
                                    case 4:
                                        tw.setFengxiang3(xpp.getText());
                                        break;
                                    case 6:
                                        tw.setFengxiang4(xpp.getText());
                                        break;
                                    case 8:
                                        tw.setFengxiang5(xpp.getText());
                                        break;
                                }
                                fengxiangCount++;
                            } else if (xpp.getName().equals("fengli")) {
                                xpp.next();
                                switch (fengliCount) {
                                    case 0:
                                        tw.setFengli(xpp.getText());
                                        break;
                                    case 2:
                                        tw.setFengli2(xpp.getText());
                                        break;
                                    case 4:
                                        tw.setFengli3(xpp.getText());
                                        break;
                                    case 6:
                                        tw.setFengli4(xpp.getText());
                                        break;
                                    case 8:
                                        tw.setFengli5(xpp.getText());
                                        break;
                                }
                                fengliCount++;
                            } else if (xpp.getName().equals("date")) {
                                xpp.next();
                                switch (dateCount) {
                                    case 0:
                                        tw.setDate(xpp.getText());
                                        break;
                                    case 1:
                                        tw.setDate2(xpp.getText().substring(xpp.getText().length() - 3));
                                        break;
                                    case 2:
                                        tw.setDate3(xpp.getText().substring(xpp.getText().length() - 3));
                                        break;
                                    case 3:
                                        tw.setDate4(xpp.getText().substring(xpp.getText().length() - 3));
                                        break;
                                    case 4:
                                        tw.setDate5(xpp.getText().substring(xpp.getText().length() - 3));
                                        break;
                                }
                                dateCount++;
                            } else if (xpp.getName().equals("high")) {
                                xpp.next();
                                switch (highCount) {
                                    case 0:
                                        tw.setHigh(xpp.getText().substring(2).trim());
                                        break;
                                    case 1:
                                        tw.setHigh2(xpp.getText().substring(2).trim());
                                        break;
                                    case 2:
                                        tw.setHigh3(xpp.getText().substring(2).trim());
                                        break;
                                    case 3:
                                        tw.setHigh4(xpp.getText().substring(2).trim());
                                        break;
                                    case 4:
                                        tw.setHigh5(xpp.getText().substring(2).trim());
                                        break;
                                }
                                highCount++;
                            } else if (xpp.getName().equals("low")) {
                                xpp.next();
                                switch (lowCount) {
                                    case 0:
                                        tw.setLow(xpp.getText().substring(2).trim());
                                        break;
                                    case 1:
                                        tw.setLow2(xpp.getText().substring(2).trim());
                                        break;
                                    case 2:
                                        tw.setLow3(xpp.getText().substring(2).trim());
                                        break;
                                    case 3:
                                        tw.setLow4(xpp.getText().substring(2).trim());
                                        break;
                                    case 4:
                                        tw.setLow5(xpp.getText().substring(2).trim());
                                        break;
                                }
                                lowCount++;
                            } else if (xpp.getName().equals("type")) {
                                xpp.next();
                                switch (typeCount) {
                                    case 0:
                                        tw.setType(xpp.getText());
                                        break;
                                    case 2:
                                        tw.setWeather2(xpp.getText());
                                        break;
                                    case 4:
                                        tw.setWeather3(xpp.getText());
                                        break;
                                    case 6:
                                        tw.setWeather4(xpp.getText());
                                        break;
                                    case 8:
                                        tw.setWeather5(xpp.getText());
                                        break;
                                }
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

    // 初始化LocationClient
    private void initLocation() {
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                Log.d("location", "onReceiveLocation: ");
                mUpdateBtn.setVisibility(View.VISIBLE);
                mUpdateProgress.setVisibility(View.GONE);
                //定位失败反馈
//            if (location == null || TextUtils.isEmpty(location.getCity())) {
//                // T.showShort(getApplicationContext(), "location = null");
//                final Dialog dialog = IphoneDialog.getTwoBtnDialog(
//                        MainActivity.this, "定位失败", "是否手动选择城市?");
//                ((Button) dialog.findViewById(R.id.ok))
//                        .setOnClickListener(new View.OnClickListener() {
//
//                            @Override
//                            public void onClick(View v) {
//                                startActivityForResult();
//                                dialog.dismiss();
//                            }
//                        });
//                dialog.show();
//                return;
//            }
                String cityName = bdLocation.getCity();
                mLocationClient.stop();
                Message msg = mHandler.obtainMessage();
                msg.what = LOACTION_OK;
                msg.obj = cityName;
                mHandler.sendMessage(msg);// 更新天气
            }
        });

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(0);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedLocationDescribe(true);
        option.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);


        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d(TAG, "网络ok");
            Toast.makeText(MainActivity.this, "网络ok", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了", Toast.LENGTH_LONG).show();
        }

        initView();
        initLocation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_city_manager:
                Intent i = new Intent(this, SelectCity.class);
                startActivityForResult(i, 1);
                break;
            case R.id.title_update_btn:
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
                break;
            case R.id.title_location:
                if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                    if (!mLocationClient.isStarted())
                        mLocationClient.start();
                    mLocationClient.requestLocation();
                    Toast.makeText(MainActivity.this, "正在定位...", Toast.LENGTH_LONG).show();
                } else {
//                    T.showShort(this, R.string.net_err);
                    Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d(TAG, "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d(TAG, "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }
}
