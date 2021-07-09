//
// Created by stj on 2021/7/9.
//
#include <jni.h>
#include <string>

extern "C" {
JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_ffmepg_FFmpegKit_checkFFmpeg(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF("4.1.2");

}
}
