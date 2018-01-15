package com.hexing.upgrade.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hexing.upgrade.R;
import com.hexing.upgrade.presenter.IUpgradeContract;
import com.hexing.upgrade.presenter.UpgradePresenter;
import com.hexing.upgrade.utils.CRCUtil;
import com.hexing.upgrade.utils.CommandUtil;
import com.hexing.upgrade.utils.FileUtil;
import com.hexing.upgrade.utils.LogUtils;
import com.hexing.upgrade.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements IUpgradeContract.View, View.OnClickListener {
    private IUpgradeContract.Presenter presenter;
    private TextView tvSetMode;
    private TextView tvData;
    private TextView tvGetMeter;
    private TextView tvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSetMode = (TextView) findViewById(R.id.tvSetMode);
        tvData = (TextView) findViewById(R.id.tvData);
        tvGetMeter = (TextView) findViewById(R.id.tvGetMeter);
        tvTest = (TextView) findViewById(R.id.tvTest);
        tvGetMeter.setOnClickListener(this);
        tvSetMode.setOnClickListener(this);
        tvTest.setOnClickListener(this);
        new UpgradePresenter(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvSetMode:
                Intent intent = new Intent();
                intent.setClass(this, UpgradeActivity.class);
                startActivity(intent);

                // CommandUtil.getInstance().setMode();
                break;
            case R.id.tvGetMeter:
                //CommandUtil.getInstance().readMeter();
                File file = null;
                try {
                    file = File.createTempFile("temp", ".hex");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    InputStream stream = getAssets().open("HXE.hex");
                    file = FileUtil.inputStreamToFile(stream, file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                String crc = CRCUtil.getCRC16("04F6C5008E0F8F10");
//                LogUtils.d("测试1", crc);
//                crc = CRCUtil.getCRC16("8E0F8F10");
//                LogUtils.d("测试2", crc);
//                //crc =CRCUtil.checkSum("04F6C5008E0F8F10".toCharArray(),"04F6C5008E0F8F10".length());
//                LogUtils.d("测试3", crc);
//                crc =  CRCUtil.checkSum("04F6C50084058506");
//                LogUtils.d("测试4", crc);
                this.presenter.verifyFile(file);
                //CRCUtil.checkSum("");
                break;
            case R.id.tvTest:
                //test();
                parseData();
                break;
            default:
                break;
        }
    }

    private void parseData() {
        String data = "09 03 07 03 03 03 03 03 06 03 03 03 03 01 03 70 02 02 05 3E 39";
        data = data.replace(" ", "");
        data = CRCUtil.ExclusiveOrOperation(data);
        data = StringUtil.convertHexToString(data);
        Log.e("数据转换", data);

        test();
    }

    private void test() {
        //截取冒号之后的数据
        String TAG = MainActivity.class.getSimpleName();
        String sourceData = ":00000001FF";
        String last = "0D0A";
        //去除第一个字符冒号
        String tempData = sourceData.substring(1, sourceData.length());
        tempData = tempData.substring(0, tempData.length() - 2);
        //LL 部分
        String byteLen = tempData.substring(0, 2);
        // TT部分：00－代表数据记录、01－代表结束记录、02－代表扩展段地址记录、04－代表扩展线性地址记录（TDK•654xG使用此种地址扩展）
        String tt = tempData.substring(6, 8);
        //最后一个字节为校验和
        String cc = sourceData.substring(sourceData.length() - 2, sourceData.length());
        //数据 第4位开始 取2个字节 是目标地址 AAAA部分 //第4位开始 取1个字节 是目标地址 TT部分
        String address = tempData.substring(2, 6);
        String sendData;
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder sendBuilder;
        if (!"00".equals(byteLen) && "00".equals(tt)) {
            sendBuilder = new StringBuilder();
            //数据字节大于0  内容区域DD模块 每个字节需要加上0x20
            String context = tempData.substring(8, tempData.length());
            sendBuilder.append(byteLen);
            sendBuilder.append(address);
            sendBuilder.append(tt);
            for (int i = 0; i < context.length() / 2; i++) {
                int num = Integer.valueOf(context.substring(i * 2, i * 2 + 2), 16);
                num = num + 0x20;
                String hexNum = Integer.toHexString(num);
                if (hexNum.length() == 1) {
                    hexNum = "0" + hexNum;
                }
                sendBuilder.append(hexNum);
            }
            sendBuilder.append(cc);
            Log.e(TAG, "待发送数据=" + sendBuilder.toString());
        } else {
            sendBuilder = new StringBuilder();
            sendBuilder.append(tempData);
            sendBuilder.append(cc);
        }

        //取冒号 转Ascii
        sendData = sendBuilder.toString().toUpperCase();
        Log.d(TAG, "待发送数据=" + sendData);
        stringBuilder.append(StringUtil.parseAscii(sourceData.substring(0, 1)));
        for (int i = 0; i < sendData.length(); i++) {
            stringBuilder.append(StringUtil.parseAscii(sendData.substring(i, i + 1)));
        }
        stringBuilder.append(last);
        sendData = stringBuilder.toString();
        Log.d(TAG, "待发送Ascii数据=" + sendData);

        stringBuilder = new StringBuilder();
        for (int i = 0, k = sendData.length() / 2; i < k; i++) {
            //进行异或运算 0x33
            int num = Integer.parseInt(sendData.substring(i * 2, i * 2 + 2), 16);
            num = num ^ 0x33;
            stringBuilder.append(StringUtil.getHex(num, true));
        }
        LogUtils.d(TAG, "待发送最终数据=" + stringBuilder.toString().toUpperCase());
    }

    @Override
    public void setPresenter(IUpgradeContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showProgress(float progress) {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showError(int resourceId) {

    }
}
