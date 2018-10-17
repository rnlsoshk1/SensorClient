package com.kwon.sensorclient.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NetworkService {
    //기반데이터저장
    @POST("saveData")
    Call<Res> saveData(@Body SaveDataObj obj);

    //문열림 감지
    @POST("open")
    Call<Res> open(@Body SaveDataObj obj);
}
