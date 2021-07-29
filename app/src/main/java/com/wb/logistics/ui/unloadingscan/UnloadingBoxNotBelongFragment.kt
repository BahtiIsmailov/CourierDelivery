package com.wb.logistics.ui.unloadingscan

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.UnloadingBoxNotBelongFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarListener
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UnloadingBoxNotBelongFragment : Fragment() {

    private val viewModel by viewModel<UnloadingBoxNotBelongModel> {
        parametersOf(requireArguments().getParcelable<UnloadingBoxNotBelongParameters>(
            BOX_NOT_BELONG_KEY))
    }

    private var _binding: UnloadingBoxNotBelongFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = UnloadingBoxNotBelongFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.belongInfo.observe(viewLifecycleOwner) {
            when (it) {
                is UnloadingBoxNotBelongState.BelongInfo -> {
                    binding.title.text = it.title
                    binding.description.text = it.description
                    binding.code.text = it.code
                    binding.address.text = it.address
                    if (it.isShowAddress) {
                        binding.titleAddress.visibility = View.VISIBLE
                        binding.address.visibility = View.VISIBLE
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
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun showToolbar() {
        (activity as NavToolbarListener).showToolbar()
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
data class UnloadingBoxNotBelongParameters(
    val title: String, val description: String, val box: String, val address: String,
) : Parcelable