# SO库安全校验功能集成指南

## 概述

本文档详细介绍如何集成和使用SO库的包名白名单校验功能。该功能提供了多层安全防护，包括包名白名单校验、签名验证、反调试检测等，所有关键校验逻辑都在native层实现，确保最高级别的安全性。

**版本：** v2.0.0  
**更新日期：** 2024年12月  
**适用平台：** Android NDK

## 功能特性

### 🔒 核心安全功能
- **包名白名单校验**：只允许预定义的包名访问SO库
- **应用签名验证**：验证应用签名的合法性
- **反调试检测**：检测调试器、模拟器、Root环境
- **完整性校验**：防止应用被篡改
- **安全token机制**：动态生成和验证安全token

### 🛡️ 安全防护措施
- **字符串混淆**：关键字符串在native层加密存储
- **反调试保护**：检测并阻止动态调试
- **防篡改机制**：检测应用完整性
- **多层校验**：组合多种安全检查机制

## 快速开始

### 1. 基础集成

```kotlin
import me.shetj.sdk.utils.Utils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // 执行完整的安全校验
            val result = Utils.performSecurityCheck(this)
            Log.d("Security", "安全校验通过: ${result.message}")
            
            // 继续应用逻辑
            initializeApp()
            
        } catch (e: Utils.SecurityException) {
            Log.e("Security", "安全校验失败: ${e.message}")
            // 处理安全校验失败的情况
            handleSecurityFailure(e.result)
        }
    }
}
```

### 2. 简单校验

```kotlin
// 快速校验（不包括反调试检测）
if (Utils.quickSecurityCheck(this)) {
    // 校验通过，继续执行
    proceedWithNormalFlow()
} else {
    // 校验失败，处理异常情况
    handleSecurityFailure()
}
```

### 3. 严格校验

```kotlin
// 严格校验（包括所有安全检查）
if (Utils.strictSecurityCheck(this)) {
    // 所有安全检查都通过
    proceedWithSecureFlow()
} else {
    // 存在安全风险
    handleSecurityRisk()
}
```

## API 详细说明

### 核心校验方法

#### `performSecurityCheck(context: Context): SecurityCheckResult`
执行完整的安全校验，包括所有安全检查项。

**参数：**
- `context`: Android上下文

**返回值：**
- `SecurityCheckResult`: 校验结果枚举

**异常：**
- `SecurityException`: 当校验失败时抛出

**示例：**
```kotlin
try {
    val result = Utils.performSecurityCheck(this)
    when (result) {
        Utils.SecurityCheckResult.SUCCESS -> {
            // 校验成功
        }
    }
} catch (e: Utils.SecurityException) {
    when (e.result) {
        Utils.SecurityCheckResult.PACKAGE_NOT_ALLOWED -> {
            // 包名不在白名单中
        }
        Utils.SecurityCheckResult.SIGNATURE_MISMATCH -> {
            // 签名不匹配
        }
        Utils.SecurityCheckResult.ANTI_DEBUG_DETECTED -> {
            // 检测到调试环境
        }
        // ... 其他情况
    }
}
```

#### `performSecurityCheckSafe(context: Context): SecurityCheckResult`
执行安全校验但不抛出异常。

**示例：**
```kotlin
val result = Utils.performSecurityCheckSafe(this)
when (result) {
    Utils.SecurityCheckResult.SUCCESS -> {
        // 校验成功
    }
    Utils.SecurityCheckResult.PACKAGE_NOT_ALLOWED -> {
        // 处理包名不在白名单的情况
    }
    // ... 其他情况
}
```

### 包名校验方法

#### `isPackageAllowed(packageName: String): Boolean`
检查指定包名是否在白名单中。

**示例：**
```kotlin
val isAllowed = Utils.isPackageAllowed("com.example.myapp")
if (isAllowed) {
    // 包名在白名单中
} else {
    // 包名不在白名单中
}
```

#### `isCurrentPackageAllowed(): Boolean`
检查当前应用包名是否在白名单中。

**示例：**
```kotlin
if (Utils.isCurrentPackageAllowed()) {
    // 当前应用在白名单中
} else {
    // 当前应用不在白名单中
}
```

### 安全检测方法

#### `detectAntiDebug(): Boolean`
检测反调试环境。

