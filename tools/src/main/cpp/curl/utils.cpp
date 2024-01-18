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

bool ToCppBool(jboolean value) {
    return value == JNI_TRUE;
}


jobject getApplication(JNIEnv *env) {
    jobject application = NULL;
    jclass activity_thread_clz = env->FindClass("android/app/ActivityThread");
    if (activity_thread_clz != NULL) {
        jmethodID get_Application = env->GetStaticMethodID(activity_thread_clz,
                                                           "currentActivityThread",
                                                           "()Landroid/app/ActivityThread;");
        if (get_Application != NULL) {
            jobject currentActivityThread = env->CallStaticObjectMethod(activity_thread_clz,
                                                                        get_Application);
            jmethodID getal = env->GetMethodID(activity_thread_clz, "getApplication",
                                               "()Landroid/app/Application;");
            application = env->CallObjectMethod(currentActivityThread, getal);
        }
        return application;
    }
    return application;
}


jstring getPackageName(JNIEnv *env) {
    jobject context = getApplication(env);
    if (context == NULL) {
        LOGE("context is null!");
        return NULL;
    }
    jclass activity = env->GetObjectClass(context);
    jmethodID methodId_pack = env->GetMethodID(activity, "getPackageName", "()Ljava/lang/String;");
    jstring name_str = static_cast<jstring >( env->CallObjectMethod(context, methodId_pack));
    return name_str;
}


extern "C"
JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_utils_Utils_getPackageName(JNIEnv *env, jclass clazz) {
    return getPackageName(env);
}


const char* RELEASE_SIGN = "3082036930820251a00302010202042394e404300d06092a864886f70d01010b05003065310b300906035504061302434e31"
                           "123010060355040813096775616e67646f6e673111300f060355040713085368656e5a68656e310e300c060355040a130573"
                           "6865746a310e300c060355040b1305736865746a310f300d060355040313067469656a756e301e170d313930393136303930"
                           "3233355a170d3434303930393039303233355a3065310b300906035504061302434e31123010060355040813096775616e67"
                           "646f6e673111300f060355040713085368656e5a68656e310e300c060355040a1305736865746a310e300c060355040b1305"
                           "736865746a310f300d060355040313067469656a756e30820122300d06092a864886f70d01010105000382010f003082010a"
                           "02820101008a81d7ec73bce4c5f5d5ecd7ac91dd642593de595a6e7d27e0ceb4bf14611f1d91e017d0a672348c4200f4e9f7"
                           "618b9164dbae09b42300734297d0380530f5b9e78d78852cae2de2dcd1ca32f77ad818cece69a0a4ee307c975e8f54f3ee37"
                           "63569b5855495809be0b9741b18109d7e714a53181928458e142b1e86dbf132279f819d3ec567c05b3619bad32a6b9ce83c9"
                           "5afee4cebc48d55889728d27939b2bfbf91ee603f21655bc631ef2ee41203995efbf6bf3032d322af86bcf42628cd08fc4c6"
                           "ffa33747f377cf68b3f22d2af89768eb9c0383e638e508e8be8f5329c09dcd82a4fd28a3ebf24e4ada44512678df39b10d68"
                           "15ed9e29cebb8632fba06d0203010001a321301f301d0603551d0e0416041432fcd02a36934784dc23b3fcab881ce9c724f8"
                           "b3300d06092a864886f70d01010b050003820101005d8e8ee1fde394fda9c9a550d47384b4dd3bf9206f75b3d1f03679a098"
                           "4121e08aa7b493adc827bf7d0de2853f530b38f04608ac79e7179b8e4e5d26f7cfa649f7e10caecb8583b9ed7a255b2e427f"
                           "b991f0107f30f0187f6bc99976a8a18c59508070dfb0785ba02fcd22d058c966a6b09e8e5e8b461d980a81a2c6943e0a99c7"
                           "87482f6e722eadf177c26de170cdd269acd60f718432d41b44dbc94106b3fe852212550df68bbc43b13cede4c44a5391ef1b"
                           "f33d48119c5cefaddb68c070467743661e753ef36637b34d61b209a401a11815a418dde79673331d77a35941ab82e0858096"
                           "59ec09d3c67c465aa47d89502b1cf8e555e33f5b386e815048e44c";
const char* RELEASE_PACKAGE = "me.shetj.sdk.ffmepg.demo";

extern "C"
JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_utils_Utils_verificationSign(JNIEnv *env, jclass clazz, jobject context) {

    jclass context_class = env->GetObjectClass(context);

    //context.getPackageManager()
    jmethodID methodId = env->GetMethodID(context_class, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject package_manager_object = env->CallObjectMethod(context, methodId);
    if (package_manager_object == NULL) {
        return NULL;
    }

    //context.getPackageName()
    methodId = env->GetMethodID(context_class, "getPackageName", "()Ljava/lang/String;");
    jstring package_name_string = (jstring)env->CallObjectMethod(context, methodId);
    if (package_name_string == NULL) {
        return NULL;
    }

    env->DeleteLocalRef(context_class);

    //PackageManager.getPackageInfo(Sting, int)
    jclass pack_manager_class = env->GetObjectClass(package_manager_object);
    methodId = env->GetMethodID(pack_manager_class, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    env->DeleteLocalRef(pack_manager_class);
    jobject package_info_object = env->CallObjectMethod(package_manager_object, methodId, package_name_string, 64);
    if (package_info_object == NULL) {
        return NULL;
    }

    env->DeleteLocalRef(package_manager_object);

    //PackageInfo.signatures[0]
    jclass package_info_class = env->GetObjectClass(package_info_object);
    jfieldID fieldId = env->GetFieldID(package_info_class, "signatures", "[Landroid/content/pm/Signature;");
    env->DeleteLocalRef(package_info_class);
    jobjectArray signature_object_array = (jobjectArray)env->GetObjectField(package_info_object, fieldId);
    if (signature_object_array == NULL) {
        return NULL;
    }
    jobject signature_object = env->GetObjectArrayElement(signature_object_array, 0);

    env->DeleteLocalRef(package_info_object);

    //Signature.toCharsString()
    jclass signature_class = env->GetObjectClass(signature_object);
    methodId = env->GetMethodID(signature_class, "toCharsString", "()Ljava/lang/String;");
    env->DeleteLocalRef(signature_class);
    jstring signature_string = (jstring) env->CallObjectMethod(signature_object, methodId);

    const char* c_sign = (char*)env->GetStringUTFChars(signature_string, 0);

    //签名一致  返回合法的 api key，否则返回错误
    if(strcmp(c_sign, RELEASE_SIGN)==0) {
        return (env)->NewStringUTF("sign true");
    } else {
        return (env)->NewStringUTF("sign error");
    }

}

extern "C"
JNIEXPORT jstring JNICALL
Java_me_shetj_sdk_utils_Utils_verificationPkg(JNIEnv *env, jclass clazz) {
    jstring packageName = getPackageName(env);
    const char* c_pkg = (char*)env->GetStringUTFChars(packageName, 0);
    if(strcmp(c_pkg, RELEASE_PACKAGE)==0) {
        return (env)->NewStringUTF("pkg true");
    } else {
        return (env)->NewStringUTF("pkg error");
    }
}