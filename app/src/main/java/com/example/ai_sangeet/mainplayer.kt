package com.example.ai_sangeet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlinx.android.synthetic.main.activity_mainplayer.*

class mainplayer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainplayer)


        mainplayerlyt.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                super.onSwipeDown()
                startActivity(Intent(this@mainplayer,MainActivity::class.java))
                Animatoo.animateSlideDown(this@mainplayer);
            }
        })
    }
}
