package com.kwon.sensorclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class ActiveFragment extends Fragment{
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("LOG", "onDestroy()");
        getContext().stopService(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    Button on_btn, off_btn;
    ImageView active_img;

    Intent intent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.active_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = this.getView();

        intent = new Intent(getContext(), MyService.class);

        on_btn = (Button) view.findViewById(R.id.on_btn);
        off_btn = (Button) view.findViewById(R.id.off_btn);
        active_img = (ImageView) view.findViewById(R.id.active_img);

        on_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_btn.setEnabled(false);
                off_btn.setEnabled(true);
                //active_img.setImageDrawable("@mipmap/ic_launcher");
                getContext().startService(intent);
            }
        });

        off_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_btn.setEnabled(true);
                off_btn.setEnabled(false);
                if (intent != null) {
                    getContext().stopService(intent);
                }
            }
        });
    }
}