**示例：**
```kotlin
if (Utils.detectAntiDebug()) {
    // 检测到调试环境，采取保护措施
    Log.w("Security", "检测到调试环境")
    // 可以选择退出应用或采取其他保护措施
} else {
    // 正常环境
}
```

#### `verifyIntegrity(context: Context): Boolean`
验证应用完整性。

**示例：**
```kotlin
if (Utils.verifyIntegrity(this)) {
    // 应用完整性校验通过
} else {
    // 应用可能被篡改
    Log.e("Security", "应用完整性校验失败")
}
```

### Token机制

#### `generateSecurityToken(context: Context): String?`
生成安全token。

**示例：**
```kotlin
val token = Utils.generateSecurityToken(this)
if (token != null) {
    // 保存token用于后续验证
    saveTokenForLaterUse(token)
} else {
    // token生成失败
}
```

#### `validateSecurityToken(token: String, context: Context): Boolean`
验证安全token。

**示例：**
```kotlin
val savedToken = getSavedToken()
if (Utils.validateSecurityToken(savedToken, this)) {
    // token有效
} else {
    // token无效或已过期
}
```

### 便捷方法

#### `getSecurityStatus(context: Context): Map<String, Any>`
获取详细的安全状态信息。

**示例：**
```kotlin
val status = Utils.getSecurityStatus(this)
Log.d("Security", "包名: ${status["packageName"]}")
Log.d("Security", "包名是否允许: ${status["packageAllowed"]}")
Log.d("Security", "完整性校验: ${status["integrityValid"]}")
Log.d("Security", "反调试检测: ${status["antiDebugDetected"]}")
Log.d("Security", "允许的包名列表: ${status["allowedPackages"]}")
Log.d("Security", "安全校验结果: ${status["securityCheckResult"]}")
```

## 配置说明

### 包名白名单配置

包名白名单在native层硬编码，需要在编译时确定。默认配置的白名单包名包括：

```cpp
// 在utils.cpp中的加密白名单
static const char* ENCRYPTED_WHITELIST[] = {
    // 加密后的包名，实际使用时需要解密
    "encrypted_package_name_1",
    "encrypted_package_name_2",
    // ... 更多包名
};
```

### 自定义白名单

如需修改白名单，请按以下步骤操作：

1. **修改native代码**：在 `utils.cpp` 中更新 `ENCRYPTED_WHITELIST` 数组
2. **重新编译**：重新编译SO库
3. **测试验证**：确保新的包名能够通过校验

**注意：** 生产环境中的包名应该加密存储，避免被轻易发现和修改。

## 安全最佳实践

### 1. 应用启动时校验

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 应用启动时立即进行安全校验
        if (!Utils.strictSecurityCheck(this)) {
            // 安全校验失败，退出应用
            Log.e("Security", "安全校验失败，应用将退出")
            exitProcess(1)
        }
    }
}
```

### 2. 关键操作前校验

```kotlin
fun performCriticalOperation() {
    // 在执行关键操作前再次校验
    if (!Utils.quickSecurityCheck(this)) {
        throw SecurityException("安全校验失败，无法执行关键操作")
    }
    
    // 执行关键操作
    doCriticalWork()
}
```

### 3. 定期校验

```kotlin
class SecurityChecker {
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 30000L // 30秒
    
    private val securityCheckRunnable = object : Runnable {
        override fun run() {
            if (!Utils.quickSecurityCheck(context)) {
                // 处理安全校验失败
                handleSecurityFailure()
            }
            // 安排下次检查
            handler.postDelayed(this, checkInterval)
        }
    }
    
    fun startPeriodicCheck() {
        handler.post(securityCheckRunnable)
    }
    
