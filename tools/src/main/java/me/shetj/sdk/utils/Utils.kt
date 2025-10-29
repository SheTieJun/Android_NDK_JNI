package me.shetj.sdk.utils

import android.content.Context

/**
 * 安全校验工具类
 *
 * 提供包名白名单校验、签名验证、反调试检测等安全功能
 * 所有关键校验逻辑都在native层实现，确保安全性
 *
 * @author shetj
 * @since 2.0.0
 */
class Utils {

    /**
     * 安全校验结果枚举
     */
    enum class SecurityCheckResult(val code: Int, val message: String) {
        /** 校验成功 */
        SUCCESS(0, "Security check passed"),

        /** 安全 */
        SAFE(0, "Security check passed"),

        /** 包名不在白名单中 */
        PACKAGE_NOT_ALLOWED(1, "Package not in whitelist"),

        /** 签名不匹配 */
        SIGNATURE_MISMATCH(2, "Signature verification failed"),

        /** 检测到反调试 */
        ANTI_DEBUG_DETECTED(3, "Anti-debug environment detected"),

        /** 检测到篡改 */
        TAMPER_DETECTED(4, "Application tampering detected"),

        /** 未知错误 */
        UNKNOWN_ERROR(5, "Unknown security error"),

        /** 未知状态 */
        UNKNOWN(6, "Unknown status");

        companion object {
            fun fromCode(code: Int): SecurityCheckResult {
                return values().find { it.code == code } ?: UNKNOWN_ERROR
            }
        }
    }

    /**
     * 安全校验异常
     */
    class SecurityException(
        val result: SecurityCheckResult,
        message: String = result.message
    ) : Exception(message)

