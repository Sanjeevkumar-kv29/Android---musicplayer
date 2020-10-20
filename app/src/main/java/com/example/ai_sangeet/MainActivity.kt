package com.example.ai_sangeet

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bumptech.glide.Glide
import com.example.ai_sangeet.songservice.MusicService
import com.example.ai_sangeet.songservice.SongAdapter
import com.example.ai_sangeet.songservice.SongViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.item_song.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager


    private lateinit var SSviewAdapter: RecyclerView.Adapter<*>
    private lateinit var SSviewManager: RecyclerView.LayoutManager


    private lateinit var mPagerViewAdapter: PagerViewAdapter
    private lateinit var runnable:Runnable
    var mylistview: ListView? = null
    lateinit var items: ArrayList<String>

    var counter = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        stitle.setEllipsize(TextUtils.TruncateAt.MARQUEE)
        stitle.setSelected(true);
        stitle.setSingleLine(true);
        stitle.setText("sheela ki jawaani ................sheela ki jawani...................sheela ki jawani..................sheela ki jawani....")





            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {
            mediaBrowser = MediaBrowserCompat(
                this,
                ComponentName(this, MusicService::class.java),
                connectionCallbacks,
                null
            )
        }



//---------------------------------------------------------------------------------------------------------------------------------------------------//

        controls.setOnTouchListener(object : OnSwipeTouchListener(this@MainActivity) {

            override fun onSwipeUp() {
                super.onSwipeUp()
                startActivity(Intent(this@MainActivity, mainplayer::class.java))
                //overridePendingTransition(R.anim.swipeup,R.anim.fadeout)
                Animatoo.animateSlideUp(this@MainActivity);
            }

            override fun onSwipeDown() {
                super.onSwipeDown()
                Toast.makeText(this@MainActivity, "Swipe down gesture detected", Toast.LENGTH_SHORT)
                    .show()
            }
        })
//onclick listner --------------------------------------------------------------------------------------------------------------------------------//



        homeBtn.setOnClickListener {
            mViewPager.currentItem = 0

        }

        searchBtn.setOnClickListener {

            mViewPager.currentItem = 1

        }


        notiBtn.setOnClickListener {
            mViewPager.currentItem = 2

        }

        AiFunBtn.setOnClickListener {
            mViewPager.currentItem = 3

        }







        /*stop.setOnClickListener {
            stop.visibility= View.GONE
            play.visibility = View.VISIBLE
            Toasty.info(this, "Pause", Toast.LENGTH_SHORT, true).show();
        }*/


        mPagerViewAdapter = PagerViewAdapter(supportFragmentManager)
        mViewPager.adapter = mPagerViewAdapter
        mViewPager.offscreenPageLimit = 4


