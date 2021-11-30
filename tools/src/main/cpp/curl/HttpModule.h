//
// Created by stj on 2021/11/30.
//

#ifndef ANDROID_NDK_HTTPPOSTMODULE_H
#define ANDROID_NDK_HTTPPOSTMODULE_H



#include <string>
#include "curl/curl.h"
#include <iostream>

using namespace std;

typedef size_t (*WriteFunc)(char *ptr, size_t size, size_t nmemb,
							void *userdata);

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

public:
	//设置超时
	bool SetTimeOut(unsigned short usSecond);

	//设置方法类型：get,post
	bool SetMethod(const string method);
	
	//设置 请求的url
	bool SetURL(const string strURL);

	bool SetPostJson(const string json);
	
	//设置http头
	bool SetHttpHead(const string &strHttpHead);

	//设置返回数据回调函数
	bool SetWriteFunction(WriteFunc pFunc);
	
	//发送http请求
	int SendRequest(void);

public:
	CURL *m_pCurl;
private:

};
//NetModule end

#endif //ANDROID_NDK_HTTPPOSTMODULE_H
