//
// Created by stj on 2021/11/30.
//

#ifndef ANDROID_NDK_UTILS_H
#define ANDROID_NDK_UTILS_H

#include <string>
#include "LogKit.h"
#include <jni.h>

using namespace std;
class utils {

public:
    //char比较
    static  bool icompare_pred(unsigned char a, unsigned char b);

    //字符串比较
    static  bool icasecompare(std::string const& a, std::string const& b);

    //jstring 转string
    static string jString2String(JNIEnv *env,jstring data);
};

#endif //ANDROID_NDK_UTILS_H
