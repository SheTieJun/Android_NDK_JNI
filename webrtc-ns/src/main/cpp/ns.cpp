#include <jni.h>
#include <string>
#include <cstdio>
#include <cstdlib>
#include <android/log.h>

#include "webrtc_ns/noise_suppressor.h"

//添加日志输出
#define LogD(...) __android_log_print(ANDROID_LOG_DEBUG,"WebRTC-NS" ,__VA_ARGS__)

using namespace webrtc;


extern "C"
JNIEXPORT jlong JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_webRtcNsCreate(JNIEnv *env, jobject thiz, jint sampleRate,
                                                 jint num_channels, jint level) {

    NsConfig cfg;
    if (level == 0)
        cfg.target_level = NsConfig::SuppressionLevel::k6dB;
    else if (level == 1)
        cfg.target_level = NsConfig::SuppressionLevel::k12dB;
    else if (level == 2)
        cfg.target_level = NsConfig::SuppressionLevel::k18dB;
    else if (level == 3)
        cfg.target_level = NsConfig::SuppressionLevel::k21dB;
    NoiseSuppressor *ns = new NoiseSuppressor(cfg, sampleRate, num_channels);
    return (jlong) ns;
}




extern "C"
JNIEXPORT void JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_noiseSuppressionByBytes(JNIEnv *env, jobject thiz,
                                                          jlong ns_handler,
                                                          jlong ab_handler, jlong sc_handler,
                                                          jbyteArray inputbuffer) {

    NoiseSuppressor *ns = (NoiseSuppressor *) ns_handler;
    AudioBuffer *audio = (AudioBuffer *) ab_handler;
    StreamConfig *stream_config = (StreamConfig *) sc_handler;

    jbyte *input = env->GetByteArrayElements(inputbuffer, NULL);

    jshort *input_short = (jshort *) input;
    int sampleRate  = stream_config->sample_rate_hz();

    bool split_bands = sampleRate > 16000;

    audio->CopyFrom(input_short, *stream_config);

    if (split_bands) {
        audio->SplitIntoFrequencyBands();
    }
    ns->Analyze(*audio);
    ns->Process(audio);
    if (split_bands) {
        audio->MergeFrequencyBands();
    }
    audio->CopyTo(*stream_config, input_short);

    env->ReleaseByteArrayElements(inputbuffer, input, 0);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_noiseSuppressionByShort(JNIEnv *env, jobject thiz,
                                                          jlong ns_handler,
                                                          jlong ab_handler, jlong sc_handler,
                                                          jshortArray inputbuffer) {

    NoiseSuppressor *ns = (NoiseSuppressor *) ns_handler;
    AudioBuffer *audio = (AudioBuffer *) ab_handler;
    StreamConfig *stream_config = (StreamConfig *) sc_handler;

    jshort *input = env->GetShortArrayElements(inputbuffer, NULL);
    int sampleRate  = stream_config->sample_rate_hz();

    bool split_bands = sampleRate > 16000;

    audio->CopyFrom(input, *stream_config);

    if (split_bands) {
        audio->SplitIntoFrequencyBands();
    }
    ns->Analyze(*audio);
    ns->Process(audio);
    if (split_bands) {
        audio->MergeFrequencyBands();
    }
    audio->CopyTo(*stream_config, input);
    env->ReleaseShortArrayElements(inputbuffer, input, 0);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_createAudioBuffer(JNIEnv *env, jobject thiz, jint sampleRate,
                                                    jint num_channels) {

    AudioBuffer *audio = new AudioBuffer(sampleRate, num_channels,
                                         sampleRate, num_channels,
                                         sampleRate, num_channels);
    return (jlong) audio;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_createStreamConfig(JNIEnv *env, jobject thiz, jint sample_rate,
                                                     jint num_channels) {

    StreamConfig *stream_config = new StreamConfig(sample_rate, num_channels);
    return (jlong) stream_config;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_webRtcNsFree(JNIEnv *env, jobject thiz, jlong ns_handler,
                                               jlong ab_handler, jlong sc_handler) {

    NoiseSuppressor *ns = (NoiseSuppressor *) ns_handler;
    AudioBuffer *audio = (AudioBuffer *) ab_handler;
    StreamConfig *stream_config = (StreamConfig *) sc_handler;
    free(ns);
    free(audio);
    free(stream_config);
}