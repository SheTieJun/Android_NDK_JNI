//
// Created by stj on 2021/12/1.
//

#include "JsonKit.h"
#include <jni.h>
#include "utils.h"

extern "C" JNIEXPORT void JNICALL
Java_me_shetj_sdk_json_JsonKit_test(JNIEnv *env, jclass clazz) {
    JsonKit tool = JsonKit();
//    string s = " {\n"
//               "    \t\"bool\" : false,\n"
//               "    \t\"int\" : 1,\n"
//               "    \t\"key\" : true,\n"
//               "    \t\"long\" : 1\n"
//               "    }";
//    tool.createByString(s);


    Json::Value boolo;
    boolo["test"] = false;
//
    tool.addValue("key", "value");
    tool.addValue("int", 1);
    tool.addValue("bool", false);
    tool.addValue("boolo", boolo);
    std::string jsonString;
    jsonString = tool.toJsonString();
    LOGI("%s", jsonString.c_str());
    tool.removeValue("boolo");
    jsonString = tool.toJsonString();
    LOGI("%s", jsonString.c_str());
}