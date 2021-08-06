package ru.wb.perevozka.ui.dcloading

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.wb.perevozka.databinding.DcLoadingBoxNotBelongFragmentBinding
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.utils.LogUtils
import kotlinx.parcelize.Parcelize
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
                    binding.title.text = it.title
                    LogUtils{logDebugApp("DcLoadingBoxNotBelongState " + it.code)}
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
data class DcLoadingBoxNotBelongParameters(
    val title: String, val box: String, val address: String,
) : Parcelable