//
// Created by 37510 on 2019/9/6.
//
#include "include/lame.h"
#include "jni.h"
#include "stdio.h"

static lame_global_flags *lame = NULL;

JNIEXPORT jstring JNICALL Java_me_shetj_ndk_lame_LameUtils_version(
        JNIEnv *env,
        jclass jcls) {
    return (*env)->NewStringUTF(env, get_lame_version());
};




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
        jboolean vbr) {
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
    if (vbr){
        lame_set_VBR(lame,vbr_mtrh); //设置成vbr
        lame_set_bWriteVbrTag(lame,1); //用来解决vbr，1 on, 0 off读取时间问题,但是无效
    }
    lame_set_lowpassfreq(lame,lowpassfreq);
    lame_set_highpassfreq(lame,highpassfreq);
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

JNIEXPORT void JNICALL
Java_me_shetj_ndk_lame_LameUtils_writeVBRHeader(JNIEnv *env, jobject thiz, jstring file) {
    //must before close lame
    FILE* mp3File = fopen(file,"wb+");
    lame_mp3_tags_fid(lame, mp3File);
}