package com.wb.logistics.ui.auth

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.LoginFragmentBinding
import com.wb.logistics.views.ProgressImageButtonMode
import io.reactivex.Observable
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class LoginFragment : Fragment(R.layout.login_fragment) {

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethod: InputMethodManager
    private val viewModel by viewModel<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        (activity as AppCompatActivity).supportActionBar?.hide()
        inputMethod =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initListener() {
        binding.title.setOnLongClickListener {
            findNavController().navigate(R.id.configActivity)
            true
        }
    }

    private fun initViews() {
//        binding.numberAttempt
//        binding.numberNotFound
//        binding.phoneNumber

        val phone = binding.phoneNumber
        val next = binding.next

//        viewModel.successfulResponse.observe(viewLifecycleOwner) {
//            val action = LoginFragmentDirections.actionLoginFragmentToVerificationFragment(
//                binding.phoneNumber.text.toString().replace("\\D+".toRegex(), "")
//            )
//            findNavController().navigate(action)
//        }
//
//        viewModel.errorRespond.observe(this.viewLifecycleOwner) {
//            when (it.error.code) {
//                "PHONE_IS_UNEXPECTED" -> {
//                    inputMethod.hideSoftInputFromWindow(editText2.windowToken, 0)
//                    textCountDownTitle.visibility = View.VISIBLE
//                }
//                "CODE_SENT" -> {
//                    val action = LoginFragmentDirections.actionLoginFragmentToVerificationFragment(
//                        editText2.text.toString().replace("\\D+".toRegex(), "")
//                    )
//                    findNavController().navigate(action)
//                    //TODO: Add comment?
//                }
//                "SMS_SENT_EXCEEDED_ATTEMPTS" -> {
//                    inputMethod.hideSoftInputFromWindow(editText2.windowToken, 0)
//                    Snackbar.make(
//                        requireView(),
//                        "Превышено количество попыток. Попробуйте позже через 20 минут",
//                        Snackbar.LENGTH_LONG
//                    ).show()
//                }
//                "SMS_AUTHENTIFICATION_LOCKED" -> {
//                    inputMethod.hideSoftInputFromWindow(editText2.windowToken, 0)
//                    val action = LoginFragmentDirections.actionLoginFragmentToPasswordFragment(
//                        PasswordParameters(
//                            phone = editText2.text.toString().replace("\\D+".toRegex(), "")
//                        )
//
//                    )
//                    findNavController().navigate(action)
//                }
//                else -> {
//                    inputMethod.hideSoftInputFromWindow(editText2.windowToken, 0)
//                    Snackbar.make(
//                        requireView(),
//                        it.error.message,
//                        Snackbar.LENGTH_LONG
//                    ).show()
//                }
//
//            }
//        }

//        viewModel.failureResponse.observe(this.viewLifecycleOwner) {
//            inputMethod.hideSoftInputFromWindow(editText2.windowToken, 0)
//            Snackbar.make(
//                requireView(),
//                "Сервер не доступен или не отвечает",
//                Snackbar.LENGTH_LONG
//            ).show()
//        }

        phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val text: String = phone.text.toString()
                val textLength: Int = phone.text.length
                if (text.endsWith("-") || text.endsWith(" ") || text.endsWith(" ")) return

                next.setState(if (textLength == 18) ProgressImageButtonMode.ENABLED else ProgressImageButtonMode.DISABLED)

                if (textLength == 1) {
                    if (!text.contains("+7 (") &&
                        !text.contains("+7 ") &&
                        !text.contains("+7") &&
                        !text.contains("+")
                    ) {
                        phone.setText(
                            StringBuilder(text).insert(text.length - 1, "+7 (").toString()
                        )
                        phone.setSelection(phone.text.length)
                    }
                } else if (textLength == 8) {
                    if (!text.contains(")")) {
                        phone.setText(
                            StringBuilder(text).insert(text.length - 1, ")").toString()
                        )
                        phone.setSelection(phone.text.length)
                    }
                } else if (textLength == 9) {
                    phone.setText(StringBuilder(text).insert(text.length - 1, " ").toString())
                    phone.setSelection(phone.text.length)
                } else if (textLength == 13) {
                    if (!text.contains("-")) {
                        phone.setText(
                            StringBuilder(text).insert(text.length - 1, "-").toString()
                        )
                        phone.setSelection(phone.text.length)
                    }
                } else if (textLength == 16) {
                    if (text.contains("-")) {
                        phone.setText(
                            StringBuilder(text).insert(text.length - 1, "-").toString()
                        )
                        phone.setSelection(phone.text.length)
                    }
                }
            }
        })

        next.setOnClickListener {
//            binding.numberAttempt.visibility = View.GONE
//            Log.d("TheApp", phone.text.toString().filter { it.isDigit() })
//            viewModel.getCodeByPhone(
//                phone.text.toString().filter { it.isDigit() }
//            )

            next.setState(ProgressImageButtonMode.PROGRESS)
            Observable.timer(3, TimeUnit.SECONDS).subscribe(
                { findNavController().navigate(R.id.navigationActivity) },
                {  })
        }

        binding.next.isClickable = false

    }

}
