package cn.edu.pku.wangyun.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cn.edu.pku.wangyun.bean.City;
import cn.edu.pku.wangyun.miniweather.R;

public class CityListAdapter extends ArrayAdapter<City> {
    private int resourceId;

    public CityListAdapter(@NonNull Context context, int resourceId, List<City> objects) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        City city = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView cityName = view.findViewById(R.id.city_name);
        cityName.setText(city.getCity());
        return view;
    }
}
