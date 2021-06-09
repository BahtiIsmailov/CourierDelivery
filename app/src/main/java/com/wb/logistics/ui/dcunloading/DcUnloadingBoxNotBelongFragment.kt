package com.wb.logistics.ui.dcunloading

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.DcUnloadingBoxNotBelongFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarListener
import kotlinx.android.parcel.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DcUnloadingBoxNotBelongFragment : Fragment() {

    private val viewModel by viewModel<DcUnloadingBoxNotBelongModel> {
        parametersOf(requireArguments().getParcelable<DcUnloadingBoxNotBelongParameters>(
            DC_BOX_NOT_BELONG_KEY))
    }

    private var _binding: DcUnloadingBoxNotBelongFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcUnloadingBoxNotBelongFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.belongInfo.observe(viewLifecycleOwner) {
            when (it) {
                is DcUnloadingBoxNotBelongState.BelongInfo -> {
                    (activity as NavToolbarListener).updateTitle(it.toolbarTitle)
                }
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        binding.understand.setOnClickListener {
            viewModel.onUnderstandClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val DC_BOX_NOT_BELONG_KEY = "dc_box_not_belong_key"
    }

}

@Parcelize
data class DcUnloadingBoxNotBelongParameters(val toolbarTitle: String) :
    Parcelable