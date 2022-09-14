package me.shetj.ffmpeg.kt

/**
 * Build command
 * 构建命令
 * @param commands
 * @return
 */
fun buildCommand(vararg commands:String): Array<String> {
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