# Android NDK Tools Library

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-v2.0.0-blue.svg)]()

**更新日期：** 2024年12月  
**适用平台：** Android NDK

## 📌 概述

Android NDK Tools Library 是一个功能强大的 Android 原生开发工具库，提供网络请求、安全校验、JSON解析等核心功能。所有关键逻辑都在 Native (C/C++) 层实现，依托 NDK Prefab 引入三方库，确保高性能和防逆向安全性。

## ✨ 主要功能模块

### 🔒 安全校验模块 (Utils)
- **包名白名单校验**：基于明文白名单的高性能包名验证算法，避免被 Java 层 Hook。
- **应用签名验证**：在 Native 层读取并验证应用签名的合法性。
- **反调试检测**：检测调试器附加、模拟器环境、Root 环境。
- **完整性校验**：防止应用被二次打包篡改。

### 🌐 网络请求模块 (CurlHttp)
- **完整HTTP客户端**：基于 `libcurl`，支持 GET、POST、PUT、DELETE 等所有 HTTP 方法。
- **高级功能**：支持请求/响应拦截器、智能重试、缓存策略。
- **异步支持**：结合 Kotlin 协程，提供非阻塞异步请求。
- **性能优化**：连接池复用、内存管理、性能状态监控。

### 📄 JSON解析模块 (JsonCpp)
- **高性能JSON处理**：基于 `jsoncpp` 的原生 C++ 解析器，速度远超 Java 层解析。
- **类型安全**：严格的类型检查和安全转换。
- **内存优化**：高效的内存管理和资源释放。

## 🚀 快速开始

### 1. 初始化库

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化底层网络库
        CurlHttp.initCurl()
        
        // 执行安全校验
        if (!Utils.strictSecurityCheck(this)) {
            Log.e("Security", "安全校验失败，应用可能被篡改")
            exitProcess(1)
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // 清理 C++ 资源
        CurlHttp.cleanUp()
    }
}
```

### 2. 发起网络请求

```kotlin
// 简单的 GET 请求
val response = CurlHttp.get("https://api.example.com/data")
println("Response: ${response.body}")

// 发送 POST JSON 请求
val json = """{"name": "John", "age": 30}"""
val response = CurlHttp.postJson("https://api.example.com/users", json)

// 异步协程请求
lifecycleScope.launch {
    val request = HttpRequest(url = "https://api.example.com/data", method = HttpMethod.GET)
    try {
        val result = CurlHttp.executeAsync(request)
        // 处理成功
    } catch (e: Exception) {
        // 处理异常
    }
}
```

## 🛠 安全校验配置与使用

### 包名白名单配置
底层白名单配置在 `utils.cpp` 中，修改后需重新编译：
```cpp
static const char* PACKAGE_WHITELIST[] = {
    "me.shetj.sdk.ffmepg.demo",
    "me.shetj.sdk.ffmepg.demo.test", 
    nullptr
};
```

### 执行校验
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

## 📦 依赖要求

- **Android API Level**: 21+
- **NDK 版本**: r21+
- **Kotlin 版本**: 1.8+

## 📚 外部原生依赖
本模块通过 NDK `prefab` 特性集成了以下第三方库：
- **libcurl** (7.69.1): 网络请求库
- **jsoncpp** (1.8.4): JSON 解析库
- **openssl** (1.1.1g): 加密和 SSL 握手支持

## ⚠️ 注意事项

1. **线程安全**：大部分 API 是线程安全的，可在多线程环境使用，但 `CUrlKit` 内部的单例生命周期需要留意。
2. **网络权限**：请确保在 `AndroidManifest.xml` 中添加 `<uses-permission android:name="android.permission.INTERNET" />`。
3. **安全配置**：生产环境部署前务必充分测试包名和签名的安全校验逻辑。
