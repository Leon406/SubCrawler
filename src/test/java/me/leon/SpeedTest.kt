package me.leon

import java.nio.charset.Charset
import me.leon.domain.LiteSpeed
import me.leon.domain.LiteSpeedConfig
import me.leon.support.fromJson
import me.leon.support.toFile
import me.leon.support.toJson
import me.leon.support.writeLine
import org.junit.jupiter.api.Test

class SpeedTest {
    @Test
    fun exec() {

        val config = "litespeed/config.json".toFile()

        config.outputStream().use { it.write(LiteSpeedConfig(NODE_ALL).toJson().toByteArray()) }
        val nodes = mutableMapOf<Int, String>()
        val oks = mutableListOf<Int>()

        val process =
            Runtime.getRuntime().exec("litespeed/lite --config litespeed/config.json --test Leon")

        process.errorStream.bufferedReader(Charset.defaultCharset()).use {
            it.forEachLine {
                val message = it.substring(19)
                if (message.contains("json options: ")) {
                    return@forEachLine
                }
                val liteSpeed = message.fromJson<LiteSpeed>()
                liteSpeed.servers?.forEach { nodes[it.id] = it.link }

                liteSpeed.ping()?.run { oks.add(liteSpeed.id) }
            }
        }

        println("${oks.size} $oks ")
        NODE_LITE.writeLine(nodes.filter { oks.contains(it.key) }.values.joinToString("\n"),false)
    }
}

const val INCREMENT = "█"
const val EMPTY = "*"

fun progress(cur: Int, max: Int, length: Int = 60, desc: String = "Progress") {
    require(max > 0)
    val p = cur * length / max
    print("\r$desc: [${INCREMENT.repeat(p)}${EMPTY.repeat(length - p)}]")
}
