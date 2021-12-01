package me.shetj.sdk.json

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/12/1<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
class JsonKit {

    companion object{
        init {
            System.loadLibrary("toolsCurl")
        }

        //全局
        @JvmStatic
        external fun test()


    }
}