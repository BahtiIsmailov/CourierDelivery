package com.wb.logistics.ui.dcunloading.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wb.logistics.R;

public class DcUnloadingInfoView extends FrameLayout {

    private final static String DEFAULT_CODE_BOX_TEXT = "";
    private final static int DEFAULT_STATE = DcUnloadingInfoMode.EMPTY;

    private TextView emptyBoxTextView;
    private TextView currentBoxTextView;
    private TextView barcode;
    private TextView status;

    private String codeBox = DEFAULT_CODE_BOX_TEXT;
    private int state = DEFAULT_STATE;

    public DcUnloadingInfoView(Context context) {
        super(context);
        init(null);
    }

    public DcUnloadingInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DcUnloadingInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        initView();
        initAttrs(attrs);
        initState();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutView = inflater.inflate(R.layout.dc_unloading_info_box, this, false);
        emptyBoxTextView = layoutView.findViewById(R.id.empty_box);
        currentBoxTextView = layoutView.findViewById(R.id.current_box);
        barcode = layoutView.findViewById(R.id.barcode);
        status = layoutView.findViewById(R.id.status);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.DcUnloadingInfoView);

            try {
                codeBox = array.getString(R.styleable.DcUnloadingInfoView_dc_unloading_info_box);
                state = array.getInteger(R.styleable.DcUnloadingInfoView_dc_unloading_info_state, DEFAULT_STATE);
            } catch (NullPointerException exception) {
                initDefaultState();
            } finally {
                array.recycle();
            }
        }
    }

    private void initDefaultState() {
        codeBox = getResources().getString(R.string.dc_loading_code_pref_box);
        state = DEFAULT_STATE;
    }

    private void initState() {
        switch (state) {
            case DcUnloadingInfoMode.EMPTY:
                emptyState();
                break;
            case DcUnloadingInfoMode.COMPLETE:
                completeState();
                break;
            case DcUnloadingInfoMode.NOT_INFO_DENY:
                notInfoState();
                break;
        }
    }

    private void emptyState() {
        emptyBoxTextView.setVisibility(VISIBLE);
        currentBoxTextView.setVisibility(INVISIBLE);
        barcode.setVisibility(INVISIBLE);
        status.setVisibility(INVISIBLE);
    }

    private void completeState() {
        emptyBoxTextView.setVisibility(GONE);
        currentBoxTextView.setVisibility(VISIBLE);
        barcode.setVisibility(VISIBLE);
        status.setVisibility(VISIBLE);
        barcode.setText(codeBox);
        status.setText(getResources().getString(R.string.dc_unloading_boxes_info_box_unload));
        status.setTextColor(getResources().getColor(R.color.icon_success));
    }

    private void notInfoState() {
        emptyBoxTextView.setVisibility(GONE);
        currentBoxTextView.setVisibility(VISIBLE);
        barcode.setVisibility(VISIBLE);
        status.setVisibility(VISIBLE);
        barcode.setText(codeBox);
        status.setText(getResources().getString(R.string.dc_unloading_boxes_not_info_box_unload));
        status.setTextColor(getResources().getColor(R.color.icon_deny));
    }

    public void setCodeBox(String codeBox, int state) {
        this.codeBox = codeBox;
        this.state = state;
        initState();
    }

    public void setCodeBox(int state) {
        this.state = state;
        initState();
    }

}
