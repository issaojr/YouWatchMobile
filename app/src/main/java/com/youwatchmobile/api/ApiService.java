package com.youwatchmobile.api;

import com.youwatchmobile.model.LoginRequest;
import com.youwatchmobile.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/Login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}


