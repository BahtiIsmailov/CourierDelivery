package com.wb.logistics.ui.unloading

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.UnloadingBoxNotBelongFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import kotlinx.android.parcel.Parcelize
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

                    (activity as NavToolbarTitleListener).updateTitle(it.toolbarTitle)

                    binding.title.text = it.title
                    binding.code.text = it.code
                    binding.address.text = it.address
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
        const val BOX_NOT_BELONG_KEY = "box_not_belong_key"
    }

}

@Parcelize
data class UnloadingBoxNotBelongParameters(
    val toolbarTitle: String, val title: String, val box: String, val address: String,
) : Parcelable