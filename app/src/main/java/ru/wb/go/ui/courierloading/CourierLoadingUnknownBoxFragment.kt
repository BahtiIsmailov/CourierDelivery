package ru.wb.go.ui.courierloading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.databinding.CourierLoadingUnknownBoxFragmentBinding

class CourierLoadingUnknownBoxFragment : Fragment() {

    private val viewModel by viewModel<CourierLoadingUnknownBoxViewModel>()

    private var _binding: CourierLoadingUnknownBoxFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierLoadingUnknownBoxFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToBack.observe(viewLifecycleOwner) { findNavController().popBackStack() }

        binding.understand.setOnClickListener { viewModel.onUnderstandClick() }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}