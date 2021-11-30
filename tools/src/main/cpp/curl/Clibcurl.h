//
// Created by stj on 2021/11/30.
//

#ifndef ANDROID_NDK_CLIBCURL_H
#define ANDROID_NDK_CLIBCURL_H
#include <iostream>
#include <string>
#include <assert.h>
#include "curl/curl.h"
#include "curl/easy.h"
#include <jni.h>
#include "CLibcurlCallback.h"

class CLibcurl {
public:
    CLibcurl(void);

    ~CLibcurl(void);

    /******************************************************************************
    *封装类的外部调用接口
    */
    bool SetPorts(long port);                                            //设置连接端口号
    bool SetTimeout(int nSecond);                                        //设置执行超时（秒）
    bool SetConnectTimeout(int nSecond);                                //设置连接超时（秒）
    bool SetUserAgent(std::string lpAgent);                                    //设置用户代理
    bool SetResumeFrom(long lPos);                                        //设置断点续传起始位置
    bool SetResumeFromLarge(long llPos);                            //设置断点续传起始位置，针对大文件
    bool AddHeader(std::string lpKey, std::string lpValue);                        //添加自定义头
    void ClearHeaderList();                                                //清理HTTP列表头
    bool SetCookie(std::string lpCookie);                                    //设置HTTP请求cookie
    bool SetCookieFile(std::string lpFilePath);                                //设置HTTP请求cookie文件
    const char *GetError() const;                                        //获取错误详细信息
    void SetCallback(CLibcurlCallback *pCallback, void *lpParam);        //设置下载进度回调
    bool DownloadToFile(std::string lpUrl, std::string lpFile);                    //下载文件到磁盘
    bool Post(std::string lpUrl, std::string lpData);                                //Post 字符串数据
    bool Post(std::string lpUrl, unsigned char *lpData, unsigned int nSize); //Post 字符串或者二进制数据
    bool Get(string lpUrl);                                                //Get 请求
    const string &GetRespons() const;                                    //获取Post/Get请求返回数据
    const char *GetResponsPtr() const;                                    //获取Post/Get请求返回数据

protected:
    static size_t WriteCallback(void *pBuffer, size_t nSize, size_t nMemByte, void *pParam);

    static size_t HeaderCallback(void *pBuffer, size_t nSize, size_t nMemByte, void *pParam);

    static int
    ProgressCallback(void *pParam, double dltotal, double dlnow, double ultotal, double ulnow);

private:
    CURL *m_pCurl;
    long m_nPort;
    string m_hFile;
    CURLcode m_curlCode;
    string m_strRespons;
    LibcurlFlag m_lfFlag;
    curl_slist *m_curlList;
    void *m_pCallbackParam;
    CLibcurlCallback *m_pCallback;
};


#endif //ANDROID_NDK_CLIBCURL_H
