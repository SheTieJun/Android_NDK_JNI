//
// Created by 37510 on 2019/9/6.
//
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include "include/lame.h"
#include "jni.h"
#include "stdio.h"
#include <android/log.h>

//打印日志
#define LAME_TAG "Lame_Log"
#define LogE(...) __android_log_print(ANDROID_LOG_ERROR,LAME_TAG ,__VA_ARGS__)
#define LogD(...) __android_log_print(ANDROID_LOG_DEBUG,LAME_TAG ,__VA_ARGS__)


#define BOOL int
#define TRUE 1
#define FALSE 0

static lame_global_flags *lame = NULL;

JNIEXPORT jstring JNICALL Java_me_shetj_ndk_lame_LameUtils_version(
        JNIEnv *env,
        jclass jcls) {
    return (*env)->NewStringUTF(env, get_lame_version());
};

void errorMsg(const char *msg, va_list vaList) {
    char string[256];
    vsprintf(string, msg, vaList);
    LogE("%s", string);
}

void debugMsg(const char *msg, va_list vaList) {
    char string[256];
    vsprintf(string, msg, vaList);
    LogD("%s", string);
}

void wMsg(const char *msg, va_list vaList) {
    char string[256];
    vsprintf(string, msg, vaList);
    LogD("%s", string);
}

JNIEXPORT void JNICALL Java_me_shetj_ndk_lame_LameUtils_init(
        JNIEnv *env,
        jclass cls,
        jint inSamplerate,
        jint inChannel,
        jint outSamplerate,
        jint outBitrate,
        jint quality,
        jint lowpassfreq,
        jint highpassfreq,
        jboolean vbr,
        jboolean enableLog) {
    if (lame != NULL) {
        lame_close(lame);
        lame = NULL;
    }
    lame = lame_init();
    //初始化，设置参数
    lame_set_in_samplerate(lame, inSamplerate);//输入采样率
    lame_set_out_samplerate(lame, outSamplerate);//输出采样率
    lame_set_num_channels(lame, inChannel);//声道
    lame_set_brate(lame, outBitrate);//比特率
    lame_set_quality(lame, quality);//质量
    if (vbr) {
        // 读取录制时间问题会存在
        lame_set_VBR(lame, vbr_mtrh); //设置成vbr
        lame_set_bWriteVbrTag(lame, 1); //用来解决vbr，1 on, 0 off
        lame_set_VBR_mean_bitrate_kbps(lame, outBitrate);
    }
    lame_set_lowpassfreq(lame, lowpassfreq); //设置滤波器，-1 disabled
    lame_set_highpassfreq(lame, highpassfreq);//设置滤波器，-1 disabled
    //设置信息输出
    if (enableLog) {
        lame_set_errorf(lame, errorMsg);
        lame_set_debugf(lame, debugMsg);
        lame_set_msgf(lame, wMsg);
        LogD("lame_init_params:\ninSamplerate =%d,\ninChannel=%d,\noutSamplerate=%d,\noutBitrate=%d,\nquality=%d,\nlowpassfreq=%d,\nhighpassfreq=%d,\nvbr=%hhu",
             inSamplerate, inChannel, outSamplerate, outBitrate, quality, lowpassfreq,
             highpassfreq,vbr);
    }
    lame_init_params(lame);
}



JNIEXPORT jint JNICALL Java_me_shetj_ndk_lame_LameUtils_encode(
        JNIEnv *env,
        jclass cls,
        jshortArray buffer_left,
        jshortArray buffer_right,
        jint samples,
        jbyteArray mp3buf) {

    //把Java传过来参数转成C中的参数进行修改
    jshort *j_buff_left = (*env)->GetShortArrayElements(env, buffer_left, NULL);
    jshort *j_buff_right = (*env)->GetShortArrayElements(env, buffer_right, NULL);

    const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);

    jbyte *j_mp3buff = (*env)->GetByteArrayElements(env, mp3buf, NULL);

    int result = lame_encode_buffer(lame, j_buff_left, j_buff_right, samples, j_mp3buff,
                                    mp3buf_size);


    //释放参数
    (*env)->ReleaseShortArrayElements(env, buffer_left, j_buff_left, 0);
    (*env)->ReleaseShortArrayElements(env, buffer_right, j_buff_right, 0);
    (*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buff, 0);
    return result;

}

JNIEXPORT jint JNICALL Java_me_shetj_ndk_lame_LameUtils_encodeInterleaved(
        JNIEnv *env,
        jclass cls,
        jshortArray pcm_buffer,
        jint samples,
        jbyteArray mp3buf) {

    //把Java传过来参数转成C中的参数进行修改
    jshort *j_pcm_buffer = (*env)->GetShortArrayElements(env, pcm_buffer, NULL);

    const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);

    jbyte *j_mp3buff = (*env)->GetByteArrayElements(env, mp3buf, NULL);

    int result = lame_encode_buffer_interleaved(lame, j_pcm_buffer, samples, j_mp3buff,
                                                mp3buf_size);

    //释放参数
    (*env)->ReleaseShortArrayElements(env, pcm_buffer, j_pcm_buffer, 0);
    (*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buff, 0);
    return result;

}


