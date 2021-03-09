package com.wb.logistics.ui.reception

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
import com.wb.logistics.databinding.ReceptionFragmentBinding
import com.wb.logistics.mvp.model.base.BaseItem
import com.wb.logistics.ui.delivery.delegates.OnRouteEmptyCallback
import com.wb.logistics.ui.delivery.delegates.RouteDelegate
import com.wb.logistics.ui.delivery.delegates.RouteEmptyDelegate
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReceptionFragment : Fragment() {

    private val receptionViewModel by viewModel<ReceptionViewModel>()

    private var _binding: ReceptionFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ReceptionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}