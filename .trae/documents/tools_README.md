# Android NDK Tools Library

**版本：** v2.0.0  
**更新日期：** 2024年12月  
**适用平台：** Android NDK

## 概述

Android NDK Tools Library 是一个功能强大的Android原生开发工具库，提供网络请求、安全校验、JSON解析等核心功能。所有关键逻辑都在native层实现，确保高性能和安全性。

## 主要功能模块

### 🔒 安全校验模块 (Utils)
- **包名白名单校验**：基于明文白名单的高性能包名验证算法
- **应用签名验证**：验证应用签名的合法性
- **反调试检测**：检测调试器、模拟器、Root环境
- **完整性校验**：防止应用被篡改
- **多层安全防护**：组合多种安全检查机制

### 🌐 网络请求模块 (CurlHttp)
- **完整HTTP客户端**：支持GET、POST、PUT、DELETE等HTTP方法
- **高级功能**：请求/响应拦截器、智能重试、缓存策略
- **异步支持**：基于Kotlin协程的异步请求
- **性能优化**：连接复用、内存管理、性能监控

### 📄 JSON解析模块 (JsonCpp)
- **高性能JSON处理**：基于jsoncpp库的C++JSON解析
- **类型安全**：完整的类型检查和转换
- **内存优化**：高效的内存管理和资源释放

## 快速开始

### 1. 添加依赖

在你的 `build.gradle` 文件中添加：

```gradle
dependencies {
    implementation project(':tools')
}
```

### 2. 初始化库

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化网络库
        CurlHttp.initCurl()
        
        // 执行安全校验
        if (!Utils.strictSecurityCheck(this)) {
            Log.e("Security", "安全校验失败")
            exitProcess(1)
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // 清理资源
        CurlHttp.cleanUp()
    }
}
```

## 安全校验模块详细说明

### 核心特性

#### 明文包名白名单算法
- **高性能**：直接字符串匹配，时间复杂度 O(n*m)
- **易维护**：明文存储，便于调试和维护
- **内存优化**：无加密开销，空间复杂度 O(1)
- **安全可靠**：精确匹配，确保安全性

#### 多层安全防护
1. **包名校验**：验证应用包名是否在预定义白名单中
2. **签名验证**：检查应用签名的合法性
3. **反调试检测**：检测调试器、模拟器、Root环境
4. **完整性校验**：防止应用被篡改

### API接口

#### 基础校验方法

```kotlin
// 执行完整安全校验
try {
    val result = Utils.performSecurityCheck(context)
    Log.d("Security", "校验通过: ${result.message}")
} catch (e: Utils.SecurityException) {
    Log.e("Security", "校验失败: ${e.message}")
    handleSecurityFailure(e.result)
}

// 安全校验（不抛异常）
val result = Utils.performSecurityCheckSafe(context)
when (result) {
    Utils.SecurityCheckResult.SUCCESS -> {
        // 校验成功
    }
    Utils.SecurityCheckResult.PACKAGE_NOT_ALLOWED -> {
        // 包名不在白名单中
    }
    // ... 其他情况
}
```

#### 包名校验方法

```kotlin
// 检查指定包名
val isAllowed = Utils.isPackageAllowed("com.example.app")

// 检查当前应用包名
val isCurrentAllowed = Utils.isCurrentPackageAllowed(context)

// 获取所有允许的包名
val allowedPackages = Utils.getAllowedPackages()
```

#### 便捷校验方法

```kotlin
// 快速校验（基础检查）
if (Utils.quickSecurityCheck(context)) {
    // 校验通过
}

// 严格校验（包含反调试）
if (Utils.strictSecurityCheck(context)) {
    // 所有检查通过
}

