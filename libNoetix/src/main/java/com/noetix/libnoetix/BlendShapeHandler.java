package com.noetix.libnoetix;


import android.annotation.SuppressLint;
import android.util.SparseArray;

import com.noetix.libnoetix.csv.CVSReaderManager;
import com.noetix.utils.KLog;
import com.noetix.utils.P;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class BlendShapeHandler {

    private static final String TAG = "BlendShapeHandler";

    private static final String KEY_TEMPLATE = "template";
    private static final String CSV_FILE_HIGH = "high.csv";
    private static final String CSV_FILE_MID = "mid.csv";
    private static final String CSV_FILE_LOW = "low.csv";
    private static final int MAP_SIZE = 51;
    private BlockingQueue<FramePair> mBlockingQueue;
    private LinkedList<FramePair> mPcmList;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private String fileName;
    private LinkedList<Map<String, Float>> blendShapesList;
    private CVSReaderManager cvsReaderManager;
    private int csvListSize;
    private int currentCsvIndex = 0;
    private Audio2FaceListener audio2FaceListener;
    private Callback mCallback;

    //当前处理到的帧 ID
    private  int processCountIndex = 0;



    private SparseArray<String> csvArray = new SparseArray<>(3);


    // 同步缓存数据结构：同时保存blendShape和audio的未处理余数
    private static class RemainingData {
        float[] blendShape; // 未处理的blendShape数据
        float[] audio;      // 对应的未处理audio数据
    }

    private final RemainingData previousRemaining = new RemainingData();// 有值就说明有未拼接完的数据


    public BlendShapeHandler() {
    }

    @SuppressLint("DiscouragedApi")
    public void init() {
        audio2FaceListener = new Audio2FaceListener();
        fileName = P.get().getString(KEY_TEMPLATE, CSV_FILE_MID);
        KLog.d(TAG, "template file name: " + fileName);
        previousRemaining.blendShape = new float[0];
        previousRemaining.audio = new float[0];
        mBlockingQueue = new LinkedBlockingQueue<>();
        mPcmList = new LinkedList<>();
        scheduler.scheduleAtFixedRate(this::executeMotorCommand, SDKContext.delaySecondRunAngles * 1000, 100, TimeUnit.MILLISECONDS);
        KLog.d(TAG, "name " + fileName);
        cvsReaderManager = CVSReaderManager.getInstance();
        initCsvData();
    }

    public void setCallback(Callback mCallback) {
        this.mCallback = mCallback;
    }

    private void initCsvData() {
        //assets
        Map<String, LinkedList<Map<String, Float>>> defaultBlendShapes = cvsReaderManager.getEmotionBlendShapes();
        //sdcard
        Map<String, LinkedList<Map<String, Float>>> sdcardBlendShapes = cvsReaderManager.getDefaultBlendShapes();

        if (defaultBlendShapes!=null && !defaultBlendShapes.isEmpty()){
            blendShapesList = defaultBlendShapes.get(fileName);
            if (blendShapesList!=null && !blendShapesList.isEmpty()){
                KLog.d(TAG,"assets FileName ->"+fileName);
                KLog.d(TAG,"使用内assets 内置的csv size -> "+blendShapesList.size());
            }
        }

        if (sdcardBlendShapes!=null && !sdcardBlendShapes.isEmpty()){
            blendShapesList = sdcardBlendShapes.get("/sdcard/robot_csv/default.csv");
            if (blendShapesList!=null && !blendShapesList.isEmpty()){
                KLog.d(TAG,"使用sdcard 上的csv size -> "+blendShapesList.size());
            }
        }

        if (blendShapesList != null) {
            csvListSize = blendShapesList.size();
            KLog.d(TAG, "blendShapesList size: " + csvListSize);
        }
    }


    public Audio2FaceListener getAudio2FaceListener() {
        return audio2FaceListener;
    }

    private class Audio2FaceListener implements Audio2FaceCallback {
        @Override
        public void onResult(float[] outputParams, float[] audioData, int tag) {
            updateBlendShape(outputParams, audioData, tag);
        }
    }

    /**
     * 核心更新方法：处理blendShape和audio的同步帧数据
     *
     * @param blendShape 表情参数数组，每帧包含MAP_SIZE个元素
     * @param audioData  音频数据数组，总长度应为帧数×每帧样本数
     */
    public void updateBlendShape(float[] blendShape, float[] audioData, int tag) {
        KLog.d(TAG, "updateBlendShape");

        if (mCallback != null) {
            mCallback.onOriginResult(blendShape, audioData);
        }

        if (scheduler == null) {
            KLog.d(TAG, "scheduler==null");
        }


        if (scheduler != null) {
            boolean isTerminated = scheduler.isTerminated();
            boolean isShutdown = scheduler.isShutdown();
            KLog.d(TAG, "isTerminated -> " + isTerminated + "   isShutdown  -> " + isShutdown);
        }

        // 空值检查确保数据有效性
        if (blendShape == null || blendShape.length == 0 || audioData == null || audioData.length == 0) {
            KLog.d(TAG, "输入数据为空...");
            return;
        }

        KLog.d(TAG, "blendShape len  " + blendShape.length);
        KLog.d(TAG, "audioData len  " + audioData.length);

        //合并历史缓存数据与当前新数据
        float[] combinedBlend = concatArrays(previousRemaining.blendShape, blendShape);
        float[] combinedAudio = concatArrays(previousRemaining.audio, audioData);

        processCompleteFramesAK(combinedBlend, combinedAudio,tag);



//        // 计算总帧数（基于blendShape长度）
//        int totalBlendFrames = combinedBlend.length / MAP_SIZE;
//        // 计算每帧音频样本数（根据原始数据长度比例保持同步）
//        int audioSamplesPerFrame = (combinedAudio.length > 0 && totalBlendFrames > 0) ?
//                combinedAudio.length / totalBlendFrames : 0;
//
//
//        //计算可处理的完整3帧组
//        int remainderFrames = totalBlendFrames % 3;  // 余数帧数
//        int processedFrames = totalBlendFrames - remainderFrames; // 可处理帧数
//
//        //缓存余数数据（同时处理blend和audio）
//        cacheRemainingData(combinedBlend, combinedAudio, processedFrames, audioSamplesPerFrame);
//
//        //处理完整3帧组数据
//        if (processedFrames > 0) {
//            processCompleteFrames(combinedBlend, combinedAudio, processedFrames, audioSamplesPerFrame, tag);
//        }
    }

    /**
     * 缓存未处理的余数数据（原子化操作）
     *
     * @param blendData       合并后的完整blend数据
     * @param audioData       合并后的完整audio数据
     * @param processedFrames 已处理的帧数
     * @param samplesPerFrame 每帧音频样本数
     */
    private void cacheRemainingData(float[] blendData, float[] audioData,
                                    int processedFrames, int samplesPerFrame) {
        // 计算blendShape余数起始位置：已处理帧数×每帧参数数量
        int blendStart = processedFrames * MAP_SIZE;
        previousRemaining.blendShape = Arrays.copyOfRange(
                blendData, blendStart, blendData.length);

        // 计算audio余数起始位置：已处理帧数×每帧样本数
        int audioStart = processedFrames * samplesPerFrame;
        previousRemaining.audio = Arrays.copyOfRange(
                audioData, audioStart, audioData.length);
    }

    private int mLastTag = -1;



    // blendData  和  audioData 数据都是加完尾巴的全部数据。 注意 1， blendData数据也有可能不是3的整赔数
    private void processCompleteFramesAK(float[] blendData, float[] audioData,int frameType) {

        int status = 0;
        long  start = System.nanoTime();
        //BS与音频对齐 总帧数
        int totalFrames = blendData.length / MAP_SIZE / 3;
        //BS余数
        int bsremainder = blendData.length / MAP_SIZE % 3;

        //1，BS有余数缓存起来 下一组数据放到头部。
        int bsremainderSize = bsremainder * MAP_SIZE;
        KLog.d(TAG, "开始处理推理返回数据 blendData float size:" + blendData.length + " audioData floatSize:" + audioData.length + " totalFrames is" + totalFrames + " bs余数:" + bsremainder + " BS缓存位数：" + bsremainderSize + " frameType:" + frameType);
        if (bsremainder == 0) {
            previousRemaining.blendShape = Arrays.copyOfRange(
                    blendData, blendData.length - bsremainderSize, blendData.length);
        }


        for (int i = 0; i < totalFrames; i++) {
            processCountIndex += 1;
            //2,取出每一帧的音频数据，
            float[] aduioFrame = new float[1600];
            int audioStartIndex = 1600 * i;
            int audioendIndex = audioStartIndex + 1600;
            if (audioendIndex > audioData.length) {
                //1,1音频有余数缓存起来 下一组数据放到头部。
                int audioRemainder = audioData.length % 1600;
                KLog.d(TAG, "audioRemainder is:" + audioRemainder);
                previousRemaining.audio = Arrays.copyOfRange(
                        audioData, audioData.length - audioRemainder, audioData.length);

            } else {
                aduioFrame = Arrays.copyOfRange(audioData, audioStartIndex, audioendIndex);
            }

            //3,取出每一帧的 BS 数据。
            int blendStartIndex = MAP_SIZE * i * 3;
            int blendendIndex = blendStartIndex + MAP_SIZE;
            float[] currentBlend = Arrays.copyOfRange(
                    blendData,
                    blendStartIndex,
                    blendendIndex
            );
            KLog.d(TAG, "blendStartIndex : " + blendStartIndex + "  blendendIndex:" + blendendIndex + " currentBlend float size:" + currentBlend.length);
            //转换blend格式
            Map<String, Float> blendMap = oneFrameBsToMap(currentBlend);

            //4,设置帧的状态
            if (frameType == 0) {
                status = i == 0 ? 0 : 1;
//                KLog.d(TAG + "_Status", "tag ->0" + " groupIndex  " + groupIndex + " status " + status);
            } else if (frameType == 1) {
                status = 1;
//                KLog.d(TAG + "_Status", "tag ->1" + " groupIndex  " + groupIndex + " status " + status);
            } else if (frameType == 2) {
                if (mLastTag == 1) {
                    // 0,1 ,2 连续返回
                    status = i == totalFrames - 1 ? 2 : 1;
//                    KLog.d(TAG + "_Status", "tag ->2" + " groupIndex  " + groupIndex + " status " + status);
//                } else {
                    if (totalFrames == 1) {
                        status = 2;
                    } else if (totalFrames == 2) {
                        status = i == 0 ? 0 : 2;
                    } else {
//                    totalFrames>=3
                        if (i == 0) {
                            status = 0;
                        } else if (i < totalFrames - 1) {
                            status = 1;
                        } else {
                            status = 2;
                        }
                    }
                }

            } else {
                //tag =3
                if (totalFrames == 1) {
                    status = 2;
                } else if (totalFrames == 2) {
                    status = i == 0 ? 0 : 2;
                } else {
//                    totalFrames>=3
                    if (i == 0) {
                        status = 0;
                    } else if (i < totalFrames - 1) {
                        status = 1;
                    } else {
                        status = 2;
                    }
                }


            }

            mLastTag = frameType;

            //add by ak 添加特殊判断如果还有尾巴数据，帧状态强制设置为1
            if(status ==2 &&previousRemaining.blendShape.length > 0){
                status = 1;
            }

            KLog.d(TAG,"status   ->"+status);

            // 创建帧对并加入队列（保持原始帧序号）
            FramePair pair = new FramePair(
                    blendMap,
                    aduioFrame,
                    processCountIndex,
                    status
            );

            mBlockingQueue.add(pair);
            mPcmList.add(pair);


            KLog.d(TAG, "推理返回数据处理用时间: " + String.format("%.6f", (System.nanoTime() - start) / 1_000_000.0) + " ms  BS 数据量：" + blendData.length);

        }

        //4,特殊处理 如果是结尾帧 并且尾巴还有数据，都播放掉
        if(previousRemaining.blendShape.length > 0) {
            KLog.d(TAG, "已经是尾帧播放所有缓存数据");

            processCountIndex += 1;
            //4.1拿出 BS 的第一组，也有可能只有一组
            float[] currentBlend = Arrays.copyOfRange(
                    previousRemaining.blendShape,
                    0,
                    MAP_SIZE
            );
            Map<String, Float> blendMap = oneFrameBsToMap(currentBlend);

            //4.2拿出音频的所有数据
            FramePair pair = new FramePair(
                    blendMap,
                    previousRemaining.audio,
                    processCountIndex,
                    2
            );

            mBlockingQueue.add(pair);
            mPcmList.add(pair);
        }


    }


    /**
     * 处理完整的3帧组数据（每3帧取1个blend，生成3个audio帧）
     */
    private void processCompleteFrames(float[] blendData, float[] audioData,
                                       int processedFrames, int samplesPerFrame, int tag) {
        // 每组处理3帧，总处理组数 = 总处理帧数 / 3
        int totalGroups = processedFrames / 3;  //eg totalGroups=18

        int status = 0;
        for (int groupIndex = 0; groupIndex < totalGroups; groupIndex++) {
            // 提取代表blend帧（每组的第1帧）
            int blendFrameIndex = groupIndex * 3; // 0,3,6...
            float[] currentBlend = extractBlendFrame(blendData, blendFrameIndex);

            //转换blend格式
            Map<String, Float> blendMap = oneFrameBsToMap(currentBlend);
            if (blendMap == null) continue;

            // 提取对应3个audio帧（每组包含3帧音频）
            int audioStartFrame = groupIndex * 3;
            float[] threeAudioFrames = extractAudioFrames(
                    audioData, audioStartFrame, 3, samplesPerFrame);


            if (tag == 0) {
                status = groupIndex == 0 ? 0 : 1;
//                KLog.d(TAG + "_Status", "tag ->0" + " groupIndex  " + groupIndex + " status " + status);
            } else if (tag == 1) {
                status = 1;
//                KLog.d(TAG + "_Status", "tag ->1" + " groupIndex  " + groupIndex + " status " + status);
            } else if (tag == 2) {
                if (mLastTag == 1) {
                    // 0,1 ,2 连续返回
                    status = groupIndex == totalGroups - 1 ? 2 : 1;
//                    KLog.d(TAG + "_Status", "tag ->2" + " groupIndex  " + groupIndex + " status " + status);
//                } else {
                    if (totalGroups == 1) {
                        status = 2;
                    } else if (totalGroups == 2) {
                        status = groupIndex == 0 ? 0 : 2;
                    } else {
//                    totalGroups>=3
                        if (groupIndex == 0) {
                            status = 0;
                        } else if (groupIndex < totalGroups - 1) {
                            status = 1;
                        } else {
                            status = 2;
                        }
                    }
                }

            } else {
                //tag =3
                if (totalGroups == 1) {
                    status = 2;
                } else if (totalGroups == 2) {
                    status = groupIndex == 0 ? 0 : 2;
                } else {
//                    totalGroups>=3
                    if (groupIndex == 0) {
                        status = 0;
                    } else if (groupIndex < totalGroups - 1) {
                        status = 1;
                    } else {
                        status = 2;
                    }
                }
            }

            mLastTag = tag;

            // 创建帧对并加入队列（保持原始帧序号）
            FramePair pair = new FramePair(
                    blendMap,
                    threeAudioFrames,
                    groupIndex,
                    status
            );
            mBlockingQueue.add(pair);
            mPcmList.add(pair);
        }
    }

    private float[] extractBlendFrame(float[] data, int frameIndex) {
        return Arrays.copyOfRange(
                data,
                frameIndex * MAP_SIZE,
                (frameIndex + 1) * MAP_SIZE
        );
    }

    /**
     * 从音频数据中提取指定数量的连续帧
     *
     * @param startFrame      起始帧索引（从0开始）
     * @param frameCount      需要提取的帧数
     * @param samplesPerFrame 每帧样本数
     */
    private float[] extractAudioFrames(float[] audioData, int startFrame,
                                       int frameCount, int samplesPerFrame) {
        int startIndex = startFrame * samplesPerFrame;
        int endIndex = startIndex + frameCount * samplesPerFrame;
        // 边界检查防止数组越界
        endIndex = Math.min(endIndex, audioData.length);
        return Arrays.copyOfRange(audioData, startIndex, endIndex);
    }


    /**
     * 数组合并工具方法（用于合并缓存和新数据）
     *
     * @return 合并后的新数组，保留a在前b在后的顺序
     */
    private float[] concatArrays(float[] a, float[] b) {
        if (a == null || a.length == 0) return b;
        if (b == null || b.length == 0) return a;

        float[] result = new float[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length); // 复制第一部分
        System.arraycopy(b, 0, result, a.length, b.length); // 复制第二部分
        return result;
    }


    private Map<String, Float> oneFrameBsToMap(float[] bs) {
        Map<String, Float> blendShapes = new HashMap<>(MAP_SIZE);

        if (bs == null || bs.length != MAP_SIZE) {
            KLog.d(TAG, "bs 数组为空，或长度不对...");
            return null;
        }

        for (int i = 0; i < MAP_SIZE; i++) {
            String key = BlendShapeOrder.blendShapeOrder[i];
            float value = bs[i];
            blendShapes.put(key, value);
        }
        blendShapes.putAll(BlendShapeOrder.getEyeMap());
        return blendShapes;
    }


    //TODO
    public void interrupt() {
        // 清空残留数据
        previousRemaining.blendShape = new float[0];
        previousRemaining.audio = new float[0];
        mBlockingQueue.clear();
        mPcmList.clear();
    }

    private void executeMotorCommand() {

        try {

//            KLog.d(TAG, "SDKContext.isPause  ->" + SDKContext.isPause);
            if (SDKContext.isPause) return;

            if (blendShapesList == null || blendShapesList.isEmpty()) {
                KLog.d(TAG, "blendShapesList 为空... 重新获取csv数据");
                initCsvData();
                return;
            }

            currentCsvIndex = (currentCsvIndex +6)% csvListSize;
            Map<String, Float> csvBlendShapes = blendShapesList.get(currentCsvIndex);
            FramePair framePair = mBlockingQueue.poll();

            if (mCallback != null) {
                if (framePair!=null){
//                    KLog.d(TAG, "mCallback-- dts " + framePair.getDts());
                }

                mCallback.onSynchronousResult(framePair, csvBlendShapes, mPcmList);
            }

            currentCsvIndex++;

        } catch (Exception e) {
            KLog.w(TAG, e.getMessage());
        }
    }

}