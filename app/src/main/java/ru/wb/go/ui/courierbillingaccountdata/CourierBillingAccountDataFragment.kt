package ru.wb.go.ui.courierbillingaccountdata

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.content.res.getDrawableOrThrow
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
import ru.wb.go.R
import ru.wb.go.databinding.CourierBillingDataFragmentBinding
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataFragment.ClickEventInterface
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataFragment.TextChangesInterface
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorAmountParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.utils.SoftKeyboard
import ru.wb.go.views.ProgressButtonMode
import java.util.*

class CourierBillingAccountDataFragment : Fragment(R.layout.courier_billing_data_fragment) {

    private var _binding: CourierBillingDataFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethod: InputMethodManager

    companion object {
        const val COURIER_BILLING_DATA_AMOUNT_KEY = "courier_billing_data_amount_key"
    }

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
        initListener()
        initInputMethod()
        initObservers()
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
        binding.removeAccount.setOnClickListener { viewModel.onRemoveAccountClick() }
        binding.saveChangeAccount.setOnClickListener {
            viewModel.onSaveChangeAccountClick(
                getCourierBillingAccountEntity()
            )
        }
    }

    private val changeText = ArrayList<ViewChanges>()

    data class ViewChanges(
        val textLayout: TextInputLayout,
        val text: EditText,
        val type: CourierBillingAccountDataQueryType
    )

    private fun changeFieldObservables(): ArrayList<Observable<CourierBillingAccountDataUIAction>> {
        val changeTextObservables = ArrayList<Observable<CourierBillingAccountDataUIAction>>()

//        changeTextObservables.add(
//            createFieldChangesObserver().initListener(
//                binding.surnameLayout,
//                binding.surname,
//                CourierBillingAccountDataQueryType.SURNAME
//            )
//        )
//        changeTextObservables.add(
//            createFieldChangesObserver().initListener(
//                binding.innLayout,
//                binding.inn,
//                CourierDataQueryType.INN
//            )
//        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.accountLayout,
                binding.account,
                CourierBillingAccountDataQueryType.ACCOUNT
            )
        )
//        changeTextObservables.add(
//            createFieldChangesObserver().initListener(
//                binding.bankLayout,
//                binding.bank,
//                CourierBillingAccountDataQueryType.BANK
//            )
//        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.bikLayout,
                binding.bik,
                CourierBillingAccountDataQueryType.BIK
            )
        )

        changeTextObservables.add(createClickObserver().initListener(binding.save))

        return changeTextObservables
    }

    private fun getFormUserData() = mutableListOf(
//        CourierAccountData(
//            binding.surname.text.toString(),
//            CourierBillingAccountDataQueryType.SURNAME
//        ),
//        CourierAccountData(binding.inn.text.toString(), CourierDataQueryType.INN),
        CourierAccountData(
            binding.account.text.toString(), CourierBillingAccountDataQueryType.ACCOUNT
        ),
//        CourierAccountData(binding.bank.text.toString(), CourierBillingAccountDataQueryType.BANK),
        CourierAccountData(binding.bik.text.toString(), CourierBillingAccountDataQueryType.BIK)
    )

    private fun getCourierBillingAccountEntity() = CourierBillingAccountEntity(
        userName = binding.userName.text.toString(),
        inn = binding.inn.text.toString(),
        correspondentAccount = binding.account.text.toString(),
        bic = binding.bik.text.toString(),
        bank = binding.bank.text.toString(),
    )

    fun interface ClickEventInterface {
        fun initListener(view: View): Observable<CourierBillingAccountDataUIAction>
    }

    private fun createClickObserver(): ClickEventInterface {
        return ClickEventInterface { view ->
            view.clicks().map {
                view.isEnabled = false
                binding.overlay.visibility = VISIBLE
                CourierBillingAccountDataUIAction.CompleteClick(getFormUserData())
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

        viewModel.userDataState.observe(viewLifecycleOwner) {
            binding.userName.setText(it.userName)
            binding.inn.setText(it.userInn)
        }

        viewModel.initUIState.observe(viewLifecycleOwner) {
            when (it) {
                CourierBillingAccountDataInitUIState.Create -> {
                    binding.save.visibility = VISIBLE
                    binding.editAccountLayout.visibility = GONE
                }
                is CourierBillingAccountDataInitUIState.Edit -> {
                    binding.save.visibility = GONE
                    binding.editAccountLayout.visibility = VISIBLE
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
                is CourierBillingAccountDataUIState.Complete -> {
                    val textLayout =
                        changeText.find { it.type == state.typeBillingAccount }?.textLayout
                    textLayout?.error = getText(R.string.error_empty)
                }
                is CourierBillingAccountDataUIState.Error -> {
                    val textLayout =
                        changeText.find { it.type == state.typeBillingAccount }?.textLayout
                    textLayout?.error = state.message//getText(R.string.error)
                }
                is CourierBillingAccountDataUIState.ErrorFocus -> {
                    changeText.find { it.type == state.typeBillingAccount }?.text?.let {
                        it.setSelection(it.length())
                        it.requestFocus()
                        scrollToViewTop(binding.scrollView, it)
                    }
                }
                CourierBillingAccountDataUIState.Next -> {
                    viewModel.onSaveClick(getCourierBillingAccountEntity())
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
            when (it) {
                false -> hideKeyboard()
            }
        }

        viewModel.bankNameState.observe(viewLifecycleOwner) {
            binding.bank.setText(it)
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    is CourierBillingAccountDataNavAction.NavigateToAccountSelector -> {
                        findNavController().navigate(
                            CourierBillingAccountDataFragmentDirections.actionCourierBillingAccountDataFragmentToCourierBillingAccountSelectorFragment(
                                CourierBillingAccountSelectorAmountParameters(
                                    state.inn,
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
                    CourierBillingAccountDataUILoaderState.Disable ->
                        binding.save.setState(ProgressButtonMode.DISABLE)
                    CourierBillingAccountDataUILoaderState.Enable ->
                        binding.save.setState(ProgressButtonMode.ENABLE)
                    CourierBillingAccountDataUILoaderState.Progress ->
                        binding.save.setState(ProgressButtonMode.PROGRESS)
                }
            })

        viewModel.holderState.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    true -> binding.overlay.visibility = VISIBLE
                    false -> binding.overlay.visibility = INVISIBLE
                }
            })

    }

    private fun hideProgressBic() {
        binding.bikLayout.endIconMode = TextInputLayout.END_ICON_NONE
    }

    private fun showProgressBic() {
        binding.bikLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        val endIcon = requireContext().getProgressBarDrawable()
        (endIcon as? Animatable)?.start()
        binding.bikLayout.endIconDrawable = endIcon
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

    private fun showDialog(style: Int, title: String, message: String, positiveButtonName: String) {
        DialogInfoFragment.newInstance(style, title, message, positiveButtonName)
            .show(parentFragmentManager, DIALOG_INFO_TAG)
    }

}

data class CourierAccountData(val text: String, val type: CourierBillingAccountDataQueryType)

@Parcelize
data class CourierBillingAccountDataAmountParameters(
    val inn: String,
    val account: String,
    val amount: Int
) : Parcelable