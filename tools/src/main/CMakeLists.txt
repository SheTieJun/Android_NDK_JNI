# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("tools")

#// ${PROJECT_SOURCE_DIR}  实际当前module 但是有些AS 会有问题
#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI})
# 1. 定义so库和头文件所在目录，方面后面使用
set(curl_lib ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI})


##当前 cpp目录的所有.c .cpp源文件,并且赋值到SRC_LIST
AUX_SOURCE_DIRECTORY(cpp/curl SRC_LIST_CURL)
AUX_SOURCE_DIRECTORY(cpp/json SRC_LIST_JSON)
AUX_SOURCE_DIRECTORY(cpp/my SRC_LIST_MY)

include_directories(${PROJECT_SOURCE_DIR}/cpp/curl)
include_directories(${PROJECT_SOURCE_DIR}/cpp/json)
include_directories(${PROJECT_SOURCE_DIR}/cpp/my)


add_definitions(-DDEBUG)

add_library( # Sets the name of the library.
        toolsCurl

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${SRC_LIST_CURL}
        ${SRC_LIST_JSON}
        ${SRC_LIST_MY}
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(
        log-lib

        log )

find_package(curl REQUIRED CONFIG)

find_package(jsoncpp REQUIRED CONFIG)

set_target_properties(toolsCurl PROPERTIES CXX_STANDARD 17)
target_compile_options(toolsCurl PRIVATE -Wall -Werror)

target_link_libraries(toolsCurl ${log-lib} curl::curl jsoncpp::jsoncpp )