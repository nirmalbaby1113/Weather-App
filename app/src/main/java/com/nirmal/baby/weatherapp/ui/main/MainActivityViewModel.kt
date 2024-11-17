package com.nirmal.baby.weatherapp.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nirmal.baby.weatherapp.data.WeatherRepository
import com.nirmal.baby.weatherapp.data.WeatherResponse
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val repository = WeatherRepository()

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> get() = _weatherData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>() // Loading state
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchWeather(location: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("MainActivityViewModel", "try")
                val response = repository.fetchWeatherData(location)
                _weatherData.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
                Log.d("MainActivityViewModel", "catch: ${_error.value}")
            } finally {
                _isLoading.value = false // Hide ProgressBar
            }
        }
    }
}