package com.wb.logistics.ui.delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.wb.logistics.adapters.DefaultAdapter
import com.wb.logistics.databinding.DeliveryFragmentBinding
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.delivery.delegates.OnRouteEmptyCallback
import com.wb.logistics.ui.delivery.delegates.RouteDelegate
import com.wb.logistics.ui.delivery.delegates.RouteEmptyDelegate
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeliveryFragment : Fragment() {

    private val deliveryViewModel by viewModel<DeliveryViewModel>()

    private var _binding: DeliveryFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DeliveryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initAdapter()
        deliveryViewModel.fetchFlights()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        deliveryViewModel.visibleStartAddingBoxes.observe(viewLifecycleOwner) {
            visibleStartAddingBoxes(it)
        }
        deliveryViewModel.flights.observe(viewLifecycleOwner) { displayItems(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayItems(items: List<BaseItem>) {
        adapter.clear()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    private fun initAdapter() {
        adapter = with(DefaultAdapter()) {
            addDelegate(RouteDelegate(requireContext()))
            addDelegate(RouteEmptyDelegate(requireContext(), object : OnRouteEmptyCallback {
                override fun onUpdateRouteClick() {
                    deliveryViewModel.updateScreenClick()
                }
            }))
        }
        binding.recyclerView.adapter = adapter
    }

    private fun visibleStartAddingBoxes(isVisible: Boolean) {
        binding.startAddingBoxesButton.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

}