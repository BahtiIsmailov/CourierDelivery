package ru.wb.perevozka.ui.couriercompletedelivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.databinding.CourierCompleteDeliveryFragmentBinding
import ru.wb.perevozka.ui.splash.NavToolbarListener

class CourierCompleteDeliveryFragment : Fragment() {

    private val viewModel by viewModel<CourierCompleteDeliveryViewModel>()

    private var _binding: CourierCompleteDeliveryFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = CourierCompleteDeliveryFragmentBinding.inflate(inflater, container, false)
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
    }

    private fun initObserver() {

        viewModel.infoState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierCompleteDeliveryState.InfoDelivery -> {
                    binding.earnedCount.text = it.amount
                    binding.deliveredCount.text = it.deliveredCount
                }
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().navigate(CourierCompleteDeliveryFragmentDirections.actionCourierCompleteDeliveryFragmentToCourierLoaderFragment())
        }

    }

    private fun initListener() {
        binding.completeDelivery.setOnClickListener {
            viewModel.onCompleteDeliveryClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}