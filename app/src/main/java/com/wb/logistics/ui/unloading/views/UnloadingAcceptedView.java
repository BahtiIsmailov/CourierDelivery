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

public class UnloadingAcceptedView extends FrameLayout {

    private final static String DEFAULT_COUNT_BOX_TEXT = "0/0";
    private final static int DEFAULT_CURRENT_STATE = UnloadingAcceptedMode.EMPTY;

    private int enableColor;
    private int disableColor;
    private int denyColor;

    private View background;
    private TextView countBoxTextView;
    private ImageView redoIcon;
    private TextView listBoxTextView;

    private String countBox = DEFAULT_COUNT_BOX_TEXT;
    private int currentState = DEFAULT_CURRENT_STATE;

    public UnloadingAcceptedView(Context context) {
        super(context);
        init(null);
    }

    public UnloadingAcceptedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public UnloadingAcceptedView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        View layoutView = inflater.inflate(R.layout.unloading_accepted, this, false);
        background = layoutView.findViewById(R.id.background);
        countBoxTextView = layoutView.findViewById(R.id.count_box);
        redoIcon = layoutView.findViewById(R.id.redo);
        listBoxTextView = layoutView.findViewById(R.id.list_box);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.UnloadingAcceptedView);
            try {
                countBox = array.getString(R.styleable.UnloadingAcceptedView_unloading_accepted_box);
                currentState = array.getInteger(R.styleable.UnloadingAcceptedView_unloading_accepted_state, DEFAULT_CURRENT_STATE);
                enableColor = getResources().getColor(R.color.icon_success);
                disableColor = getResources().getColor(R.color.icon_default);
                denyColor = getResources().getColor(R.color.icon_deny);
            } catch (NullPointerException exception) {
                initDefaultState();
            } finally {
                array.recycle();
            }
        }
    }

    private void initDefaultState() {
        countBox = getResources().getString(R.string.unloading_boxes_empty_count);
        currentState = DEFAULT_CURRENT_STATE;
    }

    private void initState() {
        switch (currentState) {
            case UnloadingAcceptedMode.EMPTY:
                emptyState();
                break;
            case UnloadingAcceptedMode.COMPLETE:
                containsCompleteState();
                break;
            case UnloadingAcceptedMode.ACTIVE:
                containsCompleteActiveState();
                break;
            case UnloadingAcceptedMode.DENY:
                containsDenyState();
                break;
        }
    }

    private void emptyState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.scanner_rounded_corner_empty));
        countBoxTextView.setText(getResources().getString(R.string.unloading_boxes_empty_count));
        redoIcon.setColorFilter(disableColor, PorterDuff.Mode.SRC_ATOP);
        listBoxTextView.setVisibility(INVISIBLE);
    }

    private void containsCompleteState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.scanner_rounded_corner_empty));
        redoIcon.setColorFilter(disableColor, PorterDuff.Mode.SRC_ATOP);
        countBoxTextView.setText(countBox);
        listBoxTextView.setVisibility(listVisible());
    }

    private void containsCompleteActiveState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_complete_trans));
        redoIcon.setColorFilter(enableColor, PorterDuff.Mode.SRC_ATOP);
        countBoxTextView.setText(countBox);
        listBoxTextView.setVisibility(listVisible());
    }

    private void containsDenyState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_deny_trans));
        redoIcon.setColorFilter(denyColor, PorterDuff.Mode.SRC_ATOP);
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
