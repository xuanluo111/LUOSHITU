package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.TooltipCompat;

public final class DialogActionView extends AppCompatImageView {

    public DialogActionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // 洛视频：图标本体只有 24dp，老人点不中。用 padding 把可点区域撑到 48dp，图标仍居中显示。
        float density = context.getResources().getDisplayMetrics().density;
        int target = Math.round(48 * density);
        int padding = Math.round(12 * density);
        setMinimumWidth(target);
        setMinimumHeight(target);
        setPadding(padding, padding, padding, padding);
        TooltipCompat.setTooltipText(this, getContentDescription());
    }

    @Override
    public void setContentDescription(@Nullable CharSequence description) {
        super.setContentDescription(description);
        TooltipCompat.setTooltipText(this, description);
    }
}
