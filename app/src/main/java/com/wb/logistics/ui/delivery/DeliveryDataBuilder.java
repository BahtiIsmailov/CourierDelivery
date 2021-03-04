package com.wb.logistics.ui.delivery;

import androidx.annotation.NonNull;

import com.wb.logistics.mvp.model.base.BaseItem;

import java.util.List;

public interface DeliveryDataBuilder {

    @NonNull
    List<BaseItem> buildFlights();

}