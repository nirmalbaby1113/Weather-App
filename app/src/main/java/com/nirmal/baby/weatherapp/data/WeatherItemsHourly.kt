package com.nirmal.baby.weatherapp.data

data class WeatherItemsHourly(
    val temperature: String, // e.g., "25°C"
    val iconResId: Int,      // Drawable resource ID for the weather icon
    val time: String         // e.g., "12:00 PM"
)
