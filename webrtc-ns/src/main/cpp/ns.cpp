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
    auto ns = new NoiseSuppressor(cfg, sampleRate, num_channels);
    return (jlong) ns;
}




extern "C"
JNIEXPORT void JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_noiseSuppressionByBytes(JNIEnv *env, jobject thiz,
                                                          jlong ns_handler,
                                                          jlong ab_handler, jlong sc_handler,
                                                          jbyteArray inputbuffer) {

    auto *ns = (NoiseSuppressor *) ns_handler;
    auto *audio = (AudioBuffer *) ab_handler;
    auto *stream_config = (StreamConfig *) sc_handler;

    jbyte *input = env->GetByteArrayElements(inputbuffer, NULL);
    jsize input_length_bytes = env->GetArrayLength(inputbuffer);

    auto *input_short = (jshort *) input;
    size_t input_length_shorts = input_length_bytes / 2;

    int sampleRate  = stream_config->sample_rate_hz();
    size_t chunk_length = stream_config->num_samples(); // shorts per 10ms frame
    bool split_bands = sampleRate > 16000;

    // WebRTC NS strictly processes 10ms of audio per call.
    // We must iterate over the buffer in 10ms chunks.
    for (size_t offset = 0; offset + chunk_length <= input_length_shorts; offset += chunk_length) {
        audio->CopyFrom(input_short + offset, *stream_config);

        if (split_bands) {
            audio->SplitIntoFrequencyBands();
        }
        ns->Analyze(*audio);
        ns->Process(audio);
        if (split_bands) {
            audio->MergeFrequencyBands();
        }
        audio->CopyTo(*stream_config, input_short + offset);
    }

    env->ReleaseByteArrayElements(inputbuffer, input, 0);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_noiseSuppressionByShort(JNIEnv *env, jobject thiz,
                                                          jlong ns_handler,
                                                          jlong ab_handler, jlong sc_handler,
                                                          jshortArray inputbuffer) {

    auto *ns = (NoiseSuppressor *) ns_handler;
    auto *audio = (AudioBuffer *) ab_handler;
    auto *stream_config = (StreamConfig *) sc_handler;

    jshort *input = env->GetShortArrayElements(inputbuffer, NULL);
    jsize input_length = env->GetArrayLength(inputbuffer);

    int sampleRate  = stream_config->sample_rate_hz();
    size_t chunk_length = stream_config->num_samples(); // shorts per 10ms frame
    bool split_bands = sampleRate > 16000;

    // WebRTC NS strictly processes 10ms of audio per call.
    for (size_t offset = 0; offset + chunk_length <= (size_t)input_length; offset += chunk_length) {
        audio->CopyFrom(input + offset, *stream_config);

        if (split_bands) {
            audio->SplitIntoFrequencyBands();
        }
        ns->Analyze(*audio);
        ns->Process(audio);
        if (split_bands) {
            audio->MergeFrequencyBands();
        }
        audio->CopyTo(*stream_config, input + offset);
    }
    env->ReleaseShortArrayElements(inputbuffer, input, 0);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_createAudioBuffer(JNIEnv *env, jobject thiz, jint sampleRate,
                                                    jint num_channels) {

    auto *audio = new AudioBuffer(sampleRate, num_channels,
                                         sampleRate, num_channels,
                                         sampleRate, num_channels);
    return (jlong) audio;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_createStreamConfig(JNIEnv *env, jobject thiz, jint sample_rate,
                                                     jint num_channels) {

    auto *stream_config = new StreamConfig(sample_rate, num_channels);
    return (jlong) stream_config;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_shetj_webrtc_ns_WebRtcNs_webRtcNsFree(JNIEnv *env, jobject thiz, jlong ns_handler,
                                               jlong ab_handler, jlong sc_handler) {

    auto *ns = (NoiseSuppressor *) ns_handler;
    auto *audio = (AudioBuffer *) ab_handler;
    auto *stream_config = (StreamConfig *) sc_handler;
    free(ns);
    free(audio);
    free(stream_config);
}