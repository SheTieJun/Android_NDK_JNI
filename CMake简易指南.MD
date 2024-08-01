# CMake简易指南

CMake 详细说明参考官方文档 https://cmake.org/cmake/help/latest/index.html  CMake 版本，
API 有差异，请根据当前项目设置的最低版本来参考，高版本 API 在低版本无法使用。3.20之后的文档会标记该 API 的生效版本

> cmake 的优势不是性能和易用性，而是通用性与跨平台。


## 核心概念
- Target：目标单元，在CMake中，target是一个非常核心的概念，与其他现代化工程系统中的target类似，由 add_library/add_executable/add_custom_target 这三种方式生成，前两者很好理解，库和执行文件，第三种则比较特殊，通常用于执行自定义命令，如：调用protoc编译proto文件、编译完成时拷贝数据等等
- Generator：生成器，可以理解为编译系统，如：Ninja / Unix Makefiles / Visual Studio / Xcode
- cmake-commands：cmake 命令，通常写在 CMakeLists.txt / *.cmake 文件中调用的内置语法和函数都称之为 cmake 命令
- cmake-generator-expressions：生成器表达式，一种特殊的表达式，编译过程才生效
- Command-Line：cmake 控制台命令，即在终端控制台使用的命令，可以用于触发配置和编译之外，还可以用于文件操作以及解压缩等

## 版本选择
```cmake
cmake_minimum_required(VERSION 3.14)
```

## 目标编译系统
`G XXX`来指定翻译的目标编译系统

cmake 还支持跨平台编译系统Google Ninja，Ninja 属于目标编译系统，且效率很高，默认会根据系统处理器内核数来分配编译线程数，

默认编译系统:
- Linux :Unix Makefiles(Makefile)
- macOS ：Unix Makefiles(Makefile)
- Windows ：Visual Studio(.sln/.vcxproj)

## 常见语法

```cmake
# 指定最低cmake版本要求
cmake_minimum_required(VERSION 3.14)
# 创建项目标识
project(mylib)
# 添加名为mylib的目标，类型为动态库
add_library(mylib SHARED lib.cc lib.h)
# 添加名为myexe的目标，类型为可自行文件
add_executable(myexe main.cc)
# 为myexe添加对mylib的链接关联
target_link_libraries(myexe PUBLIC mylib)
```

add_subdirectory() 来关联子目录
```cmake
# 入口cmake文件
cmake_minimum_required(VERSION 3.10)
project(mylib)

option(build_with_test "是否编译测试代码" ON)

add_subdirectory(third_party/crbase ${CMAKE_BINARY_DIR}/crbase)
add_subdirectory(mylib)

if (build_with_test)
  add_subdirectory(test)
endif ()

########## 文件分隔符 ##########

# 源文件cmake配置
add_library(mylib SHARED lib.cc lib.h)
target_link_libraries(mulib PUBLIC crbase)

########## 文件分隔符 ##########

# 测试代码cmake配置
add_executable(test tests.cc)
target_link_libraries(test PUBLIC mylib)
```

## 平台差分
- CMAKE_SYSTEM_NAME：描述目标平台名称，可以理解为运行编译产物的操作系统，如： Windows | Darwin | Linux | Android | iOS，交叉编译时由工具链指定
- CMAKE_SYSTEM_PROCESSOR：描述目标处理器类型，交叉编译时由工具链指定
- CMAKE_HOST_SYSTEM_NAME：描述本地平台名称，可以理解为执行编译动作的操作系统，如： Windows | Darwin | Linux
- CMAKE_HOST_SYSTEM_PROCESSOR：描述本地处理器类型
```cmake
# 检测当前编译平台
# iOS没有官方工具链，三方工具链部分设置的名称为iOS，部分为IOS，此处统一改成小写
string(TOLOWER ${CMAKE_SYSTEM_NAME} __system_name)
if (${__system_name} STREQUAL "darwin")
  set(OS_MACOS TRUE)
elseif (${__system_name} STREQUAL "linux")
  set(OS_LINUX TRUE)
elseif (${__system_name} STREQUAL "windows")
  set(OS_WINDOWS TRUE)
elseif (${__system_name} STREQUAL "android")
  set(OS_ANDROID TRUE)
elseif (${__system_name} STREQUAL "ios")
  set(OS_IOS TRUE)
else ()
  message(FATAL_ERROR "Unsupported system : [${CMAKE_SYSTEM_NAME}]")
endif ()
unset(__system_name)
```
## 变量
```cmake
# 常规变量
# set(<variable> <value>... [PARENT_SCOPE])
set(NORMAL_VAR "normal variable")
unset(NORMAL_VAR)
# 缓存变量
# set(<variable> <value>... CACHE <type> <docstring> [FORCE])
set(CACHE_VAR "cache variable" CAHCE STRING "description")
# 环境变量
# set(ENV{<variable>} [<value>])
set(ENV{PATH} "$ENV{PATH}:${CMAKE_CURRENT_LIST_DIR}")
# 获取变量
message(STATUS "NORMAL_VAR = ${NORMAL_VAR}")
message(STATUS "CACHE_VAR = ${CACHE_VAR}")
message(STATUS "ENV_PATH = $ENV{PATH}")
```

