package com.noetix.libnoetix;

import android.os.Build;

import androidx.annotation.RequiresApi;
import com.noetix.utils.IPAddressUtils;
import com.noetix.utils.KLog;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;


public class LiveFaceService {

    private static final String TAG = "LiveFaceService";
    public static  String IP_PORT = "";

    // UDP网络相关
    private DatagramSocket socket;
    private volatile boolean isRunning; // 服务器运行状态
    private Thread receiveThread;       // 数据接收线程
    private int port = 29999;           // 默认端口

    // 网络参数
    private static final int BUFFER_SIZE = 4096;        // UDP缓冲区大小
    private static final int MAX_PORT_TRIES = 10;       // 最大端口尝试次数
    private static final int UDP_TIME_OUT = 2000;       // UDP接收超时(ms)

    // 原生API接口
    private final INativeApi iNativeApi = INativeApi.get();

    // 平滑控制参数
    private static final float FILTER_FACTOR = 0.12f;          // 低通滤波系数(0-1)，值越小越平滑
    private static final long FACE_LOST_THRESHOLD_MS = 50;   // 人脸丢失判定阈值(ms)
    private static final float TRANSITION_SPEED = 0.05f;      // 归位过渡速度(0-1)

    // 状态跟踪
    private final AtomicLong lastFaceUpdateTime = new AtomicLong(System.currentTimeMillis());
    private volatile boolean isFaceLost = false; // 人脸丢失状态标志

    // 数据容器（使用原子引用保证线程安全）
    private final AtomicReference<Map<String, Float>> filteredBlendShapes =
            new AtomicReference<>(new ConcurrentHashMap<>()); // 滤波后的BlendShape值

    private final AtomicReference<Map<String, Float>> targetBlendShapes =
            new AtomicReference<>(new ConcurrentHashMap<>()); // 目标BlendShape值

    private final AtomicReference<Map<String, Float>> neutralBlendShapes =
            new AtomicReference<>(new ConcurrentHashMap<>()); // 中性位置值

    // 同步锁（保护目标值更新和状态转换）
    private final ReentrantLock dataLock = new ReentrantLock();
    private final ScheduledExecutorService mScheduler = Executors.newSingleThreadScheduledExecutor();


    // 新增状态常量
    private static final int STATE_LOST = 0;
    private static final int STATE_TRACKING = 1;
    private static final int STATE_TRANSITIONING = 2;  // 新增过渡状态

    // 状态变量
    private volatile int faceState = STATE_TRACKING;

    // 新增过渡参数
    private static final long TRANSITION_DURATION_MS = 2000; // 过渡持续时间
    private final AtomicLong transitionStartTime = new AtomicLong(0);
    private final AtomicReference<Map<String, Float>> transitionTargetBlendShapes =
            new AtomicReference<>(new ConcurrentHashMap<>());


    // 单例模式（确保全局唯一实例）
    private LiveFaceService() {
        initializeNeutralShapes(); // 初始化中性位置
    }

    private static class Holder {
        private static final LiveFaceService instance = new LiveFaceService();
    }

    public static LiveFaceService instance() {
        return Holder.instance;
    }

    /**
     * 初始化中性位置（电机归位状态）
     */
    private void initializeNeutralShapes() {
        Map<String, Float> neutral = new ConcurrentHashMap<>();
        // 根据实际模型设置中性位置值
        neutral.put("HeadPitch", 0f);   // 颈部旋转
        neutral.put("HeadRoll", 0f);   // 颈部倾斜
        neutral.put("HeadYaw", 0f);    // 颈部平移
        // 可添加其他维度...
        neutralBlendShapes.set(neutral);
    }

