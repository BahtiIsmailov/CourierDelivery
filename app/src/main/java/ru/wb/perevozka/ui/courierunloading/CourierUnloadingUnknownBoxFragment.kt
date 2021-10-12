package ru.wb.perevozka.ui.courierunloading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.databinding.CourierLoadingUnknownBoxFragmentBinding
import ru.wb.perevozka.databinding.CourierUnloadingUnknownBoxFragmentBinding
import ru.wb.perevozka.ui.splash.NavToolbarListener

class CourierUnloadingUnknownBoxFragment : Fragment() {

    private val viewModel by viewModel<CourierUnloadingUnknownBoxViewModel>()

    private var _binding: CourierUnloadingUnknownBoxFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierUnloadingUnknownBoxFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToBack.observe(viewLifecycleOwner) { findNavController().popBackStack() }

        binding.understand.setOnClickListener {
            viewModel.onUnderstandClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}