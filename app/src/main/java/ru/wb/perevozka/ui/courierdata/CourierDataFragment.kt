package ru.wb.perevozka.ui.courierdata

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.perevozka.R
import ru.wb.perevozka.app.AppConsts
import ru.wb.perevozka.databinding.CourierDataFragmentBinding
import ru.wb.perevozka.network.api.app.entity.CourierDocumentsEntity
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.courierdata.CourierDataFragment.ClickEventInterface
import ru.wb.perevozka.ui.courierdata.CourierDataFragment.TextChangesInterface
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.perevozka.ui.dialogs.date.DatePickerDialog
import ru.wb.perevozka.ui.dialogs.date.OnDateSelected
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.utils.time.DateTimeFormatter
import ru.wb.perevozka.views.ProgressButtonMode
import java.util.*
import android.text.TextUtils
import androidx.fragment.app.setFragmentResultListener
import ru.wb.perevozka.ui.courieragreement.CourierAgreementFragment
import ru.wb.perevozka.ui.courieragreement.CourierAgreementFragment.Companion.VALUE_RESULT_KEY
import ru.wb.perevozka.ui.courierexpects.CourierExpectsParameters


class CourierDataFragment : Fragment(R.layout.courier_data_fragment) {

    private var _binding: CourierDataFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethod: InputMethodManager
    private val viewModel by viewModel<UserFormViewModel> {
        parametersOf(requireArguments().getParcelable<CourierDataParameters>(PHONE_KEY))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierDataFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initInputMethod()
        initObservers()
        setLinkAgreement()
        initAgreementResult()
    }

    private fun initAgreementResult() {
        setFragmentResultListener(CourierAgreementFragment.BUNDLE_RESULT_KEY) { _, bundle ->
            if (bundle.containsKey(VALUE_RESULT_KEY)) {
                val isConfirm = bundle.get(VALUE_RESULT_KEY) as Boolean
                binding.checkedAgreement.isChecked = isConfirm
                updateChecked()
            }
        }
    }

    private fun updateChecked() {
        viewModel.onCheckedClick(
            binding.checkedComplete.isChecked,
            binding.checkedAgreement.isChecked,
            binding.checkedPersonal.isChecked
        )
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }

        binding.overlayDate.setOnClickListener {
            dateSelect(it, binding.passportDateOfIssue)
        }

        viewModel.onFormChanges(changeFieldObservables())

