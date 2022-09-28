package io.microshow.rxffmpeg

import android.text.TextUtils
import me.shetj.ffmpeg.FFmpegCommandList

internal object RxFFmpegCommandSupport {
    /**
     * 给视频加毛玻璃效果
     *
     * @param inputPath  输入视频文件
     * @param outputPath 输出视频文件
     * @param boxblur    blur效果调节，默认 "5:1"
     * @param isLog      true: 构建命令后 Log打印命令日志;  false :不打印命令日志
     * @return cmds
     */
    fun getBoxblur(
        inputPath: String,
        outputPath: String,
        boxblur: String?,
        isLog: Boolean
    ): Array<String> {
        val cmdlist = FFmpegCommandList()
        cmdlist.append("-i")
        cmdlist.append(inputPath)
        cmdlist.append("-vf")
        cmdlist.append("boxblur=" + (if (TextUtils.isEmpty(boxblur)) "5:1" else boxblur))
        cmdlist.append("-preset")
        cmdlist.append("superfast")
        cmdlist.append(outputPath)
        return cmdlist.build(isLog)
    }
}