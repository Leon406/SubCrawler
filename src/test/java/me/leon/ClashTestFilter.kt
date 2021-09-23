package me.leon

import me.leon.domain.ClashConnectLog
import me.leon.support.*
import org.junit.jupiter.api.Test

class ClashTestFilter {

    companion object {
        //        tr  https://sub.cm/LtHIdxd
        //        ssr https://sub.cm/TTgunAH
        //        v2 https://sub.cm/w8HCJno
        //        ss https://sub.cm/FLJ17fi
        //        4 in one https://sub.cm/7lWFj2u
        //        4 in one https://sub.cm/9vJONwY
        const val URL = "https://suo.yt/LATLo63"

        // clash_win/Cache 目录下日志文件
        const val clashLogPath = "C:/Users/Leon/Desktop/f_0039d7 - 副本"
    }

    @Test
    fun parseClashLog() {

        val nodeMap =
            Parser.parseFromSub(URL).fold(mutableMapOf<String, Sub>()) { acc, sub ->
                acc.apply { acc[sub.name] = sub }
            }

        println("_______ 订阅节点数量 ${nodeMap.size}")
        NodeCrawler.nodeInfoLocal.writeLine(
            "更新时间${timeStamp()}${System.lineSeparator().repeat(2)}",
            false
        )
        NodeCrawler.nodeInfoLocal.writeLine("**节点总数: ${nodeMap.size}**\n")
        clashLogPath
            .readText()
            .fromJson<ClashConnectLog>()
            .proxies
            .filter { it.value.hasSpeedTestHistory }
            //            .also {
            //                it.forEach { (t, u) -> println("$t  ${u.history.last().delay}") }
            //                println()
            //            }
            .filter { nodeMap[it.key] != null }
            .map { nodeMap[it.key] }
            .also {
                println("_______ ${it.size}")
                NODE_ALL.writeLine(it.joinToString("\n") { it!!.toUri() }.b64Encode(), false)
                NodeCrawler.nodeInfoLocal.writeLine("**有效节点数: ${it.size}**\n")
                println(it.joinToString("\n") { it!!.toUri() })
            }
            .groupBy { it?.javaClass }
            .forEach { (t, u) ->
                u.firstOrNull()?.run {
                    name =
                        name.takeUnless { it.contains(NodeCrawler.customInfo) }
                            ?: (NodeCrawler.customInfo + name)
                }
                val data = u.joinToString("\n") { it!!.toUri() }.b64Encode()
                //                println(u.joinToString("\n") { it!!.name })
                when (t) {
                    SS::class.java ->
                        NODE_SS2.writeLine(data, false).also {
                            NodeCrawler.nodeInfoLocal.writeLine("- ss节点: ${u.size}")
                        }
                    SSR::class.java ->
                        NODE_SSR2.writeLine(data, false).also {
                            NodeCrawler.nodeInfoLocal.writeLine("- ssr节点: ${u.size}")
                        }
                    V2ray::class.java ->
                        NODE_V22.writeLine(data, false).also {
                            NodeCrawler.nodeInfoLocal.writeLine("- v2ray节点: ${u.size}")
                        }
                    Trojan::class.java ->
                        NODE_TR2.writeLine(data, false).also {
                            NodeCrawler.nodeInfoLocal.writeLine("- trojan节点: ${u.size}")
                        }
                }
            }
    }

    @Test
    fun speedTestResultParse() {
        val map =
            Parser.parseFromSub(URL).fold(mutableMapOf<String, Sub>()) { acc, sub ->
                acc.apply {
                    //                    println(sub.name)
                    acc[sub.name] = sub
                }
            }
        println(map)
        SPEED_TEST_RESULT
            .readLines()
            .asSequence()
            .distinct()
            .map { it.substringBeforeLast('|') to it.substringAfterLast('|') }
            .sortedByDescending { it.second.replace("Mb|MB".toRegex(), "").toFloat() }
            .filter { map[it.first] != null }
            .also {
                val data =
                    it
                        .joinToString("\n") {
                            map[it.first]!!
                                .apply {
                                    name =
                                        name.replace(NodeCrawler.REG_AD, "")
                                            .removeFlags()
                                            .substringBeforeLast('|') + "|" + it.second
                                }
                                //                                .also { println(it.name) }
                                .toUri()
                        }
                        .b64Encode()

                println(data)
                println()
            }
            .groupBy { map[it.first]!!.javaClass }
            .forEach { (_, u) ->
                val data =
                    u
                        .joinToString("\n") {
                            map[it.first]!!
                                .apply {
                                    name =
                                        name.replace(NodeCrawler.REG_AD, "")
                                            .removeFlags()
                                            .substringBeforeLast('|') + "|" + it.second
                                }
                                //                                .also { println(it.name) }
                                .toUri()
                        }
                        .b64Encode()

                println(u.size)
                println(data)
            }
    }
}
