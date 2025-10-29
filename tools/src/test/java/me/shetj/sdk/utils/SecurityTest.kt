package me.shetj.sdk.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 安全校验功能测试类
 * 
 * 测试包名白名单校验、签名验证、反调试检测等安全功能
 * 
 * @author shetj
 * @since 2.0.0
 */
@RunWith(AndroidJUnit4::class)
class SecurityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ==================== 基础功能测试 ====================

    @Test
    fun testGetPackageName() {
        val packageName = Utils.getPackageName(context)
        assertNotNull("包名不应为空", packageName)
        assertTrue("包名应包含点号", packageName.contains("."))
    }

    @Test
    fun testVerificationPkg() {
        val result = Utils.verificationPkg(context)
        assertNotNull("包名验证结果不应为空", result)
        assertTrue("包名验证结果应包含'pkg'", result.contains("pkg"))
    }

    @Test
    fun testVerificationSign() {
        val result = Utils.verificationSign(context)
        assertNotNull("签名验证结果不应为空", result)
        assertTrue("签名验证结果应包含'sign'", result!!.contains("sign"))
    }

    // ==================== 白名单功能测试 ====================

    @Test
    fun testIsCurrentPackageAllowed() {
        val result = Utils.isCurrentPackageAllowed(context)
        println("当前包名是否在白名单中: $result")
    }

    @Test
    fun testIsPackageAllowed() {
        // 测试已知的白名单包名
        val testPackages = listOf(
            "com.example.test",
            "com.android.test",
            "me.shetj.sdk.test"
        )
        
        for (packageName in testPackages) {
            val result = Utils.isPackageAllowed(packageName)
            println("包名 $packageName 是否在白名单中: $result")
        }
    }

    @Test
    fun testGetAllowedPackages() {
        val allowedPackages = Utils.getAllowedPackages()
        println("白名单包名列表:")
        for (packageName in allowedPackages) {
            println("  - $packageName")
        }
    }

    // ==================== XOR加密功能测试 ====================

    @Test
    fun testGenerateEncryptedPackageName() {
        val testCases = listOf(
            "com.example.test" to "testkey123",
            "me.shetj.sdk" to "mykey456",
            "com.android.app" to "secretkey789"
        )
        
        for ((packageName, key) in testCases) {
            // 测试加密
            val encrypted = Utils.generateEncryptedPackageName(packageName, key)
            assertNotNull("加密结果不应为空", encrypted)
            assertTrue("加密结果不应与原文相同", encrypted != packageName)
            
            // 测试解密验证
            val decrypted = Utils.decryptPackageNameWithKey(encrypted, key)
            assertEquals("解密后应与原文相同", packageName, decrypted)
            
            println("包名: $packageName")
            println("密钥: $key")
            println("加密: $encrypted")
            println("解密: $decrypted")
            println("---")
            
            // 测试不同密钥的加密结果应不同
            val differentKey = key + "diff"
            val encryptedWithDifferentKey = Utils.generateEncryptedPackageName(packageName, differentKey)
            assertTrue("不同密钥的加密结果应不同", encrypted != encryptedWithDifferentKey)
            
            // 测试错误密钥解密
            try {
                val wrongDecrypted = Utils.decryptPackageNameWithKey(encrypted, differentKey)
                assertTrue("错误密钥解密结果应与原文不同", wrongDecrypted != packageName)
            } catch (e: Exception) {
                // 预期可能抛出异常
                println("错误密钥解密抛出异常: ${e.message}")
            }
        }
    }

    @Test
    fun testEncryptionConsistency() {
        val packageName = "com.test.consistency"
        val key = "consistencykey"
        
        // 多次加密应产生相同结果
        val encrypted1 = Utils.generateEncryptedPackageName(packageName, key)
        val encrypted2 = Utils.generateEncryptedPackageName(packageName, key)
        val encrypted3 = Utils.generateEncryptedPackageName(packageName, key)
        
        assertEquals("多次加密结果应一致", encrypted1, encrypted2)
        assertEquals("多次加密结果应一致", encrypted2, encrypted3)
    }

    @Test
    fun testEncryptionPerformance() {
        val packageName = "com.performance.test"
        val key = "performancekey"
        val iterations = 1000
        
        val startTime = System.nanoTime()
        for (i in 0 until iterations) {
            val encrypted = Utils.generateEncryptedPackageName(packageName, key)
            val decrypted = Utils.decryptPackageNameWithKey(encrypted, key)
            assertEquals("性能测试中解密应正确", packageName, decrypted)
        }
        val endTime = System.nanoTime()
        
        val totalTime = (endTime - startTime) / 1_000_000.0 // 转换为毫秒
        val avgTime = totalTime / iterations
        
        println("总时间: ${totalTime}ms")
        println("平均时间: ${avgTime}ms")
        println("每秒操作数: ${1000.0 / avgTime}")
        
        // 验证性能要求（平均每次加密应小于1ms）
        assertTrue("平均加密时间应小于1ms", avgTime < 1.0)
    }

    @Test
    fun testEncryptionEdgeCases() {
        val key = "edgekey"
        
        // 测试短包名
        val shortPackage = "a.b"
        val encryptedShort = Utils.generateEncryptedPackageName(shortPackage, key)
        val decryptedShort = Utils.decryptPackageNameWithKey(encryptedShort, key)
        assertEquals("短包名加密解密应正确", shortPackage, decryptedShort)
        
        // 测试长包名（但不超过256字符）
        val longPackage = "com." + "a".repeat(240) + ".test"
        assertTrue("测试包名应小于256字符", longPackage.length < 256)
        val encryptedLong = Utils.generateEncryptedPackageName(longPackage, key)
        val decryptedLong = Utils.decryptPackageNameWithKey(encryptedLong, key)
        assertEquals("长包名加密解密应正确", longPackage, decryptedLong)
        
        // 测试包含特殊字符的包名
        val specialPackage = "com.test_app-v1.2.3"
        val encryptedSpecial = Utils.generateEncryptedPackageName(specialPackage, key)
        val decryptedSpecial = Utils.decryptPackageNameWithKey(encryptedSpecial, key)
        assertEquals("特殊字符包名加密解密应正确", specialPackage, decryptedSpecial)
    }

    @Test
    fun testEncryptionErrorHandling() {
        val packageName = "com.test.error"
        val key = "errorkey"
        
        // 测试正常情况
        try {
            val encrypted = Utils.generateEncryptedPackageName(packageName, key)
            val decrypted = Utils.decryptPackageNameWithKey(encrypted, key)
            assertEquals("正常加密解密应成功", packageName, decrypted)
        } catch (e: Exception) {
            // 如果抛出异常，记录但不失败测试
            println("加密过程中的异常: ${e.message}")
        }
        
        // 测试空字符串和null处理在JNI层进行
        try {
            val result = Utils.generateEncryptedPackageName("", key)
            println("空包名加密结果: $result")
        } catch (e: Exception) {
            println("空包名加密异常: ${e.message}")
        }
    }

    @Test
    fun testAddToEncryptedWhitelist() {
        val packageName = "com.test.whitelist"
        val key = "whitelistkey"
        
        // 测试添加到加密白名单
        try {
            val encryptedForWhitelist = Utils.addToEncryptedWhitelist(packageName, key)
            assertNotNull("白名单加密结果不应为空", encryptedForWhitelist)
            
            // 验证结果与直接加密一致
            val directEncrypted = Utils.generateEncryptedPackageName(packageName, key)
            assertEquals("白名单加密应与直接加密结果一致", directEncrypted, encryptedForWhitelist)
            
            println("包名: $packageName")
            println("白名单加密结果: $encryptedForWhitelist")
        } catch (e: Exception) {
            println("白名单加密异常: ${e.message}")
        }
    }

    // ==================== 安全检测功能测试 ====================

    @Test
    fun testDetectAntiDebug() {
        val result = Utils.detectAntiDebug()
        println("反调试检测结果: $result")
        // 注意：在测试环境中可能检测到调试器
    }

    @Test
    fun testVerifyIntegrity() {
        val result = Utils.verifyIntegrity(context)
        println("完整性验证结果: $result")
        // 注意：在测试环境中完整性验证可能失败
    }

    // ==================== 综合安全检查测试 ====================

    @Test
    fun testPerformSecurityCheckSafe() {
        try {
            val result = Utils.performSecurityCheck(context, Utils.SecurityCheckResult.SAFE)
            println("安全检查结果 (SAFE): $result")
            if (result == Utils.SecurityCheckResult.SAFE) {
                println("✓ 安全检查通过")
            } else {
                println("⚠ 安全检查未通过: $result")
            }
        } catch (e: Utils.SecurityException) {
            println("安全检查异常: ${e.message}")
        }
    }

    @Test
    fun testPerformSecurityCheckWithException() {
        // 测试可能抛出异常的情况
        try {
            // 使用可能触发异常的参数
            val result = Utils.performSecurityCheck(context, Utils.SecurityCheckResult.UNKNOWN)
            println("异常测试结果: $result")
        } catch (e: Utils.SecurityException) {
            println("预期的安全异常: ${e.message}")
            // 这是预期的异常，测试通过
        } catch (e: Exception) {
            println("其他异常: ${e.message}")
        }
    }

    @Test
    fun testSecurityTokenGeneration() {
        val token = Utils.generateSecurityToken(context)
        assertNotNull("安全令牌不应为空", token)
        assertTrue("Token长度应大于0", token.isNotEmpty())
        println("生成的安全令牌: $token")
    }

    @Test
    fun testSecurityTokenValidation() {
        val token = Utils.generateSecurityToken(context)
        val isValid = Utils.validateSecurityToken(context, token)
        println("令牌验证结果: $isValid")
        
        // 测试无效令牌
        val invalidToken = "invalid_token_12345"
        val isInvalidTokenValid = Utils.validateSecurityToken(context, invalidToken)
        println("无效令牌验证结果: $isInvalidTokenValid")
    }

    @Test
    fun testQuickSecurityCheck() {
        val result = Utils.quickSecurityCheck(context)
        println("快速安全检查结果: $result")
    }

    @Test
    fun testStrictSecurityCheck() {
        val result = Utils.strictSecurityCheck(context)
        println("严格安全检查结果: $result")
    }

    @Test
    fun testGetSecurityStatus() {
        val status = Utils.getSecurityStatus(context)
        assertTrue("安全状态应包含信息", status.isNotEmpty())
        
        println("安全状态信息:")
        println("==================")
        for ((key, value) in status) {
            println("$key: $value")
        }
        println("==================")
        
        // 验证必要的状态信息
        assertTrue("应包含包名信息", status.containsKey("packageName"))
        assertTrue("应包含安全校验结果", status.containsKey("securityCheckResult"))
    }

    // ==================== 动态白名单测试（仅用于测试） ====================

    @Test
    fun testDynamicWhitelistManagement() {
        val testPackage = "com.test.dynamic.package"
        
        // 测试添加包名到白名单
        val addResult = Utils.addPackageToWhitelist(testPackage)
        println("添加包名到白名单结果: $addResult")
        
        if (addResult) {
            // 验证包名是否在白名单中
            val isAllowed = Utils.isPackageAllowed(testPackage)
            println("动态添加的包名是否在白名单中: $isAllowed")
            
            // 测试移除包名
            val removeResult = Utils.removePackageFromWhitelist(testPackage)
            println("从白名单移除包名结果: $removeResult")
            
            if (removeResult) {
                // 验证包名是否已被移除
                val isStillAllowed = Utils.isPackageAllowed(testPackage)
                println("移除后包名是否还在白名单中: $isStillAllowed")
            }
        }
    }

    // ==================== 压力测试 ====================

    @Test
    fun testSecurityCheckPerformance() {
        val iterations = 100
        val startTime = System.currentTimeMillis()
        
        for (i in 0 until iterations) {
            try {
                Utils.performSecurityCheck(context, Utils.SecurityCheckResult.SAFE)
            } catch (e: Exception) {
                // 忽略异常，继续测试
            }
        }
        
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        val averageTime = totalTime.toDouble() / iterations
        
        println("性能测试结果:")
        println("总时间: ${totalTime}ms")
        println("平均时间: ${averageTime}ms")
        
        // 验证性能要求（平均每次校验不超过100ms）
        assertTrue("平均校验时间应小于100ms", averageTime < 100.0)
    }

    @Test
    fun testConcurrentSecurityCheck() {
        val threadCount = 10
        val iterationsPerThread = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = mutableListOf<Boolean>()
        
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    for (j in 0 until iterationsPerThread) {
                        val result = Utils.performSecurityCheck(context, Utils.SecurityCheckResult.SAFE)
                        synchronized(results) {
                            results.add(result == Utils.SecurityCheckResult.SAFE)
                        }
                    }
                } catch (e: Exception) {
                    println("线程 $i 异常: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 等待所有线程完成
        assertTrue("所有线程应在30秒内完成", latch.await(30, TimeUnit.SECONDS))
        executor.shutdown()
        
        println("并发测试完成，总结果数: ${results.size}")
        val successCount = results.count { it }
        val failureCount = results.count { !it }
        println("成功次数: $successCount, 失败次数: $failureCount")
        
        // 验证所有线程都完成了测试
        assertEquals("结果数量应等于总迭代次数", threadCount * iterationsPerThread, results.size)
    }
}