package cn.edu.pku.wangyun.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.wangyun.bean.City;
import cn.edu.pku.wangyun.db.CityDB;
import cn.edu.pku.wangyun.miniweather.R;

public class MyApplication extends Application {
    private static final String TAG = "MyAPP";

    private static MyApplication mApplication;

    private CityDB mCityDB;

    private List<City> mCityList;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MyApplication");

        mApplication = this;

        mCityDB = openCityDB();
        initCityList();
    }

    public static MyApplication getInstance() {
        return mApplication;
    }

    // 打开数据库
    private CityDB openCityDB() {
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG, path);
        if (!db.exists()) {
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if (!dirFirstFolder.exists()) {
                dirFirstFolder.mkdirs();
                Log.i(TAG, "mkdirs");
            }
            Log.i(TAG, "db is not exists");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }

    private void initCityList() {
        mCityList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }

    // 初始化city list
    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();
        int i = 0;
        for (City city : mCityList) {
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            Log.d(TAG, "prepareCityList: " + cityCode + ":" + cityName);
        }
        Log.d(TAG, "prepareCityList: i=" + i);
        return true;
    }

    public List<City> getmCityList() {
        return mCityList;
    }

    public City getCity(String cityName) {
        return mCityDB.getCity(cityName);
    }

    public int getWeatherIcon(String weather2) {
        return R.drawable.biz_plugin_weather_qing;
    }
}
