package ru.wb.go.ui.courierbillingaccountdata

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.content.res.getDrawableOrThrow
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.databinding.CourierBillingDataFragmentBinding
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataFragment.ClickEventInterface
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataFragment.TextChangesInterface
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorAmountParameters
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.utils.SoftKeyboard
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.views.ProgressButtonMode
import java.util.*

class CourierBillingAccountDataFragment : Fragment(R.layout.courier_billing_data_fragment) {

    private var _binding: CourierBillingDataFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethod: InputMethodManager
    private val changeText = ArrayList<ViewChanges>()

    private val viewModel by viewModel<CourierBillingAccountDataViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierBillingAccountDataAmountParameters>(
                COURIER_BILLING_DATA_AMOUNT_KEY
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierBillingDataFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initFields()
        initListener()
        initInputMethod()
        initObservers()
        initReturnDialogResult()
    }

    private fun initReturnDialogResult() {
        setFragmentResultListener(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.removeConfirmed()
            }
        }
    }

    private fun initView() {
        binding.account.inputType = InputType.TYPE_CLASS_NUMBER
        binding.bik.inputType = InputType.TYPE_CLASS_NUMBER
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initFields() {
        val params = viewModel.getParams()
        if (params.account == null) {
            return
        }
        with(params.account) {
            binding.userName.setText(userName)
            binding.inn.setText(inn)
            binding.account.setText(account)
            binding.bik.setText(bic)
            binding.bank.setText(bank)
        }
        if (params.billingAccounts.size > 1)
            binding.removeAccountButton.visibility = VISIBLE
        else
            binding.removeAccountButton.visibility = GONE
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }

        viewModel.onFormChanges(changeFieldObservables())
        binding.removeAccountButton.setOnClickListener { viewModel.onRemoveAccountClick() }

    }

    private fun changeFieldObservables(): ArrayList<Observable<CourierBillingAccountDataUIAction>> {
        val changeTextObservables = ArrayList<Observable<CourierBillingAccountDataUIAction>>()

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.accountLayout,
                binding.account,
                CourierBillingAccountDataQueryType.ACCOUNT
            )
        )

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.bikLayout,
                binding.bik,
                CourierBillingAccountDataQueryType.BIK
            )
        )

        changeTextObservables.add(createClickObserver().initListener(binding.saveAccountButton))

        return changeTextObservables
    }

    private fun getFormUserData() = mutableListOf(
        CourierAccountData(
            binding.account.text.toString(), CourierBillingAccountDataQueryType.ACCOUNT
        ),
        CourierAccountData(binding.bik.text.toString(), CourierBillingAccountDataQueryType.BIK)
    )

    private fun getCourierBillingAccountEntity(): CourierBillingAccountEntity {
        val bankEntity = viewModel.getBankEntity()!!
        return CourierBillingAccountEntity(
            userName = binding.userName.text.toString(),
            inn = binding.inn.text.toString(),
            account = binding.account.text.toString(),
            correspondentAccount = bankEntity.correspondentAccount,
            bic = bankEntity.bic,
            bank = bankEntity.name,
        )
    }

    fun interface ClickEventInterface {
        fun initListener(view: View): Observable<CourierBillingAccountDataUIAction>
    }

    private fun createClickObserver(): ClickEventInterface {
        return ClickEventInterface { view ->
            view.clicks().map {
                view.isEnabled = false
                CourierBillingAccountDataUIAction.SaveClick(getFormUserData())
            }
        }
    }

    fun interface TextChangesInterface {
        fun initListener(
            textInputLayout: TextInputLayout,
            editText: EditText,
            queryType: CourierBillingAccountDataQueryType
        ): Observable<CourierBillingAccountDataUIAction>
    }

    private fun createFieldChangesObserver(): TextChangesInterface {
        return TextChangesInterface { textInputLayout, editText, queryType ->
            changeText.add(ViewChanges(textInputLayout, editText, queryType))
            val textChanges = editText.textChanges()
                .map { it.toString() }
                .map { CourierBillingAccountDataUIAction.TextChange(it, queryType) }
            val focusChanges = editText.focusChanges()
                .map {
                    CourierBillingAccountDataUIAction.FocusChange(
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

    private fun hideKeyboard() {
        SoftKeyboard.hideKeyBoard(requireActivity())
    }

    private fun initObservers() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it
        }

        viewModel.initUIState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierBillingAccountDataInitUIState.Create -> {
                    binding.userName.setText(it.userName)
                    binding.inn.setText(it.userInn)
                    binding.removeAccountButton.visibility = GONE
                }
                is CourierBillingAccountDataInitUIState.Edit -> {
                    binding.removeAccountButton.visibility = VISIBLE
                    with(it.field) {
                        binding.userName.setText(userName)
                        binding.inn.setText(inn)
                        binding.account.setText(account)
                        binding.bik.setText(bik)
                        binding.bank.setText(bank)
                    }
                }
            }
        }

        viewModel.navigateToDialogConfirmInfo.observe(viewLifecycleOwner) {
            showDialogConfirmInfo(it.type, it.title, it.message, it.positive, it.negative)
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

        viewModel.formUIState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierBillingAccountDataUIState.Complete -> {
                    val textLayout =
                        changeText.find { it.type == state.typeBillingAccount }?.textLayout
                    textLayout?.error = getText(R.string.error_empty)
                }
                is CourierBillingAccountDataUIState.Error -> {
                    val textLayout =
                        changeText.find { it.type == state.typeBillingAccount }?.textLayout
                    textLayout?.error = state.message
                }
                is CourierBillingAccountDataUIState.ErrorFocus -> {
                    changeText.find { it.type == state.typeBillingAccount }?.text?.let {
                        it.setSelection(it.length())
                        it.requestFocus()
                        scrollToViewTop(binding.scrollView, it)
                    }
                }
                CourierBillingAccountDataUIState.Next -> {
                    viewModel.onSaveAccountClick(getCourierBillingAccountEntity())
                }
            }
        }

        viewModel.bicProgressState.observe(viewLifecycleOwner) {
            when (it) {
                true -> showProgressBic()
                false -> hideProgressBic()
            }
        }

        viewModel.keyboardState.observe(viewLifecycleOwner) {
            if (!it) hideKeyboard()
        }

        viewModel.bankFindState.observe(viewLifecycleOwner) {
            binding.bank.setText(it.name)
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierBillingAccountDataNavAction.NavigateToAccountSelector ->
                    findNavController().navigate(
                        CourierBillingAccountDataFragmentDirections
                            .actionCourierBillingAccountDataFragmentToCourierBillingAccountSelectorFragment(
                                CourierBillingAccountSelectorAmountParameters(
                                    state.balance
                                )
                            )
                    )
                CourierBillingAccountDataNavAction.NavigateToBack ->
                    findNavController().popBackStack()
                is CourierBillingAccountDataNavAction.NavigateToConfirmDialog -> {
                    val msg = "Удалить счет\n${state.account}?"
                    showDialogConfirmInfo(
                        DialogInfoStyle.INFO.ordinal,
                        getString(R.string.attention_title),
                        msg,
                        getString(R.string.ok_button_title),
                        getString(R.string.exit_app_cancel)
                    )

                }
            }
        }

        viewModel.waitLoader.observe(viewLifecycleOwner) { state ->
            when (state) {
                WaitLoader.Wait -> showProgressDialog()
                WaitLoader.Complete -> closeProgressDialog()
            }
        }

        viewModel.navigateToMessageState.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.loaderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierBillingAccountDataUILoaderState.Disable ->
                    binding.saveAccountButton.setState(ProgressButtonMode.DISABLE)
                CourierBillingAccountDataUILoaderState.Enable ->
                    binding.saveAccountButton.setState(ProgressButtonMode.ENABLE)

            }
        }

    }

    private fun hideProgressBic() {
        binding.bikLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.account.isEnabled = true
        binding.bik.isEnabled = true
        binding.saveAccountButton.setState(ProgressButtonMode.ENABLE)
        binding.removeAccountButton.isEnabled = true
    }

    private fun showProgressBic() {
        binding.bikLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        val endIcon = requireContext().getProgressBarDrawable()
        (endIcon as? Animatable)?.start()
        binding.bikLayout.endIconDrawable = endIcon
        binding.account.isEnabled = false
        binding.bik.isEnabled = false
        binding.saveAccountButton.setState(ProgressButtonMode.DISABLE)
        binding.removeAccountButton.isEnabled = false
    }

    private fun Context.getProgressBarDrawable(): Drawable {
        val value = TypedValue()
        theme.resolveAttribute(android.R.attr.progressBarStyleSmall, value, false)
        val progressBarStyle = value.data
        val attributes = intArrayOf(android.R.attr.indeterminateDrawable)
        val array = obtainStyledAttributes(progressBarStyle, attributes)
        val drawable = array.getDrawableOrThrow(0)
        array.recycle()
        return drawable
    }

    private fun scrollToViewTop(scrollView: ScrollView, childView: View) {
        val delay: Long = 100
        scrollView.postDelayed({ scrollView.smoothScrollTo(0, childView.top) }, delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialogConfirmInfo(
        style: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            resultTag = DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_RESULT_TAG,
            type = style,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_TAG)
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

    private fun showProgressDialog() {
        val progressDialog = ProgressDialogFragment.newInstance()
        progressDialog.show(parentFragmentManager, ProgressDialogFragment.PROGRESS_DIALOG_TAG)
    }

    private fun closeProgressDialog() {
        parentFragmentManager.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG)?.let {
            if (it is ProgressDialogFragment) it.dismiss()
        }
    }

    companion object {
        const val COURIER_BILLING_DATA_AMOUNT_KEY = "courier_billing_data_amount_key"
    }

}

data class CourierAccountData(val text: String, val type: CourierBillingAccountDataQueryType)

data class ViewChanges(
    val textLayout: TextInputLayout,
    val text: EditText,
    val type: CourierBillingAccountDataQueryType
)

@Parcelize
data class CourierBillingAccountDataAmountParameters(
    val account: @RawValue CourierBillingAccountEntity?,
    val billingAccounts: @RawValue List<CourierBillingAccountEntity>,
    val balance: Int,
) : Parcelable

