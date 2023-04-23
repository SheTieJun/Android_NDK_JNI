#include <jni.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>

#include "webrtc_ns_v3/noise_suppressor.h"

//添加日志输出
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "WebRTC-NS", __VA_ARGS__)

using namespace webrtc;


extern "C"
JNIEXPORT jlong JNICALL
Java_com_shetj_webrtc_ns_WebRTCNS_WebRtcNsCreate(JNIEnv *env, jobject thiz, jint sampleRate,
                                                 jint num_channels) {

    AudioBuffer audio(sampleRate, num_channels, sampleRate, num_channels, sampleRate,
                      num_channels);
    StreamConfig stream_config(sampleRate, num_channels);
    NsConfig cfg;
    /*
     * NsConfig::SuppressionLevel::k6dB
     * NsConfig::SuppressionLevel::k12dB
     * NsConfig::SuppressionLevel::k18dB
     * NsConfig::SuppressionLevel::k21dB
     */
    cfg.target_level = NsConfig::SuppressionLevel::k21dB;
    NoiseSuppressor *ns  = new NoiseSuppressor(cfg, sampleRate, num_channels);
    return (jlong)ns;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_shetj_webrtc_ns_WebRTCNS_WebRtcNsFree(JNIEnv *env, jobject thiz, jlong ns_handler) {

    NoiseSuppressor *ns = (NoiseSuppressor *) ns_handler;
    delete ns;

}


extern "C"
JNIEXPORT void JNICALL
Java_com_shetj_webrtc_ns_WebRTCNS_noiseSuppression(JNIEnv *env, jobject thiz, jlong ns_handler,jstring file_in,
                                                   jstring file_out, jint sample, jint mode) {
    NoiseSuppressor *ns = (NoiseSuppressor *) ns_handler;






}