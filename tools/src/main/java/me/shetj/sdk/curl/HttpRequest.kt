package me.shetj.sdk.curl

/**
 * HTTP请求数据类
 * 
 * @author shetj
 * @createTime 2024/01/01
 * @email 375105540@qq.com
 * @describe HTTP请求配置类
 */
data class HttpRequest(
    val url: String,
    val method: HttpMethod = HttpMethod.GET,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val timeout: Int = 30,
    val connectTimeout: Int = 10,
    val userAgent: String? = null,
    val cookies: String? = null,
    val followRedirects: Boolean = true,
    val maxRedirects: Int = 5,
    val certificatePath: String? = null,
    val ignoreSSL: Boolean = false
)

/**
 * HTTP方法枚举
 */
enum class HttpMethod {
    GET, POST, PUT, DELETE, HEAD, PATCH, OPTIONS
}

/**
 * HTTP响应数据类
 */
data class HttpResponse(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String,
    val contentLength: Long = 0,
    val contentType: String? = null,
    val responseTime: Long = 0,
    val isSuccess: Boolean = statusCode in 200..299
)

/**
 * HTTP异常类
 */
class HttpException(
    val statusCode: Int,
    val statusMessage: String,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * 请求拦截器接口
 */
interface RequestInterceptor {
    fun intercept(request: HttpRequest): HttpRequest
}

/**
 * 响应拦截器接口
 */
interface ResponseInterceptor {
    fun intercept(response: HttpResponse): HttpResponse
}

/**
 * 缓存策略枚举
 */
enum class CacheStrategy {
    NO_CACHE,           // 不缓存
    CACHE_FIRST,        // 优先使用缓存
    NETWORK_FIRST,      // 优先使用网络
    CACHE_ONLY,         // 仅使用缓存
    NETWORK_ONLY        // 仅使用网络
}

/**
 * 缓存配置
 */
data class CacheConfig(
    val strategy: CacheStrategy = CacheStrategy.NO_CACHE,
    val maxAge: Long = 300, // 缓存最大存活时间（秒）
    val maxSize: Long = 10 * 1024 * 1024 // 缓存最大大小（字节）
)

/**
 * 重试配置
 */
data class RetryConfig(
    val maxRetries: Int = 3,
    val retryDelay: Long = 1000, // 重试延迟（毫秒）
    val backoffMultiplier: Float = 2.0f, // 退避乘数
    val retryOnConnectionFailure: Boolean = true,
    val retryOnTimeout: Boolean = true
)