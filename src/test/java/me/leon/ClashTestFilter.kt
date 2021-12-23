package me.leon

import me.leon.domain.ClashConnectLog
import me.leon.support.*
import org.junit.jupiter.api.Test

class ClashTestFilter {

    companion object {
        // private url
                const val URL = "https://v1.mk/3ZeDhzj"
        //        share url
     // const val URL = "https://v1.mk/gvHEd88"
//        const val URL = "https://suo.yt/2mf9ihW"
//        const val URL = "https://sub.id9.cc/sub?target=clash&new_name=true" +
//                "&url=https://raw.githubusercontent.com/Leon406/SubCrawler/main/sub/share/all" +
//                "&insert=false" +
//                "&config=https://raw.githubusercontent.com/ACL4SSR/ACL4SSR/master/Clash/config/ACL4SSR_Online.ini"


        // clash_win/Cache 目录下日志文件
        const val clashLogPath = "C:/Users/Leon/AppData/Roaming/clash_win/Cache/副本"
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
            .filter { it.value.hasSpeedTestHistory && nodeMap[it.key] != null }
            .map {
                nodeMap[it.key].apply {
                    this!!.name =
                        this.name
                            .removeFlags()
                            .replace(NodeCrawler.REG_AD, "")
                            .replace(NodeCrawler.REG_AD_REPLACE, NodeCrawler.customInfo)
                }
            }
            .also {
                println("_______ ${it.size}")
                NODE_ALL2.writeLine(it.joinToString("\n") { it!!.toUri() }.b64Encode(), false)
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
                writeData(t!!, data, u)
            }
    }

    private fun writeData(clazz: Class<Sub>, data: String, subList: List<Sub?>) {
        when (clazz) {
            SS::class.java ->
                NODE_SS2.writeLine(data, false).also {
                    println(
                        "ss节点: ${subList.size}".also {
                            NodeCrawler.nodeInfoLocal.writeLine("- $it")
                        }
                    )
                }
            SSR::class.java ->
                NODE_SSR2.writeLine(data, false).also {
                    println(
                        "ssr节点: ${subList.size}".also {
                            NodeCrawler.nodeInfoLocal.writeLine("- $it")
                        }
                    )
                }
            V2ray::class.java ->
                NODE_V22.writeLine(data, false).also {
                    println(
                        "v2ray节点: ${subList.size}".also {
                            NodeCrawler.nodeInfoLocal.writeLine("- $it")
                        }
                    )
                }
            Trojan::class.java ->
                NODE_TR2.writeLine(data, false).also {
                    println(
                        "trojan节点: ${subList.size}".also {
                            NodeCrawler.nodeInfoLocal.writeLine("- $it")
                        }
                    )
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
