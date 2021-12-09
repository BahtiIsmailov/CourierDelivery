package ru.wb.go.ui.courierbillingaccountdata

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
        binding.saveChangeAccount.setOnClickListener { viewModel.onSaveChangeAccountClick(getCourierBillingAccountEntity()) }
    }

    private val changeText = ArrayList<ViewChanges>()

    data class ViewChanges(
        val textLayout: TextInputLayout,
        val text: EditText,
        val type: CourierBillingAccountDataQueryType
    )

    private fun changeFieldObservables(): ArrayList<Observable<CourierBillingAccountDataUIAction>> {
        val changeTextObservables = ArrayList<Observable<CourierBillingAccountDataUIAction>>()

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.surnameLayout,
                binding.surname,
                CourierBillingAccountDataQueryType.SURNAME
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.firstNameLayout,
                binding.firstName,
                CourierBillingAccountDataQueryType.NAME
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.middleNameLayout,
                binding.middleName,
                CourierBillingAccountDataQueryType.MIDDLE_NAME
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.innLayout,
                binding.inn,
                CourierBillingAccountDataQueryType.INN
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.accountLayout,
                binding.account,
                CourierBillingAccountDataQueryType.ACCOUNT
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.bankLayout,
                binding.bank,
                CourierBillingAccountDataQueryType.BANK
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.bikLayout,
                binding.bik,
                CourierBillingAccountDataQueryType.BIK
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.kppLayout,
                binding.kpp,
                CourierBillingAccountDataQueryType.KPP
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.corAccountLayout,
                binding.corAccount,
                CourierBillingAccountDataQueryType.COR_ACCOUNT
            )
        )

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.innBankLayout,
                binding.innBank,
                CourierBillingAccountDataQueryType.INN_BANK
            )
        )

        changeTextObservables.add(createClickObserver().initListener(binding.save))

        return changeTextObservables
    }

    private fun getFormUserData() = mutableListOf(
        CourierAccountData(
            binding.surname.text.toString(),
            CourierBillingAccountDataQueryType.SURNAME
        ),
        CourierAccountData(
            binding.firstName.text.toString(),
            CourierBillingAccountDataQueryType.NAME
        ),
        CourierAccountData(
            binding.middleName.text.toString(),
            CourierBillingAccountDataQueryType.MIDDLE_NAME
        ),
        CourierAccountData(binding.inn.text.toString(), CourierBillingAccountDataQueryType.INN),

        CourierAccountData(
            binding.account.text.toString(),
            CourierBillingAccountDataQueryType.ACCOUNT
        ),
        CourierAccountData(binding.bank.text.toString(), CourierBillingAccountDataQueryType.BANK),
        CourierAccountData(binding.bik.text.toString(), CourierBillingAccountDataQueryType.BIK),
        CourierAccountData(
            binding.corAccount.text.toString(),
            CourierBillingAccountDataQueryType.COR_ACCOUNT
        ),
        CourierAccountData(
            binding.innBank.text.toString(),
            CourierBillingAccountDataQueryType.INN_BANK
        )
    )

    private fun getCourierBillingAccountEntity() = CourierBillingAccountEntity(
        surName = binding.surname.text.toString(),
        firstName = binding.firstName.text.toString(),
        middleName = binding.middleName.text.toString(),
        inn = binding.inn.text.toString(),
        account = binding.account.text.toString(),
        bank = binding.bank.text.toString(),
        bik = binding.bik.text.toString(),
        kpp = binding.kpp.text.toString(),
        corAccount = binding.corAccount.text.toString(),
        innBank = binding.innBank.text.toString(),
    )

    fun interface ClickEventInterface {
        fun initListener(view: View): Observable<CourierBillingAccountDataUIAction>
    }

    private fun createClickObserver(): ClickEventInterface {
        return ClickEventInterface { view ->
            view.clicks().map { CourierBillingAccountDataUIAction.CompleteClick(getFormUserData()) }
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

    private fun initObservers() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it
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
                        binding.surname.setText(surName)
                        binding.firstName.setText(firstName)
                        binding.middleName.setText(middleName)
                        binding.inn.setText(inn)
                        binding.account.setText(account)
                        binding.bank.setText(bank)
                        binding.bik.setText(bik)
                        binding.kpp.setText(kpp)
                        binding.corAccount.setText(corAccount)
                        binding.innBank.setText(innBank)
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
                    textLayout?.error = getText(R.string.error)
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

        viewModel.navigationEvent.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    is CourierBillingAccountDataNavAction.NavigateToAccountSelector -> {
                        findNavController().navigate(
                            CourierBillingAccountDataFragmentDirections.actionCourierBillingAccountDataFragmentToCourierBillingAccountSelectorFragment(
                                CourierBillingAccountSelectorAmountParameters(state.balance)
                            )
                        )
                    }
                }
            })

        viewModel.loaderState.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    CourierBillingAccountDataUILoaderState.Disable -> binding.save.setState(
                        ProgressButtonMode.DISABLE
                    )
                    CourierBillingAccountDataUILoaderState.Enable -> {
                        binding.save.setState(ProgressButtonMode.ENABLE)
                        binding.overlayBoxes.visibility = View.GONE
                    }
                    CourierBillingAccountDataUILoaderState.Progress -> {
                        binding.save.setState(ProgressButtonMode.PROGRESS)
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

data class CourierAccountData(val text: String, val type: CourierBillingAccountDataQueryType)

@Parcelize
data class CourierBillingAccountDataAmountParameters(val account: String, val amount: Int) :
    Parcelable