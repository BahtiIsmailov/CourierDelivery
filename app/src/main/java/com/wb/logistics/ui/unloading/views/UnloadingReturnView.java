package com.wb.logistics.ui.unloading.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.wb.logistics.R;

public class UnloadingReturnView extends FrameLayout {

    private final static String DEFAULT_COUNT_BOX_TEXT = "0/0";
    private final static int DEFAULT_CURRENT_STATE = com.wb.logistics.ui.unloading.views.UnloadingReturnMode.EMPTY;

    private int enableColor;
    private int disableColor;
    private int denyColor;

    private View background;
    private TextView countBoxTextView;
    private ImageView undoIcon;
    private TextView listBoxTextView;

    private String countBox = DEFAULT_COUNT_BOX_TEXT;
    private int currentState = DEFAULT_CURRENT_STATE;

    public UnloadingReturnView(Context context) {
        super(context);
        init(null);
    }

    public UnloadingReturnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public UnloadingReturnView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        View layoutView = inflater.inflate(R.layout.unloading_return, this, false);
        background = layoutView.findViewById(R.id.background);
        countBoxTextView = layoutView.findViewById(R.id.count_box);
        undoIcon = layoutView.findViewById(R.id.undo);
        listBoxTextView = layoutView.findViewById(R.id.list_box);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.UnloadingReturnView);
            try {
                countBox = array.getString(R.styleable.UnloadingReturnView_unloading_return_box);
                currentState = array.getInteger(R.styleable.UnloadingReturnView_unloading_return_state, DEFAULT_CURRENT_STATE);
                disableColor = getResources().getColor(R.color.icon_default);
                enableColor = getResources().getColor(R.color.icon_warning);
                denyColor = getResources().getColor(R.color.icon_deny);
            } catch (NullPointerException exception) {
                initDefaultState();
            } finally {
                array.recycle();
            }
        }
    }

    private void initDefaultState() {
        countBox = getResources().getString(R.string.unloading_boxes_return_empty_count);
        currentState = DEFAULT_CURRENT_STATE;
    }

    private void initState() {
        switch (currentState) {
            case com.wb.logistics.ui.unloading.views.UnloadingReturnMode.EMPTY:
                emptyState();
                break;
            case UnloadingReturnMode.COMPLETE:
                containsCompleteState();
                break;
            case UnloadingReturnMode.ACTIVE:
                containsActiveState();
                break;
            case UnloadingReturnMode.DENY:
                containsDenyState();
                break;
        }
    }

    private void emptyState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.scanner_rounded_corner_empty));
        undoIcon.setColorFilter(disableColor, PorterDuff.Mode.SRC_ATOP);
        countBoxTextView.setText(getResources().getString(R.string.unloading_boxes_return_empty_count));
        listBoxTextView.setVisibility(INVISIBLE);
    }

    private void containsCompleteState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.scanner_rounded_corner_empty));
        undoIcon.setColorFilter(disableColor, PorterDuff.Mode.SRC_ATOP);
        countBoxTextView.setText(countBox);
        listBoxTextView.setVisibility(listVisible());
    }

    private void containsActiveState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_has_added_trans));
        undoIcon.setColorFilter(enableColor, PorterDuff.Mode.SRC_ATOP);
        countBoxTextView.setText(countBox);
        listBoxTextView.setVisibility(listVisible());
    }

    private void containsDenyState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_deny_trans));
        undoIcon.setColorFilter(denyColor, PorterDuff.Mode.SRC_ATOP);
        countBoxTextView.setText(countBox);
        listBoxTextView.setVisibility(listVisible());
    }

    private int listVisible() {
        return (countBox.startsWith("0")) ? GONE : VISIBLE;
    }

    public void setCountBox(String countBox, int currentState) {
        this.countBox = countBox;
        this.currentState = currentState;
        initState();
    }

    public void setCountBox(String countBox) {
        this.countBox = countBox;
        initState();
    }

    public void setState(int currentState) {
        this.currentState = currentState;
        initState();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        listBoxTextView.setOnClickListener(onClickListener);
    }

}
