package ru.wb.go.ui.unloadingscan.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import ru.wb.go.R;

public class UnloadingInfoView extends FrameLayout {

    private final static String DEFAULT_CODE_BOX_TEXT = "";
    private final static int DEFAULT_STATE = UnloadingInfoMode.EMPTY;

    private TextView emptyBoxTextView;
    private TextView currentBoxTextView;
    private TextView barcode;
    private TextView status;

    private String codeBox = DEFAULT_CODE_BOX_TEXT;
    private int state = DEFAULT_STATE;

    public UnloadingInfoView(Context context) {
        super(context);
        init(null);
    }

    public UnloadingInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public UnloadingInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        View layoutView = inflater.inflate(R.layout.unloading_info_box, this, false);
        emptyBoxTextView = layoutView.findViewById(R.id.empty_box);
        currentBoxTextView = layoutView.findViewById(R.id.current_box);
        barcode = layoutView.findViewById(R.id.barcode);
        status = layoutView.findViewById(R.id.status);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.UnloadingInfoView);

            try {
                codeBox = array.getString(R.styleable.UnloadingInfoView_unloading_info_box);
                state = array.getInteger(R.styleable.UnloadingInfoView_unloading_info_state, DEFAULT_STATE);
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
            case UnloadingInfoMode.EMPTY:
                emptyState();
                break;
            case UnloadingInfoMode.UNLOADING:
                unloadingState();
                break;
            case UnloadingInfoMode.RETURN:
                returnState();
                break;
            case UnloadingInfoMode.UNLOAD_DENY:
                containsUnloadDenyState();
                break;
            case UnloadingInfoMode.NOT_INFO_DENY:
                containsNotInfoDenyState();
                break;
        }
    }

    private void emptyState() {
        emptyBoxTextView.setVisibility(VISIBLE);
        currentBoxTextView.setVisibility(INVISIBLE);
        barcode.setVisibility(INVISIBLE);
        status.setVisibility(INVISIBLE);
    }

    private void unloadingState() {
        emptyBoxTextView.setVisibility(GONE);
        currentBoxTextView.setVisibility(VISIBLE);
        barcode.setVisibility(VISIBLE);
        status.setVisibility(VISIBLE);
        barcode.setText(codeBox);
        status.setText(getResources().getString(R.string.unloading_boxes_info_box_unload));
        status.setTextColor(getResources().getColor(R.color.icon_success));
    }

    private void returnState() {
        emptyBoxTextView.setVisibility(GONE);
        currentBoxTextView.setVisibility(VISIBLE);
        barcode.setVisibility(VISIBLE);
        status.setVisibility(VISIBLE);
        barcode.setText(codeBox);
        status.setText(getResources().getString(R.string.unloading_boxes_info_box_return));
        status.setTextColor(getResources().getColor(R.color.icon_warning));
    }

    private void containsUnloadDenyState() {
        emptyBoxTextView.setVisibility(GONE);
        currentBoxTextView.setVisibility(VISIBLE);
        barcode.setVisibility(VISIBLE);
        status.setVisibility(VISIBLE);
        barcode.setText(codeBox);
        status.setText(getResources().getString(R.string.unloading_boxes_info_box_return_on_car));
        status.setTextColor(getResources().getColor(R.color.icon_deny));
    }

    private void containsNotInfoDenyState() {
        emptyBoxTextView.setVisibility(GONE);
        currentBoxTextView.setVisibility(VISIBLE);
        barcode.setVisibility(VISIBLE);
        status.setVisibility(VISIBLE);
        barcode.setText(codeBox);
        status.setText(getResources().getString(R.string.unloading_boxes_info_box_drop));
        status.setTextColor(getResources().getColor(R.color.icon_deny));
    }

    public void setState(String codeBox, int state) {
        this.codeBox = codeBox;
        this.state = state;
        initState();
    }

    public void setState(int state) {
        this.state = state;
        initState();
    }

}
