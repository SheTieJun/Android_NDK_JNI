## tool
- 放一些公共类
- curl: c/c++ 网络请求库
- jsoncpp:c/c++中解析json很方便的库

## curl
项目地址：[https://github.com/curl/curl](https://github.com/curl/curl)

[curlAPI文档](https://curl.se/libcurl/c/)

### 已实现方法
```c++
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
    bool SetURL(const std::string strURL);

    //设置post的json 需要SetMethod（post）
    bool SetPostJson(std::string json);

    bool SetCertificate(std::string path);

    //忽略证书
    bool ignoreSSL();

    //设置http头
    bool AddHeader(std::string Key, std::string Value);

    //设置http头
    bool SetHttpHead(const string &strHttpHead);

    //设置返回数据
    bool SetResponseStr(std::string &strResponse);

    //设置HTTP请求cookie
    bool SetCookie(std::string lpCookie);

    //发送http请求
    CURLcode SendRequest(void);
```


### GET 实现
```c++
    string buf; //结果
    HttpModule get; //每次都创建新的
    get.SetMethod("get");
    get.SetTimeOut(300);
    get.ignoreSSL();//忽略证书验证
    get.SetResponseStr(buf);
    get.SetURL(utils::jString2String(env, url).c_str());
    CURLcode nRet = get.SendRequest();
    if (nRet == CURLE_OK) {
        return env->NewStringUTF(buf.c_str());
    } else {
        LOGE("HttpGet error code:%d", nRet);
        return env->NewStringUTF(("HttpGet error code:" + to_string(nRet)).c_str());
    }
```

### POST 实现
```c++
     string buf;
     HttpModule post; //每次都创建新的
     post.SetMethod("post");
     post.SetTimeOut(300);
     post.SetHttpHead("content-type:application/json;charset=UTF-8");
     post.ignoreSSL();//忽略证书验证
     post.SetPostJson(utils::jString2String(env, json));
     post.SetResponseStr(buf);
     post.SetURL(utils::jString2String(env, url).c_str());
     CURLcode nRet = post.SendRequest();
     if (nRet == CURLE_OK) {
         return env->NewStringUTF(buf.c_str());
     } else {
         LOGE("post json  error code:%d:%s", nRet, curl_easy_strerror(nRet));
         return env->NewStringUTF(("post json error code:" + to_string(nRet) + ":"
         + curl_easy_strerror(nRet)).c_str());
     }
```

## jsoncpp

c++中解析json很方便的库,项目地址：[https://github.com/open-source-parsers/jsoncpp](https://github.com/open-source-parsers/jsoncpp)

[jsoncpp文档](http://open-source-parsers.github.io/jsoncpp-docs/doxygen/index.html)

