package com.sendbird.live.videoliveeventsample.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import coil.load
import com.sendbird.live.SendbirdLive
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.databinding.FragmentMySettingsBinding
import com.sendbird.live.videoliveeventsample.util.PrefManager
import com.sendbird.live.videoliveeventsample.view.SignInManuallyActivity

class MySettingsFragment : BaseFragment<FragmentMySettingsBinding>(FragmentMySettingsBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.ivProfile.load(SendbirdLive.currentUser?.profileUrl) {
            crossfade(true)
            placeholder(R.drawable.icon_user)
            error(R.drawable.icon_user)
        }
        binding.tvUserId.text = SendbirdLive.currentUser?.userId ?: ""
        binding.tvNickName.text = SendbirdLive.currentUser?.nickname ?: ""
        binding.tvAppId.text = SendbirdLive.applicationId
        binding.btnSignOut.setOnClickListener { signOut() }
    }

    private fun signOut() {
        SendbirdLive.deauthenticate(null)
        val prefManager = PrefManager(requireContext())
        prefManager.removeAll()
        startActivity(Intent(requireActivity(), SignInManuallyActivity::class.java))
        requireActivity().finishAffinity()
    }
}