#include <jni.h>
#include <android/log.h>
#include <stdexcept>
#include <string>

using namespace std;

#include "soundtouch/SoundTouch.h"
#include "soundtouch/WavFile.h"

#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "SOUNDTOUCH", __VA_ARGS__)


// String for keeping possible c++ exception error messages. Notice that this isn't
// thread-safe but it's expected that exceptions are special situations that won't
// occur in several threads in parallel.
static string _errMsg = "";


#define DLL_PUBLIC __attribute__ ((visibility ("default")))
#define BUFF_SIZE 4096


using namespace soundtouch;


// Set error message to return
static void _setErrmsg(const char *msg) {
    _errMsg = msg;
}

#if 0   // apparently following workaround not needed any more with concurrent Android SDKs
#ifdef _OPENMP

#include <pthread.h>
extern pthread_key_t gomp_tls_key;
static void * _p_gomp_tls = NULL;

/// Function to initialize threading for OpenMP.
///
/// This is a workaround for bug in Android NDK v10 regarding OpenMP: OpenMP works only if
/// called from the Android App main thread because in the main thread the gomp_tls storage is
/// properly set, however, Android does not properly initialize gomp_tls storage for other threads.
/// Thus if OpenMP routines are invoked from some other thread than the main thread,
/// the OpenMP routine will crash the application due to NULL pointer access on uninitialized storage.
///
/// This workaround stores the gomp_tls storage from main thread, and copies to other threads.
/// In order this to work, the Application main thread needws to call at least "getVersionString"
/// routine.
static int _init_threading(bool warn)
{
    void *ptr = pthread_getspecific(gomp_tls_key);
    LOGV("JNI thread-specific TLS storage %ld", (long)ptr);
    if (ptr == NULL)
    {
        LOGV("JNI set missing TLS storage to %ld", (long)_p_gomp_tls);
        pthread_setspecific(gomp_tls_key, _p_gomp_tls);
    }
    else
    {
        LOGV("JNI store this TLS storage");
        _p_gomp_tls = ptr;
    }
    // Where critical, show warning if storage still not properly initialized
    if ((warn) && (_p_gomp_tls == NULL))
    {
        _setErrmsg("Error - OpenMP threading not properly initialized: Call SoundTouch.getVersionString() from the App main thread!");
        return -1;
    }
    return 0;
}

#else
static int _init_threading(bool warn)
{
    // do nothing if not OpenMP build
    return 0;
}
#endif

#endif

static SoundTouch *pSoundTouch = NULL;

// Processes the sound file
static void _processFile(SoundTouch *pSoundTouch, const char *inFileName, const char *outFileName) {
    int nSamples;
    int nChannels;
    int buffSizeSamples;
    SAMPLETYPE sampleBuffer[BUFF_SIZE];

    // open input file
    WavInFile inFile(inFileName);
    int sampleRate = inFile.getSampleRate();
    int bits = inFile.getNumBits();
    nChannels = inFile.getNumChannels();

    // create output file
    WavOutFile outFile(outFileName, sampleRate, bits, nChannels);

    pSoundTouch->setSampleRate(sampleRate);
    pSoundTouch->setChannels(nChannels);

    assert(nChannels > 0);
    buffSizeSamples = BUFF_SIZE / nChannels;

    // Process samples read from the input file
    while (inFile.eof() == 0) {
        int num;

        // Read a chunk of samples from the input file
        num = inFile.read(sampleBuffer, BUFF_SIZE);
        nSamples = num / nChannels;

        // Feed the samples into SoundTouch processor
        pSoundTouch->putSamples(sampleBuffer, nSamples);

        // Read ready samples from SoundTouch processor & write them output file.
        // NOTES:
        // - 'receiveSamples' doesn't necessarily return any samples at all
        //   during some rounds!
        // - On the other hand, during some round 'receiveSamples' may have more
        //   ready samples than would fit into 'sampleBuffer', and for this reason
        //   the 'receiveSamples' call is iterated for as many times as it
        //   outputs samples.
        do {
            nSamples = pSoundTouch->receiveSamples(sampleBuffer, buffSizeSamples);
            outFile.write(sampleBuffer, nSamples * nChannels);
        } while (nSamples != 0);
    }
    // Now the input file is processed, yet 'flush' few last samples that are
    // hiding in the SoundTouch's internal processing pipeline.
    pSoundTouch->flush();
    do {
        nSamples = pSoundTouch->receiveSamples(sampleBuffer, buffSizeSamples);
        outFile.write(sampleBuffer, nSamples * nChannels);
    } while (nSamples != 0);
}

