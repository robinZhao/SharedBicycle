package com.github.robinzhao.shibike.bike;

import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.github.robinzhao.shibike.R;

import java.io.Serializable;

/**
 * Created by zhaoruibin on 2016/11/4.
 */

public class BikeItem implements Serializable {
    public String id;
    public String netName;
    public String address;
    public String netType;
    public String netStatus;
    public String bicycleCapacity;
    public String bicycleNum;
    public String gpsy;
    public String gpsx;
    public Overlay marker;
    private MarkerOptions markerOptions;

    public static volatile BitmapDescriptor icon;

    public MarkerOptions getMarkerOptions(){
        if(null==markerOptions) {
            if (null == icon) {
                icon = BitmapDescriptorFactory
                        .fromResource(R.drawable.bike);
            }
            markerOptions = new MarkerOptions();
            markerOptions .icon(icon);
            markerOptions .flat(true);
            markerOptions .perspective(false);
            Bundle bundle = new Bundle();
            bundle.putSerializable("bikeItem",this);
            bundle.putString("id",this.id);
            markerOptions.extraInfo(bundle);
        }
        try {
            float x = Float.parseFloat((String) this.gpsy);
            float y = Float.parseFloat((String) this.gpsx);
            markerOptions.position(new LatLng(x, y));
        }catch(Exception e){
            throw new NumberFormatException(e.getMessage());
        }
            return markerOptions;
    }

    public void add(BaiduMap baiduMap){
        if(null==this.marker){
            this.marker= baiduMap.addOverlay(this.getMarkerOptions());
        }
    }

    public void remove(){
        if(null!=this.marker){
            this.marker.remove();
            this.marker=null;
        }
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("名称:"+netName+"\n\r");
        sb.append("地址:"+address+"\n\r");
        sb.append("类型:"+netType+"\n\r");
        sb.append("状态:"+netStatus+"\n\r");
        sb.append("容量:"+bicycleCapacity+"\n\r");
        sb.append("数量:"+bicycleNum+"\n\r");
        return sb.toString();

    }

    public void show(BaiduMap baiduMap){
        this.add(baiduMap);
        if(!this.marker.isVisible()) {
            this.marker.setVisible(true);
        }
    }
    public void hide(){
        if(null==this.marker)return;
        if(this.marker.isVisible()){
            this.marker.setVisible(false);
        }
    }


}
