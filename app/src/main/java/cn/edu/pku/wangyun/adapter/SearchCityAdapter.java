package cn.edu.pku.wangyun.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.wangyun.bean.City;
import cn.edu.pku.wangyun.miniweather.R;

public class SearchCityAdapter extends BaseAdapter implements Filterable{

    private List<City> mAllCities;
    private List<City> mResultCities;
    private LayoutInflater mInflater;

    public SearchCityAdapter(Context context, List<City> AllCities) {
        this.mAllCities = AllCities;
        this.mInflater = LayoutInflater.from(context);
        this.mResultCities = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mResultCities.size();
    }

    @Override
    public Object getItem(int position) {
        return mResultCities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.city_item, null);
        }
        TextView cityName = convertView.findViewById(R.id.city_name);
        cityName.setText(mResultCities.get(position).getCity());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String str = constraint.toString().toUpperCase();
                FilterResults results = new FilterResults();
                List<City> cityList = new ArrayList<>();
                if (mAllCities != null && mAllCities.size() > 0) {
                    for (City city: mAllCities) {
                        if (city.getAllFristPY().contains(str)
                                || city.getAllPY().contains(str)
                                || city.getCity().contains(str) ) {
                            cityList.add(city);
                        }
                    }
                }
                results.values = cityList;
                results.count = cityList.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mResultCities = (ArrayList<City>) results.values;
                if (results.count > 0){
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
