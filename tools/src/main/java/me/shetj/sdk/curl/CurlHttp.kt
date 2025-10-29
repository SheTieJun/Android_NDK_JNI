package me.shetj.sdk.curl

import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/11/30<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b> 高级HTTP客户端，支持重试、拦截器、缓存等功能 <br>
 */
object CurlHttp {

    private val curl by lazy { CUrlKit() }
    private val requestInterceptors = mutableListOf<RequestInterceptor>()
    private val responseInterceptors = mutableListOf<ResponseInterceptor>()
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val requestCounter = AtomicLong(0)
    
    // 默认配置
    private var defaultTimeout = 30
    private var defaultConnectTimeout = 10
    private var defaultRetryConfig = RetryConfig()
    private var defaultCacheConfig = CacheConfig()

    init {
        CUrlKit.init()
    }

    /**
     * 初始化curl
     */
    fun initCurl() {
        CUrlKit.init()
    }

    /**
     * 清理资源
     */
    fun cleanUp() {
        CUrlKit.cleanup()
        cache.clear()
    }

    /**
     * 设置默认超时时间
     */
    fun setDefaultTimeout(timeout: Int, connectTimeout: Int = 10) {
        defaultTimeout = timeout
        defaultConnectTimeout = connectTimeout
    }

    /**
     * 设置默认重试配置
     */
    fun setDefaultRetryConfig(retryConfig: RetryConfig) {
        defaultRetryConfig = retryConfig
    }

    /**
     * 设置默认缓存配置
     */
    fun setDefaultCacheConfig(cacheConfig: CacheConfig) {
        defaultCacheConfig = cacheConfig
    }

    /**
     * 添加请求拦截器
     */
    fun addRequestInterceptor(interceptor: RequestInterceptor) {
        requestInterceptors.add(interceptor)
    }

    /**
     * 添加响应拦截器
     */
    fun addResponseInterceptor(interceptor: ResponseInterceptor) {
        responseInterceptors.add(interceptor)
    }

    /**
     * 移除请求拦截器
     */
    fun removeRequestInterceptor(interceptor: RequestInterceptor) {
        requestInterceptors.remove(interceptor)
    }

    /**
     * 移除响应拦截器
     */
    fun removeResponseInterceptor(interceptor: ResponseInterceptor) {
        responseInterceptors.remove(interceptor)
    }

    /**
     * 清空所有拦截器
     */
    fun clearInterceptors() {
        requestInterceptors.clear()
        responseInterceptors.clear()
    }

    /**
     * 执行HTTP请求（同步）
     */
    fun execute(request: HttpRequest): HttpResponse {
        return executeWithRetry(request, defaultRetryConfig)
    }

