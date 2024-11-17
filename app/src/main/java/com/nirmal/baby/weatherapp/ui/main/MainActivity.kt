package com.nirmal.baby.weatherapp.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nirmal.baby.weatherapp.R
import com.nirmal.baby.weatherapp.adapters.HorizontalRecyclerAdapter
import com.nirmal.baby.weatherapp.data.WeatherItemsHourly
import com.nirmal.baby.weatherapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var horizontalRecyclerAdapter: HorizontalRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        // Initialize RecyclerView Adapter
        horizontalRecyclerAdapter = HorizontalRecyclerAdapter(emptyList())
        binding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = horizontalRecyclerAdapter

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

                // Get the current time
                val currentTime = Calendar.getInstance().time
                val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                // Extract current hour explicitly from the `current` section
                val nowWeatherItem = WeatherItemsHourly(
                    temperature = "${data.current.temp_c}°C",
                    iconResId = R.drawable.sample, // Replace with actual icon mapping logic
                    time = "Now"
                )

                // Filter hourly data for subsequent hours
                val hourlyData = data.forecast.forecastday[0].hour.filter { hour ->
                    val hourTime = timeFormat.parse(hour.time)
                    hourTime != null && hourTime.after(currentTime)
                }.map { hour ->
                    WeatherItemsHourly(
                        temperature = "${hour.temp_c}°C",
                        iconResId = R.drawable.sample, // Replace with actual icon mapping logic
                        time = hour.time.split(" ")[1] // Extract time part
                    )
                }

                // Combine "Now" data with filtered hourly data
                val finalData = listOf(nowWeatherItem) + hourlyData

                // Update RecyclerView
                horizontalRecyclerAdapter.updateData(finalData)

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
}