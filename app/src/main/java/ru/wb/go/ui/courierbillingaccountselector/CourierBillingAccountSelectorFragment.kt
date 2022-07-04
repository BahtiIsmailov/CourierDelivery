package ru.wb.go.ui.courierbillingaccountselector

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.View.NOT_FOCUSABLE
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ScrollView
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.databinding.CourierBillingAccountSelectorFragmentBinding
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataAmountParameters
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorFragment.ClickEventInterface
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorFragment.TextChangesInterface
import ru.wb.go.ui.courierbilllingcomplete.CourierBillingCompleteParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.utils.SoftKeyboard
import ru.wb.go.utils.clicks
import ru.wb.go.utils.focusChanges
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.textChanges


class CourierBillingAccountSelectorFragment :
    BaseServiceFragment<CourierBillingAccountSelectorViewModel, CourierBillingAccountSelectorFragmentBinding>(
        CourierBillingAccountSelectorFragmentBinding::inflate
    ) {


    private lateinit var inputMethod: InputMethodManager

    companion object {
        const val COURIER_BILLING_ACCOUNT_SELECTOR_AMOUNT_KEY =
            "courier_billing_account_selector_amount_key"
    }

    override val viewModel by viewModel<CourierBillingAccountSelectorViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierBillingAccountSelectorAmountParameters>(
                COURIER_BILLING_ACCOUNT_SELECTOR_AMOUNT_KEY
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initInputMethod()
        initObservers()
        viewModel.init()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener {
            binding.amount.isEnabled = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.amount.focusable = NOT_FOCUSABLE
            }
            hideKeyboard()
            findNavController().popBackStack()
        }

        viewModel.onFormChanges(changeFieldObservables())
        binding.spinnerAccountDrop.setOnClickListener {
            binding.spinnerAccount.performClick()
        }

    }

    private val changeText :ArrayList<ViewChanges>? = null

    data class ViewChanges(
        val textLayout: TextInputLayout,
        val text: EditText,
        val type: CourierBillingAccountSelectorQueryType
    )

    private fun changeFieldObservables(): ArrayList<Flow<CourierBillingAccountSelectorUIAction>> {
        val changeTextObservables = ArrayList<Flow<CourierBillingAccountSelectorUIAction>>()

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.amountLayout,
                binding.amount,
                CourierBillingAccountSelectorQueryType.SURNAME
            )
        )

        changeTextObservables.add(createClickObserver().initListener(binding.next))

        return changeTextObservables
    }

    private fun getFormUserData() = mutableListOf(
        CourierData(
            binding.amount.text.toString(),
            CourierBillingAccountSelectorQueryType.SURNAME
        )
    )

    fun interface ClickEventInterface {
        fun initListener(view: View): Flow<CourierBillingAccountSelectorUIAction>
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
        ): Flow<CourierBillingAccountSelectorUIAction>
    }

    @OptIn(FlowPreview::class)
    private fun createFieldChangesObserver(): TextChangesInterface {
        return TextChangesInterface { textInputLayout, editText, queryType ->
            changeText?.add(ViewChanges(textInputLayout, editText, queryType))
            val textChanges = editText.textChanges()
                .map { it.toString() }
                .map { CourierBillingAccountSelectorUIAction.TextChange(it, queryType) }

            val focusChanges = editText.focusChanges()
                .map{
                CourierBillingAccountSelectorUIAction.FocusChange(
                    editText.text.toString(),
                    queryType,
                    it
                )
            }
           flowOf(textChanges,focusChanges).flattenMerge().drop(2)
        }
    }

    private fun initInputMethod() {
        inputMethod =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initObservers() {

        viewModel.courierInnLivaData.observe(viewLifecycleOwner){
            binding.inn.setText(it)
        }
        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it
        }

        viewModel.balanceState.observe(viewLifecycleOwner) {
            binding.balance.text = it
            binding.amount.text?.clear()
        }

        viewModel.balanceChangeState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierBillingAccountSelectorBalanceAction.Init -> {
                    binding.next.isEnabled = false
                    binding.next.text = it.text
                }
                is CourierBillingAccountSelectorBalanceAction.Complete -> {
                    binding.next.isEnabled = true
                    binding.next.text = it.text
                }
                is CourierBillingAccountSelectorBalanceAction.Error -> {
                    binding.next.isEnabled = false
                    binding.next.setText(it.text)
                }
            }
        }

        viewModel.errorDialogState.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.formUIState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierBillingAccountSelectorUIState.Empty -> {
                    val textLayout =
                        changeText?.find { it.type == state.typeBillingAccount }?.textLayout
                    textLayout?.error = state.message
                }
                is CourierBillingAccountSelectorUIState.Complete -> {
                    val textLayout =
                        changeText?.find { it.type == state.typeBillingAccount }?.textLayout
                    textLayout?.error = getText(R.string.error_empty)

                    val text = changeText?.find { it.type == state.typeBillingAccount }?.text
                    text?.setText(state.formatBalance)
                    text?.setSelection(state.formatBalance.length)
                }
                is CourierBillingAccountSelectorUIState.Error -> {
                    val textLayout =
                        changeText?.find { it.type == state.typeBillingAccount }?.textLayout
                    textLayout?.error = state.message

                    val text = changeText?.find { it.type == state.typeBillingAccount }?.text
                    text?.setText(state.formatBalance)
                    text?.setSelection(state.formatBalance.length)
                }
                is CourierBillingAccountSelectorUIState.ErrorFocus -> {
                    changeText?.find { it.type == state.typeBillingAccount }?.text?.let {
                        it.setSelection(it.length())
                        it.requestFocus()
                        scrollToViewTop(binding.scrollView, it)
                    }
                }
                CourierBillingAccountSelectorUIState.NextComplete -> {
                    viewModel.onNextCompleteClick(
                        binding.spinnerAccount.selectedItemId,
                        binding.amount.text.toString()
                    )
                }

            }
        }

        viewModel.dropAccountState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierBillingAccountSelectorDropAction.SetItems -> {
                    val callback = object : OnCourierBillingAccountSelectorCallback {
                        override fun onEditClick(idView: Int) {
                            binding.spinnerAccount.onDetachedFromWindow()
                            viewModel.onEditAccountClick(idView)
                        }

                        override fun onAddClick() {
                            binding.spinnerAccount.onDetachedFromWindow()
                            viewModel.onAddAccountClick()
                        }
                    }

                    val adapter = CourierBillingAccountSelectorAdapter(
                        requireContext(), it.items, callback
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

                        }
                    }
                }
                is CourierBillingAccountSelectorDropAction.SetSelected -> {
                    binding.spinnerAccount.setSelection(it.id)
                }

            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierBillingAccountSelectorNavAction.NavigateToAccountEdit -> {
                    findNavController().navigate(
                        CourierBillingAccountSelectorFragmentDirections.actionCourierBillingAccountSelectorFragmentToCourierBillingAccountDataFragment(
                            CourierBillingAccountDataAmountParameters(
                                state.account,
                                state.billingAccounts,
                                state.balance
                            )
                        )
                    )
                }
                is CourierBillingAccountSelectorNavAction.NavigateToAccountCreate -> {
                    findNavController().navigate(
                        CourierBillingAccountSelectorFragmentDirections.actionCourierBillingAccountSelectorFragmentToCourierBillingAccountDataFragment(
                            CourierBillingAccountDataAmountParameters(
                                account = null,
                                state.billingAccounts,
                                state.balance
                            )
                        )
                    )
                }
                is CourierBillingAccountSelectorNavAction.NavigateToBillingComplete -> {
                    findNavController().navigate(
                        CourierBillingAccountSelectorFragmentDirections.actionCourierBillingAccountSelectorFragmentToCourierBillingCompleteFragment(
                            CourierBillingCompleteParameters(state.balance)
                        )
                    )
                }
            }
        }

        viewModel.loaderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierBillingAccountSelectorUILoaderState.Disable -> {
                    binding.next.isEnabled = false
                }
                CourierBillingAccountSelectorUILoaderState.Enable -> {
                    binding.next.isEnabled = true
                }
                CourierBillingAccountSelectorUILoaderState.Progress -> {
                    hideKeyboard()
                }
            }
        }
    }

    private fun scrollToViewTop(scrollView: ScrollView, childView: View) {
        val delay: Long = 100
        scrollView.postDelayed({ scrollView.smoothScrollTo(0, childView.top) }, delay)
    }

    private fun showDialogInfo(
        errorDialogData: ErrorDialogData
    ) {
        DialogInfoFragment.newInstance(
            resultTag = errorDialogData.dlgTag,
            type = errorDialogData.type,
            title = errorDialogData.title,
            message = errorDialogData.message,
            positiveButtonName = requireContext().getString(R.string.ok_button_title)
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun hideKeyboard() {
        SoftKeyboard.hideKeyBoard(requireActivity())
    }

}

data class CourierData(val text: String, val type: CourierBillingAccountSelectorQueryType)

@Parcelize
data class CourierBillingAccountSelectorAmountParameters(var balance: Int) :
    Parcelable


