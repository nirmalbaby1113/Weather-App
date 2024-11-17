package com.nirmal.baby.weatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nirmal.baby.weatherapp.R
import com.nirmal.baby.weatherapp.data.WeatherItemsHourly

class HorizontalRecyclerAdapter(
    private var weatherItems: List<WeatherItemsHourly>
) : RecyclerView.Adapter<HorizontalRecyclerAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val temperatureTextView: TextView = view.findViewById(R.id.textViewTemperature)
        val weatherIconImageView: ImageView = view.findViewById(R.id.imageViewWeatherIcon)
        val timeTextView: TextView = view.findViewById(R.id.textViewTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hourly_recycler_item, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val item = weatherItems[position]
        holder.temperatureTextView.text = item.temperature
        holder.weatherIconImageView.setImageResource(item.iconResId)
        holder.timeTextView.text = item.time
    }

    override fun getItemCount(): Int = weatherItems.size

    // To update data dynamically
    fun updateData(newItems: List<WeatherItemsHourly>) {
        weatherItems = newItems
        notifyDataSetChanged()
    }
}