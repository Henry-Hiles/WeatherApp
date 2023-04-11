package com.henryhiles.qweather.presentation.screenmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import com.henryhiles.qweather.R
import com.henryhiles.qweather.domain.location.LocationTracker
import com.henryhiles.qweather.domain.repository.WeatherRepository
import com.henryhiles.qweather.domain.util.Resource
import com.henryhiles.qweather.domain.weather.HourlyWeatherInfo
import kotlinx.coroutines.launch

data class HourlyWeatherState(
    val hourlyWeatherInfo: HourlyWeatherInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val expanded: Int? = null
)

class HourlyWeatherScreenModel constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val context: Context
) : ScreenModel {
    var state by mutableStateOf(HourlyWeatherState())
        private set

    fun loadWeatherInfo(cache: Boolean = true) {
        coroutineScope.launch {
            state = state.copy(isLoading = true, error = null)
            val currentLocation = locationTracker.getCurrentLocation()
            currentLocation?.let { location ->
                state = when (val result =
                    repository.getHourlyWeatherData(
                        lat = location.latitude,
                        long = location.longitude,
                        cache = cache
                    )) {
                    is Resource.Success -> {
                        state.copy(
                            hourlyWeatherInfo = result.data,
                            isLoading = false,
                            error = null
                        )
                    }

                    is Resource.Error -> {
                        state.copy(
                            hourlyWeatherInfo = null,
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            } ?: kotlin.run {
                state = state.copy(
                    isLoading = false,
                    error = context.getString(R.string.error_location)
                )
            }
        }
    }
}