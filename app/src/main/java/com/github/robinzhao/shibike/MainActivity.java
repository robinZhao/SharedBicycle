package com.github.robinzhao.shibike;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.github.robinzhao.shibike.base.LogUtil;
import com.github.robinzhao.shibike.base.MessageHandler;
import com.github.robinzhao.shibike.base.PopInfoWindow;
import com.github.robinzhao.shibike.bike.BikeManager;
import com.github.robinzhao.shibike.routing.WalkingRoutingManager;
import com.github.robinzhao.shibike.search.MapSearchManager;


public class MainActivity extends AppCompatActivity implements BaiduMap.OnMapLoadedCallback {
    public static final String TITLE = "上海公共自行车";
    public static final String FIXING = "正在定位……";
    private static BitmapDescriptor icon;
    public LocationClient mLocationClient;
    public TextureMapView mapView;
    public BaiduMap baiduMap;
    public boolean loadFix = true;
    private MapStatus defaultZoomStatus;
    private boolean manualFix;
    private BikeManager bikeMananger;
    private WalkingRoutingManager walkingRoutingManager;
    private MapSearchManager mapSearchManager;
    private MessageHandler msgHandler;
    private com.github.robinzhao.shibike.base.MapStatus mapStatus = new com.github.robinzhao.shibike.base.MapStatus();
    private PopInfoWindow popInfoWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTitle(TITLE);
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        this.msgHandler=new MessageHandler(this);
        this.initBaiduMap();
        //初始LocationClient
        this.initLocation();
        ImageButton btn = (ImageButton) this.findViewById(R.id.fix_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualFix = true;
                msgHandler.showMsg(FIXING);
                mapSearchManager.clear();
                moveToMyPosition();
                mLocationClient.requestLocation();
            }
        });
        popInfoWindow = new PopInfoWindow(baiduMap, this);
        popInfoWindow.setGoClickListener(new PopInfoWindow.GoClickListener() {
            @Override
            public void goClick(ImageButton btn) {
                if (null != mapStatus.getCurrentBikeItem()) {
                    walkingRoutingManager.searchRouting(getLastLatLng(), mapStatus.getCurrentBikeItem().getMarkerOptions().getPosition());
                    baiduMap.hideInfoWindow();
                    mapStatus.setCurrentBikeItem(null);
                }
            }
        });
        bikeMananger = new BikeManager(baiduMap, popInfoWindow, this.msgHandler, this.mapStatus);
        this.defaultZoomStatus = new MapStatus.Builder().zoom(17).build();
        baiduMap.setOnMapLoadedCallback(this);
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(defaultZoomStatus));
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                bikeMananger.onMarkerClick(marker);
                mapSearchManager.onMarkerClick(marker);
                return true;
            }
        });
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                baiduMap.hideInfoWindow();
                reStoreView();
            }
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        this.walkingRoutingManager = new WalkingRoutingManager(baiduMap, msgHandler, (TextView) findViewById(R.id.route_info), mapStatus);
        this.mapSearchManager = new MapSearchManager(this.baiduMap, this.mLocationClient, this);
    }

    /**
     * 向地图添加Marker点
     */
    public void addMarkers() {
        this.msgHandler.showMsg("开始加载网点信息...");
        this.bikeMananger.load();
    }

    public void refreshMarks() {
        this.msgHandler.showMsg("正在刷新网点信息...");
        this.baiduMap.hideInfoWindow();
        this.bikeMananger.refresh();
    }

    @Override
    public void onMapLoaded() {
        addMarkers();
    }

    public void initBaiduMap() {
        mapView = (TextureMapView) this.findViewById(R.id.bmapView);
        mapView.showZoomControls(true);
        baiduMap = mapView.getMap();
        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
    }

    public LatLng getLastLatLng() {
        BDLocation loc = this.mLocationClient.getLastKnownLocation();
        if (null == loc) return null;
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setNeedDeviceDirect(true);//需要本机确定方向
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                fixLocation(bdLocation);
            }
        });    //注册监听函数
    }

    public void startLocation() {
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }

    public void stopLocation() {
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }

    public void fixLocation(BDLocation location) {
        LogUtil.log(location);
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .direction(location.getDirection()).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        // 设置定位数据
        baiduMap.setMyLocationData(locData);
        if (this.loadFix || this.manualFix) {
            String prefix = "网络";
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                prefix = "GPS";
            }
            if (location.getLocType() == BDLocation.TypeCacheLocation) {
                prefix = "缓存";
            }
            msgHandler.showMsg(prefix + "定位成功");
        }
        if (this.loadFix) {
            this.moveToMyPosition();
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null);
            baiduMap.setMyLocationConfigeration(config);
        }
        this.completeLocation();
    }

    public void moveTo(double latitude, double longitude) {
        LatLng ll = new LatLng(latitude, longitude);
        MapStatusUpdate loc = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(loc);
    }

    public void moveToMyPosition() {
        MyLocationData locData = baiduMap.getLocationData();
        if (null != locData)
            this.moveTo(locData.latitude, locData.longitude);
    }

    public void completeLocation() {
        this.loadFix = false;
        this.manualFix = false;
    }

    public void reStoreView() {
        walkingRoutingManager.clear();
        if (null != mapStatus&&null!=mapStatus.getLastStatus()) {
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus.getLastStatus()));
            mapStatus.setLastStatus(null);
        }
    }

    @Override
    protected void onDestroy() {
        this.mapSearchManager.destroy();
        this.walkingRoutingManager.destory();
        this.bikeMananger.destory();
        this.stopLocation();
        this.baiduMap.setMyLocationEnabled(false);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        this.mapView.onResume();
        this.startLocation();
        super.onResume();
    }

    @Override
    protected void onPause() {
        this.bikeMananger.pause();
        this.stopLocation();
        this.mapView.onPause();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        this.mapSearchManager.setSearchView(searchView);
        // searchView.setBackgroundColor(getResources().getColor(R.color.white));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
                break;
            case R.id.refresh:
                this.refreshMarks();
                break;
            case R.id.search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            this.mapSearchManager.clear();
            this.mapSearchManager.search(mLocationClient.getLastKnownLocation().getCity(), query);
        }
    }
}