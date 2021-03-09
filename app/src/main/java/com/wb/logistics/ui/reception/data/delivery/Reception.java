package com.wb.logistics.ui.reception.data.delivery;

import androidx.room.Entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "delivery")
public class Reception {
    @SerializedName("template")
    @Expose
    private String template;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}

