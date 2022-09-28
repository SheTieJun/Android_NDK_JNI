package me.shetj.ffmpeg

import android.util.Log

/**
 * 指令集合,
 * 默认会加上 ffmpeg -y，如果想去除默认的指令可以调用clearCommands()清除
 *
 */
class FFmpegCommandList {
    private val list = ArrayList<String>()

    /**
     * 清除命令集合
     */
    fun clearCommands(): FFmpegCommandList {
        list.clear()
        return this
    }

    /**
     * 追加命令
     *
     * @param s cmd
     * @return RxFFmpegCommandList
     */
    fun append(s: String): FFmpegCommandList {
        list.add(s)
        return this
    }

    /**
     * 构建命令
     *
     * @return -
     */
    fun build(): Array<String> {
        return list.toTypedArray()
    }

    /**
     * 构建命令
     *
     * @param isLog true:构建命令后 Log打印命令日志;  false :不打印命令日志
     * @return -
     */
    fun build(isLog: Boolean): Array<String> {
        val cmds: Array<String> = build()
        if (isLog) { //需要打印构建后的命令
            val cmdLogStr = StringBuilder()
            for (i in cmds.indices) {
                cmdLogStr.append(cmds[i])
                if (i < cmds.size - 1) {
                    cmdLogStr.append(" ")
                }
            }
            Log.e("TAG_FFMPEG", "cmd: $cmdLogStr")
        }
        return cmds
    }

    init {
        list.add("ffmpeg")
        list.add("-y")
    }
}