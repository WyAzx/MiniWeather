package cn.edu.pku.wangyun.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.List;

import cn.edu.pku.wangyun.adapter.SearchCityAdapter;
import cn.edu.pku.wangyun.app.MyApplication;
import cn.edu.pku.wangyun.bean.City;
import cn.edu.pku.wangyun.adapter.CityListAdapter;


public class SelectCity extends Activity implements View.OnClickListener {
    private static final String TAG = "SelectCity";

    private ImageView mBackBtn;
    private ListView mCityList;
    private SearchView mCitySearch;
    private List<City> cityList;
    private SearchCityAdapter mSearchCityAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);
        initViews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode", "101160101");
                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }

    }

    private void initViews() {
        mBackBtn = findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mCityList = findViewById(R.id.city_list);
        MyApplication mApp = (MyApplication) getApplication();
        cityList = mApp.getmCityList();
        CityListAdapter adapter = new CityListAdapter(SelectCity.this,
                R.layout.city_item, cityList);
        mCityList.setAdapter(adapter);
        mCityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City c = (City) mCityList.getAdapter().getItem(position);
                Intent i = new Intent();
                i.putExtra("cityCode", c.getNumber());
                setResult(RESULT_OK, i);
                finish();
            }
        });

        mCitySearch = findViewById(R.id.city_search);
        mCitySearch.setQueryHint("请输入城市名称或拼音");
        mCitySearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return onQueryTextChange(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    mSearchCityAdapter = new SearchCityAdapter(SelectCity.this, cityList);
                    mCityList.setAdapter(mSearchCityAdapter);
                    mSearchCityAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

    }
}
