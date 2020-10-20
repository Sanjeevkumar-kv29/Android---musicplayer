package com.example.ai_sangeet.songservice


import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_song.view.*

class SongViewHolder(item: View) : RecyclerView.ViewHolder(item) {
    val song: ViewGroup = item.song
    val title: TextView = item.title
    val artist: TextView = item.artist
    val art: ImageView = item.art
    val duration: TextView = item.duration
    val size: TextView = item.size
    val favorite: ImageButton = item.favorite
}