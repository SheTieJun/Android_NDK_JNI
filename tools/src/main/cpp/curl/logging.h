//
// Created by stj on 2021/11/29.
//

#ifndef ANDROID_NDK_LOGGING_H
#define ANDROID_NDK_LOGGING_H

#pragma once

#include <jni.h>

namespace logging {

    //致命异常
    [[noreturn, gnu::format(printf, 2, 3)]] void FatalError(JNIEnv *env,
                                                            const char *fmt, ...);

}  // namespace logging

#endif //ANDROID_NDK_LOGGING_H
