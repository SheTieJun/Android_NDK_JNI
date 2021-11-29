//
// Created by stj on 2021/11/29.
//

#ifndef ANDROID_NDK_JAVA_INTEROP_H
#define ANDROID_NDK_JAVA_INTEROP_H

#endif //ANDROID_NDK_JAVA_INTEROP_H
#pragma once

#include <jni.h>

#include <cstdlib>
#include <string>
#include <vector>

#include "logging.h"

namespace curlssl {
    namespace jni {

        template<typename... ToTypes>
        struct Convert;

        template<>
        struct Convert<std::string> {
            static std::string from(JNIEnv *env, const jstring &value) {
                typedef std::unique_ptr<const char[], std::function<void(const char *)>>
                        JniString;

                JniString cstr(env->GetStringUTFChars(value, nullptr), [=](const char *p) {
                    env->ReleaseStringUTFChars(value, p);
                });

                if (cstr == nullptr) {
                    logging::FatalError(env, "%s: GetStringUTFChars failed", __func__);
                }

                return cstr.get();
            }
        };

        template<>
        struct Convert<jobjectArray, jstring> {
            static jobjectArray from(JNIEnv *env, const std::vector <std::string> &value) {
                const char stringClassName[] = "java/lang/String";
                jclass stringClass = env->FindClass(stringClassName);
                if (stringClass == nullptr) {
                    logging::FatalError(env, "%s: FindClass(\"%s\") failed", __func__,
                                        stringClassName);
                }

                jobjectArray array =
                        env->NewObjectArray(value.size(), stringClass, nullptr);
                if (array == nullptr) {
                    logging::FatalError(env, "%s: NewObjectArray failed", __func__);
                }
                for (size_t i = 0; i < value.size(); ++i) {
                    jstring str = env->NewStringUTF(value[i].c_str());
                    if (str == nullptr) {
                        logging::FatalError(env, "%s: NewStringUTF(\"%s\") failed", __func__,
                                            value[i].c_str());
                    }
                    env->SetObjectArrayElement(array, i, str);
                }
                return array;
            }
        };

    }
}