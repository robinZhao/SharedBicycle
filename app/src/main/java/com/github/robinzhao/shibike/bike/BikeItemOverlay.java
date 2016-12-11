package com.github.robinzhao.shibike.bike;

import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.github.robinzhao.shibike.base.OverlayManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 用于显示步行路线的overlay，自3.4.0版本起可实例化多个添加在地图中显示
 */
public class BikeItemOverlay extends OverlayManager implements BaiduMap.OnMapStatusChangeListener, BaiduMap.OnMarkerClickListener {
    private List<BikeItem> bikeItems = new LinkedList<BikeItem>();
    private Runnable refreshTask;
    private boolean stopRefresh=true;

    public BikeItemOverlay(BaiduMap baiduMap) {
        super(baiduMap);
        baiduMap.setOnMapStatusChangeListener(this);
    }


    public void setData(List<BikeItem> _bikeItems) {
        synchronized (this.bikeItems) {
            this.removeFromMap();
            this.bikeItems.clear();
            for (BikeItem m : _bikeItems) {
                try {
                    m.getMarkerOptions();
                    this.bikeItems.add(m);
                } catch (NumberFormatException e) {
                }
            }
        }
        this.addToMap();
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (bikeItems == null) {
            return null;
        }

        List<OverlayOptions> overlayList = new ArrayList<OverlayOptions>();
        if (!bikeItems.isEmpty()) {
            for (BikeItem item : bikeItems) {
                overlayList.add(item.getMarkerOptions());
            }
        }
        return overlayList;
    }

    public void asyncRenderBikes() {
        if(null!=mBaiduMap.getMapStatus()){
            this.asyncRenderBikes(mBaiduMap.getMapStatus());
        }
    }


    public synchronized void asyncRenderBikes(MapStatus status){
        this.setRefreshTask(status);
        if(stopRefresh){
            startRefreshTask();
        }else{
            this.notifyAll();
        }
    }

    public synchronized void stopRefreshTask(){
        stopRefresh=true;
        this.notifyAll();
    }

    private synchronized void startRefreshTask(){
        stopRefresh=false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!stopRefresh){
                    Runnable runnable=refreshTask;
                    synchronized (BikeItemOverlay.this){
                        if(runnable!=null) {
                            refreshTask = null;
                        }else{
                            try{
                                BikeItemOverlay.this.wait();
                            }catch (InterruptedException e){
                                stopRefresh=true;
                                Log.i("thread",Thread.currentThread().getName()+"interrupted restarting");
                            }
                            continue;
                        }
                    }
                    runnable.run();
                }
                Log.i("thread",Thread.currentThread().getName()+"stoped");
                stopRefresh=true;
            }
        }).start();
    }

    public synchronized void setRefreshTask(final MapStatus status){
        this.refreshTask=new Runnable() {
            @Override
            public void run() {
                synchronized (bikeItems) {
                    long current = System.currentTimeMillis();
                    boolean newtask = false;
                    for (BikeItem item : bikeItems) {
                        if (refreshTask != null) {
                            newtask=true;
                            Log.i(Thread.currentThread().getName()+"render线程退出", "render线程退出,因为有更新的线程出现");
                            break;
                        }
                        if (status.bound.contains(item.getMarkerOptions().getPosition())) {
                            item.add(mBaiduMap);
                            mOverlayList.add(item.marker);
                        } else {
                            item.remove();
                            mOverlayList.remove(item.marker);
                        }
                    }
                    Log.i(Thread.currentThread().getName()+"----"+newtask,"render complete"+((System.currentTimeMillis()-current)/1000));
                }
            }
        };
    }

    /**
     * 将所有Overlay 添加到地图上
     */
    public void addToMap() {
        if (mBaiduMap == null) {
            return;
        }
        asyncRenderBikes();
    }

    /**
     * 将所有Overlay 从 地图上消除
     */
    public void removeFromMap() {
        if (mBaiduMap == null) {
            return;
        }
        for (Overlay marker : mOverlayList) {
            marker.remove();
        }
        synchronized (bikeItems) {
            for (BikeItem item : bikeItems) {
                item.remove();
            }
            bikeItems.clear();
        }
        mOverlayList.clear();
    }

    public void onMapStatusChangeStart(MapStatus var1) {
    }

    public void onMapStatusChange(MapStatus var1) {
    }

    public void onMapStatusChangeFinish(MapStatus status) {
        asyncRenderBikes(status);
    }
}