    companion object {
        init {
            System.loadLibrary("tools")
        }

        // ==================== 基础功能 ====================

        /**
         * 获取当前应用包名
         *
         * @return 应用包名
         */
        @JvmStatic
        external fun getPackageName(): String

        /**
         * 验证包名是否合法（兼容旧版本）
         *
         * @return "pkg true" 表示合法，"pkg error" 表示不合法
         */
        @JvmStatic
        external fun verificationPkg(): String

        /**
         * 验证应用签名（兼容旧版本）
         *
         * @return "sign true" 表示签名正确，"sign error" 表示签名错误
         */
        @JvmStatic
        external fun verificationSign(context: Context): String?

        // ==================== 新增安全校验功能 ====================

        /**
         * 执行完整的安全校验
         *
         * 包括包名白名单校验、签名验证、反调试检测等多层安全检查
         *
         * @param context Android上下文
         * @param expectedResult 期望的安全校验结果
         * @return 安全校验结果
         * @throws SecurityException 当校验失败时抛出异常
         */
        @JvmStatic
        fun performSecurityCheck(context: Context, expectedResult: SecurityCheckResult = SecurityCheckResult.SUCCESS): SecurityCheckResult {
            val resultCode = performSecurityCheckNative(context)
            val result = SecurityCheckResult.fromCode(resultCode)

            if (result != SecurityCheckResult.SUCCESS && result != SecurityCheckResult.SAFE) {
                throw SecurityException(result)
            }

            return result
        }

        /**
         * 执行安全校验（不抛出异常）
         *
         * @param context Android上下文
         * @return 安全校验结果
         */
        @JvmStatic
        fun performSecurityCheckSafe(context: Context): SecurityCheckResult {
            return try {
                val resultCode = performSecurityCheckNative(context)
                SecurityCheckResult.fromCode(resultCode)
            } catch (e: Exception) {
                SecurityCheckResult.UNKNOWN_ERROR
            }
        }

        /**
         * 检查指定包名是否在白名单中
         *
         * @param packageName 要检查的包名
         * @return true表示在白名单中，false表示不在
         */
        @JvmStatic
        fun isPackageAllowed(packageName: String): Boolean {
            return isPackageAllowedNative(packageName)
        }

        /**
         * 检查当前应用包名是否在白名单中
         *
         * @param context Android上下文
         * @return true表示在白名单中，false表示不在
         */
        @JvmStatic
        fun isCurrentPackageAllowed(context: Context): Boolean {
            return try {
                val packageName = context.packageName
                isPackageAllowed(packageName)
            } catch (e: Exception) {
                false
            }
        }



        /**
         * 检测反调试环境
         *
         * 检测是否存在调试器、模拟器、Root环境等
         *
         * @return true表示检测到反调试环境，false表示正常环境
         */
        @JvmStatic
        fun detectAntiDebug(): Boolean {
            return detectAntiDebugNative()
        }

        /**
         * 验证应用完整性
         *
         * @param context Android上下文
         * @return true表示完整性校验通过，false表示失败
         */
        @JvmStatic
        fun verifyIntegrity(context: Context): Boolean {
            return verifyIntegrityNative(context)
        }

        /**
         * 获取所有允许的包名列表
         *
         * @return 包名数组
         */
        @JvmStatic
        fun getAllowedPackages(): Array<String> {
            return getAllowedPackagesNative() ?: emptyArray()
        }

        // ==================== 动态白名单管理（仅用于测试） ====================

        /**
         * 添加包名到动态白名单（仅用于测试环境）
         *
         * 注意：此功能仅在测试环境下使用，生产环境应使用预定义的加密白名单
         *
         * @param packageName 要添加的包名
         * @return true表示添加成功，false表示失败
         */
        @JvmStatic
        fun addPackageToWhitelist(packageName: String): Boolean {
            return addPackageToWhitelistNative(packageName)
        }

        /**
         * 从动态白名单中移除包名（仅用于测试环境）
         *
         * @param packageName 要移除的包名
         * @return true表示移除成功，false表示失败
         */
        @JvmStatic
        fun removePackageFromWhitelist(packageName: String): Boolean {
            return removePackageFromWhitelistNative(packageName)
        }

        // ==================== 便捷方法 ====================

        /**
         * 快速安全校验
         *
         * 执行基本的包名和签名校验，不包括反调试检测
         *
         * @param context Android上下文
         * @return true表示校验通过，false表示失败
         */
        @JvmStatic
        fun quickSecurityCheck(context: Context): Boolean {
            return try {
                isCurrentPackageAllowed(context) && verifyIntegrity(context)
            } catch (e: Exception) {
                false
            }
        }

        /**
         * 严格安全校验
         *
         * 执行完整的安全校验，包括反调试检测
         *
         * @param context Android上下文
         * @return true表示校验通过，false表示失败
         */
        @JvmStatic
        fun strictSecurityCheck(context: Context): Boolean {
            return try {
                performSecurityCheckSafe(context) == SecurityCheckResult.SUCCESS
            } catch (e: Exception) {
                false
            }
        }

        /**
         * 获取安全状态信息
         *
         * @param context Android上下文
         * @return 包含安全状态的Map
         */
        @JvmStatic
        fun getSecurityStatus(context: Context): Map<String, Any?> {
            return try {
                mapOf(
                    "packageName" to getPackageName(),
                    "packageAllowed" to isCurrentPackageAllowed(context),
                    "integrityValid" to verifyIntegrity(context),
                    "antiDebugDetected" to detectAntiDebug(),
                    "allowedPackages" to getAllowedPackages().toList(),
                    "securityCheckResult" to performSecurityCheckSafe(context).name
                )
            } catch (e: Exception) {
                mapOf(
                    "error" to e.message,
                    "securityCheckResult" to SecurityCheckResult.UNKNOWN_ERROR.name
                )
            }
        }

        // ==================== Native方法声明 ====================

        @JvmStatic
        external fun performSecurityCheckNative(context: Context): Int

        @JvmStatic
        external fun isPackageAllowedNative(packageName: String): Boolean


        @JvmStatic
        external fun detectAntiDebugNative(): Boolean

        @JvmStatic
        external fun verifyIntegrityNative(context: Context): Boolean

        @JvmStatic
        external fun getAllowedPackagesNative(): Array<String>?

        @JvmStatic
        external fun addPackageToWhitelistNative(packageName: String): Boolean

        @JvmStatic
        external fun removePackageFromWhitelistNative(packageName: String): Boolean
    }
}