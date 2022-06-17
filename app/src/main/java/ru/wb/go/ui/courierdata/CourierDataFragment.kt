package ru.wb.go.ui.courierdata

import CheckInternet
import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.app.AppConsts
import ru.wb.go.databinding.CourierDataFragmentBinding
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.courierdata.CourierDataFragment.ClickEventInterface
import ru.wb.go.ui.courierdata.CourierDataFragment.TextChangesInterface
import ru.wb.go.ui.courierdataexpects.CourierDataExpectsParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.date.DatePickerDialog
import ru.wb.go.ui.dialogs.date.OnDateSelected
import ru.wb.go.utils.SoftKeyboard
import ru.wb.go.utils.clicks
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.textChanges
import ru.wb.go.utils.time.DateTimeFormatter


class CourierDataFragment : Fragment(R.layout.courier_data_fragment) {

    private var _binding: CourierDataFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethod: InputMethodManager
    private val viewModel by viewModel<UserFormViewModel> {
        parametersOf(requireArguments().getParcelable<CourierDataParameters>(REGISTER_FORM_PARAMS))
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
        initializeFields()

    }

    private fun initializeFields() {
        val params = viewModel.getParams()
        if (params.docs.errorAnnotate.isNullOrEmpty()) {
            SoftKeyboard.showKeyboard(requireActivity(), binding.surname)
            return
        }
        //viewModel.showAnnotation()
        with(params.docs) {
            binding.surname.setText(surName)
            binding.firstName.setText(firstName)
            binding.middleName.setText(middleName)
            binding.inn.setText(inn)
            binding.passportSeries.setText(passportSeries)
            binding.passportNumber.setText(passportNumber)
            binding.passportDateOfIssue.setText(passportDateOfIssue)
            binding.passportDepartmentCode.setText(passportDepartmentCode)
            binding.passportIssuedBy.setText(passportIssuedBy)
            binding.checkedAgreement.isChecked = false
        }
    }

    private fun updateChecked() {
        viewModel.onCheckedClick(
            binding.checkedAgreement.isChecked,
        )


    }

    private fun initView() {
        binding.inn.inputType = InputType.TYPE_CLASS_NUMBER
        binding.passportSeries.inputType = InputType.TYPE_CLASS_NUMBER
        binding.passportNumber.inputType = InputType.TYPE_CLASS_NUMBER
        binding.passportDepartmentCode.inputType = InputType.TYPE_CLASS_NUMBER
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        viewModel.onFormChanges(changeFieldObservables())
        binding.overlayDate.setOnClickListener { dateSelect(it, binding.passportDateOfIssue) }
        binding.checkedAgreement.setOnClickListener { updateChecked() }
        binding.textAgree.setOnClickListener {
            if (CheckInternet.checkConnection(requireContext())) {
                viewModel.onShowAgreementClick()
            } else {
                CheckInternet.showDialogHaveNotInternet(requireContext())
                    .show(parentFragmentManager, DIALOG_INFO_TAG)
            }
        }
    }

    private val changeText = ArrayList<ViewChanges>()

    data class ViewChanges(
        val textLayout: TextInputLayout,
        val text: EditText,
        val type: CourierDataQueryType
    )