// 获取详细安全状态
val status = Utils.getSecurityStatus(context)
Log.d("Security", "安全状态: $status")
```

### 包名白名单配置

当前预配置的白名单包名：

```cpp
static const char* PACKAGE_WHITELIST[] = {
    "me.shetj.sdk.ffmepg.demo",
    "me.shetj.sdk.ffmepg.demo.test", 
    "me.shetj.sdk.ffmepg.demo.dev",
    nullptr
};
```

**修改白名单：**
1. 编辑 `utils.cpp` 中的 `PACKAGE_WHITELIST` 数组
2. 重新编译SO库
3. 测试验证新配置

## 网络请求模块详细说明

### 核心特性

- **多HTTP方法支持**：GET、POST、PUT、DELETE、HEAD、PATCH、OPTIONS
- **请求/响应拦截器**：支持自定义请求和响应处理
- **智能重试机制**：支持指数退避的重试策略
- **多种缓存策略**：CACHE_FIRST、NETWORK_FIRST等
- **异步请求支持**：基于Kotlin协程
- **连接池管理**：自动连接复用和管理
- **性能监控**：请求统计和性能分析

### API接口

#### 基础用法

```kotlin
// 初始化
CurlHttp.initCurl()

// GET请求
val response = CurlHttp.get("https://api.example.com/data")
println("Response: ${response.body}")

// POST JSON请求
val json = """{"name": "John", "age": 30}"""
val response = CurlHttp.postJson("https://api.example.com/users", json)

// 带自定义头部
val headers = mapOf("Authorization" to "Bearer token")
val response = CurlHttp.get("https://api.example.com/data", headers)
```

#### 高级用法

```kotlin
// 使用HttpRequest对象
val request = HttpRequest(
    url = "https://api.example.com/data",
    method = HttpMethod.GET,
    headers = mapOf("User-Agent" to "MyApp/1.0"),
    timeout = 30,
    connectTimeout = 10,
    ignoreSSL = false
)

val response = CurlHttp.execute(request)

// 异步请求
lifecycleScope.launch {
    try {
        val response = CurlHttp.executeAsync(request)
        // 处理响应
    } catch (e: Exception) {
        // 处理错误
    }
}
```

#### 拦截器配置

```kotlin
// 请求拦截器
val requestInterceptor = object : RequestInterceptor {
    override fun intercept(request: HttpRequest): HttpRequest {
        return request.copy(
            headers = request.headers + ("X-API-Key" to "your-api-key")
        )
    }
}
CurlHttp.addRequestInterceptor(requestInterceptor)

// 响应拦截器
val responseInterceptor = object : ResponseInterceptor {
    override fun intercept(response: HttpResponse): HttpResponse {
        Log.d("CurlHttp", "Response time: ${response.responseTime}ms")
        return response
    }
}
CurlHttp.addResponseInterceptor(responseInterceptor)
```

#### 重试和缓存配置

```kotlin
// 重试配置
val retryConfig = RetryConfig(
    maxRetries = 3,
    retryDelay = 1000,
    backoffMultiplier = 2.0f,
    retryOnConnectionFailure = true,
    retryOnTimeout = true
)
CurlHttp.setDefaultRetryConfig(retryConfig)

// 缓存配置
val cacheConfig = CacheConfig(
    strategy = CacheStrategy.CACHE_FIRST,
    maxAge = 300, // 5分钟
    maxSize = 10 * 1024 * 1024 // 10MB
)
CurlHttp.setDefaultCacheConfig(cacheConfig)
```

## JSON解析模块详细说明

### 核心特性

基于jsoncpp库的高性能C++ JSON解析器：

- **完整JSON支持**：支持所有JSON数据类型
- **类型安全**：严格的类型检查和转换
- **高性能**：原生C++实现，性能优异
- **内存安全**：自动内存管理，防止泄漏

### 使用方法

```cpp
#include "json/json.h"

// 解析JSON字符串
Json::Value root;
Json::Reader reader;
bool parsingSuccessful = reader.parse(jsonString, root);

if (parsingSuccessful) {
    // 访问JSON数据
    std::string name = root["name"].asString();
    int age = root["age"].asInt();
    
    // 遍历数组
    const Json::Value& array = root["items"];
    for (int i = 0; i < array.size(); i++) {
        std::string item = array[i].asString();
    }
}

// 生成JSON
Json::Value data;
data["name"] = "John";
data["age"] = 30;
data["active"] = true;

