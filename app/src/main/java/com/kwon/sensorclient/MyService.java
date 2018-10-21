package com.kwon.sensorclient;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.kwon.sensorclient.network.NetworkService;
import com.kwon.sensorclient.network.Res;
import com.kwon.sensorclient.network.RetrofitClass;
import com.kwon.sensorclient.network.SaveDataObj;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Math.abs;

public class MyService extends IntentService implements SensorEventListener {
    public MyService() {
        super("MyService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    static double callThreshold = 0.02;
    private SensorManager mSensorManager = null;
    private Sensor mAccelometerSensor = null;
    private Sensor mMagneticSensor = null;
    private boolean accumulate = false;
    private boolean magnetic = false;
    private boolean send = false;

    int total = 0, count = 0, i = 0, j =0;
    double sum_x = 0.0, sum_y = 0.0, sum_z = 0.0;
    double[] mMagnetic = new double[3];
    double[] array = new double[150];
    private NetworkService mRetro = new RetrofitClass().getRetroService();

    //bindservice 시에 호출
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Log.i("KWON", "Service onCreate");
    }

    //startservice 시에 호출
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("KWON", "Service onStart");
        mSensorManager.registerListener(this, mAccelometerSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_UI);
        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            if(!magnetic){
                j++;
                sum_x += event.values[0];
                sum_y += event.values[1];
                sum_z += event.values[2];

                if(j >= 30){
                    mMagnetic[0] = sum_x / 30.0;
                    mMagnetic[1] = sum_y / 30.0;
                    mMagnetic[2] = sum_z / 30.0;
                    magnetic = true;
                }
            } else {
                send = (abs(event.values[0] - mMagnetic[0]) >= 10) || (abs(event.values[1] - mMagnetic[1]) >= 10)
                        || (abs(event.values[2] - mMagnetic[2]) >= 10);
            }
        }

        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            // temp. 무시하세요
            total++;
            // temp end

            // 임계값보다 크면 가속데이터 수집시작
            if (!accumulate && (abs(event.values[0] - MyGlobals.getInstance().getX()) > callThreshold || abs(event.values[1] - MyGlobals.getInstance().getY()) > callThreshold ||
                    abs(event.values[2] - MyGlobals.getInstance().getZ()) > callThreshold)) {
                accumulate = true;
                Log.i("LOG", "callThreshold초과" +
                        "\nx:" + (abs(event.values[0] - MyGlobals.getInstance().getX())) +
                        "\ny: " + (abs(event.values[0] - MyGlobals.getInstance().getY())) +
                        "\nz: "+ (abs(event.values[0] - MyGlobals.getInstance().getZ())));
                Log.i("LOG", "샘플링 횟수 : " + total + ", 호출 횟수 : " + ++count);
            }

            //x50개, y50개, z50개 수집완료, 서버전송
            if (i >= 50) {
                Log.i("LOG", "Array is Full.");
                Log.i("KWON","x : "+ MyGlobals.getInstance().getX() + ", y : "+ MyGlobals.getInstance().getY() + ", z : "+ MyGlobals.getInstance().getZ());
                i = 0;

                if(send){
                    SaveDataObj obj = new SaveDataObj();
                    obj.setData(array);
                    mRetro.open(obj).enqueue(new Callback<Res>() {
                        @Override
                        public void onResponse(Call<Res> call, Response<Res> response) {
                            if (response.isSuccessful()) {
                                Log.d("LOG", "전송 : " + response.body());
                                Toast.makeText(getApplicationContext(), "전송", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("LOG", "response = " + response.code());
                                Toast.makeText(getApplicationContext(), "Noise", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Res> call, Throwable t) {
                            Log.e("LOG", "네트워크 확인 : " + t);
                            Toast.makeText(getApplicationContext(), "네트워크 연결 확인 필요", Toast.LENGTH_SHORT).show();
                            //onDestroy();
                        }
                    });
                    accumulate = false;
                }
            } else if (accumulate) {    // 가속임계값 넘으면 가속데이터 수집
                array[i] = abs(event.values[0] - MyGlobals.getInstance().getX());
                array[i + 50] = abs(event.values[1] - MyGlobals.getInstance().getY());
                array[i + 100] = abs(event.values[2] - MyGlobals.getInstance().getZ());
                i++;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("LOG", "onDestroy()");
        mSensorManager.unregisterListener(this);
    }
}
