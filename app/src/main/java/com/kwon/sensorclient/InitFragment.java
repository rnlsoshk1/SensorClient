package com.kwon.sensorclient;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

public class InitFragment extends Fragment {

    static double INF = Double.MAX_VALUE;
    ProgressBar progressBar;
    Button button;

    private SensorManager mSensorManager = null;
    private SensorEventListener mAccLis;
    private Sensor mAccelometerSensor = null;

    private double currentArray[] = new double[]{INF, INF, INF};    //보정가속값

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.init_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = this.getView();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        button = (Button) view.findViewById(R.id.button);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccLis = new CurrentAccelometerListener();
        MyGlobals.getInstance().setX(currentArray[0]);
        MyGlobals.getInstance().setY(currentArray[1]);
        MyGlobals.getInstance().setZ(currentArray[2]);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setEnabled(false);
                mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        });
    }

    public class CurrentAccelometerListener implements SensorEventListener{
        int i = 0;
        private double x = 0.0, y = 0.0, z = 0.0;

        double max1 = -987654.0, min1 = Double.MAX_VALUE;
        double max2 = -987654.0, min2 = Double.MAX_VALUE;
        double max3 = -987654.0, min3 = Double.MAX_VALUE;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (i == 250) {
                currentArray[0] = x / 250.0;
                currentArray[1] = y / 250.0;
                currentArray[2] = z / 250.0;
                Log.i("LOG", "average accelometer : " + currentArray[0] + ", " + currentArray[1] + ", " + currentArray[2]);
                MyGlobals.getInstance().setX(currentArray[0]);
                MyGlobals.getInstance().setY(currentArray[1]);
                MyGlobals.getInstance().setZ(currentArray[2]);
                mSensorManager.unregisterListener(mAccLis);

                // temp. 무시하세요
                Log.i("LOG", "max1 : " + String.format("%.4f", max1 - currentArray[0]) + ", min1 : " + String.format("%.4f", currentArray[0] - min1));
                Log.i("LOG", "max2 : " + String.format("%.4f", max2 - currentArray[1]) + ", min2 : " + String.format("%.4f", currentArray[1] - min2));
                Log.i("LOG", "max3 : " + String.format("%.4f", max3 - currentArray[2]) + ", min3 : " + String.format("%.4f", currentArray[2] - min3));
                // temp end
                Log.i("MyGlobal","x : "+MyGlobals.getInstance().getX() + ", y : "+MyGlobals.getInstance().getY() + ", z : "+ MyGlobals.getInstance().getZ());
            } else {
                // temp. 무시하세요
                double temx = event.values[0], temy = event.values[1], temz = event.values[2];
                max1 = max1 > temx ? max1 : temx;
                min1 = min1 < temx ? min1 : temx;
                max2 = max2 > temy ? max2 : temy;
                min2 = min2 < temy ? min2 : temy;
                max3 = max3 > temz ? max3 : temz;
                min3 = min3 < temz ? min3 : temz;
                Log.e("LOG", "current" + i + "번째           [X]:" + String.format("%.4f", event.values[0])
                        + "           [Y]:" + String.format("%.4f", event.values[1])
                        + "           [Z]:" + String.format("%.4f", event.values[2]));
                // temp end

                x += event.values[0];
                y += event.values[1];
                z += event.values[2];
                i++;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}