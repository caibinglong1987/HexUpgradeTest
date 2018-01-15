package com.hexing.upgrade.model;

import android.os.Handler;
import android.os.Looper;

import com.hexing.upgrade.R;
import com.hexing.upgrade.presenter.UpgradePresenter;
import com.hexing.upgrade.utils.CommandUtil;
import com.hexing.upgrade.utils.FileUtil;
import com.hexing.upgrade.utils.HexThreadManager;
import com.hexing.upgrade.utils.LogUtils;

import java.io.File;
import java.util.List;

/**
 * @author by HEC271
 *         on 2018/1/11.
 */

public class UpgradeBusiness {
    private final String TAG = UpgradeBusiness.class.getSimpleName();
    private UpgradePresenter presenter;
    private CommandUtil commandUtil;

    public UpgradeBusiness(UpgradePresenter presenter) {
        this.presenter = presenter;
        this.commandUtil = new CommandUtil();
    }

    /**
     * 验证文件 正确性
     *
     * @param file 文件
     */
    public void verifyFile(File file) {
        if (file == null) {
            return;
        }

        //return;
        sendData(file);
    }

    private void sendData(File file) {
        final List<String> dataList = FileUtil.readFileOnLine(file);
        dataList.subList(0, 258).clear();
        if (dataList.size() == 0) {
            this.presenter.hideProgress();
            this.presenter.showMessage(R.string.file_parse_error);
            return;
        }

        this.presenter.showProgress(0);
        /**
         * 验证表是否可用
         * 目前只读取表号 做验证
         */
        commandUtil.addListener(new CommandUtil.ICallback() {
            @Override
            public void sendData(final float progress) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        presenter.showProgress(progress);
                    }
                });
            }

            @Override
            public void finish() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        presenter.hideProgress();
                        presenter.showMessage(R.string.upgrade_error);
                    }
                });
            }
        });

        HexThreadManager.getInstance().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                boolean aCanReadMeter = commandUtil.readMeter();
                presenter.showProgress(1 / dataList.size());
                boolean aCanSetMeterUpgradeMode = commandUtil.writeMeterUpgradeMode();
                presenter.showProgress(2 / dataList.size());
                if (!aCanReadMeter && !aCanSetMeterUpgradeMode) {
                    LogUtils.e(TAG, "升级开始，读取表号和初始化升级失败,开始尝试过程升级");
                    commandUtil.setUpgradeMode(true);
                    commandUtil.sendUpgradeDataByLocal(dataList);
                }
                if (aCanReadMeter && aCanSetMeterUpgradeMode) {
                    LogUtils.e(TAG, "升级开始，读取表号和初始化升级成功,开始正常升级");
                    commandUtil.setUpgradeMode(false);
                    commandUtil.sendUpgradeDataByLocal(dataList);
                }
            }
        });
    }

}
