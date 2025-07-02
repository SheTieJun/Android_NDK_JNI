## Android NDK

这个是一个测试项目

### [LAME](lame)

- Lame官网（lame.sourceforge.io/index.php）
- LAME是一个高质量的MPEG音频层III (MP3)编码器，在LGPL下授权

### [SoundTouch](soundTouch)

- soundtouch 的主要功能功能：变音。  通过：音速、音调
- 官网(http://www.surina.net/soundtouch)
- 下载地址(https://codeberg.org/soundtouch/soundtouch)
- 在LGPL下授权

### [FFmpeg](ffmpeg)

- 官网(https://www.ffmpeg.org/)
- https://github.com/FFmpeg/FFmpeg


### [curl 、openssl 和jsoncpp](tools)
#### curl：C中请求网络很好的库，项目地址：
- https://github.com/curl/curl
- [curlAPI文档](https://curl.se/libcurl/c/)

#### openssl
- [https://github.com/openssl/openssl](https://github.com/openssl/openssl)

#### jsoncpp：c++中解析json很方便的库，项目地址：
- https://github.com/open-source-parsers/jsoncpp
- [jsoncpp文档](http://open-source-parsers.github.io/jsoncpp-docs/doxygen/index.html)

### [WebRTC噪音模块](webrtc-ns)
- 源码部分：https://webrtc.googlesource.com/src/+/refs/heads/main/modules/audio_processing/ns
- 官网：https://webrtc.org/

### 兼容 16KB,兼容Android 15
```cmake
#3.13 之前：
target_link_libraries(xxx  "-Wl,-z,max-page-size=16384")
#3.13之后
target_link_options(xxx PRIVATE "-Wl,-z,max-page-size=16384")
```
在CMake 3.13之前，要向目标添加链接选项，您可以使用target_link_libraries命令，并将链接选项作为字符串传递给该命令。
在CMake 3.13及更高版本中，引入了target_link_options命令，使得向目标添加链接选项更加直观和一致。
 

