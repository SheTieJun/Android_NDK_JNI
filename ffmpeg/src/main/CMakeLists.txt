# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
#指定 Cmake 需要的最低版本
cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.
project("android_ffmpeg")

#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../libs/${ANDROID_ABI})
# 1. 定义so库和头文件所在目录，方面后面使用
set(ffmpeg_lib_dir ${PROJECT_SOURCE_DIR}/../libs/${ANDROID_ABI})

##当前../jni目录的所有.c .cpp源文件,并且赋值到SRC_LIST
AUX_SOURCE_DIRECTORY(${PROJECT_SOURCE_DIR}/jni/include SRC_LIST)

# 2. 添加头文件目录
include_directories(${PROJECT_SOURCE_DIR}/jni/include)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


# 3. 添加ffmpeg相关的so库
#创建和命名该库，第一个参数是库的名字，例如取名为 native-lib，将会生成一个命名为 libnative-lib.so 的库。
#    第二个参数是指定库的类型，一般为 SHARED，即动态库（以 .so 为后缀），还有一种是静态库 STATIC，即静态库（以 .a 为后缀）。
#    第三个参数是指定该库使用的源文件路径。
#    使用多个 add_library() 命令，您可以为 CMake 定义要从其他源文件构建的更多库。
# set_target_properties 设置输出别名，所以，希望 "hello_static" 在输出时，不是"hello_static"，而是以"hello"的名字显示，故设置如下
add_library( avutil
        SHARED
        IMPORTED )
set_target_properties( avutil
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libavutil.so )

add_library( swresample
        SHARED
        IMPORTED )
set_target_properties( swresample
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libswresample.so )

add_library( avcodec
        SHARED
        IMPORTED )
set_target_properties( avcodec
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libavcodec.so )

add_library( avfilter
        SHARED
        IMPORTED)
set_target_properties( avfilter
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libavfilter.so )

add_library( swscale
        SHARED
        IMPORTED)
set_target_properties( swscale
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libswscale.so )

add_library( avformat
        SHARED
        IMPORTED)
set_target_properties( avformat
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libavformat.so )

add_library( postproc
        SHARED
        IMPORTED)
set_target_properties( postproc
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libpostproc.so )


add_library( avdevice
        SHARED
        IMPORTED)
set_target_properties( avdevice
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libavdevice.so )

# 配置目标so库编译信息
add_library( # Sets the name of the library.
        ffmpeg_lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${PROJECT_SOURCE_DIR}/jni/ffmpeg-lib.cpp
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
# 找到一个 NDK 的库，并且将这个库的路径存储在一个变量中。例如上例中是找到 NDK 中的 log 库（Android 特定的日志支持库），
# 并将其路径存储在 “log-lib” 变量中，在后面你就可以通过 “${log-lib}” 命令取出变量中的值了。
# 查找代码中使用到的系统库
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
# 关联库。将指定的库关联起来 。
# 指定库的库应该链接到你的目标库。您可以链接多个库，比如在这个构建脚本中定义的库、预构建的第三方库或系统库。
target_link_libraries( # Specifies the target library.
        ffmpeg_lib

        # 4. 连接 FFmpeg 相关的库
        avutil
        swresample
        avcodec
        avfilter
        swscale
        avformat
        postproc
        avdevice

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )

target_link_options(ffmpeg_lib PRIVATE "-Wl,-z,max-page-size=16384")