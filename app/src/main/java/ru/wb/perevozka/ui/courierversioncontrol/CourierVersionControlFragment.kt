package ru.wb.perevozka.ui.courierversioncontrol

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.databinding.CourierVersionControlFragmentBinding
import ru.wb.perevozka.ui.splash.NavDrawerListener
import ru.wb.perevozka.ui.splash.NavToolbarListener

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
        (activity as NavDrawerListener).lock()
    }

    private fun initObserver() {
        viewModel.navigateToBack.observe(viewLifecycleOwner) {}
    }

    private fun showGoogleStore(uriPlayMarket: String, uriGoogle: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriPlayMarket)))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriGoogle)))
        }
    }

    private fun initListener() {
        binding.completeDelivery.setOnClickListener {
            viewModel.onUpdateClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}