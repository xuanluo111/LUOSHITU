package com.fongmi.android.tv.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Product;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Value;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.FragmentTypeBinding;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.ui.activity.SearchActivity;
import com.fongmi.android.tv.ui.activity.VideoActivity;
import com.fongmi.android.tv.ui.activity.WebActivity;
import com.fongmi.android.tv.ui.adapter.VodAdapter;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.custom.CustomScroller;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.HashMap;

public class TypeFragment extends BaseFragment implements CustomScroller.Callback, VodAdapter.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    /** 洛视频：超过这个时间还没拿到结果就结束转圈，不能让老人对着一个永远转的圈 */
    private static final long TIMEOUT_LOAD = 25_000;

    private HashMap<String, String> mExtends;
    private FragmentTypeBinding mBinding;
    private CustomScroller mScroller;
    private SiteViewModel mViewModel;
    private VodAdapter mAdapter;

    private final Runnable mLoadTimeout = () -> {
        if (mBinding == null || !mBinding.progressLayout.isProgress()) return;
        // 转圈永远收不掉是实测最常见的「 App 卡死」假象：站源慢时请求可能一直不回来。
        // 这里把转圈换成看得懂的空态，并把「下一步干什么」写清楚。
        showEmptyText(R.string.error_load_timeout);
        mBinding.progressLayout.showEmpty();
    };

    public static TypeFragment newInstance(String key, String typeId, Style style, HashMap<String, String> extend, boolean folder, int y) {
        Bundle args = new Bundle();
        args.putInt("y", y);
        args.putString("key", key);
        args.putString("typeId", typeId);
        args.putBoolean("folder", folder);
        args.putParcelable("style", style);
        args.putSerializable("extend", extend);
        TypeFragment fragment = new TypeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String getKey() {
        return getArguments().getString("key");
    }

    private String getTypeId() {
        return getArguments().getString("typeId");
    }

    private Style getStyle() {
        return isFolder() ? Style.list() : getSite().getStyle(getArguments().getParcelable("style"));
    }

    private HashMap<String, String> getExtend() {
        return (HashMap<String, String>) getArguments().getSerializable("extend");
    }

    private int getY() {
        return getArguments().getInt("y");
    }

    private boolean isFolder() {
        return getArguments().getBoolean("folder");
    }

    private boolean isHome() {
        return "home".equals(getTypeId());
    }

    private Site getSite() {
        return VodConfig.get().getSite(getKey());
    }

    private FolderFragment getParent() {
        return (FolderFragment) getParentFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentTypeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        showProgress();
        mScroller = new CustomScroller(this);
        mExtends = getExtend();
        setRecyclerView();
        setViewModel();
        getVideo();
    }

    @Override
    protected void initEvent() {
        mBinding.swipeLayout.setOnRefreshListener(this);
        mBinding.recycler.addOnScrollListener(mScroller);
    }

    private void setRecyclerView() {
        mBinding.recycler.setTranslationY(-ResUtil.dp2px(getY()));
        mBinding.recycler.setHasFixedSize(true);
        setStyle(getStyle());
    }

    private void setStyle(Style style) {
        mBinding.recycler.setAdapter(mAdapter = new VodAdapter(this, style, Product.getSpec(requireActivity(), style)));
        mBinding.recycler.setLayoutManager(style.isList() ? new LinearLayoutManager(requireActivity()) : new GridLayoutManager(getContext(), Product.getColumn(requireActivity(), style)));
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        mViewModel.getResult().observe(getViewLifecycleOwner(), this::setAdapter);
        mViewModel.getAction().observe(getViewLifecycleOwner(), result -> WebActivity.handleAction(requireActivity(), result));
    }

    private void getHome() {
        mAdapter.clear(() -> mViewModel.homeContent());
    }

    private void getVideo() {
        mScroller.reset();
        mAdapter.clear(() -> {
            if (!mBinding.swipeLayout.isRefreshing()) showProgress();
            if (isHome()) setAdapter(getParent().getResult());
            else getVideo(getTypeId(), "1");
        });
    }

    /** 起转圈的同时挂上看门狗，结果回来后由 setAdapter 拆掉 */
    private void showProgress() {
        if (mBinding == null) return;
        mBinding.progressLayout.showProgress();
        App.post(mLoadTimeout, TIMEOUT_LOAD);
    }

    private void getVideo(String typeId, String page) {
        mViewModel.categoryContent(getKey(), typeId, page, true, mExtends);
    }

    private void setAdapter(Result result) {
        App.removeCallbacks(mLoadTimeout);
        boolean first = mScroller.first();
        int size = result.getList().size();
        // 洛视频：站源返回空列表时，原先只有一句「这里什么都没有👀」，没说下一步干什么；
        // 换成带动作的句子，并且点一下重新拉取。
        if (first && size == 0) showEmptyText(R.string.empty_vod_hint);
        mBinding.progressLayout.showContent(first, size);
        mBinding.swipeLayout.setRefreshing(false);
        mScroller.endLoading(result);
        if (size > 0) addVideo(result);
    }

    /** 洛视频：空态文案 + “这句提示本身就是重试按钮”的适老化处理 */
    private void showEmptyText(int resId) {
        TextView text = mBinding.progressLayout.findViewById(R.id.text);
        if (text == null) return;
        text.setText(resId);
        // 适老化：下拉刷新是个隐藏手势，老人不会想到，直接把这句提示本身做成重试入口。
        if (text.getParent() instanceof View empty) empty.setOnClickListener(v -> onRefresh());
    }

    private void addVideo(Result result) {
        Style style = result.getVod().getStyle(getStyle());
        if (!style.equals(mAdapter.getStyle())) setStyle(style);
        mAdapter.addAll(result.getList(), this::checkMore);
    }

    private void checkMore() {
        mBinding.recycler.post(() -> {
            if (isHome()) return;
            mScroller.checkMore(mBinding.recycler);
        });
    }

    public void scrollToTop() {
        mBinding.recycler.smoothScrollToPosition(0);
    }

    public void setFilter(String key, Value value) {
        if (value.isSelected()) mExtends.put(key, value.getV());
        else mExtends.remove(key);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        if (isHome()) getHome();
        else getVideo();
    }

    @Override
    public boolean onLoadMore(String page) {
        if (isHome()) return false;
        getVideo(getTypeId(), page);
        return true;
    }

    @Override
    public void onItemClick(Vod item) {
        if (item.isAction()) {
            mViewModel.action(getKey(), item.getAction());
        } else if (item.isFolder()) {
            getParent().openFolder(item.getId(), mExtends);
        } else {
            if (getSite().isIndex()) SearchActivity.start(requireActivity(), item.getName());
            else VideoActivity.start(requireActivity(), getKey(), item.getId(), item.getName(), item.getPic(), isFolder() ? item.getName() : null);
        }
    }

    @Override
    public boolean onLongClick(Vod item) {
        if (item.isAction() || item.isFolder()) return false;
        SearchActivity.start(requireActivity(), item.getName());
        return true;
    }
}
