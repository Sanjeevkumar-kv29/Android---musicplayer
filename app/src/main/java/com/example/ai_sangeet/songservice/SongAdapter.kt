package com.example.ai_sangeet.songservice


import android.media.MediaMetadata
import android.support.v4.media.MediaBrowserCompat
import android.text.format.Formatter.formatFileSize
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ai_sangeet.Fragments.HomeFragment
import com.example.ai_sangeet.MainActivity
import com.example.ai_sangeet.R

class SongAdapter(
    private var songList: MutableList<MediaBrowserCompat.MediaItem>,
    var mainActivity: MainActivity
) :
    RecyclerView.Adapter<SongViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        // create a new view
        val songView = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(songView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val duration =
            ((songList[position].description.extras?.getInt(MediaMetadata.METADATA_KEY_DURATION)
                ?.div(1000)
                ?.div(60))).toString() + ":" + ((songList[position].description.extras?.getInt(
                MediaMetadata.METADATA_KEY_DURATION
            )
                ?.div(1000)?.rem(60))).toString()
        holder.title.text = songList[position].description.extras?.getString(MediaMetadata.METADATA_KEY_TITLE)
        holder.artist.text = songList[position].description.extras?.getString(MediaMetadata.METADATA_KEY_ARTIST)
        Glide.with(mainActivity)
            .load(songList[position].description.extras?.getString(MediaMetadata.METADATA_KEY_ART_URI))
            .placeholder(R.drawable.roboimgplaceholder).centerCrop().into(holder.art)
        holder.duration.text = duration
        holder.size.text = songList[position].description.extras?.getInt("SIZE")?.toLong()?.let {
            formatFileSize(
                mainActivity,
                it
            )
        }

        mainActivity.onSongClick(holder, songList[position])
        mainActivity.onFavoriteClick(holder, songList[position])
    }

    override fun getItemCount() = songList.size
}
