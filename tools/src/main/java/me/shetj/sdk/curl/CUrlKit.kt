package me.shetj.sdk.curl

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
            System.loadLibrary("tools")
        }

        //全局
        @JvmStatic
        external fun init()

        //全局
        @JvmStatic
        external fun cleanup()

        @JvmStatic
        external fun getVersion(): String

        //设置证书
        //如果证书有问题：77:Problem with the SSL CA cert (path? access rights?)
        @JvmStatic
        external fun setCertificate(certificatePath: String)
    }

    external fun postJson(url:String,json:String):String

    external fun get(url:String):String
}