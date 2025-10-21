package com.noetix.libnoetix

import com.noetix.utils.KLog

class AngleReset {

    fun reset() {
        val targetBlendShapes = FacialExpressionMap.createExpressionMap()
        val motorAngles: IntArray = INativeApi.get().mappingMotor(targetBlendShapes)
        KLog.d("AngleReset", "Motor angles: ")
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