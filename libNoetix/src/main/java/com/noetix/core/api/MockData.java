package com.noetix.core.api;

public class MockData {

    public static final String APP_INFO = """
            {
               "data": {
                 "apkUrl": "https",
                 "appVersion": "1.0.5",
                 "isForceUpgrade": true,
                 "packageName": "com.nexit.doubao"
               },
               "message": "查询成功",
               "timestamp": "2025-09-24 16:48:55",
               "status": "0"
             }
    """;

    public static final String DEVICE_INFO = """
           {
             "data": {
               "cppSdkConfig": "{\\"nickStructureType\\":1}",
               "createBy": "admin",
               "createTime": "2025-09-22 19:35:25",
               "defaultActionUrl": "http://t330ytgmc.hb-bkt.clouddn.com/NTX_HOBBS_v1_BJ01_250925_001/defaultActionUrl_1758698248.csv",
               "isLock": 1,
               "onlineStatus": 0,
               "onlineTimestamp": 1758702723,
               "productType": 0,
               "remark": "",
               "rknnUrl": "http://t330ytgmc.hb-bkt.clouddn.com/NTX_HOBBS_v1_BJ01_250925_001/rknnUrl_1758698248",
               "sn": "NTX_HOBBS_v1_BJ01_250925_001",
               "templateUrl": "http://t330ytgmc.hb-bkt.clouddn.com/NTX_HOBBS_v1_BJ01_250925_001/templateUrl_1758698248.zip",
               "updateBy": "System",
               "updateTime": "2025-09-24 08:48:22",
               "zeroConfigUrl": "http://t330ytgmc.hb-bkt.clouddn.com/NTX_HOBBS_v1_BJ01_250925_001/zeroConfigUrl_1758698248.yaml"
             },
             "message": "查询设备信息成功",
             "timestamp": "2025-09-24 17:07:23",
             "status": "0"
           }
    """;

    public static final String HEARTBEAT = """
           {
             "data": {
             },
             "message": "查询设备信息成功",
             "timestamp": "2025-09-24 17:07:23",
             "status": "0"
           }
    """;
}