        binding.checkedComplete.setOnCheckedChangeListener { _, _ -> updateChecked() }
        binding.checkedPersonal.setOnCheckedChangeListener { _, _ -> updateChecked() }
//        binding.checkedAgreement.setOnClickListener { viewModel.onShowAgreementClick() }

    }

    private val changeText = ArrayList<ViewChanges>()

    data class ViewChanges(
        val textLayout: TextInputLayout,
        val text: EditText,
        val type: CourierDataQueryType
    )

    private fun changeFieldObservables(): ArrayList<Observable<CourierDataUIAction>> {
        val changeTextObservables = ArrayList<Observable<CourierDataUIAction>>()

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.surnameLayout,
                binding.surname,
                CourierDataQueryType.SURNAME
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.firstNameLayout,
                binding.firstName,
                CourierDataQueryType.NAME
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.innLayout,
                binding.inn,
                CourierDataQueryType.INN
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportSeriesLayout,
                binding.passportSeries,
                CourierDataQueryType.PASSPORT_SERIES
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportNumberLayout,
                binding.passportNumber,
                CourierDataQueryType.PASSPORT_NUMBER
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportDateOfIssueLayout,
                binding.passportDateOfIssue,
                CourierDataQueryType.PASSPORT_DATE
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportCodeLayout,
                binding.passportCode,
                CourierDataQueryType.PASSPORT_CODE
            )
        )

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportIssuedByLayout,
                binding.passportIssuedBy,
                CourierDataQueryType.PASSPORT_ISSUED_BY
            )
        )

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportDepartmentCodeLayout,
                binding.passportDepartmentCode,
                CourierDataQueryType.PASSPORT_DEPARTMENT_CODE
            )
        )

        changeTextObservables.add(createClickObserver().initListener(binding.next))

        return changeTextObservables
    }

    private fun getFormUserData() = mutableListOf(
        CourierData(binding.surname.text.toString(), CourierDataQueryType.SURNAME),
        CourierData(binding.firstName.text.toString(), CourierDataQueryType.NAME),
        CourierData(binding.inn.text.toString(), CourierDataQueryType.INN),
        CourierData(binding.passportSeries.text.toString(), CourierDataQueryType.PASSPORT_SERIES),
        CourierData(binding.passportNumber.text.toString(), CourierDataQueryType.PASSPORT_NUMBER),
        CourierData(
            binding.passportDateOfIssue.text.toString(),
            CourierDataQueryType.PASSPORT_DATE
        ),
        CourierData(
            binding.passportIssuedBy.text.toString(),
            CourierDataQueryType.PASSPORT_ISSUED_BY
        ),
        CourierData(
            binding.passportDepartmentCode.text.toString(),
            CourierDataQueryType.PASSPORT_DEPARTMENT_CODE
        )
    )

    private fun getCourierDocumentsEntity() = CourierDocumentsEntity(
        surName = binding.surname.text.toString(),
        firstName = binding.firstName.text.toString(),
        middleName = binding.middleName.text.toString(),
        inn = binding.inn.text.toString(),
        passportSeries = binding.passportSeries.text.toString(),
        passportNumber = binding.passportNumber.text.toString(),
        passportDateOfIssue = binding.passportDateOfIssue.text.toString(),
        passportIssuedBy = binding.passportIssuedBy.text.toString(),
        passportDepartmentCode = binding.passportDepartmentCode.text.toString(),
    )

    private fun dateSelect(view: View, dateText: EditText) {
        view.isEnabled = false
        initDatePiker(object : OnDateSelected {

            override fun onDateSelected(date: DateTime) {
                dateText.setText(formatDatePicker(date))
                view.isEnabled = true
            }

            override fun onCanceled() {
                view.isEnabled = true
            }

        })
    }

    private fun formatDatePicker(date: DateTime) =
        DateTimeFormat.forPattern(DATE_PICKER_PATTERN).print(date)

    private fun initDatePiker(dateSelectedCallback: OnDateSelected) {
        val dateMin: DateTime = DateTimeFormatter.parseDate(AppConsts.PRIVATE_INFO_MIN_DATE_PIKER)
        val dateMax: DateTime
        var date: DateTime
        dateMax = DateTimeFormatter.currentDateTime().also { date = it }
        showDatePiker(dateMin, dateMax, date, dateSelectedCallback)
    }

    private fun showDatePiker(
        minDate: DateTime,
        maxDate: DateTime,
        date: DateTime,
        dateSelectedCallback: OnDateSelected
    ) {
        val dateTimePickerDialog = DatePickerDialog.newInstance(minDate, maxDate, date)
        dateTimePickerDialog.setListener(dateSelectedCallback)
        dateTimePickerDialog.show(
            requireActivity().supportFragmentManager,
            DATE_TIME_PICKER_FRAGMENT
        )
    }

    fun interface ClickEventInterface {
        fun initListener(view: View): Observable<CourierDataUIAction>
    }

    private fun createClickObserver(): ClickEventInterface {
        return ClickEventInterface { view ->
            view.clicks().map { CourierDataUIAction.CompleteClick(getFormUserData()) }
        }
    }

    fun interface TextChangesInterface {
        fun initListener(
            textInputLayout: TextInputLayout, editText: EditText, queryType: CourierDataQueryType
        ): Observable<CourierDataUIAction>
    }

    private fun createFieldChangesObserver(): TextChangesInterface {
        return TextChangesInterface { textInputLayout, editText, queryType ->
            changeText.add(ViewChanges(textInputLayout, editText, queryType))
            val textChanges = editText.textChanges()
                .map { it.toString() }
                .map { CourierDataUIAction.TextChange(it, queryType) }
            val focusChanges = editText.focusChanges()
                .map { CourierDataUIAction.FocusChange(editText.text.toString(), queryType, it) }
            Observable.merge(textChanges, focusChanges).skip(2)
        }
    }

    private fun initInputMethod() {
        inputMethod =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initObservers() {

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
                is CourierDataUIState.Complete -> {
                    val textLayout = changeText.find { it.type == state.type }?.textLayout
                    textLayout?.error = getText(R.string.error_empty)
                }
                is CourierDataUIState.Error -> {
                    val textLayout = changeText.find { it.type == state.type }?.textLayout
                    textLayout?.error = getText(R.string.error)
                }
                is CourierDataUIState.ErrorFocus -> {
                    changeText.find { it.type == state.type }?.text?.let {
                        it.setSelection(it.length())
                        it.requestFocus()
                        scrollToViewTop(binding.scrollView, it)
                    }
                }
                CourierDataUIState.Next -> {
                    viewModel.onNextClick(getCourierDocumentsEntity())
                }
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    is CourierDataNavAction.NavigateToCouriersCompleteRegistration -> {
                        findNavController().navigate(
                            CourierDataFragmentDirections.actionUserFormFragmentToCouriersCompleteRegistrationFragment(
                                CourierExpectsParameters(state.phone)
                            )
                        )
                    }
                    CourierDataNavAction.NavigateToAgreement -> {
                        findNavController().navigate(
                            CourierDataFragmentDirections.actionUserFormFragmentToCourierAgreementFragment()
                        )
                    }
                }
            })

        viewModel.loaderState.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    CourierDataUILoaderState.Disable -> binding.next.setState(ProgressButtonMode.DISABLE)
                    CourierDataUILoaderState.Enable -> {
                        binding.next.setState(ProgressButtonMode.ENABLE)
                        binding.overlayBoxes.visibility = View.GONE
                    }
                    CourierDataUILoaderState.Progress -> {
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

    companion object {
        const val PHONE_KEY = "phone_key"
        const val DATE_TIME_PICKER_FRAGMENT = "date_time_picker_fragment"
        const val DATE_PICKER_PATTERN = "dd.MM.yyyy"
    }

    private val clickableSpan: ClickableSpan = object : ClickableSpan() {

        override fun onClick(widget: View) {
            viewModel.onShowAgreementClick()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = ContextCompat.getColor(requireContext(), R.color.text_clickable)
        }

    }

    private fun setLinkAgreement() {
        val link = getString(R.string.user_form_agreement_link)
        val spannable = SpannableString(link)
        spannable.setSpan(clickableSpan, 0, link.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val cs = TextUtils.expandTemplate(getString(R.string.user_form_agreement), spannable)
        binding.textAgreement.text = cs
        binding.textAgreement.movementMethod = LinkMovementMethod.getInstance()
    }

}

@Parcelize
data class CourierDataParameters(val phone: String) : Parcelable

data class CourierData(val text: String, val type: CourierDataQueryType)


