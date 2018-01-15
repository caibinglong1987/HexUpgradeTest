package com.hexing.upgrade.utils;

import android.serialport.DeviceControl;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cn.hexing.fdm.protocol.comm.CommOpticalSerialPort;
import cn.hexing.fdm.protocol.icomm.ICommucation;
import cn.hexing.fdm.protocol.model.CommPara;
import cn.hexing.fdm.protocol.model.HXFramePara;
import cn.hexing.fdm.services.CommServer;

/**
 * @author caibinglong
 *         date 2018/1/3.
 *         desc desc
 */

public class CommandUtil {

    private final static String TAG = CommandUtil.class.getSimpleName();
    private HXFramePara FramePara;
    private CommServer commDlmsServer;
    private static CommPara para = new CommPara();
    private boolean isOpenDevice = false;
    private ICommucation iComm = new CommOpticalSerialPort();
    private static final String OPTICAL_PATH = "/dev/ttyUSB0";//OTG 模式

    /**
     * 是否 过程中升级
     */
    private boolean CLEAR_UPGRADE = true;
    private int failCount = 0;
    /**
     * 擦除数据指令
     */
    private final String CLEAR_UPGRADE_DATA = "09030303030303767502023E39";//擦除指令 加密之后的数据
    private final String CLEAR_UPGRADE_ACTION = ":000000EF11";//擦除指令

    // 发送计数 SSS
    private int Nsend = 0;
    // 接收计数 RRR
    private int Nrec = 0;

    private static CommandUtil instance;
    private int progressNum = 0; //
    private int totalNum = 0;
    private ICallback callback;

//    public static CommandUtil getInstance() {
//        if (instance == null) {
//            synchronized (CommandUtil.class) {
//                instance = new CommandUtil();
//            }
//        }
//
//        return instance;
//    }

    static {
        int baudrate = 300;
        int nBits = 8;
        String sVerify = "N";
        char cVerify = sVerify.charAt(0);
        int nStop = 1;
        para.setComName(OPTICAL_PATH);
        // para.setBRate(baudrate);
        para.setDBit(nBits);
        para.setPty(cVerify);
        para.setSbit(nStop);
    }

    /**
     * 设置模式
     */
    public void setUpgradeMode(boolean aBool) {
        this.CLEAR_UPGRADE = aBool;
    }

    public void addListener(ICallback callback) {
        this.callback = callback;
    }

    /**
     * 初始化 参数 以便 open device
     */
    public void resetParaToOpen() {
        // DLMS 协议通讯参数
        commDlmsServer = new CommServer();
        FramePara = new HXFramePara();
        // RF（无线通讯）  Optical(本地通讯)
        FramePara.CommDeviceType = "Optical";
        FramePara.FirstFrame = true;
        FramePara.Mode = HXFramePara.AuthMode.HLS;
        FramePara.enLevel = 0x00;
        FramePara.SourceAddr = 0x03;
        FramePara.setMeterNo("");
        FramePara.WaitT = 15000;
        FramePara.ByteWaitT = 15000;
        FramePara.Pwd = "00000000";
        FramePara.aesKey = new byte[16];
        FramePara.auKey = new byte[16];
        FramePara.enKey = new byte[16];
        FramePara.StrsysTitleC = "4845430005000001";
        FramePara.encryptionMethod = HXFramePara.AuthMethod.AES_GCM_128;
        FramePara.sysTitleS = new byte[8];
        FramePara.MaxSendInfo_Value = 255;

        para.setBRate(9600);
    }

    /**
     * 开启升级模式 第一步
     */
    public void setMode() {
        openSerial();
        FramePara.OBISattri = ObisUtil.WRITE_REMOTELY_UPGRADE_MODE;
        FramePara.strDecDataType = "Bool";
        FramePara.setWriteData("0301");
        try {
            boolean aBool = commDlmsServer.Write(FramePara, iComm);
            Log.d(TAG, "设置模式=" + aBool);
            LogUtils.e(TAG, "function =setMode===设置模式=" + aBool);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "function =setMode===" + e.getMessage());
        } finally {
            //closeSerial();
        }

        try {
            Thread.sleep(400);
            readPreFrame();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(400);
            readMeter();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(400);
            transferInit();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取 每帧长度 第二步
     */
    public void readPreFrame() {
        resetParaToOpen();
        FramePara.strDecDataType = "U32";
        FramePara.OBISattri = ObisUtil.READ_PRE_FRAME;
        LogUtils.d(TAG, "readPreFrame OBis=" + FramePara.OBISattri);

        try {
            String result = commDlmsServer.Read(FramePara, iComm);
            LogUtils.d(TAG, "readPreFrame result=" + result);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "function =readPreFrame=" + e.getMessage());

        } finally {
            //closeSerial();
        }

    }

