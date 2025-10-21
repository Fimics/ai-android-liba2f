package com.noetix.utils;

import java.io.File;
import java.util.Objects;

public class ConfigChecker {
    private static final String TAG ="ConfigChecker";
    public static boolean checkAllPathsExistAndNotEmpty(String[] paths) {
        for (String path : paths) {
            File target = new File(path);

            if (!target.exists()) {
                KLog.d(TAG,path + " 不存在");
                return false;
            }

            if (target.isDirectory()) {
                if (Objects.requireNonNull(target.list()).length == 0) {
                    KLog.d(TAG,path + " 存在但为空文件夹");
                    return false;
                }
            } else {
                if (target.length() == 0) {
                    KLog.d(TAG,path + " 存在但为空文件");
                    return false;
                }
            }
        }
        return true;
    }
}
