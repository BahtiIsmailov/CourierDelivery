package ru.wb.perevozka.ui.auth

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.AuthPhoneFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.ui.splash.NavDrawerListener
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressButtonMode

class NumberPhoneFragment : Fragment(R.layout.auth_phone_fragment) {

    private var _binding: AuthPhoneFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<NumberPhoneViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthPhoneFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initStateObserve()
    }

    private fun phoneSpannable(state: NumberPhoneUIState.PhoneSpanFormat): Spannable {
        val phone = state.numberFormat
        val spannable: Spannable = SpannableString(phone)
        val first = 0
        val last = state.count
        val span = ForegroundColorSpan(
            ResourcesCompat.getColor(resources, R.color.text_spannable_phone, null)
        )
        spannable.setSpan(span, first, last, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun initViews() {
        binding.next.setState(ProgressButtonMode.DISABLE)
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lock()
        binding.toolbarLayout.back.visibility = INVISIBLE
    }

    private fun initListeners() {

        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }

        binding.loginLayout.setOnLongClickListener {
            viewModel.onLongClick()
            true
        }

        with(binding.phoneNumberTitle) {
            binding.next.setOnClickListener { viewModel.onCheckPhone(text.toString()) }
        }

        viewModel.onNumberObservableClicked(binding.viewKeyboard.observableListener)

    }

    private fun initStateObserve() {
        viewModel.navigationEvent.observe(viewLifecycleOwner, { state ->
            when (state) {
                is NumberPhoneNavAction.NavigateToCheckPassword -> {
                    findNavController().navigate(
                        NumberPhoneFragmentDirections.actionAuthNumberPhoneFragmentToCheckSmsFragment(
                            CheckSmsParameters(state.number, state.ttl)
                        )
                    )
                }
                NumberPhoneNavAction.NavigateToConfig ->
                    findNavController().navigate(R.id.authConfigActivity)
            }
        })

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE
                }

                is NetworkState.Complete -> {
                    binding.toolbarLayout.noInternetImage.visibility = INVISIBLE
                }
            }
        }

        viewModel.stateBackspaceUI.observe(viewLifecycleOwner) {
            when (it) {
                NumberPhoneBackspaceUIState.Active -> binding.viewKeyboard.active()
                NumberPhoneBackspaceUIState.Inactive -> binding.viewKeyboard.inactive()
            }
        }

        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                NumberPhoneUIState.NumberCheckProgress -> {
                    binding.numberNotFound.visibility = GONE
                    binding.viewKeyboard.lock()
                    binding.viewKeyboard.inactive()
                    binding.next.setState(ProgressButtonMode.PROGRESS)
                }
                NumberPhoneUIState.NumberFormatComplete -> {
                    binding.next.setState(ProgressButtonMode.ENABLE)
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                }
                NumberPhoneUIState.NumberNotFilled -> {
                    binding.next.setState(ProgressButtonMode.DISABLE)
                    binding.numberNotFound.visibility = INVISIBLE
                }
                is NumberPhoneUIState.NumberNotFound -> {
                    showDialog(
                        DialogStyle.WARNING.ordinal,
                        state.title,
                        state.message,
                        state.button
                    )
                    binding.next.setState(ProgressButtonMode.ENABLE)
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                }
                is NumberPhoneUIState.Error -> {
                    showDialog(
                        DialogStyle.WARNING.ordinal, state.title,
                        state.message,
                        state.button
                    )
                    binding.next.setState(ProgressButtonMode.ENABLE)
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                }
                is NumberPhoneUIState.PhoneSpanFormat -> {
                    binding.phoneNumberTitle.setText(
                        phoneSpannable(state),
                        TextView.BufferType.SPANNABLE
                    )
                }
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialog(style: Int, title: String, message: String, positiveButtonName: String) {
        DialogInfoFragment.newInstance(style, title, message, positiveButtonName)
            .show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

}