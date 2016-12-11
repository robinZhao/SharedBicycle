package com.github.robinzhao.shibike.bike;

import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Marker;
import com.github.robinzhao.shibike.base.MapStatus;
import com.github.robinzhao.shibike.base.MessageHandler;
import com.github.robinzhao.shibike.base.PopInfoWindow;

import java.util.List;

/**
 * Created by zhaoruibin on 2016/12/4.
 */

public class BikeManager implements BikeLoader.PointLoadedCallback {
    private static final String MSG_FAILUE = "自行车位置加载失败，请检查你的网络……";

    private final BaiduMap baiduMap;
    private BikeLoader bikeLoader;
    private BikeItemOverlay bikeItemOverlay;
    private MessageHandler msgHandler;
    private MapStatus status;

    public BikeManager(final BaiduMap baiduMap, final PopInfoWindow infoWindow, final MessageHandler msgHandler, final MapStatus status) {
        this.baiduMap = baiduMap;
        this.msgHandler = msgHandler;
        //  this.popInfoWindow=infoWindow;
        this.bikeItemOverlay = new BikeItemOverlay(baiduMap);
        bikeItemOverlay.setOnMarkerClickListeners(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                if (null == marker.getExtraInfo()) return true;
                BikeManager.this.status.setCurrentBikeItem((BikeItem) marker.getExtraInfo().get("bikeItem"));
                if (null == BikeManager.this.status.getCurrentBikeItem())
                    return true;
                infoWindow.show(BikeManager.this.status.getCurrentBikeItem().toString(), marker.getPosition());
                return true;
            }
        });
        this.bikeLoader = new BikeLoader();
        bikeLoader.setPointLoadedCallback(this);
        this.status = status;
    }

    public void onMarkerClick(Marker marker) {
        this.bikeItemOverlay.onMarkerClick(marker);
    }

    @Override
    public void pointLoaded(List<BikeItem> points) {
        bikeItemOverlay.setData(points);
        msgHandler.showMsg("加载成功");
    }

    @Override
    public void error(Exception e) {
        Log.i(MSG_FAILUE, Log.getStackTraceString(e));
        msgHandler.showMsg(MSG_FAILUE);
    }

    @Override
    public void refreshed(List<BikeItem> points) {
        baiduMap.hideInfoWindow();
        bikeItemOverlay.setData(points);
        msgHandler.hideProgress();
        msgHandler.showMsg("刷新成功……");
    }

    public void destory() {
        this.bikeItemOverlay.stopRefreshTask();
    }

    public void pause() {
        this.bikeItemOverlay.stopRefreshTask();
    }

    public void load() {
        this.bikeLoader.load();
    }

    public void refresh() {
        this.bikeLoader.refresh();
    }


}
