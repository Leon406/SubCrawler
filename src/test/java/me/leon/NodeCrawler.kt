package me.leon

import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.support.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("LongMethod")
class NodeCrawler {

    private val maps = linkedMapOf<String, LinkedHashSet<Sub>>()

    /** 1.爬取配置文件对应链接的节点,并去重 2.同时进行可用性测试 tcping */
    @Test
    fun crawl() {
        // 1.爬取配置文件的订阅
        crawlNodes()
        SpeedTest().exec()
        nodeGroup()
    }

    /** 爬取配置文件数据，并去重写入文件 */
    @Test
    @Disabled
    fun crawlNodes() {
        val subs1 = "$ROOT/pool/subpool".readLines()
        val subs2 = "$ROOT/pool/subs".readLines()
        //        val subs3 = "$SHARE2/tmp".readLines()
        val sublist = "$ROOT/pool/sublists".readLines()
        val subs3 =
            sublist
                .map { it.readFromNet() }
                .flatMap { it.split("\r\n|\n".toRegex()) }
                .distinct()
                .also { println("before ${it.size}") }
                .filterNot { it.startsWith("#") || it.trim().isEmpty() }
                .also {
                    println(it)
                    println("after ${it.size}")
                }
        val subs = (subs1 + subs2 + subs3).toHashSet()
        println(subs.size)
        val prefix = SimpleDateFormat("MMdd").format(Date())
        val countryMap = mutableMapOf<String, Int>()
        val errorList = mutableListOf<String>()
        POOL.writeLine()
        runBlocking {
            subs
                .filterNot { it.trim().startsWith("#") || it.trim().isEmpty() }
                .also { println("共有订阅源：${it.size.also { subCount = it }}") }
                .map { sub ->
                    sub to
                        async(DISPATCHER) {
                            runCatching {
                                    val uri = sub
                                    Parser.parseFromSub(uri).also {
                                        println("$uri ${it.size}")
                                        if (it.size == 0) {
                                            errorList.add(uri)
                                        }
                                    }
                                }
                                .getOrElse {
                                    println("___parse failed $sub  ${it.message}")
                                    linkedSetOf()
                                }
                        }
                }
                .map { it.first to it.second.await() }
                .fold(linkedSetOf<Sub>()) { acc, linkedHashSet ->
                    maps[linkedHashSet.first] = linkedHashSet.second
                    acc.apply { acc.addAll(linkedHashSet.second) }
                }
                .also { nodeCount = it.size }
                .filterNot { it.methodUnSupported() }
                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort, 2000) } }
                .filter { it.second.await() > -1 }
                .map {
                    it.first.apply {
                        with(this.ipCountryZh()) {
                            countryMap[this] = countryMap.getOrDefault(this, 0) + 1
                            name = "${this}_$prefix${"%03d".format(countryMap[this])}"
                        }
                    }
                }
                .sortedBy { it.name }
                .also {
                    NODE_OK.writeLine(it.joinToString("\n") { it.toUri() }, false)
                    // 2.筛选可用节点
                    println("有效节点: ${it.size}")
                    nodeInfo.writeLine("更新时间${timeStamp()}\r\n", false)
                    nodeInfo.writeLine("${System.lineSeparator()}**总订阅: $subCount**")
                    nodeInfo.writeLine("**总节点: $nodeCount**")

                    it.filterIsInstance<Vless>()
                        .groupBy { it.javaClass }
                        .forEach { (clazz, subList) ->
                            subList.firstOrNull()?.run { name = customInfo + name }
                            val data = subList.joinToString("\n") { it.toUri() }.b64Encode()
                            NODE_VLESS.writeLine()
                            writeData(clazz, data, subList)
                        }
                }
        }

        println("_________________ \n${errorList.joinToString(System.lineSeparator())}")
    }

    private fun nodeGroup() {
        NODE_SS.writeLine()
        NODE_SSR.writeLine()
        NODE_V2.writeLine()
        NODE_TR.writeLine()
        val nodes = Parser.parseFromSub(NODE_OK)
        nodeInfo.writeLine("\n**google ping有效节点: ${nodes.size}**")
        NODE_ALL.writeLine(
            nodes.filterNot { it is Vless }.joinToString("\n") { it.toUri() }.b64Encode(),
            false
        )

        nodes
            .groupBy { it.javaClass }
            .forEach { (clazz, subList) ->
                subList.firstOrNull()?.run { name = customInfo + name }
                val data = subList.joinToString("\n") { it.toUri() }.b64Encode()
                writeData(clazz, data, subList)
            }
    }

    private fun writeData(clazz: Class<out Sub>, data: String, subList: List<Sub>) {
        when (clazz) {
            SS::class.java ->
                NODE_SS.writeLine(data).also {
                    println("ss节点: ${subList.size}".also { nodeInfo.writeLine("- $it") })
                }
            SSR::class.java ->
                NODE_SSR.writeLine(data).also {
                    println("ssr节点: ${subList.size}".also { nodeInfo.writeLine("- $it") })
                }
            V2ray::class.java ->
                NODE_V2.writeLine(data).also {
                    println("v2ray节点: ${subList.size}".also { nodeInfo.writeLine("- $it") })
                }
            Trojan::class.java ->
                NODE_TR.writeLine(data).also {
                    println("trojan节点: ${subList.size}".also { nodeInfo.writeLine("- $it") })
                }
            Vless::class.java ->
                NODE_VLESS.writeLine(data).also {
                    println("vless节点: ${subList.size}".also { nodeInfo.writeLine("- $it") })
                }
        }
    }

    companion object {
        private val nodeInfo = "$ROOT/info.md"
        const val customInfo = "防失效github SubCrawler"
        private var subCount = 0
        private var nodeCount = 0
    }
}
