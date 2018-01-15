package com.hexing.upgrade.utils;

/**
 * @author caibinglong
 *         date 2018/1/3.
 *         desc desc
 */

public class ObisUtil {
    //升级模式 远程
    public final static String WRITE_REMOTELY_UPGRADE_MODE = "001200002C0000FF0500";

    //每帧数据长度
    public final static String READ_PRE_FRAME = "001200002C0000FF0200";

    //初始化
    public final static String EXECUTE_INIT = "001200002C0000FF01";

    //传输升级包文件
    public final static String EXECUTE_SEND_DATA = "001200002C0000FF02";

    //检查是否有遗漏的文件 处理补发
    public final static String READ_UPGRADE_RESULT = "001200002C0000FF0300";

    //检测升级包是否传输完毕，
    public final static String EXECUTE_UPGRADE_PACGE_OVER = "001200002C0000FF03";

    //镜像信息 验证是否正确
    public final static String READ_VERIFICATION_MIRROR = "001200002C0000FF0700";

    //设置激活时间
    public final static String EXECUTE_ACTIVATION_TIME = "001200002C0000FF04";

    //读取表号
    public final static String READ_METER = "00010000600100FF0200";

    //激活表计进入直接通讯式BootLoader状态 本地升级
    public final static String WRITE_LOCAL_METER_MODE = "000101001F806AFF0200";

}
