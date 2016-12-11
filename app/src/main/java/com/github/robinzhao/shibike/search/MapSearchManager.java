package com.github.robinzhao.shibike.search;

import android.app.Activity;
import android.app.SearchManager;
import android.database.MatrixCursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.github.robinzhao.shibike.R;

/**
 * Created by zhaoruibin on 2016/11/19.
 */

public class MapSearchManager implements
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener, SearchView.OnQueryTextListener {
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private BaiduMap mBaiduMap = null;
    private SearchView searchView;
    PoiOverlay poiOverlay;
    /**
     * 搜索关键字输入窗口
     */
    private String city = "上海市";
    private String keyWord = null;
    private int loadIndex = 0;
    private Activity mainActivity;
    LatLng center = new LatLng(39.92235, 116.380338);
    int radius = 500;
    LatLng southwest = new LatLng(39.92235, 116.380338);
    LatLng northeast = new LatLng(39.947246, 116.414977);
    LatLngBounds searchbound = new LatLngBounds.Builder().include(southwest).include(northeast).build();
    LocationClient locationClient;
    int searchType = 0;  // 搜索的类型，在显示时区分

    public MapSearchManager(BaiduMap mBaiduMap, LocationClient locationClient, Activity mainActivity) {
        this.mainActivity = mainActivity;
        this.locationClient = locationClient;
        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);


        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        this.mBaiduMap = mBaiduMap;
        this.poiOverlay = new MyPoiOverlay(mBaiduMap);
    }

    public void setSearchView(SearchView view) {
        this.searchView = view;
        view.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(view.getContext(), R.layout.suggest_item,
                null, new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2},
                new int[]{R.id.textview1, R.id.textview2}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.searchView.setQuery(query, false);
        // new SearchRecentSuggestions(this.mainActivity, MyRecentSuggestions.AUTHORITY, MyRecentSuggestions.MODE).saveRecentQuery(query, null);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (null == newText || newText.length() <3) {
            return true;
        }
        SuggestionSearchOption option = new SuggestionSearchOption();
        option.keyword(newText);
        BDLocation location = this.locationClient.getLastKnownLocation();
        option.city("上海市");
        if (null != location && null != location.getCity()) {
            option.city(location.getCity());
            option.location(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        this.mSuggestionSearch.requestSuggestion(option);
        return true;
    }

    public void destroy() {
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
    }

    /**
     * 响应城市内搜索按钮点击事件
     *
     * @param
     */
    public void search() {
        if(null==city)city="上海市";
        searchType = 1;
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(city).keyword(keyWord).pageNum(loadIndex));
    }

    public void search(String city, String keyWord) {
        if (null != city) {
            this.city = city;
        }
        this.keyWord = keyWord;
        this.search();
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    /**
     * 响应周边搜索按钮点击事件
     *
     * @param v
     */
    public void searchNearbyProcess(View v) {
        searchType = 2;
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption().keyword(keyWord).sortType(PoiSortType.distance_from_near_to_far).location(center)
                .radius(radius).pageNum(loadIndex);
        mPoiSearch.searchNearby(nearbySearchOption);
    }

    public void clear() {
        this.poiOverlay.removeFromMap();
    }

    /**
     * 获取POI搜索结果，包括searchInCity，searchNearby，searchInBound返回的搜索结果
     *
     * @param result
     */
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(this.mainActivity, "未找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            this.poiOverlay.setData(result);
            this.poiOverlay.addToMap();
            this.poiOverlay.zoomToSpan();

            switch (searchType) {
                case 2:
                    showNearbyArea(center, radius);
                    break;
                case 3:
                    showBound(searchbound);
                    break;
                default:
                    break;
            }

            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(this.mainActivity, strInfo, Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * 获取POI详情搜索结果，得到searchPoiDetail返回的搜索结果
     *
     * @param result
     */
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this.mainActivity, "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(this.mainActivity, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    /**
     * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
     *
     * @param res
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }//

        //  MatrixCursor c1= (MatrixCursor)searchView.getSuggestionsAdapter().getCursor();
        String[] columns = new String[]{
                SearchManager.SUGGEST_COLUMN_FORMAT,
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_QUERY,
                "_ID"
        };
        MatrixCursor c1 = new MatrixCursor(columns);
        int idx = 1;
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                String searchStr = info.key;
                if (null != info.district && info.district.length() > 0) {
                    searchStr += ("," + info.district);
                }
                if (null != info.city && info.city.length() > 0) {
                    searchStr += ("," + info.city);
                }
                c1.addRow(new Object[]{null, null, info.key, searchStr, info.key, idx++});
            }
        }
        searchView.getSuggestionsAdapter().changeCursor(c1);
        searchView.getSuggestionsAdapter().notifyDataSetChanged();
    }

    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            // if (poi.hasCaterDetails) {
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                    .poiUid(poi.uid));
            // }
            return true;
        }
    }

    /**
     * 对周边检索的范围进行绘制
     *
     * @param center
     * @param radius
     */
    public void showNearbyArea(LatLng center, int radius) {
        BitmapDescriptor centerBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_geo);
        MarkerOptions ooMarker = new MarkerOptions().position(center).icon(centerBitmap);
        mBaiduMap.addOverlay(ooMarker);

        OverlayOptions ooCircle = new CircleOptions().fillColor(0xCCCCCC00)
                .center(center).stroke(new Stroke(5, 0xFFFF00FF))
                .radius(radius);
        mBaiduMap.addOverlay(ooCircle);
    }

    /**
     * 对区域检索的范围进行绘制
     *
     * @param bounds
     */
    public void showBound(LatLngBounds bounds) {
        BitmapDescriptor bdGround = BitmapDescriptorFactory
                .fromResource(R.drawable.ground_overlay);

        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
        mBaiduMap.addOverlay(ooGround);

        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(bounds.getCenter());
        mBaiduMap.setMapStatus(u);
        bdGround.recycle();
    }

    public void onMarkerClick(Marker marker){
        this.poiOverlay.onMarkerClick(marker);
    }
}
