package me.shetj.sdk.utils

import android.content.Context

class Uitls {


    companion object{
        init {
            System.loadLibrary("tools")
        }

        //全局
        @JvmStatic
        external fun getPackageName():String

        external fun verificationPkg():String

        @JvmStatic
        external fun verificationSign(context: Context):String?
    }

}