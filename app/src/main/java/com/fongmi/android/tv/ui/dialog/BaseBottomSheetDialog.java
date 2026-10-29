package com.fongmi.android.tv.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.activity.ComponentDialog;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.fongmi.android.tv.utils.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public abstract class BaseBottomSheetDialog extends BottomSheetDialogFragment {

    private final OnBackPressedCallback backCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            onBackInvoked();
        }
    };

    protected abstract ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window == null) return dialog;
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Util.isFullscreen(getActivity())) window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getBinding(inflater, container).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackDispatcher();
        initView();
        initEvent();
    }

    @Override
    public void onStart() {
        super.onStart();
        setSheet((BottomSheetDialog) requireDialog());
    }

    protected void initView() {
    }

    protected void initEvent() {
    }

    protected void onBackInvoked() {
    }

    protected final void setBackCallbackEnabled(boolean enabled) {
        backCallback.setEnabled(enabled);
    }

    protected int getMaxHeight() {
        return 0;
    }

    /** 洛视频：返回大于 0 的宽度就覆盖 material 默认的 640dp 上限，0 表示维持原样 */
    protected int getWidth() {
        return 0;
    }

    private void setSheet(BottomSheetDialog dialog) {
        FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;
        // 洛视频：material 给 sheet 的 style 带 640dp maxWidth，而容器子项默认 start 对齐，
        // 平板横屏（屏宽 > 640dp）上整块面板靠左、右侧露出窗口背景。所以只把水平方向改成居中。
        // 竖向 gravity 必须保持 material 默认的 TOP：BottomSheetBehavior 在 onLayoutChild 里按
        // offsetTopAndBottom(top - savedTop) 定位，一旦父布局用 BOTTOM 先把 sheet 摆到屏幕底部，
        // behavior 算出的修正量就是 0，面板会停在可视区之外，表现为「点了没反应、面板不显示」。
        // sheet 的 parent 是 CoordinatorLayout，其 LayoutParams 不能被强转成 FrameLayout.LayoutParams，
        // 强转会 ClassCastException 崩溃。
        ViewGroup.LayoutParams params = sheet.getLayoutParams();
        if (params instanceof CoordinatorLayout.LayoutParams coordinator) {
            coordinator.gravity = Gravity.CENTER_HORIZONTAL | verticalGravity(coordinator.gravity);
        } else if (params instanceof FrameLayout.LayoutParams frame) {
            frame.gravity = Gravity.CENTER_HORIZONTAL | verticalGravity(frame.gravity);
        }
        sheet.setLayoutParams(params);
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
        // 洛视频：640dp 这个上限是 BottomSheetBehavior.maxWidth 兜的，在 measure 阶段裁剪，
        // 只改 LayoutParams.width 会被它盖回去（实测面板宽度纹丝不动，必须两个一起改）。
        int width = getWidth();
        if (width > 0) {
            behavior.setMaxWidth(width);
            params.width = width;
            sheet.setLayoutParams(params);
        }
        setHeight(sheet, behavior);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

    private static int verticalGravity(int gravity) {
        int vertical = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        return vertical == Gravity.NO_GRAVITY ? Gravity.TOP : vertical;
    }

    private void setHeight(FrameLayout sheet, BottomSheetBehavior<FrameLayout> behavior) {
        int maxHeight = getMaxHeight();
        if (maxHeight <= 0) return;
        behavior.setMaxHeight(maxHeight);
        ViewGroup.LayoutParams params = sheet.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        sheet.setLayoutParams(params);
        ViewCompat.setOnApplyWindowInsetsListener(requireView(), (view, insets) -> {
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int navigation = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int height = maxHeight + Math.max(0, keyboard - navigation);
            if (behavior.getMaxHeight() != height) {
                behavior.setMaxHeight(height);
                sheet.requestLayout();
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(requireView());
    }

    private void setupBackDispatcher() {
        if (requireDialog() instanceof ComponentDialog dialog) dialog.getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backCallback);
    }

    @Override
    public void onDestroyView() {
        backCallback.setEnabled(false);
        super.onDestroyView();
    }
}
