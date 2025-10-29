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

    // 基础HTTP方法
    external fun get(url: String): String
    external fun post(url: String, body: String): String
    external fun put(url: String, body: String): String
    external fun delete(url: String): String
    external fun head(url: String): String
    external fun patch(url: String, body: String): String
    external fun options(url: String): String

    // JSON请求方法
    external fun postJson(url: String, json: String): String
    external fun putJson(url: String, json: String): String
    external fun patchJson(url: String, json: String): String

    // 高级请求方法 - 支持完整配置
    external fun request(
        url: String,
        method: String,
        headers: Array<String>?,
        body: String?,
        timeout: Int,
        connectTimeout: Int,
        userAgent: String?,
        cookies: String?,
        followRedirects: Boolean,
        maxRedirects: Int,
        certificatePath: String?,
        ignoreSSL: Boolean
    ): String

    // 获取响应详情（包含状态码、头部等）
    external fun requestWithDetails(
        url: String,
        method: String,
        headers: Array<String>?,
        body: String?,
        timeout: Int,
        connectTimeout: Int,
        userAgent: String?,
        cookies: String?,
        followRedirects: Boolean,
        maxRedirects: Int,
        certificatePath: String?,
        ignoreSSL: Boolean
    ): String // 返回JSON格式的详细响应信息

    // 文件下载
    external fun downloadFile(url: String, filePath: String): Boolean

    // 文件上传
    external fun uploadFile(url: String, filePath: String, fieldName: String): String

    // 批量请求
    external fun batchRequest(requests: Array<String>): Array<String>

    // 连接池管理
    external fun setMaxConnections(maxConnections: Int)
    external fun setKeepAlive(keepAlive: Boolean)
    external fun setConnectionPoolTimeout(timeout: Int)

    // 代理设置
    external fun setProxy(proxyUrl: String, proxyType: String)
    external fun setProxyAuth(username: String, password: String)

    // DNS设置
    external fun setDnsServers(dnsServers: Array<String>)
    external fun setDnsTimeout(timeout: Int)

    // 性能监控
    external fun getConnectionStats(): String
    external fun resetConnectionStats()
}