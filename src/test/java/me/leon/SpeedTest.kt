package me.leon

import me.leon.domain.LiteSpeed
import me.leon.domain.LiteSpeedConfig
import me.leon.support.*
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class SpeedTest {
    @Test
    fun exec() {
        val config = "litespeed/config.json".toFile().absoluteFile
        if (!NODE_OK.toFile().exists()) {
            println("---------------------- error")
            return
        }
        config.writeBytes(LiteSpeedConfig(NODE_OK).toJson().toByteArray())
        val nodes = mutableMapOf<Int, String>()
        val oks = mutableListOf<Int>()

        val process =
            Runtime.getRuntime().exec("litespeed/lite --config litespeed/config.json --test Leon")

        process.errorStream.bufferedReader(Charset.defaultCharset()).use {
            it.forEachLine {
                if (it.length <=19) {
                    return@forEachLine
                }
                val message = it.substring(19)
                if (message.contains("json options: ")) {
                    return@forEachLine
                }
                runCatching {
                    val liteSpeed = message.fromJson<LiteSpeed>()
                    liteSpeed.servers?.forEach { nodes[it.id] = it.link }
                    liteSpeed.ping()?.run { oks.add(liteSpeed.id) }
                }.getOrElse {
                    println("$message ${it.stackTraceToString()}")
                }
            }
        }

        println("${oks.size} $oks ")
        NODE_OK.writeLine(nodes.filter { oks.contains(it.key) }.values.joinToString("\n"), false)
    }
}

const val INCREMENT = "█"
const val EMPTY = "*"

fun progress(cur: Int, max: Int, length: Int = 60, desc: String = "Progress") {
    require(max > 0)
    val p = cur * length / max
    print("\r$desc: [${INCREMENT.repeat(p)}${EMPTY.repeat(length - p)}]")
}