    /**
     * 执行HTTP请求（异步）
     */
    suspend fun executeAsync(request: HttpRequest): HttpResponse = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            try {
                val response = execute(request)
                continuation.resume(response)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * 带重试的请求执行
     */
    private fun executeWithRetry(request: HttpRequest, retryConfig: RetryConfig): HttpResponse {
        var lastException: Exception? = null
        var currentDelay = retryConfig.retryDelay
        
        repeat(retryConfig.maxRetries + 1) { attempt ->
            try {
                return executeInternal(request)
            } catch (e: Exception) {
                lastException = e
                
                // 判断是否应该重试
                val shouldRetry = when (e) {
                    is HttpException -> {
                        // 5xx错误或特定4xx错误可以重试
                        e.statusCode >= 500 || e.statusCode == 408 || e.statusCode == 429
                    }
                    else -> {
                        // 连接失败或超时可以重试
                        retryConfig.retryOnConnectionFailure || retryConfig.retryOnTimeout
                    }
                }
                
                if (attempt < retryConfig.maxRetries && shouldRetry) {
                    Log.w("CurlHttp", "Request failed, retrying in ${currentDelay}ms. Attempt: ${attempt + 1}")
                    Thread.sleep(currentDelay)
                    currentDelay = (currentDelay * retryConfig.backoffMultiplier).toLong()
                } else {
                    return@repeat
                }
            }
        }
        
        throw lastException ?: Exception("Request failed after ${retryConfig.maxRetries} retries")
    }

    /**
     * 内部请求执行逻辑
     */
    private fun executeInternal(originalRequest: HttpRequest): HttpResponse {
        // 应用请求拦截器
        var request = originalRequest
        requestInterceptors.forEach { interceptor ->
            request = interceptor.intercept(request)
        }

        // 检查缓存
        val cacheKey = generateCacheKey(request)
        if (request.method == HttpMethod.GET) {
            val cachedResponse = getCachedResponse(cacheKey, defaultCacheConfig)
            if (cachedResponse != null) {
                Log.d("CurlHttp", "Cache hit for: ${request.url}")
                return cachedResponse
            }
        }

        // 执行实际请求
        val startTime = System.currentTimeMillis()
        val responseBody = try {
            when (request.method) {
                HttpMethod.GET -> curl.get(request.url)
                HttpMethod.POST -> {
                    val body = request.body
                    if (body != null) {
                        if (isJsonContent(request)) {
                            curl.postJson(request.url, body)
                        } else {
                            curl.post(request.url, body)
                        }
                    } else {
                        curl.post(request.url, "")
                    }
                }
                HttpMethod.PUT -> {
                    val body = request.body
                    if (body != null) {
                        if (isJsonContent(request)) {
                            curl.putJson(request.url, body)
                        } else {
                            curl.put(request.url, body)
                        }
                    } else {
                        curl.put(request.url, "")
                    }
                }
                HttpMethod.DELETE -> curl.delete(request.url)
                HttpMethod.HEAD -> curl.head(request.url)
                HttpMethod.PATCH -> {
                    val body = request.body
                    if (body != null) {
                        if (isJsonContent(request)) {
                            curl.patchJson(request.url, body)
                        } else {
                            curl.patch(request.url, body)
                        }
                    } else {
                        curl.patch(request.url, "")
                    }
                }
                HttpMethod.OPTIONS -> curl.options(request.url)
            }
        } catch (e: Exception) {
            Log.e("CurlHttp", "Request failed: ${e.message}", e)
            throw HttpException(0, "Request failed", e.message ?: "Unknown error", e)
        }
        
        val responseTime = System.currentTimeMillis() - startTime

        // 创建响应对象（简化版，实际应该解析详细响应信息）
        var response = HttpResponse(
            statusCode = 200, // 简化处理，实际应该从native层获取
            statusMessage = "OK",
            headers = emptyMap(),
            body = responseBody,
            responseTime = responseTime
        )

        // 应用响应拦截器
        responseInterceptors.forEach { interceptor ->
            response = interceptor.intercept(response)
        }

        // 缓存响应
        if (request.method == HttpMethod.GET && response.isSuccess) {
            cacheResponse(cacheKey, response, defaultCacheConfig)
        }

        return response
    }

    /**
     * 判断是否为JSON内容
     */
    private fun isJsonContent(request: HttpRequest): Boolean {
        return request.headers["Content-Type"]?.contains("application/json") == true ||
                request.body?.trim()?.startsWith("{") == true ||
                request.body?.trim()?.startsWith("[") == true
    }

    /**
     * 生成缓存键
     */
    private fun generateCacheKey(request: HttpRequest): String {
        return "${request.method.name}:${request.url}:${request.headers.hashCode()}"
    }

    /**
     * 获取缓存响应
     */
    private fun getCachedResponse(cacheKey: String, cacheConfig: CacheConfig): HttpResponse? {
        if (cacheConfig.strategy == CacheStrategy.NO_CACHE || 
            cacheConfig.strategy == CacheStrategy.NETWORK_ONLY) {
            return null
        }

        val cacheEntry = cache[cacheKey] ?: return null
        val now = System.currentTimeMillis()
        
        if (now - cacheEntry.timestamp > cacheConfig.maxAge * 1000) {
            cache.remove(cacheKey)
            return null
        }
        
        return cacheEntry.response
    }

    /**
     * 缓存响应
     */
    private fun cacheResponse(cacheKey: String, response: HttpResponse, cacheConfig: CacheConfig) {
        if (cacheConfig.strategy == CacheStrategy.NO_CACHE) {
            return
        }

        // 简单的LRU缓存清理
        if (cache.size >= 100) { // 最大缓存条目数
            val oldestKey = cache.keys.first()
            cache.remove(oldestKey)
        }

        cache[cacheKey] = CacheEntry(response, System.currentTimeMillis())
    }

    /**
     * 清空缓存
     */
    fun clearCache() {
        cache.clear()
    }

    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): String {
        return "Cache size: ${cache.size}, Total requests: ${requestCounter.get()}"
    }

    /**
     * 便捷方法 - GET请求
     */
    fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse {
        return execute(HttpRequest(url = url, method = HttpMethod.GET, headers = headers))
    }

    /**
     * 便捷方法 - POST JSON请求
     */
    fun postJson(url: String, json: String, headers: Map<String, String> = emptyMap()): HttpResponse {
        val jsonHeaders = headers.toMutableMap()
        jsonHeaders["Content-Type"] = "application/json"
        return execute(HttpRequest(
            url = url, 
            method = HttpMethod.POST, 
            headers = jsonHeaders, 
            body = json
        ))
    }

    /**
     * 便捷方法 - PUT JSON请求
     */
    fun putJson(url: String, json: String, headers: Map<String, String> = emptyMap()): HttpResponse {
        val jsonHeaders = headers.toMutableMap()
        jsonHeaders["Content-Type"] = "application/json"
        return execute(HttpRequest(
            url = url, 
            method = HttpMethod.PUT, 
            headers = jsonHeaders, 
            body = json
        ))
    }

    /**
     * 便捷方法 - DELETE请求
     */
    fun delete(url: String, headers: Map<String, String> = emptyMap()): HttpResponse {
        return execute(HttpRequest(url = url, method = HttpMethod.DELETE, headers = headers))
    }

    /**
     * 设置证书
     */
    fun setCertificate(cacert: File) {
        Log.i("setCertificate", cacert.path)
        CUrlKit.setCertificate(cacert.path)
    }

    /**
     * 测试GET请求（保持向后兼容）
     */
    fun testGet(): String {
        return get("https://dummyjson.com/c/3029-d29f-4014-9fb4").body
    }

    /**
     * 测试POST请求（保持向后兼容）
     */
    fun testPost(): String {
        return postJson(
            "https://a24ca463-edeb-468b-a0ae-8f85dfe81baa.mock.pstmn.io/posttest",
            "{\"code\":\"0000\"}"
        ).body
    }
}

/**
 * 缓存条目
 */
private data class CacheEntry(
    val response: HttpResponse,
    val timestamp: Long
)