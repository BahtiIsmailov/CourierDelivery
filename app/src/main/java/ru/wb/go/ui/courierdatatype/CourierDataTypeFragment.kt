package ru.wb.go.ui.courierdatatype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.databinding.CourierDataTypeFragmentBinding
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.courierdata.CourierDataFragment
import ru.wb.go.ui.courierdata.CourierDataParameters
import ru.wb.go.utils.SoftKeyboard

class CourierDataTypeFragment : Fragment() {

    private val viewModel by viewModel<CourierDataTypeViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierDataParameters>(CourierDataFragment.REGISTER_FORM_PARAMS)
        )
    }

    private var _binding: CourierDataTypeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierDataTypeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initListener()
        initKeyboard()
    }

    private fun initKeyboard() {
        SoftKeyboard.hideKeyBoard(requireActivity())
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initObserver() {

        viewModel.switchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierDataTypeSwitchState.IsIP -> binding.radioIp.isChecked = true
                CourierDataTypeSwitchState.IsSelfEmployed -> binding.radioSelfEmployed.isChecked =
                    true
                CourierDataTypeSwitchState.IsEmpty -> {
                    binding.radioGroup.clearCheck()
                    binding.completeType.isEnabled = false
                }
            }
        }

        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierDataTypeNavAction.NavigateToCourierData ->
                    findNavController().navigate(
                        CourierDataTypeFragmentDirections
                            .actionCourierDataTypeFragmentToCourierDataFormFragment(
                                CourierDataParameters(
                                    phone = state.phone,
                                    docs = state.docs
                                )
                            )
                    )
            }
        }
    }

    private fun initListener() {
        binding.radioGroup.setOnCheckedChangeListener { compoundButton, b ->
            binding.completeType.isEnabled = true
        }
        binding.completeType.setOnClickListener { viewModel.onUpdateStatusClick(binding.radioSelfEmployed.isChecked) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}