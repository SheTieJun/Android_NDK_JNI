//
// Created by stj on 2021/11/30.
//

#include "Clibcurl.h"
#include <iostream>

CLibcurl::CLibcurl(void)
        : m_pCurl(NULL)
        , m_nPort(80)
        , m_hFile(NULL)
        , m_curlCode(CURLE_OK)
        , m_lfFlag(Lf_None)
        , m_curlList(NULL)
        , m_pCallbackParam(NULL)
        , m_pCallback(NULL)
{
    m_pCurl = curl_easy_init();
    curl_easy_setopt(m_pCurl, CURLOPT_WRITEFUNCTION, WriteCallback);
    curl_easy_setopt(m_pCurl, CURLOPT_WRITEDATA, this);
}

CLibcurl::~CLibcurl(void)
{
    ClearHeaderList();
    curl_easy_cleanup(m_pCurl);
}

bool CLibcurl::SetPorts(long port)
{
    if (port == m_nPort)
        return true;
    m_nPort = port;
    m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_PORT, m_nPort);
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::SetTimeout(int nSecond)
{
    if (nSecond < 0)
        return false;
    m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_TIMEOUT, nSecond);
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::SetConnectTimeout(int nSecond)
{
    if (nSecond < 0)
        return false;
    m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_CONNECTTIMEOUT, nSecond);
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::SetUserAgent(std::string lpAgent)
{
    if (lpAgent.empty())
        return false;
    int nLen = strlen(lpAgent.c_str());
    if (nLen == 0)
        return false;
    m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_USERAGENT, lpAgent.c_str());
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::SetResumeFrom(long lPos)
{
    if (lPos < 0)
        return false;
    m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_RESUME_FROM, lPos);
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::SetResumeFromLarge(long llPos)
{
    if (llPos < 0)
        return false;
    m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_RESUME_FROM_LARGE, llPos);
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::AddHeader(std::string lpKey, std::string lpValue)
{
    int nLen1 = lpKey.size(), nLen2 =lpValue.size() ;
    assert(nLen1 > 0 && nLen2 > 0);
    string strHeader(lpKey);
    strHeader.append(": ");
    strHeader.append(lpValue);
    m_curlList = curl_slist_append(m_curlList, strHeader.c_str());
    m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_HTTPHEADER, m_curlList);
    return CURLE_OK == m_curlCode;
}

void CLibcurl::ClearHeaderList()
{
    if (m_curlList)
    {
        curl_slist_free_all(m_curlList);
        m_curlList = NULL;
    }
}

bool CLibcurl::SetCookie(std::string lpCookie)
{
    assert(!lpCookie.empty());
    m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_COOKIE, lpCookie.c_str());
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::SetCookieFile(string lpFilePath)
{
    assert(!lpFilePath.empty());
    m_curlCode = curl_easy_setopt(m_pCurl, CURLOPT_COOKIEFILE, lpFilePath.c_str());
    return CURLE_OK == m_curlCode;
}

const char* CLibcurl::GetError() const
{
    return curl_easy_strerror(m_curlCode);
}

void CLibcurl::SetCallback(CLibcurlCallback* pCallback, void* lpParam)
{
    m_pCallbackParam = lpParam;
    m_pCallback = pCallback;
}

