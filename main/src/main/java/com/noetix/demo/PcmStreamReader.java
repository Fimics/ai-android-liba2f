package com.noetix.demo;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;

public class PcmStreamReader {
    
    /**
     * 流式读取 assets 目录下的 PCM 文件
     *
     * @param context  Android 上下文
     * @param fileName assets 中的 PCM 文件名 (e.g., "audio.pcm")
     * @param callback 包含 onDataRead(byte[]) 和 onComplete() 的回调接口
     */
    public static void readPcmStream(Context context, String fileName, PcmStreamCallback callback) {
        InputStream inputStream = null;
        try {
            // 1. 通过 AssetManager 打开文件流
            inputStream = context.getAssets().open(fileName);
            int totalSize = inputStream.available();
            // 2. 设置读取缓冲区大小 (可根据需求调整)
            byte[] buffer = new byte[16000]; // 4KB 缓冲区
            int bytesRead;
            
            // 3. 循环读取直到文件结束
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // 每次读取到数据时回调
                Thread.sleep(100);
//                AudioAmplify.amplifyAll(buffer,1.5f);
                callback.onDataRead(buffer, bytesRead,totalSize);
                
            }
            
            // 4. 读取完成后回调
            callback.onComplete();
            
        } catch (IOException e) {
            callback.onError(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 5. 确保关闭文件流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    callback.onError(e);
                }
            }
        }
    }


    /**
     * 回调接口定义
     */
    public interface PcmStreamCallback {
        /**
         * 每次读取到数据块时调用
         * @param buffer 包含 PCM 数据的字节数组
         * @param size   实际读取的有效数据长度 (可能小于 buffer 长度)
         */
        void onDataRead(byte[] buffer, int size,int totalSize);

        /**
         * 全部数据读取完成时调用
         */
        void onComplete();

        /**
         * 发生错误时调用
         * @param e 异常信息
         */
        void onError(Exception e);
    }
}
