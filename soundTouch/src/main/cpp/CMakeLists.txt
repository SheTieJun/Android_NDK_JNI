# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

project("soundTouch")

#// ${PROJECT_SOURCE_DIR}  实际当前module 但是有些AS 会有问题
#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../libs/${ANDROID_ABI})

##当前../jni目录的所有.c .cpp源文件,并且赋值到SRC_LIST
AUX_SOURCE_DIRECTORY(soundtouch SRC_LIST)

# 2. 添加头文件目录
include_directories(soundtouch)
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_definitions(-DDEBUG)
add_library( # Sets the name of the library.
        soundTouch

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        soundtouch-jni.cpp
        ${SRC_LIST})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        soundTouch
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

target_link_options(soundTouch PRIVATE "-Wl,-z,max-page-size=16384")