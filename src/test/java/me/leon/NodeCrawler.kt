package me.leon

import java.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.support.*
import org.junit.jupiter.api.Test

class NodeCrawler {
    companion object {
        private val nodeInfo = "$ROOT/info.md"
        val nodeInfoLocal = "$ROOT/info2.md"
        private val adConfig = "$ROOT/ad.txt"
        private val adReplaceConfig = "$ROOT/adreplace.txt"
        const val customInfo = "防失效github.com/Leon406/Sub "
        private var subCount = 0
        private var nodeCount = 0
        val REG_AD by lazy { adConfig.toFile().readLines().joinToString("|").toRegex() }
        private val REG_AD_REPLACE by lazy {
            adReplaceConfig.toFile().readLines().joinToString("|").toRegex().also { println(it) }
        }
    }

    private val maps = linkedMapOf<String, LinkedHashSet<Sub>>()

    /** 1.爬取配置文件对应链接的节点,并去重 2.同时进行可用性测试 tcping */
    @Test
    fun crawl() {
        // 1.爬取配置文件的订阅
        crawlNodes()
        checkNodes()
        nodeGroup()
        //        IpFilterTest().reTestFailIps()
    }

    /** 爬取配置文件数据，并去重写入文件 */
    @Test
    fun crawlNodes() {
        val subs1 = "$ROOT/pool/subpool".readLines()
        val subs2 = "$ROOT/pool/subs".readLines()
        val unavailable = "$ROOT/pool/unavailable".readLines()
        //        val subs3 = "$SHARE2/tmp".readLines()
        val sublist = "$ROOT/pool/sublists".readLines()
        val subs3 =
            sublist
                .map { it.readFromNet() }
                .flatMap { it.split("\r\n|\n".toRegex()) }
                .distinct()
                .also { println("before ${it.size}") }
                .filterNot { unavailable.contains(it) || it.startsWith("#") || it.trim().isEmpty() }
                .also {
                    println(it)
                    println("after ${it.size}")
                }
        val subs = (subs1 + subs2 + subs3).filterNot { unavailable.contains(it) }.toHashSet()

        POOL.writeLine()
        runBlocking {
            subs
                .filterNot { it.trim().startsWith("#") || it.trim().isEmpty() }
                .also { println("共有订阅源：${it.size.also { subCount = it }}") }
                .map { sub ->
                    sub to
                        async(DISPATCHER) {
                            runCatching {
                                Parser.parseFromSub(sub).also { println("$sub ${it.size} ") }
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
                .sortedBy {
                    it
                        .apply {
                            name =
                                name.removeFlags()
                                    .replace(REG_AD, "")
                                    .replace(REG_AD_REPLACE, customInfo)
                        }
                        .toUri()
                }
                .also {
                    POOL.writeLine(
                        it.also { nodeCount = it.size }.joinToString("\n") { it.toUri() }
                    )
                }
        }
    }

    /** 节点可用性测试 */
    @Test
    fun checkNodes() {
        nodeInfo.writeLine()
        // 2.筛选可用节点
        NODE_OK.writeLine()
        val ok: HashSet<Sub>
        runBlocking {
            ok =
                Parser.parseFromSub(POOL)
                    .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort, 2000) } }
                    .filter { it.second.await() > -1 }
                    .also {
                        println(
                            "有效节点: ${it.size}".also {
                                nodeInfo.writeLine("更新时间${timeStamp()}\r\n")
                                nodeInfo.writeLine("**总订阅: $subCount**")
                                nodeInfo.writeLine("**总节点: $nodeCount**")
                                nodeInfo.writeLine("**$it**")
                            }
                        )
                    }
                    .map { it.first }
                    .toHashSet()
                    .also { NODE_OK.writeLine(it.joinToString("\n") { it.toUri() }) }
        }

        println("节点分布: ")
        maps.forEach { (t, u) -> (ok - (ok - u)).also { println("$t ${it.size}/${u.size}") } }
    }

    fun nodeGroup() {
        NODE_SS.writeLine()
        NODE_SSR.writeLine()
        NODE_V2.writeLine()
        NODE_TR.writeLine()

        Parser.parseFromSub(NODE_OK).groupBy { it.javaClass }.forEach { (clazz, subList) ->
            subList.firstOrNull()?.run { name = customInfo + name }
            val data = subList.joinToString("\n") { it.toUri() }.b64Encode()
            writeData(clazz, data, subList)
        }
    }

