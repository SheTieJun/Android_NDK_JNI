package me.shetj.sdk.curl

import android.util.Log

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/11/19<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b> curl 不是线程安全，需要每个线程用自己的curl <br>
 */
class CUrlKit {

    companion object{
        init {
            System.loadLibrary("toolsCurl")
        }

        @JvmStatic
        external fun init()

        @JvmStatic
        external fun cleanup()

        @JvmStatic
        external fun getVersion(): String
    }



    external fun postJson(url:String,json:String)

    external fun get(url:String):String

    fun callback(data:String){
        Log.i("CUrlKit",data)
    }
}