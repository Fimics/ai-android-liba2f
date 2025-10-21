package com.noetix.libnoetix

class FaceAnglesHelper {

     fun resetFaceAngles() {
        var defaultBlendShapes: MutableMap<String, Float> = FacialExpressionMap.createExpressionMap()
        val motorAngles: IntArray = INativeApi.get().mappingMotor(defaultBlendShapes)
        val motorCommands = intArrayToFloatArray(motorAngles)
        INativeApi.get().setFaceAngles(motorCommands)
    }

    private fun intArrayToFloatArray(motorAngles: IntArray): FloatArray {
        val motorCommands = FloatArray(motorAngles.size)
        for (i in motorAngles.indices) {
            motorCommands[i] = motorAngles[i].toFloat()
        }
        return motorCommands
    }


}