    private fun writeData(clazz: Class<Sub>, data: String, subList: List<Sub>) {
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
        }
    }

    /** 本地筛选节点 */
    @Test
    fun localUse() {
        runBlocking {
            NODE_OK.writeLine()
            nodeInfoLocal.writeLine()
            nodeInfoLocal.writeLine("更新时间${timeStamp()}\r\n")
            Parser.parseFromSub(POOL)
                .also { nodeInfoLocal.writeLine("**节点总数: ${it.size}**\n") }
                .filter { if (it is SSR) it.method != "rc4" else true }
                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort, 1000) } }
                .filter { it.second.await() > -1 }
                .also { nodeInfoLocal.writeLine("**有效节点数: ${it.size}**\n") }
                .map { it.first }
                .toHashSet()
                .also { NODE_OK.writeLine(it.joinToString("\n") { it.toUri() }) }
            NODE_SS2.writeLine()
            NODE_SSR2.writeLine()
            NODE_V22.writeLine()
            NODE_TR2.writeLine()

            Parser.parseFromSub(NODE_OK).groupBy { it.javaClass }.forEach { (clazz, subList) ->
                subList.firstOrNull()?.run { name = customInfo + name }
                val data = subList.joinToString("\n") { it.toUri() }.b64Encode()
                writePrivateData(clazz, data, subList)
            }
        }
    }

    private fun writePrivateData(t: Class<Sub>, data: String, u: List<Sub>) {
        when (t) {
            SS::class.java ->
                NODE_SS2.writeLine(data).also { nodeInfoLocal.writeLine("- ss节点: ${u.size}") }
            SSR::class.java ->
                NODE_SSR2.writeLine(data).also { nodeInfoLocal.writeLine("- ssr节点: ${u.size}") }
            V2ray::class.java ->
                NODE_V22.writeLine(data).also { nodeInfoLocal.writeLine("- v2ray节点: ${u.size}") }
            Trojan::class.java ->
                NODE_TR2.writeLine(data).also { nodeInfoLocal.writeLine("- trojan节点: ${u.size}") }
        }
    }

    @Test
    fun removeAd() {
        Parser.parseFromSub(NODE_OK)
            .map {
                it.also {
                    it.name =
                        it.name
                            .removeFlags()
                            .replace(REG_AD, "")
                            .replace(REG_AD_REPLACE, customInfo)
                }
            }
            .forEach { println(it.name) }
    }

    @Test
    fun nodeNationGroup() {
        Parser.parseFromSub(NODE_OK).groupBy { it.SERVER.ipCountryZh() }.forEach { (t, u) ->
            println("$t: ${u.size}")
            if (t == "UNKNOWN") println(u.map { it.SERVER })
        }
    }

    /**
     * 上面筛好节点后,进行第三方或者本地节点测速 进行节点分组 测速地址
     * - http://gz.cloudtest.cc/
     * - http://a.cloudtest.icu/
     */
    @Test
    fun availableSpeedTest() {
        Parser.parseFromSub(NODE_OK)
            .filterIsInstance<V2ray>()
            .map {
                if (it.name.isEmpty()) println(it.toUri())
                it
            }
            .chunked(200)
            .mapIndexed { index, list ->
                list.map(Sub::toUri).subList(0.takeIf { index == 0 } ?: 0, list.size).also {
                    println(it.joinToString("|"))
                }
            }
    }

    /**
     * ，将网站测速后的结果复制到 speedtest.txt F12 控制台输入以下内容,提取有效节点信息,默认提取速度大于1MB/s的节点 <code>
     * ```
     *     var rrs=document.querySelectorAll("tr.el-table__row");
     *     var ll=[];for(var i=0;i<rrs.length;i++){if(rrs[i].children[4].innerText.indexOf("MB")>0&&
     *     Number(rrs[i].children[4].innerText.replace("MB","")) >1){ll.push(rrs[i].children[1].innerText+"|"
     *     +rrs[i].children[4].innerText);}};ll.join("\n");
     * ```
     * </code> 最后进行分享链接生成
     */
    @Test
    fun speedTestResultParse() {
        val map =
            Parser.parseFromSub(NODE_OK).also { println(it.size) }.fold(
                    mutableMapOf<String, Sub>()
                ) { acc, sub -> acc.apply { acc[sub.name] = sub } }
        NODE_SS2.writeLine()
        NODE_SSR2.writeLine()
        NODE_V22.writeLine()
        NODE_TR2.writeLine()
        SPEED_TEST_RESULT
            .readLines()
            .asSequence()
            .distinct()
            .map { it.substringBeforeLast('|') to it.substringAfterLast('|') }
            .sortedByDescending { it.second.replace("Mb|MB".toRegex(), "").toFloat() }
            .filter { map[it.first] != null }
            .groupBy { map[it.first]!!.javaClass }
            .forEach { (t, u) ->
                val data =
                    u
                        .joinToString("\n") {
                            map[it.first]!!
                                .apply {
                                    name =
                                        name.replace(REG_AD, "")
                                            .removeFlags()
                                            .substringBeforeLast('|') + "|" + it.second
                                    //                        with(SERVER.ipCityZh()) {
                                    //                            name = (this?.takeUnless {
                                    // name.removeFlags().contains(it) }?.run { this + "_" }
                                    //                                ?: "") +
                                    // (name.removeFlags().substringBeforeLast('|') + "|" +
                                    // it.second)
                                    //                        }
                                }
                                .also { println(it.name) }
                                .toUri()
                        }
                        .b64Encode()
                when (t) {
                    SS::class.java -> NODE_SS2.writeLine(data).also { println("ss节点: ${u.size}") }
                    SSR::class.java ->
                        NODE_SSR2.writeLine(data).also { println("ssr节点: ${u.size}") }
                    V2ray::class.java ->
                        NODE_V22.writeLine(data).also { println("v2ray节点: ${u.size}") }
                    Trojan::class.java ->
                        NODE_TR2.writeLine(data).also { println("trojan节点: ${u.size}") }
                }
            }
    }

    @Test
    fun t() {
        println(System.lineSeparator())

        Parser.parseFromSub(POOL)
            .also { println(it.size) }
            .fold(mutableMapOf<String, Sub>()) { acc, sub -> acc.apply { acc[sub.name] = sub } }
            .forEach {
                it.key.replace(REG_AD, "").replace(REG_AD_REPLACE, customInfo).also { println(it) }
                //                println(REG_AD.replace(it.key, ""))
            }
    }
}
