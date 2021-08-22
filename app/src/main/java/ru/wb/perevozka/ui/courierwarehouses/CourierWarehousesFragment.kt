package ru.wb.perevozka.ui.courierwarehouses

import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierWarehouseFragmentBinding
import ru.wb.perevozka.ui.courierorders.CourierOrderParameters
import ru.wb.perevozka.ui.dialogs.InformationDialogFragment
import ru.wb.perevozka.ui.splash.KeyboardListener
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressButtonMode


class CourierWarehousesFragment : Fragment() {

    private val viewModel by viewModel<CourierWarehousesViewModel>()

    private var _binding: CourierWarehouseFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CourierWarehousesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private lateinit var progressDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierWarehouseFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initObservable()
        initListener()
        initProgressDialog()
        viewModel.update()
    }

    private fun initView() {
        (activity as NavToolbarListener).showToolbar()
        (activity as KeyboardListener).panMode()
    }

    private fun initObservable() {

        viewModel.navigateToMessage.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

        viewModel.warehouse.observe(viewLifecycleOwner) {
            when (it) {
                is CourierWarehousesUIState.InitItems -> {
                    binding.emptyList.visibility = GONE
                    binding.progress.visibility = GONE
                    binding.boxes.visibility = VISIBLE
                    binding.update.setState(ProgressButtonMode.ENABLE)
                    val callback = object : CourierWarehousesAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int, isChecked: Boolean) {
                            viewModel.onItemClick(index, isChecked)
                        }
                    }
                    adapter = CourierWarehousesAdapter(requireContext(), it.items, callback)
                    binding.boxes.adapter = adapter
                }
                is CourierWarehousesUIState.UpdateItems -> {
                    adapter.setItem(it.index, it.item)
                    adapter.notifyItemChanged(it.index, it.item)
                }
                is CourierWarehousesUIState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.boxes.visibility = GONE
                    binding.emptyTitle.text = it.info
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierWarehousesProgressState.Progress -> showProgressDialog()
                CourierWarehousesProgressState.ProgressComplete -> closeProgressDialog()
            }
        }

        viewModel.navigateUIState.observe(viewLifecycleOwner) {
            when (it) {
                CourierWarehousesUINavState.NavigateToBack -> findNavController().popBackStack()
                is CourierWarehousesUINavState.NavigateToCourierOrder ->
                    findNavController().navigate(
                        CourierWarehousesFragmentDirections.actionCourierWarehouseFragmentToCourierOrderFragment(
                            CourierOrderParameters(it.officeId, it.address)
                        )
                    )
                is CourierWarehousesUINavState.NavigateToMessageInfo -> showSimpleDialog(it)
            }

        }

    }

    private fun initListener() {
        binding.update.setOnClickListener { viewModel.onUpdateClick() }
    }

    private fun showSimpleDialog(it: CourierWarehousesUINavState.NavigateToMessageInfo) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.boxes
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.simple_layout_dialog, viewGroup, false)
        val message: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        builder.setView(dialogView)

        val alertDialog: AlertDialog = builder.create()

        message.text = it.message
        negative.setOnClickListener {
            alertDialog.dismiss()
        }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = it.button
        alertDialog.show()
    }

    // TODO: 20.08.2021 переработать
    private fun initProgressDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomProgressAlertDialog)
        val viewGroup: ViewGroup = binding.boxes
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_progress_layout_dialog, viewGroup, false)
        builder.setView(dialogView)
        progressDialog = builder.create()
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == ACTION_UP) {
                progressDialog.dismiss()
                viewModel.onCancelLoadClick()
            }
            true
        }
    }

    private fun closeProgressDialog() {
        if (progressDialog.isShowing) progressDialog.dismiss()
    }

    private fun showProgressDialog() {
        progressDialog.show()
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.boxes.layoutManager = layoutManager
        binding.boxes.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.boxes.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}