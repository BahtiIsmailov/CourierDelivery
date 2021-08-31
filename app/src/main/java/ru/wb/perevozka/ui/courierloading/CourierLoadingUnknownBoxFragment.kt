package ru.wb.perevozka.ui.courierloading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.databinding.CourierLoadingUnknownBoxFragmentBinding
import ru.wb.perevozka.ui.splash.NavToolbarListener

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

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            showToolbar()
            findNavController().popBackStack()
        }

        binding.understand.setOnClickListener {
            viewModel.onUnderstandClick()
        }

        hideToolbar()
    }

    private fun hideToolbar() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun showToolbar() {
        (activity as NavToolbarListener).showToolbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}