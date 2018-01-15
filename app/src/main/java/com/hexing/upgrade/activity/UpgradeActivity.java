package com.hexing.upgrade.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hexing.upgrade.R;
import com.hexing.upgrade.presenter.IUpgradeContract;
import com.hexing.upgrade.presenter.UpgradePresenter;
import com.hexing.upgrade.utils.Constant;
import com.hexing.upgrade.utils.FileUtil;
import com.hexing.upgrade.utils.LFilePicker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author by HEC271
 *         on 2018/1/11.
 */

public class UpgradeActivity extends AppCompatActivity implements IUpgradeContract.View, View.OnClickListener {
    private IUpgradeContract.Presenter presenter;
    private int mIconType = Constant.ICON_STYLE_YELLOW;
    private int mBackArrawType = Constant.BACKICON_STYLEONE;
    private Toolbar mToolbar;
    private ImageView mClipLeftImageView;
    private TextView proBar;
    private EditText tv;
    private Button btn_begin;
    private LinearLayout ll_unclick;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        new UpgradePresenter(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        proBar = (TextView) findViewById(R.id.processbar);
        ll_unclick = (LinearLayout) findViewById(R.id.ll_unclick);
        tv = (EditText) findViewById(R.id.path);
        ll_unclick.getBackground().setAlpha(0);
        btn_begin = (Button) findViewById(R.id.btn_begin);
        btn_begin.setOnClickListener(this);
        initToolbar();
//        mClipLeftImageView = (ImageView) findViewById(R.id.iv_image_clip_left);
//        mClipLeftImageView.setImageLevel(10000);
//        ll_unclick.setVisibility(View.VISIBLE);
    }

    /**
     * 更新Toolbar展示
     */
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle("back");

        //mToolbar.setTitleTextColor(255); //设置标题颜色
        //mToolbar.setBackgroundColor(0);
        mToolbar.setNavigationIcon(R.mipmap.backincostyleone);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void setPresenter(IUpgradeContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showProgress(final float progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progress == 1.0f) {
                    Toast.makeText(getApplicationContext(), "升级成功", Toast.LENGTH_SHORT).show();
                    ll_unclick.setVisibility(View.GONE);
                } else {
                    String nowProgress = progress * 100 + "%";
                    proBar.setText(nowProgress);
                }
            }
        });
    }

    @Override
    public void hideProgress() {
        ll_unclick.setVisibility(View.GONE);
        proBar.setText("0%");

    }

    @Override
    public void showError(int resourceId) {
        Toast.makeText(getApplicationContext(), resourceId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_begin:
                //点升级按钮事件
                ll_unclick.setVisibility(View.VISIBLE);
                File file = new File(path);
                presenter.verifyFile(file);
                break;
            default:
                break;
        }
    }

    public void openFromActivity(View view) {
        new LFilePicker()
                .withActivity(this)
                .withRequestCode(Consant.REQUESTCODE_FROM_ACTIVITY)
                .withTitle("文件选择")
                .withIconStyle(mIconType)
                .withBackIcon(mBackArrawType)
                .withMutilyMode(false)
                .withMaxNum(2)
                .withNotFoundBooks("至少选择一个文件")
                .withChooseMode(true)//文件夹选择模式
                //.withFileFilter(new String[]{"txt", "png", "docx"})
                .start();
    }

    private String path = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Consant.REQUESTCODE_FROM_ACTIVITY) {
                List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
                path = list.get(0);
                Toast.makeText(getApplicationContext(), "选中的路径为" + path, Toast.LENGTH_SHORT).show();
                Log.i("LeonFilePicker", path);
                tv.setText(path);
                if (path != "")
                    btn_begin.setEnabled(true);
                else
                    btn_begin.setEnabled(false);
                //
            }
        }
    }

}
