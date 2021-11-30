//
// Created by stj on 2021/11/19.
//
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include "utils.h"
#include "HttpModule.h"
#include "CurlTools.h"
#include "Clibcurl.h"

static jobject g_objAc = NULL;
static JNIEnv *g_env = NULL;


size_t PostDispose(char *buffer, size_t size, size_t nmemb, void *userdata) {
    LOGI("PostDispose!");
    if (g_env == NULL) return nmemb;
    jobject jdata = g_env->NewStringUTF(buffer);
    jclass curlKit = g_env->GetObjectClass(g_objAc);
    jmethodID methodPostDispose = g_env->GetMethodID(curlKit, "callback",
                                                     "(Ljava/lang/String;)V");
    g_env->CallVoidMethod(g_objAc, methodPostDispose, jdata);
    return nmemb;
}


extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_getVersion(JNIEnv *env, jclass obj) {
    curl_version_info_data *ver = curl_version_info(CURLVERSION_NOW);
    LOGI("libcurl version %d.%d.%d\n"
         "ssl = %s",
         (ver->version_num >> 16) & 0xff,
         (ver->version_num >> 8) & 0xff,
         ver->version_num & 0xff,
         ver->ssl_version);
    return env->NewStringUTF("libcurl version");
}




extern "C" JNIEXPORT void JNICALL
Java_me_shetj_sdk_curl_CUrlKit_postJson(JNIEnv *env, jobject thiz, jstring url, jstring json) {
    g_objAc = thiz;
    HttpModule post; //每次都创建新的
    post.SetTimeOut(60);
    post.SetMethod("post");
    post.SetHttpHead("Content-Type:application/json;charset=UTF-8");
    post.SetPostJson(utils::jString2String(env, json));
    post.SetWriteFunction(PostDispose);
    post.SetURL(utils::jString2String(env, url).c_str());
    int nRet = post.SendRequest();
    if (nRet == CURLE_OK)
        LOGI("post success!");
    else
        LOGE("post error code:%d", nRet);
}

extern "C" JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_get(JNIEnv *env, jobject thiz, jstring url) {
    string buf;
    CURLcode res =  CurlTools::HttpGet(utils::jString2String(env, url), buf, 300);
    if(res == CURLE_OK){
        LOGI("HttpGet:%s",buf.c_str());
        return env->NewStringUTF(buf.c_str());
    }else{
        LOGE("HttpGet error code:%d", res);
        return env->NewStringUTF(("HttpGet error code:"+to_string(res)).c_str());
    }

}

extern "C" JNIEXPORT void JNICALL
Java_me_shetj_sdk_curl_CUrlKit_init(JNIEnv *env, jclass clazz) {
    HttpModule::Init();
    g_env = env;
}

extern "C" JNIEXPORT void JNICALL
Java_me_shetj_sdk_curl_CUrlKit_cleanup(JNIEnv *env, jclass clazz) {
    HttpModule::Cleanup();
    g_env = NULL;
    g_objAc = NULL;
}

