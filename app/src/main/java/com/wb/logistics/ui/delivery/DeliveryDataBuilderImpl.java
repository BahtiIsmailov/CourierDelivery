package com.wb.logistics.ui.delivery;

import androidx.annotation.NonNull;

import com.wb.logistics.mvp.model.base.BaseItem;
import com.wb.logistics.ui.delivery.res.DeliveryResourceProvider;

import java.util.List;

public class DeliveryDataBuilderImpl implements DeliveryDataBuilder {

    @NonNull
    private final DeliveryResourceProvider resourceProvider;

    public DeliveryDataBuilderImpl(@NonNull DeliveryResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @NonNull
    @Override
    public List<BaseItem> buildFlights() {
        return null;
    }

}