Json::StreamWriterBuilder builder;
std::string jsonString = Json::writeString(builder, data);
```

## 错误处理和调试

### 安全校验错误处理

```kotlin
fun handleSecurityFailure(result: Utils.SecurityCheckResult) {
    when (result) {
        Utils.SecurityCheckResult.PACKAGE_NOT_ALLOWED -> {
            Log.e("Security", "包名不在白名单中")
            // 处理包名校验失败
        }
        Utils.SecurityCheckResult.SIGNATURE_MISMATCH -> {
            Log.e("Security", "签名验证失败")
            // 处理签名校验失败
        }
        Utils.SecurityCheckResult.ANTI_DEBUG_DETECTED -> {
            Log.e("Security", "检测到调试环境")
            // 处理反调试检测
        }
        else -> {
            Log.e("Security", "未知安全错误")
        }
    }
}
```

### 网络请求错误处理

```kotlin
try {
    val response = CurlHttp.get("https://api.example.com/data")
    if (response.isSuccess) {
        // 处理成功响应
        println("Success: ${response.body}")
    } else {
        // 处理HTTP错误
        println("HTTP Error: ${response.statusCode} - ${response.statusMessage}")
    }
} catch (e: HttpException) {
    // 处理HTTP异常
    println("HTTP Exception: ${e.statusCode} - ${e.message}")
} catch (e: Exception) {
    // 处理其他异常
    println("Error: ${e.message}")
}
```

### 调试技巧

```kotlin
// 启用详细日志
val status = Utils.getSecurityStatus(context)
status.forEach { (key, value) ->
    Log.d("Debug", "$key: $value")
}

// 网络请求统计
val cacheStats = CurlHttp.getCacheStats()
Log.d("Debug", "Cache stats: $cacheStats")

// 分步校验
Log.d("Debug", "包名校验: ${Utils.isCurrentPackageAllowed(context)}")
Log.d("Debug", "完整性校验: ${Utils.verifyIntegrity(context)}")
Log.d("Debug", "反调试检测: ${Utils.detectAntiDebug()}")
```

## 性能优化建议

### 安全校验优化

1. **合理使用校验频率**：避免过于频繁的安全校验
2. **选择合适的校验级别**：根据场景选择快速或严格校验
3. **缓存校验结果**：对于短时间内的重复校验，可以缓存结果

### 网络请求优化

1. **使用连接复用**：避免频繁创建连接
2. **合理配置超时**：根据网络环境调整超时时间
3. **启用缓存**：对于重复请求启用缓存机制
4. **使用异步请求**：避免阻塞主线程

## 依赖要求

- **Android API Level**: 21+
- **NDK版本**: r21+
- **Kotlin版本**: 1.8+
- **Gradle版本**: 7.0+

## 外部依赖

- **libcurl**: 网络请求库
- **jsoncpp**: JSON解析库
- **openssl**: 加密和哈希计算

## 注意事项

⚠️ **重要提醒：**

1. **线程安全**：所有API都是线程安全的，可在多线程环境使用
2. **内存管理**：库会自动管理内存，但建议在应用退出时调用清理方法
3. **网络权限**：确保在AndroidManifest.xml中添加网络权限
4. **安全配置**：生产环境部署前务必充分测试安全功能
5. **版本兼容**：确保SO库版本与API版本匹配

## 版本更新日志

### v2.0.0 (2024年12月)
- 🔄 **重大更新**：将XOR加密算法替换为明文包名白名单算法
- ✨ **新增功能**：完整的安全校验API体系
- 🚀 **性能优化**：明文白名单算法，性能提升显著
- 🛡️ **安全增强**：多层安全防护机制
- 📚 **文档完善**：全面更新API文档和使用指南
- 🔧 **API重构**：CurlHttp类提供更完整的HTTP客户端功能
- 🧪 **测试完善**：添加完整的单元测试和集成测试

### v1.0.0
- 基础HTTP请求功能
- 简单的包名和签名校验
- JSON解析支持

## 技术支持

如果在使用过程中遇到问题，请：

1. 查阅本文档的相关章节
2. 检查日志输出获取详细错误信息
3. 确认版本兼容性
4. 联系技术支持团队

## 许可证

本项目采用 MIT 许可证，详见 LICENSE 文件。

---

*本文档持续更新中，如有疑问请及时反馈。*