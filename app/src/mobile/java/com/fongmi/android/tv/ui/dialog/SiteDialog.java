package com.fongmi.android.tv.ui.dialog;

import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.DialogSiteBinding;
import com.fongmi.android.tv.impl.SiteListener;
import com.fongmi.android.tv.ui.adapter.SiteAdapter;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class SiteDialog extends BaseAlertDialog implements SiteAdapter.OnClickListener {

    private DialogSiteBinding binding;
    private SiteListener listener;
    private SiteAdapter adapter;
    private boolean search;
    private boolean change;

    public static SiteDialog create() {
        return new SiteDialog();
    }

    public SiteDialog search() {
        search = true;
        return this;
    }

    public SiteDialog change() {
        change = true;
        return this;
    }

    public void show(Fragment fragment) {
        show(fragment.getChildFragmentManager(), null);
        if (fragment instanceof SiteListener) listener = (SiteListener) fragment;
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogSiteBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setTitle(getTitleRes()).setView(getBinding().getRoot());
    }

    /**
     * 洛视频：原来这个弹窗一个标题都没有，老人点进来只看到一列圆角条，不知道该选什么；
     * 三个入口的意图不一样，标题跟着入口说话。
     */
    private int getTitleRes() {
        if (search && change) return R.string.dialog_site_title_home;
        if (search) return R.string.dialog_site_title_search;
        return R.string.dialog_site_title_change;
    }

    @Override
    protected void initView() {
        adapter = new SiteAdapter(this);
        binding.recycler.setAdapter(adapter);
        adapter.search(search).change(change);
        binding.recycler.setItemAnimator(null);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.addItemDecoration(new SpaceItemDecoration(1, 8));
        // 洛视频：站点多时列表按 376dp 钉死，平板横屏上上下各露半截，看着像坏了；
        // 改成按屏高给一半高度，弹窗稳定落在屏幕中间，也不至于把屏幕盖满。
        binding.recycler.setMaxHeight(ResUtil.getScreenHeight() / 2);
        binding.recycler.post(() -> binding.recycler.scrollToPosition(VodConfig.getHomeIndex()));
    }

    @Override
    public void onTextClick(Site item) {
        if (listener != null) listener.setSite(item);
        dismiss();
    }

    @Override
    public void onSearchClick(int position, Site item) {
        item.setSearchable(!item.isSearchable()).saveSettings();
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onChangeClick(int position, Site item) {
        item.setChangeable(!item.isChangeable()).saveSettings();
        adapter.notifyItemChanged(position);
    }

    @Override
    public boolean onSearchLongClick(Site item) {
        boolean result = !item.isSearchable();
        List<Site> sites = adapter.getItems();
        sites.forEach(site -> site.setSearchable(result));
        Site.saveSettings(sites);
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        Notify.show(getString(R.string.dialog_action_bulk_result, getString(R.string.dialog_action_site_search), getString(result ? R.string.setting_on : R.string.setting_off)));
        return true;
    }

    @Override
    public boolean onChangeLongClick(Site item) {
        boolean result = !item.isChangeable();
        List<Site> sites = adapter.getItems();
        sites.forEach(site -> site.setChangeable(result));
        Site.saveSettings(sites);
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        Notify.show(getString(R.string.dialog_action_bulk_result, getString(R.string.dialog_action_site_change), getString(result ? R.string.setting_on : R.string.setting_off)));
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter.getItemCount() == 0) dismiss();
        else if (ResUtil.isLand(requireContext())) setWidth(0.5f);
    }
}
