package ru.wb.perevozka.ui.dcunloading.views;

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

import ru.wb.perevozka.R;

public class DcUnloadingAcceptedView extends FrameLayout {

    private final static String DEFAULT_COUNT_BOX_TEXT = "0/0";
    private final static int DEFAULT_CURRENT_STATE = DcUnloadingAcceptedMode.EMPTY;

    private int enableColor;
    private int disableColor;
    private int denyColor;

    private View background;
    private TextView countBoxTextView;
    private ImageView undoIcon;
    private TextView listBoxTextView;

    private String countBox = DEFAULT_COUNT_BOX_TEXT;
    private int currentState = DEFAULT_CURRENT_STATE;

    public DcUnloadingAcceptedView(Context context) {
        super(context);
        init(null);
    }

    public DcUnloadingAcceptedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DcUnloadingAcceptedView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        View layoutView = inflater.inflate(R.layout.dc_unloading_accepted, this, false);
        background = layoutView.findViewById(R.id.background);
        countBoxTextView = layoutView.findViewById(R.id.count_box);
        undoIcon = layoutView.findViewById(R.id.undo);
        listBoxTextView = layoutView.findViewById(R.id.list_box);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.DcUnloadingAcceptedView);
            try {
                countBox = array.getString(R.styleable.DcUnloadingAcceptedView_dc_unloading_accepted_box);
                currentState = array.getInteger(R.styleable.DcUnloadingAcceptedView_dc_unloading_accepted_state, DEFAULT_CURRENT_STATE);
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
            case DcUnloadingAcceptedMode.EMPTY:
                emptyState();
                break;
            case DcUnloadingAcceptedMode.COMPLETE:
                containsCompleteState();
                break;
            case DcUnloadingAcceptedMode.ACTIVE:
                containsActiveState();
                break;
            case DcUnloadingAcceptedMode.DENY:
                containsDenyState();
                break;
        }
    }

    private void emptyState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.scanner_rounded_corner_empty));
        countBoxTextView.setText(countBox);
        undoIcon.setColorFilter(disableColor, PorterDuff.Mode.SRC_ATOP);
        listBoxTextView.setVisibility(INVISIBLE);
    }

    private void containsCompleteState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.scanner_rounded_corner_empty));
        undoIcon.setColorFilter(disableColor, PorterDuff.Mode.SRC_ATOP);
        countBoxTextView.setText(countBox);
        listBoxTextView.setVisibility(listVisible());
    }

    private void containsActiveState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_complete_trans));
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
