package ru.wb.go.ui.courieragreement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierAgreementFragmentBinding
import ru.wb.go.ui.splash.NavToolbarListener


class CourierAgreementFragment : Fragment(R.layout.courier_agreement_fragment) {

    private var _binding: CourierAgreementFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<CourierAgreementViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierAgreementFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initAgreement()
        initListeners()
    }

    private fun initListeners() {
//        binding.cancel.setOnClickListener { viewModel.onCancelClick() }
        binding.confirm.setOnClickListener { viewModel.onCompleteClick() }
    }

    private fun initAgreement() {
        val input = requireContext().resources.openRawResource(R.raw.agreement_wbgo)
        binding.pdfView.fromStream(input).load()
        binding.viewProgress.visibility = GONE
    }

    private fun initView() {
        (activity as NavToolbarListener).hideBackButton()
    }

    private fun initObserver() {


        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierAgreementNavigationState.Complete -> {
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
