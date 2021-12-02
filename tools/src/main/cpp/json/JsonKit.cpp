//
// Created by stj on 2021/11/30.
//

#include "JsonKit.h"
#include <jni.h>
#include <sstream>

JsonKit::JsonKit() {
}

JsonKit::~JsonKit() {
    if(root != NULL){
        root.clear();
        root = NULL;
    }
}

void JsonKit::addValue(std::string key, std::string value) {
    root[key.c_str()] = value.c_str();
}

void JsonKit::removeValue(std::string key) {
    root.removeMember(key.c_str());
}

void JsonKit::addValue(std::string key, bool value) {
    root[key.c_str()] = value;
}

void JsonKit::addValue(std::string key, int value) {
    root[key.c_str()] = value;
}

void JsonKit::addValue(std::string key, Json::Value value) {
    root[key.c_str()] = value;
}

std::string JsonKit::toJsonString() {
    Json::StreamWriterBuilder writerBuilder;
    std::ostringstream os;
    writerBuilder["emitUTF8"] = true;
    std::unique_ptr<Json::StreamWriter>jsWriter(writerBuilder.newStreamWriter());
    jsWriter->write(root,  &os);
    return os.str();
}

void JsonKit::createByString(std::string document) {
    Json::CharReaderBuilder reader;
    JSONCPP_STRING errs;
    std::unique_ptr<Json::CharReader> const jsonReader(reader.newCharReader());
    jsonReader->parse(document.c_str(), document.c_str()+document.length(), &root, &errs);
}
