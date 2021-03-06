package cn.edu.pku.wangyun.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.edu.pku.wangyun.app.MyApplication;
import cn.edu.pku.wangyun.bean.TodayWeather;
import cn.edu.pku.wangyun.miniweather.R;
import cn.edu.pku.wangyun.util.TimeUtil;

public class FirstWeatherFragment extends Fragment {
    private TextView weekTv1, weekTv2, weekTv3;
    private ImageView weather_imgIv1, weather_imgIv2, weather_imgIv3;
    private TextView temperatureTv1, temperatureTv2, temperatureTv3;
    private TextView climateTv1, climateTv2, climateTv3;
    private TextView windTv1, windTv2, windTv3;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.biz_plugin_weather_item,
                container, false);
        View view1 = view.findViewById(R.id.subitem1);
        View view2 = view.findViewById(R.id.subitem2);
        View view3 = view.findViewById(R.id.subitem3);

        weekTv1 = view1.findViewById(R.id.week);
        weekTv2 = view2.findViewById(R.id.week);
        weekTv3 = view3.findViewById(R.id.week);

        weather_imgIv1 = view1.findViewById(R.id.weather_img);
        weather_imgIv2 = view2.findViewById(R.id.weather_img);
        weather_imgIv3 = view3.findViewById(R.id.weather_img);
        temperatureTv1 = view1.findViewById(R.id.temperature);
        temperatureTv2 = view2.findViewById(R.id.temperature);
        temperatureTv3 = view3.findViewById(R.id.temperature);

        climateTv1 = view1.findViewById(R.id.climate);
        climateTv2 = view2.findViewById(R.id.climate);
        climateTv3 = view3.findViewById(R.id.climate);

        windTv1 = view1.findViewById(R.id.wind);
        windTv2 = view2.findViewById(R.id.wind);
        windTv3 = view3.findViewById(R.id.wind);
        return view;
    }

    public void updateWeather(TodayWeather weatherinfo) {
        if (weatherinfo != null) {
            weekTv1.setText(weatherinfo.getDate2());
            weekTv2.setText(weatherinfo.getDate3());
            weekTv3.setText(weatherinfo.getDate4());

            weather_imgIv1.setImageResource(MyApplication.getInstance()
                    .getWeatherIcon(weatherinfo.getWeather2()));
            weather_imgIv2.setImageResource(MyApplication.getInstance()
                    .getWeatherIcon(weatherinfo.getWeather3()));
            weather_imgIv3.setImageResource(MyApplication.getInstance()
                    .getWeatherIcon(weatherinfo.getWeather4()));
            climateTv1.setText(weatherinfo.getWeather2());
            climateTv2.setText(weatherinfo.getWeather3());
            climateTv3.setText(weatherinfo.getWeather4());

            String tem2 = weatherinfo.getLow2() + '~' + weatherinfo.getHigh2();
            String tem3 = weatherinfo.getLow3() + '~' + weatherinfo.getHigh3();
            String tem4 = weatherinfo.getLow4() + '~' + weatherinfo.getHigh4();
            temperatureTv1.setText(tem2);
            temperatureTv2.setText(tem3);
            temperatureTv3.setText(tem4);

            windTv1.setText(weatherinfo.getFengxiang2());
            windTv2.setText(weatherinfo.getFengxiang3());
            windTv3.setText(weatherinfo.getFengxiang4());
        } else {
            weather_imgIv1.setImageResource(R.drawable.na);
            weather_imgIv2.setImageResource(R.drawable.na);
            weather_imgIv3.setImageResource(R.drawable.na);

            climateTv1.setText("N/A");
            climateTv2.setText("N/A");
            climateTv3.setText("N/A");

            temperatureTv1.setText("N/A");
            temperatureTv2.setText("N/A");
            temperatureTv3.setText("N/A");

            windTv1.setText("N/A");
            windTv2.setText("N/A");
            windTv3.setText("N/A");
        }
    }

}