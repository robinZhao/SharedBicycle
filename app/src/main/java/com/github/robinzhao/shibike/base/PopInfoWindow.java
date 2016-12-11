package com.github.robinzhao.shibike.base;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.model.LatLng;
import com.github.robinzhao.shibike.R;

/**
 * Created by zhaoruibin on 2016/12/4.
 */

public class PopInfoWindow {
    private Context ctx;
    private BaiduMap baiduMap;
    private GoClickListener goClickListener;


    public interface GoClickListener {
        public void goClick(ImageButton btn);
    }

    View popView;

    public PopInfoWindow(BaiduMap baiduMap, Context ctx) {
        this.baiduMap = baiduMap;
        this.popView = LayoutInflater.from(ctx).inflate(R.layout.bubble_text, null); // 自定义气泡形状
        popView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopInfoWindow.this.baiduMap.hideInfoWindow();
            }
        });
        popView.findViewById(R.id.go_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goClickListener.goClick((ImageButton) v);
            }
        });
    }

    public void show(String text, LatLng position) {
        ((TextView) popView.findViewById(R.id.b_text)).setText(text);
        ((TextView) popView.findViewById(R.id.b_text)).setTextColor(Color.rgb(3, 3, 3));
        baiduMap.showInfoWindow(new InfoWindow(popView, position, 0));

    }

    public GoClickListener getGoClickListener() {
        return goClickListener;
    }

    public void setGoClickListener(GoClickListener goClickListener) {
        this.goClickListener = goClickListener;
    }
}
