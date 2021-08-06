package ru.wb.perevozka.ui.auth

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.textChanges
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.AuthInputPasswordFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.utils.SoftKeyboard
import ru.wb.perevozka.views.ProgressImageButtonMode
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class InputPasswordFragment : Fragment(R.layout.auth_input_password_fragment) {

    private var _binding: AuthInputPasswordFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<InputPasswordViewModel> {
        parametersOf(requireArguments().getParcelable<InputPasswordParameters>(PHONE_KEY))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthInputPasswordFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initKeyboard()
        initViews()
        initListener()
        initStateObserve()
    }

    private fun initViews() {
        binding.password.isEnabled = true
        binding.remindPassword.isEnabled = true
        binding.next.setState(ProgressImageButtonMode.ENABLED)
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.auth_number_phone_toolbar_label)
        binding.toolbarLayout.back.visibility = View.VISIBLE
    }

    private fun initKeyboard() {
        activity?.let { binding.password.postDelayed( {
            SoftKeyboard.showKeyboard(it, binding.password) } , 500) }
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        val password = binding.password
        viewModel.action(InputPasswordUIAction.PasswordChanges(password.textChanges()))
        binding.remindPassword.setOnClickListener {
            viewModel.action(InputPasswordUIAction.RemindPassword)
        }
        binding.next.setOnClickListener {
            viewModel.action(InputPasswordUIAction.Auth(password.text.toString()))
        }
    }

    private fun initStateObserve() {

        viewModel.navigationEvent.observe(viewLifecycleOwner, { state ->
            when (state) {
                is InputPasswordNavAction.NavigateToTemporaryPassword ->
                    findNavController().navigate(
                        InputPasswordFragmentDirections.actionInputPasswordFragmentToTemporaryPasswordFragment(
                            TemporaryPasswordParameters(state.phone)
                        )
                    )
                InputPasswordNavAction.NavigateToApplication -> {
                    findNavController().navigate(R.id.load_navigation)
                }
            }
        })

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE
                }

                is NetworkState.Complete -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                InputPasswordUIState.NextDisable -> binding.next.setState(
                    ProgressImageButtonMode.DISABLED
                )
                InputPasswordUIState.NextEnable -> {
                    binding.passwordLayout.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_input_normal)
                    binding.passwordNotFound.visibility = View.GONE
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                InputPasswordUIState.AuthProcess -> {
                    binding.password.isEnabled = false
                    binding.remindPassword.isEnabled = false
                    binding.next.setState(ProgressImageButtonMode.PROGRESS)
                }
                InputPasswordUIState.AuthComplete -> {
                    binding.password.isEnabled = false
                    binding.remindPassword.isEnabled = false
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                is InputPasswordUIState.Error -> {
                    showBarMessage(state.message)
                    binding.password.isEnabled = true
                    binding.remindPassword.isEnabled = true
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is InputPasswordUIState.PasswordNotFound -> {
                    binding.passwordLayout.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_input_error)
                    binding.passwordNotFound.text = state.message
                    binding.passwordNotFound.visibility = View.VISIBLE
                    binding.password.isEnabled = true
                    binding.remindPassword.isEnabled = true
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
            }
        })
    }

    private fun showBarMessage(state: String) {
        Snackbar.make(binding.next, state, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val PHONE_KEY = "phone_key"
    }

}

@Parcelize
data class InputPasswordParameters(val phone: String) : Parcelable


