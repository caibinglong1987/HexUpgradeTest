package com.hexing.upgrade.presenter;

import com.hexing.upgrade.view.IBaseView;

import java.io.File;

/**
 * @author by HEC271
 *         on 2018/1/11.
 */

public interface IUpgradeContract {
    interface Presenter {
        /**
         * 文件校验
         *
         * @param file 文件
         */
        void verifyFile(File file);
    }


    interface View extends IBaseView<Presenter> {
        void showProgress(float progress);

        void hideProgress();

        void showError(int resourceId);
    }
}
