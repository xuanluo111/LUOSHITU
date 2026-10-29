package com.fongmi.android.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Filter;
import com.fongmi.android.tv.databinding.DialogFilterBinding;
import com.fongmi.android.tv.impl.FilterListener;
import com.fongmi.android.tv.ui.adapter.FilterAdapter;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.List;

public class FilterDialog extends BaseBottomSheetDialog {

    private DialogFilterBinding binding;
    private FilterListener listener;
    private List<Filter> filter;

    public static FilterDialog create() {
        return new FilterDialog();
    }

    public FilterDialog filter(List<Filter> filter) {
        this.filter = filter;
        return this;
    }

    public void show(Fragment fragment) {
        for (Fragment f : fragment.getChildFragmentManager().getFragments()) if (f instanceof FilterDialog) return;
        show(fragment.getChildFragmentManager(), null);
        this.listener = (FilterListener) fragment;
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogFilterBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setAdapter(new FilterAdapter(listener, filter));
    }

    @Override
    protected int getWidth() {
        // 洛视频：material 默认把面板限定在 640dp 宽，平板横屏（屏宽 1244dp）上这条白底
        // 只占屏幕一半，筛选行也跟着被截断。屏宽本来就超过 640dp 时放宽到 92%，
        // 竖屏手机屏宽不到 640dp，返回 0 保持原样，不影响。
        int screen = ResUtil.getScreenWidth();
        return screen > ResUtil.dp2px(640) ? (int) (screen * 0.92f) : 0;
    }
}
