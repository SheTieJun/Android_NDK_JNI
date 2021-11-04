//
// Created by stj on 2021/11/4.
// #include "TimeUtils.h" // 包含这个头文件
//	__TIC__(fwrite); // 耗时统计起始处
//    int size = fwrite(data, 1, len, file);
//    __TOC__(fwrite); // 耗时统计终止处，注意括号内的内容必须一致
//

#ifndef _TIME_UTILS_H
#define _TIME_UTILS_H

#ifdef DEBUG // 在 LogUtils.h 中定义了，也可以单独使用另一个宏开控制开关
#include "LogKit.h"
#include <ctime>
#include <chrono>

#define __TIC__(tag) auto time_##tag##_start = std::chrono::high_resolution_clock::now()
#define __TOC__(tag) auto time_##tag##_end = std::chrono::high_resolution_clock::now();\
        std::chrono::duration<double> ##tag##_time_span = std::chrono::duration_cast<std::chrono::duration<double>>(time_##tag##_end - time_##tag##_start);\
        LOGD(#tag " time: %.3f ms", ##tag##_time_span.count() * 1000)
#else
#define __TIC__(tag)
#define __TOC__(tag)
#endif // DEBUG


#endif //ANDROID_FFMPEG_TIMEUTILS_H
