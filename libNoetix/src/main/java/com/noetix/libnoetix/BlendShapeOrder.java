package com.noetix.libnoetix;

import java.util.HashMap;
import java.util.Map;

public class BlendShapeOrder {
    public static final Map<String,Float> eyeMap = new HashMap<String,Float>();
    static {
        eyeMap.put("RightEyeYaw",0.0f);
        eyeMap.put("RightEyePitch",0.0f);
        eyeMap.put("LeftEyeYaw",0.0f);
        eyeMap.put("LeftEyePitch",0.0f);
    }

    public static final String[] blendShapeOrder = {
            "EyeBlinkLeft", "EyeBlinkRight", "EyeSquintLeft", "EyeSquintRight",
            "EyeLookDownLeft", "EyeLookDownRight", "EyeLookInLeft", "EyeLookInRight",
            "EyeWideLeft", "EyeWideRight", "EyeLookOutLeft", "EyeLookOutRight",
            "EyeLookUpLeft", "EyeLookUpRight", "BrowDownLeft", "BrowDownRight",
            "BrowInnerUp", "BrowOuterUpLeft", "BrowOuterUpRight", "JawOpen",
            "MouthClose", "JawLeft", "JawRight", "JawForward", "MouthUpperUpLeft",
            "MouthUpperUpRight", "MouthLowerDownLeft", "MouthLowerDownRight",
            "MouthRollUpper", "MouthRollLower", "MouthSmileLeft", "MouthSmileRight",
            "MouthDimpleLeft", "MouthDimpleRight", "MouthStretchLeft",
            "MouthStretchRight", "MouthFrownLeft", "MouthFrownRight", "MouthPressLeft",
            "MouthPressRight", "MouthPucker", "MouthFunnel", "MouthLeft", "MouthRight",
            "MouthShrugLower", "MouthShrugUpper", "NoseSneerLeft", "NoseSneerRight",
            "CheekPuff", "CheekSquintLeft", "CheekSquintRight"
    };


    public static Map<String,Float> getEyeMap(){
        return eyeMap;
    }

}
