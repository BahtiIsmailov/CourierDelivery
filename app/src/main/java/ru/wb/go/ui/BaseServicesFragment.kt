package ru.wb.go.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.Flow
import ru.wb.go.R
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.app.SupportListener

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseServiceFragment<VM : ServicesViewModel, VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : Fragment() {

    protected abstract val viewModel: VM
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initListener()
    }

    private fun initListener() {
        binding.root.findViewById<ImageButton>(R.id.support_app).setOnClickListener {
            (activity as SupportListener).showSupportDialog()
        }
    }

    private fun initObservers() {
        viewModel.networkState.observe(viewLifecycleOwner) {
            val ic = when (it) {
                is NetworkState.Complete -> R.drawable.ic_inet_complete
                else -> R.drawable.ic_inet_failed
            }

            binding.root.findViewById<ImageView>(R.id.no_internet_image).setImageDrawable(
                ContextCompat.getDrawable(requireContext(), ic)
            )
        }

//        viewModel.versionApp.observe(viewLifecycleOwner) {
//            binding.root.findViewById<TextView>(R.id.version_app).text = it
//        }

    }

    fun <T> Flow<T>.observeEvent(observer: (T) -> Unit) {
        lifecycleScope.launchWhenStarted {
            this@observeEvent.collect { event ->
                observer.invoke(event)
            }
        }
    }

    fun<T> LiveData<T>.observe(observer: (T) -> Unit) {
        this.observe(viewLifecycleOwner, observer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun Fragment.getHorizontalDividerDecoration(): DividerItemDecoration {
        val decoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        ResourcesCompat.getDrawable(resources, R.drawable.divider_line, null)
            ?.let { decoration.setDrawable(it) }
        return decoration
    }
}