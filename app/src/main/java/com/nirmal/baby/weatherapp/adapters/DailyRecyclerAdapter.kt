package com.nirmal.baby.weatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nirmal.baby.weatherapp.R
import com.nirmal.baby.weatherapp.data.WeatherItemsDaily

class DailyRecyclerAdapter(private var dailyData: List<WeatherItemsDaily>) :
    RecyclerView.Adapter<DailyRecyclerAdapter.DailyViewHolder>() {

    class DailyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.textViewDate)
        val icon: ImageView = itemView.findViewById(R.id.imageViewDailyIcon)
        val highLow: TextView = itemView.findViewById(R.id.textViewDailyHighLow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.daily_forecast_item, parent, false)
        return DailyViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        val item = dailyData[position]
        holder.date.text = item.date
        holder.highLow.text = "${item.high}°/${item.low}°"

        Glide.with(holder.icon.context)
            .load(item.iconUrl)
            .into(holder.icon)
    }

    override fun getItemCount(): Int = dailyData.size

    fun updateData(newData: List<WeatherItemsDaily>) {
        dailyData = newData
        notifyDataSetChanged()
    }
}

