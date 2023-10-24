package com.sendbird.live.uikit.sample

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.sendbird.live.uikit.fragments.LiveEventListFragment
import com.sendbird.live.uikit.sample.databinding.ActivitySendbirdliveBinding

class SendbirdLiveActivity : AppCompatActivity() {
    lateinit var binding: ActivitySendbirdliveBinding
    private val liveEventListFragment = LiveEventListFragment.Builder().build()
    private val mySettingsFragment = MySettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendbirdliveBinding.inflate(LayoutInflater.from(this))
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
