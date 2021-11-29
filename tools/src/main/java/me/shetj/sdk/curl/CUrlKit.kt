package me.shetj.sdk.curl

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/11/19<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
class CUrlKit {

    companion object{
        init {
            System.loadLibrary("toolsCurl")
        }
    }

    external fun getVersion(): String


}