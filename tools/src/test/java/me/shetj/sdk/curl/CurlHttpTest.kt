package me.shetj.sdk.curl

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * CurlHttp单元测试
 * 
 * @author shetj
 * @createTime 2024/01/01
 * @email 375105540@qq.com
 * @describe CurlHttp功能测试用例
 */
class CurlHttpTest {

    @Before
    fun setUp() {
        CurlHttp.initCurl()
    }

    @After
    fun tearDown() {
        CurlHttp.cleanUp()
    }

    @Test
    fun testBasicGetRequest() {
        val response = CurlHttp.get("https://httpbin.org/get")
        assertTrue("GET request should succeed", response.isSuccess)
        assertTrue("Response body should not be empty", response.body.isNotEmpty())
        assertEquals("Status code should be 200", 200, response.statusCode)
    }

    @Test
    fun testBasicPostJsonRequest() {
        val json = """{"test": "data", "number": 123}"""
        val response = CurlHttp.postJson("https://httpbin.org/post", json)
        assertTrue("POST JSON request should succeed", response.isSuccess)
        assertTrue("Response body should contain posted data", response.body.contains("test"))
    }

    @Test
    fun testPutJsonRequest() {
        val json = """{"update": "data"}"""
        val response = CurlHttp.putJson("https://httpbin.org/put", json)
        assertTrue("PUT JSON request should succeed", response.isSuccess)
        assertTrue("Response body should contain put data", response.body.contains("update"))
    }

    @Test
    fun testDeleteRequest() {
        val response = CurlHttp.delete("https://httpbin.org/delete")
        assertTrue("DELETE request should succeed", response.isSuccess)
    }

    @Test
    fun testRequestWithCustomHeaders() {
        val headers = mapOf(
            "User-Agent" to "CurlHttp-Test/1.0",
            "X-Custom-Header" to "test-value"
        )
        val response = CurlHttp.get("https://httpbin.org/headers", headers)
        assertTrue("Request with custom headers should succeed", response.isSuccess)
        assertTrue("Response should contain custom header", response.body.contains("X-Custom-Header"))
    }

    @Test
    fun testRequestWithHttpRequestObject() {
        val request = HttpRequest(
            url = "https://httpbin.org/get",
            method = HttpMethod.GET,
            headers = mapOf("Accept" to "application/json"),
            timeout = 30
        )
        val response = CurlHttp.execute(request)
        assertTrue("Request with HttpRequest object should succeed", response.isSuccess)
    }

    @Test
    fun testAsyncRequest() = runBlocking {
        val request = HttpRequest(
            url = "https://httpbin.org/delay/1",
            method = HttpMethod.GET
        )
        val response = CurlHttp.executeAsync(request)
        assertTrue("Async request should succeed", response.isSuccess)
        assertTrue("Response time should be recorded", response.responseTime > 0)
    }

    @Test
    fun testRequestInterceptor() {
        val interceptor = object : RequestInterceptor {
            override fun intercept(request: HttpRequest): HttpRequest {
                return request.copy(
                    headers = request.headers + ("X-Intercepted" to "true")
                )
            }
        }
        
        CurlHttp.addRequestInterceptor(interceptor)
        
        val response = CurlHttp.get("https://httpbin.org/headers")
        assertTrue("Request with interceptor should succeed", response.isSuccess)
        assertTrue("Response should contain intercepted header", response.body.contains("X-Intercepted"))
        
        CurlHttp.removeRequestInterceptor(interceptor)
    }

    @Test
    fun testResponseInterceptor() {
        val interceptor = object : ResponseInterceptor {
            override fun intercept(response: HttpResponse): HttpResponse {
                return response.copy(
                    headers = response.headers + ("X-Response-Intercepted" to "true")
                )
            }
        }
        
        CurlHttp.addResponseInterceptor(interceptor)
        
        val response = CurlHttp.get("https://httpbin.org/get")
        assertTrue("Request with response interceptor should succeed", response.isSuccess)
        assertTrue("Response should have intercepted header", 
                  response.headers.containsKey("X-Response-Intercepted"))
        
        CurlHttp.removeResponseInterceptor(interceptor)
    }

