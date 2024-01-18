package me.shetj.sdk.utils

import android.content.Context

class Utils {


    companion object{
        init {
            System.loadLibrary("tools")
        }

        //全局
        @JvmStatic
        external fun getPackageName():String

        @JvmStatic
        external fun verificationPkg():String

        @JvmStatic
        external fun verificationSign(context: Context):String?
    }

}