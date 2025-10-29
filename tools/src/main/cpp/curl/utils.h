//
// Created by stj on 2021/11/30.
//

#ifndef ANDROID_NDK_UTILS_H
#define ANDROID_NDK_UTILS_H

#include <string>
#include <vector>
#include <unordered_set>
#include "LogKit.h"
#include <jni.h>

using namespace std;

// 安全校验结果枚举
enum class SecurityCheckResult {
    SUCCESS = 0,
    PACKAGE_NOT_ALLOWED = 1,
    SIGNATURE_MISMATCH = 2,
    ANTI_DEBUG_DETECTED = 3,
    TAMPER_DETECTED = 4,
    UNKNOWN_ERROR = 5
};

// 包名白名单配置结构
struct PackageWhitelistConfig {
    vector<string> allowedPackages;
    bool enableSignatureCheck;
    bool enableAntiDebug;
    bool enableTamperDetection;
    int maxRetryCount;
};

class utils {

public:
    //char比较
    static  bool icompare_pred(unsigned char a, unsigned char b);

    //字符串比较
    static  bool icasecompare(std::string const& a, std::string const& b);

    //jstring 转string
    static string jString2String(JNIEnv *env,jstring data);
    
    // 新增安全校验相关函数
    
    // 包名白名单校验
    static bool isPackageInWhitelist(const string& packageName);
    
    // 多层安全校验
    static SecurityCheckResult performSecurityCheck(JNIEnv *env, jobject context);
    
    // 反调试检测
    static bool detectAntiDebug();
    
    // 完整性校验
    static bool verifyIntegrity(JNIEnv *env, jobject context);
    
    // 获取加密的包名白名单
    static vector<string> getEncryptedWhitelist();
    
    // 获取包名白名单 - 明文白名单算法
    static vector<string> getPackageWhitelist();
    
    // 生成校验token
    static string generateSecurityToken(const string& packageName, const string& signature);
    
    // 验证校验token
    static bool validateSecurityToken(const string& token, const string& packageName);
    
private:
    // 私有安全函数
    static bool checkDebuggerAttached();
    static bool checkEmulatorEnvironment();
    static bool checkRootEnvironment();
    static string calculateHash(const string& input);
    static void obfuscateString(string& str);
};

// JNI接口声明
extern "C" {
    // 现有接口
    JNIEXPORT jstring JNICALL Java_me_shetj_sdk_utils_Utils_getPackageName(JNIEnv *env, jclass clazz);
    JNIEXPORT jstring JNICALL Java_me_shetj_sdk_utils_Utils_verificationSign(JNIEnv *env, jclass clazz,jobject context);

    // 新增安全校验接口
    JNIEXPORT jint JNICALL Java_me_shetj_sdk_utils_Utils_performSecurityCheckNative(JNIEnv *env, jclass clazz, jobject context);
    JNIEXPORT jboolean JNICALL Java_me_shetj_sdk_utils_Utils_isPackageAllowedNative(JNIEnv *env, jclass clazz, jstring packageName);
    JNIEXPORT jboolean JNICALL Java_me_shetj_sdk_utils_Utils_isPackageAllowedNative(JNIEnv *env, jclass clazz, jstring packageName);

    JNIEXPORT jboolean JNICALL Java_me_shetj_sdk_utils_Utils_detectAntiDebugNative(JNIEnv *env, jclass clazz);
    JNIEXPORT jboolean JNICALL Java_me_shetj_sdk_utils_Utils_verifyIntegrityNative(JNIEnv *env, jclass clazz, jobject context);
    JNIEXPORT jobjectArray JNICALL Java_me_shetj_sdk_utils_Utils_getAllowedPackagesNative(JNIEnv *env, jclass clazz);


}

#endif //ANDROID_NDK_UTILS_H
