package com.fongmi.android.tv.ui.fragment;

import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.github.catvod.utils.Prefers;
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
        root.setPadding(48, 64, 48, 48);
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
        view.setTextSize(26);
        view.setPadding(0, 24, 0, 24);
        return view;
    }

    private View contactLine() {
        String name = Prefers.getString("luoshitu_contact_name");
        String phone = Prefers.getString("luoshitu_contact_phone");
        TextView view = new TextView(requireContext());
        view.setText((name.isEmpty() && phone.isEmpty()) ? "家人还没有在这里留联系方式" : ElderText.clean(name) + "  " + phone);
        view.setTextSize(20);
        view.setPadding(0, 0, 0, 32);
        return view;
    }

    private View bigButton(String text, Runnable action) {
        Button button = new Button(requireContext());
        button.setText(text);
        button.setTextSize(22);
        button.setPadding(0, 24, 0, 24);
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 24;
        button.setLayoutParams(params);
        return button;
    }

    private void dial() {
        String phone = Prefers.getString("luoshitu_contact_phone");
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "请先在家属模式里保存联系方式", Toast.LENGTH_LONG).show();
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
