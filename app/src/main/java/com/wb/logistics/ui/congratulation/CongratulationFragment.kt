package com.wb.logistics.ui.congratulation

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.CongratulationFragmentBinding
import kotlinx.android.parcel.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CongratulationFragment : Fragment() {

    private val viewModel by viewModel<CongratulationViewModel> {
        parametersOf(requireArguments().getParcelable<CongratulationParameters>(CONGRATULATION_KEY))
    }

    private var _binding: CongratulationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CongratulationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
    }

    private fun initObserver() {

        viewModel.infoState.observe(viewLifecycleOwner) {
            binding.info.text = it
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        binding.complete.setOnClickListener {
            viewModel.onCompleteClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val CONGRATULATION_KEY = "congratulation_key"
    }

}

@Parcelize
data class CongratulationParameters(val defaultId: Int = 0) : Parcelable