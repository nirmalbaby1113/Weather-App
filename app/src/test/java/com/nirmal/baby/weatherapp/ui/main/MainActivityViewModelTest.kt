package com.nirmal.baby.weatherapp.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nirmal.baby.weatherapp.data.Condition
import com.nirmal.baby.weatherapp.data.CurrentWeather
import com.nirmal.baby.weatherapp.data.Forecast
import com.nirmal.baby.weatherapp.data.Location
import com.nirmal.baby.weatherapp.data.WeatherRepository
import com.nirmal.baby.weatherapp.data.WeatherResponse
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MainActivityViewModelTest {

    private lateinit var viewModel: MainActivityViewModel
    private val repository = mockk<WeatherRepository>(relaxed = true)
    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = MainActivityViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchWeather updates weatherData on success`() = runTest {
        val mockResponse = WeatherResponse(
            location = Location("London", "Ontario", "Canada", 0.0, 0.0, "America/Toronto", "2024-11-24 10:00"),
            current = CurrentWeather(15.0, 59.0, 1, Condition("Cloudy", "//cdn.weatherapi.com/icon.png", 1003), 10.0, 16.0, 180, "S", 80, 75, 15.0, 59.0, 15.0, 59.0, 0.0, 0.0, 5.0),
            forecast = Forecast(emptyList())
        )
        coEvery { repository.fetchWeatherData(any()) } returns mockResponse

        viewModel.fetchWeather("London, Ontario")

        advanceUntilIdle() // Ensure coroutines complete

        assertEquals(mockResponse, viewModel.weatherData.value)
    }

    @Test
    fun `fetchWeather updates error on failure`() = runTest {
        coEvery { repository.fetchWeatherData(any()) } throws Exception("Network error")

        viewModel.fetchWeather("London, Ontario")

        advanceUntilIdle() // Ensure coroutines complete

        assertEquals("Network error", viewModel.error.value)
    }
}