- CMAKE_PROJECT_NAME：顶层项目名称，由project(xxx)指定
- PROJECT_NAME：多级项目时最后一个项目名称，由project(xxx)指定
- CMAKE_SOURCE_DIR：获取入口 cmake 文件所在路径，相对路径时建议使用 CMAKE_CURRENT_LIST_DIR
- CMAKE_CURRENT_LIST_DIR：获取当前 cmake 文件（可以是CMakeLists.txt，也可是xxx.cmake）所在路径，CMAKE_CURRENT_LIST_DIR 更为常用
- CMAKE_BINARY_DIR：顶层缓存路径，即执行 cmake 配置的路径
- CMAKE_CURRENT_BINARY_DIR：当前缓存路径，add_subdirectory(subproject subpath) 添加的 subpath
- PROJECT_BINARY_DIR：当前项目缓存路径，即最后一个 project 所在路径
- CMAKE_BUILD_TYPE：编译类型，常用有 Debug / Release，RelWithDebInfo / MinSizeRel不常用
- CMAKE_<LANG>_FLAGS：编译选项，<LANG> 为编译语言，如：CMAKE_C_FLAGS / CMAKE_CXX_FLAGS
- CMAKE_<LANG>_COMPILER：编译器信息

## 编译选项
```cmake
target_compile_features：编译特征支持检查
target_precompile_headers(3.16+)：预编译头文件
target_compile_definitions/add_definitions：宏定义
target_compile_options/add_compile_options：编译选项，通常用于修改编译器参数，需要搭配编译器一起使用
target_include_directories/include_directories：头文件查找路径
target_link_directories(3.13+)/link_directories：库文件查找路径
target_link_libraries(3.13+)/link_libraries：链接库名称
target_link_options(3.13+)/add_link_options(3.13+)：链接选项
```

```cmake

复制
# 分别添加`C11`和`C++14`特征支持检查
target_compile_features(mylib PUBLIC c_std_11 cxx_std_14)
# 添加预编译头文件，通常用于编译提速
target_precompile_headers(mylib PRIVATE precompile.h)
# 相当于-DFoo=1
target_compile_definitions(mylib PUBLIC -DFoo=1)
# 表达式编译选项
target_compile_options(mylib PUBLIC -fno-exceptions
  PRIVATE $<$<COMPILE_LANGUAGE:C>:${__CFLAGS_C}>            # C编译选项
  PRIVATE $<$<COMPILE_LANGUAGE:CXX>:${__CFLAGS_CXX}>        # C++编译选项
  PRIVATE $<$<CXX_COMPILER_ID:GNU>:${__CFLAGS_CXX_GNU}>     # GNU编译器生效
          $<$<CXX_COMPILER_ID:Clang>:${__CFLAGS_CXX_CLANG}> # Clang编译器生效
          $<$<CXX_COMPILER_ID:AppleClang>:${__CFLAGS_CXX_CLANG}>
)
# 添加头文件搜索路径，相当于 -Iinclude
target_include_directories(mylib PUBLIC include)
# 添加库文件查找路径，相当于 -Llib
target_link_directories(mylib PUBLIC lib)
# 添加库链接，相当于 -lfoo
target_link_libraries(mylib PUBLIC foo)
# 添加链接选项，启用lld链接器
target_link_options(mylib PUBLIC -fuse-ld=lld)
```