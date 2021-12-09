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
                .filter { if (it is SSR) it.method != "rc4" else true }
                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort, 1000) } }
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
    fun removeAd() {
        Parser.parseFromSub(NODE_OK)
            .map {
                it.also {
                    it.name =
                        it.name
                            .removeFlags()
                            .replace(NodeCrawler.REG_AD, "")
                            .replace(NodeCrawler.REG_AD_REPLACE, NodeCrawler.customInfo)
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
                                        name.replace(NodeCrawler.REG_AD, "")
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
}
