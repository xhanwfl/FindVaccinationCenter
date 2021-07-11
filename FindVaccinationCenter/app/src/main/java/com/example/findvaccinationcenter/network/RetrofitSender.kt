package com.example.findvaccinationcenter.network

import android.content.ContentValues
import android.util.Log
import com.example.findvaccinationcenter.model.CenterDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitSender {
    var retrofit : Retrofit? = null

    fun getInstance():Retrofit{

        if(retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl("https://api.odcloud.kr/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit!!
    }
}