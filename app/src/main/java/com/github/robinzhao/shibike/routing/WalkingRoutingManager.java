package com.github.robinzhao.shibike.routing;

import android.app.ProgressDialog;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.github.robinzhao.shibike.R;
import com.github.robinzhao.shibike.base.MapStatus;
import com.github.robinzhao.shibike.base.MessageHandler;
import com.github.robinzhao.shibike.base.OnGetRoutePlanResultListenerAdapter;
import com.github.robinzhao.shibike.base.OverlayManager;

/**
 * Created by zhaoruibin on 2016/11/7.
 */

public class WalkingRoutingManager extends OnGetRoutePlanResultListenerAdapter {
    ProgressDialog progress = null;
    RoutePlanSearch search = null;
    OverlayManager routeOverlay;
    private TextView routeInfo;
    BaiduMap baiduMap;
    MessageHandler msgHandler;
    MapStatus status;

    public WalkingRoutingManager(BaiduMap baiduMap,MessageHandler msgHandler,TextView routeInfo,MapStatus status) {
        this.baiduMap=baiduMap;
        this.routeInfo=routeInfo;
        this.msgHandler=msgHandler;
        this.search = RoutePlanSearch.newInstance();
        this.status=status;
        search.setOnGetRoutePlanResultListener(this);
    }

    public void searchRouting(LatLng start, LatLng end) {
        msgHandler.showProgress("查找路线请稍侯...");
        search.walkingSearch(new WalkingRoutePlanOption()
                .from(PlanNode.withLocation(start)).to(PlanNode.withLocation(end)));
    }

    public void destory(){
        this.search.destroy();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        msgHandler.hideProgress();
        if (null!=walkingRouteResult&&walkingRouteResult.getRouteLines().isEmpty()) {
            msgHandler.showMsg("没有找到路线...");
            return;
        }
        if (null != routeOverlay) {
            routeOverlay.removeFromMap();
        } else {
            routeOverlay = new MyWalkingRouteOverlay(baiduMap);
        }
        RouteLine rl = walkingRouteResult.getRouteLines().get(0);
        ((MyWalkingRouteOverlay) routeOverlay).setData(walkingRouteResult.getRouteLines().get(0));
        routeOverlay.addToMap();
        status.setLastStatus(baiduMap.getMapStatus());
        routeOverlay.zoomToSpan();
        routeInfo.setVisibility(View.VISIBLE);
        routeInfo.setText("距离目的地" + rl.getDistance() + "米");
    }

    public void clear(){
        routeInfo.setText("");
        routeInfo.setVisibility(View.GONE);
        if (null != routeOverlay) {
            routeOverlay.removeFromMap();
        } else {
            return;
        }
    }

    class MyWalkingRouteOverlay extends WalkingRouteOverlay {
        private boolean useDefaultIcon=true;

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

}
