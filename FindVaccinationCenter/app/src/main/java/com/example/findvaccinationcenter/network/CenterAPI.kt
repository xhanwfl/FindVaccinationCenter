package com.example.findvaccinationcenter.network


import retrofit2.Call
import com.example.findvaccinationcenter.model.CenterDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface CenterAPI {
    @GET("15077586/v1/centers")
   fun getCenterData(
        @Query("page") page : Int,
        @Query("perPage") pageSize : Int,
        @Query("serviceKey") apiKey : String
   ): Call<CenterDTO>
}