package com.wb.logistics.ui.flights.delegates

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ListView
import androidx.recyclerview.widget.RecyclerView
import com.wb.logistics.R
import com.wb.logistics.adapters.BaseAdapterDelegate
import com.wb.logistics.databinding.FlightsDelegateBinding
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flights.delegates.items.FlightItem

class FlightsDelegate(context: Context) :
    BaseAdapterDelegate<FlightItem?, FlightsDelegate.RouterViewHolder?>(context) {

    override fun isForViewType(item: BaseItem): Boolean {
        return item is FlightItem
    }

    override fun getLayoutId(): Int {
        return R.layout.flights_delegate
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(item: FlightItem, holder: RouterViewHolder) {
        holder.itemView.tag = item
        val binding = holder.binding
        binding.flightText.text = item.flight
        binding.dateText.text = item.date
        binding.timeText.text = item.time
        binding.parkingNumberText.text = item.parkingNumber
        binding.routesTitleText.text = item.routesTitle
        initRoutes(item.routes, binding.routeListView)
    }

    private fun initRoutes(routeItems: List<String>, listLegendView: ListView) {
        listLegendView.adapter = FlightsAdapter(context, routeItems)
    }

    companion object {
        const val DURATION = 500L
    }

    inner class RouterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private var isViewExpanded = true
        private var originalHeight = 0
        var binding: FlightsDelegateBinding = FlightsDelegateBinding.bind(itemView)

        init {
            initListener()
        }

        private fun initListener() {
            binding.showRouteImage.setOnClickListener {
//                val animId =
//                    if (isViewExpanded) R.anim.rotate_center_top_to_bottom else R.anim.rotate_center_bottom_to_top

                val animationRotateCenter =
                    AnimationUtils.loadAnimation(context, R.anim.rotate_center_circle)
                animationRotateCenter.fillAfter = true
                binding.showRouteImage.startAnimation(animationRotateCenter)
                val view: View = binding.routeListView
                if (originalHeight == 0) {
                    originalHeight = view.height
                }
                val valueAnimator: ValueAnimator
                if (!isViewExpanded) {
                    view.visibility = View.VISIBLE
                    view.isEnabled = true
                    isViewExpanded = true
                    valueAnimator = ValueAnimator.ofInt(0, originalHeight)
                } else {
                    isViewExpanded = false
                    valueAnimator = ValueAnimator.ofInt(originalHeight, 0)
                    val alphaAnimation: Animation = AlphaAnimation(1.00f, 0.00f)
                    alphaAnimation.duration = DURATION
                    alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            view.visibility = View.GONE
                            view.isEnabled = false
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                    view.startAnimation(alphaAnimation)
                }
                valueAnimator.duration = DURATION
                valueAnimator.interpolator = AccelerateDecelerateInterpolator()
                valueAnimator.addUpdateListener { animation: ValueAnimator ->
                    view.layoutParams.height = animation.animatedValue as Int
                    view.requestLayout()
                }
                valueAnimator.start()
            }
        }

    }
}