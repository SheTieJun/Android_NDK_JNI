package me.shetj.ffmpeg

import java.io.File

/**
 * Build command
 * 构建命令
 * @param commands
 * @return
 */
fun buildCommand(vararg commands: String): Array<String> {
    return commands.toList().toTypedArray()
}

/**
 * Convert to command
 * 转换为命令
 * @return
 */
fun String.convertToCommand(): Array<String> {
    return this.split(" ").toTypedArray()
}


/**
 * Build command
 * 默认会加上 ffmpeg -y，如果想去除默认的指令可以调用clearCommands()清除所以当前命令
 * @param block 添加命令
 * @receiver
 * @return
 */
fun buildCommand(block: (FFmpegCommandList.() -> Unit)): Array<String> {
    return FFmpegCommandList().apply(block).build(true)
}

/**
 * Build cut :构建剪切命令
 *    -write_xing 0 -id3v2_version 0 可以避免剪切处出现错误字节，导致 H5 无法播放
 *    -avoid_negative_ts 精准度
 *    copy 指明只拷贝
 * @param input 输入文件
 * @param output 输出文件
 * @param startTime 开始时间默认 0
 * @param endTime 结束时间
 * @return
 */
fun buildCutCommand(input: String, output: String, startTime: Double = 0.0, endTime: Double): Array<String> {
   return "ffmpeg -y -ss $startTime -i $input -t $endTime -write_xing 0 -id3v2_version 0 -c copy -avoid_negative_ts 1 $output"
        .split(" ")
        .toTypedArray()
}


/**
 * Build merge command
 *
 * @param inputs 合并的文件
 * @param output 输出文件
 * @return
 */
fun buildMergeCommand(vararg inputs: String, output: String): Array<String> {
    val inputList = StringBuilder()
    inputs.forEach {
        if (inputs.isEmpty() || !File(it).exists()) {
            return@forEach
        }
        // 加入首个
        if (inputList.isEmpty()) {
            inputList.append(it)
            return@forEach
        }
        // 后续
        inputList.append("|$it")
    }
    return "ffmpeg -y -i concat:$inputList -write_xing 0 -id3v2_version 0 -acodec copy $output"
        .split(" ")
        .toTypedArray()
}