    fun stopPeriodicCheck() {
        handler.removeCallbacks(securityCheckRunnable)
    }
}
```

### 4. 错误处理策略

```kotlin
fun handleSecurityFailure(result: Utils.SecurityCheckResult) {
    when (result) {
        Utils.SecurityCheckResult.PACKAGE_NOT_ALLOWED -> {
            // 包名不在白名单中 - 可能是盗版应用
            showErrorDialog("应用验证失败，请从官方渠道下载")
            exitProcess(1)
        }
        Utils.SecurityCheckResult.SIGNATURE_MISMATCH -> {
            // 签名不匹配 - 应用被重新打包
            showErrorDialog("应用完整性验证失败")
            exitProcess(1)
        }
        Utils.SecurityCheckResult.ANTI_DEBUG_DETECTED -> {
            // 检测到调试环境
            showErrorDialog("检测到不安全的运行环境")
            exitProcess(1)
        }
        Utils.SecurityCheckResult.TAMPER_DETECTED -> {
            // 检测到篡改
            showErrorDialog("应用被篡改，存在安全风险")
            exitProcess(1)
        }
        else -> {
            // 其他未知错误
            showErrorDialog("安全校验失败")
            exitProcess(1)
        }
    }
}
```

## 测试指南

### 1. 基础功能测试

```kotlin
@Test
fun testBasicSecurity() {
    // 测试包名校验
    assertTrue(Utils.isCurrentPackageAllowed())
    
    // 测试完整性校验
    assertTrue(Utils.verifyIntegrity(context))
    
    // 测试安全校验
    val result = Utils.performSecurityCheckSafe(context)
    assertEquals(Utils.SecurityCheckResult.SUCCESS, result)
}
```

### 2. 白名单测试

```kotlin
@Test
fun testWhitelist() {
    // 测试已知的合法包名
    assertTrue(Utils.isPackageAllowed("com.example.allowed"))
    
    // 测试不在白名单中的包名
    assertFalse(Utils.isPackageAllowed("com.example.notallowed"))
    
    // 获取所有允许的包名
    val allowedPackages = Utils.getAllowedPackages()
    assertTrue(allowedPackages.isNotEmpty())
}
```

### 3. Token机制测试

```kotlin
@Test
fun testTokenMechanism() {
    // 生成token
    val token = Utils.generateSecurityToken(context)
    assertNotNull(token)
    
    // 验证token
    assertTrue(Utils.validateSecurityToken(token!!, context))
    
    // 验证无效token
    assertFalse(Utils.validateSecurityToken("invalid_token", context))
}
```

## 故障排除

### 常见问题

#### 1. 校验失败：包名不在白名单中
**原因：** 当前应用的包名不在预定义的白名单中。
**解决方案：** 
- 检查应用的包名是否正确
- 确认白名单配置是否包含当前包名
- 重新编译SO库并更新白名单

#### 2. 校验失败：签名不匹配
**原因：** 应用签名与预期不符。
**解决方案：**
- 确认使用正确的签名文件
- 检查是否使用了debug签名而非release签名
- 更新native层的签名校验逻辑

#### 3. 校验失败：检测到调试环境
**原因：** 在调试环境中运行应用。
**解决方案：**
- 在正式环境中测试
- 临时禁用反调试检测（仅用于开发测试）

#### 4. SO库加载失败
**原因：** SO库文件缺失或架构不匹配。
**解决方案：**
- 确认SO库文件已正确打包到APK中
- 检查目标设备的CPU架构是否支持
- 重新编译对应架构的SO库

### 调试技巧

#### 1. 启用详细日志

```kotlin
// 获取详细的安全状态信息
val status = Utils.getSecurityStatus(this)
status.forEach { (key, value) ->
    Log.d("SecurityDebug", "$key: $value")
}
```

#### 2. 分步校验

```kotlin
// 分别测试各个校验项
Log.d("Debug", "包名校验: ${Utils.isCurrentPackageAllowed()}")
Log.d("Debug", "完整性校验: ${Utils.verifyIntegrity(this)}")
Log.d("Debug", "反调试检测: ${Utils.detectAntiDebug()}")
```

## 版本更新说明

### v2.0.0
- 新增包名白名单校验功能
- 添加多层安全防护机制
- 实现安全token机制
- 增强反调试和防篡改能力
- 提供详细的API文档和使用指南

## 技术支持

如果在集成过程中遇到问题，请：

1. 查阅本文档的故障排除部分
2. 检查日志输出获取详细错误信息
3. 确认SO库版本与API版本匹配
4. 联系技术支持团队

## 注意事项

⚠️ **重要提醒：**

1. **生产环境部署前务必充分测试**
2. **包名白名单一旦确定，修改需要重新编译SO库**
3. **不要在日志中输出敏感的安全信息**
4. **定期更新安全策略以应对新的安全威胁**
5. **建议结合服务端校验实现双重保护**

---

*本文档持续更新中，如有疑问请及时反馈。*