package com.example.finalw1eatherapi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.finalw1eatherapi.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("Mohali")
        searchCity()
    }

    private fun searchCity() {
        val searchView = binding.searchview
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, "d940b49147264253ffdbd68f895cf97c", "metric")

        response.enqueue(object : Callback<Weatherapp> {
            override fun onResponse(call: Call<Weatherapp>, response: Response<Weatherapp>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val temperature = responseBody?.main?.temp.toString()
                    val humidity = responseBody?.main?.humidity
                    val windSpeed = responseBody?.wind?.speed
                    val sunRise = responseBody?.sys?.sunrise
                    val sunSet = responseBody?.sys?.sunset
                    val seaLevel = responseBody?.main?.pressure
                    val condition = responseBody?.weather?.firstOrNull()?.main ?: "unknown"
                    val maxTemp = responseBody?.main?.temp_max
                    val minTemp = responseBody?.main?.temp_min

                    binding.temperature.text = "$temperature °C"
                    binding.weather.text = condition
                    binding.min.text = "Min Temp: $minTemp °C"
                    binding.max.text = "Max Temp: $maxTemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.wind.text = "$windSpeed m/s"
                    binding.sunrise.text = convertUnixTimeToTime(sunRise)
                    binding.sunset.text = convertUnixTimeToTime(sunSet)
                    binding.sea.text = "$seaLevel hPa"
                    binding.sunny.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = currentDate()
                    binding.cityname.text = cityName

                    changeWeather(condition)
                }
            }

            override fun onFailure(call: Call<Weatherapp>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun changeWeather(conditions: String) {
        val currentTimeMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val isDaytime = hour >= 6 && hour < 18

        when (conditions) {
            "Clear", "Sky", "Sunny" -> {
                if (isDaytime) {
                    binding.root.setBackgroundResource(R.drawable.sunnybg)
                    binding.anim.setAnimation(R.raw.sun)
                } else {
                    binding.root.setBackgroundResource(R.drawable.ngt)
                binding.anim.setAnimation(R.raw.moon)
                }
            }
            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                if (isDaytime) {
                    binding.root.setBackgroundResource(R.drawable.colud_background)
                    binding.anim.setAnimation(R.raw.cloud)
                } else {
                    binding.root.setBackgroundResource(R.drawable.cloudy_night)
                    binding.anim.setAnimation(R.raw.clearmoon)
                }
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.anim.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.anim.setAnimation(R.raw.snow)
            }
            else -> {
                if (isDaytime) {
                    binding.root.setBackgroundResource(R.drawable.sunny_background)
                    binding.anim.setAnimation(R.raw.sun)
                } else {
                    binding.root.setBackgroundResource(R.drawable.ngt)
                   binding.anim.setAnimation(R.raw.clearmoon)
                }
            }
        }
        binding.anim.playAnimation()
    }

    private fun convertUnixTimeToTime(unixTime: Int?): String {
        return unixTime?.let {
            val date = Date(it.toLong() * 1000)
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            format.format(date)
        } ?: "N/A"
    }

    private fun dayName(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("EEEE", Locale.getDefault())
        return format.format(date)
    }

    private fun currentDate(): String {
        val date = Date()
        val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return format.format(date)
    }
}
