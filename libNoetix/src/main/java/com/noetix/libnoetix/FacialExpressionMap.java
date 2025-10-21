package com.noetix.libnoetix;

import java.util.HashMap;
import java.util.Map;

public class FacialExpressionMap {
    public static Map<String, Float> createExpressionMap() {
        Map<String, Float> expressionMap = new HashMap<>();

        // ============ 眼睛动作 - 左眼 ============
        expressionMap.put("EyeBlinkLeft", 0.0f);      // 左眼眨眼程度 (0=睁开, 1=完全闭合)
        expressionMap.put("EyeLookDownLeft", 0.0f);           // 左眼向下看程度 (0=无动作, 1=最大幅度)
        expressionMap.put("EyeLookInLeft", 0.0f);             // 左眼向内（鼻侧）转动程度
        expressionMap.put("EyeLookOutLeft", 0.0f);    // 左眼向外（太阳穴）转动程度
        expressionMap.put("EyeLookUpLeft", 0.0f);     // 左眼向上看程度
        expressionMap.put("EyeSquintLeft", 0.0f);     // 左眼眯眼/收缩程度
        expressionMap.put("EyeWideLeft", 0.0f);               // 左眼瞪大程度 (0=正常, 1=最大瞪眼)

        // ============ 眼睛动作 - 右眼 ============
        expressionMap.put("EyeBlinkRight", 0.0f);     // 右眼眨眼程度
        expressionMap.put("EyeLookDownRight", 0.0f);          // 右眼向下看程度
        expressionMap.put("EyeLookInRight", 0.0f);    // 右眼向内转动程度
        expressionMap.put("EyeLookOutRight", 0.0f);           // 右眼向外转动程度
        expressionMap.put("EyeLookUpRight", 0.0f);    // 右眼向上看程度
        expressionMap.put("EyeSquintRight", 0.0f);    // 右眼眯眼程度
        expressionMap.put("EyeWideRight", 0.0f);              // 右眼瞪大程度

        // ============ 下巴动作 ============
        expressionMap.put("JawForward", 0.0f);        // 下巴前伸程度 (0=正常位置, 1=最大前伸)
        expressionMap.put("JawRight", 0.0f);          // 下巴右移程度 (面部坐标系)
        expressionMap.put("JawLeft", 0.0f);                   // 下巴左移程度
        expressionMap.put("JawOpen", 0.0f);           // 下巴张开程度 (0=闭合, 1=最大张开)

        // ============ 嘴唇基础动作 ============
        expressionMap.put("MouthClose", 0.0f);        // 嘴唇闭合程度（与JawOpen相反）
        expressionMap.put("MouthFunnel", 0.0f);       // 嘴唇呈漏斗形（噘嘴）程度
        expressionMap.put("MouthPucker", 0.0f);       // 嘴唇皱起（接吻状）程度
        expressionMap.put("MouthRight", 0.0f);                // 嘴唇整体右移
        expressionMap.put("MouthLeft", 0.0f);         // 嘴唇整体左移

        // ============ 嘴唇表情 ============
        expressionMap.put("MouthSmileLeft", 0.0f);     // 左嘴角上扬（微笑）
        expressionMap.put("MouthSmileRight", 0.0f);   // 右嘴角上扬
        expressionMap.put("MouthFrownLeft", 0.0f);            // 左嘴角下垂（悲伤）
        expressionMap.put("MouthFrownRight", 0.0f);           // 右嘴角下垂
        expressionMap.put("MouthDimpleLeft", 0.0f);    // 左脸颊酒窝显现程度
        expressionMap.put("MouthDimpleRight", 0.0f);  // 右脸颊酒窝
        expressionMap.put("MouthStretchLeft", 0.0f);   // 左嘴角横向拉伸
        expressionMap.put("MouthStretchRight", 0.0f); // 右嘴角横向拉伸

        // ============ 复杂嘴部动作 ============
        expressionMap.put("MouthRollLower", 0.0f);    // 下嘴唇卷曲程度
        expressionMap.put("MouthRollUpper", 0.0f);    // 上嘴唇卷曲程度
        expressionMap.put("MouthShrugLower", 0.0f);   // 下嘴唇上耸（接触上唇）
        expressionMap.put("MouthShrugUpper", 0.0f);   // 上嘴唇下压
        expressionMap.put("MouthPressLeft", 0.0f);    // 左唇压紧牙齿
        expressionMap.put("MouthPressRight", 0.0f);   // 右唇压紧牙齿
        expressionMap.put("MouthLowerDownLeft", 0.0f);// 左下唇部下拉
        expressionMap.put("MouthLowerDownRight", 0.0f);// 右下唇部下拉
        expressionMap.put("MouthUpperUpLeft", 0.0f);  // 左上唇部提升
        expressionMap.put("MouthUpperUpRight", 0.0f); // 右上唇部提升

        // ============ 眉毛动作 ============
        expressionMap.put("BrowDownLeft", 0.0f);      // 左眉下压（皱眉）
        expressionMap.put("BrowDownRight", 0.0f);     // 右眉下压
        expressionMap.put("BrowInnerUp", 0.0f);       // 双眉内侧抬起（惊讶）
        expressionMap.put("BrowOuterUpLeft", 0.0f);           // 左眉外侧抬起
        expressionMap.put("BrowOuterUpRight", 0.0f);          // 右眉外侧抬起

        // ============ 脸颊动作 ============
        expressionMap.put("CheekPuff", 0.0f);         // 脸颊鼓起程度
        expressionMap.put("CheekSquintLeft", 0.0f);   // 左脸颊上提（使眼睛眯起）
        expressionMap.put("CheekSquintRight", 0.0f);  // 右脸颊上提

        // ============ 鼻子动作 ============
        expressionMap.put("NoseSneerLeft", 0.0f);     // 左鼻翼扩张（轻蔑表情）
        expressionMap.put("NoseSneerRight", 0.0f);    // 右鼻翼扩张

        // ============ 舌头动作 ============
        expressionMap.put("TongueOut", 0.0f);         // 舌头伸出程度 (0=口腔内, 1=完全伸出)

        // ============ 头部运动（单位：弧度） ============
        expressionMap.put("HeadYaw", 0.0f);          // 头部左右转动（负=左转，正=右转）
        expressionMap.put("HeadPitch", 0.0f);        // 头部上下点头（负=抬头，正=低头）
        expressionMap.put("HeadRoll", 0.0f);          // 头部侧倾（负=左倾，正=右倾）

        // ============ 眼部独立运动 ============
        expressionMap.put("LeftEyeYaw", 0.0f);       // 左眼水平转动
        expressionMap.put("LeftEyePitch", 0.0f);     // 左眼垂直转动
        expressionMap.put("LeftEyeRoll", 0.0f);       // 左眼旋转（虹膜扭转）
        expressionMap.put("RightEyeYaw", 0.0f);      // 右眼水平转动
        expressionMap.put("RightEyePitch", 0.0f);     // 右眼垂直转动
        expressionMap.put("RightEyeRoll", 0.0f);      // 右眼旋转

        return expressionMap;
    }
}
