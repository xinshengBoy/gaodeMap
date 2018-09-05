package com.yks.gaodemap;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ZoomControls;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述：集成高德地图
 * 作者：zzh
 * 开发文档：https://lbs.amap.com/api/android-sdk/guide/create-project/android-studio-create-project
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private MapView map_view;
    private Spinner sp_type;
    private Button btn_screen_shot;
    private AMap aMap;
    private String[] mapTypeName = {"标准地图", "卫星地图", "夜景地图", "导航地图", "交通地图"};
    private String[] mapTypes = {"1", "2", "3", "4", "5"};
    private List<Map<String, String>> mapList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState) {
        sp_type = findViewById(R.id.sp_type);
        btn_screen_shot = findViewById(R.id.btn_screen_shot);
        map_view = findViewById(R.id.map_view);
        map_view.onCreate(savedInstanceState);//初始化后第一时间调用这个方法
        if (aMap == null) {
            aMap = map_view.getMap();
        }
        initLocation();
        initMapType();
        //设置spinner的值
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mapTypeName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_type.setAdapter(adapter);
        sp_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String typeName = sp_type.getItemAtPosition(i).toString();//根据spinner所选的切换到不同的地图类型
                for (int j = 0; j < mapList.size(); j++) {
                    Map<String, String> map = mapList.get(i);
                    if (map.get("name").equals(typeName)) {
                        int type = Integer.parseInt(map.get("type"));
                        switch (type) {
                            case 1://标准地图
                                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                                break;
                            case 2://卫星地图
                                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                                break;
                            case 3://夜景地图
                                aMap.setMapType(AMap.MAP_TYPE_NIGHT);
                                break;
                            case 4://导航地图
                                aMap.setMapType(AMap.MAP_TYPE_NAVI);
                                break;
                            case 5://交通地图
                                aMap.setTrafficEnabled(true);
                                break;
                            default:
                                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn_screen_shot.setOnClickListener(this);

    }

    /**
     * 描述：初始化定位，自动定位到当前的位置
     * 作者：zzh
     */
    private void initLocation(){
        MyLocationStyle style;
        style = new MyLocationStyle();//初始化定位蓝点的样式
        style.interval(2000);//定位刷新间隔
        style.showMyLocation(true);//显示我的位置
        style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。（1秒1次定位）
        aMap.setMyLocationStyle(style);//设置定位蓝点style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//显示定位图标
        aMap.setMyLocationEnabled(true);//是否显示定位蓝点
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));//缩放比
        style.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);//连续定位
        aMap.showIndoorMap(true);//是否显示室内地图

        //todo 控件交互
        UiSettings settings = aMap.getUiSettings();
        settings.setZoomControlsEnabled(true);//是否显示缩放按钮,默认显示
        settings.setZoomPosition(ZoomControls.SCROLLBAR_POSITION_DEFAULT);//缩放按钮显示的位置
        settings.setCompassEnabled(true);//是否显示指南针
        settings.setScaleControlsEnabled(true);//是否显示比例尺
        settings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_CENTER);//logo显示的位置（logo不能移除）
        //todo 手势交互（一下手势默认都是开启的）
        settings.setZoomGesturesEnabled(true);//缩放手势
        settings.setScrollGesturesEnabled(true);//滑动手势
        settings.setRotateGesturesEnabled(true);//旋转手势
        settings.setTiltGesturesEnabled(true);//倾斜手势
        settings.setAllGesturesEnabled(true);//所有手势


    }

    /**
     * 描述：初始化地图类型
     * 作者：zzh
     */
    private void initMapType(){
        for (int i=0;i<mapTypeName.length;i++){
            Map<String,String> map = new HashMap<>();
            map.put("name",mapTypeName[i]);
            map.put("type",mapTypes[i]);
            mapList.add(map);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btn_screen_shot){
            DistrictSearch search = new DistrictSearch(MainActivity.this);
            DistrictSearchQuery query = new DistrictSearchQuery();
            query.setKeywords("龙岗区");//关键字
            query.setShowBoundary(true);//是否返回边界值
            search.setQuery(query);
            search.setOnDistrictSearchListener(new DistrictSearch.OnDistrictSearchListener() {
                @Override
                public void onDistrictSearched(DistrictResult districtResult) {
                    if (districtResult != null && districtResult.getDistrict() != null){
                        if (districtResult.getAMapException().getErrorCode() == com.amap.api.services.core.AMapException.CODE_AMAP_SUCCESS){
                            ArrayList<DistrictItem> list = districtResult.getDistrict();
                            if (list != null && list.size() > 0){
//                                DistrictItem item = list.get()
                            }
                        }
                    }


                    String s = "dddd";
                }
            });
            search.searchDistrictAsyn();//开始搜索
        }
    }

    private void getDistrictItem(ArrayList<DistrictItem> list,int id){
        DistrictItem item = list.get(id);
    }

    private void updatePosition(Location location){
        LatLng pos = new LatLng(location.getLatitude(),location.getLongitude());
        CameraUpdate cu = CameraUpdateFactory.changeLatLng(pos);
        aMap.moveCamera(cu);
        aMap.clear();
        MarkerOptions options = new MarkerOptions();
        options.position(pos);
        Marker marker = aMap.addMarker(options);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map_view.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map_view.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map_view.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map_view.onDestroy();
    }
}