static jint _processSamples(JNIEnv *env, jbyteArray data, jint size, jbyteArray outbuf) {
    int samples;
    int channel = pSoundTouch->numChannels();
    int bufferSize = size / (channel * 2); //可能是双声道，是一半
    SAMPLETYPE sampleBuffer[size];
    pSoundTouch->putSamples((SAMPLETYPE *) data, bufferSize);
    samples = pSoundTouch->receiveSamples(sampleBuffer, bufferSize);
    env->SetByteArrayRegion(outbuf, 0, samples * channel, (jbyte *) sampleBuffer);
    return samples * channel;
}


extern "C" DLL_PUBLIC jstring
Java_me_shetj_ndk_soundtouch_SoundTouch_getVersionString(JNIEnv *env, jobject thiz) {
    const char *verStr;

    LOGV("JNI call SoundTouch.getVersionString");

    // Call example SoundTouch routine
    verStr = SoundTouch::getVersionString();

    // gomp_tls storage bug workaround - see comments in _init_threading() function!
    // update: apparently this is not needed any more with concurrent Android SDKs
    // _init_threading(false);

    int threads = 0;
#pragma omp parallel
    {
#pragma omp atomic
        threads++;
    }
    LOGV("JNI thread count %d", threads);

    // return version as string
    return env->NewStringUTF(verStr);
}



extern "C" DLL_PUBLIC jlong
Java_me_shetj_ndk_soundtouch_SoundTouch_newInstance(JNIEnv *env, jobject thiz) {
    pSoundTouch = new SoundTouch();
    return 1;
}


extern "C" DLL_PUBLIC void
Java_me_shetj_ndk_soundtouch_SoundTouch_deleteInstance(JNIEnv *env, jobject thiz) {
    delete pSoundTouch;
    pSoundTouch = NULL;
}

extern "C" DLL_PUBLIC void
Java_me_shetj_ndk_soundtouch_SoundTouch_init(JNIEnv *env, jobject thiz, jint channels,
                                             jint sampleRate, jint tempo, jfloat pitch,
                                             jfloat speed) {
    if (pSoundTouch == NULL) {
        pSoundTouch = new SoundTouch();
    }
    pSoundTouch->setSampleRate(sampleRate);
    pSoundTouch->setChannels(channels);
    pSoundTouch->setTempo(tempo);
    pSoundTouch->setPitchSemiTones(pitch);
    pSoundTouch->setTempo(tempo);
    pSoundTouch->setRate(speed);
}


extern "C" DLL_PUBLIC void
Java_me_shetj_ndk_soundtouch_SoundTouch_setTempo(JNIEnv *env, jobject thiz,
                                                 jfloat tempo) {
    if (pSoundTouch == NULL) {
        return;
    }
    pSoundTouch->setTempo(tempo);
}


extern "C" DLL_PUBLIC void
Java_me_shetj_ndk_soundtouch_SoundTouch_setPitchSemiTones(JNIEnv *env, jobject thiz,
                                                          jfloat pitch) {
    if (pSoundTouch == NULL) {
        return;
    }
    pSoundTouch->setPitchSemiTones(pitch);
}


extern "C" DLL_PUBLIC void
Java_me_shetj_ndk_soundtouch_SoundTouch_setRate(JNIEnv *env, jobject thiz,
                                                jfloat speed) {
    if (pSoundTouch == NULL) {
        return;
    }
    pSoundTouch->setRate(speed);
}

extern "C" DLL_PUBLIC void
Java_me_shetj_ndk_soundtouch_SoundTouch_setRateChange(JNIEnv *env, jobject thiz,
                                                      jfloat rateChange) {
    if (pSoundTouch == NULL) {
        return;
    }
    pSoundTouch->setRateChange(rateChange);
}


