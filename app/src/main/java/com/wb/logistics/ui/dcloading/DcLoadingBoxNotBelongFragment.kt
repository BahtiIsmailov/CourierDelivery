package com.wb.logistics.ui.dcloading

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.DcLoadingBoxNotBelongFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import kotlinx.android.parcel.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DcLoadingBoxNotBelongFragment : Fragment() {

    private val viewModel by viewModel<DcLoadingBoxNotBelongViewModel> {
        parametersOf(requireArguments().getParcelable<DcLoadingBoxNotBelongParameters>(
            BOX_NOT_BELONG_KEY))
    }

    private var _binding: DcLoadingBoxNotBelongFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcLoadingBoxNotBelongFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.belongInfo.observe(viewLifecycleOwner) {
            when (it) {
                is DcLoadingBoxNotBelongState.BelongInfo -> {
                    (activity as NavToolbarTitleListener).updateTitle(it.toolbarTitle)
                    binding.title.text = it.title
                    binding.code.text = it.code
                    binding.address.text = it.address
                    if (it.isShowAddress) {
                        binding.titleAddress.visibility = VISIBLE
                        binding.address.visibility = VISIBLE
                    }
                }
            }
        }

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
        (activity as NavToolbarTitleListener).hideToolbar()
    }

    private fun showToolbar() {
        (activity as NavToolbarTitleListener).showToolbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val BOX_NOT_BELONG_KEY = "box_not_belong_key"
    }

}

@Parcelize
data class DcLoadingBoxNotBelongParameters(
    val toolbarTitle: String, val title: String, val box: String, val address: String,
) : Parcelable