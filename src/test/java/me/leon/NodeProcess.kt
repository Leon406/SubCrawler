package me.leon

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.support.*
import org.junit.jupiter.api.Test

class NodeProcess {

    /** 本地筛选节点 */
    @Test
    fun localUse() {
        runBlocking {
            NODE_OK.writeLine()
            NodeCrawler.nodeInfoLocal.writeLine()
            NodeCrawler.nodeInfoLocal.writeLine("更新时间${timeStamp()}\r\n")
            listOf(NODE_SS, NODE_SSR, NODE_TR, NODE_V2)
                .fold(linkedSetOf<Sub>()) { acc, s ->
                    acc.apply { acc.addAll(Parser.parseFromSub(s)) }
                }
                .also { NodeCrawler.nodeInfoLocal.writeLine("**节点总数: ${it.size}**\n") }
                .filterNot { it.methodUnSupported() }
                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort, 3000) } }
                .filter { it.second.await() > -1 }
                .also { NodeCrawler.nodeInfoLocal.writeLine("**有效节点数: ${it.size}**\n") }
                .map { it.first }
                .toHashSet()
                .also { NODE_OK.writeLine(it.joinToString("\n") { it.toUri() }) }
            NODE_SS2.writeLine()
            NODE_SSR2.writeLine()
            NODE_V22.writeLine()
            NODE_TR2.writeLine()

            Parser.parseFromSub(NODE_OK)
                .also {
                    NODE_ALL2.writeLine(it.joinToString("\n") { it.toUri() }.b64Encode(), false)
                }
                .groupBy { it.javaClass }
                .forEach { (clazz, subList) ->
                    subList.firstOrNull()?.run { name = NodeCrawler.customInfo + name }
                    val data = subList.joinToString("\n") { it.toUri() }.b64Encode()
                    writePrivateData(clazz, data, subList)
                }
        }
    }

    private fun writePrivateData(t: Class<Sub>, data: String, u: List<Sub>) {
        when (t) {
            SS::class.java ->
                NODE_SS2.writeLine(data).also {
                    NodeCrawler.nodeInfoLocal.writeLine("- ss节点: ${u.size}")
                }
            SSR::class.java ->
                NODE_SSR2.writeLine(data).also {
                    NodeCrawler.nodeInfoLocal.writeLine("- ssr节点: ${u.size}")
                }
            V2ray::class.java ->
                NODE_V22.writeLine(data).also {
                    NodeCrawler.nodeInfoLocal.writeLine("- v2ray节点: ${u.size}")
                }
            Trojan::class.java ->
                NODE_TR2.writeLine(data).also {
                    NodeCrawler.nodeInfoLocal.writeLine("- trojan节点: ${u.size}")
                }
        }
    }

    @Test
    fun nodeNationGroup() {
        Parser.parseFromSub(NODE_OK)
            .filter { it.SERVER.contains("p3r.centaur.net") && it.SERVER.ipCountryZh() =="中国" }
            .groupBy {
                it.SERVER.ipCountryZh()
            }
            .forEach { (t, u) ->
                println("$t: ${u.size}")
                if (t == "UNKNOWN") println(u.map { it.SERVER })
            }
    }
}