    @Test
    fun testCacheConfiguration() {
        val cacheConfig = CacheConfig(
            strategy = CacheStrategy.CACHE_FIRST,
            maxAge = 60
        )
        CurlHttp.setDefaultCacheConfig(cacheConfig)
        
        // First request - should hit network
        val response1 = CurlHttp.get("https://httpbin.org/get")
        assertTrue("First request should succeed", response1.isSuccess)
        
        // Second request - should hit cache (if implemented)
        val response2 = CurlHttp.get("https://httpbin.org/get")
        assertTrue("Second request should succeed", response2.isSuccess)
        
        CurlHttp.clearCache()
    }

    @Test
    fun testRetryConfiguration() {
        val retryConfig = RetryConfig(
            maxRetries = 2,
            retryDelay = 100,
            backoffMultiplier = 1.5f
        )
        CurlHttp.setDefaultRetryConfig(retryConfig)
        
        // Test with a URL that might fail occasionally
        try {
            val response = CurlHttp.get("https://httpbin.org/status/500")
            // This might succeed or fail depending on retry logic
        } catch (e: Exception) {
            // Expected for 500 status
            assertTrue("Should be HttpException", e is HttpException)
        }
    }

    @Test
    fun testTimeoutConfiguration() {
        CurlHttp.setDefaultTimeout(5, 2)
        
        val request = HttpRequest(
            url = "https://httpbin.org/delay/10", // 10 second delay
            method = HttpMethod.GET,
            timeout = 3 // 3 second timeout
        )
        
        try {
            CurlHttp.execute(request)
            fail("Request should timeout")
        } catch (e: Exception) {
            // Expected timeout exception
            assertTrue("Should timeout", true)
        }
    }

    @Test
    fun testBackwardCompatibility() {
        // Test old methods still work
        val getResult = CurlHttp.testGet()
        assertTrue("Legacy testGet should work", getResult.isNotEmpty())
        
        val postResult = CurlHttp.testPost()
        assertTrue("Legacy testPost should work", postResult.isNotEmpty())
    }

    @Test
    fun testCacheStats() {
        CurlHttp.clearCache()
        val initialStats = CurlHttp.getCacheStats()
        assertTrue("Cache stats should be available", initialStats.contains("Cache size"))
        
        // Make a request to potentially populate cache
        CurlHttp.get("https://httpbin.org/get")
        
        val afterStats = CurlHttp.getCacheStats()
        assertTrue("Cache stats should be updated", afterStats.contains("Total requests"))
    }

    @Test
    fun testErrorHandling() {
        try {
            CurlHttp.get("https://invalid-url-that-does-not-exist.com")
            fail("Should throw exception for invalid URL")
        } catch (e: Exception) {
            assertTrue("Should handle network errors", true)
        }
    }

    @Test
    fun testHttpMethods() {
        val baseUrl = "https://httpbin.org"
        
        // Test all HTTP methods
        val getResponse = CurlHttp.execute(HttpRequest("$baseUrl/get", HttpMethod.GET))
        assertTrue("GET should work", getResponse.isSuccess)
        
        val postResponse = CurlHttp.execute(HttpRequest("$baseUrl/post", HttpMethod.POST, body = "test"))
        assertTrue("POST should work", postResponse.isSuccess)
        
        val putResponse = CurlHttp.execute(HttpRequest("$baseUrl/put", HttpMethod.PUT, body = "test"))
        assertTrue("PUT should work", putResponse.isSuccess)
        
        val deleteResponse = CurlHttp.execute(HttpRequest("$baseUrl/delete", HttpMethod.DELETE))
        assertTrue("DELETE should work", deleteResponse.isSuccess)
        
        val patchResponse = CurlHttp.execute(HttpRequest("$baseUrl/patch", HttpMethod.PATCH, body = "test"))
        assertTrue("PATCH should work", patchResponse.isSuccess)
    }
}