JNIEXPORT jint JNICALL Java_me_shetj_ndk_lame_LameUtils_encodeInterleavedByByte(
        JNIEnv *env,
        jclass cls,
        jbyteArray pcm_buffer,
        jint samples,
        jbyteArray mp3buf) {

    //把Java传过来参数转成C中的参数进行修改
    jbyte *j_pcm_buffer = (*env)->GetByteArrayElements(env, pcm_buffer, NULL);

    const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);

    jbyte *j_mp3buff = (*env)->GetByteArrayElements(env, mp3buf, NULL);

    int result = lame_encode_buffer_interleaved(lame, (const short *) j_pcm_buffer, samples / 2,
                                                j_mp3buff, mp3buf_size);

    //释放参数
    (*env)->ReleaseShortArrayElements(env, pcm_buffer, j_pcm_buffer, 0);
    (*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buff, 0);
    return result;

}


JNIEXPORT jint JNICALL Java_me_shetj_ndk_lame_LameUtils_encodeByByte(
        JNIEnv *env,
        jclass cls,
        jbyteArray buffer_left,
        jbyteArray buffer_right,
        jint samples,
        jbyteArray mp3buf) {

    //把Java传过来参数转成C中的参数进行修改
    jbyte *j_buff_left = (*env)->GetByteArrayElements(env, buffer_left, NULL);
    jbyte *j_buff_right = (*env)->GetByteArrayElements(env, buffer_right, NULL);

    const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);

    jbyte *j_mp3buff = (*env)->GetByteArrayElements(env, mp3buf, NULL);

    int result = lame_encode_buffer(lame, (const short *) j_buff_left, (const short *) j_buff_right,
                                    samples / 2, j_mp3buff, mp3buf_size);

    //释放参数
    (*env)->ReleaseByteArrayElements(env, buffer_left, j_buff_left, 0);
    (*env)->ReleaseByteArrayElements(env, buffer_right, j_buff_right, 0);
    (*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buff, 0);
    return result;

}

JNIEXPORT jint JNICALL Java_me_shetj_ndk_lame_LameUtils_flush(
        JNIEnv *env,
        jclass cls,
        jbyteArray mp3buf) {
    const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);

    jbyte *j_mp3buff = (*env)->GetByteArrayElements(env, mp3buf, NULL);

    int result = lame_encode_flush(lame, j_mp3buff, mp3buf_size);
    //释放
    (*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buff, 0);

    return result;
}

JNIEXPORT void JNICALL Java_me_shetj_ndk_lame_LameUtils_close(
        JNIEnv *env,
        jclass cls) {
    lame_close(lame);
    lame = NULL;
}

// jstring转string类型方法
char *jstring2string(JNIEnv *env, jstring jstr) {
    char *bytes = NULL;
    jclass classString = (*env)->FindClass(env, "java/lang/String");
    jbyteArray byteArray = (jbyteArray) (*env)->CallObjectMethod(env,
                                                                 jstr,
                                                                 (*env)->GetMethodID(env,
                                                                                     classString,
                                                                                     "getBytes",
                                                                                     "(Ljava/lang/String;)[B"),
                                                                 (*env)->NewStringUTF(env, "UTF8"));
    jsize length = (*env)->GetArrayLength(env, byteArray);
    jbyte *jbytes = (*env)->GetByteArrayElements(env, byteArray, JNI_FALSE);
    if (length > 0) {
        bytes = (char *) malloc(length + 1);
        memcpy(bytes, jbytes, length);
        bytes[length] = 0;
    }
    (*env)->ReleaseByteArrayElements(env, byteArray, jbytes, 0);
    return bytes;
}

JNIEXPORT void JNICALL
Java_me_shetj_ndk_lame_LameUtils_writeVBRHeader(JNIEnv *env, jobject thiz, jstring file) {
    char *path = jstring2string(env, file);
    FILE *mp3File = fopen(path, "ab+");
    lame_mp3_tags_fid(lame, mp3File);
    fclose(mp3File);
}

JNIEXPORT jint JNICALL
Java_me_shetj_ndk_lame_LameUtils_getPCMDB(JNIEnv *env, jobject thiz, jshortArray pcm,
                                          jint samples) {

    int db = 0;
    short int value = 0;
    double sum = 0;

    jshort *j_pcm_buffer = (*env)->GetShortArrayElements(env, pcm, NULL);

    //16 bit == 2字节 == short int
    for (int i = 0; i < samples; i += 2) {
        memcpy(&value, j_pcm_buffer + i, 2); //获取2个字节的大小（值）
        sum += abs(value); //绝对值求和
    }
    sum = sum / (samples / 2); //求平均值（2个字节表示一个振幅，所以振幅个数为：size/2个）
    if (sum > 0) {
        db = (int) (20.0 * log10(sum));
    }
    //释放参数
    (*env)->ReleaseShortArrayElements(env, pcm, j_pcm_buffer, 0);
    return db;

}