    /**
     * 启动UDP服务器和控制线程
     */
    public void start() {
        KLog.d(TAG, "启动UDP服务器...");
        if (isRunning) {
            KLog.d(TAG, "服务器已在运行中");
            return;
        }


        isFaceLost = false;
        try {
            // 尝试绑定端口（支持端口冲突时自动切换）
            socket = tryBindSocket(port);
            if (socket == null) {
                KLog.e(TAG, "无法绑定端口，请检查网络配置");
                return;
            }

            isRunning = true;

            // 获取并记录服务器地址信息
            InetAddress address = IPAddressUtils.getLocalIPv4Address();
            String serverInfo = Objects.requireNonNull(address).getHostAddress() + ":" + socket.getLocalPort();
            KLog.d(TAG, "UDP服务器启动成功: " + serverInfo);
            LiveFaceService.IP_PORT = serverInfo; // 存储到全局上下文

            // 启动数据接收线程
            receiveThread = new Thread(this::producerTask, "producerTask");
            receiveThread.start();
            mScheduler.scheduleWithFixedDelay(consumerTask,100,16, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            KLog.e(TAG, "启动失败: " + e.getMessage());
        }
    }

    /**
     * 尝试绑定端口（支持自动端口切换）
     */
    private DatagramSocket tryBindSocket(int startPort) {
        for (int i = 0; i < MAX_PORT_TRIES; i++) {
            int tryPort = startPort + i;
            try {
                DatagramSocket tempSocket = new DatagramSocket(tryPort);
                tempSocket.setSoTimeout(UDP_TIME_OUT);
                KLog.d(TAG, "成功绑定端口: " + tryPort);
                port = tryPort; // 更新实际使用端口
                return tempSocket;
            } catch (SocketException e) {
                KLog.w(TAG, "端口 " + tryPort + " 被占用，尝试下一个...");
            }
        }
        KLog.e(TAG, "无法绑定任何端口，请检查端口范围 " + startPort + "-" + (startPort + MAX_PORT_TRIES - 1));
        return null;
    }

    /**
     * 生产者 UDP数据接收任务（运行在独立线程）
     */
    private void producerTask() {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        KLog.d(TAG, "UDP接收线程启动");

        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                // 阻塞接收数据（最多等待UDP_TIME_OUT毫秒）
                socket.receive(packet);

                // 提取有效数据
                byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());

                // 处理接收到的数据
                processUdpData(data);

            } catch (SocketTimeoutException e) {
                // 超时属于正常情况，继续轮询
            } catch (Exception e) {
                // 仅在运行状态下记录错误（避免关闭时的异常干扰）
                if (isRunning) {
                    KLog.e(TAG, "UDP接收错误: " + e.getMessage());
                }
            }
        }
        KLog.d(TAG, "UDP接收线程退出");
    }



    /**
     * 消费 task
     */
    private final Runnable consumerTask = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            if (!isRunning) return; // 确保服务器在运行状态

            // 1. 检查人脸状态 -------------------------------------------------
            long currentTime = System.currentTimeMillis();
            long lastUpdate = lastFaceUpdateTime.get();
            boolean faceCurrentlyLost = (currentTime - lastUpdate) > FACE_LOST_THRESHOLD_MS;

            // 2. 状态转换处理 -------------------------------------------------
            handleStateTransition(faceCurrentlyLost);


            // 3. 获取当前状态快照（加锁保证一致性）------------------------------
            Map<String, Float> currentFiltered = new ConcurrentHashMap<>(filteredBlendShapes.get());
            Map<String, Float> currentTarget = getTargetSnapshot();

            // 4. 插值计算 -----------------------------------------------------
            Map<String, Float> newFiltered = calculateInterpolation(currentFiltered, currentTarget, faceCurrentlyLost);

            // 5. 更新滤波后数据（原子操作）--------------------------------------
            filteredBlendShapes.set(newFiltered);

            // 6. 发送电机指令 -------------------------------------------------
            iNativeApi.setNecksWithBS(newFiltered);
        }
    };


    private void handleStateTransition(boolean faceCurrentlyLost) {
        final long currentTime = System.currentTimeMillis();

        if (faceCurrentlyLost) {
            // 人脸丢失处理
            if (faceState != STATE_LOST) {
                KLog.d(TAG, "人脸丢失，开始过渡到中性位置");
                faceState = STATE_LOST;
                dataLock.lock();
                try {
                    targetBlendShapes.set(new ConcurrentHashMap<>(neutralBlendShapes.get()));
                } finally {
                    dataLock.unlock();
                }
            }
        } else {
            // 人脸重新出现处理
            if (faceState == STATE_LOST) {
                KLog.d(TAG, "人脸重新出现，开始平滑过渡");
                faceState = STATE_TRANSITIONING;
                transitionStartTime.set(currentTime);

                // 保存当前滤波值作为过渡起点
                Map<String, Float> currentFiltered = new ConcurrentHashMap<>(filteredBlendShapes.get());

                dataLock.lock();
                try {
                    // 设置过渡目标值为最新检测值
                    transitionTargetBlendShapes.set(new ConcurrentHashMap<>(targetBlendShapes.get()));
                    // 保持当前目标值为中性位置（继续向中性移动）
                } finally {
                    dataLock.unlock();
                }
            }
            // 状态自动转换：过渡完成 -> 跟踪状态
            else if (faceState == STATE_TRANSITIONING &&
                    (currentTime - transitionStartTime.get()) > TRANSITION_DURATION_MS) {
                faceState = STATE_TRACKING;
                KLog.d(TAG, "过渡完成，进入正常跟踪状态");
            }
        }
    }


    /**
     * 获取目标值的线程安全快照
     */
    private Map<String, Float> getTargetSnapshot() {
        dataLock.lock();
        try {
            return new ConcurrentHashMap<>(targetBlendShapes.get());
        } finally {
            dataLock.unlock();
        }
    }

    /**
     * 计算插值结果
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private Map<String, Float> calculateInterpolation(Map<String, Float> currentFiltered,
                                                      Map<String, Float> currentTarget,
                                                      boolean faceCurrentlyLost) {

        Map<String, Float> newFiltered = new ConcurrentHashMap<>();
        float effectiveFilterFactor = FILTER_FACTOR;

        // 过渡状态特殊处理
        if (faceState == STATE_TRANSITIONING) {
            // 使用更强的平滑系数（更小的值）
            effectiveFilterFactor = FILTER_FACTOR * 0.3f;

            // 获取过渡目标值
            Map<String, Float> transitionTarget = transitionTargetBlendShapes.get();
            final long elapsed = System.currentTimeMillis() - transitionStartTime.get();
            final float progress = Math.min(elapsed / (float) TRANSITION_DURATION_MS, 1.0f);

            // 混合目标值：当前目标(中性) -> 过渡目标(检测值)
            for (String key : transitionTarget.keySet()) {
                float targetValue = currentTarget.getOrDefault(key, 0f);
                float transitionValue = transitionTarget.getOrDefault(key, 0f);
                float blendedTarget = targetValue + (transitionValue - targetValue) * progress;

                float currentValue = currentFiltered.getOrDefault(key, 0f);
                newFiltered.put(key, lowPassFilter(currentValue, blendedTarget, effectiveFilterFactor));
            }
        }
        else {
            // 正常处理逻辑
            for (String key : currentTarget.keySet()) {
                float targetValue = currentTarget.getOrDefault(key, 0f);
                float currentValue = currentFiltered.getOrDefault(key, 0f);

                if (faceState == STATE_LOST) {
                    newFiltered.put(key, lerp(currentValue, targetValue, TRANSITION_SPEED));
                } else {
                    newFiltered.put(key, lowPassFilter(currentValue, targetValue, effectiveFilterFactor));
                }
            }
        }

        // 保留现有维度
        for (String key : currentFiltered.keySet()) {
            if (!currentTarget.containsKey(key)) {
                newFiltered.put(key, currentFiltered.get(key));
            }
        }

        return newFiltered;
    }

    /**
     * 停止服务器并释放资源
     */
    public void stop() {
        if (!isRunning) return;

        KLog.d(TAG, "正在停止UDP服务...");
        isRunning = false;

        // 关闭网络资源
        if (socket != null && !socket.isClosed()) {
            socket.close();
            KLog.d(TAG, "端口 " + port + " 已释放");
        }

        // 中断接收线程
        if (receiveThread != null && receiveThread.isAlive()) {
            receiveThread.interrupt();
        }

        // 停止所有任务线程
        isFaceLost = false;
//        mScheduler.shutdownNow();
        KLog.d(TAG, "UDP服务已停止");
    }


    /**
     * 处理接收到的UDP数据
     */
    private void processUdpData(byte[] rawData) {
        // 更新最后接收到人脸数据的时间戳
        lastFaceUpdateTime.set(System.currentTimeMillis());
        Map<String, Float> newBlendShapes = iNativeApi.decode2Blendshape(rawData);
        processFaceAngles(newBlendShapes);

        dataLock.lock();
        try {
            if (faceState == STATE_TRACKING) {
                // 直接更新目标值
                targetBlendShapes.set(new ConcurrentHashMap<>(newBlendShapes));
            }
            else if (faceState == STATE_TRANSITIONING) {
                // 更新过渡目标值（平滑过渡到最新值）
                transitionTargetBlendShapes.set(new ConcurrentHashMap<>(newBlendShapes));
            }
        } finally {
            dataLock.unlock();
        }
    }

    private long index;
    private void processFaceAngles( Map<String, Float> blendShapes ){
        if (index % 6 == 0) {
            int[] motorAngles = iNativeApi.mappingMotor(blendShapes);
            float[] motorCommands = new float[motorAngles.length];
            for (int i = 0; i < motorAngles.length; i++) {
                motorCommands[i] = (float) motorAngles[i];
            }
            //设置人脸舵机指令
            iNativeApi.setFaceAngles(motorCommands);
            KLog.d(TAG, "中间Frame 直接转动 ....");
//            KLog.d(TAG, "执行电机命令: " + Arrays.toString(motorCommands));
        }
        index++;
    }

    /**
     * 线性插值计算
     * @param start 起始值
     * @param end 目标值
     * @param progress 进度（0-1）
     * @return 插值结果
     */
    private float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    /**
     * 低通滤波器
     * @param current 当前值
     * @param target 目标值
     * @param factor 滤波系数（0-1）
     * @return 滤波后的值
     */
    private float lowPassFilter(float current, float target, float factor) {
        return current + (target - current) * factor;
    }
}
