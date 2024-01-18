//
// Created by stj on 2021/11/30.
//

#ifndef ANDROID_NDK_HTTPPOSTMODULE_H
#define ANDROID_NDK_HTTPPOSTMODULE_H


#include <string>
#include "curl/curl.h"
#include <iostream>

using namespace std;

/*
 * 发送http请求
 * 使用开源curl库进行相应的实现
 * */
class HttpModule {
public:
    HttpModule();

    virtual ~HttpModule();

    static void Init();

    static void Cleanup();

    static size_t receive_data(void *contents, size_t size, size_t nmemb, void *stream);

/*
 * 自己进行拼接实现功能
 */
public:
    //设置超时
    bool SetTimeOut(unsigned short usSecond);

    //设置连接端口号
    bool SetPorts(long port);

    //设置连接超时（秒）
    bool SetConnectTimeout(int nSecond);

    //设置userAgent
    bool SetUserAgent(std::string userAgent);

    //设置方法类型：get,post
    bool SetMethod(const string method);

    //设置 请求的url
    bool SetURL(const std::string& strURL);

    //设置post的json 需要SetMethod（post）
    bool SetPostJson(const std::string& json);

    bool SetCertificate(const std::string& path) const;

    //忽略证书
    bool ignoreSSL();

    //设置http头
    bool AddHeader(std::string Key, std::string Value);

    //设置http头
    bool SetHttpHead(const string &strHttpHead);

    //设置返回数据
    bool SetResponseStr(std::string &strResponse);

    //设置HTTP请求cookie
    bool SetCookie(const std::string& lpCookie);

    //发送http请求
    CURLcode SendRequest() const;


public:
    CURL *m_pCurl;
    long m_nPort = 80;
    CURLcode m_curlCode = CURLE_OK;
    struct curl_slist *headers = NULL;
    static bool checkResult(CURLcode &curLcode) ;
};
//NetModule end

#endif //ANDROID_NDK_HTTPPOSTMODULE_H
