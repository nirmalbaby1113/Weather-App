package com.nirmal.baby.weatherapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/v1/") // Base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(WeatherApiService::class.java)

    suspend fun fetchWeatherData(location: String): WeatherResponse {
        return apiService.getWeatherData(
            apiKey = "aef73390620d4eb080912325231611",
            location = location,
            days = 1,
            aqi = "no",
            alerts = "no"
        )
    }
}
