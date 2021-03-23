package com.wb.logistics.ui.flights.delegates;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wb.logistics.R;
import com.wb.logistics.adapters.BaseAdapterDelegate;
import com.wb.logistics.databinding.FlightsLayoutRouteBinding;
import com.wb.logistics.mvvm.model.base.BaseItem;
import com.wb.logistics.ui.flights.delegates.items.FlightItem;

import java.util.List;

public class FlightsDelegate extends BaseAdapterDelegate<FlightItem, FlightsDelegate.RouterViewHolder> {

    public FlightsDelegate(@NonNull Context context) {
        super(context);
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flights_layout_route;
    }

    @NonNull
    @Override
    protected RouterViewHolder createViewHolder(@NonNull View view) {
        return new RouterViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightItem item, @NonNull RouterViewHolder holder) {
        holder.itemView.setTag(item);
        FlightsLayoutRouteBinding binding = holder.binding;
        binding.flightText.setText(item.getFlight());
        binding.dateText.setText(item.getDate());
        binding.timeText.setText(item.getTime());

        binding.parkingNumberText.setText(item.getParkingNumber());
        binding.routesTitleText.setText(item.getRoutesTitle());
        initRoutes(item.getRoutes(), binding.routeListView);
    }

    private void initRoutes(@NonNull List<String> routeItems,
                            @NonNull ListView listLegendView) {
        FlightsAdapter chartLegendAdapter = new FlightsAdapter(context, routeItems, idItem -> {
            // TODO: 03.03.2021 реализовать
        });
        listLegendView.setAdapter(chartLegendAdapter);
    }

    class RouterViewHolder extends RecyclerView.ViewHolder {

        private final static int DURATION = 500;
        private boolean isViewExpanded = true;
        private int originalHeight = 0;

        FlightsLayoutRouteBinding binding;

        private RouterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightsLayoutRouteBinding.bind(itemView);

            initListener();

        }


        private void initListener() {
            binding.showRouteImage.setOnClickListener(v -> {
                final int animId = isViewExpanded ? R.anim.rotate_center_top_to_bottom : R.anim.rotate_center_bottom_to_top;
                final Animation animationRotateCenter = AnimationUtils.loadAnimation(context, R.anim.rotate_center_circle);
                animationRotateCenter.setFillAfter(true);
                binding.showRouteImage.startAnimation(animationRotateCenter);

                View view = binding.routeListView;
                if (originalHeight == 0) {
                    originalHeight = view.getHeight();
                }
                ValueAnimator valueAnimator;
                if (!isViewExpanded) {
                    view.setVisibility(View.VISIBLE);
                    view.setEnabled(true);
                    isViewExpanded = true;
                    valueAnimator = ValueAnimator.ofInt(0, originalHeight);
                } else {
                    isViewExpanded = false;
                    valueAnimator = ValueAnimator.ofInt(originalHeight, 0);
                    Animation alphaAnimation = new AlphaAnimation(1.00f, 0.00f);
                    alphaAnimation.setDuration(DURATION);
                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            view.setVisibility(View.GONE);
                            view.setEnabled(false);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    view.startAnimation(alphaAnimation);
                }
                valueAnimator.setDuration(DURATION);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.addUpdateListener(animation -> {
                    view.getLayoutParams().height = (int) (Integer) animation.getAnimatedValue();
                    view.requestLayout();
                });
                valueAnimator.start();
            });
        }

    }

}