bool CLibcurl::DownloadToFile(std::string lpUrl, std::string lpFile)
{
//    CURLcode code = curl_easy_setopt(m_pCurl, CURLOPT_URL, lpUrl.c_str());
    curl_easy_setopt(m_pCurl, CURLOPT_URL, lpUrl.c_str());
    curl_easy_setopt(m_pCurl, CURLOPT_NOPROGRESS, 0);
    curl_easy_setopt(m_pCurl, CURLOPT_PROGRESSFUNCTION, ProgressCallback);
    curl_easy_setopt(m_pCurl, CURLOPT_PROGRESSDATA, this);
    m_lfFlag = Lf_Download;
    //开始执行请求
    m_curlCode = curl_easy_perform(m_pCurl);
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::Post(string lpUrl, string lpData)
{
    assert(!lpData.empty());
    curl_easy_setopt(m_pCurl, CURLOPT_POST, 1);
    curl_easy_setopt(m_pCurl, CURLOPT_POSTFIELDS, lpData.c_str());
    //curl_easy_setopt(m_pCurl, CURLOPT_POSTFIELDSIZE, lpData);
    curl_easy_setopt(m_pCurl, CURLOPT_URL, lpUrl.c_str());
    m_lfFlag = Lf_Post;
    m_strRespons.clear();
    m_curlCode = curl_easy_perform(m_pCurl);
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::Post(string lpUrl, unsigned char* lpData, unsigned int nSize)
{
    assert(lpData != NULL && nSize > 0);
    curl_easy_setopt(m_pCurl, CURLOPT_POST, 1);
    curl_easy_setopt(m_pCurl, CURLOPT_POSTFIELDS, lpData);
    curl_easy_setopt(m_pCurl, CURLOPT_POSTFIELDSIZE, nSize);
    curl_easy_setopt(m_pCurl, CURLOPT_URL, lpUrl.c_str());
    m_lfFlag = Lf_Post;
    m_strRespons.clear();
    m_curlCode = curl_easy_perform(m_pCurl);
    return CURLE_OK == m_curlCode;
}

bool CLibcurl::Get(string lpUrl)
{
    assert(!lpUrl.empty());
    curl_easy_setopt(m_pCurl, CURLOPT_HTTPGET, 1);
    curl_easy_setopt(m_pCurl, CURLOPT_URL, lpUrl.c_str());
    curl_easy_setopt(m_pCurl, CURLOPT_FOLLOWLOCATION, 1);//支持重定向
    curl_easy_setopt(m_pCurl, CURLOPT_SSL_VERIFYPEER, 0L);
    curl_easy_setopt(m_pCurl, CURLOPT_SSL_VERIFYHOST, 0L);
    m_lfFlag = Lf_Get;
    m_strRespons.clear();
    m_curlCode = curl_easy_perform(m_pCurl);
    return CURLE_OK == m_curlCode;
}

const string& CLibcurl::GetRespons() const
{
    return m_strRespons;
}

const char* CLibcurl::GetResponsPtr() const
{
    return m_strRespons.c_str();
}

size_t CLibcurl::WriteCallback(void* pBuffer, size_t nSize, size_t nMemByte, void* pParam)
{
    //把下载到的数据以追加的方式写入文件(一定要有a，否则前面写入的内容就会被覆盖了)
//    CLibcurl* pThis = (CLibcurl*)pParam;
//    DWORD dwWritten = 0;
//    switch (pThis->m_lfFlag)
//    {
//        case Lf_Download://下载
//        {
//            if (pThis->m_hFile == INVALID_HANDLE_VALUE)
//                return 0;
//            if (!WriteFile(pThis->m_hFile, pBuffer, nSize*nMemByte, &dwWritten, NULL))
//                return 0;
//        }
//            break;
//        case Lf_Post://Post数据
//        case Lf_Get://Get数据
//        {
//            pThis->m_strRespons.append((const char*)pBuffer, nSize*nMemByte);
//            dwWritten = nSize * nMemByte;
//        }
//            break;
//        case Lf_None://未定义
//            break;
//    }
    return nSize * nMemByte;
}

size_t CLibcurl::HeaderCallback(void* pBuffer, size_t nSize, size_t nMemByte, void* pParam)
{
//    CLibcurl* pThis = (CLibcurl*)pParam;
    return 0;
}


int CLibcurl::ProgressCallback(void *pParam, double dltotal, double dlnow, double ultotal, double ulnow)
{
    CLibcurl* pThis = (CLibcurl*)pParam;
    if (pThis->m_pCallback)
    {
        pThis->m_pCallback->Progress(pThis->m_pCallbackParam, dltotal, dlnow);
    }
    return 0;
}
