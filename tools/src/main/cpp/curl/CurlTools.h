//
// Created by stj on 2021/11/30.
//

#ifndef ANDROID_NDK_CURLTOOLS_H
#define ANDROID_NDK_CURLTOOLS_H

#include <iostream>
#include <string>
#include <assert.h>
#include "Clibcurl.h"
#include "utils.h"


class CurlTools {
public:
    CurlTools() {}

    ~CurlTools() {}

public:
    static size_t receive_data(void *contents, size_t size, size_t nmemb, void *stream);

    // HTTP 下载文件的回掉函数
    static size_t writedata2file(void *ptr, size_t size, size_t nmemb, FILE *stream);

    // 文件下载接口
    static int download_file(const char *url, const char outfilename[FILENAME_MAX]);

    // http get 请求
    static CURLcode HttpGet(const std::string &strUrl, std::string &strResponse, int nTimeout);

    // htpp post 请求
    static CURLcode
    HttpPost(const std::string &strUrl, std::string szJson, std::string &strResponse, int nTimeout);
};

#endif //ANDROID_NDK_CURLTOOLS_H
