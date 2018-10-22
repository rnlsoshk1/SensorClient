package com.kwon.sensorclient.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClass {
    private NetworkService mRetroService;
    private String baseUrl = "http://115.139.157.24:8764/";
//            "http://13.124.254.99:3856/";    //aws
//            "http://115.139.157.24:8764/"   //라즈베리

    public RetrofitClass() {
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit mRetro = new Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();

        mRetroService = mRetro.create(NetworkService.class);
    }

    public NetworkService getRetroService() {return mRetroService;}
}
