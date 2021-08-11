package ru.wb.perevozka.ui.userdata.couriers

import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import ru.wb.perevozka.ui.splash.NavToolbarListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.AuthCouriersCompletionRegistrationFragmentBinding
import ru.wb.perevozka.ui.userdata.userform.UserFormFragment
import ru.wb.perevozka.ui.userdata.userform.UserFormParameters
import ru.wb.perevozka.ui.userdata.userform.UserFormViewModel
import ru.wb.perevozka.utils.SoftKeyboard
import ru.wb.perevozka.views.ProgressImageButtonMode

class CouriersCompleteRegistrationFragment : Fragment() {

    private val viewModel by viewModel<CouriersCompleteRegistrationViewModel> {
        parametersOf(requireArguments().getParcelable<CouriersCompleteRegistrationParameters>(PHONE_KEY))
    }

    private var _binding: AuthCouriersCompletionRegistrationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthCouriersCompletionRegistrationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initListener()
        initKeyboard()
    }

    private fun initKeyboard() {
        SoftKeyboard.hideKeyBoard(requireActivity())
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initObserver() {
        viewModel.navigateToUpdateDialogInfo.observe(viewLifecycleOwner) {
            showSimpleDialog()
            binding.updateStatus.setState(ProgressImageButtonMode.ENABLED)
        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when(it){
                CouriersCompleteRegistrationProgressState.Complete -> binding.updateStatus.setState(ProgressImageButtonMode.PROGRESS)
                CouriersCompleteRegistrationProgressState.Progress -> binding.updateStatus.setState(ProgressImageButtonMode.ENABLED)
            }
        }
    }

    private fun showSimpleDialog() {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.main
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.simple_layout_dialog, viewGroup, false)
        val title: TextView = dialogView.findViewById(R.id.title)
        val message: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        builder.setView(dialogView)

        val alertDialog: AlertDialog = builder.create()

        title.text = "Ваши данные еще не подтверждены"
        message.text = "Обратитесь к сотруднику на ПВЗ"
        negative.setOnClickListener { alertDialog.dismiss() }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = "Понятно"
        alertDialog.show()
    }

    private fun initListener() {
        binding.updateStatus.setOnClickListener { viewModel.onUpdateStatusClick() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val PHONE_KEY = "phone_key"
    }

}

@Parcelize
data class CouriersCompleteRegistrationParameters(val phone: String) : Parcelable