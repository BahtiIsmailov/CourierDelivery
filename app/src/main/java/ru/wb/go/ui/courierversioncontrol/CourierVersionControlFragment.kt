package ru.wb.go.ui.courierversioncontrol

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.databinding.CourierVersionControlFragmentBinding
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener

class CourierVersionControlFragment : Fragment() {

    private val viewModel by viewModel<CourierVersionControlViewModel>()

    private var _binding: CourierVersionControlFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierVersionControlFragmentBinding.inflate(inflater, container, false)
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
    }

    private fun initObserver() {
        viewModel.navigateToBack.observe(viewLifecycleOwner) {}

        viewModel.versionTitleState.observe(viewLifecycleOwner) {
            binding.versionTitle.text = it
        }

        viewModel.updateFromGooglePlay.observe(viewLifecycleOwner) {
            showGoogleStore(it.uriPlayMarket, it.uriPlayMarket)
        }

    }

    private fun showGoogleStore(uriPlayMarket: String, uriGoogle: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriPlayMarket)))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriGoogle)))
        }
    }

    private fun initListener() {
        binding.completeDeliveryButton.setOnClickListener {
            viewModel.onUpdateClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}