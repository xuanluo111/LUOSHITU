package com.fongmi.android.tv.ui.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.github.catvod.utils.Prefers;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.utils.ElderText;

/**
 * 帮助 tab（F6 求助闭环，老人常驻出口）：一键拨号 + 三个自助出口。
 * 与家属模式 HelpActivity 同源；此页为老人日常路径，绝不出现配置/接口/诊断术语。
 */
public class HelpFragment extends BaseFragment {

    private LinearLayout root;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        // 原代码直接传 px，在 2.25 倍密度屏上只剩一半视觉间距，这里统一换成 dp。
        root.setPadding(dp(24), dp(32), dp(24), dp(24));
        // 洛视频：本页是代码现搭的，TextView 不指定颜色就继承主题默认色；页面实际跑在深色容器上，
        // 默认色是深灰，实测标题与底色对比只有 1.0:1（肉眼几乎看不见）。
        // 这里把底色和文字色一起钉死，主题切到浅色也不会反过来变成白字白底。
        root.setBackgroundColor(color(R.color.elder_page_dark));
        return () -> root;
    }

    @Override
    protected void initView() {
        root.addView(title("遇到问题，点这里找家人"));
        root.addView(contactLine());
        root.addView(bigButton("一键拨号给家人", this::dial));
        root.addView(bigButton("关掉重新打开试试", this::reopen));
        root.addView(bigButton("把声音调大", this::raiseVolume));
        root.addView(bigButton("换个节目看", this::backToVod));
    }

    private View title(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(28);
        view.setTextColor(color(R.color.elder_text_on_dark));
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setPadding(0, dp(8), 0, dp(12));
        return view;
    }

    private View contactLine() {
        String name = Prefers.getString("luoshitu_contact_name");
        String phone = Prefers.getString("luoshitu_contact_phone");
        TextView view = new TextView(requireContext());
        view.setText((name.isEmpty() && phone.isEmpty()) ? "家人还没有在这里留联系方式" : ElderText.clean(name) + "  " + phone);
        view.setTextSize(20);
        view.setTextColor(color(R.color.elder_text_on_dark_secondary));
        view.setPadding(0, 0, 0, dp(24));
        return view;
    }

    private View bigButton(String text, Runnable action) {
        Button button = new Button(requireContext());
        button.setText(text);
        button.setTextSize(22);
        // 洛视频：原生的 Button 是系统方角灰底，跟全站圆角蓝底完全不同风格，老人看到会犹豫；
        // 这里直接换成适老化令牌里的主色圆角底 + 白字，四个出口长得一模一样，一眼认得是按钮。
        button.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.elder_button_primary));
        button.setTextColor(color(R.color.elder_on_primary));
        button.setGravity(Gravity.CENTER);
        button.setAllCaps(false);
        // 去掉默认的按钮阴影（靠 setStateListAnimator 清掉），否则圆角底外面会多一圈方影。
        button.setStateListAnimator(null);
        button.setPadding(dp(16), dp(12), dp(16), dp(12));
        // 原高度实测约 56dp，低于适老化要求的 64dp 触摸目标，老人容易点空。
        button.setMinHeight(getResources().getDimensionPixelSize(R.dimen.elder_touch));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(16);
        button.setLayoutParams(params);
        return button;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
    }

    private void dial() {
        String phone = Prefers.getString("luoshitu_contact_phone");
        if (phone.isEmpty()) {
            // 洛视频：老人不知道「家属模式」在哪，也不知道自己能去哪里填，
            // 这里把话说成「找谁办」，而不是把后台概念抛给使用者。
            Toast.makeText(requireContext(), "还没有家人的电话，叫家里人帮忙填一下", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
    }

    private void reopen() {
        requireActivity().recreate();
    }

    private void raiseVolume() {
        AudioManager audio = (AudioManager) requireContext().getSystemService(android.content.Context.AUDIO_SERVICE);
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int target = Math.max(audio.getStreamVolume(AudioManager.STREAM_MUSIC), (int) (max * 0.6f));
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0);
        Toast.makeText(requireContext(), "声音已经调大，可以开始看了", Toast.LENGTH_LONG).show();
    }

    private void backToVod() {
        // 换个节目看：直接切回找片首页，让老人重新挑（不走返回栈，避免误退应用）
        ((com.fongmi.android.tv.ui.activity.HomeActivity) requireActivity()).change(0);
    }
}
