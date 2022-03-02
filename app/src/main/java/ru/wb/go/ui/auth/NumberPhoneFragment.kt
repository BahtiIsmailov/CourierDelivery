package ru.wb.go.ui.auth

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
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.AuthPhoneFragmentBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.app.StatusBarListener
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.utils.SoftKeyboard
import ru.wb.go.views.ProgressButtonMode

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
            ResourcesCompat.getColor(resources, R.color.primary, null)
        )
        spannable.setSpan(span, first, last, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun initViews() {
        binding.next.setState(ProgressButtonMode.DISABLE)
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
        (activity as StatusBarListener).showStatusBar()
        binding.toolbarLayout.back.visibility = INVISIBLE

        SoftKeyboard.hideKeyBoard(requireActivity())
    }

    private fun initListeners() {

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
        viewModel.navigationEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NumberPhoneNavAction.NavigateToCheckPassword -> {
                    findNavController().navigate(
                        NumberPhoneFragmentDirections.actionAuthNumberPhoneFragmentToCheckSmsFragment(
                            CheckSmsParameters(state.number, state.ttl)
                        )
                    )
                }
                else -> {}
            }
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            val ic = when (it) {
                is NetworkState.Complete -> R.drawable.ic_inet_complete
                else -> R.drawable.ic_inet_failed
            }
            binding.toolbarLayout.noInternetImage.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), ic)
            )
        }

        viewModel.versionApp.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarVersion.text = it
        }

        viewModel.stateBackspaceUI.observe(viewLifecycleOwner) {
            when (it) {
                NumberPhoneBackspaceUIState.Active -> binding.viewKeyboard.active()
                NumberPhoneBackspaceUIState.Inactive -> binding.viewKeyboard.inactive()
            }
        }

        viewModel.stateUI.observe(viewLifecycleOwner) { state ->
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
                        DialogInfoStyle.WARNING.ordinal,
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
                        DialogInfoStyle.WARNING.ordinal, state.title,
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

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialog(type: Int, title: String, message: String, positiveButtonName: String) {
        DialogInfoFragment.newInstance(
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName
        ).show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

}