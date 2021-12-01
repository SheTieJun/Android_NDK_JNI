//
// Created by stj on 2021/11/30.
//

#ifndef ANDROID_NDK_JSONKIT_H
#define ANDROID_NDK_JSONKIT_H

#include <stdio.h>
#include <string>
#include "json/json.h"

using namespace std;

class JsonKit {

public:
    JsonKit();

    virtual ~JsonKit();


public:

    void createByString(std::string document);

    void addValue(std::string key, std::string value);

    void addValue(std::string key, bool value);

    void addValue(std::string key, int value);

    void addValue(std::string key, long value);

    void addValue(std::string key, Json::Value value);

    void removeValue(std::string key);

    std::string toJsonString();

public:
    Json::Value root;
};


#endif //ANDROID_NDK_JSONKIT_H
