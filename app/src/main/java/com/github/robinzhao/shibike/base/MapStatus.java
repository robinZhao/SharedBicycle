package com.github.robinzhao.shibike.base;

import com.github.robinzhao.shibike.bike.BikeItem;

/**
 * Created by zhaoruibin on 2016/12/4.
 */

public class MapStatus {
    private com.baidu.mapapi.map.MapStatus lastStatus;
    private BikeItem currentBikeItem;

    public com.baidu.mapapi.map.MapStatus getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(com.baidu.mapapi.map.MapStatus lastStatus) {
        this.lastStatus = lastStatus;
    }

    public BikeItem getCurrentBikeItem() {
        return currentBikeItem;
    }

    public void setCurrentBikeItem(BikeItem currentBikeItem) {
        this.currentBikeItem = currentBikeItem;
    }
}
