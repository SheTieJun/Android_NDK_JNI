//
// Created by stj on 2021/11/30.
//

#include "HttpModule.h"
#include "curl/curl.h"
#include "utils.h"

/**
 * curl API doc
 * @link:https://curl.se/libcurl/c/curl_easy_setopt.html
 */

HttpModule::HttpModule() :
		m_pCurl(NULL)
{
	m_pCurl = curl_easy_init();
	curl_easy_setopt(m_pCurl, CURLOPT_NOSIGNAL, 1L);
}
HttpModule::~HttpModule()
{
	if (headers) {
		curl_slist_free_all(headers);
		headers = NULL;
	}
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
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_TIMEOUT, usSecond);
	return  checkResult(m_curlCode);
}

bool HttpModule::SetURL(const std::string&  strURL)
{
	if (m_pCurl == nullptr)
		return false;
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_URL, strURL.c_str());
	return  checkResult(m_curlCode);
}
bool HttpModule::SetHttpHead(const string& strHttpHead)
{
	if (m_pCurl == nullptr)
		return false;
	headers = curl_slist_append(headers, strHttpHead.c_str());
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_HTTPHEADER, headers);
	return  checkResult(m_curlCode);
}
bool HttpModule::SetResponseStr(std::string & strResponse)
{
	if (m_pCurl == nullptr){
		LOGE("Failed to create CURL object");
		return false;
	}
	curl_easy_setopt(m_pCurl, CURLOPT_WRITEDATA,  (void*)&strResponse);
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_WRITEFUNCTION, HttpModule::receive_data);
    return  checkResult(m_curlCode);
}

CURLcode HttpModule::SendRequest() const {
	if (m_pCurl == nullptr)
		return CURLE_FAILED_INIT;
	return curl_easy_perform(m_pCurl);
}

bool HttpModule::SetMethod(const string method) {
	if (m_pCurl == nullptr){
		LOGE("Failed to create CURL object");
		return false;
	}
	if(utils::icasecompare(method,"get")){
		m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_HTTPGET, 1L);
		return  checkResult(m_curlCode);
	}
	if(utils::icasecompare(method,"post")){
		m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_POST, 1L);
		return  checkResult(m_curlCode);
	}
	if(utils::icasecompare(method,"put")){
		m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_CUSTOMREQUEST, "PUT");
		return  checkResult(m_curlCode);
	}
	if(utils::icasecompare(method,"delete")){
		m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_CUSTOMREQUEST, "DELETE");
		return  checkResult(m_curlCode);
	}
	if(utils::icasecompare(method,"head")){
		m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_NOBODY, 1L);
		return  checkResult(m_curlCode);
	}
	if(utils::icasecompare(method,"patch")){
		m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_CUSTOMREQUEST, "PATCH");
		return  checkResult(m_curlCode);
	}
	if(utils::icasecompare(method,"options")){
		m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_CUSTOMREQUEST, "OPTIONS");
		return  checkResult(m_curlCode);
	}
	LOGE("SetMethod: unsupported method: %s", method.c_str());
	return false;
}

bool HttpModule::SetPostJson(const std::string& szJson) {
	if (m_pCurl == nullptr){
		LOGE("Failed to create CURL object");
		return false;
	}
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_POSTFIELDS, szJson.c_str());
	curl_easy_setopt(m_pCurl, CURLOPT_POSTFIELDSIZE, szJson.length());
	return 	checkResult(m_curlCode);
}

bool HttpModule::SetCertificate(const std::string& cacert_path) const {
	if (m_pCurl == nullptr){
		LOGE("Failed to create CURL object");
		return false;
	}
	CURLcode curlCode = curl_easy_setopt(m_pCurl, CURLOPT_CAINFO, cacert_path.c_str());
	return checkResult(curlCode);
}

bool HttpModule::checkResult(CURLcode &curLcode) {
	if (curLcode != CURLE_OK) {
		LOGE("curl_easy_setopt failed: %s",curl_easy_strerror(curLcode));
		return false;
	}
	return true;
}

//数据接收回调
size_t HttpModule::receive_data(void *contents, size_t size, size_t nmemb, void *stream) {
	string *str = (string*)stream;
	(*str).append((char*)contents, size*nmemb);
	return size * nmemb;
}


bool HttpModule::ignoreSSL() {
	if (m_pCurl == NULL){
		LOGE("Failed to create CURL object");
		return false;
	}
	//不验证证书
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_SSL_VERIFYPEER, 0L);
	if (m_curlCode != CURLE_OK) {
		return  checkResult(m_curlCode);
	}
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_SSL_VERIFYHOST, 0L);
	if (m_curlCode != CURLE_OK) {
		return  checkResult(m_curlCode);
	}
	return true;
}

bool HttpModule::SetUserAgent(std::string userAgent) {
	if (userAgent.empty())
		return false;
	int nLen = strlen(userAgent.c_str());
	if (nLen == 0)
		return false;
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_USERAGENT, userAgent.c_str());
	return checkResult(m_curlCode);
}

bool HttpModule::SetPorts(long port) {
	if (port == m_nPort)
		return true;
	m_nPort = port;
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_PORT, m_nPort);
	return checkResult(m_curlCode);
}

bool HttpModule::SetConnectTimeout(int nSecond) {
	if (nSecond < 0)
		return false;
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_CONNECTTIMEOUT, nSecond);
	return checkResult(m_curlCode);
}

bool HttpModule::AddHeader(std::string Key, std::string Value) {
	int nLen1 = Key.size(), nLen2 =Value.size() ;
	if(nLen1 < 0 && nLen2 < 0){
		return false;
	}
	string strHeader(Key);
	strHeader.append(": ");
	strHeader.append(Value);
	headers = curl_slist_append(headers, strHeader.c_str());
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_HTTPHEADER, headers);
	return checkResult(m_curlCode);
}

bool HttpModule::SetCookie(const std::string& Cookie) {
	if (Cookie.empty())
		return false;
	m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_COOKIE, Cookie.c_str());
	return checkResult(m_curlCode);
}


