package com.nirmal.baby.weatherapp.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.nirmal.baby.weatherapp.R
import com.nirmal.baby.weatherapp.adapters.DailyRecyclerAdapter
import com.nirmal.baby.weatherapp.adapters.HorizontalRecyclerAdapter
import com.nirmal.baby.weatherapp.data.WeatherItemsDaily
import com.nirmal.baby.weatherapp.data.WeatherItemsHourly
import com.nirmal.baby.weatherapp.data.WeatherRepository
import com.nirmal.baby.weatherapp.databinding.ActivityMainBinding
import com.nirmal.baby.weatherapp.factory.MainActivityViewModelFactory
import com.nirmal.baby.weatherapp.ui.main.MainActivityViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var horizontalRecyclerAdapter: HorizontalRecyclerAdapter
    private lateinit var dailyRecyclerAdapter: DailyRecyclerAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show loading spinner and hide main content initially
        binding.loadingSpinner.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize ViewModel
        val repository = WeatherRepository()
        val factory = MainActivityViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainActivityViewModel::class.java]

        // Initialize RecyclerView Adapters
        horizontalRecyclerAdapter = HorizontalRecyclerAdapter(emptyList())
        dailyRecyclerAdapter = DailyRecyclerAdapter(emptyList())

        binding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = horizontalRecyclerAdapter

        binding.dailyRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.dailyRecyclerView.adapter = dailyRecyclerAdapter

        // Check and Request Location Permissions
        if (hasLocationPermission()) {
            fetchCurrentLocation()
        } else {
            requestLocationPermission()
        }

        // Observe Weather Data
        observeWeatherData()

        // Observe Errors and Loading State
        observeErrorAndLoadingState()

        // Set Click Listener on Location Name
        setupLocationNameClickListener()
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun fetchCurrentLocation() {
        // Explicit permission check
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        binding.loadingSpinner.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val cityName = addresses[0].locality ?: "Unknown"
                            //Toast.makeText(this, "Current location: $cityName", Toast.LENGTH_SHORT).show()

                            // Set the location name in the TextView
                            binding.textViewLocationName.text = cityName

                            // Fetch weather for the current city
                            viewModel.fetchWeather(cityName)

                        } else {
                            Toast.makeText(this, "Unable to determine city name", Toast.LENGTH_SHORT).show()
                            binding.textViewLocationName.text = "Unknown Location"
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Geocoder error: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.textViewLocationName.text = "Error Fetching Location"
                    }
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                    binding.textViewLocationName.text = "No Location Found"
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch location: ${it.message}", Toast.LENGTH_SHORT).show()
                binding.textViewLocationName.text = "Failed to Fetch Location"
            }
        } catch (e: SecurityException) {
            // Handle the case where the permission check is bypassed or fails
            Toast.makeText(this, "Permission denied for location access: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.textViewLocationName.text = "Permission Denied"
        }
    }




    private fun observeWeatherData() {
        viewModel.weatherData.observe(this) { data ->
            binding.apply {
                // Update UI with weather data
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

                // Update hourly RecyclerView Data
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                val hourlyData = data.forecast.forecastday[0].hour.filter { hour ->
                    val hourValue = hour.time.split(" ")[1].split(":")[0].toInt() // Extract hour from "HH:mm"
                    hourValue >= currentHour // Filter out past hours
                }.mapIndexed { index, hour ->
                    WeatherItemsHourly(
                        time = if (index == 0) "Now" else hour.time.split(" ")[1], // Display "Now" for the first item
                        temperature = "${hour.temp_c}°C",
                        iconUrl = "https:${hour.condition.icon}"
                    )
                }
                horizontalRecyclerAdapter.updateData(hourlyData)


                // Update RecyclerView Data
                val dailyData = data.forecast.forecastday.map { day ->
                    WeatherItemsDaily(
                        date = formatDate(day.date),
                        iconUrl = "https:${day.day.condition.icon}",
                        high = day.day.maxtemp_c.toString(),
                        low = day.day.mintemp_c.toString()
                    )
                }
                dailyRecyclerAdapter.updateData(dailyData)
            }
        }
    }

    private fun observeErrorAndLoadingState() {
        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.mainContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        return try {
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    private fun setupLocationNameClickListener() {
        binding.textViewLocationName.setOnClickListener {
            // Hide the icon when transitioning to EditText
            binding.iconLocation.visibility = View.INVISIBLE

            // Create EditText dynamically with specified features
            val editText = androidx.appcompat.widget.AppCompatEditText(this).apply {
                id = View.generateViewId()
                layoutParams = binding.textViewLocationName.layoutParams
                setText(binding.textViewLocationName.text.toString())
                hint = "Search location"
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.curved_edit_text_background)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                setHintTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                imeOptions = EditorInfo.IME_ACTION_SEARCH
                inputType = InputType.TYPE_CLASS_TEXT
                // Set padding (left, top, right, bottom)
                setPadding(24, 16, 24, 16)
            }

            val parent = binding.textViewLocationName.parent as ViewGroup
            val index = parent.indexOfChild(binding.textViewLocationName)

            // Replace TextView with EditText
            parent.removeViewAt(index)
            parent.addView(editText, index)

            editText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

            // Set listener to handle user input when search action is triggered
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val newLocation = editText.text.toString().trim()
                    if (newLocation.isNotEmpty()) {
                        binding.textViewLocationName.text = newLocation

                        // Replace EditText back with TextView
                        parent.removeViewAt(index)
                        parent.addView(binding.textViewLocationName, index)

                        // Show the icon when switching back to TextView
                        binding.iconLocation.visibility = View.VISIBLE

                        // Fetch weather for the entered location
                        viewModel.fetchWeather(newLocation)
                    } else {
                        Toast.makeText(this, "Please enter a valid location", Toast.LENGTH_SHORT).show()
                    }

                    true
                } else {
                    false
                }
            }

            // Handle focus loss to revert EditText to TextView
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    parent.removeViewAt(index)
                    parent.addView(binding.textViewLocationName, index)
                    binding.iconLocation.visibility = View.VISIBLE
                }
            }
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
