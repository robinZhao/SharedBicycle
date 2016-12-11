package com.github.robinzhao.shibike.bike;

import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaoruibin on 2016/10/15.
 */

public class BikeLoader {

    // private List<Map> points = null;

    private PointLoadedCallback callback = null;

    private final static String[] fields = new String[]{
            "id",
            "netName",
            "address",
            "netType",
            "netStatus",
            "bicycleCapacity",
            "bicycleNum",
            "gpsy",
            "gpsx"};


    public List<BikeItem> loadPointRemote() throws IOException {
        String urlStr = "http://www.shibike.com/branch/bmap";
        InputStream stream = null;
        String pageContent = "";
        try {
            URL url = new URL(urlStr);
            stream = url.openStream();
            pageContent = IOUtils.toString(stream);
        } finally {
            if (null != stream)
                stream.close();

        }
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("points\\[[0-9]+\\]=(\\{[\\s\\S\\n\\r]*?\\})", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(pageContent);
        while (matcher.find()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(matcher.group(1));
        }
        StringBuffer sb1 = new StringBuffer();
        Pattern pattern1 = Pattern.compile("parseFloat\\((\"[\\s\\S]*?\")\\)");
        Matcher matcher1 = pattern1.matcher(sb.toString());
        while (matcher1.find()) {
            matcher1.appendReplacement(sb1, matcher1.group(1));
        }
        matcher1.appendTail(sb1);
      //  System.out.println(sb1);
        //System.out.println(sb1.toString());
        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
               return !Arrays.asList(fields).contains(f.getName());
            }
            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();
        List<BikeItem> objs = gson.fromJson("[" + sb1.toString() + "]", new TypeToken<List<BikeItem>>() {
        }.getType());
        return objs;
    }

    //only support remote load
    private void asyncLoadPoint(boolean forceLoad) throws IOException {
//        if (null == points || forceLoad) {
//            BikeLoader.this.points = this.loadPointRemote();
//        }
        List<BikeItem> points = this.loadPointRemote();
        if (null != this.callback)
            callback.pointLoaded(points);
    }

    private void remoteRefresh() throws IOException {
        List<BikeItem> points = this.loadPointRemote();
        if (null != this.callback)
            callback.refreshed(points);
    }


    public interface PointLoadedCallback {
        public void pointLoaded(List<BikeItem> points);

        public void error(Exception e);

        public void refreshed(List<BikeItem> points);
    }


    public void setPointLoadedCallback(PointLoadedCallback callback) {
        this.callback = callback;
    }


    public void load(final boolean force) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BikeLoader.this.asyncLoadPoint(force);
                } catch (Exception e) {
                    e.printStackTrace();
                   Log.i("err", Log.getStackTraceString(e));
                    if (null != BikeLoader.this.callback)
                        BikeLoader.this.callback.error(e);
                }

            }
        }).start();
    }


    public void refresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BikeLoader.this.remoteRefresh();
                } catch (Exception e) {
                    if (null != BikeLoader.this.callback)
                        BikeLoader.this.callback.error(e);
                }

            }
        }).start();
    }

    public void load() {
        this.load(false);
    }

    public static void main(String[] args) {
            BikeLoader loader = new BikeLoader();
        loader.setPointLoadedCallback(new PointLoadedCallback() {
            @Override
            public void pointLoaded(List<BikeItem> points) {
                System.out.println(points.size());
                for(BikeItem item:points){
                    System.out.println(item.netName+"\t"+item.id+"\t"+item.gpsx+"\t"+item.gpsy);
                  //  item.getMarkerOptions();
                }
            }

            @Override
            public void error(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void refreshed(List<BikeItem> points) {

            }
        });
        try {
            loader.asyncLoadPoint(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("err", Log.getStackTraceString(e));
            if (null !=loader.callback)
                loader.callback.error(e);
        }
    }
}
