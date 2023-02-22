package com.fongmi.android.tv.ui.fragment.child;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Product;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.FragmentHomeBinding;
import com.fongmi.android.tv.ui.activity.BaseFragment;
import com.fongmi.android.tv.ui.activity.DetailActivity;
import com.fongmi.android.tv.ui.adapter.HistoryAdapter;
import com.fongmi.android.tv.ui.adapter.VodAdapter;
import com.fongmi.android.tv.ui.fragment.VodFragment;

public class HomeFragment extends BaseFragment implements VodAdapter.OnClickListener, HistoryAdapter.OnClickListener {

    private GridLayoutManager mRecommendManager;
    private GridLayoutManager mHistoryManager;
    private HistoryAdapter mHistoryAdapter;
    private FragmentHomeBinding mBinding;
    private VodAdapter mVodAdapter;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    private VodFragment getParent() {
        return (VodFragment) getParentFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mBinding.progressLayout.showProgress();
        setRecyclerView();
        getHistory();
    }

    @Override
    protected void initEvent() {
        mBinding.getRoot().setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> getParent().toggleLink(scrollY - oldScrollY));
    }

    private void setRecyclerView() {
        mBinding.history.setHasFixedSize(true);
        mBinding.history.getItemAnimator().setChangeDuration(0);
        mBinding.history.setLayoutManager(mHistoryManager = new GridLayoutManager(getContext(), Product.getColumn(getActivity())));
        mBinding.history.setAdapter(mHistoryAdapter = new HistoryAdapter(this));
        mBinding.recommend.setHasFixedSize(true);
        mBinding.recommend.setLayoutManager(mRecommendManager = new GridLayoutManager(getContext(), Product.getColumn(getActivity())));
        mBinding.recommend.setAdapter(mVodAdapter = new VodAdapter(this));
    }

    public void getHistory() {
        mHistoryAdapter.addAll(History.get());
    }

    public void showContent(Result result) {
        mBinding.progressLayout.showContent();
        mVodAdapter.addAll(result.getList());
    }

    @Override
    public void onItemClick(Vod item) {
        if (item.shouldSearch()) onLongClick(item);
        else DetailActivity.start(getActivity(), item.getVodId(), item.getVodName());
    }

    @Override
    public boolean onLongClick(Vod item) {
        //CollectActivity.start(this, item.getVodName());
        return true;
    }

    @Override
    public void onItemClick(History item) {
        DetailActivity.start(getActivity(), item.getSiteKey(), item.getVodId(), item.getVodName());
    }

    @Override
    public void onItemDelete(History item) {
        mHistoryAdapter.remove(item.delete());
        App.post(() -> mBinding.history.requestLayout(), 250);
        if (mHistoryAdapter.getItemCount() == 0) setHistoryDelete(false);
    }

    @Override
    public boolean onLongClick() {
        setHistoryDelete(!mHistoryAdapter.isDelete());
        return true;
    }

    private void setHistoryDelete(boolean delete) {
        mHistoryAdapter.setDelete(delete);
        mHistoryAdapter.notifyItemRangeChanged(0, mHistoryAdapter.getItemCount());
    }

    public boolean canBack() {
        if (!mHistoryAdapter.isDelete()) return true;
        setHistoryDelete(false);
        return false;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mHistoryManager.setSpanCount(Product.getColumn(getActivity()));
        mRecommendManager.setSpanCount(Product.getColumn(getActivity()));
    }
}