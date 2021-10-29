package ru.wb.perevozka.ui.courierbillingaccountselector

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.perevozka.databinding.CourierBillingAccountSelectorFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.courierbillingaccountdata.CourierBillingAccountDataAmountParameters
import ru.wb.perevozka.ui.courierbillingaccountselector.CourierBillingAccountSelectorFragment.ClickEventInterface
import ru.wb.perevozka.ui.courierbillingaccountselector.CourierBillingAccountSelectorFragment.TextChangesInterface
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressButtonMode
import java.util.*


class CourierBillingAccountSelectorFragment :
    Fragment(ru.wb.perevozka.R.layout.courier_billing_account_selector_fragment) {

    private var _binding: CourierBillingAccountSelectorFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethod: InputMethodManager

    companion object {
        const val COURIER_BILLING_ACCOUNT_SELECTOR_AMOUNT_KEY =
            "courier_billing_account_selector_amount_key"
    }

    private val viewModel by viewModel<CourierBillingAccountSelectorViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierBillingAccountSelectorAmountParameters>(
                COURIER_BILLING_ACCOUNT_SELECTOR_AMOUNT_KEY
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierBillingAccountSelectorFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initInputMethod()
        initObservers()

//        val counting = listOf(
//            "ПАО «Сбербанк» ****3132",
//            "ПАО «Сбербанк» ****3133",
//            "АО «АЛЬФА-БАНК» ****3132",
//            "Добавить счет"
//        )
//        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
//            requireContext(),
//            ru.wb.perevozka.R.layout.billing_account_add_adapter_layout,
//            counting
//        )
//
//        binding.filledExposedDropdown.setAdapter(adapter)

//        val accounts = listOf(
//            "ПАО «Сбербанк» ****3132",
//            "ПАО «Сбербанк» ****3133",
//            "АО «АЛЬФА-БАНК» ****3132",
//            "Добавить счет"
//        )
//        binding.spinnerAccountTest.adapter =
//            CourierBillingAccountSelectorAdapter(
//                requireContext(),
//                accounts,
//                object : OnCourierBillingAccountSelectorCallback {
//                    override fun onEditClick(idView: Int) {
//                        viewModel.onEditAccountClick(idView)
//                    }
//                })
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }

        viewModel.onFormChanges(changeFieldObservables())
    }

    private val changeText = ArrayList<ViewChanges>()

    data class ViewChanges(
        val textLayout: TextInputLayout,
        val text: EditText,
        val type: CourierBillingAccountSelectorQueryType
    )

    private fun changeFieldObservables(): ArrayList<Observable<CourierBillingAccountSelectorUIAction>> {
        val changeTextObservables = ArrayList<Observable<CourierBillingAccountSelectorUIAction>>()

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.surnameLayout,
                binding.surname,
                CourierBillingAccountSelectorQueryType.SURNAME
            )
        )

        changeTextObservables.add(createClickObserver().initListener(binding.next))

        return changeTextObservables
    }

    private fun getFormUserData() = mutableListOf(
        CourierData(
            binding.surname.text.toString(),
            CourierBillingAccountSelectorQueryType.SURNAME
        )
    )

    fun interface ClickEventInterface {
        fun initListener(view: View): Observable<CourierBillingAccountSelectorUIAction>
    }

    private fun createClickObserver(): ClickEventInterface {
        return ClickEventInterface { view ->
            view.clicks()
                .map { CourierBillingAccountSelectorUIAction.CompleteClick(getFormUserData()) }
        }
    }

    fun interface TextChangesInterface {
        fun initListener(
            textInputLayout: TextInputLayout,
            editText: EditText,
            queryType: CourierBillingAccountSelectorQueryType
        ): Observable<CourierBillingAccountSelectorUIAction>
    }

    private fun createFieldChangesObserver(): TextChangesInterface {
        return TextChangesInterface { textInputLayout, editText, queryType ->
            changeText.add(ViewChanges(textInputLayout, editText, queryType))
            val textChanges = editText.textChanges()
                .map { it.toString() }
                .map { CourierBillingAccountSelectorUIAction.TextChange(it, queryType) }
            val focusChanges = editText.focusChanges()
                .map {
                    CourierBillingAccountSelectorUIAction.FocusChange(
                        editText.text.toString(),
                        queryType,
                        it
                    )
                }
            Observable.merge(textChanges, focusChanges).skip(2)
        }
    }

    private fun initInputMethod() {
        inputMethod =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initObservers() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it
        }

        viewModel.balanceState.observe(viewLifecycleOwner) {
            binding.balance.text = it
        }

        viewModel.navigateToMessageState.observe(viewLifecycleOwner) {
            showDialog(it.style, it.title, it.message, it.button)
        }

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

        viewModel.formUIState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierBillingAccountSelectorUIState.Complete -> {
                    val textLayout =
                        changeText.find { it.type == state.typeBillingAccount }?.textLayout
                    textLayout?.error = getText(ru.wb.perevozka.R.string.error_empty)
                }
                is CourierBillingAccountSelectorUIState.Error -> {
                    val textLayout =
                        changeText.find { it.type == state.typeBillingAccount }?.textLayout
                    textLayout?.error = getText(ru.wb.perevozka.R.string.error)
                }
                is CourierBillingAccountSelectorUIState.ErrorFocus -> {
                    changeText.find { it.type == state.typeBillingAccount }?.text?.let {
                        it.setSelection(it.length())
                        it.requestFocus()
                        scrollToViewTop(binding.scrollView, it)
                    }
                }
                CourierBillingAccountSelectorUIState.Next -> {
                    //viewModel.onNextClick(getCourierDocumentsEntity())
                }
            }
        }

        viewModel.dropAccountState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierBillingAccountSelectorDropAction.SetItems -> {
                    val callback = object : OnCourierBillingAccountSelectorCallback {
                        override fun onEditClick(idView: Int) {
                            viewModel.onEditAccountClick(idView)
                        }
                    }

                    val adapter = CourierBillingAccountSelectorAdapter(
                        requireContext(),
                        it.accounts,
                        callback
                    )
                    adapter.notifyDataSetChanged()

                    binding.spinnerAccount.adapter = adapter
                    binding.spinnerAccount.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            addapter: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            viewModel.onAccountSelectClick(position)
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
//                            TODO("Not yet implemented")
                        }


                    }
                }
                is CourierBillingAccountSelectorDropAction.SetSelected -> {
                    binding.spinnerAccount.setSelection(it.id)
                }

            }


        }

        viewModel.navigationEvent.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    is CourierBillingAccountSelectorNavAction.NavigateToAccountEdit -> {
                        findNavController().navigate(
                            CourierBillingAccountSelectorFragmentDirections.actionCourierBillingAccountSelectorFragmentToCourierBillingAccountDataFragment(
                                CourierBillingAccountDataAmountParameters(
                                    state.account,
                                    state.balance
                                )
                            )
                        )
                    }
                    is CourierBillingAccountSelectorNavAction.NavigateToAccountCreate -> {
                        findNavController().navigate(
                            CourierBillingAccountSelectorFragmentDirections.actionCourierBillingAccountSelectorFragmentToCourierBillingAccountDataFragment(
                                CourierBillingAccountDataAmountParameters(
                                    state.account,
                                    state.balance
                                )
                            )
                        )
                    }
                }
            })

        viewModel.loaderState.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    CourierBillingAccountSelectorUILoaderState.Disable -> binding.next.setState(
                        ProgressButtonMode.DISABLE
                    )
                    CourierBillingAccountSelectorUILoaderState.Enable -> {
                        binding.next.setState(ProgressButtonMode.ENABLE)
                        binding.overlayBoxes.visibility = View.GONE
                    }
                    CourierBillingAccountSelectorUILoaderState.Progress -> {
                        binding.next.setState(ProgressButtonMode.PROGRESS)
                        binding.overlayBoxes.visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun scrollToViewTop(scrollView: ScrollView, childView: View) {
        val delay: Long = 100
        scrollView.postDelayed({ scrollView.smoothScrollTo(0, childView.top) }, delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialog(style: Int, title: String, message: String, positiveButtonName: String) {
        DialogInfoFragment.newInstance(style, title, message, positiveButtonName)
            .show(parentFragmentManager, DIALOG_INFO_TAG)
    }

}

data class CourierData(val text: String, val type: CourierBillingAccountSelectorQueryType)

@Parcelize
data class CourierBillingAccountSelectorAmountParameters(val balance: Int) : Parcelable


