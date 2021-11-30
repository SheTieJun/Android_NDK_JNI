#include <iostream>
#include <string>
#include <assert.h>
#include <curl/curl.h>
#include <curl/easy.h>

#include "CLibcurlCallback.h"

class CLibcurlCallbackEx
    : public CLibcurlCallback
{
public:
    virtual void Progress(void* lpParam, double dTotal, double dLoaded)
    {
        if (dTotal == 0.0)
            return;
        double bPercent = (dLoaded / dTotal) * 100;
        printf("下载进度：%0.2lf%%\n", bPercent);
    }

};


int test()
{
    //string url("http://t.weather.sojson.com/api/weather/city/101270101");
    //string buf;
    //CurlTools::HttpGet(url, buf, 300);

    //FILE *File = NULL;
    //fopen_s(&File, "test.json", "wb+");
    //if (File)
    //{
    //    fwrite(buf.c_str(), buf.length(), 1, File);
    //}
    //fclose(File);
    //File = NULL;
    return 0;
}






