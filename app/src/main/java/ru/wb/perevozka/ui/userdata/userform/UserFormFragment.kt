package ru.wb.perevozka.ui.userdata.userform

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.dialogs.date.DatePickerDialog
import ru.wb.perevozka.ui.dialogs.date.OnDateSelected
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.ui.userdata.couriers.CouriersCompleteRegistrationParameters
import ru.wb.perevozka.ui.userdata.userform.UserFormFragment.TextChangesInterface
import ru.wb.perevozka.utils.SoftKeyboard
import ru.wb.perevozka.utils.time.DateTimeFormatter
import ru.wb.perevozka.views.ProgressImageButtonMode
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
        initViews()
        initListener()
        initInputMethod()
        initStateObserver()
    }

    private fun initViews() {
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.auth_user_data_label)
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        viewModel.onTextChanges(changeObservables())
        binding.passportDatePickerIcon.setOnClickListener {
            dateSelect(it, binding.passportDate)
        }
        binding.next.setOnClickListener {
            SoftKeyboard.hideKeyBoard(requireActivity())
            viewModel.onNextClick() }
    }

    private lateinit var activityCallback: OnDateSelected

    private fun dateSelect(it: View, dateText: EditText) {
        it.isEnabled = false
        initDatePiker(object : OnDateSelected {

            override fun onDateSelected(date: DateTime) {
                dateText.setText(formatDatePicker(date))
                it.isEnabled = true
            }

            override fun onCanceled() {
                it.isEnabled = true
            }

        })
    }

    private fun formatDatePicker(date: DateTime) =
        DateTimeFormat.forPattern(DATE_PICKER_PATTERN).print(date)

    private fun initDatePiker(activityCallback: OnDateSelected) {
        this.activityCallback = activityCallback
        val dateMin: DateTime = DateTimeFormatter.parseDate(AppConsts.PRIVATE_INFO_MIN_DATE_PIKER)
        val dateMax: DateTime
        var date: DateTime
        dateMax = DateTimeFormatter.currentDateTime().also { date = it }
        showDatePiker(dateMin, dateMax, date)
    }

    private fun showDatePiker(minDate: DateTime, maxDate: DateTime, date: DateTime) {
        val dateTimePickerDialog = DatePickerDialog.newInstance(minDate, maxDate, date)
        dateTimePickerDialog.setListener(activityCallback)
        activity?.supportFragmentManager?.let {
            dateTimePickerDialog.show(it, DATE_TIME_PICKER_FRAGMENT)
        }
    }

    private fun changeObservables(): ArrayList<Observable<Pair<String, UserFormQueryType>>> {
        val changeObservables: ArrayList<Observable<Pair<String, UserFormQueryType>>> =
            ArrayList<Observable<Pair<String, UserFormQueryType>>>()

        changeObservables.add(
            createTextChangesObserver().initListener(
                binding.surname,
                UserFormQueryType.SURNAME
            )
        )
        changeObservables.add(
            createTextChangesObserver().initListener(
                binding.name,
                UserFormQueryType.NAME
            )
        )
        changeObservables.add(
            createTextChangesObserver().initListener(
                binding.patronymic,
                UserFormQueryType.PATRONYMIC
            )
        )
        changeObservables.add(
            createTextChangesObserver().initListener(
                binding.passportSeries,
                UserFormQueryType.PASSPORT_SERIES
            )
        )
        changeObservables.add(
            createTextChangesObserver().initListener(
                binding.passportNumber,
                UserFormQueryType.PASSPORT_NUMBER
            )
        )
        changeObservables.add(
            createTextChangesObserver().initListener(
                binding.passportDate,
                UserFormQueryType.PASSPORT_DATE
            )
        )
        changeObservables.add(
            createTextChangesObserver().initListener(
                binding.passportCode,
                UserFormQueryType.PASSPORT_CODE
            )
        )
        changeObservables.add(
            createTextChangesObserver().initListener(
                binding.inn,
                UserFormQueryType.INN
            )
        )
        return changeObservables
    }

    fun interface TextChangesInterface {
        fun initListener(
            textView: TextView, queryType: UserFormQueryType
        ): Observable<Pair<String, UserFormQueryType>>
    }

    private fun createTextChangesObserver(): TextChangesInterface {
        return TextChangesInterface { textView, queryType ->
            textView.textChanges()
                .map { it.toString() }
                .map { Pair(it, queryType) }
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

    private fun initStateObserver() {

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

        viewModel.navigationEvent.observe(viewLifecycleOwner, { state ->
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

        viewModel.loaderState.observe(viewLifecycleOwner, { state ->
            when (state) {
                UserFormUILoaderState.Disable -> binding.next.setState(ProgressImageButtonMode.DISABLED)
                UserFormUILoaderState.Enable -> {
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                    binding.overlayBoxes.visibility = View.GONE
                }
                UserFormUILoaderState.Progress -> {
                    binding.next.setState(ProgressImageButtonMode.PROGRESS)
                    binding.overlayBoxes.visibility = View.VISIBLE
                }
            }
        })
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


