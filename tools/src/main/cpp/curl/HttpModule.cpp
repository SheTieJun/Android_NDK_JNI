//
// Created by stj on 2021/11/30.
//

#include "HttpModule.h"
#include "curl/curl.h"
#include "utils.h"

//https://curl.se/libcurl/c/curl_easy_setopt.html

HttpModule::HttpModule() :
		m_pCurl(NULL)
{
	m_pCurl = curl_easy_init();
	curl_easy_setopt(m_pCurl, CURLOPT_NOSIGNAL, 1L);
	curl_easy_setopt(m_pCurl, CURLOPT_SSL_VERIFYHOST, 2L);
}
HttpModule::~HttpModule()
{
	curl_easy_cleanup(m_pCurl);
	m_pCurl = NULL;
}

void HttpModule::Init()
{
	curl_global_init(CURL_GLOBAL_ALL);
	
}
void HttpModule::Cleanup()
{
	curl_global_cleanup();
	
}
bool HttpModule::SetTimeOut(unsigned short usSecond)
{
	if (m_pCurl == NULL)
		return false;
	int nRet = curl_easy_setopt(m_pCurl, CURLOPT_TIMEOUT, usSecond);
	if (nRet == CURLE_OK)
		return true;
	else
	{
		LOGE("SetTimeOut ERROR code=%d",nRet);
		return false;
	}
}
bool HttpModule::SetURL(const string  strURL)
{
	if (m_pCurl == NULL)
		return false;
	int nRet = curl_easy_setopt(m_pCurl, CURLOPT_URL, strURL.c_str());
	if (nRet == CURLE_OK)
		return true;
	else
	{
		LOGE( "SetURL ERROR code =%d",nRet);
		return false;
	}
}
bool HttpModule::SetHttpHead(const string& strHttpHead)
{
	if (m_pCurl == NULL)
		return false;
	curl_slist *plist = curl_slist_append(NULL, strHttpHead.c_str());
	int nRet = curl_easy_setopt(m_pCurl, CURLOPT_HTTPHEADER, plist);
	if (nRet == CURLE_OK)
		return true;
	else
	{
		LOGE("SetHttpHead ERROR code =%d",nRet);
		return false;
	}
}
bool HttpModule::SetWriteFunction(WriteFunc pFunc)
{
	if (m_pCurl == NULL)
		return false;
	curl_easy_setopt(m_pCurl, CURLOPT_WRITEDATA, NULL);
	int nRet = curl_easy_setopt(m_pCurl, CURLOPT_WRITEFUNCTION, pFunc);
	if (nRet == CURLE_OK)
		return true;
	else
	{
		LOGE("SetCallbackFunc ERROR code =%d",nRet);
		return false;
	}
}

int HttpModule::SendRequest(void) {
	if (m_pCurl == NULL)
		return -1;
	int nRet = curl_easy_perform(m_pCurl);
	if (nRet == CURLE_OK)
		return 0;
	else {
		return nRet;
	}
}

bool HttpModule::SetMethod(const string method) {
	if (m_pCurl == NULL)
		return false;
	if(utils::icasecompare(method,"get")){
		int nRet = curl_easy_setopt(m_pCurl, CURLOPT_HTTPGET, 1L);
		return nRet == CURLE_OK;
	}
	if(utils::icasecompare(method,"post")){
		int nRet = curl_easy_setopt(m_pCurl, CURLOPT_POST, 1L);
		return nRet == CURLE_OK;
	}
	LOGE("SetMethod: only support get and post");
	return false;
}

bool HttpModule::SetPostJson(const string json) {
	if (m_pCurl == NULL)
		return false;
	int nRet = curl_easy_setopt(m_pCurl, CURLOPT_POSTFIELDS, json.c_str());
	return nRet == CURLE_OK;
}

