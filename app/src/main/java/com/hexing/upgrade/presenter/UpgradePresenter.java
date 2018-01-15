package com.hexing.upgrade.presenter;

import com.hexing.upgrade.model.UpgradeBusiness;

import java.io.File;

/**
 * @author by HEC271
 *         on 2018/1/11.
 */

public class UpgradePresenter implements IUpgradeContract.Presenter {
    private IUpgradeContract.View upgradeView;
    private UpgradeBusiness business;

    public UpgradePresenter(IUpgradeContract.View upgradeView) {
        this.upgradeView = upgradeView;
        this.upgradeView.setPresenter(this);
        this.business = new UpgradeBusiness(this);
    }

    /**
     * 显示错误信息
     *
     * @param resourceId 资源id
     */
    public void showMessage(int resourceId) {
        this.upgradeView.showError(resourceId);
    }

    /**
     * 隐藏进度条
     *
     * @param
     */
    public void hideProgress() {
        this.upgradeView.hideProgress();
    }

    /**
     * 更新进度条
     *
     * @param
     */
    public void showProgress(float progress) {
        this.upgradeView.showProgress(progress);
    }


    @Override
    public void verifyFile(File file) {
        business.verifyFile(file);
    }
}
