//
// Created by stj on 2021/11/29.
//

#include "logging.h"

#include <jni.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>

namespace curlssl {
    namespace logging {

        [[noreturn, gnu::format(printf, 2, 3)]] void FatalError(JNIEnv* env,
                                                                const char* fmt, ...) {
            va_list ap;
            va_start(ap, fmt);
            char* msg = nullptr;
            vasprintf(&msg, fmt, ap);
            va_end(ap);

            env->FatalError(msg);
            // env->FatalError() is specified to not return, but the function is not
            // annotated with the noreturn attribute. abort() just in case.
            abort();
        }

    }  // namespace logging
}