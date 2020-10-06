package com.example.covid19.network

import com.example.covid19.model.InfoNegara
import com.example.covid19.model.ResponseCountry
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

//untuk meng-get data yg ada di dlam directory summray
interface ApiService {
    @GET("summary")
    fun getAllNegara() : Call<ResponseCountry>
}
//untuk akses tabel yg tersedia di model
interface InfoService{
    @GET
    fun getInfoService(@Url url:String?) : Call<List<InfoNegara>>
}
//untuk koneksi
object RetrofitBuilder{
        //untuk mengcek koneksi internet

    private val okhttp = OkHttpClient().newBuilder()
        .connectTimeout(15,TimeUnit.SECONDS)
        .readTimeout(10,TimeUnit.SECONDS)
        .writeTimeout(15,TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.covid19api.com/")
        .client(okhttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    //testetstes
}
