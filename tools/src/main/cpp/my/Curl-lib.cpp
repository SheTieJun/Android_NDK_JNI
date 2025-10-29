//
// Created by stj on 2021/11/19.
//
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include "utils.h"
#include "HttpModule.h"
#include "CurlTools.h"
#include "curl/curl.h"

static std::string Certificate_file = "";

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_getVersion(JNIEnv *env, jclass obj) {
    curl_version_info_data *ver = curl_version_info(CURLVERSION_NOW);
    return env->NewStringUTF(("libcurl version" + to_string((ver->version_num >> 16) & 0xff) + "." +
                              to_string((ver->version_num >> 8) & 0xff) + "." +
                              to_string(ver->version_num & 0xff)).c_str());
}

// 基础HTTP方法实现
extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_get(JNIEnv *env, jobject thiz, jstring url) {
    string buf;
    HttpModule get;
    get.SetMethod("get");
    get.SetTimeOut(300);
    get.ignoreSSL();
    get.SetResponseStr(buf);
    get.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        get.SetCertificate(Certificate_file);
    }else{
        get.ignoreSSL();
    }
    CURLcode nRet = get.SendRequest();

    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("HttpGet error code:%d", nRet);
        return env->NewStringUTF(("HttpGet error code:" + to_string(nRet)).c_str());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_post(JNIEnv *env, jobject thiz, jstring url, jstring body) {
    string buf;
    HttpModule post;
    post.SetMethod("post");
    post.SetTimeOut(300);
    post.SetPostJson(utils::jString2String(env, body));
    post.SetResponseStr(buf);
    post.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        post.SetCertificate(Certificate_file);
    }else{
        post.ignoreSSL();
    }

    CURLcode nRet = post.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("post error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("post error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_put(JNIEnv *env, jobject thiz, jstring url, jstring body) {
    string buf;
    HttpModule put;
    put.SetMethod("put");
    put.SetTimeOut(300);
    put.SetPostJson(utils::jString2String(env, body));
    put.SetResponseStr(buf);
    put.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        put.SetCertificate(Certificate_file);
    }else{
        put.ignoreSSL();
    }

    CURLcode nRet = put.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("put error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("put error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_delete(JNIEnv *env, jobject thiz, jstring url) {
    string buf;
    HttpModule del;
    del.SetMethod("delete");
    del.SetTimeOut(300);
    del.ignoreSSL();
    del.SetResponseStr(buf);
    del.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        del.SetCertificate(Certificate_file);
    }else{
        del.ignoreSSL();
    }

    CURLcode nRet = del.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("delete error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("delete error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_head(JNIEnv *env, jobject thiz, jstring url) {
    string buf;
    HttpModule head;
    head.SetMethod("head");
    head.SetTimeOut(300);
    head.ignoreSSL();
    head.SetResponseStr(buf);
    head.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        head.SetCertificate(Certificate_file);
    }else{
        head.ignoreSSL();
    }

    CURLcode nRet = head.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("head error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("head error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_patch(JNIEnv *env, jobject thiz, jstring url, jstring body) {
    string buf;
    HttpModule patch;
    patch.SetMethod("patch");
    patch.SetTimeOut(300);
    patch.SetPostJson(utils::jString2String(env, body));
    patch.SetResponseStr(buf);
    patch.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        patch.SetCertificate(Certificate_file);
    }else{
        patch.ignoreSSL();
    }

    CURLcode nRet = patch.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("patch error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("patch error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_options(JNIEnv *env, jobject thiz, jstring url) {
    string buf;
    HttpModule options;
    options.SetMethod("options");
    options.SetTimeOut(300);
    options.ignoreSSL();
    options.SetResponseStr(buf);
    options.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        options.SetCertificate(Certificate_file);
    }else{
        options.ignoreSSL();
    }

    CURLcode nRet = options.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("options error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("options error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

// JSON请求方法
extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_postJson(JNIEnv *env, jobject thiz, jstring url, jstring json) {
    string buf;
    HttpModule post;
    post.SetMethod("post");
    post.SetTimeOut(300);
    post.SetHttpHead("content-type:application/json;charset=UTF-8");
    post.SetPostJson(utils::jString2String(env, json));
    post.SetResponseStr(buf);
    post.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        post.SetCertificate(Certificate_file);
    }else{
        post.ignoreSSL();
    }

    CURLcode nRet = post.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("post json error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("post json error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_putJson(JNIEnv *env, jobject thiz, jstring url, jstring json) {
    string buf;
    HttpModule put;
    put.SetMethod("put");
    put.SetTimeOut(300);
    put.SetHttpHead("content-type:application/json;charset=UTF-8");
    put.SetPostJson(utils::jString2String(env, json));
    put.SetResponseStr(buf);
    put.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        put.SetCertificate(Certificate_file);
    }else{
        put.ignoreSSL();
    }

    CURLcode nRet = put.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("put json error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("put json error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_patchJson(JNIEnv *env, jobject thiz, jstring url, jstring json) {
    string buf;
    HttpModule patch;
    patch.SetMethod("patch");
    patch.SetTimeOut(300);
    patch.SetHttpHead("content-type:application/json;charset=UTF-8");
    patch.SetPostJson(utils::jString2String(env, json));
    patch.SetResponseStr(buf);
    patch.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        patch.SetCertificate(Certificate_file);
    }else{
        patch.ignoreSSL();
    }

    CURLcode nRet = patch.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("patch json error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("patch json error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

extern "C" JNIEXPORT void JNICALL
Java_me_shetj_sdk_curl_CUrlKit_init(JNIEnv *env, jclass clazz) {
    HttpModule::Init();
}

extern "C" JNIEXPORT void JNICALL
Java_me_shetj_sdk_curl_CUrlKit_cleanup(JNIEnv *env, jclass clazz) {
    HttpModule::Cleanup();
}

extern "C"
JNIEXPORT void JNICALL
Java_me_shetj_sdk_curl_CUrlKit_setCertificate(JNIEnv *env, jclass clazz, jstring certificate_path) {
    Certificate_file = utils::jString2String(env, certificate_path);
}