package com.noetix.utils


class KeysUtils {

    companion object {
        private const val TAG = "KeysUtils"

        const val TIME_CSV = "time_csv"
        const val TIME_RKNN = "time_rknn"
        const val TIME_TEMPLATE = "time_template"
        const val TIME_ZERO = "time_zero"
        const val NATIVE_CONFIG ="native_config"


        @JvmStatic
        fun testClearTimestamp() {
            //1759117087146
            P.get().putString(TIME_CSV,"1759117087046")
            //1759117129616
            P.get().putString(TIME_RKNN,"1759117129616")
            //1759117080542
            P.get().putString(TIME_TEMPLATE,"1759117080042")
            //1759081445641
            P.get().putString(TIME_ZERO,"1759081445541")
        }

        @JvmStatic
        fun clearTimestamp() {
            KLog.d(TAG,"clearTimestamp")
            //1759117087146
            P.get().putString(TIME_CSV,"0")
            //1759117129616
            P.get().putString(TIME_RKNN,"0")
            //1759117080542
            P.get().putString(TIME_TEMPLATE,"0")
            //1759081445641
            P.get().putString(TIME_ZERO,"0")
        }

        @JvmStatic
        fun putNativeConfig(config: String){
            P.get().putString(NATIVE_CONFIG,config)
        }

        @JvmStatic
        fun getNativeConfig(): String{
            return P.get().getString(NATIVE_CONFIG,"")
        }
    }




}