package ru.wb.go.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.databinding.SettingsFragmentBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener

class SettingsFragment : Fragment() {

    private val viewModel by viewModel<SettingsViewModel>()

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initListener()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.settings_title)
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.flashSwitch.isChecked = viewModel.getSetting(AppPreffsKeys.SETTING_START_FLASH_ON, false)
        binding.voiceSwitch.isChecked = viewModel.getSetting(AppPreffsKeys.SETTING_VOICE_SCAN, true)
        binding.scannerAutoOff.isChecked = viewModel.getSetting(AppPreffsKeys.SETTING_SANNER_OFF, false)
    }

    private fun initObserver() {

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            val ic = when (it) {
                is NetworkState.Complete -> R.drawable.ic_inet_complete
                else -> R.drawable.ic_inet_failed
            }
            binding.toolbarLayout.noInternetImage.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), ic)
            )
        }

        viewModel.versionApp.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarVersion.text = it
        }

    }

    private fun initListener() {
        binding.voiceSwitch.setOnCheckedChangeListener { _, b ->
            viewModel.settingClick(AppPreffsKeys.SETTING_VOICE_SCAN, b)
        }
        binding.flashSwitch.setOnCheckedChangeListener { _, b ->
            viewModel.settingClick(AppPreffsKeys.SETTING_START_FLASH_ON, b)
        }
        binding.scannerAutoOff.setOnCheckedChangeListener { _, b ->
            viewModel.settingClick(AppPreffsKeys.SETTING_SANNER_OFF, b)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}