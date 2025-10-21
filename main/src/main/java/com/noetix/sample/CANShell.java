package com.noetix.sample;


public class CANShell {
    private static final String TAG = "nx_app_can";
    public static void executeCanCommands() {
        // 提升超级用户权限
//        RootShell.execCommand2("su");
//        RootShell.execCommand2("chmod 0777 /data/local/tmp/candump");
//       RootShell.execCommand2("chmod 0777 /data/local/tmp/cansend");

        // 查询当前网络设备
        RootShell.execCommand2("ifconfig -a");
        // 关闭CAN
         RootShell.execCommand2("ip link set can0 down");
        // 设置比特率为1000000 (1 Mbps)
        RootShell.execCommand2("ip link set can0 type can bitrate 1000000");
        // 启动CAN设备
         RootShell.execCommand2("ip link set can0 up");
        // 打印CAN接口信息
         RootShell.execCommand2("ip -details link show can0");

        // 发送测试数据
//        result = RootShell.execCommand2("/data/local/tmp/cansend can0 001#FFFFFFFFFFFFFFFD");
//        KLog.d(TAG,"Send test data to can0: " + result);
    }
}