    private fun changeFieldObservables(): ArrayList<Flow<CourierDataUIAction>> {
        val changeTextObservables = ArrayList<Flow<CourierDataUIAction>>()

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
                binding.middleNameLayout,
                binding.middleName,
                CourierDataQueryType.MIDDLE_NAME
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
                binding.passportDepartmentCodeLayout,
                binding.passportDepartmentCode,
                CourierDataQueryType.PASSPORT_DEPARTMENT_CODE
            )
        )

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportIssuedByLayout,
                binding.passportIssuedBy,
                CourierDataQueryType.PASSPORT_ISSUED_BY
            )
        )


        changeTextObservables.add(createClickObserver().initListener(binding.next))

        return changeTextObservables
    }

    private fun getFormUserData() = mutableListOf(
        with(viewModel) {
            CourierData(decodeToUTF8(binding.surname.text.toString()), CourierDataQueryType.SURNAME)
            CourierData(decodeToUTF8(binding.firstName.text.toString()), CourierDataQueryType.NAME)
            CourierData(binding.inn.text.toString(), CourierDataQueryType.INN)
            CourierData(
                binding.passportSeries.text.toString(),
                CourierDataQueryType.PASSPORT_SERIES
            )
            CourierData(
                binding.passportNumber.text.toString(),
                CourierDataQueryType.PASSPORT_NUMBER
            )
            CourierData(
                binding.passportDateOfIssue.text.toString(),
                CourierDataQueryType.PASSPORT_DATE
            )
            CourierData(
                binding.passportDepartmentCode.text.toString(),
                CourierDataQueryType.PASSPORT_DEPARTMENT_CODE
            )
            CourierData(
                decodeToUTF8(binding.passportIssuedBy.text.toString()),
                CourierDataQueryType.PASSPORT_ISSUED_BY
            )
        }

    )

    private fun getCourierDocumentsEntity() = CourierDocumentsEntity(
        surName = binding.surname.text.toString().trim(),
        firstName = binding.firstName.text.toString().trim(),
        middleName = binding.middleName.text.toString().trim(),
        inn = binding.inn.text.toString().trim(),
        passportSeries = binding.passportSeries.text.toString().trim(),
        passportNumber = binding.passportNumber.text.toString().trim(),
        passportDateOfIssue = binding.passportDateOfIssue.text.toString().trim(),
        passportDepartmentCode = binding.passportDepartmentCode.text.toString().trim(),
        passportIssuedBy = binding.passportIssuedBy.text.toString().trim(),
        errorAnnotate = null
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
        fun initListener(view: View): Flow<CourierDataUIAction>
    }

    private fun createClickObserver(): ClickEventInterface {
        return ClickEventInterface { view ->
            view.clicks().map { CourierDataUIAction.CompleteClick(getFormUserData()) }
        }
    }

    fun interface TextChangesInterface {
        fun initListener(
            textInputLayout: TextInputLayout, editText: EditText, queryType: CourierDataQueryType
        ): Flow<CourierDataUIAction>
    }

    private fun createFieldChangesObserver(): TextChangesInterface {
        return TextChangesInterface { textInputLayout, editText, queryType ->
            changeText.add(ViewChanges(textInputLayout, editText, queryType))

            editText.textChanges()
                .drop(1)
                .map { it.toString() }
                .map { CourierDataUIAction.TextChange(it, queryType) }
        }
    }

    private fun initInputMethod() {
        inputMethod =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initObservers() {

        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
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
                is CourierDataUIState.Complete -> {
                    val textLayout = changeText.find { it.type == state.type }?.textLayout
                    textLayout?.error = getText(R.string.error_empty)
                }
                is CourierDataUIState.Error -> {
                    val textLayout = changeText.find { it.type == state.type }?.textLayout
                    textLayout?.error = errorFieldHint(CourierData(state.message, state.type))
                }
                is CourierDataUIState.ErrorFocus -> {
                    val textLayout = changeText.find { it.type == state.type }?.textLayout
                    textLayout?.error = errorFieldHint(CourierData(state.message, state.type))
                    if (state.type != CourierDataQueryType.PASSPORT_DATE) {
                        changeText.find { it.type == state.type }?.text?.let {
                            it.setSelection(it.length())
                            it.requestFocus()
                        }
                    }
                }
                CourierDataUIState.Next -> {
                    viewModel.onNextClick(getCourierDocumentsEntity())
                }
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierDataNavAction.NavigateToCouriersCompleteRegistration -> {
                    findNavController().navigate(
                        CourierDataFragmentDirections.actionCourierDataFragmentToCouriersCompleteRegistrationFragment(
                            CourierDataExpectsParameters(state.phone)
                        )
                    )
                }
                CourierDataNavAction.NavigateToAgreement -> {
                    findNavController().navigate(
                        CourierDataFragmentDirections.actionCourierDataFragmentToCourierAgreementFragment()
                    )
                }
            }
        }

        viewModel.loaderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierDataUILoaderState.Disable -> binding.next.isEnabled = false
                CourierDataUILoaderState.Enable -> binding.next.isEnabled = true
                CourierDataUILoaderState.Progress -> binding.next.isEnabled = false
            }
        }
    }

    private fun errorFieldHint(field: CourierData): CharSequence {
        if (field.text.isEmpty()) {
            return getText(R.string.error)
        }
        return when (field.type) {
            CourierDataQueryType.SURNAME -> getText(R.string.error_register_letter)
            CourierDataQueryType.MIDDLE_NAME -> getText(R.string.error_register_letter)
            CourierDataQueryType.NAME -> getText(R.string.error_register_letter)
            CourierDataQueryType.INN -> getText(R.string.error_register_size_12)
            CourierDataQueryType.PASSPORT_SERIES -> getText(R.string.error_register_size_4)
            CourierDataQueryType.PASSPORT_NUMBER -> getText(R.string.error_register_size_6)
            CourierDataQueryType.PASSPORT_DATE -> getText(R.string.error)
            CourierDataQueryType.PASSPORT_DEPARTMENT_CODE -> getText(R.string.error_register_size_6)
            CourierDataQueryType.PASSPORT_ISSUED_BY -> getText(R.string.error)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    companion object {
        const val REGISTER_FORM_PARAMS = "REGISTER_FORM_PARAMS"
        const val DATE_TIME_PICKER_FRAGMENT = "date_time_picker_fragment"
        const val DATE_PICKER_PATTERN = "dd.MM.yyyy"
    }

}


@Parcelize
data class CourierDataParameters(
    val phone: String,
    val docs: CourierDocumentsEntity
) : Parcelable


data class CourierData(val text: String, val type: CourierDataQueryType)
