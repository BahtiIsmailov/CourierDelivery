package com.wb.logistics.ui.dcloading.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wb.logistics.R;

public class ReceptionInfoView extends FrameLayout {

    private final static String DEFAULT_CODE_BOX_TEXT = "";
    private final static String STATUS_BOX_EMPTY_TEXT = "";
    private final static int DEFAULT_STATE = ReceptionInfoMode.EMPTY;

    private TextView emptyBoxTextView;
    private TextView currentBoxTextView;
    private TextView codeBoxTextView;
    private TextView statusBoxTextView;

    private String codeBox = DEFAULT_CODE_BOX_TEXT;
    private int state = DEFAULT_STATE;

    public ReceptionInfoView(Context context) {
        super(context);
        init(null);
    }

    public ReceptionInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ReceptionInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        View layoutView = inflater.inflate(R.layout.dc_loading_info_box, this, false);
        emptyBoxTextView = layoutView.findViewById(R.id.empty_box);
        currentBoxTextView = layoutView.findViewById(R.id.current_box);
        codeBoxTextView = layoutView.findViewById(R.id.count_box);
        statusBoxTextView = layoutView.findViewById(R.id.status_box);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ReceptionInfoView);

            try {
                codeBox = array.getString(R.styleable.ReceptionInfoView_reception_info_box);
                state = array.getInteger(R.styleable.ReceptionInfoView_reception_info_state, DEFAULT_STATE);
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
            case ReceptionInfoMode.EMPTY:
                emptyState();
                break;
            case ReceptionInfoMode.SUBMERGE:
                submergeState();
                break;
            case ReceptionInfoMode.RETURN:
                returnState();
                break;
            case ReceptionInfoMode.CONTAINS_HAS_ADDED:
                hasAddedState();
                break;
        }
    }

    private void emptyState() {
        emptyBoxTextView.setVisibility(VISIBLE);
        currentBoxTextView.setVisibility(INVISIBLE);
        codeBoxTextView.setVisibility(INVISIBLE);
        statusBoxTextView.setVisibility(INVISIBLE);
    }

    private void submergeState() {
        emptyBoxTextView.setVisibility(GONE);
        currentBoxTextView.setVisibility(VISIBLE);
        codeBoxTextView.setVisibility(VISIBLE);
        statusBoxTextView.setVisibility(VISIBLE);
        codeBoxTextView.setText(codeBox);
        statusBoxTextView.setText(getResources().getString(R.string.dc_loading_status_box_submerge));
        statusBoxTextView.setTextColor(getResources().getColor(R.color.complete));
    }

    private void returnState() {
        emptyBoxTextView.setVisibility(GONE);
        currentBoxTextView.setVisibility(VISIBLE);
        codeBoxTextView.setVisibility(VISIBLE);
        statusBoxTextView.setVisibility(VISIBLE);
        codeBoxTextView.setText(codeBox);
        statusBoxTextView.setText(getResources().getString(R.string.dc_loading_status_box_return));
        statusBoxTextView.setTextColor(getResources().getColor(R.color.error));
    }

    private void hasAddedState() {
        emptyBoxTextView.setVisibility(GONE);
        currentBoxTextView.setVisibility(VISIBLE);
        codeBoxTextView.setVisibility(VISIBLE);
        statusBoxTextView.setVisibility(VISIBLE);
        codeBoxTextView.setText(codeBox);
        statusBoxTextView.setText(getResources().getString(R.string.dc_loading_status_box_has_added));
        statusBoxTextView.setTextColor(getResources().getColor(R.color.complete));
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