//--------------------------------------------------------------------------------------------------------------------------------------------//
        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                changeTabs(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })




        mViewPager.currentItem = 0
        homeBtn.setImageResource(R.drawable.ic_home_pink)





    }

    private fun changeTabs(position: Int) {


        if (position == 0) {
            homeBtn.setImageResource(R.drawable.ic_home_pink)
            searchBtn.setImageResource(R.drawable.ic_search_black)
            notiBtn.setImageResource(R.drawable.ic_notifications_blck)

        }
        if (position == 1) {
            homeBtn.setImageResource(R.drawable.ic_home_black_)
            searchBtn.setImageResource(R.drawable.ic_search_pink)
            notiBtn.setImageResource(R.drawable.ic_notifications_blck)

        }
        if (position == 2) {
            homeBtn.setImageResource(R.drawable.ic_home_black_)
            searchBtn.setImageResource(R.drawable.ic_search_black)
            notiBtn.setImageResource(R.drawable.ic_notifications_fill)

        }
        if (position == 3) {
            homeBtn.setImageResource(R.drawable.ic_home_black_)
            searchBtn.setImageResource(R.drawable.ic_search_black)
            notiBtn.setImageResource(R.drawable.ic_notifications_blck)
            AiFunBtn.setImageResource(R.drawable.aicolored)

        }



//-------------------------------------------------------------------------------------------------------------------------------------------------------//



        /*override fun onSwipeLeft() {
                super.onSwipeLeft()
                Toast.makeText(this@MainActivity, "Swipe Left gesture detected",
                    Toast.LENGTH_SHORT)
                    .show()
            }
            override fun onSwipeRight() {
                super.onSwipeRight()
                Toast.makeText(
                    this@MainActivity,
                    "Swipe Right gesture detected",
                    Toast.LENGTH_SHORT
                ).show()
            }*/

    }


    public override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    public override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    public override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallbacks)
        mediaBrowser.disconnect()
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    this@MainActivity, // Context
                    token
                )

                // Save the controller
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            }

            mediaBrowser.subscribe(
                mediaBrowser.root,
                object : MediaBrowserCompat.SubscriptionCallback() {
                    override fun onChildrenLoaded(
                        parentId: String,
                        children: MutableList<MediaBrowserCompat.MediaItem>
                    ) {
                        super.onChildrenLoaded(parentId, children)

                        viewManager = LinearLayoutManager(
                            this@MainActivity,
                            RecyclerView.VERTICAL,
                            false
                        )
                        viewAdapter = SongAdapter(children, this@MainActivity)

                        SSviewManager = LinearLayoutManager(
                            this@MainActivity,
                            RecyclerView.VERTICAL,
                            false
                        )
                        SSviewAdapter = SongAdapter(children, this@MainActivity)

                        songs.apply {
                            layoutManager = viewManager
                            adapter = viewAdapter
                        }

                        songsSearch.apply {
                            layoutManager = SSviewManager
                            adapter = SSviewAdapter
                        }


                        search.queryHint = "Search"
                        /*search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }
                            override fun onQueryTextChange(newText: String?): Boolean {

                                return true
                            }
                        })*/

                        /* recyclerView.addItemDecoration(
                            DividerItemDecoration(
                                recyclerView.context,
                                DividerItemDecoration.VERTICAL
                            )
                        )*/
                    }
                })

            val mediaController = MediaControllerCompat.getMediaController(this@MainActivity)
            // Grab the view for the play/pause button
            play.setOnClickListener {
                if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.transportControls.pause()
                    play.gifResource=R.drawable.play
                } else {
                    mediaController.transportControls.play()
                    play.gifResource=R.drawable.pause
                }
            }
            /*stop.setOnClickListener {
                mediaController.transportControls.stop()
            }*/
            next.setOnClickListener {
                mediaController.transportControls.skipToNext()
                play.gifResource=R.drawable.pause
            }
            // Register a Callback to stay in sync
            mediaController.registerCallback(controllerCallbacks)



        }


    }







    private val controllerCallbacks = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Glide.with(this@MainActivity)
                .load(metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)).centerCrop()
                .into(art)
            stitle.text = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            artist.text = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            controls.visibility = View.VISIBLE
        }
    }

    fun onSongClick(holder: SongViewHolder, song: MediaBrowserCompat.MediaItem) {
        holder.song.setOnClickListener {
            mediaController.transportControls.playFromMediaId(
                song.description.mediaId,
                song.description.extras
            )

            val duration = ((song.description.extras?.getInt(MediaMetadata.METADATA_KEY_DURATION)
                    ?.div(1000)
                    ?.div(60))).toString() + ":" + ((song.description.extras?.getInt(MediaMetadata.METADATA_KEY_DURATION)
                    ?.div(1000)?.rem(60))).toString()

            sartist.setText(song.description.subtitle)
            songFixtime.setText(duration)
            startTimeCounter(song.description.extras?.getInt(MediaMetadata.METADATA_KEY_DURATION)!!)
            //Toast.makeText(this,song.description.subtitle,Toast.LENGTH_SHORT).show()
            //play.gifResource=R.drawable.pause
        }
    }

    fun onFavoriteClick(holder: SongViewHolder, song: MediaBrowserCompat.MediaItem) {
        holder.favorite.setOnClickListener {
            if (holder.favorite.background.constantState === getDrawable(R.drawable.ic_favorite_outline)?.constantState) {
                holder.favorite.background = getDrawable(R.drawable.ic_favorite)
            } else {
                holder.favorite.background = getDrawable(R.drawable.ic_favorite_outline)
            }
        }
    }


    fun startTimeCounter(duration: Int) {
        val countTime: TextView = findViewById(R.id.songTimecount)
        object : CountDownTimer(duration.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //val hours: Long = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                //val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                //val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

                val duration = millisUntilFinished?.div(1000) ?.div(60).toString() +
                        ":" +millisUntilFinished?.div(1000)?.rem(60).toString()


                //countTime.text = minutes.toString()+":"+seconds.toString()

                countTime.text = duration
                counter++
            }
            override fun onFinish() {
                //countTime.text = "Finished"
            }
        }.start()
    }

}
