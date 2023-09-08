package com.sendbird.live.audioliveeventsample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.databinding.ActivityMainBinding
import com.sendbird.live.audioliveeventsample.view.fragment.LiveEventListFragment
import com.sendbird.live.audioliveeventsample.view.fragment.MySettingsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val liveEventListFragment = LiveEventListFragment()
    private val mySettingsFragment = MySettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuButtonLiveEvents -> attachLiveEventListFragment()
                R.id.menuButtonMySettings -> attachMySettingsFragment()
            }
            return@setOnItemSelectedListener true
        }
        attachLiveEventListFragment()
    }

    private fun attachLiveEventListFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainerView, liveEventListFragment)
        }
    }

    private fun attachMySettingsFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainerView, mySettingsFragment)
        }
    }
}