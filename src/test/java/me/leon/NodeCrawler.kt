package me.leon

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.support.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.*

@Suppress("LongMethod")
class NodeCrawler {

    private val onlyGenerateAll = true

    private val maps = linkedMapOf<String, LinkedHashSet<Sub>>()

    private val typeMapper = mapOf(
        SS::class.java to (NODE_SS to "ss节点: "),
        SSR::class.java to (NODE_SSR to "ssr节点: "),
        V2ray::class.java to (NODE_V2 to "v2ray节点: "),
        Trojan::class.java to (NODE_TR to "trojan节点: "),
        Vless::class.java to (NODE_VLESS to "vless节点: "),
        Hysteria2::class.java to (NODE_HYS2 to "hysteria2节点: "),
    )


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
        val mergeSubs = mergeAllNodesUrl()
        println(mergeSubs.size)
        val prefix = SimpleDateFormat("MMddHH").format(Date())
        val countryMap = mutableMapOf<String, Int>()
        val errorList = mutableListOf<String>()
        POOL.writeLine()
        runBlocking {
            mergeSubs.map { sub ->
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
                            subList.firstOrNull()?.run { name = CUSTOM_INFO + name }
                            val data = subList.joinToString("\n") { it.toUri() }.b64Encode()
                            NODE_VLESS.writeLine()
                            writeData(clazz, data, subList)
                        }
                    it.filterIsInstance<Hysteria2>()
                        .groupBy { it.javaClass }
                        .forEach { (clazz, subList) ->
                            subList.firstOrNull()?.run { name = CUSTOM_INFO + name }
                            val data = subList.joinToString("\n") { it.toUri() }.b64Encode()
                            NODE_HYS2.writeLine()
                            writeData(clazz, data, subList)
                        }
                }
        }

        println("_________________ \n${errorList.joinToString(System.lineSeparator())}")
    }

    private fun nodeGroup() {
        val nodes = Parser.parseFromSub(NODE_OK)
        val regex = "[a-zA-Z]".toRegex()
        nodes.filterNot { it.SERVER.contains(regex) }
            .forEach {
                kotlin.runCatching {
                    it.name += " "+it.SERVER.ipScore().lvl
                }.onFailure {
                    println("${it.message}")
                }
        }
        nodeInfo.writeLine("\n**google ping有效节点: ${nodes.size}**")
        NODE_ALL.writeLine(
            nodes.filterNot { it is Vless || it is Hysteria2 }.joinToString("\n") { it.toUri() }.b64Encode(),
            false
        )

        nodes
            .groupBy { it.javaClass }
            .forEach { (clazz, subList) ->
                subList.firstOrNull()?.run { name = CUSTOM_INFO + name }
                val data = subList.joinToString("\n") { it.toUri() }.b64Encode()
                writeData(clazz, data, subList)
            }
    }

    private fun writeData(clazz: Class<out Sub>, data: String, subList: List<Sub>) {
        if (onlyGenerateAll && clazz != Vless::class.java && clazz != Hysteria2::class.java) {
            return
        }
        typeMapper[clazz]?.run {
            first.writeLine(data, false).also {
                println("$second${subList.size}".also { nodeInfo.writeLine("- $it") })
            }
        }
    }

    private fun mergeAllNodesUrl(): HashSet<String> {
        val sub1 = "$ROOT/pool/subs".readLines()
        val sublist = "$ROOT/pool/sublists".readLines()
        val sub2 =
            sublist
                .map { it.readFromNet() }
                .flatMap { it.split("\r\n|\n".toRegex()) }
                .distinct()
                .filterNot { it.startsWith("#") || it.trim().isEmpty() }
                .also {
                    println(it)
                    println("after ${it.size}")
                }
        val mergeSubs = (sub1 + sub2)
            .filterNot { it.trim().startsWith("#") || it.trim().isEmpty() }
            .toHashSet()
            .also { println("共有订阅源：${it.size.also { subCount = it }}") }

        return mergeSubs
    }

    companion object {
        private val nodeInfo = "$ROOT/info.md"
        const val CUSTOM_INFO = "防失效github SubCrawler"
        private var subCount = 0
        private var nodeCount = 0
    }
}
