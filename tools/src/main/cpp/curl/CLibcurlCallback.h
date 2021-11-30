//
// Created by stj on 2021/11/30.
//

#ifndef ANDROID_NDK_CLIBCURLCALLBACK_H
#define ANDROID_NDK_CLIBCURLCALLBACK_H
using namespace std;

class CLibcurlCallback
{
public:
    virtual void Progress(void* lpParam, double dTotal, double bLoaded) = 0;
};

enum LibcurlFlag
{
    Lf_None = 0,
    Lf_Download,
    Lf_Post,
    Lf_Get,
};


#endif //ANDROID_NDK_CLIBCURLCALLBACK_H
