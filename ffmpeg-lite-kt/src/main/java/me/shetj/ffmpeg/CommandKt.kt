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
   return "ffmpeg -y -ss ${doubleToTs(startTime)} -i $input -t ${doubleToTs(endTime)} -write_xing 0 -id3v2_version 0 -c copy -avoid_negative_ts 1 $output"
       .convertToCommand()
}

/**
 * Build cut command
 *
 * @param input 输入文件
 * @param output 输出文件
 * @param startTime 开始时间  00:00.0
 * @param endTime 结束时间 00:00.0
 * @return
 */
fun buildCutCommand(input: String, output: String, startTime: String, endTime: String): Array<String> {
    return "ffmpeg -y -ss $startTime -i $input -t $endTime -write_xing 0 -id3v2_version 0 -c copy -avoid_negative_ts 1 $output"
        .convertToCommand()
}


private fun doubleToTs(seconds: Double): String {
    val mils = seconds%1000
    val second = seconds/1000
    return (getTwoDecimalsValue(second.toInt() / 60) + ":"
            + getTwoDecimalsValue(second.toInt()  % 60)) +"."+ mils.toInt()
}

private fun getTwoDecimalsValue(value: Int): String {
    return if (value in 0..9) {
        "0$value"
    } else {
        value.toString() + ""
    }
}


/**
 * Build merge command
 *
 * @param inputs 合并的文件
 * @param output 输出文件
 * @return
 */
fun buildMergeCommand(vararg inputs: String, output: String): Array<String> {
    val inputStringBuilder = StringBuilder()
    inputs.forEach {
        if (inputs.isEmpty() || !File(it).exists()) {
            return@forEach
        }
        // 加入首个
        if (inputStringBuilder.isEmpty()) {
            inputStringBuilder.append(it)
            return@forEach
        }
        // 后续
        inputStringBuilder.append("|$it")
    }
    return "ffmpeg -y -i concat:$inputStringBuilder -write_xing 0 -id3v2_version 0 -acodec copy $output"
        .convertToCommand()
}