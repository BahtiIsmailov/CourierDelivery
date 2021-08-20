package ru.wb.perevozka.ui.userdata.userform

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
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
import ru.wb.perevozka.databinding.AuthUserFormFragmentBinding
import ru.wb.perevozka.network.api.app.entity.CourierDocumentsEntity
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.dialogs.date.DatePickerDialog
import ru.wb.perevozka.ui.dialogs.date.OnDateSelected
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.ui.userdata.couriers.CouriersCompleteRegistrationParameters
import ru.wb.perevozka.ui.userdata.userform.UserFormFragment.ClickEventInterface
import ru.wb.perevozka.ui.userdata.userform.UserFormFragment.TextChangesInterface
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.time.DateTimeFormatter
import ru.wb.perevozka.views.ProgressButtonMode
import java.util.*


class UserFormFragment : Fragment(R.layout.auth_user_form_fragment) {

    private var _binding: AuthUserFormFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethod: InputMethodManager
    private val viewModel by viewModel<UserFormViewModel> {
        parametersOf(requireArguments().getParcelable<UserFormParameters>(PHONE_KEY))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthUserFormFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initInputMethod()
        initObservers()
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

        binding.checkedComplete.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onCheckedClick(isChecked, binding.checkedPersonal.isChecked)
        }
        binding.checkedPersonal.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onCheckedClick(isChecked, binding.checkedComplete.isChecked)
        }

    }

    private val changeText = ArrayList<ViewChanges>()

    data class ViewChanges(
        val textLayout: TextInputLayout,
        val text: EditText,
        val type: UserFormQueryType
    )

    private fun changeFieldObservables(): ArrayList<Observable<UserFormUIAction>> {
        val changeTextObservables = ArrayList<Observable<UserFormUIAction>>()

        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.surnameLayout,
                binding.surname,
                UserFormQueryType.SURNAME
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.firstNameLayout,
                binding.firstName,
                UserFormQueryType.NAME
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.innLayout,
                binding.inn,
                UserFormQueryType.INN
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportSeriesLayout,
                binding.passportSeries,
                UserFormQueryType.PASSPORT_SERIES
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportNumberLayout,
                binding.passportNumber,
                UserFormQueryType.PASSPORT_NUMBER
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportDateOfIssueLayout,
                binding.passportDateOfIssue,
                UserFormQueryType.PASSPORT_DATE
            )
        )
        changeTextObservables.add(
            createFieldChangesObserver().initListener(
                binding.passportCodeLayout,
                binding.passportCode,
                UserFormQueryType.PASSPORT_CODE
            )
        )

        LogUtils { logDebugApp(changeTextObservables.toString()) }

        changeTextObservables.add(createClickObserver().initListener(binding.next))

        return changeTextObservables
    }

    private fun getFormUserData() = mutableListOf(
        UserData(binding.surname.text.toString(), UserFormQueryType.SURNAME),
        UserData(binding.firstName.text.toString(), UserFormQueryType.NAME),
        UserData(binding.inn.text.toString(), UserFormQueryType.INN),
        UserData(binding.passportSeries.text.toString(), UserFormQueryType.PASSPORT_SERIES),
        UserData(binding.passportNumber.text.toString(), UserFormQueryType.PASSPORT_NUMBER),
        UserData(binding.passportDateOfIssue.text.toString(), UserFormQueryType.PASSPORT_DATE)
    )

    private fun getCourierDocumentsEntity() = CourierDocumentsEntity(
        surName = binding.surname.text.toString(),
        firstName = binding.firstName.text.toString(),
        middleName = binding.middleName.text.toString(),
        inn = binding.inn.text.toString(),
        passportSeries = binding.passportSeries.text.toString(),
        passportNumber = binding.passportNumber.text.toString(),
        passportDateOfIssue = binding.passportDateOfIssue.text.toString()
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
        fun initListener(view: View): Observable<UserFormUIAction>
    }

    private fun createClickObserver(): ClickEventInterface {
        return ClickEventInterface { view ->
            view.clicks().map { UserFormUIAction.CompleteClick(getFormUserData()) }
        }
    }

    fun interface TextChangesInterface {
        fun initListener(
            textInputLayout: TextInputLayout, editText: EditText, queryType: UserFormQueryType
        ): Observable<UserFormUIAction>
    }

    private fun createFieldChangesObserver(): TextChangesInterface {
        return TextChangesInterface { textInputLayout, editText, queryType ->
            changeText.add(ViewChanges(textInputLayout, editText, queryType))
            val textChanges = editText.textChanges()
                .map { it.toString() }
                .map { UserFormUIAction.TextChange(it, queryType) }
            val focusChanges = editText.focusChanges()
                .map { UserFormUIAction.FocusChange(editText.text.toString(), queryType, it) }
            Observable.merge(textChanges, focusChanges).skip(2)
        }
    }

    private fun initInputMethod() {
        inputMethod =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun showSimpleDialog(it: UserFormViewModel.NavigateToMessageInfo) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.loginLayout
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.simple_layout_dialog, viewGroup, false)
        val title: TextView = dialogView.findViewById(R.id.title)
        val message: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        builder.setView(dialogView)

        val alertDialog: AlertDialog = builder.create()

        title.text = it.title
        message.text = it.message
        negative.setOnClickListener {
            alertDialog.dismiss()
        }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = it.button
        alertDialog.setOnDismissListener {}
        alertDialog.show()
    }

    private fun initObservers() {

        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            showSimpleDialog(it)
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
                is UserFormUIState.Complete -> {
                    val textLayout = changeText.find { it.type == state.type }?.textLayout
                    textLayout?.error = getText(R.string.error_empty)
                }
                is UserFormUIState.Error -> {
                    val textLayout = changeText.find { it.type == state.type }?.textLayout
                    textLayout?.error = getText(R.string.error)
                }
                is UserFormUIState.ErrorFocus -> {
                    changeText.find { it.type == state.type }?.text?.let {
                        it.setSelection(it.length())
                        it.requestFocus()
                        scrollToViewTop(binding.scrollView, it)
                    }
                }
                UserFormUIState.Next -> {
                    viewModel.onNextClick(getCourierDocumentsEntity())
                }
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    is UserFormNavAction.NavigateToCouriersCompleteRegistration -> {
                        findNavController().navigate(
                            UserFormFragmentDirections.actionUserFormFragmentToCouriersCompleteRegistrationFragment(
                                CouriersCompleteRegistrationParameters(state.phone)
                            )
                        )
                    }
                }
            })

        viewModel.loaderState.observe(viewLifecycleOwner,
            { state ->
                when (state) {
                    UserFormUILoaderState.Disable -> binding.next.setState(ProgressButtonMode.DISABLE)
                    UserFormUILoaderState.Enable -> {
                        binding.next.setState(ProgressButtonMode.ENABLE)
                        binding.overlayBoxes.visibility = View.GONE
                    }
                    UserFormUILoaderState.Progress -> {
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

    companion object {
        const val PHONE_KEY = "phone_key"
        const val DATE_TIME_PICKER_FRAGMENT = "date_time_picker_fragment"
        const val DATE_PICKER_PATTERN = "dd.MM.yyyy"
    }

}

@Parcelize
data class UserFormParameters(val phone: String) : Parcelable

data class UserData(val text: String, val type: UserFormQueryType)


