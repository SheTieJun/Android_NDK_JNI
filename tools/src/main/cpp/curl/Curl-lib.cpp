//
// Created by stj on 2021/11/19.
//
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>


extern "C"
{

#include "curl/curl.h"

JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_curl_CUrlKit_getVersion(JNIEnv *env, jobject obj) {
    curl_version_info_data *ver = curl_version_info(CURLVERSION_NOW);
//    CURL *curl = curl_easy_init();
//    if(curl) {
//        CURLcode res;
//        curl_easy_setopt(curl, CURLOPT_URL, "https://example.com");
//        res = curl_easy_perform(curl);
//        curl_easy_cleanup(curl);
//    }
    __android_log_print((int)ANDROID_LOG_INFO, "curl", "libcurl version %d.%d.%d\n",
           (ver->version_num >> 16) & 0xff,
           (ver->version_num >> 8) & 0xff,
           ver->version_num & 0xff);

    return env->NewStringUTF("libcurl version");
}
}