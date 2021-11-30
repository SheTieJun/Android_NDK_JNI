package me.shetj.sdk.curl

import java.io.File

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/11/30<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
object CurlHttp {

    fun initCurl(){
        CUrlKit.init()
    }

    fun cleanUp(){
        CUrlKit.cleanup()
    }

    fun testGet(): String {
       return CUrlKit().get("https://109cffaa-4442-49c0-b87d-3265b7dc2b3e.mock.pstmn.io/shetj_test")
    }

    fun setCacert(cacert: File) {

    }

}