    /**
     * 传输初始化 第三步
     */
    public void transferInit() {
        resetParaToOpen();
        FramePara.setWriteData("");
        iComm = commDlmsServer.OpenDevice(para, iComm);
        FramePara.OBISattri = ObisUtil.EXECUTE_INIT;
        LogUtils.d(TAG, "transferInit OBis=" + FramePara.OBISattri);

        try {
            boolean aBool = commDlmsServer.Action(FramePara, iComm);
            Log.d(TAG, "transferInit=" + aBool);
            LogUtils.e(TAG, "function =transferInit==" + aBool);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "function =transferInit===" + e.getMessage());
        }
        closeSerial();

    }

    /**
     * 循环发送
     * 发送升级包数据 第四步
     */
    public void sendUpgradeData(String filePath) {
        resetParaToOpen();
        FramePara.OBISattri = ObisUtil.EXECUTE_SEND_DATA;
        try {
            byte[] writeByte = FileUtil.getBytesFromFile(new File(filePath));
            byte[] XADRcodeStr = writeByte;
            String writeStr = Integer.toString((XADRcodeStr[XADRcodeStr.length - 4] & 255) * 16777216 + (XADRcodeStr[XADRcodeStr.length - 3] & 255) * 65536 + (XADRcodeStr[XADRcodeStr.length - 2] & 255) * 256 + (XADRcodeStr[XADRcodeStr.length - 1] & 255));
            //for (){}
            FramePara.setWriteData(writeStr);
            boolean aBool = commDlmsServer.Write(FramePara, iComm);
            Log.d(TAG, "send Upgrade=" + aBool);
            LogUtils.e(TAG, "function =sendUpgradeData==" + aBool);

        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "function =sendUpgradeData===" + e.getMessage());
        }

    }

    /**
     * 验证是否有遗漏文件 第五
     */
    public void verifyMissingFile() {
        FramePara.OBISattri = ObisUtil.READ_UPGRADE_RESULT;
    }

    /**
     * 检测升级包是否传输完毕 第六
     */
    public void verifyTransmission() {
        FramePara.OBISattri = ObisUtil.EXECUTE_UPGRADE_PACGE_OVER;
    }

    /**
     * 验证镜像文件 第七
     */
    public void verifyImageFile() {
        FramePara.OBISattri = ObisUtil.READ_VERIFICATION_MIRROR;
    }

    /**
     * 设置激活时间,启用新系统 第8
     */
    public void setActivationTime() {
        FramePara.OBISattri = ObisUtil.EXECUTE_ACTIVATION_TIME;
    }

    /**
     * 读取表号
     */
    public boolean readMeter() {
        openSerial();
        FramePara.OBISattri = ObisUtil.READ_METER;
        FramePara.strDecDataType = "Ascs";
        LogUtils.d(TAG, "readMeter OBis=" + FramePara.OBISattri);
        try {
            String result = commDlmsServer.Read(FramePara, iComm);
            LogUtils.d(TAG, "readMeter result=" + result);
            if (result != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "function =readMeter=" + e.getMessage());
        } finally {
            closeSerial();
        }
        return false;
    }

    /**
     * 开启 表升级模式 本地升级
     */
    public boolean writeMeterUpgradeMode() {
        // Data: 7E A0 1B 03 03 54 AA F7 E6 E6 00 C1 01 C1 00 01 01 00 1F 80 6A FF 02 00 11 00 61 97 7E
        openSerial();
        FramePara.OBISattri = ObisUtil.WRITE_LOCAL_METER_MODE;
        FramePara.setWriteData("00");
        FramePara.strDecDataType = "U8";
        LogUtils.d(TAG, "writeMeterUpgradeMode OBis=" + FramePara.OBISattri);
        try {
            boolean result = commDlmsServer.Write(FramePara, iComm);
            LogUtils.d(TAG, "writeMeterUpgradeMode result=" + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "function =writeMeterUpgradeMode=" + e.getMessage());
        } finally {
            closeSerial();
        }
        return false;
    }

    /**
     * 本地升级 方案  发送数据
     *
     * @param data data
     */
    public void sendUpgradeDataByLocal(final List<String> data) {
        data.add(0, ":020000040000FA");
        data.add(0, ":020000040000FA");
        totalNum = data.size();
        progressNum = 0;
        failCount = 0;
        openSerial();
        if (iComm == null) {
            return;
        }
        HexThreadManager.getInstance().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (CLEAR_UPGRADE) {
                    boolean aResult = iComm.SendByt(CRCUtil.hexStringToByte(CLEAR_UPGRADE_DATA));
                    if (aResult) {
                        try {
                            Thread.sleep(6000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        byte[] bytes = iComm.ReceiveByt(15000, 5000);
                        String result = upgrade(CRCUtil.bytesToHexString(bytes));

                        LogUtils.d(TAG, "发送最终数据并接收数据=" + Arrays.toString(bytes) + "||result = " + result);
                        // if (result.contains("OK")) {
                        CLEAR_UPGRADE = false;
                        // }
                    }
                }

                while (data.size() > 0) {
                    if (failCount >= 4) {
                        if (callback != null) {
                            callback.finish();
                        }
                        return;
                    }
                    //所有数据区加和0xF6,校验和重新计算 (超过0xFF，减去0x100)
                    //DD....为数据区
                    //:LLAAAATT[DD...............]CC[0D][0A]
                    String sendData = upgradeEncrypt(data.get(0));
                    //sendData = ":020000040001F9";
                    boolean aResult = iComm.SendByt(CRCUtil.hexStringToByte(sendData));
                    if (aResult) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        byte[] bytes = iComm.ReceiveByt(300, 100);
                        if (bytes != null && bytes.length > 0) {
                            String result = upgrade(CRCUtil.bytesToHexString(bytes));
                            result = "OK";
                            if (result.contains("OK")) {
                                failCount = 0;
                                data.remove(0);
                                Log.d(TAG, "发送最终数据并接收数据=" + Arrays.toString(bytes) + "||剩余data size=" + data.size());
                                if (callback != null) {
                                    callback.sendData(progressNum++ * 1.0f / totalNum);
                                }
                            } else {
                                failCount++;
                            }
                        } else {
                            failCount++;
                        }
                    } else {
                        failCount++;
                    }
                }
                if (data.size() > 0) {
                    LogUtils.d(TAG, "发送数据异常失败||剩余data size=" + data.size());
                    closeSerial();
                    if (callback != null) {
                        callback.finish();
                    }
                } else {
                    LogUtils.d(TAG, "===升级文件发送结束====");
                }
            }
        });
    }

    /**
     * 打开串口 上电
     */

    public void openSerial() {
        if (isOpenDevice) {
            return;
        }
        LogUtils.d(TAG, "打开串口");
        try {
            //HT380A 设备上电
            //mPower232.Rs232_PowerOn();
            //KT50 耳机口上电
            DeviceControl deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN, 70);
            deviceControl.PowerOnDevice();
            resetParaToOpen();
            iComm = commDlmsServer.OpenDevice(para, iComm);
            if (iComm == null) {
                isOpenDevice = false;
                LogUtils.d(TAG, "打开串口失败");
                if (callback != null) {
                    callback.finish();
                }
                return;
            }
            LogUtils.d(TAG, "打开串口成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtils.d(TAG, "上电失败=" + ex.getMessage());
            if (iComm == null && callback != null) {
                callback.finish();
            }
        } finally {
        }

    }

    /**
     * 关闭串口 下电
     */
    public void closeSerial() {
        LogUtils.d(TAG, "关闭串口");
        if (iComm != null) {
            try {
                commDlmsServer.DiscFrame(iComm);
                try {
                    commDlmsServer.Close(iComm);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //波特率设置为：9600，保证条码扫描能正常
        // para.setBRate(9600);
        //commDlmsServer.OpenDevice(para, iComm);

        //给HT380A设备下电
        //mPower232.Rs232_PowerOff();

        //给KT50设备下电 耳机口
        try {
            DeviceControl deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN, 70);
            deviceControl.PowerOffDevice();

            isOpenDevice = false;
            LogUtils.d(TAG, "下电操作");

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "下电失败=" + e.getMessage());

        }
    }

    /**
     * 固件升级 加密 算法
     *
     * @param sourceData 原字符串
     */
    private String upgradeEncrypt(String sourceData) {
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
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 解密 硬件返回字符串
     *
     * @param encryptData 加密字符串
     * @return 返回解密字符串 ascii
     */
    private String upgrade(String encryptData) {
        //String data = "09 03 03 03 03 03 03 76 75 02 02 3E 39";
        String data = encryptData.replace(" ", "");
        data = CRCUtil.ExclusiveOrOperation(data);
        data = StringUtil.convertHexToString(data.toUpperCase());
        data = data.replace(" ", "").toUpperCase();
        Log.e("硬件返回数据解析", "||data=" + data.replace(" ", ""));
        return data;
    }

    public interface ICallback {
        void sendData(float progress);

        void finish();
    }
}
