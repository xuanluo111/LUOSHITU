package com.fongmi.android.tv.ui.custom;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fongmi.android.tv.ui.base.BaseFragment;

import java.util.function.IntFunction;

public class FragmentStateManager {

    private final ViewGroup container;
    private final FragmentManager fm;
    private final IntFunction<Fragment> factory;

    public FragmentStateManager(ViewGroup container, FragmentManager fm, IntFunction<Fragment> factory) {
        this.container = container;
        this.factory = factory;
        this.fm = fm;
    }

    public boolean change(int position) {
        String tag = getTag(position);
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = factory.apply(position);
        } else {
            // 洛视频 A25/A39：覆盖安装后 FragmentManager 恢复的旧 tag 可能与本版类型错位，校验并重建
            Fragment fresh = factory.apply(position);
            if (!fresh.getClass().equals(fragment.getClass())) {
                fm.beginTransaction().remove(fragment).commitNowAllowingStateLoss();
                fragment = fresh;
            }
        }
        FragmentTransaction ft = fm.beginTransaction().setTransition(TRANSIT_FRAGMENT_OPEN);
        if (fm.findFragmentByTag(tag) == null) ft.add(container.getId(), fragment, tag);
        Fragment current = fm.getPrimaryNavigationFragment();
        if (current != null && current != fragment) ft.hide(current);
        ft.show(fragment).setPrimaryNavigationFragment(fragment).setReorderingAllowed(true).commitNowAllowingStateLoss();
        return true;
    }

    private String getTag(int position) {
        return "android:switcher:" + position;
    }

    public BaseFragment getFragment(int position) {
        return (BaseFragment) fm.findFragmentByTag(getTag(position));
    }

    public boolean isVisible(int position) {
        Fragment fragment = getFragment(position);
        return fragment != null && fragment.isVisible();
    }

    public boolean canBack(int position) {
        BaseFragment fragment = getFragment(position);
        return fragment != null && fragment.canBack();
    }
}
