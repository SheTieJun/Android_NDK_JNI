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




extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_postJson(JNIEnv *env, jobject thiz, jstring url, jstring json) {

    string buf;
    HttpModule post; //每次都创建新的
    post.SetMethod("post");
    post.SetTimeOut(300);
    post.SetHttpHead("content-type:application/json;charset=UTF-8");
    post.SetPostJson(utils::jString2String(env, json));
    post.SetResponseStr(buf);
    post.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        post.SetCertificate(Certificate_file);
    }else{
        post.ignoreSSL();//忽略证书验证
    }

    CURLcode nRet = post.SendRequest();
//    CURLcode nRet = CurlTools::HttpPost(utils::jString2String(env, url),utils::jString2String(env, json), buf, 300);

    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("post json  error code:%d:%s", nRet, curl_easy_strerror(nRet));
        return env->NewStringUTF(("post json error code:" + to_string(nRet) + ":"
        + curl_easy_strerror(nRet)).c_str());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_get(JNIEnv *env, jobject thiz, jstring url) {
    string buf;
    HttpModule get; //每次都创建新的
    get.SetMethod("get");
    get.SetTimeOut(300);
    get.ignoreSSL();//忽略证书验证
    get.SetResponseStr(buf);
    get.SetURL(utils::jString2String(env, url));
    if(!Certificate_file.empty()){
        get.SetCertificate(Certificate_file);
    }else{
        get.ignoreSSL();//忽略证书验证
    }
    CURLcode nRet = get.SendRequest();

//    CURLcode res = CurlTools::HttpGet(utils::jString2String(env, url), buf, 300);
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("HttpGet error code:%d", nRet);
        return env->NewStringUTF(("HttpGet error code:" + to_string(nRet)).c_str());
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