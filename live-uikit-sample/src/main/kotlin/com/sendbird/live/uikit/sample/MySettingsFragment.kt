package com.sendbird.live.uikit.sample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.sendbird.live.SendbirdLive
import com.sendbird.live.uikit.SendbirdLiveUIKit
import com.sendbird.live.uikit.sample.databinding.FragmentMySettingsBinding

class MySettingsFragment : Fragment() {
    lateinit var binding: FragmentMySettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = SendbirdLive.currentUser ?: return
        binding.ivProfile.load(user.profileUrl)
        binding.tvUserName.text = user.nickname.ifEmpty { user.userId }
        binding.tvUserId.text = user.userId
        binding.buttonSignOut.setOnClickListener {
            SendbirdLiveUIKit.disconnect {
                startActivity(Intent(requireContext(), AuthenticationActivity::class.java))
                activity?.finish()
            }
        }

        binding.clApplicationInformation.setOnClickListener {
            startActivity(Intent(requireContext(), ApplicationInformationActivity::class.java))
        }
    }
}
