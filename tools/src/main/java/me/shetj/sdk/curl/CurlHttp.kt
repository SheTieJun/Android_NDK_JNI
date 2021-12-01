package me.shetj.sdk.curl

import android.util.Log
import java.io.File

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/11/30<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
object CurlHttp {

    val curl by lazy { CUrlKit() }


    fun initCurl(){
        CUrlKit.init()
    }

    fun cleanUp(){
        CUrlKit.cleanup()
    }

    fun testGet(): String {
       return curl.get("https://109cffaa-4442-49c0-b87d-3265b7dc2b3e.mock.pstmn.io/me.shet")
    }

    fun testPost(): String {
        return curl.postJson("https://a24ca463-edeb-468b-a0ae-8f85dfe81baa.mock.pstmn.io/posttest","{\"code\":\"0000\"}")
    }




    fun setCertificate(cacert: File) {
        Log.i("setCertificate",cacert.path)
        CUrlKit.setCertificate(cacert.path)
    }

}