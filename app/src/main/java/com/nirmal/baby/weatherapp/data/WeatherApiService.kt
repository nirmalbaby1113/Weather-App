package com.nirmal.baby.weatherapp.data

import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiService {
    @GET("forecast.json") // Endpoint for the Weather API
    suspend fun getWeatherData(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int,
        @Query("aqi") aqi: String,
        @Query("alerts") alerts: String
    ): WeatherResponse
}