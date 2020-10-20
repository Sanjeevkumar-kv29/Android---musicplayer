package com.example.ai_sangeet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ai_sangeet.Fragments.HomeFragment
import com.example.ai_sangeet.Fragments.NotificationFragment
import com.example.ai_sangeet.Fragments.ProfileFragment
import com.example.ai_sangeet.Fragments.SearchFragment

internal class PagerViewAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
    override fun getItem(position: Int): Fragment {

        return when (position) {
            0 -> {
                HomeFragment()
            }
            1 -> {
                SearchFragment()
            }
            2 -> {
                NotificationFragment()
            }
            else -> ProfileFragment()
        }
    }

    override fun getCount(): Int {

        return 4
    }

}
