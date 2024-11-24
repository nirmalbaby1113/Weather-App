package com.nirmal.baby.weatherapp.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.nirmal.baby.weatherapp.R
import com.nirmal.baby.weatherapp.adapters.DailyRecyclerAdapter
import com.nirmal.baby.weatherapp.adapters.HorizontalRecyclerAdapter
import com.nirmal.baby.weatherapp.data.WeatherItemsDaily
import com.nirmal.baby.weatherapp.data.WeatherRepository
import com.nirmal.baby.weatherapp.data.WeatherItemsHourly
import com.nirmal.baby.weatherapp.databinding.ActivityMainBinding
import com.nirmal.baby.weatherapp.factory.MainActivityViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var horizontalRecyclerAdapter: HorizontalRecyclerAdapter
    private lateinit var dailyRecyclerAdapter: DailyRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create WeatherRepository instance
        val repository = WeatherRepository()

        // Pass WeatherRepository to ViewModelFactory
        val factory = MainActivityViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainActivityViewModel::class.java]

        // Initialize Hourly RecyclerView Adapter
        horizontalRecyclerAdapter = HorizontalRecyclerAdapter(emptyList())
        binding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = horizontalRecyclerAdapter

        // Initialize Daily RecyclerView Adapter
        dailyRecyclerAdapter = DailyRecyclerAdapter(emptyList())
        binding.dailyRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.dailyRecyclerView.adapter = dailyRecyclerAdapter


        // Observe ViewModel data
        viewModel.weatherData.observe(this) { data ->
            binding.apply {
                textViewWeatherDegree.text = "${data.current.temp_c}°"
                weatherConditionText.text = data.current.condition.text
                weatherFeelsLikeText.text = "Feels like ${data.current.feelslike_c}°"
                weatherHighText.text = "High: ${data.forecast.forecastday[0].day.maxtemp_c}°"
                weatherLowText.text = "Low: ${data.forecast.forecastday[0].day.mintemp_c}°"
                textViewValueUv.text = "${data.current.uv}"
                textViewValueHumidity.text = "${data.current.humidity}%"
                textViewValueWind.text = "${data.current.wind_kph} km/h"
                textViewSunriseValue.text = data.forecast.forecastday[0].astro.sunrise
                textViewSunsetValue.text = data.forecast.forecastday[0].astro.sunset

                Glide.with(this@MainActivity)
                    .load("https:${data.current.condition.icon}")
                    .into(weatherConditionImageMain)

                // Get the current time
                val currentTime = Calendar.getInstance().time
                val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                // Extract current hour explicitly from the `current` section
                val nowWeatherItem = WeatherItemsHourly(
                    temperature = "${data.current.temp_c}°C",
                    iconUrl = "https:${data.current.condition.icon}",
                    time = "Now"
                )

                // Filter hourly data for subsequent hours
                val hourlyData = data.forecast.forecastday[0].hour.filter { hour ->
                    val hourTime = timeFormat.parse(hour.time)
                    hourTime != null && hourTime.after(currentTime)
                }.map { hour ->
                    WeatherItemsHourly(
                        temperature = "${hour.temp_c}°C",
                        iconUrl = "https:${hour.condition.icon}",
                        time = hour.time.split(" ")[1] // Extract time part
                    )
                }

                // Map 3-day forecast data
                val dailyData = data.forecast.forecastday.map { day ->
                    WeatherItemsDaily(
                        date = formatDate(day.date),
                        iconUrl = "https:${day.day.condition.icon}",
                        high = day.day.maxtemp_c.toString(),
                        low = day.day.mintemp_c.toString()
                    )
                }

                // Combine "Now" data with filtered hourly data
                val finalData = listOf(nowWeatherItem) + hourlyData

                // Update RecyclerView
                horizontalRecyclerAdapter.updateData(finalData)
                dailyRecyclerAdapter.updateData(dailyData)
            }
        }

        // Observe Error
        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
        }

        // Observe Loading State
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.mainContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Fetch Weather Data
        viewModel.fetchWeather("London, Ontario")
    }

    private fun formatDate(dateString: String): String {
        // The input date format from the API
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // The desired output format
        val outputFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

        return try {
            val date = inputFormat.parse(dateString) // Parse the input date
            outputFormat.format(date!!) // Format to desired output
        } catch (e: Exception) {
            "Invalid Date" // Fallback in case of an error
        }
    }
}
