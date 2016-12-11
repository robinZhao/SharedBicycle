package com.github.robinzhao.shibike.base;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by zhaoruibin on 2016/11/7.
 */

public class DirectionSensor {
    float[] magneticFieldValues;
    float[] accelerometerValues;
    SensorManager sensorManager = null;
    Context ctx;

    public DirectionSensor(Context ctx) {
        this.ctx = ctx;
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                magneticFieldValues = event.values;
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                accelerometerValues = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                //不及时更新方向
                // direct();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public float getDirection() {
        if (null == accelerometerValues || null == magneticFieldValues)
            return -1;
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        float direct = (float) Math.toDegrees(values[0]);
        //要经过一次数据格式的转换，转换为度
        return direct;
    }

    private Object getSystemService(String name) {
        return ctx.getSystemService(name);
    }

    ;

    public void regSensorListener() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(sensorEventListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unRegSensorListener() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorEventListener);
    }

}
