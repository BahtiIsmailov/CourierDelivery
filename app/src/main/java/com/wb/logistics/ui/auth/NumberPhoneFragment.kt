package com.wb.logistics.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.R
import com.wb.logistics.databinding.AuthNumberPhoneFragmentBinding
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class NumberPhoneFragment : Fragment(R.layout.auth_number_phone_fragment) {

    private var _binding: AuthNumberPhoneFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethod: InputMethodManager
    private val viewModel by viewModel<NumberPhoneViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AuthNumberPhoneFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        initInputMethod()
        initStateObserve()
    }

    private fun initInputMethod() {
        inputMethod =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initStateObserve() {
        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                NumberPhoneUIState.PhoneCheck -> {
                    binding.phoneNumber.isEnabled = false
                    binding.numberNotFound.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.PROGRESS)
                    inputMethod.hideSoftInputFromWindow(binding.next.windowToken, 0)
                }
                is NumberPhoneUIState.NavigateToInputPassword ->
                    findNavController().navigate(
                        NumberPhoneFragmentDirections.actionNumberPhoneFragmentToInputPasswordFragment(
                            InputPasswordParameters(state.number)
                        )
                    )
                is NumberPhoneUIState.NavigateToTemporaryPassword ->
                    findNavController().navigate(
                        NumberPhoneFragmentDirections.actionNumberPhoneFragmentToTemporaryPasswordFragment(
                            TemporaryPasswordParameters(state.number)
                        )
                    )
                NumberPhoneUIState.NavigateToConfig ->
                    findNavController().navigate(R.id.configActivity)
                is NumberPhoneUIState.NumberNotFound -> {
                    binding.phoneNumber.isEnabled = true
                    binding.numberNotFound.text = state.message
                    binding.numberNotFound.visibility = VISIBLE
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is NumberPhoneUIState.Error -> {
                    showBarMessage(state.message)
                    binding.phoneNumber.isEnabled = true
                    binding.numberNotFound.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is NumberPhoneUIState.NumberFormat -> {
                    val phoneNumber = state.number
                    binding.phoneNumber.setText(phoneNumber)
                    binding.phoneNumber.setSelection(phoneNumber.length)
                    binding.numberNotFound.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                NumberPhoneUIState.NumberFormatComplete -> {
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                NumberPhoneUIState.Empty -> {
                }
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showBarMessage(state: String) {
        Snackbar.make(binding.next, state, Snackbar.LENGTH_LONG).show()
    }

    private fun initListener() {
        binding.title.setOnLongClickListener {
            viewModel.action(NumberPhoneUIAction.LongTitle)
            true
        }
        val phone = binding.phoneNumber
        viewModel.action(NumberPhoneUIAction.NumberChanges(phone.textChanges()))
        binding.next.setOnClickListener { viewModel.action(NumberPhoneUIAction.CheckPhone(phone.text.toString())) }
    }

    private fun initViews() {
        binding.next.setState(ProgressImageButtonMode.DISABLED)
    }

}
