package com.kwon.sensorclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kwon.sensorclient.network.NetworkService;
import com.kwon.sensorclient.network.Res;
import com.kwon.sensorclient.network.RetrofitClass;
import com.kwon.sensorclient.network.SaveDataObj;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Math.abs;

public class BaseFragment extends Fragment {
    //private static final String URL = BuildConfig.HOST_AWS;
    //    private static final String URL = BuildConfig.Host_RASPBERRY;
    //    private static final String URL = 원하는 서버주소;
    Button true_btn, false_btn, init_btn;
    TextView true_cnt, false_cnt;

    private NetworkService mRetro = new RetrofitClass().getRetroService();

    private SensorManager mSensorManager = null;
    private SensorEventListener mAccLis;
    private Sensor mAccelometerSensor = null;
    SharedPreferences pref2;
    private boolean data = false;

    private double INF = Double.MAX_VALUE;   //최대값
    private int seq = 1;
    private int cntTrue = 0, cntFalse = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.base_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = this.getView();

        true_btn = (Button) view.findViewById(R.id.true_btn);
        false_btn = (Button) view.findViewById(R.id.false_btn);
        true_cnt = (TextView) view.findViewById(R.id.true_cnt);
        false_cnt = (TextView) view.findViewById(R.id.false_cnt);
        init_btn = (Button) view.findViewById(R.id.init_btn2);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        pref2 = getActivity().getSharedPreferences("pref2", Context.MODE_PRIVATE);
        if(getPreferences()){
            true_btn.setEnabled(false);
            true_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.enable_shape));
            false_btn.setEnabled(false);
            false_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.enable_shape));
            true_cnt.setText("30/30");
            false_cnt.setText("15/15");
        }

        init_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                true_btn.setEnabled(true);
                true_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_shape));
                false_btn.setEnabled(true);
                false_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_shape));
                true_cnt.setText("0/30");
                false_cnt.setText("0/15");
                cntTrue = 0;
                cntFalse = 0;
                seq = 1;
            }
        });

        true_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("KWON","x : "+ MyGlobals.getInstance().getX() + ", INF : "+ INF + ", seq : "+seq );
                Log.i("KWON","x : "+ MyGlobals.getInstance().getX() + ", y : "+ MyGlobals.getInstance().getY() + ", z : "+MyGlobals.getInstance().getZ() );
                if((MyGlobals.getInstance().getX() == INF) || (seq > 30))
                    Toast.makeText(getActivity().getApplicationContext(), "Current 값이 없거나 저장공간을 모두 할당했습니다.",
                            Toast.LENGTH_SHORT).show();
                else{
                    cntTrue++;
                    true_cnt.setText(cntTrue+"/30");
                    true_btn.setEnabled(false);
                    true_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.enable_shape));
                    false_btn.setEnabled(false);
                    false_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.enable_shape));
                    mAccLis = new AccelometerListener();
                    mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_GAME);
                }
            }
        });

        false_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(seq <= 30)
                    Toast.makeText(getActivity().getApplicationContext(), "true 데이터 30개가 필요합니다.",
                            Toast.LENGTH_SHORT).show();
                else if((MyGlobals.getInstance().getX() == INF) || (seq > 45))
                    Toast.makeText(getActivity().getApplicationContext(), "Current 값이 없거나 저장공간을 모두 할당했습니다.",
                            Toast.LENGTH_SHORT).show();
                else{
                    cntFalse++;
                    false_cnt.setText(cntFalse+"/15");
                    if(cntFalse == 15){
                        savePreferences();
                    }
                    true_btn.setEnabled(false);
                    true_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.enable_shape));
                    false_btn.setEnabled(false);
                    false_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.enable_shape));
                    mAccLis = new AccelometerListener_False();
                    mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_GAME);
                }
            }
        });
    }
    private boolean getPreferences(){
        return pref2.getBoolean("data", false);
    }

    private void savePreferences(){
        SharedPreferences.Editor editor = pref2.edit();
        editor.putBoolean("data", true);
        editor.apply();
    }

    //true 기반데이터
    private class AccelometerListener implements SensorEventListener {
        double[] array = new double[150];
        int i = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (i == 50) {   //array가 꽉 차면 저장
                Log.i("LOG", "Array is Full.");
                mSensorManager.unregisterListener(mAccLis);
                i = 0;
                true_btn.setEnabled(true);
                true_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_shape));
                false_btn.setEnabled(true);
                false_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_shape));
                sendSDO(new SaveDataObj(seq, array));
            } else {   //basePoint를 array배열에 저장. basePoint = 절대값(측정되는 값 - 현 위치 current값)
                array[i] = abs(event.values[0] - MyGlobals.getInstance().getX());
                array[i + 50] = abs(event.values[1] - MyGlobals.getInstance().getY());
                array[i + 100] = abs(event.values[2] - MyGlobals.getInstance().getZ());

                Log.e("LOG", "ACCELOMETER true           [X]:" + String.format("%.4f", array[i])
                        + "           [Y]:" + String.format("%.4f", array[i + 50])
                        + "           [Z]:" + String.format("%.4f", array[i + 100]));
                i++;
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    //false 기반데이터
    private class AccelometerListener_False implements SensorEventListener {
        double[] array = new double[150];
        int i = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (i == 50) {
                Log.i("LOG", "Array is Full.");
                mSensorManager.unregisterListener(mAccLis);
                i = 0;
                true_btn.setEnabled(true);
                true_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_shape));
                false_btn.setEnabled(true);
                false_btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_shape));
                sendSDO(new SaveDataObj(seq, array));
            } else {
                array[i] = abs(event.values[0] - MyGlobals.getInstance().getX());
                array[i + 50] = abs(event.values[1] - MyGlobals.getInstance().getY());
                array[i + 100] = abs(event.values[2] - MyGlobals.getInstance().getZ());

                Log.e("LOG", "ACCELOMETER false           [X]:" + String.format("%.4f", array[i])
                        + "           [Y]:" + String.format("%.4f", array[i + 50])
                        + "           [Z]:" + String.format("%.4f", array[i + 100]));
                i++;
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    //레트로핏 함수. 주소랑 객체만 받음 됩니다.
    private void sendSDO(SaveDataObj obj) {

       mRetro.saveData(obj).enqueue(new Callback<Res>() {
           @Override
           public void onResponse(Call<Res> call, Response<Res> response) {
               if (response.isSuccessful()) {
                   Log.d("LOG", "전송 : " + response.body());
//                   Toast.makeText(getActivity().getApplicationContext(), "전송", Toast.LENGTH_SHORT).show();
                   //seqText.setText("저장개수" + seq);
                   seq++;
               } else {
                   Log.e("LOG", "신호 전송실패 = " + response.code());
                   Toast.makeText(getActivity().getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();
               }
           }

           @Override
           public void onFailure(Call<Res> call, Throwable t) {
               Log.e("LOG", "네트워크 확인 : " + t);
               Toast.makeText(getActivity().getApplicationContext(), "네트워크 연결 확인 필요", Toast.LENGTH_SHORT).show();
           }
       });
    }
}