extern "C" DLL_PUBLIC void
Java_me_shetj_ndk_soundtouch_SoundTouch_setTempoChange(JNIEnv *env, jobject thiz,
                                                       jfloat newTempo) {
    if (pSoundTouch == NULL) {
        return;
    }
    pSoundTouch->setTempoChange(newTempo);
}

extern "C" DLL_PUBLIC jstring
Java_me_shetj_ndk_soundtouch_SoundTouch_getErrorString(JNIEnv *env, jobject thiz) {
    jstring result = env->NewStringUTF(_errMsg.c_str());
    _errMsg.clear();

    return result;
}

extern "C" DLL_PUBLIC void
Java_me_shetj_ndk_soundtouch_SoundTouch_putSamples(JNIEnv *env, jobject thiz,
                                                   jshortArray samples, jint size) {

    try {
        jboolean isArrayCopied = false;
        jshort *samplesArray = env->GetShortArrayElements(samples, &isArrayCopied);
        int channel = pSoundTouch->numChannels();

        pSoundTouch->putSamples((SAMPLETYPE *) samplesArray, size/channel);

        if (isArrayCopied) {
            env->ReleaseShortArrayElements(samples, samplesArray, 0);
        }
    }
    catch (const runtime_error &e) {
        const char *err = e.what();
        // An exception occurred during processing, return the error message
        LOGV("JNI exception in SoundTouch::putSamples: %s", err);
        _setErrmsg(err);
    }
}

extern "C" DLL_PUBLIC jint
Java_me_shetj_ndk_soundtouch_SoundTouch_receiveSamples(JNIEnv *env, jobject thiz,
                                                       jshortArray output) {

    try {
        jboolean isArrayCopied = false;
        const jsize buf_size = env->GetArrayLength(output);
        int channel = pSoundTouch->numChannels();
        jshort *samplesArray = env->GetShortArrayElements(output, &isArrayCopied);
        int nSamples = pSoundTouch->receiveSamples((SAMPLETYPE *) samplesArray,buf_size/channel);
        if (nSamples == 0) {
            return 0;
        }
        if (isArrayCopied) {
            env->ReleaseShortArrayElements(output, samplesArray, 0);
        }
        return nSamples*channel;
    }
    catch (const runtime_error &e) {
        const char *err = e.what();
        // An exception occurred during processing, return the error message
        LOGV("JNI exception in SoundTouch::receiveSamples: %s", err);
        _setErrmsg(err);
        return 0;
    }

}

extern "C" DLL_PUBLIC jint
Java_me_shetj_ndk_soundtouch_SoundTouch_flush(JNIEnv *env, jobject thiz,
                                              jshortArray outArray) {
    try {
        if (pSoundTouch == NULL) {
            _setErrmsg("SoundTouch is NULL , u should init first");
            return -1;
        }
        pSoundTouch->flush(); //flush,然后处理最后的数据，可能处理不完，后续再测试一下，或者修改一下
        return 0;
    } catch (const runtime_error &e) {
        const char *err = e.what();
        // An exception occurred during processing, return the error message
        LOGV("JNI exception in SoundTouch::processFile: %s", err);
        _setErrmsg(err);
        return -1;
    }
}


extern "C" DLL_PUBLIC int
Java_me_shetj_ndk_soundtouch_SoundTouch_processFile(JNIEnv *env, jobject thiz,
                                                    jstring jinputFile, jstring joutputFile) {
    if (pSoundTouch == NULL) {
        _setErrmsg("SoundTouch is NULL , u should init first");
        return -1;
    }

    const char *inputFile = env->GetStringUTFChars(jinputFile, 0);
    const char *outputFile = env->GetStringUTFChars(joutputFile, 0);

    LOGV("JNI process file %s", inputFile);

//    /// gomp_tls storage bug workaround - see comments in _init_threading() function!
//    if (_init_threading(true)) return -1;

    try {
        _processFile(pSoundTouch, inputFile, outputFile);
    }
    catch (const runtime_error &e) {
        const char *err = e.what();
        // An exception occurred during processing, return the error message
        LOGV("JNI exception in SoundTouch::processFile: %s", err);
        _setErrmsg(err);
        return -1;
    }


    env->ReleaseStringUTFChars(jinputFile, inputFile);
    env->ReleaseStringUTFChars(joutputFile, outputFile);

    return 0;
}
