//
// Created by stj on 2021/11/30.
//

#include "utils.h"




bool utils::icompare_pred(unsigned char a, unsigned char b)
{
    return std::tolower(a) == std::tolower(b);
}

bool utils::icasecompare(std::string const& a, std::string const& b)
{
    if (a.length() == b.length()) {
        return std::equal(b.begin(), b.end(),
                          a.begin(), icompare_pred);
    }
    else {
        return false;
    }
}

string utils::jString2String(JNIEnv *env,jstring jStr) {
    if (!jStr)
        return "";
    typedef std::unique_ptr<const char[], std::function<void(const char *)>>
            JniString;

    JniString cstr(env->GetStringUTFChars(jStr, nullptr), [=](const char *p) {
        env->ReleaseStringUTFChars(jStr, p);
    });

    if (cstr == nullptr) {
        LOGE( "jString2String: GetStringUTFChars failed");
    }
    return cstr.get();
}




